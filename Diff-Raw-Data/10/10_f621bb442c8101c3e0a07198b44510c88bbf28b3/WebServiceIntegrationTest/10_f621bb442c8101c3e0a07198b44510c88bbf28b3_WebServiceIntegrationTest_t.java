 /**
  * @author Brad Leege <leege@doit.wisc.edu>
  * Created on 3/18/13 at 2:09 PM
  */
 package org.jasig.portlet.blackboardvcportlet.test;
 
 import com.elluminate.sas.GetServerQuotasResponseCollection;
 import com.elluminate.sas.ObjectFactory;
 import com.elluminate.sas.ServerQuotasResponse;
 import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceTemplate;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import java.util.List;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/test-applicationContext.xml")
 public class WebServiceIntegrationTest
 {
 	private static final Logger logger = LoggerFactory.getLogger(WebServiceIntegrationTest.class);
 
 	@Autowired
 	private SASWebServiceTemplate sasWebServiceTemplate;
 
 	@Autowired
 	private ObjectFactory elluminateObjectFactory;
 
 	@Test
 	public void testConnection() throws Exception
 	{
 		logger.info("Starting testConnection()...");
		GetServerQuotasResponseCollection serverQuotasResponseCollection = (GetServerQuotasResponseCollection)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/GetServerQuotas", elluminateObjectFactory.createGetServerQuotas(null));
 		assertNotNull(serverQuotasResponseCollection);
 		List<ServerQuotasResponse> quotaResult = serverQuotasResponseCollection.getServerQuotasResponses();
 		assertNotNull(quotaResult);
 		assertTrue(quotaResult.size() > 0);
 		ServerQuotasResponse response = quotaResult.get(0);
 		assertNotNull(response.getDiskQuota());
 		assertNotNull(response.getDiskQuotaAvailable());
 		assertNotNull(response.getSessionQuota());
 		assertNotNull(response.getSessionQuotaAvailable());
 		logger.info("Finished testConnection().");
 	}
 }
