 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import BackEnd.UserSystem.IllegalCharacterException;
 import BackEnd.UserSystem.PasswordMismatchError;
 import BackEnd.ManagerSystem.PrivilegeInsufficientException;
 import EMS_Database.DoesNotExistException;
 
 /**
  *
  * @author Karina
  */
 public class TestGuiDriver 
 {
     public static void main(String[] args) throws PasswordMismatchError, IllegalCharacterException, PrivilegeInsufficientException, DoesNotExistException
     {
         // MainManager manager = MainManager.getInstance();
         Home home = new Home();
         home.setVisible(true);
     }
 }
