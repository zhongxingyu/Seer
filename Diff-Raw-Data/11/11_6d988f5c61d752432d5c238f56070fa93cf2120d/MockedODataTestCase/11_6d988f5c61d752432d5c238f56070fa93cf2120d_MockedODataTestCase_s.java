 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.odata.tests.mock;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.mule.api.MuleMessage;
 import org.mule.api.NestedProcessor;
 import org.mule.api.transport.PropertyScope;
 import org.mule.modules.odata.ODataConnector;
 import org.mule.modules.odata.factory.ODataConsumerFactory;
 import org.mule.modules.odata.odata4j.extensions.OBatchRequest;
 import org.mule.modules.odata.tests.People;
 import org.odata4j.consumer.ODataClientRequest;
 import org.odata4j.consumer.ODataConsumer;
 import org.odata4j.core.OCreateRequest;
 import org.odata4j.core.ODataVersion;
 import org.odata4j.core.OEntity;
 import org.odata4j.core.OModifyRequest;
 import org.odata4j.core.OProperty;
 import org.odata4j.format.FormatType;
 import org.odata4j.jersey.consumer.ConsumerDeleteEntityRequest;
 import org.odata4j.producer.resources.BatchBodyPart;
 
 /**
  * 
  * @author mariano.gonzalez@mulesoft.com
  *
  */
 public class MockedODataTestCase extends TestCase {
 
 	private static final String SET = "PeopleSet";
 	private static final String URL = "http://odata.netflix.com/Catalog/";
 	
 	private ODataConnector connector;
 	private ODataConsumer consumer;
 	private MuleMessage message;
 	private People people;
 	private List<BatchBodyPart> parts;
 	
 	@Override
 	protected void setUp() throws Exception {
 		this.message = Mockito.mock(MuleMessage.class);
 		
 		this.people = new People();
 		this.people.setId(33);
 		this.people.setName("Mariano");
 		
 		this.connector = new ODataConnector();
 		this.initConnector();
 	}
 
 
 	private void initConnector() {
 		this.connector.setBaseServiceUri(URL);
 		this.connector.setConsumerFactory(new ODataConsumerFactory() {
 		
 			@Override
 			public ODataConsumer newConsumer(String baseServiceUri,
 					FormatType formatType, String username, String password,
 					ODataVersion version) {
 				
 				consumer = Mockito.mock(ODataConsumer.class);
 				return consumer;
 			}
 		});
 		
 		this.connector.init();
 	}
 	
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testCreateWithInferedSetNameAndBaseUrl() {
 		final OCreateRequest<OEntity> request = Mockito.mock(OCreateRequest.class);
 		
 		Mockito.when(this.consumer.createEntity(Mockito.anyString())).thenAnswer(new Answer<OCreateRequest<OEntity>>() {
 			
 			@Override
 			public OCreateRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], SET);
 				return request;
 			}
 		});
 				
 		Mockito.when(request.properties(Mockito.any(Iterable.class))).thenAnswer(new Answer<OCreateRequest<OEntity>>() {
 			
 			@Override
 			public OCreateRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				Iterable<OProperty<?>> properties = (Iterable<OProperty<?>>) invocation.getArguments()[0];
 				
 				Iterator<OProperty<?>> it = properties.iterator();
 				OProperty<?> prop = it.next();
 				assertEquals("id property expected", prop.getName(), "id");
 				assertEquals("wrong id value", prop.getValue(), people.getId());
 				
 				prop = it.next();
 				assertEquals("name property expected", prop.getName(), "name");
 				assertEquals("wrong name", prop.getValue(), people.getName());
 				
 				return request;
 			}
 			
 		});
 		
 		this.connector.createEntity(this.message, this.people, null, null);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(URL));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testCreateWithCustomSetname() {
 		final String customSetname = "lalala";
 		final OCreateRequest<OEntity> request = Mockito.mock(OCreateRequest.class);
 		
 		Mockito.when(this.consumer.createEntity(Mockito.anyString())).thenAnswer(new Answer<OCreateRequest<OEntity>>() {
 			
 			@Override
 			public OCreateRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], customSetname);
 				return request;
 			}
 		});
 		
 		this.connector.createEntity(this.message, this.people, customSetname, null);
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testCreateWithCustomUrl() {
 		final String customUrl = "lalala/";
 		final OCreateRequest<OEntity> request = Mockito.mock(OCreateRequest.class);
 		
 		Mockito.when(this.consumer.createEntity(Mockito.anyString())).thenReturn(request);
 		
 		this.connector.createEntity(this.message, this.people, null, customUrl);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(customUrl));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testUpdateWithInferedSetNameAndBaseUrl() {
 		final OModifyRequest<OEntity> request = Mockito.mock(OModifyRequest.class);
 		
 		Mockito.when(this.consumer.mergeEntity(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenAnswer(new Answer<OModifyRequest<OEntity>>() {
 			
 			@Override
 			public OModifyRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], SET);
 				assertEquals("wrong object key", invocation.getArguments()[1], people.getId());
 				return request;
 			}
 		});
 				
 		Mockito.when(request.properties(Mockito.any(Iterable.class))).thenAnswer(new Answer<OModifyRequest<OEntity>>() {
 			
 			@Override
 			public OModifyRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				Iterable<OProperty<?>> properties = (Iterable<OProperty<?>>) invocation.getArguments()[0];
 				
 				Iterator<OProperty<?>> it = properties.iterator();
 				OProperty<?> prop = it.next();
 				assertEquals("id property expected", prop.getName(), "id");
 				assertEquals("wrong id value", prop.getValue(), people.getId());
 				
 				prop = it.next();
 				assertEquals("name property expected", prop.getName(), "name");
 				assertEquals("wrong name", prop.getValue(), people.getName());
 				
 				return request;
 			}
 			
 		});
 		
 		this.connector.updateEntity(message, people, null, "id", null);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(URL));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testUpdateWithCustomUrl() {
 		final String customUrl = "alalaa/";
 		final OModifyRequest<OEntity> request = Mockito.mock(OModifyRequest.class);
 		Mockito.when(this.consumer.mergeEntity(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(request);
 				
 		this.connector.updateEntity(message, people, null, "id", customUrl);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(customUrl));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testUpdateWithCustomSetName() {
 		final String customSetname = "alalaa";
 		final OModifyRequest<OEntity> request = Mockito.mock(OModifyRequest.class);
 			
 		Mockito.when(this.consumer.mergeEntity(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenAnswer(new Answer<OModifyRequest<OEntity>>() {
 			
 			@Override
 			public OModifyRequest<OEntity> answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], customSetname);
 				return request;
 			}
 		});
 				
 		this.connector.updateEntity(message, people, customSetname, "id", null);
 	}
 	
 	@Test
 	public void testDelete() {
 		final ConsumerDeleteEntityRequest request = Mockito.mock(ConsumerDeleteEntityRequest.class);
 		
		Mockito.when(this.consumer.deleteEntity(Mockito.anyString(), Mockito.any())).thenAnswer(new Answer<ConsumerDeleteEntityRequest>() {
 			
 			@Override
 			public ConsumerDeleteEntityRequest answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], SET);
 				assertEquals("wrong object key", invocation.getArguments()[1], people.getId());
 				return request;
 			}
 		});
 				
 		this.connector.deleteEntity(message, people, null, "id", null);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(URL));
 	}
 	
 	@Test
 	public void testDeleteWithCustomUrl() {
 		final String url = "kakak/";
 		final ConsumerDeleteEntityRequest request = Mockito.mock(ConsumerDeleteEntityRequest.class);
 		
		Mockito.when(this.consumer.deleteEntity(Mockito.anyString(), Mockito.any())).thenReturn(request);
 		this.connector.deleteEntity(message, people, null, "id", url);
 		Mockito.verify(request, Mockito.times(1)).execute(Mockito.eq(url));
 	}
 	
 	@Test
 	public void testDeleteWithCustomSetName() {
 		final String setname = "sss";
 		final ConsumerDeleteEntityRequest request = Mockito.mock(ConsumerDeleteEntityRequest.class);
 		
		Mockito.when(this.consumer.deleteEntity(Mockito.anyString(), Mockito.any())).thenAnswer(new Answer<ConsumerDeleteEntityRequest>() {
 			
 			@Override
 			public ConsumerDeleteEntityRequest answer(InvocationOnMock invocation) throws Throwable {
 				assertEquals("wrong set name", invocation.getArguments()[0], setname);
 				return request;
 			}
 		});
 				
 		this.connector.deleteEntity(message, people, setname, "id", null);
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testBatch() {
 		this.connector = new ODataConnector() {
 			
 			@Override
 			protected BatchBodyPart toBatchBodyPart(ODataClientRequest request) {
 				return new BatchBodyPart();
 			}
 		};
 		
 		this.initConnector();
 		
 		final OCreateRequest<OEntity> createRequest = Mockito.mock(OCreateRequest.class);
 		Mockito.when(this.consumer.createEntity(Mockito.anyString())).thenReturn(createRequest);
 		
 		final OModifyRequest<OEntity> updateRequest = Mockito.mock(OModifyRequest.class);
 		Mockito.when(this.consumer.mergeEntity(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(updateRequest);
 		
 		final ConsumerDeleteEntityRequest deleteRequest = Mockito.mock(ConsumerDeleteEntityRequest.class);
		Mockito.when(this.consumer.deleteEntity(Mockito.anyString(), Mockito.any())).thenReturn(deleteRequest);
 		
 		final OBatchRequest batchRequest = Mockito.mock(OBatchRequest.class);
 		Mockito.when(this.consumer.createBatch(Mockito.anyString())).thenReturn(batchRequest);
 		
 		Mockito.doAnswer(new Answer<Void>() {
 			
 			@Override
 			public Void answer(InvocationOnMock invocation) throws Throwable {
 				parts = (List<BatchBodyPart>) invocation.getArguments()[1];
 				return null;
 			}
 		}).when(this.message).setInvocationProperty(Mockito.eq(ODataConnector.BATCH_PARTS), Mockito.any(List.class));
 		
 		Mockito.when(this.message.getInvocationProperty(Mockito.eq(ODataConnector.BATCH_PARTS))).thenAnswer(new Answer<List<BatchBodyPart>>() {
 			@Override
 			public List<BatchBodyPart> answer(InvocationOnMock invocation) throws Throwable {
 				return parts;
 			}
 		});
 
 		NestedProcessor proc = new NestedProcessor() {
 			
 			@Override
 			public Object processWithExtraProperties(Map<String, Object> arg0) throws Exception {
 				return null;
 			}
 			
 			@Override
 			public Object process(Object arg0, Map<String, Object> arg1) throws Exception {
 				return null;
 			}
 			
 			@Override
 			public Object process(Object arg0) throws Exception {
 				return null;
 			}
 			
 			@Override
 			public Object process() throws Exception {
 				connector.createEntity(message, people, null, null);
 				connector.updateEntity(message, people, null, "id", null);
 				connector.deleteEntity(message, people, null, "id", null);
 				return null;
 			}
 			
 		};
 		
 		this.connector.batch(message, "", Arrays.asList(proc));
 		
 		Mockito.verify(createRequest, Mockito.never()).execute(Mockito.anyString());
 		Mockito.verify(updateRequest, Mockito.never()).execute(Mockito.anyString());
 		Mockito.verify(deleteRequest, Mockito.never()).execute(Mockito.anyString());
 		
 		Mockito.verify(this.message, Mockito.times(1)).removeProperty(ODataConnector.BATCH_PARTS, PropertyScope.INVOCATION);
 		
 		Mockito.verify(batchRequest, Mockito.times(1)).execute(Mockito.same(parts), Mockito.any(FormatType.class));
 		
 		assertEquals("wrong number of parts", parts.size(), 3);
 	}
 	
 }
