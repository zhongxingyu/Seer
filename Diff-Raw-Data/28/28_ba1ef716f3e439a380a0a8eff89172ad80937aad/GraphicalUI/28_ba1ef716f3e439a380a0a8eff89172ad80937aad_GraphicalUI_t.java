 package com.rightscale.ssh.ui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.rightscale.ssh.Launcher;
 import com.rightscale.ssh.SessionInfo;
 import com.rightscale.ssh.launchers.SimpleLauncher;
 
 public class GraphicalUI extends JPanel implements com.rightscale.ssh.UI {
	private static final long serialVersionUID = -1348534272091360876L;

 	// //
 	// // UI fields and functions.
 	// //
 
 	boolean     _initialized = false;
 	SessionInfo _session     = null;
 	Container   _parent      = null;
 	JPanel      _pnlMain     = null;
 
 	@SuppressWarnings("serial")
 	Action _actTroubleshoot = new AbstractAction("Troubleshoot") {
 		public void actionPerformed(ActionEvent evt) {
 			URL url = _session.getTroubleshootingLink();
 
 			Desktop desktop = Desktop.isDesktopSupported() ? Desktop
 					.getDesktop() : null;
 			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
 				try {
 					desktop.browse(url.toURI());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	};
 
 	@SuppressWarnings("serial")
 	Action _actrun = new AbstractAction("Launch SSH") {
 		public void actionPerformed(ActionEvent evt) {
 			_session.launch();
 		}
 	};
 
 	PropertyChangeListener _sessionListener = new PropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent evt) {
 			if(evt.getPropertyName() == "launcher") {
 				Launcher launcher = (Launcher)evt.getNewValue();
 				String newName = null;
 				
 				if(launcher != null) {
 					newName = "Launch " + launcher.getFriendlyName();
 				}
 				else {
 					newName = "Launch SSH";
 				}
 				
 				_actrun.putValue(Action.NAME, newName);						
 			}
 		}		
 	};
 
 	public GraphicalUI(SessionInfo session, Container parentContainer) {
 		_session = session;
 		_parent = parentContainer;
 		
 		// Make sure our UI updates when the session's properties change
 		_session.addPropertyChangeListener(_sessionListener);
 		
 		// A header that is shared between all display states
 		Container header = createHeaderUI();
 
 		// One panel for each display state the applet can be in
 		Container pnlNoLauncher = createNoLauncherUI(), pnlMissingKey = createMissingKeyUI(), pnlLaunching = createLaunchingUI();
 
 		// Add all of the initialized panels to the main (CardLayout) panel
 		_pnlMain = new JPanel();
 		_pnlMain.setLayout(new CardLayout());
 		_pnlMain.add(pnlLaunching, LAUNCHING);
 		_pnlMain.add(pnlMissingKey, MISSING_KEY);
 		_pnlMain.add(pnlNoLauncher, NO_LAUNCHER);
 
 		// Add the main and header panels to us
 		setLayout(new BorderLayout());
 		add(header, BorderLayout.NORTH);
 		add(_pnlMain, BorderLayout.CENTER);
 		
 		// All done!
 		_parent.add(this, BorderLayout.CENTER);
 	}
 	
 	public void setDisplayState(String newState) {
 		CardLayout layout = (CardLayout) _pnlMain.getLayout();
 		layout.show(_pnlMain, newState);
 	}
 
 	private Container createHeaderUI() {
 		JPanel pnl = new JPanel();
 		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
 		JLabel lbl = new JLabel("Connecting to");
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnl.add(lbl);
 		lbl = new JLabel(_session.getServerName());
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnl.add(lbl);
 		pnl.add(Box.createRigidArea(new Dimension(1, 16)));
 		return pnl;
 	}
 
 	private Container createNoLauncherUI() {
 		JPanel pnl = new JPanel();
 		Box pnlCenter = Box.createVerticalBox();
 
 		JLabel lbl = new JLabel("No supported SSH client is available.");
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		lbl.setForeground(Color.RED);
 		pnlCenter.add(lbl);
 
 		if (SimpleLauncher.isPlatform("Windows")) {
 			lbl = new JLabel("Install PuTTY or OpenSSH");
 		} else if (SimpleLauncher.isPlatform("Mac")) {
 			lbl = new JLabel(
 					"Confirm that AppleScript, Terminal.app and /usr/bin/ssh are installed and functioning normally");
 		} else {
 			lbl = new JLabel(
 					"Install OpenSSH and a suppored terminal (gnome-terminal, konsole, xterm)");
 		}
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnlCenter.add(lbl);
 
 		pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
 
 		if (_session.getTroubleshootingLink() != null) {
 			pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
 			Box pnlButtons = Box.createHorizontalBox();
 			pnlButtons.add(new JButton(_actTroubleshoot));
 			pnlCenter.add(pnlButtons);
 		}
 
 		pnl.setLayout(new BorderLayout());
 		pnl.add(pnlCenter, BorderLayout.CENTER);
 		return pnl;
 	}
 
 	private Container createLaunchingUI() {
 		JPanel pnl = new JPanel();
 		Box pnlCenter = Box.createVerticalBox();
 		Box pnlButtons = Box.createHorizontalBox();
 		JLabel lbl = new JLabel("SSH sessions launch in a separate window.");
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnlCenter.add(lbl);
 		pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
 		JButton btnrun = new JButton(_actrun);
 		pnlButtons.add(btnrun);
 		pnlCenter.add(pnlButtons);
 		pnl.setLayout(new BorderLayout());
 		pnl.add(pnlCenter, BorderLayout.CENTER);
 		return pnl;
 	}
 
 	private Container createMissingKeyUI() {
 		JPanel pnl = new JPanel();
 		Box pnlCenter = Box.createVerticalBox();
 
 		JLabel lbl = new JLabel("Missing private key file/material");
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnlCenter.add(lbl);
 
 		String path = null;
 		try {
 			path = _session.getUserKeyFile().getCanonicalPath();
 		} catch (IOException e) {
 			// If we can't even find the canonical path of the file...
 			path = _session.getUserKeyPath();
 		} catch (NullPointerException e) {
 			// If no user key file was specified
 			path = "(unknown)";
 		}
 
 		lbl = new JLabel(path);
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		pnlCenter.add(lbl);
 
 		pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
 
 		lbl = new JLabel(
 				"Please change your SSH settings or create the file mentioned above.");
 		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		lbl.setForeground(Color.RED);
 		pnlCenter.add(lbl);
 
 		if (_session.getTroubleshootingLink() != null) {
 			pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
 			Box pnlButtons = Box.createHorizontalBox();
 			pnlButtons.add(new JButton(_actTroubleshoot));
 			pnlCenter.add(pnlButtons);
 		}
 
 		pnl.setLayout(new BorderLayout());
 		pnl.add(pnlCenter, BorderLayout.CENTER);
 		return pnl;
 	}
 
 	// //
 	// // Implementation of com.rightscale.ssh.UI
 	// //
 
 	public void log(String message) {
 		System.out.println(message);
 	}
 
 	public void log(String message, Throwable problem) {
 		System.err.println(String.format("%s - %s: %s", message, problem
 				.getClass().getName(), problem.getMessage()));
 	}
 
 	public void alert(String message) {
 		log(message);
 		JOptionPane.showMessageDialog(_parent, message, "SSH Launcher",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public void alert(String message, Throwable problem) {
 		log(message, problem);
 		JOptionPane.showMessageDialog(_parent, String.format("%s\n(%s: %s)",
 				message, problem.getClass().getName(), problem.getMessage()),
 				"Error", JOptionPane.ERROR_MESSAGE);
 	}	
 }
