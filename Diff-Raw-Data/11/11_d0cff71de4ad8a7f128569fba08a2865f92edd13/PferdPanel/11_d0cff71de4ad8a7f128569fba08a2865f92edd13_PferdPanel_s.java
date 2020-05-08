 package sepm.ss13.e1005233.gui;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.apache.log4j.Logger;
 
 import sepm.ss13.e1005233.domain.Buchung;
 import sepm.ss13.e1005233.domain.Pferd;
 import sepm.ss13.e1005233.domain.Rechnung;
 import sepm.ss13.e1005233.domain.SuchPferd;
 import sepm.ss13.e1005233.exceptions.BuchungValidationException;
 import sepm.ss13.e1005233.exceptions.NotEnoughPferdeException;
 import sepm.ss13.e1005233.exceptions.PferdPersistenceException;
 import sepm.ss13.e1005233.exceptions.PferdValidationException;
 import sepm.ss13.e1005233.exceptions.RechnungPersistenceException;
 import sepm.ss13.e1005233.exceptions.RechnungValidationException;
 import sepm.ss13.e1005233.service.JDBCService;
 import sepm.ss13.e1005233.service.Service;
 
 import net.miginfocom.swing.MigLayout;
 
 public class PferdPanel extends JPanel implements ActionListener{
 
 	private Service service;
 	private JScrollPane scrollpanepferd, scrollpaneaktiv;
 	private JTable pferde, aktiv;
 	private JButton deleteButton, updatePferdButton, suchButton,
 	addZuRechnungButton, rechnungSpeichernButton, resetButton, cancelPferdButton,
 	addPferdButton, rechnungAbbrechenButton, picButton, editPicButton, editPferdButton;
 	private JPanel neuRechnungPanel, suchPanel,	neuPferdPanel, editPferdPanel, neuRechnungDetailsPanel;
 	private JLabel picPferd, pferdLabel, editPicLabel, descSearch, descInsert, picLabel, editInsert;
 	private JComboBox<String> therapieAuswahl, kinderAuswahl, therapieForm, editTherapieForm, zahlungsAuswahl;
 	private JMenuBar menuBar;
 	private JMenu menu;
 	private Pferd p;
 	private String selectedPic, zahlungsart;
 	private JCheckBox insertKinder, editInsertKinder;
 	private JFrame parent;
 	private PferdeTable ctm;
 	private AktivTable atm;
 	private JFileChooser picChooser;
 	private ListSelectionListener lsl= new ListSelectionListener() {
 	      @Override
 		public void valueChanged(ListSelectionEvent e) {
 	    	  if(pferde.getSelectedRow()<0 || pferde.getSelectedColumn() < 0) {
 	    		  return;
 	    	  }
 	    	  setPferdIcon(service.getPferd(new Pferd((int) pferde.getValueAt(pferde.getSelectedRow(), 0))).getFoto());
 	      }
 	    };
 	private JTextField preisVonTextField, preisBisTextField, nameTextField, rasseTextField, nameForm,
 	 editNameForm, editPreisForm, editRasseForm, rasseForm, preisForm, rNameForm, telefonForm;
 	private JMenuItem menuItem;
 	private String therapieart;
 	private final Object[][] temp = {};
 	private final String[] loeschen = {"Ja", "Nein"};
 	private final String[] kinderfreundlich = {"(Egal)", "Ja", "Nein"};
 	private final String[] therapieArten = {"(Egal)", "Hippotherapie","Heilpdagogisches Voltigieren","Heilpdagogisches Reiten"};
 	private final String[] insertTherapieArten = {"Hippotherapie","Heilpdagogisches Voltigieren","Heilpdagogisches Reiten"};
 	private final String[] zahlungsarten = {"Barzahlung", "Kreditkarte", "berweisung"};
 	private final String[] pferdColumnNames = {"ID#", "Name", "Preis", "Therapieart", "Rasse", "Fr Kinder"};
 	private final String[] aktivColumnNames = {"ID#", "Name", "Preis/h", "Stunden"};
 	private ListSelectionModel ldm;
 	private int editPferdId;
 	private int aktivRows;
 	private Object[][] oldRows = {};
 	private static final Logger log = Logger.getLogger(PferdPanel.class);
 	public PferdPanel(JFrame parent, JDBCService service) {
 		super(new MigLayout());
 		log.debug("Erstelle neues PferdPanel...");
 		
 		this.service = service;
 		picChooser = new JFileChooser();
 		selectedPic = "";
 		picChooser.setFileFilter(new BildFilter());
 		this.parent = parent;
 		//erstelle Menu
 		menuBar = new JMenuBar();
 		menu = new JMenu("Aktionen");
 		menuBar.add(menu);
 		menuItem = new JMenuItem("Neues Pferd...");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("NeuesPferdForm");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Neue Rechnung...");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("NeueRechnung");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Beliebteste Pferde...");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("PopularPferde");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Beenden");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("Beenden");
 		menu.add(menuItem);	
 
 		//erstelle Suchpanel
 		suchPanel = new JPanel(new MigLayout());
 		
 		descSearch = new JLabel("SUCHE:");
 		suchPanel.add(descSearch, " wrap");
 		descSearch = new JLabel("Name:      ");
 		suchPanel.add(descSearch, "split 2");
 		nameTextField = new JTextField(13);
 		suchPanel.add(nameTextField, "wrap");
 		
 		descSearch = new JLabel("Rasse:     ");
 		suchPanel.add(descSearch, "split 2");
 		rasseTextField = new JTextField(13);
 		suchPanel.add(rasseTextField, "wrap");
 		
 		descSearch = new JLabel("Preis:        Von");
 		suchPanel.add(descSearch, "split 4");
 		preisVonTextField = new JTextField(4);
 		preisVonTextField.setText("0");
 		suchPanel.add(preisVonTextField, "span");
 		descSearch = new JLabel("bis");
 		suchPanel.add(descSearch);
 		preisBisTextField = new JTextField(4);
 		preisBisTextField.setText("0");
 		suchPanel.add(preisBisTextField, "wrap");
 		
 		descSearch = new JLabel("Therapieart:");
 		suchPanel.add(descSearch, "wrap");
 		therapieAuswahl = new JComboBox<String>(therapieArten);
 		suchPanel.add(therapieAuswahl, "wrap");
 		therapieAuswahl.setSelectedIndex(0);
 		
 		descSearch = new JLabel("Kinderfreundlich:");
 		suchPanel.add(descSearch, "wrap");
 		kinderAuswahl = new JComboBox<String>(kinderfreundlich);
 		suchPanel.add(kinderAuswahl, "wrap");
 		therapieAuswahl.setSelectedIndex(0);
 				
 		suchButton = new JButton("Suchen");
 		suchButton.setActionCommand("SUCHEN");
 		suchButton.addActionListener(this);
 		suchPanel.add(suchButton, "split 2");
 		resetButton = new JButton("Reset");
 		resetButton.setActionCommand("RESET");
 		resetButton.addActionListener(this);
 		suchPanel.add(resetButton, "wrap");
 		
 		//erstelle Pferd-profilbild
 		picPferd = new JLabel("Pferd auswhlen um Bild anzuzeigen.");
 		setPferdIcon("Profilbilder/logo.jpg");
 		descSearch = new JLabel("Profilbild:");
 		suchPanel.add(descSearch, "wrap, gaptop 35");
 		suchPanel.add(picPferd, "span");
 		
 		add(suchPanel, "dock west");
 		
 		//Erstelle Buttons fr Pferdfunktionalitten
 		pferdLabel = new JLabel("Ausgewhltes Pferd: ");
 		add(pferdLabel, "split 3, gapleft 5");
 		
 		updatePferdButton = new JButton("Bearbeiten");
 		updatePferdButton.setActionCommand("PferdBearbeiten");
 		updatePferdButton.addActionListener(this);
 		add(updatePferdButton, "gapleft 5, gaptop 7");
 		
 		deleteButton = new JButton("Lschen");
 		deleteButton.setActionCommand("PferdLschen");
 		deleteButton.addActionListener(this);
 		add(deleteButton, "wrap, gaptop 5");
 				
 		//Erstelle Pferdetabelle, uneditierbar
 		ctm = new PferdeTable(updateTable(service.findAllUndeletedPferde()), pferdColumnNames);
 		pferde = new JTable(ctm);
 		ldm = pferde.getSelectionModel();
 		ldm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		ldm.addListSelectionListener(lsl);
 		scrollpanepferd = new JScrollPane(pferde);
 		add(scrollpanepferd);
 		
 		
 		//Erstelle Panel fr neue Rechnungen
 		neuRechnungPanel = new JPanel(new MigLayout());
 		
 		addZuRechnungButton = new JButton("Hinzufgen");
 		addZuRechnungButton.addActionListener(this);
 		addZuRechnungButton.setActionCommand("RechnungHinzufgen");
 		neuRechnungPanel.add(addZuRechnungButton, "split 3");
 		
 		rechnungSpeichernButton = new JButton("Rechnung speichern");
 		rechnungSpeichernButton.addActionListener(this);
 		rechnungSpeichernButton.setActionCommand("RechnungSpeichern");
 		neuRechnungPanel.add(rechnungSpeichernButton);
 		
 		rechnungAbbrechenButton = new JButton("Abbrechen");
 		rechnungAbbrechenButton.addActionListener(this);
 		rechnungAbbrechenButton.setActionCommand("RechnungAbbrechen");
 		neuRechnungPanel.add(rechnungAbbrechenButton, "wrap");
 		
 		neuRechnungDetailsPanel = new JPanel(new MigLayout());
 		
 		descInsert = new JLabel("Name:    ");
 		neuRechnungDetailsPanel.add(descInsert, "split 4");
 		rNameForm = new JTextField(8);
 		neuRechnungDetailsPanel.add(rNameForm);
 		
 		descInsert = new JLabel("Zahlungsart: ");
 		neuRechnungDetailsPanel.add(descInsert);
 		zahlungsAuswahl = new JComboBox<String>(zahlungsarten);
 		zahlungsAuswahl.setSelectedIndex(-1);
 		neuRechnungDetailsPanel.add(zahlungsAuswahl, "wrap");
 		
 		descInsert = new JLabel("Telefon: ");		
 		neuRechnungDetailsPanel.add(descInsert, "split 2");
 		telefonForm = new JTextField(8);
 		neuRechnungDetailsPanel.add(telefonForm, "wrap");
 		
 		neuRechnungPanel.add(neuRechnungDetailsPanel, "wrap");
 		
 		
 		aktivRows = 0;
 		atm = new AktivTable(temp, aktivColumnNames);
 		aktiv = new JTable(atm);
 		scrollpaneaktiv = new JScrollPane(aktiv);
 		scrollpaneaktiv.setPreferredSize(new Dimension(340, 370));
 		ldm = aktiv.getSelectionModel();
 		ldm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		neuRechnungPanel.add(scrollpaneaktiv);
 		
 
 		
 		
 		
 		add(neuRechnungPanel, "dock east");
 		removeRechnung();
 		
 		//Erstelle Panel fr neue Pferde
 		neuPferdPanel = new JPanel(new MigLayout());
 		
 		descInsert = new JLabel("Neues Pferd hinzufgen:");
 		neuPferdPanel.add(descInsert, "wrap");
 		
 		descInsert = new JLabel("Name:  ");
 		neuPferdPanel.add(descInsert, "split 2");
 		nameForm = new JTextField(13);
 		neuPferdPanel.add(nameForm, "wrap");		
 		
 		descInsert = new JLabel("Rasse: ");
 		neuPferdPanel.add(descInsert, "split 2");
 		rasseForm = new JTextField(13);
 		neuPferdPanel.add(rasseForm, "wrap");
 		
 		descInsert = new JLabel("Stundenpreis:  ");
 		neuPferdPanel.add(descInsert, "split 2");
 		preisForm = new JTextField(9);
 		neuPferdPanel.add(preisForm, "wrap");
 		
 		descInsert = new JLabel("Therapieart:");
 		neuPferdPanel.add(descInsert, "wrap");
 		therapieForm = new JComboBox<String>(insertTherapieArten);
 		neuPferdPanel.add(therapieForm, "wrap");
 		therapieForm.setSelectedIndex(-1);
 		
 		picButton = new JButton("Whle Profilbild");
 		picButton.addActionListener(this);
 		picButton.setActionCommand("PicAuswahl");
 		neuPferdPanel.add(picButton, "wrap");
 		picLabel = new JLabel("Kein Bild ausgewhlt.");
 		neuPferdPanel.add(picLabel, "wrap");
 		
 		insertKinder = new JCheckBox("Kinderfreundlich");
 		neuPferdPanel.add(insertKinder, "wrap");
 		
 		addPferdButton = new JButton("Speichern");
 		addPferdButton.addActionListener(this);
 		addPferdButton.setActionCommand("NeuesPferdSpeichern");
 		neuPferdPanel.add(addPferdButton, "split 2");
 		
 		cancelPferdButton = new JButton("Abbrechen");
 		cancelPferdButton.addActionListener(this);
 		cancelPferdButton.setActionCommand("NeuesPferdAbbrechen");
 		neuPferdPanel.add(cancelPferdButton, "wrap");
 		
 		add(neuPferdPanel, "dock east");
 		removePferdForm();
 		
 		
 		//erstelle Panel zum Bearbeiten von Pferden
 		editPferdPanel = new JPanel(new MigLayout());
 		
 		editInsert = new JLabel("Pferd bearbeiten:");
 		editPferdPanel.add(editInsert, "wrap");
 		
 		editInsert = new JLabel("Name:  ");
 		editPferdPanel.add(editInsert, "split 2");
 		editNameForm = new JTextField(13);
 		editPferdPanel.add(editNameForm, "wrap");		
 		
 		editInsert = new JLabel("Rasse: ");
 		editPferdPanel.add(editInsert, "split 2");
 		editRasseForm = new JTextField(13);
 		editPferdPanel.add(editRasseForm, "wrap");
 		
 		editInsert = new JLabel("Stundenpreis:  ");
 		editPferdPanel.add(editInsert, "split 2");
 		editPreisForm = new JTextField(9);
 		editPferdPanel.add(editPreisForm, "wrap");
 		
 		editInsert = new JLabel("Therapieart:");
 		editPferdPanel.add(editInsert, "wrap");
 		editTherapieForm = new JComboBox<String>(insertTherapieArten);
 		editPferdPanel.add(editTherapieForm, "wrap");
 		editTherapieForm.setSelectedIndex(-1);
 		
 		editPicButton = new JButton("Whle Profilbild");
 		editPicButton.addActionListener(this);
 		editPicButton.setActionCommand("PicAuswahl");
 		editPferdPanel.add(editPicButton, "wrap");
 		editPicLabel = new JLabel("Kein Bild ausgewhlt.");
 		editPferdPanel.add(editPicLabel, "wrap");
 		
 		editInsertKinder = new JCheckBox("Kinderfreundlich");
 		editPferdPanel.add(editInsertKinder, "wrap");
 		
 		editPferdButton = new JButton("Speichern");
 		editPferdButton.addActionListener(this);
 		editPferdButton.setActionCommand("PferdBearbeitenSpeichern");
 		editPferdPanel.add(editPferdButton, "split 2");
 		
 		cancelPferdButton = new JButton("Abbrechen");
 		cancelPferdButton.addActionListener(this);
 		cancelPferdButton.setActionCommand("PferdBearbeitenAbbrechen");
 		editPferdPanel.add(cancelPferdButton, "wrap");
 		
 		add(editPferdPanel, "dock east");
 		removeEditPferd();
 	}
 	
 	public Object[][] updateTable(List<Pferd> pferdeList){
 		log.debug("Aktualisiere Tabelle mit Pferden...");
 		Object[][] allPferdeArray = new Object[pferdeList.size()][6];
 		int i = 0;
 		for(Pferd p : pferdeList) {
 			allPferdeArray[i][0] = p.getId();
 			allPferdeArray[i][1] = p.getName();
 			allPferdeArray[i][2] = p.getPreis();
 			allPferdeArray[i][3] = p.getTherapieart();
 			allPferdeArray[i][4] = p.getRasse();
 			allPferdeArray[i][5] = p.isKinderfreundlich();
 			
 			i++;
 		}
 		return allPferdeArray;
 	}
 	
 	/**
 	 * ndert das angezeigte Bild mit dem in path angegebenen.
 	 * @param path der Dateipfad zum Bild
 	 */
 	public void setPferdIcon(String path) {
 		log.info("Bereite nderung vom Bild vor...");
 		BufferedImage myPicture = null;
 		Image im = null;
 		//reset Text and Icon
 		picPferd.setIcon(null);
 		picPferd.setText("");
 
 		try {
 			myPicture = ImageIO.read(new File(path));
 			log.debug("Image size is " + myPicture.getWidth() + " x " + myPicture.getHeight());
 			im = myPicture.getScaledInstance(200, (int) (200 * ((double)myPicture.getHeight()/myPicture.getWidth())), Image.SCALE_SMOOTH);
 			if(myPicture.getHeight() > myPicture.getWidth())
 				im = im.getScaledInstance((int) (170 * ((double)myPicture.getWidth()/myPicture.getHeight())), 170, Image.SCALE_SMOOTH);
 			log.info("Bild vom Pferd geladen!");
 			picPferd.setIcon(new ImageIcon(im));
 		} catch (IOException e) {
 			log.info("Kein zugehriges Bild zum Pferd gespeichert!");
 			picPferd.setText("Bild konnte nicht geladen werden.");
 		}
 	}
 	/**
 	 * Gibt das Menue zurck
 	 * @return
 	 */
 	public JMenuBar getJMenuBar() {
 		return menuBar;
 	}
 	/**
 	 * Lscht den Rechnungsteil aus dem GUI
 	 */
 	public void removeRechnung() {
 		log.info("Lsche Rechnungdialog...");
 		remove(neuRechnungPanel);
 	}
 	/**
 	 * Fgt den Rechnungsteil zum GUI hinzu
 	 */
 	public void addRechnung() {
 		log.info("Fge Rechnungdialog hinzu...");
 		add(neuRechnungPanel, "dock east");
 	}
 	/**
 	 * Fgt das Pferdformular zum GUI hinzu
 	 */
 	public void addPferdForm() {
 		log.info("Fge Pferdformular hinzu...");
 		add(neuPferdPanel, "dock east");
 	}
 	/**
 	 * Lscht das Pferdformular aus dem GUI
 	 */
 	public void removePferdForm() {
 		log.info("Lsche Pferdformular...");
 		remove(neuPferdPanel);
 	}
 	/**
 	 * Fgt das Bearbeitenformular zum GUI hinzu
 	 */
 	public void addEditPferd() {
 		log.info("Fge Pferdformular zur Bearbeitung hinzu...");
 		add(editPferdPanel, "dock east");
 	}
 	/**
 	 * Lscht das Bearbeitenformular aus dem GUI
 	 */
 	public void removeEditPferd() {
 		log.info("Lsche Formular zur Bearbeitung...");
 		remove(editPferdPanel);
 	}
 	
 	/**
 	 * Aktualisiert den Frame
 	 */
 	public void updateFrame() {
 		log.info("Aktualisiere Frame...");
 		repaint();
 		parent.repaint();
 		parent.pack();
 	}
 	
 	/**
 	 * Durchsucht die Datenbank mit den Kriterien, die im Suchfeld angegeben sind.
 	 * Aktualisiert dann die Tabelle mit den gefundenen Ergebnissen
 	 */
 	public void startSuche() {
 		log.info("Starte Suchvorgang...");
 		String therapieart = "";
 		String kinderfreundlich = "";
 		switch (therapieAuswahl.getSelectedIndex()) {
 		case 0:
 			therapieart = "";	
 			break;
 		case 1:
 			therapieart = "Hippotherapie";
 			break;
 		case 2:
 			therapieart = "HPV";
 			break;
 		case 3:
 			therapieart = "HPR";
 			break;
 		}
 		switch (kinderAuswahl.getSelectedIndex()) {
 		case 0:
 			kinderfreundlich = "";
 			break;
 		case 1:
 			kinderfreundlich = "TRUE";
 			break;
 		case 2:
 			kinderfreundlich = "FALSE";
 			break;
 		}
 
 		try {
 			ctm = new PferdeTable(updateTable(service.findBy(new SuchPferd(nameTextField.getText(), therapieart,
 					rasseTextField.getText(), Double.parseDouble(preisVonTextField.getText()),
 					Double.parseDouble(preisBisTextField.getText()), kinderfreundlich))), pferdColumnNames);
 		    pferde.setModel(ctm);
 		    ctm.fireTableDataChanged();
 		    ctm.fireTableStructureChanged();
 		} catch (NumberFormatException e2) {
 			log.debug("Eingabe ist zu lang!");
 			JOptionPane.showMessageDialog(this,"Preisangabe muss eine (Dezimal-)Zahl sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 		}
 		
 	}
 	/**
 	 * Gibt das in der Tabelle ausgewhlte Pferd zurck
 	 * @return das ausgewhlte Pferd
 	 */
 	public Pferd getSelectedPferd() {
 		int id =(int)pferde.getValueAt(pferde.getSelectedRow(), 0);
 		log.info("Gebe ausgewhltes Pferd zurck mit der ID: " + id);
 		return new Pferd(id);
 	}
 	
 	/**
 	 * Setzt das Formular zum Einfgen neuer Pferde zurck
 	 */
 	public void resetInsertPferdForm() {
 		log.debug("Setze Pferdformular zurck...");
 		nameForm.setText("");
 		rasseForm.setText("");
 		preisForm.setText("");
 		insertKinder.setSelected(false);
 		therapieForm.setSelectedIndex(-1);
 	}
 	/**
 	 * EventHandler. Fhrt Aktionen aus wenn Buttons
 	 * und Menpunkte gedrckt werden
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		switch (e.getActionCommand()) {
 		case "SUCHEN":
 			startSuche();
 			break;
 		case "RESET":
 			log.info("Resette Eingabefeld...");
 			preisVonTextField.setText("0");
 			preisBisTextField.setText("0");
 			nameTextField.setText(null);
 			rasseTextField.setText(null);
 			setPferdIcon("Profilbilder/logo.jpg");
 			therapieAuswahl.setSelectedIndex(0);
 			kinderAuswahl.setSelectedIndex(0);
 			startSuche();
 			break;
 		case "NeueRechnung":
 			log.info("Erstelle Rechnungsfeld...");
 			removePferdForm();
 			removeEditPferd();
 			addRechnung();
 			updateFrame();
 			break;
 		case "RechnungSpeichern":
 			log.info("Speichere Rechnung ab...");
 			if(atm.getRowCount() == 0) {
 				log.debug("Es wurden keine Pferde zur Buchung gewhlt, breche Abspeichern ab...");
 				JOptionPane.showMessageDialog(this,"Whle zuerst zu buchende Pferde aus!", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			zahlungsart = "";
 			switch (zahlungsAuswahl.getSelectedIndex()) {
 			case -1:
 				JOptionPane.showMessageDialog(this,"Gebe eine Zahlungsart an.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			case 0:
 				zahlungsart = "Barzahlung";
 				break;
 			case 1:
 				zahlungsart = "Kreditkarte";
 				break;
 			case 2:
 				zahlungsart = "Ueberweisung";
 				break;
 			}
 			zahlungsAuswahl.setSelectedIndex(-1);
 			Timestamp datum = new Timestamp(new Date().getTime());
 			double gesamtpreis = 0;
 			int gesamtstunden = 0;
 			try {
 			List<Buchung> buchungen = new ArrayList<Buchung>();
 			for(int i = 0; i < atm.getRowCount(); i++) {
 				int id = (int) atm.getValueAt(i, 0);
 				double preis = (double) atm.getValueAt(i, 2);
 				int stunden = (int) atm.getValueAt(i, 3);
 				gesamtpreis += (preis*stunden);
 				gesamtstunden += stunden;
 				buchungen.add(new Buchung(new Pferd(id), new Rechnung(datum), stunden, preis));
 			}
			Rechnung r = new Rechnung(datum, rNameForm.getText(), zahlungsart, gesamtpreis, gesamtstunden, Long.parseLong(telefonForm.getText()), buchungen);
 			service.insertRechnung(r);
 			} catch (NumberFormatException e3) {
 				log.debug("Eingabe ist keine Dezimalzahl!");
 				JOptionPane.showMessageDialog(this,"Telefonnummer muss eine Ganzzahl (grer als 1000 und kleiner als 9223372036854775806) sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch (BuchungValidationException e4) {
 				log.debug("Buchungsinformationen fehlerhaft.");
 				JOptionPane.showMessageDialog(this,"Bitte berprfe die Buchungen.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch (RechnungValidationException e1) {
 				log.debug("Rechnungsattribute fehlerhaft!");
				JOptionPane.showMessageDialog(this,"Name darf nicht leer sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch (RechnungPersistenceException e1) {
 				log.debug("Fehler beim abspeichern!");
 				JOptionPane.showMessageDialog(this,"berprfe ob alle Constraints eingehalten wurden!","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 			}
 			clearAktivTable();
 			removeRechnung();
 			updateFrame();
 			break;
 		case "RechnungHinzufgen":
 			if(pferde.getSelectedRow() < 0) {
 				JOptionPane.showMessageDialog(this,"Whle zuerst ein Pferd aus!","Auswahlfehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			int id = (int) pferde.getValueAt(pferde.getSelectedRow(), 0);
 			for (int i = 0; i < atm.getRowCount(); i++) {
 				if( id == (int)atm.getValueAt(i, 0)) {
 					JOptionPane.showMessageDialog(this,"Pferd schon in der Tabelle!","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 			}
 			log.info("Fge Pferd zur Rechnung hinzu mit der ID " + id);
 			//Stundenabfrage
 			int stunden = 0;
 			String eingabe = "";
 			while(stunden<=0) {
 				try {
 				eingabe = JOptionPane.showInputDialog(this, "Wieviele Stunden sollen gebucht werden (>0):", "Stundeneingabe", JOptionPane.YES_NO_CANCEL_OPTION);
 				if(eingabe == null || eingabe.isEmpty())
 					return;
 				stunden = Integer.parseInt(eingabe);
 				}
 				catch (NumberFormatException nfe) {
 					JOptionPane.showMessageDialog(this,"Stundenangabe muss eine realistisch groe Ganzzahl sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 			log.debug("Fr das Pferd " + id + " wurden " + stunden + " Stunden gewhlt");			
 			atm = new AktivTable(aktivRowNames(id, service.getPferd(new Pferd(id)).getName() , service.getPferd(new Pferd(id)).getPreis(), stunden), aktivColumnNames);
 			aktiv.setModel(atm);
 			atm.fireTableDataChanged();			
 			break;
 		case "RechnungAbbrechen":
 			log.info("Breche Anlegen neuer Rechnung ab...");
 			clearAktivTable();
 			removeRechnung();
 			updateFrame();
 			break;
 		case "Beenden":
 			log.debug("Beende Programm...");
 			System.exit(0);
 			break;
 		case "NeuesPferdForm":
 			log.info("Bereite Form fr neues Pferd vor...");
 			clearAktivTable();
 			removeRechnung();
 			removeEditPferd();
 			addPferdForm();
 			picChooser.setSelectedFile(new File(""));
 			updateFrame();
 			break;
 		case "NeuesPferdAbbrechen":
 			log.info("Breche Vorgang zur Erstellung des neuen Pferds ab, lsche Pferdformular...");
 			resetInsertPferdForm();
 			removePferdForm();
 			updateFrame();
 			break;
 		case "NeuesPferdSpeichern":
 			log.info("Speichere neu angelegtes Pferd ab...");
 			
 			therapieart = "";
 			switch (therapieForm.getSelectedIndex()) {
 			case -1:
 				JOptionPane.showMessageDialog(this,"Gebe eine Therapieart an.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			case 0:
 				therapieart = "Hippotherapie";
 				break;
 			case 1:
 				therapieart = "HPV";
 				break;
 			case 2:
 				therapieart = "HPR";
 				break;
 			}
 			if(selectedPic.isEmpty() || selectedPic == null) {
 				JOptionPane.showMessageDialog(this,"Bitte gebe ein Bild an.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			try {
 			p = new Pferd(service.getNewId(), nameForm.getText(), selectedPic, Double.parseDouble(preisForm.getText()),
 					therapieart, rasseForm.getText(), insertKinder.isSelected(), false);
 			service.insertPferd(p);
 			} catch (NumberFormatException e3) {
 				log.debug("Eingabe ist keine Dezimalzahl!");
 				JOptionPane.showMessageDialog(this,"Preisangabe muss eine (Dezimal-)Zahl sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch (PferdValidationException e4) {
 				log.debug("Eingabe darf nicht leer sein!");
 				JOptionPane.showMessageDialog(this,"Name darf nicht leer sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch(PferdPersistenceException e5) {
 				log.debug("Eingabe ist zu lang!");
 				JOptionPane.showMessageDialog(this,"Eingabe ist zu lang!","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 						
 			resetInsertPferdForm();
 			removePferdForm();
 			startSuche();
 			updateFrame();
 			break;
 		case "PferdLschen":
 			log.info("Lsche ausgewhltes Pferd...");
 			if(pferde.getSelectedRow() < 0 || pferde.getSelectedColumn() < 0) {
 				JOptionPane.showMessageDialog(this,"Whle eine Zeile aus.","Auswahlfehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			int dec = JOptionPane.showOptionDialog(parent, "Wollen sie das Pferd wirklich lschen?",
 					"Pferd lschen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
 					null, loeschen, loeschen[0]);
 			log.debug("Entscheidung ist: " + dec);
 			if(dec != 0)
 				return;
 			service.deletePferd(getSelectedPferd());
 			startSuche();
 			updateFrame();
 			break;
 		case "PopularPferde" :
 			List<Pferd> popular = null;
 			try {
 				popular = service.getPopularPferde();
 			} catch (NotEnoughPferdeException e1) {
 				log.error("Es sind weniger als 3 Pferde gespeichert!");
 				JOptionPane.showMessageDialog(this,"Es sind weniger als 3 Pferde gespeichert!", "Fehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String popularPferdeMessage = "Die Beliebtesten Pferde sind: \n";
 			int popularCount = 1;
 			for(Pferd p : popular) {
 				popularPferdeMessage += popularCount + ". " + p.getName() + " (ID: " + p.getId() + " ) \n";
 				popularCount++;
 			}
 			popularPferdeMessage += "Stundenpreis dieser Pferde um 5% erhhen?";
 			
 			dec = JOptionPane.showOptionDialog(parent, popularPferdeMessage, "3 Meistgebuchten Pferde",
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, loeschen, loeschen[0]);
 			if(dec == 0) {
 				service.verteurePferde(popular);
 				startSuche();
 			}
 			log.debug("Pferde erfolgreich um 5% verteuert!");
 			break;
 		case "PferdBearbeiten":
 			log.info("Bearbeite ausgewhltes Pferd...");
 			if(pferde.getSelectedRow() < 0 || pferde.getSelectedColumn() < 0) {
 				JOptionPane.showMessageDialog(this,"Whle eine Zeile aus.","Auswahlfehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			removeRechnung();
 			clearAktivTable();
 			removePferdForm();
 			addEditPferd();
 			updateFrame();
 			Pferd p = service.getPferd(getSelectedPferd());
 			editPferdId = p.getId();
 			editNameForm.setText(p.getName());
 			editPreisForm.setText(Double.toString(p.getPreis()));
 			editRasseForm.setText(p.getRasse());
 			editInsertKinder.setSelected(p.isKinderfreundlich());
 			picChooser.setSelectedFile(new File(p.getFoto()));
 			selectedPic = p.getFoto();
 			editPicLabel.setText(BildFilter.getFileName(p.getFoto()));
 			switch (p.getTherapieart()) {
 			case "Hippotherapie":
 				editTherapieForm.setSelectedIndex(0);
 				break;
 			case "HPV":
 				editTherapieForm.setSelectedIndex(1);
 				break;
 			case  "HPR":
 				editTherapieForm.setSelectedIndex(2);
 				break;
 			}
 			break;
 		case "PicAuswahl":
 			log.info("Whle Bild aus...");
 			int val = picChooser.showOpenDialog(this);
 			if (val == JFileChooser.APPROVE_OPTION) {
 				selectedPic = picChooser.getSelectedFile().getAbsolutePath();
 				picLabel.setText("Profilbild ausgewhlt.");
 			}
 			break;
 		case "PferdBearbeitenAbbrechen":
 			log.info("Breche Vorgang zur Bearbeitung ab...");
 			removeEditPferd();
 			updateFrame();
 			break;
 		case "PferdBearbeitenSpeichern":
 			log.info("Speichere bearbeitetes Pferd ab...");
 			therapieart = "";
 			switch (editTherapieForm.getSelectedIndex()) {
 			case -1:
 				JOptionPane.showMessageDialog(this,"Gebe eine Therapieart an.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			case 0:
 				therapieart = "Hippotherapie";
 				break;
 			case 1:
 				therapieart = "HPV";
 				break;
 			case 2:
 				therapieart = "HPR";
 				break;
 			}
 			if(selectedPic.isEmpty() || selectedPic == null) {
 				JOptionPane.showMessageDialog(this,"Bitte gebe ein Bild an.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			try {
 			p = new Pferd(editPferdId, editNameForm.getText(), selectedPic, Double.parseDouble(editPreisForm.getText()),
 					therapieart, editRasseForm.getText(), editInsertKinder.isSelected(), false);
 			service.updatePferd(p);
 			} catch (NumberFormatException e3) {
 				log.debug("Eingabe ist keine Dezimalzahl!");
 				JOptionPane.showMessageDialog(this,"Preisangabe muss eine (Dezimal-)Zahl sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch (PferdValidationException e4) {
 				log.debug("Eingabe darf nicht leer sein!");
 				JOptionPane.showMessageDialog(this,"Name darf nicht leer sein.","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			} catch(PferdPersistenceException e5) {
 				log.debug("Eingabe ist zu lang!");
 				JOptionPane.showMessageDialog(this,"Eingabe ist zu lang!","Eingabefehler", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 			removeEditPferd();
 			startSuche();
 			updateFrame();
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	/**
 	 * Lscht alle Daten aus der zu erstellenden Rechnung
 	 */
 	private void clearAktivTable() {
 		aktivRows = 0;
 		atm = new AktivTable(temp, aktivColumnNames);
 		aktiv.setModel(atm);
 		atm.fireTableDataChanged();		
 	}
 
 	/**
 	 * Erstellt die Daten fr die anzulegende Rechnung
 	 * @param id ID des gebuchten Pferds
 	 * @param name Name des Pferds
 	 * @param preis Preis des Pferds
 	 * @param stunden die Stunden, die das Pferd gebucht werden soll
 	 * @return ein Array aller Daten der neu erstellten Rechnung
 	 */
 	private Object[][] aktivRowNames(int id, String name, double preis, int stunden) {
 		Object[][] newRows = new Object[++aktivRows][4];
 		for(int i = 0; i < aktivRows-1; i++ ) {
 			newRows[i][0] = oldRows[i][0];
 			newRows[i][1] = oldRows[i][1];
 			newRows[i][2] = oldRows[i][2];
 			newRows[i][3] = oldRows[i][3];
 			
 		}
 		newRows[aktivRows-1][0] = id;
 		newRows[aktivRows-1][1] = name;
 		newRows[aktivRows-1][2] = preis;
 		newRows[aktivRows-1][3] = stunden;
 		oldRows = newRows;
 		return newRows;
 	}
 	//fgt Pferd- und Rechnung-erstellen-mens hinzu
 	public void addMenus() {		
 		menuItem = new JMenuItem("Neues Pferd...");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("NeuesPferdForm");
 		menu.add(menuItem, 0);
 		
 		menuItem = new JMenuItem("Neue Rechnung...");
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("NeueRechnung");
 		menu.add(menuItem, 1);
 		
 	}
 
 	//lscht Pferd- und Rechnung-erstellen-mens
 	public void deleteMenus() {
 		menu.remove(menu.getMenuComponent(0));
 		menu.remove(menu.getMenuComponent(0));
 		
 		
 	}
 
 }
