 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.testcases;
 
 import static org.ops4j.pax.exam.CoreOptions.bundle;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.ops4j.pax.exam.util.PathUtils;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.pax.test.msg.device.EletronicMsg;
 import fr.imag.adele.apam.pax.test.msg.m1.producer.impl.M1ConsumerImpl01;
 import fr.imag.adele.apam.pax.test.msg.m1.producer.impl.M1ProducerImpl;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class MessageTest extends ExtensionAbstract {
 
 	@Override
 	public List<Option> config() {
 		// TODO Auto-generated method stub
 
 		List<Option> defaults = super.config();
 		defaults.add(0,packApamDynaMan());
		defaults.add(bundle("file://" + PathUtils.getBaseDir()
				+ "/bundle/wireadmin.jar"));
 		
 //		defaults.add(mavenBundle("fr.imag.adele.apam", "apam-maven-plugin").version("0.0.2-SNAPSHOT"));
 		
 		return defaults;
 	}
 
 //	@Test
 //	public void testFieldType() throws ParseException {
 //		
 //		String ipojoMetadata = "fr.imag.adele.apam:implementation { " +
 //					" $name=\"MessageConsumeImpl01\" " +
 //					" $classname=\"fr.imag.adele.apam.pax.test.msg.m1.producer.impl.M1ConsumerImpl01\" " +
 //					" $specification=\"MessageConsumerSpec01\" " +
 //					"" +
 //					"fr.imag.adele.apam:dependency { $pull=\"list\" $multiple=\"true\" }" +
 //					"" +
 //					"manipulation { " +
 //					" field { $name=\"list\" $type=\"java.util.List\" }" +
 //					" }" +
 //					"}";
 //		
 //		Element root = ManifestMetadataParser.parseHeaderMetadata(ipojoMetadata);
 //		
 //		CoreParser parser = new CoreMetadataParser(root);
 //		List<ComponentDeclaration> components = parser.getDeclarations(new ErrorHandler() {
 //
 //			@Override
 //			public void error(Severity severity, String message) {
 //				System.err.println(message);
 //				Assert.assertTrue(false);
 //			}
 //		});
 //
 //		List <ComponentDeclaration> dependencies = new ArrayList <ComponentDeclaration> ();
 //        ApamRepoBuilder arb = new ApamRepoBuilder(components, dependencies);
 //		StringBuffer obrContent = arb.writeOBRFile();
 //
 //	}
 		
 	@Test
 	public void MessageSingleProducerSingleConsumerSendReceive_tc075() {
 
 		Implementation producerImpl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		Instance producer = producerImpl.createInstance(null, null);
 
 		Instance consumer = consumerImpl.createInstance(null, null);
 
 		M1ConsumerImpl01 m1ConsumerImpl = (M1ConsumerImpl01) consumer
 				.getServiceObject();
 
 		M1ProducerImpl m1ProdImpl = (M1ProducerImpl) producer
 				.getServiceObject();
 
 		// TODO message: forcewire in message test
 		m1ConsumerImpl.getQueue();
 
 		m1ProdImpl.pushMessage("message 1");
 
 		Assert.assertTrue(
 				String.format(
 						"After producer sends 1 message, the consumer received %d messages instead of 1",
 						m1ConsumerImpl.getQueue().size()), m1ConsumerImpl
 						.getQueue().size() == 1);
 
 		EletronicMsg message = m1ConsumerImpl.getQueue().poll();
 
 		Assert.assertTrue(
 				String.format("After producer sends 1 message, the consumer received the right number of messages but not the expected message (not the same message sent)"),
 				message.equals(new EletronicMsg("message 1")));
 
 	}
 	
 	@Test
 	public void MessageSingleProducerSingleConsumerStartingConsumerBeforeProducerSendReceive_tc082() {
 
 		Implementation producerImpl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		Instance consumer = consumerImpl.createInstance(null, null);
 		
 		M1ConsumerImpl01 m1ConsumerImpl = (M1ConsumerImpl01) consumer
 				.getServiceObject();
 
 		// TODO message: forcewire in message test
 		m1ConsumerImpl.getQueue();
 		
 		Instance producer = producerImpl.createInstance(null, null);
 		
 		M1ProducerImpl m1ProdImpl = (M1ProducerImpl) producer
 				.getServiceObject();
 
 		m1ProdImpl.pushMessage("message 1");
 
 		Assert.assertTrue(
 				String.format(
 						"In this use case where the consumer was started before the producer, after producer sends 1 message, the consumer received %d messages instead of 1 ",
 						m1ConsumerImpl.getQueue().size()), m1ConsumerImpl
 						.getQueue().size() == 1);
 
 		EletronicMsg message = m1ConsumerImpl.getQueue().poll();
 
 		Assert.assertTrue(
 				String.format("In this use case where the consumer was started before the producer, after producer sends 1 message, the consumer received the right number of messages but not the expected message (not the same message sent)"),
 				message.equals(new EletronicMsg("message 1")));
 
 	}
 
 	@Test
 	public void MessageSingleProducerSingleConsumerReceiveAllInStackOrder_tc076() {
 
 		Implementation producerImpl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		final int POOL_SIZE = 10;
 
 		Instance producer = producerImpl.createInstance(null, null);
 
 		Instance consumer = consumerImpl.createInstance(null, null);
 
 		M1ConsumerImpl01 m1ConsumerImpl = (M1ConsumerImpl01) consumer
 				.getServiceObject();
 
 		M1ProducerImpl m1ProdImpl = (M1ProducerImpl) producer
 				.getServiceObject();
 
 		// TODO message: forcewire in message test
 		m1ConsumerImpl.getQueue();
 
 		for (int x = 0; x < POOL_SIZE; x++) {
 			m1ProdImpl.pushMessage(String.format("message %d", x));
 		}
 
 		Assert.assertTrue(String.format(
 				"Producer sent %d messages but the consumer received %d",
 				POOL_SIZE, m1ConsumerImpl.getQueue().size()), m1ConsumerImpl
 				.getQueue().size() == 10);
 
 		for (int x = 0; x < POOL_SIZE; x++) {
 			EletronicMsg message = m1ConsumerImpl.getQueue().poll();
 			Assert.assertTrue(
 					String.format(
 							"The producer sent %d messages in ascending order, the consumer should receive in the same order but the message %d arrived in wrong order",
 							POOL_SIZE, x), message.equals(new EletronicMsg(
 							String.format("message %d", x))));
 		}
 
 	}
 
 	@Test
 	public void MessageMultipleProducersSingleConsumerSendReceive_tc077() {
 
 		Implementation producer01Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 		Implementation producer02Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl02");
 		Implementation producer03Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl03");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		Instance producerApam1 = producer01Impl.createInstance(null, null);
 		Instance producerApam2 = producer02Impl.createInstance(null, null);
 		Instance producerApam3 = producer03Impl.createInstance(null, null);
 
 		Instance consumerApam = consumerImpl.createInstance(null, null);
 
 		M1ConsumerImpl01 consumer = (M1ConsumerImpl01) consumerApam
 				.getServiceObject();
 
 		M1ProducerImpl producer1 = (M1ProducerImpl) producerApam1
 				.getServiceObject();
 		M1ProducerImpl producer2 = (M1ProducerImpl) producerApam2
 				.getServiceObject();
 		M1ProducerImpl producer3 = (M1ProducerImpl) producerApam3
 				.getServiceObject();
 
 		// TODO message: force wire in message test
 		consumer.getQueue();
 
 		final String message1producer1 = "message 1 produce 1";
 		final String message1producer2 = "message 2 produce 2";
 		final String message1producer3 = "message 3 produce 3";
 
 		producer1.pushMessage(message1producer1);
 		producer2.pushMessage(message1producer2);
 		producer3.pushMessage(message1producer3);
 
 		Assert.assertTrue(consumer.getQueue().size() == 3);
 
 		Assert.assertTrue(consumer.getQueue().poll()
 				.equals(new EletronicMsg(message1producer1)));
 		Assert.assertTrue(consumer.getQueue().poll()
 				.equals(new EletronicMsg(message1producer2)));
 		Assert.assertTrue(consumer.getQueue().poll()
 				.equals(new EletronicMsg(message1producer3)));
 
 	}
 
 	@Test
 	public void MessageMultipleProducersSingleConsumerEnsureOnlyQueueTypeIsAccepted_tc078() {
 
 		Implementation producer01Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		String usecase = "Single producer sending multiple messages, in the consumer site all the messages should be injected in java.util.Queue, all the other types shouldnt be filled with messages. But %s";
 
 		Instance producerApam1 = producer01Impl.createInstance(null, null);
 
 		Instance consumerApam = consumerImpl.createInstance(null, null);
 
 		M1ConsumerImpl01 consumer = (M1ConsumerImpl01) consumerApam
 				.getServiceObject();
 
 		M1ProducerImpl producer1 = (M1ProducerImpl) producerApam1
 				.getServiceObject();
 
 		// TODO message: force wire in message test
 		consumer.getQueue();
 		consumer.getList();
 		consumer.getSet();
 
 		producer1.pushMessage("message 1");
 		producer1.pushMessage("message 2");
 		producer1.pushMessage("message 3");
 
 		// ensure queue is inject
 		Assert.assertTrue(String.format(usecase,
 				"but the queue was not injected (has null value assigned)"),
 				consumer.getQueue() != null);
 		Assert.assertTrue(
 				String.format(
 						usecase,
 						"but the queue was not injected (has size different than the number of messages sent)"),
 				consumer.getQueue().size() == 3);
 		Assert.assertTrue(
 				String.format(usecase,
 						"but the queue was not injected (the message is different than the one sent)"),
 				consumer.getQueue().poll()
 						.equals(new EletronicMsg("message 1")));
 
 		// ensure that list and set type are not injected
 		Assert.assertTrue(
 				String.format(usecase, "the type List received the messages"),
 				consumer.getList() == null);
 		Assert.assertTrue(
 				String.format(usecase, "the type Set received the messages"),
 				consumer.getSet() == null);
 
 	}
 
 	@Test
 	public void MessageSingleProducersSingleConsumerAssyncSend_tc083() throws InterruptedException {
 
 		Implementation producer01Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		Instance producerApam1 = producer01Impl.createInstance(null, null);
 
 		Instance consumerApam = consumerImpl.createInstance(null, null);
 	
 		M1ConsumerImpl01 consumer = (M1ConsumerImpl01) consumerApam
 				.getServiceObject();
 
 		M1ProducerImpl producer1 = (M1ProducerImpl) producerApam1
 				.getServiceObject();
 		
 		// TODO message: force wire in message test
 		consumer.getQueue();
 	
 		List<EletronicMsg> messages1=new ArrayList<EletronicMsg>();
 		List<EletronicMsg> messages2=new ArrayList<EletronicMsg>();;
 		
 		for(int i=1;i<=20;i++){
 			messages1.add(new EletronicMsg(Integer.toString(i)));
 		}
 
 		for(int i=21;i<=50;i++){
 			messages2.add(new EletronicMsg(Integer.toString(i)));
 		}
 		
 	
 		List<EletronicMsg> allMessages=new ArrayList<EletronicMsg>();
 		allMessages.addAll(messages1);
 		allMessages.addAll(messages2);
 	
 		AsynchronousSender sender=new AsynchronousSender(producer1,messages1,messages2);
 		
 		sender.execute();
 		sender.join();
 		
 		final String message = "Given one producer and one consumer, the method that produces the message is called by different threads expecting that all messages are received, but some messages got lost therefore not seem by the consumer";
 		
 		Assert.assertTrue(message,consumer.getQueue().containsAll(allMessages));
 		
 	}
 	
 	@Test
 	public void MessageMultipleProducersSingleConsumerAssyncSend_tc084() throws InterruptedException {
 
 		Implementation producer01Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl01");
 		
 		Implementation producer02Impl = CST.apamResolver.findImplByName(null,
 				"MessageProducerImpl02");
 
 		Implementation consumerImpl = CST.apamResolver.findImplByName(null,
 				"MessageConsumeImpl01");
 
 		Instance producerApam1 = producer01Impl.createInstance(null, null);
 		Instance producerApam2 = producer02Impl.createInstance(null, null);
 
 		Instance consumerApam = consumerImpl.createInstance(null, null);
 	
 		M1ConsumerImpl01 consumer = (M1ConsumerImpl01) consumerApam
 				.getServiceObject();
 
 		M1ProducerImpl producer1 = (M1ProducerImpl) producerApam1
 				.getServiceObject();
 		M1ProducerImpl producer2 = (M1ProducerImpl) producerApam2
 				.getServiceObject();
 		
 		// TODO message: force wire in message test
 		consumer.getQueue();
 	
 		List<EletronicMsg> messages1=new ArrayList<EletronicMsg>();
 		List<EletronicMsg> messages2=new ArrayList<EletronicMsg>();;
 		
 		for(int i=1;i<=20;i++){
 			messages1.add(new EletronicMsg(Integer.toString(i)));
 		}
 
 		for(int i=21;i<=50;i++){
 			messages2.add(new EletronicMsg(Integer.toString(i)));
 		}
 		
 	
 		List<EletronicMsg> allMessages=new ArrayList<EletronicMsg>();
 		allMessages.addAll(messages1);
 		allMessages.addAll(messages2);
 	
 		
 		AsynchronousSender sender1=new AsynchronousSender(producer1,messages1);
 		AsynchronousSender sender2=new AsynchronousSender(producer2,messages2);
 				
 		sender1.execute();
 		sender2.execute();
 		
 		sender1.join();
 		sender2.join();
 		
 		final String message = "Given multiple producers and one consumer, the method that produces the message, from the different producers, are called by different threads. It is expected that all messages are received, but some messages got lost therefore not seem by the consumer";
 		
 		Assert.assertTrue(message,consumer.getQueue().containsAll(allMessages));
 		
 	}
 	
 	
 }
 
 class AsynchronousSender {
 	M1ProducerImpl producer;
 	List<List<EletronicMsg>> list=new ArrayList<List<EletronicMsg>>();
 	List<Envelope> sent=new ArrayList<Envelope>();
 	public AsynchronousSender(final M1ProducerImpl producer,List<EletronicMsg> ... messages ){
 		this.list=Arrays.asList(messages);
 		this.producer=producer;
 	}
 	
 	public void execute(){
 		
 		for(List<EletronicMsg> local:list){
 			Envelope e=new Envelope(producer, local);
 			sent.add(e);
 			e.start();
 		}
 		
 	}
 
 	public void join() {
 		// force wait until threads to be finished
 		for (Envelope env : sent) {
 			try {
 				env.join();
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 }
 
 class Envelope extends Thread {
 	M1ProducerImpl producer;
 	List<EletronicMsg> list=new ArrayList<EletronicMsg>();
 	public Envelope(final M1ProducerImpl producer,List<EletronicMsg> messages ){
 		this.list=messages;
 		this.producer=producer;
 	}
 	
 	@Override
 	public void run(){
 		
 		for(EletronicMsg msg:list){
 				producer.pushMessage(msg.toString());	
 		}
 		
 	}
 }
