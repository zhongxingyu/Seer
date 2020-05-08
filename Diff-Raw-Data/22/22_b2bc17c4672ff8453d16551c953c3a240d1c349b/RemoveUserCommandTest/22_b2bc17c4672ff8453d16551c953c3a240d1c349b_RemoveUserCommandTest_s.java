 package test;
 
 import junit.framework.TestCase;
 import handler.DocumentController;
 import user.CTEUser;
<<<<<<< HEAD
 import user.UserNotFoundException;
=======
>>>>>>> 50182a8a48aa89adbb10d2877844c665d7e6fbaa
 import commands.*;  //AddUserCommand and RemoveUserCommand
 import java.awt.Color;
 import java.net.InetAddress;
 
 public class RemoveUserCommandTest extends TestCase{
   
   private DocumentController _docController;
   private CTEUser _firstUser;
   private CTEUser _secondUser;
   private CTEUser _thirdUser;
   private CTEUser _fourthUser;
   private CTEUser _fifthUser;
   private CTEUser _sixthUser;
   private AddUserCommand _addFirst;
   private AddUserCommand _addSecond;
   private AddUserCommand _addThird;
   private AddUserCommand _addFourth;
   private AddUserCommand _addFifth;
   private AddUserCommand _addSixth;
   private RemoveUserCommand _removeFirst;
   private RemoveUserCommand _removeSecond;
   private RemoveUserCommand _removeThird;
   private RemoveUserCommand _removeFourth;
   private RemoveUserCommand _removeFifth;
   private RemoveUserCommand _removeSixth;
<<<<<<< HEAD
   private RemoveUserCommand _removeNonExistent;
   
   @Override
=======
  
>>>>>>> 50182a8a48aa89adbb10d2877844c665d7e6fbaa
   protected void setUp ( ) {
     _docController = new DocumentController();
     try {
       _firstUser = new CTEUser("firstUser", InetAddress.getLocalHost(), Color.BLACK);
       _addFirst = new AddUserCommand(_firstUser);
       _removeFirst = new RemoveUserCommand(_firstUser);
       _secondUser = new CTEUser("secondUser", InetAddress.getLocalHost(), Color.BLACK);
       _addSecond = new AddUserCommand(_secondUser);
       _removeSecond = new RemoveUserCommand(_secondUser);
       _thirdUser = new CTEUser("thirdUser", InetAddress.getLocalHost(), Color.BLACK);
       _addThird = new AddUserCommand(_thirdUser);
       _removeThird = new RemoveUserCommand(_thirdUser);
       _fourthUser = new CTEUser("fourthUser", InetAddress.getLocalHost(), Color.BLACK);
       _addFourth = new AddUserCommand(_fourthUser);
       _removeFourth = new RemoveUserCommand(_fourthUser);
       _fifthUser = new CTEUser("fifthUser", InetAddress.getLocalHost(), Color.BLACK);
       _addFifth = new AddUserCommand(_fifthUser);
       _removeFifth = new RemoveUserCommand(_fifthUser);
       _sixthUser = new CTEUser("sixthUser", InetAddress.getLocalHost(), Color.BLACK);
       _addSixth = new AddUserCommand(_sixthUser);
       _removeSixth = new RemoveUserCommand(_sixthUser);
<<<<<<< HEAD
       _removeNonExistent = new RemoveUserCommand(new CTEUser("nonExistent", InetAddress.getLocalHost(), Color.BLACK));
=======
>>>>>>> 50182a8a48aa89adbb10d2877844c665d7e6fbaa
     }
     catch (Exception e) { System.out.println(e.getMessage()); }
   }
   
   public void testRemoveUserExecute(){
     try{
       assertEquals(0, _docController.getUsers().size());
       
       //Add six user to DocumentController
       _addFirst.execute(_docController);
       assertEquals(1, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_firstUser));
       
       _addSecond.execute(_docController);
       assertEquals(2, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_secondUser));
       
       _addThird.execute(_docController);
       assertEquals(3, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_thirdUser));
       
       _addFourth.execute(_docController);
       assertEquals(4, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_fourthUser));
       
       _addFifth.execute(_docController);
       assertEquals(5, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_fifthUser));
       
       _addSixth.execute(_docController);
       assertEquals(6, _docController.getUsers().size());
       assertTrue(_docController.getUsers().contains(_sixthUser));
       
       //Remove the six users
       _removeFirst.execute(_docController);
       assertEquals(5, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_firstUser));
       
       _removeSecond.execute(_docController);
       assertEquals(4, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_secondUser));
       
       _removeThird.execute(_docController);
       assertEquals(3, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_thirdUser));
       
       _removeFourth.execute(_docController);
       assertEquals(2, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_fourthUser));
       
       _removeFifth.execute(_docController);
       assertEquals(1, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_fifthUser));
       
       _removeSixth.execute(_docController);
       assertEquals(0, _docController.getUsers().size());
       assertFalse(_docController.getUsers().contains(_sixthUser));
<<<<<<< HEAD
      
       //Test ideas: Remove a non-existent user / Remove from an empty CTEUserManager
=======
>>>>>>> 50182a8a48aa89adbb10d2877844c665d7e6fbaa
     }
     catch (Exception e){fail(e.getMessage());}
   }//End testUserRemoveExecute()
   
 }//End class RemoveUserCommandTest
