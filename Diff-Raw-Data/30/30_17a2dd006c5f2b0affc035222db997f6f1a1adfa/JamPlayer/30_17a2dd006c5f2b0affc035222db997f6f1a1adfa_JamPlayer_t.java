 package iitm.apl.player.ui;
 
 import iitm.apl.bktree.BKTree;
 import iitm.apl.bktree.LevenshteinDistance;
 import iitm.apl.player.Song;
 import iitm.apl.player.ThreadedPlayer;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.GroupLayout.Alignment;
 
 /**
  * The JamPlayer Main Class Sets up the UI, and stores a reference to a threaded
  * player that actually plays a song.
  * 
  * TODO: a) Implement the search functionality b) Implement a play-list
  * generation feature
  */
 public class JamPlayer {
 
 	// UI Items
 	private JFrame mainFrame;
 	private PlayerPanel pPanel;
 
 	private JTable libraryTable;
 	private LibraryTableModel libraryModel;
 
 	private Thread playerThread = null;
 	private ThreadedPlayer player = null;
 
 	public JamPlayer() {
 		// Create the player
 		player = new ThreadedPlayer();
 		playerThread = new Thread(player);
 		playerThread.start();
 	}
 	
 	/* My declarations */
 	public BKTree<String> songTree = new BKTree<String>(new LevenshteinDistance());
 	public String searchTerm = new String();
 	
 
 	/**
 	 * Create a file dialog to choose MP3 files to add
 	 */
 	Vector<Song> inputSongs;
 	private Vector<Song> addFileDialog() {
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		int returnVal = chooser.showOpenDialog(null);
 		if (returnVal != JFileChooser.APPROVE_OPTION)
 			return null;
 		inputSongs = new Vector<Song>();
 		File selectedFile = chooser.getSelectedFile();
 		
 		return recursiveFileAdd(selectedFile);
 	}
 	private Vector<Song> recursiveFileAdd(File selectedFile)
 	{
 		if (selectedFile.isFile() && selectedFile.getName().toLowerCase().endsWith(".mp3")) 
 		{
 			inputSongs.add(new Song(selectedFile));
 			return inputSongs;
 		}
 		if(selectedFile.isDirectory())
 		{
 			for(File file : selectedFile.listFiles())
 			{
 				if(file.getName().toLowerCase().endsWith(".mp3")) inputSongs.add(new Song(file));
 				if(file.isDirectory()) recursiveFileAdd(file);
 			}
 		}
 		return inputSongs;
 	}
 
 	/**
 	 * Create the GUI and show it. For thread safety, this method should be
 	 * invoked from the event-dispatching thread.
 	 */
 	private void createAndShowGUI() {
 		// Create and set up the window.
 		mainFrame = new JFrame("AMP - The Ultimate Music Player");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setMinimumSize(new Dimension(300, 400));
 
 		// Create and set up the content pane.
 		Container pane = mainFrame.getContentPane();
 		pane.add(createMenuBar(), BorderLayout.NORTH);
 		pane.add(Box.createHorizontalStrut(30), BorderLayout.EAST);
 		pane.add(Box.createHorizontalStrut(30), BorderLayout.WEST);
 		pane.add(Box.createVerticalStrut(30), BorderLayout.SOUTH);
 
 		JPanel mainPanel = new JPanel();
 		GroupLayout layout = new GroupLayout(mainPanel);
 		mainPanel.setLayout(layout);
 
 		pPanel = new PlayerPanel(player);
 		JLabel searchLabel = new JLabel("Search: ");
 		final JTextField searchText = new JTextField(200);
 		searchText.setMaximumSize(new Dimension(200, 20));
 		searchText.addKeyListener(new KeyListener() {
 			@Override
 			public void keyTyped(KeyEvent arg0) 
 			{
 				// TODO: Handle the case when the text field has been modified.
 				// Optional: Can you update the search "incrementally" i.e. as
 				// and when the user changes the search text?
 				searchText.validate();
 				searchTerm = searchText.getText();
 				if(arg0.getKeyChar() != '\b')
 				{
 					searchTerm = searchTerm.concat(Character.toString(arg0.getKeyChar()));
 				}
 				System.out.println(searchTerm);
 				libraryModel.filter(searchTerm, songTree, songVector, wordToSong);
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 			}
 		});
 
 		libraryModel = new LibraryTableModel();
 		libraryTable = new JTable(libraryModel);
 		libraryTable.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if (arg0.getClickCount() > 1) {
 					Song song = libraryModel.get(libraryTable.getSelectedRow());
 					if (song != null) {
 						player.setSong(song);
 						pPanel.setSong(song);
						pPanel.playPauseButton.setIcon(pPanel.pause);
 					}
 				}
 			}
 		});
 
 		libraryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		JScrollPane libraryPane = new JScrollPane(libraryTable);
 
 		layout.setHorizontalGroup(layout
 				.createParallelGroup(Alignment.CENTER)
 				.addComponent(pPanel)
 				.addGroup(
 						layout.createSequentialGroup().addContainerGap()
 								.addComponent(searchLabel)
 								.addComponent(searchText).addContainerGap())
 				.addComponent(libraryPane));
 
 		layout.setVerticalGroup(layout
 				.createSequentialGroup()
 				.addComponent(pPanel)
 				.addContainerGap()
 				.addGroup(
 						layout.createParallelGroup(Alignment.CENTER)
 								.addComponent(searchLabel)
 								.addComponent(searchText))
 				.addComponent(libraryPane));
 
 		pane.add(mainPanel, BorderLayout.CENTER);
 
 		// Display the window.
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 	public Vector<Song> songVector = new Vector<Song>();
 	private JMenuBar createMenuBar() {
 		JMenuBar mbar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem addSongs = new JMenuItem("Add new files to library");
 		addSongs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Vector<Song> songs = addFileDialog();
 				for(Song song : songs)
 				{
 					boolean dontAdd = false;
 					for(Song song_2 : songVector)
 					{
 						if(song.isSameAs(song_2)) dontAdd = true;
 					}
 					if(dontAdd == false) songVector.add(song);
 				}
 				System.out.println("The size of songVector is " + songVector.size());
 				if (songs != null)
 				{
 					/* Adds the elements to a BKTree */
 					songTree = VectorToBKTree(songVector);
 					libraryModel.clearSongListing();
 					libraryModel.add(songVector);
 				}
 			}
 		});
 		file.add(addSongs);
 		
 		JMenu sortPlaylist = new JMenu("Sort playlist");
 		JMenuItem byTitle = new JMenuItem("By Song Title");
 		JMenuItem byArtist = new JMenuItem("By Song Artist");
 		JMenuItem byAlbum = new JMenuItem("By Song Album");
 		JMenuItem byDuration = new JMenuItem("By Song Duration");
 		sortPlaylist.add(byTitle);
 		sortPlaylist.add(byArtist);
 		sortPlaylist.add(byAlbum);
 		sortPlaylist.add(byDuration);
 		byTitle.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				libraryModel.sortSongListing(1);
 			}
 		});
 		byArtist.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				libraryModel.sortSongListing(3);
 			}
 		});
 		byAlbum.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				libraryModel.sortSongListing(2);
 			}
 		});
 		byDuration.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				libraryModel.sortSongListing(4);
 			}
 		});
 		file.add(sortPlaylist);
 		
 		JMenuItem setFilterLevel = new JMenuItem("Set Search Filter Level");
 		
 		setFilterLevel.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String thresh = JOptionPane.showInputDialog("Set Filter Level : 0 = strict, 5 = lenient, 2 = optimum", "2");
 				int threshold = Integer.parseInt(thresh);
 				if(threshold < 0 || threshold > 5) 
 				{
 					JOptionPane.showMessageDialog(null, "Invalid Filter Level");
 					return;
 				}
 				LibraryTableModel.thresholdDistance = threshold;
 			}
 		});
 		file.add(setFilterLevel);
 		
 		JMenuItem quitItem = new JMenuItem("Quit");
 		quitItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainFrame.dispose();
 			}
 		});
 		file.add(quitItem);
 
 		mbar.add(file);
 
 		return mbar;
 	}
 
 	protected void createPlayListHandler() {
 		// TODO: Create a dialog window allowing the user to choose length of
 		// play list, and a play list you create that best fits the time
 		// specified
 		PlayListMakerDialog dialog = new PlayListMakerDialog(this);
 		dialog.setVisible(true);
 	}
 	
 	public Vector<Song> getSongList()
 	{
 		Vector<Song> songs = new Vector<Song>();
 		for(  int i = 0; i< libraryModel.getRowCount(); i++)
 			songs.add(libraryModel.get(i));
 		return songs;
 	}
 	
 	/* Method to convert Vector of Songs to a BKTree of Strings with the song "title" - NOTE TITLE */
 	Hashtable wordToSong = new Hashtable();
 	public BKTree<String> VectorToBKTree(Vector<Song> songs) 
 	{
 		BKTree<String> bktree = new BKTree<String>(new LevenshteinDistance());
 		for(Song song : songs)
 		{
 			String title = song.getTitle().toLowerCase();
 			String[] titleWords = title.split(" ");
 			for(String string : titleWords)
 			{
 				Vector<Song> list = new Vector<Song>();
 				/* If the hashtable contains the string already, add the song to the list that is hashed by the string*/
 				if(wordToSong.containsKey(string))
 				{
 					list = (Vector<Song>) wordToSong.get(string);
 				}
 				/* If not, or either way, just add the song to the list, and the add the set to the hashtable */
 				list.add(song);
 				wordToSong.remove(string);
 				wordToSong.put(string, list);
 				bktree.add(string);
 				//System.out.println(wordToSong);
 			}
 			String album = song.getAlbum().toLowerCase();
 			String[] albumWords = album.split(" ");
 			for(String string : albumWords)
 			{
 				Vector<Song> list = new Vector<Song>();
 				/* If the hashtable contains the string already, add the song to the list that is hashed by the string*/
 				if(wordToSong.containsKey(string))
 				{
 					list = (Vector<Song>) wordToSong.get(string);
 				}
 				/* If not, or either way, just add the song to the list, and the add the set to the hashtable */
 				list.add(song);
 				wordToSong.remove(string);
 				wordToSong.put(string, list);
 				bktree.add(string);
 				//System.out.println(wordToSong);
 			}
 			String artist = song.getArtist().toLowerCase();
 			String[] artistWords = artist.split(" ");
 			for(String string : artistWords)
 			{
 				Vector<Song> list = new Vector<Song>();
 				/* If the hashtable contains the string already, add the song to the list that is hashed by the string*/
 				if(wordToSong.containsKey(string))
 				{
 					list = (Vector<Song>) wordToSong.get(string);
 				}
 				/* If not, or either way, just add the song to the list, and the add the set to the hashtable */
 				list.add(song);
 				wordToSong.remove(string);
 				wordToSong.put(string, list);
 				bktree.add(string);
 				//System.out.println(wordToSong);
 			}
 		}
 		return bktree;
 	}
 	public static void main(String[] args) {
 		// Schedule a job for the event-dispatching thread:
 		// creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				JamPlayer player = new JamPlayer();
 				player.createAndShowGUI();
 			}
 		});
 	}
 }
