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
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.sql.SQLException;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 import org.debox.connector.api.exception.ProviderException;
 import org.debox.model.OAuth2Token;
 import org.debox.photo.dao.UserDao;
 import org.debox.photo.dao.thirdparty.ThirdPartyTokenWrapper;
 import org.debox.photo.model.user.DeboxUser;
 import org.debox.photo.model.Provider;
 import org.debox.photo.model.Role;
 import org.debox.photo.model.user.ThirdPartyAccount;
 import org.debox.photo.model.user.User;
 import org.debox.photo.thirdparty.ServiceUtil;
 import org.debox.photo.util.StringUtils;
 import org.debox.util.HttpUtils;
 import org.debux.webmotion.server.render.Render;
 import org.scribe.model.Verifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class AccountService extends DeboxService {
 
     private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
     
     protected UserDao userDao = new UserDao();
     protected HomeService homeController = new HomeService();
     
     public Render authenticate(String username, String password) {
         Subject currentUser = SecurityUtils.getSubject();
         UsernamePasswordToken token = new UsernamePasswordToken(username, password);
         token.setRememberMe(true);
 
         try {
             currentUser.login(token);
 
         } catch (AuthenticationException e) {
             logger.error(e.getMessage(), e);
             return renderRedirect("/#/sign-in?error");
         }
         return renderRedirect("/");
     }
     
    public Render register(String username, String password, String firstname, String lastname) throws SQLException {
        if (StringUtils.atLeastOneIsEmpty(username, password, firstname, lastname)) {
            return renderError(HttpURLConnection.HTTP_PRECON_FAILED, "Username, password, firstname and lastname are mandatory.");
         }
         
         DeboxUser user = new DeboxUser();
         user.setId(StringUtils.randomUUID());
         user.setUsername(username);
         user.setPassword(password);
         user.setFirstName(firstname);
         user.setLastName(lastname);
         
         try {
             Role role = userDao.getRole("user");
             userDao.save(user, role);
         } catch (SQLException ex) {
             logger.error("Unable to register user {}, cause: {}", username, ex.getErrorCode() + " - " + ex.getMessage());
             if (ex.getErrorCode() == 1062) {
                 return renderRedirect("/#/register?alreadyRegistered");
             } else {
                 return renderRedirect("/#/register?error");
             }
         }
         
         this.authenticate(username, password);
        return renderRedirect("/#/register?success");
     }
     
     public Render handleFacebookCallback(String code) throws SQLException {
         Verifier verifier = new Verifier(code);
         ThirdPartyTokenWrapper tokenWrapper = new ThirdPartyTokenWrapper(verifier);
         Subject subject = SecurityUtils.getSubject();
 
         if (!subject.hasRole("administrator")) {
             subject.login(tokenWrapper);
             return renderRedirect("/");
         }
         
         org.scribe.model.Token token = ServiceUtil.getFacebookService().getAccessToken(null, tokenWrapper.getCode());
         
         DefaultFacebookClient client = new DefaultFacebookClient(token.getToken());
         com.restfb.types.User fbUser = client.fetchObject("me", com.restfb.types.User.class);
         
         User user = (User) SecurityUtils.getSubject().getPrincipal();
         
         ThirdPartyAccount thirdPartyAccount = new ThirdPartyAccount(ServiceUtil.getProvider("facebook"), fbUser.getId(), token.getToken());
         thirdPartyAccount.setId(user.getId());
         thirdPartyAccount.setUsername(fbUser.getName());
         thirdPartyAccount.setAccountUrl(fbUser.getLink());
         thirdPartyAccount.setFirstName(fbUser.getFirstName());
         thirdPartyAccount.setLastName(fbUser.getLastName());
         
         userDao.save(thirdPartyAccount);
 
         user.addThirdPartyAccount(thirdPartyAccount);
         return renderRedirect("/#/account/tokens");
     }
     
     public Render handleGoogleCallback(String code) throws SQLException, ProviderException, IOException {
         Verifier verifier = new Verifier(code);
         ThirdPartyTokenWrapper tokenWrapper = new ThirdPartyTokenWrapper(verifier);
         Subject subject = SecurityUtils.getSubject();
 
         if (!subject.hasRole("administrator")) {
             subject.login(tokenWrapper);
             return renderRedirect("/");
         }
         
         OAuth2Token token = ServiceUtil.getAuthenticationToken(code);
         String response = HttpUtils.getResponse("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + token.getAccessToken());
         ObjectMapper mapper = new ObjectMapper();
         JsonNode node = mapper.readTree(response);
         
         User user = (User) SecurityUtils.getSubject().getPrincipal();
 
         ThirdPartyAccount thirdPartyAccount = new ThirdPartyAccount(ServiceUtil.getProvider("google"), node.get("email").asText(), token.getAccessToken());
         thirdPartyAccount.setId(user.getId());
         thirdPartyAccount.setUsername(node.get("name").asText());
         thirdPartyAccount.setAccountUrl(node.get("link").asText());
 
         userDao.save(thirdPartyAccount);
 
         user.addThirdPartyAccount(thirdPartyAccount);
         return renderRedirect("/#/account/tokens");
     }
     
     public Render deleteThirdPartyAccount(String id) throws SQLException {
         String providerId = StringUtils.substringBefore(id, "-");
         String providerAccountId = StringUtils.substringAfter(id, "-");
         
         Provider provider = ServiceUtil.getProvider(providerId);
         if (provider == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND);
         }
         
         ThirdPartyAccount account = userDao.getUser(providerId, providerAccountId);
         if (account == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND);
         }
         
         userDao.delete(account);
         
         User user = (User) SecurityUtils.getSubject().getPrincipal();
         user.removeThirdPartyAccount(account);
         
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
 
     public Render getLoggedUser() {
         User user = (User) SecurityUtils.getSubject().getPrincipal();
         return renderJSON(user);
     }
 
     public Render editPersonalData(String userId, String username, String firstname, String lastname) {
         try {
             DeboxUser user = (DeboxUser) SecurityUtils.getSubject().getPrincipal();
             if (!user.getId().equals(userId)) {
                 return renderError(HttpURLConnection.HTTP_FORBIDDEN, "Access denied");
             }
             
             if (StringUtils.atLeastOneIsEmpty(username, firstname, lastname)) {
                 return renderError(HttpURLConnection.HTTP_PRECON_FAILED, "Username, firstname and lastname are mandatory.");
             }
 
             user.setUsername(username);
             user.setFirstName(firstname);
             user.setLastName(lastname);
 
             userDao.updateUserInfo(user);
 
             return renderJSON("username", firstname + " " + lastname);
 
         } catch (SQLException ex) {
             logger.error(ex.getMessage(), ex);
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, null);
         }
     }
     
     public Render editCredentials(String userId, String oldPassword, String password) {
         try {
             DeboxUser user = (DeboxUser) SecurityUtils.getSubject().getPrincipal();
             if (!user.getId().equals(userId)) {
                 return renderError(HttpURLConnection.HTTP_FORBIDDEN, "Access denied");
             }
 
             // Check current credentials
             try {
                 UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), oldPassword);
                 SecurityUtils.getSecurityManager().authenticate(token);
 
             } catch (UnknownAccountException | IncorrectCredentialsException e) {
                 logger.info("Given credentials are wrong, reason: " + e.getMessage());
                 return renderError(HttpURLConnection.HTTP_UNAUTHORIZED, "Given credentials are wrong");
             }
 
             user.setPassword(password);
             userDao.update(user);
 
             return renderSuccess();
 
         } catch (SQLException ex) {
             logger.error(ex.getMessage(), ex);
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, null);
         }
     }
 
     public Render logout() {
         try {
             SecurityUtils.getSubject().logout();
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
         return renderLastPage();
     }
 
     public Render deleteAccount(String userId) {
         try {
             Subject subject = SecurityUtils.getSubject();
             DeboxUser user = (DeboxUser) subject.getPrincipal();
             if (!user.getId().equals(userId)) {
                 return renderError(HttpURLConnection.HTTP_FORBIDDEN, "Access denied");
             }
 
             userDao.deleteUser(user);
             SecurityUtils.getSecurityManager().logout(subject);
 
             return renderRedirect("/");
 
         } catch (SQLException ex) {
             logger.error(ex.getMessage(), ex);
             return renderRedirect("/#/account/" + userId + "/delete?error");
         }
     }
     
 }
