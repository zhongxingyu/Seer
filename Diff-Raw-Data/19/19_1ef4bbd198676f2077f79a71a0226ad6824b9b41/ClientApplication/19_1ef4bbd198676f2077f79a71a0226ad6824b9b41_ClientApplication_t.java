 package no.ntnu.fp.client.controller;
 
 import no.ntnu.fp.client.Connection;
 import no.ntnu.fp.client.gui.MainView;
 import no.ntnu.fp.client.gui.FindPersonView;
 import no.ntnu.fp.common.model.Employee;
 import no.ntnu.fp.common.model.Event;
 
 import javax.swing.*;

 import java.util.ArrayList;
 
 public class ClientApplication {
     private static JFrame mainFrame, eventFrame, findPersonFrame;
     
     private static EventViewController eventViewController = new EventViewController();
     private static MainViewController mainViewController;
     private static FindPersonViewController findPersonViewController = new FindPersonViewController();
     
    public static void main (String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
//    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     	mainFrame = new MainView();
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mainFrame.setVisible(true);
         
         mainViewController = new MainViewController((MainView)mainFrame);
         eventViewController = new EventViewController();
         
         
         findPersonFrame = new FindPersonView();
         findPersonFrame.setLocationRelativeTo(null);
         findPersonFrame.setVisible(false);
 
 
 //        ArrayList<Event> events = Connection.fetchEvents();
 //        for(Event e:events)
 //            System.out.println(e.toString());
 
 //        pack();
     }
 
     public static void pack(){
         if(mainFrame.isVisible()) mainFrame.pack();
         if(eventFrame.isVisible()) eventFrame.pack();
         if(findPersonFrame.isVisible()) findPersonFrame.pack();
     }
    
    public static void showEventView(){
    	eventViewController.setEventVisible(true);
    }
 
     public static void setMainFrameVisible(boolean visibility){
         mainFrame.setVisible(visibility);
     }
 
     public static void setEventFrameVisible(boolean visibility){
         eventFrame.setVisible(visibility);
     }
     public static void setFindPersonFrameVisible(boolean visibility){
         findPersonFrame.setVisible(visibility);
     }
 
     public static void setCurrentUser(Employee employee) {
         eventViewController.setCurrentUser(employee);
         findPersonViewController.setCurrentUser(employee);
         mainViewController.setCurrentUser(employee);
     }
     
     public static MainViewController getMainViewController() {
     	return mainViewController;
     }
 }
