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
 
 package org.waveprotocol.wave.examples.fedone.robots;
 
 import com.google.common.base.Strings;
 import com.google.gxp.base.GxpContext;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 import org.waveprotocol.wave.examples.fedone.account.AccountData;
 import org.waveprotocol.wave.examples.fedone.account.RobotAccountData;
 import org.waveprotocol.wave.examples.fedone.account.RobotAccountDataImpl;
 import org.waveprotocol.wave.examples.fedone.gxp.RobotRegistrationPage;
 import org.waveprotocol.wave.examples.fedone.persistence.AccountStore;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 
 import java.io.IOException;
 import java.net.URI;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet for Robot Registration.
  *
  * @author ljvderijk@google.com (Lennard de Rijk)
  */
 public class RobotRegistrationServlet extends HttpServlet {
 
   private static final Log LOG = Log.get(RobotRegistrationServlet.class);
 
   private final AccountStore accountStore;
   private final String domain;
 
   @Inject
   private RobotRegistrationServlet(
       AccountStore accountStore, @Named("wave_server_domain") String domain) {
     this.accountStore = accountStore;
     this.domain = domain;
   }
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String pathInfo = req.getPathInfo();
    if (pathInfo.equals("/register")) {
       doRegisterGet(req, resp, "");
     } else {
       resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
     }
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String pathInfo = req.getPathInfo();
    if (pathInfo.equals("/register")) {
       doRegisterPost(req, resp);
     } else {
       resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
     }
   }
 
   /**
    * Handles GET request for the register page.
    *
    * @param message non-null but optional message to show on the page
    */
   private void doRegisterGet(HttpServletRequest req, HttpServletResponse resp, String message)
       throws IOException {
     RobotRegistrationPage.write(resp.getWriter(), new GxpContext(req.getLocale()), domain, message);
     resp.setContentType("text/html");
     resp.setStatus(HttpServletResponse.SC_OK);
   }
 
   /**
    * Handles POST request for the register page.
    */
   private void doRegisterPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String username = req.getParameter("username");
     String location = req.getParameter("location");
 
     if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(location)) {
       doRegisterGet(req, resp, "Please complete all fields.");
       return;
     }
 
     ParticipantId id;
     try {
       id = ParticipantId.of(username + "@" + domain);
     } catch (InvalidParticipantAddress e) {
       doRegisterGet(req, resp, "Invalid username specified, use alphanumeric characters only.");
       return;
     }
 
     AccountData account = accountStore.getAccount(id);
     if (account != null) {
       doRegisterGet(req, resp, username + " is already in use, please choose another one.");
       return;
     }
 
     URI uri;
     try {
       uri = URI.create(location);
     } catch (IllegalArgumentException e) {
       doRegisterGet(
           req, resp, "Invalid Location specified, please specify a location in URI format.");
       return;
     }
 
     String robotLocation = "http://" + uri.getHost() + uri.getPath();
     if (robotLocation.endsWith("/")) {
       robotLocation = robotLocation.substring(0, robotLocation.length() - 1);
     }
 
     // TODO(ljvderijk): Implement the verification and handing out of consumer
     // token steps.
     RobotAccountData robotAccount =
         new RobotAccountDataImpl(id, robotLocation, "secret", null, true);
     accountStore.putAccount(robotAccount);
     LOG.info(robotAccount.getId() + " is now registered as a RobotAccount with Url "
         + robotAccount.getUrl());
 
     doRegisterGet(req, resp, "Your Robot has been succesfully registered.");
   }
 }
