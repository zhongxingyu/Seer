 /*
  *  @author: SahniH
  *  Created on Oct 15, 2004
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
 package gov.nih.nci.nautilus.queryprocessing;
 
 import gov.nih.nci.nautilus.data.PatientData;
 import gov.nih.nci.nautilus.query.CompoundQuery;
 import gov.nih.nci.nautilus.query.OperatorType;
 import gov.nih.nci.nautilus.query.Queriable;
 import gov.nih.nci.nautilus.query.Query;
 import gov.nih.nci.nautilus.query.QueryManager;
 import gov.nih.nci.nautilus.queryprocessing.cgh.CopyNumber;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.resultset.ClinicalResultSet;
 import gov.nih.nci.nautilus.resultset.CompoundResultSet;
 import gov.nih.nci.nautilus.resultset.ResultSet;
 import gov.nih.nci.nautilus.view.Viewable;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 
 
 /**
  * @author SahniH
  * Date: Oct 15, 2004
  * 
  */
 public class CompoundQueryProcessor {
 	private static Logger logger = Logger.getLogger(CompoundQueryProcessor.class);
 	public static CompoundResultSet execute(CompoundQuery compoundQuery) throws Exception{
 		ResultSet[] resultSets = null;
 		CompoundResultSet compoundResultet= null;
 		Set sampleIds = new HashSet();
 		Collection results = new Vector();
 		if(compoundQuery != null){
 			Queriable leftQuery = compoundQuery.getLeftQuery();
 			Queriable rightQuery = compoundQuery.getRightQuery();
 			OperatorType operator = compoundQuery.getOperatorType();
 			Viewable view = compoundQuery.getAssociatedView();
 			CompoundResultSet leftResultSets = null;
 			CompoundResultSet rightResultSets = null;
 				if (leftQuery != null) {
 					if (leftQuery instanceof CompoundQuery) {
 						leftQuery.setAssociatedView(view);
 						leftResultSets = execute((CompoundQuery)leftQuery);
 						results.addAll(retriveResultSetsFromCompoundResultset(leftResultSets));
 						}
 					else if (leftQuery instanceof Query){
 						leftQuery.setAssociatedView(view);						
 						leftResultSets = createCompoundResultset((ResultSet[])QueryManager.executeQuery((Query)leftQuery));
 						results.add(leftResultSets.getResults().toArray());
 					}
 					
 				}
 				
 				if (rightQuery != null) {
 					if (rightQuery instanceof CompoundQuery) {
 						rightQuery.setAssociatedView(view);
 						rightResultSets = execute((CompoundQuery)rightQuery);
 						results.addAll(retriveResultSetsFromCompoundResultset(rightResultSets));
 						}
 					else if (rightQuery instanceof Query){
 						rightQuery.setAssociatedView(view);
 						rightResultSets = createCompoundResultset((ResultSet[]) QueryManager.executeQuery((Query)rightQuery));
 						results.add(rightResultSets.getResults().toArray());
 					}
 					
 				}
 	
 			if (operator != null) {
 					if(operator.equals(OperatorType.AND)){
 						sampleIds.addAll(resultSetInterSection(leftResultSets,rightResultSets));
 					}
 					else if(operator.equals(OperatorType.OR)){
 						sampleIds.addAll(resultSetUnion(leftResultSets,rightResultSets));
 					}
 					else if(operator.equals(OperatorType.NOT)){
 						sampleIds.addAll(resultSetDifference(leftResultSets,rightResultSets));
 					}
 					else if(operator.equals(OperatorType.PROJECT_RESULTS_BY)){
 						sampleIds.addAll(resultSetProjectResultsBy(leftResultSets,rightResultSets));
 					}
 				compoundResultet = processCompoundQuery(results,sampleIds);					
 			}else{ //then its the right query				
 				compoundResultet = processCompoundQuery(results,rightResultSets.getSampleIds());	//rightResultSets;	
 				
 			}
 				
 		}
 		return compoundResultet;	
 	}
     private static CompoundResultSet createCompoundResultset(ResultSet[]results){
     	Set ids = new HashSet();
     	if(results instanceof ResultSet[]){
     		ResultSet[] resultsets = (ResultSet[])results;
     		if(resultsets instanceof ClinicalResultSet[]){
 	    		for(int i = 0; i < resultsets.length; i++){
 	    			ClinicalResultSet clinicalResultset = (ClinicalResultSet) resultsets[i];
 	    			ids.add(clinicalResultset.getBiospecimenId());
 	    		}
 	    	}else if(resultsets instanceof GeneExpr.GeneExprGroup[]){
 	    		for(int i = 0; i < resultsets.length; i++){
 	    			GeneExpr.GeneExprGroup groupResultset = (GeneExpr.GeneExprGroup) resultsets[i];
 	    			ids.add(groupResultset.getDiseaseTypeId());
 	    		}
 	    	}
     	}
     	return new CompoundResultSet(Arrays.asList(results),ids);
     }
 	private static boolean isGroupResultsets(CompoundResultSet leftResultSets,CompoundResultSet rightResultSets){
 		boolean isGroup = false;
 		if(leftResultSets != null && rightResultSets != null &&
 			leftResultSets.getResults() != null && rightResultSets.getResults() != null
 			&& leftResultSets.getResults().toArray() instanceof GeneExpr.GeneExprGroup[] 
 			&& rightResultSets.getResults().toArray() instanceof GeneExpr.GeneExprGroup[] ){
 			isGroup = true;
 		}
 		return isGroup;
 	}
 	private static Collection retriveResultSetsFromCompoundResultset(CompoundResultSet compoundResultSet){
 		Collection resultSetCollection = new Vector();
 		Collection results = compoundResultSet.getResults();
 		for (Iterator resultsIterator = results.iterator(); resultsIterator.hasNext();) {
 			Object resultObj = resultsIterator.next();
 			if(resultObj instanceof ResultSet[]){
 				resultSetCollection.add(resultObj);
 			}
 		}
 		return resultSetCollection;
 	}
 	
 	/**
 	 * @param results
 	 * @param sampleIds
 	 * @return
 	 */
 	private static CompoundResultSet processCompoundQuery(Collection results, Set sampleIds) {
 		Collection finalResultsets = new Vector();
 		for (Iterator resultsIterator = results.iterator(); resultsIterator.hasNext();) {
 			Set resultset = new HashSet();
 			Object obj = resultsIterator.next();
 			if(obj instanceof ResultSet[]){
 	    		ResultSet[] resultsets = (ResultSet[])obj;
 	    		if(resultsets instanceof ClinicalResultSet[]){
 		    		for(int i = 0; i < resultsets.length; i++){
 		    			ClinicalResultSet clinicalResultset = (ClinicalResultSet) resultsets[i];
 		    			for(Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();){
 		    				Long id = (Long)sampleIdIterator.next();
 				    		if(clinicalResultset.getBiospecimenId().equals(id)){
 				    			resultset.add(clinicalResultset);
 				    		}
 		    			}
 		    		}
 		    		if(resultsets instanceof  GeneExpr.GeneExprSingle[]){
		    			if(resultsets.length > 0){
		    				finalResultsets.add(resultset.toArray(new GeneExpr.GeneExprSingle[resultsets.length]));
		    			}
 		    		}
 		    		else if(resultsets instanceof  CopyNumber[]){
		    			if(resultsets.length > 0){
		    				finalResultsets.add(resultset.toArray(new CopyNumber[resultsets.length]));
		    			}
 		    		}
 		    		else if(resultsets instanceof  PatientData[]){
		    			if(resultsets.length > 0){
		    				finalResultsets.add(resultset.toArray(new PatientData[resultsets.length]));
		    			}
 		    		}
 	    		}else if(resultsets instanceof GeneExpr.GeneExprGroup[]){
 		    		for(int i = 0; i < resultsets.length; i++){
 		    			GeneExpr.GeneExprGroup groupResultset = (GeneExpr.GeneExprGroup) resultsets[i];
 		    			for(Iterator groupIterator = sampleIds.iterator(); groupIterator.hasNext();){
 		    				Long id = (Long)groupIterator.next();
 				    		if(groupResultset.getDiseaseTypeId().equals(id)){
 				    			resultset.add(groupResultset);
 				    		}
 		    			}
 		    		}
		    		if(resultsets.length > 0){
		    			finalResultsets.add(resultset.toArray(new GeneExpr.GeneExprGroup[resultsets.length]));
		    		}
 		    	}
 			}
 		}
 		return new CompoundResultSet(finalResultsets,sampleIds);
 	}
 	/**
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return
 	 */
 	private static Set resultSetGroupProjectResultsBy(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/**
 	 * ResultSetDifference (minus): set of every element that is in the leftResultset but not in the rightResultset
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @param sampleIds
 	 * @return resultSet[] which is set of every element that is in the leftResultset but not in the rightResultset
 	 */
 	private static Set resultSetGroupDifference(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		Set diffset  = new HashSet();
 		GeneExpr.GeneExprGroup[] results = null;
 		if(leftResultSets != null && rightResultSets != null &&
 			leftResultSets.getResults() != null && rightResultSets.getResults() != null){
 			 results = (GeneExpr.GeneExprGroup[]) leftResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[leftResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				leftResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			results =  (GeneExpr.GeneExprGroup[]) rightResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[rightResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				rightResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			//the removeAll operation effectively modifies this set so that its value is the asymmetric set difference of the two sets.
 			logger.debug("L:"+leftResults.size());			
 			logger.debug("R:"+rightResults.size());
 			diffset = new HashSet(leftResults);
 			diffset.removeAll(rightResults);
 			
 		}
 		return diffset;
 	}
 	/**
 	 * ResultSetUnion: a resultSet containing every element that is in either the leftResultSet or the rightResultSet, or both
 	 * (must be union-compatible: same number of objects and corresponding objects have identical )
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return resultSet[] containing every element that is in either the leftResultSet or the rightResultSet, or both
 	 */
 	private static Set resultSetGroupUnion(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		Set unionSet = new HashSet();
 		GeneExpr.GeneExprGroup[] results = null;
 		if(leftResultSets != null && rightResultSets != null &&
 			leftResultSets.getResults() != null && rightResultSets.getResults() != null){
 			 results = (GeneExpr.GeneExprGroup[]) leftResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[leftResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				leftResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			results =  (GeneExpr.GeneExprGroup[]) rightResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[rightResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				rightResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			//the addAll operation effectively modifies this set so that its value is the union of the two sets.
 			unionSet = new HashSet(leftResults);
 			logger.debug("L:"+leftResults.size());
 			logger.debug("R:"+rightResults.size());
 			unionSet.addAll(rightResults);
 			logger.debug("Union:"+unionSet.size());	
 			
 		}
 		return unionSet;
 	}
 	/**
 	 * ResultSetIntersection (intersect): a resultset containing every element that is in both rightResultSet and LeftResultset
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return resultSet[] containing every element that is in both rightResultSet and LeftResultset
 	 */
 	private static Set resultSetGroupInterSection(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		GeneExpr.GeneExprGroup[] results = null;
 		Set interSectSet = new HashSet();
 		if(leftResultSets != null && rightResultSets != null &&
 			leftResultSets.getResults() != null && rightResultSets.getResults() != null){
 			 results = (GeneExpr.GeneExprGroup[]) leftResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[leftResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				leftResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			results =  (GeneExpr.GeneExprGroup[]) rightResultSets.getResults().toArray(new GeneExpr.GeneExprGroup[rightResultSets.getResults().size()]);
 			for(int i =0;i < results.length;i++){
 				GeneExpr.GeneExprGroup groupResultSet = (GeneExpr.GeneExprGroup) results[i];
 				rightResults.add(groupResultSet.getDiseaseTypeId());
 			}
 			//this operation effectively modifies this set so that its value is the intersection of the two sets.
 			interSectSet = new HashSet(leftResults);
 			logger.debug("L:"+leftResults.size());
 			logger.debug("R:"+rightResults.size());
 			interSectSet.retainAll(rightResults);
 			
 	    	
 		}
 		return interSectSet; //(ResultSet[])finalResults.toArray(new ResultSet[1]);
 	}
 	/**
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return
 	 */
 	private static Set resultSetProjectResultsBy(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/**
 	 * ResultSetDifference (minus): set of every element that is in the leftResultset but not in the rightResultset
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @param sampleIds
 	 * @return resultSet[] which is set of every element that is in the leftResultset but not in the rightResultset
 	 */
 	private static Set resultSetDifference(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		ClinicalResultSet[] results = null;
 		Set diffset = new HashSet();
 		leftResults.addAll(leftResultSets.getSampleIds());
 		rightResults.addAll(rightResultSets.getSampleIds());
 
 		//the removeAll operation effectively modifies this set so that its value is the asymmetric set difference of the two sets.
 		logger.debug("L:"+leftResults.size());			
 		logger.debug("R:"+rightResults.size());
 		diffset = new HashSet(leftResults);
 		diffset.removeAll(rightResults);
 			
 		return diffset;
 	}
 	/**
 	 * ResultSetUnion: a resultSet containing every element that is in either the leftResultSet or the rightResultSet, or both
 	 * (must be union-compatible: same number of objects and corresponding objects have identical )
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return resultSet[] containing every element that is in either the leftResultSet or the rightResultSet, or both
 	 */
 	private static Set resultSetUnion(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		ClinicalResultSet[] results = null;
 		Set unionSet = new HashSet();
 		leftResults.addAll(leftResultSets.getSampleIds());
 		rightResults.addAll(rightResultSets.getSampleIds());
 		
 		//the addAll operation effectively modifies this set so that its value is the union of the two sets.
 		unionSet = new HashSet(leftResults);
 		logger.debug("L:"+leftResults.size());
 		logger.debug("R:"+rightResults.size());
 		unionSet.addAll(rightResults);
 		logger.debug("Union:"+unionSet.size());	
 		return unionSet;
 	}
 	/**
 	 * ResultSetIntersection (intersect): a resultset containing every element that is in both rightResultSet and LeftResultset
 	 * @param leftResultSets
 	 * @param rightResultSets
 	 * @return resultSet[] containing every element that is in both rightResultSet and LeftResultset
 	 */
 	private static Set resultSetInterSection(CompoundResultSet leftResultSets, CompoundResultSet rightResultSets) {
 		Collection leftResults = new HashSet();
 		Collection rightResults = new HashSet();
 		ClinicalResultSet[] results = null;
 		Set interSectSet = new HashSet();
 		leftResults.addAll(leftResultSets.getSampleIds());
 		rightResults.addAll(rightResultSets.getSampleIds());
 		
 		//this operation effectively modifies this set so that its value is the intersection of the two sets.
 		interSectSet = new HashSet(leftResults);
 		logger.debug("L:"+leftResults.size());
 		logger.debug("R:"+rightResults.size());
 		interSectSet.retainAll(rightResults);
 		logger.debug("InterSect:"+interSectSet.size());	
 		return interSectSet; 
 	}
 }
