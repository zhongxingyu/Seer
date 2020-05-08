 /*
  * Created on Sep 14, 2004
  *
  */
 package gov.nih.nci.rembrandt.queryservice;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.view.GroupType;
 import gov.nih.nci.rembrandt.dbbean.PatientData;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.cgh.CopyNumber;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.UnifiedGeneExpr;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr.GeneExprGroup;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr.GeneExprSingle;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.UnifiedGeneExpr.UnifiedGeneExprGroup;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.UnifiedGeneExpr.UnifiedGeneExprSingle;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprDiseaseGroupViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.geneExpressionPlot.DiseaseGeneExprPlotResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.geneExpressionPlot.GeneExprDiseasePlotContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.geneExpressionPlot.GeneExprDiseasePlotHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewHandler;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author SahniH
  *
  * This class takes a DifferentialExpressionSfact and DifferentialExpressionGfact object and helps create a GeneCentricViewHandler or a SampleCentricViewHandler classes.
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
 
 public class ResultsetProcessor {
 	private static Logger logger = Logger.getLogger(ResultsetProcessor.class);
  	public static ResultsContainer handleGeneExprSingleView(Resultant resultant, GeneExpr.GeneExprSingle[] geneExprObjects, GroupType groupType) throws Exception{
 		DimensionalViewContainer dimensionalViewContainer = null;;
      	GeneExprSingleViewResultsContainer geneExprSingleResultsContainer = null;
     	SampleViewResultsContainer sampleViewResultsContainer;
   		if(resultant != null && resultant.getResultsContainer() instanceof DimensionalViewContainer){
  			dimensionalViewContainer = (DimensionalViewContainer) resultant.getResultsContainer();
   	    	sampleViewResultsContainer = dimensionalViewContainer.getSampleViewResultsContainer();
   			geneExprSingleResultsContainer = dimensionalViewContainer.getGeneExprSingleViewContainer();
   			if(geneExprSingleResultsContainer == null){
   	  			geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
   			}
 //  		Populate sampleViewResultsContainer with ClinicalData
 	        try {
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer,geneExprObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
   			geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
   	    	//sampleViewResultsContainer = new SampleViewResultsContainer();
 	        //Populate sampleViewResultsContainer with ClinicalData
 	        try {
 	        	sampleViewResultsContainer = new SampleViewResultsContainer();
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer,geneExprObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
   		}
  		ResultsContainer resultsContainer = null;
           for (int i = 0; i < geneExprObjects.length; i++) {
     		if(geneExprObjects[i] != null) {
             ResultSet obj = geneExprObjects[i];
             	if (obj instanceof GeneExpr.GeneExprSingle)  {
 	              	//Propulate the GeneExprSingleResultsContainer
 	               	GeneExprSingle  exprObj = (GeneExpr.GeneExprSingle) obj;
	               	GeneExprSingleViewHandler.handleGeneExprSingleView(geneExprSingleResultsContainer,exprObj, groupType);
 	               	//Populate the SampleViewResultsContainer
 	               	sampleViewResultsContainer = SampleViewHandler.handleSampleView(sampleViewResultsContainer,exprObj,groupType);
 	               	dimensionalViewContainer.setSampleViewResultsContainer(sampleViewResultsContainer);
 	               	dimensionalViewContainer.setGeneExprSingleViewContainer(geneExprSingleResultsContainer);
 	               	resultsContainer = dimensionalViewContainer;
                }
     		}
         }//for
         return resultsContainer;
 	}
 
 	/**
 	 * @param resultant
 	 * @param resultsets
 	 * @return
 	 */
 	public static ResultsContainer handleGeneExprDiseaseView(Resultant resultant, GeneExpr.GeneExprGroup[] geneExprObjects) {
 		ResultsContainer resultsContainer = null;
       	GeneExprResultsContainer geneExprResultsContainer = null;
  	  	SampleViewHandler sampleViewHandler = new SampleViewHandler();
  	  	if(resultant != null && resultant.getResultsContainer() instanceof GeneExprResultsContainer){
   			geneExprResultsContainer = (GeneExprResultsContainer) resultant.getResultsContainer();
   		}
 		if (geneExprResultsContainer == null){
   	    	geneExprResultsContainer = new GeneExprResultsContainer();
   		}
 
           for (int i = 0; i < geneExprObjects.length; i++) {
     		if(geneExprObjects[i] != null) {
             ResultSet obj = geneExprObjects[i];
               if (obj instanceof GeneExpr.GeneExprGroup)  {
               	GeneExpr.GeneExprGroup exprObj = (GeneExpr.GeneExprGroup) obj;
                	//Propulate the GeneExprSingleResultsContainer
               	geneExprResultsContainer = GeneExprDiseaseGroupViewHandler.handleGeneExprDiseaseView(geneExprResultsContainer,exprObj);
               	resultsContainer = geneExprResultsContainer;
               }
     		}
         }//for
         return resultsContainer;
 	}
 	public static ResultsContainer handleCopyNumberSingleView(Resultant resultant, CopyNumber[] copyNumberObjects, GroupType groupType) throws Exception{
  		ResultsContainer resultsContainer = null;
 		DimensionalViewContainer dimensionalViewContainer = null;
 		CopyNumberSingleViewResultsContainer copyNumberSingleViewResultsContainer = null;
    	SampleViewResultsContainer sampleViewResultsContainer = null;
   		if(resultant != null && resultant.getResultsContainer() instanceof DimensionalViewContainer){
  			dimensionalViewContainer = (DimensionalViewContainer) resultant.getResultsContainer();
   	    	sampleViewResultsContainer = dimensionalViewContainer.getSampleViewResultsContainer();
  			copyNumberSingleViewResultsContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
  			if(copyNumberSingleViewResultsContainer == null){
  	  			copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
  			}
  			try {
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer, copyNumberObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
   			copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
   	    	//sampleViewResultsContainer = new SampleViewResultsContainer();
 	         //Populate sampleViewResultsContainer with ClinicalData
 	        try {
 	        	sampleViewResultsContainer = new SampleViewResultsContainer();
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer, copyNumberObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
   		}
 
           for (int i = 0; i < copyNumberObjects.length; i++) {
     		if(copyNumberObjects[i] != null) {
             ResultSet obj = copyNumberObjects[i];
             	if (obj instanceof CopyNumber)  {
 	              	//Propulate the CopyNumberSingleViewResultsContainer
             		CopyNumber  copyNumberObj = (CopyNumber) obj;
             		copyNumberSingleViewResultsContainer = CopyNumberSingleViewHandler.handleCopyNumberSingleView(copyNumberSingleViewResultsContainer,copyNumberObj, groupType);
 	               	//Populate the SampleViewResultsContainer
 	               	sampleViewResultsContainer = SampleViewHandler.handleSampleView(sampleViewResultsContainer,copyNumberObj,groupType);
 	               	dimensionalViewContainer.setSampleViewResultsContainer(sampleViewResultsContainer);
 	               	dimensionalViewContainer.setCopyNumberSingleViewContainer(copyNumberSingleViewResultsContainer);
 	               	resultsContainer = dimensionalViewContainer;
                }
     		}
         }//for
 
         return resultsContainer;
 	}
 
 	/**
 	 * @param groups
 	 * @return
 	 * @throws Exception
 	 */
 	public static ResultsContainer handleGeneExpressPlot(GeneExprGroup[] geneExprObjects) throws Exception {
 		ResultsContainer resultsContainer = null;
 		GeneExprDiseasePlotContainer geneExprDiseasePlotContainer = new GeneExprDiseasePlotContainer();
 		DiseaseGeneExprPlotResultset nonTumor = new DiseaseGeneExprPlotResultset( new DiseaseNameDE(RembrandtConstants.NON_TUMOR));
 		geneExprDiseasePlotContainer.addDiseaseGeneExprPlotResultset(nonTumor);
  		geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleDiseaseGeneExprPlotResultset(geneExprDiseasePlotContainer);
 		for (int i = 0; i < geneExprObjects.length; i++) {
     		if(geneExprObjects[i] != null) {
             ResultSet obj = geneExprObjects[i];
               if (obj instanceof GeneExpr.GeneExprGroup)  {
               	GeneExpr.GeneExprGroup exprObj = (GeneExpr.GeneExprGroup) obj;
                	//Propulate the GeneExprSingleResultsContainer
               	geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleGeneExprDiseaseView(geneExprDiseasePlotContainer,exprObj);
               	geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleNoramlAsDisease(geneExprDiseasePlotContainer,exprObj);
               	resultsContainer = geneExprDiseasePlotContainer;
               }
     		}
         }//for
 		return resultsContainer;
 	}
 	/**
 	 * @param groups
 	 * @return
 	 * @throws Exception
 	 */
 	public static ResultsContainer handleUnifiedGeneExpressPlot(UnifiedGeneExprGroup[] geneExprObjects) throws Exception {
 		ResultsContainer resultsContainer = null;
 		GeneExprDiseasePlotContainer geneExprDiseasePlotContainer = new GeneExprDiseasePlotContainer();
 		DiseaseGeneExprPlotResultset nonTumor = new DiseaseGeneExprPlotResultset( new DiseaseNameDE(RembrandtConstants.NON_TUMOR));
 		geneExprDiseasePlotContainer.addDiseaseGeneExprPlotResultset(nonTumor);
  		geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleDiseaseGeneExprPlotResultset(geneExprDiseasePlotContainer);
 		for (int i = 0; i < geneExprObjects.length; i++) {
     		if(geneExprObjects[i] != null) {
             ResultSet obj = geneExprObjects[i];
               if (obj instanceof UnifiedGeneExpr.UnifiedGeneExprGroup)  {
             	  UnifiedGeneExpr.UnifiedGeneExprGroup exprObj = (UnifiedGeneExpr.UnifiedGeneExprGroup) obj;
                	//Propulate the GeneExprSingleResultsContainer
               	geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleUnifiedGeneExprDiseaseView(geneExprDiseasePlotContainer,exprObj);
               	geneExprDiseasePlotContainer = GeneExprDiseasePlotHandler.handleNoramlAsDisease(geneExprDiseasePlotContainer,exprObj);
               	resultsContainer = geneExprDiseasePlotContainer;
               }
     		}
         }//for
 		return resultsContainer;
 	}
 
 	/**
 	 * @param resultant
 	 * @param datas
 	 * @return
 	 */
 	public static ResultsContainer handleClinicalSampleView(Resultant resultant, PatientData[] patientDatas) {
 		ResultsContainer resultsContainer = null;
  		DimensionalViewContainer dimensionalViewContainer;
     	SampleViewResultsContainer sampleViewResultsContainer;
   		if(resultant != null && resultant.getResultsContainer() instanceof DimensionalViewContainer){
  			dimensionalViewContainer = (DimensionalViewContainer) resultant.getResultsContainer();
   	    	sampleViewResultsContainer = dimensionalViewContainer.getSampleViewResultsContainer();
   	    	if (sampleViewResultsContainer == null){
   	  	    	sampleViewResultsContainer = new SampleViewResultsContainer();
   	  		}
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
    	    	sampleViewResultsContainer = new SampleViewResultsContainer();
   		}
 	
           for (int i = 0; i < patientDatas.length; i++) {
     		if(patientDatas[i] != null) {
             ResultSet obj = patientDatas[i];
             	if (obj instanceof PatientData)  {
             		PatientData  patientDataObj = (PatientData) obj;
 	               	//Populate the SampleViewResultsContainer
 	               	sampleViewResultsContainer = SampleViewHandler.handleSampleView(sampleViewResultsContainer,patientDataObj);
 	               	dimensionalViewContainer.setSampleViewResultsContainer(sampleViewResultsContainer);
 	               	resultsContainer = dimensionalViewContainer;
                }
     		}
         }//for
         return resultsContainer;
 	}
 
 	public static ResultsContainer handleUnifiedGeneExprSingleView(Resultant resultant, UnifiedGeneExprSingle[] unifiedGeneExprObjects, GroupType groupType) throws Exception {
 		DimensionalViewContainer dimensionalViewContainer;
       	GeneExprSingleViewResultsContainer geneExprSingleResultsContainer;
     	SampleViewResultsContainer sampleViewResultsContainer;
   		if(resultant != null && resultant.getResultsContainer() instanceof DimensionalViewContainer){
  			dimensionalViewContainer = (DimensionalViewContainer) resultant.getResultsContainer();
   	    	sampleViewResultsContainer = dimensionalViewContainer.getSampleViewResultsContainer();
   			geneExprSingleResultsContainer = dimensionalViewContainer.getGeneExprSingleViewContainer();
   			if(geneExprSingleResultsContainer == null){
   	  			geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
   			}
 	        //Populate sampleViewResultsContainer with ClinicalData
 	        try {
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer, unifiedGeneExprObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
   			geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
   	    	//sampleViewResultsContainer = new SampleViewResultsContainer();
 	        //Populate sampleViewResultsContainer with ClinicalData
 	        try {
 	        	sampleViewResultsContainer = new SampleViewResultsContainer();
 				sampleViewResultsContainer = SampleViewHandler.populateWithClinicalData( sampleViewResultsContainer, unifiedGeneExprObjects);
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
   		}
  		ResultsContainer resultsContainer = null;
 	 		if(unifiedGeneExprObjects != null  ){
 	 			for (int i = 0; i < unifiedGeneExprObjects.length; i++) {
 	 				if(unifiedGeneExprObjects[i] != null) {
 	 					ResultSet obj = unifiedGeneExprObjects[i];
 	 					if (obj instanceof UnifiedGeneExpr.UnifiedGeneExprSingle)  {
 		              	//Propulate the GeneExprSingleResultsContainer
 	 					UnifiedGeneExpr.UnifiedGeneExprSingle  exprObj = (UnifiedGeneExpr.UnifiedGeneExprSingle) obj;
		               	GeneExprSingleViewHandler.handleGeneExprSingleView(geneExprSingleResultsContainer,exprObj, groupType);
 		               	//Populate the SampleViewResultsContainer
 		               	sampleViewResultsContainer = SampleViewHandler.handleSampleView(sampleViewResultsContainer,exprObj,groupType);
 		               	dimensionalViewContainer.setSampleViewResultsContainer(sampleViewResultsContainer);
 		               	dimensionalViewContainer.setGeneExprSingleViewContainer(geneExprSingleResultsContainer);
 		               	resultsContainer = dimensionalViewContainer;
 	 					}//if
 	 				}//if
 	 			}//for
 	 		}//if
  	        return resultsContainer;
 	}
 
 
 }
