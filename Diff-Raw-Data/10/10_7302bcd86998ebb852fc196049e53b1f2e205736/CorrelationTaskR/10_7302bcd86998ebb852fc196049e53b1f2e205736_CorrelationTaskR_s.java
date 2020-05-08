 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.CorrelationRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CorrelationResult;
 import gov.nih.nci.caintegrator.enumeration.CorrelationType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 public class CorrelationTaskR extends AnalysisTaskR {
 
 	private CorrelationResult result;
 	
 	private static Logger logger = Logger.getLogger(CorrelationTaskR.class);
 
 	
 	public CorrelationTaskR(AnalysisRequest request) {
 		super(request);
 		
 	}
 
 	public CorrelationTaskR(AnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 		
 	}
 
 	@Override
 	public void run() {
 		CorrelationRequest corrRequest = (CorrelationRequest) getRequest();
 		result = new CorrelationResult(getRequest().getSessionId(), getRequest().getTaskId());
 		logger.info(getExecutingThreadName() + " processing correlation request="
 						+ corrRequest);
 		
 		try {
 			
 			String dataFileName = corrRequest.getDataFileName();
 			
 			if (dataFileName == null) {
 			  //check to make sure that the vectors have data
 			  if ((corrRequest.getVector1()==null)||(corrRequest.getVector2()==null)) {
 				throw new AnalysisServerException("Problem with correlation request. No data file specified and vectors are null.");
 			  }	  
 			}
 			else {
 			  setDataFile(corrRequest.getDataFileName());
 			}
 			
 		} catch (AnalysisServerException e) {
 			e.setFailedRequest(corrRequest);
 			logger.error("Internal Error. " + e.getMessage());
 			setException(e);
 			return;
 		}
 
 		
 		try {
 		
 			//Need to handle case where gene expression reporters are passed in..
 			
 			
			String cmd = CorrelationTaskR.getRgroupCmd("GRP1", corrRequest.getVector1());
 			doRvoidEval(cmd);
 			
			cmd = CorrelationTaskR.getRgroupCmd("GRP2", corrRequest.getVector2());
 			doRvoidEval(cmd);
 			
 			REXP rVal = null;
 			
 			if (corrRequest.getCorrelationType() == CorrelationType.PEARSON) {
 			  cmd = "r <- correlation(GRP1,GRP2,\"pearson\")";
 			  rVal = doREval(cmd);
 			}
 			else if (corrRequest.getCorrelationType() == CorrelationType.SPEARMAN) {
 			  cmd = "r <- correlation(GRP1,GRP2, \"spearman\")";
 			  rVal = doREval(cmd);
 			}
 			else {
 			  throw new AnalysisServerException("Unrecognized correlationType or correlation type is null.");
 			}
 	
 			double r = rVal.asDouble();
 			result.setCorrelationValue(r);
			result.setVector1Name(corrRequest.getVector1Name());
			result.setVector1(corrRequest.getVector1());			
			result.setVector2Name(corrRequest.getVector2Name());
 			result.setVector2(corrRequest.getVector2());
 		}
 		catch (AnalysisServerException asex) {
 			AnalysisServerException aex = new AnalysisServerException(
 			"Problem computing correlation. Caught AnalysisServerException in CorrelationTaskR." + asex.getMessage());
 	        aex.setFailedRequest(corrRequest);
 	        setException(aex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught Exception in CorrelationTaskR exClass=" + ex.getClass() + " msg=" + ex.getMessage());
 	        asex.setFailedRequest(corrRequest);
 	        setException(asex);
 	        return;  
 		}
 	}
 
 	@Override
 	public void cleanUp() {
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 			logger.error("Error in cleanUp method.");
 			logger.error(e);
 			setException(e);
 		}
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 }
