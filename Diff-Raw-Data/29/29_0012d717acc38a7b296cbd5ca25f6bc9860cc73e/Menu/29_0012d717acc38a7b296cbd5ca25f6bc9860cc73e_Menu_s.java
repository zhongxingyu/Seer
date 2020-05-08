 /**
  *  Some code to provide menu displaying for
  *    TimesTables program... :)
  * Author:     Jason Angus
  * File:       Menu.java
  * Last Edit:  09/10/2010
  */
 
 import java.util.*;
 
 
 public class Menu {
 
    private int menuSelection = 0;
    public static final int MENU_MAX = 3;
    public static final int MENU_MIN = 1;
    private boolean validChoice = false;
 
    public void displayMenu() {
       System.out.println("Times Tables Tester!");
       System.out.println("1. Single Times Table");
       System.out.println("2. All Times Tables");
       System.out.println("3. Exit");
       System.out.println("");
    } // close displayMenu method
 
    public boolean getMenuSelection() {
      System.out.println("Make a Selection:");
       Scanner in = new Scanner(System.in);
       int choice = in.nextInt();
       if (choice >= MENU_MIN && choice <= MENU_MAX) {
          this.menuSelection = choice;
          this.validChoice = true;
          return true;
      } else {
          System.out.println("Make a Valid Selection PinHead!");
          this.validChoice = false;
          return false;
       }
 
    } // close  getMenuSelection method
 
    public void goGoGO() {
       if ( this.validChoice ) {
          if (this.menuSelection == 1) {
             goChoice1();
          } else // close option 1
          if (this.menuSelection == 2) {
             goChoice2();
          } else // close option 1
          if (this.menuSelection == 3) {
             goChoice3();
          }
       } // close outer if
 

    } // close goGoGO method
 
    public void goChoice1() {
       // ask user for the table they want to test then do it!
       while (true) {
          System.out.println("You chose Single Times Tables! - menu #1");
          System.out.print("Enter table to attempt (1 - 12), or zero to quit: ");
          Scanner in = new Scanner(System.in);
         int choice = in.nextInt();
          if (choice == 0)
             break;
          else if (choice >= TimesTable.TABLE_MIN &&
                   choice <= TimesTable.TABLE_MAX) {
             TimesTable myTable = new TimesTable(choice);
             System.out.println("You chose " + choice + " times table");
             int tries = 0;
             for (int i=TimesTable.TABLE_MIN; i<=TimesTable.TABLE_MAX; i++) {
                int answer = -1;
                do {
                   myTable.displaySum(i);
                   // TODO: create more robust user input method
                   // current method breaks too easily if non-integer entered
                   answer = in.nextInt();
                   ++tries;
                } while (answer != myTable.getSum(i));
             } // end for
             System.out.printf("Great! You took %d goes!\n", tries);
          } else {
             System.out.println("Thats gotta be a table between 1 and 12 you Ninny!");
          } // end else
       } // end while


    } // close goChoice1 method
 
    public void goChoice2() {
       // throw random problems at the user until they've had enough
       System.out.println("You chose All Times Tables!! - menu #2");
    } // close goChoice2 method
 
    public void goChoice3() {
       System.out.println("You chose Exit!!! - menu #3");
       System.out.println("Goodbye then!");
       System.exit(0);
    } // close goChoice3 method
 } // close Menu class
 
