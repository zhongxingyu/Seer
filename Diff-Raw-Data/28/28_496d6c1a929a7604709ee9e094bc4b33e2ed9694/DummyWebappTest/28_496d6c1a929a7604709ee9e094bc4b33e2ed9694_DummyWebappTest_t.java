 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package com.redhat.rhevm.api.dummy.webapp;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.HttpUnitOptions;
 import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 import com.meterware.servletunit.ServletRunner;
 import com.meterware.servletunit.ServletUnitClient;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This test asserts that the web.xml is well formed and provides a
  * lightweight sanity check that the resources are deployed properly
  * in the JAX-RS stack. The response entities are not drilled into
  * in any meaningful way.
  */
 public class DummyWebappTest extends Assert {
 
     private static final String BASE_URL = "http://somehost";
     private static final String RHEVM_CONTEXT_PATH = "/rhevm-api-dummy";
     private static final String VMS_PATH = "/vms";
     private static final String HOSTS_PATH = "/hosts";
     private static final String CONFIG_PATH = "/WEB-INF/web.xml";
     private static final String PROLOGUE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
     protected ServletRunner runner;
 
     @Before
     public void setUpServlet() throws Exception {
         InputStream config = getClass().getResourceAsStream(CONFIG_PATH);
         runner = new ServletRunner(config, RHEVM_CONTEXT_PATH);
         HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
     }
 
     @Test
     public void testVMs() throws Exception {
         doTestGet(VMS_PATH, "<name>vm9</name>");
     }
 
     @Test
     public void testVmSubResource() throws Exception {
         doTestGet(VMS_PATH + "/1", "<name>vm1</name>");
     }
 
     @Test
     public void testHosts() throws Exception {
         doTestGet(HOSTS_PATH, "<name>host3</name>");
     }
 
     @Test
     public void testHostSubResource() throws Exception {
         doTestGet(HOSTS_PATH + "/1", "<name>host1</name>");
     }
 
     @Test
     public void testVmCreation() throws Exception {
         doTestPost(VMS_PATH, "<vm><name>snafu</name></vm>", 201, "<name>snafu</name>");
     }
 
     // FIXME: fails with a 405, suspect a HttpUnit-ism
    @Test
     public void testVmUpdate() throws Exception {
        doTestPut(VMS_PATH + "/1", "<vm><name>foobar</name></vm>", 200, "<name>foobar</name>");
     }
 
     @Test
     public void testVmAction() throws Exception {
         doTestPost(VMS_PATH + "/1/migrate", "<action><async>true</async></action>", 202, null);
     }
 
     private void doTestGet(String context, String expectedReponsePattern) throws Exception {
         ServletUnitClient client = runner.newClient();
         WebRequest request = new GetMethodWebRequest(BASE_URL + RHEVM_CONTEXT_PATH + context);
         request.setHeaderField("Accept", "application/xml");
 
         verify(client.getResponse(request), 200, expectedReponsePattern);
     }
 
     private void doTestPost(String context,
                             String body,
                             int expectedStatus,
                             String expectedReponsePattern) throws Exception {
        doTestPush(context, body, expectedStatus, expectedReponsePattern, true);
    }

    private void doTestPut(String context,
                           String body,
                           int expectedStatus,
                           String expectedReponsePattern) throws Exception {
        doTestPush(context, body, expectedStatus, expectedReponsePattern, false);
    }

    private void doTestPush(String context,
                            String body,
                            int expectedStatus,
                            String expectedReponsePattern,
                            boolean isPost) throws Exception {
         ServletUnitClient client = runner.newClient();
         InputStream requestBody = new ByteArrayInputStream((PROLOGUE + body).getBytes());
         String contentType = "application/xml";
         WebRequest request =
            isPost
            ? new PostMethodWebRequest(BASE_URL + RHEVM_CONTEXT_PATH + context, requestBody, contentType)
            : new PutMethodWebRequest(BASE_URL + RHEVM_CONTEXT_PATH + context, requestBody, contentType);
         request.setHeaderField("Accept", "application/xml");
 
         verify(client.getResponse(request), expectedStatus, expectedReponsePattern);
     }
 
     private void verify(WebResponse response,
                         int expectedStatus,
                         String expectedResponsePattern) throws Exception {
         assertEquals("unexpected response code", expectedStatus, response.getResponseCode());
         // FIXME: HttpUnit seems to overwrite application/xml with text/plain
         // assertEquals("unexpected response content", "application/xml", response.getContentType());
         if (expectedResponsePattern != null) {
             InputStream is = response.getInputStream();
             String respStr = toString(is);
             assertTrue("unexpected response: " + respStr
                        + ", no match for: " + expectedResponsePattern,
                        respStr.indexOf(expectedResponsePattern) != -1);
         }
     }
 
     private String toString(InputStream is) throws IOException {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         int c = is.read();
         while (c != -1) {
             os.write(c);
             c = is.read();
         }
         os.flush();
         return os.toString();
     }
 }
