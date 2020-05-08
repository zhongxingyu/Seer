 package h2db;
 
 import java.sql.SQLException;
 import java.util.Scanner;
 
 /**
  * Hello world!
  */
 public class GuestBook {
     public static void main(String[] args) throws SQLException, ClassNotFoundException {
 
         GuestBookControllerImpl guestBookController;
         guestBookController = new GuestBookControllerImpl("guestbook", "SYSDBA", "MASTERKEY");
 
         for (Record rec : guestBookController.getRecords()) {
             rec.print();
         }
        System.out.println("Enter your message with DONE at end");
         Scanner mScanner = new Scanner(System.in);
         String record = "";
             try {
                 record += mScanner.nextLine();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         guestBookController.addRecord(record);
         guestBookController.close();
 
     }
 }
