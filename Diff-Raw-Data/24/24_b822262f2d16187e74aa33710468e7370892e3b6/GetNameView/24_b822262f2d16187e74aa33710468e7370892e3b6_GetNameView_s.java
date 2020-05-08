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
         
         
         public String getInput(Player player) {
             String playersName = null;
             
             
            boolean valid= false;
             
             while (!valid){
                    System.out.println("\n\t= player.getName()"
                     + ",Enter Player Name.");
                
              playerName = in.nextLine();           
                     
             if(playerName.length() =< 20);     
                    continue;   
                 
                  
             String regExpressionPattern = [a-z A-Z];
             if (!playerName[0-20].matches(regExpressionPattern)) {
                 new ConnectFourError().displayError(
                         "You must enter only letters; either upper or lower case."
                         + "No symbols or numbers. Try again.");
                continue;
              }
             System.out.println(playerName);
             
            
                             
     }
             
}
            
   
         
         
 
