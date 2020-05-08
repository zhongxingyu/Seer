 package br.ufmg.dcc.vod.ncrawler;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestFileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestJobExecutor;
 
 public class ThreadedCrawlerTest extends TestCase {
 
 	private File myTempDir;
 
 	@Before
 	public void setUp() {
 		String tmpDir = System.getProperty("java.io.tmpdir");
 		do  {
 			int nextInt = new Random().nextInt();
 			myTempDir = new File(tmpDir + File.separator + nextInt);
 		} while (myTempDir.exists());
 		
 		myTempDir.mkdirs();
 	}
 
 	@After
 	public void tearDown() {
 		for (File f : myTempDir.listFiles()) {
 			f.delete();
 			f.deleteOnExit();
 		}
 		myTempDir.delete();
 		myTempDir.deleteOnExit();
 	}
 	
 	@Test
 	public void testCrawl1Thread() throws Exception {
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestFileSaver saver = new TestFileSaver();
 		JobExecutor jobExecutor = new TestJobExecutor(g);
 		
 		Crawler crawler = 
 				CrawlerFactory.createThreadedCrawler(1, myTempDir, saver, 
 						jobExecutor);
 		
 		crawler.dispatch("0");
 		crawler.crawl();
 		
 		Map<Integer, byte[]> crawled = saver.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 
 
 	@Test
 	public void testCrawl2Thread() throws Exception {
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestFileSaver saver = new TestFileSaver();
 		JobExecutor jobExecutor = new TestJobExecutor(g);
 		
 		Crawler crawler = 
 				CrawlerFactory.createThreadedCrawler(2, myTempDir, saver, 
 						jobExecutor);
 		
 		crawler.dispatch("0");
 		crawler.crawl();
 		
 		Map<Integer, byte[]> crawled = saver.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 	
 	@Test
 	public void testCrawl100Thread() throws Exception {
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestFileSaver saver = new TestFileSaver();
 		JobExecutor jobExecutor = new TestJobExecutor(g);
 		
 		Crawler crawler = 
 				CrawlerFactory.createThreadedCrawler(100, myTempDir, saver, 
 						jobExecutor);
 		
 		crawler.dispatch("0");
 		crawler.crawl();
 		
 		Map<Integer, byte[]> crawled = saver.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 
 	
 	private void doTheAsserts(Map<Integer, byte[]> crawled, 
 			RandomizedSyncGraph g) {
 		assertEquals(g.getNumVertex(), crawled.size());
 		
 		for (Entry<Integer, byte[]> e: crawled.entrySet()) {
 			
 			int[] neighbours = g.getNeighbours(e.getKey());
 			IntBuffer buff = ByteBuffer.wrap(e.getValue()).asIntBuffer();
 			buff.rewind();
 			
 			for (int i = 0; i < neighbours.length; i++) {
 				assertEquals(neighbours[i], buff.get());
 			}
 			Assert.assertFalse(buff.hasRemaining());
 		}
 	}
 }
