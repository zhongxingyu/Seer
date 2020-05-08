 package uk.ac.ed.inf.Metabolic.sbmlexport;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class LibSBMLLoaderTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
     @Test
     public void testSBMLLibsAreLoadedInTheCorrectOrder () {
    	String [] libNames = LibSBMLLoader.SBML_WINDOWS_LIBS;
     	assertEquals("libexpat", libNames[0]);
     	assertEquals("libsbml", libNames[1]);
     	assertEquals("sbmlj", libNames[2]);
     }
 	
     
 
 }
