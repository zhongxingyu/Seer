 package com.github.dozedoff.commonj.hash;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.nio.file.Path;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.dozedoff.commonj.file.FileUtil;
 
 
 public class HashMakerTest {
 	byte[] testData = {12,45,6,12,99};	// SHA-256: 95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815
 	byte[] testData2 = {99,21,6,45,12}; // SHA-256: 20FC038E00E13585E68E7EBE50D79CBE7D476A74D8FDE71872627DA6CD8FC8BB
 	byte[] testData3 = {}; 				// SHA-256: E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855
 	
	Path dataPath = FileUtil.WorkingDir().toPath().resolve("test").resolve("hash");
 	
	File testFile = dataPath.resolve("test1").toFile();
	File testFile2 = dataPath.resolve("test2").toFile();
	File testFile3 = dataPath.resolve("test3").toFile();
 	
 	HashMaker hm;
 	
 	@Before
 	public void setUp() throws Exception {
 		hm = new HashMaker();
 	}
 
 	@Test
 	public void testHash1() {
 		assertThat(hm.hash(testData), is("95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815"));
 	}
 	
 	@Test
 	public void testHash2() {
 		assertThat(hm.hash(testData2), is("20FC038E00E13585E68E7EBE50D79CBE7D476A74D8FDE71872627DA6CD8FC8BB"));
 	}
 	
 	@Test
 	public void testNoData() {
 		assertThat(hm.hash(testData3), is("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"));
 	}
 	
 	@Test
 	public void testNullData(){
 		assertNull(hm.hash(null));
 	}
 	
 	@Test
 	public void testHashFile1() throws Exception {
 		System.out.println();
 		assertThat(hm.hashFile(testFile), is("95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815"));
 	}
 	
 	@Test
 	public void testHashFile2() throws Exception {
 		assertThat(hm.hashFile(testFile2), is("20FC038E00E13585E68E7EBE50D79CBE7D476A74D8FDE71872627DA6CD8FC8BB"));
 	}
 	
 	@Test
 	public void testHashFile3() throws Exception {
 		assertThat(hm.hashFile(testFile3), is("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"));
 	}
 	
 	@Test
 	public void testNoDataFile(){
 		assertNull(hm.hashFile(null));
 	}
 }
