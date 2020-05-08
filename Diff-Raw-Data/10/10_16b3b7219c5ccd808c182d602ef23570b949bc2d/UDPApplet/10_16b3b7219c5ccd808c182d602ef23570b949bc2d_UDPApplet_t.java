 import java.applet.*;
 import java.net.*;
 import java.awt.Graphics;
 
 
 public class UDPApplet extends Applet {
     public void init() {
 	hello();
     }
     
     public void hello() {
 	
	String msg = "alert - Hello, Applet!";
 	
	String cmd = "javascript:alert(\"" + msg + "\")";
 	
 	try {
 	    getAppletContext().showDocument(new URL(cmd));
 	}
 	catch (MalformedURLException me) { }
 	
     }
 }
