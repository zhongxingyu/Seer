 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectFour;
 
 import java.util.Scanner;
 /**
  *
  * @author Noble
  */
 public class MainMenuView {
     
 
     private static final String[][] menuItems = {
         {"1", "One player game"},
         {"2", "Two player game"},
         {"H", "Help"},
         {"X", "Exit Connect Four"}
     }; 
     
     MainMenuControl mainMenuControl = new MainMenuControl(); 
         
     public MainMenuView() {
         
     }
     
     public void getInput() {
         
         String gameStatus = Game.PLAYING;
         do {
             this.display();
             
             // gets command entered by user
             String command = this.getCommand();
             switch (command) {
                 case "1":
                     this.mainMenuControl.startGame(1);
                     break;
                 case "2":
                     this.mainMenuControl.startGame(2);
                     break;
                 case "H":
                   this.mainMenuControl.displayHelpMenu();
                  // HelpMenuView helpMenu = ConnectFour.getHelpMenu();
                  // helpMenu.getInput();
                      break;
                 case "X":
                     break;
             }
         }  while (!gameStatus.equals("QUIT"));
          
         return;
 }
     
 public final String getCommand() {
 
        Scanner inFile = ConnectFour.getInput();
         String command;
         boolean valid = false;
         do{
             command = inFile.nextLine();
             command = command.trim().toUpperCase();
             valid = validCommand(command);
             if (!vaildCommand(command));
             if (!vaildCommand(command)) {
                 new ConnectFourError().displayError("Invalid command. Please enter a vaild command.");
                    continue;
                }
 
         } while (!valid);
         
         return command;
    }
 
 
  public final void display() {
         System.out.println("\n\t===============================================================");
         System.out.println("\tEnter the letter associated with one of the following commands:");
 
         for (int i = 0; i < MainMenuView.menuItems.length; i++) {
             System.out.println("\t   " + menuItems[i][0] + "\t" + menuItems[i][1]);
         }
         System.out.println("\t===============================================================\n");
     }
 
     private boolean validCommand(String command) {
         String[][] items = MainMenuView.menuItems;
 
         for (String[] item : MainMenuView.menuItems) {
             if (item[0].equals(command)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean vaildCommand(String command) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     private void Error(String invalid_command_Please_enter_a_vaild_comm) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
    
     
 }
 
