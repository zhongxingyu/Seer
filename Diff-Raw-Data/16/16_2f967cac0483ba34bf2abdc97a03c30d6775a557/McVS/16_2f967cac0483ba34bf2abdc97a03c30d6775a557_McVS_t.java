 package com.Jessy1237.McVS;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 
 @SuppressWarnings("serial")
 public class McVS extends JFrame {
 	
 	public static String userHome = System.getProperty("user.home");
 	public static File Windir = new File(userHome, "/AppData/roaming/.minecraft");
 	public static File dir = new File(userHome, "/Library/Application Support/minecraft");
 	public static File Wintxt = new File(Windir, "/bin/.mcversion.txt");
 	public static File Mactxt = new File(dir, "/bin/mcversion.txt");
	public static double vernum = 5.12;
 	public static String osName = System.getProperty("os.name").toLowerCase();
 	public JButton Email;
 	public JButton dLoad;
 	public JButton Updateb;
 	public static String Jar = "";
 	static JLabel Version1;
 	static Switch S;
 	static TextWriter Wr;
 	static TextMaker Ma;
 	static Update U;
 	static Properties prop = new Properties();
 	public static String mcver;
 	public static JList list;
 	public static DefaultListModel model = new DefaultListModel();
 	public JScrollPane pane;
 	public JLabel a;
 	public JButton play;
 	public JButton add;
 	public JButton remove;
 
 	public static void mcver() throws IOException {
 		if (osName.contains("win")) {
 			if (Wintxt.exists()) {
 				FileInputStream in = new FileInputStream(Wintxt);
 				prop.load(in);
 				mcver = prop.getProperty("jar");
 				in.close();
 			}
 		} else {
 			if (Mactxt.exists()) {
 				FileInputStream in = new FileInputStream(Mactxt);
 				prop.load(in);
 				mcver = prop.getProperty("jar");
 				in.close();
 			}
 		}
 	}
 
 	public McVS() {
 		Thread t = new Thread(new Update());
 		t.start();
 		setTitle("Minecraft Version Switcher By Jessy1237");
 		setLayout(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(new Dimension(480, 490));
 		setResizable(false);
 		setLocationRelativeTo(null);
 
 		play = new JButton();
 		play.setText("Switch");
 		play.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (list.isSelectionEmpty()) {
 					JOptionPane.showMessageDialog(null, "Please select a Jar!", "Minecraft Version Switcher: Remove Jar", 1);
 				} else {
 					String s = (String) list.getSelectedValue();
 					s = s.replace("Play ", "");
 					if (s.contains("Minecraft 1."))
 						s = s.replace("Minecraft ", "");
 					if (s.contains("Minecraft Beta 1."))
 						s = s.replace("Minecraft Beta ", "");
 					Switch S = new Switch();
 					try {
 						Jar = s;
 						S.switchJar(s);
 						McVS.mcver();
 						a.setText("Current Selected Version: " + McVS.mcver);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 
 		add = new JButton();
 		add.setText("Add Jar");
 		add.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String s = JOptionPane.showInputDialog(null, "What is the jar called excluding '.jar'", "Minecraft Version Switcher: Add Jar", 1);
 				if (s.equals(null) || s.equals("") || s.equals(" ")) {
 					JOptionPane.showMessageDialog(null, "Please Specify the Jar Name next time!", "Minecraft Version Switcher: Add Jar", 1);
 				} else {
 					if (Switch.getFile(s).exists()) {
 						try {
 							JarMethods.addJar(s);
 							JOptionPane.showMessageDialog(null, "Done!", "Minecraft Version Switcher: Add Jar", 1);
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 					} else {
 						JOptionPane.showMessageDialog(null, " The jar " + s + " does not exist! \n Make sure the jar is in your \n bin Folder!", "Minecraft Version Switcher: Add Jar", 1);
 					}
 				}
 			}
 		});
 
 		remove = new JButton();
 		remove.setText("Remove Jar");
 		remove.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (list.isSelectionEmpty()) {
 					JOptionPane.showMessageDialog(null, "Please select a Jar!", "Minecraft Version Switcher: Remove Jar", 1);
 				} else {
 					String s = (String) list.getSelectedValue();
 					s = s.replace("Play ", "");
 					if (s.contains("Minecraft 1."))
 						s = s.replace("Minecraft W", "");
 					if (s.contains("Minecraft Beta 1."))
 						s = s.replace("Minecraft Beta ", "");
 					if (s.equals("1.1") || s.equals("1.2") || s.equals("1.0") || s.equals("1.8") || s.equals("1.7")) {
 						JOptionPane.showMessageDialog(null, "You cannot remove this jar from this\nlist but you can delete the file if you want.", "Minecraft Version Switcher: Remove Jar", 1);
 					} else {
 						if (s.equals(mcver)) {
 							JOptionPane.showMessageDialog(null, "You cannot remove this jar and mod\nfolder until you switch to another one!", "Minecraft Version Switcher: Remove Jar", 1);
 						} else {
 							try {
 								JarMethods.removeJar(s);
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							} catch (ClassNotFoundException e1) {
 								e1.printStackTrace();
 							}
 							JOptionPane.showMessageDialog(null, "Done!", "Minecraft Version Switcher: Remove Jar", 1);
 						}
 					}
 				}
 			}
 		});
 
 		Updateb = new JButton();
 		Updateb.setText("Update");
 		Updateb.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				Update.download();
 			}
 		});
 
 		dLoad = new JButton();
 		dLoad.setText("Download");
 		dLoad.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					dLoad_ActionPerformed();
 					setVisible(false);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 
 		});
 
 		Email = new JButton();
 		Email.setText("Talk to Dev");
 		Email.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Emailer em = new Emailer();
 				em.setVisible(true);
 				setVisible(false);
 			}
 		});
 		add.setBounds(355, 105, 80, 30);
 		remove.setBounds(340, 145, 110, 30);
 		dLoad.setBounds(340, 185, 110, 30);
 		Email.setBounds(340, 225, 110, 30);
 		Updateb.setBounds(340, 265, 110, 30);
 		play.setBounds(355, 305, 80, 30);
 
 		a = new JLabel();
 		a.setText("Current Selected Version: " + mcver);
 		a.setBounds(10, 440, 460, 20);
 		a.setFont(new Font("Arial", Font.PLAIN, 14));
 
 		Version1 = new JLabel();
 		Version1.setText("Program Version: ");
 		Version1.setBounds(10, 5, 460, 20);
 		Version1.setFont(new Font("Arial", Font.PLAIN, 14));
 
 		list = new JList(model);
 
 		add(list);
 
 		if (!model.isEmpty())
 			model.removeAllElements();
 
 		try {
 			JarMethods.read();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		} catch (ClassNotFoundException e2) {
 			e2.printStackTrace();
 		}
 
 		pane = new JScrollPane(list);
 		pane.setBounds(10, 27, 320, 413);
 
 		add(pane);
 		add(Version1);
 		add(add);
 		add(play);
 		add(dLoad);
 		add(Updateb);
 		add(Email);
 		add(remove);
 		add(a);
 
 	}
 
 	public static void dLoad_ActionPerformed() throws IOException {
 		dLoadJars d = new dLoadJars();
 		d.setVisible(true);
 	}
 }
