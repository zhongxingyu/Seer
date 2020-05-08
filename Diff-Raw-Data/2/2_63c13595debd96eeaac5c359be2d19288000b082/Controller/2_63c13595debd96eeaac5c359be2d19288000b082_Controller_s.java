 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 
 import view.HistoryFrame;
 import view.ProjectsFrame;
 import view.OneActionFrame;
 import view.ActionsFrame;
 import model.exceptions.DatabaseException;
 import model.Action;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.Thought;
 import model.exceptions.ThingsException;
 import view.ThoughtsFrame;
 import com.google.gdata.util.ServiceException;
 import java.io.IOException;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import model.GTDcomplete;
 import view.OptionsFrame;
 import view.MainMenuFrame;
 import model.GoogleCalendar;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import model.Options;
 
 import view.MessageBox;
 import static view.MainConstants.*;
 
 /**
  * Houd alles bij van alle schermen. Is een soort van totaaloverzicht omdat alle schermen los van elkaar moeten werken
  * @author 
  */
 public class Controller implements Observer {
 
     //private ThoughtsFrame thoughtsFrame = new ThoughtsFrame();
     private GTDcomplete gtd = new GTDcomplete();
     
     private MainMenuFrame mainMenuFrame = new MainMenuFrame();;
     private OptionsFrame optionsMenuFrame;
     private ThoughtsFrame thoughtsFrame;
     private ActionsFrame actionFrame;
     private ProjectsFrame projectsFrame;
     
     private JButton[] buttons = mainMenuFrame.GetButtons();
     
     public Controller(){
         //thoughtsFrame.setVisible(false);
         //mainMenu.setVisible(false);
         
         //laad alle opties in
         OPTIONS.LoadOptions();
         
         //voiegt listeners toe
         AddMainMenuListeners();
         
         DoCheckStartupScreen();
         
         
         gtd.addObserver(this);
         
     }
     
     //voegt de actionlisteners toe aan het mainframe en andere JFrames
     private void AddMainMenuListeners(){
         
         
         //thoughts scherm buttons
         buttons[0].addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
                 //System.out.println("PUSH DA BUTTON BUTTON 1");
                 //System.out.println("save button - gUser: " + OPTIONS.getGCUsername());
                 
                 try {
                     //een scherm kan niet 2x geopent worden
                     if(thoughtsFrame == null){
                         gtd.SetAllThoughts();
                         thoughtsFrame = new ThoughtsFrame(gtd.GetAllThoughtsAsArray());
                         OPTIONS.setLastOpenedScreen(MenuScreen.THOUGHTS);
                         thoughtsFrame.addWindowListener(new WindowAdapter(){
                                public void windowOpened( WindowEvent e ){
                                     //thoughtsFrame.requestFocus();
                                  }
                                public void windowClosing( WindowEvent e ){
                                    //mainMenuFrame.setEnabled(true);
                                    DoReopenMainMenuFrame();
                                    thoughtsFrame.dispose();
                                    thoughtsFrame = null;
 
 
                                }
                        });
                     }
                 } catch (ThingsException ex) {
                 ex.printStackTrace();
                     MessageBox.DoOkErrorMessageBox(mainMenuFrame, "FOUT: laden gedachtes!",
                             "FOUT BIJ HET OPSLAAN VAN DE GEDACHTE, verbinding is in orde,"
                             + "\ngedachtes konden niet opgehaald worden van de database!");
                 } catch (DatabaseException ex) {
                     ex.printStackTrace();
                 MessageBox.DoOkErrorMessageBox(mainMenuFrame, "FOUT: laden gedachtes!",
                         "FOUT BIJ HET LADEN VAN DE GEDCHTES, \ncontrolleer de verbinding!");
                 }
                 
             }
         });
         
         //actions scherm
         buttons[1].addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
                 
                     //een scherm kan niet 2x geopent worden
                     if(actionFrame == null){
                         //de false is ervoor dat het GEEN history frame is
                         actionFrame = new ActionsFrame(false);
                         OPTIONS.setLastOpenedScreen(MenuScreen.ACTIONS); //stelt het laatst geopende scherm in
                         actionFrame.addWindowListener(new WindowAdapter(){
                        public void windowOpened( WindowEvent e ){
                             //thoughtsFrame.requestFocus();
                          }
                            public void windowClosing( WindowEvent e ){
                                //mainMenuFrame.setEnabled(true);
                                actionFrame.dispose();
                                actionFrame = null;
                                DoReopenMainMenuFrame();
 
                            }
                         });
 
                         actionFrame.previousButton.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e)
                             {
                                actionFrame.dispose();
                                actionFrame = null;
                                DoReopenMainMenuFrame();
 
                             }
                         });
                 }
                 
             }
         });
         
         //projecten
         buttons[2].addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {                
                 //een scherm kan niet 2x geopent worden
                 if(projectsFrame == null){
 
                     projectsFrame = new ProjectsFrame();
                     OPTIONS.setLastOpenedScreen(MenuScreen.PROJECTS); //stelt het laatst geopende scherm in
                     projectsFrame.addWindowListener(new WindowAdapter(){
                    public void windowOpened( WindowEvent e ){
                         //thoughtsFrame.requestFocus();
                      }
                        public void windowClosing( WindowEvent e ){
                            //mainMenuFrame.setEnabled(true);
                            //projectsFrame.dispose();
                            if(projectsFrame.alreadyExited){
                                projectsFrame = null;
                                System.gc();
                                DoReopenMainMenuFrame();
                            } else {
                                projectsFrame.DoExitReal();
                            }
                        }
                     });
                 }
                 
             }
         });
         
         //history
         buttons[3].addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
                 
                     //een scherm kan niet 2x geopent worden
                     if(actionFrame == null){
                         
                        actionFrame = new ActionsFrame(false);
                         OPTIONS.setLastOpenedScreen(MenuScreen.HISTORY); //stelt het laatst geopende scherm in
                         actionFrame.addWindowListener(new WindowAdapter(){
                        public void windowOpened( WindowEvent e ){
                             //thoughtsFrame.requestFocus();
                          }
                            public void windowClosing( WindowEvent e ){
                                //mainMenuFrame.setEnabled(true);
                                actionFrame.dispose();
                                actionFrame = null;
                                DoReopenMainMenuFrame();
 
                            }
                         });
 
                         actionFrame.previousButton.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent e)
                             {
                                actionFrame.dispose();
                                actionFrame = null;
                                DoReopenMainMenuFrame();
 
                             }
                         });
                 }
                 
             }
         });
             
         
         //haalt het optionsmenuitem op uit de het mainMenu en koppelt hier weer een actionlistener aan
         mainMenuFrame.GetOptionsMenuItem().addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
                 //System.out.println("menu opties3");
                 //laat het options scherm en schakelt het hoofdscherm uit
                 optionsMenuFrame = new OptionsFrame();
                 mainMenuFrame.setEnabled(false);
                 OPTIONS.setLastOpenedScreen(MenuScreen.OPTIONS); //stelt het laatst geopende scherm in
                 //de listener voor het optionsMenu (Ander JFrame) om ervoor te zorgen
                 //dat als het options scherm sluit, je het main scherm weer kan bewerken
                 optionsMenuFrame.addWindowListener(new WindowAdapter(){
                    public void windowOpened( WindowEvent e ){
                         //field1.requestFocus();
                      }
                    public void windowClosing( WindowEvent e ){
                        mainMenuFrame.setEnabled(true);
                        optionsMenuFrame.dispose();
                        optionsMenuFrame = null;
                        mainMenuFrame.toFront();
                        
                    }
                 });
                 
                 
                     }
                 });
 
         
         //de exit knop uit het hoofdscherm
         mainMenuFrame.GetExitMenuItem().addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
                 System.exit(0);
             }
         });
         
         
         mainMenuFrame.addWindowListener(new WindowAdapter(){
            public void windowOpened( WindowEvent e ){
                 //field1.requestFocus();
              }
            public void windowClosing( WindowEvent e ){
                    mainMenuFrame.setVisible(false);
                    DoCheckLastWindowCloses();
                    //dispose();
            }
         }); 
 
     }
     
     public GTDcomplete GetModel(){
         return gtd;
     }
     
     @Override
     public void update(Observable o, Object arg) {
         System.out.println("update observer");
         if(arg instanceof Thought){
             
             //update de thougts table
             if(thoughtsFrame != null){
                 
                 thoughtsFrame.UpdateThoughts(gtd.GetAllThoughtsAsArray());
                 System.out.println("update observer thought");
             }
         } else if(arg instanceof Action){
             
         }
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     
     private void DoReopenMainMenuFrame(){
        
            //mainMenuFrame = new MainMenuFrame();
            mainMenuFrame.setVisible(true);
            mainMenuFrame.toFront();
        
     }
     
     //checkt of alle  schermen gesloten zijn, dan moet ie afsluiten
     private void DoCheckLastWindowCloses(){
         if(optionsMenuFrame == null && thoughtsFrame == null && actionFrame == null && projectsFrame == null && mainMenuFrame.isVisible() == false){
             System.exit(0);
         }
     }
 
     private void DoCheckStartupScreen() {
         MenuScreen prefferedOpened = OPTIONS.getPrefferedOpenedScreen();
         MenuScreen lastOpened = OPTIONS.getLastOpenedScreen();
         
         MenuScreen screenToOpen = (prefferedOpened == MenuScreen.LASTOPENED) ? lastOpened : prefferedOpened;
         
         switch(screenToOpen){
             case MAIN:
                 break;
             case ACTIONS:
                 buttons[1].getActionListeners()[0].actionPerformed(null);
                 break;
             case HISTORY:
                 buttons[3].getActionListeners()[0].actionPerformed(null);
                 break;
             case OPTIONS:
                 mainMenuFrame.GetOptionsMenuItem().getActionListeners()[0].actionPerformed(null);
                 break;
             case PROJECTS:
                 buttons[2].getActionListeners()[0].actionPerformed(null);
                 break;
             case THOUGHTS:
                 buttons[0].getActionListeners()[0].actionPerformed(null);
                 break;
         }
         
     }
     
 }
 
     
