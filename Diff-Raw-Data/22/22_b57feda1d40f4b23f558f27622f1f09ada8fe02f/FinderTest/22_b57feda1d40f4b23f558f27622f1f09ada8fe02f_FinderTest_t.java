 package com.filemuster.file;
 
import Finder;

 import java.io.File;
 import java.util.List;
 
 import org.junit.Test;
 
 public class FinderTest {
 
 	@Test
 	public void test() {
 		String filename = "test.jpg";
 		List <File> fileList = Finder.findFile(filename, directory); 
 	}
 
 }
