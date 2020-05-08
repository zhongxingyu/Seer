 package de.team55.mms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 import de.team55.mms.data.*;
 import de.team55.mms.function.SendMail;
 import de.team55.mms.function.ServerConnection;
 import javax.swing.JSplitPane;
 
 public class mainscreen {
 
 	private static JFrame frame;
 
 	private static final int SUCCES = 2;
 	private final Dimension btnSz = new Dimension(140, 50);
 	public ServerConnection database = new ServerConnection();
 
 	// Variablen
 	private static User current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb",
 			false, false, false, false, true); // Gast
 	String studtransferstring = ""; // uebergabe String fuer Tabellen -
 									// studiengang
 	String modbuchtransferstring = ""; // uebergabe String fuer Tabellen -
 										// modulbuch
 	String modtyptransferstring = ""; // uebergabe String fuer Tabellen -
 										// modultyp
 	String modulselectionstring = ""; // ubergabe String des ausgewaehlten
 										// Moduls
 
 	// Listen
 	private ArrayList<User> worklist = null; // Liste mit Usern
 	private ArrayList<User> neueUser = new ArrayList<User>(); // Liste mit Usern
 
 	private ArrayList<Studiengang> studienlist = null; // Liste mit
 														// Studiengngen
 	private ArrayList<Modul> selectedmodullist = null; // Liste der Module im
 														// durchstoebern segment
 	private ArrayList<Modulhandbuch> modulhandlist = null; // Liste der
 															// Modulhandbuecher
 															// des ausgewaehlten
 															// Studiengangs
 	private ArrayList<Zuordnung> typen = null; // Liste mit Zuordnungen
 	private HashMap<JButton, Integer> buttonmap = new HashMap<JButton, Integer>(); // Map
 																					// der
 																					// Dynamischen
 																					// Buttons
 	private ArrayList<String> defaultlabels = new ArrayList<String>();
 
 	// Modelle
 	private DefaultTableModel tmodel;
 	private DefaultTableModel tmodelNeu;
 	private DefaultTableModel studmodel;
 	private DefaultTableModel modbuchmodel;
 	private DefaultTableModel modtypmodel;
 	private DefaultTableModel modshowmodel;
 	private DefaultComboBoxModel<Studiengang> cbmodel = new DefaultComboBoxModel<Studiengang>();
 	private DefaultComboBoxModel<Zuordnung> cbmodel_Z = new DefaultComboBoxModel<Zuordnung>();
 	private DefaultListModel<Modul> lm = new DefaultListModel<Modul>();
 	private DefaultListModel<Modul> lm_ack = new DefaultListModel<Modul>();
 	private DefaultListModel<Studiengang> studimodel = new DefaultListModel<Studiengang>();
 	private DefaultListModel<Zuordnung> typenmodel = new DefaultListModel<Zuordnung>();
 
 	// Komponenten
 	private static JPanel cards = new JPanel();
 	private static JPanel modul_panel = new JPanel();
 	private static JPanel modul_panel_edit = new JPanel();
 	private static JButton btnModulEinreichen = new JButton("Modul Einreichen");
 	private static JButton btnModulVerwaltung = new JButton("Verwaltung");
 	private static JButton btnModulBearbeiten = new JButton("Modul bearbeiten");
 	private static JButton btnMHB = new JButton("<html>Module<br>Durchst\u00f6bern");
 	private static JButton btnUserVerwaltung = new JButton("User Verwaltung");
 	private static JButton btnLogin = new JButton("Einloggen");
 	private static JPanel mod = new JPanel();
 
 	// main Frame
 	public mainscreen() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 800, 480);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		centerscr();
 		topscr();
 		leftscr();
 
 		frame.setVisible(true);
 	}
 
 	// center Frame
 	private void centerscr() {
 
 		frame.getContentPane().add(cards, BorderLayout.CENTER);
 		cards.setLayout(new CardLayout(0, 0));
 
 		defaultlabels.add("Zuordnung");
 		defaultlabels.add("Krzel");
 		defaultlabels.add("Prfungsform");
 
 		defaultlabels.add("Jahrgang");
 		defaultlabels.add("Name");
 		defaultlabels.add("K\u00fcrzel");
 		defaultlabels.add("Titel");
 		defaultlabels.add("Leistungspunkte");
 		defaultlabels.add("Dauer");
 		defaultlabels.add("Turnus");
 		defaultlabels.add("Modulverantwortlicher");
 		defaultlabels.add("Dozenten");
 		defaultlabels.add("Inhalt");
 		defaultlabels.add("Lernziele");
 		defaultlabels.add("Literatur");
 		defaultlabels.add("Sprache");
 		defaultlabels.add("Pr\u00fcfungsform");
 		defaultlabels.add("Notenbildung");
 
 		mod.setLayout(new BorderLayout());
 
 		homecard();
 		usermgtcard();
 		newmodulecard();
 		modulbearbeitenCard();
 		studiengangCard();
 		manage();
 		cards.add(mod, "modBearbeiten");
 	}
 
 	private void manage() {
 		JPanel pnl_manage = new JPanel();
 		cards.add(pnl_manage, "manage");
 		pnl_manage.setLayout(new BoxLayout(pnl_manage, BoxLayout.Y_AXIS));
 
 		JPanel pnl_studiengang = new JPanel();
 		pnl_manage.add(pnl_studiengang);
 		pnl_studiengang.setLayout(new BorderLayout(0, 0));
 
 		JList<Studiengang> list = new JList<Studiengang>(studimodel);
 		JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pnl_studiengang.add(scrollPane, BorderLayout.CENTER);
 
 		JPanel buttons = new JPanel();
 		pnl_studiengang.add(buttons, BorderLayout.SOUTH);
 
 		JButton btnNeuerStudiengang = new JButton("Neuer Studiengang");
 		btnNeuerStudiengang.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 					String name = JOptionPane.showInputDialog(frame, "Name des neuen Studiengangs:",
 							"neuer Studiengang", JOptionPane.PLAIN_MESSAGE);
 
 					while (name.isEmpty()) {
 						name = JOptionPane.showInputDialog(frame,
 								"Bitte g\u00fcltigen Namen des neuen Studiengangs eingeben:", "neuer Studiengang",
 								JOptionPane.PLAIN_MESSAGE);
 					}
 
 					studienlist = database.getStudiengaenge();
 					boolean neu = true;
 					for (int i = 0; i < studienlist.size(); i++) {
 						if (studienlist.get(i).equals(name)) {
 							neu = false;
 							break;
 						}
 					}
 					if (neu) {
 						database.setStudiengang(name);
 						studimodel.removeAllElements();
 						studienlist = database.getStudiengaenge();
 						for (int i = 0; i < studienlist.size(); i++) {
 							studimodel.addElement(studienlist.get(i));
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Studiengang ist schon vorhanden", "Fehler",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				} catch (NullPointerException np) {
 
 				}
 			}
 		});
 		buttons.add(btnNeuerStudiengang);
 
 		JLabel lblStudiengnge = new JLabel("Studieng\u00E4nge");
 		pnl_studiengang.add(lblStudiengnge, BorderLayout.NORTH);
 
 		JPanel pnl_zuordnungen = new JPanel();
 		pnl_manage.add(pnl_zuordnungen);
 		pnl_zuordnungen.setLayout(new BorderLayout(0, 0));
 
 		JList<Zuordnung> list1 = new JList<Zuordnung>(typenmodel);
 
 		JPanel buttons1 = new JPanel();
 		pnl_zuordnungen.add(buttons1, BorderLayout.SOUTH);
 
 		JButton btnNeueZuordnung = new JButton("Neue Zuordnung");
 		btnNeueZuordnung.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 
 					JTextField neu_Name = new JTextField();
 					JTextField neu_Abschluss = new JTextField();
 					JComboBox<Studiengang> neu_sgbox = new JComboBox<Studiengang>(cbmodel);
 
 					Object[] message = { "Name des Types:", neu_Name, "Abschluss:", neu_Abschluss, "Studiengang:",
 							neu_sgbox };
 
 					int option = JOptionPane.showConfirmDialog(frame, message, "Neuen Typ anlegen",
 							JOptionPane.OK_CANCEL_OPTION);
 					if (option == JOptionPane.OK_OPTION) {
 
 						while ((neu_Name.getText().isEmpty() || (neu_sgbox.getSelectedItem() == null) || neu_Abschluss
 								.getText().isEmpty()) && (option == JOptionPane.OK_OPTION)) {
 							Object[] messageEmpty = { "Bitte alle Felder ausf\u00fcllen!", "Name des Types:", neu_Name,
 									"Abschluss:", neu_Abschluss, "Studiengang:", neu_sgbox };
 							option = JOptionPane.showConfirmDialog(frame, messageEmpty, "Neuen Typ anlegen",
 									JOptionPane.OK_CANCEL_OPTION);
 						}
 						if (option == JOptionPane.OK_OPTION) {
 							Studiengang s = (Studiengang) neu_sgbox.getSelectedItem();
 							Zuordnung z = new Zuordnung(neu_Name.getText(), s.getName(), s.getId(), neu_Abschluss
 									.getText());
 
 							boolean neu = true;
 							for (int i = 0; i < typen.size(); i++) {
 								if (typen.get(i).equals(z)) {
 									neu = false;
 									break;
 								}
 							}
 							if (neu) {
 								database.setZuordnung(z);
 								typen = database.getZuordnungen();
 								typenmodel.removeAllElements();
 								for (int i = 0; i < typen.size(); i++) {
 									typenmodel.addElement(typen.get(i));
 								}
 							} else {
 								JOptionPane.showMessageDialog(frame, "Zuordnung ist schon vorhanden", "Fehler",
 										JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					}
 
 				} catch (NullPointerException np) {
 					np.printStackTrace();
 				}
 			}
 
 		});
 		buttons1.add(btnNeueZuordnung);
 
 		JButton btnZurck_1 = new JButton("Zur\u00FCck");
 		btnZurck_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttons1.add(btnZurck_1);
 
 		JLabel lblZuordnungen = new JLabel("Zuordnungen");
 		pnl_zuordnungen.add(lblZuordnungen, BorderLayout.NORTH);
 
 		JScrollPane scrollPane_1 = new JScrollPane(list1, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pnl_zuordnungen.add(scrollPane_1, BorderLayout.CENTER);
 
 	}
 
 	// top frame part
 	private void topscr() {
 		JPanel top = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) top.getLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		frame.getContentPane().add(top, BorderLayout.NORTH);
 
 		JLabel lblMMS = new JLabel("Modul Management System");
 		lblMMS.setFont(new Font("Tahoma", Font.BOLD, 16));
 		lblMMS.setHorizontalAlignment(SwingConstants.LEFT);
 		lblMMS.setLabelFor(frame);
 		top.add(lblMMS);
 	}
 
 	// funktionen zum hinzufuegen von Element in die jeweiligen Tabellen
 	private void addToTable(User usr) {
 		tmodel.addRow(new Object[] { usr.getTitel(), usr.getVorname(), usr.getNachname(), usr.geteMail(),
 				usr.getManageUsers(), usr.getCreateModule(), usr.getAcceptModule(), usr.getReadModule() });
 	}
 
 	private void addToTable(Studiengang stud) {
 		studmodel.addRow(new Object[] { stud.getName() });
 	}
 
 	private void addToTable(Modul mod) {
 		modshowmodel.addRow(new Object[] { mod.getName() });
 	}
 
 	private void addToTable(Modulhandbuch modbuch) {
 		modbuchmodel.addRow(new Object[] { modbuch.getJahrgang() });
 	}
 
 	private void addToTable(String modtyp) {
 		modtypmodel.addRow(new Object[] { modtyp });
 	}
 
 	private JPanel defaultmodulPanel(String name, String string, boolean b) {
 		final Dimension preferredSize = new Dimension(120, 20);
 
 		JPanel pnl = new JPanel();
 		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
 
 		JLabel label = new JLabel(name);
 		label.setPreferredSize(preferredSize);
 		pnl.add(label);
 
 		JTextArea txt = new JTextArea(string);
 		txt.setLineWrap(true);
 		pnl.add(txt);
 
 		if (!name.equals("Jahrgang")) {
 			JCheckBox dez = new JCheckBox("Dezernat 2", b);
 			pnl.add(dez);
 		} 
 		return pnl;
 	}
 
 	private JPanel modulPanel(String name, String string) {
 		final Dimension preferredSize = new Dimension(120, 20);
 
 		JPanel pnl = new JPanel();
 		pnl.setLayout(new GridLayout(1, 2, 0, 0));
 		pnl.setBorder(BorderFactory.createLineBorder(Color.black));
 		JLabel label = new JLabel(name);
 		label.setPreferredSize(preferredSize);
 		pnl.add(label);
 
 		JLabel txt = new JLabel(string);
 		txt.setPreferredSize(preferredSize);
 		pnl.add(txt);
 
 		return pnl;
 	}
 
 	private void homecard() {
 		JPanel welcome = new JPanel();
 		FlowLayout flowLayout_2 = (FlowLayout) welcome.getLayout();
 		flowLayout_2.setVgap(20);
 		cards.add(welcome, "welcome page");
 
 		JLabel lblNewLabel = new JLabel("Willkommen beim Modul Management System");
 		welcome.add(lblNewLabel);
 
 	}
 
 	private void leftscr() {
 		JPanel leftpan = new JPanel();
 		frame.getContentPane().add(leftpan, BorderLayout.WEST);
 
 		JPanel left = new JPanel();
 		leftpan.add(left);
 		left.setLayout(new GridLayout(0, 1, 5, 20));
 
 		left.add(btnModulEinreichen);
 		btnModulEinreichen.setEnabled(false);
 		btnModulEinreichen.setPreferredSize(btnSz);
 		btnModulEinreichen.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnModulEinreichen.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				typen = database.getZuordnungen();
 				studienlist = database.getStudiengaenge();
 				cbmodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					cbmodel.addElement(studienlist.get(i));
 				}
 				cbmodel_Z.removeAllElements();
 				for (int i = 0; i < typen.size(); i++) {
 					cbmodel_Z.addElement(typen.get(i));
 				}
 				showCard("newmodule");
 			}
 
 		});
 
 		left.add(btnModulBearbeiten);
 		btnModulBearbeiten.setEnabled(false);
 		btnModulBearbeiten.setPreferredSize(btnSz);
 		btnModulBearbeiten.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		btnModulBearbeiten.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				ArrayList<Modul> module = database.getModule(false);
 				lm.removeAllElements();
 				for (int i = 0; i < module.size(); i++) {
 					lm.addElement(module.get(i));
 				}
 
 				module = database.getModule(true);
 				lm_ack.removeAllElements();
 				for (int i = 0; i < module.size(); i++) {
 					lm_ack.addElement(module.get(i));
 				}
 				showCard("modulbearbeiten");
 			}
 
 		});
 
 		left.add(btnLogin);
 		btnLogin.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				current = database.login(current.geteMail(), current.getPassword());
 				if (current != null) {
 					if (current.geteMail().equals("gast@gast.gast")) {
 						logindialog log = new logindialog(frame, "Login", database);
 						int resp = log.showCustomDialog();
 						if (resp == 1) {
 							current = log.getUser();
 							database = log.getServerConnection();
 							btnLogin.setText("Ausloggen");
 							checkRights();
 						}
 					} else {
 						current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb",
 								false, false, false, false, true);
 						if (database.isConnected() == SUCCES) {
 							checkRights();
 						}
 						btnLogin.setText("Einloggen");
 						btnUserVerwaltung.setText("User Verwaltung");
 						btnUserVerwaltung.setEnabled(false);
 						showCard("welcome page");
 					}
 				} else {
 					noConnection();
 				}
 			}
 		});
 		btnLogin.setPreferredSize(btnSz);
 		btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		btnUserVerwaltung.setEnabled(false);
 		left.add(btnUserVerwaltung);
 		btnUserVerwaltung.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (current.getManageUsers()) {
 					// Tabelle leeren
 					tmodel.setRowCount(0);
 
 					// Tabelle mit neuen daten fllen
 					worklist = database.userload();
 					for (int i = 0; i < worklist.size(); i++) {
 						if (worklist.get(i).isFreigeschaltet())
 							addToTable(worklist.get(i));
 						else
 							neueUser.add(worklist.get(i));
 					}
 					showCard("user managment");
 					for (int i = 0; i < neueUser.size(); i++) {
 
 						userdialog dlg = new userdialog(frame, "User besttigen", neueUser.get(i), true, database);
 						int response = dlg.showCustomDialog();
 						// Wenn ok gedrckt wird
 						// neuen User abfragen
 						User tmp = dlg.getUser();
 						if (response == 1) {
 							tmp.setFreigeschaltet(true);
 							if(SendMail.send(current.geteMail(),neueUser.get(i).geteMail(),"Sie wurden freigeschaltet!")==1){
 								tmp.setFreigeschaltet(true);
								System.out.println(database.userupdate(tmp, tmp.geteMail()).getStatus());
								if(database.userupdate(tmp, tmp.geteMail()).getStatus()==1){
 									addToTable(tmp);
 									neueUser.remove(i);
 								}
 							}
 						} else{
 							int n = JOptionPane.showConfirmDialog(frame,
 									"Mchten Sie diesen Benutzer lschen", "Besttigung",
 									JOptionPane.YES_NO_OPTION);
 							if (n == 0) {
 								database.deluser(tmp.geteMail());
 							}
 						}
 					}
 				} else {
 					userdialog dlg = new userdialog(frame, "User bearbeiten", current, false, database);
 					int response = dlg.showCustomDialog();
 					// Wenn ok gedrckt wird
 					// neuen User abfragen
 					if (response == 1) {
 						User tmp = dlg.getUser();
 						if (database.userupdate(tmp, current.geteMail()).getStatus() == 201) {
 							current = tmp;
 							checkRights();
 						} else
 							JOptionPane.showMessageDialog(frame, "Update Fehlgeschlagen!", "Update Error",
 									JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 			}
 		});
 		btnUserVerwaltung.setPreferredSize(btnSz);
 		btnUserVerwaltung.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnModulVerwaltung.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				studienlist = database.getStudiengaenge();
 				studimodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					studimodel.addElement(studienlist.get(i));
 				}
 				cbmodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					cbmodel.addElement(studienlist.get(i));
 				}
 				typen = database.getZuordnungen();
 				for (int i = 0; i < typen.size(); i++) {
 					typenmodel.addElement(typen.get(i));
 				}
 				showCard("manage");
 
 			}
 		});
 
 		left.add(btnModulVerwaltung);
 		btnModulVerwaltung.setEnabled(false);
 		btnModulVerwaltung.setPreferredSize(btnSz);
 		btnModulVerwaltung.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		left.add(btnMHB);
 		btnMHB.setEnabled(true);
 		btnMHB.setPreferredSize(btnSz);
 		btnMHB.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnMHB.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				current = database.login(current.geteMail(), current.getPassword());
 				if (current != null) {
 					studmodel.setRowCount(0);
 					studienlist = database.getStudiengaenge();
 					for (int i = 0; i < studienlist.size(); i++) {
 						addToTable(studienlist.get(i));
 					}
 
 					typen = database.getZuordnungen();
 
 					showCard("studiengang show");
 				} else {
 					noConnection();
 				}
 			}
 
 		});
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
 	private void newmodulecard() {
 		modul_panel.removeAll();
 		final JPanel pnl_newmod = new JPanel();
 		if (!buttonmap.isEmpty()) {
 			for (int i = 0; i < buttonmap.size(); i++)
 				buttonmap.remove(i);
 		}
 		final ArrayList<String> labels = new ArrayList<String>();
 		labels.addAll(defaultlabels);
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_newmod.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_bottom = new JPanel();
 		pnl_newmod.add(pnl_bottom, BorderLayout.SOUTH);
 
 		JButton btnNeuesFeld = new JButton("Neues Feld");
 		btnNeuesFeld.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String text = "Name des Feldes";
 				String name = JOptionPane.showInputDialog(frame, text);
 				try {
 					while (name.isEmpty() || labels.contains(name)) {
 						Object[] params = { "Bitte geben Sie eine gltige Bezeichnung ein!", text };
 						name = JOptionPane.showInputDialog(frame, params);
 					}
 					labels.add(name);
 					// Platzhalter
 					JPanel pnl_tmp = new JPanel();
 					modul_panel.add(pnl_tmp);
 					modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 					int numOfPanels = modul_panel.getComponentCount();
 					pnl_tmp.setLayout(new BoxLayout(pnl_tmp, BoxLayout.X_AXIS));
 
 					JLabel label_tmp = new JLabel(name);
 					label_tmp.setPreferredSize(preferredSize);
 					pnl_tmp.add(label_tmp);
 
 					JTextArea txt_tmp = new JTextArea();
 					txt_tmp.setLineWrap(true);
 					pnl_tmp.add(txt_tmp);
 
 					JCheckBox dez = new JCheckBox("Dezernat 2", false);
 					pnl_tmp.add(dez);
 
 					JButton btn_tmp_entf = new JButton("Entfernen");
 					btn_tmp_entf.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							int id = buttonmap.get(e.getSource());
 							// Bezeichnung aus Liste entfernen
 							String name = ((JLabel) ((JPanel) modul_panel.getComponent(id)).getComponent(0)).getText();
 							labels.remove(name);
 
 							// Feld mit ID id von Panel entfernen
 							modul_panel.remove(id);
 							// Platzhalter entfernen
 							modul_panel.remove(id - 1);
 							// Aus ButtonMap entfernen
 							buttonmap.remove(e.getSource());
 
 							// ids der Buttons ndern, damit auch ein Feld aus
 							// der Mitte gelcht werden kann
 							HashMap<JButton, Integer> tmpmap = new HashMap<JButton, Integer>();
 							Iterator<Entry<JButton, Integer>> entries = buttonmap.entrySet().iterator();
 							while (entries.hasNext()) {
 								Entry<JButton, Integer> thisEntry = entries.next();
 								JButton key = thisEntry.getKey();
 								int value = thisEntry.getValue();
 								if (value > id) {
 									value = value - 2;
 								}
 								tmpmap.put(key, value);
 							}
 							buttonmap = tmpmap;
 							modul_panel.revalidate();
 
 						}
 					});
 
 					// Button btn_tmp_entf mit ID (numOfPanels-2) zu ButtonMap
 					buttonmap.put(btn_tmp_entf, numOfPanels - 2);
 
 					pnl_tmp.add(btn_tmp_entf);
 
 					modul_panel.revalidate();
 
 				} catch (NullPointerException npe) {
 					// nichts tuen
 				}
 			}
 		});
 		pnl_bottom.add(btnNeuesFeld);
 
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				modul_panel.removeAll();
 				modul_panel.revalidate();
 				newmodulecard();
 				showCard("welcome page");
 			}
 		});
 		pnl_bottom.add(btnHome);
 
 		JScrollPane scrollPane = new JScrollPane(modul_panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		modul_panel.setLayout(new BoxLayout(modul_panel, BoxLayout.Y_AXIS));
 
 		// Panel Zuordnung + Platzhalter
 		JPanel pnl_Z = new JPanel();
 		pnl_Z.setLayout(new BoxLayout(pnl_Z, BoxLayout.X_AXIS));
 		JLabel label_MH = new JLabel("Zuordnung");
 
 		label_MH.setPreferredSize(preferredSize);
 		pnl_Z.add(label_MH);
 
 		final DefaultListModel<Zuordnung> lm = new DefaultListModel<Zuordnung>();
 		final JList<Zuordnung> zlist = new JList<Zuordnung>(lm);
 		zlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		pnl_Z.add(zlist);
 
 		final JComboBox cb_Z = new JComboBox(cbmodel_Z);
 		cb_Z.setMaximumSize(new Dimension(400, 20));
 
 		pnl_Z.add(cb_Z);
 
 		JButton z_btn = new JButton("Zuordnung ausw\u00e4hlen");
 		z_btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!lm.contains((Zuordnung) cb_Z.getSelectedItem()))
 					lm.addElement((Zuordnung) cb_Z.getSelectedItem());
 			}
 		});
 		pnl_Z.add(z_btn);
 
 		modul_panel.add(pnl_Z);
 
 		JButton btnZuordnungEntfernen = new JButton("Zuordnung entfernen");
 		btnZuordnungEntfernen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int i = zlist.getSelectedIndex();
 				if (i > -1) {
 					lm.remove(i);
 				}
 			}
 		});
 		pnl_Z.add(btnZuordnungEntfernen);
 		modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		for (int i = 3; i < defaultlabels.size(); i++) {
 			modul_panel.add(defaultmodulPanel(defaultlabels.get(i), "", false));
 			modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 		}
 
 		JButton btnOk = new JButton("Annehmen");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<Zuordnung> zlist = new ArrayList<Zuordnung>();
 				String jg = ((JTextArea) ((JPanel) modul_panel.getComponent(2)).getComponent(1)).getText();
 				int jahrgang;
 				try {
 					jahrgang = Integer.parseInt(jg);
 				} catch (NumberFormatException nfe) {
 					jahrgang = 0;
 				}
 				for (int i = 0; i < lm.getSize(); i++) {
 					zlist.add(lm.getElementAt(i));
 				}
 
 				if (!zlist.isEmpty()) {
 
 					if (jahrgang != 0) {
 
 						String Name = ((JTextArea) ((JPanel) modul_panel.getComponent(4)).getComponent(1)).getText();
 
 						if (Name.isEmpty()) {
 							JOptionPane.showMessageDialog(frame, "Bitte fllen Sie alle Felder aus!", "Eingabe Fehler",
 									JOptionPane.ERROR_MESSAGE);
 						} else {
 
 							boolean filled = true;
 							ArrayList<Feld> felder = new ArrayList<Feld>();
 							// Eintraege der Reihe nach auslesen
 							for (int i = 6; i < modul_panel.getComponentCount(); i = i + 2) {
 								JPanel tmp = (JPanel) modul_panel.getComponent(i);
 								JLabel tmplbl = (JLabel) tmp.getComponent(0);
 								JTextArea tmptxt = (JTextArea) tmp.getComponent(1);
 
 								boolean dezernat2 = ((JCheckBox) tmp.getComponent(2)).isSelected();
 								String value = tmptxt.getText();
 								String label = tmplbl.getText();
 								if (label.isEmpty()) {
 									filled = false;
 									break;
 								}
 								felder.add(new Feld(label, value, dezernat2));
 							}
 							if (filled == true) {
 								int version = database.getModulVersion(Name) + 1;
 
 								Date d = new Date();
 
 								Modul neu = new Modul(Name, zlist, jahrgang, felder, version, d, false, false, current
 										.geteMail());
 								int n = JOptionPane.showConfirmDialog(frame,
 										"Sind Sie sicher, dass Sie dieses Modul einreichen wollen?", "Besttigung",
 										JOptionPane.YES_NO_OPTION);
 								if (n == 0) {
 									database.setModul(neu);
 									labels.removeAll(labels);
 									modul_panel.removeAll();
 									modul_panel.revalidate();
 									newmodulecard();
 									showCard("newmodule");
 								}
 							} else {
 								JOptionPane.showMessageDialog(frame, "Bitte fllen Sie alle Felder aus!",
 										"Eingabe Fehler", JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame,
 								"Bitte geben Sie einen gltigen Wert fr den Jahrgang ein!", "Eingabe Fehler",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				} else {
 					JOptionPane.showMessageDialog(frame, "Bitte whlen Sie min. einen Zuordnung aus!",
 							"Eingabe Fehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		pnl_bottom.add(btnOk);
 
 		pnl_newmod.add(scrollPane);
 		cards.add(pnl_newmod, "newmodule");
 
 	}
 
 	private void removeFromTable(int rowid) {
 		tmodel.removeRow(rowid);
 	}
 
 	private static void showCard(String card) {
 		((CardLayout) cards.getLayout()).show(cards, card);
 	}
 
 	@SuppressWarnings("serial")
 	private void usermgtcard() {
 		JPanel usrmg = new JPanel();
 		cards.add(usrmg, "user managment");
 		usrmg.setLayout(new BorderLayout(0, 0));
 
 		JPanel usrpan = new JPanel();
 		FlowLayout fl_usrpan = (FlowLayout) usrpan.getLayout();
 		fl_usrpan.setAlignment(FlowLayout.RIGHT);
 		usrmg.add(usrpan, BorderLayout.SOUTH);
 
 		final JTable usrtbl = new JTable();
 		JScrollPane ussrscp = new JScrollPane(usrtbl);
 		usrtbl.setBorder(new LineBorder(new Color(0, 0, 0)));
 		usrtbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		//
 		// Inhalt der Tabelle
 		//
 		tmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Titel", "Vorname", "Nachname", "e-Mail",
 				"Benutzer verwalten", "Module einreichen", "Module Annehmen", "Verwaltung" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class, String.class, String.class, String.class, boolean.class,
 					boolean.class, boolean.class, boolean.class };
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 
 		usrtbl.setModel(tmodel);
 
 		JButton btnUserAdd = new JButton("User hinzuf\u00fcgen");
 		btnUserAdd.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				userdialog dlg = new userdialog(frame, "User hinzuf\u00fcgen", database);
 				int response = dlg.showCustomDialog();
 				// Wenn ok gedrckt wird
 				// neuen User abfragen
 				if (response == 1) {
 					User tmp = dlg.getUser();
 					tmp.setFreigeschaltet(true);
 					database.usersave(tmp);
 					addToTable(tmp);
 				}
 			}
 
 		});
 		usrpan.add(btnUserAdd);
 
 		JButton btnUserEdit = new JButton("User bearbeiten");
 		btnUserEdit.setToolTipText("Zum Bearbeiten Benutzer in der Tabelle markieren");
 		btnUserEdit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int row = usrtbl.getSelectedRow();
 				if (row != -1) {
 					String t = (String) usrtbl.getValueAt(row, 0);
 					String vn = (String) usrtbl.getValueAt(row, 1);
 					String nn = (String) usrtbl.getValueAt(row, 2);
 					String em = (String) usrtbl.getValueAt(row, 3);
 					boolean r1 = (boolean) usrtbl.getValueAt(row, 4);
 					boolean r2 = (boolean) usrtbl.getValueAt(row, 5);
 					boolean r3 = (boolean) usrtbl.getValueAt(row, 6);
 					boolean r4 = (boolean) usrtbl.getValueAt(row, 7);
 					User alt = new User(vn, nn, t, em, null, r1, r2, r3, r4, true);
 
 					userdialog dlg = new userdialog(frame, "User bearbeiten", alt, true, database);
 					int response = dlg.showCustomDialog();
 					// Wenn ok ged\u00fcckt wird
 					// neuen User abfragen
 					if (response == 1) {
 						User tmp = dlg.getUser();
 						tmp.setFreigeschaltet(true);
 						if (database.userupdate(tmp, em).getStatus() == 201) {
 							removeFromTable(row);
 							addToTable(tmp);
 							if (em.equals(current.geteMail())) {
 								current = tmp;
 								checkRights();
 							}
 						} else
 							JOptionPane.showMessageDialog(frame, "Update Fehlgeschlagen", "Update Fehler",
 									JOptionPane.ERROR_MESSAGE);
 
 					}
 
 				}
 			}
 		});
 		usrpan.add(btnUserEdit);
 
 		JButton btnUserDel = new JButton("User l\u00f6schen");
 		btnUserDel.setToolTipText("Zum L\u00f6schen Benutzer in der Tabelle markieren");
 		btnUserDel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int row = usrtbl.getSelectedRow();
 				if (row != -1) {
 					int n = JOptionPane.showConfirmDialog(frame,
 							"Sind Sie sicher, dass Sie diesen Benutzer l\u00f6schen wollen?", "Besttigung",
 							JOptionPane.YES_NO_OPTION);
 					if (n == 0) {
 						if (database.deluser((String) usrtbl.getValueAt(row, 3)).getStatus() != 201) {
 							removeFromTable(row);
 						} else
 							JOptionPane.showMessageDialog(frame, "L\u00f6schen Fehlgeschlagen",
 									"Fehler beim L\u00f6schen", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		usrpan.add(btnUserDel);
 
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		usrpan.add(btnHome);
 
 		JPanel usrcenter = new JPanel();
 		usrmg.add(usrcenter, BorderLayout.CENTER);
 		usrcenter.setLayout(new BorderLayout(5, 5));
 
 		usrcenter.add(ussrscp);
 		JPanel leftpan = new JPanel();
 		frame.getContentPane().add(leftpan, BorderLayout.WEST);
 
 	}
 
 	protected void checkRights() {
 		btnModulEinreichen.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnModulVerwaltung.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnUserVerwaltung.setEnabled(false);
 
 		if (current.getCreateModule()) {
 			btnModulEinreichen.setEnabled(true);
 		}
 		if (current.getAcceptModule()) {
 			btnModulBearbeiten.setEnabled(true);
 		}
 		if (current.getReadModule()) {
 			btnModulVerwaltung.setEnabled(true);
 		}
 		if (current.getManageUsers()) {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("User Verwaltung");
 		} else {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("Account bearbeiten");
 			showCard("welcome page");
 		}
 	}
 
 	public void modulbearbeitenCard() {
 
 		JPanel pnl_modedit = new JPanel();
 		pnl_modedit.setLayout(new BorderLayout(0, 0));
 		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
 		pnl_modedit.add(tabs);
 
 		JPanel nichtakzeptiert = new JPanel();
 		tabs.addTab("Noch nicht akzeptierte Module", null, nichtakzeptiert, null);
 		nichtakzeptiert.setLayout(new BorderLayout(0, 0));
 		final JList<Modul> list_notack = new JList<Modul>(lm);
 		list_notack.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list_notack.setLayoutOrientation(JList.VERTICAL_WRAP);
 
 		nichtakzeptiert.add(list_notack);
 
 		JPanel buttonpnl = new JPanel();
 		nichtakzeptiert.add(buttonpnl, BorderLayout.SOUTH);
 
 		JButton btnModulBearbeiten = new JButton("Modul bearbeiten");
 		btnModulBearbeiten.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Modul m = list_notack.getSelectedValue();
 				m.setInbearbeitung(database.getModulInEdit(m.getName()));
 				if (m != null) {
 					if (!m.isInbearbeitung()) {
 						boolean rights = false;
 						if (m.getUser().equals(current.geteMail())) {
 							rights = true;
 						} else {
 							ArrayList<String> rel = database.getUserRelation(current.geteMail());
 							if (rel.contains(m.getUser())) {
 								rights = true;
 							}
 						}
 						if (rights) {
 							mod.removeAll();
 							mod.add(modeditCard(m), BorderLayout.CENTER);
 							m.setInbearbeitung(true);
 							database.setModulInEdit(m);
 							showCard("modBearbeiten");
 						} else {
 							JOptionPane.showMessageDialog(frame,
 									"Sie besitzen nicht die n\u00f6tigen Rechte, um dieses Modul zu bearbeiten!",
 									"Zugriff verweigert", JOptionPane.ERROR_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!",
 								"Zugriff verweigert", JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 
 			}
 		});
 		buttonpnl.add(btnModulBearbeiten);
 
 		JButton btnModulAkzeptieren = new JButton("Modul akzeptieren");
 		btnModulAkzeptieren.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Modul m = list_notack.getSelectedValue();
 				if (m != null) {
 					m.setInbearbeitung(database.getModulInEdit(m.getName()));
 					if (!m.isInbearbeitung()) {
 						if (m.getName().isEmpty()) {
 							JOptionPane.showMessageDialog(frame, "Bei diesem Modul sind nicht alle Felder ausgefllt!",
 									"Fehler im Modul", JOptionPane.ERROR_MESSAGE);
 						} else {
 							boolean hasDezernat = false;
 							boolean isCorrect = true;
 							ArrayList<Feld> felder = m.getFelder();
 							for (int i = 0; i < felder.size(); i++) {
 								if (felder.get(i).getValue().isEmpty()) {
 									JOptionPane.showMessageDialog(frame,
 											"Bei diesem Modul sind nicht alle Felder ausgefllt!", "Fehler im Modul",
 											JOptionPane.ERROR_MESSAGE);
 									isCorrect = false;
 									break;
 								}
 								if (felder.get(i).isDezernat()) {
 									hasDezernat = true;
 								}
 							}
 
 							if (isCorrect) {
 								boolean checked = true;
 								if (hasDezernat) {
 									int n = JOptionPane
 											.showConfirmDialog(
 													frame,
 													"Dieses Modul besitzt Felder, die vom Dezernat2 berprft werden mssen, wurde das getan?",
 													"Besttigung", JOptionPane.YES_NO_OPTION);
 									if (n == 0) {
 										checked = true;
 									} else {
 										checked = false;
 									}
 								}
 								if (checked) {
 									database.acceptModul(m);
 									ArrayList<Modul> module = database.getModule(false);
 									lm.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm.addElement(module.get(i));
 									}
 
 									module = database.getModule(true);
 									lm_ack.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm_ack.addElement(module.get(i));
 									}
 									showCard("modulbearbeiten");
 								}
 							}
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!",
 								"Zugriff verweigert", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		buttonpnl.add(btnModulAkzeptieren);
 
 		JButton btnZurck = new JButton("Zur\u00FCck");
 		btnZurck.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttonpnl.add(btnZurck);
 
 		JPanel akzeptiert = new JPanel();
 		tabs.addTab("akzeptierte Module", null, akzeptiert, null);
 		tabs.setEnabledAt(1, true);
 		akzeptiert.setLayout(new BorderLayout(0, 0));
 
 		final JList<Modul> list_ack = new JList<Modul>(lm_ack);
 		list_ack.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list_ack.setLayoutOrientation(JList.VERTICAL_WRAP);
 		akzeptiert.add(list_ack);
 
 		JPanel buttonpnl2 = new JPanel();
 		akzeptiert.add(buttonpnl2, BorderLayout.SOUTH);
 
 		JButton btnModulBearbeiten2 = new JButton("Modul bearbeiten");
 		btnModulBearbeiten2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Modul m = list_ack.getSelectedValue();
 				if (m != null) {
 					m.setInbearbeitung(database.getModulInEdit(m.getName()));
 					if (!m.isInbearbeitung()) {
 						boolean rights = false;
 						if (m.getUser().equals(current.geteMail())) {
 							rights = true;
 						} else {
 							ArrayList<String> rel = database.getUserRelation(current.geteMail());
 							if (rel.contains(m.getUser())) {
 								rights = true;
 							}
 						}
 						if (rights) {
 							mod.removeAll();
 							mod.add(modeditCard(m), BorderLayout.CENTER);
 							showCard("modBearbeiten");
 						} else {
 							JOptionPane.showMessageDialog(frame,
 									"Sie besitzen nicht die n\u00f6tigen Rechte, um dieses Modul zu bearbeiten!",
 									"Zugriff verweigert", JOptionPane.ERROR_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!",
 								"Zugriff verweigert", JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 
 			}
 		});
 		buttonpnl2.add(btnModulBearbeiten2);
 
 		JButton btnZurck2 = new JButton("Zur\u00FCck");
 		btnZurck2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttonpnl2.add(btnZurck2);
 		cards.add(pnl_modedit, "modulbearbeiten");
 
 	}
 
 	private JPanel modeditCardPrev(Modul m) {
 		final JPanel pnl_editmod = new JPanel();
 		final JPanel pnl_mod_prev = new JPanel();
 
 		final ArrayList<Feld> felder = m.getFelder();
 		final ArrayList<String> labels = new ArrayList<String>();
 		for (int i = 0; i < felder.size(); i++) {
 			labels.add(felder.get(i).getLabel());
 		}
 
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_editmod.setLayout(new BorderLayout(0, 0));
 
 		JScrollPane scrollPane = new JScrollPane(pnl_mod_prev, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pnl_mod_prev.setLayout(new BoxLayout(pnl_mod_prev, BoxLayout.Y_AXIS));
 
 		// Panel Zuordnung + Platzhalter
 		JPanel pnl_Z = new JPanel();
 		pnl_Z.setLayout(new BoxLayout(pnl_Z, BoxLayout.X_AXIS));
 		JLabel label_MH = new JLabel("Zuordnung");
 
 		label_MH.setPreferredSize(preferredSize);
 		pnl_Z.add(label_MH);
 
 		final DefaultListModel<Zuordnung> lm = new DefaultListModel<Zuordnung>();
 		typen = m.getZuordnungen();
 		for (int i = 0; i < typen.size(); i++) {
 			lm.addElement(typen.get(i));
 		}
 		JList<Zuordnung> zlist = new JList<Zuordnung>(lm);
 		pnl_Z.add(zlist);
 
 		pnl_mod_prev.add(pnl_Z);
 		pnl_mod_prev.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		JPanel jg = new JPanel();
 		jg.setLayout(new BoxLayout(jg, BoxLayout.X_AXIS));
 
 		JLabel lbl_jg = new JLabel("Jahrgang");
 		lbl_jg.setPreferredSize(preferredSize);
 		jg.add(lbl_jg);
 
 		JTextArea txt_jg = new JTextArea(m.getJahrgang() + "");
 		txt_jg.setLineWrap(true);
 		txt_jg.setEditable(false);
 		jg.add(txt_jg);
 
 		pnl_mod_prev.add(jg);
 		pnl_mod_prev.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		JPanel name = new JPanel();
 		name.setLayout(new BoxLayout(name, BoxLayout.X_AXIS));
 
 		JLabel lbl_n = new JLabel("Name");
 		lbl_n.setPreferredSize(preferredSize);
 		name.add(lbl_n);
 
 		JTextArea txt_n = new JTextArea(m.getName());
 		txt_n.setLineWrap(true);
 		txt_n.setEditable(false);
 		name.add(txt_n);
 
 		pnl_mod_prev.add(name);
 		pnl_mod_prev.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		for (int i = 0; i < m.getFelder().size(); i++) {
 			Feld f = m.getFelder().get(i);
 
 			JPanel pnl = new JPanel();
 			pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
 
 			JLabel label = new JLabel(f.getLabel());
 			label.setPreferredSize(preferredSize);
 			pnl.add(label);
 
 			JTextArea txt = new JTextArea(f.getValue());
 			txt.setLineWrap(true);
 			txt.setEditable(false);
 			pnl.add(txt);
 
 			JCheckBox dez = new JCheckBox("Dezernat 2", f.isDezernat());
 			dez.setEnabled(false);
 			pnl.add(dez);
 
 			pnl_mod_prev.add(pnl);
 			pnl_mod_prev.add(Box.createRigidArea(new Dimension(0, 5)));
 		}
 
 		pnl_editmod.add(scrollPane);
 
 		return pnl_editmod;
 
 	}
 
 	private JPanel modeditCard(final Modul m) {
 		final JPanel pnl_editmod = new JPanel();
 		modul_panel_edit.removeAll();
 		if (!buttonmap.isEmpty()) {
 			for (int i = 0; i < buttonmap.size(); i++)
 				buttonmap.remove(i);
 		}
 		final ArrayList<Feld> felder = m.getFelder();
 		final ArrayList<String> labels = new ArrayList<String>();
 		for (int i = 0; i < felder.size(); i++) {
 			labels.add(felder.get(i).getLabel());
 		}
 
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_editmod.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_bottom = new JPanel();
 		pnl_editmod.add(pnl_bottom, BorderLayout.SOUTH);
 
 		JButton alt = new JButton("Vorherige Version");
 		alt.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int v = m.getVersion() - 1;
 				if (v > 0) {
 					String name = m.getName();
 					Modul pre = database.getModul(name, v);
 					if (pre != null)
 						JOptionPane.showMessageDialog(frame, modeditCardPrev(pre), "Vorherige Version", 1);
 				} else {
 					JOptionPane.showMessageDialog(frame, "Keine Vorherige Version vorhanden!", "Fehler",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		pnl_bottom.add(alt);
 
 		JButton btnNeuesFeld = new JButton("Neues Feld");
 		btnNeuesFeld.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String text = "Name des Feldes";
 				String name = JOptionPane.showInputDialog(frame, text);
 				try {
 					while (name.isEmpty() || labels.contains(name)) {
 						Object[] params = { "Bitte geben Sie eine gltige Bezeichnung ein!", text };
 						name = JOptionPane.showInputDialog(frame, params);
 					}
 					labels.add(name);
 					// Platzhalter
 					JPanel pnl_tmp = new JPanel();
 					modul_panel_edit.add(pnl_tmp);
 					modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
 					int numOfPanels = modul_panel_edit.getComponentCount();
 					pnl_tmp.setLayout(new BoxLayout(pnl_tmp, BoxLayout.X_AXIS));
 
 					JLabel label_tmp = new JLabel(name);
 					label_tmp.setPreferredSize(preferredSize);
 					pnl_tmp.add(label_tmp);
 
 					JTextArea txt_tmp = new JTextArea();
 					txt_tmp.setLineWrap(true);
 					pnl_tmp.add(txt_tmp);
 
 					JCheckBox dez = new JCheckBox("Dezernat 2", false);
 					pnl_tmp.add(dez);
 
 					JButton btn_tmp_entf = new JButton("Entfernen");
 					btn_tmp_entf.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							int id = buttonmap.get(e.getSource());
 							// Bezeichnung aus Liste entfernen
 							String name = ((JLabel) ((JPanel) modul_panel_edit.getComponent(id)).getComponent(0))
 									.getText();
 							labels.remove(name);
 
 							// Feld mit ID id von Panel entfernen
 							modul_panel_edit.remove(id);
 							// Platzhalter entfernen
 							modul_panel_edit.remove(id - 1);
 							// Aus ButtonMap entfernen
 							buttonmap.remove(e.getSource());
 
 							// ids der Buttons ndern, damit auch ein Feld aus
 							// der Mitte gelscht werden kann
 							HashMap<JButton, Integer> tmpmap = new HashMap<JButton, Integer>();
 							Iterator<Entry<JButton, Integer>> entries = buttonmap.entrySet().iterator();
 							while (entries.hasNext()) {
 								Entry<JButton, Integer> thisEntry = entries.next();
 								JButton key = thisEntry.getKey();
 								int value = thisEntry.getValue();
 								if (value > id) {
 									value = value - 2;
 								}
 								tmpmap.put(key, value);
 							}
 							buttonmap = tmpmap;
 							modul_panel_edit.revalidate();
 
 						}
 					});
 
 					// Button btn_tmp_entf mit ID (numOfPanels-2) zu ButtonMap
 					buttonmap.put(btn_tmp_entf, numOfPanels - 2);
 
 					pnl_tmp.add(btn_tmp_entf);
 
 					modul_panel_edit.revalidate();
 
 				} catch (NullPointerException npe) {
 					// nichts tuen
 				}
 			}
 		});
 		pnl_bottom.add(btnNeuesFeld);
 
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				modul_panel_edit.removeAll();
 				modul_panel_edit.revalidate();
 				m.setInbearbeitung(false);
 				database.setModulInEdit(m);
 				modulbearbeitenCard();
 				showCard("modulbearbeiten");
 			}
 		});
 		pnl_bottom.add(btnHome);
 
 		JScrollPane scrollPane = new JScrollPane(modul_panel_edit, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		modul_panel_edit.setLayout(new BoxLayout(modul_panel_edit, BoxLayout.Y_AXIS));
 
 		// Panel Zuordnung + Platzhalter
 		JPanel pnl_Z = new JPanel();
 		pnl_Z.setLayout(new BoxLayout(pnl_Z, BoxLayout.X_AXIS));
 		JLabel label_MH = new JLabel("Zuordnung");
 
 		label_MH.setPreferredSize(preferredSize);
 		pnl_Z.add(label_MH);
 
 		final DefaultListModel<Zuordnung> lm_Z = new DefaultListModel<Zuordnung>();
 		typen = m.getZuordnungen();
 		for (int i = 0; i < typen.size(); i++) {
 			lm_Z.addElement(typen.get(i));
 		}
 		final JList<Zuordnung> zlist = new JList<Zuordnung>(lm_Z);
 		zlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		pnl_Z.add(zlist);
 		typen = database.getZuordnungen();
 		cbmodel_Z.removeAllElements();
 		for (int i = 0; i < typen.size(); i++) {
 			cbmodel_Z.addElement(typen.get(i));
 		}
 		final JComboBox cb_Z = new JComboBox(cbmodel_Z);
 		cb_Z.setMaximumSize(new Dimension(cb_Z.getMaximumSize().width, 20));
 
 		pnl_Z.add(cb_Z);
 
 		JButton z_btn = new JButton("Zuordnung ausw\u00e4hlen");
 		z_btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!lm_Z.contains((Zuordnung) cb_Z.getSelectedItem()))
 					lm_Z.addElement((Zuordnung) cb_Z.getSelectedItem());
 			}
 		});
 		pnl_Z.add(z_btn);
 
 		JButton btnZuordnungEntfernen = new JButton("Zuordnung entfernen");
 		btnZuordnungEntfernen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int i = zlist.getSelectedIndex();
 				if (i > -1) {
 					lm_Z.remove(i);
 				}
 			}
 		});
 		pnl_Z.add(btnZuordnungEntfernen);
 
 		modul_panel_edit.add(pnl_Z);
 		modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		modul_panel_edit.add(defaultmodulPanel("Jahrgang", m.getJahrgang() + "", false));
 		modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		JPanel pnl = new JPanel();
 		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
 
 		JLabel label = new JLabel("Name");
 		label.setPreferredSize(preferredSize);
 		pnl.add(label);
 
 		JTextArea txt = new JTextArea(m.getName());
 		txt.setLineWrap(true);
 		pnl.add(txt);
 		txt.setEditable(false);
 		
 		modul_panel_edit.add(pnl);
 		modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		for (int i = 0; i < m.getFelder().size(); i++) {
 			Feld f = m.getFelder().get(i);
 			JPanel feld = defaultmodulPanel(f.getLabel(), f.getValue(), f.isDezernat());
 
 			if (!defaultlabels.contains(f.getLabel())) {
 				int numOfPanels = modul_panel_edit.getComponentCount();
 
 				JButton btn_tmp_entf = new JButton("Entfernen");
 				btn_tmp_entf.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						int id = buttonmap.get(e.getSource());
 						// Bezeichnung aus Liste entfernen
 						String name = ((JLabel) ((JPanel) modul_panel_edit.getComponent(id)).getComponent(0)).getText();
 						labels.remove(name);
 						// Feld mit ID id von Panel entfernen
 						modul_panel_edit.remove(id);
 						// Platzhalter entfernen
 						modul_panel_edit.remove(id - 1);
 						// Aus ButtonMap entfernen
 						buttonmap.remove(e.getSource());
 
 						// ids der Buttons ndern, damit auch ein Feld aus
 						// der Mitte gelscht werden kann
 						HashMap<JButton, Integer> tmpmap = new HashMap<JButton, Integer>();
 						Iterator<Entry<JButton, Integer>> entries = buttonmap.entrySet().iterator();
 						while (entries.hasNext()) {
 							Entry<JButton, Integer> thisEntry = entries.next();
 							JButton key = thisEntry.getKey();
 							int value = thisEntry.getValue();
 							if (value > id) {
 								value = value - 2;
 							}
 							tmpmap.put(key, value);
 						}
 						buttonmap = tmpmap;
 						modul_panel_edit.revalidate();
 						modul_panel_edit.repaint();
 
 					}
 				});
 				feld.add(btn_tmp_entf);
 				// Button btn_tmp_entf mit ID (numOfPanels-2) zu ButtonMap
 				buttonmap.put(btn_tmp_entf, numOfPanels);
 			}
 			modul_panel_edit.add(feld);
 			modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 		}
 
 		JButton btnOk = new JButton("Annehmen");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<Zuordnung> zlist = new ArrayList<Zuordnung>();
 				String jg = ((JTextArea) ((JPanel) modul_panel_edit.getComponent(2)).getComponent(1)).getText();
 				int jahrgang;
 				try {
 					jahrgang = Integer.parseInt(jg);
 				} catch (NumberFormatException nfe) {
 					jahrgang = 0;
 				}
 
 				for (int i = 0; i < lm_Z.getSize(); i++) {
 					zlist.add(lm_Z.getElementAt(i));
 				}
 
 				if (!zlist.isEmpty()) {
 
 					if (jahrgang != 0) {
 
 						String Name = ((JTextArea) ((JPanel) modul_panel_edit.getComponent(4)).getComponent(1))
 								.getText();
 
 						if (Name.isEmpty()) {
 							JOptionPane.showMessageDialog(frame, "Bitte fllen Sie alle Felder aus!", "Eingabe Fehler",
 									JOptionPane.ERROR_MESSAGE);
 						} else {
 
 							boolean filled = true;
 							ArrayList<Feld> felder = new ArrayList<Feld>();
 							// Eintraege der Reihe nach auslesen
 							for (int i = 6; i < modul_panel_edit.getComponentCount(); i = i + 2) {
 								JPanel tmp = (JPanel) modul_panel_edit.getComponent(i);
 								JLabel tmplbl = (JLabel) tmp.getComponent(0);
 								JTextArea tmptxt = (JTextArea) tmp.getComponent(1);
 
 								boolean dezernat2 = ((JCheckBox) tmp.getComponent(2)).isSelected();
 								String value = tmptxt.getText();
 								String label = tmplbl.getText();
 								if (label.isEmpty()) {
 									filled = false;
 									break;
 								}
 								felder.add(new Feld(label, value, dezernat2));
 							}
 							if (filled == true) {
 
 								int version = database.getModulVersion(Name) + 1;
 
 								Date d = new Date();
 
 								Modul neu = new Modul(Name, zlist, jahrgang, felder, version, d, false, false, current
 										.geteMail());
 
 								int n = JOptionPane.showConfirmDialog(frame,
 										"Sind Sie sicher, dass Sie dieses Modul einreichen wollen?", "Besttigung",
 										JOptionPane.YES_NO_OPTION);
 								if (n == 0) {
 									m.setInbearbeitung(false);
 									database.setModul(neu);
 									labels.removeAll(labels);
 									modul_panel_edit.removeAll();
 									modul_panel_edit.revalidate();
 
 									ArrayList<Modul> module = database.getModule(false);
 									lm.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm.addElement(module.get(i));
 									}
 
 									module = database.getModule(true);
 									lm_ack.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm_ack.addElement(module.get(i));
 									}
 									showCard("modulbearbeiten");
 								}
 							} else {
 
 							}
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame,
 								"Bitte geben Sie einen gltigen Wert fr den Jahrgang ein!", "Eingabe Fehler",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				} else {
 					JOptionPane.showMessageDialog(frame, "Bitte whlen Sie min. einen Zuordnung aus!",
 							"Eingabe Fehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		pnl_bottom.add(btnOk);
 
 		pnl_editmod.add(scrollPane);
 
 		return pnl_editmod;
 
 	}
 
 	@SuppressWarnings("serial")
 	private void studiengangCard() {
 
 		JPanel studiengangshow = new JPanel();
 		cards.add(studiengangshow, "studiengang show");
 		studiengangshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("oeffnen");
 		JButton back = new JButton("zurueck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable studtable = new JTable();
 		JScrollPane studscp = new JScrollPane(studtable);
 		studtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		studtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		studiengangshow.add(studscp);
 		studiengangshow.add(btnpan, BorderLayout.SOUTH);
 
 		studmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Studiengang" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class };
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 		studtable.setModel(studmodel);
 		studtransferstring = "";
 		goforit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (studtable.getSelectedRow() != -1) {
 					int openrow = studtable.getSelectedRow();
 					studtransferstring = (String) studtable.getValueAt(openrow, 0);
 					modhandshowCard();
 					showCard("modbuch show");
 				}
 			}
 		});
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 
 	}
 
 	@SuppressWarnings("serial")
 	private void modhandshowCard() {
 		JPanel modbuchshow = new JPanel();
 		cards.add(modbuchshow, "modbuch show");
 		modbuchshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("oeffnen");
 		JButton back = new JButton("zurueck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modbuchtable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modbuchtable);
 		modbuchtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modbuchtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modbuchshow.add(modtypscp);
 		modbuchshow.add(btnpan, BorderLayout.SOUTH);
 
 		modbuchmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Modulhandbuch Jahrgang" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class };
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 
 		modbuchtable.setModel(modbuchmodel);
 		modbuchmodel.setRowCount(0);
 		modulhandlist = database.getModulhandbuch(studtransferstring);
 		for (int i = 0; i < modulhandlist.size(); i++) {
 
 			addToTable(modulhandlist.get(i));
 
 		}
 		modbuchtransferstring = "";
 		goforit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (modbuchtable.getSelectedRow() != -1) {
 					int openrow = modbuchtable.getSelectedRow();
 					modbuchtransferstring = (String) modbuchtable.getValueAt(openrow, 0);
 					modtypshowCard();
 					showCard("modtyp show");
 				}
 			}
 		});
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("studiengang show");
 
 			}
 		});
 	}
 
 	@SuppressWarnings("serial")
 	private void modtypshowCard() {
 		JPanel modtypshow = new JPanel();
 		cards.add(modtypshow, "modtyp show");
 		modtypshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("oeffnen");
 		JButton back = new JButton("zurueck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modtyptable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modtyptable);
 		modtyptable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modtyptable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modtypshow.add(modtypscp);
 		modtypshow.add(btnpan, BorderLayout.SOUTH);
 
 		modtypmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Modul Typ" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class };
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 		modtyptable.setModel(modtypmodel);
 		modtypmodel.setRowCount(0);
 		int test = 0;
 		for (int i = 0; i < studienlist.size(); i++) {
 			if (studienlist.get(i).getName().equalsIgnoreCase(studtransferstring)) {
 				test = studienlist.get(i).getId();
 				break;
 			}
 		}
 
 		for (int i = 0; i < typen.size(); i++) {
 			if (test == (typen.get(i).getSid()))
 				addToTable(typen.get(i).getName());
 		}
 		modtyptransferstring = "";
 		goforit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (modtyptable.getSelectedRow() != -1) {
 					int openrow = modtyptable.getSelectedRow();
 					modtyptransferstring = (String) modtyptable.getValueAt(openrow, 0);
 					modshowCard();
 					showCard("mod show");
 				}
 			}
 		});
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("modbuch show");
 
 			}
 		});
 	}
 
 	@SuppressWarnings("serial")
 	private void modshowCard() {
 		JPanel modshow = new JPanel();
 		cards.add(modshow, "mod show");
 		modshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("oeffnen");
 		JButton back = new JButton("zurueck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modshowtable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modshowtable);
 		modshowtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modshowtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modshow.add(modtypscp);
 		modshow.add(btnpan, BorderLayout.SOUTH);
 
 		modshowmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Module" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class };
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 
 		modshowtable.setModel(modshowmodel);
 		modshowmodel.setRowCount(0);
 		selectedmodullist = database.getselectedModul(studtransferstring, modtyptransferstring, modbuchtransferstring);
 		for (int i = 0; i < selectedmodullist.size(); i++) {
 			addToTable(selectedmodullist.get(i));
 		}
 		modulselectionstring = "";
 		goforit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (modshowtable.getSelectedRow() != -1) {
 					int openrow = modshowtable.getSelectedRow();
 					modulselectionstring = (String) modshowtable.getValueAt(openrow, 0);
 					modCard();
 					showCard("selmodshow");
 				}
 			}
 		});
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("modtyp show");
 
 			}
 		});
 
 	}
 
 	private void modCard() {
 		JPanel modshow = new JPanel();
 		cards.add(modshow, "selmodshow");
 		modshow.setLayout(new BorderLayout(0, 0));
 		JPanel modpanel = new JPanel();
 		JButton pdfbtn = new JButton("Als PDF ausgeben");
 		JButton back = new JButton("zurueck");
 		JPanel btnpan = new JPanel();
 		btnpan.add(back);
 		btnpan.add(pdfbtn);
 		modshow.add(btnpan, BorderLayout.SOUTH);
 		JScrollPane modscp = new JScrollPane(modpanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		modshow.add(modscp, BorderLayout.CENTER);
 		Modul zws = null;
 		modpanel.setLayout(new BoxLayout(modpanel, BoxLayout.Y_AXIS));
 		for (int i = 0; i < selectedmodullist.size(); i++) {
 			if (selectedmodullist.get(i).getName().equalsIgnoreCase(modulselectionstring)) {
 				zws = selectedmodullist.get(i);
 			}
 		}
 		modpanel.add(modulPanel("Name", zws.getName()));
 		modpanel.add(modulPanel("Jahrgang", modbuchtransferstring));
 		modpanel.add(modulPanel("Modultyp", modtyptransferstring));
 		for (int i = 0; i < zws.getFelder().size(); i++) {
 			modpanel.add(modulPanel(zws.getFelder().get(i).getLabel(), zws.getFelder().get(i).getValue()));
 		}
 
 		pdfbtn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("mod show");
 
 			}
 		});
 
 	}
 
 	public static void noConnection() {
 		JOptionPane.showMessageDialog(frame, "Keine Verbindung zum Server!", "Verbindungsfehler",
 				JOptionPane.ERROR_MESSAGE);
 		current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb", false, false,
 				false, false, true);
 		btnModulEinreichen.setEnabled(false);
 		btnModulVerwaltung.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnMHB.setEnabled(true);
 		btnUserVerwaltung.setEnabled(false);
 		btnLogin.setText("Einloggen");
 		showCard("welcome page");
 	}
 
 }
