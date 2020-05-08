 package timeSheet;
 
 import timeSheet.database.entity.Employee;
 import timeSheet.database.manager.EmployeeManager;
 import timeSheet.database.manager.SettingsManager;
 import timeSheet.util.TimeSheetException;
 import timeSheet.util.ldap.LDAPAuthenticate;
 
 /**
  * User: John Lawrence
  * Date: 1/20/11
  * Time: 5:35 AM
  */
 public class Login {
     private Employee employee;
     private String password;
     private LDAPAuthenticate ldap;
     private String failureMessage;
 
     public Login(String name, String password) {
         this.password = password;
         EmployeeManager manager = new EmployeeManager();
         employee = manager.getEmployee(name);
         SettingsManager settings = new SettingsManager();
         ldap = new LDAPAuthenticate(settings.getLDAPServer(), settings.getLDAPDomain());
     }
 
     public boolean checkDatabaseLogin() {
        return employee != null && employee.getPassword().equals(password);
     }
 
     public boolean checkLDAPLogin(String password) {
         try {
             ldap.login(employee.getUserName(), password);
             return true;
         } catch (TimeSheetException e) {
             failureMessage = e.getMessage();
             return false;
         }
     }
 
     public Employee getEmployee() {
         return employee;
     }
 
     public String getFailureMessage() {
         return failureMessage;
     }
 }
