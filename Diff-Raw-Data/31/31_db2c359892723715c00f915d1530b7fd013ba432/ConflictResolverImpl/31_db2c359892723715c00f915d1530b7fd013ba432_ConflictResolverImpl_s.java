 package cz.cuni.mff.odcleanstore.conflictresolution.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.odcleanstore.conflictresolution.CRContext;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictClusterFilter;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolutionPolicy;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolver;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolverFactory;
 import cz.cuni.mff.odcleanstore.conflictresolution.EnumAggregationErrorStrategy;
 import cz.cuni.mff.odcleanstore.conflictresolution.EnumCardinality;
 import cz.cuni.mff.odcleanstore.conflictresolution.ResolutionFunction;
 import cz.cuni.mff.odcleanstore.conflictresolution.ResolutionFunctionRegistry;
 import cz.cuni.mff.odcleanstore.conflictresolution.ResolutionStrategy;
 import cz.cuni.mff.odcleanstore.conflictresolution.ResolvedStatement;
 import cz.cuni.mff.odcleanstore.conflictresolution.ResolvedStatementFactory;
 import cz.cuni.mff.odcleanstore.conflictresolution.URIMapping;
 import cz.cuni.mff.odcleanstore.conflictresolution.exceptions.ConflictResolutionException;
 import cz.cuni.mff.odcleanstore.conflictresolution.exceptions.ResolutionFunctionNotRegisteredException;
 import cz.cuni.mff.odcleanstore.conflictresolution.impl.util.CRUtils;
 import cz.cuni.mff.odcleanstore.conflictresolution.impl.util.EmptyMetadataModel;
 import cz.cuni.mff.odcleanstore.conflictresolution.impl.util.GrowingArray;
 import cz.cuni.mff.odcleanstore.conflictresolution.resolution.AllResolution;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 
 /**
  * Implementation of the RDF conflict resolution algorithm.
  * URIs are translated according to given URI mappings and conflict resolved by
  * application of conflict resolution functions according to resolution settings. 
  * @author Jan Michelfeit
  */
 public class ConflictResolverImpl implements ConflictResolver {
     private static final Logger LOG = LoggerFactory.getLogger(ConflictResolverImpl.class);
 
     /** Default prefix of graph names where resolved quads are placed. */ 
     public static final String DEFAULT_RESOLVED_GRAPHS_URI_PREFIX = ODCS.NAMESPACE + "CR/";
     
     /** Default conflict resolution strategy. */
     public static final ResolutionStrategy DEFAULT_RESOLUTION_STRATEGY = new ResolutionStrategyImpl(
             AllResolution.getName(),
             EnumCardinality.MANYVALUED,
             EnumAggregationErrorStrategy.RETURN_ALL);
     private static final int EXPECTED_REDUCTION_FACTOR = 3;
 
     private Model metadata = null;
     private URIMapping uriMapping = EmptyURIMapping.getInstance();
     private ResolvedStatementFactory resolvedStatementFactory =
             new ResolvedStatementFactoryImpl(DEFAULT_RESOLVED_GRAPHS_URI_PREFIX);
     private ConflictResolutionPolicy conflictResolutionPolicy;
     private ResolutionFunctionRegistry resolutionFunctionRegistry;
     private ConflictClusterFilter conflictClusterFilter;
     
     private CRContextImpl context;
 
     // private StatementFilter statementFilter;
 
     /**
      * Creates a new instance with default resolution function registry and default settings for conflict
      * resolution.
      */
     public ConflictResolverImpl() {
         this(ConflictResolverFactory.createInitializedResolutionFunctionRegistry());
     }
     
     /**
      * Creates a new instance with the given resolution function registry and defaults for other conflict 
      * resolution settings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry) {
         this.resolutionFunctionRegistry = resolutionFunctionRegistry;
     }
 
     /**
      * Creates a new instance with the given settings and no URI mappings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      * @param conflictResolutionPolicy conflict resolution parameters
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry,
             ConflictResolutionPolicy conflictResolutionPolicy) {
         this(resolutionFunctionRegistry);
         this.conflictResolutionPolicy = conflictResolutionPolicy;
     }
 
     /**
      * Creates a new instance with the given settings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      * @param conflictResolutionPolicy conflict resolution parameters
      * @param uriMapping mapping of URIs to their canonical URI (based on owl:sameAs links)
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry,
             ConflictResolutionPolicy conflictResolutionPolicy, URIMapping uriMapping) {
         this(resolutionFunctionRegistry, conflictResolutionPolicy);
         this.uriMapping = uriMapping;
     }
 
     /**
      * Creates a new instance with the given settings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      * @param conflictResolutionPolicy conflict resolution parameters
      * @param uriMapping mapping of URIs to their canonical URI (based on owl:sameAs links)
      * @param metadata additional metadata for use by resolution functions (e.g. source quality etc.)
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry, 
             ConflictResolutionPolicy conflictResolutionPolicy, URIMapping uriMapping, Model metadata) {
         this(resolutionFunctionRegistry, conflictResolutionPolicy, uriMapping);
         this.metadata = metadata;
     }
 
     /**
      * Creates a new instance with the given settings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      * @param conflictResolutionPolicy conflict resolution parameters
      * @param uriMapping mapping of URIs to their canonical URI (based on owl:sameAs links)
      * @param metadata additional metadata for use by resolution functions (e.g. source quality etc.)
      * @param resolvedGraphsURIPrefix prefix of graph names where resolved quads are placed
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry,
             ConflictResolutionPolicy conflictResolutionPolicy, URIMapping uriMapping, Model metadata,
             String resolvedGraphsURIPrefix) {
         this(resolutionFunctionRegistry, conflictResolutionPolicy, uriMapping, metadata);
         if (resolvedGraphsURIPrefix != null) {
             resolvedStatementFactory = new ResolvedStatementFactoryImpl(resolvedGraphsURIPrefix);
         }
     }
     
     /**
      * Creates a new instance with the given settings.
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      * @param conflictResolutionPolicy conflict resolution parameters
      * @param uriMapping mapping of URIs to their canonical URI (based on owl:sameAs links)
      * @param metadata additional metadata for use by resolution functions (e.g. source quality etc.)
      * @param resolvedGraphsURIPrefix prefix of graph names where resolved quads are placed
      * @param conflictClusterFilter additional filter for quads in a conflict cluster
      */
     public ConflictResolverImpl(ResolutionFunctionRegistry resolutionFunctionRegistry,
             ConflictResolutionPolicy conflictResolutionPolicy, URIMapping uriMapping, Model metadata,
             String resolvedGraphsURIPrefix, ConflictClusterFilter conflictClusterFilter) {
         this(resolutionFunctionRegistry, conflictResolutionPolicy, uriMapping, metadata, resolvedGraphsURIPrefix);
         this.conflictClusterFilter = conflictClusterFilter;
     }
 
     /**
      * Sets conflict resolution settings.
      * @param conflictResolutionPolicy conflict resolution settings
      */
     public void setConflictResolutionPolicy(ConflictResolutionPolicy conflictResolutionPolicy) {
         this.conflictResolutionPolicy = conflictResolutionPolicy;
     }
 
     /**
      * Sets URI mapping to use.
      * @param uriMapping canonical URI mapping
      */
     public void setURIMapping(URIMapping uriMapping) {
         this.uriMapping = uriMapping;
     }
 
     /**
      * Sets additional metadata available for resolution.
      * @param metadata metadata as RDF model
      */
     public void setMetadata(Model metadata) {
         this.metadata = metadata;
     }
 
     /**
      * Sets factory for resolved statements produced as output of conflict resolution.
      * @param factory resolved statement factory
      */
     public void setResolvedStatementFactory(ResolvedStatementFactory factory) {
         this.resolvedStatementFactory = factory;
     }
 
     /**
      * Sets registry with resolution function implementations. 
      * @param resolutionFunctionRegistry registry for obtaining conflict resolution function implementations
      */
     public void setResolutionFunctionRegistry(ResolutionFunctionRegistry resolutionFunctionRegistry) {
         this.resolutionFunctionRegistry = resolutionFunctionRegistry;
     }
     
     /**
      * Sets additional filter for quads in a conflict cluster. 
      * @param conflictClusterFilter  additional filter for quads in a conflict cluster.
      */
     public void setConflictClusterFilter(ConflictClusterFilter conflictClusterFilter) {
         this.conflictClusterFilter = conflictClusterFilter;
     }
     
     @Override
     public Collection<ResolvedStatement> resolveConflicts(Iterator<Statement> statements) throws ConflictResolutionException {
         GrowingArray<Statement> growingArray = new GrowingArray<Statement>();
         while (statements.hasNext()) {
             growingArray.add(statements.next());
         }
         return resolveConflictsInternal(growingArray.getArray());
     }
 
     @Override
     public Collection<ResolvedStatement> resolveConflicts(Collection<Statement> statements) throws ConflictResolutionException {
         return resolveConflictsInternal(statements.toArray(new Statement[0]));
     }
 
     /**
      * The internal implementation of the conflict resolution algorithm.
      * @param statements RDF quads to be resolved; NOTE that this array will by modified by the function
      * @return resolved quads (see {@link #resolveConflicts(Collection)})
      * @throws ConflictResolutionException conflict resolution error
      */
     protected Collection<ResolvedStatement> resolveConflictsInternal(Statement[] statements) throws ConflictResolutionException {
         long startTime = 0;
         if (LOG.isDebugEnabled()) {
             LOG.debug("Resolving conflicts among {} quads.", statements.length);
             startTime = System.currentTimeMillis();
         }
 
         // Prepare effective resolution strategy based on per-predicate strategies, default strategy & uri mappings
         ConflictResolutionPolicy effectiveResolutionPolicy = getEffectiveResolutionPolicy();
         
         // Apply owl:sameAs mappings, remove duplicities, sort into clusters of conflicting quads
         ConflictClustersCollection conflictClusters = new ConflictClustersCollection(statements, uriMapping,
                 resolvedStatementFactory.getValueFactory());
         initContext(conflictClusters.asModel());
 
         // Resolve conflicts:
         Collection<ResolvedStatement> result = createResultCollection(conflictClusters.size());
         for (List<Statement> conflictCluster : conflictClusters) {
             if (conflictCluster.isEmpty()) {
                 continue;
             }
             
             // Get resolution strategy
             URI predicate = getPredicate(conflictCluster);
             ResolutionStrategy resolutionStrategy = effectiveResolutionPolicy.getPropertyResolutionStrategies().get(predicate);
             if (resolutionStrategy == null) {
                 resolutionStrategy = effectiveResolutionPolicy.getDefaultResolutionStrategy();
             }
             
             // Prepare resolution functions & context
             ResolutionFunction resolutionFunction = getResolutionFunction(resolutionStrategy);
             CRContext context = getContext(conflictCluster, resolutionStrategy);
             
             // Apply additional filtering
             if (conflictClusterFilter != null) {
                 conflictCluster = conflictClusterFilter.filter(conflictCluster, context);
                 if (conflictCluster.isEmpty()) {
                     continue;
                 }
             }
             
             // Resolve conflicts & append to result
             Model conflictClusterModel = new SortedListModel(conflictCluster);
             Collection<ResolvedStatement> resolvedStatements = resolutionFunction.resolve(conflictClusterModel, context);
             result.addAll(resolvedStatements);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Conflict resolution executed in {} ms, resolved to {} quads",
                     System.currentTimeMillis() - startTime, result.size());
         }
         return result;
     }
 
     private void initContext(Model statementsModel) {
         Model metadataModel = metadata != null ? metadata : new EmptyMetadataModel(); 
         context = new CRContextImpl(statementsModel, metadataModel, resolvedStatementFactory);
     }
 
     private CRContext getContext(List<Statement> conflictCluster, ResolutionStrategy resolutionStrategy) {
         context.setSubject(getSubject(conflictCluster));
         context.setResolutionStrategy(resolutionStrategy);
         return context;
     }
 
     private ConflictResolutionPolicy getEffectiveResolutionPolicy() {
        ConflictResolutionPolicyImpl result = new ConflictResolutionPolicyImpl();
        
        ResolutionStrategy effectiveDefaultStrategy = conflictResolutionPolicy.getDefaultResolutionStrategy() != null
                ? CRUtils.fillResolutionStrategyDefaults(conflictResolutionPolicy.getDefaultResolutionStrategy(),
                        DEFAULT_RESOLUTION_STRATEGY)
                : DEFAULT_RESOLUTION_STRATEGY;
        result.setDefaultResolutionStrategy(effectiveDefaultStrategy);
        
         Map<URI, ResolutionStrategy> effectivePropertyStrategies = new HashMap<URI, ResolutionStrategy>();
        for (Entry<URI, ResolutionStrategy> entry : conflictResolutionPolicy.getPropertyResolutionStrategies().entrySet()) {
            URI mappedURI = uriMapping.mapURI(entry.getKey());
            ResolutionStrategy strategy = CRUtils.fillResolutionStrategyDefaults(entry.getValue(), effectiveDefaultStrategy);
            effectivePropertyStrategies.put(mappedURI, strategy);
         }
         result.setPropertyResolutionStrategy(effectivePropertyStrategies);
         return result;
     }
 
     /**
      * Creates an empty instance of collection that is returned as the output
      * of conflict resolution.
      * @return an empty collection
      */
     private Collection<ResolvedStatement> createResultCollection(int inputSize) {
         return new ArrayList<ResolvedStatement>(inputSize / EXPECTED_REDUCTION_FACTOR);
     }
 
     private ResolutionFunction getResolutionFunction(ResolutionStrategy resolutionStrategy) 
             throws ResolutionFunctionNotRegisteredException {
         
         return resolutionFunctionRegistry.get(resolutionStrategy.getResolutionFunctionName());
     }
 
     private Resource getSubject(List<Statement> conflictCluster) {
         assert !conflictCluster.isEmpty();
         return conflictCluster.get(0).getSubject();
     }
 
     private URI getPredicate(List<Statement> conflictCluster) {
         assert !conflictCluster.isEmpty();
         return conflictCluster.get(0).getPredicate();
     }
 }
