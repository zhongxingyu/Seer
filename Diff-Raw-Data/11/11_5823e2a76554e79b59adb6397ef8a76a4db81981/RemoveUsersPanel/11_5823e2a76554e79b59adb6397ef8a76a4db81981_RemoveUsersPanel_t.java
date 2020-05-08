 package GUI;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import main.BlasterCardListener;
 import main.SystemAdministrator;
 import main.User;
 import main.UserComparator;
 
 import com.mongodb.DBObject;
 
 public class RemoveUsersPanel extends ContentPanel {
 
 	private JButton removeButton;
 	private ButtonListener buttonListener;
 	private JButton nameSearchGoButton;
 	private JButton idSearchGoButton;
 	private JTextField nameSearchField;
 	private JTextField idSearchField;
 	private JPanel resultsPanel;
 	private JScrollPane scroller;
 
 	// Holds the list of users to potentially be deleted (searched by the admin)
 	private ArrayList<User> resultsList; 
 
 	public RemoveUsersPanel() {
 
 		super("Remove Users");
 		buttonListener = new ButtonListener();
 		resultsList = new ArrayList<User>();
 
 		JLabel nameSearchLabel = new JLabel("Search By Name:");
 		JLabel idSearchLabel = new JLabel("Search By CWID:");
 
 		nameSearchField = new JTextField();
 		idSearchField = new JTextField();
 
 		nameSearchField.setText("Search All");
 		idSearchField.setText("Search All");
 
 		nameSearchField.addActionListener(buttonListener);
 		idSearchField.addActionListener(buttonListener);
 
 		JPanel nameSearchPanel = new JPanel(new GridLayout(1, 3));
 		JPanel idSearchPanel = new JPanel(new GridLayout(1, 3));
 
 		JPanel dataPanel = new JPanel(new GridBagLayout());
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.5;
 		c.gridx = 0;
 		c.gridy = 0;
 		dataPanel.add(nameSearchPanel, c);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.5;
 		c.gridx = 0;
 		c.gridy = 1;
 		dataPanel.add(idSearchPanel, c);
 
 		nameSearchGoButton = new JButton("Go");
 		idSearchGoButton = new JButton("Go");
 
 		nameSearchGoButton.addActionListener(buttonListener);
 		idSearchGoButton.addActionListener(buttonListener);
 
 		nameSearchLabel.setFont(buttonFont);
 		idSearchLabel.setFont(buttonFont);
 		nameSearchField.setFont(textFont);
 		idSearchField.setFont(textFont);
 		nameSearchGoButton.setFont(buttonFont);
 		idSearchGoButton.setFont(buttonFont);
 
 		nameSearchPanel.add(nameSearchLabel);
 		nameSearchPanel.add(nameSearchField);
 		nameSearchPanel.add(nameSearchGoButton);
 
 		idSearchPanel.add(idSearchLabel);
 		idSearchPanel.add(idSearchField);
 		idSearchPanel.add(idSearchGoButton);
 
 		resultsPanel = new JPanel(new GridLayout(0, 1));
 
 		scroller = new JScrollPane(resultsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);		
 		scroller.setPreferredSize(new Dimension(scroller.getWidth(), scroller.getHeight()));
 		scroller.setMaximumSize(scroller.getPreferredSize());
 		scroller.getVerticalScrollBar().setUnitIncrement(13);
 
 		TitledBorder border = new TitledBorder("Search Results");
 		border.setTitleFont(borderFont);
 		scroller.setBorder(border);
 
 		removeButton = new JButton("Remove Users");
 		removeButton.setFont(buttonFont);
 		removeButton.addActionListener(buttonListener);
 
 		/////////////////////////////////////////////////////////////////////////////////////////////////
 		/******************** All weighty values should add up to 0.9 ***********************************
 		 ******************** All weightx values should add up to 0.8 **********************************/
 		/////////////////////////////////////////////////////////////////////////////////////////////////
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.2;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(dataPanel, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.5;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(scroller, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		add(removeButton, c);
 
 		c.weighty = 0.1;
 		c.gridy = 4;
 		add(new JPanel(), c);
 
 	}
 
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == removeButton) {
 				boolean noBoxesChecked = true;
 				for ( int i = 0; i < resultsPanel.getComponentCount(); ++i ) {
 					JCheckBox cb = (JCheckBox) resultsPanel.getComponent(i);
 					if ( cb.isSelected() ) {
 						noBoxesChecked = false;
 						break;
 					}
 				}
 				ArrayList<String> removed = new ArrayList<String>();
 				ArrayList<User> usersInQuestion = new ArrayList<User>();
 
 				String question = "Are you sure you want to remove these users from the database?"
 						+ "\nThis action is permanent and cannot be undone.";
 
 				// First check that they actually want to remove the users.
 				if ( !noBoxesChecked && confirm(question)) {
 					ArrayList<JCheckBox> removedBoxes = new ArrayList<JCheckBox>();
 					System.out.println(Driver.getAccessTracker().getCurrentUser());
 					SystemAdministrator admin = (SystemAdministrator) Driver.getAccessTracker().getCurrentUser();
 					boolean duplicates = false;
 
 					for ( int i = 0; i < resultsPanel.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) resultsPanel.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
							System.out.println(s);
 							for ( User u : resultsList ) {
								if ( s.equals(u.getFirstName() + " " + u.getLastName() + " [" + u.getDepartment() + "]")) {
 									String CWID = u.getCWID();
 									removed.add(s);
 									removedBoxes.add(cb);
 									usersInQuestion.add(u);
 								}
 							}
 						}
 					}
 
 					for (String r : removed) {
						System.out.println(r);
 						int dupCounter = 0;
 						for (User u : resultsList) {
 							if ((u.getFirstName() + " " + u.getLastName() + " [" + u.getDepartment() + "]").equals(r) ) {
								System.out.println(u.getFirstName() + " " + u.getLastName() + " [" + u.getDepartment() + "]");
 								dupCounter++;
 								if (dupCounter > 1) {
									showMessage("There are multiple users with the same name and same department.\nPlease search by CWID to find the user you want to remove.");
 									duplicates = true;
 									break;
 								}
 							}
 						}
 						
 						if (duplicates)
 							break;
 					}
 
 					if (!duplicates) {
 						for (User u : usersInQuestion) {
 							resultsList.remove(u);
 							admin.removeUser(u.getCWID());
 						}
 
 						for ( JCheckBox cb : removedBoxes ) {
 							resultsPanel.remove(cb);
 						}
 						
 						// Lists all the users that are removed.
 						String message = "You Removed:\n\n";
 						for ( String s : removed ) {
 							message += s + "\n";
 						}
 						if ( !removed.isEmpty() ) {
 							showMessage(message);
 						}
 					}
 
 					repaint();
 					duplicates = false;
 				}
 			} else if (e.getSource() == nameSearchGoButton || e.getSource() == idSearchGoButton |
 					e.getSource() == nameSearchField || e.getSource() == idSearchField ) {
 
 				resultsPanel.removeAll();
 				resultsList.clear();
 				repaint();
 				ArrayList<DBObject> userList = new ArrayList<DBObject>();
 
 				if ( e.getSource() == nameSearchGoButton || e.getSource() == nameSearchField ) {
 
 					if ( nameSearchField.getText().equals("Search All") || nameSearchField.getText().equals(""))
 						userList = Driver.getAccessTracker().searchDatabase("Users", "CWID", "");
 					else						
 						userList = Driver.getAccessTracker().searchDatabaseForUser(nameSearchField.getText());
 
 				} else {
 					if ( idSearchField.getText().equals("Search All") || idSearchField.getText().equals(""))
 						userList = Driver.getAccessTracker().searchDatabase("Users", "CWID", "");
 					else {
 						String input = idSearchField.getText();
 						if ( input.length() > 7)
 							input = BlasterCardListener.strip(input);
 						userList = Driver.getAccessTracker().searchDatabase("Users", "CWID", input);
 					}
 					idSearchField.setText("");
 					nameSearchField.setText("");
 				}
 
 				for ( DBObject u : userList ) {
 					User user = new User( (String) u.get("firstName"), (String) u.get("lastName"), (String) u.get("CWID"), (String) u.get("email"), (String) u.get("department"));
 					resultsList.add(user);
 				}
 
 				// sorts the resultslist
 				Collections.sort(resultsList, new UserComparator());
 				for ( User u : resultsList ) {
 					JCheckBox cb = new JCheckBox(u.getFirstName() + " " + u.getLastName() + " [" + u.getDepartment() + "]");
 					cb.setHorizontalAlignment(JCheckBox.LEFT);
 					cb.setFont(buttonFont);
 					resultsPanel.add(cb);
 				}				
 			}
 		}
 	}
 }
