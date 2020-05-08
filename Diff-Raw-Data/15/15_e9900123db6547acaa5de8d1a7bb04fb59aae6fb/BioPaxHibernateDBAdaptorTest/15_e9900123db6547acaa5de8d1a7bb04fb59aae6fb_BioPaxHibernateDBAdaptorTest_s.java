 package org.bioinfo.infrared.lib.impl.hibernate;
 
 import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import org.bioinfo.infrared.core.biopax.v3.Complex;
 import org.bioinfo.infrared.core.biopax.v3.Pathway;
 import org.bioinfo.infrared.core.biopax.v3.PhysicalEntity;
 import org.bioinfo.infrared.core.biopax.v3.Protein;
 import org.bioinfo.infrared.lib.api.BioPaxDBAdaptor;
 import org.bioinfo.infrared.lib.impl.DBAdaptorFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BioPaxHibernateDBAdaptorTest {
 
 	private DBAdaptorFactory dbAdaptorFact = new HibernateDBAdaptorFactory();
 	private BioPaxDBAdaptor bioPaxDBAdaptor = dbAdaptorFact.getBioPaxDBAdaptor("hsa");
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testGetComplexStringString() {
 		Complex complex = bioPaxDBAdaptor.getComplex("Complex14", "Reactome");
 		if(complex != null) {
 			System.out.println(complex.toString());
 			for(PhysicalEntity ph: complex.getPhysicalEntities()) {
 				for(Protein prot: ph.getProteins()) {
 //					System.out.println(prot.getPhysicalEntity().getBioEntity().getNameEntities().toArray()[0].toString());
 				}
 			}
 		}else {
 			System.out.println("Not found");
 		}
 	}
 
 	@Test
 	public void testGetComplexInteger() {
 		Complex complex = bioPaxDBAdaptor.getComplex(11);
 		if(complex != null) {
 			System.out.println(complex.toString());
 //			System.out.println(complex.getPhysicalEntity().getProteins());
 		}else {
 			System.out.println("Not found");
 		}
 	}
 	
 	@Test
 	public void testGetPathwaysStringString() {
 		
 		BioPaxDBAdaptor dbAdaptor = dbAdaptorFact.getBioPaxDBAdaptor("hsa");
		List<Pathway> pathways = dbAdaptor.getPathways("Reactome", "apoptosis", true);
		System.out.println("Test3: "+pathways.size());
 	}
 
 	@Test
 	public void testGetInteractionsComplex() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPathwaysComplex() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetProteinsComplex() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPathwaysPhysicalEntity() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetNumberOfGenes() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetNumberOfInteractions() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetProteinsInteraction() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetNumberOfPathways() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPathwayStringString() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPathwaysString() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetInteractionsPathway() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetProteinsPathway() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetDataSources() {
 		fail("Not yet implemented");
 	}
 
 }
