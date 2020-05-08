 /**
  * 
  */
 package org.opf_labs.arc_cd.collection;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import org.junit.Test;
 import org.opf_labs.arc_cd.AllArcCdCliTests;
 
 /**
  * @author carl
  * 
  */
 public class CollectionItemTest {
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.collection.CdItemRecord#fromInfoFile(java.io.File)}
 	 * 
 	 * @throws URISyntaxException 
 	 * @throws IOException 
 	 */
 	@Test
 	public void testFromFile() throws URISyntaxException, IOException {
 		File test = AllArcCdCliTests.getResourceAsFile("org/opf_labs/arc_cd/collection/00001.info");
 		CdItemRecord item = CdItemRecord.fromInfoFile(test);
 		assertTrue(item.albumArtist, item.albumArtist.equals("Pink Floyd"));
 		assertTrue(item.title.equals("Wish You Were Here"));
 		assertTrue(item.tracks.size() == 2);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.collection.ArchiveCollection#isInfoFile(File)}
 	 * 
 	 * @throws URISyntaxException
 	 */
 	@Test
 	public void testIsInfoFile() throws URISyntaxException {
 		File test = AllArcCdCliTests.getResourceAsFile("org/opf_labs/arc_cd/collection/00001.info");
 		assertTrue(ArchiveCollection.isInfoFile(test));
 	}
 }
