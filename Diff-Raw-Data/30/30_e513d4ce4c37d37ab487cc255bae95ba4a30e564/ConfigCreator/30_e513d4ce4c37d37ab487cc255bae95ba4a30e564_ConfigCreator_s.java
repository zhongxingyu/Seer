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
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 //import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class ConfigCreator extends JFrame {
 
 	private static final long serialVersionUID = -5059373169601854784L;
 	/* backend variables */
 	private File currentFile;
 	private ConfigModel model;
 	private static final String templateFileName = "/resource/template.conf";
	/* frontend variables */
	private JPanel contentPane;
	private JPanel statusBar;
 	private ConfigController controller;
 	private JFileChooser fileChooser;
 	/**
 	 * Create the frame.
 	 */
 	public ConfigCreator() {
 		/* initialize backend */
 		currentFile = null;
 		model = new ConfigModel();
 		
 		try {
 			model.parseTemplate(getClass().getResourceAsStream(templateFileName));
 //			model.parseTemplate(new FileInputStream("resource/template.conf"));
 		} catch (IOException e1) {
			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.exit(-1);
 		}
 		model.setModifyListener(new ModelChangeListener() {
 			@Override
 			public void modelChanged() {
 				updateTitle();
 			}
 		});
 		
 		controller = new ConfigController(model);
 		
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
 				controller.showPreview();
 			}
 		});
 		mnEdit.add(mntmPreview);
		
		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
		
		statusBar = new JPanel();
 		contentPane.add(statusBar, BorderLayout.SOUTH);
 		
 		contentPane.add(controller.getViewPanel(), BorderLayout.CENTER);
 
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
 	}
 	private void updateTitle() {
 		this.setTitle("ConfigCreator " + (currentFile != null ? currentFile.getAbsolutePath() : "") + (model.isModified() ? " *" : ""));
 	}
 	private void newFile() {
 		closeFile();
 	}
 	private void openFile() {
 		if (model.isModified() && !closeFile()) {
 			return;
 		}
 		int res = fileChooser.showOpenDialog(this);
 		if (res != JFileChooser.APPROVE_OPTION) return;
 		File file = fileChooser.getSelectedFile();
 		try {
 			model.loadFromFile(file);
 		}catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(this, "The file \""+file.getName()+"\" does not exist.", "Open file", JOptionPane.ERROR_MESSAGE);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		currentFile = file;
 		model.setModified(false);
 	}
 	private boolean saveFile() {
 		if (currentFile == null || !currentFile.canWrite()) {
 			return saveAs();
 		} else {
 			try {
 				model.writeToFile(currentFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			model.setModified(false);
 			return true;
 		}
 	}
 	private boolean saveAs() {
 		int res = fileChooser.showSaveDialog(this);
 		if (res != JFileChooser.APPROVE_OPTION) return false;
 		File file = fileChooser.getSelectedFile();
 		try {
 			model.writeToFile(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		currentFile = file;
 		model.setModified(false);
 		return true;
 	}
 	private boolean closeFile() {
 		if (model.isModified()) {
 			int result = JOptionPane.showConfirmDialog(this, "The current file have not been saved. Do you want to save it?");
 			switch (result) {
 			case JOptionPane.YES_OPTION:
 				if(saveFile()){
 					break;
 				} else {
 					return false;
 				}
 			case JOptionPane.NO_OPTION:
 				break;
 			case JOptionPane.CANCEL_OPTION:
 				return false;
 			default:
 				throw new UnknownError("Unexpected JOptionPane returns");
 			}
 		}
 		model.clearValue();
 		currentFile = null;
 		model.setModified(false);
 		return true;
 	}
 	private void exit() {
 		if (closeFile()) {
 			System.exit(0);
 		}
 	}
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
 }
