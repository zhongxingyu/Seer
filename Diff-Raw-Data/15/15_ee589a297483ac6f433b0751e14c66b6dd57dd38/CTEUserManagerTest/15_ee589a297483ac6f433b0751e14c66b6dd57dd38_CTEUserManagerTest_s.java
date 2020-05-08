 package test;
 
 import handler.*;
 import user.*;
 import junit.framework.TestCase;
 import static java.lang.System.out;
 import java.net.*;
 import java.awt.Color;
 import java.util.ArrayList;
 
 public class CTEUserManagerTest extends TestCase {
 
     private volatile CTEUserManager _userManager;
     private CTEUser manuel;
     private CTEUser pedro;
     private CTEUser bonehead;
     private CTEUser frank;
     private CTEUser foo;
     private CTEUser bar;
     private CTEUser mbrinle;
 
     protected void setUp ( ) {
         _userManager = new CTEUserManager();
         try {
             manuel = new CTEUser("manuel", InetAddress.getLocalHost(), ColorList.getColor(0));
             pedro = new CTEUser("pedro", InetAddress.getLocalHost(), ColorList.getColor(0));
             bonehead = new CTEUser("bonehead", InetAddress.getLocalHost(), ColorList.getColor(0));
             frank = new CTEUser("frank", InetAddress.getLocalHost(), ColorList.getColor(0));
             foo = new CTEUser("foo", InetAddress.getLocalHost(), ColorList.getColor(0));
             bar = new CTEUser("bar", InetAddress.getLocalHost(), ColorList.getColor(0));
             mbrinle = new CTEUser("mlbrinle", InetAddress.getLocalHost(), ColorList.getColor(0));
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 
     public void testUser ( ) {
         try {
             Color _inputColor0 = Color.black;
             String _inputUserID0 = "mlbrinle";
             InetAddress _IPAddress0 = null;
             _IPAddress0 = InetAddress.getLocalHost();
             CTEUser testUser0 = new CTEUser(_inputUserID0, _IPAddress0, _inputColor0);
             assertEquals(_inputColor0, testUser0.getCursorColor());
             assertEquals(_inputUserID0, testUser0.getName());
             assertEquals(InetAddress.getLocalHost(), testUser0.getIPAddress());
             assertEquals(0, testUser0.getPosition().getPosition());
 
             //Test create user with null userID
             try {
                 CTEUser testUser1 = new CTEUser("", _IPAddress0, _inputColor0);
                 assertEquals(testUser1.getCursorColor(), _inputColor0);
                 assertEquals(testUser1.getName(), "");
             }
             catch (InvalidUserIDException e) {
                 String exceptionMessage = e.getMessage();
                 assertEquals("User ID can not be null", exceptionMessage);
             }
         } catch (Exception otherExceptions) { fail(otherExceptions.getMessage()); }
     }
 
     public void testAddUser ( ) {
         try {
             assertEquals(0, _userManager.getNumberOfUsers());
 
             _userManager.addUser(manuel);
 
             assertTrue(_userManager.contains(manuel));
             assertEquals(1, _userManager.getNumberOfUsers());
 
             _userManager.addUser(pedro);
 
             assertTrue(_userManager.contains(pedro));
             assertEquals(2, _userManager.getNumberOfUsers());
 
             _userManager.addUser(bonehead);
 
             assertTrue(_userManager.contains(bonehead));
             assertEquals(3, _userManager.getNumberOfUsers());
 
             _userManager.addUser(frank);
 
             assertTrue(_userManager.contains(frank));
             assertEquals(4, _userManager.getNumberOfUsers());
 
             _userManager.addUser(foo);
 
             assertTrue(_userManager.contains(foo));
             assertEquals(5, _userManager.getNumberOfUsers());
 
             _userManager.addUser(bar);
 
             assertTrue(_userManager.contains(bar));
             assertEquals(6, _userManager.getNumberOfUsers());
 
             assertTrue(_userManager.contains(manuel));
             assertTrue(_userManager.contains(pedro));
             assertTrue(_userManager.contains(bonehead));
             assertTrue(_userManager.contains(frank));
             assertTrue(_userManager.contains(foo));
             assertTrue(_userManager.contains(bar));
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 
     public void testCTEUserManager ( ) {
       out.println("testusermanager");
         try {
             assertEquals(_userManager.getNumberOfUsers(), 0);
             _userManager.addUser(mbrinle);
             assertEquals(_userManager.getNumberOfUsers(), 1);
             //see if a user can be added with the same user ID as one contained
             _userManager.addUser((CTEUser) mbrinle.clone());
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 
     public void testCTEUserManagerColors ( ) {
       out.println("testcolors");
         ArrayList<CTEUser> tempUsers = new ArrayList<CTEUser>();
         try {
             String baseName = "user";
             for (int i = 0; i < 15 ; i ++) {
                Thread.sleep(1); // Ensures users aren't created fast enough they have the same uniqueID
                 String userName = baseName + i;
                 CTEUser temp = new CTEUser(userName, InetAddress.getLocalHost(), ColorList.getColor(i));
 
                 _userManager.addUser(temp);
                 tempUsers.add(temp);
             }
             int len = tempUsers.size();
             for (int i = 0; i < len; i ++) {
                 CTEUser temp = tempUsers.get(i);
                 _userManager.removeUser(temp);
             }
             assertEquals(0, _userManager.getNumberOfUsers());
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 
     public void testUpdateBeyond ( ) {
       out.println("testupdatebeyond");
         try {
             frank.setPosition(new TextPosition(41));
             foo.setPosition(new TextPosition(42));
             bar.setPosition(new TextPosition(43));
 
             _userManager.addUser(frank);
             _userManager.addUser(foo);
             _userManager.addUser(bar);
 
             TextPosition pivot = new TextPosition(42);
             _userManager.updateBeyond(pivot, 10);
 
             assertEquals(new TextPosition(41), frank.getPosition());
             assertEquals(new TextPosition(42), foo.getPosition());
             assertEquals(new TextPosition(53), bar.getPosition());
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 
     public void testUpdateBetween ( ) {
       out.println("testupdatebetween");
         try {
             manuel.setPosition(new TextPosition(21));
             pedro.setPosition(new TextPosition(22));
             bonehead.setPosition(new TextPosition(23));
             frank.setPosition(new TextPosition(41));
             foo.setPosition(new TextPosition(42));
             bar.setPosition(new TextPosition(43));
 
             _userManager.addUser(manuel);
             _userManager.addUser(pedro);
             _userManager.addUser(bonehead);
             _userManager.addUser(frank);
             _userManager.addUser(foo);
             _userManager.addUser(bar);
 
             TextPosition front = new TextPosition(22);
             TextPosition back = new TextPosition(42);
 
             System.out.println( "" + _userManager + " >>> " + _userManager.getNumberOfUsers());
             _userManager.updateBetween(front, back);
             System.out.println(_userManager);
 
             assertEquals(new TextPosition(21), manuel.getPosition());
             assertEquals(new TextPosition(22), pedro.getPosition());
             assertEquals(new TextPosition(22), bonehead.getPosition());
             assertEquals(new TextPosition(22), frank.getPosition());
             assertEquals(new TextPosition(22), foo.getPosition());
             assertEquals(new TextPosition(43), bar.getPosition());
         }
         catch (Exception e) { fail(e.getMessage()); }
     }
 }
