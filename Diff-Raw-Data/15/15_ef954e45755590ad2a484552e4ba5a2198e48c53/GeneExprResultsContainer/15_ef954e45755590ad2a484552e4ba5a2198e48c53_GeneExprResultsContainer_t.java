 /*
  *  @author: SahniH
  *  Created on Oct 29, 2004
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
 package gov.nih.nci.nautilus.resultset.gene;
 
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 import gov.nih.nci.nautilus.resultset.ResultsContainer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * @author SahniH
  * Date: Oct 29, 2004
  * 
  */
 public class GeneExprResultsContainer implements ResultsContainer{
 	protected SortedMap genes = new TreeMap();
 	protected SortedMap groupsLabels = new TreeMap();
 	/**
 	 * @return Returns the groupsLabels.
 	 */
 	public Collection getGroupsLabels() {
 		return  this.groupsLabels.keySet();
 	}
 
 	/**
 	 * @param geneResultset Adds geneResultset to this GeneExprSingleViewResultsContainer object.
 	 */
 	public void addGeneResultset(GeneResultset geneResultset){
 		if(geneResultset != null && geneResultset.getGeneSymbol() != null){
 			genes.put(geneResultset.getGeneSymbol().getValue().toString(), geneResultset);
 		}
 		else if(geneResultset != null && geneResultset.isAnonymousGene() == true){
 			genes.put("zzzzzzzzzzzzzz", geneResultset);
 		}
 	}
 	/**
 	 * @param geneResultset Removes geneResultset to this GeneExprSingleViewResultsContainer object.
 	 */
 	public void removeGeneResultset(GeneResultset geneResultset){
 		if(geneResultset != null && geneResultset.getGeneSymbol() != null){
 			genes.remove(geneResultset.getGeneSymbol().toString());
 		}
 	}
 	/**
 	 * @return geneResultset Returns geneResultset to this GeneExprSingleViewResultsContainer object.
 	 */
     public Collection getGeneResultsets(){
     		return genes.values();
     }
     /**
      * @param geneSymbol
 	 * @return geneResultset Returns geneResultset to this geneSymbol.
 	 */
     public GeneResultset getGeneResultset(String geneSymbol){
     	if(geneSymbol != null){
 			return (GeneResultset) genes.get(geneSymbol);
 		}
     		return null;
     }
     /**
      * @param geneSymbol
 	 * @return reporterResultset Returns reporterResultset for this geneSymbol.
 	 */
     public Collection getRepoterResultsets(String geneSymbol){
     	if(geneSymbol != null){
     		GeneResultset geneResultset = (GeneResultset) genes.get(geneSymbol);
 			return geneResultset.getReporterResultsets();
 		}
     		return null;
     }
 	/**
 	 * @param none Removes all geneResultset in this GeneExprSingleViewResultsContainer object.
 	 */
     public void removeAllGeneResultset(){
     	genes.clear();
     }
     /**
      * @param geneSymbol,reporterName
 	 * @return groupResultset Returns groupResultset for this reporterName & geneSymbol.
 	 */
     public Collection getGroupByResultsets(String geneSymbol,String reporterName){
     	if(geneSymbol!= null && reporterName != null){
     		GeneResultset geneResultset = (GeneResultset) genes.get(geneSymbol);
     		ReporterResultset reporterResultset = (ReporterResultset) geneResultset.getRepoterResultset(reporterName);
 			return reporterResultset.getGroupByResultsets();
 		}
     		return null;
     }
     /**
      * @param geneSymbol,reporterName
 	 * @return groupResultset Returns groupResultset for this reporterName & geneSymbol.
 	 */
     public Collection getFilteredGroupByResultsets(String[] groupLabels, boolean isShow){
     	Collection geneResults = new ArrayList();
    	
    	Collection geneCollection = genes.values();
     	if(isShow){ //get all geneResultant objects that are in the collection
     		for (Iterator geneIterator = geneCollection.iterator(); geneIterator.hasNext();) {
 	    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 	    		Collection reporters = geneResultset.getReporterResultsets();
     		
 	    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 	        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 	        		for (int i = 0; i < groupLabels.length ; i++) {
 	    	        	String label = (String) groupLabels[i];
 	    	        	Groupable groupableResultset = reporterResultset.getGroupByResultset(label);
 	    	        	if(groupableResultset != null){
 	    	        		geneResults.add(geneResultset);
 	    	        	}
 	        		}
 	    		}
     		}
 	    	  
     	}
        	else{//return everything besides the ones that are in the collection
     		geneResults.addAll(geneCollection);
        		for (Iterator geneIterator = geneCollection.iterator(); geneIterator.hasNext();) {
 	    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 	    		Collection reporters = geneResultset.getReporterResultsets();
     		
 	    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 	        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 	        		for (int i = 0; i < groupLabels.length ; i++) {
 	    	        	String label = (String) groupLabels[i];
 	    	        	Groupable groupableResultset = reporterResultset.getGroupByResultset(label);
 	    	        	if(groupableResultset != null){
 	    	        		geneResults.remove(geneResultset);
 	    	        	}
 	        		}
 	    		}
     		}
    	}
     	return geneResults;
     }    
 	/**
 	 * @param diseaseType
 	 */
 	public void addDiseaseTypes(String diseaseType) {
 		groupsLabels.put(diseaseType,null);
 		
 	}
 	//getPaginatedGeneResultsets(int start, int count)  returns gene resultant from start to start+count
     public Collection  getPaginatedGeneResultsets(int start, int count){
     	Set keys = genes.keySet();
     	String[] geneNames = (String[]) keys.toArray(new String[genes.size()]);
     	Collection geneResults = new ArrayList();
     	int size = count;
     	if( geneNames.length < count) {
     		size = geneNames.length;
     	}
     	for (int i = start; i < size; i++ ){
     		geneResults.add(genes.get(geneNames[i]));
     	}
     	return geneResults;
     }
 //  getFilteredGeneResultsets( GeneIdentifierDE.GeneSymbol[] geneSymbols, boolean isShow )  returns gene resultant Select to show/hide genes listed
     public Collection  getFilteredGeneResultsets( GeneIdentifierDE.GeneSymbol[] geneSymbols, boolean isShow ){
     	Collection geneResults = new ArrayList();
     	if(isShow){ //get all geneResultant objects that are in the collection
 	    	for (int i = 0; i < geneSymbols.length; i++ ){
 	    		String geneSymbol = geneSymbols[i].getValue().toString();
 	    		if(genes.containsKey(geneSymbol)){
 	    			geneResults.add(genes.get(geneSymbol));
 	    		}
 	    	}
     	}
        	else{//return everything besides the ones that are in the collection
        		Set symbols = genes.keySet();
 	    	for (int i = 0; i < geneSymbols.length; i++ ){
 	    		String geneSymbol = geneSymbols[i].getValue().toString();
 	    		if(genes.containsKey(geneSymbol)){
 	    			symbols.remove((String) genes.get(geneSymbol));
 	    		}
 	    	}
 	    	Collection geneKeys = new ArrayList();
 	    	for(Iterator symbolsIterator = symbols.iterator(); symbolsIterator.hasNext();){
 	    		String symbol = (String) symbolsIterator.next();
 	    		geneKeys.add(new GeneIdentifierDE.GeneSymbol(symbol));
 	    	}
 	    	geneResults = getFilteredGeneResultsets((GeneIdentifierDE.GeneSymbol[])geneKeys.toArray(new GeneIdentifierDE.GeneSymbol[geneKeys.size()]),true);
     	}
     	return geneResults;
     }
 }
