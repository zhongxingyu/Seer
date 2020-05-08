 package com.irdeto.activemq;
 
 import junit.framework.*;
 
 public class LoadConfigurationTest extends TestCase {
	
 	public void testUrlProperties(){
 		String url = "failover://tcp://localhost:61616";
		LoadConfiguration.loadProperties("config.properties");
 		String fileUrl = LoadConfiguration.url;
 		
 		assertEquals(url,fileUrl);
 	}
 	
 	public void testFakeQueueProperties(){
 		String queue = "NOTdev";
		LoadConfiguration.loadProperties("config.properties");
 		String queueFile = LoadConfiguration.queue;
 		
 		assertNotSame(null, queue,queueFile);
 	}
 	
 	public void testQueueProperties(){
 		String queue = "dev";
		LoadConfiguration.loadProperties("config.properties");
 		String fileQueue = LoadConfiguration.queue;
 		
 		assertEquals(queue,fileQueue);
 	}
 	
 	public void testFakeUrlProperties(){
 		String url = "NOTfailover://tcp://localhost:61616";
		LoadConfiguration.loadProperties("config.properties");
 		String fileUrl = LoadConfiguration.url;
 		
 		assertNotSame(null, url,fileUrl);
 	}
 	
 }
