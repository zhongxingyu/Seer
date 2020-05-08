 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: twer
  * Date: 9/13/12
  * Time: 11:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Context {
    private static StartPage startPage;
    public Context()
    {
        startPage = new StartPage();
    }
 
     public static void main(String[] args){
         startPage.showWelcomeMessage();
         startPage.showMenuList();
         System.out.println("Select a number to the enum");
         Scanner in = new Scanner(System.in);
         int input;
         input = in.nextInt();
         startPage.getUserInput(input);
 
 
     }
 }
