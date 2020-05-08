 import java.io.IOException;
 
 public class Main {
     public static void main(String args[]) {
 
         Input input = new Input();
         LoginScreen login = new LoginScreen();
         try {
            IRC irc = new IRC();
         } catch (IOException e) {
         }
     }
 }
