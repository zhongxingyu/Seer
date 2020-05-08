 package cz.cuni.mff.odcleanstore.engine.outputws;
 
 import org.restlet.representation.Representation;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.queryexecution.NamedGraphMetadataQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryExecution;
 
 /**
  *  @author Jan Michelfeit
  */
 public class NamedGraphQueryExecutorResource extends QueryExecutorResourceBase {
 
 	protected Representation execute() {
 		try {
 			String namedGraphURI = getFormValue("uri");
 			JDBCConnectionCredentials connectionCredentials = 
 					ConfigLoader.getConfig().getBackendGroup().getCleanDBJDBCConnectionCredentials();
 
 			// Get metadata
 			QueryExecution queryExecution = new QueryExecution(connectionCredentials, ConfigLoader.getConfig());
 			NamedGraphMetadataQueryResult metadataResult = queryExecution.findNamedGraphMetadata(namedGraphURI);
 			
 			// Get QA results
 			long qaStartTime = System.currentTimeMillis();
			//TODO: This is HOTFIX. NamedGraphQueryExecutorResource needs to pass proper groupIds or groupLabels in constructor of QAImpl
			//This only makes common ids be selected (as groupId is IDENTITY (AUTOINCREMENT starting at 1))
			QualityAssessorImpl qualityAssessor = new QualityAssessorImpl(0, 1, 2, 3, 4, 5);
 			GraphScoreWithTrace qaResult = qualityAssessor.getGraphScoreWithTrace(namedGraphURI, connectionCredentials);
 			
 			if (metadataResult == null || qaResult == null)
 				return return404();
 			
 			long totalTime = System.currentTimeMillis() - qaStartTime + metadataResult.getExecutionTime();
 
 			return getFormatter(ConfigLoader.getConfig().getOutputWSGroup()).format(
 					metadataResult, qaResult, totalTime, getRequestReference());
 		} catch (Exception e) {
 			return return404();
 		}
 	}
 }
