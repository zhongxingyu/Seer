 package com.od.jtimeseries.ui.timeserious.action;
 
 import com.od.jtimeseries.ui.timeserious.action.DesktopSelectionActionModel;
 import com.od.jtimeseries.ui.util.ImageUtils;
 import com.od.swing.action.ModelDrivenAction;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 26-Mar-2010
  * Time: 16:32:43
  */
 public class NewVisualizerAction extends ModelDrivenAction<DesktopSelectionActionModel> {
 
     private JFrame mainFrame;
 
     public NewVisualizerAction(JFrame mainFrame, DesktopSelectionActionModel m) {
         super(m, "New Visualizer", ImageUtils.FRAME_ICON_16x16);
         this.mainFrame = mainFrame;
         super.putValue(SHORT_DESCRIPTION, "Create a new chart visualizer in current desktop");
     }
 
     public void actionPerformed(ActionEvent e) {
         if ( getActionModel().isDesktopSelected()) {
             String name = JOptionPane.showInputDialog(mainFrame, "Name for visualizer?", "Choose Name", JOptionPane.QUESTION_MESSAGE);
            name = name.trim();
            name = name.length() == 0 ? "Visualizer" : name;
            getActionModel().getDesktop().createAndAddVisualizer(name);
         }
     }
 }
