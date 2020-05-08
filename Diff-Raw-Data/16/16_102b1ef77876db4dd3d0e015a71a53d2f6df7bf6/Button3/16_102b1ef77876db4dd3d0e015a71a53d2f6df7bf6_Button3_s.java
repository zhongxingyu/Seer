 import Button2.NetworkRequest;
 import Button2.Sender;
 import Util.Messages;
 import Util.Popup;
 import Util.Strings;
 import javax.microedition.lcdui.Display;
 import javax.microedition.midlet.MIDlet;
 

 /**
  * Application for button3. Requests server to end monitoring for this specific
  * user.
  *
  * @author Team Cirno
  */
public class Button3
        extends MIDlet
 {
     /**
      * Constant string representing the connection being OK.
      */
     private static final String OK = "OK"; 
     
     /**
      * When application is launched, startApp will be invoked.
      */
     public void startApp()
     {
         String response;
         Popup p;
         Display disp;
         disp = Display.getDisplay(this);
        try{
        
             String[] str = Strings.getAllMessages();
             p = new Popup(this, str[Messages.SENDING], false, null);
             disp.setCurrent(p);
             response = Sender.send(null, NetworkRequest.END_MONITORING_MESSAGE);
             p = new Popup(this, (response.equals(OK)) ? str[Messages.RECEIVED] : response, true, null);
             disp.setCurrent(p);
        }catch(Exception e){
             //most likely to happen if there is an error in Strings.getAllMessages()
             Popup pop = new Popup(this, "Please run configurator first.", true, null);
             disp.setCurrent(pop);
         }
     }
 
     public void pauseApp()
     {
     }
 
     public void destroyApp(boolean unconditional)
     {
     }

 }
