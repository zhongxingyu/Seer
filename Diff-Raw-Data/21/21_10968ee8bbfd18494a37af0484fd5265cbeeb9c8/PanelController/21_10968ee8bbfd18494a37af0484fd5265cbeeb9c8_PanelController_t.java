 package com.ece.superkids.ui.controllers;
 
 import com.ece.superkids.ui.events.PanelListener;
 import com.ece.superkids.ui.events.Session;
 import com.ece.superkids.ui.frames.MainFrame;
 import com.ece.superkids.ui.parent.panels.InitialSetupPanel;
 import com.ece.superkids.ui.parent.panels.ParentalControlPanel;
import com.ece.superkids.ui.user.panels.NewGamePanel;
 import com.ece.superkids.ui.user.panels.StartScreenPanel;
 import com.ece.superkids.ui.user.panels.SubjectSelectionPanel;
 import com.ece.superkids.ui.user.panels.UserSelectionPanel;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  * THIS CLASS CONTROLS PANELS THAT ARE ADDED TO THE MAIN FRAME
  * BASICALLY ALLOWS THE USAGE OF GO BACK FUNCTION
  * SINGLETON PATTERN IS USED FOR ACCESS GLOBALLY
  * THIS CONTROLLER ALLOWS YOU TO ADD AND REMOVE FRAMES EASILY BY JUST CALLING THE
  * APPROPRIATE FUNCTION WHEN A BUTTON IS CLICKED
  */
 /**
  *
  * @author baris & david c
  */
 public class PanelController {
 
     private ArrayList<JPanel> panels;
     private MainFrame mainFrame;
     private PanelListener listener;
     private Session session = Session.aSession();
 
     private PanelController() {
         panels = new ArrayList<JPanel>();
     }
 
     private static class PanelControllerHolder {
         public static final PanelController INSTANCE = new PanelController();
     }
 
     public static PanelController getInstance() {
         return PanelControllerHolder.INSTANCE;
     }
 
     public void setMainFrame(MainFrame mainFrame) {
         this.mainFrame = mainFrame;
         listener = new PanelListener(mainFrame);
     }
 
     //add to panels list.
     //add to mainFrame.
     public void addPanel(JPanel panel) {
         //first add to list
         panels.add(panel);
         //if we are not on start panel
         //set the panel before invisible
         if (onStartPanel() == false) {
             panels.get(panels.size() - 2).setVisible(false);
         }
         //now add this new panel to the frame itself
         mainFrame.contentArea.add(panel, java.awt.BorderLayout.CENTER);
         //need to trigger setvisible true in listener so need to set it to false first
         panel.setVisible(false);
         //add to listener
         listener.addPanel(panel);
         //and change the visiblity to true
         panel.setVisible(true);
     }
 
     //removes the last panel from the list by setting it invisible and removing it from the list
     //shouldnt call this directly
     //call goBackOnePanel instead
     public void removePanel() {
         //set last panel invisible
         panels.get(panels.size() - 1).setVisible(false);
         //then remove from list
         panels.remove(panels.size() - 1);
     }
 
     //are we on start panel?
     private boolean onStartPanel() {
         if (panels.size() == 1) {
             return true;
         }
         return false;
     }
 
     //get the current panel in the list/visible on the main frame.
     public JPanel getCurrentPanel() {
         return panels.get(panels.size() - 1);
     }
 
     //when going back only remove the last panel from the list and set the panel before that visible.
     public void goBackOnePanel() {
 
         //if there is only one panel in the list then you are in the main menu, so return this function
         if (this.onStartPanel()) {
             return;
         }
         //remove last panel from list and set it invisible
         removePanel();
         //set the panel before the last panel visible
         panels.get(panels.size() - 1).setVisible(true);
     }
    
    public void goToLearning(){
        if (session.isUserLoggedIn()) {
            NewGamePanel sPanel = new NewGamePanel("New Tutorial"); 
            this.addPanel(sPanel);
        } else {
            this.addPanel(new UserSelectionPanel());
        }
    }
 
     //go to main menu (startscreenpanel)
     public void goToMainMenu() {
         boolean found = false;
         String startScreenName = "";
         if (session.isUserLoggedIn()) {
             startScreenName = "StartScreen";
         } else {
             startScreenName = "UserSelection";
         }
         //start from back of the list to search for start screen
         for (int i = panels.size() - 1; i > -1; i--) {
             //if the panel name is not null
             if (panels.get(i).getName() != null) {
                 //and if the name is eqaul to start screen
                 if (panels.get(i).getName().equals(startScreenName)) {
                     //set the subject selection visible                    
                     panels.get(i).setVisible(true);
                     return;
                 }
             }
             //go back one panel until subjectselection is found
             goBackOnePanel();
         }
         //if not found then create you self.
         if (found == false) {
           if(startScreenName.equals("StartScreen")){
               this.addPanel(StartScreenPanel.getInstance());
           }
           if(startScreenName.equals("UserSelection")){
               this.addPanel(new UserSelectionPanel());
           }
         }
     }
 //goes back to subject selection menu if found
 
     public void goToSubjectMenu() {
         boolean found = false;
         //start from back of the list to search for subject selection
         for (int i = panels.size() - 1; i > -1; i--) {
             //if the panel name is not null
             if (panels.get(i).getName() != null) {
                 //and if the name is eqaul to subjectselection
                 if (panels.get(i).getName().equals("SubjectSelection")) {
                     //set the subject selection visible
                     panels.get(i).setVisible(true);
                     return;
                 }
             }
             //go back one panel until subjectselection is found
             goBackOnePanel();
         }
         if(found==false){
             this.addPanel(new SubjectSelectionPanel(session.getLoggedInUser().getState().getCurrentLevel(),false));
         }
     }
     
     
     //Goes back to parentalcontrol Panel
     public void goToParentalControl() {
         //start from back of the list to search for subject selection
         for (int i = panels.size() - 1; i > -1; i--) {
             //if the panel name is not null
             if (panels.get(i).getName() != null) {
                 //and if the name is eqaul to parental control
                 if (panels.get(i).getName().equals("ParentalControl")) {
                     //set the subject selection visible
                     panels.get(i).setVisible(true);
                     return;
                 }
             }
             //go back one panel until ParentalControl is found
             goBackOnePanel();
         }       
     }
     
     public void setParentalControlLayerPane(JPanel panel) {
          //start from back of the list to search for subject selection
         for (int i = panels.size() - 1; i > -1; i--) {
             //if the panel name is not null
             if (panels.get(i).getName() != null) {
                 //and if the name is eqaul to parental control
                 if (panels.get(i).getName().equals("ParentalControl")) {
                     //set the subject selection visible
                     ParentalControlPanel foo = (ParentalControlPanel)panels.get(i);
                     foo.jLayeredPane2.removeAll();
                     foo.jLayeredPane2.repaint();
                     foo.jLayeredPane2.add(panel);
                     return;
                 }
             }
         }
         
     }
     
     //Create a new InitialSetup Panel and remove all other panels
     public void goToInitialSetup() {
         for (int i = panels.size()-1; i >=0; i--)
         {
             panels.get(i).setVisible(false);
             //then remove from list
             panels.remove(i);
         }
         addPanel(new InitialSetupPanel());
         
     }
 }
