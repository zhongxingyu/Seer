 /*
  * Created on Sep 14, 2004
  *
  */
 package gov.nih.nci.nautilus.resultset;
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
  	public static ResultsContainer handleGeneExprSingleView(GeneExpr.GeneExprSingle[] geneExprObjects, GroupType groupType){
  		ResultsContainer resultsContainer = null;
       	GeneExprSingleViewResultsContainer geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
     	SampleViewResultsContainer sampleViewResultsContainer = new SampleViewResultsContainer();
     	DimensionalViewContainer dimensionalViewContainer = new DimensionalViewContainer();
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
 	 * @param resultsets
 	 * @return
 	 */
 	public static ResultsContainer handleGeneExprDiseaseView(GeneExpr.GeneExprGroup[] geneExprObjects) {
 		ResultsContainer resultsContainer = null;
  	  	SampleViewHandler sampleViewHandler = new SampleViewHandler();
       	GeneExprSingleViewResultsContainer geneExprSingleResultsContainer = new GeneExprSingleViewResultsContainer();
     	SampleViewResultsContainer sampleViewResultsContainer = new SampleViewResultsContainer();
     	DimensionalViewContainer dimensionalViewContainer = new DimensionalViewContainer();
       	GeneExprResultsContainer geneExprResultsContainer = new GeneExprResultsContainer();
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
 	public static ResultsContainer handleCopyNumberSingleView(CopyNumber[] copyNumberObjects, GroupType groupType){
  		ResultsContainer resultsContainer = null;
       	CopyNumberSingleViewResultsContainer copyNumberSingleViewResultsContainer = new CopyNumberSingleViewResultsContainer();
     	SampleViewResultsContainer sampleViewResultsContainer = new SampleViewResultsContainer();
     	DimensionalViewContainer dimensionalViewContainer = new DimensionalViewContainer();
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
 	 */
 	public static ResultsContainer handleGeneExpressPlot(GeneExprGroup[] geneExprObjects) {
 		ResultsContainer resultsContainer = null;
 		GeneExprDiseasePlotContainer geneExprDiseasePlotContainer = new GeneExprDiseasePlotContainer();
 		DiseaseGeneExprPlotResultset normal = new DiseaseGeneExprPlotResultset( new DiseaseNameDE("NORMAL"));
 		geneExprDiseasePlotContainer.addDiseaseGeneExprPlotResultset(normal);
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
 
 
 }
