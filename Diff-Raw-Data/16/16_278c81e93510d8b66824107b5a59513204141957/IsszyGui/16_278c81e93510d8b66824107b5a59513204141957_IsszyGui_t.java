 package sum.isszy;
 
 import sum.component.*;
 import sum.event.*;
 import sum.isszy.dialog.ISettingsDialog;
 import sum.isszy.dialog.IOverwriteDialog;
 import sum.isszy.Isszy;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import javax.swing.JSplitPane;
 import javax.swing.JOptionPane;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import java.awt.Toolkit;
 
 public class IsszyGui extends JFrame implements FileListListener, DirPanelListener, ActionListener, KeyEventDispatcher
 {
     //source and destenation image sleectors
     private File lister, sorter, trash;
 
     //split pane for left which includes the file list and 2nd split pane
     private JSplitPane sp_left;
       private JSideFileList sfl_list;
     //second split pane which contains image and right buttons
       private JImagePane image = new JImagePane();
       private JPanel east = new JPanel();
         private JDirPanel dp_dirlist;
         private JPanel bottom_right = new JPanel(); 
           private JButton b_mkdir = new JButton("New Folder");
           private JButton b_trash = new JButton("Trash");
 
     private JMenuBar menu = new JMenuBar();
     private JMenu m_file = new JMenu("File");
     private JMenuItem mi_exit = new JMenuItem("Exit");
     private JMenu m_options = new JMenu("Settings");
     private JMenuItem mi_preferences = new JMenuItem("Preferences");
     private JMenu m_help = new JMenu("Help");
     private JMenuItem mi_about = new JMenuItem("About");
 
     //position/size vars
 	private int xcor, ycor, pwidth, pheight;
 
 	//storage variables
 	protected File[] selectedfiles;
 
 	public IsszyGui() {
 		super();
 		readPrefs();
 		construct();
 	}
 
 	private void construct() {
 		//construct menubars
 		m_file.add(mi_exit);
 		m_options.add(mi_preferences);
 		m_help.add(mi_about);
 		menu.add(m_file);
 		menu.add(m_options);
 		menu.add(m_help);
 		mi_preferences.addActionListener(this);
 
 		//setup two main componets with vars specified by previous constructor
 		dp_dirlist = new JDirPanel(sorter);
 		sfl_list = new JSideFileList(lister);
 
 		//setup west panel
 		east.setLayout(new BorderLayout());
 		bottom_right.setLayout(new GridLayout(2, 1));
 		east.add(dp_dirlist, BorderLayout.CENTER);
 		east.add(bottom_right, BorderLayout.SOUTH);
 		bottom_right.add(b_mkdir);
 		bottom_right.add(b_trash);
 
 		//setup SplitPanes
 		sp_left = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sfl_list, image);
 		sp_left.setDividerLocation(sp_left.getMinimumDividerLocation());
 
 		//add listeners
 		sfl_list.addFileListListener(this);
 		dp_dirlist.addDirPanelListener(this);
 		b_mkdir.addActionListener(this);
 		b_trash.addActionListener(this);
 		mi_exit.addActionListener(this);
 		mi_about.addActionListener(this);
 
 		//add everything to Frame
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(sp_left, BorderLayout.CENTER);
 		getContentPane().add(east, BorderLayout.EAST);
 		getContentPane().add(menu, BorderLayout.NORTH);
 
 		//set our size
 		this.setSize(pwidth, pheight);
 
 		//set our title
 		this.setTitle("Isszy");
 
 		//get some information about our screen
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension frameSize = this.getSize();
 		//if x/y cors haven't been established, 
 		//or if they've become invalid, center the window
 		if (xcor < 0 || ycor < 0) {
 			//Center the window 
 
 			if (frameSize.height > screenSize.height) {
 				frameSize.height = screenSize.height;
 			}
 			if (frameSize.width > screenSize.width) {
 				frameSize.width = screenSize.width;
 			}
 			this.setLocation((screenSize.width - frameSize.width) / 2,
 					(screenSize.height - frameSize.height) / 2);
 			//end centering window code
 		} else {
 			this.setLocation(xcor, ycor);
 		}
 
 		//handle window closign 
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				//be sure to save window size/position if user wants them
 				if (IsszyPrefs.getSaveSettings()) {
 					//write all position/size info
 					IsszyPrefs.setHeight(e.getWindow().getHeight());
 					IsszyPrefs.setWidth(e.getWindow().getWidth());
 					IsszyPrefs.setXPosition(e.getWindow().getLocation().x);
 					IsszyPrefs.setYPosition(e.getWindow().getLocation().y);
 				}
				//exit program
				System.exit(0);
 			}//end windowClosing()
 		}//end anonymous innerclass
 				);//end addWindowListner()
 
 		//global keyboard events
 		KeyboardFocusManager manager = KeyboardFocusManager
 				.getCurrentKeyboardFocusManager();
 		manager.addKeyEventDispatcher(this);
 
 	}//end construct()
 
 	public boolean dispatchKeyEvent(KeyEvent e) {
 
 		//pageup or up
 		if (e.getID() == KeyEvent.KEY_PRESSED && (e.getKeyCode() == 38 || e.getKeyCode() == 33) ) {
 			sfl_list.moveFileListUp();
 			return true;
 		} else if (e.getID() == KeyEvent.KEY_PRESSED && (e.getKeyCode() == 34 || e.getKeyCode() == 40)) {
 			sfl_list.moveFileListDown();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	//begin FileListListener implemented functions
 	public void dirEmpty() {
 		selectedfiles = null;
 		try {
 			image.setImage(null);
 		} catch (IOException e) {
 		}
 
 	}
 
 	public void dirSelected(File dir) {
 	}
 
 	public void mountSelected(String mount) {
 	}
 
 	public void filesSelected(File[] files) {
 		selectedfiles = files;
 		//if only one file is selected or the slideshow is turned off,
 		//show one image
 		if (files.length == 1 || IsszyPrefs.getSlideShow() == false) {
 			try {
 				image.setImage(files[0]);
 			} catch (IOException v) {
 				//message box
 				JOptionPane.showMessageDialog(new JFrame(),
 						"Error Loading Image: " + files[0].getName(),
 						"Image Load Error", JOptionPane.ERROR_MESSAGE);
 			}
 		} else //more than one file was selected
 		{
 			try {
 				image.setImage(files, IsszyPrefs.getSlideTime());
 			} catch (IOException l) {
 				//error message
 				JOptionPane.showMessageDialog(new JFrame(),
 						"Error Loading Selected Images", "Image Load Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 
 		}
 	}
 
 	//end FileListListener implemented functions
 
 	//start DirPanelListener implemented function
 	public void directorySelected(File dir) {
 		if (selectedfiles == null) {
 			JOptionPane.showMessageDialog(new JFrame(),
 					"You Must First Select a File", "No Selected File",
 					JOptionPane.WARNING_MESSAGE);
 		} else {
 			for (int x = 0; x < selectedfiles.length; x++) {
 				if (IsszyPrefs.getPromptOverwrite()
 						&& (new File(dir.getPath() + "/"
 								+ selectedfiles[x].getName()).exists())) {
 					if (IOverwriteDialog.showOverwriteDialog(selectedfiles[x],
 							new File(dir.getPath() + "/"
 									+ selectedfiles[x].getName()))) {
 						if (selectedfiles[x].renameTo(new File(dir.getPath()
 								+ "/" + selectedfiles[x].getName())) == false) {
 							JOptionPane.showMessageDialog(new JFrame(),
 									"Unable to Move File", "Error",
 									JOptionPane.ERROR_MESSAGE);
 						}
 					}
 
 				} else {
 					if (selectedfiles[x].renameTo(new File(dir.getPath() + "/"
 							+ selectedfiles[x].getName())) == false) {
 						JOptionPane.showMessageDialog(new JFrame(),
 								"Unable to Move File", "Error",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 			selectedfiles = null;
 			sfl_list.refreshFiles();
 		}
 	}
 
 	//end DirPanelListener implemented function
 
 	//being aciton listener
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == mi_preferences) {
 			ISettingsDialog d = new ISettingsDialog();
 			d.setVisible(true);
 			readPrefs();
 			image.repaint();
 		} else if (e.getSource() == b_mkdir) {
 			String newdir = JOptionPane.showInputDialog(new JFrame(),
 					"Enter a Directory Name", "New Directory",
 					JOptionPane.QUESTION_MESSAGE);
 			if (newdir != null) {
 				if ((new File(IsszyPrefs.getSortDirectory(), newdir)).mkdirs() == false) {
 					JOptionPane.showMessageDialog(new JFrame(),
 							"Unable to Create Directory", "Error",
 							JOptionPane.ERROR_MESSAGE);
 				} else {
 					dp_dirlist.refresh();
 				}
 			}
 		} else if (e.getSource() == b_trash) //send file(s) to trash
 		{
 			for (int x = 0; x < selectedfiles.length; x++) {
 				if (!selectedfiles[x].renameTo(new File(trash, selectedfiles[x]
 						.getName()))) {
 					JOptionPane.showMessageDialog(new JFrame(),
 							"Could not move: " + selectedfiles[x].getName()
 									+ " to trash.", "Error Trashing File",
 							JOptionPane.WARNING_MESSAGE);
 				}
 			}
 			sfl_list.refreshFiles(); //refresh list to show files moved to trash
 		} else if (e.getSource() == mi_exit) {
 			//run our windowClosing() function in the window adapter
 			this
 					.dispatchEvent(new WindowEvent(this,
 							WindowEvent.WINDOW_CLOSING));
 		} else if (e.getSource() == mi_about) {
 			//displays about dialogue
 			JOptionPane.showMessageDialog(new JFrame(), "Isszy "
 					+ Isszy.VERSION + "\n" + "Isszy is a simple Image Sorter\n"
 					+ "Licensed under the GPL\n"
					+ "Copyright 2002,2007 Sumit Khanna\n<sumdog@gmail.com>\n" 
					+ "http://penguindreams.org",
 					"Isszy", JOptionPane.PLAIN_MESSAGE);
 		}
 	}
 
 	private void readPrefs() {
 		//establish preferences
 		xcor = IsszyPrefs.getXPosition();
 		ycor = IsszyPrefs.getYPosition();
 		pwidth = IsszyPrefs.getWidth();
 		pheight = IsszyPrefs.getHeight();
 		image.setZoom(IsszyPrefs.getZoomImage());
 		image.setCenter(IsszyPrefs.getCenterImage());
 		lister = new File(IsszyPrefs.getInitialDirectory());
 		sorter = new File(IsszyPrefs.getSortDirectory());
 		trash = new File(IsszyPrefs.getTrashDirectory());
 		if (sfl_list != null || dp_dirlist != null) {
 			sfl_list.setDir(lister);
 			dp_dirlist.setDir(sorter);
 		}
 	}
 }
 
 
