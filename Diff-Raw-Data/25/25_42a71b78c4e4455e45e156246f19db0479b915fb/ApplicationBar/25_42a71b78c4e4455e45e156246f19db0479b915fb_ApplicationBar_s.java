 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.ui.applicationbar;
 
 import com.jme.system.DisplaySystem;
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BWindow;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import com.jmex.bui.text.HTMLView;
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.DiagramKey;
 import edu.gatech.statics.exercise.DiagramType;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.exercise.persistence.StaticsXMLDecoder;
 import edu.gatech.statics.exercise.persistence.StaticsXMLEncoder;
 import edu.gatech.statics.exercise.state.ExerciseState;
 import edu.gatech.statics.ui.InterfaceRoot;
 import java.beans.ExceptionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class ApplicationBar extends BWindow {
 
     private static final int ADVICE_BOX_SIZE = 200;
     public static final int APPLICATION_BAR_HEIGHT = 200;
     private List<ApplicationTab> tabs = new ArrayList<ApplicationTab>();
     private BContainer tabBar;
     private BContainer mainBar;
     private BContainer diagramBox;
     private HTMLView adviceBox;
     private ApplicationModePanel modePanel;
     private BButton undoButton,  redoButton,  saveButton,  loadButton;
 
     // ***
     // DO WE NEED A TITLE BAR, ONE WITH TEXT TO SHOW THE DIAGRAM NAME???
     // MAYBE MOVE THESE INTO ApplicationModePanel
     public void setAdvice(String advice) {
         adviceBox.setContents(advice);
     }
 
     public ApplicationModePanel getModePanel() {
         return modePanel;
     }
 
     protected void disableAllTabs() {
         for (ApplicationTab tab : tabs) {
             tab.setTabEnabled(false);
         }
     }
 
     public void enableTab(Mode mode, boolean enabled) {
         ApplicationModePanel panel = InterfaceRoot.getInstance().getModePanel(mode.getModeName());
         panel.getTab().setTabEnabled(enabled);
     }
 
     /**
      * Sets a mode panel to be current. This is called after the new diagram is set.
      * @param modePanel
      */
     public void setModePanel(ApplicationModePanel modePanel) {
         if (this.modePanel != null) {
             this.modePanel.getTab().setActive(false);
             mainBar.remove(this.modePanel);
         }
         this.modePanel = modePanel;
         if (modePanel != null) {
             mainBar.add(modePanel, BorderLayout.CENTER);
 
             modePanel.activate();
             modePanel.getTab().setActive(true);
 
             enableTabs();
         }
     }
 
     /**
      * This enables the tabs that should be active, given the current diagram.
      */
     protected void enableTabs() {
 
         // this is the current diagram, the most up-to-date one.
         DiagramKey key = StaticsApplication.getApp().getCurrentDiagram().getKey();
 
         // go through all types
         for (DiagramType type : DiagramType.allTypes()) {
 
             // is this type enabled for our key?
             // if not, continue.
             if (Exercise.getExercise().getDiagram(key, type) == null) {
                 continue;            // go through the types of mode panels
             // and pick out the one that matches the type
             }
             for (ApplicationModePanel panel : InterfaceRoot.getInstance().getAllModePanels()) {
 
                 if (panel.getDiagramType() == type) {
                     panel.getTab().setTabEnabled(true);
                 }
             }
         }
     }
 
     public ApplicationBar() {
         super(InterfaceRoot.getInstance().getStyle(), new BorderLayout());
 
         //tabBar = createTabBar();
         tabBar = new BContainer(GroupLayout.makeHoriz(GroupLayout.LEFT));
         add(tabBar, BorderLayout.NORTH);
 
         mainBar = new BContainer(new BorderLayout(5, 0));
         add(mainBar, BorderLayout.CENTER);
 
         BContainer sideBox = new BContainer(new BorderLayout());
         mainBar.add(sideBox, BorderLayout.EAST);
 
         adviceBox = createAdviceBox();
         sideBox.add(adviceBox, BorderLayout.EAST);
 
         BContainer undoRedoBox = createUndoRedoBox();
         sideBox.add(undoRedoBox, BorderLayout.CENTER);
 
         BContainer saveLoadBox = createSaveLoadBox();
         sideBox.add(saveLoadBox, BorderLayout.WEST);
 
         diagramBox = createDiagramBox();
         //mainBar.add(diagramBox, BorderLayout.WEST);
 
         mainBar.setStyleClass("application_bar");
 
         setPreferredSize(DisplaySystem.getDisplaySystem().getWidth(), APPLICATION_BAR_HEIGHT);
     }
 
     private BContainer createDiagramBox() {
         diagramBox = new BContainer(new BorderLayout());
         diagramBox.setStyleClass("advice_box"); // this will change.
         diagramBox.setPreferredSize(100, 100);
         return diagramBox;
     }
 
     private BContainer createUndoRedoBox() {
 
         BContainer inner = new BContainer(GroupLayout.makeVert(GroupLayout.CENTER));
         //outer.add(inner, BorderLayout.CENTER);
 
         ActionListener undoRedoListener = new UndoRedoListener();
         undoButton = new BButton("Undo", undoRedoListener, "undo");
         redoButton = new BButton("Redo", undoRedoListener, "redo");
         inner.add(undoButton);
         inner.add(redoButton);
 
         return inner;
     }
 
     private BContainer createSaveLoadBox() {
         BContainer inner = new BContainer(GroupLayout.makeVert(GroupLayout.CENTER));
         //outer.add(inner, BorderLayout.CENTER);
 
         ActionListener saveLoadListener = new SaveLoadListener();
         saveButton = new BButton("Save", saveLoadListener, "save");
         loadButton = new BButton("Load", saveLoadListener, "load");
         inner.add(saveButton);
         inner.add(loadButton);
 
         return inner;
     }
 
     private final class SaveLoadListener implements ActionListener {
 
         public void actionPerformed(ActionEvent event) {
             try {
                 if (event.getAction().equals("save")) {
                     FileOutputStream output = new FileOutputStream("Test.xml");
                     if (getModePanel() == null || getModePanel().getDiagram() == null) {
                         return;
                     }
                     StaticsXMLEncoder encoder = new StaticsXMLEncoder(new BufferedOutputStream(output));
 //                XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(output));
                     encoder.setExceptionListener(new ExceptionListener() {
 
                         public void exceptionThrown(Exception e) {
                             System.out.println("got exception!!:");
                             e.printStackTrace();
                         }
                     });
 
                     encoder.writeObject(Exercise.getExercise().getState());
                     //encoder.writeObject(getModePanel().getDiagram().getCurrentState());
                     encoder.close();
 
                     System.out.println(encoder.getPersistenceDelegate(getModePanel().getDiagram().getCurrentState().getClass()));
 
                     System.out.println(new String(output.toString()));
                 } else if (event.getAction().equals("load")) {
 
                     StaticsXMLDecoder decoder = new StaticsXMLDecoder(new BufferedInputStream(new FileInputStream("Test.xml")));
                     ExerciseState stateTest = (ExerciseState) decoder.readObject();
                     ExerciseState stateCheck = Exercise.getExercise().getState();
                     //DiagramState stateTest = (DiagramState) decoder.readObject();
                     //DiagramState stateCheck = getModePanel().getDiagram().getCurrentState();
                    
                    System.out.println("old: "+stateCheck);
                    System.out.println("new: "+stateTest);
                    
                     System.out.println(stateTest.equals(stateCheck));
                    
 //                    ModifiedXMLDecoder mDecoder = new ModifiedXMLDecoder(new BufferedInputStream(new FileInputStream("Test.xml")));
 //                    DiagramState mstateTest = (DiagramState) mDecoder.readObject();
 //                    DiagramState mstateCheck = getModePanel().getDiagram().getCurrentState();
 //                    
 //                    System.out.println("old: "+mstateCheck);
 //                    System.out.println("new: "+mstateTest);
 //                    
 //                    System.out.println(mstateTest.equals(mstateCheck));
                    
                 //FileInputStream input = new FileInputStream("Test.xml");
 //                    String outString = decoder.toString();
 //                    System.out.println(outString);
 //                    System.out.println(outString);
                 //StaticsXMLDecoder decoder = new StaticsXMLDecoder(new BufferedInputStream(input));
                 }
             } catch (Exception e) {
                 System.out.println(e);
             }
         }
     }
 
     void updateUndoRedoState() {
         if (getModePanel() == null || getModePanel().getDiagram() == null) {
             undoButton.setEnabled(false);
             redoButton.setEnabled(false);
             return;
         }
         undoButton.setEnabled(getModePanel().getDiagram().canUndo());
         redoButton.setEnabled(getModePanel().getDiagram().canRedo());
     }
 
     private final class UndoRedoListener implements ActionListener {
 
         public void actionPerformed(ActionEvent event) {
             if (getModePanel() == null || getModePanel().getDiagram() == null) {
                 return;
             }
             if (event.getAction().equals("undo")) {
                 getModePanel().getDiagram().undo();
             } else if (event.getAction().equals("redo")) {
                 getModePanel().getDiagram().redo();
             }
         }
     }
 
     private HTMLView createAdviceBox() {
         adviceBox = new HTMLView();
         adviceBox.setContents("Help is described here");
         adviceBox.setPreferredSize(ADVICE_BOX_SIZE, ADVICE_BOX_SIZE);
         adviceBox.setStyleClass("advice_box");
         return adviceBox;
     }
 
     public void setTabs(List<ApplicationModePanel> panels) {
         BContainer spacer = new BContainer();
         spacer.setPreferredSize(3, -1);
         tabBar.add(spacer);
 
         for (ApplicationModePanel panel : panels) {
 
             ApplicationTab tab = panel.getTab();
             tabBar.add(tab);
             tabs.add(tab);
         }
     }
 
     public void removeTabs() {
         tabBar.removeAll();
     }
 }
