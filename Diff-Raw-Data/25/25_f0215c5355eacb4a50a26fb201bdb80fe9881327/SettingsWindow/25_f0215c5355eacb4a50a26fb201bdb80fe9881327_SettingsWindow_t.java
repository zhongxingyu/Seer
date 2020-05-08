 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view;
 
 import controller.Simulation;
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 
 
 /**
  *
  * @author Dan
  */
 public class SettingsWindow {
 
     //TODO: the settings window closes even if an error occurs, fix this logic. 
     //TODO: there are todo's in the user interface class. do those too.
     private JFrame frame;
     private Container contentPane;
     private JLabel densityLbl;
     private JLabel aggressionLbl;
     private JLabel carRatioLbl;
     private JLabel truckRatioLbl;
     private JLabel junctionLbl;
     private JTextField densityField;
     private JTextField aggressionField;
     private JTextField carRatioField;
     private JTextField truckRatioField;
     private JButton submitBtn;
     private JButton defaultBtn;
     private GridBagConstraints cons;
     private JTextField[] fields;
     private JComboBox<String> junctions;
 
     public SettingsWindow() {
         initComponents();
         addComponents();
         addListeners();
         frame.setVisible(true);
         Simulation.getSimThread().start();
     }
 
     private void initComponents() {
         frame = new JFrame("Settings");
         contentPane = frame.getContentPane();
         densityLbl = new JLabel("Density of cars (number of cars created per second)");
         aggressionLbl = new JLabel("Aggression of drivers in simulation");
         carRatioLbl = new JLabel("Ratio of Cars");
         truckRatioLbl = new JLabel("Ratio of Trucks");
         junctionLbl = new JLabel("Junction to be simulated: ");
         densityField = new JTextField(20);
         aggressionField = new JTextField(20);
         carRatioField = new JTextField(20);
         truckRatioField = new JTextField(20);
         cons = new GridBagConstraints();
         submitBtn = new JButton("Submit");
         defaultBtn = new JButton("Default Values");
         frame.setResizable(false);
         contentPane.setLayout(new GridBagLayout());
         fields = new JTextField[4];
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         String[] junctionOptions = {"<<please enter a choice>>", "Two-Lane Junction", "Roundabout Junction", "Traffic light Junction", "Flyover Junction", "Plain Junction"};
         junctions = new JComboBox<String>(junctionOptions);
 
         frame.setLocationRelativeTo(null); //centers the frame in the screen
         
         
 
     }
 
     private void addComponents() {
         cons.fill = GridBagConstraints.HORIZONTAL;
         cons.gridx = 0;
         cons.gridy = 0;
         cons.ipadx = 5;
         cons.ipady = 5;
         contentPane.add(densityLbl, cons);
 
         cons.gridy = 1;
         contentPane.add(aggressionLbl, cons);
 
         cons.gridy = 2;
         contentPane.add(carRatioLbl, cons);
 
 
         cons.gridx = 1;
         cons.gridy = 0;
 
         contentPane.add(densityField, cons);
 
         cons.gridy = 1;
         contentPane.add(aggressionField, cons);
 
         cons.gridy = 2;
         contentPane.add(carRatioField, cons);
 
         cons.gridx = 2;
         cons.gridy = 1;
         contentPane.add(submitBtn, cons);
 
         cons.gridy = 2;
         contentPane.add(defaultBtn, cons);
 
         cons.gridy = 3;
         cons.gridx = 0;
         contentPane.add(truckRatioLbl, cons);
 
         cons.gridx = 1;
         contentPane.add(truckRatioField, cons);
 
         cons.gridy = 4;
         cons.gridx = 0;
         contentPane.add(junctionLbl, cons);
 
         cons.gridx = 1;
         contentPane.add(junctions, cons);
 
         densityField.setName(Simulation.DENSITY);
         fields[0] = densityField;
 
         aggressionField.setName(Simulation.AGGRESSION);
         fields[1] = aggressionField;
 
         carRatioField.setName(Simulation.CAR_RATIO);
         fields[2] = carRatioField;
 
         truckRatioField.setName(Simulation.TRUCK_RATIO);
         fields[3] = truckRatioField;
 
         
         
         frame.pack();
 
     }
 
     private void addListeners() {
         submitBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 int newValues = 0;
                 for (JTextField field : fields) {
                     if (!field.getText().isEmpty()) {
                         try {
                             int value = Integer.parseInt(field.getText());
 
                             if (value < 0) {
                                 showErrMessage("You have inputted negative values for 1 or more fields. Please enter sensible, positive values", "Error");
                                 return;
                             }
                             if (field.getName().equals(Simulation.AGGRESSION)) {
                                 if (value > 100) {
                                     showErrMessage("You cannot have an aggression level greater than 100", "Error");
                                     return;
                                 }
                                 
                             }
                             if (field.getName().equals(Simulation.CAR_RATIO)) {
                                 if(value > 100){
                                     showErrMessage("You cannot have a car ratio level greater than 100", "Error");
                                     return;
                                 }
                                 
                             }
                             
                             if (field.getName().equals(Simulation.TRUCK_RATIO)) {
                                 if(value > 100){
                                     showErrMessage("You cannot have a truck ratio level greater than 100", "Error");
                                     return;
                                 }
                                 
                             }
 
                             Simulation.setOption(field.getName(), value);
                             newValues++;
                         } catch (NumberFormatException nfe) {
                             showErrMessage("Some inputted values are incorrect. Please enter correct values", "Error");
                             return;
                         }
                         
                     }
                 }
                 
 
                 if(((Integer)Simulation.getOption(Simulation.CAR_RATIO)) + ((Integer)Simulation.getOption(Simulation.TRUCK_RATIO)) > 100){
                     showErrMessage("The ratio of cars : trucks must sum to 100.", "Error");
                     Simulation.setOption(Simulation.CAR_RATIO, 0);
                     Simulation.setOption(Simulation.TRUCK_RATIO, 0);
                     return;
                 }
                 
                 if (junctions.getSelectedIndex() == 0) {
                     showErrMessage("You haven't selected a junction. Please select a junction", "Error");
                     return;
                 }
 
                 if (newValues < fields.length) {
                     showErrMessage("You need to enter values for all fields", "Error");
                     return;
                 }
                 
                 String junction = (String)junctions.getSelectedItem();
                 if(junction.equals("Two-Lane Junction")){
                     Simulation.setOption(Simulation.JUNCTION_TYPE, new model.junctions.TwoLaneJunction());
                 }else if(junction.equals("Roundabout Junction")){
                     Simulation.setOption(Simulation.JUNCTION_TYPE, new model.junctions.RoundaboutJunction());
                 }
                 
                 synchronized(Simulation.getSimThread()){
                    SettingsWindow.this.setVisible(false);
                     Simulation.getSimThread().notify();
                 }
                
                
                
                 SettingsWindow.this.frame.dispose();
             }
         });
 
         defaultBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 densityField.setText("10");
                 aggressionField.setText("25");
                 carRatioField.setText("50");
                 truckRatioField.setText("50");
                 junctions.setSelectedIndex(1);
                /* Simulation.setOption(Simulation.DENSITY, 10);
                 Simulation.setOption(Simulation.AGGRESSION, 25);
                 Simulation.setOption(Simulation.CAR_RATIO, 50);
                 Simulation.setOption(Simulation.TRUCK_RATIO, 50);
                 Simulation.setOption(Simulation.JUNCTION_TYPE, JunctionConstants.TWO_LANE_JUNCTION);*/
             }
         });
     }
 
     private void showErrMessage(String message, String title) {
         JOptionPane.showMessageDialog(frame,
                 message,
                 title,
                 JOptionPane.ERROR_MESSAGE);
     }
     
     public boolean isVisible(){
        return frame.isVisible();
    }
    
    public void setVisible(boolean visible){
        frame.setVisible(visible);
     }
 }
