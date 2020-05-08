 /*
  *  @author: SahniH
  *  Created on Oct 26, 2004
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
 package gov.nih.nci.nautilus.test;
 
 import gov.nih.nci.nautilus.criteria.ArrayPlatformCriteria;
 import gov.nih.nci.nautilus.criteria.Constants;
 import gov.nih.nci.nautilus.criteria.FoldChangeCriteria;
 import gov.nih.nci.nautilus.criteria.GeneIDCriteria;
 import gov.nih.nci.nautilus.de.ArrayPlatformDE;
 import gov.nih.nci.nautilus.de.ExprFoldChangeDE;
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 import gov.nih.nci.nautilus.query.CompoundQuery;
 import gov.nih.nci.nautilus.query.GeneExpressionQuery;
 import gov.nih.nci.nautilus.query.Query;
 import gov.nih.nci.nautilus.query.QueryManager;
 import gov.nih.nci.nautilus.query.QueryType;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.resultset.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.GeneResultset;
 import gov.nih.nci.nautilus.resultset.GroupResultset;
 import gov.nih.nci.nautilus.resultset.GroupType;
 import gov.nih.nci.nautilus.resultset.ReporterResultset;
 import gov.nih.nci.nautilus.resultset.ResultSet;
 import gov.nih.nci.nautilus.resultset.ResultsetProcessor;
 import gov.nih.nci.nautilus.resultset.SampleFoldChangeValuesResultset;
 import gov.nih.nci.nautilus.resultset.SampleResultset;
 import gov.nih.nci.nautilus.resultset.SampleViewResultsContainer;
 import gov.nih.nci.nautilus.view.ViewFactory;
 import gov.nih.nci.nautilus.view.ViewType;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * @author SahniH
  * Date: Oct 26, 2004
  * 
  */
 public class ResultsetViewTest extends TestCase {
     ArrayPlatformCriteria allPlatformCrit;
     GeneIDCriteria geneCrit;
     FoldChangeCriteria foldCrit;
     GeneExpressionQuery geneQuery;
 	ResultsetProcessor resultsetProc = new ResultsetProcessor();
     ResultSet[] geneExprObjects;
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
         buildPlatformCrit();
         buildFoldChangeCrit();
         buildGeneIDCrit();
         buildGeneExprGeneSingleViewQuery();
         buildSingleQueryInCompoundQueryProcessor();
 	}
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		// TODO Auto-generated method stub
 		super.tearDown();
 	}
 
     /**
 	 * @param geneExprObjects
 	 */
 	private void print(ResultSet[] geneExprObjects) {
 		if(geneExprObjects != null){
 			System.out.println("Number of Records:"+ geneExprObjects.length);
 	        for (int i =0; i < geneExprObjects.length; i++) {
 	        	GeneExpr.GeneExprSingle expObj = (GeneExpr.GeneExprSingle) geneExprObjects[i];
 	        	if(expObj != null){
 	            System.out.println( "uID: " + expObj.getDesId() + "|geneSymbol: " + expObj.getGeneSymbol() +"|clone: " + expObj.getCloneName()+"|probeSet: "+expObj.getProbesetName()+"|biospecimenID: " + expObj.getBiospecimenId() );
 	        	}
 	        }
 		}
 		
 	}
 
 	public static Test suite() {
 		TestSuite suit =  new TestSuite();
         suit.addTest(new TestSuite(ResultsetViewTest.class));
         return suit;
 	}
 
 	public static void main (String[] args) {
 		junit.textui.TestRunner.run(suite());
 
     }
     private void buildGeneIDCrit() {
         geneCrit = new GeneIDCriteria();
         Collection geneList = new ArrayList();
         geneList.add( new GeneIdentifierDE.GeneSymbol("KIF1B"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("EGFR"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("SHREW1"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("VEGF"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("NEK1"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("AGA"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("AADAT"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("IRX4"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("CTNND2"));
         geneList.add( new GeneIdentifierDE.GeneSymbol("CDH12"));
         geneCrit.setGeneIdentifiers(geneList);
     }
     private void buildFoldChangeCrit() {
         Float upRegExpected = new Float(2.0);
         Float downRegExpected = new Float(1.0);
         ExprFoldChangeDE.UpRegulation upRegObj = new ExprFoldChangeDE.UpRegulation(upRegExpected );
         ExprFoldChangeDE.DownRegulation downRegObj = new ExprFoldChangeDE.DownRegulation(downRegExpected );
         //ExprFoldChangeDE.UnChangedRegulationUpperLimit upUnChangedObj = new ExprFoldChangeDE.UnChangedRegulationUpperLimit(upperUnchangedExpected );
         //ExprFoldChangeDE.UnChangedRegulationDownLimit downUnChangedRegObj = new ExprFoldChangeDE.UnChangedRegulationDownLimit(downUnChangedExpected );
 
         foldCrit = new FoldChangeCriteria();
         Collection objs = new ArrayList(4);
         objs.add(upRegObj);
         objs.add(downRegObj);
         //objs.add(upUnChangedObj); objs.add(downUnChangedRegObj);
         foldCrit.setFoldChangeObjects(objs);
     }
     private void buildPlatformCrit() {
         allPlatformCrit = new ArrayPlatformCriteria(new ArrayPlatformDE(Constants.ALL_PLATFROM));
     }
 	private void buildSingleQueryInCompoundQueryProcessor() {
 		try {
 			//test Single Query
 			System.out.println("Building Single Gene Compound Query>>>>>>>>>>>>>>>>>>>>>>>");
 			CompoundQuery myCompoundQuery = new CompoundQuery(geneQuery);
 			geneExprObjects = QueryManager.executeQuery(myCompoundQuery);
 			System.out.println("SingleQuery:\n"+ myCompoundQuery.toString());
 			print(geneExprObjects);
 	    	assertNotNull(geneExprObjects);
 	        assertTrue(geneExprObjects.length > 0);
 	    	resultsetProc.handleGeneExprView(geneExprObjects, GroupType.DISEASE_TYPE_GROUP );
 		} catch (Exception e) {
 			e.printStackTrace();
 			}
 		}
     private void buildGeneExprGeneSingleViewQuery(){
         geneQuery = (GeneExpressionQuery) QueryManager.createQuery(QueryType.GENE_EXPR_QUERY_TYPE);
         geneQuery.setQueryName("GeneQuery");
         geneQuery.setAssociatedView(ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
         geneQuery.setGeneIDCrit(geneCrit);
         geneQuery.setArrayPlatformCrit(allPlatformCrit);
         geneQuery.setFoldChgCrit(foldCrit);
     }
     public void testGeneExprSingleView(){
 		System.out.println("Testing Single Gene Query for entire Query >>>>>>>>>>>>>>>>>>>>>>>");
         GeneExprSingleViewResultsContainer geneViewContainer = resultsetProc.getGeneViewResultsContainer();
        displayGeneExprSingleView(geneViewContainer);
     }
	public void displayGeneExprSingleView(GeneExprSingleViewResultsContainer geneViewContainer){
 		final DecimalFormat resultFormat = new DecimalFormat("0.00");		 
     	Collection genes = geneViewContainer.getGeneResultsets();
     	Collection labels = geneViewContainer.getGroupsLabels();
     	Collection sampleIds = null;
     	StringBuffer header = new StringBuffer();
     	StringBuffer sampleNames = new StringBuffer();
         StringBuffer stringBuffer = new StringBuffer();
     	//get group size (as Disease or Agegroup )from label.size
         
         System.out.println("GroupSize= "+labels.size());
         for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
         	String label = (String) labelIterator.next();
         	System.out.println(label);
         	sampleIds = geneViewContainer.getBiospecimenLabels(label); 
         	//For each group get the number of samples in it from sampleIds.size()
             System.out.println("SampleSize= "+sampleIds.size());
            	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
            		System.out.println(sampleIdIterator.next()); 
            	}
            	 
     	}
     	
         //set up the header for the table
     	header.append("Gene\tReporter\t");
     	sampleNames.append("Name\tName\t\t");
 	   
     	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
         	String label = (String) labelIterator.next();
         	header.append("|"+label.substring(0,3)+"\t"); //remove this for table
         	sampleIds = geneViewContainer.getBiospecimenLabels(label);        	
 	           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 	            	sampleNames.append(sampleIdIterator.next()+"\t"); 
 	            	header.append("\t");
 	           	}
            	header.deleteCharAt(header.lastIndexOf("\t"));
     	}
     	header.append("|"); 
 
     	//System.out.println("Gene Count: "+genes.size());
 		System.out.println(header.toString());
 		System.out.println(sampleNames.toString());
     	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
     		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
     		String geneSymbol = geneResultset.getGeneSymbol().getValue().toString();
     		Collection reporters = geneViewContainer.getRepoterResultsets(geneSymbol); //geneResultset.getReporterResultsets();
         	//System.out.println("Repoter Count: "+reporters.size());
     		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
         		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
         		String reporterName = reporterResultset.getReporter().getValue().toString();
         		Collection groupTypes = geneViewContainer.getGroupResultsets(geneSymbol,reporterName); //reporterResultset.getGroupResultsets();
         		stringBuffer = new StringBuffer();
             	//System.out.println("Group Count: "+groupTypes.size());
         		if(reporterName.length()< 10){ //Remove this from table
         			reporterName= reporterName+"        ";
         			reporterName = reporterName.substring(0,10);
         		}
         		//get the gene name, and reported Name
         		
         		stringBuffer.append(geneSymbol+"\t"+
     					reporterName+"\t");
         		for (Iterator groupIterator = groupTypes.iterator(); groupIterator.hasNext();) {
         			GroupResultset groupResultset = (GroupResultset)groupIterator.next();
         			String label = groupResultset.getType().getValue().toString();
         			sampleIds = geneViewContainer.getBiospecimenLabels(label);
                      	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
                        		String sampleId = (String) sampleIdIterator.next();
                        		SampleFoldChangeValuesResultset sampleResultset = (SampleFoldChangeValuesResultset) groupResultset.getBioSpecimenResultset(sampleId);//geneViewContainer.getBioSpecimentResultset(geneSymbol,reporterName,label,sampleId);
                        		if(sampleResultset != null){
                        			Double ratio = (Double)sampleResultset.getFoldChangeRatioValue().getValue();
                       			stringBuffer.append(resultFormat.format(ratio)+"\t");                                 
                        		}
                        		else 
                        		{
                        			stringBuffer.append("\t");
                        		}
                        	}
          		}
         		System.out.println(stringBuffer.toString());
     		}
 
     	}
 	
 	}
 	public void testSampleView(){
 		   System.out.println("Testing Sample View for entire Query >>>>>>>>>>>>>>>>>>>>>>>");
 	       SampleViewResultsContainer sampleViewContainer = resultsetProc.getSampleViewResultsContainer();
 	       Collection samples = sampleViewContainer.getBioSpecimenResultsets();
  		   System.out.println("SAMPLE\tAGE\tGENDER\tSURVIVAL\tDISEASE");
   		   StringBuffer stringBuffer = new StringBuffer();
    		for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
    			SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();
 
    			System.out.println(sampleResultset.getBiospecimen().getValue()+
    					"\t"+sampleResultset.getAgeGroup().getValue()+
 					"\t"+sampleResultset.getGenderCode().getValue()+
 					"\t"+sampleResultset.getSurvivalLengthRange().getValue()+
 					"\t"+sampleResultset.getDisease().getValue());
     		}
 	}
 	public void testGeneViewForEverySample(){
 		   System.out.println("Testing Sample View for Every Gene >>>>>>>>>>>>>>>>>>>>>>>");
 	       SampleViewResultsContainer sampleViewContainer = resultsetProc.getSampleViewResultsContainer();
 	       Collection samples = sampleViewContainer.getBioSpecimenResultsets();
 		for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
 			SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();
 			String sampleId = sampleResultset.getBiospecimen().getValue().toString();
 			displayGeneViewForASample(sampleId);
  		}
 	}
 	private void displayGeneViewForASample(String sampleID){
 	       SampleViewResultsContainer sampleViewContainer = resultsetProc.getSampleViewResultsContainer();
 	       SampleResultset sampleResultset = (SampleResultset) sampleViewContainer.getBioSpecimenResultset(sampleID);
 	       GeneExprSingleViewResultsContainer geneViewContainer = sampleResultset.getGeneExprSingleViewResultsContainer();
	       displayGeneExprSingleView(geneViewContainer);	       
 	}
     private void changeQueryView(Query query,ViewType view){
     	if(query !=null){
     		query.setAssociatedView(view);
     	}
     }
 }
