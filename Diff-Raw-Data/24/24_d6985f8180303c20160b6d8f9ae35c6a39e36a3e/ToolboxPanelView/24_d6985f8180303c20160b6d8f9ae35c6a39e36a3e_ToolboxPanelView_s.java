 package src.ui;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import src.nonui.AppController;
 
 /**
  * View for the toolbox floating panel.
  * 
  * Actually a subclass of JDialog, in order to get the right interaction with
  * the main frame.
  */
 public class ToolboxPanelView extends JDialog {
     // JDialog is serializable, so we need some ID to avoid compiler warnings.
     private static final long serialVersionUID = 1L;
 
     // The controller.
     private final AppController controller;
 
     public ToolboxPanelView(JFrame parentFrame, AppController appController) {
         super(parentFrame);
 
         this.controller = appController;
 
         addWindowListener(new ToolboxWindowListener(controller));
         initUI();
     }
 
     /**
      * Sets up the UI for the toolbox.
      */
     private void initUI() {
         setLayout(new FlowLayout());
 
        JButton finishedEditingButton = new JButton("Done");
         finishedEditingButton.setMnemonic(KeyEvent.VK_N);
         finishedEditingButton.setSize(50, 20);
         finishedEditingButton.setEnabled(true);
         finishedEditingButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.toolboxDoneButtonClicked();
             }
         });
        finishedEditingButton.setToolTipText("Finish editing label");
 
         JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
         undoButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.undoLastVertex();
             }
         });
         undoButton.setToolTipText("Undo");
 
         JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
         redoButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.redoLastVertex();
             }
         });
         redoButton.setToolTipText("Redo");
 
        JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.cancelAddingPolygon();
             }
         });
        cancelButton.setToolTipText("Cancel editing label");
 
         setLayout(new GridLayout(2, 1));
 
         JLabel instructions = new JLabel(
                 "<html><center>"
                         + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                         + "Click on the image to add points to the label.<br />"
                         + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                         + "Double click (or click \"Done\") to finish.</center></html>");
 
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
         buttonPane.add(Box.createHorizontalStrut(20));
         buttonPane.add(finishedEditingButton);
         buttonPane.add(Box.createHorizontalStrut(10));
         buttonPane.add(undoButton);
         buttonPane.add(Box.createHorizontalStrut(10));
         buttonPane.add(redoButton);
         buttonPane.add(Box.createHorizontalStrut(10));
         buttonPane.add(cancelButton);
         buttonPane.add(Box.createHorizontalStrut(20));
 
         add(instructions);
         add(buttonPane);
 
         setFocusableWindowState(false);
         setResizable(false);
         setVisible(false);
         pack();
     }
 
     /**
      * Listener for the Toolbox, to allow the 'X' button to apply the same
      * effect as "Cancel".
      */
     private static class ToolboxWindowListener implements WindowListener {
         private final AppController controller;
 
         public ToolboxWindowListener(AppController controller) {
             this.controller = controller;
         }
 
         @Override
         public void windowClosing(WindowEvent e) {
             controller.cancelAddingPolygon();
         }
 
         @Override
         public void windowOpened(WindowEvent e) {
         }
 
         @Override
         public void windowIconified(WindowEvent e) {
         }
 
         @Override
         public void windowDeiconified(WindowEvent e) {
         }
 
         @Override
         public void windowDeactivated(WindowEvent e) {
         }
 
         @Override
         public void windowClosed(WindowEvent e) {
         }
 
         @Override
         public void windowActivated(WindowEvent e) {
         }
     }
 }
