 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package system_user;
 
 import Beans.Stateless_SessionBeansRemote;
 import Beans.Authentication_ControlRemote;
 import Beans.SavingsAcc_ControlRemote;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Scanner;
 import javax.ejb.EJB;
 
 /**
  *
  * @author JJ & Baron & Marcel
  */
 public class Main implements Serializable {
 
     @EJB
     private static Stateless_SessionBeansRemote initBean; 
 
     @EJB
     private static SavingsAcc_ControlRemote savingsControl;
     
     @EJB
     private static Authentication_ControlRemote authControl;
 
     private static boolean loggedIn = false;
     private static int opsPerformed = 0;
     
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         int menuSelection;
         //initBean.addEmployee("Baron", "Ch'ng", "bjm123");
         //authControl.login(1000, "bjm123");
         getLogin();
         
         //initBean.addCustomer("Tom", "Adrianus", java.sql.Date.valueOf("2010-01-31"), "32, Elizabeth Street Melbourne Victoria 3000");
 
         do {
             System.out.println("****************************");
             System.out.println("**     Banking System     **");
             System.out.println("****************************");
             System.out.println();
             
             System.out.println("****************************");
             System.out.println("**    Management Panel    **");
             System.out.println("****************************");
             System.out.println("1. Create a customer");
             System.out.println("2. View all customers");
             System.out.println("3. View operations performed");
             System.out.println("4. Log Out");
                         
             System.out.println("****************************");
             System.out.println("**   Savings Management   **");
             System.out.println("****************************");
             System.out.println("5. Open a Savings account");
             System.out.println("6. Deposit into Savings account");
             System.out.println("7. Withdraw from Savings account");
             System.out.println("8. View balance of Savings account");
             System.out.println("9. Get associated Savings account");
 
             menuSelection = getSelection();
 
             if (opsPerformed == 10) {
                 System.out.println("Maximum operations achieved. Please restart program.");
                 System.exit(0);
             }
             
             try {
                 authControl.check();
             }
             catch (Exception e) {
                 System.out.println("Session timeout. Please restart program.");
                 System.exit(0);
             }
             
             switch (menuSelection) {
                 case 1:
                     createCustomer();
                     break;
                 case 2:
                     viewAllCust();
                     break;
                 case 3:
                     System.out.println("Operations Performed: " + opsPerformed);
                     break;
                 case 4:
                     System.out.println("Thank you for using the system.");
                     System.out.println("Logging out...");
                     authControl.logout();
                     break;
                 case 5:
                     createSavings();
                     break;
                 case 6:
                     sav_deposit();
                     break;
                 case 7:
                     sav_withdraw();
                     break;
                 case 8:
                     sav_viewBal();
                     break;
                 case 9:
                     getSavingsAccNum();
                     break;
             }
             
             // every loop +1 operation performed
             opsPerformed++;
             
         } while (menuSelection != 4);
     }
 
     private static int getSelection() {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String input = "";
         int output = -1;
         boolean validInput = false;
 
         do {
             try {
                 input = br.readLine();
                 if (input.length() > 1) {
                     continue;
                 }
 
                 output = Integer.parseInt(input);
                 if (output < 0 || output > 9) {
                     continue;
                 }
                 validInput = true;
             } catch (IOException ioe) {
                 System.out.println(ioe.getMessage());
                 validInput = false;
             } catch (NumberFormatException nfe) {
                 System.out.println(nfe.getMessage());
                 validInput = false;
             }
         } while (validInput == false);
         return output;
     }
     
     private static boolean getLogin() {
         
         Scanner in = new Scanner(System.in);
         int loginID;
         String password;
         
         if (!loggedIn) {
             System.out.println("** Welcome to Banking System **");
             System.out.println("Please login below..");
         }
         
         while (!loggedIn) {
             try {
                 System.out.print("Emp ID: ");
                 loginID = Integer.parseInt(in.nextLine());
                 System.out.print("Password: (enter 'q' to quit) ");
                 password = in.nextLine();
                 
                 // decides to quit
                 if (password.equals("q")) {
                     System.out.println("System terminating.");
                     System.exit(0);
                 }
                 
                 
                 // try to login
                 if (authControl.login(loginID, password)) {
                     loggedIn = true;
                     break;
                 }
                 
             } catch (NumberFormatException nfe) {
                 System.out.println("Please enter only digits for ID.");
                 System.out.println(nfe.getMessage());
             } catch (Exception e) {
                 System.out.println("Session timeout. Please restart program.");
                 System.exit(0);
             }
         }
         
         return loggedIn;
     }
     
     private static void createCustomer() {
         
         Scanner in = new Scanner(System.in);
         String firstname, lastname, dob, street, suburb, state, address;
         int postcode;
         
         while (true) {
             try {
                 System.out.print("Last Name: ");
                 lastname = in.nextLine();
                 System.out.print("First Name: ");
                 firstname = in.nextLine();
                 System.out.print("Date of Birth: (e.g 1992-01-21) ");
                 dob = in.nextLine();
                 System.out.print("Address(street): ");
                 street = in.nextLine();
                 System.out.print("Suburb: ");
                 suburb = in.nextLine();
                 System.out.print("State: ");
                 state = in.nextLine();
                 System.out.print("Postcode: ");
                 postcode = Integer.parseInt(in.nextLine());
                 
                 address = street+", "+suburb+", "+state+", "+postcode;
                         
                 initBean.addCustomer(firstname, lastname, 
                         java.sql.Date.valueOf(dob), 
                         address);
                 
                 break;
                 
             } catch (NumberFormatException nfe) {
                 System.out.println("Wrong format of input. Please check.");
             } catch (IllegalArgumentException iae) {
                 System.out.println("Illegal format of date.");
                 System.out.println(iae.getMessage());
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         System.out.println("A Customer account has been created.");
         
     }
     
     private static void createSavings() {
         Scanner in = new Scanner(System.in);
         int custAccNum;
         String balStr;
         double balance;
         boolean result = true;
         
         while (result) {
             try {
                 System.out.print("Customer ACC Num: ");
                 custAccNum = Integer.parseInt(in.nextLine());
                 System.out.print("Initial Balance: ");
                 balStr = in.nextLine();
                 
                 if (checkMoneyFormat(balStr)) {
                     balance = Double.parseDouble(balStr);
                     result = savingsControl.createSavings(custAccNum, balance);
                     break;
                 }
                 
                 System.out.println("Invalid format in the input.");
                 
             } catch (NumberFormatException nfe) {
                 System.out.println("Please enter only digits.");
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         if (result)
             System.out.println("A Savings account has been created.");
         else
             System.out.println("Savings account creation failure.");
     }
     
     private static void sav_deposit() {
         Scanner in = new Scanner(System.in);
         int custAccNum;
         double amount;
         boolean result = false;
         String balStr;
         
         while (true) {
             try {
                 System.out.print("Savings ACC Num: ");
                 custAccNum = Integer.parseInt(in.nextLine());
                 System.out.print("Amount to deposit: ");
                 balStr = in.nextLine();
                 
                 if (checkMoneyFormat(balStr)) {
                     amount = Double.parseDouble(balStr);
                     result = savingsControl.deposit(custAccNum, amount);
                     break;
                 }
                 
                 System.out.println("Invalid format in the input.");
                 
             } catch (NumberFormatException nfe) {
                 System.out.println("Please enter only digits for ID.");
                 System.out.println(nfe.getMessage());
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         if (result) {
             System.out.println("You have successfully deposited "
                     + "$"+amount+" into ACC Num:" +custAccNum);
         }
         else {
             System.out.println("Attempt to deposit $"+amount+" into ACC Num:"
                     +custAccNum+" has failed.");
         }
     }
     
     private static void sav_withdraw() {
         Scanner in = new Scanner(System.in);
         int custAccNum;
         String balStr;
         double amount;
         boolean result = false;
         
         while (true) {
             try {
                 System.out.print("Savings ACC Num: ");
                 custAccNum = Integer.parseInt(in.nextLine());
                 System.out.print("Amount to withdraw: ");
                 balStr = in.nextLine();
                 
                 if (checkMoneyFormat(balStr)) {
                     amount = Double.parseDouble(balStr);
                     result = savingsControl.withdraw(custAccNum, amount);
                     break;
                 }
                 
                 System.out.println("Invalid format in the input.");
                 
             } catch (NumberFormatException nfe) {
                 System.out.println("Please enter only digits for ID.");
                 System.out.println(nfe.getMessage());
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         if (result) {
             System.out.println("You have successfully withdraw "
                     + "$"+amount+" into ACC Num:" +custAccNum);
         }
         else {
             System.out.println("Attempt to withdraw $"+amount+" from ACC Num:"
                     +custAccNum+" has failed.");
         }
     }
     
     private static void sav_viewBal() {
         Scanner in = new Scanner(System.in);
         int custAccNum;
         String result = "Cannot find an account";
         
         while (true) {
             try {
                 System.out.print("Savings ACC Num: ");
                 custAccNum = Integer.parseInt(in.nextLine());
                 
                 result = savingsControl.viewBal(custAccNum);
                 
                 break;
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         System.out.println(result);
     }
     
     private static void viewAllCust() {
         ArrayList<String> list = initBean.viewAllCustomer();
         
         for(String str : list) {
             System.out.println(str);
         }
     }
     
     private static void getSavingsAccNum() {
         
         Scanner in = new Scanner(System.in);
         int c_id;
         String result = "";
         
         while (true) {
             try {
                 System.out.print("Customer ACC Num: ");
                 c_id = Integer.parseInt(in.nextLine());
                 
                 result = savingsControl.getSavingsAcc(c_id);
                 
                 break;
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
             
         }
         
         System.out.println(result);
         
     }
     
     private static boolean checkMoneyFormat(String amount) {
        String pattern = "[0-9]{0,10}\\.{0,1}[0-9]{0,2}";
         
         if (!amount.matches(pattern))
             return false;
         
         return true;
     }
 }
