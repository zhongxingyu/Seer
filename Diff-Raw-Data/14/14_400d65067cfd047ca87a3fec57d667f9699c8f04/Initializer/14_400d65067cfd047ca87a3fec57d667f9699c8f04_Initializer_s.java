 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.modeler;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.security.AccessControlException;
 import java.security.Policy;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.prefs.Preferences;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.JWindow;
 import javax.swing.border.Border;
 
 import org.concord.modeler.util.FileUtilities;
 
 import com.apple.eio.FileManager;
 
 public class Initializer {
 
 	private final static boolean IS_JAVA5 = System.getProperty("java.version").compareTo("1.6") < 0;
 	private JWindow splash;
 	private JProgressBar progressBar;
 	private Preferences preference;
 	private File propDirectory, cacheDirectory, pluginDirectory, galleryDirectory;
 	private Map<String, String> systemProperties;
 
 	private static Initializer sharedInstance = new Initializer();
 
 	private Initializer() {
 		preference = Preferences.userNodeForPackage(Modeler.class);
 		systemProperties = new HashMap<String, String>();
 		checkDir();
 	}
 
 	private void read() {
 		if (!"true".equalsIgnoreCase(System.getProperty("mw.nosecurity")))
 			setupSecurity();
 		File f = new File(propDirectory, "bookmarks.xml");
 		if (f.exists())
 			BookmarkManager.sharedInstance().readBookmarks(f);
 		f = new File(propDirectory, "history.xml");
 		if (f.exists())
 			HistoryManager.sharedInstance().readHistory(f);
 		recognizeUser();
 		readSystemProperties();
 	}
 
 	public Preferences getPreferences() {
 		return preference;
 	}
 
 	public static void init() {
 		try {
 			sharedInstance.read();
 		}
 		catch (Throwable e) {
 			e.printStackTrace();
 		}
 		if (Modeler.isMac())
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", Modeler.NAME);
 		if (IS_JAVA5)
 			System.setProperty("sun.swing.enableImprovedDragGesture", "true");
 		ModelerUtilities.init();
 		File f = new File(sharedInstance.propDirectory, "filechooser.xml");
 		if (f.exists())
 			ModelerUtilities.fileChooser.readHistory(f);
 	}
 
 	private void setupSecurity() {
 		if (Modeler.hostIsLocal)
 			return;
 		if (Modeler.runOnCD)
 			return;
 		File file = new File(propDirectory, "mw2.policy");
 		if (!file.exists()) {
 			PrintWriter writer = null;
 			try {
 				writer = new PrintWriter(file);
 			}
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			if (writer != null) {
 				writer.println("grant codeBase \"file:mw.jar\" {");
 				writer.println("    permission java.security.AllPermission;");
 				writer.println("};");
 				writer.println("grant codeBase \"file:dist/mw.jar\" {");
 				writer.println("    permission java.security.AllPermission;");
 				writer.println("};");
 				String s = null;
 				try {
 					s = pluginDirectory.toURI().toURL().toString();
 				}
 				catch (MalformedURLException e) {
 					e.printStackTrace();
 				}
 				if (s != null) {
 					writer.println("grant codeBase \"" + s + "*\" {");
 					writer.println("    permission java.security.AllPermission;");
 					writer.println("};");
 				}
 				writer.close();
 			}
 		}
 		System.setProperty("java.security.policy", file.toString());
 		Policy.getPolicy().refresh();
 		System.setSecurityManager(new SecurityManager());
 	}
 
 	public String getResetJnlpAddress() {
 		File file = new File(propDirectory, "reset.jnlp");
 		if (!file.exists()) {
 			FileUtilities.copy(Modeler.class.getResource("properties/reset.jnlp"), file);
 		}
 		return file.toString();
 	}
 
 	void recognizeUser() {
 		Properties prop = new Properties();
 		FileInputStream in = null;
 		File f = new File(propDirectory, "User.properties");
 		if (f.exists()) {
 			try {
 				in = new FileInputStream(f);
 				prop.load(in);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			catch (AccessControlException e) {
 				e.printStackTrace();
 			}
 			finally {
 				if (in != null)
 					try {
 						in.close();
 					}
 					catch (IOException e) {
 					}
 			}
 		}
 		Modeler.user.setUserID(prop.getProperty("User"));
 		Modeler.user.setPassword(prop.getProperty("Password"));
 		Modeler.user.setEmailAddress(prop.getProperty("Email"));
 		Modeler.user.setFirstName(prop.getProperty("FirstName"));
 		Modeler.user.setLastName(prop.getProperty("LastName"));
 		Modeler.user.setInstitution(prop.getProperty("Institution"));
 		Modeler.user.setState(prop.getProperty("State"));
 		Modeler.user.setCountry(prop.getProperty("Country"));
 		Modeler.user.setTeacher(prop.getProperty("Teacher"));
 	}
 
 	public final static Initializer sharedInstance() {
 		return sharedInstance;
 	}
 
 	/** returns the directory of properties files */
 	public File getPropertyDirectory() {
 		return propDirectory;
 	}
 
 	/** returns the local folder that contains cached files */
 	public File getCacheDirectory() {
 		return cacheDirectory;
 	}
 
 	/** returns the local folder that contains images from the snapshot gallery */
 	public File getGalleryDirectory() {
 		return galleryDirectory;
 	}
 
 	public File getPluginDirectory() {
 		return pluginDirectory;
 	}
 
 	private void checkDir() {
 
 		File root = null;
 		if (Modeler.isMac()) {
 			try {
 				int kApplicationSupportFolder = 0x61737570;// asup
 				short kUserDomain = -32763;
 				root = new File(FileManager.findFolder(kUserDomain, kApplicationSupportFolder));
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 				root = new File(System.getProperty("user.home"), "Application Data");
 			}
 		}
 		else {
 			root = new File(System.getProperty("user.home"), "Application Data");
 		}
 		if (!root.exists())
 			root.mkdir();
 
 		File baseDir = new File(root, Modeler.NAME);
 		if (!baseDir.exists())
 			baseDir.mkdirs();
 
 		cacheDirectory = new File(baseDir, "cache");
 		if (!cacheDirectory.exists())
 			cacheDirectory.mkdir();
 
 		pluginDirectory = new File(baseDir, "plugin");
 		if (!pluginDirectory.exists())
 			pluginDirectory.mkdir();
 
 		propDirectory = new File(baseDir, "properties");
 		if (!propDirectory.exists())
 			propDirectory.mkdir();
 
 		galleryDirectory = new File(baseDir, "gallery");
 		if (!galleryDirectory.exists())
 			galleryDirectory.mkdir();
 
 	}
 
 	final void putSystemProperty(String key, String val) {
 		systemProperties.put(key, val);
 	}
 
 	final String getSystemProperty(String key) {
 		return systemProperties.get(key);
 	}
 
 	private void readSystemProperties() {
 		File f = new File(propDirectory, "system.xml");
 		if (!f.exists())
 			return;
 		XMLDecoder in = null;
 		try {
 			in = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (in == null)
 			return;
 		try {
 			systemProperties = (HashMap) in.readObject();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			in.close();
 		}
 	}
 
 	final void writeSystemProperties() {
 		XMLEncoder out = null;
 		try {
 			out = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(new File(propDirectory, "system.xml"))));
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (out == null)
 			return;
 		try {
 			out.writeObject(systemProperties);
 		}
 		finally {
 			out.close();
 		}
 	}
 
 	void setMessage(final String s) {
 		if (progressBar == null)
 			return;
 		if (EventQueue.isDispatchThread()) {
 			progressBar.setString(s);
 		}
 		else {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					progressBar.setString(s);
 				}
 			});
 		}
 	}
 
 	void showSplashScreen() {
 		final Color color = new Color(8, 24, 99);
 		final Font font = new Font("Verdana", Font.PLAIN, 10);
 		progressBar = new JProgressBar();
 		progressBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5));
 		progressBar.setBackground(Color.white);
 		progressBar.setForeground(color);
 		progressBar.setIndeterminate(false);
 		progressBar.setMaximum(10);
 		progressBar.setMinimum(0);
 		progressBar.setStringPainted(true);
 		progressBar.setString("Launching......");
 		progressBar.setFont(font);
 		ImageIcon icon = new ImageIcon(getClass().getResource("images/Splash.gif"));
 		int iconWidth = icon.getIconWidth();
 		int iconHeight = icon.getIconHeight();
 		progressBar.setPreferredSize(new Dimension(iconWidth, 22));
 		splash = new JWindow();
 		Container c = splash.getContentPane();
 		c.add(new JLabel(icon) {
 			public void paintComponent(Graphics g) {
 				Icon icon = getIcon();
 				icon.paintIcon(this, g, 0, 0);
 				g.setFont(font);
 				g.setColor(color);
 				g.drawString("Version " + Modeler.VERSION + " Copyright 2004-2008.", 10, icon.getIconHeight() - 25);
 				g.drawString("Supported by the National Science Foundation.", 10, icon.getIconHeight() - 10);
 			}
 		}, BorderLayout.CENTER);
 
 		progressBar.setBorder(new Border() {
 			public Insets getBorderInsets(Component c) {
 				return new Insets(2, 2, 2, 2);
 			}
 
 			public boolean isBorderOpaque() {
 				return true;
 			}
 
 			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
 				g.setColor(Color.black);
 				g.drawLine(1, height - 1, width - 1, height - 1);
 				g.drawLine(1, height - 2, width - 1, height - 2);
 				g.drawLine(0, 0, 0, height);
 				g.drawLine(width - 1, 0, width - 1, height);
 				g.drawLine(width, 0, width, height);
 			}
 		});
 		c.add(progressBar, BorderLayout.SOUTH);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		splash.setLocation(screenSize.width / 2 - iconWidth / 2, screenSize.height / 2 - iconHeight / 2);
 		splash.pack();
 		splash.setVisible(true);
 	}
 
 	void hideSplashScreen() {
 		if (splash != null) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					splash.dispose();
 					Debugger.print("Splash pane disposed");
 				}
 			});
 		}
 	}
 
 }
