 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CluelessCommands;
 
 /**
  *
  * @author davis_gigogne
  */
 public class ChangeCharacter extends Command {
    public Long gameId;
   public String identity;
    
    public ChangeCharacter(){
       super(ChangeCharacter.class.getSimpleName());
    }
 }
