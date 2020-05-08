 package com.anthavio.hatatitla.test;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.junit.Assert;
 
import com.anthavio.hatatitla.HttpClient3Sender;
import com.anthavio.hatatitla.HttpSender;
import com.anthavio.hatatitla.PutRequest;
import com.anthavio.hatatitla.SenderRequest;
import com.anthavio.hatatitla.inout.ResponseBodyExtractor.ExtractedBodyResponse;
 import com.anthavio.jetty.JettyWrapper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class HttlTestMain {
 
 	static TestObject bean;
 	static {
 		bean = new TestObject();
 		bean.setDate(new Date());
 		bean.setNumber(333);
 		bean.setString("Hurray!");
 	}
 
 	public static void main(String[] args) {
 		JettyWrapper jetty = new JettyWrapper("src/main/jetty8", 0);
 		jetty.start();
 
 		try {
 			//System.setProperty("keep.alive", "true");
 			int iterations = 10000;
 			int threads = 15;
 			int port = jetty.getPort();
 
 			long millis = httl(iterations, threads, port);
 			//long millis = jersey(iterations, threads, port);
 			System.out.println(millis + " millis");
 		} catch (Exception x) {
 			x.printStackTrace();
 		} finally {
 			jetty.stop();
 		}
 	}
 
 	private static long httl(int iterations, int threads, int port) throws InterruptedException {
 		//HttpURLSender sender = new HttpURLSender("localhost:" + port);
 		//HttpClient4Sender sender = new HttpClient4Sender("localhost:" + port);
 		HttpClient3Sender sender = new HttpClient3Sender("localhost:" + port);
 		AtomicInteger counter = doSender(sender, threads * 2, threads); //little warmup
 		synchronized (counter) {
 			counter.wait();
 		}
 		long start = System.currentTimeMillis();
 		counter = doSender(sender, iterations, threads);
 		synchronized (counter) {
 			counter.wait();
 		}
 		long millis = System.currentTimeMillis() - start;
 		sender.close();
 		return millis;
 	}
 
 	private static long jersey(int iterations, int threads, int port) throws InterruptedException {
 		ClientConfig clientConfig = new DefaultClientConfig();
 		clientConfig.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE, threads);
 
 		//clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
 		Client client = Client.create(clientConfig);
 		//NonBlockingClient client = NonBlockingClient.create(clientConfig);
 		//ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);
 
 		WebResource webResource = client.resource("http://localhost:" + port + "/rest/");
 		AtomicInteger counter = doResource(webResource, threads * 2, threads);//little warmup
 		synchronized (counter) {
 			counter.wait();
 		}
 		long start = System.currentTimeMillis();
 		counter = doResource(webResource, iterations, threads);
 		synchronized (counter) {
 			counter.wait();
 		}
 		long millis = System.currentTimeMillis() - start;
 		client.destroy();
 		return millis;
 	}
 
 	private static AtomicInteger doResource(final WebResource webResource, int iterations, int threadCount) {
 		final AtomicInteger icounter = new AtomicInteger(iterations);
 		final AtomicInteger tcounter = new AtomicInteger(threadCount);
 		final Thread[] threads = new Thread[threadCount];
 		for (int i = 0; i < threads.length; i++) {
 			threads[i] = new Thread("http-" + i) {
 
 				@Override
 				public void run() {
 					while (icounter.getAndDecrement() > 0) {
 						Builder builder = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
 						doEntity(builder, 1);
 					}
 					synchronized (icounter) {
 						//System.out.println("exit " + getName());
 						int tcnt = tcounter.decrementAndGet();
 						if (tcnt == 0) {
 							icounter.notify();
 						}
 					}
 				}
 			};
 		}
 		for (int i = 0; i < threads.length; i++) {
 			threads[i].setDaemon(true);
 			//System.out.println("start " + threads[i].getName());
 			threads[i].start();
 		}
 		return icounter;
 	}
 
 	private static void doEntity(Builder builder, int iterations) {
 		for (int i = 0; i < iterations; i++) {
 			ClientResponse response = builder.put(ClientResponse.class, bean);
 			TestObject object = response.getEntity(TestObject.class);
 			Assert.assertEquals(200, response.getClientResponseStatus().getStatusCode());
 			Assert.assertEquals(bean.getNumber(), object.getNumber());
 			//System.out.println(object);
 		}
 	}
 
 	private static AtomicInteger doSender(final HttpSender sender, int iterations, int threadCount) {
 		final AtomicInteger icounter = new AtomicInteger(iterations);
 		final AtomicInteger tcounter = new AtomicInteger(threadCount);
 		Thread[] threads = new Thread[threadCount];
 		for (int i = 0; i < threads.length; i++) {
 			threads[i] = new Thread("http-" + i) {
 
 				@Override
 				public void run() {
 					while (icounter.getAndDecrement() > 0) {
						PutRequest request = sender.PUT("/rest/").body(bean, MediaType.APPLICATION_JSON).build();
 						doRequest(sender, request, 1);
 					}
 					synchronized (icounter) {
 						//System.out.println("exit " + getName());
 						int tcnt = tcounter.decrementAndGet();
 						if (tcnt == 0) {
 							icounter.notify();
 						}
 					}
 				}
 			};
 		}
 		for (int i = 0; i < threads.length; i++) {
 			threads[i].setDaemon(true);
 			//System.out.println("start " + threads[i].getName());
 			threads[i].start();
 		}
 		return icounter;
 	}
 
 	private static void doRequest(HttpSender sender, SenderRequest request, int iterations) {
 		for (int i = 0; i < iterations; i++) {
 			ExtractedBodyResponse<TestObject> extract = sender.extract(request, TestObject.class);
 			Assert.assertEquals(200, extract.getResponse().getHttpStatusCode());
 			Assert.assertEquals(bean.getNumber(), extract.getBody().getNumber());
 			//System.out.println(extract.getBody());
 		}
 	}
 
 	@XmlRootElement
 	public static class TestObject implements Serializable {
 
 		private String string;
 
 		private Date date;
 
 		private int number;
 
 		public String getString() {
 			return string;
 		}
 
 		public void setString(String string) {
 			this.string = string;
 		}
 
 		public Date getDate() {
 			return date;
 		}
 
 		public void setDate(Date date) {
 			this.date = date;
 		}
 
 		public int getNumber() {
 			return number;
 		}
 
 		public void setNumber(int number) {
 			this.number = number;
 		}
 
 		@Override
 		public String toString() {
 			return "TestObject [string=" + string + ", date=" + date + ", number=" + number + "]";
 		}
 	}
 }
