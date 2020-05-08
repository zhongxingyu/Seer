 /*
 	This file is part of Peers, a java SIP softphone.
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 	Copyright 2010 Yohann Martineau
  */
 
 //mvn clean package;java -jar target/ccphone-0.1-SNAPSHOT-jar-with-dependencies.jar  org.micoli.phone.ccphone.MainFrame
 
 package org.micoli.phone.ccphone;
 
 import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.Menu;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import net.sourceforge.peers.Logger;
 import net.sourceforge.peers.XmlConfig;
 import net.sourceforge.peers.sip.Utils;
 import net.sourceforge.peers.sip.core.useragent.UserAgent;
 import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
 import net.sourceforge.peers.sip.transport.SipRequest;
 import net.sourceforge.peers.sip.transport.SipResponse;
 
 import org.micoli.phone.ccphone.registrations.Registration;
 import org.micoli.phone.ccphone.remote.VertX;
 

 public class Main {
 
 	private AsyncEventManager eventManager ;
 	private Registration registration;
 	private Logger logger;
 	private SystemTray tray = null;
 	private TrayIcon trayIcon;
 	private String peersHome;
 	VertX vertX;
 	public XmlConfig config;
 
 	public static void main(final String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new Main(args);
 			}
 		});
 	}
 
 	public Main(final String[] args) {
 		peersHome = Utils.DEFAULT_PEERS_HOME;
 		if (args.length > 0) {
 			peersHome = args[0];
 		}
 		logger = new Logger(peersHome);
 		config = new XmlConfig(peersHome + File.separator+ UserAgent.CONFIG_FILE, this.logger);
 
 		launchThreads(args);
 
 		initTray();
 	}
 
 	private void launchThreads(final String[] args) {
 		VertX.init(logger);
 		VertX.run();
 
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				String peersHome = Utils.DEFAULT_PEERS_HOME;
 				if (args.length > 0) {
 					peersHome = args[0];
 				}
 				eventManager = new AsyncEventManager(Main.this, peersHome, logger);
 
 				try {
 					eventManager.register();
 				} catch (SipUriSyntaxException e) {
 					// statusLabel.setText(e.getMessage());
 				}
 			}
 		});
 		thread.start();
 
 		try {
 			while (eventManager == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			return;
 		}
 	}
 
 	private void initTray() {
 		if (tray != null) {
 			return;
 		}
 		if (!SystemTray.isSupported()) {
 			logger.error("SystemTray is not supported");
 			return;
 		}
 		final PopupMenu popup = new PopupMenu();
 		URL url = getClass().getResource("Settings-Phone-icon.png");
 		Image imageIcon = Toolkit.getDefaultToolkit().getImage(url);
 		trayIcon = new TrayIcon(imageIcon, "test");
 		tray = SystemTray.getSystemTray();
 
 		// Create a popup menu components
 		MenuItem aboutItem = new MenuItem("About");
 		Menu displayMenu = new Menu("Display");
 		MenuItem errorItem = new MenuItem("Error");
 		MenuItem warningItem = new MenuItem("Warning");
 		MenuItem infoItem = new MenuItem("Info");
 		MenuItem noneItem = new MenuItem("None");
 		MenuItem exitItem = new MenuItem("Exit");
 
 		// Add components to popup menu
 		popup.add(aboutItem);
 		popup.addSeparator();
 		popup.add(displayMenu);
 		displayMenu.add(errorItem);
 		displayMenu.add(warningItem);
 		displayMenu.add(infoItem);
 		displayMenu.add(noneItem);
 		popup.add(exitItem);
 
 		trayIcon.setPopupMenu(popup);
 
 		try {
 			tray.add(trayIcon);
 		} catch (AWTException e) {
 			logger.error("TrayIcon could not be added.");
 			return;
 		}
 
 		trayIcon.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(null, "This dialog box is run from System Tray");
 			}
 		});
 
 		aboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(null, "This dialog box is run from the About menu item");
 			}
 		});
 
 		ActionListener listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				MenuItem item = (MenuItem) e.getSource();
 				if ("Error".equals(item.getLabel())) {
 					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an error message", TrayIcon.MessageType.ERROR);
 
 				} else if ("Warning".equals(item.getLabel())) {
 					trayIcon.displayMessage("Sun TrayIcon Demo", "This is a warning message", TrayIcon.MessageType.WARNING);
 
 				} else if ("Info".equals(item.getLabel())) {
 					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an info message", TrayIcon.MessageType.INFO);
 
 				} else if ("None".equals(item.getLabel())) {
 					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an ordinary message", TrayIcon.MessageType.NONE);
 				}
 			}
 		};
 
 		errorItem.addActionListener(listener);
 		warningItem.addActionListener(listener);
 		infoItem.addActionListener(listener);
 		noneItem.addActionListener(listener);
 
 		exitItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tray.remove(trayIcon);
 				System.exit(0);
 			}
 		});
 	}
 
 	public void windowClosed(WindowEvent e) {
 		eventManager.windowClosed();
 	}
 
 	public void registerFailed(SipResponse sipResponse) {
 		registration.registerFailed();
 	}
 
 	public void registerSuccessful(SipResponse sipResponse) {
 		registration.registerSuccessful();
 	}
 
 	public void registering(SipRequest sipRequest) {
 		registration.registerSent();
 	}
 
 	public void socketExceptionOnStartup() {
 		logger.error("peers SIP port " + "unavailable, exiting");
 		System.exit(1);
 	}
 
 	public void windowActivated(WindowEvent arg0) {
 	}
 
 	public void windowClosing(WindowEvent arg0) {
 	}
 
 	public void windowDeactivated(WindowEvent arg0) {
 	}
 
 	public void windowDeiconified(WindowEvent arg0) {
 	}
 
 	public void windowIconified(WindowEvent arg0) {
 	}
 
 	public void windowOpened(WindowEvent arg0) {
 	}
 }
