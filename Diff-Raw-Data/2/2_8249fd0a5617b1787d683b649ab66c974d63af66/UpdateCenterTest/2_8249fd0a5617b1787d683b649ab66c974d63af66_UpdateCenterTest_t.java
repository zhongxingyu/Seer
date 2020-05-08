 /*******************************************************************************
  *
  * Copyright (c) 2004-2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: 
 *
 *    Kohsuke Kawaguchi, Nikita Levyankov
  *     
  *
  *******************************************************************************/ 
 
 package hudson.model;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import net.sf.json.JSONObject;
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertTrue;
 
 /**
  * Quick test for {@link UpdateCenter}.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UpdateCenterTest {
 
     @Test
     public void testData() throws IOException {
         // check if we have the internet connectivity. See HUDSON-2095
         try {
             HttpURLConnection con = (HttpURLConnection) new URL("http://hudson-ci.org/").openConnection();
             con.setRequestMethod("HEAD");
             con.setConnectTimeout(10000); //set timeout to 10 seconds
             if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                 System.out.println("Skipping this test. Page doesn't exists");
                 return;
             }
         } catch (java.net.SocketTimeoutException e) {
             System.out.println("Skipping this test. Timeout exception");
             return;
         } catch (IOException e) {
             System.out.println("Skipping this test. No internet connectivity");
             return;
         }
 
         URL url = new URL("http://hudson-ci.org/update-center3/update-center.json?version=build");
         String jsonp = IOUtils.toString(url.openStream());
         String json = jsonp.substring(jsonp.indexOf('(')+1,jsonp.lastIndexOf(')'));
 
         UpdateSite us = new UpdateSite("default", url.toExternalForm());
         UpdateSite.Data data = us.new Data(JSONObject.fromObject(json));
        assertTrue(data.core.url.startsWith("http://hudson-ci.org/") || data.core.url.startsWith("http://download.eclipse.org/"));
         assertTrue(data.plugins.containsKey("rake"));
         System.out.println(data.core.url);
     }
 }
