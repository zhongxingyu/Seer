 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.service;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.FacebookClient;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletContext;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.debox.photo.model.Configuration;
 import org.debox.photo.model.user.DeboxUser;
 import org.debox.photo.model.Provider;
 import org.debox.photo.model.user.ThirdPartyAccount;
 import org.debox.photo.model.user.User;
 import org.debox.photo.server.ApplicationContext;
 import org.debox.photo.thirdparty.ServiceUtil;
 import org.debox.photo.util.SessionUtils;
 import org.debox.util.HttpUtils;
 import org.debux.webmotion.server.render.Render;
 import org.scribe.exceptions.OAuthException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class HomeService extends DeboxService {
 
     private static final Logger logger = LoggerFactory.getLogger(HomeService.class);
     
     public static String getUsername() {
         Subject subject = SecurityUtils.getSubject();
         String username = null;
         Object principal = subject.getPrincipal();
         if (SessionUtils.isLogged(subject)) {
             if (principal instanceof DeboxUser) {
                 DeboxUser user = (DeboxUser) principal;
                 if (user.getFirstName() != null && user.getLastName() != null) {
                     username = user.getFirstName() + " " + user.getLastName();
                 } else {
                     username = user.getUsername();
                 }
                 
             } else if (principal instanceof ThirdPartyAccount) {
                 ThirdPartyAccount user = (ThirdPartyAccount) principal;
                 switch (user.getProviderId()) {
                     case "facebook":
                         FacebookClient client = new DefaultFacebookClient(user.getToken());
                         com.restfb.types.User me = client.fetchObject("me", com.restfb.types.User.class);
                         username = me.getFirstName() + " " + me.getLastName();
                         break;
                     case "google":
                         try {
                             String response = HttpUtils.getResponse("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + user.getToken());
                             ObjectMapper mapper = new ObjectMapper();
                             JsonNode node = mapper.readTree(response);
                             if (node.get("error") != null && node.get("error").get("code") != null) {
                                 if (node.get("error").get("code").asInt() == 401) {
                                     throw new OAuthException("google");
                                 }
                             }
                             username = node.get("name").asText();
                             
                         } catch (IOException ex) {
                             logger.error("Unable to get Google session", ex);
                         }
                         break;
                 }
                 
             }
         }
         return username;
     }
     
     public Render renderTemplates() {
         if (!ApplicationContext.isConfigured()) {
             return renderJSON("templates", getTemplates());
         }
         
         Configuration configuration = ApplicationContext.getInstance().getOverallConfiguration();
         String title = configuration.get(Configuration.Key.TITLE);
         
         Subject subject = SecurityUtils.getSubject();
         String username = getUsername();
         User user = SessionUtils.getUser(subject);
         String userId = null;
         if (user != null) {
             userId = user.getId();
         }
         
         Map<String, Object> headerData = new HashMap<>(6);
         headerData.put("title", title);
         headerData.put("avatar", null);
         headerData.put("username", username);
         headerData.put("userId", userId);
         headerData.put("administrator", subject.hasRole("administrator"));
         headerData.put("authenticated", SessionUtils.isLogged(subject));
         
         Map<String, Object> result = new HashMap<>(2);
         result.put("config", headerData);
         result.put("templates", getTemplates());
         List<Provider> providers = ServiceUtil.getAuthenticationUrls();
         result.put("hasProviders", providers != null);
         result.put("providers", providers);
         
         return renderJSON(result);
     }
 
    protected Map<String, String> getTemplates() {
        Map<String, String> templates = new HashMap<>();
 
         try {
            URL templatesDirectoryUrl = this.getClass().getClassLoader().getResource("../templates");
            if (templatesDirectoryUrl == null) {
                ServletContext servletContext = getContext().getServletContext();
                templatesDirectoryUrl = servletContext.getResource("/WEB-INF/templates");
            }
             URI templatesURI = templatesDirectoryUrl.toURI();
             File templatesDirectory = new File(templatesURI);
             if (templatesDirectory != null && templatesDirectory.isDirectory()) {
                 for (File child : templatesDirectory.listFiles()) {
                     try (FileInputStream fis = new FileInputStream(child)) {
                         
                         String filename = StringUtils.substringBeforeLast(child.getName(), ".");
                         String content = IOUtils.toString(fis, "UTF-8");
                         if ((SessionUtils.isAdministrator(SecurityUtils.getSubject()) && filename.startsWith("administration")) || !filename.startsWith("administration")) {
                             templates.put(filename, content);
                         } else {
                             templates.put(filename, "");
                         }
 
                     } catch (IOException ex) {
                         logger.error("Unable to load template " + child.getAbsolutePath(), ex);
                     }
                 }
             }
         } catch (MalformedURLException | URISyntaxException ex) {
             logger.error("Unable to load templates", ex);
         }
 
         return templates;
     }
 }
