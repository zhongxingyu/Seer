 /**
  * Copyright (C) 2011 Justin Lee <jlee@antwerkz.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.miloframework.tests;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.miloframework.ServletContainer;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 @Test(dataProvider = "containers")
 public class BasicServletTest extends MiloTestBase {
     public void deployServlet(ServletContainer container) throws IOException, ServletException {
         try {
             container.start();
            container.createContext("ROOT", "/", "test-parent/basic/target/basic");
             HttpClient httpclient = new DefaultHttpClient();
             HttpResponse response = httpclient.execute(new HttpGet("http://localhost:" + 8080 + "/root"));
             final StatusLine statusLine = response.getStatusLine();
             Assert.assertNotEquals(statusLine.getStatusCode(), 404, "Should not have returned a 404");
             validateHeader(response, "listener", "org.miloframework.ServletListener");
             validateHeader(response, "name", "value");
             validateHeader(response, "name2", "value2");
             validateHeader(response, "name3", "value3");
             String body = read(response.getEntity().getContent());
             Assert.assertTrue(body.contains("BasicServlet.service"));
             Assert.assertTrue(body.endsWith("###AFTER###"));
         } finally {
             container.stop();
         }
     }
 }
