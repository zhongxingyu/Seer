 package net.miscjunk.aamp.server.java;
 
 import net.miscjunk.aamp.common.AppListener;
 import net.miscjunk.aamp.common.PlayerHandler;
 
 public class JavaTestAppListener extends AppListener {
 
 	public JavaTestAppListener(PlayerHandler handler) {
 		super(handler);
 	}
 
 	@Override
 	public void start() {
		handler.onControlEvent("play");
		try {Thread.sleep(500);}catch(Exception e) {}
		handler.onControlEvent("pause");
 		
 	}
 
 }
