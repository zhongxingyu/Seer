 package hms;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.SQLException;
 
 import hms.util.Database;
 import hms.views.*;
 import hms.controllers.LoginController;
 
 class HospitalManager implements Runnable
 {
 	public void run() {
		final ApplicationFrame appFrame = new ApplicationFrame();
		final LoginController loginController = new LoginController();
		final LoginView loginView = new LoginView(appFrame, loginController);
		loginView.show();
		appFrame.setVisible(true);
 	}
 	
 	public static void main(String[] args) {
 		// Shutdown hook to close the database connection when the application exits
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				try {
 					Database.getInstance().closeConnection();
 				} catch (SQLException sqle) { }
 			}
 		});
 		// Asynchronously instantiate our database connection now so we don't have 
 		// to when the user tries to log in
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Database.getInstance();
 				} catch (SQLException sqle) { }
 			}
 		});
 		
 		HospitalManager hm = new HospitalManager();
 		SwingUtilities.invokeLater(hm);
 	}
 }
