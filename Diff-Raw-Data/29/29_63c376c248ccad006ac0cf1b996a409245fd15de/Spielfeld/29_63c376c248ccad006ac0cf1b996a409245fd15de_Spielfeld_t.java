 package de.dhbw.gui;
 
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 
 import de.dhbw.gui.BestaetigungBeenden;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.dhbw.muehle_api.*;
 import de.dhbw.muehle_api.strategy.IBewegung;
 import de.dhbw.muehle_api.strategy.ISpielzug;
 import de.dhbw.muehle_api.strategy.StrategieException;
 import de.dhbw.muehle_spiel.Bewegung;
 import de.dhbw.muehle_spiel.EPhase;
 import de.dhbw.muehle_spiel.Pruefung;
 import de.dhbw.muehle_spiel.Spieler;
 import de.dhbw.muehle_spiel.Spielstein;
 import de.dhbw.strategy.Bewertung;
 import de.dhbw.strategy.Spielzug;
 import de.dhbw.strategy.Strategie;
 
 import javax.swing.JDialog;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuKeyListener;
 import javax.swing.event.MenuKeyEvent;
 import javax.swing.event.MenuListener;
 
 public class Spielfeld extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static JPanel contentPane;
 	
 	//Die Buttons werden initialisiert
 	TransparentButtonFeld btnNewButton_1, btnNewButton_2, btnNewButton_3, btnNewButton_4, 
 							btnNewButton_5, btnNewButton_6, btnNewButton_7, btnNewButton_8,
 							btnNewButton_9, btnNewButton_10, btnNewButton_11, btnNewButton_12,
 							btnNewButton_13, btnNewButton_14, btnNewButton_15, btnNewButton_16,
 							btnNewButton_17, btnNewButton_18, btnNewButton_19, btnNewButton_20,
 							btnNewButton_21, btnNewButton_22, btnNewButton_23, btnNewButton_24;
 	
 	//Die Spieler werden initialisiert 
 	Spieler Spieler1;
 	Spieler Spieler2;
 
 	
 	static JPanel panel;
 	
 	//Array mit den Steinen die aktuell auf dem Spielfeld stehen an der Position im Array
 	Spielstein[][][] SpielfeldArray = new Spielstein[3][3][3];
 	
 	//Array mit den Indizes der Steine die man aktuell auf dem Spielfeld hat
 	int[] Indizes = new int[9];
 	
 	private JMenuBar menuBar;
 	private JMenu mnNewMenu1, mnNewMenu2, mnNewMenu3;
 	private JPanel panel_1;
 
 	
 	//xPos und yPos sind die reellen Positionen auf dem Feld, 
 	//anzahlRunden zhlt die Anzahl der Runden
 	static int xPos;
 
 	static int yPos;
 
 	int anzahlRunden;
 	int zaehler1 = 1;
 	int zaehler2 = 0;
 	
 	static Font gFont = new Font(Font.SERIF, Font.BOLD + Font.ITALIC, 30);
 
 	
 	private boolean hatMuehle = false;
 	
 	private Spieler aktuellerSpieler, passiverSpieler;
 	
 	private Bewegung neueBewegung;
 	
 	//Array mit den aktuellen Steinen auf dem Feld
 	List<ISpielstein> gSteine = new ArrayList<ISpielstein>();
 	
 	//static Database db = new Database();
 	
 	private boolean SteinKannGeloeschtWerden = true;
 	
 
 	
 	//Mode des Spiels 1 = mensch gg mensch; 2 = mensch gg pc
 	private int mode = 1;
 	
 	private String nameSpieler1; //= "default Name Spieler 1";
 	private String nameSpieler2; //= "default Name Spieler 2";
 	private String textMuehle = " hat eine Mhle!";
 	private String textNeuesSpiel = "Neues Spiel - Stefan beginnt!";
 	private String textRunde2 = "Ab jetzt: Steine ziehen!";
 	private static String textSpielName = "Mhle - Das Brettspiel fr Gro und Klein";
 	private String textGewonnen = " hat gewonnen!!!";
 	private String textDarfSpringen = " darf jetzt springen!";
 	
 	private int meldungsZeit = 1;
 	private int wichtigeMeldungsZeit = 2;
 	
 	//Alte Position wenn man zieht und die variable ob der aktuelle klick schon das neue setzen ist
 	Position altePosition; 
 	boolean hatAltePosition;
 	
 	//die linken oberen Positionen der drei Felder auf der rechten Seite
 	int xPosOben = 12;				
 	int yPosOben = 48;				
 	int xPosMitte = 12;				
 	int yPosMitte = 226;
 	int xPosUnten = 12;				
 	int yPosUnten = 404;
 	
 	//variablen die mitzhlen wie viel Steine von dem jeweiligen Spieler wegen Mhlen vom Spielfeld entfernt wurden
 	int entfernteSteineWeiss, entfernteSteineSchwarz = 0;
 	
 	//Wenn man auf einen Stein drckt um ihn zu verschieben, wird dieser Stein in dieser Variable gespeichert
 	Spielstein ausgewaehlterStein;	
 	
 	//Variable die mit true anzeigt ob das Spiel beendet ist
 	private boolean SpielBeendet = false;
 	
 	//neue Instanz der Prfungsklasse
 	Pruefung pruef = new Pruefung();
 	
 	//neue Instanz der Strategie, falls man gegen den PC spielen mchte
 	Strategie strategie1, strategie2;
 	
 	//Liste mit allen Knpfen auf dem Feld
 	List<TransparentButtonFeld> gButtons = new ArrayList<TransparentButtonFeld>();
 	
 	//neue Instanz der Prfung ZUM TESTEN!
 	Bewertung bewertung = new Bewertung();
 	
 	//Schwierigkeitsstufe des Spiels
 	private int gSchwierigkeit;
 	
 	//Gre des Meldungsfensters
 	private static int x = 600;
 	private static int y = 100;
 	
 	/**
 	 * Hier wird das Spielfeld gestartet
 	 */
 	
 	public static void main(String[] args) 
 	{
 		EventQueue.invokeLater(new Runnable() 
 		{
 			public void run() 
 			{
 				try 
 				{
 					Spielfeld frame = new Spielfeld("Stefan", "Georg", 1, 5, 1);
 					frame.addWindowListener(new WindowAdapter() {
 											public void windowClosing(WindowEvent evt) {
 											exitForm(evt);}});
 					frame.setVisible(true);
 					 this.neueMeldung(frame.wichtigeMeldungsZeit, frame.textNeuesSpiel, frame);
 				} catch (Exception e) 
 				{
 					e.printStackTrace();
 				}
 			} 
 
 			private void neueMeldung(final int sekunden, final String meldung, Spielfeld frame) 
 			{
 				//hier wird die Position festgelegt wo die meldung erscheinen soll
 				Point pos = frame.panel.getLocationOnScreen();
 				final int xPos = (int)(pos.getX() + 20);
 				final int yPos = (int)pos.getY()+200;
 				new Thread() 
 				{
 				      { 
 				    	  start(); 
 				      } 
 				      public void run() 
 				      {
 				        try 
 				        { 
 				        	Anweisung1 probe = new Anweisung1(meldung, xPos, yPos, 600, 100, new Font(Font.SERIF, Font.BOLD + Font.ITALIC, 30));
 							probe.setAlwaysOnTop(true);
 							probe.setVisible(true);
 				        	sleep(sekunden * 1000);
 				        	probe.setVisible(false);
 				        }
 				        catch ( InterruptedException e ) { e.printStackTrace(); }
 				      } 
 				};
 				
 			}
 		});
 	}
 
 	private static void exitForm(WindowEvent evt) 
 	{
 //		db.lschetb("protokoll");
         System.exit(0);
     }
 	
 	/**
 	 * Das JPanel wird erzeugt
 	 * Die Buttons werden instanziiert und mit dem action listener verknpft
 	 * @throws StrategieException 
 	 */
 	public Spielfeld(String Spieler1Name, String Spieler2Name, int lmode, int lSchwierigkeit, final int theme) throws StrategieException 
 	{
 		//Name der in dem Fenster angezeigt wird
 		super(textSpielName);
 
 		final String nameSpieler1 = Spieler1Name;
 		final String nameSpieler2 = Spieler2Name;
 		mode = lmode;
 		gSchwierigkeit = lSchwierigkeit;
 
 		
 		Spieler1 = new Spieler(ESpielsteinFarbe.WEISS, nameSpieler1);
 		switch(mode)
 		{
 			case 1 : Spieler2 = new Spieler(ESpielsteinFarbe.SCHWARZ, nameSpieler2); break;
 			case 2 : Spieler2 = new Spieler(ESpielsteinFarbe.SCHWARZ, nameSpieler2); strategie1 = new Strategie(); strategie1.startePartie(ESpielsteinFarbe.SCHWARZ); break;
 			case 3 : Spieler1 = new Spieler(ESpielsteinFarbe.WEISS, nameSpieler2); Spieler2 = new Spieler(ESpielsteinFarbe.SCHWARZ, nameSpieler2); 
 						strategie1 = new Strategie(); strategie2 = new Strategie(); 
 						strategie1.startePartie(ESpielsteinFarbe.WEISS); strategie2.startePartie(ESpielsteinFarbe.SCHWARZ);break;
 		}
 		
 		//Fenster fixieren
 		setResizable(false);
 		
 		setBounds(20,20,890,700);		//20,20,900,700
 		
 		//das Bild fr den weien und schwarzen Stein wird geladen
 		final Image SteinWeiss = Toolkit.getDefaultToolkit().getImage(  
                 Spielfeld.class.getResource("/de/dhbw/images/Spielstein hell.png"));
 		final Image SteinSchwarz = Toolkit.getDefaultToolkit().getImage(  
                 Spielfeld.class.getResource("/de/dhbw/images/Spielstein dunkel.png"));
 		
 		final Image transparentSteinWeiss = Toolkit.getDefaultToolkit().getImage(  
                 Spielfeld.class.getResource("/de/dhbw/images/transparent Spielstein hell.png"));
 		final Image transparentSteinSchwarz = Toolkit.getDefaultToolkit().getImage(  
                 Spielfeld.class.getResource("/de/dhbw/images/transparent Spielstein dunkel.png"));
 		
 		
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		mnNewMenu1 = new JMenu("Neues Spiel");
 		mnNewMenu1.addMenuListener(new MenuListener() {
 			
 			@Override
 			public void menuSelected(MenuEvent e) {
 				dispose();
 //				JFrame neuesSpiel = null;
 				try {
 					 Spielfeld neuesSpiel = new Spielfeld(nameSpieler1, nameSpieler2, mode, gSchwierigkeit, theme);
 					 neuesSpiel.setVisible(true);
 				} catch (StrategieException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				
 				
 			}
 			
 			@Override
 			public void menuDeselected(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void menuCanceled(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	
 		menuBar.add(mnNewMenu1);
 		
 		mnNewMenu2 = new JMenu("Spiel Beenden");
 		mnNewMenu2.addMenuListener(new MenuListener() {
 			@Override
 			public void menuSelected(MenuEvent e) {
 				final BestaetigungBeenden frageBeenden = new BestaetigungBeenden(100,100);
 				frageBeenden.setListener( new BestaetigungBeenden.BestatigungsListener() {
 					
 					@Override
 					public void onOK() {
 						
 						Spielfeld.this.dispose();
 						frageBeenden.dispose();
 //						db.lschetb("protokoll");
 						//schlieen
 					}
 					@Override
 					public void onCancel() {
 						//schlieen?	
 					}
 				});
 				frageBeenden.setVisible(true);
 			}
 			
 			@Override
 			public void menuDeselected(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void menuCanceled(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		menuBar.add(mnNewMenu2);
 		
 		
 		
 		
 		
 		mnNewMenu3 = new JMenu("Hilfe");
 		mnNewMenu3.addMenuListener(new MenuListener() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void menuSelected(MenuEvent e) {
 					JDialog anleitung = new Anleitung();
 					anleitung.show(true);
 					anleitung.setAlwaysOnTop(true);
 			}
 			
 			@Override
 			public void menuDeselected(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void menuCanceled(MenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		menuBar.add(mnNewMenu3);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		setContentPane(contentPane);
 		
 		
 		//hier wird das haupt-Panel erzeugt inc. Hintergrundbild
 		panel = new JPanel()
 		{  
             /**
 			 * 
 			 */
 			
 			private static final long serialVersionUID = 1L;
 
 			protected void paintComponent(Graphics g) 
             {
 				Image spielfeld = null;
 				
 				switch(theme)
 				{
 					case 1: spielfeld = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIlinks.png")); break;
 					case 2: spielfeld = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_Frhling_GUIlinks.png")); break;
 					case 3: spielfeld = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIlinks.png")); break;
 					case 4: spielfeld = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIlinks.png")); break;
 					case 5: spielfeld = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIlinks.png")); break;
 				}
 				
                 g.drawImage(spielfeld, 0, 0, this.getWidth(), this.getHeight(), this);  
                 
                 for(int i = 0; i <= 2; i++)
                 {
                 	for(int j = 0; j <= 2; j++)
                 	{
                 		for(int k = 0; k <= 2; k++)
                 		{
                 			if(SpielfeldArray[i][j][k] != null)
                 			{
 	                			Spielstein aktuellerStein = SpielfeldArray[i][j][k];
 	                			
 	                			int xPosi = aktuellerStein.getxPos();
                 				int yPosi = aktuellerStein.getyPos();
                 				
                 				//hier wird das Verhltnis festgelet, in dem die Steine zum Spielfeld stehen (gre)
                 				int breite = (int) ((int)spielfeld.getWidth(this)/6);
                 				int hoehe = (int) ((int)spielfeld.getHeight(this)/6);
                 				
                 				
                 				if(aktuellerStein != ausgewaehlterStein)
                 				{	
 		                			if(aktuellerStein.FarbVergleich(ESpielsteinFarbe.WEISS))
 		                			{
 		                				/*
 		                				 * hier luft das ab wenn auf einem Spielfeld ein weier stein steht 
 		                				 */
 		                				
 		                				if(!hatAltePosition && !hatMuehle)
 		                					g.drawImage(SteinWeiss,  xPosi , yPosi , breite, hoehe, this);
 		                				else
 		                				{
 		                					if(hatMuehle && aktuellerSpieler.equals(Spieler2) && 
 		                							(!pruef.checkInMuehle(aktuellerStein.getIndex(), Spieler1.Steine) || alleGegnerSteineInMhle(Spieler1)))
 		                						g.drawImage(SteinWeiss,  xPosi , yPosi , breite+6, hoehe+6, this);
 		                					else
 		                						g.drawImage(transparentSteinWeiss,  xPosi , yPosi , breite, hoehe, this);
 		                				}
 		                			}
 		                			else 
 		                			{
 		                				/*
 		                				 * hier luft das ab wenn auf einem Spielfeld ein weier stein steht 
 		                				 */
 		                				
 		                				if(!hatAltePosition && !hatMuehle)
 		                					g.drawImage(SteinSchwarz,  xPosi , yPosi , breite-4, hoehe-4, this);
 		                				else
 		                				{
 		                					if(hatMuehle && aktuellerSpieler.equals(Spieler1) && 
 		                							(!pruef.checkInMuehle(aktuellerStein.getIndex(), Spieler2.Steine) || alleGegnerSteineInMhle(Spieler2)))
 		                						g.drawImage(SteinSchwarz,  xPosi , yPosi , breite+2, hoehe+2, this);
 		                					else
 		                						g.drawImage(transparentSteinSchwarz,  xPosi , yPosi , breite-4, hoehe-4, this);
 		                				}
 		                			}
                 				}
 	                			
                 			}
                 			else
                 			{
                 				
                 			}
                 			
                 		}
                 	}
                 }
                 
                 //hier wird der ausgewhlte Stein wieder ohne transparenz gezeichnet
                 if(hatAltePosition)
                 {
                 	int e, x, y;
         			e = posIndexUmrechnen(altePosition.getEbene());
         			x = posIndexUmrechnen(altePosition.getX());
         			y = posIndexUmrechnen(altePosition.getY());
         			Spielstein aktuellerStein = SpielfeldArray[e][x][y];
         			
         			int xPosi = aktuellerStein.getxPos();
     				int yPosi = aktuellerStein.getyPos();
     				
     				//hier wird das Verhltnis festgelet, in dem die Steine zum Spielfeld stehen (grer)
     				int breite = (int) ((int)spielfeld.getWidth(this)/5);
     				int hoehe = (int) ((int)spielfeld.getHeight(this)/5);
     				
     				if(aktuellerStein.FarbVergleich(ESpielsteinFarbe.WEISS))
         				g.drawImage(SteinWeiss,  xPosi , yPosi , breite + 6, hoehe + 6, this);
     				else
     					g.drawImage(SteinSchwarz,  xPosi , yPosi , breite + 2, hoehe + 2, this);
         			
                 	
                 }
             }
 
 
 		};
 		
 		//Layout des Panels
 		panel.setLayout(new MigLayout("", "[5][66.15][25][66.15][25][60][2][60][2.00][60][25][66.15][25][66.15]", "[20][66.15px][35][66.15][37.48][60][5][60][2.2][60][37.48][66.15][40.79][66.15][25]"));
 		
 		
 		//ab hier werden die ganzen knpfe definiert und mit dem actionlistener verknpft und in die Button Liste eingefgt
 		btnNewButton_1 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_1.addActionListener(this);
 		gButtons.add(btnNewButton_1);
 		panel.add(btnNewButton_1, "cell 1 1,grow");
 		
 		btnNewButton_2 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_2.addActionListener(this);
 		gButtons.add(btnNewButton_2);
 		panel.add(btnNewButton_2, "cell 7 1,grow");
 		
 		btnNewButton_3 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_3.addActionListener(this);
 		gButtons.add(btnNewButton_3);
 		panel.add(btnNewButton_3, "cell 13 1,grow");
 		
 		btnNewButton_4 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_4.addActionListener(this);
 		gButtons.add(btnNewButton_4);
 		panel.add(btnNewButton_4, "cell 3 3,grow");
 		
 		btnNewButton_5 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_5.addActionListener(this);
 		gButtons.add(btnNewButton_5);
 		panel.add(btnNewButton_5, "cell 7 3,grow");
 		
 		btnNewButton_6 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_6.addActionListener(this);
 		gButtons.add(btnNewButton_6);
 		panel.add(btnNewButton_6, "cell 11 3,grow");
 		
 		btnNewButton_7 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_7.addActionListener(this);
 		gButtons.add(btnNewButton_7);
 		panel.add(btnNewButton_7, "cell 5 5,grow");
 
 		btnNewButton_8 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_8.addActionListener(this);
 		gButtons.add(btnNewButton_8);
 		panel.add(btnNewButton_8, "cell 7 5,grow");
 		
 		btnNewButton_9 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_9.addActionListener(this);
 		gButtons.add(btnNewButton_9);
 		panel.add(btnNewButton_9, "cell 9 5,grow");
 		
 		btnNewButton_10 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_10.addActionListener(this);
 		gButtons.add(btnNewButton_10);
 		panel.add(btnNewButton_10, "cell 1 7,grow");
 		
 		btnNewButton_11 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_11.addActionListener(this);
 		gButtons.add(btnNewButton_11);
 		panel.add(btnNewButton_11, "cell 3 7,grow");
 		
 		btnNewButton_12 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_12.addActionListener(this);
 		gButtons.add(btnNewButton_12);
 		panel.add(btnNewButton_12, "cell 5 7,grow");
 		
 		btnNewButton_13 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_13.addActionListener(this);
 		gButtons.add(btnNewButton_13);
 		panel.add(btnNewButton_13, "cell 9 7,grow");
 		
 		btnNewButton_14 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_14.addActionListener(this);
 		gButtons.add(btnNewButton_14);
 		panel.add(btnNewButton_14, "cell 11 7,grow");
 		
 		btnNewButton_15 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_15.addActionListener(this);
 		gButtons.add(btnNewButton_15);
 		panel.add(btnNewButton_15, "cell 13 7,grow");
 			
 		btnNewButton_16 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_16.addActionListener(this);
 		gButtons.add(btnNewButton_16);
 		panel.add(btnNewButton_16, "cell 5 9,grow");
 		
 		btnNewButton_17 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_17.addActionListener(this);
 		gButtons.add(btnNewButton_17);
 		panel.add(btnNewButton_17, "cell 7 9,grow");
 		
 		btnNewButton_18 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_18.addActionListener(this);
 		gButtons.add(btnNewButton_18);
 		panel.add(btnNewButton_18, "cell 9 9,grow");
 		
 		btnNewButton_19 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_19.addActionListener(this);
 		gButtons.add(btnNewButton_19);
 		panel.add(btnNewButton_19, "cell 3 11,grow");
 		
 		btnNewButton_20 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_20.addActionListener(this);
 		gButtons.add(btnNewButton_20);
 		panel.add(btnNewButton_20, "cell 7 11,grow");
 		
 		btnNewButton_21 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_21.addActionListener(this);
 		gButtons.add(btnNewButton_21);
 		panel.add(btnNewButton_21, "cell 11 11,grow");
 		
 		btnNewButton_22 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_22.addActionListener(this);
 		gButtons.add(btnNewButton_22);
 		panel.add(btnNewButton_22, "cell 1 13,grow");
 		
 		btnNewButton_23 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_23.addActionListener(this);
 		gButtons.add(btnNewButton_23);
 		panel.add(btnNewButton_23, "cell 7 13,grow");
 		
 		btnNewButton_24 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_24.addActionListener(this);
 		gButtons.add(btnNewButton_24);
 		panel.add(btnNewButton_24, "cell 13 13,grow");
 
 		//ab hier werden die Labels definiert die als Platzhalter zwischen den Buttons dienen
 		JLabel lblNewLabel = new JLabel("");
 		panel.add(lblNewLabel, "cell 3 1,grow");
 		
 		JLabel lblNewLabel_1 = new JLabel("");
 		panel.add(lblNewLabel_1, "cell 9 1,grow");
 		
 		JLabel lblNewLabel_2 = new JLabel("");
 		panel.add(lblNewLabel_2, "cell 11 1,grow");
 	
 		JLabel lblNewLabel_3 = new JLabel("");
 		panel.add(lblNewLabel_3, "cell 9 3,grow");
 		
 		JLabel label = new JLabel("");
 		panel.add(label, "cell 5 1,grow");
 		
 		JLabel label_1 = new JLabel("");
 		panel.add(label_1, "cell 1 3,grow");
 		
 		JLabel label_2 = new JLabel("");
 		panel.add(label_2, "cell 5 3,grow");
 		
 		JLabel label_3 = new JLabel("");
 		panel.add(label_3, "cell 13 3,grow");
 		
 		JLabel label_4 = new JLabel("");
 		panel.add(label_4, "cell 1 5,grow");
 		
 		JLabel label_5 = new JLabel("");
 		panel.add(label_5, "cell 3 5,grow");
 
 		JLabel label_6 = new JLabel("");
 		panel.add(label_6, "cell 11 5,grow");
 		
 		JLabel label_7 = new JLabel("");
 		panel.add(label_7, "cell 13 5,grow");
 
 		JLabel label_8 = new JLabel("");
 		panel.add(label_8, "cell 7 7,grow");
 
 		JLabel label_9 = new JLabel("");
 		panel.add(label_9, "cell 1 9,grow");
 		
 		JLabel label_10 = new JLabel("");
 		panel.add(label_10, "cell 3 9,grow");
 		
 		JLabel label_11 = new JLabel("");
 		panel.add(label_11, "cell 11 9,grow");
 		
 		JLabel label_12 = new JLabel("");
 		panel.add(label_12, "cell 13 9,grow");
 		
 		JLabel label_13 = new JLabel("");
 		panel.add(label_13, "cell 1 11,grow");
 		
 		JLabel label_15 = new JLabel("");
 		panel.add(label_15, "cell 9 11,grow");
 		
 		JLabel label_16 = new JLabel("");
 		panel.add(label_16, "cell 13 11,grow");
 		
 		JLabel label_17 = new JLabel("");
 		panel.add(label_17, "cell 3 13,grow");
 		
 		JLabel label_18 = new JLabel("");
 		panel.add(label_18, "cell 5 13,grow");
 
 		JLabel label_19 = new JLabel("");
 		panel.add(label_19, "cell 9 13,grow");
 		
 		JLabel label_20 = new JLabel("");
 		panel.add(label_20, "cell 11 13,grow");
 		
 		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		
 		
 		//hier wird das Panel auf der rechten seite erzeugt
 		panel_1 = new JPanel()
 		{
 			/**  
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void paint(Graphics g) 
 			{				
 				//font wird festgelegt
 				g.setFont(new Font(Font.SERIF, Font.BOLD + Font.ITALIC, 30));
 				
 				//Das Hintergrundbild wird geladen
 				Image spielfeldRechts = null;
 				
 				switch(theme)
 				{
 					case 1: spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIrechts.png")); break;
 					case 2: spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_Frhling_GUIrechts.png")); break;
 					case 3: spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIrechts.png")); break;
 					case 4: spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIrechts.png")); break;
 					case 5: spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
 	                          Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIrechts.png")); break;
 				} 
 				  
 				g.drawImage(spielfeldRechts, 0, 0, this.getWidth(), this.getHeight(), this);  
 				
 				//hier wird das Verhltnis festgelet, in dem die Steine zum Spielfeld stehen (gre)
 				int breite = panel.getWidth()/8;
 				int hoehe = panel.getWidth()/8;
 				
 				//Beschriftung der Felder
 				g.drawString(nameSpieler1 + ": ", xPosOben+15, yPosOben+35);
 				g.drawString(nameSpieler2 + ": ", xPosUnten+15, yPosUnten+42);
 				
 				//Zeichnet die noch vorhandenen Steine des Spielers
 				for(int i = 0; i < (9-Spieler1.getAnzahlSteine()-entfernteSteineWeiss); i++)
 				{
 					g.drawImage(SteinWeiss,  xPosOben + (i*15) , yPosOben + 30 , breite, hoehe, this);
 				}
 				for(int i = 0; i < (9-Spieler2.getAnzahlSteine()-entfernteSteineSchwarz); i++)
 				{
 					g.drawImage(SteinSchwarz,  xPosUnten + (i*15) , yPosUnten + 37 , breite-3, hoehe-3, this);
 				}
 				
 				//Zeichnet die gelschten Steine vom Gegner
 				for(int i = 0; i < entfernteSteineSchwarz; i++)
 				{
 					g.drawImage(SteinSchwarz,  xPosOben + (i*15) , yPosOben + 85 , breite-3, hoehe-3, this);
 				}
 				for(int i = 0; i < entfernteSteineWeiss; i++)
 				{
 					g.drawImage(SteinWeiss,  xPosUnten + (i*15) , yPosUnten + 92 , breite, hoehe, this);
 				}
 				
 				//Zeichnet in das mittlere Feld einen Stein der aktuellen Farbe
 				if(Spieler1.getAnzahlSteine()==0)
 					g.drawImage(SteinWeiss,  xPosMitte + 40, yPosMitte + 10 , breite+50, hoehe+50, this);
 				else
 				{
 					if(aktuellerSpieler == Spieler2)
 						g.drawImage(SteinWeiss,  xPosMitte + 40, yPosMitte + 10 , breite+50, hoehe+50, this);
 					else
 						g.drawImage(SteinSchwarz,  xPosMitte + 40, yPosMitte + 10 , breite+43, hoehe+43, this);
 				}
 				//Rundenanzeige
 				g.drawString("Runde " + (anzahlRunden+1), xPosMitte+15, yPosMitte+140);
 			}
 		};
 		
 		/*panel_1.setLayout(new MigLayout("", "[300]", "[225][227][225]"));
 		
 		JLabel labelSpieler1 = new JLabel("Spieler 1: ");
 		labelSpieler1.setFont(new Font(Font.SERIF, Font.BOLD + Font.ITALIC, 30));
 		labelSpieler1.setText("Hallo das hier ist der Text");
 		labelSpieler1.setVisible(true);
 		panel_1.add(labelSpieler1, "cell 0 0,grow");
 		
 		JLabel labelMitte = new JLabel("Mitte: ");
 		panel_1.add(labelMitte, "cell 0 1,grow");
 		
 		JLabel labelSpieler2 = new JLabel("Spieler 2: ");
 		panel_1.add(labelSpieler2, "cell 0 2,grow");*/
 		
 		
 		
 		
 		contentPane.setLayout(new MigLayout("", "[664.94px]0[300px]", "[677px]"));
 		contentPane.add(panel, "cell 0 0,alignx left,aligny top");
 		contentPane.add(panel_1, "cell 1 0,grow");
 		
 		
 		
 //		 db.erzeugetb("protokoll");
 
 		
 	}
 
 	
 	/*
 	 * Hier berprft der actionlistener welcher Knopf 
 	 *gedrckt wurde und gibt diesen dann an die methode aktion weiter
 	 */
 	
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		Object obj = e.getSource();
 		
 		if(obj.equals(this.btnNewButton_1))
 		{
 			this.aktion(btnNewButton_1);			
 		}
 		else if(obj.equals(this.btnNewButton_2))
 		{
 			this.aktion(btnNewButton_2);
 		}
 		else if(obj.equals(this.btnNewButton_3))
 		{
 			this.aktion(btnNewButton_3);
 		}
 		else if(obj.equals(this.btnNewButton_4))
 		{
 			this.aktion(btnNewButton_4);
 		}
 		else if(obj.equals(this.btnNewButton_5))
 		{
 			this.aktion(btnNewButton_5);
 		}
 		else if(obj.equals(this.btnNewButton_6))
 		{
 			this.aktion(btnNewButton_6);
 		}
 		else if(obj.equals(this.btnNewButton_7))
 		{
 			this.aktion(btnNewButton_7);
 		}
 		else if(obj.equals(this.btnNewButton_8))
 		{
 			this.aktion(btnNewButton_8);
 		}
 		else if(obj.equals(this.btnNewButton_9))
 		{
 			this.aktion(btnNewButton_9);
 		}
 		else if(obj.equals(this.btnNewButton_10))
 		{
 			this.aktion(btnNewButton_10);
 		}
 		else if(obj.equals(this.btnNewButton_11))
 		{
 			this.aktion(btnNewButton_11);
 		}
 		else if(obj.equals(this.btnNewButton_12))
 		{
 			this.aktion(btnNewButton_12);
 		}
 		else if(obj.equals(this.btnNewButton_13))
 		{
 			this.aktion(btnNewButton_13);
 		}
 		else if(obj.equals(this.btnNewButton_14))
 		{
 			this.aktion(btnNewButton_14);
 		}
 		else if(obj.equals(this.btnNewButton_15))
 		{
 			this.aktion(btnNewButton_15);
 		}
 		else if(obj.equals(this.btnNewButton_16))
 		{
 			this.aktion(btnNewButton_16);
 		}
 		else if(obj.equals(this.btnNewButton_17))
 		{
 			this.aktion(btnNewButton_17);
 		}
 		else if(obj.equals(this.btnNewButton_18))
 		{
 			this.aktion(btnNewButton_18);
 		}
 		else if(obj.equals(this.btnNewButton_19))
 		{
 			this.aktion(btnNewButton_19);
 		}
 		else if(obj.equals(this.btnNewButton_20))
 		{
 			this.aktion(btnNewButton_20);
 		}
 		else if(obj.equals(this.btnNewButton_21))
 		{
 			this.aktion(btnNewButton_21);
 		}
 		else if(obj.equals(this.btnNewButton_22))
 		{
 			this.aktion(btnNewButton_22);
 		}
 		else if(obj.equals(this.btnNewButton_23))
 		{
 			this.aktion(btnNewButton_23);
 		}
 		else if(obj.equals(this.btnNewButton_24))
 		{
 			this.aktion(btnNewButton_24);
 		}
 
 		
 		/*
 		 * hier wird der neue Zug der Strategie abgefragt und this.aktion mit dem entsprechenden Button,
 		 * also wo die Strategie hin will, ausgefhrt
 		 */
 		if(mode == 2)
 		{
 			try 
 			{
 				ISpielzug neuerZug = strategie1.bewegeStein(gSteine);
 				IBewegung neueIBewegung = neuerZug.bewegeSpielStein();
 				Bewegung neueBewegung = new Bewegung(neueIBewegung.altePosition(), neueIBewegung.neuePosition());
 				this.bewegungInButtons(neueBewegung);
 			} 
 			catch (StrategieException e1) 
 			{
 				e1.printStackTrace();
 			}
 		}
 	}
 		
 	
 	/*
 	 * wenn ein Knopf gedrckt wurde wird diese aktion ausgefhrt
 	 */
 	
 	public void aktion(TransparentButtonFeld lButton)
 	{
 		Position PositionGeklickt = lButton.getPosition();
 		while(!SpielBeendet)
 		{
 
 			int e, x, y;
 			e = posIndexUmrechnen(PositionGeklickt.getEbene());
 			x = posIndexUmrechnen(PositionGeklickt.getX());
 			y = posIndexUmrechnen(PositionGeklickt.getY());
 			Spielstein aktuellerStein = SpielfeldArray[e][x][y];
 			
 			//solange der aktuelle Spieler keine Mhle hat
 			if(hatMuehle != true)
 			{
 				if(Spieler1.getAnzahlZuege() <= Spieler2.getAnzahlZuege())
 				{
 					aktuellerSpieler = Spieler1;
 					passiverSpieler = Spieler2;
 				}
 				else
 				{
 					aktuellerSpieler = Spieler2;
 					passiverSpieler = Spieler1;
 				}
 
 				//erste Phase wenn noch nicht alle Steine gesetzt wurden
 				while(Spieler2.getPhase().equals(EPhase.Setzen)) //(anzahlRunden < 9)
 				{
 					//neue Bewegung erstellen
 					neueBewegung = new Bewegung(null, PositionGeklickt);
 					
 					//verschiedene Prfungen
 					if(pruef.checkSetzen(PositionGeklickt, aktuellerSpieler, passiverSpieler) == true)
 					{
 						this.SpielsteinSetzen(neueBewegung, aktuellerSpieler, lButton);
 						this.verschiedeneAusgaben();
 					}
 					else
 					{
 						System.out.print("checkSetzen ergab: ");
 						System.out.println(pruef.checkSetzen(PositionGeklickt, aktuellerSpieler, passiverSpieler));	
 						return;
 					}
 					
 					//Steht der Stein in einer Mhle
 					if(pruef.checkInMuehle(anzahlRunden, aktuellerSpieler.Steine))
 					{
 						
 						System.out.println(aktuellerSpieler.getName() + textMuehle);
 						System.out.println("Alle gegnerischen Steine stehen in einer Mhle: " + this.alleGegnerSteineInMhle(passiverSpieler));
 						
 						if((pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true && passiverSpieler.getAnzahlZuege() == 9)
 								|| (passiverSpieler.getAnzahlSteine() == 3 && passiverSpieler.getAnzahlZuege() == 9))
 						{
 							SpielBeendet = true;
 							this.aktion(btnNewButton_1);
							return;
 						}
						this.neueMeldung(meldungsZeit, aktuellerSpieler.getName() + textMuehle);
 						hatMuehle = true;
 						panel.repaint();
 						panel_1.repaint();
 						return;
 					}
 					else
 					{
 //						db.zugspeichern(neueBewegung, aktuellerSpieler, false, aktuellerStein);
 					}
 					
 					if(aktuellerSpieler == Spieler2)
 						anzahlRunden++;
 					
 					//neu zeichnen
 					panel.repaint();
 					panel_1.repaint();
 					if(Spieler2.getAnzahlZuege()==9)
 					{
 						new Thread() 
 						{
 							{
 								start(); 
 							}
 							public void run()
 							{
 								try 
 								{ 
 									if(!pruef.checkSpielBeendet(passiverSpieler, aktuellerSpieler))
 									{
 										Spielfeld.neueMeldung(wichtigeMeldungsZeit, textRunde2);
 										sleep(wichtigeMeldungsZeit * 1000);
 										if(aktuellerSpieler.getAnzahlSteine() == 3)
 										{
 											Spielfeld.neueMeldung(wichtigeMeldungsZeit, aktuellerSpieler.getName() + textDarfSpringen);
 											sleep(wichtigeMeldungsZeit * 1000);
 										}
 										if(passiverSpieler.getAnzahlSteine() == 3)
 										{
 											Spielfeld.neueMeldung(wichtigeMeldungsZeit, passiverSpieler.getName() + textDarfSpringen);
 											sleep(wichtigeMeldungsZeit * 1000);
 										}
 									}
									else
									{
										SpielBeendet = true;
										return;
									}
 								}
 								catch ( InterruptedException e ) 
 								{ 
 									e.printStackTrace();
 								} 
 							} 
 						};  //Thread zum warten
 						
 						
 					}
 					
 					if(pruef.checkSpielBeendet(passiverSpieler, aktuellerSpieler) == true && passiverSpieler.getAnzahlZuege() == 9)
 					{
 						SpielBeendet = true;
 						this.aktion(btnNewButton_1);
						return;
 					}
 					if(pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true && passiverSpieler.getAnzahlZuege() == 9)
 					{
 						aktuellerSpieler = passiverSpieler;
 						SpielBeendet = true;
 						this.aktion(btnNewButton_1);
						return;
 					}
 					
 					return;
 				}				
 		
 				//Hier wird der Code ausgefhrt, wenn die erste Phase abgeschlossen ist
 				while(true)				//Spieler1.getPhase().equals(EPhase.Schieben)
 				{
 	
 					//hier der Fall wenn auf einen bereits gelegten Stein gedrckt wird
 					if(hatAltePosition == false)
 					{
 						if(aktuellerStein != null) //nur wenn auf Feld gedrckt wird auf dem auch ein Stein steht
 						{
 							if((aktuellerStein.FarbVergleich(ESpielsteinFarbe.WEISS) && aktuellerSpieler.getSpielerfarbe().equals(ESpielsteinFarbe.WEISS))
 								|| (aktuellerStein.FarbVergleich(ESpielsteinFarbe.SCHWARZ) && aktuellerSpieler.getSpielerfarbe().equals(ESpielsteinFarbe.SCHWARZ)))
 							{
 								altePosition = PositionGeklickt;
 								hatAltePosition = true;
 								ausgewaehlterStein = aktuellerStein;
 								panel.repaint();
 								return;
 							}
 							else
 							{
 								System.out.println("Ein Spieler hat auf einen Stein gedrckt der nicht ihm gehrt");
 								return;
 							}
 						}
 						else
 						{
 							System.out.println("Hier steht kein Stein");
 							return;
 						}
 					}
 					
 					else 		//hier der Fall wenn die alte Position bereits abgespeichert wurde
 					{
 						//den ausgewaehlten Stein wieder zurcksetzen
 						ausgewaehlterStein = null;
 						
 						//neue Bewegung erstellen
 						neueBewegung = new Bewegung(altePosition, PositionGeklickt);
 						System.out.println(neueBewegung);
 						
 						//verschiedene Prfungen
 						if(pruef.checkZug(neueBewegung, aktuellerSpieler, passiverSpieler) == true)
 						{	
 							this.SpielsteinBewegen(neueBewegung, aktuellerSpieler, lButton);
 							this.verschiedeneAusgaben();
 							this.createArrayEigenerIndeizes(aktuellerSpieler);
 							if(pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true)
 							{
 								SpielBeendet = true;
 								this.aktion(btnNewButton_1);
								return;
 							}
 							if(pruef.checkSpielBeendet(passiverSpieler, aktuellerSpieler) == true)
 							{
 								SpielBeendet = true;
 								this.aktion(btnNewButton_1);
								return;
 							}
 						}
 						else
 						{
 							System.out.print("checkZug ergab: ");
 							System.out.println(pruef.checkZug(neueBewegung, aktuellerSpieler, passiverSpieler));
 							panel.repaint();
 							hatAltePosition = false;
 							return;
 						}
 						
 						int e1, x1, y1;
 						e1 = posIndexUmrechnen(PositionGeklickt.getEbene());
 						x1 = posIndexUmrechnen(PositionGeklickt.getX());
 						y1 = posIndexUmrechnen(PositionGeklickt.getY());
 						Spielstein aktuellerStein1 = SpielfeldArray[e1][x1][y1];
 						
 						if(pruef.checkInMuehle(aktuellerStein1.getIndex() , aktuellerSpieler.Steine))
 						{
 							
 							this.verschiedeneAusgaben();
 							
 							hatMuehle = true;
 							hatAltePosition = false;
 							panel.repaint();
 							this.createArrayEigenerIndeizes(aktuellerSpieler);
 							if(pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true 
 									|| passiverSpieler.getAnzahlSteine() == 3)
 							{
 								SpielBeendet = true;
 								this.aktion(btnNewButton_1);
								return;
 							}
 							this.neueMeldung(meldungsZeit, aktuellerSpieler.getName() + textMuehle);
 							return;
 						}
 		
 						if(aktuellerSpieler == Spieler2)
 							anzahlRunden++;
 						
 						panel.repaint();
 						panel_1.repaint();
 						hatAltePosition = false;
 						return;		
 					}
 				}
 
 			}
 			else		//hier der code wenn ein Spieler eine Mhle hat
 			{
 				if(aktuellerStein != null)
 				{
 					//hat der Spieler auf einen anderen Stein gedrckt?
 					if((aktuellerStein.FarbVergleich(ESpielsteinFarbe.SCHWARZ) && aktuellerSpieler.getSpielerfarbe().equals(ESpielsteinFarbe.WEISS))
 							|| (aktuellerStein.FarbVergleich(ESpielsteinFarbe.WEISS) && aktuellerSpieler.getSpielerfarbe().equals(ESpielsteinFarbe.SCHWARZ)))
 					{
 						this.muehle(aktuellerStein, aktuellerSpieler, PositionGeklickt, neueBewegung);
 						if(!SteinKannGeloeschtWerden)	 
 						{
 							System.out.println("Dieser Stein steht in einer Mhle, und kann daher nicht gelscht werden!");
 							SteinKannGeloeschtWerden = true;
 							return;
 						}							
 
 						if(aktuellerSpieler == Spieler2)
 						{
 							anzahlRunden++;
 							entfernteSteineWeiss++;
 						}
 						else
 						{
 							entfernteSteineSchwarz++;
 //							
 						}
 						if(pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true)
 //							{
 //								SpielBeendet = true;
 //								this.aktion(btnNewButton_1);
 //							}
 						if(pruef.checkSpielBeendet(aktuellerSpieler, passiverSpieler) == true && passiverSpieler.getAnzahlZuege() == 9)
 						{
 							aktuellerSpieler = passiverSpieler;
 							SpielBeendet = true;
 							this.aktion(btnNewButton_1);
 						}
 						if(pruef.checkSpielBeendet(passiverSpieler, aktuellerSpieler) == true && passiverSpieler.getAnzahlZuege() >= 9)
 						{
 							SpielBeendet = true;
 							this.aktion(btnNewButton_1);
 						}
 						panel_1.repaint();
 						this.verschiedeneAusgaben();
 						if(Spieler2.getAnzahlZuege()==9)
 						{
 							new Thread() 
 							{
 								{
 									start(); 
 								}
 								public void run()
 								{
 									try 
 									{ 
 										if(!pruef.checkSpielBeendet(passiverSpieler, aktuellerSpieler))
 										{
 											Spielfeld.neueMeldung(wichtigeMeldungsZeit, textRunde2);
 											sleep(wichtigeMeldungsZeit * 1000);
 											if(aktuellerSpieler.getAnzahlSteine() == 3)
 											{
 												Spielfeld.neueMeldung(wichtigeMeldungsZeit, aktuellerSpieler.getName() + textDarfSpringen);
 												sleep(wichtigeMeldungsZeit * 1000);
 											}
 											if(passiverSpieler.getAnzahlSteine() == 3)
 											{
 												Spielfeld.neueMeldung(wichtigeMeldungsZeit, passiverSpieler.getName() + textDarfSpringen);
 												sleep(wichtigeMeldungsZeit * 1000);
 											}
 										}
										else
										{
											SpielBeendet = true;
											return;
										}
 									}
 									catch ( InterruptedException e ) 
 									{ 
 										e.printStackTrace();
 									} 
 								} 
 							};  //Thread zum warten
 						}
 						
 						//wenn duch das Mhle schlagen der passive Spieler nur noch 3 Steine hat wird die Meldung erzeugt, dass dieser jetzt springen darf
 						if(passiverSpieler.getAnzahlSteine() == 3 && passiverSpieler.getAnzahlZuege() > 9)
 							this.neueMeldung(wichtigeMeldungsZeit, aktuellerSpieler.getName() + textDarfSpringen);
 							
 							
 						return;
 					}
 					else //der Spieler hat auf einen seiner eigenen Steine gedrckt
 					{
 						System.out.println("Der Spieler hat auf einen eigenen Stein gedrckt obwohl er eine Mhle hat");
 						return;
 					}
 				}
 				else	//der Spieler hat auf ein leeres Feld gedrckt
 				{
 					return;
 				}
 			}	
 		}
 		
 		Gewonnen gewinnerBild = new Gewonnen(aktuellerSpieler.getName() + textGewonnen);
 		gewinnerBild.setVisible(true);
 //		this.neueMeldung(wichtigeMeldungsZeit, aktuellerSpieler.getName() + textGewonnen);
 		System.out.println("Spieler beendet!");
 		
 	}	
 
 	public void muehle(Spielstein aktuellerStein, Spieler aktuellerSpieler, Position PositionGeklickt, Bewegung neueBewegung)
 	{
 		int e, x, y;
 		e = posIndexUmrechnen(PositionGeklickt.getEbene());
 		x = posIndexUmrechnen(PositionGeklickt.getX());
 		y = posIndexUmrechnen(PositionGeklickt.getY());
 		Spielstein zuLoeschenderStein = SpielfeldArray[e][x][y];
 		
 		
 		// wenn der ausgewhlte Stein nicht in einer Mhle steht oder alle Steine des Gegners in einer Mhle stehen, kann der Stein gelscht werden
 		if(!pruef.checkInMuehle(zuLoeschenderStein.getIndex() , passiverSpieler.Steine) || this.alleGegnerSteineInMhle(passiverSpieler) == true)
 		{
 			SpielfeldArray[e][x][y] = null;
 			passiverSpieler.entferneSpielstein(zuLoeschenderStein.getIndex());
 			hatAltePosition = false;
 			gSteine.remove(zuLoeschenderStein);
 		}
 
 		else
 		{
 			SteinKannGeloeschtWerden = false;
 			return;
 		}
 		
 		
 		panel.repaint();
 		hatMuehle = false;
 		
 //		db.zugspeichern(neueBewegung, aktuellerSpieler, true, zuLoeschenderStein);
 	}
 	
 	
 	public void SpielsteinSetzen(Bewegung neueBewegung, Spieler lSpieler, TransparentButtonFeld lButton)
 	{
 		//Die Position an der der Stein gezeichnet werden soll wird ermittelt 
 		Point pos = lButton.getLocation();
 		xPos = (int)pos.getX()-5;
 		yPos = (int)pos.getY()-3;
 		
 		//Die Bewegung wird an den Spieler weitergegeben 
 		lSpieler.setzeSpielstein(neueBewegung.getNach(), xPos, yPos);
 		
 		//Das Array in dem die Spielsteine gepeichert sind wird gefllt
 		int e, x, y;
 		e = posIndexUmrechnen(neueBewegung.getNach().getEbene());
 		x = posIndexUmrechnen(neueBewegung.getNach().getX());
 		y = posIndexUmrechnen(neueBewegung.getNach().getY());
 		SpielfeldArray[e][x][y] = lSpieler.getSpielstein(anzahlRunden);
 		
 		//Stein wird der Liste mit den aktuellen Steinen auf dem Feld hinzugefgt
 		gSteine.add(lSpieler.getSpielstein(anzahlRunden));
 		
 		//Ausgabe der Bewertung des Zuges ZUM TEST
 //		System.out.println("Dieser Zug wird so bewertet: " + bewertung.bewerteZug(gSteine, neueBewegung, aktuellerSpieler.getAnzahlZuege()));
 	}
 	
 	
 	public void SpielsteinBewegen(Bewegung neueBewegung, Spieler lSpieler, TransparentButtonFeld lButton)
 	{
 		//Die Position an der der Stein gezeichnet werden soll wrd ermittelt
 		Point pos = lButton.getLocation();
 		xPos = (int)pos.getX()-5;
 		yPos = (int)pos.getY()-3;
 		
 		//Die alte Position des Steins wird aus dem Array gelscht und in der Variable aktueller Stein zwischengespeichert
 		int e, x, y;
 		e = posIndexUmrechnen(altePosition.getEbene());
 		x = posIndexUmrechnen(altePosition.getX());
 		y = posIndexUmrechnen(altePosition.getY());
 		Spielstein aktuellerStein = SpielfeldArray[e][x][y];
 		
 		//Die neue Position wird in das Array geschrieben 
 		int a, b, c;
 		a = posIndexUmrechnen(neueBewegung.getNach().getEbene());
 		b = posIndexUmrechnen(neueBewegung.getNach().getX());
 		c = posIndexUmrechnen(neueBewegung.getNach().getY());
 		SpielfeldArray[a][b][c] = aktuellerStein;
 		
 		//Die Bewegung wird an den Spieler weitergegeben
 		lSpieler.bewegeSpielstein(neueBewegung, aktuellerStein.getIndex(), xPos, yPos);	
 		
 		//Stein wird in der Liste mit den aktuellen Steinen auf dem Feld verndert
 		gSteine.set(gSteine.indexOf(SpielfeldArray[e][x][y]), aktuellerStein);
 		
 		//Stein wird aus dem SpielfeldArray gelscht
 		SpielfeldArray[e][x][y] = null;
 		
 		//Ausgabe der Bewertung des Zuges ZUM TEST
 //		System.out.println("Dieser Zug wird so bewertet: " + bewertung.bewerteZug(gSteine, neueBewegung, aktuellerSpieler.getAnzahlZuege()));
 	}
 	
 	//EPositionIndex auf dem Feld wird in int umgerechnet
 	public int posIndexUmrechnen(EPositionIndex lPosition)
 	{
 		if(lPosition.equals(EPositionIndex.Eins))
 			return 0;
 		else if(lPosition.equals(EPositionIndex.Zwei))
 			return 1;
 		else if(lPosition.equals(EPositionIndex.Drei))
 			return 2;
 		else 
 			return 99;
 	}
 	
 	//berprft ob alle gegnerischen Steine in Mhle stehen
 	//True wenn alle Steine in Mhle Stehen, false wenn nicht 
 	public boolean alleGegnerSteineInMhle(Spieler passiverSpieler)
 	{
 		int counter = 0;
 		
 		/*
 		 * das array mit allen Spielsteinen durchlaufen und prfen ob der aktuelle Stein vom passiven Spieler ist, 
 		 * wenn ja dann gucken ob dieser Stein in einer Mhle steht und wenn das auch true ist dann den counter hochsetzen
 		 */
 		for(int i = 0; i <= 2; i++)
         {
         	for(int j = 0; j <= 2; j++)
         	{
         		for(int k = 0; k <= 2; k++)
         		{
         			if(SpielfeldArray[i][j][k] != null)
         			{
             			Spielstein aktuellerStein = SpielfeldArray[i][j][k];
             			if(aktuellerStein.getFarbe() == passiverSpieler.getSpielerfarbe())
             			{
             				if(pruef.checkInMuehle(aktuellerStein.getIndex(), passiverSpieler.Steine))
             					counter++;
             			}
             				
         			}
         		}
         	}
         }
 		
 		//wenn der counter gleich der Anzahl Steine des passiven Spielers ist stehen alle Steine in einer Mhle
 		if(passiverSpieler.getAnzahlSteine() == counter)
 			return true;
 		else
 			return false; 
 	}
 	
 	
 	
 	public static void neueMeldung(final int sekunden, final String meldung)
 	{
 		//hier wird die Position festgelegt wo die meldung erscheinen soll
 		Point pos = panel.getLocationOnScreen();
 		xPos = (int)pos.getX() + 20;
 		yPos = (int)pos.getY() + 200;
 		
 		
 		
 	
 		new Thread() 
 		{
 		      { 
 		    	  start(); 
 		      } 
 		      public void run() 
 		      {
 		        try 
 		        { 
 		        	Anweisung1 probe = new Anweisung1(meldung, xPos, yPos, x, y, gFont);
 					probe.setAlwaysOnTop(true);
 					probe.setVisible(true);
 		        	sleep(sekunden * 1000);
 		        	probe.setVisible(false);
 		        }
 		        catch ( InterruptedException e ) { }
 		      } 
 		};
 	}
 	
 	public void verschiedeneAusgaben()
 	{
 		//verschiedene Ausgaben
 		System.out.print("Spieler1: " + Spieler1.getAnzahlZuege() + " Zge, " + Spieler1.getAnzahlSteine() + " Steine ||  ");
 		System.out.println("Spieler2: " + Spieler2.getAnzahlZuege() + " Zge, " + Spieler2.getAnzahlSteine() + " Steine ||  Anzahl Runden: " + anzahlRunden); 
 		System.out.println("Zug Beendet ----------------------------------------------------------------------------");
 			
 	}
 	
 	public void createArrayEigenerIndeizes(Spieler aktuellerSpieler)
 	{
 		int zaehler = 0;
 		for(int i = 0; i <= 2; i++)
         {
         	for(int j = 0; j <= 2; j++)
         	{
         		for(int k = 0; k <= 2; k++)
         		{
         			if(SpielfeldArray[i][j][k] != null)
         			{
             			Spielstein aktuellerStein = SpielfeldArray[i][j][k];
             			if(aktuellerStein.getFarbe() == aktuellerSpieler.getSpielerfarbe())
             			{
             				Indizes[zaehler] = aktuellerStein.getIndex();
             				zaehler++;
             			}
             				
         			}
         		}
         	}
         }
 	}
 	
 	//Methode die fr die Bewegung die die Strategie zurckliefert die entsprechenenden Button-klicks simuliert
 	public void bewegungInButtons(Bewegung neueBewegung)
 	{
 		if(neueBewegung.getVon() == null)
 		{
 			
 			if(btnNewButton_1.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_1.doClick(); 								//this.actionPerformed(new ActionEvent(btnNewButton_1, 1001, btnNewButton_1.)));
 			if(btnNewButton_2.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_2.doClick(); 
 			if(btnNewButton_3.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_3.doClick(); 
 			if(btnNewButton_4.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_4.doClick(); 
 			if(btnNewButton_5.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_5.doClick(); 
 			if(btnNewButton_6.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_6.doClick(); 
 			if(btnNewButton_7.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_7.doClick(); 
 			if(btnNewButton_8.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_8.doClick(); 
 			if(btnNewButton_9.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_9.doClick(); 
 			if(btnNewButton_10.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_10.doClick(); 
 			if(btnNewButton_11.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_11.doClick(); 
 			if(btnNewButton_12.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_12.doClick(); 
 			if(btnNewButton_13.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_13.doClick(); 
 			if(btnNewButton_14.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_14.doClick(); 
 			if(btnNewButton_15.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_15.doClick(); 
 			if(btnNewButton_16.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_16.doClick(); 
 			if(btnNewButton_17.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_17.doClick(); 
 			if(btnNewButton_18.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_18.doClick(); 
 			if(btnNewButton_19.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_19.doClick(); 
 			if(btnNewButton_20.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_20.doClick(); 
 			if(btnNewButton_21.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_21.doClick(); 
 			if(btnNewButton_22.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_22.doClick(); 
 			if(btnNewButton_23.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_23.doClick(); 
 			if(btnNewButton_24.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_24.doClick(); 
 			
 		}
 		else
 		{
 			//VON Bewegung in button klick umgewandelt
 			if(btnNewButton_1.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_1.doClick(); 								//this.actionPerformed(new ActionEvent(btnNewButton_1, 1001, btnNewButton_1.)));
 			if(btnNewButton_2.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_2.doClick(); 
 			if(btnNewButton_3.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_3.doClick(); 
 			if(btnNewButton_4.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_4.doClick(); 
 			if(btnNewButton_5.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_5.doClick(); 
 			if(btnNewButton_6.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_6.doClick(); 
 			if(btnNewButton_7.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_7.doClick(); 
 			if(btnNewButton_8.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_8.doClick(); 
 			if(btnNewButton_9.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_9.doClick(); 
 			if(btnNewButton_10.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_10.doClick(); 
 			if(btnNewButton_11.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_11.doClick(); 
 			if(btnNewButton_12.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_12.doClick(); 
 			if(btnNewButton_13.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_13.doClick(); 
 			if(btnNewButton_14.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_14.doClick(); 
 			if(btnNewButton_15.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_15.doClick(); 
 			if(btnNewButton_16.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_16.doClick(); 
 			if(btnNewButton_17.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_17.doClick(); 
 			if(btnNewButton_18.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_18.doClick(); 
 			if(btnNewButton_19.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_19.doClick(); 
 			if(btnNewButton_20.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_20.doClick(); 
 			if(btnNewButton_21.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_21.doClick(); 
 			if(btnNewButton_22.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_22.doClick(); 
 			if(btnNewButton_23.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_23.doClick(); 
 			if(btnNewButton_24.vergleichePosition(neueBewegung.getVon()) == true)
 				btnNewButton_24.doClick(); 
 			
 			
 			
 			//nach button klick simuliert
 			if(btnNewButton_1.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_1.doClick(); 								//this.actionPerformed(new ActionEvent(btnNewButton_1, 1001, btnNewButton_1.)));
 			if(btnNewButton_2.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_2.doClick(); 
 			if(btnNewButton_3.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_3.doClick(); 
 			if(btnNewButton_4.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_4.doClick(); 
 			if(btnNewButton_5.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_5.doClick(); 
 			if(btnNewButton_6.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_6.doClick(); 
 			if(btnNewButton_7.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_7.doClick(); 
 			if(btnNewButton_8.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_8.doClick(); 
 			if(btnNewButton_9.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_9.doClick(); 
 			if(btnNewButton_10.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_10.doClick(); 
 			if(btnNewButton_11.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_11.doClick(); 
 			if(btnNewButton_12.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_12.doClick(); 
 			if(btnNewButton_13.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_13.doClick(); 
 			if(btnNewButton_14.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_14.doClick(); 
 			if(btnNewButton_15.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_15.doClick(); 
 			if(btnNewButton_16.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_16.doClick(); 
 			if(btnNewButton_17.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_17.doClick(); 
 			if(btnNewButton_18.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_18.doClick(); 
 			if(btnNewButton_19.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_19.doClick(); 
 			if(btnNewButton_20.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_20.doClick(); 
 			if(btnNewButton_21.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_21.doClick(); 
 			if(btnNewButton_22.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_22.doClick(); 
 			if(btnNewButton_23.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_23.doClick(); 
 			if(btnNewButton_24.vergleichePosition(neueBewegung.getNach()) == true)
 				btnNewButton_24.doClick(); 
 		}
 	}
 	
 
 		
 	
 	
 }
 
 
 
