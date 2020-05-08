 package org.pescuma.buildhealth.extractor.findbugs;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.File;
 import java.io.InputStream;
 
 import org.junit.Test;
 import org.pescuma.buildhealth.extractor.BaseExtractorTest;
 import org.pescuma.buildhealth.extractor.PseudoFiles;
 import org.pescuma.buildhealth.extractor.staticanalysis.FindBugsExtractor;
 
 public class FindBugsExtractorTest extends BaseExtractorTest {
 	
 	@Test
 	public void test1Folder1Bug() {
 		InputStream stream = load("findbugs.1folder.1bug.xml");
 		
 		FindBugsExtractor extractor = new FindBugsExtractor(new PseudoFiles(stream));
 		
 		extractor.extractTo(table, tracker);
 		
 		verify(tracker).onStreamProcessed();
 		verify(tracker, never()).onFileProcessed(any(File.class));
 		
 		assertEquals(1, table.size());
 		
		assertEquals(
				1,
				table.get(
						"Static analysis",
						"Java",
						"FindBugs",
						"C:\\buildhealth.core\\src\\org\\pescuma\\buildhealth\\computer\\loc\\LOCComputer.java",
						"86",
						"Internationalization",
						"Reliance on default encoding",
						"Found reliance on default encoding in org.pescuma.buildhealth.computer.loc.LOCComputer.createFileList(): new java.io.FileWriter(File)\n"
								+ "Found a call to a method which will perform a byte to String (or String to byte) conversion, and will assume that the default platform encoding is suitable. This will cause the application behaviour to vary between platforms. Use an alternative API and specify a charset name or Charset object explicitly."),
 				0.0001);
 	}
 	
 	@Test
 	public void test2Folder2Bugs() {
 		InputStream stream = load("findbugs.2folders.2bugs.xml");
 		
 		FindBugsExtractor extractor = new FindBugsExtractor(new PseudoFiles(stream));
 		
 		extractor.extractTo(table, tracker);
 		
 		verify(tracker).onStreamProcessed();
 		verify(tracker, never()).onFileProcessed(any(File.class));
 		
 		assertEquals(2, table.size());
 		assertEquals(2, table.sum(), 0.001);
 	}
 	
 }
