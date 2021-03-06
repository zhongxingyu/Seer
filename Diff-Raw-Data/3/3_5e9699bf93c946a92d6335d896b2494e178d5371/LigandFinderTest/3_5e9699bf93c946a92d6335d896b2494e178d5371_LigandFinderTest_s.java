 package org.biojava3.structure.align.symm.census2.analysis;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.biojava.bio.structure.align.util.AtomCache;
 import org.biojava.bio.structure.scop.ScopDatabase;
 import org.biojava.bio.structure.scop.ScopFactory;
 import org.biojava3.structure.align.symm.census2.Census;
 import org.biojava3.structure.align.symm.census2.CensusJob;
 import org.biojava3.structure.align.symm.census2.Result;
 import org.biojava3.structure.align.symm.census2.Results;
 import org.biojava3.structure.align.symm.census2.SignificanceFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  * A test for {@link LigandFinder}.
  * @author dmyerstu
  */
 public class LigandFinderTest {
 
 	private static int RADIUS = 5;
 	
 	private AtomCache cache = new AtomCache();
 	private ScopDatabase scop = ScopFactory.getSCOP(ScopFactory.VERSION_1_75B);
 
 	@Before
 	public void setUp() {
 		cache.setFetchFileEvenIfObsolete(true);
 	}
 
 	@Test
 	public void testInCenter1() {
 		// 4-helix bundles with each heme situated in center
 		String[] scopIds = new String[] {"d1hmda_", "d1hmdb_", "d1hmdc_", "d1hmdd_"};
 		for (String scopId : scopIds) {
 			String ligand = find(scopId, RADIUS);
 			assertNotNull(ligand);
 			assertTrue(ligand.contains("Fe2O"));
 		}
 	}
 
 	@Test
 	public void testNearAndFar() {
 		// A transcriptional regulator (from paper)
 		String scopId = "d3ddva1";
 		String ligand = find(scopId, 10);
 		assertNotNull(ligand);
 		assertFalse(ligand.contains(","));
 		assertTrue(ligand.contains("Mg"));
 		ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
 
 	@Test
 	public void testNotInCenter1() {
 		// callogen; ligands are actually pretty close to centroid
 		String scopId = "d1caga_";
 		String ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
 
 	@Test
 	public void testNotInCenter2() {
 		// a 4-helix bundle with a Zinc ligand outside
 		String scopId = "d1a0ba_";
 		String ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
 
 	@Test
 	public void testNotInCenter3() {
 		// symmetry along interface (from paper)
 		String scopId = "d1squa_";
 		String ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
 
 	@Test
 	public void testHybrid() {
 		// An organic sulfate is in the center, but a hydrocarbon is just outside
 		String scopId = "d3ejba1";
 		String ligand = find(scopId, RADIUS);
 		assertNotNull(ligand);
 		assertFalse(ligand.contains(","));
 		assertTrue(ligand.contains("C8O5S"));
 	}
 
 	@Test
 	public void testNoLigand() {
 		// a TIM barrel with no ligand
 		String scopId = "d1ypia_";
 		String ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
	
 
 	@Test
 	public void testAsymmetric() {
 		String scopId = "d1a19a_";
 		String ligand = find(scopId, RADIUS);
 		assertNull(ligand);
 	}
 	
 	private String find(String scopId, int radius) {
 		CensusJob job = CensusJob.forScopId(Census.AlgorithmGiver.getDefault(), SignificanceFactory.rotationallySymmetricSmart(), scopId, 0, cache, scop);
 		Result result = job.call();
 		System.out.println(result);
 		if (!SignificanceFactory.rotationallySymmetricSmart().isSignificant(result)) return null;
 		Results results = new Results();
 		results.add(result);
 		LigandFinder finder = new LigandFinder(radius);
 		finder.find(results);
 		Map<String,String> formulas = finder.getFormulas();
 		return formulas.get(scopId);
 	}
 }
