 package cz.cuni.mff.odcleanstore.engine.outputws.output;
 
 import cz.cuni.mff.odcleanstore.configuration.OutputWSConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.QualityAssessmentRule;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryResultBase;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.vocabulary.DC;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCSInternal;
 import cz.cuni.mff.odcleanstore.vocabulary.RDF;
 import cz.cuni.mff.odcleanstore.vocabulary.W3P;
 
 import com.hp.hpl.jena.datatypes.RDFDatatype;
 import com.hp.hpl.jena.datatypes.TypeMapper;
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.graph.impl.LiteralLabel;
 import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  * An abstract base class for formatters having a serialized form of RDF as their output.
  * @author Jan Michelfeit
  */
 public abstract class RDFFormatter extends ResultFormatterBase {
     /** {@ ODCS#totalResults} as a {@link Node}. */
     protected static final Node TOTAL_RESULTS_PROPERTY = Node.createURI(ODCS.totalResults);
     /** {@ RDF#type} as a {@link Node}. */
     protected static final Node TYPE_PROPERTY = Node.createURI(RDF.type);
     /** {@ DC#title} as a {@link Node}. */
     protected static final Node TITLE_PROPERTY = Node.createURI(DC.title);
     /** {@ DC#date} as a {@link Node}. */
     protected static final Node DATE_PROPERTY = Node.createURI(DC.date);
     /** {@ ODCS#query} as a {@link Node}. */
     protected static final Node QUERY_PROPERTY = Node.createURI(ODCS.query);
     /** {@ ODCS#score} as a {@link Node}. */
     protected static final Node SCORE_PROPERTY = Node.createURI(ODCS.score);
     /** {@ ODCS#publisherScore} as a {@link Node}. */
     protected static final Node PUBLISHER_SCORE_PROPERTY = Node.createURI(ODCS.publisherScore);
     /** {@ W3P#source} as a {@link Node}. */
     protected static final Node SOURCE_PROPERTY = Node.createURI(W3P.source);
     /** {@ W3P#insertedAt} as a {@link Node}. */
     protected static final Node INSERTED_AT_PROPERTY = Node.createURI(W3P.insertedAt);
     /** {@ W3P#publishedBy} as a {@link Node}. */
     protected static final Node PUBLISHED_BY_PROPERTY = Node.createURI(W3P.publishedBy);
     /** {@link ODCS#updateTag} as a {@link Node}. */
     protected static final Node UPDATE_TAG_PROPERTY = Node.createURI(ODCS.updateTag);
     /** {@ DC#license} as a {@link Node}. */
     protected static final Node LICENSE_PROPERTY = Node.createURI(DC.license);
     /** {@ DC#description} as a {@link Node}. */
     protected static final Node DESCRIPTION_PROPERTY = Node.createURI(DC.description);
     /** {@ ODCS#violatedQARule} as a {@link Node}. */
     protected static final Node VIOLATED_QA_RULE_PROPERTY = Node.createURI(ODCS.violatedQARule);
     /** {@ ODCS#coefficient} as a {@link Node}. */
     protected static final Node COEFFICIENT_PROPERTY = Node.createURI(ODCS.coefficient);
     /** {@ ODCS#QARule} as a {@link Node}. */
     protected static final Node QARULE_CLASS = Node.createURI(ODCS.QARule);
     /** {@ ODCS#queryResponse} as a {@link Node}. */
     protected static final Node QUERY_RESPONSE_CLASS = Node.createURI(ODCS.queryResponse);
     
     /** Configuration of the output webservice from the global configuration file. */
     protected final OutputWSConfig outputWSConfig;
     
     /**
      * Creates a new instance.
      * @param outputWSConfig configuration of the output webservice from the global configuration file
      */
     public RDFFormatter(OutputWSConfig outputWSConfig) {
         this.outputWSConfig = outputWSConfig;
     }
     
     /**
      * Adds basic metadata about the query response to the given graph.
      * @param requestURI URI of the current request
      * @param queryResult query results
      * @param graph graph where the metadata is placed
      */
     protected void addBasicQueryMetadata(Node requestURI, QueryResultBase queryResult, Graph graph) {
         graph.add(new Triple(requestURI, TYPE_PROPERTY, QUERY_RESPONSE_CLASS));
 
         String title = formatQueryTitle(queryResult.getQuery(), queryResult.getQueryType());
         graph.add(new Triple(requestURI, TITLE_PROPERTY, Node.createLiteral(title)));
 
         RDFDatatype dateTimeDatatype = TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.getURI());
         LiteralLabel nowLiteral = LiteralLabelFactory.create(new Date(), null, dateTimeDatatype);
         graph.add(new Triple(requestURI, DATE_PROPERTY, Node.createLiteral(nowLiteral)));
 
         graph.add(new Triple(requestURI, QUERY_PROPERTY, Node.createLiteral(queryResult.getQuery())));
     }
     
     /**
      * Adds metadata from the metadata argument as triples to the given graph.
      * @param metadata metadata to be added
      * @param graph graph where the metadata are placed
      * @param addScore indicates whether add a triple about named graph score or not
      */
     protected void addODCSNamedGraphMetadata(NamedGraphMetadataMap metadata, Graph graph, boolean addScore) {
         for (NamedGraphMetadata graphMetadata : metadata.listMetadata()) {
             Node namedGraphURI = Node.createURI(graphMetadata.getNamedGraphURI());
             Collection<String> dataSourceList = graphMetadata.getSources();
             if (dataSourceList != null) {
                 for (String dataSource : dataSourceList) {
                     graph.add(new Triple(namedGraphURI, SOURCE_PROPERTY, Node.createURI(dataSource)));
                 }
             }
 
             Double score = graphMetadata.getScore();
             if (addScore && score != null) {
                 LiteralLabel literal = LiteralLabelFactory.create(score);
                 graph.add(new Triple(namedGraphURI, SCORE_PROPERTY, Node.createLiteral(literal)));
             }
 
             Date storedAt = graphMetadata.getInsertedAt();
             if (storedAt != null) {
                 RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.getURI());
                 LiteralLabel literal = LiteralLabelFactory.create(storedAt, null, datatype);
                 graph.add(new Triple(namedGraphURI, INSERTED_AT_PROPERTY, Node.createLiteral(literal)));
             }
 
             List<String> publisherList = graphMetadata.getPublishers();
             if (publisherList != null) {
                 for (String publisher : publisherList) {
                     graph.add(new Triple(namedGraphURI, PUBLISHED_BY_PROPERTY, Node.createURI(publisher)));
                 }
             }
 
             List<String> licenseList = graphMetadata.getLicences();
             if (licenseList != null) {
                 for (String license : licenseList) {
                     Node licenseNode = license.startsWith("http://") && Utils.isValidIRI(license)
                             ? Node.createURI(license)
                             : Node.createLiteral(license);
                     graph.add(new Triple(namedGraphURI, LICENSE_PROPERTY, licenseNode));
                 }
             }
 
            List<Double> publisherScoreList = graphMetadata.getPublisherScores();
            if (publisherScoreList != null) {
                for (Double publisherScore : publisherScoreList) {
                    LiteralLabel literal = LiteralLabelFactory.create(publisherScore);
                    graph.add(new Triple(namedGraphURI, PUBLISHER_SCORE_PROPERTY, Node.createLiteral(literal)));
                }
             }
             
             String updateTag = graphMetadata.getUpdateTag();
             if (updateTag != null) {
                 LiteralLabel literal = LiteralLabelFactory.create(updateTag);
                 graph.add(new Triple(namedGraphURI, UPDATE_TAG_PROPERTY, Node.createLiteral(literal)));
             }
         }
     }
     
     /**
      * Adds quality assessment results as triples to graph.
      * @param namedGraphURI URI on which QA was executed
      * @param qaResult results of quality assessment
      * @param graph graph where the results are placed
      */
     protected void addQualityAssessmentResults(Node namedGraphURI, GraphScoreWithTrace qaResult, Graph graph) {
         if (qaResult != null) {
             LiteralLabel scoreLiteral = LiteralLabelFactory.create(qaResult.getScore());
             graph.add(new Triple(namedGraphURI, SCORE_PROPERTY, Node.createLiteral(scoreLiteral)));
             for (QualityAssessmentRule qaRule : qaResult.getTrace()) {
                 Node ruleNode = Node.createURI(outputWSConfig.getResultDataURIPrefix().toString() 
                         + ODCSInternal.queryQARuleUriInfix + qaRule.getId().toString());
                 graph.add(new Triple(namedGraphURI, VIOLATED_QA_RULE_PROPERTY, ruleNode));
     
                 graph.add(new Triple(ruleNode, TYPE_PROPERTY, QARULE_CLASS));
                 graph.add(new Triple(ruleNode, DESCRIPTION_PROPERTY, Node.createLiteral(qaRule.getDescription())));
                 LiteralLabel coefficientLiteral = LiteralLabelFactory.create(qaRule.getCoefficient());
                 graph.add(new Triple(ruleNode, COEFFICIENT_PROPERTY, Node.createLiteral(coefficientLiteral)));
             }
         }
     }
 }
