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
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
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
 
 import com.toedter.calendar.JDateChooser;
 
 import de.team55.mms.data.Fach;
 import de.team55.mms.data.FachTransfer;
 import de.team55.mms.data.Feld;
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.data.User;
 import de.team55.mms.data.pordnung;
 import de.team55.mms.function.SendMail;
 import de.team55.mms.function.ServerConnection;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JComboBox;
 
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
 	private boolean canReadMessages = false;
 
 	// Listen
 	private ArrayList<User> worklist = null; // Liste mit Usern
 	private ArrayList<User> neueUser = new ArrayList<User>(); // Liste mit Usern
 
 	private ArrayList<Studiengang> studienlist = null; // Liste mit
 														// Studiengngen
 	private ArrayList<Studiengang> prototyplist = null; // Liste mit
 	// prototyp Studiengngen
 	private ArrayList<Modul> selectedmodullist = null; // Liste der Module im
 														// durchstbern segment
 	// Liste der Modulhandbuecher des ausgewhlten Studiengangs
 	private ArrayList<Modulhandbuch> modulhandlist = null;
 	// private ArrayList<Zuordnung> typen = null; // Liste mit Zuordnungen
 	// Map der Dynamischen Buttons
 	private HashMap<JButton, Integer> buttonmap = new HashMap<JButton, Integer>();
 	private ArrayList<String> defaultlabels = new ArrayList<String>();
 	private ArrayList<Feld> defaultFelder = new ArrayList<Feld>();
 
 	// Modelle
 	private DefaultTableModel tmodel;
 	private DefaultTableModel studmodel;
 	private DefaultTableModel modbuchmodel;
 	private DefaultTableModel modtypmodel;
 	private DefaultTableModel modshowmodel;
 	private DefaultTableModel messagemodel;
 	private DefaultComboBoxModel<Studiengang> cbmodel = new DefaultComboBoxModel<Studiengang>();
 	private DefaultComboBoxModel<Modulhandbuch> cbModBuchMo = new DefaultComboBoxModel<Modulhandbuch>();
 	// private DefaultComboBoxModel<Zuordnung> cbmodel_Z = new
 	// DefaultComboBoxModel<Zuordnung>();
 	private DefaultListModel<Modul> lm = new DefaultListModel<Modul>();
 	private DefaultListModel<Modul> lm_ack = new DefaultListModel<Modul>();
 	private DefaultListModel<Studiengang> studimodel = new DefaultListModel<Studiengang>();
 	private DefaultListModel<Fach> fachmodel = new DefaultListModel<Fach>();
 	private DefaultListModel<pordnung> pomodel = new DefaultListModel<pordnung>();
 
 	// private DefaultListModel<Zuordnung> typenmodel = new
 	// DefaultListModel<Zuordnung>();
 
 	// Komponenten
 	private static JPanel cards = new JPanel();
 	private static JPanel top = new JPanel();
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
 	private static HomeCard welcome;
 	private static LookCard looking;
 	private static ProtoLookCard prototyp;
 	private int messages = 0;
 	private JDateChooser calender = new JDateChooser();
 	private static JPanel nModCard;
 
 	// zum testen von drag and drop und fr die Verwaltung der
 	// Modulverantwortlichen
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
 
 	private Thread t;
 
 	private boolean run;
 
 	protected int newMessages;
 
 	protected static JButton btnNewButton = new JButton("Sie haben keine neue Nachricht.");
 
 	protected static String selectedCard = "welcome Page";
 	private JTable table;
 
 	private DefaultTableModel tableFelder;
 
 	private Vector<String> columnIdentifiers;
 
 	private DefaultListModel modListModel;
 
 	private ArrayList<Modulhandbuch> nichtAckMBs = new ArrayList<Modulhandbuch>();
 
 	private JList modbuchList;
 
 	protected ArrayList<pordnung> poList;
 
 	protected ArrayList<Modul> module;
 
 	private ArrayList<Fach> fachzws;
 
 	protected JPanel modeditp;
 
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
 
 		columnIdentifiers = new Vector<String>();
 		columnIdentifiers.add("Position");
 		columnIdentifiers.add("Name");
 		columnIdentifiers.add("Dezernat 2");
 		tableFelder = new DefaultTableModel(new Vector(), columnIdentifiers) {
 			Class[] columnTypes = new Class[] { Integer.class, String.class, Boolean.class };
 
 			public Class getColumnClass(int columnIndex) {
 				return columnTypes[columnIndex];
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 		prototyp = new ProtoLookCard();
 		cards.add(prototyp, "protoshow");
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		pnl_manage.add(tabbedPane);
 
 		JPanel pnl_modbuch = new JPanel();
 		tabbedPane.addTab("Modulhandb\u00FCcher", null, pnl_modbuch, "Modulhandb\u00FCcher und Stichtag verwalten");
 		pnl_modbuch.setLayout(new BorderLayout(0, 0));
 
 		// Liste mit Zuordnungen (Modultypen)
 		// JList<Zuordnung> list1 = new JList<Zuordnung>(typenmodel);
 
 		JPanel buttons1 = new JPanel();
 		pnl_modbuch.add(buttons1, BorderLayout.SOUTH);
 
 		// Anlegen einer neuen Zuordnung
 //		JButton btnNeueZuordnung = new JButton("Modul Verwalter");
 //		btnNeueZuordnung.addActionListener(new ActionListener() {
 //			@Override
 //			public void actionPerformed(ActionEvent e) {
 //				// try {
 //				modverwaltung();
 //				showCard("modverwaltung");
 				// JTextField neu_Name = new JTextField();
 				// JTextField neu_Abschluss = new JTextField();
 				// JComboBox<Studiengang> neu_sgbox = new
 				// JComboBox<Studiengang>(cbmodel);
 				//
 				// Object[] message = { "Name des Types:", neu_Name,
 				// "Abschluss:", neu_Abschluss, "Studiengang:", neu_sgbox };
 				//
 				// // Dialog anzeigen, in dem Daten eingetragen werden
 				// int option = JOptionPane.showConfirmDialog(frame, message,
 				// "Neuen Typ anlegen", JOptionPane.OK_CANCEL_OPTION);
 				// if (option == JOptionPane.OK_OPTION) {
 				//
 				// // Teste, ob alle Felder ausgefllt werden
 				// while ((neu_Name.getText().isEmpty() ||
 				// (neu_sgbox.getSelectedItem() == null) ||
 				// neu_Abschluss.getText().isEmpty())
 				// && (option == JOptionPane.OK_OPTION)) {
 				// Object[] messageEmpty = {
 				// "Bitte alle Felder ausf\u00fcllen!", "Name des Types:",
 				// neu_Name, "Abschluss:",
 				// neu_Abschluss, "Studiengang:", neu_sgbox };
 				// option = JOptionPane.showConfirmDialog(frame, messageEmpty,
 				// "Neuen Typ anlegen", JOptionPane.OK_CANCEL_OPTION);
 				// }
 				// // Wenn ok gedrckt wird
 				// if (option == JOptionPane.OK_OPTION) {
 				// Studiengang s = (Studiengang) neu_sgbox.getSelectedItem();
 				// // Zuordnung z = new Zuordnung(neu_Name.getText(),
 				// // s.getName(), s.getId(), neu_Abschluss.getText());
 				//
 				// // Teste, ob Zuordnung schon vorhanden
 				// boolean neu = true;
 				// // for (int i = 0; i < typen.size(); i++) {
 				// // if (typen.get(i).equals(z)) {
 				// // neu = false;
 				// // break;
 				// // }
 				// // }
 				//
 				// // Falls neu, in Datenbank eintragen und Liste und
 				// // Model aktualisieren
 				// if (neu) {
 				// // serverConnection.setZuordnung(z);
 				// // typen = serverConnection.getZuordnungen();
 				// // typenmodel.removeAllElements();
 				// // for (int i = 0; i < typen.size(); i++) {
 				// // typenmodel.addElement(typen.get(i));
 				// // }
 				// }
 				// // Ansonsten Fehler ausgeben
 				// else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Zuordnung ist schon vorhanden", "Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// }
 				// }
 				//
 				// } catch (NullPointerException np) {
 				// // Bei abbruch nichts tuen
 				// }
 //			}
 //
 //		});
 
 		JButton btnModulhandbuchAkzeptieren = new JButton("Modulhandbuch akzeptieren");
 		btnModulhandbuchAkzeptieren.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int id = modbuchList.getSelectedIndex();
 				if (id != -1) {
 					int x = JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie dieses Modulhandbuch akzeptieren mchten?"
 							+ "\n Weitere nderungen sind dann nicht mehr mglich.", "Feld entfernen", JOptionPane.OK_CANCEL_OPTION);
 					if (x == 0) {
 						Modulhandbuch m = nichtAckMBs.get(id);
 						x = serverConnection.setModulHandbuchAccepted(m).getStatus();
 						System.out.println(x);
 						if (x == 201) {
 							modListModel.removeAllElements();
 							studienlist = serverConnection.getStudiengaenge(false);
 							nichtAckMBs.clear();
 							for (int i = 0; i < studienlist.size(); i++) {
 								Studiengang s = studienlist.get(i);
 								ArrayList<Modulhandbuch> mb = s.getModbuch();
 								for (int j = 0; j < mb.size(); j++) {
 									m = mb.get(i);
 									nichtAckMBs.add(m);
 									modListModel.addElement(s.getAbschluss() + " " + s.getName() + ", PO " + m.getPruefungsordnungsjahr()
 											+ ", Modulhandbuch " + m.getJahrgang());
 								}
 							}
 						}
 					}
 				}
 			}
 		});
 
 		JButton btnNeuesModulhandbuch = new JButton("neues Modulhandbuch");
 		btnNeuesModulhandbuch.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String jahrgang = "";
 				String prosa = "";
 				pordnung pordnung = new pordnung();
 				int jahr = 0;
 				String sem = "";
 				ArrayList<Fach> fach = new ArrayList<Fach>();
 				Studiengang s = new Studiengang();
 				ModulHandbuchDialog dialog = new ModulHandbuchDialog();
 				poList = serverConnection.getPOs();
 				int x = 0;
 				do {
 					x = dialog.showDialog(frame, poList);
 					try {
 						pordnung = dialog.getPO();
 						prosa = dialog.getProsa();
 						sem = dialog.getSemester();
 						jahr = Integer.parseInt(dialog.getJahr());
 						s.setAbschluss(pordnung.getStudabschluss());
 						s.setName(pordnung.getStudname());
 
 					} catch (NumberFormatException n) {
 						jahr = 0;
 					}
 
 				} while ((x == 1) && (jahr == 0));
 				if (x == 1) {
 					jahrgang = sem + "/" + jahr;
 
 					ArrayList<Modulhandbuch> mbs = new ArrayList<Modulhandbuch>();
 					mbs.add(new Modulhandbuch(0, jahrgang, prosa, pordnung.getPjahr(), fach));
 					s.setModbuch(mbs);
 					x = serverConnection.setModulHandbuchAccepted(s).getStatus();
 					if (x == 201) {
 						modListModel.removeAllElements();
 						studienlist = serverConnection.getStudiengaenge(false);
 						nichtAckMBs.clear();
 						for (int i = 0; i < studienlist.size(); i++) {
 							s = studienlist.get(i);
 							ArrayList<Modulhandbuch> mb = s.getModbuch();
 							for (int j = 0; j < mb.size(); j++) {
 								Modulhandbuch m = mb.get(i);
 								nichtAckMBs.add(m);
 								modListModel.addElement(s.getAbschluss() + " " + s.getName() + ", PO " + m.getPruefungsordnungsjahr()
 										+ ", Modulhandbuch " + m.getJahrgang());
 							}
 						}
 					}
 				}
 
 			}
 		});
 		buttons1.add(btnNeuesModulhandbuch);
 		btnModulhandbuchAkzeptieren.setToolTipText("Ausgew\u00E4hltes Modulhandbuch akzeptieren");
 		buttons1.add(btnModulhandbuchAkzeptieren);
 //		buttons1.add(btnNeueZuordnung);
 		JButton btnPrototyp = new JButton("MHBProttyp");
 		buttons1.add(btnPrototyp);
 
 		btnPrototyp.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				prototyplist = serverConnection.getStudiengaenge(false);
 				prototyp.setConnection(serverConnection);
 				prototyp.setStudienlist(prototyplist);
 				prototyp.buildTree();
 				showCard("protoshow");
 			}
 		});
 
 		JButton btnZurck_1 = new JButton("Zur\u00FCck");
 		btnZurck_1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Zurck zur Start Card
 				showCard("welcome page");
 			}
 		});
 		buttons1.add(btnZurck_1);
 
 		JPanel pnl_contents = new JPanel();
 		pnl_modbuch.add(pnl_contents, BorderLayout.CENTER);
 		pnl_contents.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_buch = new JPanel();
 		pnl_contents.add(pnl_buch);
 		pnl_buch.setLayout(new BorderLayout(0, 0));
 
 		JScrollPane scrollPane_2 = new JScrollPane();
 		pnl_buch.add(scrollPane_2, BorderLayout.CENTER);
 
 		modListModel = new DefaultListModel();
 
 		modbuchList = new JList(modListModel);
 		modbuchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		scrollPane_2.setViewportView(modbuchList);
 
 		JLabel lblNichtAkzeptierteModulhandbcher = new JLabel("Nicht akzeptierte Modulhandb\u00FCcher");
 		pnl_buch.add(lblNichtAkzeptierteModulhandbcher, BorderLayout.NORTH);
 
 		JPanel pnl_deadline = new JPanel();
 		pnl_contents.add(pnl_deadline, BorderLayout.NORTH);
 		pnl_deadline.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		// worklist = serverConnection.userload();
 		JLabel lblZuordnungen = new JLabel("Deadline");
 		lblZuordnungen.setHorizontalAlignment(SwingConstants.LEFT);
 		pnl_deadline.add(lblZuordnungen);
 		lblZuordnungen.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		pnl_deadline.add(calender);
 		calender.getCalendarButton().setText("Datum w\u00E4hlen");
 		calender.getCalendarButton().setVerticalAlignment(SwingConstants.BOTTOM);
 
 		JButton savedate = new JButton("Datum setzen");
 		pnl_deadline.add(savedate);
 		// JButton test = new JButton("test");
 		// buttons1.add(test);
 		// test.addActionListener(new ActionListener() {
 		//
 		// @Override
 		// public void actionPerformed(ActionEvent arg0) {
 		// // TODO Auto-generated method stub
 		// calender.setDate(serverConnection.getDate());
 		// }
 		// });
 		savedate.addActionListener(new ActionListener() {
 			String dateString;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				try {
 					serverConnection.savedate(calender.getDate());
 				} catch (Exception ex) {
 					JOptionPane.showMessageDialog(frame, "Bitte whlen Sie ein gltiges Datum aus!", "Datenfehler",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 
 		// JScrollPane scrollPane_1 = new JScrollPane(x,
 		// ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 		// ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		// pnl_zuordnungen.add(scrollPane_1, BorderLayout.CENTER);
 		//
 		// JScrollPane scrollPane_2 = new JScrollPane(y,
 		// ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 		// ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		// pnl_zuordnungen.add(scrollPane_2, BorderLayout.CENTER);
 		//
 		// JScrollPane scrollPane_3 = new JScrollPane(z,
 		// ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 		// ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		// pnl_zuordnungen.add(scrollPane_3, BorderLayout.CENTER);
 		//
 
 		JPanel pnl_studiengang = new JPanel();
 		tabbedPane.addTab("Studieng\u00E4nge & PO", null, pnl_studiengang, "Studieng\u00E4nge und Pr\u00FCfungsordnungen verwalten");
 		pnl_studiengang.setLayout(new BoxLayout(pnl_studiengang, BoxLayout.PAGE_AXIS));
 
 		JPanel panel_2 = new JPanel();
 		pnl_studiengang.add(panel_2);
 		panel_2.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblStudiengnge = new JLabel("Studieng\u00E4nge");
 		panel_2.add(lblStudiengnge, BorderLayout.NORTH);
 
 		JPanel buttons = new JPanel();
 		panel_2.add(buttons, BorderLayout.SOUTH);
 
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
 					studienlist = serverConnection.getStudiengaenge(true);
 
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
 						serverConnection.setStudiengang(name, abschluss);
 						studimodel.removeAllElements();
 						cbmodel.removeAllElements();
 						studienlist = serverConnection.getStudiengaenge(true);
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
 
 		// Liste mit Studiengngen in ScrollPane
 		JList<Studiengang> list = new JList<Studiengang>(studimodel);
 		list.setVisibleRowCount(10);
 		JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		panel_2.add(scrollPane, BorderLayout.CENTER);
 
 		JPanel pnl_po = new JPanel();
 		pnl_studiengang.add(pnl_po);
 		pnl_po.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblPrfungsordnungen = new JLabel("Pr\u00FCfungsordnungen");
 		pnl_po.add(lblPrfungsordnungen, BorderLayout.NORTH);
 
 		JScrollPane scrollPane_3 = new JScrollPane();
 		pnl_po.add(scrollPane_3, BorderLayout.CENTER);
 
 		JList list_1 = new JList(pomodel);
 		scrollPane_3.setViewportView(list_1);
 
 		JPanel pnl_po_buttons = new JPanel();
 		pnl_po.add(pnl_po_buttons, BorderLayout.SOUTH);
 
 		JButton btnNeuePrfugsordnung = new JButton("Neue Pr\u00FCfugsordnung");
 		btnNeuePrfugsordnung.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				PoDialog dialog = new PoDialog();
 				studienlist = serverConnection.getStudiengaenge(false);
 				int x = 0;
 				int jahr=0;
 				Studiengang s = new Studiengang();
 				do{
 					x=dialog.showDialog(frame, studienlist);
 					try{
 						jahr=Integer.parseInt(dialog.getJahr());
 						s = dialog.getStudiengang();
 					} catch (NumberFormatException e){
 						jahr=0;
 					}
 				}while((x==1)&&(jahr==0));
 				if(x==1){
 					pordnung po = new pordnung();
 					po.setPjahr(jahr);
 					po.setSid(s.getId());
 					po.setStudabschluss(s.getAbschluss());
 					po.setStudname(s.getName());
 					x=serverConnection.setPO(po).getStatus();
 					if(x==201){
 						refreshPoList();
 					}
 				}
 			}
 		});
 		pnl_po_buttons.add(btnNeuePrfugsordnung);
 
 		JButton btnZurck_2 = new JButton("Zur\u00FCck");
 		pnl_po_buttons.add(btnZurck_2);
 
 		JPanel pnl_felder = new JPanel();
 		tabbedPane.addTab("Standard Felder", null, pnl_felder, "Standard Felder von Modulen verwalten");
 		pnl_felder.setLayout(new BorderLayout(0, 0));
 
 		JScrollPane scrollPane_1 = new JScrollPane();
 		pnl_felder.add(scrollPane_1, BorderLayout.CENTER);
 
 		table = new JTable();
 		table.setModel(tableFelder);
 		table.setAutoCreateColumnsFromModel(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		scrollPane_1.setViewportView(table);
 
 		JPanel panel_1 = new JPanel();
 		pnl_felder.add(panel_1, BorderLayout.SOUTH);
 
 		JButton btnNeuesStandardFeld = new JButton("Neues Standard Feld");
 		btnNeuesStandardFeld.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JPanel panel = new JPanel();
 				JPanel p = new JPanel();
 				panel.add(p);
 				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 				JPanel pnl_name = new JPanel();
 				pnl_name.setToolTipText("Bestimmt den Namen des neuen Feldes");
 				p.add(pnl_name);
 				pnl_name.setLayout(new GridLayout(0, 2, 5, 5));
 				JLabel lblNameDesFeldes = new JLabel("Name des Feldes:");
 				pnl_name.add(lblNameDesFeldes);
 				JTextField txtName = new JTextField();
 				pnl_name.add(txtName);
 
 				JPanel pnl_pos = new JPanel();
 				pnl_pos.setToolTipText("Bestimmt die Reihenfolge der Felder");
 				p.add(pnl_pos);
 				JTextField txtPos = new JTextField();
 				JLabel lbl_pos = new JLabel("An Position:");
 				lbl_pos.setToolTipText("");
 				pnl_pos.setLayout(new GridLayout(0, 2, 5, 5));
 				pnl_pos.add(lbl_pos);
 				pnl_pos.add(txtPos);
 
 				JPanel pnl_dezernat = new JPanel();
 				JCheckBox chckbxDezernat = new JCheckBox("Muss vom Dezernat 2 gepr\u00FCft werden");
 				p.add(pnl_dezernat);
 				chckbxDezernat.setToolTipText("Gibt an, ob das Feld vom Dezernat 2 gepr\u00FCft werden muss.");
 
 				pnl_dezernat.add(chckbxDezernat);
 
 				// Abfrage des Namen des Feldes
 				Object[] options = { "Annehmen", "Abbrechen" };
 				int pos = -1;
 				txtPos.setText((tableFelder.getRowCount() + 1) + "");
 				int n = 0;
 				String name = "";
 				// int n = JOptionPane.showOptionDialog(frame, panel,
 				// "Neues Feld", JOptionPane.YES_NO_OPTION,
 				// JOptionPane.QUESTION_MESSAGE,
 				// null, options, // the titles of buttons
 				// options[1]); // default button title
 				// String name = txtName.getText();
 
 				do {
 					n = JOptionPane.showOptionDialog(frame, panel, "Neues Feld", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 							null, options, options[0]);
 					name = txtName.getText();
 					try {
 						pos = Integer.parseInt(txtPos.getText());
 					} catch (NumberFormatException nfe) {
 						pos = -1;
 						txtPos.setText((tableFelder.getRowCount() + 1) + "");
 					}
 				} while ((name.isEmpty() || (pos == -1)) && (n == 0));
 
 				if (n == 0) {
 					boolean dezernat = chckbxDezernat.isSelected();
 					addToTable(name, dezernat, pos);
 					defaultFelder = tableToList();
 					int x = serverConnection.setDefaultFelder(defaultFelder).getStatus();
 				}
 
 			}
 		});
 		panel_1.add(btnNeuesStandardFeld);
 
 		JButton btnFeldBearbeiten = new JButton("Feld bearbeiten");
 		btnFeldBearbeiten.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int row = table.getSelectedRow();
 				if (row != -1) {
 					// Daten aus der Tabelle abrufen und User erzeugen
 					int pos = row;
 					System.out.println(defaultFelder);
 					Feld f = defaultFelder.get(pos);
 					Feld f2 = f;
 					System.out.println(f);
 
 					String name = (String) table.getValueAt(row, 1);
 					boolean dez = (boolean) table.getValueAt(row, 2);
 					JPanel panel = new JPanel();
 					JPanel p = new JPanel();
 					panel.add(p);
 					p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 					JPanel pnl_name = new JPanel();
 					pnl_name.setToolTipText("Bestimmt den Namen des neuen Feldes");
 					p.add(pnl_name);
 					pnl_name.setLayout(new GridLayout(0, 2, 5, 5));
 					JLabel lblNameDesFeldes = new JLabel("Name des Feldes:");
 					pnl_name.add(lblNameDesFeldes);
 					JTextField txtName = new JTextField(name);
 					pnl_name.add(txtName);
 
 					JPanel pnl_pos = new JPanel();
 					pnl_pos.setToolTipText("Bestimmt die Reihenfolge der Felder");
 					p.add(pnl_pos);
 					JTextField txtPos = new JTextField();
 					JLabel lbl_pos = new JLabel("An Position:");
 					pnl_pos.setLayout(new GridLayout(0, 2, 5, 5));
 					pnl_pos.add(lbl_pos);
 					pnl_pos.add(txtPos);
 
 					JPanel pnl_dezernat = new JPanel(dez);
 					JCheckBox chckbxDezernat = new JCheckBox("Muss vom Dezernat 2 gepr\u00FCft werden");
 					p.add(pnl_dezernat);
 					chckbxDezernat.setToolTipText("Gibt an, ob das Feld vom Dezernat 2 gepr\u00FCft werden muss.");
 
 					pnl_dezernat.add(chckbxDezernat);
 
 					// Abfrage des Namen des Feldes
 					Object[] options = { "Annehmen", "Abbrechen" };
 					txtPos.setText((pos + 1) + "");
 					int n = 0;
 
 					do {
 						n = JOptionPane.showOptionDialog(frame, panel, "Neues Feld", JOptionPane.YES_NO_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 						name = txtName.getText();
 						try {
 							pos = Integer.parseInt(txtPos.getText()) - 1;
 						} catch (NumberFormatException nfe) {
 							pos = -1;
 							txtPos.setText((tableFelder.getRowCount() + 1) + "");
 						}
 					} while ((name.isEmpty() || (pos == -1)) && (n == 0));
 
 					if (n == 0) {
 						dez = chckbxDezernat.isSelected();
 						f.setDezernat(dez);
 						f.setLabel(name);
 						if (pos >= defaultFelder.size()) {
 							pos = defaultFelder.size() - 1;
 						} else if (pos < 0) {
 							pos = 0;
 						}
 						defaultFelder.remove(f2);
 						defaultFelder.add(pos, f);
 
 						int x = serverConnection.setDefaultFelder(defaultFelder).getStatus();
 						if (x == 201) {
 							addToTableFelder();
 						}
 					}
 
 				}
 			}
 
 		});
 		panel_1.add(btnFeldBearbeiten);
 
 		JButton btnFeldEntfernen = new JButton("Feld entfernen");
 		btnFeldEntfernen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int row = table.getSelectedRow();
 				if (row != -1) {
 					int x = JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie dieses Feld entfernen mchten?",
 							"Feld entfernen", JOptionPane.OK_CANCEL_OPTION);
 					if (x == 0) {
 						defaultFelder.remove(row);
 						x = serverConnection.setDefaultFelder(defaultFelder).getStatus();
 						if (x == 201) {
 							addToTableFelder();
 						}
 					}
 				}
 			}
 		});
 		panel_1.add(btnFeldEntfernen);
 		
 		
 		JPanel pnl_fach = new JPanel();
 		tabbedPane.addTab("Fcher", null, pnl_fach, "Fcher verwalten");
 		pnl_fach.setLayout(new BorderLayout(0, 0));
 
 
 		JPanel pnl_fach_content = new JPanel();
 		JPanel pnl_fach_btns = new JPanel();
 		
 		fachzws = new ArrayList<Fach>();
 		
 		final JList<Fach> fachlist = new JList<Fach>(fachmodel);
 		JButton fach_save = new JButton("neues Fach");
 		JButton fach_edit = new JButton("edit Fach");
 		
 		pnl_fach_btns.setLayout(new FlowLayout());
 		pnl_fach_btns.add(fach_save);
 		pnl_fach_btns.add(fach_edit);
 		
 		fach_save.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				JPanel panel = new JPanel();
 				JPanel p = new JPanel();
 				panel.add(p);
 				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 				JPanel pnl_name = new JPanel();
 				pnl_name.setToolTipText("Bestimmt den Namen des neuen Fachs");
 				p.add(pnl_name);
 				pnl_name.setLayout(new GridLayout(0, 2, 5, 5));
 				JLabel lblNameDesFeldes = new JLabel("Name des Fachs:");
 				pnl_name.add(lblNameDesFeldes);
 				JTextField txtName = new JTextField();
 				pnl_name.add(txtName);
 				Object[] options = { "Annehmen", "Abbrechen" };
 				int n = 0;
 
 				String name = "";
 				
 				
 				do {
 					n = JOptionPane.showOptionDialog(frame, panel, "Neues Feld", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 							null, options, options[0]);
 					name = txtName.getText();
 				} while (name.isEmpty() && (n == 0));
 				
 				if (n == 0) {
 					Fach neu = new Fach(name);
 					fachmodel.addElement(neu);
 					fachzws.add(neu);
 					serverConnection.setFach(neu);
 				}
 				
 			}
 		});
 		
 		fach_edit.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				JPanel panel = new JPanel();
 				JPanel p = new JPanel();
 				panel.add(p);
 				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 				JPanel pnl_name = new JPanel();
 				pnl_name.setToolTipText("Bestimmt den Namen des neuen Fachs");
 				p.add(pnl_name);
 				pnl_name.setLayout(new GridLayout(0, 2, 5, 5));
 				JLabel lblNameDesFeldes = new JLabel("Name des Fachs:");
 				pnl_name.add(lblNameDesFeldes);
 				JTextField txtName = new JTextField();
 				pnl_name.add(txtName);
 				Object[] options = { "Annehmen", "Abbrechen" };
 				int n = 0;
 				txtName.setText(fachlist.getSelectedValue().getName());
 				String name = "";
 				String old = fachlist.getSelectedValue().getName();
 				Fach old2 = fachlist.getSelectedValue();
 				
 				do {
 					n = JOptionPane.showOptionDialog(frame, panel, "Neues Feld", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 							null, options, options[0]);
 					name = txtName.getText();
 				} while (name.isEmpty() && (n == 0));
 				
 
 				if (n == 0) {
 					Fach neu = new Fach(name);
 					fachmodel.addElement(neu);
 					fachmodel.removeElement(old2);
 					fachzws.remove(old2);
 					fachzws.add(neu);
 					serverConnection.updateFach(new FachTransfer(old, neu.getName()));
 				}
 			}
 		});
 		
 		
 		
 		pnl_fach_content.setLayout(new BorderLayout(0, 0));
 		JScrollPane scp_1 = new JScrollPane();
 		scp_1.add(fachlist);
 		pnl_fach_content.add(scp_1 , BorderLayout.CENTER);
 		
 		pnl_fach.add(pnl_fach_content, BorderLayout.CENTER);
 		pnl_fach.add(pnl_fach_btns, BorderLayout.SOUTH);
 		scp_1.setViewportView(fachlist);		
 		
 		
 	}
 
 	protected void refreshPoList() {
 		pomodel.removeAllElements();
 		poList = serverConnection.getPOs();
 		for(int i = 0;i<poList.size();i++){
 			pomodel.addElement(poList.get(i));
 		}		
 	}
 
 	protected ArrayList<Feld> tableToList() {
 		ArrayList<Feld> felder = new ArrayList<Feld>();
 		Vector data = tableFelder.getDataVector();
 		for (int i = 0; i < data.size(); i++) {
 			Vector tmp = (Vector) data.elementAt(i);
 			String label = (String) tmp.elementAt(1);
 			boolean dez = (boolean) tmp.elementAt(2);
 			felder.add(new Feld(label, "", dez));
 		}
 		// TODO Auto-generated method stub
 		return felder;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void addToTable(String name, boolean dezernat, int pos) {
 		// defaultFelder.add(new Feld(name,pos+"",dezernat));
 		// Collections.sort(defaultFelder, new Comparator<Feld>() {
 		// public int compare(Feld f1, Feld f2) {
 		// return f1.getValue().compareTo(f2.getValue());
 		// }
 		// });
 		// System.out.println(defaultFelder);
 		Vector data = tableFelder.getDataVector();
 		pos = pos - 1;
 		if (pos < 0) {
 			pos = 0;
 		} else if (pos > data.size()) {
 			pos = data.size();
 		}
 		Vector row = new Vector();
 		row.add(pos);
 		row.add(name);
 		row.add(dezernat);
 		data.insertElementAt(row, pos);
 		for (int i = 0; i < data.size(); i++) {
 			Vector tmp = (Vector) data.elementAt(i);
 			tmp.setElementAt(i + 1, 0);
 			data.setElementAt(tmp, i);
 		}
 		int w = table.getWidth() - 120;
 		table.getColumnModel().getColumn(0).setPreferredWidth(50);
 		table.getColumnModel().getColumn(0).setMaxWidth(50);
 		table.getColumnModel().getColumn(1).setPreferredWidth(w);
 		table.getColumnModel().getColumn(2).setPreferredWidth(70);
 		table.getColumnModel().getColumn(2).setMaxWidth(70);
 
 		tableFelder.setDataVector(data, columnIdentifiers);
 	}
 
 	private void modverwaltung() {
 		JPanel mv = new JPanel();
 		cards.add(mv, "modverwaltung");
 		JTable mods = new JTable();
 		JTable aktverwalter = new JTable();
 		JTable user = new JTable();
 
 		mv.setLayout(new GridLayout(2, 1));
 
 		JPanel buttons = new JPanel();
 
 		JPanel tabellen = new JPanel();
 		tabellen.setLayout(new GridLayout(2, 3, 0, 0));
 
 		JLabel modules = new JLabel("Module");
 		JLabel aktuelle = new JLabel("Aktuelle Verwalter");
 		JLabel rest = new JLabel("Userlist");
 
 		mv.setLayout(new GridLayout(2, 1));
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
 
 		// on construction
 		ArrayList<String> modstufflist = new ArrayList<String>();
 		ArrayList<User> alluser = new ArrayList<User>();
 		ArrayList<User> verwalter = new ArrayList<User>();
 
 		modstufflist = serverConnection.getallModulnames();
 
 		for (int i = 0; i < modstufflist.size(); i++) {
 			modstuff.addRow(new Object[] { modstufflist.get(i) });
 		}
 		ArrayList<ArrayList<User>> userlisting = serverConnection.getModulverwalter(null);
 		alluser = userlisting.get(0);
 		verwalter = userlisting.get(1);
 		for (int i = 0; i < alluser.size(); i++) {
 			userstuff.addRow(new Object[] { verwalter.get(i).geteMail(), verwalter.get(i).getVorname(), verwalter.get(i).getNachname() });
 		}
 		for (int i = 0; i < verwalter.size(); i++) {
 			userstuff2.addRow(new Object[] { alluser.get(i).geteMail(), alluser.get(i).getVorname(), alluser.get(i).getNachname() });
 		}
 		userstuff.addRow(new Object[] { "", "bla1-1", "bla1-2" });
 		userstuff2.addRow(new Object[] { "BLA2", "bla2-1", "bla2-2" });
 
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
 		frame.getContentPane().add(top, BorderLayout.NORTH);
 		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
 
 		JLabel lblMMS = new JLabel("Modul Management System");
 		lblMMS.setFont(new Font("Tahoma", Font.BOLD, 16));
 		lblMMS.setHorizontalAlignment(SwingConstants.LEFT);
 		lblMMS.setLabelFor(frame);
 		top.add(lblMMS);
 
 		Component horizontalGlue = Box.createHorizontalGlue();
 		top.add(horizontalGlue);
 		// top.add(btnNewButton);
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				showCard("welcome page");
 			}
 		});
 	}
 
 	/**
 	 * Hinzufgen eines Users in die Usertabelle
 	 * 
 	 * @param usr
 	 *            Zu hinzufgender User
 	 */
 	private void addToTable(User usr) {
 		tmodel.addRow(new Object[] { usr.getTitel(), usr.getVorname(), usr.getNachname(), usr.geteMail(), usr.getManageUsers(),
 				usr.getCreateModule(), usr.getAcceptModule(), usr.getManageSystem(), usr.getRedaktion() });
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
 		if (i == 1)
 			userstuff.addRow(new Object[] { user });
 		if (i == 2)
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
 		welcome = new HomeCard(frame);
 		cards.add(welcome, "welcome page");
 	}
 
 	/**
 	 * Erstellt den linke Teil der GUI
 	 * 
 	 */
 	private void leftscr() {
 		btnLogin.setToolTipText("Klicken Sie hier, um in das MMS einzuloggen.");
 		btnModulAkzeptieren
 				.setToolTipText("Klicken Sie hier, um das ausgewhlte Modul zu akzeptieren. Damit wird es freigegeben und in der Liste der aktuellen Module angezeigt.");
 		btnModulArchiv.setToolTipText("Klicken Sie hier, um das Archiv der aktuellen Module zu durchstbern.");
 		btnModulBearbeiten.setToolTipText("Klicken Sie hier, um bereits vorhandene Module zu bearbeiten.");
 		btnModulEinreichen
 				.setToolTipText("Klicken Sie hier, um das Modul einzureichen. Damit wird es der verantwortlichen Stelle vorgelegt. Das Modul wird erst nach der Besttigung der verantwortlichen Stelle in der Liste der aktuellen Module angezeigt.");
 		btnUserVerwaltung
 				.setToolTipText("Klicken Sie hier, um die Benutzerverwaltung aufzurufen. Hier knnen Sie neue Benutzer anlegen, deren Daten ndern und ihre Rechte im MMS festlegen.");
 		btnVerwaltung
 				.setToolTipText("Klicken Sie hier, um die Verwaltung zu ffnen. Hier knnen Sie mgliche Studiengnge festlegen, fr die es Modulhandbcher geben kann, die Deadline fr die Modulhandbcher aktualisieren und Verwalter fr bestimmte Module festlegen.");
 
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
 				studienlist = serverConnection.getStudiengaenge(false);
 				ArrayList<Modulhandbuch> mbs = new ArrayList<Modulhandbuch>();
 				cbmodel.removeAllElements();
 				
 				for (int i = 0; i < studienlist.size(); i++) {
 					cbmodel.addElement(studienlist.get(i));
 					mbs.addAll(studienlist.get(i).getModbuch());
 				}
 				defaultFelder = serverConnection.getDefaultFelder();
 				// cbmodel_Z.removeAllElements();
 				// for (int i = 0; i < typen.size(); i++) {
 				// cbmodel_Z.addElement(typen.get(i));
 				// }
 
 				if(nModCard!=null) nModCard.removeAll();
 				
 				nModCard = new newModulCard(defaultFelder, mbs, serverConnection, current).getPanel();
 				cards.add(nModCard,"newmodule");
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
 				studienlist=serverConnection.getStudiengaenge(false);
 				ArrayList<Modulhandbuch> mbs = new ArrayList<Modulhandbuch>();
 				for(int i=0;i<studienlist.size();i++){
 					mbs.addAll(studienlist.get(i).getModbuch());
 				}
 				cbModBuchMo.removeAllElements();
 				for (int i = 0; i < mbs.size(); i++) {
 					cbModBuchMo.addElement(mbs.get(i));
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
 						stopThread();
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
 				
 				refreshPoList();
				
 				studienlist = serverConnection.getStudiengaenge(false);
 				for (int i = 0; i < studienlist.size(); i++) {
 					Studiengang s = studienlist.get(i);
 					ArrayList<Modulhandbuch> mb = s.getModbuch();
 					for (int j = 0; j < mb.size(); j++) {
 						Modulhandbuch m = mb.get(j);
 						nichtAckMBs.add(m);
 						modListModel.addElement(s.getAbschluss() + " " + s.getName() + ", PO " + m.getPruefungsordnungsjahr()
 								+ ", Modulhandbuch " + m.getJahrgang());
 					}
 				}
 				
 				fachzws = serverConnection.getFach();
 				for(int i = 0; i < fachzws.size(); i++){
 					fachmodel.addElement(fachzws.get(i));
 				}
 
 				defaultFelder = serverConnection.getDefaultFelder();
 				addToTableFelder();
 				studienlist = serverConnection.getStudiengaenge(true);
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
 				calender.setDate(serverConnection.getDate());
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
 					studienlist = serverConnection.getStudiengaenge(true);
 					for (int i = 0; i < studienlist.size(); i++) {
 						addToTable(studienlist.get(i));
 					}
 					// TODO something
 					// typen = serverConnection.getZuordnungen();
 					looking.setConnection(serverConnection);
 					looking.setStudienlist(studienlist);
 					looking.buildTree();
 					// Zur Card wechseln
 					showCard("studiengang show");
 				} else {
 					noConnection();
 				}
 			}
 
 		});
 	}
 
 	protected void addToTableFelder() {
 		tableFelder.setRowCount(0);
 		for (int i = 0; i < defaultFelder.size(); i++) {
 			Feld f = defaultFelder.get(i);
 			addToTable(f.getLabel(), f.isDezernat(), i + 1);
 		}
 
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
 
 		btnOk.setToolTipText("Klicken, um ihr Modul einzureichen.");
 		btnHome.setToolTipText("Klicken, um zurck in den Hauptbildschirm zu gelangen.");
 		btnNeuesFeld.setToolTipText("Klicken, um ein neues Feld in ihrem Modul zu erstellen.");
 
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
 		selectedCard = card;
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
 				"Module einreichen", "Module Annehmen", "Verwaltung", "Redaktion" }) {
 			@SuppressWarnings("rawtypes")
 			Class[] columnTypes = new Class[] { String.class, String.class, String.class, String.class, boolean.class, boolean.class,
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
 
 		btnUserAdd.setToolTipText("Klicken, um neuen Benutzer hinzuzufgen.");
 		btnHome.setToolTipText("Klicken, um zurck zum Hauptbildschirm zu gelangen.");
 		btnUserDel.setToolTipText("Klicken, um den ausgewhlten Benutzer zu lschen.");
 		btnUserEdit.setToolTipText("Klicken, um den ausgewhlten Benutzer zu bearbeiten.");
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
 		canReadMessages = false;
 		welcome.setMessageView(false);
 		welcome.setUser(current);
 		welcome.setConnection(serverConnection);
 		top.remove(btnNewButton);
 		if (current.getCreateModule()) {
 			btnModulEinreichen.setEnabled(true);
 			btnModulBearbeiten.setEnabled(true);
 			canReadMessages = true;
 		}
 		if (current.getAcceptModule()) {
 			btnModulBearbeiten.setEnabled(true);
 			btnModulAkzeptieren.setEnabled(true);
 			canReadMessages = true;
 		}
 		if (current.getManageSystem()) {
 			btnVerwaltung.setEnabled(true);
 			canReadMessages = true;
 		}
 		if (current.getManageUsers()) {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("User Verwaltung");
 			canReadMessages = true;
 		} else {
 			btnUserVerwaltung.setEnabled(true);
 			btnUserVerwaltung.setText("Account bearbeiten");
 		}
 		if (canReadMessages) {
 			top.add(btnNewButton);
 			startThread();
 			welcome.setMessageView(true);
 		}
 		showCard("welcome page");
 	}
 
 	private void startThread() {
 		run = true;
 		new Thread() {
 			@Override
 			public void run() {
 				int time = 10;// Aktualisierungsintevall in Sekunden
 				while (run) {
 					messages = welcome.getMessageCount();
 					welcome.refreshMessages();
 					welcome.getDate();
 					newMessages = welcome.getMessageCount();
 					for (int i = 0; i < time; i++) {
 						if (newMessages > 1) {
 							btnNewButton.setText("Sie haben " + newMessages + " neuen Nachrichten.");
 						} else if (newMessages == 1) {
 							btnNewButton.setText("Sie haben eine neue Nachricht.");
 						} else {
 							btnNewButton.setText("Sie haben keine neue Nachricht.");
 						}
 						try {
 							sleep(1000);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							// e.printStackTrace();
 						}
 					}
 
 				}
 			}
 		}.start();
 	}
 
 	public void stopThread() {
 		run = false;
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
 							ArrayList<Modul> rel = serverConnection.getUserRelation(current.geteMail());
 							if (rel.contains(m)) {
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
 							try{
 							cards.remove(modeditp);
 							}catch(NullPointerException e){
 								
 							}
 							ArrayList<Modulhandbuch> mb = new ArrayList<Modulhandbuch>();
 							modeditp = new newModulCard(m.getFelder(), mb, serverConnection, current).getPanel();
 							cards.add(modeditp, "modul edit");
 							showCard("modul edit");
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
 //					if (!m.isInbearbeitung()) {
 //						if (m.getName().isEmpty()) {
 //							JOptionPane.showMessageDialog(frame, "Bei diesem Modul sind nicht alle Felder ausgefllt!", "Fehler im Modul",
 //									JOptionPane.ERROR_MESSAGE);
 //						} else {
 //							// Prfe, ob ein Feld fr das Dezernat 2 markiert
 //							// wurde
 //							// und ob alle Felder ausgefllt wurden
 //							boolean hasDezernat = false;
 //							boolean isCorrect = true;
 //							ArrayList<Feld> felder = m.getFelder();
 //							for (int i = 0; i < felder.size(); i++) {
 //								if (felder.get(i).getValue().isEmpty()) {
 //									JOptionPane.showMessageDialog(frame, "Bei diesem Modul sind nicht alle Felder ausgefllt!",
 //											"Fehler im Modul", JOptionPane.ERROR_MESSAGE);
 //									isCorrect = false;
 //									break;
 //								}
 //								if (felder.get(i).isDezernat()) {
 //									hasDezernat = true;
 //								}
 //							}
 //
 //							if (isCorrect) {
 //								boolean checked = true;
 //								// Wenn Felder als Dezernat 2 markiert wurden,
 //								// nach Besttigung fragen
 //								if (hasDezernat) {
 //									int n = JOptionPane.showConfirmDialog(frame,
 //											"Dieses Modul besitzt Felder, die vom Dezernat2 berprft werden mssen, wurde das getan?",
 //											"Besttigung", JOptionPane.YES_NO_OPTION);
 //									if (n == 0) {
 //										checked = true;
 //									} else {
 //										checked = false;
 //									}
 //								}
 //								if (checked) {
 //									// Bei besttigung Modul akzeptieren und
 //									// Listen neu abrufen
 //									// dann zur Bearbeiten bersicht wechseln
 //									serverConnection.acceptModul(m);
 //									//ArrayList<Modul> module = serverConnection.getModule(false);
 //									lm.removeAllElements();
 //									for (int i = 0; i < module.size(); i++) {
 //										lm.addElement(module.get(i));
 //									}
 //
 //									//module = serverConnection.getModule(true);
 //									lm_ack.removeAllElements();
 //									for (int i = 0; i < module.size(); i++) {
 //										lm_ack.addElement(module.get(i));
 //									}
 //									showCard("modulbearbeiten");
 //								}
 //							}
 //						}
 //					} else {
 //						JOptionPane.showMessageDialog(frame, "Dieses Modul befindet sich gerade in bearbeitung!", "Zugriff verweigert",
 //								JOptionPane.ERROR_MESSAGE);
 //					}
 					serverConnection.acceptModul(m);
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
 		
 		JPanel panel = new JPanel();
 		nichtakzeptiert.add(panel, BorderLayout.NORTH);
 		
 		JLabel lblModBuch = new JLabel("Modulhandbuch: ");
 		panel.add(lblModBuch);
 		
 		final JComboBox<Modulhandbuch> comboBox= new JComboBox<Modulhandbuch>(cbModBuchMo);
 		comboBox.addItemListener(new ItemListener() {
 	        public void itemStateChanged(ItemEvent arg0) {
 	        	lm.removeAllElements();	        	
 	        	for(int i=0;i<studienlist.size();i++){
 	        		ArrayList<Modulhandbuch> mbs = studienlist.get(i).getModbuch();
 	        		for(int j=0;j<mbs.size();j++){
 	        			Modulhandbuch mb = mbs.get(j);
 	        			ArrayList<Fach> fs=mb.getFach();
 	        			for(int k=0;k<fs.size();k++){
 	        				Fach f = fs.get(k);
 	        				ArrayList<Modul> module = f.getModlist();
 	        				for (int l = 0; l < module.size(); l++) {
 	        					Modul m = module.get(l);
 	        					if(m.getStatus()<3){
 	        						lm.addElement(module.get(i));
 	        					}
 	        				}
 	        			}
 	        		}
 	        	}
 
 				
 					
 	        }
 	    });
 		panel.add(comboBox);
 
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
 
 		// Zurck zur Startseite
 		JButton btnZurck2 = new JButton("Zur\u00FCck");
 		btnZurck2.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showCard("welcome page");
 			}
 		});
 		buttonpnl2.add(btnZurck2);
 		
 		JPanel panel_1 = new JPanel();
 		akzeptiert.add(panel_1, BorderLayout.NORTH);
 		
 		JLabel label = new JLabel("Studiengang: ");
 		panel_1.add(label);
 		
 		final JComboBox<Modulhandbuch> comboBox_1 = new JComboBox<Modulhandbuch>(cbModBuchMo);
 		comboBox_1.addItemListener(new ItemListener() {
 	        public void itemStateChanged(ItemEvent arg0) {		
 				lm_ack.removeAllElements();
 				for(int i=0;i<studienlist.size();i++){
 	        		ArrayList<Modulhandbuch> mbs = studienlist.get(i).getModbuch();
 	        		for(int j=0;j<mbs.size();j++){
 	        			Modulhandbuch mb = mbs.get(j);
 	        			ArrayList<Fach> fs=mb.getFach();
 	        			for(int k=0;k<fs.size();k++){
 	        				Fach f = fs.get(k);
 	        				ArrayList<Modul> module = f.getModlist();
 	        				for (int l = 0; l < module.size(); l++) {
 	        					Modul m = module.get(l);
 	        					if(m.getStatus()==3){
 	        						lm_ack.addElement(module.get(i));
 	        					}
 	        				}
 	        			}
 	        		}
 	        	}
 					
 	        }
 	    });
 		panel_1.add(comboBox_1);
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
 		looking = new LookCard();
 		JPanel studiengangshow = new JPanel();
 		cards.add(looking, "studiengang show");
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
 		// System.out.println(studienlist.get(0).getModbuch().get(0).getId());
 		// modulhandlist =
 		// serverConnection.getModulhandbuch(studtransferstring);
 		for (int i = 0; i < studienlist.size(); i++) {
 			if (studienlist.get(i).getName().equalsIgnoreCase(studtransferstring)) {
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
