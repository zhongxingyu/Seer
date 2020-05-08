 package game.view;
 
 import game.Settings;
 import game.util.ConfigMaker;
 import game.util.CoolButton;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 /*
  * Classe de l'interface graphique : Menu
  */
 public class MenuView extends JFrame implements ActionListener {
 	
 	private GameState view;
 	
 	private Image background;
 	
 	private AudioClip currentSound;
 	
 	private Options options;
 	private Commands commandsView;
 	
 	private JPanel panel;
 	private JPanel exitOptions;
 	
 	private CoolButton singleplayer;
 	private CoolButton multiplayer;
 	private CoolButton scores;
 	private CoolButton optionsButton;
 	private CoolButton exit;
 	
 	public MenuView(){
 		super("Asteroids");
 		
 		ConfigMaker.getProperties();
 		
 		background = Toolkit.getDefaultToolkit().getImage("res/background.jpg");
 		try
 		{
 			MediaTracker mt = new MediaTracker(this);
 			mt.addImage(background,0);
 			mt.waitForAll();
 		}
 		catch(Exception e){e.printStackTrace();}
 		this.setContentPane(new ContentPane(background));
 		
         URL path = getClass().getResource("Eight_Bit_Robot_Dance.wav");
 		try{
 			currentSound = Applet.newAudioClip(path);
 			if(Settings.VOLUME){
 				currentSound.loop();
 			}
 		}catch(Exception e){e.printStackTrace();}
 		
 		this.setVisible(true); // Visible car première fenêtre à être affichée atm
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		commandsView = new Commands();
 		options = new Options(this, commandsView);
 		
 		panel = new JPanel(new GridLayout(10,1));
 		panel.setOpaque(false);
 		exitOptions = new JPanel(new GridLayout(1,3));
 		exitOptions.setOpaque(false);
 		singleplayer = new CoolButton("SINGLEPLAYER");
 		multiplayer = new CoolButton("MULTIPLAYER");
 		scores = new CoolButton("SCORES");
 		optionsButton = new CoolButton("OPTIONS");
 		exit = new CoolButton("EXIT");
 		
		
 		this.add(panel);
 		panel.add(new JLabel(""));
 		panel.add(new JLabel(""));
 		panel.add(new JLabel(""));
 		panel.add(new JLabel(""));
 		panel.add(new JLabel(""));
 		panel.add(singleplayer);
 		panel.add(multiplayer);
 		panel.add(scores);
 		exitOptions.add(exit);
 		exitOptions.add(new JLabel(""));
 		exitOptions.add(optionsButton);
 		panel.add(new JLabel(""));
 		panel.add(exitOptions);
 		singleplayer.addActionListener(this);
 		multiplayer.addActionListener(this);
 		scores.addActionListener(this);
 		exit.addActionListener(this);
 		optionsButton.addActionListener(this);
 		pack();
 		this.setSize(800, 600);
 		singleplayer.setPreferredSize(new Dimension(100, 50));
 		this.setResizable(false);
 		this.setLocationRelativeTo(null);
 		view = new GameState(this);
 		view.init();
 	}
 	
 	public void setVolume(boolean volume){
 		Settings.VOLUME = volume;
 		if(volume){
 			currentSound.loop();
 		}
 		else{
 			currentSound.stop();
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		JButton b = (JButton)e.getSource();
 		
 		if(b == exit){ 
 			this.dispose();
 			System.exit(0);
 		}
 		if(b == optionsButton){
 			options.setLocationRelativeTo(null);
 			options.setVisible(true);
 		}
 		if(b == singleplayer){
 			beforeStart(false);
 		}
 		if(b == multiplayer){
 			beforeStart(true);
 		}
 		if(b == scores){
 			showScores();
 		}
 	}
 	
 	private void showScores() {
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		JPanel headerPanel = new JPanel(new GridLayout(2,3));
 		JPanel scorePanel = new JPanel(new GridLayout(10,5));
 		headerPanel.add(new JLabel("Singleplayer"));
 		headerPanel.add(new JLabel(""));
 		headerPanel.add(new JLabel("Multiplayer"));
 		headerPanel.add(new JLabel(""));
 		headerPanel.add(new JLabel(""));
 		headerPanel.add(new JLabel(""));
 		Properties prop = new Properties();
 		 
     	try {
             //load a properties file
     		prop.load(new FileInputStream("config/scores.properties"));
  
             //get the property value and print it out
     		for(int j = 1 ; j < 11 ; j++){
     			scorePanel.add(new JLabel(prop.getProperty("single"+j+"Name")+" : "));
 	    		scorePanel.add(new JLabel(prop.getProperty("single"+j+"Score")));
 	    		scorePanel.add(new JLabel(""));
 	    		scorePanel.add(new JLabel(prop.getProperty("multi"+j+"Name")+" : "));
 	    		scorePanel.add(new JLabel(prop.getProperty("multi"+j+"Score")));
     		}
  
     	} catch (IOException ex) {
     		ex.printStackTrace();
         }
     	mainPanel.add(headerPanel);
     	mainPanel.add(scorePanel);
 		JOptionPane.showMessageDialog(null, mainPanel,"Scores", JOptionPane.PLAIN_MESSAGE);
 		
 	}
 
 	private void beforeStart(boolean isMultiplayer){
 		SpinnerNumberModel sModel = new SpinnerNumberModel(5, 1, 20, 1);
 		JSpinner spinner = new JSpinner(sModel);
 		JPanel panelOptions = new JPanel(new GridLayout(2,2));
 		JSlider difficulty = new JSlider();
 		difficulty.setMaximum(3);
 		Dictionary<Integer, JLabel> dict = new Hashtable<Integer, JLabel>();
 		dict.put(0, new JLabel("Pussy"));
 		dict.put(1, new JLabel(" Ok "));
 		dict.put(2, new JLabel("Man mode"));
 		dict.put(3, new JLabel("Insane"));
 		difficulty.setLabelTable(dict);
 		difficulty.setMinorTickSpacing(1);
 		difficulty.setMajorTickSpacing(4);
 		difficulty.setValue(1);
 		difficulty.setPaintLabels(true);
 		difficulty.setPaintTicks(true);
 		difficulty.setSnapToTicks(true);
 		panelOptions.add(new JLabel(" Game time (min) "));
 		panelOptions.add(spinner);
 		panelOptions.add(new JLabel(" Difficulty "));
 		panelOptions.add(difficulty);
 		int option = JOptionPane.showOptionDialog(null, panelOptions, " Enter game options ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
 		if (option == JOptionPane.CANCEL_OPTION)
 		{
 		    // user hit cancel -> simply return
 		} else if (option == JOptionPane.OK_OPTION)
 		{
 			Settings.TIMEOUT = (long)(Integer)spinner.getValue();
 			Settings.DIFFICULTY = difficulty.getValue();
 			view.clear();
 			view.joinGame(Settings.PLAYER1);
 			if(isMultiplayer)
 				view.joinGame(Settings.PLAYER2);
 			view.setRunning();
 			view.setLocationRelativeTo(null);
 			view.setVisible(true);
 			this.setVisible(false);
 		}
 	}
 
 	/*
 	 * Classe du background
 	 */
 	private class ContentPane extends JPanel
 	{
 		private Image image;
 		public ContentPane(Image leFond){super();image=leFond;}
 		public void paintComponent(Graphics g){g.drawImage(image,0,0,null);}
 	}
 }
 
 
