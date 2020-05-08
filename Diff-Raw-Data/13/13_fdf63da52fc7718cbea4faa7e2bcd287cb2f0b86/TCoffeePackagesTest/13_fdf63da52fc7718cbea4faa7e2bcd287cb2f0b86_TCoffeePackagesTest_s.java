 package models;
 
 import org.junit.Test;
 
 public class TCoffeePackagesTest extends TCoffeeTest {
 
 	@Test(timeout=60000)
 	public void test_amap_msa() throws Exception {
 		testWithMethod("regular", "amap_msa");
 	}	
 
 	@Test(timeout=60000)
 	public void test_kalign_msa() throws Exception {
 		testWithMethod("regular", "kalign_msa");
 	}	
 	
	@Test(timeout=60000)
	public void test_proda_msa() throws Exception {
		testWithMethod("regular", "proda_msa");
	}	
 	
 	@Test(timeout=60000)
 	public void test_prank_msa() throws Exception {
 		testWithMethod("regular", "prank_msa");
 	}	
 
 	@Test(timeout=60000)
 	public void test_fsa_msa() throws Exception {
 		testWithMethod("regular", "fsa_msa");
 	}	
 	
 		
 	@Test(timeout=60000)
	public void test_probacons4RNA_msa() throws Exception {
		testWithMethod("regular", "probacons4RNA");
 	}	
 
 	@Test(timeout=60000)
 	public void test_mus4_msa() throws Exception {
 		testWithMethod("regular", "mus4_msa");
 	}	
 	
 	
 }
