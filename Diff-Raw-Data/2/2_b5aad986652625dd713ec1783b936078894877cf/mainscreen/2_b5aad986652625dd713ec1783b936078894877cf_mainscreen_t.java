 package de.team55.mms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.ScrollPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.AllPermission;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
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
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.TransferHandler;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import com.lowagie.text.DocumentException;
 import com.toedter.calendar.JDateChooser;
 
 
 
 
 import de.team55.mms.data.Feld;
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.Nachricht;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.data.User;
 //import de.team55.mms.data.Zuordnung;
 import de.team55.mms.function.SendMail;
 import de.team55.mms.function.ServerConnection;
 
 public class mainscreen {
 
 	private static JFrame frame;
 
 	private static final int SUCCES = 2;
 	private final Dimension btnSz = new Dimension(140, 50);
 	public ServerConnection serverConnection = new ServerConnection();
 
 	// Variablen
 	private static User current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb", false, false, false,
 			false, false, true); // Gast
 	String studtransferstring = ""; // uebergabe String fuer Tabellen -
 									// studiengang
 	String modbuchtransferstring = ""; // uebergabe String fuer Tabellen -
 										// modulbuch
 	String modtyptransferstring = ""; // uebergabe String fuer Tabellen -
 										// modultyp
 	String modulselectionstring = ""; // ubergabe String des ausgewaehlten
 										// Moduls
 	private boolean canReadMessages=false;
 
 	// Listen
 	private ArrayList<User> worklist = null; // Liste mit Usern
 	private ArrayList<User> neueUser = new ArrayList<User>(); // Liste mit Usern
 
 	private ArrayList<Studiengang> studienlist = null; // Liste mit
 														// Studiengngen
 	private ArrayList<Modul> selectedmodullist = null; // Liste der Module im
 														// durchstbern segment
 	// Liste der Modulhandbuecher des ausgewhlten Studiengangs
 	private ArrayList<Modulhandbuch> modulhandlist = null;
 	// private ArrayList<Zuordnung> typen = null; // Liste mit Zuordnungen
 	// Map der Dynamischen Buttons
 	private HashMap<JButton, Integer> buttonmap = new HashMap<JButton, Integer>();
 	private ArrayList<String> defaultlabels = new ArrayList<String>();
 	private ArrayList<Nachricht> nachrichten = new ArrayList<Nachricht>();
 
 	// Modelle
 	private DefaultTableModel tmodel;
 	private DefaultTableModel studmodel;
 	private DefaultTableModel modbuchmodel;
 	private DefaultTableModel modtypmodel;
 	private DefaultTableModel modshowmodel;
 	private DefaultTableModel messagemodel;
 	private DefaultComboBoxModel<Studiengang> cbmodel = new DefaultComboBoxModel<Studiengang>();
 	// private DefaultComboBoxModel<Zuordnung> cbmodel_Z = new
 	// DefaultComboBoxModel<Zuordnung>();
 	private DefaultListModel<Modul> lm = new DefaultListModel<Modul>();
 	private DefaultListModel<Modul> lm_ack = new DefaultListModel<Modul>();
 	private DefaultListModel<Studiengang> studimodel = new DefaultListModel<Studiengang>();
 	// private DefaultListModel<Zuordnung> typenmodel = new
 	// DefaultListModel<Zuordnung>();
 
 	// Komponenten
 	private static JPanel cards = new JPanel();
 	private static JPanel modul_panel = new JPanel();
 	private static JPanel modul_panel_edit = new JPanel();
 	private static JButton btnModulEinreichen = new JButton("Modul Einreichen");
 	private static JButton btnVerwaltung = new JButton("Verwaltung");
 	private static JButton btnModulBearbeiten = new JButton("Modul bearbeiten");
 	private static JButton btnModulArchiv = new JButton("<html>Module<br>Durchst\u00f6bern");
 	private static JButton btnUserVerwaltung = new JButton("User Verwaltung");
 	private static JButton btnLogin = new JButton("Einloggen");
 	private static JButton btnModulAkzeptieren = new JButton("Modul akzeptieren");
 	private static JPanel mod = new JPanel();
 	private JTable tblmessages;
 	private static JPanel welcome = new JPanel();
 	private static JPanel pnl_content = new JPanel();
 
 	
 	//zum testen von drag and drop und fr die Verwaltung der Modulverantwortlichen
 	DefaultTableModel userstuff = new DefaultTableModel(new Object[][] {}, new String[] { "User-Email", "Vorname", "Nachname" }) {
 		@SuppressWarnings("rawtypes")
 		Class[] columnTypes = new Class[] { String.class, String.class, String.class };
 
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		@Override
 		public Class getColumnClass(int columnIndex) {
 			return columnTypes[columnIndex];
 		}
 
 		@Override
 		public boolean isCellEditable(int row, int column) {
 			return false;
 		}
 	};
 	DefaultTableModel userstuff2 = new DefaultTableModel(new Object[][] {}, new String[] { "User-Email", "Vorname", "Nachname" }) {
 		@SuppressWarnings("rawtypes")
 		Class[] columnTypes = new Class[] { String.class, String.class, String.class };
 
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		@Override
 		public Class getColumnClass(int columnIndex) {
 			return columnTypes[columnIndex];
 		}
 
 		@Override
 		public boolean isCellEditable(int row, int column) {
 			return false;
 		}
 	};
 	DefaultTableModel modstuff = new DefaultTableModel(new Object[][] {}, new String[] { "Modulname" }) {
 		@SuppressWarnings("rawtypes")
 		Class[] columnTypes = new Class[] { String.class };
 
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		@Override
 		public Class getColumnClass(int columnIndex) {
 			return columnTypes[columnIndex];
 		}
 
 		@Override
 		public boolean isCellEditable(int row, int column) {
 			return false;
 		}
 	};
 	
 	
 	
 	
 	
 	
 	
 	// main Frame
 	public mainscreen() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 800, 480);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Mitte erzeugen
 		centerscr();
 
 		// Obere Leiste erzeugen
 		topscr();
 
 		// Linke Seite erzeugen
 		leftscr();
 
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Erstellt den mittleren Teil der GUI
 	 * 
 	 */
 	private void centerscr() {
 		frame.getContentPane().add(cards, BorderLayout.CENTER);
 		cards.setLayout(new CardLayout(0, 0));
 
 		// Standartfelder Hinzufgen
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
 
 		// Liste mit Studiengngen in ScrollPane
 		JList<Studiengang> list = new JList<Studiengang>(studimodel);
 		JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pnl_studiengang.add(scrollPane, BorderLayout.CENTER);
 
 		JPanel buttons = new JPanel();
 		pnl_studiengang.add(buttons, BorderLayout.SOUTH);
 
 		// Anlegen eines neuen Studienganges
 		JButton btnNeuerStudiengang = new JButton("Neuer Studiengang");
 		btnNeuerStudiengang.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 
 					// Dialog anzeigen, in dem Daten eingetragen werden
 					String name = JOptionPane.showInputDialog(frame, "Name des neuen Studiengangs:", "neuer Studiengang",
 							JOptionPane.PLAIN_MESSAGE);
 
 					while (name.isEmpty()) {
 						name = JOptionPane.showInputDialog(frame, "Bitte g\u00fcltigen Namen des neuen Studiengangs eingeben:",
 								"neuer Studiengang", JOptionPane.PLAIN_MESSAGE);
 					}
 
 					String abschluss = JOptionPane.showInputDialog(frame, "Abschluss des neuen Studiengangs:", "Abschluss",
 							JOptionPane.PLAIN_MESSAGE);
 
 					while (name.isEmpty()) {
 						name = JOptionPane.showInputDialog(frame, "Bitte g\u00fcltigen Abschluss des neuen Studiengangs eingeben:",
 								"Abschluss", JOptionPane.PLAIN_MESSAGE);
 					}
 					// Vorhanden Studiengnge aus der Datenbank abfragen
 					studienlist = serverConnection.getStudiengaenge();
 
 					// Prfe, ob schon ein Studiengang mit dem selben Namen
 					// existiert
 					boolean neu = true;
 					for (int i = 0; i < studienlist.size(); i++) {
 						if (studienlist.get(i).equals(name)) {
 							neu = false;
 							break;
 						}
 					}
 					// Wenn keiner Vorhanden ist, anlegen und in Datenbank
 					// eintragen
 					// Anschlieend Liste und Modelle aktualisieren
 					if (neu) {
 						serverConnection.setStudiengang(name,abschluss);
 						studimodel.removeAllElements();
 						cbmodel.removeAllElements();
 						studienlist = serverConnection.getStudiengaenge();
 						for (int i = 0; i < studienlist.size(); i++) {
 							studimodel.addElement(studienlist.get(i));
 							cbmodel.addElement(studienlist.get(i));
 						}
 
 						// Ansonsten Fehler ausgeben
 					} else {
 						JOptionPane.showMessageDialog(frame, "Studiengang ist schon vorhanden", "Fehler", JOptionPane.ERROR_MESSAGE);
 					}
 				} catch (NullPointerException np) {
 					// Bei abbruch nichts tuen
 				}
 			}
 		});
 		buttons.add(btnNeuerStudiengang);
 
 		JLabel lblStudiengnge = new JLabel("Studieng\u00E4nge");
 		pnl_studiengang.add(lblStudiengnge, BorderLayout.NORTH);
 
 		JPanel pnl_zuordnungen = new JPanel();
 		pnl_manage.add(pnl_zuordnungen);
 		pnl_zuordnungen.setLayout(new BorderLayout(0, 0));
 
 		// Liste mit Zuordnungen (Modultypen)
 		// JList<Zuordnung> list1 = new JList<Zuordnung>(typenmodel);
 
 		JPanel buttons1 = new JPanel();
 		pnl_zuordnungen.add(buttons1, BorderLayout.SOUTH);
 
 		// Anlegen einer neuen Zuordnung
 		JButton btnNeueZuordnung = new JButton("Modul Verwalter");
 		btnNeueZuordnung.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 //				try {
 				modverwaltung();
 					showCard("modverwaltung");
 //					JTextField neu_Name = new JTextField();
 //					JTextField neu_Abschluss = new JTextField();
 //					JComboBox<Studiengang> neu_sgbox = new JComboBox<Studiengang>(cbmodel);
 //
 //					Object[] message = { "Name des Types:", neu_Name, "Abschluss:", neu_Abschluss, "Studiengang:", neu_sgbox };
 //
 //					// Dialog anzeigen, in dem Daten eingetragen werden
 //					int option = JOptionPane.showConfirmDialog(frame, message, "Neuen Typ anlegen", JOptionPane.OK_CANCEL_OPTION);
 //					if (option == JOptionPane.OK_OPTION) {
 //
 //						// Teste, ob alle Felder ausgefllt werden
 //						while ((neu_Name.getText().isEmpty() || (neu_sgbox.getSelectedItem() == null) || neu_Abschluss.getText().isEmpty())
 //								&& (option == JOptionPane.OK_OPTION)) {
 //							Object[] messageEmpty = { "Bitte alle Felder ausf\u00fcllen!", "Name des Types:", neu_Name, "Abschluss:",
 //									neu_Abschluss, "Studiengang:", neu_sgbox };
 //							option = JOptionPane.showConfirmDialog(frame, messageEmpty, "Neuen Typ anlegen", JOptionPane.OK_CANCEL_OPTION);
 //						}
 //						// Wenn ok gedrckt wird
 //						if (option == JOptionPane.OK_OPTION) {
 //							Studiengang s = (Studiengang) neu_sgbox.getSelectedItem();
 //							// Zuordnung z = new Zuordnung(neu_Name.getText(),
 //							// s.getName(), s.getId(), neu_Abschluss.getText());
 //
 //							// Teste, ob Zuordnung schon vorhanden
 //							boolean neu = true;
 //							// for (int i = 0; i < typen.size(); i++) {
 //							// if (typen.get(i).equals(z)) {
 //							// neu = false;
 //							// break;
 //							// }
 //							// }
 //
 //							// Falls neu, in Datenbank eintragen und Liste und
 //							// Model aktualisieren
 //							if (neu) {
 //								// serverConnection.setZuordnung(z);
 //								// typen = serverConnection.getZuordnungen();
 //								// typenmodel.removeAllElements();
 //								// for (int i = 0; i < typen.size(); i++) {
 //								// typenmodel.addElement(typen.get(i));
 //								// }
 //							}
 //							// Ansonsten Fehler ausgeben
 //							else {
 //								JOptionPane.showMessageDialog(frame, "Zuordnung ist schon vorhanden", "Fehler", JOptionPane.ERROR_MESSAGE);
 //							}
 //						}
 //					}
 //
 //				} catch (NullPointerException np) {
 //					// Bei abbruch nichts tuen
 //				}
 			}
 
 		});
 		buttons1.add(btnNeueZuordnung);
 
 		JButton btnZurck_1 = new JButton("Zur\u00FCck");
 		btnZurck_1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Zurck zur Start Card
 				showCard("welcome page");
 			}
 		});
 		buttons1.add(btnZurck_1);
 //		worklist = serverConnection.userload();
 		JLabel lblZuordnungen = new JLabel("Deadline");
 		pnl_zuordnungen.add(lblZuordnungen, BorderLayout.NORTH);
 		
 		final JDateChooser calender = new JDateChooser();
 		pnl_zuordnungen.add(calender);
 		
 		JButton savedate = new JButton("Datum setzen");
 		buttons1.add(savedate);
 		JButton test = new JButton("test");
 		buttons1.add(test);
 		test.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				calender.setDate(serverConnection.getDate());
 			}
 		});
 		savedate.addActionListener(new ActionListener() {
 		String dateString;	
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				try{
 				serverConnection.savedate(calender.getDate());
 				} catch(Exception ex){
 					JOptionPane.showMessageDialog(frame, "Bitte whlen Sie ein gltiges Datum aus!", "Datenfehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 //		JScrollPane scrollPane_1 = new JScrollPane(x,
 //		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 //		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 //		pnl_zuordnungen.add(scrollPane_1, BorderLayout.CENTER);
 //		 
 //		JScrollPane scrollPane_2 = new JScrollPane(y,
 //		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 //		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 //		pnl_zuordnungen.add(scrollPane_2, BorderLayout.CENTER);
 //				 
 //		JScrollPane scrollPane_3 = new JScrollPane(z,
 //		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 //		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 //		pnl_zuordnungen.add(scrollPane_3, BorderLayout.CENTER);
 //						 
 	}
 
 	private void modverwaltung(){
 		JPanel mv = new JPanel();
 		cards.add(mv,"modverwaltung");
 		JTable mods = new JTable();
 		JTable aktverwalter = new JTable();
 		JTable user = new JTable();	
 
 		mv.setLayout(new GridLayout(2, 1));
 		
 		
 		JPanel buttons = new JPanel();
 		
 		
 		JPanel tabellen = new JPanel();
 		tabellen.setLayout(new GridLayout(2,3,0,0));
 		
 		
 		JLabel modules = new JLabel("Module");
 		JLabel aktuelle = new JLabel("Aktuelle Verwalter");
 		JLabel rest = new JLabel("Userlist");
 
 				
 		mv.setLayout(new GridLayout(2,1));
 		mv.add(tabellen);
 		mv.add(buttons);
 		
 		ScrollPane scp_1 = new ScrollPane();
 		ScrollPane scp_2 = new ScrollPane();
 		ScrollPane scp_3 = new ScrollPane();
 		
 		scp_1.add(mods);
 		scp_2.add(aktverwalter);
 		scp_3.add(user);
 				
 		tabellen.add(modules);
 		tabellen.add(aktuelle);
 		tabellen.add(rest);
 		
 		tabellen.add(scp_1);
 		tabellen.add(scp_2);
 		tabellen.add(scp_3);
 
 		JButton back = new JButton("Zur\u00FCck");
 		JButton save = new JButton("Speichern");
 		
 		buttons.add(back);
 		buttons.add(save);
 		
 		aktverwalter.setDragEnabled(true);
 		user.setDragEnabled(true);
 				
 		mods.setModel(modstuff);
 		aktverwalter.setModel(userstuff);
 		user.setModel(userstuff2);
 		
 		userstuff.setRowCount(0);
 		userstuff2.setRowCount(0);
 		modstuff.setRowCount(0);
 		
 		//on construction
 		ArrayList<Modul> modstufflist = new ArrayList<Modul>();
 		ArrayList<User> alluser = new ArrayList<User>();
 		ArrayList<User> verwalter = new ArrayList<User>();
 		
 		modstuff.addRow(new Object[] { "Modul1" });
 		userstuff.addRow(new Object[] { "","bla1-1","bla1-2" });
 		userstuff2.addRow(new Object[] { "BLA2","bla2-1","bla2-2" });
 		
 		back.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				showCard("manage");
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Erstellt den oberen Teil der GUI
 	 */
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
 
 	/**
 	 * Hinzufgen eines Users in die Usertabelle
 	 * 
 	 * @param usr
 	 *            Zu hinzufgender User
 	 */
 	private void addToTable(User usr) {
 		tmodel.addRow(new Object[] { usr.getTitel(), usr.getVorname(), usr.getNachname(), usr.geteMail(), usr.getManageUsers(),
 				usr.getCreateModule(), usr.getAcceptModule(), usr.getManageSystem() });
 	}
 
 	/**
 	 * Hinzufgen eines Studienganges zur Studiengangtabelle
 	 * 
 	 * @param stud
 	 *            Zu hinzufgender Studiengang
 	 */
 	private void addToTable(Studiengang stud) {
 		studmodel.addRow(new Object[] { stud.getName() });
 	}
 
 	/**
 	 * Hinzufgen eines Modules zur Modultabelle
 	 * 
 	 * @param mod
 	 *            Zu hinzufgendes Modul
 	 */
 	private void addToTable(Modul mod) {
 		modshowmodel.addRow(new Object[] { mod.getName() });
 	}
 
 	/**
 	 * Hinzufgen eines Modulhandbuches zur Modulhandbuchtabelle
 	 * 
 	 * @param modbuch
 	 *            Zu hinzufgendes Modulhandbuch
 	 */
 	private void addToTable(Modulhandbuch modbuch) {
 		modbuchmodel.addRow(new Object[] { modbuch.getJahrgang() });
 	}
 
 	/**
 	 * Hinzufgen einer Zuordnung zur Zuordnungstabelle
 	 * 
 	 * @param modtyp
 	 *            Name der zu hinzufgenden Zuordnung
 	 */
 	private void addToTable(String modtyp) {
 		modtypmodel.addRow(new Object[] { modtyp });
 	}
 	
 	private void addToTable(String user, int i) {
 		if(i == 1)
 		userstuff.addRow(new Object[] { user });
 		if(i == 2)
 			userstuff2.addRow(new Object[] { user });
 	}
 
 
 	/**
 	 * Liefert ein Feld mit Label, TextArea und Checkbox
 	 * 
 	 * @return JPanel ausgeflltes Panel
 	 * @param name
 	 *            Beschriftung des Labels
 	 * @param string
 	 *            Inhalt der TextArea
 	 * @param b
 	 *            Gibt an, ob die Checkbox ausgewhlt ist
 	 */
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
 
 	/**
 	 * Liefert ein Feld mit Label und TextArea
 	 * 
 	 * @return JPanel ausgeflltes Panel
 	 * @param name
 	 *            Beschriftung des Labels
 	 * @param string
 	 *            Inhalt der TextArea
 	 */
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
 
 	/**
 	 * Erstellt den Startbildschirm der GUI
 	 * 
 	 */
 	private void homecard() {		
 		final ArrayList<String> dialogs = new ArrayList<String>();
 		cards.add(welcome, "welcome page");
 		welcome.setLayout(new BorderLayout(0, 0));
 
 		
 		pnl_content.setLayout(new BoxLayout(pnl_content, BoxLayout.Y_AXIS));
 
 		JPanel pnl_day = new JPanel();
 		pnl_content.add(pnl_day);
 
 		JLabel lblStichtag = new JLabel("Stichtag f\u00FCr das Einreichen von Modulen: 30.08.13");
 		pnl_day.add(lblStichtag);
 		lblStichtag.setHorizontalAlignment(SwingConstants.CENTER);
 		lblStichtag.setAlignmentY(0.0f);
 		lblStichtag.setForeground(Color.RED);
 		lblStichtag.setFont(new Font("Tahoma", Font.BOLD, 14));
 
 		JPanel pnl_messages = new JPanel();
 		pnl_content.add(pnl_messages);
 		pnl_messages.setLayout(new BoxLayout(pnl_messages, BoxLayout.Y_AXIS));
 
 		JPanel pnl_mestop = new JPanel();
 		pnl_messages.add(pnl_mestop);
 		pnl_mestop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
 		JLabel lblNachrichten = new JLabel("Nachrichten:");
 		pnl_mestop.add(lblNachrichten);
 		lblNachrichten.setVerticalAlignment(SwingConstants.BOTTOM);
 		lblNachrichten.setHorizontalAlignment(SwingConstants.CENTER);
 
 		JScrollPane scrollPane = new JScrollPane();
 		pnl_messages.add(scrollPane);
 
 		messagemodel = new DefaultTableModel(new Object[][] { { Boolean.FALSE, "", null, null }, }, new String[] { "", "Von", "Betreff",
 				"Datum" }) {
 			Class[] columnTypes = new Class[] { Boolean.class, String.class, String.class, String.class };
 
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			boolean[] columnEditables = new boolean[] { true, false, false, false };
 
 			public boolean isCellEditable(int row, int column) {
 				return columnEditables[column];
 			}
 		};
 		refreshMessageTable();
 
 		tblmessages = new JTable(messagemodel);
 		scrollPane.setViewportView(tblmessages);
 		tblmessages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		tblmessages.setFillsViewportHeight(true);
 		tblmessages.setShowVerticalLines(false);
 
 		tblmessages.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					int row = tblmessages.getSelectedRow();
 					Nachricht n = nachrichten.get(row);
 					nachrichten.remove(row);
 					n.setGelesen(true);
 					if(!dialogs.contains(n.toString())){
 						dialogs.add(n.toString());
 						MessageDialog dialog = new MessageDialog(n);
 						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 						dialog.setVisible(true);
 					}
 					nachrichten.add(n);
 					refreshMessageTable();
 				}
 			}
 		});
 
 		JPanel pnl_mesbot = new JPanel();
 		pnl_messages.add(pnl_mesbot);
 
 		JButton btnNeu = new JButton("Neu");
 		btnNeu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int x = -1;
 				int von = 1;
 				int an = 2;
 				String betreff = "Neuer Test";
 				Date datum = new Date();
 				boolean gelesen = false;
 				String nachricht = "foooooooooooo blabulb fooooooooo";
 				Nachricht neu = new Nachricht(x, von, an, betreff, datum, gelesen, nachricht); //abgendert damit das prog wieder startet
 				nachrichten.add(neu);
 				refreshMessageTable();
 			}
 		});
 		pnl_mesbot.add(btnNeu);
 
 		JButton btnAlsGelesenMarkieren = new JButton("Als gelesen markieren");
 		btnAlsGelesenMarkieren.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				for (int i = 0; i < messagemodel.getRowCount(); i++) {
 					if ((boolean) messagemodel.getValueAt(i, 0)) {
 						nachrichten.get(i).setGelesen(true);
 					}
 				}
 				refreshMessageTable();
 			}
 		});
 		pnl_mesbot.add(btnAlsGelesenMarkieren);
 
 		JButton btnAlsUngelesenMarkieren = new JButton("Als ungelesen markieren");
 		btnAlsUngelesenMarkieren.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				for (int i = 0; i < messagemodel.getRowCount(); i++) {
 					if ((boolean) messagemodel.getValueAt(i, 0)) {
 						nachrichten.get(i).setGelesen(false);
 					}
 				}
 				refreshMessageTable();
 			}
 		});
 		pnl_mesbot.add(btnAlsUngelesenMarkieren);
 
 		JButton btnLschen = new JButton("L\u00F6schen");
 		btnLschen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<Nachricht> tmp = new ArrayList<Nachricht>();
 				for (int i = 0; i < messagemodel.getRowCount(); i++) {
 					if ((boolean) messagemodel.getValueAt(i, 0)) {
 						tmp.add(nachrichten.get(i));
 					}
 				}
 				nachrichten.removeAll(tmp);
 				refreshMessageTable();
 			}
 		});
 		pnl_mesbot.add(btnLschen);
 
 		JPanel pnl_welc = new JPanel();
 		welcome.add(pnl_welc, BorderLayout.NORTH);
 
 		JLabel lblNewLabel = new JLabel("Willkommen beim Modul Management System");
 		pnl_welc.add(lblNewLabel);
 
 	}
 
 	private void refreshMessageTable() {
 		Collections.sort(nachrichten, new Comparator<Nachricht>() {
 			public int compare(Nachricht n1, Nachricht n2) {
 				return n1.getDatum().compareTo(n2.getDatum()) * -1;
 			}
 		});
 		messagemodel.setRowCount(0);
 		for (int i = 0; i < nachrichten.size(); i++) {
 			addToTable(nachrichten.get(i));
 		}
 	}
 
 	protected void addToTable(Nachricht neu) {
 		if(neu.isGelesen()){
 			messagemodel.addRow(new Object[] { false, neu.getAbsender(), neu.getBetreff(), neu.getDatumString() });
 		} else {
 			messagemodel.addRow(new Object[] { false, "<html><b>" +neu.getAbsender()+ "</b></html>", "<html><b>" +neu.getBetreff()+ "</b></html>", "<html><b>" +neu.getDatumString()+ "</b></html>" });
 		}
 	}
 
 	/**
 	 * Erstellt den linke Teil der GUI
 	 * 
 	 */
 	private void leftscr() {
 		btnLogin.setToolTipText("Klicken Sie hier, um in das MMS einzuloggen.");
 		btnModulAkzeptieren.setToolTipText("Klicken Sie hier, um das ausgewhlte Modul zu akzeptieren. Damit wird es freigegeben und in der Liste der aktuellen Module angezeigt.");
 		btnModulArchiv.setToolTipText("Klicken Sie hier, um das Archiv der aktuellen Module zu durchstbern.");
 		btnModulBearbeiten.setToolTipText("Klicken Sie hier, um bereits vorhandene Module zu bearbeiten.");
 		btnModulEinreichen.setToolTipText("Klicken Sie hier, um das Modul einzureichen. Damit wird es der verantwortlichen Stelle vorgelegt. Das Modul wird erst nach der Besttigung der verantwortlichen Stelle in der Liste der aktuellen Module angezeigt.");
 		btnUserVerwaltung.setToolTipText("Klicken Sie hier, um die Benutzerverwaltung aufzurufen. Hier knnen Sie neue Benutzer anlegen, deren Daten ndern und ihre Rechte im MMS festlegen.");
 		btnVerwaltung.setToolTipText("Klicken Sie hier, um die Verwaltung zu ffnen. Hier knnen Sie mgliche Studiengnge festlegen, fr die es Modulhandbcher geben kann, die Deadline fr die Modulhandbcher aktualisieren und Verwalter fr bestimmte Module festlegen.");
 		
 		JPanel leftpan = new JPanel();
 		frame.getContentPane().add(leftpan, BorderLayout.WEST);
 
 		JPanel left = new JPanel();
 		leftpan.add(left);
 		left.setLayout(new GridLayout(0, 1, 5, 20));
 
 		// Button zum Einreichen eines Modules
 		left.add(btnModulEinreichen);
 		btnModulEinreichen.setEnabled(false);
 		btnModulEinreichen.setPreferredSize(btnSz);
 		btnModulEinreichen.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnModulEinreichen.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Abfrage aller Zuordnungen und Studiengnge aus der Datenbank
 				// Danach Modelle fllen und zur Card wechseln
 				// typen = serverConnection.getZuordnungen();
 				studienlist = serverConnection.getStudiengaenge();
 				cbmodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					cbmodel.addElement(studienlist.get(i));
 				}
 				// cbmodel_Z.removeAllElements();
 				// for (int i = 0; i < typen.size(); i++) {
 				// cbmodel_Z.addElement(typen.get(i));
 				// }
 				showCard("newmodule");
 			}
 
 		});
 
 		// Button zum Bearbeiten eines Modules
 		left.add(btnModulBearbeiten);
 		btnModulBearbeiten.setEnabled(false);
 		btnModulBearbeiten.setPreferredSize(btnSz);
 		btnModulBearbeiten.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnModulBearbeiten.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Abfrage alles nicht nicht akzeptierten Module
 				// Danach Modell fllen
 				ArrayList<Modul> module = serverConnection.getModule(false);
 				lm.removeAllElements();
 				for (int i = 0; i < module.size(); i++) {
 					lm.addElement(module.get(i));
 				}
 
 				// Abfrage alles nicht akzeptierten Module
 				// Danach Modell fllen
 				module = serverConnection.getModule(true);
 				lm_ack.removeAllElements();
 				for (int i = 0; i < module.size(); i++) {
 					lm_ack.addElement(module.get(i));
 				}
 
 				// Zur card mit bersicht an Modulen wechseln
 				showCard("modulbearbeiten");
 			}
 
 		});
 
 		// Button zum Login
 		left.add(btnLogin);
 		btnLogin.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Verbindung zum Server
 				current = serverConnection.login(current.geteMail(), current.getPassword());
 				if (current != null) {
 					// Wenn noch nicht eingeloggt, einloggen
 					if (current.geteMail().equals("gast@gast.gast")) {
 						logindialog log = new logindialog(frame, "Login", serverConnection);
 						int resp = log.showCustomDialog();
 						if (resp == 1) {
 							// User bernehmen
 							current = log.getUser();
 							serverConnection = log.getServerConnection();
 							btnLogin.setText("Ausloggen");
 							// Auf Rechte prfen
 							checkRights();
 						}
 					}
 					// Wenn bereits eingeloggt, ausloggen
 					else {
 						current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb", false, false, false,
 								false, false, true);
 						if (serverConnection.isConnected() == SUCCES) {
 							checkRights();
 						}
 						btnLogin.setText("Einloggen");
 						btnUserVerwaltung.setText("User Verwaltung");
 						btnUserVerwaltung.setEnabled(false);
 						showCard("welcome page");
 					}
 				} else {
 					// Wenn keine Verbindung besteht
 					noConnection();
 				}
 			}
 		});
 		btnLogin.setPreferredSize(btnSz);
 		btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		// Button zur Userverwaltung
 		btnUserVerwaltung.setEnabled(false);
 		left.add(btnUserVerwaltung);
 		btnUserVerwaltung.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Wenn User das Recht hat, um Benutzer zu Verwalten,
 				// dann Anzeige der Benutzerverwaltung
 				if (current.getManageUsers()) {
 					// Tabelle leeren
 					tmodel.setRowCount(0);
 
 					// Tabelle mit neuen daten fllen
 					worklist = serverConnection.userload("true");
 					for (int i = 0; i < worklist.size(); i++) {
 						// Wenn der User noch nicht freigeschaltet wurde,
 						// zur Liste mit neuen Benutzern hinzufgen
 						if (worklist.get(i).isFreigeschaltet()) {
 							addToTable(worklist.get(i));
 						} else {
 							neueUser.add(worklist.get(i));
 						}
 					}
 					// Zur Userverwaltungs Card wechseln
 					showCard("user managment");
 
 					// Einblendung aller neuen User
 					for (int i = 0; i < neueUser.size(); i++) {
 						userdialog dlg = new userdialog(frame, "User besttigen", neueUser.get(i), true, serverConnection);
 						int response = dlg.showCustomDialog();
 						// Bei Besttigung, neuen User freischalten und e-Mail
 						// senden
 						User tmp = dlg.getUser();
 						if (response == 1) {
 							tmp.setFreigeschaltet(true);
 							if (SendMail.send(current.geteMail(), neueUser.get(i).geteMail(), "Sie wurden freigeschaltet!") == 1) {
 								tmp.setFreigeschaltet(true);
 								if (serverConnection.userupdate(tmp, tmp.geteMail()).getStatus() == 201) {
 									addToTable(tmp);
 									neueUser.remove(i);
 								}
 							}
 							// Ansonsten mglichkeit ihn wieder zu lschen
 						} else {
 							int n = JOptionPane.showConfirmDialog(frame, "Mchten Sie diesen Benutzer lschen", "Besttigung",
 									JOptionPane.YES_NO_OPTION);
 							if (n == 0) {
 								serverConnection.deluser(tmp.geteMail());
 							}
 						}
 					}
 				} else {
 					// Ansonsten Dialog ffnen,
 					// in dem die eigenen Daten gendert werden knnen
 					userdialog dlg = new userdialog(frame, "User bearbeiten", current, false, serverConnection);
 					int response = dlg.showCustomDialog();
 					// Wenn ok gedrckt wird
 					// neuen User abfragen
 					if (response == 1) {
 						User tmp = dlg.getUser();
 						if (serverConnection.userupdate(tmp, current.geteMail()).getStatus() == 201) {
 							current = tmp;
 							checkRights();
 						} else
 							JOptionPane.showMessageDialog(frame, "Update Fehlgeschlagen!", "Update Error", JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 			}
 		});
 		btnUserVerwaltung.setPreferredSize(btnSz);
 		btnUserVerwaltung.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		// Button zur Verwaltung von Studiengngen und Zuordnungen
 		btnVerwaltung.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Zuordnungen und Studiengnge aus Datenbank abrufen
 				// und Listen fllen
 				studienlist = serverConnection.getStudiengaenge();
 				studimodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					studimodel.addElement(studienlist.get(i));
 				}
 				cbmodel.removeAllElements();
 				for (int i = 0; i < studienlist.size(); i++) {
 					cbmodel.addElement(studienlist.get(i));
 				}
 				// typen = serverConnection.getZuordnungen();
 				// for (int i = 0; i < typen.size(); i++) {
 				// typenmodel.addElement(typen.get(i));
 				// }
 				// Zur Card wechseln
 				showCard("manage");
 
 			}
 		});
 
 		left.add(btnVerwaltung);
 		btnVerwaltung.setEnabled(false);
 		btnVerwaltung.setPreferredSize(btnSz);
 		btnVerwaltung.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		left.add(btnModulArchiv);
 		btnModulArchiv.setEnabled(true);
 		btnModulArchiv.setPreferredSize(btnSz);
 		btnModulArchiv.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		// Button zum Durchstbern von Modulen
 		btnModulArchiv.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				current = serverConnection.login(current.geteMail(), current.getPassword());
 				if (current != null) {
 					// Studiengnge und Zuordnungen abrufen
 				studmodel.setRowCount(0);
 					studienlist = serverConnection.getStudiengaenge();
 					for (int i = 0; i < studienlist.size(); i++) {
 						addToTable(studienlist.get(i));
 					}
 					//TODO something 
 					// typen = serverConnection.getZuordnungen();
 
 					// Zur Card wechseln
 					showCard("studiengang show");
 				} else {
 					noConnection();
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * Erstellt eine Card, um ein neues Modul anzulegen
 	 * 
 	 */
 	private void newmodulecard() {
 		// Alle vorhandenen Felder entfernen
 		modul_panel.removeAll();
 		final JPanel pnl_newmod = new JPanel();
 
 		// Liste dynamischer Buttons leeren
 		if (!buttonmap.isEmpty()) {
 			for (int i = 0; i < buttonmap.size(); i++)
 				buttonmap.remove(i);
 		}
 
 		// Liste mit bereits vorhandenen Felder erstellen und mit den
 		// Standartfeldern fllen
 		final ArrayList<String> labels = new ArrayList<String>();
 		labels.addAll(defaultlabels);
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_newmod.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_bottom = new JPanel();
 		pnl_newmod.add(pnl_bottom, BorderLayout.SOUTH);
 
 		// Button zum erstellen eines neuen Feldes
 		JButton btnNeuesFeld = new JButton("Neues Feld");
 		btnNeuesFeld.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String text = "Name des Feldes";
 				// Abfrage des Namen des Feldes
 				String name = JOptionPane.showInputDialog(frame, text);
 				try {
 					// Prfe, ob Name leer oder schon vorhanden ist
 					while (name.isEmpty() || labels.contains(name)) {
 						Object[] params = { "Bitte geben Sie eine gltige Bezeichnung ein!", text };
 						name = JOptionPane.showInputDialog(frame, params);
 					}
 					labels.add(name);
 					JPanel pnl_tmp = new JPanel();
 					modul_panel.add(pnl_tmp);
 					// Platzhalter
 					modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 					// Abfrage der Anzahl an Panels, die bereits vorhanden sind
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
 
 					// Button, um das Feld wieder zu entfernen
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
 					// hinzufgen
 					buttonmap.put(btn_tmp_entf, numOfPanels - 2);
 
 					pnl_tmp.add(btn_tmp_entf);
 
 					modul_panel.revalidate();
 
 				} catch (NullPointerException npe) {
 					// nichts tuen bei Abbruch
 				}
 			}
 		});
 		pnl_bottom.add(btnNeuesFeld);
 
 		// Zurck zur Startseite
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Card wieder erneuern und zur Startseite wechseln
 				newmodulecard();
 				modul_panel.revalidate();
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
 
 		// Liste mit ausgewhlten Zuordnungen
 		// final DefaultListModel<Zuordnung> lm = new
 		// DefaultListModel<Zuordnung>();
 		// final JList<Zuordnung> zlist = new JList<Zuordnung>(lm);
 		// zlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		//
 		// pnl_Z.add(zlist);
 		//
 		// // ComboBox mit Zuordnungen
 		// final JComboBox<Zuordnung> cb_Z = new
 		// JComboBox<Zuordnung>(cbmodel_Z);
 		// cb_Z.setMaximumSize(new Dimension(400, 20));
 		//
 		// pnl_Z.add(cb_Z);
 		//
 		// // Auswahl einer Zuordnung aus der ComboBox
 		// JButton z_btn = new JButton("Zuordnung ausw\u00e4hlen");
 		// z_btn.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// if (!lm.contains(cb_Z.getSelectedItem()))
 		// lm.addElement((Zuordnung) cb_Z.getSelectedItem());
 		// }
 		// });
 		// pnl_Z.add(z_btn);
 		//
 		// // In der Liste ausgewhlte Zuordnung wieder entfernen
 		// JButton btnZuordnungEntfernen = new JButton("Zuordnung entfernen");
 		// btnZuordnungEntfernen.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// int i = zlist.getSelectedIndex();
 		// if (i > -1) {
 		// lm.remove(i);
 		// }
 		// }
 		// });
 		// pnl_Z.add(btnZuordnungEntfernen);
 		//
 		// modul_panel.add(pnl_Z);
 		modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		// Alle Standartfelder, auer Zuordnung erzeugen
 		for (int i = 3; i < defaultlabels.size(); i++) {
 			modul_panel.add(defaultmodulPanel(defaultlabels.get(i), "", false));
 			modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 		}
 
 		// Button zum Annehmen eines Modules
 		JButton btnOk = new JButton("Annehmen");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ArrayList<Zuordnung> zlist = new ArrayList<Zuordnung>();
 				String jg = ((JTextArea) ((JPanel) modul_panel.getComponent(2)).getComponent(1)).getText();
 				int jahrgang;
 				try {
 					jahrgang = Integer.parseInt(jg);
 				} catch (NumberFormatException nfe) {
 					jahrgang = 0;
 				}
 				// for (int i = 0; i < lm.getSize(); i++) {
 				// zlist.add(lm.getElementAt(i));
 				// }
 
 				// Prfe, ob min. eine Zuordnung ausgewhlt und ein gltiger
 				// Jahrgang eingegeben wurde
 				// if (!zlist.isEmpty()) {
 				// if (jahrgang != 0) {
 				//
 				// String Name = ((JTextArea) ((JPanel)
 				// modul_panel.getComponent(4)).getComponent(1)).getText();
 				//
 				// if (Name.isEmpty()) {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte geben Sie einen Namen ein!", "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// } else {
 				//
 				// boolean filled = true;
 				// ArrayList<Feld> felder = new ArrayList<Feld>();
 				// // Eintraege der Reihe nach auslesen
 				// for (int i = 6; i < modul_panel.getComponentCount(); i = i +
 				// 2) {
 				// JPanel tmp = (JPanel) modul_panel.getComponent(i);
 				// JLabel tmplbl = (JLabel) tmp.getComponent(0);
 				// JTextArea tmptxt = (JTextArea) tmp.getComponent(1);
 				//
 				// boolean dezernat2 = ((JCheckBox)
 				// tmp.getComponent(2)).isSelected();
 				// String value = tmptxt.getText();
 				// String label = tmplbl.getText();
 				// // Prfe, ob alle Felder ausgefllt wurden
 				// if (value.isEmpty()) {
 				// filled = false;
 				// break;
 				// }
 				// felder.add(new Feld(label, value, dezernat2));
 				// }
 				// // Wenn alle aussgefllt wurden, neues Modul
 				// // erzeugen und bei Besttigung einreichen
 				// if (filled == true) {
 				// int version = serverConnection.getModulVersion(Name) + 1;
 				//
 				// Date d = new Date();
 				// ArrayList<String> user = new ArrayList<String>();
 				// user.add(current.geteMail());
 				// Modul neu = new Modul(Name, felder, version, d, 0, false,
 				// user, "");
 				// int n = JOptionPane.showConfirmDialog(frame,
 				// "Sind Sie sicher, dass Sie dieses Modul einreichen wollen?",
 				// "Besttigung", JOptionPane.YES_NO_OPTION);
 				// if (n == 0) {
 				// serverConnection.setModul(neu);
 				// labels.removeAll(labels);
 				// modul_panel.removeAll();
 				// modul_panel.revalidate();
 				// newmodulecard();
 				// showCard("newmodule");
 				// }
 				// } // Fehler, wenn nicht alle ausgefllt wurden
 				// else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte fllen Sie alle Felder aus!", "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte geben Sie einen gltigen Wert fr den Jahrgang ein!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte whlen Sie min. einen Zuordnung aus!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 			}
 		});
 		pnl_bottom.add(btnOk);
 
 		pnl_newmod.add(scrollPane);
 		cards.add(pnl_newmod, "newmodule");
 
 	}
 
 	/**
 	 * Entfernt einen Eintrag aus der Usertabelle
 	 * 
 	 * @param rowid
 	 *            Der Index des zu enfernenden Eintrages
 	 */
 	private void removeFromTable(int rowid) {
 		tmodel.removeRow(rowid);
 	}
 
 	/**
 	 * Wechselt zur angegeben Card
 	 * 
 	 * @param card
 	 *            Die Bezeichnung der Card, zu der gewechselt werden soll
 	 */
 	private static void showCard(String card) {
 		((CardLayout) cards.getLayout()).show(cards, card);
 	}
 
 	/**
 	 * Erstellt eine Card zur Verwaltung von Benutzern
 	 */
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
 		tmodel = new DefaultTableModel(new Object[][] {}, new String[] { "Titel", "Vorname", "Nachname", "e-Mail", "Benutzer verwalten",
 				"Module einreichen", "Module Annehmen", "Verwaltung" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class, String.class, String.class, String.class, boolean.class, boolean.class,
 					boolean.class, boolean.class };
 
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
 
 		// Button zum hinzufgen eines Benutzers
 		JButton btnUserAdd = new JButton("User hinzuf\u00fcgen");
 		btnUserAdd.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				userdialog dlg = new userdialog(frame, "User hinzuf\u00fcgen", serverConnection);
 				int response = dlg.showCustomDialog();
 				// Wenn ok gedrckt wird
 				// neuen User hinzufgen
 				if (response == 1) {
 					User tmp = dlg.getUser();
 					tmp.setFreigeschaltet(true);
 					// Wenn er erfolgreich in die DB eingetragen wurde,zur
 					// Tabelle hinzufgen
 					if (serverConnection.usersave(tmp).getStatus() == 201) {
 						addToTable(tmp);
 					}
 				}
 			}
 
 		});
 		usrpan.add(btnUserAdd);
 
 		// Button zum bearbeiten eines in der Tabelle ausgewhlten Users
 		JButton btnUserEdit = new JButton("User bearbeiten");
 		btnUserEdit.setToolTipText("Zum Bearbeiten Benutzer in der Tabelle markieren");
 		btnUserEdit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int row = usrtbl.getSelectedRow();
 				if (row != -1) {
 					// Daten aus der Tabelle abrufen und User erzeugen
 					String t = (String) usrtbl.getValueAt(row, 0);
 					String vn = (String) usrtbl.getValueAt(row, 1);
 					String nn = (String) usrtbl.getValueAt(row, 2);
 					String em = (String) usrtbl.getValueAt(row, 3);
 					boolean r1 = (boolean) usrtbl.getValueAt(row, 4);
 					boolean r2 = (boolean) usrtbl.getValueAt(row, 5);
 					boolean r3 = (boolean) usrtbl.getValueAt(row, 6);
 					boolean r4 = (boolean) usrtbl.getValueAt(row, 7);
 					boolean r5 = (boolean) usrtbl.getValueAt(row, 8);
 					User alt = new User(vn, nn, t, em, null, r1, r2, r3, r4, r5, true);
 
 					// User an Bearbeiten dialog bergeben
 					userdialog dlg = new userdialog(frame, "User bearbeiten", alt, true, serverConnection);
 					int response = dlg.showCustomDialog();
 					// Wenn ok ged\u00fcckt wird
 					// neuen User abfragen
 					if (response == 1) {
 						User tmp = dlg.getUser();
 						tmp.setFreigeschaltet(true);
 						// Wenn update erfolgreich war, alten user abfragen und
 						// neu hinzufgen
 						if (serverConnection.userupdate(tmp, em).getStatus() == 201) {
 							removeFromTable(row);
 							addToTable(tmp);
 							// Falls eigener Benutzer bearbeitet wurde, Rechte
 							// erneut prfen
 							if (em.equals(current.geteMail())) {
 								current = tmp;
 								checkRights();
 							}
 						} else
 							JOptionPane.showMessageDialog(frame, "Update Fehlgeschlagen", "Update Fehler", JOptionPane.ERROR_MESSAGE);
 
 					}
 
 				}
 			}
 		});
 		usrpan.add(btnUserEdit);
 
 		// Lschen eines ausgewhlten Benutzers
 		JButton btnUserDel = new JButton("User l\u00f6schen");
 		btnUserDel.setToolTipText("Zum L\u00f6schen Benutzer in der Tabelle markieren");
 		btnUserDel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int row = usrtbl.getSelectedRow();
 				if (row != -1) {
 					int n = JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie diesen Benutzer l\u00f6schen wollen?",
 							"Besttigung", JOptionPane.YES_NO_OPTION);
 					if (n == 0) {
 						// Wenn Lschen erfolgreich war, aus der Tabelle
 						// entfernen
 						if (serverConnection.deluser((String) usrtbl.getValueAt(row, 3)).getStatus() != 201) {
 							removeFromTable(row);
 						} else
 							JOptionPane.showMessageDialog(frame, "L\u00f6schen Fehlgeschlagen", "Fehler beim L\u00f6schen",
 									JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		usrpan.add(btnUserDel);
 
 		// Zurck zur Startseite
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
 
 	/**
 	 * berprft die Rechte des aktuell eingeloggten Benutzers und aktiviert
 	 * Buttons, auf die er Zugriff hat und deaktiviert Bttons, auf die er keinen
 	 * Zugriff hat
 	 */
 	protected void checkRights() {
 		btnModulEinreichen.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnVerwaltung.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnUserVerwaltung.setEnabled(false);
 		btnModulAkzeptieren.setEnabled(false);
 		canReadMessages=false;
 		welcome.remove(pnl_content);
 		welcome.repaint();
 		if (current.getCreateModule()) {
 			btnModulEinreichen.setEnabled(true);
 			btnModulBearbeiten.setEnabled(true);
 			canReadMessages=true;
 		}
 		if (current.getAcceptModule()) {
 			btnModulBearbeiten.setEnabled(true);
 			btnModulAkzeptieren.setEnabled(true);
 			canReadMessages=true;
 		}
 		if (current.getManageSystem()) {
 			btnVerwaltung.setEnabled(true);
 			canReadMessages=true;
 		}
 		if (current.getManageUsers()) {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("User Verwaltung");
 			canReadMessages=true;
 		} else {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("Account bearbeiten");
 		}
 		if(canReadMessages){
 			welcome.add(pnl_content, BorderLayout.CENTER);
 			nachrichten = serverConnection.getNachrichten(current.geteMail());
 			refreshMessageTable();
 		}
 		showCard("welcome page");
 	}
 
 	/**
 	 * Erstellt eine Card zur Bearbeitung von Modulen
 	 */
 	public void modulbearbeitenCard() {
 
 		JPanel pnl_modedit = new JPanel();
 		pnl_modedit.setLayout(new BorderLayout(0, 0));
 
 		// Tab fr akzeptierte und fr nicht akzeptierte Module erstellen
 		JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
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
 
 		// Button zum bearbeiten eines nicht akzeptierten Modules
 		JButton btnModulBearbeiten = new JButton("Modul bearbeiten");
 		btnModulBearbeiten.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Modul m = list_notack.getSelectedValue();
 				if (m != null) {
 					// Abfragen, ob Modul in Bearbeitung ist
 					m.setInbearbeitung(serverConnection.getModulInEdit(m.getName()));
 					if (!m.isInbearbeitung()) {
 
 						// Prfe, ob Benutzer das Rech hat, das Modul zu
 						// Bearbeiten
 						// Wenn er das Modul selbst erstellt hat, darf er es
 						// auch bearbeiten
 						// Ansonsten Prfen, ob es von einem Stellvertreter oder
 						// Vorgesetzetn von ihm erstellt wurde
 						boolean rights = false;
 						if (m.getUser().equals(current.geteMail())) {
 							rights = true;
 						} else {
 							ArrayList<String> rel = serverConnection.getUserRelation(current.geteMail());
 							if (rel.contains(m.getUser())) {
 								rights = true;
 							}
 						}
 						// Wenn er die Rechte dazu hat, Modul als in Bearbeitung
 						// markieren und zur Bearbeitung wechseln
 						if (rights) {
 							mod.removeAll();
 							mod.add(modeditCard(m), BorderLayout.CENTER);
 							m.setInbearbeitung(true);
 							serverConnection.setModulInEdit(m);
 							showCard("modBearbeiten");
 						} else {
 							JOptionPane.showMessageDialog(frame,
 									"Sie besitzen nicht die n\u00f6tigen Rechte, um dieses Modul zu bearbeiten!", "Zugriff verweigert",
 									JOptionPane.ERROR_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!", "Zugriff verweigert",
 								JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 
 			}
 		});
 		buttonpnl.add(btnModulBearbeiten);
 
 		// Button zum akzeptieren eines Modules
 		btnModulAkzeptieren.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Modul m = list_notack.getSelectedValue();
 				if (m != null) {
 					// Prfe, ob Modul in Bearbeitung ist
 					m.setInbearbeitung(serverConnection.getModulInEdit(m.getName()));
 					if (!m.isInbearbeitung()) {
 						if (m.getName().isEmpty()) {
 							JOptionPane.showMessageDialog(frame, "Bei diesem Modul sind nicht alle Felder ausgefllt!", "Fehler im Modul",
 									JOptionPane.ERROR_MESSAGE);
 						} else {
 							// Prfe, ob ein Feld fr das Dezernat 2 markiert
 							// wurde
 							// und ob alle Felder ausgefllt wurden
 							boolean hasDezernat = false;
 							boolean isCorrect = true;
 							ArrayList<Feld> felder = m.getFelder();
 							for (int i = 0; i < felder.size(); i++) {
 								if (felder.get(i).getValue().isEmpty()) {
 									JOptionPane.showMessageDialog(frame, "Bei diesem Modul sind nicht alle Felder ausgefllt!",
 											"Fehler im Modul", JOptionPane.ERROR_MESSAGE);
 									isCorrect = false;
 									break;
 								}
 								if (felder.get(i).isDezernat()) {
 									hasDezernat = true;
 								}
 							}
 
 							if (isCorrect) {
 								boolean checked = true;
 								// Wenn Felder als Dezernat 2 markiert wurden,
 								// nach Besttigung fragen
 								if (hasDezernat) {
 									int n = JOptionPane.showConfirmDialog(frame,
 											"Dieses Modul besitzt Felder, die vom Dezernat2 berprft werden mssen, wurde das getan?",
 											"Besttigung", JOptionPane.YES_NO_OPTION);
 									if (n == 0) {
 										checked = true;
 									} else {
 										checked = false;
 									}
 								}
 								if (checked) {
 									// Bei besttigung Modul akzeptieren und
 									// Listen neu abrufen
 									// dann zur Bearbeiten bersicht wechseln
 									serverConnection.acceptModul(m);
 									ArrayList<Modul> module = serverConnection.getModule(false);
 									lm.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm.addElement(module.get(i));
 									}
 
 									module = serverConnection.getModule(true);
 									lm_ack.removeAllElements();
 									for (int i = 0; i < module.size(); i++) {
 										lm_ack.addElement(module.get(i));
 									}
 									showCard("modulbearbeiten");
 								}
 							}
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!", "Zugriff verweigert",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		buttonpnl.add(btnModulAkzeptieren);
 
 		// Zurck zur Startseite
 		JButton btnZurck = new JButton("Zur\u00FCck");
 		btnZurck.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttonpnl.add(btnZurck);
 
 		// akzeptierte Module
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
 
 		// akzeptierte Module bearbeiten
 		JButton btnModulBearbeiten2 = new JButton("Modul bearbeiten");
 		btnModulBearbeiten2.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Modul m = list_ack.getSelectedValue();
 				if (m != null) {
 					// Prfe, ob Modul in Bearbeitung ist
 					m.setInbearbeitung(serverConnection.getModulInEdit(m.getName()));
 					if (!m.isInbearbeitung()) {
 						// Prfe, ob User das Recht hat, dieses Modul zu
 						// bearbeiten
 						boolean rights = false;
 						if (m.getUser().equals(current.geteMail())) {
 							rights = true;
 						} else {
 							ArrayList<String> rel = serverConnection.getUserRelation(current.geteMail());
 							if (rel.contains(m.getUser())) {
 								rights = true;
 							}
 						}
 						if (rights) {
 							// Zur Bearbeitung wechseln
 							mod.removeAll();
 							mod.add(modeditCard(m), BorderLayout.CENTER);
 							showCard("modBearbeiten");
 						} else {
 							JOptionPane.showMessageDialog(frame,
 									"Sie besitzen nicht die n\u00f6tigen Rechte, um dieses Modul zu bearbeiten!", "Zugriff verweigert",
 									JOptionPane.ERROR_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!", "Zugriff verweigert",
 								JOptionPane.ERROR_MESSAGE);
 
 					}
 				}
 
 			}
 		});
 		buttonpnl2.add(btnModulBearbeiten2);
 
 		// Zurck zur Startseite
 		JButton btnZurck2 = new JButton("Zur\u00FCck");
 		btnZurck2.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttonpnl2.add(btnZurck2);
 		cards.add(pnl_modedit, "modulbearbeiten");
 
 	}
 
 	/**
 	 * Liefert ein Panel, dass mit den Daten von Modul m ausgefllt ist Die
 	 * Felder sind dabei nicht mehr bearbeitbar
 	 * 
 	 * @return JPanel mit Daten ausgeflltes Panel
 	 * @param m
 	 *            Zu bearbeitendes Modul
 	 */
 	private JPanel modeditCardPrev(Modul m) {
 		final JPanel pnl_editmod = new JPanel();
 		final JPanel pnl_mod_prev = new JPanel();
 
 		// Felder vom Modul abfragen
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
 
 		// Zuordnugen vom Modul abfragen
 		// final DefaultListModel<Zuordnung> lm = new
 		// DefaultListModel<Zuordnung>();
 		// typen = m.getZuordnungen();
 		// for (int i = 0; i < typen.size(); i++) {
 		// lm.addElement(typen.get(i));
 		// }
 		// JList<Zuordnung> zlist = new JList<Zuordnung>(lm);
 		// pnl_Z.add(zlist);
 
 		pnl_mod_prev.add(pnl_Z);
 		pnl_mod_prev.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		// Restliche Felder erzeugen
 		JPanel jg = new JPanel();
 		jg.setLayout(new BoxLayout(jg, BoxLayout.X_AXIS));
 
 		JLabel lbl_jg = new JLabel("Jahrgang");
 		lbl_jg.setPreferredSize(preferredSize);
 		jg.add(lbl_jg);
 
 		// JTextArea txt_jg = new JTextArea(m.getJahrgang() + "");
 		// txt_jg.setLineWrap(true);
 		// txt_jg.setEditable(false);
 		// jg.add(txt_jg);
 
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
 
 			// Felder sind hier nicht bearbeitbar
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
 
 	/**
 	 * Liefert ein Panel, dass mit den Daten von Modul m ausgefllt ist
 	 * 
 	 * @return JPanel mit Daten ausgeflltes Panel
 	 * @param m
 	 *            Zu bearbeitendes Modul
 	 */
 	private JPanel modeditCard(final Modul m) {
 		// Alle Felder entfernen
 		// Buttonmap leeren
 		final JPanel pnl_editmod = new JPanel();
 		modul_panel_edit.removeAll();
 		if (!buttonmap.isEmpty()) {
 			for (int i = 0; i < buttonmap.size(); i++)
 				buttonmap.remove(i);
 		}
 		// Felder vom Modul abfragen
 		final ArrayList<Feld> felder = m.getFelder();
 		final ArrayList<String> labels = new ArrayList<String>();
 		for (int i = 0; i < felder.size(); i++) {
 			labels.add(felder.get(i).getLabel());
 		}
 
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_editmod.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_bottom = new JPanel();
 		pnl_editmod.add(pnl_bottom, BorderLayout.SOUTH);
 
 		// Button zur anzeige der vorherigen Version
 		JButton alt = new JButton("Vorherige Version");
 		alt.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				int v = m.getVersion() - 1;
 				if (v > 0) {
 					String name = m.getName();
 					Modul pre = serverConnection.getModul(name, v);
 					if (pre != null)
 						JOptionPane.showMessageDialog(frame, modeditCardPrev(pre), "Vorherige Version", 1);
 				} else {
 					JOptionPane.showMessageDialog(frame, "Keine Vorherige Version vorhanden!", "Fehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		pnl_bottom.add(alt);
 
 		// Button zum erzeugen eines neuen Feldes
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
 
 					// Abfrage der Anzahl an Feldern
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
 
 					// Feld wieder entfernen
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
 
 						}
 					});
 
 					// Button btn_tmp_entf mit ID (numOfPanels-2) zu ButtonMap
 					buttonmap.put(btn_tmp_entf, numOfPanels - 2);
 
 					pnl_tmp.add(btn_tmp_entf);
 
 					modul_panel_edit.revalidate();
 
 				} catch (NullPointerException npe) {
 					// nichts tuen bei Abbruch
 				}
 			}
 		});
 		pnl_bottom.add(btnNeuesFeld);
 
 		// Zurck zur Startseite
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Panel wider neu erzeugen,
 				// Modul als nicht in Bearbeitung markieren
 				modul_panel_edit.removeAll();
 				modul_panel_edit.revalidate();
 				m.setInbearbeitung(false);
 				serverConnection.setModulInEdit(m);
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
 
 		// Liste ausgewhlter Zuordnungen
 		// final DefaultListModel<Zuordnung> lm_Z = new
 		// DefaultListModel<Zuordnung>();
 		// typen = m.getZuordnungen();
 		// for (int i = 0; i < typen.size(); i++) {
 		// lm_Z.addElement(typen.get(i));
 		// }
 		// final JList<Zuordnung> zlist = new JList<Zuordnung>(lm_Z);
 		// zlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		//
 		// pnl_Z.add(zlist);
 		// typen = serverConnection.getZuordnungen();
 		// cbmodel_Z.removeAllElements();
 		// for (int i = 0; i < typen.size(); i++) {
 		// cbmodel_Z.addElement(typen.get(i));
 		// }
 
 		// // Zur auswahlstehende Zuordnungen
 		// final JComboBox<Zuordnung> cb_Z = new
 		// JComboBox<Zuordnung>(cbmodel_Z);
 		// cb_Z.setMaximumSize(new Dimension(cb_Z.getMaximumSize().width, 20));
 		//
 		// pnl_Z.add(cb_Z);
 		//
 		// // Zuordnung auswhlen
 		// JButton z_btn = new JButton("Zuordnung ausw\u00e4hlen");
 		// z_btn.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// if (!lm_Z.contains(cb_Z.getSelectedItem()))
 		// lm_Z.addElement((Zuordnung) cb_Z.getSelectedItem());
 		// }
 		// });
 		// pnl_Z.add(z_btn);
 		//
 		// // Zuordnung wieder entfernen
 		// JButton btnZuordnungEntfernen = new JButton("Zuordnung entfernen");
 		// btnZuordnungEntfernen.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// int i = zlist.getSelectedIndex();
 		// if (i > -1) {
 		// lm_Z.remove(i);
 		// }
 		// }
 		// });
 		// pnl_Z.add(btnZuordnungEntfernen);
 
 		modul_panel_edit.add(pnl_Z);
 		modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		// modul_panel_edit.add(defaultmodulPanel("Jahrgang", m.getJahrgang() +
 		// "", false));
 		// modul_panel_edit.add(Box.createRigidArea(new Dimension(0, 5)));
 
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
 
 		// Felder erzeugen und fllen
 		for (int i = 0; i < m.getFelder().size(); i++) {
 			Feld f = m.getFelder().get(i);
 			JPanel feld = defaultmodulPanel(f.getLabel(), f.getValue(), f.isDezernat());
 
 			// Wenn es kein Standart Feld ist, einen entfernen Button hinzufgen
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
 
 		// Button zum annehmen eines Modules
 		JButton btnOk = new JButton("Annehmen");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ArrayList<Zuordnung> zlist = new ArrayList<Zuordnung>();
 				String jg = ((JTextArea) ((JPanel) modul_panel_edit.getComponent(2)).getComponent(1)).getText();
 				int jahrgang;
 				try {
 					jahrgang = Integer.parseInt(jg);
 				} catch (NumberFormatException nfe) {
 					jahrgang = 0;
 				}
 
 				// for (int i = 0; i < lm_Z.getSize(); i++) {
 				// zlist.add(lm_Z.getElementAt(i));
 				// }
 
 				// Prfe ob min. eine Zuordnung ausgewhlt ist und ein korrekter
 				// Jahrgang ausgewhlt wurde
 				// if (!zlist.isEmpty()) {
 				//
 				// if (jahrgang != 0) {
 				//
 				// String Name = ((JTextArea) ((JPanel)
 				// modul_panel_edit.getComponent(4)).getComponent(1)).getText();
 				//
 				// if (Name.isEmpty()) {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte fllen Sie alle Felder aus!", "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// } else {
 				//
 				// boolean filled = true;
 				// ArrayList<Feld> felder = new ArrayList<Feld>();
 				// // Eintraege der Reihe nach auslesen
 				// for (int i = 6; i < modul_panel_edit.getComponentCount(); i =
 				// i + 2) {
 				// JPanel tmp = (JPanel) modul_panel_edit.getComponent(i);
 				// JLabel tmplbl = (JLabel) tmp.getComponent(0);
 				// JTextArea tmptxt = (JTextArea) tmp.getComponent(1);
 				//
 				// boolean dezernat2 = ((JCheckBox)
 				// tmp.getComponent(2)).isSelected();
 				// String value = tmptxt.getText();
 				// String label = tmplbl.getText();
 				// // Prfe, ob Feld ausgefllt ist
 				// if (value.isEmpty()) {
 				// filled = false;
 				// break;
 				// }
 				// felder.add(new Feld(label, value, dezernat2));
 				// }
 				// if (filled == true) {
 				// // Wenn alle ausgefllt sind, Modul erzeugen und
 				// // bei Besttigung einreichen
 				// int version = serverConnection.getModulVersion(Name) + 1;
 				//
 				// Date d = new Date();
 				//
 				// Modul neu = new Modul(Name, zlist, jahrgang, felder, version,
 				// d, false, false, current.geteMail());
 				//
 				// int n = JOptionPane.showConfirmDialog(frame,
 				// "Sind Sie sicher, dass Sie dieses Modul einreichen wollen?",
 				// "Besttigung", JOptionPane.YES_NO_OPTION);
 				// if (n == 0) {
 				// m.setInbearbeitung(false);
 				// serverConnection.setModul(neu);
 				// labels.removeAll(labels);
 				// modul_panel_edit.removeAll();
 				// modul_panel_edit.revalidate();
 				//
 				// // Listen neu einlesen
 				// ArrayList<Modul> module = serverConnection.getModule(false);
 				// lm.removeAllElements();
 				// for (int i = 0; i < module.size(); i++) {
 				// lm.addElement(module.get(i));
 				// }
 				//
 				// module = serverConnection.getModule(true);
 				// lm_ack.removeAllElements();
 				// for (int i = 0; i < module.size(); i++) {
 				// lm_ack.addElement(module.get(i));
 				// }
 				// showCard("modulbearbeiten");
 				// }
 				// } else {
 				//
 				// }
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte geben Sie einen gltigen Wert fr den Jahrgang ein!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte whlen Sie min. einen Zuordnung aus!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 			}
 		});
 		pnl_bottom.add(btnOk);
 
 		pnl_editmod.add(scrollPane);
 
 		return pnl_editmod;
 
 	}
 
 	/**
 	 * Erstellt eine Card zur Auswahl des Studienganges
 	 */
 	@SuppressWarnings("serial")
 	private void studiengangCard() {
 
 		JPanel studiengangshow = new JPanel();
 		cards.add(studiengangshow, "studiengang show");
 		studiengangshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("\u00d6ffnen");
 		JButton back = new JButton("Zur\u00FCck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable studtable = new JTable();
 		JScrollPane studscp = new JScrollPane(studtable);
 		studtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		studtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		studiengangshow.add(studscp);
 		studiengangshow.add(btnpan, BorderLayout.SOUTH);
 
 		// Tabelle mit Studiengngen
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
 
 		// Wechseln zur Jahrgangsauswahl, wenn ein Studiengang ausgewhlt wurde
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
 		// Zurck zur Startseite
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 
 	}
 
 	/**
 	 * Erstellt eine Card zur Auswahl des Jahrgangs
 	 */
 	@SuppressWarnings("serial")
 	private void modhandshowCard() {
 		JPanel modbuchshow = new JPanel();
 		cards.add(modbuchshow, "modbuch show");
 		modbuchshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("\u00d6ffnen");
 		JButton back = new JButton("Zur\u00FCck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modbuchtable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modbuchtable);
 		modbuchtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modbuchtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modbuchshow.add(modtypscp);
 		modbuchshow.add(btnpan, BorderLayout.SOUTH);
 
 		// Tabelle mit Jahrgngen
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
 		int zws = 0;
 		// Tabelle fllen
 		modbuchtable.setModel(modbuchmodel);
 		modbuchmodel.setRowCount(0);
 		System.out.println(studienlist.get(0).getName());
 		System.out.println(studtransferstring);
		//System.out.println(studienlist.get(0).getModbuch().get(0).getId());
 		//modulhandlist = serverConnection.getModulhandbuch(studtransferstring);
 		for(int i = 0; i < studienlist.size(); i++){
 			if(studienlist.get(i).getName().equalsIgnoreCase(studtransferstring)){
 				zws = i;
 				break;
 			}
 		}
 		for (int i = 0; i < studienlist.get(0).getModbuch().size(); i++) {
 
 			addToTable(studienlist.get(0).getModbuch().get(i));
 
 		}
 		modbuchtransferstring = "";
 
 		// Wechseln zur Auswahl von Zuordnungen
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
 
 	/**
 	 * Erstellt eine Card zur Auswahl des Modultypes
 	 */
 	@SuppressWarnings("serial")
 	private void modtypshowCard() {
 		JPanel modtypshow = new JPanel();
 		cards.add(modtypshow, "modtyp show");
 		modtypshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("\u00d6ffnen");
 		JButton back = new JButton("Zur\u00FCck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modtyptable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modtyptable);
 		modtyptable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modtyptable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modtypshow.add(modtypscp);
 		modtypshow.add(btnpan, BorderLayout.SOUTH);
 
 		// Tabelle mit Zuordnungen
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
 
 		// Tabelle fllen
 		modtyptable.setModel(modtypmodel);
 		modtypmodel.setRowCount(0);
 		int test = 0;
 		for (int i = 0; i < studienlist.size(); i++) {
 			if (studienlist.get(i).getName().equalsIgnoreCase(studtransferstring)) {
 				test = studienlist.get(i).getId();
 				break;
 			}
 		}
 
 		// for (int i = 0; i < typen.size(); i++) {
 		// if (test == (typen.get(i).getSid()))
 		// addToTable(typen.get(i).getName());
 		// }
 		modtyptransferstring = "";
 
 		// Wechseln zur Anzeige mit Modulen
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
 
 		// Zurck zur vorherigen Ansicht
 		back.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("modbuch show");
 
 			}
 		});
 	}
 
 	/**
 	 * Erstellt eine Card zur Auswahl eines Modules
 	 */
 	@SuppressWarnings("serial")
 	private void modshowCard() {
 		JPanel modshow = new JPanel();
 		cards.add(modshow, "mod show");
 		modshow.setLayout(new BorderLayout(0, 0));
 		JPanel btnpan = new JPanel();
 		JButton goforit = new JButton("\u00d6ffnen");
 		JButton back = new JButton("Zur\u00FCck");
 		btnpan.add(back);
 		btnpan.add(goforit);
 		final JTable modshowtable = new JTable();
 		JScrollPane modtypscp = new JScrollPane(modshowtable);
 		modshowtable.setBorder(new LineBorder(new Color(0, 0, 0)));
 		modshowtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		modshow.add(modtypscp);
 		modshow.add(btnpan, BorderLayout.SOUTH);
 
 		// Tabelle mit Modulen
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
 				return false;
 			}
 		};
 
 		// Tabelle fllen
 		modshowtable.setModel(modshowmodel);
 		modshowmodel.setRowCount(0);
 		selectedmodullist = serverConnection.getselectedModul(studtransferstring, modtyptransferstring, modbuchtransferstring);
 		for (int i = 0; i < selectedmodullist.size(); i++) {
 			addToTable(selectedmodullist.get(i));
 		}
 		modulselectionstring = "";
 
 		// Wechsel zur Anzeige des Modules
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
 
 		// Zurck zur vorherigen Ansicht
 		back.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("modtyp show");
 
 			}
 		});
 
 	}
 
 	/**
 	 * Erstellt eine Card zur Anzeige eines Modules
 	 */
 	private void modCard() {
 		JPanel modshow = new JPanel();
 		cards.add(modshow, "selmodshow");
 		modshow.setLayout(new BorderLayout(0, 0));
 		JPanel modpanel = new JPanel();
 		JButton pdfbtn = new JButton("Als PDF ausgeben");
 		JButton back = new JButton("Zur\u00FCck");
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
 		// Felder erstellen
 		modpanel.add(modulPanel("Name", zws.getName()));
 		modpanel.add(modulPanel("Jahrgang", modbuchtransferstring));
 		// for (int i = 0; i < zws.getZuordnungen().size(); i++) {
 		// modpanel.add(modulPanel("Zuordnung",
 		// zws.getZuordnungen().get(i).toString()));
 		// }
 		for (int i = 0; i < zws.getFelder().size(); i++) {
 			modpanel.add(modulPanel(zws.getFelder().get(i).getLabel(), zws.getFelder().get(i).getValue()));
 		}
 
 		// Pdf fr das Modul erstellen
 		pdfbtn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				try {
 					serverConnection.toPdf(modulselectionstring);
 				} catch (TransformerConfigurationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (DocumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TransformerException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		// Zurck zur vorherigen Ansicht
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("mod show");
 
 			}
 		});
 
 	}
 
 	/**
 	 * Wird aufgerufen, wenn keine Verbindung zum Server besteht Dabei wird der
 	 * Benutzer auf den Standart User zurckgesetzt
 	 */
 	public static void noConnection() {
 		JOptionPane.showMessageDialog(frame, "Keine Verbindung zum Server!", "Verbindungsfehler", JOptionPane.ERROR_MESSAGE);
 		current = new User("gast", "gast", "", "gast@gast.gast", "d4061b1486fe2da19dd578e8d970f7eb", false, false, false, false, false,
 				true);
 		btnModulEinreichen.setEnabled(false);
 		btnVerwaltung.setEnabled(false);
 		btnModulBearbeiten.setEnabled(false);
 		btnModulArchiv.setEnabled(true);
 		btnUserVerwaltung.setEnabled(false);
 		btnLogin.setText("Einloggen");
 		showCard("welcome page");
 	}
 
 }
