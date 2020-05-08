 package gui;
 import images.TaggableImage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import application.ImageLoader;
 
 
 public class ApplicationWindow extends JFrame implements ActionListener, WindowListener {
 
 	private ImageLoader imageLoader;
 	private static final long serialVersionUID = 1L;
 	private File[] selectedImages = null;
 
 	public ApplicationWindow(){
 		setLayout(new FlowLayout());
 		setResizable(false);
 		addWindowListener(this);
 		initialiseMenus();
 		initialiseWindow();
 		initialiseApplication();
 
 		pack();
 		setVisible(true);
 	}
 
 	public void initialiseApplication(){
 		imageLoader = new ImageLoader();
 	}
 
 	public void initialiseMenus(){
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		menuBar.add(file);
 		JMenuItem importItem = new JMenuItem("Import");
 		JMenuItem exportItem = new JMenuItem("Export");
 		JMenuItem quitItem = new JMenuItem("Quit");
 		importItem.addActionListener(this);
 		exportItem.addActionListener(this);
 		quitItem.addActionListener(this);
 		file.add(importItem);
 		file.add(exportItem);
 		file.add(quitItem);
 
 		JMenu option = new JMenu("Option");
 		menuBar.add(option);
 
 		JMenuItem flagItem = new JMenuItem("Flag Image");
 		JMenuItem unflagItem = new JMenuItem("Unflag Image");
 		JMenuItem preferencesItem = new JMenuItem("Preferences");
 		flagItem.addActionListener(this);
 		unflagItem.addActionListener(this);
 		preferencesItem.addActionListener(this);
 		option.add(flagItem);
 		option.add(unflagItem);
 		option.add(preferencesItem);
 
 		JMenu help = new JMenu("Help");
 		menuBar.add(help);
 
 		JMenuItem about = new JMenuItem("About");
 		JMenuItem manual = new JMenuItem("Manual");
 		about.addActionListener(this);
 		manual.addActionListener(this);
 		help.add(manual);
 		help.add(about);
 		setJMenuBar(menuBar);
 	}
 	private void initialiseWindow(){		
 		//initialise empty image grid jm 080912
 		ImageGridPanel leftPanel = new ImageGridPanel(null);
 		JScrollPane leftPane = new JScrollPane(leftPanel);
 
 		JPanel rightPanel = new JPanel();
 		leftPane.setPreferredSize(new Dimension(250, 600));
 		leftPanel.setBackground(new Color(225, 0, 0));
 
 		rightPanel.setPreferredSize(new Dimension(550, 600));
 		rightPanel.setBackground(new Color(0, 225, 0));
 
 		
 		add(leftPane);
 		add(rightPanel);
 
 		pack();
 		setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 		Debug: System.out.println(action); //jm 070912
 
 		if (action.equals("Import")) {
 			//import features
 			selectedImages = imageLoader.importImages(this);
 			initialiseWindow();
 		}
 		if (action.equals("Export")) {
 			//export features
 		}
 		if (action.equals("Quit")) {
 			//quit popup
 		}
 		if (action.equals("Flag Image")) {
 			//flag currently selected image
 		}
 		if (action.equals("Unflag Image")) {
 			//unflag the selected image
 		}
 		if (action.equals("Preferences")) {
 			//open preferences window
 		}
 		if (action.equals("Manual")) {
 			//manual features
 		}
 		if (action.equals("About")) {
 			//about dialog
 		}
 	}
 
 	public void windowOpened(WindowEvent e) {}
 	public void windowClosing(WindowEvent e) {
 		System.exit(0);
 	}
 	public void windowClosed(WindowEvent e) {}
 	public void windowIconified(WindowEvent e) {}
 	public void windowDeiconified(WindowEvent e) {}
 	public void windowActivated(WindowEvent e) {}
 	public void windowDeactivated(WindowEvent e) {}
 }
