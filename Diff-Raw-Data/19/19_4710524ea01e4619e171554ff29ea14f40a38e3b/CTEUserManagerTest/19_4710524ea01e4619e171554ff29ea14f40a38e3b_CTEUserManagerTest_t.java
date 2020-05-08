 package test;
 
 import handler.*;
 import user.*;
 import junit.framework.TestCase;
 import static java.lang.System.out;
 import java.net.*;
 import java.awt.Color;
 
 public class CTEUserManagerTest extends TestCase {
 
     private CTEUserManager _userManager;
 
     protected void setUp() {
         _userManager = new CTEUserManager();
     }
 
     public void testUser() {
         try {
             Color _inputColor0 = Color.black;
             String _inputUserID0 = "mlbrinle";
             InetAddress _IPAddress0 = null;
             _IPAddress0 = InetAddress.getLocalHost();
             CTEUser testUser0 = new CTEUser(_inputUserID0, _IPAddress0, _inputColor0);
             assertEquals(testUser0.getCursorColor(), _inputColor0);
             assertEquals(testUser0.getName(), _inputUserID0);
             assertEquals(testUser0.getIPAddress(), InetAddress.getLocalHost());
             assertEquals(testUser0.getPosition().getPosition(), 0);
 
             //Test create user with null userID
             try {
                 CTEUser testUser1 = new CTEUser("", _IPAddress0, _inputColor0);
                 assertEquals(testUser1.getCursorColor(), _inputColor0);
                 assertEquals(testUser1.getName(), "");
             } catch (InvalidUserIDException e) {
                 String exceptionMessage = e.getMessage();
                 assertEquals("User ID can not be null", exceptionMessage);
             }
         } catch (Exception otherExceptions) {
             otherExceptions.printStackTrace();
         }
     }
 
     public void testCTEUserManager() {
         try {
             CTEUserManager _manager = new CTEUserManager();
             assertEquals(_manager.getNumberOfUsers(), 0);
             CTEUser mbrinle = new CTEUser("mlbrinle", InetAddress.getLocalHost(), ColorList.getColor(0));
             _manager.addUser(mbrinle);
             assertEquals(_manager.getNumberOfUsers(), 1);
             //see if a user can be added with the same user ID as one contained
             _manager.addUser((CTEUser) mbrinle.clone());
         }
         catch (Exception e) { e.printStackTrace(); }
     }
 
     public void testCTEUserManagerColors() {
         try {
             CTEUserManager _manager = new CTEUserManager();
             String baseName = "user";
             int i = 1;
             while (i < 15) {
                 String userName = baseName + i;
                 CTEUser temp = new CTEUser(userName, InetAddress.getLocalHost(), ColorList.getColor(i));
                 _manager.addUser(temp);
                 i++;
             }
             int k = 14;
         } catch (Exception e) {
             out.println("Exception caught:");
             out.println(e.getMessage());
         }
     }
 
     public void testUpdateBeyond() {
         try {
             CTEUser frank = new CTEUser("frank", InetAddress.getLocalHost(), ColorList.getColor(0));
             frank.setPosition(new TextPosition(41));
             _userManager.addUser(frank);
             CTEUser foo = new CTEUser("foo", InetAddress.getLocalHost(), ColorList.getColor(1));
             foo.setPosition(new TextPosition(42));
             _userManager.addUser(foo);
             CTEUser bar = new CTEUser("bar", InetAddress.getLocalHost(), ColorList.getColor(2));
             bar.setPosition(new TextPosition(43));
             _userManager.addUser(bar);
             TextPosition pivot = new TextPosition(42);
             _userManager.updateBeyond(pivot, 10);
            assertEquals(new TextPosition(41), frank.getPosition());
            assertEquals(new TextPosition(42), foo.getPosition());
            assertEquals(new TextPosition(53), bar.getPosition());
         } catch (Exception e) {
             out.println("Exception caught:");
             out.println(e.getMessage());
         }
     }
 
     public void testUpdateBetween() {
         try {
             CTEUser manuel = new CTEUser("manuel", InetAddress.getLocalHost(), ColorList.getColor(0));
             manuel.setPosition(new TextPosition(21));
             _userManager.addUser(manuel);
             CTEUser pedro = new CTEUser("pedro", InetAddress.getLocalHost(), ColorList.getColor(0));
             pedro.setPosition(new TextPosition(22));
             _userManager.addUser(pedro);
             CTEUser bonehead = new CTEUser("bonehead", InetAddress.getLocalHost(), ColorList.getColor(0));
             bonehead.setPosition(new TextPosition(23));
             _userManager.addUser(bonehead);
             CTEUser frank = new CTEUser("frank", InetAddress.getLocalHost(), ColorList.getColor(0));
             frank.setPosition(new TextPosition(41));
             _userManager.addUser(frank);
             CTEUser foo = new CTEUser("foo", InetAddress.getLocalHost(), ColorList.getColor(0));
             foo.setPosition(new TextPosition(42));
             _userManager.addUser(foo);
             CTEUser bar = new CTEUser("bar", InetAddress.getLocalHost(), ColorList.getColor(0));
             bar.setPosition(new TextPosition(43));
             _userManager.addUser(bar);
             TextPosition front = new TextPosition(22);
             TextPosition back = new TextPosition(42);
             _userManager.updateBetween(front, back);
            assertEquals(new TextPosition(21), manuel.getPosition());
            assertEquals(new TextPosition(22), pedro.getPosition());
            assertEquals(new TextPosition(22), bonehead.getPosition());
            assertEquals(new TextPosition(22), frank.getPosition());
            assertEquals(new TextPosition(22), foo.getPosition());
            assertEquals(new TextPosition(43), bar.getPosition());
         } catch (Exception e) {
             out.println("Exception caught:");
             out.println(e.getMessage());
         }
     }
 }
