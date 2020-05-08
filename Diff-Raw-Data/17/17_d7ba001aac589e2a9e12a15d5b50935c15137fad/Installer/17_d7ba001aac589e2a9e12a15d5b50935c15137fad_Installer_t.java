 package com.ijg.darklight.build;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextArea;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Darklight Installation candidate
  * @author Isaac Grant
  * @author Lucas Nicodemus
  * @version .1
  *
  */
 
 public class Installer {
 	
 	private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 	private Rectangle screenSize = gd.getDefaultConfiguration().getBounds();
 	private Point center = new Point(screenSize.width / 2, screenSize.height / 2);
 	
 	private final String title = "Darklight Installer";
 	
 	JFrame frame;
 	JPanel welcome;
 	JPanel terms, chooseBuild, installPanel;
 	
 	private Parser buildParser;
 	private String installPath;
 	
 	/**
 	 * GUI constructor
 	 */
 	public Installer() {
 		frame = new JFrame();
 		
 		initWelcomeFrame();
 		
 		frame.setTitle(title);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		frame.setVisible(true);
 		frame.setContentPane(welcome);
 		frame.pack();
 	}
 	
 	/**
 	 * CLI constructor
 	 * @param buildFile The file in which the installation settings are found
 	 */
 	public Installer(String buildFile) {
 		install(new File(buildFile));
 	}
 	
 	/**
 	 * 
 	 * @param args CLI arguments
 	 */
 	public static void main(String[] args) {
 		if (args.length == 2) {
 			if (args[0] == "-build") {
 				new Installer(args[1]);
 			}
 		} else {
 			new Installer();
 		}
 	}
 	
 	/**
 	 * Non-GUI installation method
 	 * @param buildFile The file in which the installation settings are found
 	 */
 	public void install(File buildFile) {
 		initParser(buildFile);
		if (!createFileSystem())
			createFileSystem();
 		copyJar((String) buildParser.get("jar"));
 		copyModules();
 		buildParser.destroy();
 	}
 	
 	/**
 	 * Initiate the build file parser
 	 * @param buildFile The fild in which the installation settings are found
 	 */
 	private void initParser(File buildFile) {
 		buildParser = new Parser(buildFile);
 		buildParser.parse();
 		installPath = (String) buildParser.get("installpath");
 	}
 	
 	/**
 	 * Create the installation
 	 */
	private boolean createFileSystem() {
 		File installFolder = new File(installPath);
 		File pluginsFolder = new File(installFolder.getAbsolutePath(), "plugins");
 		
 		if (installFolder.exists()) {
 			installFolder.delete();
 		}
 		
 		if (pluginsFolder.exists()) {
 			pluginsFolder.delete();
 		}
 		
 		if (installFolder.mkdir()) {
 			if (pluginsFolder.mkdir()) {
 				System.out
 						.println("[DarklightInstaller] Successfully created file system in "
 								+ installFolder.getAbsolutePath());
				return true;
 			} else {
 				System.out
 						.println("[DarklightInstaller] Error creating plugins folder");
 			}
 		} else {
 			System.out
 					.println("[DarklightInstaller] Error creating install path");
 		}
		return false;
 	}
 	
 	/**
 	 * Copy the Darklight jar to the install directory
 	 * @param jarPath The path of the Darklight jar
 	 */
 	private void copyJar(String jarPath) {
 		File darklightJar = new File(jarPath);
 		
 		if (darklightJar.exists()) {
 			if (darklightJar.isFile()) {
 				darklightJar.renameTo(new File(installPath, "Darklight.jar"));
 				return;
 			}
 		}
 		System.out
 				.println("[DarklightInstaller] Error, jar does not exist");
 	}
 	
 	/**
 	 * Copy the modules to be used into the plugins folder
 	 */
 	private void copyModules() {
 		File pluginsFolder = new File("plugins");
 		
 		File[] modules = pluginsFolder.listFiles();
 		
 		for (File module : modules) {
 			module.renameTo(new File(new File(installPath, "plugins"), module.getName()));
 		}
 	}
 	
 	/**
 	 * Safely kill the installation
 	 */
 	private void kill() {
 		System.exit(0);
 	}
 	
 	/**
 	 * Create and display the welcome panel
 	 */
 	private void initWelcomeFrame() {
 		welcome = new JPanel();
 		
 		JButton next = new JButton("Continue");
 		next.setSize(new Dimension(30, 15));
 		JButton close = new JButton("Quit");
 		close.setSize(new Dimension(30, 15));
 		JLabel welcomeLabel = new JLabel("Welcome to the Darklight installer! Press continue to install or quit to cancel");
 		
 		next.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadTerms();
 			}
 		});
 		
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				kill();
 			}
 		});
 		
 		welcome.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 4;
 		c.insets = new Insets(5, 5, 5, 5);
 		welcome.add(welcomeLabel, c);
 		
 		c.gridy = 1;
 		welcome.add(new JLabel(" "), c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(0, 5, 5, 0);
 		welcome.add(close, c);
 		
 		c.gridx = 2;
 		c.anchor = GridBagConstraints.EAST;
 		c.insets = new Insets(0, 0, 5, 5);
 		welcome.add(next, c);
 		
 		frame.setLocation(center);
 		frame.setContentPane(welcome);
 		frame.pack();
 	}
 	
 	/**
 	 * Create and display the terms of service panel
 	 */
 	private void loadTerms() {
 		welcome.removeAll();
 		frame.remove(welcome);
 		
 		terms = (JPanel) frame.getContentPane();
 		
 		final JButton next = new JButton("Next");
 		next.setEnabled(false);
 		next.setSize(new Dimension(30, 15));
 		JButton close = new JButton("Quit");
 		close.setSize(new Dimension(30, 15));
 		final JCheckBox agree = new JCheckBox("I have read and agree to the above terms of use");
 		
 		JTextArea conditions = new JTextArea(
 				"By installing Darklight you agree to the following terms:\n" +
 				"[1] Darklight is not to be distributed by any person or third party\n" +
 				"[2] Darklight may not be redistributed under another name\n" +
 				"[3] An attempt to reverse engineer any Darklight Security Tools is prohibited and punishable by law\n" +
 				"Darklight Core contains tamper protection that may damage your computer " +
 				"if it detects an attempt\n to reverse engineer or modify Darklight Core in any way");
 		conditions.setEditable(false);
 		conditions.setBackground(Color.white);
 		
 		next.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadChooseBuild();
 			}
 		});
 		
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				kill();
 			}
 		});
 		
 		agree.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (agree.isSelected()) {
 					next.setEnabled(true);
 				} else {
 					next.setEnabled(false);
 				}
 			}
 		});
 		
 		terms.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = new Insets(5, 5, 5, 5);
 		terms.add(conditions, c);
 		
 		c.gridy = 1;
 		terms.add(agree, c);
 		
 		c.gridy = 2;
 		c.insets = new Insets(0, 5, 5, 0);
 		c.anchor = GridBagConstraints.WEST;
 		terms.add(close, c);
 		
 		c.gridx = 1;
 		c.insets = new Insets(0, 0, 5, 5);
 		c.anchor = GridBagConstraints.EAST;
 		terms.add(next, c);
 		
 		frame.setContentPane(terms);
 		frame.pack();
 	}
 	
 	/**
 	 * Load and display the panel to choose the build file
 	 */
 	private void loadChooseBuild() {
 		terms.removeAll();
 		frame.remove(terms);
 		
 		chooseBuild = (JPanel) frame.getContentPane();
 		
 		JButton next = new JButton("Next");
 		next.setSize(new Dimension(30, 15));
 		JButton close = new JButton("Quit");
 		close.setSize(new Dimension(30, 15));
 		final JFileChooser buildFileChooser = new JFileChooser(new File("."));
 		buildFileChooser.setSelectedFile(new File(new File("."), "install.json"));
 		buildFileChooser.setFileFilter(new FileFilter() {
 			public String getDescription() {
 				return "json";
 			}
 			public boolean accept(File f) {
 				try {
 					if (f.getName().substring(f.getName().indexOf(".")).equals(".json")) {
 						return true;
 					}
 				} catch (Exception e) {}
 				return false;
 			}
 		});
 		buildFileChooser.setMultiSelectionEnabled(false);
 		
 		next.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadInstallPanel(buildFileChooser.getSelectedFile());
 			}
 		});
 		
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				kill();
 			}
 		});
 		
 		chooseBuild.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		chooseBuild.add(buildFileChooser);
 		
 		c.gridy = 1;
 		c.insets = new Insets(0, 5, 5, 0);
 		c.anchor = GridBagConstraints.WEST;
 		chooseBuild.add(close, c);
 		
 		c.insets = new Insets(0, 0, 5, 5);
 		c.anchor = GridBagConstraints.EAST;
 		chooseBuild.add(next, c);
 		
 		frame.setContentPane(chooseBuild);
 		frame.pack();
 	}
 	
 	/**
 	 * Load and display the panel that show installation progress
 	 * @param buildFile The file in which the installation settings are found
 	 */
 	private void loadInstallPanel(File buildFile) {
 		chooseBuild.removeAll();
 		frame.remove(chooseBuild);
 		
 		installPanel = (JPanel) frame.getContentPane();
 		
 		JButton next = new JButton("Finish");
 		next.setSize(new Dimension(30, 15));
 		next.setEnabled(false);
 		next.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				kill();
 			}
 		});
 		
 		JLabel installText = new JLabel("Installing Darklight using the settings in: " + buildFile.getName());
 		JProgressBar progress = new JProgressBar();
 		
 		installPanel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = new Insets(5, 5, 5, 5);
 		installPanel.add(installText, c);
 		
 		c.gridy = 1;
 		installPanel.add(progress, c);
 		
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.EAST;
 		installPanel.add(next, c);
 		
 		frame.setContentPane(installPanel);
 		frame.pack();
 		
 		initParser(buildFile);
 		progress.setValue(10);
		if (!createFileSystem())
			createFileSystem();
 		progress.setValue(30);
 		copyJar((String) buildParser.get("jar"));
 		progress.setValue(50);
 		copyModules();
 		progress.setValue(100);
 		next.setEnabled(true);
 		buildParser.destroy();
 	}
 }
