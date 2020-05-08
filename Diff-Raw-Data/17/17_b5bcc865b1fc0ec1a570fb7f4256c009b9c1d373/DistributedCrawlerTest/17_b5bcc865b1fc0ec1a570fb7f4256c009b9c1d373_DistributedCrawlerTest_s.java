 package br.ufmg.dcc.vod.ncrawler;
 
 import java.io.File;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOServer;
 import br.ufmg.dcc.vod.ncrawler.distributed.worker.JobExecutorListener;
 import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestFileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestJobExecutor;
 import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;
 
 public class DistributedCrawlerTest  extends TestCase {
 
 	private File myTempDir;
 	private ArrayList<NIOServer<?>> servers;
 
 	@Before
 	public void setUp() throws Exception {
 		String tmpDir = System.getProperty("java.io.tmpdir");
 		do  {
 			myTempDir = new File(tmpDir + File.separator + new Random().nextInt());
 		} while (myTempDir.exists());
 		
 		myTempDir.mkdirs();
 		servers = new ArrayList<NIOServer<?>>();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		for (File f : myTempDir.listFiles()) {
 			f.delete();
 			f.deleteOnExit();
 		}
 		myTempDir.delete();
 		myTempDir.deleteOnExit();
 		
 		for (NIOServer<?> server : servers) {
 			server.shutdown();
 		}
 	}
 	
 	public Set<InetSocketAddress> initiateServers(int numServers, 
 			RandomizedSyncGraph g) {
 		Set<InetSocketAddress> ids = new HashSet<>();
 		for (int i = 0; i < numServers; i++) {
 			JobExecutor jobExecutor = new TestJobExecutor(g);
 			JobExecutorListener listener = new JobExecutorListener(jobExecutor);
 			NIOServer<CrawlRequest> server = new NIOServer<>(1, "localhost", 
 					5000 + i, listener);
 			server.start(true);
 			ids.add(new InetSocketAddress("localhost", 5000 + i));
 			servers.add(server);
 		}
 		
 		return ids;
 	}
 	
 	@Test
 	public void testCrawl1Thread() throws Exception {
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		Set<InetSocketAddress> workerAddrs = initiateServers(1, g);
 		
 		TestFileSaver saver = new TestFileSaver();
 		
 		String host = "localhost";
 		Crawler crawler = 
 				CrawlerFactory.createDistributedCrawler(host, 4540, 
 						"localhost", 4541, workerAddrs, myTempDir, saver);
 		
 		crawler.dispatch("0");
 		crawler.crawl();
 		
 		Map<Integer, byte[]> crawled = saver.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 
 
 	@Test
 	public void testCrawl2Thread() throws Exception {
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		Set<InetSocketAddress> workerAddrs = initiateServers(2, g);
 		
 		TestFileSaver saver = new TestFileSaver();
 		
 		String host = "localhost";
 		Crawler crawler = 
 				CrawlerFactory.createDistributedCrawler(host, 4540, 
 						"localhost", 4541, workerAddrs, myTempDir, saver);
 		
 		crawler.dispatch("0");
 		crawler.crawl();
 		
 		Map<Integer, byte[]> crawled = saver.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 	
 	@Test
 	public void testCrawl100Thread() throws Exception {
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		Set<InetSocketAddress> workerAddrs = initiateServers(100, g);
 		
 		TestFileSaver saver = new TestFileSaver();
 		
 		String host = "localhost";
 		Crawler crawler = 
 				CrawlerFactory.createDistributedCrawler(host, 4540, 
 						"localhost", 4541, workerAddrs, myTempDir, saver);
 		
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
