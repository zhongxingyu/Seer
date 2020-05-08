 package ui;
 
 import game.FourInARowGame;
 import game.Player;
 import general.Config;
 import general.Debug;
 import general.LogMessage;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.TitledBorder;
 
 import database.DbPlay;
 
 
 
 /**
  * Klasse zur Generierung aller UI Komponenten, um ein Spiel abzubilden
  * @author Anton
  */
 public class GameMain extends JFrame implements Observer {
 
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int CENTER = 0;											// statische Varbiable fr Textformatierung
 	
 	private FourInARowGame game;													// Instanz der Klasse FourInARowGame	
 	private int rows = Config.ROWS;													// Anzahl Zeilen
 	private int columns = Config.COLS;												// Anzahl Spalten
 	private String FileNamePlayer;													// String fr den Dateinamen der anzuzeigenden Grafik
 	private boolean HideStats = true;
 	private boolean HideLog = true;
 	
 	private JLabel lbHead;															// Label fr die berschrift
 	private JLabel lblPath;															// Label fr das Kommunikationsverzeichnis
 	private JLabel lblPlayer;
 	private JLabel lblKommV;
 	private JLabel lblAILevel;
 	private JLabel lblEnemy;
 	private JLabel [][] lblBoard;
 	private JLabel lblSpielstandA;
 	private JLabel lblSpielstandB;
 	private JLabel lblSpielsiegerA;
 	private JLabel lblSpielsiegerB;
 	private JLabel lblSatzstatusClientA;
 	private JLabel lblSatzstatusClientB;
 	private JLabel lblSiegerClientA;
 	private JLabel lblSiegerClientB;
 	private JLabel lblSatzstatusSrvA;
 	private JLabel lblSatzstatusSrvB;
 	private JLabel lblSiegerServerA;
 	private JLabel lblSiegerServerB;
 	private JLabel lblFreigabeA;
 	private JLabel lblFreigabeB;	
 	private JLabel lblSetWinner;
 	private JLabel lblTotalGamesA;
 	private JLabel lblTotalGamesB;
 	private JLabel lblTotalGamesWonA;
 	private JLabel lblTotalGamesWonB;
 
 	private JEditorPane epaLog;
 	private JScrollPane scpaLog;
 	
 	private JPanel pnlOption;														// Panel fr die Optionen
 	private JPanel pnlSpielInfo;
 	private JPanel pnlBoard;
 	private JPanel pnlLog;
 	private JPanel pnlStats;
 	private JPanel pnlSatzInfoClient;
 	private JPanel pnlSatzInfoServer;
 	private JPanel pnlCheat;
 	
 	private JComboBox cbPlayer;
 	private JComboBox cbAILevel;
 	private JComboBox cbSetWinner;
 	private JComboBox cbEnemyID;
 	
 	private JButton btnStart;														// Button zum Satz starten
 	private JButton btnKommV;
 	private JButton btnSetWinner;
 	private JButton btnHideStats;
 	private JButton btnHideLog;
 	private JButton btnNavStatsDetail;
 	private JButton btnSaveStats;
 	
 	private TitledBorder titleOption;												// TitleBorder fr das Panel Option
 	private TitledBorder titleSpielInfo;
 	private TitledBorder titleLog;
 	private TitledBorder titleStats;
 	private TitledBorder titleSatzInfoClient;
 	private TitledBorder titleSatzInfoServer;
 	private TitledBorder titleCheat;
 	
 	
 	public GameMain (){
 	
 			// Einen Top-Level-Container erzeugen
 			this.setTitle("Prototype A - Spielfeld");
 			this.setSize(1024, 768);
 			this.setLocationRelativeTo(null);										// Default Location wird mit null auf Center - x&y - gesetzt
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);					// Beim schlieen des Fensters auch das Programm beenden
 			this.setLayout(null);													// mit NULL besteht die Option der absolution Positionierung
 			
 			
 			
 						// ******************************
 						// **** 1st Level - Header ******
 						// ******************************
 					
 						// Label fr die berschrift
 						this.lbHead = new JLabel("4InALine");
 						this.lbHead.setSize(350, 50);
 						this.lbHead.setLocation(337, 0);
 						this.lbHead.setHorizontalAlignment(CENTER);
 						this.lbHead.setFont(new Font("Serif", Font.BOLD, 26));
 						this.add(lbHead);
 						
 						
 						
 						
 						// ******************************
 						// **** 2nd Level - Center ******
 						// ******************************
 						
 						// Panel fr die Auswahl der Optionen
 						this.pnlOption = new JPanel();
 						this.pnlOption.setSize(320, 410);
 						this.pnlOption.setLocation(20, 70);
 						this.titleOption = new TitledBorder(BorderFactory.createTitledBorder("Spieloptionen"));									// Titel zur Border hinzufgen und formatieren
 						this.pnlOption.setBorder(titleOption);																					// ""
 						this.pnlOption.setLayout(null);
 						this.add(this.pnlOption);
 						
 						
 						
 							// Pfadauswahl mit Label fr das Kommunikationsverzeichnis
 							this.lblKommV = new JLabel("Komm.Verzeichnis");
 							this.lblKommV.setSize(80, 20);
 							this.lblKommV.setLocation(15, 20);
 							this.pnlOption.add(this.lblKommV);
 							this.btnKommV = new JButton ("Auswhlen");
 							this.btnKommV.setSize(200, 20);
 							this.btnKommV.setLocation(100, 20);
 							this.pnlOption.add(btnKommV);
 							this.lblPath = new JLabel(Config.DEFAULT_COMMDIR);
 							this.lblPath.setSize(200, 20);
 							this.lblPath.setLocation(100, 45);
 							this.btnKommV.addActionListener(new ActionListener() {								// ActionListener regestrieren
 								public void actionPerformed(ActionEvent e) {									// Pfad-Dialog initialisieren und entsprechende Auswahl bearbeiten
 									onbtnKommVClick();
 								}
 							});
 							this.pnlOption.add(this.lblPath);
 						
 						
 							// DropDown fr Spielerauswahl
 							this.lblPlayer = new JLabel("Spieler");
 							this.lblPlayer.setSize(80, 20);
 							this.lblPlayer.setLocation(15, 70);
 							this.pnlOption.add(lblPlayer);
 							String[] cbPlayerString = { "X", "O" };
 							this.cbPlayer = new JComboBox(cbPlayerString);
 							this.cbPlayer.setSize(200, 20);
 							this.cbPlayer.setLocation(100, 70);
 							this.pnlOption.add(this.cbPlayer);
 							
 							
 							// DropDown fr Spielstrke
 							this.lblAILevel = new JLabel("KI-Strke");
 							this.lblAILevel.setSize(80, 20);
 							this.lblAILevel.setLocation(15, 95);
 							this.pnlOption.add(this.lblAILevel);
 							String[] cbAILevelString = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
 							this.cbAILevel = new JComboBox(cbAILevelString);
 							this.cbAILevel.setSize(200, 20);
 							this.cbAILevel.setLocation(100, 95);
 							this.pnlOption.add(this.cbAILevel);
 							// TODO MICHI gibt bescheid fr implementierung
 							
 							
							// Textfeld fr die Eingabe der Gegner-ID
 							this.lblEnemy = new JLabel("Gegner-ID");
 							this.lblEnemy.setSize(80, 20);
 							this.lblEnemy.setLocation(15, 120);
 							this.pnlOption.add(this.lblEnemy);
 							String[] cbEnemyIDString = { "1", "2", "3", "4" };
 							this.cbEnemyID = new JComboBox(cbEnemyIDString);
 							this.cbEnemyID.setSize(200, 20);
 							this.cbEnemyID.setLocation(100, 120);
 							this.pnlOption.add(this.cbEnemyID);
 							
 							
 							//Button zum starten des Satzes
 							this.btnStart = new JButton("Satz starten");
 							this.btnStart.setSize(200, 30);
 							this.btnStart.setLocation(100, 150);
 							this.pnlOption.add(this.btnStart);
 							this.btnStart.addActionListener(new ActionListener() {								// ActionListener regestrieren
 								public void actionPerformed(ActionEvent e) {									// Pfad-Dialog initialisieren und entsprechende Auswahl bearbeiten
 									onbtnStartClick();
 								}
 							});
 						
 						// Panel fr das Spielfeld
 						this.pnlBoard = new JPanel();
 						this.pnlBoard.setSize(310, 410);
 						this.pnlBoard.setLocation(357, 70);
 						this.pnlBoard.setLayout(null);
 						TitledBorder titleSpiel = new TitledBorder(BorderFactory.createTitledBorder("Spielfeld"));							// Titel zur Border hinzufgen und formatieren
 						this.pnlBoard.setBorder(titleSpiel);																						// ""
 						this.add(this.pnlBoard);
 
 						
 							// Board initialisieren und mit Default befllen
 							this.lblBoard = new JLabel[columns][rows];
 							for (int i = 0; i < this.rows; i++) {																			// Beginne mit unterster Zeile und gehe hoch
 								for (int j = 0; j < this.columns; j++) {																	// alle Spalten von links nach rechts
 									
 									int x = (j*42) + 7;
 									int y = 360 - (i*42);
 									this.lblBoard[j][i] = new JLabel(new ImageIcon("../4GietSuG5g/src/ui/images/blank.jpg"));				// Steine auf dem Board setzen
 									this.lblBoard[j][i].setSize(42, 42);
 									this.lblBoard[j][i].setLocation(x,y);
 									this.pnlBoard.add(this.lblBoard[j][i]);
 								} //for
 							} //for
 							
 
 							
 						// Panel fr die Spielinformationen
 						this.pnlSpielInfo = new JPanel();
 						this.pnlSpielInfo.setSize(320, 85);
 						this.pnlSpielInfo.setLocation(680, 70);
 						this.titleSpielInfo = new TitledBorder(BorderFactory.createTitledBorder("Spielinformationen"));						// Titel zur Border hinzufgen und formatieren
 						this.pnlSpielInfo.setBorder(titleSpielInfo);
 						this.pnlSpielInfo.setLayout(null);
 						this.add(pnlSpielInfo);
 						
 							// Label fr Spielstand
 							this.lblSpielstandA = new JLabel("Spielstand");
 							this.lblSpielstandA.setSize(80, 20);
 							this.lblSpielstandA.setLocation(15, 30);
 							this.pnlSpielInfo.add(this.lblSpielstandA);
 							this.lblSpielstandB = new JLabel("-");
 							this.lblSpielstandB.setSize(200, 20);
 							this.lblSpielstandB.setLocation(100, 30);
 							this.pnlSpielInfo.add(this.lblSpielstandB);
 							
 							// Label fr Spielsieger
 							this.lblSpielsiegerA = new JLabel("Spielsieger");
 							this.lblSpielsiegerA.setSize(80, 20);
 							this.lblSpielsiegerA.setLocation(15, 55);
 							this.pnlSpielInfo.add(this.lblSpielsiegerA);
 							this.lblSpielsiegerB = new JLabel("-");
 							this.lblSpielsiegerB.setSize(200, 20);
 							this.lblSpielsiegerB.setLocation(100, 55);
 							this.pnlSpielInfo.add(this.lblSpielsiegerB);
 							
 							
 						// Panel fr Satzinformationen client
 						this.pnlSatzInfoClient = new JPanel();
 						this.pnlSatzInfoClient.setSize(320, 85);
 						this.pnlSatzInfoClient.setLocation(680, 170);
 						this.titleSatzInfoClient = new TitledBorder(BorderFactory.createTitledBorder("Satzinformationen Client"));						// Titel zur Border hinzufgen und formatieren
 						this.pnlSatzInfoClient.setBorder(this.titleSatzInfoClient);
 						this.pnlSatzInfoClient.setLayout(null);
 						this.add(pnlSatzInfoClient);
 						
 							// Label fr Satzstatus Client
 							this.lblSatzstatusClientA = new JLabel("Satz Client");
 							this.lblSatzstatusClientA.setSize(80, 20);
 							this.lblSatzstatusClientA.setLocation(15, 30);
 							this.pnlSatzInfoClient.add(this.lblSatzstatusClientA);
 							this.lblSatzstatusClientB = new JLabel("-");
 							this.lblSatzstatusClientB.setSize(200, 20);
 							this.lblSatzstatusClientB.setLocation(100, 30);
 							this.pnlSatzInfoClient.add(this.lblSatzstatusClientB);
 							
 							// Label fr Sieger Client
 							this.lblSiegerClientA = new JLabel("Sieger Client");
 							this.lblSiegerClientA.setSize(80,20);
 							this.lblSiegerClientA.setLocation(15,55);
 							this.pnlSatzInfoClient.add(this.lblSiegerClientA);
 							this.lblSiegerClientB = new JLabel("-");
 							this.lblSiegerClientB.setSize(200,20);
 							this.lblSiegerClientB.setLocation(100, 55);
 							this.pnlSatzInfoClient.add(lblSiegerClientB);
 						
 						// Panel fr Satzinformationen Server
 						this.pnlSatzInfoServer = new JPanel();
 						this.pnlSatzInfoServer.setSize(320, 110);
 						this.pnlSatzInfoServer.setLocation(680, 275);
 						this.titleSatzInfoServer = new TitledBorder(BorderFactory.createTitledBorder("Satzinformationen Server"));						// Titel zur Border hinzufgen und formatieren
 						this.pnlSatzInfoServer.setBorder(this.titleSatzInfoServer);
 						this.pnlSatzInfoServer.setLayout(null);
 						this.add(pnlSatzInfoServer);	
 						
 							// Label fr Satzstatus Server
 							this.lblSatzstatusSrvA = new JLabel("Satz Server");
 							this.lblSatzstatusSrvA.setSize(80, 20);
 							this.lblSatzstatusSrvA.setLocation(15, 30);
 							this.pnlSatzInfoServer.add(this.lblSatzstatusSrvA);
 							this.lblSatzstatusSrvB = new JLabel("-");
 							this.lblSatzstatusSrvB.setSize(200, 20);
 							this.lblSatzstatusSrvB.setLocation(100, 30);
 							this.pnlSatzInfoServer.add(this.lblSatzstatusSrvB);
 							
 							// Label fr Sieger Server
 							this.lblSiegerServerA = new JLabel("Sieger Server");
 							this.lblSiegerServerA.setSize(80, 20);
 							this.lblSiegerServerA.setLocation(15, 55);
 							this.pnlSatzInfoServer.add(this.lblSiegerServerA);
 							this.lblSiegerServerB = new JLabel ("-");
 							this.lblSiegerServerB.setSize(200, 20);
 							this.lblSiegerServerB.setLocation(100, 55);
 							this.pnlSatzInfoServer.add(this.lblSiegerServerB);
 							
 							// Label fr den Freigabestatus des Servers
 							this.lblFreigabeA = new JLabel ("Freigabestatus");
 							this.lblFreigabeA.setSize(80, 20);
 							this.lblFreigabeA.setLocation(15, 80);
 							this.pnlSatzInfoServer.add(this.lblFreigabeA);
 							this.lblFreigabeB = new JLabel ("-");
 							this.lblFreigabeB.setSize(200, 20);
 							this.lblFreigabeB.setLocation(100, 80);
 							this.pnlSatzInfoServer.add(this.lblFreigabeB);
 						
 						// Panel fr Cheat
 						this.pnlCheat = new JPanel();
 						this.pnlCheat.setSize(320, 85);
 						this.pnlCheat.setLocation(680, 395);
 						this.titleCheat = new TitledBorder(BorderFactory.createTitledBorder("Cheat"));						// Titel zur Border hinzufgen und formatieren
 						this.pnlCheat.setBorder(this.titleCheat);
 						this.pnlCheat.setLayout(null);
 						this.add(pnlCheat);
 							
 							// DropDown fr das auswhlen des Gewinners
 							this.lblSetWinner = new JLabel("Gewinner setzen");
 							this.lblSetWinner.setSize(80, 20);
 							this.lblSetWinner.setLocation(15, 30);
 							this.pnlCheat.add(this.lblSetWinner);
 							String[] cbSetWinnerString = {"X", "O"};
 							this.cbSetWinner = new JComboBox(cbSetWinnerString);
 							this.cbSetWinner.setSize(200, 20);
 							this.cbSetWinner.setLocation(100, 30);
 							this.cbSetWinner.setEnabled(false);
 							this.pnlCheat.add(this.cbSetWinner);
 							
 							// Button fr das setzen des Gewinners
 							this.btnSetWinner = new JButton("Besttigen");
 							this.btnSetWinner.setSize(200,20);
 							this.btnSetWinner.setLocation(100, 55);
 							this.btnSetWinner.setEnabled(false);
 							this.pnlCheat.add(this.btnSetWinner);
 							this.btnSetWinner.addActionListener(new ActionListener() {							// ActionListener regestrieren
 								public void actionPerformed(ActionEvent e) {									// Pfad-Dialog initialisieren und entsprechende Auswahl bearbeiten
 									onbtnSetWinnerClick();
 								}
 							});
 						
 							
 							
 							
 						
 						// ******************************
 						// **** 3rd Level - Footer ******
 						// ******************************
 						
 						// Panel fr das Log
 						this.pnlLog = new JPanel();
 						this.pnlLog.setSize(480, 30);
 						this.pnlLog.setLocation(20, 490);
 						this.titleLog = new TitledBorder(BorderFactory.createTitledBorder("Log"));											// Titel zur Border hinzufgen und formatieren
 						this.pnlLog.setBorder(this.titleLog);
 						this.pnlLog.setLayout(null);
 						this.add(this.pnlLog);
 						
 							// Button zum ausblenden des Panels Log
 							this.btnHideLog = new JButton ("+");
 							this.btnHideLog.setSize(45, 25);
 							this.btnHideLog.setLocation(435, 0);
 							this.pnlLog.add(this.btnHideLog);
 							this.btnHideLog.addActionListener(new ActionListener() {														// ActionListener regestrieren
 								public void actionPerformed(ActionEvent e) {																// Pfad-Dialog initialisieren und entsprechende Auswahl bearbeiten
 									onbtnHideLogClick();
 								}
 							});
 						
 							// Editor Pane in Scroll Pane zur Darstellung des Logs
 							this.epaLog = new JEditorPane();
 							this.epaLog.setEditable(false);																					// Editor Pane ist nicht editierbar
 							this.epaLog.setSize(430, 180);
 							this.epaLog.setLocation(5, 5);
 							this.scpaLog = new JScrollPane(this.epaLog);																	// Scroll Pane fr das Log
 							this.scpaLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);									// Anzeigen der ScrollBar
 							this.scpaLog.setSize(460, 150);
 							this.scpaLog.setLocation(10, 30);
 							this.pnlLog.add(this.scpaLog);
 									
 						
 						// Panel fr die Statistik
 						this.pnlStats = new JPanel();
 						this.pnlStats.setSize(480, 30);
 						this.pnlStats.setLocation(520, 490);
 						this.titleStats = new TitledBorder(BorderFactory.createTitledBorder("Statistik"));										// Titel zur Border hinzufgen
 						this.pnlStats.setBorder(this.titleStats);																				// Titel zur Border hinzufgen
 						this.pnlStats.setLayout(null);
 						this.add(this.pnlStats);
 						
 							// Button zum ausblenden des Panels Statistik
 							this.btnHideStats = new JButton ("+");
 							this.btnHideStats.setSize(45, 25);
 							this.btnHideStats.setLocation(435, 0);
 							this.pnlStats.add(this.btnHideStats);
 							this.btnHideStats.addActionListener(new ActionListener() {								// ActionListener regestrieren
 								public void actionPerformed(ActionEvent e) {									// Pfad-Dialog initialisieren und entsprechende Auswahl bearbeiten
 									onbtnHideStatsClick();
 								}
 							});
 							
 							// Label fr die Anzahl gesamter Spiele
 							this.lblTotalGamesWonA = new JLabel("Spiele Gewonnen");
 							this.lblTotalGamesWonA.setSize (160, 20);
 							this.lblTotalGamesWonA.setLocation (15, 30);
 							this.pnlStats.add(this.lblTotalGamesWonA);
 							this.lblTotalGamesWonB = new JLabel("-");
 							this.lblTotalGamesWonB.setSize (200, 20);
 							this.lblTotalGamesWonB.setLocation (180, 30);
 							this.pnlStats.add(this.lblTotalGamesWonB);
 							
 							// Label fr die Anzahl gesamter Spiele
 							this.lblTotalGamesA = new JLabel("Spiele gespielt");
 							this.lblTotalGamesA.setSize (160, 20);
 							this.lblTotalGamesA.setLocation (15, 55);
 							this.pnlStats.add(this.lblTotalGamesA);
 							this.lblTotalGamesB = new JLabel("-");
 							this.lblTotalGamesB.setSize (200, 20);
 							this.lblTotalGamesB.setLocation (180, 55);
 							this.pnlStats.add(this.lblTotalGamesB);
 							
 							// Button zur Vorwrtsnavigation Statistik Detail
 							this.btnNavStatsDetail = new JButton (">> Details");
 							this.btnNavStatsDetail.setSize(160, 30);
 							this.btnNavStatsDetail.setLocation(15, 150);
 							this.pnlStats.add(this.btnNavStatsDetail);
 							this.btnNavStatsDetail.addActionListener(new ActionListener(){
 								public void actionPerformed(ActionEvent e){
 									onbtnNavStatsDetail();
 								}
 							});
 							
 							// Button fr das Speichern des spiels
 							this.btnSaveStats = new JButton (">> Spiel speichern");
 							this.btnSaveStats.setSize(160, 30);
 							this.btnSaveStats.setLocation(180, 150);
 							this.pnlStats.add(this.btnSaveStats);
 							this.btnSaveStats.addActionListener(new ActionListener(){
 								public void actionPerformed(ActionEvent e){
 									onbtnSaveStats();
 								}
 							});
 							
 						
 						
 				
 			// MUSS AM ENDE STEHEN, aber vor der "Sicherstellung der Darstellung
 			this.game = new FourInARowGame(Config.SETCOUNT);			// Spielinstanz initialisieren
 			this.game.addObserver(this);								// Regestriert sich selbst als Observer und lauscht
 					
 			// Sicherstellung der Darstellung
 			// MUSS IMMER AM ENDE STEHEN
 			this.invalidate();
 			this.setVisible(true);
 			this.repaint();
 	}
 
 
 
 
 
 
 	/**
 	 * Refresh Board
 	 */
 	private void refreshBoard(){
 		Player [][] board = this.game.getCurrentSet().getBoard().getBoardAs2DArray();
 
 		for (int i = 0; i < this.rows; i++) {														// beginne mit oberster Zeile	
 			for (int j = 0; j < this.columns; j++) {												// alle Spalten von links nach rechts
 				// Spieler ausgeben, falls gesetzt
 				if (board[j][i] != null){
 					if(board[j][i] == Player.X){
 					this.FileNamePlayer = "../4GietSuG5g/src/ui/images/redXApfel.jpg";
 					} else if (board[j][i] == Player.O){
 					this.FileNamePlayer = "../4GietSuG5g/src/ui/images/yellowYOrange.jpg";	
 					}
 				} else{
 						this.FileNamePlayer = "../4GietSuG5g/src/ui/images/blank.jpg";
 					}
 				this.lblBoard[j][i].setIcon(new ImageIcon(this.FileNamePlayer));
 
 			} //for
 		} //for
 	}
 	
 	
 	
 	
 	/**
 	 * Refresh Status-Label
 	 */
 	private void refreshStatusLabels(){
 		
 		// Setzen der Spielinformationen
 		this.lblSpielstandB.setText(this.game.getScore());
 		// TODO Spielsieger wird noch nicht ermittelt -> reminder
 		
 		
 		// Setzen der Satzinformationen Client
 		if (this.game.getCurrentSet().getWinnerPlayer() != null){
 		this.lblSiegerClientB.setText("Spieler " + this.game.getCurrentSet().getWinnerPlayer());
 		this.lblSatzstatusClientB.setText("Beendet");
 		} else if (this.game.getCurrentSet().getBoard().isGameDraw()){
 			this.lblSiegerClientB.setText("Unentschieden");
 			this.lblSatzstatusClientB.setText("Beendet");
 		}
 		
 		// Setzen der Satzinformationen Server
 		this.lblSatzstatusSrvB.setText(this.game.getCurrentSet().getLastServerState().getSatzstatus());
 		if (this.game.getCurrentSet().getLastServerState().getSieger() != null){
 			this.lblSiegerServerB.setText("Spieler " + this.game.getCurrentSet().getLastServerState().getSieger());
 		}
 		
 		// Setzen der Freigabe
 		if (this.game.getCurrentSet().getLastServerState().getFreigabe()){
 			this.lblFreigabeB.setText("Ja");
 		} else {
 			this.lblFreigabeB.setText("Nein");
 		}		
 	}
 	
 	
 	
 	
 	/**
 	 * Refresh Log
 	 */
 	private void refreshLog(){
 		String epaLogString = new String();
 		
 		ArrayList<LogMessage> logs = Debug.getLogHistory();
 		for (int i=logs.size()-1; i>=0; i--){
 			if (logs.get(i).getLogLevel() <= 20){
 				epaLogString += new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(logs.get(i).getTimeStamp()) + " " + logs.get(i).getMessage() + "\n";
 			}
 		}
 		this.epaLog.setText(epaLogString);	
 	}
 	
 	
 	
 	/**
 	 * Refresh - Reaktivieren der UI Komponenten
 	 */
 	private void refreshReactiveUIComp(){
 		if(this.game.getCurrentSet().isHasEnded()){
 			// Buttons reaktivieren
 			this.cbPlayer.setEnabled(true);
 			this.cbAILevel.setEnabled(true);
 			this.btnStart.setEnabled(true);
 			this.cbSetWinner.setEnabled(true);
 			this.btnSetWinner.setEnabled(true);
 		}
 	}
 
 
 
 	
 	/**
 	 * Pfadauswahl nach ButtonKlick-Event
 	 */
 	private void onbtnKommVClick(){
 		JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int dialog = fc.showOpenDialog(this);
 		if (dialog == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			String directory_path = file.getPath();									// Pfad des angegebenen Ordners wird gespeichert
 			this.lblPath.setText(directory_path);
 			
 		} else {
 			this.lblPath.setText("Pfad auswhlen!!");
 		}
 		//TODO Einbauen von Check ob Verzeichnis schreibgeschtzt-> siehe auch SatzStarten()
 	}
 	
 	
 	/**
 	 * Setze den Sieger manuell unabhngig von dem Ergebnis des Clients und Servers
 	 */
 	private void onbtnSetWinnerClick(){
 		String selectedItem;
 		selectedItem = (String)cbSetWinner.getSelectedItem();						// Selektion aus der DropDown auslesen
 
 		if (selectedItem.equalsIgnoreCase("X")) {
 			this.game.getCurrentSet().setWinnerPlayer(Player.X);
 		} else {
 			this.game.getCurrentSet().setWinnerPlayer(Player.O);
 		}
 	}
 	
 	
 	
 	/**
 	 * Verstecken der Statistik bei ButtonClick
 	 */
 	private void onbtnHideStatsClick() {
 		// Ein und Ausblenden der Panels durch das setzen der Gre
 		if (HideStats == true) {
 			this.pnlStats.setSize(480, 190);
 			this.btnHideStats.setText("^");
 			this.HideStats = false;
 		} else if (HideStats == false) {
 			this.pnlStats.setSize(480, 30);
 			this.btnHideStats.setText("+");
 			this.HideStats = true;
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * Verstecken des Logs bei ButtonClick
 	 */
 	private void onbtnHideLogClick() {
 		// Ein und Ausblenden der Panels durch das setzen der Gre
 		if (HideLog == true) {
 			this.pnlLog.setSize(480, 190);
 			this.btnHideLog.setText("^");
 			this.HideLog = false;
 		} else if (HideLog == false) {
 			this.pnlLog.setSize(480, 30);
 			this.btnHideLog.setText("+");
 			this.HideLog = true;
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Vorwrtsnavigation zur detaillierten Statistik
 	 */
 	private void onbtnNavStatsDetail(){
 		A_UIHistorie gui = new A_UIHistorie();
 		String[] args = new String[1];
 		gui.main(args);
 	}
 	
 	/**
 	 * Speichern des Spiels
 	 */
 	private void onbtnSaveStats(){
 		try {
 			DbPlay db = new DbPlay(this.game);			
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * Satz starten und Werte setzen:
 	 * 		Kommunikationsverzeichnis
 	 * 		Spieler
 	 */
 	private void onbtnStartClick() {
 		
 		// Setzen der Statusinformationen auf Default
 		this.lblSatzstatusClientB.setText("Satz spielen");
 		this.lblSiegerClientB.setText("-");
 		this.lblSatzstatusSrvB.setText("-");
 		this.lblSiegerServerB.setText("-");
 		this.lblFreigabeB.setText("-");
 		
 		// TODO Spielfeld reseten
 
 		
 		// testFile zur berfprfung, ob der Pfad beschrieben werden darf
 		File testFile = new File(this.lblPath.getText());
		String selectedItem;
 		
 		// Falls wahr, setze Optionen und starte Satz
 		if (testFile.canWrite()) {												//ueberpruefe, ob in Kommunikationspfad geschrieben werden kann
 			this.game.setCommDir(this.lblPath.getText());						// Lese Wert aus lblPath und setze damit KommVerzeichnis
			selectedItem = (String)cbPlayer.getSelectedItem();					// Lese Wert aus DropDown cbPlayer und caste in String
 			
 			//Setze eigenen Spieler und starte Satz
			if (selectedItem.equalsIgnoreCase("X")) {
 				this.game.startNewSet(Player.X);
 			} else {
 				this.game.startNewSet(Player.O);
 			}
 			
 			// Buttons deaktivieren
 			this.btnKommV.setEnabled(false);
 			this.lblKommV.setEnabled(false);
 			this.cbPlayer.setEnabled(false);
 			this.cbAILevel.setEnabled(false);
 			this.cbEnemyID.setEnabled(false);
 			this.btnStart.setEnabled(false);
 			this.cbSetWinner.setEnabled(false);
 			this.btnSetWinner.setEnabled(false);
 		} else {
 			JOptionPane.showInternalMessageDialog(this.pnlOption.getParent(), "Gewaehltes Verzeichnis kann nicht genutzt werden.", "Kommunikationsverzeichnis", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * Aufruf der Methode, sobald Logik eine Aenderung meldet
 	 * Muss am Ende stehen
 	 */
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		
 		if (arg0 != null && arg0 instanceof FourInARowGame) {
 			Debug.log(30, "GUI von Update informiert.");
 			refreshBoard();
 			refreshStatusLabels();
 			refreshLog();
 			refreshReactiveUIComp();
 			
 		}
 		
 	}	
 }
