 package ui;
 
 import general.Debug;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import database.DbStatistics;
 
 public class Historie extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 	private static final int CENTER = 0;											// statische Varbiable fr Textformatierung
 	DbStatistics stats = new DbStatistics();																// Instanz der DB
 	
 	
 	private boolean HideHistorie = true;
 	
 	// Werte fr die Sortierung
 	private int aktivesSortFeld;
 	private boolean FlagSortAsc = false;
 	private final static int SortAsc = 0;
 	private final static int SortDesc = 1;
 	
 	private JLabel lblHead;
 	private JLabel lblTotalGamesWonA;
 	private JLabel lblTotalGamesWonB;
 	private JLabel lblTotalGamesA;
 	private JLabel lblTotalGamesB;
 	private JLabel lblDurchSpieldauerA;
 	private JLabel lblDurchSpieldauerB;
 	private JLabel lblDurchAnzahlSaetzeA;
 	private JLabel lblDurchAnzahlSaetzeB;
 	private JLabel lblAnzahlSaetzeA;
 	private JLabel lblAnzahlSaetzeB;
 	private JLabel lblAnzahlGewonSaetzeA;
 	private JLabel lblAnzahlGewonSaetzeB;
 	private JLabel lblDurchZuegeSatzA;
 	private JLabel lblDurchZuegeSatzB;
 	private JLabel lblbFastGamesA;
 	private JLabel lblbFastGamesB;
 	private JLabel lblSortIndi;
 	
 	
 	private JPanel pnlDashboard;
 	private JPanel pnlHistorie;
 	private JPanel pnlChartSiegeGruppe;
 	private JPanel pnlChartGewonnenvsverloren;
 	private JPanel pnlLog;
 	private JScrollPane scpaHistorie;
 	private JEditorPane epaLog;
 	private JScrollPane scpaLog;
 	
 	private JButton btnHideHistorie;
 	
 	private TitledBorder titleDashboard;
 	private TitledBorder titleHistorie;
 	private TitledBorder titleLog;
 	
 	private JTable tblHistorie;
 	private DefaultTableModel tblModelHistorie;
 	private JTableHeader tblHeadHistorie;
 	private Object[][] tblData;
 	private String[] tblColumnNames;
 	
 	
 	
 	public Historie (final GameMain myGame){
 		
 		// Einen Top-Level-Container erzeugen
 		this.setTitle("Gruppe 5 - VierGewinnt - Statistik");
 		this.setSize(1024, 768);
 		this.setLocationRelativeTo(null);										// Default Location wird mit null auf Center - x&y - gesetzt
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);					// Beim schlieen des Fensters auch das Programm beenden
 		this.setResizable(false);
 		this.setLayout(null);
 		
 		
 		// WindowListener fr den Klick auf Schlieen
 		this.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent e){
 				onbtnCloseClick(myGame);
 			}
 		});
 		
 		
 		
 			// ******************************
 			// **** 1st Level - Header ******
 			// ******************************
 			// Label fr die berschrift
 			this.lblHead = new JLabel("VierGewinnt - Statistik");
 			this.lblHead.setSize(350, 50);
 			this.lblHead.setLocation(337, 0);
 			this.lblHead.setHorizontalAlignment(CENTER);
 			this.lblHead.setFont(new Font("Serif", Font.BOLD, 26));
 			this.add(lblHead);
 		
 		
 	
 			// ***********************************
 			// **** 2nd Level - Main - Top *******
 			// ***********************************
 			// Panel fr das Dashboard
 			this.pnlDashboard = new JPanel();
 			this.pnlDashboard.setSize(980, 280);
 			this.pnlDashboard.setLocation(20, 50);
 			this.titleDashboard = new TitledBorder(BorderFactory.createTitledBorder("Dashboard"));										// Titel zur Border hinzufgen
 			this.pnlDashboard.setBorder(titleDashboard);
 			this.pnlDashboard.setLayout(null);
 			this.add(this.pnlDashboard);
 			
 
 				
 				// Label fr die Anzahl gesamter Spiele
 				this.lblTotalGamesA = new JLabel("Spiele gespielt");
 				this.lblTotalGamesA.setSize (150, 20);
 				this.lblTotalGamesA.setLocation (15, 30);
 				this.pnlDashboard.add(this.lblTotalGamesA);
 				this.lblTotalGamesB = new JLabel("-");
 				this.lblTotalGamesB.setSize (60, 20);
 				this.lblTotalGamesB.setLocation (170, 30);
 				this.pnlDashboard.add(this.lblTotalGamesB);
 				
 				// Label fr die Anzahl gewonnener Spiele
 				this.lblTotalGamesWonA = new JLabel("Spiele gewonnen");
 				this.lblTotalGamesWonA.setSize (150, 20);
 				this.lblTotalGamesWonA.setLocation (15, 60);
 				this.pnlDashboard.add(this.lblTotalGamesWonA);
 				this.lblTotalGamesWonB = new JLabel("-");
 				this.lblTotalGamesWonB.setSize (60, 20);
 				this.lblTotalGamesWonB.setLocation (170, 60);
 				this.pnlDashboard.add(this.lblTotalGamesWonB);
 
 				// Label fr die Anzahl der schnellen Siege
 				this.lblbFastGamesA = new JLabel("K.O.-Siege");
 				this.lblbFastGamesA.setSize (150, 20);
 				this.lblbFastGamesA.setLocation (15, 90);
 				this.pnlDashboard.add(this.lblbFastGamesA);
 				this.lblbFastGamesB = new JLabel("-");
 				this.lblbFastGamesB.setSize (100, 20);
 				this.lblbFastGamesB.setLocation (170, 90);
 				this.pnlDashboard.add(this.lblbFastGamesB);
 			
 				// Label fr durchschnittliche Spieldauer
 				this.lblDurchSpieldauerA = new JLabel("Durch. Spieldauer");
 				this.lblDurchSpieldauerA.setSize(150, 20);
 				this.lblDurchSpieldauerA.setLocation(15, 120);
 				this.pnlDashboard.add(this.lblDurchSpieldauerA);
 				this.lblDurchSpieldauerB = new JLabel("-");
 				this.lblDurchSpieldauerB.setSize(100, 20);
 				this.lblDurchSpieldauerB.setLocation(170, 120);
 				this.pnlDashboard.add(this.lblDurchSpieldauerB);
 				
 				// Label fr durchschnittliche Anzahl gespielter Stze
 				this.lblDurchAnzahlSaetzeA = new JLabel("Durch. gespielte Stze");
 				this.lblDurchAnzahlSaetzeA.setSize(150, 20);
 				this.lblDurchAnzahlSaetzeA.setLocation(15, 150);
 				this.pnlDashboard.add(this.lblDurchAnzahlSaetzeA);
 				this.lblDurchAnzahlSaetzeB = new JLabel("-");
 				this.lblDurchAnzahlSaetzeB.setSize(60, 20);
 				this.lblDurchAnzahlSaetzeB.setLocation(170, 150);
 				this.pnlDashboard.add(this.lblDurchAnzahlSaetzeB);
 				
 				// Label fr die Anzahl gespielter Stze
 				this.lblAnzahlSaetzeA = new JLabel("Gespielte Stze");
 				this.lblAnzahlSaetzeA.setSize(150, 20);
 				this.lblAnzahlSaetzeA.setLocation(15, 180);
 				this.pnlDashboard.add(this.lblAnzahlSaetzeA);
 				this.lblAnzahlSaetzeB = new JLabel("-");
 				this.lblAnzahlSaetzeB.setSize(60, 20);
 				this.lblAnzahlSaetzeB.setLocation(170, 180);
 				this.pnlDashboard.add(this.lblAnzahlSaetzeB);
 				
 				// Label fr die Anzahl Gewonnener Stze
 				this.lblAnzahlGewonSaetzeA = new JLabel("Gewonnene Stze");
 				this.lblAnzahlGewonSaetzeA.setSize(150, 20);
 				this.lblAnzahlGewonSaetzeA.setLocation(15, 210);
 				this.pnlDashboard.add(this.lblAnzahlGewonSaetzeA);
 				this.lblAnzahlGewonSaetzeB = new JLabel("-");
 				this.lblAnzahlGewonSaetzeB.setSize(60, 20);
 				this.lblAnzahlGewonSaetzeB.setLocation(170, 210);
 				this.pnlDashboard.add(this.lblAnzahlGewonSaetzeB);
 				
 				// Label fr durchschnittliche Zge je Satz
 				this.lblDurchZuegeSatzA = new JLabel("Durch. Zge je Satz");
 				this.lblDurchZuegeSatzA.setSize(150, 20);
 				this.lblDurchZuegeSatzA.setLocation(15, 240);
 				this.pnlDashboard.add(this.lblDurchZuegeSatzA);
 				this.lblDurchZuegeSatzB = new JLabel("-");
 				this.lblDurchZuegeSatzB.setSize(60, 20);
 				this.lblDurchZuegeSatzB.setLocation(170, 240);
 				this.pnlDashboard.add(this.lblDurchZuegeSatzB);
 				
 				
 				
 				// ***********************************
 				// **** 2nd Level - Main - Top *******
 				// **** Visaulisierung ***************
 				// ***********************************
 				
 				// Kuchendiagramm mit "Verteilung der gewonnenen Spiele"
 				showKOSiege();
 				this.pnlChartSiegeGruppe.setSize(320, 240);
 				this.pnlChartSiegeGruppe.setLocation(300, 25);
 				this.pnlDashboard.add(this.pnlChartSiegeGruppe);
 				
 				
 				// Kuchendiagramm mit "Gewonnen vs. Verloren"
 				showPieChartGewonnenvsverloren();
 				this.pnlChartGewonnenvsverloren.setSize(320, 240);
 				this.pnlChartGewonnenvsverloren.setLocation(650, 25);
 				this.pnlDashboard.add(this.pnlChartGewonnenvsverloren);
 				
 			
 			
 			
 			// ***********************************
 			// **** 2nd Level - Main - Bottom ****
 			// ***********************************
 			// Panel fr die Historie
 			this.pnlHistorie = new JPanel();
 			this.pnlHistorie.setSize(980, 30);
 			this.pnlHistorie.setLocation(20, 350);
 			this.titleHistorie = new TitledBorder(BorderFactory.createTitledBorder("Historie"));										// Titel zur Border hinzufgen
 			this.pnlHistorie.setBorder(titleHistorie);
 			this.pnlHistorie.setLayout(null);
 			this.add(this.pnlHistorie);
 	
 				// Button zum ausblenden des Panels Historie
 				this.btnHideHistorie = new JButton ("+");
 				this.btnHideHistorie.setSize(45, 25);
 				this.btnHideHistorie.setLocation(935, 0);
 				this.pnlHistorie.add(this.btnHideHistorie);
 				this.btnHideHistorie.addActionListener(new ActionListener() {														// ActionListener regestrieren
 					public void actionPerformed(ActionEvent e) {												
 						onbtnHideHistorieClick();
 					}
 				});
 			
 				
 				
 				// ***********************************
 				// **** 2nd Level - Main - Bottom ****
 				// **** Tabelle **********************
 				// ***********************************
 				// Tabelle zur Darstellung der Informationen aus der Datenbank
 				// Spaltennamen definieren und im Array ablegen
 				this.tblColumnNames = new String []{"Spiel-ID", "ID Gegenspieler", "ID Gewinner", "Spielstand", "Spielbeginn", "Spieldauer in Min.", "Gespielte Stze"};
 				
 				// Laden der aktuellen Daten aus der DB
 				refreshDetailHistorieData();
 				
 				// Tabelle bauen mit Grundlage des TableModels
 				this.tblHistorie = new JTable();
 				
 				// Tabel Model mit den aktuellen Datenstzen wird erzeugt
 				refreshDetailHistorie();
 				
 				// Definition des Headers
 				this.tblHeadHistorie = tblHistorie.getTableHeader();
 				this.tblHeadHistorie.setReorderingAllowed(false);
 				this.tblHeadHistorie.setResizingAllowed(false);
 				
 				// Regestrieren des MouseListeners fr den Spaltenkopf
 				this.tblHeadHistorie.addMouseListener(new HeaderMouseListener());
 				
 				// Optionen fr die gewnschte Selektion von Zeilen
 				this.tblHistorie.setEnabled(true);
 				this.tblHistorie.setColumnSelectionAllowed(false);
 				this.tblHistorie.setRowSelectionAllowed(true);
 				
 
 				
 				// Event bei klicken auf eine Zeile wird die entsprechende Zeile ausgewhlt und zur Verarbeitung weiter verwendet
 				this.tblHistorie.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 					public void valueChanged(ListSelectionEvent e) {
 					// Speicher selektierte Zeile in Variable
 					int input = tblHistorie.getSelectedRow();
 					ontblHistorieRowClick(input);
 					}
 				});
 				
 				// Tabelle wird in einem ScrollPane eingebettet
 				this.scpaHistorie = new JScrollPane(this.tblHistorie);																	// Scroll Pane fr das Log
 				this.scpaHistorie.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);									// Anzeigen der ScrollBar
 				this.scpaHistorie.setSize(960, 165);
 				this.scpaHistorie.setLocation(10, 30);
 				this.pnlHistorie.add(this.scpaHistorie);
 	
 				// Grafik fr die Darstellung der Sortierung
 				this.lblSortIndi = new JLabel(new ImageIcon(getClass().getResource("images/sort_desc.JPG")));
 				this.lblSortIndi.setSize(20, 18);
 				this.lblSortIndi.setLocation(105,3);
 				this.tblHeadHistorie.add(this.lblSortIndi);
 				
 				
 				
 				// ***********************************
 				// **** 2nd Level - Main - Bottom ****
 				// **** Log **************************
 				// ***********************************
 				// Panel fr das Log
 				this.pnlLog = new JPanel();
 				this.pnlLog.setSize(960, 160);
 				this.pnlLog.setLocation(10, 200);
 				this.titleLog = new TitledBorder(BorderFactory.createTitledBorder("Bitte auf Datensatz in der Tabelle klicken zur Darstellung des Logs"));											// Titel zur Border hinzufgen und formatieren
 				this.pnlLog.setBorder(this.titleLog);
 				this.pnlLog.setLayout(null);
 				this.pnlHistorie.add(this.pnlLog);
 				
 				
 					// Editor Pane in Scroll Pane zur Darstellung des Logs
 					this.epaLog = new JEditorPane();
 					this.epaLog.setEditable(false);																					// Editor Pane ist nicht editierbar
 					this.epaLog.setSize(910, 160);
 					this.epaLog.setLocation(5, 5);
 					this.scpaLog = new JScrollPane(this.epaLog);																	// Scroll Pane fr das Log
 					this.scpaLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);									// Anzeigen der ScrollBar
 					this.scpaLog.setSize(940, 130);
 					this.scpaLog.setLocation(10, 20);
 					this.pnlLog.add(this.scpaLog);
 				
 				
 				
 	
 		// Setzen der Dashboard Informationen
 		setDataOnStart();
 
 		// Sicherstellung der Darstellung
 		// MUSS IMMER AM ENDE STEHEN
 		this.invalidate();
 		this.setVisible(true);
 		this.repaint();
 	}
 	
 	
 	/**
 	 * Methode zum erstellen eines Kuchendiagramms zur Darstellung der Verteilung der Siege pro Gruppe
 	 */
 	private void showKOSiege() {
 		
 		String titel = "K.O.-Siege vs normale Siege";
 		int xSiege = 1;
 		int yKOSiege = 1;
 		
 		
 		// Falls DB nicht leer, beflle die Variablen mit Daten aus der DB
 		if (!stats.getDbEmpty()){
 			yKOSiege = this.stats.getCountFastGames();
 			xSiege = this.stats.getCountWinningGames() - yKOSiege;
 		} else {
 			titel = "DB ist leer";
 		}
 
 		// Beschriftung der Daten fr den Konstruktor
 		String[] categories = {"Normale Siege", "K.O.-Siege"};
 		// Daten fr den Konstruktor
 		int[] amount = {xSiege, yKOSiege};
 		
 		// PieChart Konstruktor
 		this.pnlChartSiegeGruppe = new PieChart(categories, amount, titel);
 	}
 	
 	
 	/**
 	 * Methode zum erstellen eines Kuchendiagramms zur Darstellung der Siege und verlorenen Spiele
 	 */
 	private void showPieChartGewonnenvsverloren() {
 		// Werte fr das Kuchendiagramm
 		int xGewonnen = 1;
 		int yVerloren = 1;
 		String titel = "Gewonnen vs. Verloren";
 		
 		// Falls DB nicht leer, berechne Werte fr Kuchendiagramm
 		if (!stats.getDbEmpty()){
 			
 			xGewonnen = stats.getCountWinningGames();
 			yVerloren = stats.getCountGames() - xGewonnen;	
 		}
 		else {
 			titel = "DB ist leer";
 		}
 		
 		// Kategorien fr das Kuchendiagramm
 		String[] categories = {"Gewonnen", "Verloren"};
 		// Daten fr das Kuchendiagramm
 		int[] amount = {xGewonnen, yVerloren};
 		
 		this.pnlChartGewonnenvsverloren = new PieChart(categories, amount, titel);
 	}
 
 	
 	
 	/**
 	 * Handler-Methode fr das Klicken bei Schlieen / "X"
 	 */
 	private void onbtnCloseClick(GameMain myGame){
 		Debug.log(30, "Button gedrckt: Schliessen; X");
 		this.stats.statisticClose();
 		
 		boolean frameVisible = true;
 		myGame.setFrameVisibility(frameVisible);
 		
 		this.dispose();
 	}
 	
 	
 	
 	/**
 	 * Handler-Methode fr das Klicken bei btnHideHistorieClick() / "+"
 	 * Verstecken des Logs bei ButtonClick
 	 */
 	private void onbtnHideHistorieClick() {
 		// Ein und Ausblenden der Panels durch das setzen der Gre
 		if (HideHistorie == true) {
 			this.pnlHistorie.setSize(980, 375);
 			this.btnHideHistorie.setText("^");
 			this.HideHistorie = false;
 		} else if (HideHistorie == false) {
 			this.pnlHistorie.setSize(980, 30);
 			this.btnHideHistorie.setText("+");
 			this.HideHistorie = true;
 		}
 	}
 	
 	
 	/**
 	 * Handler-Methode fr dsa Klicken auf eine Zeile in der Tabelle; ontblHistorieRowClick / "Zeile"
	 * @param i: Selektierte Zeile als int
 	 */
 	private void ontblHistorieRowClick(int i){
 		
 		
 		// Falls Klick-bergabe unter 0 nicht ausfhren; Wichtig, da sonst bei bestimmten Nutzerverhalten OutOfBoundExceptions auftreten
 		if (i>=0){
 		
 		int selecZeile = Integer.parseInt((String)this.tblData[i][0]);
 
 		// Beschriftung der Border an die Selektion anpassen
 		this.titleLog = new TitledBorder(BorderFactory.createTitledBorder("Log der Spiel ID: " + selecZeile));											// Titel zur Border hinzufgen und formatieren
 		this.pnlLog.setBorder(this.titleLog);
 		
 		if (!this.stats.getDbEmpty()){
 		// Setzen der richtigen Auswahl
 		this.stats.setLogGameID(i);
 		
 		String [] logtext = this.stats.getLog();
 		String epaLogString = "";
 		
 		// Loop ber Logeintrge und verketten mit Absatz
 		for (int k = 0; k < logtext.length; k++){
 			epaLogString += logtext[k] + "\n";		
 		}
 		
 		this.epaLog.setText(epaLogString);
 		this.epaLog.setCaretPosition(0);
 		}
 		}
 	}
 	
 	
 
 	
 	
 	/**
 	 * Refresh der Detailinformationen in Tabellenform
 	 */
 	private void refreshDetailHistorieData(){
 		
 		
 		// Falls Daten in der DB, speicher Daten im Array; Sonst speicher einen Default Wert
 		if (!stats.getDbEmpty()){
 		
 			// Daten aus der DB im Array ablegen
 			this.tblData = stats.getSpielStatistik();
 			
 		} else {
 			
 			// Default Werte fr die Tabelle intialisieren
 			this.tblData = new Object [][] {{"0", "0", "0", "0", "0", "0", "0"}};
 			
 			// Tabelle mit Default-Werten bauen
 			this.tblHistorie = new JTable(tblData, this.tblColumnNames);
 		}
 		
 
 		
 	} // refreshDetailHistorieData()
 	
 	
 	
 	/**
 	 * Refresh der Historie
 	 */
 	public void refreshDetailHistorie(){
 		
 		// Tabelle bauen mit Grundlage des TableModels
 		this.tblModelHistorie = new DefaultTableModel(this.tblData, this.tblColumnNames);
 	
 		this.tblModelHistorie.fireTableDataChanged();
 		this.tblModelHistorie.fireTableStructureChanged();
 		
 		this.tblHistorie.setModel(tblModelHistorie);
 		
 	}
 	
 	
 	/**
 	 * Setzen der Informationen im Dashboard beim starten der Historie
 	 */
 	private void setDataOnStart(){
 		
 		// Falls Daten in der DB vorhanden, setze die Labels mit entsprechenden Informationen aus DB
 		if (!stats.getDbEmpty()){
 		// Setzen der Labels mit den notwendigen Daten
 		this.lblTotalGamesB.setText(String.valueOf(stats.getCountGames()));
 		this.lblTotalGamesWonB.setText(String.valueOf(stats.getCountWinningGames()));
 		this.lblbFastGamesB.setText(Integer.toString(stats.getCountFastGames()));
 		
 		// Runden der durchschnittlichen Spieldauer
 		float roundGetAvgPlayingGame = (float) (((int)(stats.getAvgPlayingGame()*100))/100.0);
 		this.lblDurchSpieldauerB.setText(Float.toString(roundGetAvgPlayingGame) + " Min.");
 		this.lblDurchAnzahlSaetzeB.setText(Float.toString(stats.getAvgPlayingSets()));
 		this.lblAnzahlSaetzeB.setText(Integer.toString(stats.getPlayedSets()));
 		this.lblAnzahlGewonSaetzeB.setText(Integer.toString(stats.getWonSets()));
 		this.lblDurchZuegeSatzB.setText(Float.toString(stats.getAvgMoves()));
 
 		}
 	}
 	
 	
 	/**
 	 * Klasse zum behandeln des MouseClick auf den Header einer Tabelle
 	 * Erbt von MouseAdapter und berschreibt MouseListener
 	 */
 	public class HeaderMouseListener extends MouseAdapter{
 
 		/**
 		 * Methode zum abfangen von Klicks auf den Header
 		 */
 		public void mouseClicked(MouseEvent e){
 			JTable table = ((JTableHeader)e.getSource()).getTable();
 			TableColumnModel colModel = table.getColumnModel();
 			
 			// Index der Spalte dessen Header angeklickt wurde
 			int HeaderIndex = colModel.getColumnIndexAtX(e.getX());
 			
 			// Verarbeitung der Information
 			setSortIndi(HeaderIndex);
 			
 		} // mouseClicked
 	} // HeaderMouseListener
 
 
 	/**
 	 * Methode zum setzen des SortierIndikators und anstoen der entsprechenden Sortierung
	 * @param headerIndex: Index der Spalte die angeklickt wurde
 	 */
 	public void setSortIndi(int headerIndex) {
 		// Position des Indikators bestimmen und setzen
 		int i = headerIndex * 134 + 105;
 		this.lblSortIndi.setLocation(i, 3);
 		
 		// Falls Daten in der DB vorhanden, trigger die Sortierung entsprechend der Bedingungen
 		if (!stats.getDbEmpty()){
 			
 			if (aktivesSortFeld == headerIndex && FlagSortAsc){
 				this.lblSortIndi.setIcon(new ImageIcon(getClass().getResource("images/sort_desc.JPG")));
 				this.FlagSortAsc = false;
 				
 				// Sortierung: Absteigend wird angestoen
 				stats.setSort(headerIndex, SortDesc);
 				
 				// Daten werden aktualisiert
 				refreshDetailHistorieData();
 				// Daten aus der DB im Array ablegen
 				this.tblData = stats.getSpielStatistik();
 				// UI Anpassung wird angestoen
 				refreshDetailHistorie();
 
 
 			} else if (aktivesSortFeld == headerIndex && FlagSortAsc != false){
 				this.lblSortIndi.setIcon(new ImageIcon(getClass().getResource("images/sort_asc.JPG")));
 				this.FlagSortAsc = true;
 
 				// Sortierung: Aufsteigend wird angestoen
 				stats.setSort(headerIndex, SortAsc);
 				
 				// Daten werden aktualisiert
 				refreshDetailHistorieData();
 				// Daten aus der DB im Array ablegen
 				this.tblData = stats.getSpielStatistik();
 				// UI Anpassung wird angestoen
 				refreshDetailHistorie();
 
 			
 			} else {
 				this.lblSortIndi.setIcon(new ImageIcon(getClass().getResource("images/sort_asc.JPG")));
 				this.aktivesSortFeld = headerIndex;
 				this.FlagSortAsc = true;
 				
 				// Sortierung: Aufsteigend wird angestoen
 				stats.setSort(headerIndex, SortAsc);
 				
 				
 				// Daten werden aktualisiert
 				refreshDetailHistorieData();
 				// Daten aus der DB im Array ablegen
 				this.tblData = stats.getSpielStatistik();
 				// UI Anpassung wird angestoen
 				refreshDetailHistorie();
 
 				
 			}
 		}
 			
 			
 	}
 }
