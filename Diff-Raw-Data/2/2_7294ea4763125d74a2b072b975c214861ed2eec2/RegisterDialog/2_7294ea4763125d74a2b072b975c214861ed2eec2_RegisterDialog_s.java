 package jku.se.tetris.prototype;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 public class RegisterDialog extends JDialog {
 	private static final long serialVersionUID = -5147339756430909616L;
 
 	// ---------------------------------------------------------------------------
 
 	public RegisterDialog(JFrame parent) {
 		super(parent);
 		// --
 		setTitle("Register...");
 		createContent();
 		pack();
 		// --
 		setModal(true);
 		// --
 		setLocationRelativeTo(parent);
 	}
 
 	// ---------------------------------------------------------------------------
 
 	public void open() {
 		setVisible(true);
 	}
 
 	public void close() {
 		setVisible(false);
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void createContent() {
 		final Container cp = getContentPane();
 		cp.setPreferredSize(new Dimension(350, 350));
 		cp.setLayout(new GroupLayout(cp));
 		// --
 		final JTextField neubenutzerinput = new JTextField();
 		final JPasswordField neupasswortinput = new JPasswordField();
 		final JPasswordField neupasswortcorrinput = new JPasswordField();
 		// --
 		JLabel reg = new JLabel("Register");
 		reg.setFont(getParent().getFont().deriveFont(20f));
 		reg.setBounds(75, 70, 200, 25);
 		cp.add(reg);
 		// --
 		JLabel neubenutzername = new JLabel("Name");
 		JLabel neupasswort = new JLabel("Enter a password");
 		JLabel neupasswortcorr = new JLabel("Retype password");
 		// --
 		neubenutzerinput.setBounds(75, 125, 200, 25);
 		cp.add(neubenutzerinput);
 		neubenutzerinput.setToolTipText("3 to 20 characters");
 		neupasswortinput.setBounds(75, 175, 200, 25);
 		neupasswortinput.setToolTipText("8-character minimum; case sensitive");
 		cp.add(neupasswortinput);
 		neubenutzername.setBounds(75, 100, 200, 25);
 		cp.add(neubenutzername);
 		neupasswort.setBounds(75, 150, 200, 25);
 		neupasswortcorr.setBounds(75, 200, 200, 25);
 		cp.add(neupasswortcorr);
 		cp.add(neupasswort);
 		neupasswortcorrinput.setBounds(75, 225, 200, 25);
 		neupasswortcorrinput.setToolTipText("Retype password");
 		cp.add(neupasswortcorrinput);
 		// --
 		JButton reset = new JButton("Reset");
 		reset.setBounds(175, 255, 100, 25);
 		cp.add(reset);
 		JButton registrieren = new JButton("Register");
 		registrieren.setBounds(75, 255, 100, 25);
 		cp.add(registrieren);
 		// --
 		reset.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				neubenutzerinput.setText("");
 				neupasswortinput.setText("");
 				neupasswortcorrinput.setText("");
 			}
 		});
 		// --
 		registrieren.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String benutzername = neubenutzerinput.getText();
 				String passwort = new String(neupasswortinput.getPassword());
 				String passwortwieder = new String(neupasswortinput.getPassword());
 				if (benutzername.equalsIgnoreCase("Max") && passwort.equalsIgnoreCase(passwortwieder) && passwort.equalsIgnoreCase("Mustermann")) {
 					neubenutzerinput.setText("");
 					neupasswortinput.setText("");
 					neupasswortcorrinput.setText("");
 					// --
 					JOptionPane.showMessageDialog(cp.getParent(), "You are now registered.", "Successfully registered", JOptionPane.INFORMATION_MESSAGE);
 					close();
 				} else {
 					String errorMessage = "Sorry, this username is already taken.";
 					// --
					if (passwort.length() < 8) {
 						errorMessage = "The password you entered does not meet the security standards (minimum of 8 characters).";
 					} else if (!passwort.equalsIgnoreCase(passwortwieder)) {
 						errorMessage = "The password confirmation does not match.";
 					}
 					// --
 					JOptionPane.showMessageDialog(cp.getParent(), errorMessage, "Failed to register", JOptionPane.ERROR_MESSAGE);
 					// --
 					neubenutzerinput.setText("");
 					neupasswortinput.setText("");
 					neupasswortcorrinput.setText("");
 				}
 			}
 		});
 	}
 }
