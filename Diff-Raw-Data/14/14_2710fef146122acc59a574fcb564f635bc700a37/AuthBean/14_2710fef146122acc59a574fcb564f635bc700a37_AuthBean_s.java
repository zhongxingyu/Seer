 package minijira.web;
 
 import ejb.database.model.Employee;
 import ejb.database.model.Project;
 import ejb.util.Log;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alexx
  * Date: 27.10.13
  * Time: 20:21
  * Email: alexey.gorovoy.work@gmail.com
  */
 @Named("auth")
 @SessionScoped
 public class AuthBean implements Serializable {
 
     @Inject
     DatabaseBean databaseBean;
 
     String message;
 
     String username;
 
     String password;
 
     private boolean admin;
 
     private boolean logged;
 
     Employee employee;
     List<Project> managedProjects = new LinkedList<Project>();
 
     public boolean managingProject(Project project) {
         for (Project p : managedProjects) {
             if (p.getId() == project.getId())
                 return true;
 
         }
         return false;
     }
 
     public  boolean hasProjects() {
         return ! (managedProjects == null || managedProjects.isEmpty());
     }
 
     public String login() {
 
         String bundlename = "minijira.locale.ResourceBundle";
         Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle(bundlename, locale);
 
         try {
             message="";
             HttpServletRequest request = (HttpServletRequest)  FacesContext.getCurrentInstance().getExternalContext().getRequest();
 
             Log.getLogger().info("Loggin in with : " + username + " pwd: " + password);
             request.login(username, password);
             if(request.isUserInRole("USER"))  {
                 logged = true;
                 employee = databaseBean.getDc().findEmployeeByEmail(username);
 
                 managedProjects = databaseBean.getDc().findProjectsByManagers(employee.getId());
 
                 Log.getLogger().info("Logged in as USER");
                 return "/index.xhtml";
             }
             if (request.isUserInRole("ADMINISTRATOR")) {
                 admin = true;
                 logged = true;
                 employee = databaseBean.getDc().findEmployeeByEmail(username);
                 Log.getLogger().info("Logged in as ADMINISTRATOR");
                 return "/index.xhtml";
             }
             if (!logged){
                 message = bundle.getString("wrongUsernameOrPassword");
                 Log.getLogger().info("Not logged in :(");
                 return "/login.xhtml";
             }
         } catch(Exception e) {
             Log.getLogger().info("Log in exception :( ");
             e.printStackTrace();
             message = bundle.getString("wrongUsernameOrPassword");
             return "/login.xhtml";
         }
         return "/index.xhtml";
     }
 
     public String logout() {
           ((HttpSession) FacesContext.getCurrentInstance().getExternalContext()
                 .getSession(true)).invalidate();
         return "/login.xhtml";
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public boolean isAdmin() {
         return admin;
     }
 
     public void setAdmin(boolean admin) {
         this.admin = admin;
     }
 
     public boolean isLogged() {
         return logged;
     }
 
     public void setLogged(boolean logged) {
         this.logged = logged;
     }
 
     public Employee getEmployee() {
         return employee;
     }
 
     public void setEmployee(Employee employee) {
         this.employee = employee;
     }
 
     public List<Project> getManagedProjects() {
         return managedProjects;
     }
 
     public void setManagedProjects(List<Project> managedProjects) {
         this.managedProjects = managedProjects;
     }
 }
