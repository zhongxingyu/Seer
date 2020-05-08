 package cz.cuni.mff.odcleanstore.engine.outputws.output;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Date;
 
 import org.restlet.data.CharacterSet;
 import org.restlet.data.MediaType;
 import org.restlet.data.Reference;
 import org.restlet.representation.Representation;
 import org.restlet.representation.WriterRepresentation;
 
 import com.hp.hpl.jena.datatypes.RDFDatatype;
 import com.hp.hpl.jena.datatypes.TypeMapper;
 import com.hp.hpl.jena.graph.Factory;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.graph.impl.LiteralLabel;
 import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
 import com.hp.hpl.jena.shared.ReificationStyle;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import cz.cuni.mff.odcleanstore.configuration.OutputWSConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.CRQuad;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.Rule;
 import cz.cuni.mff.odcleanstore.queryexecution.BasicQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.EnumQueryType;
 import cz.cuni.mff.odcleanstore.queryexecution.NamedGraphMetadataQueryResult;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.vocabulary.DC;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.RDF;
 import cz.cuni.mff.odcleanstore.vocabulary.W3P;
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphImpl;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * Returns a representation of a query result serialized to the TriG format.
  * (See http://www4.wiwiss.fu-berlin.de/bizer/TriG/ .)
  * @author Jan Michelfeit
  */
 public class TriGFormatter extends ResultFormatterBase {
     /** {@ ODCS#quality} as a {@link Node}. */
     private static final Node QUALITY_PROPERTY = Node.createURI(ODCS.quality);
     /** {@ ODCS#score} as a {@link Node}. */
     private static final Node SCORE_PROPERTY = Node.createURI(ODCS.score);
     /** {@ ODCS#publisherScore} as a {@link Node}. */
     private static final Node PUBLISHER_SCORE_PROPERTY = Node.createURI(ODCS.publisherScore);
     /** {@ W3P#source} as a {@link Node}. */
     private static final Node SOURCE_PROPERTY = Node.createURI(W3P.source);
     /** {@ W3P#insertedAt} as a {@link Node}. */
     private static final Node INSERTED_AT_PROPERTY = Node.createURI(W3P.insertedAt);
     /** {@ W3P#publishedBy} as a {@link Node}. */
     private static final Node PUBLISHED_BY_PROPERTY = Node.createURI(W3P.publishedBy);
     /** {@ DC#license} as a {@link Node}. */
     private static final Node LICENSE_PROPERTY = Node.createURI(DC.license);
     /** {@ DC#title} as a {@link Node}. */
     private static final Node TITLE_PROPERTY = Node.createURI(DC.title);
     /** {@ DC#description} as a {@link Node}. */
     private static final Node DESCRIPTION_PROPERTY = Node.createURI(DC.description);
     /** {@ DC#date} as a {@link Node}. */
     private static final Node DATE_PROPERTY = Node.createURI(DC.date);
     /** {@ ODCS#totalResults} as a {@link Node}. */
     private static final Node TOTAL_RESULTS_PROPERTY = Node.createURI(ODCS.totalResults);
     /** {@ ODCS#result} as a {@link Node}. */
     private static final Node RESULT_PROPERTY = Node.createURI(ODCS.result);
     /** {@ RDF#type} as a {@link Node}. */
     private static final Node TYPE_PROPERTY = Node.createURI(RDF.type);
     /** {@ ODCS#query} as a {@link Node}. */
     private static final Node QUERY_PROPERTY = Node.createURI(ODCS.query);
     /** {@ ODCS#violatedQARule} as a {@link Node}. */
     private static final Node VIOLATED_QA_RULE_PROPERTY = Node.createURI(ODCS.violatedQARule);
     /** {@ ODCS#coefficient} as a {@link Node}. */
     private static final Node COEFFICIENT_PROPERTY = Node.createURI(ODCS.coefficient);
     /** {@ ODCS#provenanceMetadataGraph} as a {@link Node}. */
     private static final Node PROVENANCE_GRAPH_PROPERTY = Node.createURI(ODCS.provenanceMetadataGraph);
     /** {@ ODCS#queryResponse} as a {@link Node}. */
     private static final Node QUERY_RESPONSE_CLASS = Node.createURI(ODCS.queryResponse);
     /** {@ ODCS#QARule} as a {@link Node}. */
     private static final Node QARULE_CLASS = Node.createURI(ODCS.QARule);
     
     /** Title for a URI query. */
     private static final String TITLE_URI = "URI query for <%s>";
     
     /** Title for a keyword query. */
     private static final String TITLE_KW = "Keyword query for '%s'";
     
     /** Title for a named graph metadata query. */
     private static final String TITLE_NAMED_GRAPH = "Metadata query for named graph <%s>";
     
     /** Title for an unknown type of  query. */
     private static final String TITLE_GENERAL = "Query %s";
     
     /** Configuration of the output webservice from the global configuration file. */
     private OutputWSConfig outputWSConfig;
     
     /**
      * Creates a new instance.
      * @param outputWSConfig configuration of the output webservice from the global configuration file
      */
 	public TriGFormatter(OutputWSConfig outputWSConfig) {
 		this.outputWSConfig = outputWSConfig;
 	}
 
 	@Override
 	public Representation format(final BasicQueryResult result, final Reference requestReference) {
 		WriterRepresentation representation = new WriterRepresentation(MediaType.APPLICATION_RDF_TRIG) {
 			@Override
 			public void write(Writer writer) throws IOException {
 				basicConvertToNGSet(result, requestReference).write(writer, "TRIG", "" /* baseURI */);
 			};
 		};
 		representation.setCharacterSet(CharacterSet.UTF_8);
 		return representation;
 	}
 
     /**
      * Returns a representation of crQuads and metadata as quads in a NamedGraphSet.
      * @param queryResult result of a query
      * @param requestReference Representation of the requested URI
      * @return representation of crQuads and metadata as quads in a NamedGraphSet
      */
     private NamedGraphSet basicConvertToNGSet(BasicQueryResult queryResult, Reference requestReference) {
         NamedGraphSet result = new NamedGraphSetImpl();
         NamedGraph metadataGraph = new NamedGraphImpl(
                 outputWSConfig.getMetadataGraphURIPrefix().toString(),
                 Factory.createGraphMem(ReificationStyle.Standard));
         
         Node queryURI = Node.createURI(requestReference.toString(true, false));
         
         // Data and metadata about the result
         int totalResults = 0;
         for (CRQuad crQuad : queryResult.getResultQuads()) {
         	totalResults++;
             result.addQuad(crQuad.getQuad());
             metadataGraph.add(new Triple(
                     crQuad.getQuad().getGraphName(),
                     QUALITY_PROPERTY,
                     Node.createLiteral(LiteralLabelFactory.create(crQuad.getQuality()))));
             for (String sourceNamedGraph : crQuad.getSourceNamedGraphURIs()) {
                 metadataGraph.add(new Triple(
                         crQuad.getQuad().getGraphName(),
                         SOURCE_PROPERTY,
                         Node.createURI(sourceNamedGraph)));
             }
             metadataGraph.add(new Triple(queryURI, RESULT_PROPERTY, crQuad.getQuad().getGraphName()));
         }
 
         // Metadata of source named graphs
         addODCSNamedGraphMetadata(queryResult.getMetadata(), metadataGraph, true);
         
         
         // Metadata about the query
         metadataGraph.add(new Triple(queryURI, TYPE_PROPERTY, QUERY_RESPONSE_CLASS));
         
         String title;
         if (queryResult.getQueryType() == EnumQueryType.KEYWORD) {
     		title = String.format(TITLE_KW, queryResult.getQuery());
         } else if (queryResult.getQueryType() == EnumQueryType.URI) {
     		title = String.format(TITLE_URI, queryResult.getQuery());
         } else {
     		title = String.format(TITLE_GENERAL, queryResult.getQuery());
     	}
         metadataGraph.add(new Triple(queryURI, TITLE_PROPERTY, Node.createLiteral(title)));
 
         RDFDatatype dateTimeDatatype = TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.getURI());
         LiteralLabel nowLiteral = LiteralLabelFactory.create(new Date(), null, dateTimeDatatype);
         metadataGraph.add(new Triple(queryURI, DATE_PROPERTY, Node.createLiteral(nowLiteral)));
 
         metadataGraph.add(new Triple(queryURI, QUERY_PROPERTY, Node.createLiteral(queryResult.getQuery())));
         
         Node totalResultsLiteral = Node.createLiteral(LiteralLabelFactory.create(totalResults));
         metadataGraph.add(new Triple(queryURI, TOTAL_RESULTS_PROPERTY, totalResultsLiteral));
         
         result.addGraph(metadataGraph);
         return result;
     }
 
 	@Override
 	public Representation format(
 			final NamedGraphMetadataQueryResult metadataResult,
 			final GraphScoreWithTrace qaResult,
 			final long totalTime, 
 			final Reference requestReference) {
 		
 		WriterRepresentation representation = new WriterRepresentation(MediaType.APPLICATION_RDF_TRIG) {
 			@Override
 			public void write(Writer writer) throws IOException {
 				namedGraphConvertToNGSet(metadataResult, qaResult, totalTime, requestReference)
 						.write(writer, "TRIG", "" /* baseURI */);
 			};
 		};
 		representation.setCharacterSet(CharacterSet.UTF_8);
 		return representation;
 	}
 	
 	/**
 	 * Returns a formatted representation of a named graph query result. 
 	 * @param metadataResult result of metadata query about the requested named graph 
 	 * @param qaResult result of quality assessment over the given named graph
 	 * @param totalTime execution time of the query
 	 * @param requestReference Representation of the requested URI
 	 * @return representation of the result as quads in a NamedGraphSet
 	 */
 	private NamedGraphSet namedGraphConvertToNGSet(NamedGraphMetadataQueryResult metadataResult,
 			GraphScoreWithTrace qaResult, long totalTime, Reference requestReference) {
 		
 		Node queryURI = Node.createURI(requestReference.toString(true, false));
         Node namedGraphURI = Node.createURI(metadataResult.getQuery());
         
 		NamedGraphSet result = new NamedGraphSetImpl();
         NamedGraph metadataGraph = new NamedGraphImpl(
                 outputWSConfig.getMetadataGraphURIPrefix().toString(),
                 Factory.createGraphMem(ReificationStyle.Standard));
         
         // Quality Assessment results
         LiteralLabel scoreLiteral = LiteralLabelFactory.create(qaResult.getScore());
         metadataGraph.add(new Triple(namedGraphURI, SCORE_PROPERTY, Node.createLiteral(scoreLiteral)));
         for (Rule qaRule : qaResult.getTrace()) {
         	Node ruleNode = Node.createURI(outputWSConfig.getQARuleURIPrefix() + qaRule.getId().toString());
         	metadataGraph.add(new Triple(namedGraphURI, VIOLATED_QA_RULE_PROPERTY, ruleNode));
         	
         	metadataGraph.add(new Triple(ruleNode, TYPE_PROPERTY, QARULE_CLASS));
        	metadataGraph.add(new Triple(ruleNode, DESCRIPTION_PROPERTY, Node.createLiteral(qaRule.getComment())));
         	LiteralLabel coefficientLiteral = LiteralLabelFactory.create(qaRule.getCoefficient());
         	metadataGraph.add(new Triple(ruleNode, COEFFICIENT_PROPERTY, Node.createLiteral(coefficientLiteral)));
         }
 
         // Metadata of source named graphs
         addODCSNamedGraphMetadata(metadataResult.getMetadata(), metadataGraph, false);
         
         // Metadata about the query
         metadataGraph.add(new Triple(queryURI, TYPE_PROPERTY, QUERY_RESPONSE_CLASS));
         
         String title = String.format(TITLE_NAMED_GRAPH, metadataResult.getQuery());
         metadataGraph.add(new Triple(queryURI, TITLE_PROPERTY, Node.createLiteral(title)));
 
         RDFDatatype dateTimeDatatype = TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.getURI());
         LiteralLabel nowLiteral = LiteralLabelFactory.create(new Date(), null, dateTimeDatatype);
         metadataGraph.add(new Triple(queryURI, DATE_PROPERTY, Node.createLiteral(nowLiteral)));
 
         metadataGraph.add(new Triple(queryURI, QUERY_PROPERTY, Node.createLiteral(metadataResult.getQuery())));
         
         // Additional provenance metadata
         if (!metadataResult.getProvenanceMetadata().isEmpty()) {
         	Node additionalProvenanceGraphURI = metadataResult.getProvenanceMetadata().iterator().next().getGraphName();
         	NamedGraph additionalProvenanceGraph = new NamedGraphImpl(
             		additionalProvenanceGraphURI,
                     Factory.createGraphMem(ReificationStyle.Standard));
         	metadataGraph.add(new Triple(namedGraphURI, PROVENANCE_GRAPH_PROPERTY, additionalProvenanceGraphURI));
         	
         	for (Quad quad : metadataResult.getProvenanceMetadata()) {
         		additionalProvenanceGraph.add(quad.getTriple());        		
         	}
         	
         	result.addGraph(additionalProvenanceGraph);
         }
         
         result.addGraph(metadataGraph);
         return result;
 	}
 	
     /**
      * Adds metadata from the metadata argument as triples to metadataGraph.
      * @param metadata metadata to be added
      * @param metadataGraph graph where the metadata are placed
      * @param addScore indicates whether add a triple about named graph score or not
      */
 	private void addODCSNamedGraphMetadata(NamedGraphMetadataMap metadata, NamedGraph metadataGraph, boolean addScore) {
 		for (NamedGraphMetadata graphMetadata : metadata.listMetadata()) {
             Node namedGraphURI = Node.createURI(graphMetadata.getNamedGraphURI());
             String dataSource = graphMetadata.getSource();
             if (dataSource != null) {
                 metadataGraph.add(new Triple(namedGraphURI, SOURCE_PROPERTY, Node.createURI(dataSource)));
             }
 
             Double score = graphMetadata.getScore();
             if (addScore && score != null) {
                 LiteralLabel literal = LiteralLabelFactory.create(score);
                 metadataGraph.add(new Triple(namedGraphURI, SCORE_PROPERTY, Node.createLiteral(literal)));
             }
 
             Date storedAt = graphMetadata.getInsertedAt();
             if (storedAt != null) {
                 RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.getURI());
                 LiteralLabel literal = LiteralLabelFactory.create(storedAt, null, datatype);
                 metadataGraph.add(new Triple(namedGraphURI, INSERTED_AT_PROPERTY, Node.createLiteral(literal)));
             }
 
             String publisher = graphMetadata.getPublisher();
             if (publisher != null) {
                 metadataGraph.add(new Triple(namedGraphURI, PUBLISHED_BY_PROPERTY, Node.createURI(publisher)));
             }
             
             String license = graphMetadata.getLicence();
             if (license != null) {
             	Node licenseNode = license.startsWith("http://") && Utils.isValidIRI(license)
             			? Node.createURI(license)
             			: Node.createLiteral(license);
             	metadataGraph.add(new Triple(namedGraphURI, LICENSE_PROPERTY, licenseNode));
             }
 
             Double publisherScore = graphMetadata.getPublisherScore();
             if (publisherScore != null) {
                 LiteralLabel literal = LiteralLabelFactory.create(publisherScore);
                 metadataGraph.add(new Triple(namedGraphURI, PUBLISHER_SCORE_PROPERTY, Node.createLiteral(literal)));
             }
         }
 	}
 }
