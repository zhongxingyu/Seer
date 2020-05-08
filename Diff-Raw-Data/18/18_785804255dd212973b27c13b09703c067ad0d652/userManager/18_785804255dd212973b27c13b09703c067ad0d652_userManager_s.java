 package users;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import db.DBConnect;
 import db.SQLInstruct;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.submenu.Submenu;
 import sha1.sha1;
 import tables.Discipline;
 import tables.Module;
 import tables.Test;
 
 /**
  *
  * @author lopesdasilva
  */
 @ManagedBean(name = "userM")
 @SessionScoped
 public class userManager {
 
     String loginname;
     String password;
     boolean loggedIn = false;
    
     
     User current;
     
     String disciplineSelected;
     String moduleSelected;
 
     
     Module moduleSelectedList;
 
     public Module getModuleSelectedList() {
         return moduleSelectedList;
     }
 
     public String getDisciplineSelected() {
         return disciplineSelected;
     }
 
     public String getModuleSelected() {
         return moduleSelected;
     }
 
     /** Creates a new instance of loginController */
     public userManager() {
     }
 
     public User getCurrent() {
         return current;
     }
 
     public boolean getLoggedIn() {
         return loggedIn;
     }
 
     public String getLoginname() {
         return loginname;
     }
 
     public void setLoginname(String loginname) {
         this.loginname = loginname;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String test() {
         System.out.println("yahoo");
         return "success";
     }
 
     public String CheckValidUser() {
       
         try {
 
 
 
             DBConnect db = new DBConnect(SQLInstruct.dbAdress, SQLInstruct.dbUsername, SQLInstruct.dbPassword);
             db.loadDriver();
             String sqlStatement = SQLInstruct.login(loginname, new sha1().parseSHA1Password(password));
             ResultSet rSet = db.queryDB(sqlStatement);
 
             if (rSet.next()) {
 
                 this.loggedIn = true;
                 current = new User(loginname);
                 System.out.println("User: " + loginname + " has logged On.");
                 return "success";
 
             }
 
 
         } catch (NoSuchAlgorithmException ex) {
             System.out.println("NoSuchAlgorithmException");
         } catch (InstantiationException ex) {
             System.out.println("InstantiationException");
         } catch (IllegalAccessException ex) {
             System.out.println("IllegalAccessException");
         } catch (ClassNotFoundException ex) {
             System.out.println("ClassNotFoundException");
         } catch (SQLException ex) {
 
             System.out.println(ex.getMessage());
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Wrong User or Password"));
         return "fail";
     }
 
     public String yahoo() {
         
   
 
         
 
         return "success";
     }
 
     public String moduleSelection(ActionEvent event) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
         Object obj = event.getSource();
         MenuItem aux_module = (MenuItem) obj;
         Submenu aux_discipline = (Submenu) aux_module.getParent();
 
         moduleSelected = aux_module.getValue() + "";
         disciplineSelected = aux_discipline.getLabel();
 


         DBConnect db = new DBConnect(SQLInstruct.dbAdress, SQLInstruct.dbUsername, SQLInstruct.dbPassword);
         db.loadDriver();
 
         //Meter as variaveis que o rui ainda vai arranjar.
         String sqlStatement = SQLInstruct.tests(disciplineSelected, moduleSelected);
         ResultSet rSet = db.queryDB(sqlStatement);
 
        while (rSet.next()) {
 
             for (Discipline d : current.disciplines) {
                if (d.getName().equals(disciplineSelected)) {//Sybstituir Math
                    //fazer while do rSet(Fazer rSet)
                     d.addTest(moduleSelected, rSet.getString(1));//Substituir SumModule
                 }

             }
 
         }
 
         moduleSelectedList = current.existe(disciplineSelected).existe(moduleSelected);
 
 
         return "success";
     }
 }
