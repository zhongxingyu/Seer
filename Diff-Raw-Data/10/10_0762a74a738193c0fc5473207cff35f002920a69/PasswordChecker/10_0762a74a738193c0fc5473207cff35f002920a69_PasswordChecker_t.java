 package stringdemo;
 
 public class PasswordChecker {
 
     private static boolean validate(String userPassword) {
        //String salt = "xyzzy";
         String password = "xyzzy";
         return password == userPassword;
     }
 
 
     public static void main(String[] args) {
        assert args.length == 2 : "Enter Username and Password";
         String username = args[0];
        String userPassword = args[1];
         
        if (validate(userPassword)) {
             System.out.println("Welcome " + username + "!");
         } else {
             System.out.println("ALERT ALERT ALERT!");
             System.out.println("Wrong password!");
         }
     }
 }
