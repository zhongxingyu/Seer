 package polyfive.ui.memberpages;
 
 import polyfive.ui.adminpages.*;
 import polyfive.ui.images.*;
 import polyfive.ui.master.*;
 import polyfive.ui.publicpages.*;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Font;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.JPasswordField;
 
 public class AccountDetails extends JPanel {
 	private JTextField creationDate;
 	private JTextField rank;
 	private JTextField phoneNumber;
 	private JTextField email;
 	private JTextField passportIC;
 	private JTextField paymentMethod;
 	private JTextField newCreationDate;
 	private JTextField newRank;
 	private JTextField newPhoneNumber;
 	private JTextField newEmail;
 	private JTextField newPassportIC;
 	private JTextField newPaymentMethod;
 	private MainFrame f = null;
 	private JTextField currentMember;
 	private JTextField upgradingTo;
 	private JPasswordField oldPass;
 	private JPasswordField confirmOld;
 	private JPasswordField newPass;
 	private JPasswordField confirmNew;
 	private JTextField feedbackText;
 
 	/**
 	 * Create the panel.
 	 */
	public AccountDetails(MainFrame frame) {
		f = frame;
 		setLayout(null);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
 		panel.setBounds(445, 471, 400, 204);
 		add(panel);
 		panel.setLayout(null);
 		
 		JButton btnGoldMember = new JButton("Gold Member\r\n");
 		btnGoldMember.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				upgradingTo.setText("Gold Member");
 				newRank.setText("Gold Member");
 			}
 		});
 		btnGoldMember.setBackground(new Color(255, 165, 0));
 		btnGoldMember.setForeground(new Color(255, 255, 255));
 		btnGoldMember.setBounds(15, 11, 115, 125);
 		panel.add(btnGoldMember);
 		
 		JButton btnSilverMember = new JButton("Silver Member");
 		btnSilverMember.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				upgradingTo.setText("Silver Member");
 				newRank.setText("Silver Member");
 			}
 		});
 		btnSilverMember.setBackground(new Color(255, 165, 0));
 		btnSilverMember.setForeground(new Color(255, 255, 255));
 		btnSilverMember.setBounds(140, 11, 115, 125);
 		panel.add(btnSilverMember);
 		
 		JButton btnBronzeMember = new JButton("Bronze Member");
 		btnBronzeMember.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				upgradingTo.setText("Bronze Member");
 				newRank.setText("Bronze Member");
 			}
 		});
 		btnBronzeMember.setBackground(new Color(255, 165, 0));
 		btnBronzeMember.setForeground(new Color(255, 255, 255));
 		btnBronzeMember.setBounds(265, 11, 115, 125);
 		panel.add(btnBronzeMember);
 		
 		currentMember = new JTextField();
 		currentMember.setEditable(false);
 		currentMember.setText("Basic Member\r\n");
 		currentMember.setBounds(140, 147, 240, 20);
 		panel.add(currentMember);
 		currentMember.setColumns(10);
 		
 		JLabel lblCurrentMember = new JLabel("You are currently a:");
 		lblCurrentMember.setBounds(15, 147, 115, 14);
 		panel.add(lblCurrentMember);
 		
 		upgradingTo = new JTextField();
 		upgradingTo.setText("Basic Member\r\n");
 		upgradingTo.setEditable(false);
 		upgradingTo.setColumns(10);
 		upgradingTo.setBounds(140, 173, 240, 20);
 		panel.add(upgradingTo);
 		
 		JLabel lblUpgradingTo = new JLabel("Upgrading to:");
 		lblUpgradingTo.setBounds(15, 176, 115, 14);
 		panel.add(lblUpgradingTo);
 		
 		JPanel accountManagement = new JPanel();
 		accountManagement.setBorder(new LineBorder(new Color(0, 0, 0)));
 		accountManagement.setBounds(35, 471, 400, 204);
 		add(accountManagement);
 		accountManagement.setLayout(null);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		lblPassword.setBounds(10, 37, 77, 14);
 		accountManagement.add(lblPassword);
 		
 		JLabel lblNewPassword = new JLabel("New Password:");
 		lblNewPassword.setBounds(10, 99, 77, 14);
 		accountManagement.add(lblNewPassword);
 		
 		JLabel lblConfirmOld = new JLabel("Confirm Old:");
 		lblConfirmOld.setBounds(10, 68, 77, 14);
 		accountManagement.add(lblConfirmOld);
 		
 		JLabel lblConfirmNew = new JLabel("Confirm New:");
 		lblConfirmNew.setBounds(10, 130, 77, 14);
 		accountManagement.add(lblConfirmNew);
 		
 		JButton resetPasswords = new JButton("Reset Changes");
 		resetPasswords.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				oldPass.setText("");
 				confirmOld.setText("");
 				newPass.setText("");
 				confirmNew.setText("");
 				feedbackText.setText("Successfully cleared passwords entered.");
 				//WARNING: DOES NOT CURRENTLY WORK
 			}
 		});
 		resetPasswords.setForeground(Color.WHITE);
 		resetPasswords.setBorder(null);
 		resetPasswords.setBackground(new Color(255, 165, 0));
 		resetPasswords.setBounds(10, 158, 185, 35);
 		accountManagement.add(resetPasswords);
 		
 		JButton confirmPasswords = new JButton("Confirm Changes");
 		confirmPasswords.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (oldPass == confirmOld && newPass == confirmNew){
 						oldPass.setText("");
 						confirmOld.setText("");
 						newPass.setText("");
 						confirmNew.setText("");
 						feedbackText.setText("Password has been changed successfully.");
 				}
 			}
 		});
 		confirmPasswords.setForeground(Color.WHITE);
 		confirmPasswords.setBorder(null);
 		confirmPasswords.setBackground(new Color(255, 165, 0));
 		confirmPasswords.setBounds(205, 158, 185, 35);
 		accountManagement.add(confirmPasswords);
 		
 		JLabel lblPasswords = new JLabel("Password Changes:");
 		lblPasswords.setBounds(10, 11, 116, 14);
 		accountManagement.add(lblPasswords);
 		
 		oldPass = new JPasswordField();
 		oldPass.setBounds(97, 34, 280, 20);
 		accountManagement.add(oldPass);
 		
 		confirmOld = new JPasswordField();
 		confirmOld.setBounds(97, 65, 280, 20);
 		accountManagement.add(confirmOld);
 		
 		newPass = new JPasswordField();
 		newPass.setBounds(97, 96, 280, 20);
 		accountManagement.add(newPass);
 		
 		confirmNew = new JPasswordField();
 		confirmNew.setBounds(97, 127, 280, 20);
 		accountManagement.add(confirmNew);
 		
 		JLabel lblPoly5Corp = new JLabel("POLYFIVE CORP");
 		lblPoly5Corp.setBounds(35, 30, 185, 35);
 		lblPoly5Corp.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		add(lblPoly5Corp);
 		
 		JComboBox chooseLanguage = new JComboBox();
 		chooseLanguage.setBounds(1120, 30, 225, 45);
 		chooseLanguage.setModel(new DefaultComboBoxModel(new String[] {"Select Language", "English", "Tamil", "Malay", "Mandarin"}));
 		add(chooseLanguage);
 		
 		JPanel topBar = new JPanel();
 		topBar.setBounds(35, 90, 1292, 45);
 		add(topBar);
 		topBar.setLayout(null);
 		
 		JLabel lblYouAreLogged = new JLabel("You are logged in as: Basic Member");
 		lblYouAreLogged.setBounds(5, 5, 800, 35);
 		topBar.add(lblYouAreLogged);
 		lblYouAreLogged.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		
 		JButton LogOutButton = new JButton("Log Out");
 		LogOutButton.setForeground(new Color(255, 255, 255));
 		LogOutButton.setBackground(new Color(255, 165, 0));
 		LogOutButton.setBorder(null);
 		LogOutButton.setBounds(1190, 5, 100, 35);
 		topBar.add(LogOutButton);
 		
 		JButton ReturnButton = new JButton("Back");
 		ReturnButton.setForeground(new Color(255, 255, 255));
 		ReturnButton.setBackground(new Color(255, 165, 0));
 		ReturnButton.setBorder(null);
 		ReturnButton.setBounds(1085, 5, 100, 35);
 		topBar.add(ReturnButton);
 		
 		JPanel account = new JPanel();
 		account.setBorder(new LineBorder(new Color(0, 0, 0)));
 		account.setBounds(35, 175, 400, 285);
 		add(account);
 		account.setLayout(null);
 		
 		JLabel lblAccountDetails = new JLabel("Account Details:");
 		lblAccountDetails.setBounds(10, 10, 120, 20);
 		account.add(lblAccountDetails);
 		
 		JLabel lblCreationDate = new JLabel("Creation Date:");
 		lblCreationDate.setBounds(10, 50, 90, 20);
 		account.add(lblCreationDate);
 		
 		creationDate = new JTextField();
 		creationDate.setText("2 November 2011");
 		creationDate.setEditable(false);
 		creationDate.setBounds(97, 50, 280, 20);
 		account.add(creationDate);
 		creationDate.setColumns(10);
 		
 		JLabel lblRank = new JLabel("Rank:");
 		lblRank.setBounds(10, 81, 90, 20);
 		account.add(lblRank);
 		
 		rank = new JTextField();
 		rank.setText("Basic\r\n");
 		rank.setEditable(false);
 		rank.setColumns(10);
 		rank.setBounds(97, 81, 280, 20);
 		account.add(rank);
 		
 		phoneNumber = new JTextField();
 		phoneNumber.setText("81590435\r\n");
 		phoneNumber.setEditable(false);
 		phoneNumber.setColumns(10);
 		phoneNumber.setBounds(97, 112, 280, 20);
 		account.add(phoneNumber);
 		
 		JLabel lblPhoneNumber = new JLabel("Phone Number:");
 		lblPhoneNumber.setBounds(10, 112, 90, 20);
 		account.add(lblPhoneNumber);
 		
 		email = new JTextField();
 		email.setText("p5c@polyfive.org");
 		email.setEditable(false);
 		email.setColumns(10);
 		email.setBounds(97, 143, 280, 20);
 		account.add(email);
 		
 		JLabel lblEmail = new JLabel("Email:");
 		lblEmail.setBounds(10, 143, 90, 20);
 		account.add(lblEmail);
 		
 		passportIC = new JTextField();
 		passportIC.setText("S12345678K");
 		passportIC.setEditable(false);
 		passportIC.setColumns(10);
 		passportIC.setBounds(97, 174, 280, 20);
 		account.add(passportIC);
 		
 		JLabel lblPassportIC = new JLabel("Passport/IC:");
 		lblPassportIC.setBounds(10, 174, 90, 20);
 		account.add(lblPassportIC);
 		
 		paymentMethod = new JTextField();
 		paymentMethod.setText("Credit Card");
 		paymentMethod.setEditable(false);
 		paymentMethod.setColumns(10);
 		paymentMethod.setBounds(97, 205, 280, 20);
 		account.add(paymentMethod);
 		
 		JLabel lblPaymentMethod = new JLabel("Payment Method:");
 		lblPaymentMethod.setBounds(10, 205, 90, 20);
 		account.add(lblPaymentMethod);
 		
 		JButton changeDetails = new JButton("Change Details");
 		changeDetails.setForeground(new Color(255, 255, 255));
 		changeDetails.setFocusPainted(false);
 		changeDetails.setBorder(null);
 		changeDetails.setBackground(new Color(255, 165, 0));
 		changeDetails.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				newCreationDate.setEditable(true);
 		        newPhoneNumber.setEditable(true);
 		        newEmail.setEditable(true);
 		        newPassportIC.setEditable(true);
 		        newPaymentMethod.setEditable(true);
 		        
 		        String date = creationDate.getText();
 				newCreationDate.setText(date);
 				String ra = rank.getText();
 				newRank.setText(ra);
 				String phoneNum = phoneNumber.getText();
 				newPhoneNumber.setText(phoneNum);
 				String em = email.getText();
 				newEmail.setText(em);
 				String passIC = passportIC.getText();
 				newPassportIC.setText(passIC);
 				String pay = paymentMethod.getText();
 				newPaymentMethod.setText(pay);
 		        
 			}
 		});
 		changeDetails.setBounds(10, 236, 185, 35);
 		account.add(changeDetails);
 		
 		JButton lockChanges = new JButton("Lock Changes");
 		lockChanges.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				        newCreationDate.setEditable(false);
 				        newPhoneNumber.setEditable(false);
 				        newEmail.setEditable(false);
 				        newPassportIC.setEditable(false);
 				        newPaymentMethod.setEditable(false);		        
 			}
 		});
 		lockChanges.setForeground(new Color(255, 255, 255));
 		lockChanges.setBorder(null);
 		lockChanges.setBackground(new Color(255, 165, 0));
 		lockChanges.setBounds(205, 236, 185, 35);
 		account.add(lockChanges);
 		
 		JPanel editDetails = new JPanel();
 		editDetails.setBorder(new LineBorder(new Color(0, 0, 0)));
 		editDetails.setLayout(null);
 		editDetails.setBounds(445, 175, 400, 285);
 		add(editDetails);
 		
 		JLabel lblNewDetails = new JLabel("New Account Details:");
 		lblNewDetails.setBounds(10, 10, 120, 20);
 		editDetails.add(lblNewDetails);
 		
 		JLabel lblNewCreationDate = new JLabel("Creation Date:");
 		lblNewCreationDate.setBounds(10, 50, 90, 20);
 		editDetails.add(lblNewCreationDate);
 		
 		newCreationDate = new JTextField();
 		newCreationDate.setEditable(false);
 		newCreationDate.setColumns(10);
 		newCreationDate.setBounds(97, 50, 280, 20);
 		editDetails.add(newCreationDate);
 		
 		JLabel lblNewRank = new JLabel("Rank:");
 		lblNewRank.setBounds(10, 81, 90, 20);
 		editDetails.add(lblNewRank);
 		
 		newRank = new JTextField();
 		newRank.setEditable(false);
 		newRank.setColumns(10);
 		newRank.setBounds(97, 81, 280, 20);
 		editDetails.add(newRank);
 		
 		newPhoneNumber = new JTextField();
 		newPhoneNumber.setEditable(false);
 		newPhoneNumber.setColumns(10);
 		newPhoneNumber.setBounds(97, 112, 280, 20);
 		editDetails.add(newPhoneNumber);
 		
 		JLabel lblNewPhoneNumber = new JLabel("Phone Number:");
 		lblNewPhoneNumber.setBounds(10, 112, 90, 20);
 		editDetails.add(lblNewPhoneNumber);
 		
 		newEmail = new JTextField();
 		newEmail.setEditable(false);
 		newEmail.setColumns(10);
 		newEmail.setBounds(97, 143, 280, 20);
 		editDetails.add(newEmail);
 		
 		JLabel lblNewEmail = new JLabel("Email:");
 		lblNewEmail.setBounds(10, 143, 90, 20);
 		editDetails.add(lblNewEmail);
 		
 		newPassportIC = new JTextField();
 		newPassportIC.setEditable(false);
 		newPassportIC.setColumns(10);
 		newPassportIC.setBounds(97, 174, 280, 20);
 		editDetails.add(newPassportIC);
 		
 		JLabel lblNewPassportIC = new JLabel("Passport/IC:");
 		lblNewPassportIC.setBounds(10, 174, 90, 20);
 		editDetails.add(lblNewPassportIC);
 		
 		newPaymentMethod = new JTextField();
 		newPaymentMethod.setEditable(false);
 		newPaymentMethod.setColumns(10);
 		newPaymentMethod.setBounds(97, 205, 280, 20);
 		editDetails.add(newPaymentMethod);
 		
 		JLabel lblNewPaymentMethod = new JLabel("Payment Method:");
 		lblNewPaymentMethod.setBounds(10, 205, 90, 20);
 		editDetails.add(lblNewPaymentMethod);
 		
 		JButton confirmChanges = new JButton("Confirm Changes");
 		confirmChanges.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String date = newCreationDate.getText();
 				creationDate.setText(date);
 				String ra = newRank.getText();
 				rank.setText(ra);
 				String phoneNum = newPhoneNumber.getText();
 				phoneNumber.setText(phoneNum);
 				String em = newEmail.getText();
 				email.setText(em);
 				String passIC = newPassportIC.getText();
 				passportIC.setText(passIC);
 				String pay = newPaymentMethod.getText();
 				paymentMethod.setText(pay);
 				feedbackText.setText("Your new details have been changed.");
 			}
 		});
 		confirmChanges.setForeground(new Color(255, 255, 255));
 		confirmChanges.setBackground(new Color(255, 165, 0));
 		confirmChanges.setBorder(null);
 		confirmChanges.setBounds(205, 236, 185, 35);
 		editDetails.add(confirmChanges);
 		
 		JButton resetChanges = new JButton("Reset Changes");
 		resetChanges.addActionListener(new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 		        newCreationDate.setText("");
 		        newRank.setText("");
 		        newPhoneNumber.setText("");
 		        newEmail.setText("");
 		        newPassportIC.setText("");
 		        newPaymentMethod.setText("");
 		        feedbackText.setText("Successfully cleared all new details entered.");
 		    }
 		});
 		resetChanges.setBorder(null);
 		resetChanges.setBackground(new Color(255, 165, 0));
 		resetChanges.setForeground(new Color(255, 255, 255));
 		resetChanges.setBounds(10, 236, 185, 35);
 		editDetails.add(resetChanges);
 		
 		JPanel miscDetails = new JPanel();
 		miscDetails.setBorder(new LineBorder(new Color(0, 0, 0)));
 		miscDetails.setBounds(855, 175, 470, 500);
 		add(miscDetails);
 		miscDetails.setLayout(null);
 		
 		JLabel lblTransactionHistory = new JLabel("Transaction History:");
 		lblTransactionHistory.setBounds(20, 11, 145, 14);
 		miscDetails.add(lblTransactionHistory);
 		
 		JLabel lblCurrentBookings = new JLabel("Current Bookings:");
 		lblCurrentBookings.setBounds(20, 201, 120, 14);
 		miscDetails.add(lblCurrentBookings);
 		
 		JPanel transactionHistory = new JPanel();
 		transactionHistory.setBorder(new LineBorder(new Color(0, 0, 0)));
 		transactionHistory.setBounds(20, 36, 425, 154);
 		miscDetails.add(transactionHistory);
 		transactionHistory.setLayout(null);
 		
 		JPanel currentBookings = new JPanel();
 		currentBookings.setBorder(new LineBorder(new Color(0, 0, 0)));
 		currentBookings.setBounds(20, 226, 425, 154);
 		miscDetails.add(currentBookings);
 		currentBookings.setLayout(new GridLayout(1, 0, 0, 0));
 		
 		feedbackText = new JTextField();
 		feedbackText.setEditable(false);
 		feedbackText.setBounds(20, 391, 425, 20);
 		miscDetails.add(feedbackText);
 		feedbackText.setColumns(10);
 		
 		JLabel background = new JLabel("");
		background.setIcon(new ImageIcon(AccountDetails.class.getResource("/polyfive/ui/images/p5cbg.png")));
		background.setBounds(0, 0, 1366, 768);
 		add(background);
 
 	}
 	

 }
