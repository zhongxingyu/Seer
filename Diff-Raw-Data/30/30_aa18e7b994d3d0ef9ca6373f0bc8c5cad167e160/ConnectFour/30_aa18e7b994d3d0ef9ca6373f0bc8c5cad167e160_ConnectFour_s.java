 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectFour;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Ryan
  */
 public class ConnectFour {
 
     private final static String WELCOME =
        "\n\tHello and welcome to Connect Four! The objective of this game is \n\t"
        + "to drop your playing pieces down the different slots to place them on the board \n\t"
        + "while trying to outwit your opponent in the attempt to get four pieces lined in a row. \n\t"
        + "You can do this either horizontaly, verticaly, or diagonly. As long as you've connected four \n\t"
        +  "YOU WIN!\n\t";
 
     static void getInput() {
         
        
     }
         
   
 
 public ConnectFour()  {
 
 }
 
 public static void main(String[] args) {
         ConnectFour ConnectFour = new ConnectFour();
         ConnectFour.display();
         MainMenuView mainMenu = new MainMenuView();
 
         mainMenu.getInput();   
         
     }
 
 
 private void display () {
     System.out.println(ConnectFour.WELCOME);
 
        }
 }
 
