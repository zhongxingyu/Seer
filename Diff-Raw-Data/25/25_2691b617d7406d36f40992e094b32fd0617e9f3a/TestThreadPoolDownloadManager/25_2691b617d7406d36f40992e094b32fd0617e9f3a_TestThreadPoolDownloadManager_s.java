 import java.util.Collections;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import svolkov.downloader.ThreadPoolDownloadManager;
 import svolkov.downloader.api.DownloadManager;
 import svolkov.downloader.api.DownloadRequest;
 import svolkov.downloader.api.DownloadResponse;
 import svolkov.downloader.api.DownloadStatus;
 import svolkov.downloader.api.FetchCallable;
 import svolkov.downloader.api.ProtocolProvider;
 
 public class TestThreadPoolDownloadManager {
 
 	@Test
 	public void testResultAndProgress() throws Exception {
 		DownloadResponse responce = new DownloadResponse();
 		DownloadManager manager = getDelayedManager(1, 100, responce);
 		Long id = manager.addDownloadRequest(new DownloadRequest());
 		Assert.assertNull(manager.getResponse(id));
 		Thread.sleep(10);
 		Assert.assertEquals(DownloadStatus.IN_PROGRESS, manager.getStatus(id));
 		Long id2 = manager.addDownloadRequest(new DownloadRequest());
 		Thread.sleep(10);
 		Assert.assertEquals(DownloadStatus.PENDING, manager.getStatus(id2));
 		Thread.sleep(100);
 		Assert.assertEquals(DownloadStatus.DONE, manager.getStatus(id));
 		Assert.assertEquals(responce, manager.getResponse(id));
 		manager.shutdown();
 		Assert.assertEquals(responce, manager.getResponse(id));
 		Assert.assertEquals(responce, manager.removeResponse(id));
 		Assert.assertNull(manager.getResponse(id));
 		Assert.assertNull(manager.removeResponse(id));
 
 	}
 
 	@Test
 	public void testWait() throws Exception {
 		DownloadResponse responce = new DownloadResponse();
 		DownloadManager manager = getDelayedManager(1, 100, responce);
 		Long id = manager.addDownloadRequest(new DownloadRequest());
 		Assert.assertEquals(responce, manager.waitResponse(id));
 	}
 
 	@Test
 	public void testCancell() throws Exception {
 		DownloadResponse responce = new DownloadResponse();
 		DownloadManager manager = getDelayedManager(1, 100, responce);
 		Long id = manager.addDownloadRequest(new DownloadRequest());
 		Thread.sleep(10);
 		Assert.assertTrue(manager.cancelRequest(id));
 		Assert.assertEquals(DownloadStatus.CANCELLED, manager.getStatus(id));
 		Assert.assertNull(manager.getResponse(id));
 		Assert.assertNull(manager.waitResponse(id));
 		Thread.sleep(100);
 		Assert.assertNull(manager.getResponse(id));
 		Assert.assertNull(manager.waitResponse(id));
 
 		id = manager.addDownloadRequest(new DownloadRequest());
 		Thread.sleep(110);
 		Assert.assertFalse(manager.cancelRequest(id));
 		Assert.assertEquals(DownloadStatus.DONE, manager.getStatus(id));
 		Assert.assertEquals(responce, manager.getResponse(id));
 		Assert.assertEquals(responce, manager.waitResponse(id));
 
 		id = manager.addDownloadRequest(new DownloadRequest());
 		Assert.assertNull(manager.removeResponse(id));
 
 		manager.shutdown();
 	}
 
 	@Test
 	public void concurrentTest() throws Exception {
 		DownloadResponse responce = new DownloadResponse();
 		DownloadManager manager = getDelayedManager(2, 100, responce);
 		Long id1 = manager.addDownloadRequest(new DownloadRequest());
 		Long id2 = manager.addDownloadRequest(new DownloadRequest());
 		Thread.sleep(110);
 		Assert.assertEquals(DownloadStatus.DONE, manager.getStatus(id1));
 		Assert.assertEquals(DownloadStatus.DONE, manager.getStatus(id2));
 		manager.shutdown();
 	}
 
 	private ThreadPoolDownloadManager getDelayedManager(final int threads, final long timeout, final DownloadResponse responce) {
 		ProtocolProvider provider = new ProtocolProvider() {
 			
 			public FetchCallable createCallable(DownloadRequest request) {
 				return new FetchCallable() {
 
 					public DownloadResponse call() throws Exception {
 						Thread.sleep(timeout);
 						return responce;
 					}
 
 					public void clean() {
 						// TODO Auto-generated method stub
 
 					}
 				};
 			}
 			
 			public boolean accepts(DownloadRequest request) {
 				return true;
 			}
 		};
 		ThreadPoolDownloadManager manager = new ThreadPoolDownloadManager(threads, Collections.singletonList(provider));
 		return manager;
 	}
 }
