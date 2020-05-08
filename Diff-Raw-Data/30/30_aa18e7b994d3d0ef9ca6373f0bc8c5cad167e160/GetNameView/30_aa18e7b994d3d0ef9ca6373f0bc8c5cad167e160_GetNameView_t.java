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
 public class GetNameView {
     
     private Player player;
     private boolean playerName;
         
         
         public GetNameView(Player player) {
             this.player = player;
         }
         
         
         public String getInput() {
             String playerName = null;
             
             boolean valid= false;
             
             while (!valid){
                 System.out.println("\n\t= player.getName()"
                     + ",Enter Player Name.");
                
              
                 Scanner inFile = ConnectFour.getInputFile();
                 
                 
                if (playerName.length() > 20) {     
                       new ConnectFourError().displayError(
                            "Name must be less than 20 characters."
                            + "Enter a shorter name please.");
                    continue;
                }
 
                String regExpressionPattern = "a-z A-Z";
                if (!playerName.matches(regExpressionPattern)) {
                    new ConnectFourError().displayError(
                            "You must enter only letters; either upper or lower case."
                            + "No symbols or numbers. Try again.");
                    continue;
                } 
                        
          }          
         
             
         return playerName;
     }
}
