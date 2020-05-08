 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Date;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 public class UserInterface
 	{
 
 		public JList accountJList;
 		public JList meterJList;
 		final JFrame mainFrame;
 		public UserInterface()
 		{	
 			mainFrame = new JFrame();
 			mainFrame.setSize(1000, 500);
 			//mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 			JMenuBar menuBar = new JMenuBar();
 			menuBar.add(new JMenuItem("File"));
 			mainFrame.setJMenuBar(menuBar);
 			
 
			accountJList = new JList();
			meterJList = new JList();
 			
 			JScrollPane accountScrollPane = new JScrollPane(accountJList);
 			accountScrollPane.setPreferredSize(new Dimension(200, 100));
 			JScrollPane meterScrollPane = new JScrollPane(meterJList);
 			meterScrollPane.setPreferredSize(new Dimension(200, 100));
 			
 			JPanel input = new JPanel();
 			JButton addMeter = new JButton("Enter Meter Information");
 			JButton addAccount = new JButton("New Residential Account");
 			JButton save = new JButton("Save");
 			
 			save.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e){	
 					Account_IO.AccountOut(Controller.getInstance().getAccounts());
 				}
 			});
 					
 			addMeter.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e){	
 					
 					
 					JTextField meterIDField = new JTextField(5);
 				    JTextField meterTypeField = new JTextField(12);
 				    Object[] options = {"SAVE", "CANCEL"};
 
 				      JPanel myPanel = new JPanel();
 				      myPanel.add(new JLabel("Meter ID"));
 				      myPanel.add(meterIDField);
 				      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
 				      myPanel.add(new JLabel("Meter Type"));
 				      myPanel.add(meterTypeField);
 				      
 
 				      int result = JOptionPane.showOptionDialog(null, myPanel, 
 				               "Enter information for a meter reading", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 				      if (result == 0) 
 				      {
 				    	  Meter temp = new Meter(Integer.parseInt(meterIDField.getText()), meterTypeField.getText());
 				    	  Controller.getInstance().addMeter(temp);
 				    	  refreshMeterJList();
 				      }
 				}
 			});
 			
 			addAccount.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e){	
 					JTextField firstNameField = new JTextField(10);
 				    JTextField lastNameField = new JTextField(10);
 				    JTextField accountIDField = new JTextField(10);
 				    JTextField line1Field = new JTextField(10);
 				    JTextField line2Field = new JTextField(10);
 				    JTextField cityField = new JTextField(10);
 				    JTextField stateField = new JTextField(10);
 				    JTextField zipField = new JTextField(10);
 				    
 				    Object[] options = {"SAVE", "CANCEL"};
 
 				      JPanel myPanel = new JPanel();
 				      myPanel.add(new JLabel("First Name"));
 				      myPanel.add(firstNameField);
 				      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
 				      myPanel.add(new JLabel("Last Name"));
 				      myPanel.add(lastNameField);
 				      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
 				      myPanel.add(new JLabel("Account ID"));
 				      myPanel.add(accountIDField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Address Line 1"));
 				      myPanel.add(line1Field);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Address Line 2"));
 				      myPanel.add(line2Field);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("City"));
 				      myPanel.add(cityField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("State"));
 				      myPanel.add(stateField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Zip"));
 				      myPanel.add(zipField);
 
 				      int result = JOptionPane.showOptionDialog(null, myPanel, 
 				               "Enter information for the new residential account", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 				      if (result == 0) 
 				      {
 				    	 Account temp = new ResidentialAccount(firstNameField.getText(), 
 				    			 								lastNameField.getText(), 
 				    			 								Integer.parseInt(accountIDField.getText()), 0, false,
 				    			 								new Date(), 
 				    			 								new Address(line1Field.getText(), line2Field.getText(), cityField.getText(), zipField.getText(), stateField.getText()));
 					    	 Controller.getInstance().addAccount(temp);
 					    	 /* TODO Error checking for duplicate accounts */
 					    	 refreshAccountJList();
 				         System.out.println("saved!");
 				      }
 				}
 			});
 			
 			input.add(addMeter);
 			input.add(addAccount);
 			input.add(save);
 			mainFrame.add(accountScrollPane, BorderLayout.WEST);
 			mainFrame.add(input, BorderLayout.NORTH);
 			mainFrame.add(meterScrollPane, BorderLayout.EAST);
 			
 			mainFrame.setVisible(true);
 		}
 		
 		public void refreshAccountJList() {
 			//System.out.println(Arrays.toString((Controller.getInstance().accounts.toArray()));
 			accountJList.setListData(Controller.getInstance().getAccounts().toArray());
 			//mainFrame.repaint();
 			
 		}
 		
 		public void refreshMeterJList() {
 			//System.out.println(Arrays.toString(Controller.getInstance().accounts.toArray()));
 			meterJList.setListData(Controller.getInstance().getMeters().toArray());
 			//mainFrame.repaint();
 			
 		}
 }
