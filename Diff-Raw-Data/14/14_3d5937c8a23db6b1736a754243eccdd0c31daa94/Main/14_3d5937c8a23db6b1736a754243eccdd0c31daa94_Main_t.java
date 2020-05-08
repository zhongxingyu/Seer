 import java.io.IOException;
 
 public class Main {

    public static IRC irc;

     public static void main(String args[]) {
 
         Input input = new Input();
         LoginScreen login = new LoginScreen();
         try {
            irc = new IRC();
         } catch (IOException e) {
         }
     }
 }
