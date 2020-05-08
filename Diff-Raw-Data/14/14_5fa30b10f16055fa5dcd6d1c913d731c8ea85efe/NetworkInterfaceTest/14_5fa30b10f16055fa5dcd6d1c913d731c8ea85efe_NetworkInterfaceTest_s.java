 package ch.ethz.mlmq.test.nio;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.ethz.mlmq.client.Client;
 import ch.ethz.mlmq.client.ClientImpl;
 import ch.ethz.mlmq.dto.BrokerDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.net.request.RequestResponseFactory;
 import ch.ethz.mlmq.net.response.CreateQueueResponse;
 import ch.ethz.mlmq.net.response.Response;
 import ch.ethz.mlmq.nio.BrokerNetworkInterface;
 import ch.ethz.mlmq.server.BrokerConfiguration;
 import ch.ethz.mlmq.server.processing.WorkerTask;
 import ch.ethz.mlmq.server.processing.WorkerTaskQueue;
 
 public class NetworkInterfaceTest {
 
	private Logger logger = Logger.getLogger("NetworkInterfaceTest");
 
 	private BrokerNetworkInterface networkInterface;
 	private BrokerConfiguration config;
 	private Client client;
 	private WorkerTaskQueue taskQueue;
 	private RequestResponseFactory reqRespFactory = new RequestResponseFactory();
 
 	@BeforeClass
 	public static void beforeClass() throws IOException {
 		LoggerUtil.initConsoleDebug();
 	}
 
 	@Before
 	public void before() throws IOException {
 		config = new BrokerConfiguration();
 
 		setupNetworkInterface();
 		setupClient();
 	}
 
 	private void setupClient() throws IOException {
 		BrokerDto defaultBroker = new BrokerDto();
 
 		defaultBroker.setHost("localhost");
 		defaultBroker.setPort(config.getListenPort());
 
 		client = new ClientImpl(defaultBroker);
 	}
 
 	private void setupNetworkInterface() {
 		taskQueue = new WorkerTaskQueue() {
 			@Override
 			public void enqueue(WorkerTask workerTask) {
 				doEnqueue(workerTask);
 			}
 
 		};
 
 		logger.info("Create network interface");
 		networkInterface = new BrokerNetworkInterface(config, taskQueue);
 
 		logger.info("Init network interface");
 		networkInterface.init();
 	}
 
 	@After
 	public void after() {
 		logger.info("Close network interface");
 		networkInterface.close();
 	}
 
 	@Test
 	public void testSendSimpleMessage() throws IOException {
 		QueueDto response = client.createQueue();
 
 		Assert.assertNotNull(response);
 
 		client.deleteQueue(response.getId());
 	}
 
 	/**
 	 * called by networking thread (reactor)
 	 * 
 	 * @param workerTask
 	 */
 	private void doEnqueue(final WorkerTask workerTask) {
 
 		Thread workerMock = new Thread() {
 			public void run() {
 
 				logger.info("MockWorker doing his work...");
 
 				// simuliert db select/update/delete
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					logger.severe("InterruptedException in MockWorker");
 				}
 
 				// create response
 				QueueDto queue = new QueueDto(007);
 				Response response = new CreateQueueResponse(queue);
 
 				// serialize resonse
 				CloseableByteBufferMock responseBuffer = new CloseableByteBufferMock(4000);
 				reqRespFactory.serializeResponse(response, responseBuffer.getByteBuffer());
 				workerTask.setResponseBuffer(responseBuffer);
 
 				networkInterface.getResponseQueue().enqueue(workerTask);
 				logger.info("MockWorker finished");
 			};
 		};
 
 		workerMock.setDaemon(true);
 		workerMock.start();
 	}
 }
