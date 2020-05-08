 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 package jscribble;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GraphicsDevice;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import jscribble.notebook.NoteBook;
 
 /**
  * A window and launcher for individual NoteBook. It searches the
  * configuration directory for NoteBook entries and provides a list to open
  * them. A NoteBook can then be opened in a DrawPanel.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class NotebookSelectionWindow {
 	/**
 	 * The size of newly created NoteBook instances.
 	 */
 	private Dimension noteSize = new Dimension(1024, 600);
 
 	/**
 	 * Button to handle creation of a new NoteBook.
 	 */
 	private ButtonNew buttonNew = new ButtonNew();
 
 	/**
 	 * Button to handle opening of a NoteBook.
 	 */
 	private ButtonOpen buttonOpen = new ButtonOpen();
 
 	/**
 	 * Button to handle deletion of a NoteBook.
 	 */
 	private ButtonDelete buttonDelete = new ButtonDelete();
 
 	/**
 	 * Button to handle config.
 	 */
 	private ButtonConfig buttonConfig = new ButtonConfig();
 
 	/**
 	 * Button to enter the scribble mode.
 	 */
 	private ButtonScribble buttonScribble = new ButtonScribble();
 
 
 	/**
 	 * Frame to display everything in.
 	 */
 	private JFrame frame;
 
 	/**
 	 * List that holds all the found NoteBook from the user's configuration
 	 * directory.
 	 */
 	private LinkedList<NoteBook> notebooks;
 
 	/**
 	 * String representations of the NoteBook items in the LinkedList.
 	 */
 	private String[] listData;
 
 	/**
 	 * List GUI Element to display the NoteBook items in.
 	 */
 	private JList myList;
 
 	/**
 	 * The central configuration file for storing options like the default
 	 * NoteBook directory.
 	 */
 	private File configdir = new File(System.getProperty("user.home") + File.separator + ".jscribble");
 
 
 
 	/**
 	 * Panel to display the selected NoteBook.
 	 */
 	private DrawPanel panel;
 
 	private File centralConfigFile;
 
 	/**
 	 * Creates a new window to select NoteBook from. It automatically searches
 	 * the user's configuration directory for NoteBook configuration files.
 	 */
 	public NotebookSelectionWindow() {
 		notebooks = findNotebooks();
 
 		// TODO open NoteBook when double clicking on the list
 		updateList();
 
 		GridLayout gl = new GridLayout(2, 3);
 		JPanel buttonPanel = new JPanel(gl);
 		buttonPanel.add(buttonNew);
 		buttonPanel.add(buttonOpen);
 		buttonPanel.add(buttonScribble);
 		buttonPanel.add(buttonDelete);
 		buttonPanel.add(buttonConfig);
 
 		JPanel mainPanel = new JPanel(new BorderLayout());
 		mainPanel.add(new JScrollPane(myList), BorderLayout.CENTER);
 		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
 
 
 		frame = new JFrame("Select your Notebook");
 		frame.setSize(new Dimension(300, 400));
 		frame.setLocationRelativeTo(null);
 		frame.add(mainPanel);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		centralConfigFile = new File(
 		    configdir.getAbsolutePath() +
 		    File.separator +
 		    "jscribblerc"
 		);
 		getCentralConfig();
 	}
 
 
 	/**
 	 * Displays the dialogue.
 	 */
 	public void showDialog() {
 		frame.setVisible(true);
 	}
 
 
 	/**
 	 * Tries to find a configuration directory for this program.
 	 *
 	 * @return list of NoteBook
 	 */
 	private LinkedList<NoteBook> findNotebooks() {
 		LinkedList<NoteBook> notebooks = new LinkedList<NoteBook>();
 
 		if (configdir.exists()) {
 			File[] configfiles = configdir.listFiles(new FilenameFilter() {
 
 				@Override
 				public boolean accept(File arg0, String arg1) {
 					return arg1.contains(NoteBookProgram.configFileSuffix);
 				}
 
 			});
 
 			for (File configfile : configfiles) {
 				notebooks.add(new NoteBook(configfile));
 			}
 		}
 		else {
 			configdir.mkdirs();
 		}
 
 		return notebooks;
 	}
 
 	/**
 	 * Loads the central properties from the rc file. If this does not exist,
 	 * the user is polled for the options.
 	 *
 	 * @return the central properties
 	 */
 	private Properties getCentralConfig() {
 		// load the default folder for notebooks
 		Properties centralConfig = new Properties();
 
 		File defaultDirectory = null;
 
 		if (centralConfigFile.exists()) {
 			try {
 				centralConfig.loadFromXML(new FileInputStream(centralConfigFile));
 				defaultDirectory = new File(centralConfig.getProperty("defaultDirectory"));
 			}
 			catch (InvalidPropertiesFormatException e) {
 				NoteBookProgram.handleError("The config file is not valid.");
 				e.printStackTrace();
 			}
 			catch (FileNotFoundException e) {
 				NoteBookProgram.handleError("Could not find config file.");
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				NoteBookProgram.handleError("IO error while reading config file.");
 				e.printStackTrace();
 			}
 		}
 
 
 		if (!centralConfigFile.exists() || (defaultDirectory != null && !defaultDirectory.exists())) {
 			centralConfig.setProperty("defaultDirectory", pollUserForDefaultDir(!centralConfigFile.exists()).getAbsolutePath());
 
 			saveConfig(centralConfig);
 		}
 
 		return centralConfig;
 	}
 
 
 	private void saveConfig(Properties centralConfig) {
 		// save this new rc
 		try {
 			centralConfig.storeToXML(new FileOutputStream(centralConfigFile), NoteBookProgram.generatedComment());
 		}
 		catch (FileNotFoundException e) {
 			NoteBookProgram.handleError("Could not find config file.");
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			NoteBookProgram.handleError("IO error while writing config file.");
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Asks the user to select a default directory for his NoteBook.
 	 *
 	 * @param firstTime whether the configuration file did not have an entry
 	 * before
 	 * @return the chosen directory
 	 */
 	private File pollUserForDefaultDir(boolean firstTime) {
 		// ask the user for a default directory for his NoteBook
 		String message = firstTime ?
 		                 "Please select a default folder for your NoteBooks in the following dialog." :
 		                 "Your default directory is not valid. Please choose a new one.";
 		JOptionPane.showMessageDialog(null, message);
 
 		JFileChooser defaultDirectoryChooser = new JFileChooser();
 		defaultDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 
 		int result = defaultDirectoryChooser.showOpenDialog(null);
 
 		File newDefaultDirectory = null;
 		if (result == JFileChooser.APPROVE_OPTION) {
 			newDefaultDirectory = defaultDirectoryChooser.getSelectedFile();
 		}
 
 		// if there is no file selected, use the users home folder
 		if (newDefaultDirectory == null) {
 			newDefaultDirectory = new File(System.getProperty("user.home"));
 		}
 
 		return newDefaultDirectory;
 	}
 
 
 	/**
 	 * Creates a new NoteBook and prompts the user for a name and folder to
 	 * save the images in. A config file is automatically created in the config
	 * directory.
 	 *
 	 * @return the new NoteBook
 	 */
 	private NoteBook createNewNotebook() {
 		String nickname = JOptionPane.showInputDialog("Nickname of your Notebook:");
 
 		if (nickname == null) {
 			return null;
 		}
 
 		// TODO clean up name for use as file name
 
 
 		File defaultDirectory = new File(getCentralConfig().getProperty("defaultDirectory"));
 
 		File in = null;
 
 		JFileChooser loadChooser = new JFileChooser();
 		loadChooser.setName("select folder for storing this NoteBook");
 		loadChooser.setCurrentDirectory(defaultDirectory);
 		loadChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 
 		int result = loadChooser.showOpenDialog(null);
 		if (result == JFileChooser.APPROVE_OPTION) {
 			in = loadChooser.getSelectedFile();
 		}
 
 		// if there is no file selected, abort right here
 		if (in == null) {
 			return null;
 		}
 
 		NoteBook nb = new NoteBook(noteSize, in, nickname);
 		nb.saveToConfig(configdir);
 
 		return nb;
 	}
 
 
 	/**
 	 * Updates the list with NoteBook.
 	 */
 	private void updateList() {
 		// FIXME really update the list if it is changed
 		listData = new String[notebooks.size()];
 		int j = 0;
 		for (Iterator<NoteBook> iterator = notebooks.iterator(); iterator.hasNext(); j++) {
 			NoteBook noteBook = (NoteBook) iterator.next();
 			listData[j] = noteBook.toString();
 		}
 
 
 		myList = new JList(listData);
 		if (myList != null && myList.isShowing()) {
 			myList.repaint();
 		}
 
 		buttonOpen.setEnabled(notebooks.size() > 0);
 	}
 
 
 	/**
 	 * Opens the given NoteBook in a DrawPanel.
 	 *
 	 * @param notebook NoteBook to open
 	 */
 	private void openNotebook(final NoteBook notebook) {
 		JFrame f = new JFrame(String.format("Notebook \"%s\"", notebook.getName()));
 		f.setSize(noteSize);
 		f.setLocationRelativeTo(null);
 
 		f.addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowClosing(WindowEvent winEvt) {
 				notebook.saveToFiles();
 				System.exit(0);
 			}
 		});
 
 
 		panel = new DrawPanel(notebook);
 		f.add(panel);
 
 
 		f.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent arg0) {}
 
 			public void keyReleased(KeyEvent ev) {
 				if (ev.getKeyChar() == 'j' ||
 				        ev.getKeyCode() == KeyEvent.VK_DOWN ||
 				        ev.getKeyCode() == KeyEvent.VK_RIGHT ||
 				        ev.getKeyCode() == KeyEvent.VK_SPACE ||
 				ev.getKeyCode() == KeyEvent.VK_ENTER) {
 					notebook.goForward();
 				}
 
 				if (ev.getKeyChar() == 'k' ||
 				        ev.getKeyCode() == KeyEvent.VK_UP ||
 				        ev.getKeyCode() == KeyEvent.VK_LEFT ||
 				ev.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
 					notebook.goBackwards();
 				}
 
 				if (ev.getKeyChar() == 'f' ||
 				ev.getKeyCode() == KeyEvent.VK_HOME) {
 					notebook.gotoFirst();
 				}
 
 				if (ev.getKeyChar() == 'l' ||
 				ev.getKeyCode() == KeyEvent.VK_END) {
 					notebook.gotoLast();
 				}
 			}
 
 			public void keyTyped(KeyEvent arg0) {}
 		});
 
 		notebook.gotoLast();
 
 
 		ColonListener cl = new ColonListener(panel);
 		f.addKeyListener(cl);
 		cl.addChangeListener(new Redrawer(panel));
 
 
 		if (Toolkit.getDefaultToolkit().getScreenSize().equals(noteSize)) {
 			GraphicsDevice myDevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
 			if (myDevice.isFullScreenSupported()) {
 				f.setUndecorated(true);
 				f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 				myDevice.setFullScreenWindow(f);
 
 				f.setLocation(0, 0);
 			}
 		}
 
 		f.setVisible(true);
 	}
 
 
 	/**
 	 * Button to create a new NoteBook.
 	 *
 	 * @author Martin Ueding <dev@martin-ueding.de>
 	 */
 	@SuppressWarnings("serial")
 	private class ButtonNew extends JButton implements ActionListener {
 		ButtonNew() {
 			setText("New");
 
 			addActionListener(this);
 		}
 
 		/**
 		 * Triggers the creation of a new NoteBook.
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			NoteBook newNoteBook = createNewNotebook();
 			if (newNoteBook != null) {
 				notebooks.add(newNoteBook);
 				updateList();
 				openNotebook(newNoteBook);
 			}
 
 		}
 	}
 
 
 	/**
 	 * Button to open a NoteBook.
 	 *
 	 * @author Martin Ueding <dev@martin-ueding.de>
 	 */
 	@SuppressWarnings("serial")
 	private class ButtonOpen extends JButton implements ActionListener {
 		ButtonOpen() {
 			setText("Open");
 			setEnabled(false);
 
 			addActionListener(this);
 		}
 
 		/**
 		 * Triggers the opening of a NoteBook, if one was selected.
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int selection = myList.getSelectedIndex();
 
 			if (selection >= 0) {
 				openNotebook(notebooks.get(selection));
 			}
 		}
 	}
 
 
 	/**
 	 * Button to enter the Scribble mode.
 	 *
 	 * @author Martin Ueding <dev@martin-ueding.de>
 	 */
 	@SuppressWarnings("serial")
 	private class ButtonScribble extends JButton implements ActionListener {
 		public ButtonScribble() {
 			setText("Scribble");
 
 			addActionListener(this);
 		}
 
 		/**
 		 * Triggers the entering into scribble mode with a blank new temporary
 		 * NoteBook.
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			openNotebook(new NoteBook(noteSize, null, null));
 		}
 	}
 
 	/**
 	 * Button to delete a NoteBook.
 	 *
 	 * @author Martin Ueding <dev@martin-ueding.de>
 	 */
 	@SuppressWarnings("serial")
 	private class ButtonDelete extends JButton implements ActionListener {
 		ButtonDelete() {
 			setText("Delete");
 
 			addActionListener(this);
 		}
 
 		/**
 		 * Triggers the deletion of a NoteBook.
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int selection = myList.getSelectedIndex();
 
 			if (selection >= 0) {
 				notebooks.get(selection).delete();
 				updateList();
 			}
 
 		}
 	}
 
 	/**
 	 * Button to go through the config dialog.
 	 *
 	 * @author Martin Ueding <dev@martin-ueding.de>
 	 */
 	@SuppressWarnings("serial")
 	private class ButtonConfig extends JButton implements ActionListener {
 		ButtonConfig() {
 			setText("Config");
 
 			addActionListener(this);
 		}
 
 		/**
 		 * Triggers the configuration dialog.
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			Properties p = getCentralConfig();
 			p.setProperty("defaultDirectory", pollUserForDefaultDir(true).getAbsolutePath());
 			saveConfig(p);
 		}
 	}
 }
 
