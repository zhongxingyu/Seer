 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package project;
 
 import project.gui.*;
 import project.model.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.*;
 import java.util.List;
 
 
 /**
  *
  * @author rich
  */
 
 
 public final class Controller {
 
     private Bank instance = Bank.getInstance();
     private User currentUser; //the User currently using the platform based on the window that is open. When a user interacts with a Teller, the teller window is the only window open and this would return the Teller.
     private List<AbstractUserWindow> windows = new ArrayList<AbstractUserWindow>(); //the Windows open and (therefore) those which this Controller is responsible for
 
     private List<AccountTab> tabs = new ArrayList<AccountTab>(); //the tabs open and (therefore) those which this Controller is responsible for
 
     public List<AbstractUserWindow> getWindows() {
         return windows;
     }
 
     public void setCurrentUser(User currentUser) {
         this.currentUser = currentUser;
     }
 
     public User getCurrentUser() {
         return currentUser;
     }
 
     public static void main(String[] args) throws InvalidInputException {
         setLookAndFeel();
         new Controller();
     }
 
     //static methods (called by initial thread)
     private static void setLookAndFeel() {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
     }
 
     private void initializeBank() throws InvalidInputException {
         if (Bank.getInstance().getUsers().isEmpty()) {
             Bank.getInstance().addUser("operations_manager", new User("Operations", "Manager", Calendar.getInstance(), 555555555, "omanager@bank.com"));
             Bank.getInstance().getUser("operations_manager").setRole(User.Role.OPERATIONS_MANAGER);
             Bank.getInstance().addUser("accountant", new User("Accountant", "Employee", Calendar.getInstance(), 123456789, "accountant@bank.com"));
             Bank.getInstance().getUser("accountant").setRole(User.Role.ACCOUNTANT);
             Bank.getInstance().addUser("auditor", new User("Auditor", "Employee", Calendar.getInstance(), 12345678, "auditor@bank.com"));
             Bank.getInstance().getUser("auditor").setRole(User.Role.AUDITOR);
             Bank.getInstance().addUser("account_manager", new User("Account", "Manager", Calendar.getInstance(), 222222222, "amanager@bank.com"));
             Bank.getInstance().getUser("account_manager").setRole(User.Role.ACCOUNT_MANAGER);
             Bank.getInstance().addUser("teller", new User("Teller", "Employee", Calendar.getInstance(), 777777777, "teller@bank.com"));
             Bank.getInstance().getUser("teller").setRole(User.Role.TELLER);
         }
         newLoginWindow();
     }
 
     //Opens up the first window and gives control to itself
     public Controller() throws InvalidInputException {
         initializeBank();
     }
 
     //non-static methods 
     
     //builds GUI
     public void newLoginWindow() {
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 LoginWindow login = new LoginWindow("Login", Controller.this);
                 login.setVisible(true);
             }
         });
     }
 
     /**
      * Opens the appropriate window based on 'user'.
      *
      * @param user sets currentUser
      */
     public void newAbstractUserWindow(User user) {
         currentUser = user;
         windows.clear();
 
         //create Account Holder Frame
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 AbstractUserWindow userWindow = null;
                 if (currentUser.getRole() == null) {
                     clearTabs();
                     setTabs((AccountHolderFrame) userWindow);
                     userWindow = new AccountHolderFrame(Controller.this);
                 } else {
                     switch (currentUser.getRole()) {
                         case TELLER:
                             userWindow = new TellerFrame(Controller.this);
                             break;
                         case ACCOUNTANT:
                             userWindow = new AccountantFrame(Controller.this);
                             break;
                         case OPERATIONS_MANAGER:
                             userWindow = new OperationsManagerFrame(Controller.this);
                             break;
                         case AUDITOR:
                             userWindow = new AuditorFrame(Controller.this);
                             break;
                         case ACCOUNT_MANAGER:
                             userWindow = new AccountManagerFrame(Controller.this);
                     }
                 }
                 userWindow.setVisible(true);
                 windows.add(userWindow);
             }
         });
     }
     private void setTabs(AccountHolderFrame parent) {
         for(Account acc : currentUser.getAccounts()){
             tabs.add(newAccountTab(acc, parent));
         }
     }
 
     public List<AccountTab> getTabs(AccountHolderFrame parent) {
         return tabs;
     }
 
     public void clearTabs() {
         tabs.clear();
     }
 
     /**
      * 
      * @param account
      * @param parent
      * @return 
      */
     public AccountTab newAccountTab(Account account, AbstractUserWindow parent) {
        AccountTab accTab = new AccountTab(this, account);
         accTab.setVisible(true); 
         return accTab;
     }
 
     //Interact with model
     public void withdraw(Account account, String amount) throws InvalidInputException, InsufficientFundsException {
         account.withdraw(new BigDecimal(amount));
         updateBankDisplay();
     }
 
     public void deposit(Account account, String amount) throws InvalidInputException {
         account.deposit(new BigDecimal(amount));
         updateBankDisplay();
     }
     
     public BigDecimal getInterestRate(Account account) {
         return account.getInterestRate().multiply(new BigDecimal("100"));
     }
     
     /**
      * 
      * @param firstName
      * @param lastName
      * @param birthdate
      * @param ssn
      * @param email
      * @return 
      */
     public User createNewUser(String firstName, String lastName, Calendar birthdate, int ssn, String email, String username) throws InvalidInputException {
         User user = new User(firstName, lastName, birthdate, ssn, email);
         Bank.getInstance().addUser(username, user);
         return user;
     }
     /**
      * 
      * @param firstName
      * @param lastName
      * @param birthdate
      * @param ssn
      * @param email
      * @param username 
      */
     public void createNewEmployee(String firstName, String lastName, Calendar birthdate, int ssn, String email, String username, String role) throws InvalidInputException {
         Bank.getInstance().addUser(username, new User(firstName, lastName, birthdate, ssn, email));
         Bank.getInstance().getUser(username).setRole(User.Role.valueOf(role));
     }
 
     public void addAccountToUser(User user, Account account) {
         user.addAccount(account);
     }
 
     public void closeAccount(Account account) {
         // TODO
         throw new UnsupportedOperationException();
     }
     
     public Bank getInstance() {
         this.instance = Bank.getInstance();
         return Bank.getInstance();
     }
     /**
      * Lays out all of the accounts of a certain User
      * @return 
      */
     public Object[][] updateAccountHolderTableView(){
         Set<Account> accounts = currentUser.getAccounts();
         Object[][] summaryTable = new Object[accounts.size()][3];
         int i = 0;
         for (Account account : accounts) {
             summaryTable[i][0] = account.getType();
             summaryTable[i][1] = account.getAccountNumber();
             summaryTable[i][2] = "$"+account.getBalance();
             i++;
         }
         return summaryTable;
     }
     /**
      * Lays out all of the Users and their Accounts
      * @return 
      */
     public Object[][] updateAccountManagerTableView(){
         Collection<User> users = instance.getUsers();
         int accounts = 0;
         for (User user : users) {
             for (Account account : user.getAccounts()) {
                 accounts++;
             }
         }
         Object[][] accountManagerTable = new Object[accounts][4];
         int i = 0;
         for (User user : users) {
             if(!user.isActiveCustomer()){
                 continue;
             }
                 accountManagerTable[i][0] = user;
                 StringBuilder formattedSsn = new StringBuilder(String.format("%09d", user.getSsn()));
                 formattedSsn.insert(3, "-").insert(6, "-");
                 accountManagerTable[i][1] = formattedSsn;
             for (Account account : user.getAccounts()) {
                 accountManagerTable[i][2] = account;
                 accountManagerTable[i][3] = String.format("$%.2f", account.getBalance());
                 i++;
             }
         }
         return accountManagerTable;
     }
 
     /**
      * Generates the table data used in the accountant view.
      *
      * @return the accountant table data
      */
     public Object[][] updateAccountantTableView() {
         Map<String, String> stats = Bank.getInstance().getStatistics();
         Object[][] data = new Object[stats.size()][2];
         Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();
         for (int i = 0; i < stats.size(); i++) {
             Map.Entry<String, String> entry = iterator.next();
             data[i][0] = entry.getKey();
             data[i][1] = entry.getValue();
         }
         return data;
     }
 
     public void handleException(Component parent, Exception x) {
         JOptionPane.showMessageDialog(parent, x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
     }
 
     public void shutDown() {
         try {
             Bank.saveInstance();
         } catch (IOException iox) {
             JOptionPane.showMessageDialog(null, "Could not serialize Bank using XStream!", iox.getClass().getName(), JOptionPane.WARNING_MESSAGE);
         }
     }
 
     public Object[][] updateAuditorTableView() {
         Collection<User> users = instance.getUsers();
         //finds the number of users
         int accounts = 0;
         for (User user : users) {
             accounts += user.getAccounts().size();
         }
         //lays out the Table contents
         Object[][] AuditorTable = new Object[accounts][3];
         int i = 0;
         for (User user : users) {
             if(!user.isActiveCustomer()){
                 continue;
             }
             AuditorTable[i][0] = user;
             for(Account account : user.getAccounts()){
                 AuditorTable[i][1] = account;
                 AuditorTable[i][2] = account.getBalance();
                         i++;
             }
         }
         return AuditorTable;
     }
     
     public void updateBankDisplay() {
         for (AbstractUserWindow w : windows) {
             if (w.getClass().getSimpleName().equals("AccountHolderFrame")) {
                 AccountHolderFrame accountHolderFrame = (AccountHolderFrame) w;
                 accountHolderFrame.setSummaryTableModel();
             }
         }
         for (AccountTab accTab : tabs) {
             accTab.update();
         }
     }
 }
