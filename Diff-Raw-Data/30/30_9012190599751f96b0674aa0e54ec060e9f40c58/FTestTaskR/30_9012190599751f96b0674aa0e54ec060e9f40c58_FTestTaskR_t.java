 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.FTestRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.FTestResult;
 import gov.nih.nci.caintegrator.analysis.messaging.FTestResultEntry;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.enumeration.MultiGroupComparisonAdjustmentType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 public class FTestTaskR extends AnalysisTaskR {
 
     private FTestResult result;
 	private static Logger logger = Logger.getLogger(FTestTaskR.class);
 	private Comparator ftComparator = new FTestComparator();
 
 	public FTestTaskR(AnalysisRequest request) {
 		this(request, false);
 	}
 
 	public FTestTaskR(AnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 	}
 
 	@Override
 	public void run() {
 		FTestRequest ftRequest = (FTestRequest) getRequest();
 		result = new FTestResult(getRequest().getSessionId(), getRequest().getTaskId());
 		logger.info(getExecutingThreadName() + " processing FTestRequest request="
 						+ ftRequest);
 		
 		
 		//check for overlap between the groups and for groups with too few members
 		List<SampleGroup> groups = ftRequest.getSampleGroups();
 		boolean errorCondition = false;
 		SampleGroup idsSeen = new SampleGroup();
 		String errorMsg = null;
 		for (SampleGroup group : groups) {
 		  if (group.size() < 2) {
 			 errorMsg = "Group: " + group.getGroupName() + " has less than two members. Sending exception.";
 		     logger.error(errorMsg);
 		     errorCondition = true;
 		     break;
 		  }
 		  
 		  if (idsSeen.containsAny(group)) {
 			errorMsg = "Group: " + group.getGroupName() + " contains overlapping ids. Sending exception.";
 		 	logger.error(errorMsg);
 		 	errorCondition = true;
 		 	break;
 		  }
 		  
 		  idsSeen.addAll(group);
 		}
 		
 		if (errorCondition) {
 	      AnalysisServerException ex = new AnalysisServerException("One or more groups have overlapping members or contain less than 3 entries.");
 		  ex.setFailedRequest(ftRequest);
 		  logger.error("Groups have overlapping members or less than 3 entries.");
 		  setException(ex);
 		  return;
 		}
 		
 		
 		try {
 			
 			String dataFileName = ftRequest.getDataFileName();
 			
 			if (dataFileName != null) {
 			  setDataFile(ftRequest.getDataFileName());
 			}
 			else {
 			  throw new AnalysisServerException("Null data file name");
 			}
 			
 		} catch (AnalysisServerException e) {
 			e.setFailedRequest(ftRequest);
 			logger.error("Internal Error. " + e.getMessage());
 			logStackTrace(logger, e);
 			setException(e);
 			return;
 		}
 
 		
 		try {
 		
 			List<SampleGroup> sampleGroups = ftRequest.getSampleGroups();
 			SampleGroup grp = null;
 			String cmd;
 			String grpName = null;
 			String bindCmd = "compMat <- cbind(";
 		    String matName = null;
 		    String phenoCmd = "pheno <- as.factor(c(";
 		    
 		    result.setSampleGroups(sampleGroups);
 		    
 			for (int i=0; i < sampleGroups.size(); i++) {
 			  grp = (SampleGroup)sampleGroups.get(i);
 			  grpName = "GRP" + i;
 			  cmd = getRgroupCmd(grpName, grp);
 			  doRvoidEval(cmd);
 			  matName = "M" + grpName;
 			  cmd = matName + " <- getSubmatrix.onegrp(dataMatrix," + grpName + ")";
 			  doRvoidEval(cmd);
 			 
 			  bindCmd += matName;
 			  
 			  phenoCmd += "rep(" + i + "," + grp.size() + ")";
 			  
 			  
 			  if (i < sampleGroups.size()-1) {
 			    bindCmd += ",";
 			    phenoCmd += ",";
 			  }
 			  else {
 				bindCmd += ")"; 
 				phenoCmd += "))";
 			  }
 			  
 			  //to build pheno matrix use c(rep(i,grp.size()) 
 			  //then outside the loop use pheno <- as.factor(pheno)
 			}
 			
 			doRvoidEval(bindCmd);
 			doRvoidEval(phenoCmd);
 			
 			//now call the Ftest function
 			cmd ="ftResult <- Ftests(compMat,pheno)";
 			
 			doRvoidEval(cmd);
 			
 			
 			// do filtering
             String rCmd;
 			double foldChangeThreshold = ftRequest.getFoldChangeThreshold();
 			double pValueThreshold = ftRequest.getPValueThreshold();
 			MultiGroupComparisonAdjustmentType adjMethod = ftRequest.getMultiGrpComparisonAdjType();
 
 			if (adjMethod == MultiGroupComparisonAdjustmentType.NONE) {
 				// get differentially expressed reporters using
 				// unadjusted Pvalue
 					
 				rCmd = "ftResult  <- filterDiffExpressedGenes.FTest(ftResult,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				result.setArePvaluesAdjusted(false);
 			} else if (adjMethod == MultiGroupComparisonAdjustmentType.FDR) {
 				// do adjustment
				rCmd = "adjust.result <- adjustP.Benjamini.Hochberg.Ftest(ftResult)";
 				doRvoidEval(rCmd);
 				// get differentially expressed reporters using adjusted Pvalue
 				rCmd = "ftResult  <- filterDiffExpressedGenes.FTest.adjustP(adjust.result,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				result.setArePvaluesAdjusted(true);
 			} else if (adjMethod == MultiGroupComparisonAdjustmentType.FWER) {
 				// do adjustment
				rCmd = "adjust.result <- adjustP.Bonferroni.Ftest(ftResult)";
 				doRvoidEval(rCmd);
 				// get differentially expresseed reporters using adjusted Pvalue
 				rCmd = "ftResult  <- filterDiffExpressedGenes.FTest.adjustP(adjust.result,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				result.setArePvaluesAdjusted(true);
 			}
 			else {
 				logger.error("FTest Adjustment Type unrecognized.");
 				this.setException(new AnalysisServerException("Internal error: unrecognized adjustment type."));
 				return;
 			}
 			
 			
 			double[][] grpMean = new double[sampleGroups.size()][];
 			int ind;
 			for (int i=0; i < sampleGroups.size(); i++) {
 			  ind = i+1;
 		      grpMean[i] = doREval("mean <- ftResult[," + ind + "]").asDoubleArray();
 			}
 			
 			//double[][] grpMean = doREval("mean <- as.matrix(ftResult[,1:" + sampleGroups.size()  + "])").asDoubleMatrix();
 	
 			logger.info("grpMean[0].length=" + grpMean[0].length);
 			
 			
 			double[] maxFoldChange = doREval("maxFC <- ftResult$mfc").asDoubleArray();
 			
			double[] pval;
 			
			if (adjMethod==MultiGroupComparisonAdjustmentType.NONE) {
			  pval = doREval("pval <- ftResult$pval").asDoubleArray();
			}
			else {
			  pval = doREval("pval <- ftResult$adjustP").asDoubleArray();
			}
			
			//double[] pval = doREval("pval <- ftResult$pval").asDoubleArray();
 		
 			logger.info("FTest: maxFoldChange.length=" + maxFoldChange.length);
 			logger.info("FTest: pval.length=" + pval.length);
 			
 //			 get the labels
 			Vector reporterIds = doREval("ftLabels <- dimnames(ftResult)[[1]]")
 					.asVector();
 			
 			logger.info("reporterIds.size=" + reporterIds.size());
 			
 			
 			
 			// load the result object
 			// need to see if this works for single group comparison
 			List<FTestResultEntry> resultEntries = new ArrayList<FTestResultEntry>(
 					maxFoldChange.length);
 			FTestResultEntry resultEntry;
 	        int numEntries = maxFoldChange.length;
 	        double[] grpm;
 	       
 			for (int i = 0; i < numEntries; i++) {
 				resultEntry = new FTestResultEntry();
				
				if (numEntries == 1) {
				   //had to do this work around because rServe doesn't seem to handle
				   //vectors of size one correctly
				   String reporterId = doREval("rId <- dimnames(ftResult)[[1]]").asString();
				   resultEntry.setReporterId(reporterId);
				}
				else {
				   resultEntry.setReporterId(((REXP) reporterIds.get(i)).asString());
				}
				
 				grpm = new double[sampleGroups.size()];
 				for (int j=0; j <  sampleGroups.size(); j++) {
 				  grpm[j] = grpMean[j][i];
 				  resultEntry.setGroupMeans(grpm);
 				}
 				
 				resultEntry.setMaximumFoldChange(maxFoldChange[i]);
 				resultEntry.setPvalue(pval[i]);
 				resultEntries.add(resultEntry);
 			}
 			
 			
 			Collections.sort(resultEntries, ftComparator);
 	
 			result.setResultEntries(resultEntries);
 	
 			
 			
 			//create the data matrix
 			//computeMatrix 
 			//for (each group) {
 			  //submat[i] <- getSubmatrix(dataMatrix, groupIds[i])
 			  //computeMatix <- cbind(submat[i])
 			//pheno <- as.factor(i,rep(length(group)[i])
 			//perform the FTest 
 			
 			
 			
 			
 		}
 		catch (AnalysisServerException asex) {
 			AnalysisServerException aex = new AnalysisServerException(
 			"Problem computing FTest. Caught AnalysisServerException in FTestTaskR." + asex.getMessage());
 	        aex.setFailedRequest(ftRequest);
 	        setException(aex);
 	        logger.error("Caught AnalysisServerException in FTestTaskR");
 	        logStackTrace(logger, asex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught Exception in FTestTaskR exClass=" + ex.getClass() + " msg=" + ex.getMessage());
 	        asex.setFailedRequest(ftRequest);
 	        setException(asex);
 	        logger.error("Caught Exception in FTestTaskR");
 	        logStackTrace(logger, ex);
 	        return;  
 		}
 
 	}
 
 	@Override
 	public void cleanUp() {
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 			logger.error("Error in cleanUp method.");
 			logStackTrace(logger, e);
 			setException(e);
 		}
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 }
