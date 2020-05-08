 package jDistsim.ui.panel.workspace;
 
 import jDistsim.ui.control.GradientTitle;
 import jDistsim.ui.control.tabControl.TabControl;
 import jDistsim.ui.control.tabControl.TabItem;
 import jDistsim.ui.panel.tab.*;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.resource.TextResources;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 25.9.12
  * Time: 21:21
  */
 public class InformationPanel extends JPanel {
 
     private LogTabPanel logTabPanel;
     private NotesTabPanel notesTabPanel;
     private OutputTabPanel outputTabPanel;
     private EntitiesTabPanel entitiesTabPanel;
     private RemoteModelsTabPanel remoteModelsTabPanel;
 
    public InformationPanel(JTextArea outputTextArea, JTable entitiesInfoTable, JTable remoteModelsTable, JTable environmentTable) {
         Logger.log("Initialize information panel");
        initializeComponents(outputTextArea, entitiesInfoTable, remoteModelsTable, environmentTable);
     }
 
    private void initializeComponents(JTextArea outputTextArea, JTable entitiesInfoTable, JTable remoteModelsTable, JTable environmentTable) {
         setBackground(new Color(240, 240, 240));
         setBorder(new EmptyBorder(1, 0, 1, 0));
         setLayout(new BorderLayout());
         add(new GradientTitle(this, TextResources.INFORMATION_PANEL_TITLE), BorderLayout.NORTH);
 
         JPanel panel = new JPanel();
         panel.setBorder(new EmptyBorder(5, 10, 5, 10));
         panel.setLayout(new BorderLayout());
 
         JPanel panel1 = new JPanel();
 
         logTabPanel = new LogTabPanel();
         notesTabPanel = new NotesTabPanel();
        outputTabPanel = new OutputTabPanel(outputTextArea, environmentTable);
         entitiesTabPanel = new EntitiesTabPanel(entitiesInfoTable);
         remoteModelsTabPanel = new RemoteModelsTabPanel(remoteModelsTable);
 
         //panel4.setBackground(Color.yellow);
 
         panel1.setBackground(new Color(231, 231, 231));
         TabControl tabControl = new TabControl();
         tabControl.addTabItem(new TabItem("Remote models", remoteModelsTabPanel));
         tabControl.addTabItem(new TabItem("Simulator output", outputTabPanel));
         tabControl.addTabItem(new TabItem("Entities", entitiesTabPanel));
         tabControl.addTabItem(new TabItem("Application logs", logTabPanel));
         tabControl.addTabItem(new TabItem("Statistics", panel1));
         tabControl.addTabItem(new TabItem("Notes", notesTabPanel));
 
         panel.add(tabControl, BorderLayout.CENTER);
         add(panel, BorderLayout.CENTER);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(300, 200);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         g.setColor(new Color(192, 192, 192));
         g.drawLine(0, 0, getWidth(), 0);
     }
 
     public LogTabPanel getLogTabPanel() {
         return logTabPanel;
     }
 
     public OutputTabPanel getOutputTabPanel() {
         return outputTabPanel;
     }
 
     public NotesTabPanel getNotesTabPanel() {
         return notesTabPanel;
     }
 
     public EntitiesTabPanel getEntitiesTabPanel() {
         return entitiesTabPanel;
     }
 
     public RemoteModelsTabPanel getRemoteModelsTabPanel() {
         return remoteModelsTabPanel;
     }
 }
