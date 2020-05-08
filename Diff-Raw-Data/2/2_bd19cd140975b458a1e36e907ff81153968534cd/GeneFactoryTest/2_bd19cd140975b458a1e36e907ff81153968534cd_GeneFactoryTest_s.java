 package org.bioinfo.infrared.core;
 
 import static org.junit.Assert.fail;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.infrared.common.DBConnector;
 import org.bioinfo.infrared.core.GeneDBManager;
 import org.bioinfo.infrared.core.common.FeatureList;
 import org.bioinfo.infrared.core.feature.Gene;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class GeneFactoryTest {
 
 	DBConnector ros1;
 	GeneDBManager gf1;
 	@Before
 	public void setUp() throws Exception {
 //		ros1 = new DBConnector("hsa","gen29","3306","rashid","bouchard");
 //		ros1 = new DBConnector(new File("/opt/rosetta/conf/db.conf"));
 		ros1 = new DBConnector("homo_sapiens");
 		System.out.println(ros1.toString());
 		gf1 = new GeneDBManager(ros1);
 		
 	}
 
 
 	@After
 	public void tearDown() throws Exception {
 		ros1.getDbConnection().disconnect();
 	}
 
 	@Test
 	public void testGeneFactory() {
 		try {
 			System.out.println("Test 1 - GeneDBManager");
 			FeatureList<Gene> genes = gf1.getAllByExternalId("ENSG00000000003");
 			System.out.println(genes.toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	@Test
 	public void testGeneFactoryGetTranscrits() {
 		try {
 			System.out.println("Test 2 - GeneFactoryGetTranscrits");
 			FeatureList<Gene> genes = gf1.getAllByExternalId("ENSG00000000003");
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	@Test
 	public void testGeneListFactory() {
 		try {
 			System.out.println("Test 3 - GeneListFactory");
 			List<String> gs = StringUtils.toList("BCL2 BRCA2");
 			List<FeatureList<Gene>> genes = gf1.getAllByExternalIds(gs);
 			System.out.println(genes.toString());
 //			System.out.println("Test 4 - GeneListFactory");
 //			System.out.println(genes.get(0).get(0).getTranscripts().toString());
 //			System.out.println("Test 5 - GeneListFactory");
 //			System.out.println(genes.get(0).get(0).getTranscripts().get(0).getExons().toString());
 //			System.out.println("Test 6 - GeneListFactory");
 //			System.out.println(genes.get(0).get(0).getExons().toString());
 
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 
 	@Test
 	public void testGeneByTransIdFactory() {
 		try {
 			System.out.println("Test 7 - GeneByTransIdFactory");
 			Gene gene = gf1.getByEnsemblTranscriptId("ENST00000398117");
 			System.out.println(gene.toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	@Test
 	public void testGeneByEnsIdFactory() {
 		try {
 			System.out.println("Test 8 - GeneByEnsIdFactory");
 			Gene gene = gf1.getByEnsemblId("ENSG00000000005");
 			System.out.println(gene.toString());
 			System.out.println(gene.getSequence());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	public void testAllIdsFactory() {
 		try {
 			System.out.println("Test 9 - AllIdsFactory");
 			List<String> genes = gf1.getAllEnsemblIds();
 			System.out.println(genes.get(0).toString());
 			System.out.println(genes.get(100).toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	@Test
 	public void testAllGenesFactory() {
 		try {
 			System.out.println("Test 10 - AllGenesFactory");
 			FeatureList<Gene> genes = gf1.getAll();
 			System.out.println(genes.get(0).toString());
 			System.out.println(genes.get(100).toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	@Test
 	public void testAllGenesByBiotypeFactory() {
 		try {
 			System.out.println("Test 11 - AllGenesByBiotypeFactory");
 			FeatureList<Gene> genes = gf1.getAllByBiotype("miRNA");
 			System.out.println(genes.get(0).toString());
 			System.out.println(genes.get(100).toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	@Test
 	public void testAllGenesByLocationFactory() {
 		try {
 			System.out.println("Test 12 - AllGenesByLocationFactory");
			FeatureList<Gene> genes = gf1.getAllByLocation("1", 1, 1000000000);
 			System.out.println(genes.size());
 			System.out.println(genes.get(0).toString());
 //			System.out.println(genes.getFeaturesIds().toString());
 //			System.out.println(genes.get(0).getTranscripts().toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	@Test
 	public void testAllGenesBySNPIdFactory() {
 		try {
 			System.out.println("Test 13 - AllGenesBySNPFactory");
 			FeatureList<Gene> genes = gf1.getGeneListBySNP("rs11644186");
 			System.out.println(genes.size());
 			System.out.println(genes.get(0).toString());
 //			System.out.println(genes.getFeaturesIds().toString());
 //			System.out.println(genes.get(0).getTranscripts().toString());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	public void testSequenceByEnsIdFactory() {
 		try {
 			System.out.println("Test 14 - SequenceByEnsIdFactory");
 			String seq  = gf1.getSequenceByEnsemblId("ENSG00000000005");
 			System.out.println(seq);
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 	
 	public void test() {
 		try {
 			System.out.println("Test 15 - test");
 			FeatureList<Gene> genes = gf1.test("rs3883917");
 			System.out.println(genes.size());
 			System.out.println("*************");
 			System.out.println(genes.getNumberNullElements());
 			System.out.println("==>"+genes);
 			System.out.println(genes.getFeaturesIds().toString());
 			System.out.println(genes.toString());
 			System.out.println("*************");
 			System.out.println(genes.get("ENSG00000198763"));
 			System.out.println("*************");
 //			System.out.println(genes.iterator().next().toString());
 			System.out.println("*************");
 			System.out.println(genes.getNumberNullElements());
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (ClassNotFoundException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (InstantiationException e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			fail("Not yet implemented "+e.toString());
 		}
 	}
 }
