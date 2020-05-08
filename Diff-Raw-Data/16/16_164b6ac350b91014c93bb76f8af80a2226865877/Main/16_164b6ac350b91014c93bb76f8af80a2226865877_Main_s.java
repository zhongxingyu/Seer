 package ircclient;
 
import java.io.IOException;

 import ircclient.gui.Tray;
 import ircclient.gui.windows.IRCWindow;
 import com.chopnix.workers.NetXml;
 
 /**
  *
  * @author fc
  */
 public class Main {
 
     public static void main(String[] args) {
         IRCWindow w = new IRCWindow("IRC");
         Tray t = new Tray(w);
         w.setTray(t);
         w.setVisible(true);
         // finger licking good
        try {
 		NetXml.main(new String[0]);
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
     }
 }
