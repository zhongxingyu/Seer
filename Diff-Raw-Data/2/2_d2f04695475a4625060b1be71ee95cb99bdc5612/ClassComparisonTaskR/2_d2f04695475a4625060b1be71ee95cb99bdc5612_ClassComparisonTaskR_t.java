 package gov.nih.nci.caintegrator.analysis.server;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.*;
 
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonResult;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonResultEntry;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.enumeration.*;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.*;
 
 /**
  * Performs the class comparison computation using R.
  * 
  * @author harrismic
  *
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ClassComparisonTaskR extends AnalysisTaskR {
 
 	private ClassComparisonResult ccResult = null;
 	private Comparator classComparisonComparator = new ClassComparisonComparator();
 	public static final int MIN_GROUP_SIZE = 3;
 	
 	private static Logger logger = Logger.getLogger(ClassComparisonTaskR.class);
 
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
 
 		logger.info(getExecutingThreadName() + ": processing class comparison request=" + ccRequest);
 
 		
 		
 		//set the data file
 //		check to see if the data file on the compute connection is the 
 		//same as that for the analysis task
 		
 		
 		try {
 			setDataFile(ccRequest.getDataFileName());
 		} catch (AnalysisServerException e) {
 			e.setFailedRequest(ccRequest);
 			logger.error("Internal Error. Error setting data file to fileName=" + ccRequest.getDataFileName());
 			setException(e);
 			return;
 		}
 		
 		
 		SampleGroup group1 = ccRequest.getGroup1();
 		SampleGroup baselineGroup = ccRequest.getBaselineGroup();
 		
 		if ((group1 == null) || (group1.size() < MIN_GROUP_SIZE)) {
 			  AnalysisServerException ex = new AnalysisServerException(
 				"Group1 is null or has less than " + MIN_GROUP_SIZE + " entries.");		 
 		      ex.setFailedRequest(ccRequest);
 		      setException(ex);
 		      return;
 		}
 		
 		
 		if ((baselineGroup == null) || (baselineGroup.size() < MIN_GROUP_SIZE)) {
 			  AnalysisServerException ex = new AnalysisServerException(
 				"BaselineGroup is null or has less than " + MIN_GROUP_SIZE + " entries.");		 
 		      ex.setFailedRequest(ccRequest);
 		      setException(ex);
 		      return;
 		}
 		
 		
 		//check to see if there are any overlapping samples between the two groups
 		if ((group1 != null)&&(baselineGroup != null)) {
 		  
 		  //get overlap between the two sets
 		  Set<String> intersection = new HashSet<String>();
 		  intersection.addAll(group1);
 		  intersection.retainAll(baselineGroup);
 		  
 		  if (intersection.size() > 0) {
 		     //the groups are overlapping so return an exception
 			 StringBuffer ids = new StringBuffer();
 			 for (Iterator i=intersection.iterator(); i.hasNext(); ) {
 			   ids.append(i.next());
 			   if (i.hasNext()) {
 			     ids.append(",");
 			   }
 			 }
 			 
 			 AnalysisServerException ex = new AnalysisServerException(
 				      "Can not perform class comparison with overlapping groups. Overlapping ids=" + ids.toString());		 
 				      ex.setFailedRequest(ccRequest);
 				      setException(ex);
 				      return;   
 		  }
 		}
 		
 		//For now assume that there are two groups. When we get data for two channel array then
 		//allow only one group so leaving in the possiblity of having only one group in the code 
 		//below eventhough the one group case won't be executed because of the tests above.
 		
 		
 		int grp1Len = 0, baselineGrpLen = 0;
 		
 		grp1Len = group1.size();
 		
 		String grp1RName = "GRP1IDS";
 		String baselineGrpRName = "BLGRPIDS";
 		
 		
 		String rCmd = null;
 	
 		rCmd = getRgroupCmd(grp1RName, group1);
 
 		try {
 		
 			doRvoidEval(rCmd);
 	
 			if (baselineGroup != null) {
 				// two group comparison
 				baselineGrpLen = baselineGroup.size();
 				
 				rCmd = getRgroupCmd(baselineGrpRName, baselineGroup);
 				doRvoidEval(rCmd);
 	
 				// create the input data matrix using the sample groups
 				rCmd = "ccInputMatrix <- getSubmatrix.twogrps(dataMatrix,"
 						+ grp1RName + "," + baselineGrpRName + ")";
 				doRvoidEval(rCmd);
 	
 				// check to make sure all identifiers matched in the R data file
 				rCmd = "dim(ccInputMatrix)[2]";
 				int numMatched = doREval(rCmd).asInt();
 				if (numMatched != (grp1Len + baselineGrpLen)) {
 					AnalysisServerException ex = new AnalysisServerException(
 							"Some sample ids did not match R data file for class comparison request.");
 					ex.setFailedRequest(ccRequest);
 					setException(ex);
 					return;
 				}
 			} else {
 				// single group comparison
 //				baselineGrpLen = 0;
 //				rCmd = "ccInputMatrix <- getSubmatrix.onegrp(dataMatrix,"
 //						+ grp1RName + ")";
 //				doRvoidEval(rCmd);
 				logger.error("Single group comparison is not currently supported.");
 				throw new AnalysisServerException("Unsupported operation: Attempted to do a single group comparison.");
 			}
 	
 			rCmd = "dim(ccInputMatrix)[2]";
 			int numMatched = doREval(rCmd).asInt();
 			if (numMatched != (grp1Len + baselineGrpLen)) {
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
 						+ baselineGrpLen + ")";
 				doRvoidEval(rCmd);
 			} else if (ccRequest.getStatisticalMethod() == StatisticalMethodType.Wilcoxin) {
 				// do the Wilcox computation
 				rCmd = "ccResult <- mywilcox(ccInputMatrix, " + grp1Len + ","
 						+ baselineGrpLen + ")";
 				doRvoidEval(rCmd);
 			}
 			else {
 			  logger.error("ClassComparision unrecognized statistical method.");
 			  this.setException(new AnalysisServerException("Internal error: unrecognized adjustment type."));
 			  return;
 			}
 	
 			// do filtering
 			double foldChangeThreshold = ccRequest.getFoldChangeThreshold();
 			double pValueThreshold = ccRequest.getPvalueThreshold();
 			MultiGroupComparisonAdjustmentType adjMethod = ccRequest
 					.getMultiGroupComparisonAdjustmentType();
 			if (adjMethod == MultiGroupComparisonAdjustmentType.NONE) {
 				// get differentially expressed reporters using
 				// unadjusted Pvalue
 	
 				// shouldn't need to pass in ccInputMatrix
 				rCmd = "ccResult  <- mydiferentiallygenes(ccResult,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				ccResult.setPvaluesAreAdjusted(false);
 			} else if (adjMethod == MultiGroupComparisonAdjustmentType.FDR) {
 				// do adjustment
 				rCmd = "adjust.result <- adjustP.Benjamini.Hochberg(ccResult)";
 				doRvoidEval(rCmd);
 				// get differentially expressed reporters using adjusted Pvalue
 				rCmd = "ccResult  <- mydiferentiallygenes.adjustP(adjust.result,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				ccResult.setPvaluesAreAdjusted(true);
 			} else if (adjMethod == MultiGroupComparisonAdjustmentType.FWER) {
 				// do adjustment
 				rCmd = "adjust.result <- adjustP.Bonferroni(ccResult)";
 				doRvoidEval(rCmd);
 				// get differentially expresseed reporters using adjusted Pvalue
 				rCmd = "ccResult  <- mydiferentiallygenes.adjustP(adjust.result,"
 						+ foldChangeThreshold + "," + pValueThreshold + ")";
 				doRvoidEval(rCmd);
 				ccResult.setPvaluesAreAdjusted(true);
 			}
 			else {
 				logger.error("ClassComparision Adjustment Type unrecognized.");
 				this.setException(new AnalysisServerException("Internal error: unrecognized adjustment type."));
 				return;
 			}
 	
 			// get the results and send
 	
 //			double[] meanGrp1 = doREval("mean1 <- ccResult[,1]").asDoubleArray();
 //			double[] meanBaselineGrp = doREval("meanBaseline <- ccResult[,2]").asDoubleArray();
 //			double[] meanDif = doREval("meanDif <- ccResult[,3]").asDoubleArray();
 //			double[] absoluteFoldChange = doREval("fc <- ccResult[,4]").asDoubleArray();
 //			double[] pva = doREval("pva <- ccResult[,5]").asDoubleArray();
 	
 			double[] meanGrp1 = doREval("mean1 <- ccResult$mean1").asDoubleArray();
 			double[] meanBaselineGrp = doREval("meanBaseline <- ccResult$mean2").asDoubleArray();
			double[] meanDif = doREval("meanDif <- ccResult$mean.dif").asDoubleArray();
 			double[] absoluteFoldChange = doREval("fc <- ccResult$fc").asDoubleArray();
 			double[] pva = doREval("pva <- ccResult$pval").asDoubleArray();
 			double[] stdG1 = doREval("stdG1 <- ccResult$std1").asDoubleArray();
 			double[] stdBaseline = doREval("stdBL <- ccResult$std2").asDoubleArray();
 			
 			
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
 				resultEntry.setMeanBaselineGrp(meanBaselineGrp[i]);
 				resultEntry.setMeanDiff(meanDif[i]);
 				resultEntry.setAbsoluteFoldChange(absoluteFoldChange[i]);
 				resultEntry.setPvalue(pva[i]);
 				resultEntry.setStdGrp1(stdG1[i]);
 				resultEntry.setStdBaselineGrp(stdBaseline[i]);
 				resultEntries.add(resultEntry);
 			}
 			
 			
 			Collections.sort(resultEntries, classComparisonComparator);
 	
 			ccResult.setResultEntries(resultEntries);
 	
 			ccResult.setGroup1(group1);
 			if (baselineGroup != null) {
 				ccResult.setBaselineGroup(baselineGroup);
 			}
 		}
 		catch (AnalysisServerException asex) {
 			AnalysisServerException aex = new AnalysisServerException(
 			"Internal Error. Caught AnalysisServerException in ClassComparisonTaskR." + asex.getMessage());
 	        aex.setFailedRequest(ccRequest);
 	        setException(aex);
 	        logger.error(asex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught AnalysisServerException in ClassComparisonTaskR." + ex.getMessage());
 	        asex.setFailedRequest(ccRequest);
 	        setException(asex);
 	        logger.error(ex);
 	        return;  
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
 		//doRvoidEval("remove(ccInputMatrix)");
 		//doRvoidEval("remove(ccResult)");
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 			logger.error("Error in cleanUp method.");
 			logger.error(e);
 			setException(e);
 		}
 	}
 }
