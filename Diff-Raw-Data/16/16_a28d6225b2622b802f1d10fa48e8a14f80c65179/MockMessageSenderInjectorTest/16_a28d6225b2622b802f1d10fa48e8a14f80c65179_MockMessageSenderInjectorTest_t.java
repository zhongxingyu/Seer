 /**
  * Copyright 2006 the original author or authors.
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
 package net.javacrumbs.springws.test.util;
 
 import static org.junit.Assert.assertSame;
 
 import org.junit.Test;
 import org.springframework.beans.factory.BeanCreationException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.ws.client.core.WebServiceTemplate;
 import org.springframework.ws.transport.WebServiceMessageSender;
 
 
 public class MockMessageSenderInjectorTest {
 
 	@Test
 	public void testAuto()
 	{
 		ApplicationContext context = new ClassPathXmlApplicationContext("inject/inject-auto.xml");
 		WebServiceTemplate wsTemplate = (WebServiceTemplate) context.getBean("ws-template");
 		WebServiceMessageSender mockSender = (WebServiceMessageSender) context.getBean("mock-sender");
 		assertSame(mockSender, wsTemplate.getMessageSenders()[0]);
	}
	@Test
	public void testMultiple()
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("inject/inject-multiple-templates.xml");
		WebServiceTemplate wsTemplate1 = (WebServiceTemplate) context.getBean("ws-template-1");
		WebServiceTemplate wsTemplate2 = (WebServiceTemplate) context.getBean("ws-template-2");
		WebServiceMessageSender mockSender1 = (WebServiceMessageSender) context.getBean("mock-sender-1");
		WebServiceMessageSender mockSender2 = (WebServiceMessageSender) context.getBean("mock-sender-2");
		
		assertSame(mockSender1, wsTemplate1.getMessageSenders()[0]);
		assertSame(mockSender2, wsTemplate1.getMessageSenders()[1]);
 		
		assertSame(mockSender1, wsTemplate2.getMessageSenders()[0]);
		assertSame(mockSender2, wsTemplate2.getMessageSenders()[1]);
 	}
 	@Test(expected=BeanCreationException.class)
 	public void testNoTemplate()
 	{
 		new ClassPathXmlApplicationContext("inject/inject-no-template.xml");
 	}
 }
