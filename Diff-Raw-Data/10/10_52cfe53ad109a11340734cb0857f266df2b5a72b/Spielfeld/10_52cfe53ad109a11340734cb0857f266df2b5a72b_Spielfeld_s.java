 package de.dhbw.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.Color;
 
 import javax.swing.JLabel;
 
 import de.dhbw.gui.BestaetigungBeenden;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.concurrent.TimeUnit;
 
 import de.dhbw.muehle_api.*;
 import de.dhbw.muehle_spiel.Bewegung;
 import de.dhbw.muehle_spiel.Database;
 import de.dhbw.muehle_spiel.Pruefung;
 import de.dhbw.muehle_spiel.Spieler;
 import de.dhbw.muehle_spiel.Spielstein;
 
 import javax.swing.ImageIcon;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
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
 	private JLabel lblNewLabel_4;
 	private JLabel lblNewLabel_5;
 	
 	//xPos und yPos sind die reellen Positionen auf dem Feld, 
 	//anzahlRunden zhlt die Anzahl der Runden
 	int xPos, yPos, anzahlRunden;
 	int zaehler1 = 1;
 	int zaehler2 = 0;
 	
 	private boolean rundeVorbei = false;
 	
 	private boolean hatMuehle = false;
 	
 	private boolean wurdeBewegt = false;
 	
 	private Spieler aktuellerSpieler, passiverSpieler;
 	
 	private Bewegung neueBewegung;
 	
 	static Database db = new Database();
 	
 	private boolean neuesSpielFlag = false;
 	
 	private boolean SteinKannGeloeschtWerden = true;
 	
 	private String textSpielerWechsel = " ist dran!";
 	private String textMuehle = " hat eine Mhle!";
 	private String textNeuesSpiel = "Neues Spiel -- Weiss beginnt!";
 	
 	private int meldungsZeit = 1;
 	
 	//Alte Position wenn man zieht und die variable ob der aktuelle klick schon das neue setzen ist
 	Position altePosition; 
 	boolean hatAltePosition;
 	
 
 	
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
 					 this.neueMeldung(frame.meldungsZeit, frame.textNeuesSpiel, frame);
 				} catch (Exception e) 
 				{
 					e.printStackTrace();
 				}
 			}
 
 			private void neueMeldung(final int sekunden, final String meldung, Spielfeld frame) {
 				//hier wird die Position festgelegt wo die meldung erscheinen soll
 				Point pos = frame.contentPane.getLocationOnScreen();
 				final int xPos = (int)pos.getX() + (contentPane.getWidth() / 3);
 				final int yPos = (int)pos.getY() + 200;
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
 		});
 	}
 
 	private static void exitForm(WindowEvent evt) 
 	{
 		db.lschetb("protokoll");
         System.exit(0);
     }
 	
 	/**
 	 * Das JPanel wird erzeugt
 	 * Die Buttons werden instanziiert und mit dem action listener verknpft
 	 */
 	public Spielfeld() 
 	{
 		setBounds(800, 0, 1074, 900);
 		
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
 		
 		mntmSpielBeenden = new JMenuItem("Spiel beenden");
 		mntmSpielBeenden.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				final BestaetigungBeenden frageBeenden = new BestaetigungBeenden();
 				frageBeenden.setListener( new BestaetigungBeenden.BestatigungsListener() {
 					
 					@Override
 					public void onOK() {
 						
 						Spielfeld.this.dispose();
 						frageBeenden.dispose();
 						db.lschetb("protokoll");
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
 		
 		mntmNeuesSpiel = new JMenuItem("neues Spiel");
 		mntmNeuesSpiel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				db.lschetb("protokoll");
 				dispose();
 				JFrame neuesSpiel = new Spielfeld();
 				neuesSpiel.setVisible(true);
 			}
 		});
 		
 		
 			
 		
 		mnNewMenu.add(mntmNeuesSpiel);
 		
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
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
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
                           Spielfeld.class.getResource("/de/dhbw/images/Spielbrett_GUI.png"));  
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
                 				int breite = (int) ((int)spielfeld.getWidth(this)/5);
                 				int hoehe = (int) ((int)spielfeld.getHeight(this)/5);
                 				
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
 	                						g.drawImage(SteinWeiss,  xPosi , yPosi , breite+10, hoehe+10, this);
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
 	                					g.drawImage(SteinSchwarz,  xPosi , yPosi , breite, hoehe, this);
 	                				else
 	                				{
 	                					if(hatMuehle && aktuellerSpieler.equals(Spieler1) && !pruef.checkInMuehle(aktuellerStein.getIndex(), Spieler2.Steine))
 	                						g.drawImage(SteinSchwarz,  xPosi , yPosi , breite+10, hoehe+10, this);
 	                					else
 	                						g.drawImage(transparentSteinSchwarz,  xPosi , yPosi , breite, hoehe, this);
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
     				
     				//hier wird das Verhltnis festgelet, in dem die Steine zum Spielfeld stehen (gre)
     				int breite = (int) ((int)spielfeld.getWidth(this)/5);
     				int hoehe = (int) ((int)spielfeld.getHeight(this)/5);
     				
     				if(aktuellerStein.FarbVergleich(ESpielsteinFarbe.WEISS))
         				g.drawImage(SteinWeiss,  xPosi , yPosi , breite + 10, hoehe + 10, this);
     				else
     					g.drawImage(SteinSchwarz,  xPosi , yPosi , breite + 10, hoehe + 10, this);
         			
                 	
                 }
             }
 
 
 		};  
 		
 		GridLayout gl = new GridLayout(7,7,15,15);
 
 		//panel wird hinzugefgt
 		contentPane.add(panel, BorderLayout.CENTER);
 		panel.setLayout(gl);
 		
 		//ab hier werden die ganzen knpfe definiert und mit dem actionlistener verknpft
 		btnNewButton_1 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_1.addActionListener(this);
 		panel.add(btnNewButton_1);
 		
 		JLabel lblNewLabel = new JLabel("");
 		panel.add(lblNewLabel);
 		
 		JLabel label = new JLabel("");
 		panel.add(label);
 		
 		btnNewButton_4 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_4.addActionListener(this);
 		panel.add(btnNewButton_4);
 		
 		JLabel lblNewLabel_1 = new JLabel("");
 		panel.add(lblNewLabel_1);
 		
 		JLabel lblNewLabel_2 = new JLabel("");
 		panel.add(lblNewLabel_2);
 		
 		btnNewButton_6 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_6.addActionListener(this);
 		panel.add(btnNewButton_6);
 		
 		JLabel label_1 = new JLabel("");
 		panel.add(label_1);
 		
 		btnNewButton_9 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_9.addActionListener(this);
 		panel.add(btnNewButton_9);
 		
 		JLabel label_2 = new JLabel("");
 		panel.add(label_2);
 		
 		btnNewButton_10 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_10.addActionListener(this);
 		panel.add(btnNewButton_10);
 		
 		JLabel lblNewLabel_3 = new JLabel("");
 		panel.add(lblNewLabel_3);
 		
 		btnNewButton_13 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_13.addActionListener(this);
 		panel.add(btnNewButton_13);
 		
 		JLabel label_3 = new JLabel("");
 		panel.add(label_3);
 		
 		JLabel label_4 = new JLabel("");
 		panel.add(label_4);
 		
 		JLabel label_5 = new JLabel("");
 		panel.add(label_5);
 		
 		btnNewButton_15 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Drei));
 		btnNewButton_15.addActionListener(this);
 		panel.add(btnNewButton_15);
 		
 		btnNewButton_18 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Drei));
 		btnNewButton_18.addActionListener(this);
 		panel.add(btnNewButton_18);
 		
 		btnNewButton_19 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Drei));
 		btnNewButton_19.addActionListener(this);
 		panel.add(btnNewButton_19);
 		
 		JLabel label_6 = new JLabel("");
 		panel.add(label_6);
 		
 		JLabel label_7 = new JLabel("");
 		panel.add(label_7);
 		
 		btnNewButton_22 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_22.addActionListener(this);
 		panel.add(btnNewButton_22);
 		
 		btnNewButton_23 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_23.addActionListener(this);
 		panel.add(btnNewButton_23);
 		
 		btnNewButton_24 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Zwei));
 		btnNewButton_24.addActionListener(this);
 		panel.add(btnNewButton_24);
 		
 		JLabel label_8 = new JLabel("");
 		panel.add(label_8);
 		
 		btnNewButton_26 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_26.addActionListener(this);
 		panel.add(btnNewButton_26);
 		
 		btnNewButton_27 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_27.addActionListener(this);
 		panel.add(btnNewButton_27);
 		
 		btnNewButton_28 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Zwei));
 		btnNewButton_28.addActionListener(this);
 		panel.add(btnNewButton_28);
 		
 		JLabel label_9 = new JLabel("");
 		panel.add(label_9);
 		
 		JLabel label_10 = new JLabel("");
 		panel.add(label_10);
 		
 		btnNewButton_31 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_31.addActionListener(this);
 		panel.add(btnNewButton_31);
 		
 		btnNewButton_32 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_32.addActionListener(this);
 		panel.add(btnNewButton_32);
 		
 		btnNewButton_33 = new TransparentButtonFeld("", new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_33.addActionListener(this);
 		panel.add(btnNewButton_33);
 		
 		JLabel label_11 = new JLabel("");
 		panel.add(label_11);
 		
 		JLabel label_12 = new JLabel("");
 		panel.add(label_12);
 		
 		JLabel label_13 = new JLabel("");
 		panel.add(label_13);
 		
 		btnNewButton_37 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_37.addActionListener(this);
 		panel.add(btnNewButton_37);
 		
 		JLabel label_14 = new JLabel("");
 		panel.add(label_14);
 		
 		btnNewButton_39 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_39.addActionListener(this);
 		panel.add(btnNewButton_39);
 		
 		JLabel label_15 = new JLabel("");
 		panel.add(label_15);
 		
 		btnNewButton_41 = new TransparentButtonFeld("", new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_41.addActionListener(this);
 		panel.add(btnNewButton_41);
 		
 		JLabel label_16 = new JLabel("");
 		panel.add(label_16);
 		
 		btnNewButton_43 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Eins));
 		btnNewButton_43.addActionListener(this);
 		panel.add(btnNewButton_43);
 		
 		JLabel label_17 = new JLabel("");
 		panel.add(label_17);
 		
 		JLabel label_18 = new JLabel("");
 		panel.add(label_18);
 		
 		btnNewButton_46 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Zwei, EPositionIndex.Eins));
 		btnNewButton_46.addActionListener(this);
 		panel.add(btnNewButton_46);
 		
 		JLabel label_19 = new JLabel("");
 		panel.add(label_19);
 		
 		JLabel label_20 = new JLabel("");
 		panel.add(label_20);
 		
 		btnNewButton_49 = new TransparentButtonFeld("", new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Eins));
 		btnNewButton_49.addActionListener(this);
 		panel.add(btnNewButton_49);
 		
 		panel_1 = new JPanel();
 		contentPane.add(panel_1, BorderLayout.EAST);
 		
 		lblNewLabel_4 = new JLabel("testtest");
 		panel_1.add(lblNewLabel_4);
 		
 		
 		lblNewLabel_5 = new JLabel("New label");
 		panel_1.add(lblNewLabel_5);
 		
 		 db.erzeuge_p();
 		 
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
 		
 		//verschiedene Ausgaben
 		
 		System.out.print("Spieler1: " + Spieler1.getAnzahlZuege() + "  ||  ");
 		System.out.println("Spieler2: " + Spieler2.getAnzahlZuege() + "  ||  Anzahl Runden: " + anzahlRunden); 
 		System.out.println("Zug Beendet ----------------------------------------------------------------------------");
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
 				while(Spieler2.getAnzahlZuege() < 9) //(anzahlRunden < 9)
 				{
 					//neue Bewegung erstellen
 					neueBewegung = new Bewegung(null, PositionGeklickt);
 					
 					//verschiedene Prfungen
 					if(pruef.checkSetzen(PositionGeklickt, aktuellerSpieler, passiverSpieler) == true)
 					{	
 						this.SpielsteinSetzen(neueBewegung, aktuellerSpieler, lButton);
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
						panel.repaint();
 						hatMuehle = true;
 						return;
 					}
 					else
 					{
 						db.zugspeichern(neueBewegung, aktuellerSpieler, false, aktuellerStein);
 					}
 					
 					if(aktuellerSpieler == Spieler2)
 						anzahlRunden++;
 					
 					//neu zeichnen
 					panel.repaint();
 					
 					this.neueMeldung(meldungsZeit, passiverSpieler.SpielsteinFarbeAsString() + textSpielerWechsel);
 					System.out.println(passiverSpieler.SpielsteinFarbeAsString() + textSpielerWechsel);
 					
 					return;
 				}				
 		
 				//Hier wird der Code ausgefhrt, wenn die erste Phase abgeschlossen ist
 				while(true)
 				{
 
 					/*if(!pruef.checkFeldBesetzt(PositionGeklickt, aktuellerSpieler, passiverSpieler))
 					{
 						break;
 					}*/
 					
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
 						//neue Bewegung erstellen
 						neueBewegung = new Bewegung(altePosition, PositionGeklickt);
 						System.out.println(neueBewegung);
 						
 						//verschiedene Prfungen
 						if(pruef.checkZug(neueBewegung, aktuellerSpieler, passiverSpieler) == true)
 						{	
 							this.SpielsteinBewegen(neueBewegung, aktuellerSpieler, lButton);
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
 							System.out.println(aktuellerSpieler.SpielsteinFarbeAsString() + textMuehle);
							panel.repaint();
 							hatMuehle = true;
 							return;
 						}
 		
 						if(aktuellerSpieler == Spieler2)
 							anzahlRunden++;
 						this.neueMeldung(meldungsZeit, passiverSpieler.SpielsteinFarbeAsString() + textSpielerWechsel);
 						System.out.println(passiverSpieler.SpielsteinFarbeAsString() + textSpielerWechsel);
 						
 						panel.repaint();
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
 							this.neueMeldung(meldungsZeit, "Dieser Stein steht in einer Mhle!");
 							SteinKannGeloeschtWerden = true;
 							return;
 						}
 						else
 							
 						this.neueMeldung(meldungsZeit, passiverSpieler.SpielsteinFarbeAsString() + " ist dran!");
 						System.out.println(passiverSpieler.SpielsteinFarbeAsString() + " ist dran!");
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
 		else
 		{
 			SteinKannGeloeschtWerden = false;
 			return;
 		}
 		
 		
 		panel.repaint();
 		if(aktuellerSpieler == Spieler2)
 		{
 			anzahlRunden++;
 			rundeVorbei = false;
 		}
 		hatMuehle = false;
 		
 		db.zugspeichern(neueBewegung, aktuellerSpieler, true, zuLoeschenderStein);
 	}
 	
 	
 	public void SpielsteinSetzen(Bewegung neueBewegung, Spieler lSpieler, TransparentButtonFeld lButton)
 	{
 		//Die Position an der der Stein gezeichnet werden soll wird ermittelt 
 		Point pos = lButton.getLocation();
 		xPos = (int)pos.getX() + (lButton.getWidth()/50) + 10;
 		yPos = (int)pos.getY() + (lButton.getHeight()/8) + 10;
 		
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
 		xPos = (int)pos.getX() + (lButton.getWidth()/50) + 10;
 		yPos = (int)pos.getY() + (lButton.getHeight()/8) + 10;
 		
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
 		Point pos = contentPane.getLocationOnScreen();
 		xPos = (int)pos.getX() + (contentPane.getWidth() / 3);
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
 	
 	
 
 		
 }
 
 
