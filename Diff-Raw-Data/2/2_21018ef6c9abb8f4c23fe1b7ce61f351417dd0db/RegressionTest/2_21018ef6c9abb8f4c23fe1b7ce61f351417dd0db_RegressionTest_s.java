 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import edu.mssm.pharm.maayanlab.FileUtils;
 
 public class RegressionTest extends TestCase {
 
 	private List2Networks app;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		app = new List2Networks();
 	}
 	
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite()
 	{
 		return new TestSuite( RegressionTest.class );
 	}
 
 	public void testAll() {
 		app.run(FileUtils.readResource("test_list.txt"));
 		
 		HashMap<String, LinkedList<Term>> resultsMap = app.getEnrichmentResults(); 
 		for (String bgType : resultsMap.keySet())			
 			assertEquivalentOutput(resultsMap.get(bgType), "test_list.enrichment_" + bgType.replaceAll(" ", "_") + ".tsv");
 	}
 	
 	private void assertEquivalentOutput(Collection<Term> terms, String expectedFile) {
 		Iterator<Term> term = terms.iterator();
 		Collection<String> testResults = FileUtils.readResource(expectedFile);
 		Iterator<String> result = testResults.iterator();
 		
 		assertEquals(testResults.size(), terms.size()+1);
		assertEquals(result.next(), app.HEADER);
 		
 		while (term.hasNext())
 			assertEquals(result.next(), term.next().toString());
 	}
 }
