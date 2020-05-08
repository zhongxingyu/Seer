 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CluelessCommands;
 
import Clueless.Character;

 /**
  *
  * @author davis_gigogne
  */
 public class ChangeCharacter extends Command {
    public Long gameId;
   public Character identity;
    
    public ChangeCharacter(){
       super(ChangeCharacter.class.getSimpleName());
    }
 }
