 package gov.nih.nci.caintegrator.analysis.server;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import gov.nih.nci.caintegrator.analysis.messaging.*;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 
 /**
  * 
  * Performs Principal Component Analysis using R.
  * 
  * @author harrismic
  *
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
 
 public class PrincipalComponentAnalysisTaskR extends AnalysisTaskR {
 
 	private PrincipalComponentAnalysisResult result = null;
 	
 	private static Logger logger = Logger.getLogger(PrincipalComponentAnalysisTaskR.class);
 
 	public PrincipalComponentAnalysisTaskR(
 			PrincipalComponentAnalysisRequest request) {
 		this(request, false);
 	}
 
 	public PrincipalComponentAnalysisTaskR(
 			PrincipalComponentAnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 	}
 
 	public void run() {
 		PrincipalComponentAnalysisRequest pcaRequest = (PrincipalComponentAnalysisRequest) getRequest();
 		result = new PrincipalComponentAnalysisResult(getRequest()
 				.getSessionId(), getRequest().getTaskId());
 		
 		logger.info(getExecutingThreadName() + " processing principal component analysis request=" + pcaRequest);
 		
 		try {
 			setDataFile(pcaRequest.getDataFileName());
 		} catch (AnalysisServerException e) {
 			logger.error("Internal Error. Error setting data file to fileName=" + pcaRequest.getDataFileName());
 			setException(e);
 			return;
 		}
 		
 		
 		double[] pca1, pca2, pca3;
 		
 		try {
 
 			doRvoidEval("pcaInputMatrix <- dataMatrix");
 	
 			if ((pcaRequest.getSampleGroup()==null)||(pcaRequest.getSampleGroup().size() < 2)) {
 			   //sample group should never be null when passed from middle tier
 			   AnalysisServerException ex = new AnalysisServerException(
 						"Not enough samples for PCA computation.");		 
 			   ex.setFailedRequest(pcaRequest);
 			   setException(ex);
 			   logger.error("pcaRequest has null sample group or not enough samples.");
 			   return;
 			}
 						
 			String rCmd = getRgroupCmd("sampleIds", pcaRequest.getSampleGroup());
 			doRvoidEval(rCmd);
 			rCmd = "pcaInputMatrix <- getSubmatrix.onegrp(pcaInputMatrix, sampleIds)";
 			doRvoidEval(rCmd);
 		
 			if (pcaRequest.getReporterGroup() != null) {
 				rCmd = getRgroupCmd("reporterIds", pcaRequest
 						.getReporterGroup());
 				doRvoidEval(rCmd);
 				rCmd = "pcaInputMatrix <- getSubmatrix.rep(pcaInputMatrix, reporterIds)";
 				doRvoidEval(rCmd);
 			}
 			else {
 			  logger.info("PCA request has null reporter group. Using all reporters.");
 			}
 	
 			
 			if (pcaRequest.getVarianceFilterValue() >= 0.0) {
 				
 				logger.info("Processing principal component analysis request varianceFilterVal="
 								+ pcaRequest.getVarianceFilterValue());
 				doRvoidEval("pcaResult <- computePCAwithVariance(pcaInputMatrix,"
 						+ pcaRequest.getVarianceFilterValue() + " )");
 			} 
 			else if (pcaRequest.doFoldChangeFiltering()) {
 				double foldChangeFilterValue = pcaRequest
 						.getFoldChangeFilterValue();
 				logger.info("Processing principal component analysis request foldChangeFilterVal="
 								+ foldChangeFilterValue);
 				doRvoidEval("pcaResult <- computePCAwithFC(pcaInputMatrix,"
 						+ foldChangeFilterValue + " )");
 			}
 			else {
 			   logger.error("Both variance filter and fold change filter are not active. Can't compute result.");
 			   AnalysisServerException ex = new AnalysisServerException(
 				"Both variance filter and fold change filter are not active");
 			   ex.setFailedRequest(pcaRequest);
 			   setException(ex);
 			   return;
 			}
 	
 			// check to make sure at least 3 components came back
 			int numComponents = doREval("length(pcaResult$x[1,])").asInt();
 			if (numComponents < 3) {
 				AnalysisServerException ex = new AnalysisServerException(
 						"PCA result has less than 3 components.");
 				ex.setFailedRequest(pcaRequest);
 				setException(ex);
 				return;
 			}
 	
 			pca1 = doREval("pcaMatrixX <- pcaResult$x[,1]").asDoubleArray();
 			pca2 = doREval("pcaMatrixY <- pcaResult$x[,2]").asDoubleArray();
 			pca3 = doREval("pcaMatrixZ <- pcaResult$x[,3]").asDoubleArray();
 			REXP exp = doREval("pcaLabels <- dimnames(pcaResult$x)");
 			// System.out.println("Got back xVals.len=" + xVals.length + "
 			// yVals.len=" + yVals.length + " zVals.len=" + zVals.length);
 			Vector labels = (Vector) exp.asVector();
 			Vector sampleIds = ((REXP) (labels.get(0))).asVector();
 	//		Vector pcaLabels = ((REXP) (labels.get(1))).asVector();
 	
 			List<PCAresultEntry> pcaResults = new ArrayList<PCAresultEntry>(
 					sampleIds.size());
 	
 			String sampleId = null;
 			int index = 0;
 			for (Iterator i = sampleIds.iterator(); i.hasNext();) {
 				sampleId = ((REXP) i.next()).asString();
 				pcaResults.add(new PCAresultEntry(sampleId, pca1[index],
 						pca2[index], pca3[index]));
 				index++;
 			}
 	
 			result.setResultEntries(pcaResults);
 			
 		}
 		catch (AnalysisServerException asex) {
 			AnalysisServerException aex = new AnalysisServerException(
			"Problem with PCA computation (Possibly too few samples or reporters specified). Caught AnalysisServerException in PrincipalComponentAnalysisTaskR." + asex.getMessage());
 	        aex.setFailedRequest(pcaRequest);
 	        setException(aex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught AnalysisServerException in PrincipalComponentAnalysisTaskR." + ex.getMessage());
 	        asex.setFailedRequest(pcaRequest);
 	        setException(asex);
 	        return;  
 		}
 
 		// generate the pca1 vs pca2 image
 //		doRvoidEval("maxComp1<-max(abs(pcaResult$x[,1]))");
 //		doRvoidEval("maxComp2<-max(abs(pcaResult$x[,2]))");
 //		doRvoidEval("maxComp3<-max(abs(pcaResult$x[,3]))");
 //		doRvoidEval("xrange<-c(-maxComp1,maxComp1)");
 //		doRvoidEval("yrange<-c(-maxComp2,maxComp2)");
 //		String plot1Cmd = "plot(pcaResult$x[,1],pcaResult$x[,2],xlim=xrange,ylim=yrange,main=\"Component1 Vs Component2\",xlab=\"PC1\",ylab=\"PC2\",pch=20)";
 //		byte[] img1Code = getImageCode(plot1Cmd);
 //		result.setImage1Bytes(img1Code);
 //
 //		// generate the pca1 vs pca3 image
 //		doRvoidEval("yrange<-c(-maxComp3,maxComp3)");
 //		String plot2Cmd = "plot(pcaResult$x[,1],pcaResult$x[,3],xlim=xrange,ylim=yrange,main=\"Component1 Vs Component3\",xlab=\"PC1\",ylab=\"PC3\",pch=20)";
 //		byte[] img2Code = getImageCode(plot2Cmd);
 //		result.setImage2Bytes(img2Code);
 //
 //		// generate the pca2 vs pca3 image
 //		doRvoidEval("xrange<-c(-maxComp2,maxComp2)");
 //		doRvoidEval("yrange<-c(-maxComp3,maxComp3)");
 //		String plot3Cmd = "plot(pcaResult$x[,2],pcaResult$x[,3],xlim=xrange,ylim=yrange,main=\"Component2 Vs Component3\",xlab=\"PC2\",ylab=\"PC3\",pch=20)";
 //		byte[] img3Code = getImageCode(plot3Cmd);
 //		result.setImage3Bytes(img3Code);
 
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 	/**
 	 * Clean up some R memory and release and remove the 
 	 * reference to the R connection so that this task can be 
 	 * garbage collected.
 	 */
 	public void cleanUp() {
 		//doRvoidEval("remove(hcInputMatrix)");
 		//doRvoidEval("remove(mycluster)");
 		try {
 			setRComputeConnection(null);
 		} catch (AnalysisServerException e) {
 		   logger.error("Error in cleanUp method");
 		   logger.error(e);
 		   setException(e);
 		}
 	}
 
 }
