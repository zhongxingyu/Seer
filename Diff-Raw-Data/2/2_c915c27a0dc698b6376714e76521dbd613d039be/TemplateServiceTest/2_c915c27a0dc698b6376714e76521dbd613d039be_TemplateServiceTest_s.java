 package de.flower.rmt.service.mail;
 
 import de.flower.rmt.test.AbstractIntegrationTests;
 import org.springframework.beans.factory.annotation.Value;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static de.flower.rmt.test.Assert.assertContains;
 
 /**
  * @author flowerrrr
  */
 public class TemplateServiceTest extends AbstractIntegrationTests {
 
     @Value("${app.title}")
     private String appTitle;
 
     @Value("${app.url}")
     private String appUrl;
 
     @Value("${admin.address}")
     private String adminAddress;
 
     @Test
     public void testMergeTemplate() {
         final Map<String, Object> model = new HashMap<String, Object>();
 
         model.put("bar", "bar");
        String text = templateService.mergeTemplate("test-template.vm", model);
         log.info(text);
         assertContains(text, appTitle);
         assertContains(text, appUrl);
         assertContains(text, adminAddress);
         assertContains(text, "Foo: bar");
 
     }
 }
