 package com.vogabe.randomMovie;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class GUI extends JFrame {
 
 	Logger logger = Logger.getLogger("randomMovie");
 	Settings settings = new Settings();
 	VlcExecuter vlcExecuter = new VlcExecuter(settings);
 	RandomFileChooser chooser;
 	String vlcPath;
 
 	public GUI() {
 		PropertyConfigurator.configure("log4j.conf");
 		setRootFrameProps();
 		addVlcButton();
 		addFolderButton();
 		addGoButton();
 	}
 
 	private void addVlcButton() {
 		JButton vlcButton = new JButton("Set Vlc Path");
 		vlcButton.setBounds(10, 10, 280, 60);
 		vlcButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				settings.setVlcPath(buildVlcSelector());
 			}
 		});
 		getContentPane().add(vlcButton);
 	}
 
 	private void addFolderButton() {
 		JButton folderButton = new JButton("Change Folder");
 		folderButton.setBounds(10, 80, 280, 60);
 		folderButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				String path = buildFileChooser();
 				settings.setCurrentFolder(path);
 				chooser = new RandomFileChooser(path);
 			}
 		});
 		getContentPane().add(folderButton);
 	}
 
 	private void addGoButton() {
 		JButton goButton = new JButton("Go!");
 		goButton.setBounds(10, 150, 280, 60);
 		goButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				if(chooser == null){
 					chooser = new RandomFileChooser(settings.getCurrentFolder());
 				}
 				String file = chooser.getRandomMovieFile();
 				vlcExecuter.play(file);
 			}
 		});
 		getContentPane().add(goButton);
 	}
 
 	private void setRootFrameProps() {
		setSize(315, 260);
 		setVisible(true);
 		setLayout(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 	private String buildFileChooser() {
 		JFileChooser chooser = new JFileChooser();
 		chooser.setCurrentDirectory(new java.io.File("/media/Data"));
 		chooser.setDialogTitle("Choose your movie folder");
 		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		chooser.setAcceptAllFileFilterUsed(false);
 		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
 			return "";
 		}
 		File folder = chooser.getSelectedFile();
 		return folder.getAbsolutePath();
 	}
 
 	private String buildVlcSelector() {
 		JFileChooser chooser = new JFileChooser();
 		chooser.setCurrentDirectory(new java.io.File("/media/Data"));
 		chooser.setDialogTitle("Choose your movie folder");
 		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		chooser.setAcceptAllFileFilterUsed(false);
 		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
 			return "";
 		}
 		File vlcLink = chooser.getSelectedFile();
 		return vlcLink.getAbsolutePath();
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				GUI gui = new GUI();
 			}
 		});
 	}
 }
