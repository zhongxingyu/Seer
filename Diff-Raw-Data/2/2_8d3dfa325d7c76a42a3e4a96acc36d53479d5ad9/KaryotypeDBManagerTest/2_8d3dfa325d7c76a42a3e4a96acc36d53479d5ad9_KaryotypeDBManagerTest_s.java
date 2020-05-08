 package org.bioinfo.infrared.core.dbsql;
 
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.List;
 
 import org.bioinfo.infrared.common.dbsql.DBConnector;
 import org.bioinfo.infrared.core.Chromosome;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class KaryotypeDBManagerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	
 	@Test
 	public void testGetAllDBNames() {
 		System.out.println("Test - 1");
 		try {
 			
			DBConnector man = new DBConnector("hsa");
 			KaryotypeDBManager dbMan = new KaryotypeDBManager(man);
 			System.out.println("db connector = " + man);
 			List<Chromosome> chromosomes = dbMan.getAllChromosomes();
 			for(int i=0;i<chromosomes.size(); i++) {
 				System.out.println(" ---> " + chromosomes.get(i).toString());
 			}
 			System.out.println(" -> " + chromosomes.size() + " chromosomes");
 			
 			Chromosome chromosome = dbMan.getChromosomeById("9");
 			System.out.println(" -> " + chromosome);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Not yet implemented");
 		} 
 	}
 
 }
