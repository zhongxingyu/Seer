 package edu.segal.bradbox;
 
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 public class ContactPanel extends PanelSkeleton {
 	private static final long serialVersionUID = 1L;
 	// Add contact fields
 	JCheckBox favCheckbox = new JCheckBox("Add as favorite");
 	JLabel nameLabel = new JLabel("Full Name");
 	JTextField nameField = new JTextField();
 	JLabel phoneNumberLabel = new JLabel("Phone Number");
 	JTextField numberField = new JTextField();
 	JButton saveButton = new JButton("Save");
 	JButton deleteButton = new JButton("Delete");
 	String oldName;
 	boolean fav = false;
 	boolean editContact;
 	int rank;
 	
 	ContactPanel(SuperFrame sf) {
 		super(sf, "Add Contact");
 		editContact = false;
 		initLayout();
 	}
 	
 	ContactPanel(SuperFrame sf, String nm, String no, boolean f, int r) {
 		super(sf, "Edit Contact");
 		oldName = nm;
 		fav = f;
 		rank = r;
 		
 		nameField.setText(oldName);
 		numberField.setText(no);
 		if(fav) favCheckbox.setSelected(true);
 		
 		editContact = true;
 		initLayout();
 		
 		deleteButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				
 				SwingUtilities.invokeLater(new Runnable() {
 			        public void run() {
 			            ConfirmDeleteFrame cdf = new ConfirmDeleteFrame(superframe, oldName, fav, rank);
 			            cdf.setVisible(true);
 			        }
 				});
 			}
 		});
 
 	}
 	
 	void initLayout () {
 		JPanel content = new JPanel();
 		addContent(content);
 
 		content.setLayout(new GridLayout(0, 1));
 		content.add(nameLabel);
 		content.add(nameField);
 		content.add(phoneNumberLabel);
 		content.add(numberField);
 		content.add(favCheckbox);
 		if(editContact) {
 			content.add(deleteButton);
 			content.add(saveButton);
 		} else {
 			content.add(saveButton);
 		}
 		nameLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 26));
 		nameField.setFont(Constants.BIG_FONT);
 		nameField.setColumns(10);
 		phoneNumberLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 26));
 		numberField.setFont(new Font("Lucida Grande", Font.PLAIN, 32));
 		numberField.setColumns(10);
 		saveButton.setFont(new Font("Lucida Grande", Font.BOLD, 28));
 		
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				if(editContact) monkey.shell("am broadcast -a edu.segal.androidbradbox.deletecontact -e name '" + oldName + "'");
				monkey.shell("am broadcast -a edu.segal.androidbradbox.addcontact -e name '" + nameField.getText() + "' -e number '"+ numberField.getText() + "'");
 				System.out.println("attempting to copy contacts");
 				try {
 					Runtime.getRuntime().exec("/platform-tools/copycontacts.exe");
 				} catch (IOException e) {
 					System.out.println("Error: IOException when calling copycontacts.exe");
 					e.printStackTrace();
 				}
 				
 				if(fav) {
 					superframe.updateFavorite(nameField.getText(), Integer.toString(rank));
 				} else if(favCheckbox.isSelected()) {
 					SwingUtilities.invokeLater(new Runnable() {
 				        public void run() {
				            EditFavoriteFrame ff = new EditFavoriteFrame(superframe, nameField.getText());
 				            ff.setVisible(true);
 				        }
 					});
 				}
 				nameField.setText("");
 				numberField.setText("");
 				superframe.showKeypad();
 
 			}
 		});
 		
 	}
 }
 
