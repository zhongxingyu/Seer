 package org.ebayopensource.turmeric.uat.test;
 
 import static org.junit.Assert.*;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.monitoring.v1.services.GetMetricsRequest;
 import org.ebayopensource.turmeric.monitoring.v1.services.GetMetricsResponse;
 import org.ebayopensource.turmeric.monitoring.v1.services.MetricCriteria;
 import org.ebayopensource.turmeric.monitoring.v1.services.MetricGroupData;
 import org.ebayopensource.turmeric.monitoring.v1.services.MetricResourceCriteria;
 import org.ebayopensource.turmeric.monitoring.v1.services.ResourceEntity;
 import org.ebayopensource.turmeric.monitoring.v1.services.ResourceEntityRequest;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
 import org.ebayopensource.turmeric.services.monitoring.intf.SOAMetricsQueryService;
 import org.ebayopensource.turmeric.uat.v1.services.GetMessagesRequest;
 import org.ebayopensource.turmeric.uat.v1.services.GetMessagesResponse;
 import org.ebayopensource.turmeric.uat.v1.services.SendMessageRequest;
 import org.ebayopensource.turmeric.uat.v1.services.messageservice.consumer.UatMessageServiceV1Consumer;
 import org.ebayopensource.turmeric.uat.v2.services.Message;
 import org.ebayopensource.turmeric.uat.v2.services.SendMessageResponse;
 import org.ebayopensource.turmeric.uat.v2.services.messageservice.consumer.UatMessageServiceV2Consumer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UAT434Test {
 
 	String svcAdminName = "SOAMetricsQueryService";
 	String clientName = "SOAMetricsQueryService_Test";
 	Service service = null;
 	SOAMetricsQueryService m_proxy = null;
 
 	@Before
 	public void setUp() throws ServiceException {
 		service = ServiceFactory.create(svcAdminName, "production", clientName,
 				null);
 		m_proxy = service.getProxy();
 	}
 
 	@After
 	public void getMetrics() {
 		long now = System.currentTimeMillis();
 		long oneHourAgo = now - TimeUnit.SECONDS.toMillis(3600);
 		long twoHoursAgo = oneHourAgo - TimeUnit.SECONDS.toMillis(3600);
 
 		MetricCriteria metricCriteria = new MetricCriteria();
 		metricCriteria.setSecondStartTime(System.currentTimeMillis() - 360000);
 		metricCriteria.setFirstStartTime(System.currentTimeMillis() + 36000);
 		metricCriteria.setDuration(3600000);
 		metricCriteria.setAggregationPeriod(1);
 		metricCriteria.setMetricName("CallCount");
 		metricCriteria.setNumRows("100");
 		metricCriteria.setRoleType("server");
 
 		MetricResourceCriteria metricResourceCriteria = new MetricResourceCriteria();
 		ResourceEntityRequest entityRequest = new ResourceEntityRequest();
 		entityRequest.setResourceEntityType(ResourceEntity.SERVICE);
 		metricResourceCriteria.getResourceRequestEntities().add(entityRequest);
 		metricResourceCriteria
 				.setResourceEntityResponseType(ResourceEntity.OPERATION.value());
 		ResourceEntityRequest entityRequestNameV1 = new ResourceEntityRequest();
 		entityRequestNameV1.getResourceEntityName().add("UatMessageServiceV1");
 		entityRequestNameV1.getResourceEntityName().add("UatMessageServiceV2");
 
 		GetMetricsRequest metricsRequest = new GetMetricsRequest();
 		metricsRequest.setMetricCriteria(metricCriteria);
 		metricsRequest.setMetricResourceCriteria(metricResourceCriteria);
 		GetMetricsResponse metricsResp = m_proxy.getMetricsData(metricsRequest);
 		for(MetricGroupData data : metricsResp.getReturnData()){
 			System.out.println("Service ->"+data.getCriteriaInfo().getServiceName());
 			System.out.println("Operation ->"+data.getCriteriaInfo().getOperationName());
 			
 		}
 		
 		entityRequest.setResourceEntityType(ResourceEntity.CONSUMER);
 		metricResourceCriteria
 		.setResourceEntityResponseType(ResourceEntity.OPERATION.value());
 		metricCriteria.setRoleType("consumer");
 		metricsResp = m_proxy.getMetricsData(metricsRequest);
 		for(MetricGroupData data : metricsResp.getReturnData()){
 			System.out.println("Service ->"+data.getCriteriaInfo().getServiceName());
 			System.out.println("Operation ->"+data.getCriteriaInfo().getOperationName());
 			System.out.println("Consumer ->"+data.getCriteriaInfo().getConsumerName());
 			System.out.println("RoleType ->"+data.getCriteriaInfo().getRoleType());
 		}
 	}
 
 	@Test
 	public void callServiceConsumerV1Test() throws ServiceException,
 			InterruptedException {
 		UatMessageServiceV1Consumer consumerV1 = new UatMessageServiceV1Consumer(
 				"UatMessageServiceV1Consumer", "production");
 		consumerV1.getServiceInvokerOptions().setTransportName("LOCAL");
 		SendMessageRequest sendMessageReqV1 = new SendMessageRequest();
 		sendMessageReqV1.setUserName("uat-turmeric");
 		sendMessageReqV1
 				.setMessage("This message was created in " + new Date());
		try {
			consumerV1.sendMessage(sendMessageReqV1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
 		// now, get the messages back
 		// now, i get the messages back
 		GetMessagesRequest getMsgRequest = new GetMessagesRequest();
 		getMsgRequest.setUserName("uat-turmeric");
 		GetMessagesResponse getMsgResponse = consumerV1
 				.getMessages(getMsgRequest);
 		assertEquals(AckValue.SUCCESS, getMsgResponse.getAck());
 		System.out.println("getMsgResponse.getMessages="
 				+ getMsgResponse.getMessages());
 
 		Map<String, String> transportHeaders = consumerV1.getService()
 				.getResponseContext().getTransportHeaders();
 		for (String key : transportHeaders.keySet()) {
 			System.out.println("[" + key + "] ->" + transportHeaders.get(key));
 		}
 
 	}
 
 	@Test
 	public void callServiceConsumerV2Test() throws ServiceException {
 		org.ebayopensource.turmeric.uat.v2.services.SendMessageRequest request = new org.ebayopensource.turmeric.uat.v2.services.SendMessageRequest();
 		Message msg = new Message();
 		msg.setUserName("uat-turmeric");
 		msg.setPriority(BigInteger.ONE);
 		msg.setMessageContent("message sent to version 2 of the Message Service "
 				+ new Date());
 		request.setMessage(msg);
 		UatMessageServiceV2Consumer consumerV2 = new UatMessageServiceV2Consumer(
 				"UatMessageServiceV2Consumer", "production");
 		consumerV2.getServiceInvokerOptions().setTransportName("LOCAL");
 		SendMessageResponse res = consumerV2.sendMessage(request);
 		// now, i get the messages back
 		org.ebayopensource.turmeric.uat.v2.services.GetMessagesRequest getMsgRequest = new org.ebayopensource.turmeric.uat.v2.services.GetMessagesRequest();
 		getMsgRequest.setUserName("uat-turmeric");
 		org.ebayopensource.turmeric.uat.v2.services.GetMessagesResponse getMsgResponse = consumerV2
 				.getMessages(getMsgRequest);
 		assertEquals(AckValue.SUCCESS, getMsgResponse.getAck());
 		List<Message> messages = getMsgResponse.getMessages();
 		for (Message message : messages) {
 			System.out
 					.println("message.getUserName()=" + message.getUserName());
 			System.out
 					.println("message.getPriority()=" + message.getPriority());
 			System.out.println("message.getMessageContent()="
 					+ message.getMessageContent());
 		}
 
 		Map<String, String> transportHeaders = consumerV2.getService()
 				.getResponseContext().getTransportHeaders();
 		for (String key : transportHeaders.keySet()) {
 			System.out.println("[" + key + "] ->" + transportHeaders.get(key));
 		}
 
 	}
 
 }
