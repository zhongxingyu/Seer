 package org.barteks2x.freeminelauncher;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.imageio.stream.ImageInputStream;
 import javax.swing.*;
 import javax.swing.GroupLayout.Alignment;
 
 public class LauncherFrame extends JFrame {
 
 	JPanel background;
 	private static String OS = System.getProperty("os.name").toLowerCase();
 	private String fastpng = "https://dl.dropboxusercontent.com/u/54602353/fastpng-1.0.jar",
 			jinputjar = "https://dl.dropboxusercontent.com/u/54602353/jinput-2.0.5.jar",
 			jinputdx8x64dll = "https://dl.dropboxusercontent.com/u/54602353/jinput-dx8_64.dll",
 			jinputdx8dll = "https://dl.dropboxusercontent.com/u/54602353/jinput-dx8.dll",
 			jinputraw64dll = "https://dl.dropboxusercontent.com/u/54602353/jinput-raw_64.dll",
 			jinputrawdll = "https://dl.dropboxusercontent.com/u/54602353/jinput-raw.dll",
 			jinputlinux64 = "https://dl.dropboxusercontent.com/u/54602353/libjinput-linux64.so",
 			jinputlinux = "https://dl.dropboxusercontent.com/u/54602353/libjinput-linux.so",
 			jinputosx = "https://dl.dropboxusercontent.com/u/54602353/libjinput-osx.jnilib",
 			lwjgl64so = "https://dl.dropboxusercontent.com/u/54602353/liblwjgl64.so",
 			lwjglosx = "https://dl.dropboxusercontent.com/u/54602353/liblwjgl.jnilib",
 			lwjglso = "https://dl.dropboxusercontent.com/u/54602353/liblwjgl.so",
 			oal64so = "https://dl.dropboxusercontent.com/u/54602353/libopenal64.so",
 			oalso = "https://dl.dropboxusercontent.com/u/54602353/libopenal.so",
 			lwjgl64dll = "https://dl.dropboxusercontent.com/u/54602353/lwjgl64.dll",
 			utiljar = "https://dl.dropboxusercontent.com/u/54602353/lwjgl_util-2.8.5.jar",
 			lwjgljar = "https://dl.dropboxusercontent.com/u/54602353/lwjgl-2.8.5.jar",
 			lwjgldll = "https://dl.dropboxusercontent.com/u/54602353/lwjgl.dll",
 			oaldll = "https://dl.dropboxusercontent.com/u/54602353/OpenAL32.dll",
 			oal64dll = "https://dl.dropboxusercontent.com/u/54602353/OpenAL64.dll",
 			oalosx = "https://dl.dropboxusercontent.com/u/54602353/openal.dylib";
 
 	public LauncherFrame() {
 		super("FREEMine Launcher");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setVisible(true);
 		this.setSize(640, 480);
 		this.setLocation(100, 100);
 		this.setLayout(null);
 		setBackground();
 		addComponents();
 	}
 
 	private void addComponents() {
 		JButton startButton = new MyButton("Start game");
 		startButton.setLocation(640 / 2 - 100, 480 / 2 + 40);
 		startButton.setSize(200, 20);
 		background.add(startButton);
 		startButton.addActionListener((ActionListener) startButton);
 		//this.setComponentZOrder(startButton, 0);
 		JLabel l = new JLabel("FREEMine");
 		l.setText("FREEMine");
 		l.setFont(new Font("Times New Roman", Font.PLAIN, 70));
 		l.setForeground(new Color(230, 230, 255));
 		l.setLocation(0, 100);
 		l.setSize(640, 70);
 		l.setHorizontalAlignment(JLabel.CENTER);
 		l.setVisible(true);
 		background.add(l);
 		JTextField nick = new JTextField();
 		nick.setLocation(640 / 2 - 100, 480 / 2 - 40);
 		nick.setSize(200, 20);
 		nick.setText("Nick (Not implemented))");
 		background.add(nick);
 		JTextField pass = new JTextField();
 		pass.setText("Password (Not implemented)");
 		pass.setSize(nick.getSize());
 		pass.setLocation(640 / 2 - 100, 480 / 2 - 15);
 		background.add(pass);
 	}
 
 	private void setBackground() {
 		background = new ImagePanel(Thread.currentThread().getContextClassLoader().getResourceAsStream(
 				"background.png"));
 		background.setLocation(0, 0);
 		background.setSize(640, 480);
 		this.add(background);
 		background.setLayout(null);
 	}
 
 	private class MyButton extends JButton implements ActionListener {
 
 		public MyButton(String s) {
 			super(s);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			FileDownloader d = new FileDownloader("FREEMine");
 			try {
 				d.download(new URL(fastpng));
 				d.download(new URL(lwjgljar));
 				d.download(new URL(jinputjar));
 				d.download(new URL(utiljar));
 				String os = System.getProperty("os.name");
 				FileDownloader d2 = new FileDownloader("FREEMine" + File.separator + "native");
 				if (isWindows()) {
 					d2.download(new URL(jinputdx8dll));
 					d2.download(new URL(jinputdx8x64dll));
 					d2.download(new URL(jinputraw64dll));
 					d2.download(new URL(jinputrawdll));
 					d2.download(new URL(lwjgl64dll));
 					d2.download(new URL(lwjgldll));
 					d2.download(new URL(oal64dll));
 					d2.download(new URL(oaldll));
 				}
 				if (isUnix()) {
 					d2.download(new URL(jinputlinux));
 					d2.download(new URL(jinputlinux64));
 					d2.download(new URL(lwjgl64so));
 					d2.download(new URL(lwjglso));
 					d2.download(new URL(oalso));
 					d2.download(new URL(oal64so));
 				}
 				if (isMac()) {
 					d2.download(new URL(jinputosx));
 					d2.download(new URL(lwjglosx));
 					d2.download(new URL(oalosx));
 				}
 				d.download(new URL(
 						"https://buildhive.cloudbees.com/view/All/job/Barteks2x/job/FREEMine/lastStableBuild/org.Barteks2x$FREEMine/artifact/org.Barteks2x/FREEMine/0.0.0-pre-alpha/FREEMine-0.0.0-pre-alpha.jar"),
 						   "FREEMine.jar");
 				File f = new File("FREEMine" + File.separator + "native"+File.separator);
 				File fr = new File("FREEMine" + File.separator + "FREEMine.jar");
 				File dir = new File("FREEMine");
 				ProcessBuilder b = new ProcessBuilder("java", "-Djava.library.path=" + f.getAbsolutePath(), "-jar", fr.getAbsolutePath());
 				b.directory(dir);
 				b.redirectErrorStream(true);
 				b.redirectOutput(new File("log.txt"));
 				b.start();
 			} catch (MalformedURLException ex) {
 				Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (IOException ex) {
 				Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 	}
 
 	public static boolean isWindows() {
 
 		return (OS.indexOf("win") >= 0);
 
 	}
 
 	public static boolean isMac() {
 
 		return (OS.indexOf("mac") >= 0);
 
 	}
 
 	public static boolean isUnix() {
 
 		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
 
 	}
 
 	public static boolean isSolaris() {
 
 		return (OS.indexOf("sunos") >= 0);
 
 	}
 }
