 /*
  * Copyright 2008 Arthur Bogaart <spikylee at gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.spikylee.hyves4j.example.consumer.webapp;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.oauth.OAuth;
 import net.oauth.OAuth.Parameter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.spikylee.hyves4j.H4jException;
 import com.spikylee.hyves4j.Hyves4j;
 import com.spikylee.hyves4j.client.H4jClient;
 import com.spikylee.hyves4j.client.config.H4jClientConfig;
 
 public final class HyvesConsole extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     final static Logger logger = LoggerFactory.getLogger(HyvesConsole.class);
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
         //Get Console form parameters
         String action = request.getParameter("doAction");
         String oauthToken = request.getParameter("oauthToken");
         String oauthTokenSecret = request.getParameter("oauthTokenSecret");
         String fancyLayout = request.getParameter("haFancyLayout");
         String responseFields = request.getParameter("haResponseFields");
         String haMethod = request.getParameter("haMethod") != null ? request.getParameter("haMethod") : "";
         List<Parameter> params = new ArrayList<Parameter>();
         for (int i = 1; i <= 5; i++) {
             String key = request.getParameter("key" + i);
             if (key != null && key.length() > 0) {
                 params.add(new Parameter(key, request.getParameter("value" + i)));
             }
         }
 
         //Create Hyves4j config, client and Hyves4j
        URL consumerPropertiesURL = getClass().getResource("/consumer.properties");
         H4jClientConfig config = new H4jClientConfig("hyves", consumerPropertiesURL);
         if (fancyLayout != null) {
             config.setFancyLayout(true);
         }
 
         H4jClient client = null;
         if (oauthToken != null && oauthToken.length() > 0 && oauthTokenSecret != null && oauthTokenSecret.length() > 0) {
             config.setAccessToken(oauthToken);
             config.setTokenSecret(oauthTokenSecret);
             client = new H4jClient(config);
         } else {
             client = new H4jClient(config);
         }
         Hyves4j h4j = new Hyves4j(client);
 
         if (action != null && "continue".equals(action)) {
             //Execute method
             String result;
             try {
                 result = h4j.getConsole().execute(haMethod, responseFields, params);
             } catch (H4jException e) {
                 throw new ServletException(e.getErrorCode() + "\n" + e.getErrorMessage());
             }
             //print as text
             result = result.replaceAll("<", "&lt;");
             result = result.replaceAll(">", "&gt;");
             result = result.replaceAll("\n", "<br/>");
             result = result.replaceAll(" ", "&#160;");
             response.getWriter().append(result);
         } else {
             //Show Console form
             PrintWriter w = response.getWriter();
             w.append("<html><head>");
             w.append("<script language=\"javascript\" type=\"text/javascript\" src=\"resources/js/jquery-1.2.3.js\">");
             w.append("<script language=\"javascript\" type=\"text/javascript\" src=\"resources/js/formHelper.js\">");
             w.append("</head><body>");
             w.append("<h1>Hyves4j Console</h1>");
             w.append("<form name=\"consoleForm\" id=\"consoleForm\" onSubmit=\"handlePost(this.id, 'resultDiv');return false;\">");
             w.append("<table border=\"0\"><tr><td><table border=\"0\">");
 
             addLabel(w, "url", h4j.HYVES_API);
             addLabel(w, "oauthConsumerKey", client.getConsumer().consumerKey);
             addLabel(w, "oauthConsumerSecret", client.getConsumer().consumerSecret);
             addLabel(w, "oauthVersion", OAuth.VERSION_1_0);
             addLabel(w, "oauthNonce", OAuth.OAUTH_NONCE);
             addLabel(w, "oauthSignatureMethod", OAuth.HMAC_SHA1);
             addLabel(w, "haVersion", h4j.HYVES_API_VERSION);
             addLabel(w, "haFormat", h4j.HYVES_RESPONSE_FORMAT);
             addInput(w, "haFancyLayout", Boolean.toString(config.isFancyLayout()));
             addInput(w, "haResponseFields", responseFields);
 
             w.append("</table></td><td><table border=\"0\">");
 
             addInput(w, "oauthToken", client.getAccessor().accessToken);
             addInput(w, "oauthTokenSecret", client.getAccessor().tokenSecret);
             addInput(w, "haMethod", haMethod);
             int i = 1;
             for (Parameter parameter : params) {
                 w.append("<tr><td>");
                 w.append("key:value " + i);
                 w.append("</td><td>");
                 addOnlyInput(w, "key" + i, parameter.getKey());
                 addOnlyInput(w, "value" + i, parameter.getValue());
                 w.append("</td></tr>");
                 i++;
             }
             for (; i <= 5; i++) {
                 w.append("<tr><td>");
                 w.append("key:value " + i);
                 w.append("</td><td>");
                 addOnlyInput(w, "key" + i, "");
                 addOnlyInput(w, "value" + i, "");
                 w.append("</td></tr>");
             }
             w.append("</table></td></tr></table><input type=\"submit\" value=\"go\"/><input type=\"hidden\" name=\"doAction\" value=\"continue\" /></form>");
             w.append("<br/><h3>Result</h3><p><div id=\"resultDiv\"></div></p>");
             w.append("</body></html>");
         }
     }
     
     //Append new table row + text input
     private void addInput(PrintWriter w, String name, String value) throws IOException {
         if (value == null)
             value = "";
         w.append("<tr><td>");
         w.append(name + "</td><td>");
         addOnlyInput(w, name, value);
         w.append("</td></tr>");
     }
     
     //Append text input
     private void addOnlyInput(PrintWriter w, String name, String value) throws IOException {
         w.append("<input type=\"text\" name=\"" + name + "\" id=\"" + name + "\" value=\"" + value + "\"");
     }
     
     //Append table row + label
     private void addLabel(PrintWriter w, String name, String value) throws IOException {
         w.append("<tr><td>");
         w.append(name + "</td><td>" + value + "</td></tr>");
     }
 
 }
