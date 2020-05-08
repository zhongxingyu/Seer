 package com.stalkindustries.main.game;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 
 import com.stalkindustries.main.Button;
 import com.stalkindustries.main.TheStreet;
 
 /**
  * 
  * @author Alle haben mitgewirkt
  *
  */
 public class GUILayer extends JFrame implements MouseMotionListener {
 
 	private HashMap<String, Button> buttons = new HashMap<String, Button>();
 	private static final long serialVersionUID = 1L;
 	private Timer timer;
 	private Map karte;
 	private JLayeredPane baseLayer;
 	private JLayeredPane liveTickerLayer;
 	private JLayeredPane fensterSpionage;
 	private JLayeredPane fensterBeschwichtigen;
 	private JLayeredPane fensterQuiz;
 	private JLayeredPane fensterHaus;
 	private JLayeredPane fensterDialog;
 	private Simulation simulation;
 	private Control control;
 	private Haus haus;
 	private ArrayList<Mensch> humans = new ArrayList<Mensch>();
 	private int stepcounter = 0;
 	private Quiz quiz;
 	private Highscore highscore;
 	
 	private double tickerStep = 0;
 	private int tickerTextSize = 0;
 	private static final int tickerHaufigkeit = 400;
 
 	private JLabel anzeigeZeit = new JLabel();
 	private JLabel anzeigeTag = new JLabel();
 	private JLabel overlayMenubar = new JLabel();
 	private JLabel anzeigeStatusMisstrauen = new JLabel();
 	private JLabel anzeigeStatusUeberwachung = new JLabel();
 	private JLabel overlayMousefollower = new JLabel();
 	private JLabel overlayNacht = new JLabel();
 	private JLabel spionageBeschr = new JLabel();
 	private JLabel beschwichtigenBeschr = new JLabel();
 	private JLabel[] hausinformationen = new JLabel[10]; //Titelfeld, 4Personenbilder, 4 Namen, Leiste berwachungsstatus
 	private JLabel[] informationsbalken = new JLabel[3]; //Misstrauen positiv, Misstrauen Negativ, berwachung
 	private JLabel dialogberschrift = new JLabel(); //berschrift, Beschreibungstext
 	private JTextArea dialogText = new JTextArea();
 	private JLabel newsticker = new JLabel();
 	private JLabel fortschrittsstatus = new JLabel(); //Statusanzeige der Agentenaktion;
 	
 	private BufferedImage fortschrittskreis[]=new BufferedImage[7];
 	
 	
 	/**
 	 * Konstruktor - steuert die Initialisierung aller GUI-Elemente
 	 * @param agentname 
 	 */
 	public GUILayer(String levelname, String agentname) {
 		
 		Mensch.loadImages(levelname);
 		Ressources.loadLevelInfomration(levelname);
 		
 		simulation = new Simulation();
 		for(int i=0;i<7;i++){
 			fortschrittskreis[i]=Ressources.ingamebutton.getSubimage(948, 222+i*Ressources.RASTERHEIGHT+44, Ressources.RASTERHEIGHT, Ressources.RASTERHEIGHT);
 		}
 		this.quiz = new Quiz(this);
 		this.control = new Control(this,this.quiz);
 		this.initComponents(levelname, agentname);
 		this.simulation.initialisiereBeziehungsmatrix();
 		this.setVisible(true);
 		this.timer = new Timer(Ressources.GAMESPEED, new OSTimer(this));
 		this.timer.setCoalesce(false);
 		this.timer.start(); 
 		this.highscore = new Highscore(this.simulation,this.quiz,this.simulation.getAgent(),levelname);
 	}
 
 
 
 	/**
 	 * Beenden: das Spiel stoppen und Fenster schlieen
 	 */
 	public void endGame() {
 		TheStreet.loadMenu(simulation.getAgent().getName());
 		this.timer.stop();
 		this.dispose();
 		//TODO auf Auswertungsmen weiterleiten
 	}
 
 
 
 	/**
 	 * Ruft alle Submethoden zur Initialisierung des Spieles auf
 	 * @param agentname 
 	 */
 	private void initComponents(String levelname, String agentname) {
 		
 		//Einstellungen zum Basisfenster
 		this.baseLayer = new JLayeredPane();
 		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 		this.setResizable(false);
 		this.setUndecorated(true);
 	
 		//LiveTickerLayer initialisieren
 		this.liveTickerLayer = new JLayeredPane();
 		this.liveTickerLayer.setBounds(Ressources.ZEROPOS.width+15,Ressources.ZEROPOS.height,Ressources.MAPWIDTH-120,Ressources.RASTERHEIGHT+2);
 		this.baseLayer.add(liveTickerLayer,javax.swing.JLayeredPane.DEFAULT_LAYER);
 				
 
 		//MouseListener starten
 		this.initMouseListener();
 
 		//Ingame Fenster initialisieren
 		this.initIngameFenster();
 
 		//Buttons auf Husern erzeugen
 		this.initButtonsHaeuser();
 
 		//Buttons im Spielfenster erzeugen
 		this.initButtonsGame();
 		
 		//Spiel-Statusanzeigen und Aktionsleisten-Grafik erzeugen
 		this.initSpielanzeigen();
 
 		//Menschen erzeugen und in Huser beamen
 		this.initHumans(agentname);
 
 		//Levelkarte laden
 		this.initMap(levelname);
 
 		//Letzte Einstellungen zum Fenster
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this.getContentPane());
 		this.getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(
 				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
 				this.baseLayer, javax.swing.GroupLayout.DEFAULT_SIZE,
 				Ressources.SCREEN.width, Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(
 				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
 				this.baseLayer, javax.swing.GroupLayout.DEFAULT_SIZE,
 				Ressources.SCREEN.height, Short.MAX_VALUE));
 
 		this.setBackground(Color.black);
 		this.pack();
 	}
 
 	
 	
 	/**
 	 * Erzeugen der Spielanzeigen und Aktionsleisten-Grafik
 	 * @author Tobi
 	 */
 	private void initSpielanzeigen() {
 		// Tag malen
 		String s = "Tag " + this.simulation.getSpielTag();
 		this.anzeigeTag.setText(s);
 		this.anzeigeTag.setBounds(1004 + Ressources.ZEROPOS.width, 636 + Ressources.ZEROPOS.height, 100, 37);
 		this.anzeigeTag.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 		this.anzeigeTag.setFont(new Font("Corbel", Font.BOLD, 30));
 		this.anzeigeTag.setForeground(new java.awt.Color(249, 249, 249));
 		this.anzeigeTag.setVisible(true);
 		this.baseLayer.add(this.anzeigeTag, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// Uhr malen
 		s = this.simulation.getSpielzeitAsString();
 		this.anzeigeZeit.setText(s);
 		this.anzeigeZeit.setBounds(1010 + Ressources.ZEROPOS.width, 669 + Ressources.ZEROPOS.height, 100, 37);
 		this.anzeigeZeit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		this.anzeigeZeit.setFont(new Font("Corbel", Font.BOLD, 40));
 		this.anzeigeZeit.setForeground(new java.awt.Color(249, 249, 249));
 		this.anzeigeZeit.setVisible(true);
 		this.baseLayer.add(this.anzeigeZeit, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// Misstrauensanzeige in der Strae
 		s = "0.0%";
 		this.anzeigeStatusMisstrauen.setText(s);
 		this.anzeigeStatusMisstrauen.setBounds(713 + Ressources.ZEROPOS.width, 638 + Ressources.ZEROPOS.height, 183, 37);
 		this.anzeigeStatusMisstrauen.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 		this.anzeigeStatusMisstrauen.setFont(new Font("Corbel", Font.BOLD, 16));
 		this.anzeigeStatusMisstrauen.setForeground(new java.awt.Color(249, 249, 249));
 		this.anzeigeStatusMisstrauen.setVisible(true);
 		this.baseLayer.add(this.anzeigeStatusMisstrauen, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// berwachungsanzeige in der Strae
 		this.anzeigeStatusUeberwachung.setText(s);
 		this.anzeigeStatusUeberwachung.setBounds(713 + Ressources.ZEROPOS.width, 677 + Ressources.ZEROPOS.height, 183, 37);
 		this.anzeigeStatusUeberwachung.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 		this.anzeigeStatusUeberwachung.setFont(new Font("Corbel", Font.BOLD, 16));
 		this.anzeigeStatusUeberwachung.setForeground(new java.awt.Color(249, 249, 249));
 		this.anzeigeStatusUeberwachung.setVisible(true);
 		this.baseLayer.add(this.anzeigeStatusUeberwachung, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//berwachung Balken
 
 		informationsbalken[2]=new JLabel();
 		informationsbalken[2].setIcon(new ImageIcon(Ressources.ingamebutton.getSubimage(948, 158, 179, 20)));
 		informationsbalken[2].setBounds(Ressources.ZEROPOS.width+733, Ressources.ZEROPOS.height+685, 166, 19);
 		baseLayer.add(informationsbalken[2], javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		informationsbalken[1]=new JLabel();
 		informationsbalken[1].setIcon(new ImageIcon(Ressources.ingamebutton.getSubimage(948, 118, 179, 20)));
 		informationsbalken[1].setBounds(Ressources.ZEROPOS.width+733, Ressources.ZEROPOS.height+646, 166, 19);
 		baseLayer.add(informationsbalken[1], javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		informationsbalken[0]=new JLabel();
 		informationsbalken[0].setIcon(new ImageIcon(Ressources.ingamebutton.getSubimage(948, 138, 179, 20)));
 		informationsbalken[0].setBounds(Ressources.ZEROPOS.width+733, Ressources.ZEROPOS.height+646, 166, 19);
 		baseLayer.add(informationsbalken[0], javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		
 		//Newsticker
 //		this.newsticker.setBounds(Ressources.ZEROPOS.width +15, Ressources.ZEROPOS.height, Ressources.MAPWIDTH-120, Ressources.RASTERHEIGHT+2);
 //		this.newsticker.setFont(new Font("Corbel", Font.BOLD, 16));
 //		this.newsticker.setForeground(new java.awt.Color(249, 249, 249));
 //		this.newsticker.setVisible(true);
 //		this.baseLayer.add(this.newsticker, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		this.newsticker.setBounds(0, 0, 2000, 2000);
 		this.newsticker.setFont(new Font("Corbel", Font.BOLD, 16));
 		this.newsticker.setForeground(new java.awt.Color(249, 249, 249));
 		this.newsticker.setVisible(true);
 		this.liveTickerLayer.add(this.newsticker, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		
 		// Ingame Menbars
 		this.overlayMenubar.setIcon(new ImageIcon(Ressources.menubars));
 		this.overlayMenubar.setBounds(Ressources.ZEROPOS.width, Ressources.ZEROPOS.height, Ressources.MAPWIDTH, Ressources.MAPHEIGHT);
 		this.baseLayer.add(this.overlayMenubar, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 
 		// Nacht-Modus Overlay
 		this.overlayNacht.setBounds(Ressources.ZEROPOS.width, Ressources.ZEROPOS.height, Ressources.MAPWIDTH, Ressources.MAPHEIGHT);
 		this.overlayNacht.setBackground(new Color(0, 0, 1f, 0.2f));
 		this.overlayNacht.setOpaque(true);
 		this.overlayNacht.setVisible(false);
 		this.baseLayer.add(this.overlayNacht, javax.swing.JLayeredPane.DEFAULT_LAYER);
 	}
 	
 	
 	
 	/**
 	 * Levelkarte laden und einfgen
 	 */
 	private void initMap(String levelname) {
 		// Karte laden
 		this.karte = new Map(levelname, this.humans.get(this.humans.size() - 1).getHausId());
 		// Agent steht an letzter Stelle
 		this.karte.setBounds(Ressources.ZEROPOS.width, Ressources.ZEROPOS.height, 1125, 720);
 		this.baseLayer.add(this.karte, javax.swing.JLayeredPane.DEFAULT_LAYER);
 	}
 
 	
 	
 	/**
 	 * Initialisierung des MouseListeners und Mousefollower-Icons
 	 * @author Tiki
 	 */
 	private void initMouseListener() {
 		// MouseMotionListener fr die kleinen Icons die die Maus verfolgen
 		this.addMouseMotionListener(this);
 
 		// MousefollowerIcon wenn man eine Aktion ttigt
 		this.overlayMousefollower.setBounds(15, 225, 39, 39);
 		this.overlayMousefollower.setVisible(false);
 		this.overlayMousefollower.setIcon(new ImageIcon(Ressources.ingamebutton.getSubimage(0, 0, 39, 39)));
 		this.baseLayer.add(this.overlayMousefollower, javax.swing.JLayeredPane.DEFAULT_LAYER);
 	}
 	
 
 	
 	/**
 	 * Erzeugt die Ingamefenster mit Inhalten
 	 * @author Stephan
 	 */
 	private void initIngameFenster() {
 
 		//Dummys fr Erzeugung
 		JLabel label; 
 		Button button;
 		
 		
 		//TODO Belehrung entfernen
 		//So Leute... bitte alle Komponenten zu einem bestimmten Fenster
 		//Direkt hintereinander in einem Codeabschnitt angeben!
 		//Man findet sonst nix wieder ... :)
 		
 		
 		//---
 		//Quiz
 		//---
 		
 		//Fenster: Quizfragen
 		this.fensterQuiz = new JLayeredPane();
 		this.fensterQuiz.setBounds(Ressources.ZEROPOS.width+(Ressources.MAPWIDTH-598)/2, Ressources.ZEROPOS.height+(Ressources.MAPHEIGHT-327)/2,598, 327);
 		this.baseLayer.add(this.fensterQuiz, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		//Titel Quizfrage
 		label = new JLabel();
 		label.setText("Quizfrage");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 25));
 		label.setForeground(new java.awt.Color(0x1f, 0x1f, 0x1f));
 		label.setBounds(12, 12, 200, 30);
 		this.fensterQuiz.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Komponenten in Control Laden
 		this.quiz.initQuizWindow(this.control);
 		
 		//Hintergund Quiz Fenster
 		label = new JLabel();
 		label.setIcon(new ImageIcon(Ressources.ingameFrame.getSubimage(765, 0, 598, 327)));
 		label.setBounds(0, 0, 598, 327);
 		this.fensterQuiz.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Quizfenster zu Anfang ausblenden
 		this.fensterQuiz.setVisible(false);
 		this.fensterQuiz.setEnabled(false);
 		
 		
 		
 		//---
 		//Haus
 		//---
 				
 		//Fenster: Haus
 		this.fensterHaus = new JLayeredPane();
 		this.fensterHaus.setBounds(Ressources.ZEROPOS.width+(Ressources.MAPWIDTH-460)/2, Ressources.ZEROPOS.height+(Ressources.MAPHEIGHT-327)/2,460, 327);
 		this.baseLayer.add(this.fensterHaus, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		//Schlieen-Button Hausinfo
 		button = new Button(this.control,
 				Ressources.ingamebutton.getSubimage(948, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(975, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1002, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1029, 90, 27, 27),
 				"closeHaus", 415, 15, this);
 		this.buttons.put("closeHaus", button);
 		this.fensterHaus.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		//Titel Hausinfo
 		hausinformationen[0] = new JLabel();
 		hausinformationen[0].setText("Haus1");
 		hausinformationen[0].setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		hausinformationen[0].setFont(new Font("Corbel", Font.BOLD, 25));
 		hausinformationen[0].setForeground(new java.awt.Color(0x1f, 0x1f, 0x1f));
 		hausinformationen[0].setBounds(12, 12, 200, 30);
 		this.fensterHaus.add(hausinformationen[0], javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//berwachungsbalken
 		hausinformationen[9]=new JLabel();
 		hausinformationen[9].setIcon(new ImageIcon(Ressources.ingamebutton.getSubimage(948, 158, 179, 20)));
 		hausinformationen[9].setBounds(237, 133, 166, 19);
 		fensterHaus.add(hausinformationen[9], javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Werkzeug Button
 		int buttonSize = 39;
 		int buttonSliceX = 0;
 		int buttonSliceY = buttonSize;
 		String[] buttonNamesSmall = { "werkzeugWanze", "werkzeugKamera","werkzeugHacken","werkzeugFernglas" };
 		for (int i = 0; i < buttonNamesSmall.length; i++) {
 			button = new Button(control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					buttonNamesSmall[i], 230+(i)*45, 210 , this );
 			this.fensterHaus.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			buttons.put(buttonNamesSmall[i], button);
 			
 		}
 		//Inhalte der Hausinfo
 		label = new JLabel();
 		label.setText("Bewohner");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 20));
 		label.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		label.setBounds(30, 60, 150, 30);
 		this.fensterHaus.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		label = new JLabel();
 		label.setText("berwachung");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 20));
 		label.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		label.setBounds(230, 60, 200, 30);
 		this.fensterHaus.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		label = new JLabel();
 		label.setText("Status");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 18));
 		label.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		label.setBounds(230, 100, 200, 30);
 		this.fensterHaus.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		label = new JLabel();
 		label.setText("Installierte Werkzeuge");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 18));
 		label.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		label.setBounds(230, 170, 200, 30);
 		this.fensterHaus.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Personen im Hausfenster
 		for(int i=0;i<4;i++){
 			hausinformationen[i+1] = new JLabel();
 			hausinformationen[i+1].setText(" ");
 			hausinformationen[i+1].setBounds(30, 93+45*i, 45, 45);
 			this.fensterHaus.add(hausinformationen[i+1], javax.swing.JLayeredPane.DEFAULT_LAYER);
 			hausinformationen[i+5]= new JLabel();
 			hausinformationen[i+5].setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 			hausinformationen[i+5].setFont(new Font("Corbel", Font.BOLD, 18));
 			hausinformationen[i+5].setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 			hausinformationen[i+5].setBounds(85, 93+45*i, 200, 45);
 			this.fensterHaus.add(hausinformationen[i+5], javax.swing.JLayeredPane.DEFAULT_LAYER);
 		}
 
 		//Hintergrundbild HausFenster
 		label = new JLabel();
 		label.setIcon(new ImageIcon(Ressources.ingameFrame.getSubimage(270, 0, 460, 327)));
 		label.setBounds(0, 0, 460, 327);
 		this.fensterHaus.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Hausinfo initial ausblenden
 		this.fensterHaus.setVisible(false);
 		this.fensterHaus.setEnabled(false);
 		
 		
 				
 		//---
 		// Aktionsfenster Beschwichtigen
 		//---
 		
 		// Fenster: Beschwichtigen Aktionsfenster
 		this.fensterBeschwichtigen = new JLayeredPane();
 		this.fensterBeschwichtigen.setBounds(Ressources.ZEROPOS.width + 10, Ressources.ZEROPOS.height + 390, 248, 232);
 		this.baseLayer.add(this.fensterBeschwichtigen, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// Schlieen-Button Beschwichtigen
 		button = new Button(this.control,
 				Ressources.ingamebutton.getSubimage(948, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(975, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1002, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1029, 90, 27, 27),
 				"closeBeschwichtigen", 205, 13, this);
 		this.buttons.put("closeBeschwichtigen", button);
 		this.fensterBeschwichtigen.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// Inhalt des Beschwichtigen Fensters
 		label = new JLabel();
 		label.setText("Sozialleben");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 25));
 		label.setForeground(new java.awt.Color(0x1f, 0x1f, 0x1f));
 		label.setBounds(12, 12, 200, 30);
 		this.fensterBeschwichtigen.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		beschwichtigenBeschr.setText("");
 		beschwichtigenBeschr.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		beschwichtigenBeschr.setFont(new Font("Corbel", Font.BOLD, 18));
 		beschwichtigenBeschr.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		beschwichtigenBeschr.setBounds(20, 50, 200, 30);
 		this.fensterBeschwichtigen.add(beschwichtigenBeschr, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Buttons in Aktionsfenster Beschwichtigen
 		String[] buttonNamesBeschwichtigen = { "aktionKuchen",
 				"aktionUnterhalten", "aktionFlirten", "aktionHand",
 				"parkBeschwichtigen"};
 		this.initButtonsAktionsfenster(fensterBeschwichtigen, 156, buttonNamesBeschwichtigen);
 
 		//Hintergrundbild Beschwichtigen-Aktionsfenster
 		label = new JLabel();
 		label.setIcon(new ImageIcon(Ressources.ingameFrame.getSubimage(0, 0, 248, 235)));
 		label.setBounds(0, 0, 248, 232);
 		this.fensterBeschwichtigen.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Fenster ausblenden
 		this.fensterBeschwichtigen.setEnabled(false);
 		this.fensterBeschwichtigen.setVisible(false);
 		
 		
 		
 		//---
 		//Aktionsfenster Spionage
 		//---
         
 		// Fenster: Spionage Aktionsfenster
 		this.fensterSpionage = new JLayeredPane();
 		this.fensterSpionage.setBounds(Ressources.ZEROPOS.width + 90, Ressources.ZEROPOS.height + 390, 248, 235);
 		this.baseLayer.add(this.fensterSpionage, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		//Schlieen-Button Spionage
 		button = new Button(this.control,
 				Ressources.ingamebutton.getSubimage(948, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(975, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1002, 90, 27, 27),
 				Ressources.ingamebutton.getSubimage(1029, 90, 27, 27),
 				"closeSpionage", 205, 13, this);
 		this.buttons.put("closeSpionage", button);
 		this.fensterSpionage.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		// Inhalt des Spionage Fensters
 		label = new JLabel();
 		label.setText("Spionage");
 		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		label.setFont(new Font("Corbel", Font.BOLD, 25));
 		label.setForeground(new java.awt.Color(0x1f, 0x1f, 0x1f));
 		label.setBounds(12, 12, 200, 30); 
 		this.fensterSpionage.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		spionageBeschr.setText("");
 		spionageBeschr.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		spionageBeschr.setFont(new Font("Corbel", Font.BOLD, 18));
 		spionageBeschr.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
 		spionageBeschr.setBounds(20, 50, 200, 30);
 		this.fensterSpionage.add(spionageBeschr, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		this.fensterSpionage.setEnabled(false);
 		this.fensterSpionage.setVisible(false);
 		
 		//Aktionsbuttons bei Spionage
 		String[] buttonNamesSpionage = { "aktionWanze", "aktionKamera",
 				"aktionHacken", "aktionFernglas"};
 		this.initButtonsAktionsfenster(fensterSpionage, 420, buttonNamesSpionage);
 		
 		//Hintergrundbild Spionage-Aktionsfenster
 		label = new JLabel();
 		label.setIcon(new ImageIcon(Ressources.ingameFrame.getSubimage(0, 0, 248, 235)));
 		label.setBounds(0, 0, 248, 232);
 		this.fensterSpionage.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 
 		
 		//---
 		//generisches Dialogfenster
 		//---
 		
 		//generisches Dialogfenster
 		this.fensterDialog = new JLayeredPane();
 		this.fensterDialog.setBounds(Ressources.ZEROPOS.width + (Ressources.MAPWIDTH - 248) / 2,
 									 Ressources.ZEROPOS.height + (Ressources.MAPHEIGHT - 235) / 2,
 									 248, 235);
 		this.baseLayer.add(this.fensterDialog, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		// berschrift des Dialogfensters
 		dialogberschrift.setText("berschrift");
 		dialogberschrift.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		dialogberschrift.setFont(new Font("Corbel", Font.BOLD, 25));
 		dialogberschrift.setForeground(new java.awt.Color(0x1f, 0x1f, 0x1f));
 		dialogberschrift.setBounds(12, 12, 200, 30); 
 		this.fensterDialog.add(dialogberschrift, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
 		
         dialogText.setLineWrap(true);
         dialogText.setText("Text des Dialogfeldes als Test der Lnge und so.");
         dialogText.setWrapStyleWord(true);
         dialogText.setFocusCycleRoot(true);
         dialogText.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         dialogText.setFocusable(false);
         dialogText.setOpaque(false);
         dialogText.setFont(new Font("Corbel",Font.BOLD,18));
         dialogText.setForeground(new java.awt.Color(0xf9, 0xf9, 0xf9));
         dialogText.setBounds(12 ,60, 204, 75);
       	this.fensterDialog.add(dialogText, javax.swing.JLayeredPane.DEFAULT_LAYER);
       	
 
 		// Buttons Dialogfenster
 		buttonSize = 66;
 		buttonSliceX = 684;
 		buttonSliceY = buttonSize;
 
 		String[] buttonNamesDialog = { "dialogAccept", "dialogDecline" };
 		for (int i = 0; i < buttonNamesDialog.length; i++) {
 			button = new Button(this.control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, (i+4) * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, (i+4) * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, (i+4) * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, (i+4) * buttonSliceY, buttonSize, buttonSize),
 					buttonNamesDialog[i], 40 + i * 100, 132, this);
 			this.fensterDialog.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			this.buttons.put(buttonNamesDialog[i], button);
 		}
 
 		//Hintergrundbild Dialogfenster
 		label = new JLabel();
 		label.setIcon(new ImageIcon(Ressources.ingameFrame.getSubimage(0, 0, 248, 235)));
 		label.setBounds(0, 0, 248, 232);
 		this.fensterDialog.add(label, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		
 		//Disable Dialogfenster on startup
 		fensterDialog.setEnabled(false);
 		fensterDialog.setVisible(false);
 	}
 
 
 
 	/**
 	 * Erzeugt alle Buttons (Aktionsbuttons, Pause/Exit) im Spielfenster
 	 * @author Stephan
 	 */
 	private void initButtonsGame() {
 		Button button;
 		// Pause + Exit Button
 		int buttonSize = Ressources.RASTERHEIGHT;
 		int buttonSliceX = 948;
 		int buttonSliceY = buttonSize;
 
 		String[] buttonNamesPauseExit = { "pause", "close" };
 		for (int i = 0; i < buttonNamesPauseExit.length; i++) {
 			button = new Button(this.control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					buttonNamesPauseExit[i], Ressources.ZEROPOS.width + (23 + i) * Ressources.RASTERHEIGHT, Ressources.ZEROPOS.height, this);
 			this.baseLayer.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			this.buttons.put(buttonNamesPauseExit[i], button);
 		}
 
 		//Statusanzeige fr den Fortschritt einer Aktion
 //		this.fortschrittsstatus.setBounds(Ressources.ZEROPOS.width+400, Ressources.ZEROPOS.height +642, 45, 45);
 //		this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[2]));
 		this.baseLayer.add(fortschrittsstatus, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		this.fortschrittsstatus.setVisible(false);
 		// Groe Buttons
 		buttonSize = 66;
 
 		// Buttons Aktionsleiste
 		buttonSliceX = 684;
 		buttonSliceY = buttonSize;
 
 		String[] buttonNamesAktionsleiste = { "beschwichtigen",
 				"spionage", "nachHause", "aktionrazzia" };
 		for (int i = 0; i < buttonNamesAktionsleiste.length; i++) {
 			button = new Button(this.control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					buttonNamesAktionsleiste[i], Ressources.ZEROPOS.width + 12 + i * 2*Ressources.RASTERHEIGHT, Ressources.ZEROPOS.height + 642, this);
 			// Nach Hause und Razzia disabled
 			if (i == 2) {
 				button.setEnabled(false);
 			}
 			this.baseLayer.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			this.buttons.put(buttonNamesAktionsleiste[i], button);
 		}
 		
 
 		// Kleine Buttons TODO entfernen bei Code Cleanup am Schluss - solange bitte nicht lschen!
 		/*buttonSize = 39;
 		buttonSliceX = 0;
 		buttonSliceY = buttonSize;
 
 		String[] buttonNamesSmall = { "smallWanze", "smallKamera",
 				"smallHacken", "smallFernglas", "smallParkSpionage",
 				"small6Spionage", "smallKuchen", "smallUnterhalten",
 				"smallFlirten", "smallHand", "smallParkBeschwichtigen",
 				"small6Beschwichtigen", "smallNachhause", "smallRazzia",
 				"smallQuizA", "smallQuizB", "smallQuizC" };
 		for (int i = 0; i < buttonNamesSmall.length; i++) {
 			button = new Button(this.control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					buttonNamesSmall[i], Ressources.ZEROPOS.width - 39, 0 + i * buttonSize, this);
 			this.baseLayer.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			this.buttons.put(buttonNamesSmall[i], button);
 		}*/
 	}
 
 
 
 	/**
 	 * Erzeugung der unsichtbaren Buttons ber den Husern
 	 * Nummeriert von 1-9
 	 * @author Sven
 	 */
 	private void initButtonsHaeuser() {
 		Button button;
 		Integer i;
 		Integer j;
 		Integer x;
 		Integer b;
 		b = 0;
 		for (x = 1; x <= Ressources.NUMBERHOUSES; x++) {
 			b = 0;
 			if (b == 0) {
 				for (j = 0; j < Ressources.getLocation_ids().size(); j++) {
 					if (b == 0) {
 						for (i = 0; i < Ressources.getLocation_ids().get(0)
 								.size(); i++) {
 							if (Ressources.getLocation_ids().get(j).get(i)
 									.equals(String.valueOf(x))
 									&& b == 0) {
 								b = 1;
 								button = new Button(this.control,
 										Ressources.RASTERHEIGHT * 3,
 										Ressources.RASTERHEIGHT * 3, "Haus"
 												+ String.valueOf(x),
 										Ressources.ZEROPOS.width + i
 												* Ressources.RASTERHEIGHT,
 										Ressources.ZEROPOS.height + j
 												* Ressources.RASTERHEIGHT, this);
 								this.baseLayer.add(button,
 										javax.swing.JLayeredPane.DEFAULT_LAYER);
 								this.buttons.put("Haus" + String.valueOf(x),
 										button);
 
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 
 	/**
 	 * Erzeugt Aktionsfenster fr Spionage und Beschwichtigen
 	 * @param fenster Fenster-Objekt
 	 * @param buttonSliceX Start-X-Position auf der Button Slicemap
 	 * @param buttonNames String-Array mit zugehrigen Aktionscodes der Buttons
 	 * @author Stephan
 	 */
 	private void initButtonsAktionsfenster(JLayeredPane fenster, int buttonSliceX, String[] buttonNames) {
 		int buttonSize = 66;
 		int buttonSliceY = buttonSize;
 		int buttonPosX = 0;
 		int buttonPosY = 76;
 
 		for (int i = 0; i < buttonNames.length; i++) {
 			Button button = new Button(this.control,
 					Ressources.ingamebutton.getSubimage(buttonSliceX, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 2 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					Ressources.ingamebutton.getSubimage(buttonSliceX + 3 * buttonSize, i * buttonSliceY, buttonSize, buttonSize),
 					buttonNames[i], 15 + buttonPosX * 75, buttonPosY, this);
 			fenster.add(button, javax.swing.JLayeredPane.DEFAULT_LAYER);
 			this.buttons.put(buttonNames[i], button);
 
 			//Button 1 Position weiter rechts zeichnen
 			buttonPosX++;
 			//nach 3 Buttons neue Zeile
 			if ((i + 1) % 3 == 0) {
 				buttonPosX = 0;
 				buttonPosY += 75;
 			}
 		}
 	}
 
 
 
 	/**
 	 * Generieren der Menschen und Teleport in die Huser
 	 * @author Miri
 	 * @param agentname 
 	 */
 	private void initHumans(String agentname) {
 		// jeweils dem Agenten und dem Schwerverbrecher eine Hausnummer generieren
 		// und dafr sorgen, dass sie ungleich sind
 		int house_of_terrorist = (int) (Math.random() * Ressources.NUMBERHOUSES);
 		int agent_house_nr = (int) (Math.random() * Ressources.NUMBERHOUSES);
 		if (agent_house_nr == house_of_terrorist) {
 			if (house_of_terrorist == Ressources.NUMBERHOUSES-1) {
 				house_of_terrorist--;
 			} else {
 				house_of_terrorist++;
 			}
 		}
 
 		int people_per_house;
 		int number_of_adults;
 		int number_of_children;
 
 		// erster Erscheinungspunkt einer Person in einem Haus --> jedes Haus
 		// hat einen
 		int spawnHausX[] = new int[Ressources.NUMBERHOUSES];
 		int spawnHausY[] = new int[Ressources.NUMBERHOUSES];
 
 		// alle Erscheinungspunkte der Personen in einem Haus
 		int spawnPersonX[] = new int[4];
 		int spawnPersonY[] = new int[4];
 
 		int familien_cnt = 0; // wie viele Menschen in einem Haus schon
 								// generiert wurden
 		int mensch_cnt = 0; // wie viele Menschen insgesamt generiert wurden
 		Mensch mensch; // Hilfsvariable zum Zwischenspeichern
 
 		// Spawnpunkte initialisieren
 		// immer linke obere Ecke in einem Haus
 		ArrayList<ArrayList<String>> location_raster = Ressources
 				.getLocation_ids();
 		for (int haus = 0; haus < Ressources.NUMBERHOUSES; haus++) {
 			// fr jedes Haus die Location in der Map herausfinden
 			for (int i = 0; i < location_raster.size(); i++) { // y-Achse
 				for (int j = 0; j < location_raster.get(i).size(); j++) { // x-Achse
 					// das erste 45er-Pixel, das man von dem Haus findet, wird
 					// der erste Spawnpunkt
 					if (location_raster.get(i).get(j).charAt(0) == ("" + (haus + 1))
 							.charAt(0)) {
 						spawnHausX[haus] = j * Ressources.RASTERHEIGHT
 								+ Ressources.ZEROPOS.width - 2
 								* Ressources.RASTERHEIGHT;
 						spawnHausY[haus] = i * Ressources.RASTERHEIGHT
 								+ Ressources.ZEROPOS.height - 2
 								* Ressources.RASTERHEIGHT;
 					}
 
 				}
 			}
 		}
 
 		for (int i = 0; i < Ressources.NUMBERHOUSES; i++) {
 			// fr jedes Haus die Familie erstellen
 			familien_cnt = 0;
 			// fr ein Haus die Spawnpunkte festlegen
 			spawnPersonX[0] = spawnHausX[i];
 			spawnPersonY[0] = spawnHausY[i];
 			spawnPersonX[1] = spawnHausX[i] + Ressources.RASTERHEIGHT;
 			spawnPersonY[1] = spawnHausY[i];
 			spawnPersonX[2] = spawnHausX[i] + 2 * Ressources.RASTERHEIGHT;
 			spawnPersonY[2] = spawnHausY[i];
 			spawnPersonX[3] = spawnHausX[i];
 			spawnPersonY[3] = spawnHausY[i] + Ressources.RASTERHEIGHT;
 
 			// Agent wird als letztes hinzugefgt
 			if (i != agent_house_nr) {
 				people_per_house = (int) (Math.random() * 4) + 1;
 				// wie viele Menschen in einem Haus wohnen sollen
 				if (i == house_of_terrorist) {
 					// Terrorist muss kann kein Kind sein
 					number_of_adults = 1 + (int) (Math.random() * people_per_house);
 					
 					//Evil Event
 					int id = (int)(Math.random()*Ressources.getEvilEvents().size());
 					mensch = new Terrorist(i,this.includeHaus(Ressources.getEvilEvents().get(id),i));	
 					System.out.println(mensch_cnt+". "+this.includeHaus(Ressources.getEvilEvents().get(id),i).get(0));	//TODO: System.out.... entfernen
 					
 					this.humans.add(mensch);
 					this.baseLayer.add(mensch,
 							javax.swing.JLayeredPane.DEFAULT_LAYER);
 					this.humans.get(mensch_cnt).teleport(
 							spawnPersonX[familien_cnt],
 							spawnPersonY[familien_cnt]);
 					this.humans.get(mensch_cnt).setHomePosX(
 							spawnPersonX[familien_cnt]);
 					this.humans.get(mensch_cnt).setHomePosY(
 							spawnPersonY[familien_cnt]);
 					mensch_cnt++;
 					familien_cnt++;
 
 					// brige Erwachsene setzen
 					for (int j = 0; j < number_of_adults - 1; j++) {
 						mensch = new Erwachsene(i,this.includeHaus(Ressources.getNormalEvents().get(mensch_cnt),i));
 						this.humans.add(mensch);
 						this.baseLayer.add(mensch,
 								javax.swing.JLayeredPane.DEFAULT_LAYER);
 						this.humans.get(mensch_cnt).teleport(
 								spawnPersonX[familien_cnt],
 								spawnPersonY[familien_cnt]);
 						this.humans.get(mensch_cnt).setHomePosX(
 								spawnPersonX[familien_cnt]);
 						this.humans.get(mensch_cnt).setHomePosY(
 								spawnPersonY[familien_cnt]);
 						mensch_cnt++;
 						familien_cnt++;
 					}
 				} else {
 					// in jedem Haushalt muss mindestens ein Erwachsener leben
 					number_of_adults = (int) (Math.random() * people_per_house) + 1;
 					for (int j = 0; j < number_of_adults; j++) {
 						mensch = new Erwachsene(i,this.includeHaus(Ressources.getNormalEvents().get(mensch_cnt),i));
 						this.humans.add(mensch);
 						this.baseLayer.add(mensch,
 								javax.swing.JLayeredPane.DEFAULT_LAYER);
 						this.humans.get(mensch_cnt).teleport(
 								spawnPersonX[familien_cnt],
 								spawnPersonY[familien_cnt]);
 						this.humans.get(mensch_cnt).setHomePosX(
 								spawnPersonX[familien_cnt]);
 						this.humans.get(mensch_cnt).setHomePosY(
 								spawnPersonY[familien_cnt]);
 						mensch_cnt++;
 						familien_cnt++;
 					}
 				}
 				// Kinder berechnen
 				number_of_children = people_per_house - number_of_adults;
 				for (int j = 0; j < number_of_children; j++) {
 					mensch = new Kinder(i,this.includeHaus(Ressources.getNormalEvents().get(mensch_cnt),i));
 					this.humans.add(mensch);
 					this.baseLayer.add(mensch,
 							javax.swing.JLayeredPane.DEFAULT_LAYER);
 					this.humans.get(mensch_cnt).teleport(
 							spawnPersonX[familien_cnt],
 							spawnPersonY[familien_cnt]);
 					this.humans.get(mensch_cnt).setHomePosX(
 							spawnPersonX[familien_cnt]);
 					this.humans.get(mensch_cnt).setHomePosY(
 							spawnPersonY[familien_cnt]);
 					mensch_cnt++;
 					familien_cnt++;
 				}
 				
 			} this.initHaus(i, false, spawnHausX[i], spawnHausY[i]);
 		}
 
 		// Simulation bentigt die Information von allen Bewohnern (ohne Agent)
 		for (int i = 0; i < this.humans.size(); i++) {
 			this.simulation.setPeople((Person) this.humans.get(i));
 		}
 
 		// Agent hinzufgen
 		spawnPersonX[0] = spawnHausX[agent_house_nr];
 		spawnPersonY[0] = spawnHausY[agent_house_nr];
 		mensch = new Agent(agent_house_nr,agentname);
 		this.humans.add(mensch);
 		this.baseLayer.add(mensch, javax.swing.JLayeredPane.DEFAULT_LAYER);
 		this.humans.get(mensch_cnt).teleport(spawnPersonX[0], spawnPersonY[0]);
 		this.humans.get(mensch_cnt).setHomePosX(spawnPersonX[0]);
 		this.humans.get(mensch_cnt).setHomePosY(spawnPersonY[0]);
 		this.simulation.setAgent((Agent) mensch);
 		this.initHaus(agent_house_nr, true, spawnPersonX[0], spawnPersonY[0]);
 	}
 
 	
 
 	/**
 	 * Erzeugung eines Hauses
 	 * @param hausnr Haus-ID
 	 * @param agentenhaus Ist dieses Haus das Agentenhaus?
 	 * @param x X-Position des Hauses
 	 * @param y Y-Position des Hauses
 	 * @author Miri
 	 */
 	private void initHaus(int hausnr, boolean agentenhaus, int x, int y) {
 		this.haus = new Haus(hausnr, agentenhaus, x, y);
 		this.simulation.setHouses(this.haus);
 	}
 
 
 	
 	/**
 	 * LocationID einer Person bei jedem Step updaten
 	 * @author Miri
 	 */
 	private void updateLocationID() {
 		ArrayList<ArrayList<String>> location_raster = Ressources.getLocation_ids();
 		int x;
 		int y;
 		//Update for Humans
 		for (int i = 0; i < this.humans.size(); i++) {
 			x = (this.humans.get(i).getPosX() + 2 * Ressources.RASTERHEIGHT - Ressources.ZEROPOS.width)
 					/ Ressources.RASTERHEIGHT - 2;
 			y = (this.humans.get(i).getPosY() + 2 * Ressources.RASTERHEIGHT - Ressources.ZEROPOS.height)
 					/ Ressources.RASTERHEIGHT - 2;
 			this.humans.get(i).setLocationId(
 					location_raster.get(y).get(x).charAt(0));
 		}
 		//Update for Agent
 		x = (getSimulation().getAgent().getPosX() + 2 * Ressources.RASTERHEIGHT - Ressources.ZEROPOS.width) / Ressources.RASTERHEIGHT - 2;
 		y = (getSimulation().getAgent().getPosY() + 2 * Ressources.RASTERHEIGHT - Ressources.ZEROPOS.height)/ Ressources.RASTERHEIGHT - 2;
 		getSimulation().getAgent().setLocationId(location_raster.get(y).get(x).charAt(0));
 	}
 
 	
 	
 	/**
 	 * Zeitanzeige in der Spielfenster-Aktionsleiste updaten
 	 * @author Miri 
 	 */
 	public void updateTime() {
 		// Tag malen
 		String s = "Tag " + this.simulation.getSpielTag();
 		this.anzeigeTag.setText(s);
 
 		// Uhr malen
 		s = this.simulation.getSpielzeitAsString();
 		this.anzeigeZeit.setText(s);
 	}
 
 	
 	
 	/**
 	 * Misstrauensstatus in der Spielfenster-Aktionsleiste updaten
 	 * @author Miri
 	 */
 	public void updateMisstrauen() {
 		String s = (float) Math.round(this.simulation
 				.calcMisstrauenInStreet() * 100) / 100 + "%";
 		this.anzeigeStatusMisstrauen.setText(s);
 	}
 
 	
 	
 	/**
 	 * berwachungsstatus in der Spielfenster-Aktionsleiste updaten 
 	 * @author Miri
 	 */
 	public void updateUeberwachung() {
 		String s = (float) Math.round(this.simulation
 				.calcUeberwachungsstatusInStreet() * 100) / 100 + "%";
 		this.anzeigeStatusUeberwachung.setText(s);
 	}
 
 	
 	
 	/**
 	 * Pausiert das Spiel wenn es luft, und setzt es fort, wenn es pausiert ist
 	 * @author Stephan
 	 */
 	public void updateTimerStatus() {
 		if (this.timer.isRunning()) {
 			this.timer.stop();
 		} else {
 			this.timer.start();
 		}
 	}
 	
 	/**
 	 * @author Miriam
 	 */
 	public ArrayList<String> includeHaus(ArrayList<String> input, int i){
 		String hausnr = String.valueOf(i+1);
 		String output = "";
 		output = input.get(0).replace("%",hausnr);
 		input.set(0,output);
 		return input;
 	}
 	
 	/**
 	 * @author Miriam
 	 */
 	public String includeNames(String input){	
 		String frau = "";
 		String mann = "";
 		int index = (int)(Math.random()*(this.humans.size()-1));
 		if(((Person)this.humans.get(index)).getGeschlecht() == 2){
 			while(((Person)this.humans.get(index)).getGeschlecht() != 1){
 				frau = this.humans.get(index).getName();
 				index = (int)(Math.random()*(this.humans.size()-1));
 			}
 			mann = this.humans.get(index).getName();
 		}
 		else{
 			while(((Person)this.humans.get(index)).getGeschlecht() != 2){
 				mann = this.humans.get(index).getName();
 				index = (int)(Math.random()*(this.humans.size()-1));
 			}
 			frau = this.humans.get(index).getName();	
 		}
 		
 		
 		
 		String hausnr = String.valueOf((int)(Math.random()*Ressources.NUMBERHOUSES)+1);
 		String egal1 = this.humans.get((int)(Math.random()*this.humans.size()-1)).getName();
 		String egal2 = this.humans.get((int)(Math.random()*this.humans.size()-1)).getName();
 		while(egal1.equals(egal2))
 			egal2 = this.humans.get((int)(Math.random()*this.humans.size()-1)).getName();
 			
 		String output="";
 				if(input.contains("$1")){
 					output = input.replace("$1",egal1);
 					input = output;
 				}
 				if(input.contains("$2")){
 					output = input.replace("$2",egal2);
 					input = output;
 				}
 				if(input.contains("$M")){
 					output = input.replace("$M",mann);
 					input = output;
 				}
 				if(input.contains("$W")){
 					output = input.replace("$W",frau);
 					input = output;
 				}
 				if(input.contains("%")){
 					output = input.replace("%",hausnr);
 					input = output;
 				}
 			return input;
 		}
 	
 	/**
 	 * @author Miriam
 	 */
 	public String getLiveTickerGags(){
 		String text;
 		int zufall = (int)(Math.random()*Ressources.getLiveTickerGags().size());
 		ArrayList<String> gag = Ressources.getLiveTickerGags().get(zufall);
 		text = gag.get(0);
 		return includeNames(text);
 	}
 	
 	/**
 	 * @author Miriam
 	 */
 	public void stopGame(){
 		this.control.clickPause();
 		this.buttons.get("pause").setEnabled(false);
 		//this.callHighscore();
 	}
 	
 	
 	/**
 	 * @author Miriam
 	 */
 	private void generateLiveTicker(){
 		boolean b = false;
 		int zufall = (int)(Math.random()*5);
 		//alle Personen auf Events berprfen
 		for(int i=0;i<this.humans.size()-1;i++){	
 			if(this.humans.get(i) instanceof Person){
 				//wenn das Event noch nicht aufgetaucht ist
 				if(((Person)this.humans.get(i)).getEvent().size() == 3){
 					//wenn der berwachungswert des Hauses hoch genug ist, um das Event zu entdecken
 					if(this.simulation.getHouses().get(this.humans.get(i).getHausId()).getUeberwachungsstatus() >= Integer.valueOf(((Person)this.humans.get(i)).getEvent().get(2))){
 						if(zufall == 1){
 							b = true;
 							this.newsticker.setForeground(new java.awt.Color(249, 50, 50));
 							this.newsticker.setText(((Person)this.humans.get(i)).getEvent().get(0));
 							((Person)this.humans.get(i)).addStringToEvent("used");
 							break;
 						}
 					}
 				}
 			}
 		}
 		if(!b){
 			this.newsticker.setForeground(new java.awt.Color(249, 249, 249));
 			this.newsticker.setText(this.getLiveTickerGags());
 		}
 		this.tickerTextSize = this.newsticker.getGraphics().getFontMetrics().stringWidth(this.newsticker.getText());
 		this.newsticker.setBounds(0, 0, this.tickerTextSize+5, Ressources.RASTERHEIGHT+2);
 		tickerStep = 0;
 		if(this.tickerTextSize > Ressources.MAPWIDTH-120)
 			tickerStep = 50;
 	}
 	
 	
 	/**
 	 * Step steuert den zeitlichen Ablauf des Spieles und triggert die Simulation und Updates im Spielfenster
 	 * @author Miri, Tobias, Tiki
 	 */
 	public void step() {
 		this.updateLocationID();
 		
 		
 		if(this.stepcounter%5==0){
 			//zeichne neuen berwachungs und Misstrauenswertwert
 			this.updateUeberwachung();
 			this.updateBalken();
 			getSimulation().updateUeberwachungsstatus();
 			checkThreeFernglaeserInStreet();
 			this.updateMisstrauen(); // Wert neu zeichnen
 			if(this.simulation.calcGameOver()){
 				this.stopGame();
 				if (getSimulation().calcMisstrauenInStreet()>=90.0){
 					this.showDialogMessage("Verloren", "Die Bevlkerung wurde zu misstrauisch. Ihre Identitt wurde enttarnt.", false, false);
 				}
 				this.showDialogMessage("Verloren", "Der Schwerverbrecher ist geflohen.", false, false);
 			}
 		}
 		
 		
 		//TODO etwas besseres Verfahren ausdenken
 		//Quizaufruf 
 		//int zeitpunkt = (int) (Math.random() * 1000);
 		if(this.stepcounter % (2000) == 0 && ((getSimulation().getSpielTag()==1 && getSimulation().getSpielStunde()>7) || getSimulation().getSpielTag()>1)) {
 			quiz.starteQuiz();
 			getMousefollower().setVisible(false);
 			buttons.get("pause").setEnabled(false);
			control.closeWindow("fensterhaus");
 		}
 		
 		if(stepcounter%2==0 && quiz.isRunning()){
 			quiz.step();
 		}
 		
 		//Beschwerden Miri
 		//Liveticker
 		if(this.stepcounter%tickerHaufigkeit == 0){
 			this.generateLiveTicker();
 		}
 		
 		//Newsticker durchtickern lassen, falls der Text zu lang ist
 		if(this.tickerTextSize > Ressources.MAPWIDTH-120){
 			int tmp = tickerHaufigkeit/(this.tickerTextSize-(Ressources.MAPWIDTH-120));
 			if(tmp == 0)
 				tmp = 3;
 			else
 				tmp = 2;
 			if((this.stepcounter)%2 == 0){
 				tickerStep -= tmp;
 				this.newsticker.setLocation((int)tickerStep,0);
 			}
 		}
 		
 		//Unwohlsein durch installierte berwachungsmodule
 		if(this.stepcounter%1000 == 0){
 			this.simulation.calcMisstrauenDuringUeberwachung();
 		}
 		
 		//Misstrauen bei berwachungsaktion
 		if(this.stepcounter%(Ressources.GAMESPEED/2) == 0){
 			getSimulation().agentBetrittFremdesHaus();
 		}
 		
 
 		//Nacht-Modus aktivieren / deaktivieren				//Fragen an Sven
 		int stunde = this.simulation.getSpielStunde();
 		int minute = this.simulation.getSpielMinute();
 		float farbteil1 = 0.05f;
 		float farbteil2 = 0.01f;
 		if(stunde == 21){
 			if (minute == 0){
 				this.overlayNacht.setVisible(true);
 			}
 			if ((minute >= 0) && (minute <= 20)){
 				this.overlayNacht.setBackground(new Color(0, 0, farbteil1*minute, farbteil2*minute));
 				this.repaint();	
 			}
 			buttons.get("aktionKuchen").setEnabled(false);
 			buttons.get("aktionFlirten").setEnabled(false);
 			buttons.get("aktionUnterhalten").setEnabled(false);
 			buttons.get("aktionHand").setEnabled(false);
 			
 			if (!getSimulation().isWieschteAktion() && !control.getLastFunktioncode().equals("parkBeschwichtigen")){
 				getMousefollower().setVisible(false);
 				if (!getSimulation().getAgent().getMussWuseln().equals("Park")){
 					getSimulation().getAgent().setMussWuseln("");
 				}
 				control.setLastFunktioncode("");
 			}
 		}
 		if (stunde == 2){
 			buttons.get("beschwichtigen").setEnabled(false);
 			control.closeWindow("beschwichtigen");
 			if (getSimulation().getAgent().getMussWuseln().equals("Park")){
 				getSimulation().getAgent().setMussWuseln("");
 			}
 		}
 		if (stunde == 6) {
 			if ((minute >= 0) && (minute <= 20)){
 				this.overlayNacht.setBackground(new Color(0, 0, 1f-farbteil1*minute, 0.2f-farbteil2*minute));
 				this.repaint();	
 			}			
 			if(minute == 20){
 				this.overlayNacht.setVisible(false);
 			}
 			buttons.get("aktionKuchen").setEnabled(true);
 			buttons.get("aktionFlirten").setEnabled(true);
 			buttons.get("aktionUnterhalten").setEnabled(true);
 			buttons.get("aktionHand").setEnabled(true);
 			buttons.get("parkBeschwichtigen").setEnabled(true);
 			buttons.get("beschwichtigen").setEnabled(true);
 		}
 
 		//Misstrauen berechnen alle 100 Steps
 		if (this.stepcounter % 100 == 0) {
 			this.simulation.calcMisstrauen();
 			this.simulation.calcMisstrauenInStreet();
 			for (int i = 0; i < this.humans.size(); i++) {
 				if (this.humans.get(i) instanceof Person) {
 					((Person) this.humans.get(i)).updateSchatten();
 				}
 			}
 			//this.updateMisstrauen(); // Wert neu zeichnen
 			this.simulation.calcMisstrauenMax();
 			this.updateBalken();
 			
 				
 		}
 		
 
 		//Spielzeit und Tagesablauf berechnen alle 4 Steps
 		if (this.stepcounter % 4 == 0) {
 			this.simulation.calcSpielzeit();
 			this.updateTime(); // Zeichne Spiel
 			this.simulation.tagesablauf();
 		}
 
 		// Update der Personen - Step ausfhren
 		for (int i = 0; i < this.humans.size(); i++) {
 			this.humans.get(i).step();
 		}
 		if (getSimulation().getAgent().getCurrentMove()!= 'n' &&
 				(int)(getSimulation().getAgent().getLocationId()-48)>=1 && (int)(getSimulation().getAgent().getLocationId()-48)<=9 &&
 				getSimulation().isWieschteAktion()){
 			drawFortschrittsbalken();
 		} else{
 			this.fortschrittsstatus.setVisible(false);
 		}
 		
 		
 		if (getSimulation().getAgent().getCurrentMove()=='n' && !getSimulation().getAgent().getMussWuseln().isEmpty()){
 			getSimulation().doSomethingAfterAgentAction();
 		}
 		
 		//durchgefhrte Beschwichtigungen um 0Uhr zurcksetzen
 		if(this.simulation.getSpielStunde() == 0 && this.simulation.getSpielMinute() == 0){
 			for(int i=0;i<this.simulation.getPeople().size();i++){
 				for(int j=0;j<Ressources.NUMBERBESCHWICHTIGENACTIONS;j++){
 					this.simulation.getPeople().get(i).setDurchgefuehrteBeschwichtigungen(j, 0);
 				}
 			}
 		}
 		
 		
 		this.stepcounter++;
 	}
 
 	private void drawFortschrittsbalken() {
 		int step=0;
 		int hausposx = 0;
 		int hausposy = 0;
 		
 		hausposx= getSimulation().getHouses().get((int)(getSimulation().getAgent().getLocationId()-48-1)).getPosX() + Ressources.RASTERHEIGHT;
 		hausposy= getSimulation().getHouses().get((int)(getSimulation().getAgent().getLocationId()-48-1)).getPosY() + Ressources.RASTERHEIGHT;
 
 		if (getSimulation().getAgent().getMussWuseln().equals("Wanze+") || getSimulation().getAgent().getMussWuseln().equals("Wanzer+") ||
 			getSimulation().getAgent().getMussWuseln().equals("Hacken+") || getSimulation().getAgent().getMussWuseln().equals("Hackenr+")){
 			step = 30/7;
 		}
 		if (getSimulation().getAgent().getMussWuseln().equals("Kamera+") || getSimulation().getAgent().getMussWuseln().equals("Kamerar+")){
 			step = 45/7;
 		}
 		if (getSimulation().getAgent().getMussWuseln().contains("Fernglas+") || getSimulation().getAgent().getMussWuseln().contains("Fernglasr+")){
 			step = 15/7;
 		}
 		
 		
 		
 		if (step!=0){
 			switch (step=getSimulation().getAgent().getMoves().size()/step){
 			case 0: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[6]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 1: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[5]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 2: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[4]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 3:	
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[3]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 4: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[2]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 5: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[1]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			case 6: 
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[0]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			default:
 					this.fortschrittsstatus.setBounds(hausposx, hausposy, 45, 45);
 					this.fortschrittsstatus.setIcon(new ImageIcon(fortschrittskreis[0]));
 					this.fortschrittsstatus.setVisible(true);
 					break;
 			}
 		}
 	}
 
 
 
 	/**
 	 * deaktiviert den Fernglasbutton, wenn 3 Fernglaeser installiert sind
 	 * aktiviert den Fernglasbutton, wenn weniger als 3 Fernglaeser installiert sind
 	 * @author Martika
 	 */
 	private void checkThreeFernglaeserInStreet(){
 		int fernglasCounter = 0;
 		
 		for (int i = 0; i<=Ressources.NUMBERHOUSES-1; i++){
 			if (getSimulation().getHouses().get(i).getUeberwachungsmodule().contains("Fernglas")){
 				fernglasCounter++;
 			}
 		}
 		if (fernglasCounter==3){
 			getButtonsMap().get("aktionFernglas").setEnabled(false);
 		} else{
 			getButtonsMap().get("aktionFernglas").setEnabled(true);
 		}
 	}
 	
 	
 	
 	/**
 	 * Gibt die HashMap der Buttons zurck
 	 * @return Hashmap der Ingame-Buttons
 	 * @author Stephan
 	 */
 	public HashMap<String, Button> getButtonsMap() {
 		return this.buttons;
 	}
 
 	
 	
 	/**
 	 * Gibt das passende Fenster zurck
 	 * @param window Name des Fensters
 	 * @return Fenster-Objekt
 	 * @author Tobi
 	 */
 	public JLayeredPane getWindow(String window) {
 		if (window.equals("spionage"))
 			return this.fensterSpionage;
 		if (window.equals("beschwichtigen"))
 			return this.fensterBeschwichtigen;
 		if (window.equals("quizfenster"))
 			return this.fensterQuiz;
 		if (window.equals("fensterhaus"))
 			return this.fensterHaus;
 		if (window.equals("dialog"))
 			return this.fensterDialog;
 		return null;
 	}
 
 	/**
 	 * Lifert das Jlabel fr den Beschreibungstext im Aktionsfenster
 	 * @param window String fr das Fenster
 	 * @return jLabel des Beschreibungstext
 	 */
 	public JLabel getBeschreibung(String window){
 		if (window.equals("spionage"))
 			return this.spionageBeschr;
 		if (window.equals("beschwichtigen"))
 			return this.beschwichtigenBeschr;
 		return null;
 	}
 
 	/**
 	 * Gibt Mousefollower zurck
 	 * @return Mousefollower JLabel (Grafik)
 	 */
 	public JLabel getMousefollower() {
 		return this.overlayMousefollower;
 	}
 	
 	/**
 	 * Gibt Simulationsobjekt zurck
 	 * @return Simulation
 	 */
 	public Simulation getSimulation(){
 		return this.simulation;
 	}
 	
 	
 	/**
 	 * ActionListener fr bewegte Maus
 	 * @author Tiki
 	 */
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		//Mausicon bewegen
 		this.overlayMousefollower.setLocation(e.getXOnScreen() - 15,
 				e.getYOnScreen() - 15);
 	}
 	
 	
 	/**
 	 * ActionListener fr Drag & Drop der Maus
 	 * @author Tiki
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// do nothing; notwendig fr implements MouseMotion (Interface)
 	}
 	
 	/**
 	 * ArrayList der Menschen zurckggeben
 	 * @return ArrayList Mensch
 	 */
 	public ArrayList<Mensch> getHumans(){
 		return this.humans;
 	}
 	
 	public JLabel[] getHausinfoLabels(){
 		return hausinformationen;
 	}
 	
 	/**
 	 * Erneuert die Balekn, die das Misstrauen und die berwachung anzeigen
 	 * @author Tobias
 	 */
 	public void updateBalken(){
 		double misstrauen = simulation.calcMisstrauenInStreet();
 		if(misstrauen > 0.0f){
 			this.informationsbalken[0].setSize((int)(1.66*misstrauen),19);
 			this.informationsbalken[1].setSize(0,19);
 		}
 		else{
 			this.informationsbalken[1].setSize((int)(-1.66*misstrauen),19);
 			this.informationsbalken[0].setSize(0,19);
 		}
 		this.informationsbalken[2].setSize((int)(simulation.calcUeberwachungsstatusInStreet()*1.66),19);
 	}
 	
 	/**
 	 * Zeigt ein Dialogfenster mit berschrift und Beschribung an.
 	 * <b>Die berschrift muss identifizierend sein, um die clickEvents zuordnen zu knnen</b>
 	 * @author Tobias
 	 * @param ueberschrift String fr die berschrift
 	 * @param beschreibung String fr den Beschreibungstext
 	 * @param cancelButton true falls cancel Button angezeigt werden soll
 	 * @param acceptButton true faalls accept button angezeigt werden soll
 	 */
 	public void showDialogMessage(String ueberschrift, String beschreibung, boolean cancelButton, boolean acceptButton){
 		dialogText.setText(beschreibung);
 		dialogberschrift.setText(ueberschrift);
 		fensterDialog.setEnabled(true);
 		fensterDialog.setVisible(true);
 		buttons.get("dialogAccept").setLocation(cancelButton?40:90, 132);
 		buttons.get("dialogDecline").setLocation(acceptButton?140:90, 132);
 		buttons.get("dialogAccept").setVisible(acceptButton);
 		buttons.get("dialogAccept").setEnabled(acceptButton);
 		buttons.get("dialogDecline").setVisible(cancelButton);
 		buttons.get("dialogDecline").setEnabled(cancelButton);
 	}
 	
 	public String getDialogUeberschrift(){
 		return dialogberschrift.getText();
 	}
 	
 	//Beschwerden Miri
 	public void callHighscore(){
 		this.highscore.calcHighscoreOfAgent();
 		this.highscore.exportIntoUser();
 		this.highscore.exportIntoScores();
 	}
 	
 	//Beschwerden Miri
 	public Highscore getHighscore(){
 		return this.highscore;
 	}
 	
 }
