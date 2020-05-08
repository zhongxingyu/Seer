 package GUI;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import main.BlasterCardListener;
 import main.Machine;
 import main.Tool;
 import main.User;
 import main.Validator;
 
 public class LogInAnotherUserPanel extends ContentPanel {
 
 	private JButton saveButton;
 	private JButton goButton;
 	private JButton logOutUser;
 	private ButtonListener buttonListener;
 	private JTextField cwidField;
 
 	private JScrollPane scroller1;
 	private JScrollPane scroller2;
 	private JScrollPane scroller3;
 	private JPanel selectionPanel;
 	private JPanel machines;
 	private JPanel availableTools;
 	private JPanel checkedOutTools;
 	private User user;
 	private User current;
 
 	public LogInAnotherUserPanel() {
 		// All the fonts are in ContentPanel.
 		super("Sign In Another User");
 
 		current = Driver.getAccessTracker().getCurrentUser();
 
 		buttonListener = new ButtonListener();
 		
 		selectionPanel = new JPanel(new GridLayout(1, 3));
 
 		JLabel cwidLabel = new JLabel("Enter CWID:");
 		cwidField = new JTextField();
 
 		cwidField.setFont(textFont);
 		cwidField.addActionListener(buttonListener);
 
 		cwidLabel.setFont(borderFont);
 
 		goButton = new JButton("Go");
 		goButton.setFont(buttonFont);
 		goButton.addActionListener(buttonListener);
 
 		machines = new JPanel();
 		availableTools = new JPanel();
 		checkedOutTools = new JPanel();
 
 		machines.setLayout(new GridLayout(0, 1));
 		availableTools.setLayout(new GridLayout(0, 1));
 		checkedOutTools.setLayout(new GridLayout(0, 1));
 
 		scroller1 = new JScrollPane(machines, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scroller2 = new JScrollPane(availableTools, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scroller3 = new JScrollPane(checkedOutTools, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		TitledBorder border1 = new TitledBorder("Select Machines");
 		TitledBorder border2 = new TitledBorder("Check Out Tools");
 		TitledBorder border3 = new TitledBorder("Return Tools");
 		
 		border1.setTitleFont(borderFont);
 		border2.setTitleFont(borderFont);
 		border3.setTitleFont(borderFont);
 		
 		scroller1.setBorder(border1);
 		scroller2.setBorder(border2);
 		scroller3.setBorder(border3);
 		
 		selectionPanel.add(scroller1);
 		selectionPanel.add(scroller2);
 		selectionPanel.add(scroller3);
 		
 		logOutUser = new JButton("Sign Out User");
 		logOutUser.setFont(buttonFont);
 		logOutUser.addActionListener(buttonListener);
 
 		JPanel cwidPanel = new JPanel(new GridLayout(1, 3));
 
 		cwidPanel.add(cwidLabel);
 		cwidPanel.add(cwidField);
 		cwidPanel.add(goButton);
 
 		saveButton = new JButton("Save");
 		saveButton.setFont(buttonFont);
 		saveButton.addActionListener(buttonListener);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.6;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(cwidPanel, c);
 
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.0;
 		c.weighty = 0.1;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(logOutUser, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 0.3;
 		c.weighty = 0.5;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 3;
 		add(selectionPanel, c);
 	
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 0.0;
 		c.weighty = 0.1;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 4;
 		add(saveButton, c);
 
 		c.weighty = 0.1;
 		c.gridy = 5;
 		add(new JPanel(), c);
 	}
 
 	public void showMessage(String message) {
 		JOptionPane.showMessageDialog(this, message);
 	}
 
 	private void showMachines() {
 		machines.removeAll();
 		for ( Machine m : user.getCertifiedMachines() ) {
 			JCheckBox cb = new JCheckBox(m.getName() + " [" + m.getID() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(checkBoxFont);
 			machines.add(cb);
 			if (user.getCurrentEntry().getMachinesUsed().contains(m)){
 				cb.setEnabled(false);
 			}
 		}
 	}
 
 	private void showaAvailableTools() {
 		availableTools.removeAll();
 		for ( Tool t : Driver.getAccessTracker().getTools()) {
 			JCheckBox cb = new JCheckBox(t.getName() + " [" + t.getUPC() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(checkBoxFont);
 			if (!user.getToolsCheckedOut().contains(t))
 				availableTools.add(cb);
 			if (t.isCheckedOut()) {
 				cb.setEnabled(false);
 			}
 		}
 	}
 
 	private void showCheckedOutTools() {
 		checkedOutTools.removeAll();
 		for ( Tool t : user.getToolsCheckedOut()) {
 			JCheckBox cb = new JCheckBox(t.getName() + " [" + t.getUPC() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(checkBoxFont);
 			checkedOutTools.add(cb);
 		}
 	}
 
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if ( e.getSource() == saveButton) {
 				if ( user == null ) {
 					showMessage("Please enter the user's CWID.");
 				} else {
 					ArrayList<Machine> machinesSelected = new ArrayList<Machine>();
 
 					for ( int i = 0; i < machines.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) machines.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Machine m : Driver.getAccessTracker().getMachines() ) {
 								String ID = m.getID();
 								if ( s.equals(ID) ) {
 									machinesSelected.add(m);
 								}
 							}
 						}
 					}
 
 					ArrayList<Tool> availableToolsSelected = new ArrayList<Tool>();
 
 					for ( int i = 0; i < availableTools.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) availableTools.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Tool t : Driver.getAccessTracker().getTools() ) {
 								String UPC = t.getUPC();
 								if ( s.equals(UPC) ) {
 									availableToolsSelected.add(t);
 								}
 							}
 						}
 					}
 
 					ArrayList<Tool> checkedOutToolsSelected = new ArrayList<Tool>();
 
 					for ( int i = 0; i < checkedOutTools.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) checkedOutTools.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Tool t : Driver.getAccessTracker().getTools() ) {
 								String UPC = t.getUPC();
 								if ( s.equals(UPC) ) {
 									checkedOutToolsSelected.add(t);
 								}
 							}
 						}
 					}
 
 					for (Machine m : machinesSelected) {
 						user.useMachine(m);
 					}
 					user.getCurrentEntry().addMachinesUsed(machinesSelected);
 					for (Tool t : availableToolsSelected) {
 						user.checkoutTool(t);
 					}
 					user.getCurrentEntry().addToolsCheckedOut(availableToolsSelected);
 					if (checkedOutToolsSelected.size() > 0){
 						user.returnTools(checkedOutToolsSelected);
 						user.getCurrentEntry().addToolsReturned(checkedOutToolsSelected);
 					}
 				}
 
 				clearFields();
 
 			} else if ( e.getSource() == goButton || e.getSource() == cwidField ) {
 				if (cwidField.getText().equals("")) {
 					showMessage("Please enter 8-digit CWID, numbers only.");
					clearFields();
 				} else {
 					String input = BlasterCardListener.strip(cwidField.getText());
 					if (!Validator.isValidCWID(input)) {
 						return;
 					}
 					if (!input.equals(current.getCWID())) {
 						user = Driver.getAccessTracker().processLogIn(input);
 						Driver.getAccessTracker().setCurrentUser(current);
 
 						System.out.println(user);
 						cwidField.setText(user.getFirstName() + " " + user.getLastName() + " [" + user.getDepartment() + "]");
 
 
 						showMachines();
 						showaAvailableTools();
 						showCheckedOutTools();
 					} else {
 						cwidField.setText("");
 						showMessage("You can't sign yourself in again. Sorry");
 					}
 				}
 			} else if ( e.getSource() == logOutUser) {
 				if (user != null)  {
 					Driver.getAccessTracker().processLogOut(user.getCWID());
 					clearFields();
 				}
 			}
 		}
 	}
 
 	// Clears all the text fields to empty, and set the user null.
 	private void clearFields() {
 		cwidField.setText("");
 		user = null;
 		machines.removeAll();
 		availableTools.removeAll();
 		checkedOutTools.removeAll();
 		Driver.getAccessTracker().setCurrentUser(current);
 	}
 }
