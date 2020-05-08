 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editDescription.
  */
 package user;
 
 import etoile.javaapi.question.Question;
 import etoile.javapi.professor.*;
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import menu.MenuBean;
 import org.primefaces.component.commandbutton.CommandButton;
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.submenu.Submenu;
 import sha1.sha1;
 import test.testManager;
 
 @ManagedBean(name = "userManager")
 @SessionScoped
 public class userManager implements Serializable {
 
     @ManagedProperty(value = "#{sha1}")
     private sha1 sha1;
     private String username;
     private String password;
     private ServiceManager manager;
     private MenuBean menu;
     private Professor current_user;
     public Discipline selectedDiscipline;
     private String moduleName;
     private testManager testManager;
     private String removeModuleSelection;
     public Test selectedTest;
     private Module selectedModule;
     private LinkedList<Result> testResults;
     private String editDescription;
     private String addNews;
     private String addNewsTitle;
 
     public String getAddNewsTitle() {
         return addNewsTitle;
     }
 
     public void setAddNewsTitle(String addNewsTitle) {
         this.addNewsTitle = addNewsTitle;
     }
 
     public String getAddNews() {
         return addNews;
     }
 
     public void setAddNews(String addNews) {
         this.addNews = addNews;
     }
 
     public String getEditDescription() {
         return editDescription;
     }
 
     public void setEditDescription(String editDescription) {
         this.editDescription = editDescription;
     }
 
 
     public Professor getCurrent_user() {
         return current_user;
     }
 
     public void setCurrent_user(Professor current_user) {
         this.current_user = current_user;
     }
 
     public String getRemoveModuleSelection() {
         return removeModuleSelection;
     }
 
     public void setRemoveModuleSelection(String removeModuleSelection) {
         this.removeModuleSelection = removeModuleSelection;
     }
 
     public LinkedList<Result> getTestResults() {
         return testResults;
     }
 
     public void setTestResults(LinkedList<Result> testResults) {
         this.testResults = testResults;
     }
 
     public Test getSelectedTest() {
         return selectedTest;
     }
 
     public void setSelectedTest(Test selectedTest) {
         this.selectedTest = selectedTest;
     }
 
     public Module getSelectedModule() {
         return selectedModule;
     }
 
     public void setSelectedModule(Module selectedModule) {
         this.selectedModule = selectedModule;
     }
 
     public testManager getTestManager() {
         return testManager;
     }
 
     public void setTestManager(testManager testManager) {
         this.testManager = testManager;
     }
 
     public String getModuleName() {
         return moduleName;
     }
 
     public void setModuleName(String moduleName) {
         this.moduleName = moduleName;
     }
 
     public Discipline getSelectedDiscipline() {
         return selectedDiscipline;
     }
 
     public void setSelectedDiscipline(Discipline selectedDiscipline) {
         this.selectedDiscipline = selectedDiscipline;
     }
 
     public MenuBean getMenu() {
         this.menu = new MenuBean(current_user.getDisciplines());
         return menu;
     }
 
     public void setMenu(MenuBean menu) {
         this.menu = menu;
     }
 
     public void setSha1(sha1 sha1) {
         this.sha1 = sha1;
     }
 
     public sha1 getSha1() {
         return sha1;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void idleListener() {
         System.out.println("idle Listener");
         FacesContext ctx = FacesContext.getCurrentInstance();
         killHttpSession(ctx);
         doRedirectToLoggedOutPage(ctx);
     }
 
     public void activeListener() {
         System.out.println("active listener");
         FacesContext ctx = FacesContext.getCurrentInstance();
         killHttpSession(ctx);
         doRedirectToLoggedOutPage(ctx);
     }
 
     public String checkValidUser() {
         System.out.println("DEBUG: USER: " + username + " TRYING TO LOGIN");
         try {
             manager = new ServiceManager();
 
             if (manager.setAuthentication(username, sha1.parseSHA1Password(password))) {
 
                 current_user = manager.getCurrentProfessor();
                 manager.userService().updateDisciplines(current_user.getId());
                 manager.userService().getNews();
 
                 this.menu = new MenuBean(current_user.getDisciplines());
 
                 System.out.println("DEBUG: USER: " + username + " SUCCESS");
                 return "success";
             }
 
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Wrong User or Password"));
         System.out.println("DEBUG: USER: " + username + " FAIL");
         return "fail";
 
     }
 
     public String logOff() {
         //TODO: Propper logout
         try {
             System.out.println("DEBUG: Redirecting to Logoff");
             manager.closeConnection();
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return "logoff";
 
     }
 
     public void redirectAddTest(ActionEvent event) {
         Object obj = event.getSource();
         MenuItem aux_info = (MenuItem) obj;
         Submenu aux_discipline = (Submenu) aux_info.getParent();
         selectedDiscipline = manager.userService().getDiscipline(aux_discipline.getLabel());
         System.out.println("DEBUG: SELECTED DISCIPLINE: " + selectedDiscipline.name + " ID: " + selectedDiscipline.getId());
         for (Module m : selectedDiscipline.modules) {
             System.out.println("Module" + m.name);
         }
         testManager = new testManager(manager);
 
     }
 
     public String redirectAddTest() {
         System.out.println("DEBUG: Redirecting to addTest");
         return "addTest";
     }
 
     public void redirectAddModule(ActionEvent event) {
         Object obj = event.getSource();
         MenuItem aux_info = (MenuItem) obj;
         Submenu aux_discipline = (Submenu) aux_info.getParent();
         selectedDiscipline = manager.userService().getDiscipline(aux_discipline.getLabel());
         System.out.println("DEBUG: SELECTED DISCIPLINE: " + selectedDiscipline.name + " ID: " + selectedDiscipline.getId());
 
     }
 
     public String redirectAddModule() {
         System.out.println("DEBUG: Redirecting to addModule");
         return "addModule";
     }
 
     public void redirectEditContents(ActionEvent event) {
         Object obj = event.getSource();
         MenuItem aux_info = (MenuItem) obj;
         Submenu aux_discipline = (Submenu) aux_info.getParent();
         selectedDiscipline = manager.userService().getDiscipline(aux_discipline.getLabel());
         System.out.println("DEBUG: SELECTED DISCIPLINE: " + selectedDiscipline.name + " ID: " + selectedDiscipline.getId());
 
     }
 
     public String redirectEditContents() {
         System.out.println("DEBUG: Redirecting to Edit Contents");
         return "editContents";
     }
 
     public int addModule() {
         for (Module m : selectedDiscipline.modules) {
             if (m.name.equals(moduleName)) {
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fail", "Module already exists"));
                 return 0;
             }
         }
 
         try {
             System.out.println("Adding Module :" + moduleName);
             manager.userService().addModule(moduleName, selectedDiscipline);
 
             //MARTELO
 
 
         } catch (SQLException ex) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fail", "Adding Module"));
 
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Module Added"));
         return 1;
     }
 
     public String redirectModule() {
         System.out.println("DEBUG: Redirecting to Module");
         return "module";
 
     }
 
     public void redirectModule(ActionEvent event) {
         try {
             Object obj = event.getSource();
             MenuItem aux_info = (MenuItem) obj;
             Submenu aux_discipline = (Submenu) aux_info.getParent();
 
             selectedDiscipline = manager.userService().getDiscipline(aux_discipline.getLabel());
             selectedModule = manager.userService().getModule(aux_info.getValue().toString());
             manager.userService().updateTests(selectedModule);
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("DEBUG: SELECTED DISCIPLINE: " + selectedDiscipline.name + " ID: " + selectedDiscipline.getId());
         System.out.println("DEBUG: SELECTED MODULE: " + selectedModule.name + " ID: " + selectedModule.getId());
 
     }
 
     public void checkResults(ActionEvent actionEvent) {
         try {
             System.out.println("DEBUG: Check Results");
             Object obj = actionEvent.getSource();
             CommandButton cb = (CommandButton) obj;
 
             for (Test t : selectedModule.getTests()) {
                 if (t.getId() == Integer.parseInt(cb.getLabel())) {
                     try {
                         this.selectedTest = t;
                         selectedTest.setQuestions(new LinkedList<Question>());
                         manager.userService().updateQuestions(selectedTest);
                     } catch (SQLException ex) {
                         Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
            testResults = manager.userService().getResults(selectedTest.getId());
             System.out.println("DEBUG: SELECTED TEST: " + selectedTest.name + " ID: " + selectedTest.getId());
             //        System.out.println("DEBUG: SELECTED TEST AUTHOR: " + selectedTest.author);
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
     public String redirectResults() {
         System.out.println("DEBUG: Redirecting to results");
         return "results";
     }
 
     public void removeModule() {
         System.out.println("Removing Selected Module");
         for (Module m : selectedDiscipline.getModules()) {
             if (m.name.equals(removeModuleSelection)) {
                 try {
                     manager.userService().removeModule(selectedDiscipline, m.getId());
                 } catch (SQLException ex) {
                     FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fail", "Fail to remove module"));
                 }
             }
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Module Removed"));
     }
 
     public void removeTest(ActionEvent actionEvent) {
         System.out.println("Removing Selected Test");
 
         System.out.println("DEBUG: Check Results");
         Object obj = actionEvent.getSource();
         CommandButton cb = (CommandButton) obj;
 
         for (Test t : selectedModule.getTests()) {
             if (t.getId() == Integer.parseInt(cb.getLabel())) {
                 try {
                     this.selectedTest = t;
                     manager.userService().removeTest(selectedModule, t.getId());
                 } catch (SQLException ex) {
                     FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fail", "Test not removed"));
                 }
 
             }
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Test Removed"));
     }
 
     public void saveDescription() {
         try {
             System.out.println("DEBUG Edit Contents for Discipline: " + selectedDiscipline.name);
             System.out.println("DEBUG Edit Contens new Content:" +  selectedDiscipline.description);
             
             manager.userService().changeDisciplineDescription(selectedDiscipline, selectedDiscipline.description);
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Description Changed"));
         
     }
 
     public void submitNews() {
         try {
             System.out.println("DEBUG Adding News Title:" + addNewsTitle);
             System.out.println("DEBUG Adding News Content:" + addNews);
 
             manager.userService().insertNews(addNewsTitle, addNews, "");
 
 
         } catch (SQLException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "News Added"));
         addNews = "";
         addNewsTitle = "";
     }
 
     private void doRedirectToLoggedOutPage(FacesContext ctx) {
         try {
             ctx.getExternalContext().redirect("index.xhtml");
         } catch (IOException ex) {
             Logger.getLogger(userManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void killHttpSession(FacesContext ctx) {
         HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
         HttpSession session = request.getSession(false);
         session.invalidate();
 
     }
 }
