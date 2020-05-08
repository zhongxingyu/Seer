 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 
 public class ControlFrame extends JFrame {
     private static final int DEFAULT_SIZE = 6;
 
     private Simulation  theSim;
     private DetailFrame theDetails;
 
     private JPanel toolbar;
 
     private JButton runButton;
     private JButton stopButton;
     private JButton stepButton;
    private JComboBox fileCombo;
 
     public ControlFrame() {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         setLocationByPlatform(true);
 
         toolbar = new JPanel();
 
         runButton  = new RunButton(this);
         stepButton = new StepButton();
         stopButton = new StopButton();
        fileCombo  = new JComboBox(new String[] {
             "foobar",
             "foobaz",
             "quuxba"
         });
 
         fileCombo.setEditable(false);
 
         toolbar.add(fileCombo);
         toolbar.add(runButton);
         toolbar.add(stepButton);
         toolbar.add(stopButton);
 
         add(toolbar);
 
         pack();
         setMinimumSize(getSize());
 
         theSim = new Simulation(DEFAULT_SIZE);
         theDetails = new DetailFrame(this, null); // theSim.getGrid().getSummaries()
 
         setVisible(true);
     }
 
     private class RunButton
     extends JButton
     implements ActionListener {
         private ControlFrame that;
         public RunButton(ControlFrame that) {
             super("Run");
             this.that = that;
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e) {
             if (theSim == null) {
                 theSim = new Simulation(DEFAULT_SIZE);
                 theDetails = new DetailFrame(that, null); // theSim.getGrid().getSummaries()
             }
         }
     }
 
     private class StopButton
     extends JButton
     implements ActionListener {
         public StopButton() {
             super("Stop");
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e) {
             stop();
         }
     }
 
     protected void stop() {
         theDetails.dispose();
         theSim = null;
     }
 
     private class StepButton
     extends JButton
     implements ActionListener {
         public StepButton() {
             super("Step");
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e) {
             if (theSim != null) {
                 theSim.step();
             }
         }
     }
 }
