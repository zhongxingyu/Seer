 /*
  * Copyright 2002-2008 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.springframework.integration.config.xml;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.channel.QueueChannel;
 import org.springframework.integration.core.Message;
 import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.core.MessagingException;
import org.springframework.integration.message.MessageBuilder;
 import org.springframework.integration.message.StringMessage;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 /**
  * @author Mark Fisher
 * @author Iwein Fuld
  */
 @ContextConfiguration
 public class BridgeParserTests extends AbstractJUnit4SpringContextTests {
 
 	@Autowired
 	@Qualifier("pollableChannel")
 	private PollableChannel pollableChannel;
 
 	@Autowired
 	@Qualifier("subscribableChannel")
 	private MessageChannel subscribableChannel;
 
 	@Autowired
	@Qualifier("stopperChannel")
	private MessageChannel stopperChannel;

	@Autowired
 	@Qualifier("output1")
 	private PollableChannel output1;
 
 	@Autowired
 	@Qualifier("output2")
 	private PollableChannel output2;
 
 	@Test
 	public void pollableChannel() {
 		Message<?> message = new StringMessage("test1");
 		this.pollableChannel.send(message);
 		Message<?> reply = this.output1.receive(1000);
 		assertEquals(message, reply);
 	}
 
 	@Test
 	public void subscribableChannel() {
 		Message<?> message = new StringMessage("test2");
 		this.subscribableChannel.send(message);
 		Message<?> reply = this.output2.receive(0);
 		assertEquals(message, reply);
 	}
 
	@Test
	public void stopperWithReplyHeader() {
		PollableChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload("test3").setReplyChannel(replyChannel).build();
		this.stopperChannel.send(message);
		Message<?> reply = replyChannel.receive(0);
		assertEquals(message, reply);
	}

	@Test(expected = MessagingException.class)
	public void stopperWithoutReplyHeader() {
		Message<?> message = MessageBuilder.withPayload("test3").build();
		this.stopperChannel.send(message);
	}

 }
