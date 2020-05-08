 package cz.cuni.mff.odcleanstore.engine.outputws;
 
 import cz.cuni.mff.odcleanstore.configuration.Config;
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.queryexecution.EnumQueryError;
 import cz.cuni.mff.odcleanstore.queryexecution.MetadataQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryExecutionException;
 import cz.cuni.mff.odcleanstore.shared.ErrorCodes;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCSInternal;
 
 import org.restlet.representation.Representation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 
 /**
  * ServerResource for (named graph) metadata query.
  * @author Jan Michelfeit
  */
 public class MetadataQueryExecutorResource extends QueryExecutorResourceBase {
 
     private static final Logger LOG = LoggerFactory.getLogger(MetadataQueryExecutorResource.class);
     
     /**
      * SPARQL query selecting all QA groups for given a named graph.
      * @param first named graph UUID
      */
     private static final String SELECT_QA_GROUPS = "SELECT qa_assignment.groupId"
             + "\n FROM DB.ODCLEANSTORE.QA_RULES_ASSIGNMENT AS qa_assignment"
             + "\n   JOIN DB.ODCLEANSTORE.TRANSFORMER_INSTANCES AS transformer_inst "
             + "\n      ON (qa_assignment.transformerInstanceId = transformer_inst.id)"
             + "\n   JOIN DB.ODCLEANSTORE.TRANSFORMERS AS transformers"
             + "\n     ON (transformer_inst.transformerId = transformers.id)"
             + "\n   JOIN DB.ODCLEANSTORE.EN_INPUT_GRAPHS AS input_graphs"
             + "\n     ON (transformer_inst.pipelineId = input_graphs.pipelineId)" + "\n WHERE input_graphs.uuid = ?"
             + "\n   AND transformers.fullClassName = '" + QualityAssessorImpl.class.getCanonicalName() + "'"
             + "\n   AND input_graphs.isInCleanDB = 1";
 
     @Override
     protected Representation execute() throws QueryExecutionException, ResultEmptyException, TransformerException {
         String namedGraphURI = getFormValue("uri");
 
         // Get metadata
         MetadataQueryResult metadataResult = getQueryExecution().findNamedGraphMetadata(namedGraphURI);
         if (metadataResult == null) {
             LOG.error("Query result is empty");
             throw new ResultEmptyException("Result is empty");
         }
 
         // Get QA results
         long qaStartTime = System.currentTimeMillis();
         Config config = ConfigLoader.getConfig();
         GraphScoreWithTrace qaResult = null;
         String graphUuid = Utils.extractUUID(namedGraphURI);
        // TODO FIX - now is newGraphPrefix and oldGraphPrefix not!
        if (graphUuid != null && !namedGraphURI.startsWith(ODCSInternal.newGraphPrefix)) {
             JDBCConnectionCredentials connectionCredentials =
                     config.getQueryExecutionGroup().getCleanDBJDBCConnectionCredentials();
             Integer[] qaGroupIDs = qaRuleGroupsForGraph(graphUuid, connectionCredentials);
             if (qaGroupIDs.length > 0) {
                 QualityAssessorImpl qualityAssessor = new QualityAssessorImpl(qaGroupIDs);
                 qaResult = qualityAssessor.getGraphScoreWithTrace(namedGraphURI, connectionCredentials);
             }
         }
 
         long totalTime = System.currentTimeMillis() - qaStartTime + metadataResult.getExecutionTime();
         return getFormatter(config.getOutputWSGroup())
                 .format(metadataResult, qaResult, totalTime, getRequestReference());
     }
 
     /**
      * Returns QA rule groups applied to the data named graph with the given UUID.
      * The returned QA groups are those assigned to a transformer on the named graph's pipeline.
      * @param uuid named graph UUID
      * @param connectionCredentials connection credentials to the relational database
      * @return array of QA rule group IDs
      * @throws QueryExecutionException database exception
      */
     private Integer[] qaRuleGroupsForGraph(String uuid, JDBCConnectionCredentials connectionCredentials)
             throws QueryExecutionException {
         ArrayList<Integer> result = new ArrayList<Integer>();
 
         VirtuosoConnectionWrapper connection = null;
         WrappedResultSet resultSet = null;
         try {
             connection = VirtuosoConnectionWrapper.createConnection(connectionCredentials);
             resultSet = connection.executeSelect(SELECT_QA_GROUPS, uuid);
             while (resultSet.next()) {
                 result.add(resultSet.getInt(1));
             }
         } catch (Exception e) {
             throw new QueryExecutionException(
                     EnumQueryError.DATABASE_ERROR, ErrorCodes.QE_NG_METADATA_DB_ERR, "Database error", e);
         } finally {
             if (resultSet != null) {
                 resultSet.closeQuietly();
             }
             if (connection != null) {
                 connection.closeQuietly();
             }
         }
         return result.toArray(new Integer[result.size()]);
     }
 }
