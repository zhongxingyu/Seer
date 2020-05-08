 package gov.usgs.cida.utilities.file;
 
 import static org.junit.Assert.*;
 
 import java.util.zip.ZipEntry;
 
 import org.junit.Test;
 
 public class FileHelperTest {
 
 	@Test
 	public void testEntryIsMacBundle() {
 		ZipEntry ze;
 		
 		ze = new ZipEntry(".hidden_thing");
 		assertTrue(ze.getName(),FileHelper.entryIsMacBundle(ze));
 		
 		ze = new ZipEntry(".hidden_dir/");
 		assertTrue(ze.getName(),FileHelper.entryIsMacBundle(ze));
 
 		ze = new ZipEntry("SOME_MACOSX_BUNDLE/");
 		assertTrue(ze.isDirectory());
 		
 		assertTrue(ze.getName(),FileHelper.entryIsMacBundle(ze));
 		
 		ze = new ZipEntry("NOT_MACOSX_BUNDLE");
 		assertFalse(ze.getName(),FileHelper.entryIsMacBundle(ze));
 		
 		ze = new ZipEntry("unhidden_thing");
 		assertFalse(ze.getName(),FileHelper.entryIsMacBundle(ze));
 
 		ze = new ZipEntry("unhidden_dir/");
 		assertFalse(ze.getName(),FileHelper.entryIsMacBundle(ze));
 		
 	}
 
 }
