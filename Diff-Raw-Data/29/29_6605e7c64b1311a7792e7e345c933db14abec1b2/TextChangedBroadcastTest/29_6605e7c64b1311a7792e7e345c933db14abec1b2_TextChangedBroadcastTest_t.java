 /*******************************************************************************
  * Copyright  2012 tickcode.org All rights reserved. 
  *  
  * This file is part of the Tickcode collection of software
  *  
  * This file may be distributed under the terms of the tickcode.org 
  * license as defined by tickcode.org and appearing in the file 
  * license.txt included in the packaging of this file. 
  *  
  * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
  * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
  * PARTICULAR PURPOSE. 
  *  
  *  See http://www.tickcode.org/LICENSE for licensing information. 
  *   
  *  Contact ask@tickcode.org if any conditions of this licensing 
  *  are not clear to you.
  ******************************************************************************/
 package org.tickcode.example.swing;
 
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.tickcode.broadcast.BroadcastConsumer;
 import org.tickcode.broadcast.BroadcastProducer;
 import org.tickcode.broadcast.BroadcastProxy;
 import org.tickcode.broadcast.VMMessageBroker;
 import org.tickcode.example.swing.TextChangedBroadcast;
 
 
 public class TextChangedBroadcastTest {
 
 	protected class Producer implements TextChangedBroadcast{
		protected String messageReceived;
 		@Override
 		@BroadcastProducer
 		public void textChanged(String text) {
			this.messageReceived = text;
 		}
 	};
 	protected class ProducerAndConsumer implements TextChangedBroadcast{
 		protected String messageReceived;
 		@BroadcastConsumer
 		@BroadcastProducer
 		public void textChanged(String text) {
 			this.messageReceived = text;
 		};
 	};
 	protected class Consumer implements TextChangedBroadcast{
 		protected String messageReceived;
 		@BroadcastConsumer
 		public void textChanged(String text) {
 			this.messageReceived = text;
 		};
 	};
 	
 	@Test
 	public void test() {
 		VMMessageBroker broker = new VMMessageBroker();
 		Producer producer = new Producer(); 
 		broker.add(producer);
 		ProducerAndConsumer producerAndConsumer = new ProducerAndConsumer(); 
 		broker.add(producerAndConsumer);
 		Consumer consumer = new Consumer(); 
 		broker.add(consumer);
 		
 
 		String expected = null;
 		Assert.assertEquals(expected, producerAndConsumer.messageReceived);
 		Assert.assertEquals(expected, consumer.messageReceived);
 		
 		
 		expected = "Hello World from the Producer";
 		producer.textChanged(expected);
 		Assert.assertEquals(expected, producerAndConsumer.messageReceived);
 		Assert.assertEquals(expected, consumer.messageReceived);
 		
 		String expectedForConsumer = "Hello World from the ProducerAndConsumer";
 		String expectedForProducerAndConsumer = expectedForConsumer;
		producer.messageReceived = null;
 		producerAndConsumer.textChanged(expectedForConsumer);
 		Assert.assertEquals(expectedForProducerAndConsumer, producerAndConsumer.messageReceived);
 		Assert.assertEquals(expectedForConsumer, consumer.messageReceived);
		Assert.assertNull(producer.messageReceived);
		
 	}
 	
 	@Test
 	public void testUsingProxy(){
 		VMMessageBroker broker = new VMMessageBroker();
 		broker.clear();
 		broker.setUsingAspectJ(false);
 		try{
 			Producer producer = new Producer();
 			ProducerAndConsumer producerAndConsumer = new ProducerAndConsumer();
 			Consumer consumer = new Consumer();
 			
 			TextChangedBroadcast producerProxy = (TextChangedBroadcast) BroadcastProxy.newInstance(broker, producer);
 			TextChangedBroadcast producerAndConsumerProxy = (TextChangedBroadcast) BroadcastProxy.newInstance(broker, producerAndConsumer);
 			TextChangedBroadcast consumerProxy = (TextChangedBroadcast) BroadcastProxy.newInstance(broker, consumer);
 
 			String expected = null;
 			Assert.assertEquals(expected, producerAndConsumer.messageReceived);
 			Assert.assertEquals(expected, consumer.messageReceived);
 			
 			
 			expected = "Hello World from the Producer";
 			producerProxy.textChanged(expected);
 			Assert.assertEquals(expected, producerAndConsumer.messageReceived);
 			Assert.assertEquals(expected, consumer.messageReceived);
 			
 			String expectedForConsumer = "Hello World from the ProducerAndConsumer";
 			String expectedForProducerAndConsumer = expectedForConsumer;
 			producerAndConsumerProxy.textChanged(expectedForConsumer);
 			Assert.assertEquals(expectedForProducerAndConsumer, producerAndConsumer.messageReceived);
 			Assert.assertEquals(expectedForConsumer, consumer.messageReceived);
 
 		}finally{
 			broker.setUsingAspectJ(true);
 		}
 	}
 	
 	public void test100Consumers(){
 		Producer producer = new Producer();
 		ArrayList<Consumer> consumers = new ArrayList<Consumer>();
 		
 		for(int i=0; i < 100; i++){
 			consumers.add(new Consumer());
 		}
 		
 		for(int i=0; i < 100; i++){
 			Assert.assertNull(consumers.get(i));
 		}
 
 		for(int i=0; i < 100; i++){
 			Assert.assertNull(consumers.get(i).messageReceived);
 		}
 		
 		producer.textChanged("Hello World");
 
 		for(int i=0; i < 100; i++){
 			Assert.assertEquals("Hello World",consumers.get(i).messageReceived);
 		}
 
 	}
 
 
 }
