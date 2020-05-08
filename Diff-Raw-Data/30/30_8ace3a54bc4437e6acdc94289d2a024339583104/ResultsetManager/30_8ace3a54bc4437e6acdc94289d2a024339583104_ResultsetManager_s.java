 /*
  *  @author: SahniH
  *  Created on Oct 31, 2004
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
 package gov.nih.nci.nautilus.resultset;
 import java.util.Collection;
 import java.util.Iterator;
 
 import gov.nih.nci.nautilus.data.PatientData;
 import gov.nih.nci.nautilus.query.Queriable;
 import gov.nih.nci.nautilus.query.QueryManager;
 import gov.nih.nci.nautilus.queryprocessing.cgh.CopyNumber;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr.GeneExprGroup;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr.GeneExprSingle;
 import gov.nih.nci.nautilus.resultset.kaplanMeierPlot.KaplanMeierPlotHandler;
 import gov.nih.nci.nautilus.view.ClinicalSampleView;
 import gov.nih.nci.nautilus.view.CopyNumberSampleView;
 import gov.nih.nci.nautilus.view.GeneExprDiseaseView;
 import gov.nih.nci.nautilus.view.GeneExprSampleView;
 import gov.nih.nci.nautilus.view.GroupType;
 import gov.nih.nci.nautilus.view.ViewFactory;
 import gov.nih.nci.nautilus.view.ViewType;
 import gov.nih.nci.nautilus.view.Viewable;
 
 
 
 public class ResultsetManager {
     public static Resultant executeCompoundQuery(Queriable queryToExecute) throws Exception {
     	Resultant resultant= null; 
     	if(queryToExecute != null ){
     	resultant = new Resultant();
         Viewable associatedView = queryToExecute.getAssociatedView();
         CompoundResultSet compoundResultSet = QueryManager.executeCompoundQuery(queryToExecute);
         Collection results = compoundResultSet.getResults();
         if(results != null){
         	for (Iterator resultsIterator = results.iterator(); resultsIterator.hasNext();) {
         	ResultSet[] resultsets = 	(ResultSet[]) resultsIterator.next();
 		    	if (resultsets instanceof GeneExprSingle[]){
 		    		GroupType groupType = GroupType.DISEASE_TYPE_GROUP;
 		    		if (associatedView instanceof GeneExprSampleView){
 		    			GeneExprSampleView geneExprSampleView = (GeneExprSampleView) associatedView;
 		    			groupType = geneExprSampleView.getGroupType();
 		    		}
 		    			ResultsContainer resultsContainer = ResultsetProcessor.handleGeneExprSingleView(resultant,(GeneExprSingle[]) resultsets,groupType);
 		    			resultant.setResultsContainer(resultsContainer);
 		    			resultant.setAssociatedQuery(queryToExecute);
 		    			resultant.setAssociatedView(associatedView);
 		    		}
 		    	else if (resultsets instanceof GeneExprGroup[]){
 		    		GeneExprDiseaseView geneExprDiseaseView = (GeneExprDiseaseView) associatedView;
 					ResultsContainer resultsContainer = ResultsetProcessor.handleGeneExprDiseaseView(resultant,(GeneExprGroup[]) resultsets);
 					resultant.setResultsContainer(resultsContainer);
 					resultant.setAssociatedQuery(queryToExecute);
 					resultant.setAssociatedView(associatedView);
 				}
 		    	else if (resultsets instanceof CopyNumber[]){
 		    		GroupType groupType = GroupType.DISEASE_TYPE_GROUP;
 		    		if (associatedView instanceof CopyNumberSampleView ){
 		    			CopyNumberSampleView copyNumberView = (CopyNumberSampleView) associatedView;
 		    			groupType = copyNumberView.getGroupType();
 		    		}
 					ResultsContainer resultsContainer = ResultsetProcessor.handleCopyNumberSingleView(resultant,(CopyNumber[]) resultsets, groupType);
 					resultant.setResultsContainer(resultsContainer);
 					resultant.setAssociatedQuery(queryToExecute);
 					resultant.setAssociatedView(associatedView);
 				}
 		    	else if (resultsets instanceof PatientData[]){
		    		ClinicalSampleView clinicalView = (ClinicalSampleView) associatedView;
 					ResultsContainer resultsContainer = ResultsetProcessor.handleClinicalSampleView(resultant,(PatientData[]) resultsets);
 					resultant.setResultsContainer(resultsContainer);
 					resultant.setAssociatedQuery(queryToExecute);
 					resultant.setAssociatedView(associatedView);
 				}
 			}
 		}
       }
       return resultant;
     }
     public static Resultant executeGeneExpressPlotQuery(Queriable queryToExecute) throws Exception {
     	Resultant resultant= new Resultant();
     	if(queryToExecute != null ){
     		Viewable associatedView = ViewFactory.newView(ViewType.GENE_GROUP_SAMPLE_VIEW);
     		queryToExecute.setAssociatedView(associatedView);
     		ResultSet[] resultsets = QueryManager.executeQuery(queryToExecute);
     		ResultsContainer resultsContainer = ResultsetProcessor.handleGeneExpressPlot((GeneExprGroup[]) resultsets);
 			resultant.setResultsContainer(resultsContainer);
 			resultant.setAssociatedQuery(queryToExecute);
 			resultant.setAssociatedView(associatedView);
     		}
     	return resultant;
     	}
     public static Resultant executeKaplanMeierPlotQuery(Queriable queryToExecute) throws Exception {
     	Resultant resultant= new Resultant();
     	if(queryToExecute != null ){
     		Viewable associatedView = ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW);
     		queryToExecute.setAssociatedView(associatedView);
     		ResultSet[] resultsets = QueryManager.executeQuery(queryToExecute);
     		ResultsContainer resultsContainer = KaplanMeierPlotHandler.handleKaplanMeierPlotContainer((GeneExpr.GeneExprSingle[]) resultsets);
 			resultant.setResultsContainer(resultsContainer);
 			resultant.setAssociatedQuery(queryToExecute);
 			resultant.setAssociatedView(associatedView);
     		}
     	return resultant;
     	}
 
 
 }
