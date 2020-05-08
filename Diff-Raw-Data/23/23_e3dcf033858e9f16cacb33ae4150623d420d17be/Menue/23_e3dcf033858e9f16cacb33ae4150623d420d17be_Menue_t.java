 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.*;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.openal.AL;
 
 public class Menue extends Frame
 {
 	private WindowListener l;
 	private ActionLauscher a;
 	private JLabel schriftzug;
 	private ImageIcon icon;
 	private JFrame frame;
 	public static boolean conti = false;
 	
 	protected JButton[] hauptButtons;
 	protected JButton[] spielButtons;
 	private JButton optionButton;
 	private JButton leveleditor;
 	private JButton[] multiButtons;
 	private String[] headlines;
 	private JTextField[] felder;
 	private JCheckBox[] boxen;
 	private JTextField lvlwahl;
 	private JButton starten;
 	private int spieler;
 	private JButton server, client;
 	
 	protected JTextField gameover;
 	
 	AudioClip auswahl, theme, battle, boom;
 	
 	/**
 	 * Konstruktor der Klasse Menue
 	 * @param titel: Uueberschrift des Menues.
 	 */
 	public Menue(String titel)
 	{
 		super(titel);
 
 		this.Ton(1);
 		this.Ton(2);
 
 		this.setResizable(true);
 		this.setLayout(null);
 		this.setBackground(Color.black);
 		this.setBounds(100, 100, 800, 545);
 		this.setSize(800, 545);
 		//Listener
 		a = new ActionLauscher();
 		l = new WindowListener();
 		this.addWindowListener(l);
 		
 		//String-Array für Titel der Komponenten.
 		headlines = new String[15];
 		headlines[0] = "Einzelspieler";
 		headlines[1] = "Mehrspieler";
 		headlines[2] = "Optionen";
 		headlines[3] = "Spiel beenden";
 		headlines[4] = "Fortsetzen";
 		headlines[5] = "Spiel neu starten";
 		headlines[6] = "Optionen";
 		headlines[7] = "Hauptmenue";
 		headlines[8] = "Zureuck";
 		headlines[9] = "Power-Ups";
 		headlines[10] = "Anzahl der Leben";
 		headlines[11] = "Anzahl an Bomben";
 		headlines[12] = "Reichweite d. Bomben";
 		headlines[13] = "Am PC";
 		headlines[14] = "Netzwerk";
 		
 		//Buttons
 		hauptButtons = new JButton[4];
 		for(int i=0;i<hauptButtons.length;i++)
 		{
 			JButton b = new JButton(headlines[i]);
 			b.setBackground(Color.white);
 			b.setBounds(245, 160+(i*60), 150, 50);
 			b.addActionListener(a);
 			this.add(b);
 			hauptButtons[i] = b;
 		}
 		spielButtons = new JButton[4];
 		for(int i=0;i<spielButtons.length;i++)
 		{
 			JButton b = new JButton(headlines[i+4]);
 			b.setBackground(Color.white);
 			b.setBounds(245, 160+(i*60), 150, 50);
 			b.addActionListener(a);
 			b.setVisible(false);
 			if(i!=2)
 			{
 				this.add(b);
 				spielButtons[i] = b;
 			}
 		}
 		optionButton = new JButton("Zurueck");
 		optionButton.setBackground(Color.white);
 		optionButton.setBounds(245, 435, 150, 50);
 		optionButton.addActionListener(a);
 		optionButton.setVisible(false);
 		this.add(optionButton);
 		
 		leveleditor = new JButton("Leveleditor");
 		leveleditor.setBackground(Color.white);
 		leveleditor.setBounds(405, 435, 150, 50);
 		leveleditor.addActionListener(a);
 		leveleditor.setVisible(false);
 		this.add(leveleditor);
 		
 		multiButtons = new JButton[2];
 		for(int i=0;i<multiButtons.length;i++)
 		{
 			JButton b = new JButton(headlines[i+13]);
 			b.setBackground(Color.white);
 			b.setBounds(245, 160+(i*60), 150, 50);
 			b.setVisible(false);
 			b.addActionListener(a);
 			this.add(b);
 			multiButtons[i] = b;
 		}
 		
 		//TextField
 		felder = new JTextField[5];
 		for(int i=0;i<felder.length-1;i++)
 		{
 			JTextField t = new JTextField(headlines[i+9]);
 			t.setBounds(125, 170+(i*60), 150, 50);
 			t.setBackground(Color.white);
 			t.setEditable(false);
 			t.setVisible(false);
 			this.add(t);
 			felder[i] = t;
 		}
 		felder[4]= new JTextField(" Copyright by: Simon Thyssen, Marian Martini, Raphael Podoba, Martin von Arkel, Philip Hoefges");
 		felder[4].setBounds(55, 495, 500, 50);
 		//felder[4].setForeground(Color.black);
 		felder[4].setEditable(false);
 		felder[4].setFont(new Font("Arial",Font.PLAIN , 10));
 		//felder[4].setBackground(this.getBackground());
 		this.add(felder[4]);
 		
 		gameover = new JTextField();
 		gameover.setBounds(55, 420, 500, 50);
 		gameover.setEditable(false);
 		this.add(gameover);
 		
 		lvlwahl = new JTextField();
 		lvlwahl.setBounds(245,200,150,20);
 		lvlwahl.setHorizontalAlignment(JTextField.CENTER);
 		lvlwahl.setText("Level1");
 		lvlwahl.setEditable(true);
 		lvlwahl.setVisible(false);
 		this.add(lvlwahl);
 		
 		starten = new JButton("Spiel starten!");
 		starten.setBackground(Color.white);
 		starten.setBounds(245, 240, 150, 50);
 		starten.addActionListener(a);
 		starten.setVisible(false);
 		this.add(starten);
 		
 		server = new JButton("Server");
 		server.setBackground(Color.white);
 		server.setBounds(245, 160, 150, 50);
 		server.addActionListener(a);
 		server.setVisible(false);
 		this.add(server);
 		
 		client = new JButton("Client");
 		client.setBackground(Color.white);
 		client.setBounds(245, 220, 150, 50);
 		client.addActionListener(a);
 		client.setVisible(false);
 		this.add(client);
 		
 		//CheckBoxen
 		boxen = new JCheckBox[11];
 		for(int i=0;i<2;i++)
 		{
 			String s;
 			if(i==0)
 			{
 				s = "Ja";
 			}
 			else
 			{
 				s = "Nein";
 			}
 			JCheckBox c = new JCheckBox(s);
 			if(i==0)
 			{
 				c.setSelected(true);
 			}
 			c.setBounds(300+(i*90), 180, 80, 30);
 			c.setVisible(false);
 			this.add(c);
 			boxen[i] = c;
 		}
 		int j = 1;
 		int k = 0;
 		int l = 0;
 		for(int i=2;i<boxen.length;i++)
 		{
 			JCheckBox c = new JCheckBox(""+j);
 			c.setBounds(300+(l*60), 235+(k*60), 50, 30);
 			c.setVisible(false);
 			boxen[i] = c; 
 			this.add(c); 
 			j = j+2;
 			l++; 
 			if(i==4 || i==7)
 			{
 				j = 1;
 				k = k+1;
 				l = 0;
 			}
 			if(i==2 || i==5 || i==8)
 			{
 				c.setSelected(true);
 			}
 		}
 		
 		//Image/Banner
 		schriftzug = new JLabel();
 		schriftzug.setBounds(0, 0, 800, 545);
 		this.add(schriftzug);
 		//Hier Pfad eurem PC anpassen.
    		icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("menu.png"));
 		frame = new JFrame();
 		frame.getContentPane().add(schriftzug);
 		frame.setBounds(0, 0, 800, 545);
 		schriftzug.setIcon(icon);
 		frame.pack();
 		this.add(schriftzug);
 		
 		this.setVisible(true);
 	}
 	/**
 	 * Bestimmt die Anzahl der zur Verfuegung stehenden Bomben an.
 	 * @return: Anzahl der zur Verfuegung stehenden Bomben.
 	 */
 	public int getBombs()
 	{
 		int j = 0;
 		for(int i=5; i<8; i++)
 		{
 			if(boxen[i].isSelected())
 			{
 				j = Integer.parseInt(boxen[i].getText());
 				break;
 			}
 		}
 		return j;
 	}
 	/**
 	 * gibt die Anzahl an verfuegbaren Leben an.
 	 * @return: Anzahl der verguebaren Leben.
 	 */
 	public int getLives()
 	{
 		int j = 0;
 		for(int i=2; i<5; i++) 
 		{ 
 			if(boxen[i].isSelected())
 			{
 				j = Integer.parseInt(boxen[i].getText());
 				break;
 			}
 		}
 		return j;
 	}
 	/**
 	 * Reichweite der Bomben.
 	 * @return: Die im Optionsmenue bestimmte Reichweite.
 	 */
 	public int getRange()
 	{
 		int j=0;
 		for(int i=8; i<11; i++)
 		{
 			if(boxen[i].isSelected())
 			{
 				j = Integer.parseInt(boxen[i].getText());
 				break;
 			}
 		}
 		return j;
 	}
 	public void Ton(int i){
 		
 		if(i == -2) theme.stop();
 		if(i == -1) battle.stop();
 		if(i == 1)
 			{
 			
 			File f = new File( "default.wav" ); 
 			try {
 				auswahl = Applet.newAudioClip( f.toURL() );
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			auswahl.play();
 			}
 		if(i == 2)
 		{
 			File f = new File( "theme.wav" ); 
 			try {
 				theme = Applet.newAudioClip( f.toURL() );
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			theme.loop();
 		}
 		if(i == 3)
 		{
 			File f = new File( "battle.wav" ); 
 			try {
 				
 				battle = Applet.newAudioClip( f.toURL() );
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			battle.loop();
 		}
 		if(i == 4)
 		{
 			File f = new File( "bomb2.wav" ); 
 			try {
 				
 				boom = Applet.newAudioClip( f.toURL() );
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			boom.play();
 		}
 	}
 
 	/**
 	 * ActionListener fuer Menue
 	 * @author philip
 	 *
 	 */
 	private class ActionLauscher implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getActionCommand().equals("Einzelspieler"))
 			{
 				Menue.this.Ton(1);
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					hauptButtons[i].setVisible(false);
 				}
 				gameover.setVisible(false);
 				optionButton.setVisible(true);
 				lvlwahl.setVisible(true);
 				starten.setVisible(true);
 				spieler =1;
 				
 			}
 			if(e.getActionCommand().equals("Mehrspieler"))
 			{
 				Menue.this.Ton(1);
 				optionButton.setVisible(true);
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					hauptButtons[i].setVisible(false);
 				}
 				gameover.setVisible(false);
 				for(int j=0;j<multiButtons.length;j++)
 				{
 					multiButtons[j].setVisible(true);
 				}
 			}
 			if(e.getActionCommand().equals("Optionen"))
 			{
 				Menue.this.Ton(1);
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 					hauptButtons[i].setVisible(false);
 					optionButton.setVisible(true);
 					gameover.setVisible(false);
 					felder[i].setVisible(true);
 				}
 				for(int i=0;i<boxen.length;i++)
 				{
 					boxen[i].setVisible(true);
 				}
 				leveleditor.setVisible(true);
 			}
 			if(e.getActionCommand().equals("Spiel beenden"))
 			{
 				AL.destroy();
 				System.exit(0);
 			}
 			if(e.getActionCommand().equals("Fortsetzen"))
 			{
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					if(i!=2)
 					{
 					spielButtons[i].setVisible(false);
 					}
 				}
 				Main.m.setVisible(false);
 				Main.t1.resume();
 			}
 			if(e.getActionCommand().equals("Neustarten"))
 			{
 				//aktuelle Spiel beenden, neue beginnen
 				
 				/*conti = false;
 				try {
 					TimeUnit.SECONDS.sleep(2);
 				} catch (InterruptedException f) {
 					
 					f.printStackTrace();
 				}
 				Main.t1 = new Thread(new Game());
 				Main.t1.start();*/
 			}
 			if(e.getActionCommand().equals("Hauptmenue"))
 			{
 				conti = false;
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 				}
 				Main.m.setVisible(false);
 				Main.t1.resume();
 				System.out.println("Spiel wurde erfolgreich beendet.");
 			}
 			if(e.getActionCommand().equals("Zurueck"))
 			{
 				optionButton.setVisible(false);
 				
 				if(conti == true){
 					for(int i=0;i<spielButtons.length;i++)
 					{
 						felder[i].setVisible(false);
 						if(i!=2)
 						{
 							spielButtons[i].setVisible(true);
 						}
 					}
 					for(int i=0;i<boxen.length;i++)
 					{
 						boxen[i].setVisible(false);
 					}
 				}
 				else
 				{
 					for(int i=0;i<hauptButtons.length;i++)
 					{
 						felder[i].setVisible(false);
 						hauptButtons[i].setVisible(true);
 					}
 					for(int i=0;i<boxen.length;i++)
 					{
 						boxen[i].setVisible(false);
 					}
 				}
 				leveleditor.setVisible(false);
 			    server.setVisible(false);
 			    client.setVisible(false);
 			}
 			if(e.getActionCommand().equals("Am PC"))
 			{				
 				lvlwahl.setVisible(true);
 				starten.setVisible(true);
 				spieler =2;
 				
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 				}
 				for(int i=0;i<multiButtons.length;i++)
 				{
 					multiButtons[i].setVisible(false);
 				}
 			}
 			if(e.getActionCommand().equals("Netzwerk"))
 			{
 				Menue.this.Ton(1);
 				for(int i=0;i<multiButtons.length;i++)
 				{
 					multiButtons[i].setVisible(false);
 				}
 				server.setVisible(true);
 				client.setVisible(true);
 			}
 			if(e.getActionCommand().equals("Leveleditor"))
 			{
 				Menue.this.Ton(1);
 				Thread lvl = new Thread(new Leveleditor());
 				lvl.start();
 			}
 			if(e.getActionCommand().equals("Spiel starten!"))
 			{	
 				Menue.this.Ton(-2);
 				Menue.this.Ton(1);
 				Menue.this.Ton(3);
 
 				String name = lvlwahl.getText();
 				name += ".xml";
 				File test = new File(name);
 				if (!test.exists()) lvlwahl.setText("Level nicht gefunden!"); 
 				else {
 				
 				lvlwahl.setVisible(false);
 				starten.setVisible(false);
				optionButton.setVisible(false);
 				Menue.this.setVisible(false);
 				
 				Main.t1 = new Thread(new Game(spieler,name, false, false ));
 				Main.t1.start();
 				
 				for(int i=0;i<hauptButtons.length;i++)
 					{
 					hauptButtons[i].setVisible(true);
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 					gameover.setVisible(true);
 					}
 				}
 			}
 			if(e.getActionCommand().equals("Server")){
 				Menue.this.Ton(1);
 				
 				spieler = 2;
 				
 				String name = "Level1.xml";
 				File test = new File(name);
 				if (!test.exists()) lvlwahl.setText("Level nicht gefunden!"); 
 				else {
 				
 				//lvlwahl.setVisible(false);
 				server.setVisible(false);
 				client.setVisible(false);
 				Menue.this.setVisible(false);
 				
 				Main.t1 = new Thread(new Game(spieler,name, true, true ));
 				Main.t1.start();
 				
 				for(int i=0;i<hauptButtons.length;i++)
 					{
 					hauptButtons[i].setVisible(true);
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 					gameover.setVisible(true);
 					}
 				}
 				/*
 				ServerSocket server;
 				try {
 				server = new ServerSocket(12345);
 				server.setSoTimeout(60000); //wartet eine Minute auf eingehende Verbindung
 				Socket client = server.accept();
 				new Thread(new Hosting(client)).start();
 				
 				} catch ( InterruptedIOException e1 ) {
 					  //konnte keine Verbindung hergestellt werden
 				} catch (SocketException e2) {
 				} catch (IOException e3) {
 				}
 */
 			}
 			if(e.getActionCommand().equals("Client")){
 				Menue.this.Ton(1);
 				
 				spieler = 2;
 				
 				String name = "Level1.xml";
 				File test = new File(name);
 				if (!test.exists()) lvlwahl.setText("Level nicht gefunden!"); 
 				else {
 				
 				//lvlwahl.setVisible(false);
 				server.setVisible(false);
 				client.setVisible(false);
 				Menue.this.setVisible(false);
 				
 				Main.t1 = new Thread(new Game(spieler,name, true, false ));
 				Main.t1.start();
 				
 				for(int i=0;i<hauptButtons.length;i++)
 					{
 					hauptButtons[i].setVisible(true);
 					if(i!=2)
 					{
 						spielButtons[i].setVisible(false);
 					}
 					gameover.setVisible(true);
 					}
 				}
 				/*
 				try {
 					new Thread(new Client(ip)).start();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}*/
 			}
 				
 			
 		}
 	}
 }
