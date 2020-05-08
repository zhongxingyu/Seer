 package com.Jessy1237.renamer;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.MouseInfo;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 @SuppressWarnings("serial")
 public class Opener extends JFrame {
 	public static String userHome = System.getProperty("user.home");
 	public static File Windir = new File(userHome,
 			"/AppData/roaming/.minecraft/bin");
 	public static File dir = new File(userHome,
 			"/Library/Application Support/minecraft/bin");
 	public static File Wintxt = new File(Windir, "/.mcversion.txt");
 	public static File Mactxt = new File(dir, "/mcversion.txt");
	public static double vernum = 2.31;
 	public static String osName = System.getProperty("os.name").toLowerCase();
 	public JButton Play18;
 	public static String Jar = "";
 	public static int JAStORf = 0;
 	public static int Miss = 0;
 	public static int Rev = 0;
 	public static int Un = 0;
 	public static int Done = 0;
 	public JButton Play17;
 	public JButton Play19;
 	public JButton Updateb;
 	private JLabel Version1;
 	private static JLabel mcverlabel;
 	static RenamerWin win;
 	static RenamerMac mac;
 	static TextWriter Wr;
 	static TextMaker Ma;
 	static Properties prop = new Properties();
 	public static String mcver;
 	
 	public static void mcver() throws IOException{
 		if(osName.contains("win")){
 			if(Wintxt.exists()){
 			FileInputStream in = new FileInputStream(Wintxt);
 			prop.load(in);
 			mcver = prop.getProperty("jar");
 			in.close();
 			}
 		}else{
 			if(Mactxt.exists()){
 			FileInputStream in = new FileInputStream(Mactxt);
 			prop.load(in);
 			mcver = prop.getProperty("jar");
 			in.close();
 			}
 		}
 	}
 
 	public Opener(){
 		win = new RenamerWin();
 		mac = new RenamerMac();
 		Wr = new TextWriter();
 		Ma = new TextMaker();
 		this.setTitle("Minecraft Version Switcher");
 		this.setSize(new Dimension(480, 150));
 		this.setLocation(MouseInfo.getPointerInfo().getLocation());
 		this.setResizable(false);
 		this.setLayout(null);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.Updateb = new JButton();
 		this.Updateb.setText("Check for updates");
 		this.Updateb.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				Update.download();
 			}
 		});
 		this.Play19 = new JButton();
 		this.Play19.setText("Play Minecraft Beta 1.9");
 		this.Play19.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Play19_ActionPerformed();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 
 		});
 		this.Play18 = new JButton();
 		this.Play18.setText("Play Minecraft Beta 1.8");
 		this.Play18.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Play18_ActionPerformed();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		this.Play17 = new JButton();
 		this.Play17.setText("Play Minecraft Beta 1.7");
 		this.Play17.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Play17_ActionPerformed();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		mcverlabel = new JLabel();
 		mcverlabel.setText("Current Selected Jar: " + mcver);
 		Version1 = new JLabel();
 		Version1.setText("Program Version: " + vernum);
 
 		Play18.setBounds(20, 15, 180, 30);
 		Play17.setBounds(275, 15, 180, 30);
 		Play19.setBounds(20, 58, 180, 30);
 		Updateb.setBounds(290, 58, 150, 30);
 		mcverlabel.setSize(180, 20);
 		mcverlabel.setLocation(275, 95);
 		mcverlabel.setFont(new Font("Arial", Font.PLAIN, 14));
 		Version1.setSize(150, 20);
 		Version1.setLocation(40, 95);
 		Version1.setFont(new Font("Arial", Font.PLAIN, 14));
 
 		add(Play18);
 		add(Play17);
 		add(Play19);
 		add(Updateb);
 		add(Version1);
 		add(mcverlabel);
 	}
 
 	private static void Play17_ActionPerformed() throws IOException {
 		if (osName.contains("win")) {
 			win.Win17();
 		}
 		if (osName.contains("mac")) {
 			mac.Mac17();
 		}
 		mcver();
 		mcverlabel.setText("Current Selected Jar: " + mcver);
 	}
 
 	private static void Play18_ActionPerformed() throws IOException {
 		if (osName.contains("win")) {
 			win.Win18();
 		}
 		if (osName.contains("mac")) {
 			mac.Mac18();
 		}
 		mcver();
 		mcverlabel.setText("Current Selected Jar: " + mcver);
 	}
 
 	public static void Play19_ActionPerformed() throws IOException {
 		if (osName.contains("win")) {
 			win.Win19();
 		}
 		if (osName.contains("mac")) {
 			mac.Mac19();
 		}
 		mcver();
 		mcverlabel.setText("Current Selected Jar: " + mcver);
 	}
 }
