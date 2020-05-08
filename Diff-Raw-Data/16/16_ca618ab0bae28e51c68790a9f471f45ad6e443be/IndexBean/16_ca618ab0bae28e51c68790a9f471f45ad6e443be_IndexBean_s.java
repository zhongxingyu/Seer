 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package com.blazebit.security.web.bean.main;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Inject;
 import javax.security.auth.Subject;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jboss.logging.Logger;
 
 import com.blazebit.security.PermissionManager;
 import com.blazebit.security.auth.model.Credentials;
 import com.blazebit.security.impl.model.Company;
 import com.blazebit.security.impl.model.User;
 import com.blazebit.security.web.context.UserSession;
 import com.blazebit.security.web.service.api.CompanyService;
 import com.blazebit.security.web.service.api.UserService;
 
 /**
  * 
  * @author cuszk
  */
 @ManagedBean(name = "indexBean")
 @SessionScoped
 public class IndexBean implements Serializable {
 
     protected Logger log = Logger.getLogger(IndexBean.class);
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     @Inject
     private UserService userService;
     @Inject
     private UserSession userSession;
     @Inject
     private PermissionManager permissionManager;
 
     private List<User> users = new ArrayList<User>();
     private Company selectedCompany;
     List<Company> companies;
 
     @Inject
     private CompanyService companyService;
 
     @Inject
     @SessionScoped
     private LoginContext loginContext;
 
     @Inject
     private Credentials credentials;
 
     @PostConstruct
     public void init() {
         companies = companyService.findCompanies();
         if (!companies.isEmpty()) {
             setSelectedCompany(companyService.findCompanies().get(0));
             users = userService.findUsers(selectedCompany);
             users.add(0, userService.findUser("superAdmin", selectedCompany));
         }
     }
 
     public void logInAs(User user) {
         credentials.setLogin(String.valueOf(user.getId()));
         credentials.setPassword("");
 
         // programatic login
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 
         if (getLoggedInPrincipal(loginContext.getSubject()) == null) {
             try {
                 // request.login(credentials.getLogin(), credentials.getPassword());
                 loginContext.login();
                 if (loginContext.getSubject() != null) {
                     // userSession.setSubject(loginContext.getSubject());
                    // invoke authenticate, because it sets the request user principal and the roles
                     // request.authenticate((HttpServletResponse) externalContext.getResponse());
                     log.info("Logged in  " + getLoggedInPrincipal(loginContext.getSubject()));
 
                     userSession.setUser(user);
                     userSession.setAdmin(userService.findUser("superAdmin", selectedCompany));
                 } else {
                     throw new LoginException("Failed to get logged in subject");
                 }
             } catch (LoginException e) {
                 log.error("Login " + credentials.getLogin() + " failed", e);
                 // } catch (ServletException e1) {
                 // log.error("Login " + credentials.getLogin() + " failed", e1);
                 // } catch (IOException e2) {
                 // log.error("Login " + credentials.getLogin() + " failed", e2);
             }
         } else {
//            logout();
//            logInAs(user);
             // try {
             // // LoginModule's logout. Clears subject and roles.
             // loginContext.logout();
             // // logout clear the user principal
             // request.logout();
             // FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
             // logInAs(user);
             // } catch (LoginException e) {
             // log.error("Logout " + request.getUserPrincipal() + " failed", e);
             // } catch (ServletException e) {
             // log.error("Logout " + request.getUserPrincipal() + " failed", e);
             // }
         }
         // dont redirect
 
         // FacesContext.getCurrentInstance().getExternalContext().redirect("user/users.xhtml");
         // FacesContext.getCurrentInstance().setViewRoot(new UIViewRoot());
     }
 
     private String getLoggedInPrincipal(Subject subject) {
         if (subject != null) {
             Set<Principal> principals = subject.getPrincipals();
             if (!principals.isEmpty()) {
                 Principal principal = principals.iterator().next();
                 if (principal != null) {
                     return principal.getName();
                 }
             }
         }
         return null;
     }
 
     public void logout() {
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         try {
             // LoginModule's logout. Clears subject and roles.
             loginContext.logout();
             // logout clear the user principal
             request.logout();
         } catch (LoginException e) {
             log.error("Logout " + request.getUserPrincipal() + " failed", e);
         } catch (ServletException e) {
             log.error("Logout " + request.getUserPrincipal() + " failed", e);
         }
         HttpSession session = (HttpSession) externalContext.getSession(false);
         if (session != null) {
             session.invalidate();
         }
         userSession.setUser(null);
         userSession.setAdmin(null);
         FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "/index.xhtml");
     }
 
     public void beUser(User user) throws IOException {
         userSession.setSecondLoggedInUser(user);
         // FacesContext.getCurrentInstance().getExternalContext().redirect("user/users.xhtml");
         // FacesContext.getCurrentInstance().setViewRoot(new UIViewRoot());
     }
 
     public void reset(User user) {
         permissionManager.removeAllPermissions(user);
     }
 
     public List<User> getUsers() {
         return users;
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
 
     public List<Company> getCompanies() {
         return companies;
     }
 
     public Company getSelectedCompany() {
         return selectedCompany;
     }
 
     public void setSelectedCompany(Company selectedCompany) {
         this.selectedCompany = selectedCompany;
         userSession.setSelectedCompany(selectedCompany);
         users = userService.findUsers(selectedCompany);
         users.add(0, userService.findUser("superAdmin", selectedCompany));
     }
 
     public void changeCompany(ValueChangeEvent event) {
         Company newCompany = (Company) event.getNewValue();
         setSelectedCompany(newCompany);
     }
 
     public void saveCompanyConfiguration() {
         Company company = companyService.saveCompany(userSession.getSelectedCompany());
         userSession.setSelectedCompany(company);
         userSession.getUser().setCompany(company);
     }
 
 }
