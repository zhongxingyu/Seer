 
 package pt.uac.cafeteria.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Application core
  *
  * Central hub from where application scope objects come
  */
 public class Application {
 
     /** Default administrator username */
     private final String DEFAULT_ADMIN_NAME = "administrador";
 
     /** Default administrator password */
     private final String DEFAULT_ADMIN_PASSWORD = "12345678";
 
     /** Map with administrator accounts */
     private Map<String, Administrator> administrators = new HashMap<String, Administrator>();
 
     /** Map with student accounts */
     private Map<Integer, Account> accounts = new HashMap<Integer, Account>();
 
     /** Map with old students that no longer have an account */
     private Map<Integer, Student> oldStudents = new HashMap<Integer, Student>();
 
     /**
      * Constructor
      *
      * A default administrator account is created at instantiation
      */
     public Application() {
         Administrator default_admin = new Administrator(DEFAULT_ADMIN_NAME, DEFAULT_ADMIN_PASSWORD);
         administrators.put(DEFAULT_ADMIN_NAME, default_admin);
     }
 
     /**
      * Authenticates an Administrator
      *
      * @param username  Administrator username
      * @param password  Administrator password
      * @return  Administrator object, or null if invalid
      */
     public Administrator getAdministrator(String username, String password) {
         Administrator admin = administrators.get(username);
 
         if (admin != null && admin.isPasswordValid(password)) {
             return admin;
         }
 
         return null;
     }
 
     /**
      * Adds an account to the application
      *
      * @param account  Student account
      */
     public void addAccount(Account account) {
         accounts.put(new Integer(account.getNumber()), account);
     }
 
     /**
      * Gets a student account.
      *
      * @param accountNumber  Account process number
      * @return  Student account object
      */
    Account getAccount(int accountNumber) {
         return accounts.get(new Integer(accountNumber));
     }
 
     /**
      * Authenticates a Student using his account.
      *
      * Three failed attempts blocks the account.
      *
      * @param accountNumber  Account process number
      * @param pinCode  Account pin code
      * @return  Student account object, or null if does not authenticate
      */
     public Account getAccount(int accountNumber, int pinCode) {
         Account account = getAccount(accountNumber);
 
         if (account != null && account.authenticate(pinCode)) {
             return account;
         }
 
         return null;
     }
 
     /**
      * Deletes a student account
      *
      * Student gets moved to an historic of students (old students)
      *
      * @param accountNumber  Account or student process number
      */
     public void deleteAccount(int accountNumber) {
         Integer studentNumber = new Integer(accountNumber);
         Account account = accounts.get(studentNumber);
 
         if (account != null) {
             Student student = account.getStudent();
             accounts.remove(studentNumber);
             oldStudents.put(studentNumber, student);
         }
     }
 
     /**
      * Gets an old student from the historic
      *
      * @param studentNumber  Student process number
      * @return   Student object, or null if non-existent
      */
     public Student getOldStudent(int studentNumber) {
         return oldStudents.get(new Integer(studentNumber));
     }
 }
