 package net.miscjunk.aamp.server.java;
 
 import net.miscjunk.aamp.common.AppListener;
 import net.miscjunk.aamp.common.PlayerHandler;
 import javafx.application.Application;
 import javafx.stage.Stage;
 
 public class AAMPMain extends Application{
 	PlayerHandler handler;
 	public static void main(String[] args) throws Exception {
 		Application.launch();
 	}
 
 	@Override
 	public void start(Stage unused) throws Exception {
 		handler = new JavaPlayerHandler();
		AppListener listener = new AppListener(handler);
 		listener.start();
 	}
 }
