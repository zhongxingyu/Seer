 /*
  *  @author: SahniH
  *  Created on Nov 9, 2004
  *  @version $ Revision: 1.0 $
  * 
  *	The caBIO Software License, Version 1.0
  *
  *	Copyright 2004 SAIC. This software was developed in conjunction with the National Cancer 
  *	Institute, and so to the extent government employees are co-authors, any rights in such works 
  *	shall be subject to Title 17 of the United States Code, section 105.
  * 
  *	Redistribution and use in source and binary forms, with or without modification, are permitted 
  *	provided that the following conditions are met:
  *	 
  *	1. Redistributions of source code must retain the above copyright notice, this list of conditions 
  *	and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above 
  *	copyright notice, this list of conditions and the following disclaimer in the documentation and/or 
  *	other materials provided with the distribution.
  * 
  *	2.  The end-user documentation included with the redistribution, if any, must include the 
  *	following acknowledgment:
  *	
  *	"This product includes software developed by the SAIC and the National Cancer 
  *	Institute."
  *	
  *	If no such end-user documentation is to be included, this acknowledgment shall appear in the 
  *	software itself, wherever such third-party acknowledgments normally appear.
  *	 
  *	3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or 
  *	promote products derived from this software.
  *	 
  *	4. This license does not authorize the incorporation of this software into any proprietary 
  *	programs.  This license does not authorize the recipient to use any trademarks owned by either 
  *	NCI or SAIC-Frederick.
  *	 
  *	
  *	5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED 
  *	WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  *	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE 
  *	DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR 
  *	THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  *	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  *	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
  *	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  *	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *	
  */
 package gov.nih.nci.nautilus.resultset.geneExpressionPlot;
 
 import gov.nih.nci.nautilus.de.DatumDE;
 import gov.nih.nci.nautilus.de.DiseaseNameDE;
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr.GeneExprGroup;
 
 /**
  * @author SahniH
  * Date: Nov 9, 2004
  * 
  */
 public class GeneExprDiseasePlotHandler {
 	public static GeneExprDiseasePlotContainer handleGeneExprDiseaseView(GeneExprDiseasePlotContainer geneExprDiseasePlotContainer, GeneExpr.GeneExprGroup exprObj){
 		DiseaseGeneExprPlotResultset diseaseResultset = null;
 		ReporterFoldChangeValuesResultset reporterResultset = null;
       	if (geneExprDiseasePlotContainer != null && exprObj != null){
       		geneExprDiseasePlotContainer.setGeneSymbol(new GeneIdentifierDE.GeneSymbol (exprObj.getGeneSymbol()));
       		diseaseResultset = handleDiseaseGeneExprPlotResultset(geneExprDiseasePlotContainer, exprObj);
       		reporterResultset = handleReporterFoldChangeValuesResultset(diseaseResultset,exprObj);
   			geneExprDiseasePlotContainer.addDiseaseGeneExprPlotResultset(diseaseResultset);
    			diseaseResultset.addReporterFoldChangeValuesResultset(reporterResultset);
       	}
       	return geneExprDiseasePlotContainer;
 
 	}
 
 	/**
 	 * @param diseaseResultset
 	 * @param exprObj
 	 * @return
 	 */
 	private static ReporterFoldChangeValuesResultset handleReporterFoldChangeValuesResultset(DiseaseGeneExprPlotResultset diseaseResultset, GeneExprGroup exprObj) {
   		// find out if it has a probeset or a clone associated with it
   		//populate ReporterResultset with the approciate one
 		ReporterFoldChangeValuesResultset reporterResultset = null;
 		if(diseaseResultset != null && exprObj != null){
 			//TODO:only Affy Probesets for now
 	    	if(exprObj.getProbesetName() != null){
 	  			DatumDE reporter = new DatumDE(DatumDE.PROBESET_ID,exprObj.getProbesetName());
 	       		reporterResultset = diseaseResultset.getReporterFoldChangeValuesResultset(exprObj.getProbesetName().toString());
 	      		if(reporterResultset == null){
 	      		 	reporterResultset = new ReporterFoldChangeValuesResultset(reporter);
 	      			}
 	      		reporterResultset.setRatioPval(new DatumDE(DatumDE.FOLD_CHANGE_RATIO_PVAL,exprObj.getRatioPval()));
 	      		reporterResultset.setFoldChangeIntensity(new DatumDE(DatumDE.FOLD_CHANGE_SAMPLE_INTENSITY,exprObj.getSampleIntensity()));
 	    		}
 	  		
 		}
         return reporterResultset;
 	}
 	/**
 	 * @param diseaseResultset
 	 * @param exprObj
 	 * @return
 	 */
	private static ReporterFoldChangeValuesResultset handleNoramAsDisease(DiseaseGeneExprPlotResultset normal, GeneExprGroup exprObj) {
   		// find out if it has a probeset or a clone associated with it
   		//populate ReporterResultset with the approciate one
 		ReporterFoldChangeValuesResultset reporterResultset = null;
		if(normal != null && exprObj != null){
 			//TODO:only Affy Probesets for now
 	    	if(exprObj.getProbesetName() != null){
 	  			DatumDE reporter = new DatumDE(DatumDE.PROBESET_ID,exprObj.getProbesetName());
 	       		reporterResultset = normal.getReporterFoldChangeValuesResultset(exprObj.getProbesetName().toString());
 	      		if(reporterResultset == null){
 	      		 	reporterResultset = new ReporterFoldChangeValuesResultset(reporter);
 	      			}
	      		reporterResultset.setRatioPval(new DatumDE(DatumDE.FOLD_CHANGE_RATIO_PVAL,"0.00"));
 	      		reporterResultset.setFoldChangeIntensity(new DatumDE(DatumDE.FOLD_CHANGE_SAMPLE_INTENSITY,exprObj.getNormalIntensity()));
 	    		}
 	  		
 		}
        return reporterResultset;
 	}
 	/**
 	 * @param geneExprDiseasePlotContainer
 	 * @param exprObj
 	 * @return
 	 */
 	private static DiseaseGeneExprPlotResultset handleDiseaseGeneExprPlotResultset(GeneExprDiseasePlotContainer geneExprDiseasePlotContainer, GeneExprGroup exprObj) {
 		//find out the disease type associated with the exprObj
   		//populate the DiseaseTypeResultset
 		DiseaseGeneExprPlotResultset diseaseResultset = null;
   		if(geneExprDiseasePlotContainer != null && exprObj != null &&  exprObj.getDiseaseType() != null){
   			DiseaseNameDE disease = new DiseaseNameDE(exprObj.getDiseaseType().toString());
   			diseaseResultset = (DiseaseGeneExprPlotResultset) geneExprDiseasePlotContainer.getDiseaseGeneExprPlotResultset(exprObj.getDiseaseType().toString());
   		    if (diseaseResultset == null){
   		    	diseaseResultset= new DiseaseGeneExprPlotResultset(disease);
 	      		}
       	}
   		return diseaseResultset;
 	}
 
 }
