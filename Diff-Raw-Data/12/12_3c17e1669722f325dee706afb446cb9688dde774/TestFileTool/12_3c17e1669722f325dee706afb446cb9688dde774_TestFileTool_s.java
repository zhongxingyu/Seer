 package de.thischwa.pmcms.tool;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 
 import org.junit.Test;
 
 import de.thischwa.pmcms.tool.file.FileTool;
 
 public class TestFileTool {
 
 	@Test
 	public void testNormalizeFileName_01() {
 		String expected = "ae_oe_ue_ss";
		assertEquals(expected, FileTool.normalizeFileName("___"));
 	}
 
 	@Test
 	public void testNormalizedFileName_02() {
 		String expected = "loeffel_1.jpg";
		File file = new File("/tmp/lffel 1.jpg");
 		String baseName = file.getName();
 		String actual = FileTool.normalizeFileName(baseName);
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void textExtension() {
 		String name = "readme";
 		assertNull(FileTool.getExtension(name));
 		
 		name = "read.me";
 		assertEquals("me", FileTool.getExtension(name));
 	}
 }
