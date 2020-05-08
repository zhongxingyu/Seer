 package com.horsefire.syncaws.fingerprint;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import com.horsefire.syncaws.fingerprint.DirectoryWalker.FileProcessor;
 
 public class DirectoryWalkerTest extends TestCase {
 
 	@Test
 	public void testFileList() {
 		File dir = new File("src/test/resources/sampleFiles/md5");
 		DirectoryWalker<String> engine = new DirectoryWalker<String>();
 		List<String> dirList = engine.walkDir(dir, new FileProcessor<String>() {
 			public String process(File file) {
 				return file.getName();
 			}
 		});
 		assertEquals(3, dirList.size());
 		Collections.sort(dirList);
		assertEquals("59c9a5690432939ec043b23e6d06b3fb.html", dirList.get(0));
 		assertEquals("c56c88d8651ab8d5d8d42b188cea3e6a.gif", dirList.get(1));
 		assertEquals("f3763c7b84c3f56d623391842e9fe149.txt", dirList.get(2));
 	}
 }
