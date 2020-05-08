 package pl.psnc.dl.wf4ever.model.RO;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import pl.psnc.dl.wf4ever.common.Builder;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ReadWrite;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 /**
  * A resource map of a folder. Such resource map is considered an internal part of the research object aggregating the
  * folder but it is not aggregated by it.
  * 
  * @author piotrekhol
  * 
  */
 public class FolderResourceMap extends ResourceMap {
 
     /**
      * Constructor.
      * 
      * @param user
      *            user creating the instance
      * @param dataset
      *            custom dataset
      * @param useTransactions
      *            should transactions be used. Note that not using transactions on a dataset which already uses
      *            transactions may make it unreadable.
      * @param folder
      *            folder to be described
      * @param uri
      *            resource map URI
      */
     public FolderResourceMap(UserMetadata user, Dataset dataset, boolean useTransactions, Folder folder, URI uri) {
         super(user, dataset, useTransactions, folder, uri);
     }
 
 
     /**
      * Constructor.
      * 
      * @param user
      *            user creating the instance
      * @param folder
      *            folder to be described
      * @param uri
      *            resource map URI
      */
     public FolderResourceMap(UserMetadata user, Folder folder, URI uri) {
         super(user, folder, uri);
     }
 
 
     /**
      * Add metadata specific to a folder entry (not to a proxy).
      * 
      * @param entry
      *            a folder entry
      */
     public void saveFolderEntryData(FolderEntry entry) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             Individual entryInd = model.createIndividual(entry.getUri().toString(), RO.FolderEntry);
             Literal name = model.createLiteral(entry.getEntryName());
             model.removeAll(entryInd, RO.entryName, null);
             model.add(entryInd, RO.entryName, name);
             Individual folderInd = model.createIndividual(getFolder().getUri().toString(), RO.Folder);
             Resource proxyForR = model.getResource(entry.getProxyFor().getUri().toString());
             proxyForR.addProperty(ORE.isAggregatedBy, folderInd);
             folderInd.addProperty(ORE.aggregates, proxyForR);
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Return a URI of an RDF graph that describes the folder. If folder URI is null, return null. If folder URI path is
      * empty, return folder.rdf (i.e. example.com becomes example.com/folder.rdf). Otherwise use the last path segment
      * (i.e. example.com/foobar/ becomes example.com/foobar/foobar.rdf). RDF/XML file extension is used.
      * 
      * @param folder
      *            folder described by the resource map
      * @return RDF graph URI or null if folder URI is null
      */
     public static URI generateResourceMapUri(Folder folder) {
         if (folder.getUri() == null) {
             return null;
         }
         String base;
         if (folder.getUri().getPath() == null || folder.getUri().getPath().isEmpty()) {
             base = "/folder";
         } else if (folder.getUri().getPath().equals("/")) {
             base = "folder";
         } else {
             String[] segments = folder.getUri().getRawPath().split("/");
             base = segments[segments.length - 1];
         }
         return folder.getUri().resolve(base + ".rdf");
     }
 
 
     /**
      * Identify ro:FolderEntries, aggregated by the folder.
      * 
      * @return a set of resources (not loaded)
      */
     public Map<URI, FolderEntry> extractFolderEntries() {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Map<URI, FolderEntry> entries = new HashMap<>();
             String queryString = String
                     .format(
                         "PREFIX ore: <%s> PREFIX ro: <%s> SELECT ?entry ?resource ?name WHERE { ?entry a ro:FolderEntry ; ro:entryName ?name ; ore:proxyFor ?resource ; ore:proxyIn <%s> . }",
                         ORE.NAMESPACE, RO.NAMESPACE, aggregation.getUri().toString());
 
             Query query = QueryFactory.create(queryString);
             QueryExecution qe = QueryExecutionFactory.create(query, model);
             try {
                 ResultSet results = qe.execSelect();
                 while (results.hasNext()) {
                     QuerySolution solution = results.next();
                     RDFNode r = solution.get("resource");
                     URI rUri = URI.create(r.asResource().getURI());
                     AggregatedResource proxyFor = getFolder().getResearchObject().getAggregatedResources().get(rUri);
                     RDFNode e = solution.get("entry");
                     URI eUri = URI.create(e.asResource().getURI());
                     RDFNode nameNode = solution.get("name");
                     String name = nameNode.asLiteral().getString();
                     FolderEntry entry = builder.buildFolderEntry(eUri, proxyFor, getFolder(), name);
                     entries.put(entry.getUri(), entry);
                 }
             } finally {
                 qe.close();
             }
             return entries;
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     public Folder getFolder() {
         return (Folder) aggregation;
     }
 
 
     /**
      * Create and save a new folder resource map.
      * 
      * @param builder
      *            model instance builder
      * @param folder
      *            folder described by the resource map
      * @param resourceMapUri
      *            resource map URI
      * @return a new folder resource map instance
      */
     public static FolderResourceMap create(Builder builder, Folder folder, URI resourceMapUri) {
         FolderResourceMap map = builder.buildFolderResourceMap(resourceMapUri, folder);
         map.save();
         return map;
     }
 
 
     @Override
     public void save() {
         super.save();
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             Resource manifestRes = model.createResource(getResearchObject().getManifest().getUri().toString());
             Individual roInd = model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
             model.add(roInd, ORE.isDescribedBy, manifestRes);
 
            model.createIndividual(aggregation.getUri().toString(), RO.Folder);
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     @Override
     public void delete() {
         super.delete();
     }
 
 
     /**
      * Find the research object that aggregates the folder described by this resource map.
      * 
      * @return a research object instance or null if not found
      */
     public ResearchObject extractResearchObject() {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Resource folderR = model.getResource(getFolder().getUri().toString());
             if (folderR == null) {
                 return null;
             }
             Resource roR = folderR.getPropertyResourceValue(ORE.isAggregatedBy);
             if (roR == null) {
                 return null;
             }
             return builder.buildResearchObject(URI.create(roR.getURI()));
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     @Override
     public ResearchObject getResearchObject() {
         return getFolder().getResearchObject();
     }
 }
