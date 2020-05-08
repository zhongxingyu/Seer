 package cz.cuni.mff.odcleanstore.conflictresolution.aggregation;
 
 import cz.cuni.mff.odcleanstore.conflictresolution.AggregationSpec;
 import cz.cuni.mff.odcleanstore.conflictresolution.CRQuad;
 import cz.cuni.mff.odcleanstore.conflictresolution.EnumAggregationErrorStrategy;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.shared.UniqueURIGenerator;
 
 import com.hp.hpl.jena.graph.Node;
 
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 /**
  * Base class for all aggregation methods implemented in ODCleanStore.
  * All subclasses should be stateless (see {@link AggregationMethodFactory}).
  *
  * @author Jan Michelfeit
  */
 abstract class AggregationMethodBase implements AggregationMethod {
     private static final Logger LOG = LoggerFactory.getLogger(AggregationMethodBase.class);
 
     /**
      * Node distance metric used in quality computation.
      * @see #computeQuality(Quad, Collection, NamedGraphMetadataMap)
      */
     private static/*final*/DistanceMetric distanceMetricInstance = new DistanceMetricImpl();
 
     /**
      * Coefficient used in formula computed at {@link #computeQuality()}.
      * The coefficient determines how multiple source named graphs that exactly agree on
      * the result value increase quality. Value N of the coefficient means that (N+1) sources
      * with score 1 that agree on the result increase the result quality to 1.
      * @todo determine best value
      */
     public static final double AGREE_COEFFICIENT = 4;
 
     /**
      * Default score of a named graph or a publisher used if the respective
      * score is unknown.
      * @todo determine best value
      */
     public static final double SCORE_IF_UNKNOWN = 1;
 
     /**
      * Weight of the source named graph score in source quality calculation.
      * The sum of weights may be different from 1.
      * @see #getSourceQuality(NamedGraphMetadata)
      * @see #PUBLISHER_SCORE_WEIGHT
      * @todo determine best value
      */
     protected static final double NAMED_GRAPH_SCORE_WEIGHT = 0.8;
 
     /**
      * Weight of the publisher source in source quality calculation.
      * The sum of weights may be different from 1.
      * @see #getSourceQuality(NamedGraphMetadata)
      * @see #NAMED_GRAPH_SCORE_WEIGHT
      */
     protected static final double PUBLISHER_SCORE_WEIGHT = 0.2;
 
     /**
      * Generator of unique URIs.
      */
     protected final UniqueURIGenerator uriGenerator;
 
     /**
      * Aggregation and quality calculation settings.
      */
     protected final AggregationSpec aggregationSpec;
 
     /**
      * Creates a new instance with given settings.
      * @param aggregationSpec aggregation and quality calculation settings
      * @param uriGenerator generator of URIs
      */
     public AggregationMethodBase(AggregationSpec aggregationSpec, UniqueURIGenerator uriGenerator) {
         this.uriGenerator = uriGenerator;
         this.aggregationSpec = aggregationSpec;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public abstract Collection<CRQuad> aggregate(Collection<Quad> conflictingTriples, NamedGraphMetadataMap metadata);
 
     /**
      * Return the default DistanceMetric instance.
      * @return Node distance metric that can be used in quality computation.
      */
     protected DistanceMetric getDistanceMetric() {
         return distanceMetricInstance;
     }
 
     /**
      * Calculated quality of a source of a named graph.
      * The quality is calculated as a weighted average of (error localization)
      * score of the named graph and score of its publisher with weights
      * defined in {@link #NAMED_GRAPH_SCORE_WEIGHT} and {@link #PUBLISHER_SCORE_WEIGHT}.
      * @param metadata metadata of the estimated named graph (or null if unknown)
      * @return quality of source of the named graph as a value from [0,1]
      * @see #SCORE_IF_UNKNOWN
      */
     protected double getSourceQuality(NamedGraphMetadata metadata) {
         // Weighted average of metadata.getScore() and metadata.getPublisherScore()
         if (metadata == null) {
             LOG.debug("No metadata given for source quality computation, using default scores.");
             return SCORE_IF_UNKNOWN;
         }
 
         Double namedGraphScore = metadata.getScore();
         if (namedGraphScore == null) {
             LOG.debug("No score for named graph {}, using default score.", metadata.getNamedGraphURI());
             namedGraphScore = SCORE_IF_UNKNOWN;
         }
 
         Double publisherScore = metadata.getPublisherScore();
         if (publisherScore == null) {
             LOG.debug("No score for the publisher of named graph {}, using named graph score.",
                     metadata.getNamedGraphURI());
             publisherScore = namedGraphScore;
         }
 
         double quality = namedGraphScore * NAMED_GRAPH_SCORE_WEIGHT + publisherScore * PUBLISHER_SCORE_WEIGHT;
         quality /= NAMED_GRAPH_SCORE_WEIGHT + PUBLISHER_SCORE_WEIGHT;
         return quality;
     }
 
     /**
      * Compute quality estimate of the selected quad not taking into consideration
      * possible conflicting quads or other advanced quality factors.
      *
      * Invariant: the result is <= sum of scores of sourceNamedGraphs
      * (see {@link #computeQuality()}).
      *
      * @param resultQuad the quad for which quality is to be computed
      * @param sourceNamedGraphs URIs of source named graphs containing triples used to calculate
      *        the result value; must not be empty
      * @param metadata metadata of the given source named graphs
      * @return quality estimate of resultQuad as a number from [0,1]
      * @see #getSourceQuality(NamedGraphMetadata)
      */
     protected abstract double computeBasicQuality(
             Quad resultQuad,
             Collection<String> sourceNamedGraphs,
             NamedGraphMetadataMap metadata);
 
     /**
      * Compute quality estimate of a selected quad taking into consideration
      * possible conflicting quads, source named graph metadata and aggregation settings.
      *
      * The exact formula is
      *
      * <pre>
      * q1(V,v0) = computeBasicQuality(v0) *
      *   * (1 - (SUM OF (sourceQuality(V[i]) * difference(V[i],v0))) / (SUM OF sourceQuality(V[i])))
      * q(V,v0) = q1(V,v0) + (1-q1(V,v0)) *
      *   * min(1, (-computeBasicQuality(v0) + (SUM OF sourceNamedGraphs scores)) /AGREE_COEFFICIENT)
      * </pre>
      *
      * if the multivalue attribute for resultQuad's predicate is false, otherwise
      *
      * <pre>
      * q1(V,v0) = computeBasicQuality(v0)
      * q(V,v0) = (the same as above)
      * </pre>
      *
      * (see the documentation for explanation).
      * The time complexity for multivalue properties is O(s), for non-multivalue properties
      * O(n*d + s) where n is the size of conflictingQuads, d is the complexity of difference
      * calculation, and s is the size of sourceNamedGraphs.
      *
      * (Actually + O(log k), where k is the size of aggregationSpec.getPropertiesMultivalue().)
      *
      * Precondition: resultQuad is expected to be in conflictingQuads.
      *
      * @param resultQuad the quad for which quality is to be computed
      * @param sourceNamedGraphs URIs of source named graphs containing triples used to calculate
      *        the result value; must not be empty
      * @param agreeNamedGraphs URIs of named graphs that contain exactly the value given in
      *        resultQuad; this may be the same set as sourceNamedGraphs or a completely different
      *        set (e.g. in case of calculated values)
      * @param conflictingQuads other quads conflicting with resultQuad
      *        (for what is meant by conflicting quads see AggregationMethod#aggregate());
      *        see preconditions
      * @param metadata metadata of source named graphs for resultQuad
      *        and conflictingQuads
      * @return quality estimate of resultQuad as a number from [0,1]
      * @see #getSourceQuality(NamedGraphMetadata)
      * @see #AGREE_COEFFICIENT
      */
     protected double computeQuality(
             Quad resultQuad,
             Collection<String> sourceNamedGraphs,
             Collection<String> agreeNamedGraphs,
             Collection<Quad> conflictingQuads,
             NamedGraphMetadataMap metadata) {
 
         // Compute basic score based on sourceNamedGraphs' scores
         double basicQuality = computeBasicQuality(resultQuad, sourceNamedGraphs, metadata);
         double resultQuality = basicQuality;
 
         // Usually, the quality is positive, skip the check
         // if (resultQuality == 0) {
         // return resultQuality; // BUNO
         // }
 
         // Consider conflicting values
         boolean isPropertyMultivalue = aggregationSpec.isPropertyMultivalue(resultQuad.getPredicate().getURI());
         if (!isPropertyMultivalue && conflictingQuads.size() > 1) {
             // NOTE: condition conflictingQuads.size() > 1 is an optimization that relies on
             // the fact that distance(x,x) = 0 and that resultQuad is in conflictingQuads
 
             // Calculated distance average weighted by the respective source qualities
             DistanceMetric distanceMetric = getDistanceMetric();
             double distanceAverage = 0;
             double totalSourceQuality = 0;
             for (Quad quad : conflictingQuads) {
                 NamedGraphMetadata quadMetadata = metadata.getMetadata(quad.getGraphName());
                 double quadQuality = getSourceQuality(quadMetadata);
 
                 double resultDistance = distanceMetric.distance(quad.getObject(), resultQuad.getObject());
                 distanceAverage += quadQuality * resultDistance;
                 totalSourceQuality += quadQuality;
             }
 
             // resultQuality cannot be zero (tested before) -> if sum of
             // conflictingQuads source qualities is zero, resultQuality is not
             // among them -> precondition broken
             assert (totalSourceQuality > 0) : "Precondition broken: resultQuad is not present in conflictingQuads";
 
             distanceAverage /= totalSourceQuality;
 
             resultQuality = resultQuality * (1 - distanceAverage);
         }
 
         // Increase score if multiple sources agree on the result value
         if (agreeNamedGraphs.size() > 1) {
             // IMPORTANT NOTE: the condition (agreeNamedGraphs.size() > 1) is an optimization that
             // relies on the fact the for (sourceNamedGraphs.size() == 1) the condition
             // (basicQuality == sourceScoreSum) holds
             double sourceScoreSum = 0;
             for (String sourceNamedGraphURI : agreeNamedGraphs) {
                 NamedGraphMetadata namedGraphMetadata = metadata.getMetadata(sourceNamedGraphURI);
                 sourceScoreSum += getSourceQuality(namedGraphMetadata);
             }
             double agreeQualityCoef = (sourceScoreSum - basicQuality) / AGREE_COEFFICIENT;
             // agreeQualityCoef is non-negative thanks to invariant in computeBasicQuality()
             if (agreeQualityCoef > 1) {
                 agreeQualityCoef = 1;
             } else if (agreeQualityCoef < 0) {
                 agreeQualityCoef = 0;
             }
             resultQuality += (1 - resultQuality) * agreeQualityCoef;
         }
 
         // Return result
         return resultQuality;
     }
 
     /**
      * Applies the given aggregation error strategy to a quad that cannot be aggregated by the
      * given aggregation method.
      * In case of the RETURN_ALL error strategy, the given quad is added to the result.
      *
      * @param nonAggregableQuad the quad that couldn't be aggregated
      * @param conflictingQuads other quads conflicting with nonAggregableQuad; see
      *        {@link #computeQuality(Quad,Collection,Collection,Collection,NamedGraphMetadataMap)}
      * @param metadata metadata of source named graphs for nonAggregableQuad and conflictingQuads
      * @param result result of aggregation; a new CRQuad may be added to this collection
      * @param aggregationMethod aggregation class where the aggregation error occurred
      */
     protected void handleNonAggregableObject(
             Quad nonAggregableQuad,
             Collection<Quad> conflictingQuads,
             NamedGraphMetadataMap metadata,
             Collection<CRQuad> result,
             Class<? extends AggregationMethodBase> aggregationMethod) {
 
         LOG.debug("Value {} cannot be aggregated with {}.",
                 nonAggregableQuad.getObject(), aggregationMethod.getSimpleName());
 
         EnumAggregationErrorStrategy errorStrategy = aggregationSpec.getErrorStrategy();
         switch (errorStrategy) {
         case RETURN_ALL:
             Collection<String> sourceNamedGraphs = sourceNamedGraphsForObject(
                     nonAggregableQuad.getObject(),
                     conflictingQuads);
             double quality = computeQuality(
                     nonAggregableQuad,
                     sourceNamedGraphs,
                     sourceNamedGraphs,
                     conflictingQuads,
                     metadata);
             Quad resultQuad = new Quad(Node.createURI(uriGenerator.nextURI()), nonAggregableQuad.getTriple());
             result.add(new CRQuad(
                     resultQuad,
                     quality,
                     Collections.singleton(nonAggregableQuad.getGraphName().getURI())));
             break;
         case IGNORE:
             // do nothing
             break;
         default:
             LOG.error("Unhandled aggregation error strategy {}.", errorStrategy);
             throw new RuntimeException("Unhandled error strategy!");
         }
     }
 
     /**
      * Return a set (without duplicates) of named graphs of all quads that have the selected object
      * as their object.
      *
      * @param object the searched triple object
      * @param conflictingQuads searched quads
      * @return set of named graphs
      */
    protected Collection<String> sourceNamedGraphsForObject(Node object, Collection<Quad> conflictingQuads) {
         Set<String> namedGraphs = null;
         String firstNamedGraph = null;
 
         for (Quad quad : conflictingQuads) {
            if (!object.sameValueAs(quad.getObject())) { // intentionally sameValueAs()
                 continue;
             }
 
             String newNamedGraph = quad.getGraphName().getURI();
             // Purpose of these if-else branches is to avoid creating HashSet
             // if not necessary (only zero or one named graph in the result)
             if (firstNamedGraph == null) {
                 firstNamedGraph = newNamedGraph;
             } else if (namedGraphs == null) {
                 namedGraphs = new HashSet<String>();
                 namedGraphs.add(firstNamedGraph);
                 namedGraphs.add(newNamedGraph);
             } else {
                 namedGraphs.add(newNamedGraph);
             }
         }
 
         if (firstNamedGraph == null) {
             return Collections.emptySet();
         } else if (namedGraphs == null) {
             return Collections.singleton(firstNamedGraph);
         } else {
             return namedGraphs;
         }
     }
 
     /**
      * Factory method for collections returned by
     * {@link #aggregate(Collection, NamedGraphMetadataMap, EnumAggregationErrorStrategy) aggregate()}.
      * @return an empty collection
      */
     protected Collection<CRQuad> createResultCollection() {
         return new LinkedList<CRQuad>();
     }
 
     /**
      * Factory method for collections returned by
      * {@link #aggregate(Collection, NamedGraphMetadataMap, EnumAggregationErrorStrategy)
      * aggregate()} with a single value.
      * @param resultQuad a quad initially added to the result collection
      * @return an collection containing only resultQuad
      */
     protected Collection<CRQuad> createSingleResultCollection(CRQuad resultQuad) {
         return Collections.singletonList(resultQuad);
     }
 }
