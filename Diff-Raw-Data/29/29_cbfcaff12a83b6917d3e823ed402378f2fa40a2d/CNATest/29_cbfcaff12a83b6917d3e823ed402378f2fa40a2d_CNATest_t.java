 package algorithms;
 
 /**
  * Copyright (C) <2011>
  * 
  * @author Jonas Ruef & Felix Langenegger <cis.unibe.ch@gmail.com>
  * @license GPLv3, for more informations see Readme.mdown
  */
 
 import helpers.BaumgartnerSampleTable;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import algorithms.cna.CNAException;
 import algorithms.cna.CNAlgorithm;
 import datastructures.cna.CNAList;
 import datastructures.cna.CNATable;
 import datastructures.random.RandomMTGeneratorHelper;
 import datastructures.random.RandomMTSetGenerator;
 
 public class CNATest extends UnitTest {
     private static CNAlgorithm cnaAlgorithm;
 
     @Before
     public void setup() throws CNAException {
 
 	cnaAlgorithm = new CNAlgorithm(
 		new BaumgartnerSampleTable().getSampleTable());
     }
 
     @Test
     public void shouldTestIdentifySUF() {
 	CNATable sufTable = new CNATable(";", ",", "A,B,D,E,C;" + "1,1,1,1,1;"
 		+ "1,1,0,1,1;" + "1,0,1,1,1;" + "0,1,1,1,1;" + "0,1,0,1,1;"
 		+ "1,0,0,0,1");
 	assertEquals(sufTable, cnaAlgorithm.getSufTable());
     }
 
     @Test
     public void shouldTestIdentifyMSUF() {
 	CNATable msufTable = new CNATable(";", ",", "$,1,$,$;" + "$,$,0,1;"
 		+ "1,$,$,$;");
 	assertEquals(msufTable, cnaAlgorithm.getMsufTable());
     }
 
     @Test
     public void shouldTestIdentifyNEC() {
 	CNAList necList = new CNAList(",", "0,10,0");
 	assertEquals(necList, cnaAlgorithm.getNecList());
     }
 
     @Test
     public void shouldTestIdentifyMNEC() {
	CNATable mnecTable = new CNATable(";", ",", "0,$,0");
 	assertEquals(mnecTable, cnaAlgorithm.getMnecTable());
     }
 
     @Test
     public void shouldTestIdentifyMinimatTheorieSet() {
 	assertEquals("[BX1 ∨ DX2 ∨ YE => E, BX1 ∨ AX2 ∨ YC => C]", cnaAlgorithm
 		.getSets().get(0).toString());
     }
 
     @Test
     public void shouldTestWithGeneratedTable() throws CNAException {
 	ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
 	ArrayList<Integer> bundle1 = new ArrayList<Integer>();
 	ArrayList<Integer> alterFactros = new ArrayList<Integer>();
 	bundle1.add(2);
 	alterFactros.add(1);
 	list.add(bundle1);
 	RandomMTGeneratorHelper helper = new RandomMTGeneratorHelper(list,
 		alterFactros, false);
 	RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		helper.getCompleteList(), false);
 
 	assertNotNull(generator.getMTSet());
 
     }
 }
