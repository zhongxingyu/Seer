 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package puf.m2.hms.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import puf.m2.hms.exception.UserException;
 import puf.m2.hms.model.User;
 
 /**
  * 
  * @author NHPhat
  */
 public class ManageUser extends javax.swing.JPanel implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	// Variables declaration - do not modify
 
 	private javax.swing.JButton btnDelete;
 	private javax.swing.JButton btnFind;
 	private javax.swing.JButton btnInsert;
 	private javax.swing.JButton btnUpdate;
 	private javax.swing.JComboBox cboRole;
 	private javax.swing.JLabel lblEmail;
 	private javax.swing.JLabel lblPassword;
 	private javax.swing.JLabel lblRole;
 	private javax.swing.JLabel lblUsername;
 	private javax.swing.JTextField txtEmail;
 	private javax.swing.JPasswordField txtPassword;
 
 	private javax.swing.JTextField txtUsername;
 
 	/**
 	 * Creates new form ManageUser
 	 */
 	public ManageUser() {
 		initComponents();
 		addActionListener();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String username = txtUsername.getText();
 		@SuppressWarnings("deprecation")
 		String password = txtPassword.getText();
		String email = txtEmail.getText();

 		String checkValidFields = checkValidFields();
 
 		if ("Insert".equals(e.getActionCommand())) {
 
 			if (checkValidFields() != "True") {
 				JOptionPane.showMessageDialog(this, checkValidFields, "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			if (isDuplicateUsername(username)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"This user can not be insert, because it is exists in database",
 								"Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else {
 				// save new user
				String role = cboRole.getSelectedItem().toString();
				User user = new User(username, password, email, role);
 				try {
 					user.save();
 					JOptionPane.showMessageDialog(this, "Save successful",
 							"Success", JOptionPane.INFORMATION_MESSAGE);
 				} catch (UserException e1) {
 					JOptionPane.showMessageDialog(this, "Can not save user",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		} else if ("Find".equals(e.getActionCommand())) {
 			username = JOptionPane.showInputDialog("Please type username: ");
 			User user = User.getUserByName(username);
 			if (user != null) {
 				txtUsername.setText(user.getUsername());
 				txtPassword.setText(user.getPassword());
 				txtEmail.setText(user.getUseremail());
 				String sa = user.getRole();
 				if ("Admin".equals(user.getRole()))
 					cboRole.setSelectedIndex(0);
 				else if ("Doctor".equals(user.getRole()))
 					cboRole.setSelectedIndex(1);
 				else if ("Nurse".equals(user.getRole()))
 					cboRole.setSelectedIndex(2);
 				else if ("Receptionist".equals(user.getRole()))
 					cboRole.setSelectedIndex(3);
 			} else {
 				JOptionPane.showMessageDialog(this, "User name " + username
 						+ " does not exists", "Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 
 		} else if ("Update".equals(e.getActionCommand())) {
 			if (checkValidFields() != "True") {
 				JOptionPane.showMessageDialog(this, checkValidFields, "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			User user = User.getUserByName(username);
 			if (user != null) {
 				try {
 					user.update();
 					JOptionPane.showMessageDialog(this, "Update successful",
 							"Success", JOptionPane.INFORMATION_MESSAGE);
 				} catch (UserException e1) {
 					JOptionPane.showMessageDialog(this, "Can not update user",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}
 			} else {
 				JOptionPane.showMessageDialog(this, "User name " + username
 						+ " does not exists", "Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		} else if ("Delete".equals(e.getActionCommand())) {
 			if (checkValidFields() != "True") {
 				JOptionPane.showMessageDialog(this, checkValidFields, "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			if (JOptionPane.showConfirmDialog(this,
 					"Do you want delete this user?", "Alert",
 					JOptionPane.OK_CANCEL_OPTION) == 0) { // click OK
 				// delete user
				String role = cboRole.getSelectedItem().toString();
				User user = new User(username, password, email, role);
				user.setDeleted(true);
 				try {
 					user.update();
 					JOptionPane.showMessageDialog(this, "Delete successful",
 							"Success", JOptionPane.INFORMATION_MESSAGE);
 				} catch (UserException e1) {
 					JOptionPane.showMessageDialog(this, "Can not delete user",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 
 	}
 
 	private void addActionListener() {
 		btnInsert.setActionCommand("Insert");
 		btnInsert.addActionListener(this);
 
 		btnFind.setActionCommand("Find");
 		btnFind.addActionListener(this);
 
 		btnUpdate.setActionCommand("Update");
 		btnUpdate.addActionListener(this);
 
 		btnDelete.setActionCommand("Delete");
 		btnDelete.addActionListener(this);
 	}
 
 	public javax.swing.JTextField getTxtEmail() {
 		return txtEmail;
 	}
 
 	public javax.swing.JPasswordField getTxtPassword() {
 		return txtPassword;
 	}
 
 	public javax.swing.JTextField getTxtUsername() {
 		return txtUsername;
 	}
 
 	// End of variables declaration
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	@SuppressWarnings({ "unchecked", "deprecation" })
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">
 	private void initComponents() {
 
 		txtUsername = new javax.swing.JTextField();
 		lblUsername = new javax.swing.JLabel();
 		lblPassword = new javax.swing.JLabel();
 		lblEmail = new javax.swing.JLabel();
 		lblRole = new javax.swing.JLabel();
 		txtPassword = new javax.swing.JPasswordField();
 		txtEmail = new javax.swing.JTextField();
 		cboRole = new javax.swing.JComboBox();
 		btnInsert = new javax.swing.JButton();
 		btnFind = new javax.swing.JButton();
 		btnUpdate = new javax.swing.JButton();
 		btnDelete = new javax.swing.JButton();
 
 		txtUsername.setNextFocusableComponent(txtPassword);
 
 		lblUsername.setText("Username");
 		lblUsername.setName("lblUsername");
 
 		lblPassword.setText("Password");
 
 		lblEmail.setText("Email");
 
 		lblRole.setText("Role");
 
 		txtPassword.setText("");
 		txtPassword.setNextFocusableComponent(txtEmail);
 
 		txtEmail.setNextFocusableComponent(cboRole);
 
 		cboRole.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
 				"Admin", "Doctor", "Nurse", "Receptionist" }));
 		cboRole.setNextFocusableComponent(btnInsert);
 
 		btnInsert.setText("Insert");
 		btnInsert.setNextFocusableComponent(btnFind);
 
 		btnFind.setText("Find");
 		btnFind.setNextFocusableComponent(btnUpdate);
 
 		btnUpdate.setText("Update");
 		btnUpdate.setNextFocusableComponent(btnDelete);
 
 		btnDelete.setText("Delete");
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(layout
 				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.LEADING,
 												false)
 												.addGroup(
 														layout.createSequentialGroup()
 																.addContainerGap()
 																.addGroup(
 																		layout.createParallelGroup(
 																				javax.swing.GroupLayout.Alignment.LEADING)
 																				.addComponent(
 																						lblUsername)
 																				.addComponent(
 																						lblPassword))
 																.addGap(18, 18,
 																		18)
 																.addGroup(
 																		layout.createParallelGroup(
 																				javax.swing.GroupLayout.Alignment.LEADING)
 																				.addComponent(
 																						txtPassword)
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addComponent(
 																										txtUsername,
 																										javax.swing.GroupLayout.PREFERRED_SIZE,
 																										187,
 																										javax.swing.GroupLayout.PREFERRED_SIZE)
 																								.addGap(0,
 																										0,
 																										Short.MAX_VALUE))))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addGroup(
 																		layout.createParallelGroup(
 																				javax.swing.GroupLayout.Alignment.LEADING)
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addContainerGap()
 																								.addComponent(
 																										lblEmail))
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addGap(10,
 																										10,
 																										10)
 																								.addComponent(
 																										lblRole)))
 																.addGap(42, 42,
 																		42)
 																.addGroup(
 																		layout.createParallelGroup(
 																				javax.swing.GroupLayout.Alignment.LEADING,
 																				false)
 																				.addComponent(
 																						txtEmail)
 																				.addComponent(
 																						cboRole,
 																						0,
 																						189,
 																						Short.MAX_VALUE)))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addGap(29, 29,
 																		29)
 																.addComponent(
 																		btnInsert)
 																.addPreferredGap(
 																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																.addComponent(
 																		btnFind)
 																.addPreferredGap(
 																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																.addComponent(
 																		btnUpdate)
 																.addPreferredGap(
 																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																.addComponent(
 																		btnDelete)))
 								.addContainerGap(19, Short.MAX_VALUE)));
 
 		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL,
 				new java.awt.Component[] { txtEmail, txtPassword, txtUsername });
 
 		layout.setVerticalGroup(layout
 				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addContainerGap()
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(
 														txtUsername,
 														javax.swing.GroupLayout.PREFERRED_SIZE,
 														javax.swing.GroupLayout.DEFAULT_SIZE,
 														javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(lblUsername))
 								.addPreferredGap(
 										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(
 														txtPassword,
 														javax.swing.GroupLayout.PREFERRED_SIZE,
 														javax.swing.GroupLayout.DEFAULT_SIZE,
 														javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(lblPassword))
 								.addPreferredGap(
 										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(
 														txtEmail,
 														javax.swing.GroupLayout.PREFERRED_SIZE,
 														javax.swing.GroupLayout.DEFAULT_SIZE,
 														javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(lblEmail))
 								.addPreferredGap(
 										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.LEADING)
 												.addComponent(lblRole)
 												.addComponent(
 														cboRole,
 														javax.swing.GroupLayout.PREFERRED_SIZE,
 														javax.swing.GroupLayout.DEFAULT_SIZE,
 														javax.swing.GroupLayout.PREFERRED_SIZE))
 								.addGap(18, 18, 18)
 								.addGroup(
 										layout.createParallelGroup(
 												javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(btnInsert)
 												.addComponent(btnFind)
 												.addComponent(btnUpdate)
 												.addComponent(btnDelete))
 								.addContainerGap(20, Short.MAX_VALUE)));
 	}// </editor-fold>
 
 	public boolean isDuplicateUsername(String username) {
 		User user = User.getUserByName(username);
 		if (user != null)
 			return true;
 		else
 			return false;
 	}
 
 	public String checkValidFields() {
 		String username = txtUsername.getText();
 		@SuppressWarnings("deprecation")
 		String password = txtPassword.getText();
 		String email = txtEmail.getText();
 
 		// check username does not blank
 		if (username.equals("")) {
 			return "You must put user name";
 		}
 		// check password does not blank
 		if (password.equals("")) {
 			return "You must put password";
 		}
 		// check email does not blank
 		if (email.equals("")) {
 			return "You must put email";
 		}
 		// check email is valid
 		else {
 			Pattern rfc2822 = Pattern
 					.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
 			if (!rfc2822.matcher(email).matches()) {
 				return "Your email address is not valid";
 			}
 		}
 		return "True";
 	}
 
 	public void setTxtEmail(javax.swing.JTextField txtEmail) {
 		this.txtEmail = txtEmail;
 	}
 
 	public void setTxtPassword(javax.swing.JPasswordField txtPassword) {
 		this.txtPassword = txtPassword;
 	}
 
 	public void setTxtUsername(javax.swing.JTextField txtUsername) {
 		this.txtUsername = txtUsername;
 	}
 }
