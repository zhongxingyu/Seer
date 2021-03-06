 package degraphmalizr;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.google.common.base.Optional;
 import com.google.common.collect.Iterables;
 import com.google.inject.Provider;
 import com.tinkerpop.blueprints.*;
 import configuration.*;
 import configuration.javascript.JSONUtilities;
 import degraphmalizr.jobs.*;
 import elasticsearch.ESUtilities;
 import exceptions.DegraphmalizerException;
 import graphs.GraphQueries;
 import graphs.ops.Subgraph;
 import graphs.ops.SubgraphManager;
 import modules.bindingannotations.*;
 import org.elasticsearch.action.get.GetResponse;
 import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.client.Client;
 import org.slf4j.Logger;
 import trees.*;
 
 import javax.inject.Inject;
 import java.util.*;
 import java.util.concurrent.*;
 
 class Result implements DegraphmalizeResult
 {
     final boolean succes;
     final DegraphmalizeAction action;
     final DegraphmalizerException exception;
 
     public Result(DegraphmalizeAction action, DegraphmalizerException ex)
     {
         this.succes = false;
         this.action = action;
         this.exception = ex;
     }
 
     @Override
     public boolean succes()
     {
         return succes;
     }
 
     @Override
     public DegraphmalizeAction action()
     {
         return action;
     }
 
     @Override
     public Exception exception()
     {
         return exception;
     }
 }
 
 public class Degraphmalizer implements Degraphmalizr
 {
 	@Inject
 	Logger log;
 	
 	final protected Client client;
 
 	final protected Graph graph;
     final protected SubgraphManager subgraphmanager;
 
 	final protected ExecutorService dirtyDocs;
     final protected ExecutorService service;
     final protected ExecutorService updateDocs;
 
     protected final ESUtilities updater;
 
     protected final Provider<Configuration> cfgProvider;
 
     final ObjectMapper objectMapper = new ObjectMapper();
 
 
 
 	@Inject
 	public Degraphmalizer(Client client, SubgraphManager subgraphmanager, Graph graph,
                           @Update ExecutorService updateDocs,
                           @Fetches ExecutorService service,
                           @Dirty ExecutorService dirtyDocs,
                           ESUtilities esUtilities,
                           Provider<Configuration> configProvider)
 	{
         this.service = service;
         this.dirtyDocs = dirtyDocs;
         this.updateDocs = updateDocs;
 
         this.graph = graph;
         this.subgraphmanager = subgraphmanager;
 		this.client = client;
         this.cfgProvider = configProvider;
         this.updater = esUtilities;
 	}
 
 
     @Override
     public DegraphmalizeAction degraphmalize(ID id, DegraphmalizeStatus callback) throws DegraphmalizerException
     {
         // get configuration for index
         final IndexConfig indexCfg = cfgProvider.get().indices().get(id.index());
         if(indexCfg == null)
             throw new DegraphmalizerException("No configuration for index '" + id.index() + "'");
 
         // get configuration for this type
         final TypeConfig typeCfg = indexCfg.types().get(id.type());
         if(typeCfg == null)
             throw new DegraphmalizerException("No configuration for type '" + id.type() + "'");
 
         // construct the action object
         final DegraphmalizeAction action = new DegraphmalizeAction(typeCfg, id, callback);
 
         // convert object into task and queue
         action.result = updateDocs.submit(degraphmalizeJob(action));
 
         return action;
     }
 
 	public Callable<JsonNode> degraphmalizeJob(final DegraphmalizeAction action)
 	{
 		return new Callable<JsonNode>()
 		{
 			@Override
 			public JsonNode call() throws Exception
 			{
 				try
 				{
                     final ID ID = action.id();
 					log.info("Processing request '{}', for id={}", action.hash().toString(), ID);
 
                     // get the source document from Elasticsearch
                     final GetResponse resp = client.prepareGet(ID.index(), ID.type(), ID.id()).execute().get();
 
                     if(!resp.exists())
                         throw new DegraphmalizerException("Document does not exist");
 
                     if(resp.version() != ID.version())
                         throw new DegraphmalizerException("Query expired, current version is " + resp.version());
 
                     // alright, we have the right source document, so let's start processing.
 
                     // first we parse the document into a JsonNode tree
                     action.setDocument(objectMapper.readTree(resp.getSourceAsString()));
 
                     // then we extract the graph elements from it
                     log.debug("Extracting graph elements");
                     final Subgraph sg = subgraphmanager.createSubgraph(ID);
                     action.type().extract(action, sg);
                     log.debug("Completed extraction of graph elements");
                     subgraphmanager.commitSubgraph(sg);
                     log.debug("Committed subgraph to graph");
 
 
                     // we now start traversals for each walk do find documents affected by this change
                     final Vertex root = GraphQueries.findVertex(graph, ID);
                     if (root == null)
                         // TODO this shouldn't occur, because the subgraph implicitly commits a vertex to the graph
                         throw new DegraphmalizerException("No node for document " + ID);
 
                     final ArrayList<RecomputeAction> affectedDocs = new ArrayList<RecomputeAction>();
 
                     // we add ourselves as the first job in the list
                     affectedDocs.add(new RecomputeAction(action, action.type(), root));
 
                     for(WalkConfig walkCfg : action.type().walks().values())
                     {
                         // by walking in the opposite direction we can find all nodes affected by this change
                         final Direction direction = walkCfg.direction().opposite();
 
                         // traverse graph in the other direction, starting at the root
                         log.debug("Computing tree in direction {}, starting at {}", direction, root);
                         final Tree<Pair<Edge,Vertex>> tree = GraphQueries.childrenFrom(graph, root, direction);
 
                         if(log.isDebugEnabled())
                         {
                             final int size = Iterables.size(Trees.bfsWalk(tree));
                             log.debug("Found tree of size {}", size);
                         }
 
                         // create "dirty document" messages for each node in the tree
                         for (Pair<Edge, Vertex> affectedVertex : Trees.bfsWalk(tree))
                         {
                             // skip the root of the tree, ie. ourselves:
                             if(affectedVertex.b.equals(root))
                                 continue;
 
                             affectedDocs.add(new RecomputeAction(action, action.type(), affectedVertex.b));
                         }
                     }
 
                     log.debug("Action {} caused recompute for {} documents", action.hash(), affectedDocs.size());
 
 
 
                     // create Callable from the actions
                     // TODO call 'recompute started' for each action
                     final ArrayList<Callable<Optional<IndexResponse>>> jobs = new ArrayList<Callable<Optional<IndexResponse>>>();
                     for(RecomputeAction r : affectedDocs)
                         jobs.add(recomputeDocument(r));
 
                     // recompute all affected documents and wait for results
                     // TODO call 'recompute finished' for each action
                     final List<Future<Optional<IndexResponse>>> results = dirtyDocs.invokeAll(jobs);
 
                     // TODO get rid of this innerclass
                     final DegraphmalizeResult result = new DegraphmalizeResult()
                     {
                         @Override
                         public boolean succes()
                         {
                             return true;
                         }
 
                         @Override
                         public DegraphmalizeAction action()
                         {
                             return action;
                         }
 
                         @Override
                         public Exception exception()
                         {
                             return null;
                         }
                     };
 
                     action.status().complete(result);
 
                     // TODO refactor action class
                     final Optional<IndexResponse> ourResult = results.get(0).get();
                     final ObjectNode n = objectMapper.createObjectNode();
                     if(ourResult.isPresent())
                     {
                         n.put("succes", true);
                         n.put("version", ourResult.get().version());
                     }
                     else
                         n.put("succes", false);
 
                     return n;
 				}
                 catch (final DegraphmalizerException e)
 				{
                     final DegraphmalizeResult result = new Result(action, e);
 
                     // report failure
                     action.status().exception(result);
 
                     // rethrow, this will captured by Future<>.get()
                     throw e;
 				}
                 catch (final Exception e)
                 {
                     final DegraphmalizerException ex = new DegraphmalizerException("Unknown exception occurred", e);
                     final DegraphmalizeResult result = new Result(action, ex);
 
                     // report failure
                     action.status().exception(result);
 
                     // rethrow, this will captured by Future<>.get()
                     throw e;
                 }
 			}
 		};
 	}
 
     public Callable<Optional<IndexResponse>> recomputeDocument(final RecomputeAction action)
     {
         return new Callable<Optional<IndexResponse>>()
         {
             @Override
             public Optional<IndexResponse> call() throws Exception
             {
                 try
                 {
                     // here we collect all the results of the reductions
                     final HashMap<String, JsonNode> results = new HashMap<String, JsonNode>();
 
                     // during the traverals, we collect the root document (the one we are expanding)
                     GetResponse root = null;
 
                     // ideally, this is handled in a monad, but with this boolean we keep track of failures
                     boolean isAbsent = false;
 
                     for(Map.Entry<String,WalkConfig> w : action.typeConfig.walks().entrySet())
                     {
                         // walk graph
                         final Tree<Pair<Edge,Vertex>> tree =
                                 GraphQueries.childrenFrom(graph, action.root, w.getValue().direction().opposite());
 
                         // write size information to log
                         if(log.isDebugEnabled())
                         {
                             final int size = Iterables.size(Trees.bfsWalk(tree));
                             log.debug("Retrieving {} documents from ES", size);
                         }
 
                         // get all documents in the tree from Elasticsearch (in parallel)
                         final Tree<Optional<GetResponse>> doc_tree =
                                 Trees.pmap(service, updater.documentGetter(), tree);
 
                         // the tree has Optional.absent values when versions for instance don't match up
 
                         if(doc_tree.value().isPresent())
                             root = doc_tree.value().get();
 
                         // if some value is absent from the tree, abort the computation
                         final Optional<Tree<GetResponse>> fullTree = Trees.optional(doc_tree);
 
                         // TODO split various failure modes
                         if(!fullTree.isPresent())
                         {
                             isAbsent = true;
                             break;
                         }
 
                         // reduce each property to a value based on the walk result
                         for(final Map.Entry<String,? extends PropertyConfig> p : w.getValue().properties().entrySet())
                             results.put(p.getKey(), p.getValue().reduce(fullTree.get()));
                     }
 
                     // something failed, so we abort the whole re-computation
                     if(isAbsent)
                     {
                         // TODO refactor the action, it should have the ID
                         log.debug("Some results were absent, aborting re-computation for " + GraphQueries.getID(action.root));
                         return Optional.absent();
                     }
 
                     // store the new document
                     final JsonNode rawDocument = objectMapper.readTree(root.getSourceAsString());
 
                     // preprocess document using javascript
                     final JsonNode doc = action.typeConfig.transform(rawDocument);
 
                     if(!doc.isObject())
                     {
                         log.debug("Root of processed document is not a JSON object (ie. it's a value), we are not adding the reduced properties");
                         return Optional.absent();
                     }
 
                     // add the results to the document
                     for(Map.Entry<String,JsonNode> e : results.entrySet())
                         ((ObjectNode)doc).put(e.getKey(), e.getValue());
 
                     // write the result document to the target index
                     final String targetIndex = action.typeConfig.targetIndex();
                     final String targetType = action.typeConfig.targetType();
                     final ID ID = GraphQueries.getID(action.root);
                     final String id = ID.id();
 
                     // write the source version to the document
                     ((ObjectNode)doc).put("_fromSource", JSONUtilities.toJSON(objectMapper, ID));
 
                     // write document to Elasticsearch
                     final String documentSource = doc.toString();
                     final IndexResponse ir = client.prepareIndex(targetIndex, targetType, id).setSource(documentSource)
                             .execute().actionGet();
 
                     log.debug("Written /{}/{}/{}, version={}", new Object[]{targetIndex, targetType, id, ir.version()});
 
                     log.debug("Content: {}", documentSource);
                     return Optional.of(ir);
                 }
                 catch (Exception e)
                 {
                     log.error("Exception in recomputation phase, {}", e);
                     return Optional.absent();
                 }
             }
         };
     }
 }
