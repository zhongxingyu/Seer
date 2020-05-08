 package tagEditor;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 
 /**
  * Class represents the View of the MVC.
  * 
  */
 public class View extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	// Declare the UI elements
 	private JLabel songLabel, artistLabel, albumLabel, yearLabel;
 	private JTextField song, artist, album, year;
 	private JScrollPane treeView;
 	// JLabel will have a ImageIcon inside to display the image
 	private JLabel cover;
 	protected JMenuItem jmiSave;
 	protected JMenuItem jmiOpen;
 	protected JButton editCover;
 	private Container informationArea;
 	private JTree tree;
 	private JSplitPane splitPane;
 	
 	public JMenuItem getJmiOpen() {
 		return jmiOpen;
 	}
 
 	public JMenuItem getJmiSave() {
 		return jmiSave;
 	}
 
 
 	public JTextField getSong() {
 		return song;
 	}
 
 	public JTextField getArtist() {
 		return artist;
 	}
 
 	public JTextField getAlbum() {
 		return album;
 	}
 
 	public JTextField getYear() {
 		return year;
 	}
 
 	public JLabel getCover() {
 		return cover;
 	}
 	public JScrollPane getTreeView(){
 		return treeView;
 	}
 	public Container getInformationArea() {
 		return this.informationArea;
 		
 	}
 	public JTree getTree() {
 		return tree;
 	}
 	
 	public JButton getEditCover() {
 		return editCover;
 	}
 
 	/**
 	 * Constructor-Method of the View, will initialize the UI elements, sets the
 	 * listener for the UI representation of the tree and sets the preffered
 	 * sizes.
 	 * 
 	 * @param tree
 	 *            The tree, that will hold the data to be represented
 	 */
 	public View(JTree tree) {
 		// Initialize UI elements
 		this.song = new JTextField("");
 		this.artist = new JTextField("");
 		this.album = new JTextField("");
 		this.year = new JTextField("");
 		this.cover = new JLabel();
 
 		this.songLabel = new JLabel("Titel:");
 		this.artistLabel = new JLabel("Interpret:");
 		this.albumLabel = new JLabel("Album:");
 		this.yearLabel = new JLabel("Jahr:");
 		this.editCover = new JButton("edit Cover");
 		// The tree should be scroll-able
 		this.tree = tree;
 		//tree.setRootVisible(false);
 		this.treeView = new JScrollPane(tree);
 		// Set the preferred dimensions
 		this.song.setPreferredSize(new Dimension(200, 20));
 		this.artist.setPreferredSize(new Dimension(200, 20));
 		this.album.setPreferredSize(new Dimension(200, 20));
 		this.year.setPreferredSize(new Dimension(200, 20));
 		this.editCover.setPreferredSize(new Dimension(100, 20));
 		this.treeView.setPreferredSize(new Dimension(200, 400));
 		
 		// Setup Window
 		this.setTitle("ID3-Tag-Editor");
 		this.setSize(800, 500);
 
 		// Add main menu
 		initMainMenu();
 		//visibility
 		this.setVisibilityOfInfoArea(false);
 
 		setLayout();
 
 	}
 	
 	/**
 	 * Sets the layout of the UI.
 	 */
 	private void setLayout() {
 
 		this.informationArea = new Container();
 		// This Layout isn't optimal
 		informationArea.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.insets = new Insets(10, 10, 10, 10);
 		informationArea.add(this.song, c);
 		c.gridy = 1;
 		informationArea.add(this.artist, c);
 		c.gridy = 2;
 		informationArea.add(this.album, c);
 		c.gridy = 3;
 		informationArea.add(this.year, c);
 		c.gridy = 4;
 		informationArea.add(this.cover, c);
 		c.gridx = 0;
 		c.gridy = 0;
 		informationArea.add(this.songLabel, c);
 		c.gridy = 1;
 		informationArea.add(this.artistLabel, c);
 		c.gridy = 2;
 		informationArea.add(this.albumLabel, c);
 		c.gridy = 3;
 		informationArea.add(this.yearLabel, c);
 		c.gridy = 4;
 		informationArea.add(this.editCover, c);
 
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				this.treeView, informationArea);
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(300);
 		// this.setLayout(new BorderLayout());
 
 		add(splitPane);
 
 	}
 
 	/**
 	 * Adds main menu.
 	 */
 	private void initMainMenu() {
 		JMenuBar mainMenu = new JMenuBar();
 
 		// Menu "Datei"
 		JMenu fileMenu = new JMenu("Datei");
 		fileMenu.setMnemonic('d');
 
		jmiOpen = new JMenuItem("Verzeichnis �ffnen...");
 		jmiOpen.setMnemonic('f');
 		fileMenu.add(jmiOpen);
 
 		jmiSave = new JMenuItem("Metadaten speichern...");
 		jmiSave.setMnemonic('s');
 
 		fileMenu.add(jmiSave);
 
 		fileMenu.add(new JSeparator());
 
 		JMenuItem jmiExit = new JMenuItem("Beenden");
 		jmiExit.setMnemonic('e');
 		fileMenu.add(jmiExit);
 
 		mainMenu.add(fileMenu);
 
 		// Menu "Hilfe"
 		JMenu helpMenu = new JMenu("Hilfe");
 
 		helpMenu.setMnemonic('h');
 		JMenuItem jmiAbout = new JMenuItem("Info");
 		jmiAbout.setMnemonic('i');
 		helpMenu.add(jmiAbout);
 
 		mainMenu.add(helpMenu);
 
 		this.setJMenuBar(mainMenu);
 	}
 	
 	public void setVisibilityOfInfoArea(Boolean b){
 		this.song.setVisible(b);
 		this.songLabel.setVisible(b);
 		this.artist.setVisible(b);
 		this.artistLabel.setVisible(b);
 		this.year.setVisible(b);
 		this.yearLabel.setVisible(b);
 		this.album.setVisible(b);
 		this.albumLabel.setVisible(b);
 		this.cover.setVisible(b);
 		this.editCover.setVisible(b);
 	}
 
 
 
 }
