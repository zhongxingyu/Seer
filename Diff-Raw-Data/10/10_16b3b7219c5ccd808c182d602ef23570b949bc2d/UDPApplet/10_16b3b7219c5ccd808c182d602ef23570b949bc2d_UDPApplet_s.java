 import java.applet.*;
 import java.net.*;
 import java.awt.Graphics;
 
 
 public class UDPApplet extends Applet {
     public void init() {
 	hello();
     }
     
    public void paint(Graphics g) {
	g.drawString("Hello, Graphics!",50,25);
    }
    
     public void hello() {
 	
	String msg = "Hello, Applet!";
 	
	String cmd = "javascript:doAlert(\"" + msg + "\")";
 	
 	try {
 	    getAppletContext().showDocument(new URL(cmd));
 	}
 	catch (MalformedURLException me) { }
 	
     }
 }
