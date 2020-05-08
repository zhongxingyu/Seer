 package gui.panels.nonnormpanels;
 
 import gui.panels.ControlPanel;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Hashtable;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 
 import transducer.Transducer;
 
 public class NonNormPanel extends JPanel implements ActionListener {
 
   ControlPanel parent;
   
   Transducer transducer;
   
   JComboBox nonNormSelector;
   
   JPanel bottomPanel = new JPanel();
   
   public NonNormPanel(ControlPanel c, Transducer t) {
     parent = c;
     transducer = t;
     
     this.setBackground(new Color(238,238,238));
     
     nonNormSelector = new JComboBox();
     
     nonNormSelector.addItem("Select Non-Norm: ");
     nonNormSelector.addItem("Stop/Start Conveyor (0-14)");
     nonNormSelector.addItem("Inline Workstation Break/Restart");
     nonNormSelector.addItem("Workstation Broken/Fixed");
     nonNormSelector.addItem("Popup Jam/Unjam");
     nonNormSelector.addItem("Offline Workstation Change Speed");
     nonNormSelector.addItem("Disable/Enable Offline Workstation");
     nonNormSelector.addItem("Truck Breaks Down/Fixed");
     nonNormSelector.addItem("Glass Breaking Offline");
     
     setLayout(new GridBagLayout());
     GridBagConstraints gbc = new GridBagConstraints();
     
     gbc.anchor = GridBagConstraints.NORTHWEST;
     gbc.ipadx = 400;
     gbc.gridx = 0;
     gbc.gridy = 0;
     gbc.weightx = 1;
     gbc.weighty = 1;
     add(nonNormSelector, gbc);
     gbc.gridy++;
     add(bottomPanel, gbc);
     bottomPanel.setBackground(new Color(238,238,238));
     
     nonNormActionListener();
   }
   
   public void nonNormActionListener() {
     nonNormSelector.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent ae) {
         
         JComboBox option = (JComboBox) ae.getSource();
         
         String selected = (String) option.getSelectedItem();
         
         if(selected.equals("Stop/Start Conveyor (0-14)")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           final JSlider conveyorSlider = new JSlider(0, 14);
           JButton turnOffConveyor = new JButton("Turn off");
           JButton turnOnConveyor = new JButton("Turn on");
           turnOffConveyor.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               turnOffConveyor(conveyorSlider.getValue());
             }
 
           });
           
           turnOnConveyor.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               turnOnConveyor(conveyorSlider.getValue());
             }
 
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("1"));
           table.put(2, new JLabel("2"));
           table.put(3, new JLabel("3"));
           table.put(4, new JLabel("4"));
           table.put(5, new JLabel("5"));
           table.put(6, new JLabel("6"));
           table.put(7, new JLabel("7"));
           table.put(8, new JLabel("8"));
           table.put(9, new JLabel("9"));
           table.put(10, new JLabel("10"));
           table.put(11, new JLabel("11"));
           table.put(12, new JLabel("12"));
           table.put(13, new JLabel("13"));
           table.put(14, new JLabel("14"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMinorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setPaintTicks(true);
           conveyorSlider.setSnapToTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 1;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Conveyor Numbers: 0-14"));
           gbc.gridy = 1;
           gbc.fill = 2;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           gbc.gridy = 0;
           bottomPanel.add(turnOffConveyor, gbc);
           gbc.gridy++;
           bottomPanel.add(turnOnConveyor, gbc);
           repaint();
           revalidate();
           
           
         }
         else if(selected.equals("Inline Workstation Break/Restart")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           final JSlider conveyorSlider = new JSlider(0, 6);
           JButton breakWorkstation = new JButton("Break");
           JButton fixWorkstation = new JButton("Fix");
           
           breakWorkstation.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               if(conveyorSlider.getValue() == 0) {
                 breakWorkstation(0);
               }
               else if(conveyorSlider.getValue() == 1) {
                 breakWorkstation(2);
               }
               else if(conveyorSlider.getValue() == 2) {
                 breakWorkstation(3);
               }
               else if(conveyorSlider.getValue() == 3) {
                 breakWorkstation(8);
               }
               else if(conveyorSlider.getValue() == 4) {
                 breakWorkstation(10);
               }
               else if(conveyorSlider.getValue() == 5) {
                 breakWorkstation(11);
               }
               else if(conveyorSlider.getValue() == 6) {
                 breakWorkstation(13);
               }
             }
           });
           fixWorkstation.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               if(conveyorSlider.getValue() == 0) {
                 fixWorkstation(0);
               }
               else if(conveyorSlider.getValue() == 1) {
                 fixWorkstation(2);
               }
               else if(conveyorSlider.getValue() == 2) {
                 fixWorkstation(3);
               }
               else if(conveyorSlider.getValue() == 3) {
                 fixWorkstation(8);
               }
               else if(conveyorSlider.getValue() == 4) {
                 fixWorkstation(10);
               }
               else if(conveyorSlider.getValue() == 5) {
                 fixWorkstation(11);
               }
               else if(conveyorSlider.getValue() == 6) {
                 fixWorkstation(13);
               }
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("2"));
           table.put(2, new JLabel("3"));
           table.put(3, new JLabel("8"));
           table.put(4, new JLabel("10"));
           table.put(5, new JLabel("11"));
           table.put(6, new JLabel("13"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Inline Conveyor Numbers:"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 50;
           gbc.gridx++;
           gbc.gridy = 0;
           
           JPanel workstationPanel = new JPanel();
           gbc.fill = GridBagConstraints.HORIZONTAL;
           gbc.ipadx = 50;
           bottomPanel.add(breakWorkstation, gbc);
           gbc.gridy++;
           bottomPanel.add(fixWorkstation, gbc);
           /*bottomPanel.add(breakWorkstation, gbc);
           gbc.gridy++;
           gbc.ipadx = 23;
           bottomPanel.add(fixWorkstation, gbc);*/
           
           repaint();
           revalidate();
         }
         else if(selected.equals("Workstation Broken/Fixed")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           JSlider conveyorSlider = new JSlider(0, 6);
           JButton accept = new JButton("Accept");
           accept.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("2"));
           table.put(2, new JLabel("3"));
           table.put(3, new JLabel("8"));
           table.put(4, new JLabel("10"));
           table.put(5, new JLabel("11"));
           table.put(6, new JLabel("13"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Workstation Conveyor Numbers:"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           bottomPanel.add(accept, gbc);
           
           repaint();
           revalidate();
         }
         else if(selected.equals("Popup Jam/Unjam")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           JSlider conveyorSlider = new JSlider(0, 2);
           JButton accept = new JButton("Accept");
           accept.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("1"));
           table.put(2, new JLabel("2"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Popup Index:"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           bottomPanel.add(accept, gbc);
           
           repaint();
           revalidate();
         }
         else if(selected.equals("Offline Workstation Change Speed")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           JSlider conveyorSlider = new JSlider(0, 2);
           JButton accept = new JButton("Accept");
           accept.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("1"));
           table.put(2, new JLabel("2"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Popup Index"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           bottomPanel.add(accept, gbc);
           
           repaint();
           revalidate();
         }
         else if(selected.equals("Disable/Enable Offline Workstation")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           JSlider conveyorSlider = new JSlider(0, 5);
           JButton accept = new JButton("Accept");
           accept.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("1"));
           table.put(2, new JLabel("2"));
           table.put(3, new JLabel("3"));
           table.put(4, new JLabel("4"));
           table.put(5, new JLabel("5"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Offline Workstation Index Index:"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           bottomPanel.add(accept, gbc);
           
           repaint();
           revalidate();
         }
         else if(selected.equals("Truck Breaks Down/Fixed")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
         }
         else if(selected.equals("Glass Breaking Offline")) {
           bottomPanel.removeAll();
           bottomPanel.setLayout(new GridBagLayout());
           GridBagConstraints gbc = new GridBagConstraints();
           
           JSlider conveyorSlider = new JSlider(0, 5);
           JButton accept = new JButton("Accept");
           accept.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
               
             }
           });
           
           Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
           table.put(0, new JLabel("0"));
           table.put(1, new JLabel("1"));
           table.put(2, new JLabel("2"));
           table.put(3, new JLabel("3"));
           table.put(4, new JLabel("4"));
           table.put(5, new JLabel("5"));
           
           conveyorSlider.setLabelTable(table);
           conveyorSlider.setMajorTickSpacing(1);
           conveyorSlider.setPaintLabels(true);
           conveyorSlider.setSnapToTicks(true);
           conveyorSlider.setPaintTicks(true);
           
           gbc.anchor = GridBagConstraints.NORTHWEST;
           gbc.weightx = 0;
           gbc.weighty = 5;
           gbc.gridx = 0;
           gbc.gridy = 0;
           bottomPanel.add(new JLabel("Valid Offline Workstation Index:"));
           gbc.gridy++;
           bottomPanel.add(conveyorSlider, gbc);
           gbc.ipadx = 5;
           gbc.gridx++;
           bottomPanel.add(accept, gbc);
           
           repaint();
           revalidate();
         }
         
       }
     } );
   }
 
   public void turnOffConveyor(int index) {
     
   }
   public void turnOnConveyor(int index) {
     
   }
   public void breakWorkstation(int index) {
     
   }
   public void fixWorkstation(int index) {
     
   }
   @Override
   public void actionPerformed(ActionEvent ae) {
     // TODO Auto-generated method stub
 
     JComboBox option = (JComboBox) ae.getSource();
     String selected = (String) option.getSelectedItem();
     
     if(selected.equals("Stop/Start Conveyor (0-14)")) {
       
     }
     else if(selected.equals("Inline Workstation Break/Restart")) {
       
     }
     //TODO- FINISH
   }
 
 }
