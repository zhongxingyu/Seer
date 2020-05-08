 package gov.nih.nci.caintegrator.analysis.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonResult;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonResultEntry;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonRequest.ComparisonAdjustmentMethod;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonRequest.StatisticalMethodType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import org.rosuda.JRclient.*;
 
 public class ClassComparisonTaskR extends AnalysisTaskR {
 
 	private ClassComparisonResult ccResult = null;
 
 	public ClassComparisonTaskR(ClassComparisonRequest request) {
 		this(request, false);
 	}
 
 	public ClassComparisonTaskR(ClassComparisonRequest request,
 			boolean debugRcommands) {
 		super(request, debugRcommands);
 	}
 
 	public void run() {
 
 		ClassComparisonRequest ccRequest = (ClassComparisonRequest) getRequest();
 		
 		ccResult = new ClassComparisonResult(ccRequest.getSessionId(), ccRequest.getTaskId());
 
 		System.out.println(getExecutingThreadName() + ": processing class comparison request=" + ccRequest);
 
 		int grp1Len = 0, grp2Len = 0;
 		SampleGroup group1 = ccRequest.getGroup1();
 		grp1Len = group1.size();
 		SampleGroup group2 = ccRequest.getGroup2();
 		String rCmd = null;
 		rCmd = getRgroupCmd(group1.getGroupName(), group1);
 
 		doRvoidEval(rCmd);
 
 		if (group2 != null) {
 			// two group comparison
 			grp2Len = group2.size();
 			rCmd = getRgroupCmd(group2.getGroupName(), group2);
 			doRvoidEval(rCmd);
 
 			// create the input data matrix using the sample groups
 			rCmd = "ccInputMatrix <- getSubmatrix.twogrps(dataMatrix,"
 					+ group1.getGroupName() + "," + group2.getGroupName() + ")";
 			doRvoidEval(rCmd);
 
 			// check to make sure all identifiers matched in the R data file
 			rCmd = "dim(ccInputMatrix)[2]";
 			int numMatched = doREval(rCmd).asInt();
 			if (numMatched != (grp1Len + grp2Len)) {
 				AnalysisServerException ex = new AnalysisServerException(
 						"Some sample ids did not match R data file for class comparison request.");
 				ex.setFailedRequest(ccRequest);
 				setException(ex);
				return;
 			}
 		} else {
 			// single group comparison
 			grp2Len = 0;
 			rCmd = "ccInputMatrix <- getSubmatrix.onegrp(dataMatrix,"
 					+ group1.getGroupName() + ")";
 			doRvoidEval(rCmd);
 		}
 
 		rCmd = "dim(ccInputMatrix)[2]";
 		int numMatched = doREval(rCmd).asInt();
 		if (numMatched != (grp1Len + grp2Len)) {
 			AnalysisServerException ex = new AnalysisServerException(
 					"Some sample ids did not match R data file for class comparison request.");
 			ex.setFailedRequest(ccRequest);
 			ex.setFailedRequest(ccRequest);
 			setException(ex);
 			return;
 		}
 
 		if (ccRequest.getStatisticalMethod() == StatisticalMethodType.TTest) {
 			// do the TTest computation
 			rCmd = "ccResult <- myttest(ccInputMatrix, " + grp1Len + ","
 					+ grp2Len + ")";
 			doRvoidEval(rCmd);
 		} else if (ccRequest.getStatisticalMethod() == StatisticalMethodType.Wilcox) {
 			// do the Wilcox computation
 			rCmd = "ccResult <- mywilcox(ccInputMatrix, " + grp1Len + ","
 					+ grp2Len + ")";
 			doRvoidEval(rCmd);
 		}
 
 		// do filtering
 		double foldChangeThreshold = ccRequest.getFoldChangeThreshold();
 		double pValueThreshold = ccRequest.getPvalueThreshold();
 		ComparisonAdjustmentMethod adjMethod = ccRequest
 				.getComparisonAdjustmentMethod();
 		if (adjMethod == ComparisonAdjustmentMethod.NONE) {
 			// get differentially expressed reporters using
 			// unadjusted Pvalue
 
 			// shouldn't need to pass in ccInputMatrix
 			rCmd = "ccResult  <- mydiferentiallygenes(ccResult,"
 					+ foldChangeThreshold + "," + pValueThreshold + ")";
 			doRvoidEval(rCmd);
 			ccResult.setPvaluesAreAdjusted(false);
 		} else if (adjMethod == ComparisonAdjustmentMethod.FDR) {
 			// do adjustment
 			rCmd = "adjust.result <- adjustP.Benjamini.Hochberg(ccResult)";
 			doRvoidEval(rCmd);
 			// get differentially expressed reporters using adjusted Pvalue
 			rCmd = "ccResult  <- mydiferentiallygenes.adjustP(adjust.result,"
 					+ foldChangeThreshold + "," + pValueThreshold + ")";
 			doRvoidEval(rCmd);
 			ccResult.setPvaluesAreAdjusted(true);
 		} else if (adjMethod == ComparisonAdjustmentMethod.FWER) {
 			// do adjustment
 			rCmd = "adjust.result <- adjustP.Bonferroni(ccResult)";
 			doRvoidEval(rCmd);
 			// get differentially expresseed reporters using adjusted Pvalue
 			rCmd = "ccResult  <- mydiferentiallygenes.adjustP(adjust.result,"
 					+ foldChangeThreshold + "," + pValueThreshold + ")";
 			doRvoidEval(rCmd);
 			ccResult.setPvaluesAreAdjusted(true);
 		}
 
 		// get the results and send
 
 		double[] meanGrp1 = doREval("mean1 <- ccResult[,1]").asDoubleArray();
 		double[] meanGrp2 = doREval("mean2 <- ccResult[,2]").asDoubleArray();
 		double[] meanDif = doREval("meanDif <- ccResult[,3]").asDoubleArray();
 		double[] foldChange = doREval("fc <- ccResult[,4]").asDoubleArray();
 		double[] pva = doREval("pva <- ccResult[,5]").asDoubleArray();
 
 		// get the labels
 		Vector reporterIds = doREval("ccLabels <- dimnames(ccResult)[[1]]")
 				.asVector();
 
 		// load the result object
 		// need to see if this works for single group comparison
 		List<ClassComparisonResultEntry> resultEntries = new ArrayList<ClassComparisonResultEntry>(
 				meanGrp1.length);
 		ClassComparisonResultEntry resultEntry;
 
 		for (int i = 0; i < meanGrp1.length; i++) {
 			resultEntry = new ClassComparisonResultEntry();
 			resultEntry.setReporterId(((REXP) reporterIds.get(i)).asString());
 			resultEntry.setMeanGrp1(meanGrp1[i]);
 			resultEntry.setMeanGrp2(meanGrp2[i]);
 			resultEntry.setMeanDiff(meanDif[i]);
 			resultEntry.setFoldChange(foldChange[i]);
 			resultEntry.setPvalue(pva[i]);
 			resultEntries.add(resultEntry);
 		}
 
 		ccResult.setResultEntries(resultEntries);
 
 		ccResult.setGroup1(group1);
 		if (group2 != null) {
 			ccResult.setGroup2(group2);
 		}
 	}
 
 	public AnalysisResult getResult() {
 		return ccResult;
 	}
 
 	public ClassComparisonResult getClassComparisonResult() {
 		return ccResult;
 	}
 
 	/**
 	 * Clean up some of the resources
 	 */
 	public void cleanUp() {
 		doRvoidEval("remove(ccInputMatrix)");
 		doRvoidEval("remove(ccResult)");
 		setRconnection(null);
 	}
 }
