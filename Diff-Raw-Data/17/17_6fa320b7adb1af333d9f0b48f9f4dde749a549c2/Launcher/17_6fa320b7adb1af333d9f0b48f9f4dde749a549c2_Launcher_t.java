 /*---------------------------------------------------------------
 *  Copyright 2011 by the Radiological Society of North America
 *
 *  This source software is released under the terms of the
 *  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
 *----------------------------------------------------------------*/
 
 package org.rsna.launcher;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 /**
  * The ClinicalTrialProcessor program launcher.
  * This program provides a GUI for starting and
  * stopping CTP and for configuring the Java
  * launch parameters.
  */
 public class Launcher extends JFrame implements ChangeListener {
 
 	JTabbedPane		tp;
 	JavaPanel		javaPanel;
 	VersionPanel	versionPanel;
 	SystemPanel		systemPanel;
 	ConfigPanel		configPanel;
 	IOPanel			ioPanel;
 	LogPanel		logPanel;
 
 	static boolean	autostart = false;
 
 	Configuration	config;
 
 	public static void main(String args[]) {
 		if (args.length > 0) autostart = args[0].trim().toLowerCase().equals("start");
 		new Launcher();
 	}
 
 	/**
 	 * Class constructor; creates a new Launcher object, displays a JFrame
 	 * providing the GUI for configuring and launching CTP.
 	 */
 	public Launcher() {
 		super();
 
 		config = Configuration.getInstance();
 		setTitle(config.windowTitle);
 
 		versionPanel = new VersionPanel();
 		javaPanel = new JavaPanel();
 		systemPanel = new SystemPanel();
 		configPanel = new ConfigPanel();
 		ioPanel = new IOPanel();
 		logPanel = new LogPanel();
 		tp = new JTabbedPane();
 
 		tp.add("General", javaPanel);
 		tp.add("Version", versionPanel);
 		tp.add("System", systemPanel);
		tp.add("Configuration", configPanel);
 		tp.add("Console", ioPanel);
 		tp.add("Log", logPanel);
 
 		tp.addChangeListener(this);
 
 		this.getContentPane().add( tp, BorderLayout.CENTER );
 		this.addWindowListener(new WindowCloser(this));
 
 		pack();
 
 		positionFrame();
 		setVisible(true);
 
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
 		if (autostart) javaPanel.start();
		else javaPanel.setFocusOnStart();
 	}
 
 	public void stateChanged(ChangeEvent event) {
 		Component comp = tp.getSelectedComponent();
 		if (comp.equals(logPanel)) {
 			logPanel.reload();
 		}
 		else if (comp.equals(configPanel)) {
 			configPanel.load();
 		}
 	}
 
 	private void positionFrame() {
 		setSize( 500, 600 );
 		Properties props = Configuration.getInstance().props;
 		int x = Util.getInt( props.getProperty("x"), 0 );
 		int y = Util.getInt( props.getProperty("y"), 0 );
 		if ((x == 0) && (y == 0)) {
 			Toolkit t = getToolkit();
 			Dimension scr = t.getScreenSize ();
 			x = (scr.width - getSize().width)/2;
 			y = (scr.height - getSize().height)/2;
 		}
 		setLocation( new Point(x,y) );
 	}
 
     //Class to capture a window close event and give the
     //user a chance to change his mind.
     class WindowCloser extends WindowAdapter {
 		private Component parent;
 		public WindowCloser(JFrame parent) {
 			this.parent = parent;
 			parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		}
 		public void windowClosing(WindowEvent evt) {
 			if (Util.isRunning()) {
 				int response = JOptionPane.showConfirmDialog(
 								parent,
 								"Are you sure you want to stop the "+config.programName+" server?",
 								"Are you sure?",
 								JOptionPane.YES_NO_OPTION);
 				if (response == JOptionPane.YES_OPTION) {
 					save();
 					Util.shutdown();
 					System.exit(0);
 				}
 			}
 			else {
 				save();
 				System.exit(0);
 			}
 		}
 		private void save() {
 			Properties props = Configuration.getInstance().props;
 			Point p = parent.getLocation();
 			props.setProperty("x", Integer.toString(p.x));
 			props.setProperty("y", Integer.toString(p.y));
 			javaPanel.save();
 		}
     }
 
 }
