 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.digt.web;
 
 import com.digt.auth.AuthProvider;
 import com.digt.model.UserAccount;
 import com.digt.service.UserAccountService;
 import com.digt.util.PwdUtils;
 import com.digt.web.beans.json.Message;
 import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationEngine;
 import edu.internet2.middleware.shibboleth.idp.authn.LoginHandler;
 import edu.internet2.middleware.shibboleth.idp.authn.UsernamePrincipal;
 import java.security.NoSuchAlgorithmException;
 import java.security.Principal;
import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.security.auth.Subject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.rave.model.User;
import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  *
  * @author
  * wasa
  */
 @Controller
 public class LoginController {
     
     private final UserAccountService userService;
     private final MessageSource messages;
 
     private final Map<String, AuthProvider> authProviders;
     
     private static final Logger LOG = Logger.getLogger(LoginController.class.getName());
     
     @Autowired
     public LoginController(Map<String, AuthProvider> aProvs, UserAccountService uas, MessageSource messSrc) {
         this.authProviders = aProvs;
         this.userService = uas;
         this.messages = messSrc;
     }
     
     @RequestMapping(value = "/authorize", method = RequestMethod.GET)
 	public String showLogin(HttpServletRequest request, HttpServletResponse response,
             @RequestParam(value="state", required=false) String state,
             @RequestParam(value="relparty", required=false) String relParty,
             @RequestParam(value="auth_type", required=false) String type, Model model)
     {
         AuthProvider aProv = null;
         if (type != null) aProv = authProviders.get(type);
         if (aProv != null) {
             User user = null;
             try {
                 JSONObject obj = new JSONObject();
                 obj.put("relParty", relParty);
                 user = aProv.authorize(request, response, obj.toString());
             } catch (Exception ex) {
                 LOG.log(Level.SEVERE, null, ex);
             }
             if (user != null) {
                 User registeredUser = userService.getUserByAccountId(user.getUsername(), type);
                 // create user account & user
                 if (registeredUser == null) {
                     if (user.getEmail() != null) {
                         registeredUser = userService.getUserByEmail(user.getEmail());
                         if (registeredUser != null) {
                             Message msg = new Message(false, messages.getMessage("register.email.duplicate", null, null));
                             model.addAttribute("message", msg);
                             return "login";
                         }
                     }
                     UserAccount uac = userService.registerNewAccount(user, type);
                     registeredUser = uac.getUser();
                 }
                 relParty = null;
                 try {
                     JSONObject obj = new JSONObject(state);
                     relParty = obj.optString("relParty");
                } catch (JSONException ex) {}
                 authorizeSAML(request, response, registeredUser.getUsername(), relParty);
                 return null;
             }
         }
         model.addAttribute("relParty", request.getAttribute("relyingParty"));
         return "login";
     }
 
     @RequestMapping(value = "/authorize", method = RequestMethod.POST)
 	public String login(HttpServletRequest request, HttpServletResponse response,
             @RequestParam(value="login", required=false) String login,
             @RequestParam(value="password", required=false) String password,
             @RequestParam(value="relparty", required=false) String relParty,
             Model model)
     {
         User user = userService.getUserByUsername(login);
         if (user != null) {
             try {
                 String pwd = new String(PwdUtils.cryptPassword(password.toCharArray()));
                 if (pwd.equals(user.getPassword())) {
                     authorizeSAML(request, response, login, relParty);
                     return null;
                 }
             } catch (NoSuchAlgorithmException ex) {
                 LOG.log(Level.SEVERE, null, ex);
             }
         }
         Message msg = new Message(false, messages.getMessage("login.error.invalid", null, null));
         model.addAttribute("message", msg);
         return "login";
     }
     
     private void authorizeSAML(HttpServletRequest request, HttpServletResponse response,
                                 String login, String relParty) {
         Principal principal = new UsernamePrincipal(login);
         Subject subj = new Subject();
         subj.getPrincipals().add(principal);
         request.setAttribute("forceAuthn", false);
         request.setAttribute("isPassive", true);
         request.setAttribute(LoginHandler.PRINCIPAL_KEY, principal);
         request.setAttribute(LoginHandler.PRINCIPAL_NAME_KEY, login);
         request.setAttribute(LoginHandler.SUBJECT_KEY, subj);
         request.setAttribute("authnMethod", "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified"); 
         request.setAttribute("relyingParty", relParty); 
         AuthenticationEngine.returnToAuthenticationEngine(request, response);
     }
 }
