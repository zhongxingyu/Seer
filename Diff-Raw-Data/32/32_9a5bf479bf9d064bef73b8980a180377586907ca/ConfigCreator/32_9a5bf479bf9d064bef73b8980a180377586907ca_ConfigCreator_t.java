 package org.monospace.configcreator;
 import java.awt.EventQueue;
 
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 import javax.swing.UIManager;
 
 import org.monospace.configcreator.ConfigComponent.EditEvent;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.awt.GridLayout;
 
 public class ConfigCreator extends JFrame {
 
 	private static final long serialVersionUID = -5059373169601854784L;
 	/* backend variables */
 	private File currentFile;
 	private ConfigSet configSet;
	private static final String templateFileName = "/resource/template.conf";
 	/* frontend variables */
 	private JPanel contentPane;
 	private JPanel statusBar;
 	private JPanel configPanel;
 	private JFileChooser fileChooser;
 	private ConfigPreviewFrame preview;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					ConfigCreator frame = new ConfigCreator();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	/**
 	 * Create the frame.
 	 */
 	public ConfigCreator() {
 		/* initialize backend */
 		currentFile = null;
 		configSet = new ConfigSet();
 		try {
			configSet.parseTemplate(getClass().getResourceAsStream(templateFileName));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.exit(-1);
 		}
 		configSet.setListener(new ConfigComponent.EditListener() {
 			@Override
 			public void contentChanged(EditEvent e) {
 				preview.refreshContent();
 			}
 		});
 		
 		/* initialize UI */
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationByPlatform(true);
 		setTitle("Config Creator");
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmNew = new JMenuItem("New...");
 		mntmNew.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				newFile();
 			}
 		});
 		mnFile.add(mntmNew);
 		
 		JMenuItem mntmLoad = new JMenuItem("Load...");
 		mntmLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				openFile();
 			}
 		});
 		mnFile.add(mntmLoad);
 		
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mntmSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveFile();
 			}
 		});
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
 		mntmSaveAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveAs();
 			}
 		});
 		mnFile.add(mntmSaveAs);
 		
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 		
 		JMenuItem mntmQuit = new JMenuItem("Quit");
 		mntmQuit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				exit();
 			}
 		});
 		mnFile.add(mntmQuit);
 		
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 		
 		JMenuItem mntmPreview = new JMenuItem("Preview...");
 		mntmPreview.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				preview.refreshContent();
 				preview.setVisible(true);
 			}
 		});
 		mnEdit.add(mntmPreview);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		statusBar = new JPanel();
 		contentPane.add(statusBar, BorderLayout.SOUTH);
 		
 		configPanel = new JPanel();
 		contentPane.add(configPanel, BorderLayout.CENTER);
 		configPanel.setLayout(new GridLayout(0, 3, 5, 5));
 		
 		for (int i = 0; i < configSet.size(); i++) {
 			configPanel.add(configSet.getComponent(i).getNameLabel());
 			configPanel.add(configSet.getComponent(i).getInput());
 			configPanel.add(configSet.getComponent(i).getMessageLabel());
 		}
 
 		setSize(this.getPreferredSize());
 		
 		fileChooser = new JFileChooser();
 		fileChooser.setFileFilter(new FileFilter() {
 			@Override
 			public String getDescription() {
 				return "Config file (system.conf)";
 			}
 			@Override
 			public boolean accept(File arg0) {
 				return (arg0.getName().equals("system.conf") && arg0.isFile()) || arg0.isDirectory();
 			}
 		});
 		fileChooser.setSelectedFile(new File("system.conf"));
 		
 		preview = new ConfigPreviewFrame(configSet);
 		preview.setVisible(false);
 	}
 	private void newFile() {
 		closeFile();
 	}
 	private void openFile() {
 		if (configSet.isModified() && !closeFile()) {
 			return;
 		}
 		int res = fileChooser.showOpenDialog(this);
 		if (res != JFileChooser.APPROVE_OPTION) return;
 		File file = fileChooser.getSelectedFile();
 		try {
 			configSet.loadFromFile(file);
 		}catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(this, "The file \""+file.getName()+"\" does not exist.", "Open file", JOptionPane.ERROR_MESSAGE);
 		} catch (IOException | RuntimeException e) {
 			e.printStackTrace();
 			return;
 		}
 		configSet.setModified(false);
 		currentFile = file;
 	}
 	private boolean saveFile() {
 		if (currentFile == null || !currentFile.canWrite()) {
 			return saveAs();
 		} else {
 			try {
 				configSet.writeToFile(currentFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			configSet.setModified(false);
 			return true;
 		}
 	}
 	private boolean saveAs() {
 		int res = fileChooser.showSaveDialog(this);
 		if (res != JFileChooser.APPROVE_OPTION) return false;
 		File file = fileChooser.getSelectedFile();
 		try {
 			configSet.writeToFile(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		configSet.setModified(false);
 		currentFile = file;
 		return true;
 	}
 	private boolean closeFile() {
 		if (configSet.isModified()) {
 			int result = JOptionPane.showConfirmDialog(this, "The current file have not been saved. Do you want to save it?");
 			switch (result) {
 			case JOptionPane.YES_OPTION:
 				if(saveFile()){
 					break;
 				} else {
 					return false;
 				}
 			case JOptionPane.NO_OPTION:
 				return true;
 			case JOptionPane.CANCEL_OPTION:
 				return false;
 			default:
 				throw new UnknownError("Unexpected JOptionPane returns");
 			}
 		}
 		configSet.clearValue();
 		currentFile = null;
 		configSet.setModified(false);
 		return true;
 	}
 	private void exit() {
 		if (closeFile()) {
 			System.exit(0);
 		}
 	}
 }
