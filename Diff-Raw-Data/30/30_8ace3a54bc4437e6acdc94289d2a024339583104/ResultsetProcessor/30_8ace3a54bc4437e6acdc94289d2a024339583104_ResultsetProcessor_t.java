 /*
  * Created on Sep 14, 2004
  *
  */
 package gov.nih.nci.nautilus.resultset;
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.data.PatientData;
 import gov.nih.nci.nautilus.de.DiseaseNameDE;
 import gov.nih.nci.nautilus.queryprocessing.cgh.CopyNumber;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr.GeneExprGroup;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr.GeneExprSingle;
 import gov.nih.nci.nautilus.resultset.copynumber.CopyNumberSingleViewHandler;
 import gov.nih.nci.nautilus.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprDiseaseGroupViewHandler;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprResultsContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprSingleViewHandler;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.geneExpressionPlot.DiseaseGeneExprPlotResultset;
 import gov.nih.nci.nautilus.resultset.geneExpressionPlot.GeneExprDiseasePlotContainer;
 import gov.nih.nci.nautilus.resultset.geneExpressionPlot.GeneExprDiseasePlotHandler;
 import gov.nih.nci.nautilus.resultset.sample.SampleViewHandler;
 import gov.nih.nci.nautilus.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.nautilus.view.GroupType;
 
 /**
  * @author SahniH
  *
  * This class takes a DifferentialExpressionSfact and DifferentialExpressionGfact object and helps create a GeneCentricViewHandler or a SampleCentricViewHandler classes.
  */
 public class ResultsetProcessor {
  	public static ResultsContainer handleGeneExprSingleView(Resultant resultant, GeneExpr.GeneExprSingle[] geneExprObjects, GroupType groupType){
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
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
   			geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
   	    	sampleViewResultsContainer = new SampleViewResultsContainer();
   		}
  		ResultsContainer resultsContainer = null;
           for (int i = 0; i < geneExprObjects.length; i++) {
     		if(geneExprObjects[i] != null) {
             ResultSet obj = geneExprObjects[i];
             	if (obj instanceof GeneExpr.GeneExprSingle)  {
 	              	//Propulate the GeneExprSingleResultsContainer
 	               	GeneExprSingle  exprObj = (GeneExpr.GeneExprSingle) obj;
 	               	geneExprSingleResultsContainer = GeneExprSingleViewHandler.handleGeneExprSingleView(geneExprSingleResultsContainer,exprObj, groupType);
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
 	public static ResultsContainer handleCopyNumberSingleView(Resultant resultant, CopyNumber[] copyNumberObjects, GroupType groupType){
  		ResultsContainer resultsContainer = null;
  		DimensionalViewContainer dimensionalViewContainer;
  		CopyNumberSingleViewResultsContainer copyNumberSingleViewResultsContainer;
     	SampleViewResultsContainer sampleViewResultsContainer;
   		if(resultant != null && resultant.getResultsContainer() instanceof DimensionalViewContainer){
  			dimensionalViewContainer = (DimensionalViewContainer) resultant.getResultsContainer();
   	    	sampleViewResultsContainer = dimensionalViewContainer.getSampleViewResultsContainer();
  			copyNumberSingleViewResultsContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
  			if(copyNumberSingleViewResultsContainer == null){
  	  			copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
  			}
  		}
   		else{
   			dimensionalViewContainer = new DimensionalViewContainer();
   			copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
   	    	sampleViewResultsContainer = new SampleViewResultsContainer();
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
 		DiseaseGeneExprPlotResultset normal = new DiseaseGeneExprPlotResultset( new DiseaseNameDE(NautilusConstants.NORMAL));
 		geneExprDiseasePlotContainer.addDiseaseGeneExprPlotResultset(normal);
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
 
 
 }
