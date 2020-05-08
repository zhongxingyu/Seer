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
 
 import de.dhbw.muehle_api.*;
 import de.dhbw.muehle_spiel.Bewegung;
 import de.dhbw.muehle_spiel.EPhase;
 import de.dhbw.muehle_spiel.Pruefung;
 import de.dhbw.muehle_spiel.Spieler;
 import de.dhbw.muehle_spiel.Spielstein;
 
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import net.miginfocom.swing.MigLayout;
 
 public class Spielfeld extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static JPanel contentPane;
 	
 	//Die Buttons werden initialisiert
 	TransparentButtonFeld btnNewButton_1, btnNewButton_4, btnNewButton_6, btnNewButton_9, 
 							btnNewButton_10, btnNewButton_13, btnNewButton_15, btnNewButton_18,
 							btnNewButton_19, btnNewButton_22, btnNewButton_23, btnNewButton_24,
 							btnNewButton_26, btnNewButton_27, btnNewButton_28, btnNewButton_31,
 							btnNewButton_32, btnNewButton_33, btnNewButton_37, btnNewButton_39,
 							btnNewButton_41, btnNewButton_43, btnNewButton_46, btnNewButton_49;
 	
 	//Die Spieler werden initialisiert 
 	Spieler Spieler1 = new Spieler(ESpielsteinFarbe.WEISS);
 	Spieler Spieler2 = new Spieler(ESpielsteinFarbe.SCHWARZ); 
 
 	
 	JPanel panel;
 	
 	Spielstein[][][] SpielfeldArray = new Spielstein[3][3][3];
 	private JMenuBar menuBar;
 	private JMenu mnNewMenu;
 	private JMenuItem mntmSpielBeenden;
 	private JMenuItem mntmNeuesSpiel;
 	private JMenuItem mntmAnleitung;
 	private JPanel panel_1;
 
 	
 	//xPos und yPos sind die reellen Positionen auf dem Feld, 
 	//anzahlRunden zhlt die Anzahl der Runden
 	int xPos, yPos, anzahlRunden;
 	int zaehler1 = 1;
 	int zaehler2 = 0;
 
 	
 	private boolean hatMuehle = false;
 	
 	private Spieler aktuellerSpieler, passiverSpieler;
 	
 	private Bewegung neueBewegung;
 	
 	//static Database db = new Database();
 	
 	private boolean SteinKannGeloeschtWerden = true;
 	
 	private String textMuehle = " hat eine Mhle!";
 	private String textNeuesSpiel = "Neues Spiel -- Weiss beginnt!";
 	private String textRunde2 = "Ab jetzt: Steine ziehen!";
 	private static String textSpielName = "Mhle Spiel";
 	
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
 	
 	Pruefung pruef = new Pruefung();
 	
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
 					Spielfeld frame = new Spielfeld();
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
 				final int xPos = (int)(pos.getX() + frame.panel.getWidth()/2 - 205);
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
 				        	Anweisung1 probe = new Anweisung1(meldung, xPos, yPos);
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
 	 */
 	public Spielfeld() 
 	{
 		//Name der in dem Fenster angezeigt wird
 		super(textSpielName);
 		
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
 		
 		mnNewMenu = new JMenu("Men\u00FC");
 		menuBar.add(mnNewMenu);
 		
 		//Menleiste Neues Spiel
 		mntmNeuesSpiel = new JMenuItem("Neues Spiel");
 		mntmNeuesSpiel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				db.lschetb("protokoll");
 				dispose();
 				JFrame neuesSpiel = new Spielfeld();
 				neuesSpiel.setVisible(true);
 			}
 		});
 		mnNewMenu.add(mntmNeuesSpiel);
 		
 		//Menleiste Spiel Beenden
 		mntmSpielBeenden = new JMenuItem("Spiel beenden");
 		mntmSpielBeenden.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				final BestaetigungBeenden frageBeenden = new BestaetigungBeenden();
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
 		});
 		mnNewMenu.add(mntmSpielBeenden);
 		
 		//Menleiste Anleitung
 		mntmAnleitung = new JMenuItem("Anleitung");
 		mntmAnleitung.addActionListener(new ActionListener() {
 			@SuppressWarnings("deprecation")
 			public void actionPerformed(ActionEvent e) {
 				JFrame anleitung = new Anleitung();
 				anleitung.show(true);
 			}
 		});
 		mnNewMenu.add(mntmAnleitung);
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
 				 
 			
                 Image spielfeld = Toolkit.getDefaultToolkit().getImage(  
                           Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIlinks.png"));  
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
 		                					if(hatMuehle && aktuellerSpieler.equals(Spieler2) && !pruef.checkInMuehle(aktuellerStein.getIndex(), Spieler1.Steine))
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
 		                					if(hatMuehle && aktuellerSpieler.equals(Spieler1) && !pruef.checkInMuehle(aktuellerStein.getIndex(), Spieler2.Steine))
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
 		
 		
 		btnNewButton_18 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_18.addActionListener(this);
 		
 		//ab hier werden die ganzen knpfe definiert und mit dem actionlistener verknpft
 		btnNewButton_1 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_1.addActionListener(this);
 		panel.setLayout(new MigLayout("", "[5][66.15][25][66.15][25][60][2][60][2.00][60][25][66.15][25][66.15]", "[20][66.15px][35][66.15][37.48][60][5][60][2.2][60][37.48][66.15][40.79][66.15][25]"));
 		panel.add(btnNewButton_1, "cell 1 1,grow");
 		
 		JLabel lblNewLabel = new JLabel("");
 		panel.add(lblNewLabel, "cell 3 1,grow");
 		
 		JLabel label = new JLabel("");
 		panel.add(label, "cell 5 1,grow");
 		
 		btnNewButton_9 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_9.addActionListener(this);
 		
 		btnNewButton_6 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_6.addActionListener(this);
 		
 		btnNewButton_4 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_4.addActionListener(this);
 		panel.add(btnNewButton_4, "cell 7 1,grow");
 		
 		JLabel lblNewLabel_1 = new JLabel("");
 		panel.add(lblNewLabel_1, "cell 9 1,grow");
 		
 		JLabel lblNewLabel_2 = new JLabel("");
 		panel.add(lblNewLabel_2, "cell 11 1,grow");
 		panel.add(btnNewButton_6, "cell 13 1,grow");
 		
 		JLabel label_1 = new JLabel("");
 		panel.add(label_1, "cell 1 3,grow");
 		panel.add(btnNewButton_9, "cell 3 3,grow");
 		
 		btnNewButton_10 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_10.addActionListener(this);
 		
 		JLabel label_2 = new JLabel("");
 		panel.add(label_2, "cell 5 3,grow");
 		panel.add(btnNewButton_10, "cell 7 3,grow");
 		
 		JLabel lblNewLabel_3 = new JLabel("");
 		panel.add(lblNewLabel_3, "cell 9 3,grow");
 		
 		btnNewButton_13 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_13.addActionListener(this);
 		panel.add(btnNewButton_13, "cell 11 3,grow");
 		
 		JLabel label_3 = new JLabel("");
 		panel.add(label_3, "cell 13 3,grow");
 		
 		JLabel label_4 = new JLabel("");
 		panel.add(label_4, "cell 1 5,grow");
 		
 		JLabel label_5 = new JLabel("");
 		panel.add(label_5, "cell 3 5,grow");
 		
 		btnNewButton_15 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_15.addActionListener(this);
 		panel.add(btnNewButton_15, "cell 5 5,grow");
 		panel.add(btnNewButton_18, "cell 7 5,grow");
 		
 		btnNewButton_31 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_31.addActionListener(this);
 		
 		btnNewButton_22 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_22.addActionListener(this);
 		
 		btnNewButton_19 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_19.addActionListener(this);
 		panel.add(btnNewButton_19, "cell 9 5,grow");
 		
 		JLabel label_6 = new JLabel("");
 		panel.add(label_6, "cell 11 5,grow");
 		
 		JLabel label_7 = new JLabel("");
 		panel.add(label_7, "cell 13 5,grow");
 		panel.add(btnNewButton_22, "cell 1 7,grow");
 		
 		btnNewButton_26 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_26.addActionListener(this);
 		
 		btnNewButton_23 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_23.addActionListener(this);
 		panel.add(btnNewButton_23, "cell 3 7,grow");
 		
 		btnNewButton_24 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_24.addActionListener(this);
 		panel.add(btnNewButton_24, "cell 5 7,grow");
 		
 		JLabel label_8 = new JLabel("");
 		panel.add(label_8, "cell 7 7,grow");
 		panel.add(btnNewButton_26, "cell 9 7,grow");
 		
 		btnNewButton_28 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_28.addActionListener(this);
 		
 		btnNewButton_27 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_27.addActionListener(this);
 		panel.add(btnNewButton_27, "cell 11 7,grow");
 		panel.add(btnNewButton_28, "cell 13 7,grow");
 		
 		JLabel label_9 = new JLabel("");
 		panel.add(label_9, "cell 1 9,grow");
 		
 		JLabel label_10 = new JLabel("");
 		panel.add(label_10, "cell 3 9,grow");
 		panel.add(btnNewButton_31, "cell 5 9,grow");
 		
 		btnNewButton_46 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_46.addActionListener(this);
 		
 		btnNewButton_37 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_37.addActionListener(this);
 		
 		btnNewButton_32 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_32.addActionListener(this);
 		panel.add(btnNewButton_32, "cell 7 9,grow");
 		
 		btnNewButton_33 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_33.addActionListener(this);
 		panel.add(btnNewButton_33, "cell 9 9,grow");
 		
 		JLabel label_11 = new JLabel("");
 		panel.add(label_11, "cell 11 9,grow");
 		
 		JLabel label_12 = new JLabel("");
 		panel.add(label_12, "cell 13 9,grow");
 		
 		JLabel label_13 = new JLabel("");
 		panel.add(label_13, "cell 1 11,grow");
 		panel.add(btnNewButton_37, "cell 3 11,grow");
 		
 		JLabel label_14 = new JLabel("");
 		panel.add(label_14, "cell 5 11,grow");
 		
 		btnNewButton_43 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_43.addActionListener(this);
 		
 		btnNewButton_41 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_41.addActionListener(this);
 		
 		btnNewButton_39 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_39.addActionListener(this);
 		panel.add(btnNewButton_39, "cell 7 11,grow");
 		
 		JLabel label_15 = new JLabel("");
 		panel.add(label_15, "cell 9 11,grow");
 		panel.add(btnNewButton_41, "cell 11 11,grow");
 		
 		JLabel label_16 = new JLabel("");
 		panel.add(label_16, "cell 13 11,grow");
 		panel.add(btnNewButton_43, "cell 1 13,grow");
 		
 		JLabel label_17 = new JLabel("");
 		panel.add(label_17, "cell 3 13,grow");
 		
 		JLabel label_18 = new JLabel("");
 		panel.add(label_18, "cell 5 13,grow");
 		panel.add(btnNewButton_46, "cell 7 13,grow");
 		
 		JLabel label_19 = new JLabel("");
 		panel.add(label_19, "cell 9 13,grow");
 		
 		JLabel label_20 = new JLabel("");
 		panel.add(label_20, "cell 11 13,grow");
 		
 		btnNewButton_49 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_49.addActionListener(this);
 		panel.add(btnNewButton_49, "cell 13 13,grow");
 		
 		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		
 		
 		
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
 				Image spielfeldRechts = Toolkit.getDefaultToolkit().getImage(  
                         Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUIrechts.png"));  
 				g.drawImage(spielfeldRechts, 0, 0, this.getWidth(), this.getHeight(), this);  
 				
 				//hier wird das Verhltnis festgelet, in dem die Steine zum Spielfeld stehen (gre)
 				int breite = panel.getWidth()/8;
 				int hoehe = panel.getWidth()/8;
 				
 				//Beschriftung der Felder
 				g.drawString("Spieler1: ", xPosOben+15, yPosOben+35);
				g.drawString("Spieler2: ", xPosUnten+15, yPosUnten+42);
 				
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
 		else if(obj.equals(this.btnNewButton_4))
 		{
 			this.aktion(btnNewButton_4);
 		}
 		else if(obj.equals(this.btnNewButton_6))
 		{
 			this.aktion(btnNewButton_6);
 		}
 		else if(obj.equals(this.btnNewButton_9))
 		{
 			this.aktion(btnNewButton_9);
 		}
 		else if(obj.equals(this.btnNewButton_10))
 		{
 			this.aktion(btnNewButton_10);
 		}
 		else if(obj.equals(this.btnNewButton_13))
 		{
 			this.aktion(btnNewButton_13);
 		}
 		else if(obj.equals(this.btnNewButton_15))
 		{
 			this.aktion(btnNewButton_15);
 		}
 		else if(obj.equals(this.btnNewButton_18))
 		{
 			this.aktion(btnNewButton_18);
 		}
 		else if(obj.equals(this.btnNewButton_19))
 		{
 			this.aktion(btnNewButton_19);
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
 		else if(obj.equals(this.btnNewButton_26))
 		{
 			this.aktion(btnNewButton_26);
 		}
 		else if(obj.equals(this.btnNewButton_27))
 		{
 			this.aktion(btnNewButton_27);
 		}
 		else if(obj.equals(this.btnNewButton_28))
 		{
 			this.aktion(btnNewButton_28);
 		}
 		else if(obj.equals(this.btnNewButton_31))
 		{
 			this.aktion(btnNewButton_31);
 		}
 		else if(obj.equals(this.btnNewButton_32))
 		{
 			this.aktion(btnNewButton_32);
 		}
 		else if(obj.equals(this.btnNewButton_33))
 		{
 			this.aktion(btnNewButton_33);
 		}
 		else if(obj.equals(this.btnNewButton_37))
 		{
 			this.aktion(btnNewButton_37);
 		}
 		else if(obj.equals(this.btnNewButton_39))
 		{
 			this.aktion(btnNewButton_39);
 		}
 		else if(obj.equals(this.btnNewButton_41))
 		{
 			this.aktion(btnNewButton_41);
 		}
 		else if(obj.equals(this.btnNewButton_43))
 		{
 			this.aktion(btnNewButton_43);
 		}
 		else if(obj.equals(this.btnNewButton_46))
 		{
 			this.aktion(btnNewButton_46);
 		}
 		else if(obj.equals(this.btnNewButton_49))
 		{
 			this.aktion(btnNewButton_49);
 		}
 	}
 		
 	
 	/*
 	 * wenn ein Knopf gedrckt wurde wird diese aktion ausgefhrt
 	 */
 	
 	public void aktion(TransparentButtonFeld lButton)
 	{
 		Position PositionGeklickt = lButton.getPosition();
 		while(!this.SpielBeendet())
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
 						this.neueMeldung(meldungsZeit, aktuellerSpieler.SpielsteinFarbeAsString() + textMuehle);
 						System.out.println(aktuellerSpieler.SpielsteinFarbeAsString() + textMuehle);
 						
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
 						this.neueMeldung(wichtigeMeldungsZeit, textRunde2);
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
 							this.neueMeldung(meldungsZeit, aktuellerSpieler.SpielsteinFarbeAsString() + textMuehle);
 							this.verschiedeneAusgaben();
 							
 							hatMuehle = true;
 							hatAltePosition = false;
 							panel.repaint();
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
 						if(!SteinKannGeloeschtWerden && passiverSpieler.getPhase()!=EPhase.Springen)	 	//g			
 						{
 							System.out.println("Dieser Stein steht in einer Mhle, und kann daher nicht gelscht werden!");
 							SteinKannGeloeschtWerden = true;
 							return;
 						}
 						else
 							
 
 						if(aktuellerSpieler == Spieler2)
 						{
 							anzahlRunden++;
 							entfernteSteineWeiss++;
 						}
 						else
 							entfernteSteineSchwarz++;
 						
 						panel_1.repaint();
 						this.verschiedeneAusgaben();
 						if(Spieler2.getAnzahlZuege() == 9)
 							this.neueMeldung(wichtigeMeldungsZeit, textRunde2);
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
 	}
 
 	public void muehle(Spielstein aktuellerStein, Spieler aktuellerSpieler, Position PositionGeklickt, Bewegung neueBewegung)
 	{
 		int e, x, y;
 		e = posIndexUmrechnen(PositionGeklickt.getEbene());
 		x = posIndexUmrechnen(PositionGeklickt.getX());
 		y = posIndexUmrechnen(PositionGeklickt.getY());
 		Spielstein zuLoeschenderStein = SpielfeldArray[e][x][y];
 		
 		if(!pruef.checkInMuehle(zuLoeschenderStein.getIndex() , passiverSpieler.Steine))
 		{
 			SpielfeldArray[e][x][y] = null;
 			passiverSpieler.entferneSpielstein(zuLoeschenderStein.getIndex());
 			hatAltePosition = false;
 		}
 		else if (pruef.checkInMuehle(zuLoeschenderStein.getIndex() , passiverSpieler.Steine) && passiverSpieler.getPhase()==EPhase.Springen) 	//g
 		{
 			SpielfeldArray[e][x][y] = null;
 			passiverSpieler.entferneSpielstein(zuLoeschenderStein.getIndex());
 			hatAltePosition = false;
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
 		SpielfeldArray[e][x][y] = null;
 		
 		//Die neue Position wird in das Array geschrieben 
 		int a, b, c;
 		a = posIndexUmrechnen(neueBewegung.getNach().getEbene());
 		b = posIndexUmrechnen(neueBewegung.getNach().getX());
 		c = posIndexUmrechnen(neueBewegung.getNach().getY());
 		SpielfeldArray[a][b][c] = aktuellerStein;
 		
 		//Die Bewegung wird an den Spieler weitergegeben
 		lSpieler.bewegeSpielstein(neueBewegung, aktuellerStein.getIndex(), xPos, yPos);	
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
		for(int i = 1; i < passiverSpieler.getAnzahlSteine(); i++)
		{
			pruef.checkInMuehle(i, passiverSpieler.Steine);
			counter++;
		}
		if(passiverSpieler.getAnzahlSteine() == counter)
			return true;
		else
			return false; 
	}
	
 	//kleiner Test zu Spiel beendet
 	public boolean SpielBeendet()
 	{
 		if(Spieler1.getAnzahlZuege() > 9 && Spieler1.getAnzahlSteine() < 3)
 		{
 			System.out.println("Spieler 1 hat verloren");
 			return true;
 		}
 		else if(Spieler2.getAnzahlZuege() > 9 && Spieler2.getAnzahlSteine() < 3)
 		{
 			System.out.println("Spieler 2 hat verloren");
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public void neueMeldung(final int sekunden, final String meldung)
 	{
 		//hier wird die Position festgelegt wo die meldung erscheinen soll
 		Point pos = panel.getLocationOnScreen();
 		xPos = (int)pos.getX() +  panel.getWidth()/2 - 205;
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
 		        	Anweisung1 probe = new Anweisung1(meldung, xPos, yPos);
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
 
 		
 }
 
 
