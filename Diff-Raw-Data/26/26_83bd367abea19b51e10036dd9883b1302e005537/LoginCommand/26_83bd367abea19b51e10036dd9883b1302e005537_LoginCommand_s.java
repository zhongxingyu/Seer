 package main;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 public class LoginCommand implements Command {
     public void excute() {
         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         clearLogin();
         System.out.println("Enter your library number");
         try {
             String libraryNumber = reader.readLine();
             if (validLibraryNumber(libraryNumber)) {
                 try {
                     System.out.println("Enter your Password: ");
                     String password = reader.readLine();
                     if (validPassword(password)) {
                         CheckNumberCommand.loggedIn = true;
                         CheckNumberCommand.savedLibraryNumber = libraryNumber;
                     } else {
                         System.out.println("Password Wrong!");
                     }
                 } catch (Exception e) {
                     System.out.println("Something is wrong!");
                 }
             }
         } catch (Exception e) {
             System.out.println("Something is wrong!");
         }
     }
 
     private static void clearLogin() {
         CheckNumberCommand.loggedIn = false;
         CheckNumberCommand.savedLibraryNumber = "";
     }
 
     private static boolean validLibraryNumber(String libraryNumber) {
         return libraryNumber.matches("\\d{3}-\\d{4}");
     }
 
     private static boolean validPassword(String password) {
        return "bhaisahab".equals(password);
     }
 }
