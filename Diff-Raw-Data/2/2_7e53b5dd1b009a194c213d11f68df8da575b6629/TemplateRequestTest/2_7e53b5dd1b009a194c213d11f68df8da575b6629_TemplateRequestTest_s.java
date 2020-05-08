 package org.burgers.email.integration;
 
 import org.burgers.email.client.EmailRequestClient;
 import org.burgers.email.client.TemplateEmailRequest;
 import org.burgers.email.test.support.BaseEmailSupport;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:contexts/integration-test-context.xml"})
 public class TemplateRequestTest extends BaseEmailSupport {
     private static final String TO = "to@test.com";
     private static final String FROM = "from@test.com";
     private static final String SUBJECT = "this is a subject";
     public static final String TEMPLATE_NAME = "test.template";
 
     @Autowired
     private EmailRequestClient emailRequestClient;
 
     @Test
     public void send_message() throws MessagingException, IOException, InterruptedException {
         emailRequestClient.send(createTemplateRequest());
 
        Thread.sleep(3000);
 
         assertMessageCount(1);
 
         MimeMessage message = getMessage(0);
 
         String content = (String) message.getContent();
         assertTrue(content.contains("My name is Testing"));
         assertEquals(TO, asList(message.getHeader("To")).get(0));
         assertEquals(FROM, asList(message.getHeader("From")).get(0));
         assertEquals(SUBJECT, message.getSubject());
     }
 
     private TemplateEmailRequest createTemplateRequest() {
         TemplateEmailRequest request = new TemplateEmailRequest();
         request.setSubject(SUBJECT);
         request.setTo(asList(TO));
         request.setFrom(FROM);
         Map<String, String> propertyMap = new HashMap<String, String>();
         propertyMap.put("name", "Testing");
         request.setTemplateName(TEMPLATE_NAME);
         request.setPropertyMap(propertyMap);
         return request;
     }
 
 }
