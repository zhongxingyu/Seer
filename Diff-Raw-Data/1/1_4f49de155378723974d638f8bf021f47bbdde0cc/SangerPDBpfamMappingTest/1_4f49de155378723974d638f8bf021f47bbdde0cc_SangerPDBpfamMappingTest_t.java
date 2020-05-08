 package test;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.junit.Test;
 
 import covariance.datacontainers.SangerPDBpfamMappingSingleLine;
 import covariance.parsers.SangerPDBpfamMapping;
 
 public class SangerPDBpfamMappingTest {
 	
 	@Test
 	public void test() throws IOException {
 		File sanger = new File ("/Users/kkreth/Desktop/Sanger_pdb_pfam_mapping.txt");
 		SangerPDBpfamMapping spf = new SangerPDBpfamMapping(sanger);
 		Integer goodLines = 195180;
 		Integer badLines = 3606;
 		assertEquals(badLines,spf.getBadlines());
 		HashMap<Integer, SangerPDBpfamMappingSingleLine> sangerLines = spf.getSangerLines();
 		Integer sizeOfSanger = sangerLines.size();
 		assertEquals(goodLines,sizeOfSanger);
 	}
 	
 
 
 }
