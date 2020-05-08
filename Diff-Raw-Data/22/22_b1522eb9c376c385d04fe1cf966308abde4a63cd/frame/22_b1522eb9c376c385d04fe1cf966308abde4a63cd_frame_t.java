 package newgame;
 import java.awt.event.*;
 import java.awt.Dimension;
 
 import javax.swing.*; //JButton, JFrame..
 
 import java.awt.*;
 import java.io.IOException;
 /**
  * Das Hauptfenster was als erstes erscheint
  * @author Tobias
  *
  */
 public class frame extends JFrame implements ActionListener{
 
 private JButton optionen; 
 private JButton starten; 																// fuegt der Klasse die Buttons und das Label hinzu
 private JButton beenden;
 private JButton multiplayer;
 private JButton load;
 private JLabel l1;
 public boolean test1 = false;
 
 
 public static void main(String[] args){ 
 
 frame frame = new frame ("DUNGEON CRAWLER");											// Menuetitel
 try {
 	Diggy_1_Server();
 } catch (IOException e) {
 	// TODO Auto-generated catch block
 	e.printStackTrace();
 }											
 
 }
 
 /**
  * Erstellt das Fenster mit den dazugehoerigen Parametern
  * @param title Erstellt den Titeltext des Fensters
  */
 public frame(String title){
 super(title);
 
 setSize(500,500);
 setLocationRelativeTo(null);															// zentriert das Fenster
 setDefaultCloseOperation(EXIT_ON_CLOSE);
 setLayout(new BorderLayout());
 setContentPane(new JLabel(new ImageIcon("src/Resources/Start Screen.png")));			// setzt die Groesse des Fensters, den Hintergrund, sowie die Operation zum Schliessen fest
 l1=new JLabel();
 pack();
 setVisible(true);
 
 JOptionPane.showMessageDialog(optionen, "<html><body>Benutzen Sie die Leertaste zum schieen.<br>" +			//mithilfe von html werden die Hinweise erstellt
 "Benutzen Sie die Pfeiltasten um sich zu bewegen.<br>" +
 "Benutzen Sie die Taste 'V' um mit ihrem Schwert zu schlagen.<br>" +
"Wenn Sie das Spiel speichern mchten, drcken Sie 'F1'.<br>" +
 "Wenn Sie einen Manatrank haben koennen Sie mit 'B' ihr Mana wieder auffrischen.<br>" +
 "Sollten Sie mehrere Waffen haben, so wechseln Sie diese mit 'X'.<br>" +
 "Sollten Sie mehrere Zaubersprueche haben, so wechseln Sie diese mit 'C'.<br></body></html>");
 optionen = new JButton("Optionen");
 //optionen.setBounds(170,365,150,80);
 //optionen.setIcon(new ImageIcon("src/Resources/Start Screen optionen.png"));
 optionen.addActionListener(this);
 
 starten = new JButton("Spiel Starten");
 starten.setBounds(170,95,150,80);
 starten.setFocusPainted(false);
 starten.setIcon(new ImageIcon("src/Resources/Start Screen start.png")); 				// setzt Groesse und Hintergruende fuer die Buttons fest
 starten.addActionListener(this);														// fuegt Aktion dem Buttonklick hinzu
 
 multiplayer = new JButton("Multiplayer");
 multiplayer.setBounds(170, 180, 150, 80);
 multiplayer.setFocusPainted(false);
 multiplayer.addActionListener(this);
 
 load = new JButton("Load");
 load.setBounds(170, 380, 150, 80);
 load.setFocusPainted(false);
 load.addActionListener(this);
 
 
 beenden = new JButton("Beenden");
 beenden.setBounds(170,265,150,80);
 beenden.setIcon(new ImageIcon("src/Resources/Start Screen end.png"));					// gleiches Verhalten wie beim Button "starten"
 beenden.addActionListener(this);
 
 add(l1);
 add(optionen);
 add(load);
 add(starten);
 add(beenden);
 add(multiplayer);																		// fuegt dem Frame die noetigen Buttons hinzu
 setSize(499,499);
 setSize(500,500);																		// aktualisiert das Fenster; noetig damit Einstellungen uebernommen werden
 }
 
 @Override
 public void actionPerformed(ActionEvent e) {
 // TODO Auto-generated method stub
 
 if (e.getSource()==starten){
 	try {
 		game();
 	} catch (IOException e1) {
 		// TODO Auto-generated catch block
 		e1.printStackTrace();
 	}
 }
 
 if (e.getSource()==load){
 	try {
 		game();
 	} catch (IOException e1) {
 		// TODO Auto-generated catch block
 		e1.printStackTrace();
 	}
 }
 
 if (e.getSource()==multiplayer){														//sucht nach dem Client
 	try{	
 		Diggy_1_Client();
 	} catch (IOException e1){
 		e1.printStackTrace();
 	}
 }
 
 if (e.getSource()==beenden){															// schliesst Menue
 		System.exit(0);
 
 }
 }
 
 /**
  * Startet das Hauptspiel auf Tastendruck
  * @throws IOException
  */
 public static void Diggy_1_Server() throws IOException{
 	Diggy_1_Server Server = new Diggy_1_Server();										//startet den Server, unabhngig vom Client
 	try {
 		Server.run();
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
 }
 public static void Diggy_1_Client() throws IOException{
 	Diggy_1_Client Client = new Diggy_1_Client();
 	try {
 		Client.run();																	//startet den Client
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
 }
 
 public static void game() throws IOException{											// Fenster fuer's Spiel
 	JFrame game = new JFrame("PLAY DUNGEON CRAWLER");
 	game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
 	Dimension aufloesung = Toolkit.getDefaultToolkit().getScreenSize();
     game.setSize(aufloesung);
 	game.setVisible(true);
 	game.setLocationRelativeTo(null);
 	game.add(new Board());																// oeffnet Klasse board (das eigentliche Spiel)
 
 	}
 }
