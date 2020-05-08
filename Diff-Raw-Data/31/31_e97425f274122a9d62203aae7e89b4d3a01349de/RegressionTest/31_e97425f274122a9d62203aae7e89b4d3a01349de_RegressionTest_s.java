 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.GeneSetLibrary;
 
 public class RegressionTest extends TestCase {
 
 	private Enrichment app;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		app = new Enrichment(FileUtils.readResource("test_list.txt"));
 	}
 	
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite()
 	{
 		return new TestSuite( RegressionTest.class );
 	}
 
 	public void testAll() {
 		for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories())
 			for (GeneSetLibrary library : category.getLibraries())
 				assertEquivalentOutput(app.enrich(library.getName()), "test_list." + library.getName() + "_table.txt");
 		
 	}
 	
 	private void assertEquivalentOutput(Collection<EnrichedTerm> enrichedTerms, String expectedFile) {
 		Iterator<EnrichedTerm> enrichedTerm = enrichedTerms.iterator();
 		Collection<String> testResults = FileUtils.readResource(expectedFile);
 		Iterator<String> result = testResults.iterator();
 		
 		assertEquals(testResults.size(), enrichedTerms.size()+1);
 		assertEquals(result.next(), Enrichment.HEADER);
 		
 		while (enrichedTerm.hasNext())
 			assertEquals(result.next(), enrichedTerm.next().toString());
 	}
 }
