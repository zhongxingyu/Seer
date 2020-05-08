 package org.motechproject.care;
 
 import junit.framework.Assert;
 import org.antlr.stringtemplate.StringTemplate;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.care.domain.Mother;
 import org.motechproject.care.repository.AllMothers;
 import org.motechproject.care.request.CaseType;
 import org.motechproject.care.utils.RetryTask;
 import org.motechproject.care.utils.TextHelper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.UUID;
 
 
 /**
  * Unit test for simple App.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-FunctionalTests.xml")
@Ignore
 public class RegistrationFunctionalTest {
 
     @Autowired
     private AllMothers allMothers;
 
     @Test
     public void shouldPreRegisterAPregnantMother() throws IOException {
         final String caseId = UUID.randomUUID().toString();
         String instanceId = UUID.randomUUID().toString();
         String name = "test_gen" + Math.random();
 
         StringTemplate stringTemplate = getStringTemplate("/register_pregnantmother.st");
         stringTemplate.setAttribute("caseId",caseId);
         stringTemplate.setAttribute("instanceId",instanceId);
         stringTemplate.setAttribute("name", name);
         String final_xml = stringTemplate.toString();
 
 
         HttpResponse response = postToCommCare(final_xml);
         StatusLine statusLine = response.getStatusLine();
         Assert.assertEquals(201,statusLine.getStatusCode());
         Assert.assertEquals("CREATED",statusLine.getReasonPhrase());
 
         RetryTask<Mother> task = new RetryTask<Mother>() {
             @Override
             protected Mother perform() {
                 Mother mother = allMothers.findByCaseId(caseId);
                return mother!=null?mother:null;
             }
         };
 
        Mother mother = task.execute(10, 10000);
         Assert.assertEquals(name, mother.getName());
        Assert.assertEquals("fdfd", mother.getFlwId());
         Assert.assertEquals("d823ea3d392a06f8b991e9e49394ce45", mother.getGroupId());
         Assert.assertEquals(CaseType.Mother.getType(), mother.getCaseType());
     }
 
     private HttpResponse postToCommCare(String final_xml) throws IOException {
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost("https://www.commcarehq.org/a/ananya-care/receiver/");
 
         InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(final_xml.getBytes()), "file.xml");
         MultipartEntity reqEntity = new MultipartEntity();
         reqEntity.addPart("xml_submission_file", inputStreamBody);
         httpPost.setEntity(reqEntity);
 
         return httpclient.execute(httpPost);
     }
 
     private StringTemplate getStringTemplate(String templateFilePath) {
         InputStream resourceAsStream = getClass().getResourceAsStream(templateFilePath);
         String template = TextHelper.GetText(resourceAsStream);
         return new StringTemplate(template);
     }
 
 }
 
 
