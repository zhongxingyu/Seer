 /*
  * Created on Nov 12, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.nautilus.test;
 
 import gov.nih.nci.nautilus.de.ChromosomeNumberDE;
 import gov.nih.nci.nautilus.de.CytobandDE;
 import gov.nih.nci.nautilus.lookup.AllGeneAliasLookup;
 import gov.nih.nci.nautilus.lookup.CytobandLookup;
 import gov.nih.nci.nautilus.lookup.LookupManager;
 import gov.nih.nci.nautilus.lookup.PatientDataLookup;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * @author Himanso
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class LookupManagerTest extends TestCase {
 	/**
 	 * @param string
 	 */
 	public LookupManagerTest(String string) {
 		super(string);
 	}
 
 	public static Test suite() {
 		TestSuite suite =  new TestSuite();
         //suite.addTest(new LookupManagerTest("testGetCytobandPositions"));
 //        suite.addTest(new LookupManagerTest("testgetCytobandDEs"));
         //suite.addTest(new LookupManagerTest("testGetExpPlatforms"));
         //suite.addTest(new LookupManagerTest("testGetPathways"));
         //suite.addTest(new LookupManagerTest("testGetPatientData"));
         //suite.addTest(new LookupManagerTest("testPatientData"));
 //		suite.addTest(new LookupManagerTest("testgetChrosomeCDEs"));
 		suite.addTest(new LookupManagerTest("testgetCytobandDEstoo"));
 
 
         return suite;
 	}
 
 	/*
 	 * @see TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	/*
 	 * @see TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testGetCytobandPositions() {
 		try {
 			CytobandLookup[] cytobands = LookupManager.getCytobandPositions();
 			assertNotNull(cytobands);
 			System.out.println("cbEndPos"+
 					"\tcbStart"+
 					"\tchromosome"+
 					"\tcytoband"+
 					"\tcytobandPositionId"+
 					"\torganism");
 	        for (int i =0;i<cytobands.length;i++) {
 	        	CytobandLookup cytoband = cytobands[i];
 	            System.out.println(cytoband.getCbEndPos()+
 	            					"\t"+cytoband.getCbStart()+
 									"\t"+cytoband.getChromosome()+
 									"\t"+cytoband.getCytoband()+
 									"\t"+cytoband.getCytobandPositionId()+
 									"\t"+cytoband.getOrganism());
 	        }
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	public void testgetCytobandDEs(){
 		ChromosomeNumberDE[] chromosomes;
 		try {
 			chromosomes = LookupManager.getChromosomeDEs();
 			if(chromosomes != null){
 				for(int i =0; i < chromosomes.length; i++){
 					System.out.println("Chr:"+ chromosomes[i].getValueObject());
 					CytobandDE[] cytobands = LookupManager.getCytobandDEs(chromosomes[i]);
 					if(cytobands != null){
 						for(int k = 0; k < cytobands.length; k++){
 							System.out.println("Cytos:"+ cytobands[k].getValueObject());
 						}
 					}
 					
 				}
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	public void testGetPathways() {
 		//TODO Implement getPathways().
 	}
 
 	public void testGetPatientData() {
 		//TODO Implement getPatientData().
 	}
     public void testGetExpPlatforms(){
 	    /*try{
 	    	ExpPlatformLookup[] expPlatforms = LookupManager.getExpPlatforms();
 			assertNotNull(expPlatforms);
 	        for (int i =0;i<expPlatforms.length;i++) {
 	        	ExpPlatformLookup platform = expPlatforms[i];
 	            System.out.println("expPlatformName"+ platform.getExpPlatformName()+
 	            					"\t expPlatformDesc"+platform.getExpPlatformDesc()+
 									"\t expPlatformId"+platform.getExpPlatformId());
 	        }
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		*/
     }
     public void testPatientData(){
 	    try{
 	    	PatientDataLookup[] patientData = LookupManager.getPatientData();
 			assertNotNull(patientData);
 			System.out.println("patientID"+
 					"\t survival"+
 					"\t censor");
 	        for (int i =0;i<patientData.length;i++) {
 	        	PatientDataLookup patient = patientData[i];
 	        	System.out.println(patient.getSampleId()+
     					"\t"+patient.getSurvivalLength()+
 						"\t"+patient.getCensoringStatus());
 
 	        }
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
     }
 public void testgetChrosomeCDEs(){
 	ChromosomeNumberDE[] chromosomes;
 	try {
 		chromosomes = LookupManager.getChromosomeDEs();
 		TreeSet chrNum = new TreeSet();
 		TreeSet chrStr = new TreeSet();
 		Collection returnColl = new ArrayList();
 
 		if(chromosomes != null){
 			for(int i =0; i < chromosomes.length; i++){
 
 				String x =  chromosomes[i].getValueObject();
 
 				try {
 					chrNum.add(new Integer(x));
 				}catch(NumberFormatException ex){
 					chrStr.add(x);
 				}
 
 			}
 		}
 		for (Iterator iter = chrNum.iterator(); iter.hasNext();) {
 			returnColl.add(((Integer)iter.next()).toString());
 		}
 		for (Iterator iter = chrStr.iterator(); iter.hasNext();) {
 			returnColl.add(iter.next());
 		}
 		for (Iterator iter = returnColl.iterator(); iter.hasNext();) {
 			String m = (String) iter.next();
 			System.out.println(m);
 			
 		}
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
 }
 
 public void testgetCytobandDEstoo(){
 	String[] chrNumber = {"1","2"};
 	System.out.println(chrNumber.toString());
 	for (int i = 0; i < chrNumber.length; i++) {
 	try {
 		CytobandDE[] cytobands = LookupManager.getCytobandDEs(new ChromosomeNumberDE(chrNumber[i]));
 		if(cytobands != null){
 			for(int k = 0; k < cytobands.length; k++){
 				System.out.println("CHR "+chrNumber[i]+" Cytos:"+ cytobands[k].getValueObject());
 			}
 		}
 					
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	}
 
 }
 public void testGeneSymbolAlias(){
     try{
     	List symbols = new ArrayList();
     	symbols.add("P53");
     	symbols.add("p53");
     	symbols.add("tp53");
     	symbols.add("TP53");
     	symbols.add("NAT2");
     	symbols.add("nat2");
     	symbols.add("EGFR");
 		System.out.println("Entered Symbol"+"\tAccepted Symbol"+"\tGene Name");
     	for (Iterator iter = symbols.iterator(); iter.hasNext();) {
 			String symbol = (String) iter.next();
 			System.out.print("user Input: "+symbol+"\n");
 			if(!LookupManager.isGeneSymbolFound(symbol)){
				AllGeneAliasLookup[] allGeneAlias = LookupManager.searchGeneKeyWord(symbol);
 				if(allGeneAlias != null){
 					for(int i =0; i < allGeneAlias.length ; i++){
 						AllGeneAliasLookup alias = allGeneAlias[i];
 						System.out.println(alias.getAlias()+"\t"+alias.getApprovedSymbol()+"\t"+alias.getApprovedName()+"\n");	
 					}
 				}
 			}
 			else{
 			System.out.println(symbol+" found! \n");
 			}
     	}
 
     } catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	}
 	
 
 }
