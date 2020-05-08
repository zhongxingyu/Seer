 
 package models.user;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 /**
  * Class to remember a session's user's history of IDs. This class is merely
  * a data class, and will not check whether a user is allowed to mimic another.
  * @author Jens N. Rammant
  */
 public class LoginState {
     private String sessionID;
     private Stack<String> translatorStack;
    
 
     public LoginState(String si) {
         sessionID=si;
        translatorStack = new Stack<>();
     }
     
     /*
      *Returns the ID at the top of the stack.
      *@return Currently active ID 
      */
     public String getID(){
     	if(!translatorStack.isEmpty()) return translatorStack.peek();
     	else return null;
     }
     
     /*
      * Returns whether a user is logged in or not.
      * @return Is user logged in
      */
     public boolean isLoggedIn(){
         return getID()!=null;
     }
     
     /*
      * Returns the full stack of IDs
      * @return All IDs
      */
     public ArrayList<String> getIDs(){
        return new ArrayList<>(translatorStack);
     } 
     
     /*
      * Removes the top ID from the stack. Will not do anything if the stack is empty.
      * @return Was the operation successful?
      */
     public boolean logout(){
         if(isLoggedIn()){
             translatorStack.pop();
             return true;
         }
         return false;
     }
     
     /*
      * Puts an ID on the stack IF the stack is empty.
      * @return Was the operation successful?
      */
     public boolean login(String ID){
         if(!isLoggedIn()){
             translatorStack.push(ID);
             return true;
         }
         return false;
     }
     
     /*
      * Puts an ID on the stack IF the stack is not empty.
      * @return Was the operation successful?
      */
     public boolean mimic(String ID){
         if(isLoggedIn()){
             translatorStack.push(ID);
             return true;
         }
         return false;
     }
     
     
 }
