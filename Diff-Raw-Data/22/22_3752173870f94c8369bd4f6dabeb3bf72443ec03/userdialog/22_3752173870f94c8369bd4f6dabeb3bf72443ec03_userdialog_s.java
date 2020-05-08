 package de.team55.mms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import de.team55.mms.data.StellvertreterList;
 import de.team55.mms.data.User;
 import de.team55.mms.function.Hash;
 import de.team55.mms.function.ServerConnection;
 
 public class userdialog extends JDialog {
 	private JFrame owner;
 
 	public static final int OK_OPTION = 1;
 	public static final int CANCEL_OPTION = 0;
 	private int userResponse;
 	private ServerConnection serverConnection;
 	private boolean registration = false;
 
 	private JTextField textVorname;
 	private JTextField textNachname;
 	private JTextField textMail;
 	private JTextField textPass;
 	private JTextField textTitel;
 	private JCheckBox cb_ModAnn;
 	private JCheckBox cb_ModErst;
 	private JCheckBox cb_ModLes;
 	private JCheckBox cb_BV;
 
 	private DefaultListModel<User> lm = new DefaultListModel<User>();
 
 	private boolean adminedit = true;
 
 	private User usr = new User("", "", "", "", null, false, false, false, false, false);
 
 	/**
 	 * erzeugen des Dialoges
 	 * 
 	 * @param owner
 	 *            Parent Object
 	 * @param title
 	 *            Titel des Dialoges
 	 * @param serverConnection
 	 *            Serververbindung
 	 */
 	public userdialog(JFrame owner, String title, ServerConnection serverConnection) {
 		super(owner, title, true);
 		this.setResizable(false);
 		this.serverConnection = serverConnection;
 		createDialog();
 	}
 
 	/**
 	 * erzeugen des Dialoges
 	 * 
 	 * @param owner
 	 *            Parent Object
 	 * @param title
 	 *            Titel des Dialoges
 	 * @param usr
 	 *            Der zu bearbeitende User
 	 * @param adminedit
 	 *            Gibt an, ob der Admin den User bearbeitet
 	 * @param serverConnection
 	 *            Serververbindung
 	 */
 	public userdialog(JFrame owner, String title, User usr, boolean adminedit, ServerConnection serverConnection) {
 		super(owner, title, true);
 		this.setResizable(false);
 		this.usr = usr;
 		this.adminedit = adminedit;
 		this.serverConnection = serverConnection;
 		createDialog();
 	}
 
 	/**
 	 * erzeugen des Dialoges
 	 * 
 	 * @param owner
 	 *            Parent Object
 	 * @param title
 	 *            Titel des Dialoges
 	 * @param serverConnection
 	 *            Serververbindung
 	 * @param registration
 	 *            gibt an, ob es sich um eine Regestrierung handelt
 	 */
 	public userdialog(JFrame owner, String title, ServerConnection serverConnection, boolean registration) {
 		super(owner, title, true);
 		this.setResizable(false);
 		this.serverConnection = serverConnection;
 		this.registration = registration;
 		createDialog();
 	}
 
 	/**
 	 * Gibt den eingegeben User zurck
 	 * 
 	 * @return User eingegebener Benutzer
 	 */
 	public User getUser() {
 		usr.setTitel(textTitel.getText());
 		usr.setVorname(textVorname.getText());
 		usr.setNachname(textNachname.getText());
 		if (!textPass.getText().isEmpty() && !textPass.getText().equals(usr.getPassword()))
 			usr.setPassword(Hash.getMD5(textPass.getText()));
 		usr.seteMail(textMail.getText());
 		usr.setReadModule(cb_ModLes.isSelected());
 		usr.setCreateModule(cb_ModErst.isSelected());
 		usr.setAcceptModule(cb_ModAnn.isSelected());
 		usr.setManageUsers(cb_BV.isSelected());
 		return usr;
 	}
 
 	/**
 	 * zeigt Dialog an
 	 * 
 	 * @return int status des gedrckten Buttons
 	 */
 	public int showCustomDialog() {
 		this.show();
 		return userResponse;
 
 	}
 
 	/**
 	 * erzeugt den Dialog
 	 */
 	private void createDialog() {
 
 		JPanel pnl_Dialog = new JPanel();
 		pnl_Dialog.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_title = new JPanel();
 		pnl_Dialog.add(pnl_title, BorderLayout.NORTH);
 		pnl_title.setLayout(new GridLayout(1, 0, 0, 0));
 
 		JLabel lblUserData = new JLabel("User Daten:");
 		pnl_title.add(lblUserData);
 		lblUserData.setHorizontalAlignment(SwingConstants.CENTER);
 
 		JLabel lblUserrechte = new JLabel("Userrechte:");
 		pnl_title.add(lblUserrechte);
 		lblUserrechte.setHorizontalAlignment(SwingConstants.CENTER);
 
 		JPanel pnl_fields = new JPanel();
 		pnl_Dialog.add(pnl_fields, BorderLayout.WEST);
 		pnl_fields.setLayout(new BorderLayout(0, 0));
 
 		JPanel pbl_Daten = new JPanel();
 		pnl_fields.add(pbl_Daten, BorderLayout.CENTER);
 		pbl_Daten.setLayout(new GridLayout(0, 1, 0, 0));
 
 		JPanel pnl_T = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) pnl_T.getLayout();
 		flowLayout.setAlignment(FlowLayout.TRAILING);
 		pbl_Daten.add(pnl_T);
 
 		JLabel lblT = new JLabel("Titel");
 		pnl_T.add(lblT);
 
 		textTitel = new JTextField(usr.getTitel());
 		pnl_T.add(textTitel);
 		textTitel.setColumns(10);
 
 		JPanel pnl_VN = new JPanel();
 		FlowLayout flowLayout1 = (FlowLayout) pnl_VN.getLayout();
 		flowLayout1.setAlignment(FlowLayout.TRAILING);
 		pbl_Daten.add(pnl_VN);
 
 		JLabel lblVorname = new JLabel("Vorname");
 		pnl_VN.add(lblVorname);
 
 		textVorname = new JTextField(usr.getVorname());
 		pnl_VN.add(textVorname);
 		textVorname.setColumns(10);
 
 		JPanel pnl_NN = new JPanel();
 		FlowLayout flowLayout_1 = (FlowLayout) pnl_NN.getLayout();
 		flowLayout_1.setAlignment(FlowLayout.TRAILING);
 		pbl_Daten.add(pnl_NN);
 
 		JLabel lblNachnahme = new JLabel("Nachnahme");
 		pnl_NN.add(lblNachnahme);
 
 		textNachname = new JTextField(usr.getNachname());
 		pnl_NN.add(textNachname);
 		textNachname.setColumns(10);
 
 		JPanel pnl_Mail = new JPanel();
 		pbl_Daten.add(pnl_Mail);
 		pnl_Mail.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
 
 		JLabel lblEmail = new JLabel("e-Mail");
 		pnl_Mail.add(lblEmail);
 
 		textMail = new JTextField(usr.geteMail());
 		pnl_Mail.add(textMail);
 		textMail.setColumns(10);
 
 		JPanel pnl_Pass = new JPanel();
 		pbl_Daten.add(pnl_Pass);
 		pnl_Pass.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
 
 		JLabel lblPassword = new JLabel("Password");
 		pnl_Pass.add(lblPassword);
 
 		textPass = new JPasswordField(usr.getPassword());
 		pnl_Pass.add(textPass);
 		textPass.setColumns(10);
 
 		JPanel pnl_rechte = new JPanel();
 		pnl_Dialog.add(pnl_rechte);
 		pnl_rechte.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_checkboxes = new JPanel();
 		pnl_rechte.add(pnl_checkboxes);
 		pnl_checkboxes.setLayout(new GridLayout(0, 1, 0, 0));
 
 		cb_BV = new JCheckBox("Benutzer Verwalten", usr.getManageUsers());
 		pnl_checkboxes.add(cb_BV);
 
 		cb_ModErst = new JCheckBox("Module einreichen", usr.getCreateModule());
 		pnl_checkboxes.add(cb_ModErst);
 
 		cb_ModAnn = new JCheckBox("Module Annehmen", usr.getAcceptModule());
 		pnl_checkboxes.add(cb_ModAnn);
 
		cb_ModLes = new JCheckBox("Module lesen", usr.getReadModule());
 		pnl_checkboxes.add(cb_ModLes);
 
 		// Wenn der Benutzer nicht vom Admin bearbeitet wird
 		// knnen keine Rechte gendert werden
 		if (!adminedit) {
 			cb_BV.setEnabled(false);
 			cb_ModErst.setEnabled(false);
 			cb_ModAnn.setEnabled(false);
 			cb_ModLes.setEnabled(false);
 		}
 
 		JPanel south = new JPanel();
 		south.setLayout(new BorderLayout(0, 0));
 
 		// Text bei Registration ndern
 		if (registration) {
 			lblUserData.setText("Ihre Daten:");
 			lblUserrechte.setText("Ihre gewnschten Rechte:");
 		} else {
 			JPanel pnl_user = new JPanel();
 			south.add(pnl_user, BorderLayout.CENTER);
 
 			JPanel pnl_list = new JPanel();
 
 			// Liste mit Stellvertretern
 			final JList<User> zlist = new JList<User>(lm);
 			ArrayList<User> stelv = serverConnection.getStellvertreter(usr.geteMail());
 			for (int i = 0; i < stelv.size(); i++) {
 				lm.addElement(stelv.get(i));
 			}
 
 			pnl_list.add(zlist);
 
 			// ausgewhlten Stellvertreter entfernen
 			JButton remove = new JButton("Stellvertreter entfernen");
 			remove.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					lm.removeElement(zlist.getSelectedValue());
 					pack();
 				}
 			});
 
 			pnl_list.add(remove);
 
 			south.add(pnl_list, BorderLayout.NORTH);
 
 			ArrayList<User> userlist = serverConnection.userload();
 
 			DefaultComboBoxModel<User> cbmodel = new DefaultComboBoxModel<User>();
 			for (int i = 0; i < userlist.size(); i++) {
 				User s = userlist.get(i);
 				if (!s.geteMail().equals(usr.geteMail()))
 					cbmodel.addElement(s);
 			}
 
 			// Zur Auswahl stehende Benutzer
 			final JComboBox<User> cb_Z = new JComboBox<User>(cbmodel);
 			pnl_user.add(cb_Z);
 
 			JButton z_btn = new JButton("Stellvertreter auswhlen");
 			z_btn.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (!lm.contains(cb_Z.getSelectedItem()))
 						lm.addElement((User) cb_Z.getSelectedItem());
 					pack();
 				}
 			});
 			pnl_user.add(z_btn);
 		}
 		JPanel pnl_footer = new JPanel();
 		south.add(pnl_footer, BorderLayout.SOUTH);
 		JButton btnOk = new JButton("OK");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Prfe ob alle Felder ausgefllt wurden
 				if (textVorname.getText().isEmpty() || textNachname.getText().isEmpty() || textMail.getText().isEmpty()
 						|| (textPass.getText().isEmpty() && usr.getPassword() != null)) {
 					JOptionPane.showMessageDialog(owner, "Geben Sie gltige Daten ein!", "Fehler", JOptionPane.ERROR_MESSAGE);
 				} else {
 					// Teste auf korrekte e-Mail
 					if (validateMail(textMail.getText())) {
 						userResponse = OK_OPTION;
 						ArrayList<String> usr = new ArrayList<String>();
 						for (int i = 0; i < lm.getSize(); i++) {
 							usr.add(lm.get(i).geteMail());
 						}
 						// Liste mit Stellvertretern einreichen
 						StellvertreterList sl = new StellvertreterList(textMail.getText(), usr);
 						serverConnection.setStellvertreter(sl);
 						hide();
 					} else
 						JOptionPane.showMessageDialog(owner, "Geben Sie eine gltige e-Mail-Adresse ein!", "Fehler",
 								JOptionPane.ERROR_MESSAGE);
 
 				}
 			}
 		});
 		pnl_footer.add(btnOk);
 		getRootPane().setDefaultButton(btnOk);
 
 		JButton btnAbbrechen = new JButton("Abbrechen");
 		btnAbbrechen.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				userResponse = CANCEL_OPTION;
 				hide();
 			}
 		});
 		pnl_footer.add(btnAbbrechen);
 		pnl_Dialog.add(south, BorderLayout.SOUTH);
 
 		this.setContentPane(pnl_Dialog);
 		this.pack();
 	}
 
 	/**
 	 * Prft, ob es sich bei dem eingegeben String um eine e-Mail Adresse
 	 * handelt
 	 * 
 	 * @param eMail
 	 *            zu prfenden e-Mail
 	 * @return true, wenn korrekt, ansonsten false
 	 */
 	private boolean validateMail(String eMail) {
 		String pat = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 		Pattern pattern = Pattern.compile(pat);
 		Matcher matcher = pattern.matcher(eMail);
 		return matcher.matches();
 	}
 }
