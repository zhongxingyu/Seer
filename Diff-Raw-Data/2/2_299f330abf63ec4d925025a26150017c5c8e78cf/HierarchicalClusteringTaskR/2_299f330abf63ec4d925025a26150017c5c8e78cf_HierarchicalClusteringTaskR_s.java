 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.HierarchicalClusteringRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.HierarchicalClusteringResult;
 import gov.nih.nci.caintegrator.enumeration.ClusterByType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 //import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 /**
  * Performs Hierarchical Clustering using R.
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
 
 public class HierarchicalClusteringTaskR extends AnalysisTaskR {
 
 	private HierarchicalClusteringResult result;
 	
 	public static final int MAX_REPORTERS_FOR_GENE_CLUSTERING = 3000;
 	
 	private static Logger logger = Logger.getLogger(HierarchicalClusteringTaskR.class);
 
 	public HierarchicalClusteringTaskR(HierarchicalClusteringRequest request) {
 		this(request, false);
 	}
 
 	public HierarchicalClusteringTaskR(HierarchicalClusteringRequest request,
 			boolean debugRcommands) {
 		super(request, debugRcommands);
 	}
 	
 	public HierarchicalClusteringRequest getRequest() {
 		return (HierarchicalClusteringRequest) super.getRequest();
 	}
 	
 	
 	/**
 	 * This method is used to keep an enumerated type value change from breaking the call to
 	 * the R function. The R function is expecting an exact match on the string passed 
 	 * as a parameter.
 	 * @return the quoted string representing the distance matrix type.
 	 */
 	public String getDistanceMatrixRparamStr() {
 	  switch(getRequest().getDistanceMatrix()) {
 	  case Correlation : return getQuotedString("Correlation");
 	  case Euclidean : return getQuotedString("Euclidean");
 	  }
 	  return null;
 	}
 	
 	/**
 	 * This method is used to keep an enumerated type value change from breaking the call to
 	 * the R function. The R function is expecting an exact match on the string passed 
 	 * as a parameter.
 	 * @return the quoted string representing the linkage method 
 	 */
 	public String getLinkageMethodRparamStr() {
 	  switch(getRequest().getLinkageMethod()) {
 	  case Average: return getQuotedString("average");
 	  case Complete: return getQuotedString("complete");
 	  case Single: return getQuotedString("single");
 	  }
 	  return null;
 	}
 
 	/**
 	 * Implement Hierarchical
 	 */
 	public void run() {
 		HierarchicalClusteringRequest hcRequest = (HierarchicalClusteringRequest) getRequest();
 		result = new HierarchicalClusteringResult(getRequest().getSessionId(),
 				getRequest().getTaskId());
 		logger.info(getExecutingThreadName() + " processing hierarchical clustering analysis request="
 						+ hcRequest);
 		
 		try {
 			setDataFile(hcRequest.getDataFileName());
 		} catch (AnalysisServerException e) {
 			logger.error("Internal Error. Error setting data file to fileName=" + hcRequest.getDataFileName());
 			setException(e);
 			return;
 		}
 
 		try {
 		
 			// get the submatrix to operate on
 			doRvoidEval("hcInputMatrix <- dataMatrix");
 	
 			doRvoidEval("hcInputMatrix <- GeneFilterWithVariance(hcInputMatrix,"
 					+ hcRequest.getVarianceFilterValue() + ")");
 			
 			
 	
 			String rCmd = null;
 			
 			if ((hcRequest.getSampleGroup()==null)||(hcRequest.getSampleGroup().size() < 2)) {
 			   //sample group should never be null when passed from middle tier
 			   AnalysisServerException ex = new AnalysisServerException(
 						"Not enough samples to cluster.");		 
 			   ex.setFailedRequest(hcRequest);
 			   setException(ex);
 			   logger.error("Sample group is null or not enough samples for Hierarchical clustering.");
 			   return;
 			}
 			
 			rCmd = getRgroupCmd("sampleIds", hcRequest.getSampleGroup());
 			doRvoidEval(rCmd);
 			rCmd = "hcInputMatrix <- getSubmatrix.onegrp(hcInputMatrix, sampleIds)";
 			doRvoidEval(rCmd);
 				
 			if (hcRequest.getReporterGroup() != null) {
 				rCmd = getRgroupCmd("reporterIds", hcRequest.getReporterGroup());
 				doRvoidEval(rCmd);
 				rCmd = "hcInputMatrix <- getSubmatrix.rep(hcInputMatrix, reporterIds)";
 				doRvoidEval(rCmd);
 			}
 			
 			
 			String plotCmd = null;
 			// get the request parameters
 			if (hcRequest.getClusterBy() == ClusterByType.Samples) {
 				// cluster by samples
 				rCmd = "mycluster <- mysamplecluster(hcInputMatrix,"
 						+ getDistanceMatrixRparamStr()
 						+ ","
 						+ getLinkageMethodRparamStr()
 						+ ")";
 				doRvoidEval(rCmd);
 				plotCmd = "plot(mycluster, labels=dimnames(hcInputMatrix)[[2]], xlab=\"\", ylab=\"\",ps=8,sub=\"\", hang=-1)";
 			} else if (hcRequest.getClusterBy() == ClusterByType.Genes) {
 				// cluster by genes
 				
 				//check the hcInputMatrix size. If there are more than 1000 reporters then 
 				//throw an exception. 
 				
 	//			check to see if the number of reporters to be used for the clustering is 
 				//too large. If it is then return an error
 				
 				int numReportersToUse = doREval("dim(hcInputMatrix)[1]").asInt();
 				
 				if (numReportersToUse > MAX_REPORTERS_FOR_GENE_CLUSTERING) {
 					AnalysisServerException ex = new AnalysisServerException(
 					"Too many reporters to cluster , try increasing the variance filter value, attempted to use numReporters=" + numReportersToUse);
 					ex.setFailedRequest(hcRequest);
 					setException(ex);
 					logger.info("Attempted to use numReporters=" + numReportersToUse + " in hcClustering. Returning exception.");
 					return;
 				}
 				
 				
 				rCmd = "mycluster <- mygenecluster(hcInputMatrix,"
 						+ getDistanceMatrixRparamStr()
 						+ ","
 						+ getLinkageMethodRparamStr()
 						+ ")";
 				doRvoidEval(rCmd);
 				plotCmd = "plot(mycluster, labels=dimnames(hcInputMatrix)[[1]], xlab=\"\", ylab=\"\",ps=8,sub=\"\", hang=-1)";
 			}
 			else {
 				AnalysisServerException ex = new AnalysisServerException("Unrecognized cluster by type");
 				ex.setFailedRequest(hcRequest);
 				setException(ex);
 				logger.error("Unrecognized cluster by type");
 				return;
 			}
 	
 			
 			Vector orderedLabels = doREval("clusterLabels <-  mycluster$labels[mycluster$order]").asVector();
 			float numPix = (float)orderedLabels.size() * 15.0f;
 			int imgWidth = Math.round(numPix/72.0f);
 			imgWidth = Math.max(3, imgWidth);
 			int imgHeight = 10;
 			
 			byte[] imgCode = getImageCode(plotCmd, imgHeight, imgWidth);
 			result.setImageCode(imgCode);
 			
 			List<String> orderedLabelList = new ArrayList<String>(orderedLabels.size());
 			String label = null;
 			for (int i=0; i < orderedLabels.size(); i++ ) {
 			  label = ((REXP) orderedLabels.get(i)).asString();
 			  orderedLabelList.add(i,label);
 			}
 			
 			if (hcRequest.getClusterBy() == ClusterByType.Genes) {
 			  result.setClusteredReporterIDs(orderedLabelList);
 			}
 			else if (hcRequest.getClusterBy() == ClusterByType.Samples) {
 			  result.setClusteredSampleIDs(orderedLabelList);
 			}
 		}
 		catch (AnalysisServerException asex) {
 			AnalysisServerException aex = new AnalysisServerException(
			"Internal Error. Caught AnalysisServerException in HierarchicalClusteringTaskR." + asex.getMessage());
 	        aex.setFailedRequest(hcRequest);
 	        setException(aex);
 	        return;  
 		}
 		catch (Exception ex) {
 			AnalysisServerException asex = new AnalysisServerException(
 			"Internal Error. Caught AnalysisServerException in HierarchicalClusteringTaskR." + ex.getMessage());
 	        asex.setFailedRequest(hcRequest);
 	        setException(asex);
 	        return;  
 		}
 		
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 	/**
 	 * Clean up some of the memory on the R server
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
