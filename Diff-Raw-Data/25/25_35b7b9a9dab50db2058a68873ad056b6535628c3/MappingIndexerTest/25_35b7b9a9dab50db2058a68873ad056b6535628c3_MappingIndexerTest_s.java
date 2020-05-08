 package tests.mappings;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import lib.mappings.MappingIndexer;
 
 import org.junit.Test;
 
 public class MappingIndexerTest {
 
 	@Test
 	public void testMappingIndexer() throws IOException {
 		(new File("tests/indexer")).delete(); //remove old indexer
 		
 		MappingIndexer mi = new MappingIndexer("tests/indexer");
 		
 		mi.addIndex(0, 1);
 		mi.addIndex(1, 2);
 		mi.addIndex(2, 3);
 		
 		mi.close();
 		
 		mi = new MappingIndexer("tests/indexer");
 		
 		assertEquals(1, mi.getIndex(0));
		assertEquals(2, mi.getIndex(1));
		assertEquals(3, mi.getIndex(2));
 	}
 
 }
