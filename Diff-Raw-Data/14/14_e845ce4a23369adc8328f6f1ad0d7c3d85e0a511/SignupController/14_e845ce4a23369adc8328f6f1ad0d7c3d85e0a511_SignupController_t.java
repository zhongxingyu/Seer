 package org.imirsel.nema.webapp.controller;
 
 import org.imirsel.nema.Constants;
 import org.imirsel.nema.model.PreferenceValue;
 import org.imirsel.nema.model.User;
 import org.imirsel.nema.service.PreferenceValueManager;
 import org.imirsel.nema.service.RoleManager;
 import org.imirsel.nema.service.UserExistsException;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.imirsel.nema.webapp.util.RequestUtil;
 import org.springframework.mail.MailException;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.List;
 import java.util.Locale;
 import org.imirsel.nema.model.Profile;
 
 /**
  * Controller to signup new users. Prepopulate the form with the profile if it is from an invitation.
 * Modified for openid signup.  
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author gzhu1
  */
 public class SignupController extends BaseFormController {
 
     private RoleManager roleManager;
     private PreferenceValueManager preferenceValueManager;
 
     public PreferenceValueManager getPreferenceValueManager() {
         return preferenceValueManager;
     }
 
     public void setPreferenceValueManager(
             PreferenceValueManager preferenceValueManager) {
         this.preferenceValueManager = preferenceValueManager;
     }
 
     public void setRoleManager(RoleManager roleManager) {
         this.roleManager = roleManager;
     }
 
     public SignupController() {
         setCommandName("user");
         setCommandClass(User.class);
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         User user;
         user = (User) super.formBackingObject(request);
         Profile profile = (Profile) request.getSession().getAttribute("profile");
         if (profile != null) {
             user.setProfile(profile);
         }
         return user;
     }
 
    @Override
     public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
             Object command, BindException errors)
             throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("entering 'onSubmit' method...");
         }
 
 
         User user = (User) command;
         Locale locale = request.getLocale();
 
         user.setEnabled(true);
         user.getProfile().setOwner(user);
         // Set the default user role on this new user
         user.addRole(roleManager.getRole(Constants.USER_ROLE));
         log.debug("getting default preferences");
         List<PreferenceValue> list = preferenceValueManager.getDefaultPreferenceValues();
 
         if (log.isDebugEnabled()) {
             log.debug("Got: " + list.size() + " default preferences");
         }
 
         for (PreferenceValue pvalue : list) {
             if (log.isDebugEnabled()) {
                 log.debug("adding preference: " + pvalue.toString());
             }
 
             user.addPreference(pvalue);
         }
 
         try {
             this.getUserManager().saveUser(user);
         } catch (AccessDeniedException ade) {
             // thrown by UserSecurityAdvice configured in aop:advisor userManagerSecurity
             log.warn(ade.getMessage());
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return null;
         } catch (UserExistsException e) {
             errors.rejectValue("username", "errors.existing.user",
                     new Object[]{user.getUsername(), user.getEmail()}, "duplicate user");
 
             // redisplay the unencrypted passwords
             //user.setPassword(user.getConfirmPassword());
             return showForm(request, response, errors);
         }
 
         saveMessage(request, getText("user.registered", user.getUsername(), locale));
         request.getSession().setAttribute(Constants.REGISTERED, Boolean.TRUE);
 
         // log user in automatically
         UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities());
         auth.setDetails(user);
         SecurityContextHolder.getContext().setAuthentication(auth);
         //request.getSession().setAttribute("seesionUsername", user.getProfile().getEmail());
         // Send user an e-mail
         if (log.isDebugEnabled()) {
             log.debug("Sending user '" + user.getUsername() + "' an account information e-mail");
         }
 
         // Send an account information e-mail
         message.setSubject(getText("signup.email.subject", locale));
 
         try {
             sendUserMessage(user, getText("signup.email.message", locale), RequestUtil.getAppURL(request));
         } catch (MailException me) {
             saveError(request, me.getMostSpecificCause().getMessage());
             log.error(me,me);
         }catch(Exception ex){
             saveError(request,"Fail to send signup email. "+ex.getMessage());
             log.error(ex,ex);
         }
 
         return new ModelAndView(getSuccessView());
     }
 }
