 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.CorrelationRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.CorrelationResult;
 import gov.nih.nci.caintegrator.analysis.messaging.DataPoint;
 import gov.nih.nci.caintegrator.analysis.messaging.ReporterInfo;
 import gov.nih.nci.caintegrator.enumeration.AxisType;
 import gov.nih.nci.caintegrator.enumeration.CorrelationType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 public class CorrelationTaskR extends AnalysisTaskR {
 
 	private CorrelationResult result;
 	
 	private static Logger logger = Logger.getLogger(CorrelationTaskR.class);
 	private CorrelationRequest corrRequest;
 
 	
 	public CorrelationTaskR(AnalysisRequest request) {
 		super(request);
 		
 	}
 
 	public CorrelationTaskR(AnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 		
 	}
 
 	@Override
 	public void run() {
 		corrRequest = (CorrelationRequest) getRequest();
 		result = new CorrelationResult(getRequest().getSessionId(), getRequest().getTaskId());
 		logger.info(getExecutingThreadName() + " processing correlation request="
 						+ corrRequest);
 		
 		List<DataPoint> computePoints = null;
 	
 		try {
 		
 			//Check to see if a reporter was passed in
 			ReporterInfo reporter1 = corrRequest.getReporter1();
 			ReporterInfo reporter2 = corrRequest.getReporter2();
 			
 			List<String> restrictingSampleIds = corrRequest.getSampleIds();			
 			Double r = null;
 			Map <String, DataPoint> vecMap = new HashMap<String, DataPoint>();
 						
 			if ((reporter1 != null) && (reporter2 != null)) {
 			  //CASE 1:  correlation between two reporters
 			  result.setGroup1Name(reporter1.getGeneSymbol() + "_" + reporter1.getReporterName());
 			  result.setGroup2Name(reporter2.getGeneSymbol() + "_" + reporter2.getReporterName());
 			  			 			 
 			  setDataPoints(reporter1,restrictingSampleIds,vecMap, AxisType.X_AXIS, true);
 			  setDataPoints(reporter2,restrictingSampleIds,vecMap, AxisType.Y_AXIS, false);  
 			}
 			else if ((reporter1!=null) && (reporter2==null)) {
 			  //CASE2 : a reporter against a vector			 
 			  List<DataPoint> yPoints = corrRequest.getVector2();
 			  setDataPoints(yPoints,vecMap, true);
 			  setDataPoints(reporter1,restrictingSampleIds,vecMap, AxisType.X_AXIS, false);
 			}
			else if ((reporter1==null) && (reporter2!=null) ) {			  
 			  List<DataPoint> xPoints = corrRequest.getVector1();
 			  setDataPoints(xPoints,vecMap, true);
 			  setDataPoints(reporter2,restrictingSampleIds,vecMap, AxisType.Y_AXIS, false);
 			}
 			else { 			  
 			  List<DataPoint> xPoints = corrRequest.getVector1();
 			  setDataPoints(xPoints, vecMap, true);
 			  
 			  List<DataPoint> yPoints = corrRequest.getVector2();
 			  setDataPoints(yPoints, vecMap, false);
 			}		
 			
 			computePoints = getComputePoints(vecMap.values());
 			r = computeCorrelationCofficient(computePoints);
 			
 			result.setCorrelationValue(r);			
 			result.setDataPoints(computePoints);
 		}
 		catch (AnalysisServerException asex) {
 		    asex.setFailedRequest(corrRequest);
 	        setException(asex);
 	        logger.error("Caught AnalysisServerException");
 	        logger.error(asex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught Exception in CorrelationTaskR exClass=" + ex.getClass() + " msg=" + ex.getMessage());
 	        asex.setFailedRequest(corrRequest);
 	        setException(asex);
 	        logger.error("Caught Exception in CorrelationTaskR");
 	        StringWriter sw = new StringWriter();
 	        PrintWriter pw  = new PrintWriter(sw);
 	        ex.printStackTrace(pw);
 	        logger.error(sw.toString());
 	        return;  
 		}
 	}
 
 	private List<DataPoint> getComputePoints(Collection<DataPoint> points) {
 		List<DataPoint> computePoints = new ArrayList<DataPoint>();
 		for (DataPoint point: points) {
 	      if ((point.getX()!=null) &&(point.getY()!=null)) {
 	        computePoints.add(point);
 	      }
 		}
 		return computePoints;
 	}
 
 	/**
 	 * Compute the correlation coefficient based on the specified compute points.
 	 * @param computePoints
 	 * @return
 	 * @throws AnalysisServerException
 	 */
 	private Double computeCorrelationCofficient(List<DataPoint> computePoints) throws AnalysisServerException {
 		String v1str = "v1 <- c(" + getDataString(computePoints, true,false);
 		  String v2str = "v2 <- c(" + getDataString(computePoints,false,true);
 		  Double r = null;
 		  String cmd;
 		  		  
 		  doRvoidEval(v1str);
 		  doRvoidEval(v2str);
 		  if (corrRequest.getCorrelationType() == CorrelationType.PEARSON) {
 		    cmd = "r <- correlation(v1,v2,\"pearson\")";
 		    r = doREval(cmd).asDouble();
 		  }
 		  else if (corrRequest.getCorrelationType() == CorrelationType.SPEARMAN) {
 		    cmd = "r <- correlation(v1,v2, \"spearman\")";
 		    r = doREval(cmd).asDouble();
 		  }
 		  
 		  return r;
 	}
 	
 	/**
 	 * Load the pontMap with the points
 	 * @param points
 	 * @param pointMap
 	 * @param createPoints
 	 */
 	private void setDataPoints(List<DataPoint> points, Map<String, DataPoint> pointMap, boolean createPoints) {
 	  String id;
 	  DataPoint dp;
 	
 	  
 	  if (points == null) {
 	    logger.error("setDataPoints passed null paramter points");
 	    return;
 	  }
 	  
 	  for (DataPoint point : points) {	
 		  	  
 		if (point == null) {
 		  logger.warn("Found null point in points list. This should not happen.");
 		}
 		  
 		id = point.getId();
 		dp = pointMap.get(id);
 		
 		if (dp == null)  { 
 		  if (createPoints) {
 		    pointMap.put(id, point);
 		  }
 		  else {
 			logger.warn("pointMap does not contain an entry with id=" + id + " skipping point...");
 		  }
 		}
 		else {
 		  if (dp.getX() == null) dp.setX(point.getX());
 		  if (dp.getY() == null) dp.setY(point.getY());
 		  //if (dp.getZ() == null) dp.setZ(point.getZ());
 		}
 	  }
 	}
 
 	/**
 	 * Load the point map with the sample information from the binary file for the reporter specified.
 	 * @param reporter
 	 * @param sampleIds
 	 * @param pointMap
 	 * @param axis
 	 * @param createNewPoints
 	 * @throws AnalysisServerException
 	 */
 	private void setDataPoints(ReporterInfo reporter, List<String> sampleIds, Map<String, DataPoint> pointMap, AxisType axis, boolean createNewPoints) throws AnalysisServerException{
 		
 		try {
 			
 			List<DataPoint> retList = new ArrayList<DataPoint>();
 			setDataFile(reporter.getDataFileName());
 		    String cmd;
 			
 			if ((sampleIds != null)&&(!sampleIds.isEmpty())) {
 			  String sampleIdscmd = getRgroupCmd("sampleIds", sampleIds);
 			  doRvoidEval(sampleIdscmd);
 			
 			  cmd = "SM <- getSubmatrix.onegrp(dataMatrix, sampleIds)";
 			  doRvoidEval(cmd);			  
 			}
 			else {
 			  //use the entire data matrix without restricting on samples
 			  cmd = "SM <- dataMatrix";
 			  doRvoidEval(cmd);
 			}
 			
 			
 			cmd = "RM <- getSubmatrix.rep(SM,\"" + reporter.getReporterName() + "\")";
 			double[] vec =  doREval(cmd).asDoubleArray();
 			//need to make sure that the vectors are in the same order wrt sample ids
 			cmd = "RMlabels <- dimnames(RM)[[1]]";
 			Vector RM_ids = doREval(cmd).asVector();
 			
 		    if (RM_ids == null) {
 		      throw new AnalysisServerException("Reporter " + reporter.getReporterName() + " not found in data file=" + reporter.getDataFileName() + ".");
 		    }
 			
 			DataPoint point;
 			String id;
 			REXP exp;
 			for (int i=0; i < RM_ids.size(); i++) {
 			  exp = (REXP) RM_ids.get(i);
 			  id = exp.asString();
 			  
 			  logger.info("Adding data point with id=" + id);
 			  
 			  point = pointMap.get(id);
 			  if (point == null) {
 				if (createNewPoints) {
 			      point = new DataPoint(id);
 			      pointMap.put(id, point);
 				}
 				else {
 				  logger.warn("Could not find id=" + id + " in point map reporter=" + reporter.getReporterName() + " skipping point..");
 				  continue;
 				}
 			  }
 			   
 			  if (axis == AxisType.X_AXIS) {
 			    point.setX(vec[i]);
 			  }
 			  else if (axis == AxisType.Y_AXIS) {
 			    point.setY(vec[i]);
 			  }
 			  else if (axis == AxisType.Z_AXIS) {
 			    point.setZ(vec[i]);
 			  }
 			}
 		
 		} 
 		catch (Exception ex2) {
 			logger.error("Caught exception in setDataPoints method for reporter=" + reporter);
 			logger.error(ex2);
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
 	
 	/**
 	 * 
 	 * @param points
 	 * @param useX
 	 * @param useY
 	 * @return Quoted string for use in R command
 	 */
 	private String getDataString(List<DataPoint> points, boolean useX, boolean useY) {
 	  StringBuffer sb = new StringBuffer();
 	  DataPoint point;
 	  boolean pointAppended = false;
 	  for (Iterator i=points.iterator(); i.hasNext(); ) {
 		 point = (DataPoint) i.next();
 		 
 		 if ((point.getX()!=null)&&(point.getY()!=null)) {
 			 if (pointAppended) {
 		       sb.append(",");
 			 }
 			 if (useX) {
 			   sb.append("\"").append(point.getX()).append("\"");
 			 } 
 			 
 			 
 			 if (useY) {
 			   sb.append("\"").append(point.getY()).append("\"");
 			 }
 			 pointAppended = true;
 		 }
 	  }
 	  sb.append(")");
 	  return sb.toString();
       
 	}
 	  
 }
