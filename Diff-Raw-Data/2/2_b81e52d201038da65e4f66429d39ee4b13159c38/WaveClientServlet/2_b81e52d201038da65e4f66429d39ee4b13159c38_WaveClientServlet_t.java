 /**
  * Copyright 2010 Google Inc.
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
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.rpc;
 
 import com.google.gxp.base.GxpContext;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.waveprotocol.wave.examples.fedone.common.SessionConstants;
 import org.waveprotocol.wave.examples.fedone.gxp.WaveClientPage;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * The HTTP servlet for serving a wave client along with content generated on
  * the server.
  *
  * @author kalman@google.com (Benjamin Kalman)
  */
 public class WaveClientServlet extends HttpServlet {
 
   private static final Log LOG = Log.get(WaveClientServlet.class);
 
   private final String domain;
 
   private WaveClientServlet(String domain) {
     this.domain = domain;
   }
 
   /**
    * Creates a servlet for the wave client.
    */
   public static WaveClientServlet create(String domain) {
     return new WaveClientServlet(domain);
   }
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) {
     try {
       WaveClientPage.write(
           response.getWriter(),
           new GxpContext(request.getLocale()),
           getSessionJson());
       response.setContentType("text/html");
       response.setStatus(HttpServletResponse.SC_OK);
     } catch (IOException e) {
       LOG.warning("Failed to write GXP for request " + request, e);
       response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   private JSONObject getSessionJson() {
     try {
       return new JSONObject()
          .put(SessionConstants.DOMAIN, domain);
     } catch (JSONException e) {
       LOG.severe("Failed to create session JSON");
       return new JSONObject();
     }
   }
 }
