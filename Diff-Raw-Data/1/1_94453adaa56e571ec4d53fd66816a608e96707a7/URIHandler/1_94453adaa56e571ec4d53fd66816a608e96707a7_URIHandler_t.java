 package com.robonobo.gui.platform;
 
 import com.apple.eawt.AppEvent.OpenURIEvent;
 import com.apple.eawt.OpenURIHandler;
 import com.robonobo.gui.frames.RobonoboFrame;
 
 public class URIHandler implements OpenURIHandler {
 	private RobonoboFrame frame;
 	
 	public URIHandler(RobonoboFrame frame) {
 		this.frame = frame;
 	}
 
 	public void openURI(OpenURIEvent e) {
 		String uriStr = e.getURI().toString();
 		frame.openRbnbUri(uriStr);
 	}
 }
