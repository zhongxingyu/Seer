 package org.concord.sensor.applet;
 
 import java.applet.Applet;
 import java.awt.Graphics;
 
 import netscape.javascript.JSObject;
 
 public class DetectionApplet extends Applet {
 	private static final long serialVersionUID = 1L;
	private boolean notifiedJavascript = false;
 	
 	@Override
 	public void paint(Graphics g) {
 		g.clearRect(0, 0, getWidth(), getHeight());
 		
 		g.drawString("Loading...", (getWidth()/2)-29, (getHeight()/2)+7);
 	}
 	
 	public boolean areYouLoaded() {
 		return true;
 	}
 
 	@Override
	public void start() {
		// we only want to notify javascript once
		// previously this code was in the init method which would only run once
		// but that caused problems sometimes on IE.
		if (notifiedJavascript) {
			return;
		}
		notifiedJavascript = true;
 		String codeToEval = getParameter("evalOnInit");
 		if (codeToEval == null || codeToEval.length() == 0){
 			return;
 		}
 	    JSObject window = JSObject.getWindow(this);
 	    System.out.println("DectionApplet running evalOnInit code");
 	    window.eval(codeToEval);
 	}
 }
