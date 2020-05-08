 package org.ogreg.ase4j.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
import org.ogreg.ase4j.AssociationStoreException;
 import org.ogreg.ostore.memory.StringStore;
 import org.ogreg.test.Benchmark;
 import org.ogreg.test.Benchmark.Result;
 import org.ogreg.test.FileTestSupport;
 import org.ogreg.test.TestUtils;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 /**
  * File based association store benchmarks of a huge association network.
  * 
  * @author Gergely Kiss
  */
 @Test(groups = "performance")
 public class FileAssociationStoreImplBenchmark2 {
 	FileAssociationStoreImpl<String, String> store = new FileAssociationStoreImpl<String, String>();
 	private List<String> words;
 
 	@BeforeTest
 	public void before() throws IOException {
 		File file = FileTestSupport.createTempFile("fstore");
 		File dir = FileTestSupport.createTempDir("sstore");
 		StringStore ostore = new StringStore();
 		ostore.init(null, dir, new HashMap<String, String>());
 		store.setFromStore(ostore);
 		store.setToStore(ostore);
 		store.setStorageFile(file);
 		store.init();
 	}
 
 	@AfterTest
 	public void after() throws IOException {
 		store.close();
 		words = null;
 	}
 
 	/**
 	 * Tests the performance of lots of random inserts.
 	 */
 	public void testInsert01() {
 
 		try {
 			int WORDS = 100000;
 			int ASSOCS = 5000000;
 
 			words = TestUtils.randomWords(WORDS, 31);
 			Random rnd = new Random(0);
 
 			Benchmark.start();
 
 			for (int i = 0; i < ASSOCS; i++) {
 				store.add(words.get(rnd.nextInt(WORDS)), words.get(rnd.nextInt(WORDS)), 1.0F, null);
 			}
 
 			Result r = Benchmark.stop();
 
 			System.err
 					.println((ASSOCS * 1000) / r.time(TimeUnit.MILLISECONDS) + " inserts per sec");
 			System.err.println(((double) store.getStorageFile().length() / 1024 / 1.024 / r
 					.time(TimeUnit.MILLISECONDS)) + " Mb/s");
		} catch (AssociationStoreException e) {
 			throw new AssertionError(e);
 		}
 	}
 }
