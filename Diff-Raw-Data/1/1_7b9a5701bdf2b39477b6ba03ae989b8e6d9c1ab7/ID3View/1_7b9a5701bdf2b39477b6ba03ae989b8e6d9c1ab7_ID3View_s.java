 package awesome;
 
 
 import javax.swing.*;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeSelectionModel;
 
 import java.awt.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.awt.event.*;
 
 public class ID3View extends JFrame implements TreeSelectionListener, TreeExpansionListener {
 	
 	private static final long serialVersionUID = 3797307884995261587L;
 	private JTextField titleField;
 	private JTextField albumField;
 	private JTextField yearField;
 	private JTextField artistField;
 
 	private JTree fileTree;
 	private ImageIcon coverIcon; //we need ImageContainer and not JPanel because we want to set the image!
 	private JLabel coverContainer;
 	
 	private JSplitPane splitPane;
 	
 	public ID3View() {
 		setSize(700, 500);
 		setTitle("Awesome ID3");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //TODO: Change to ID3Controller.exitApplication()
 		
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		getContentPane().add(splitPane, BorderLayout.CENTER);
 		createTree();
 		createMenu();
 		createDetailForm();
 		//recalculatedividerLocation(); //raises an exception
 	}
 	
 	
 	/**
 	 * creates and initializes the menu bar of the frame and its subcomponents.
 	 */
 	
 	private void createMenu(){
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu menuMain = new JMenu("Awesome ID3");
 		
 		JMenuItem itemReload = new JMenuItem("Reload MP3 Files");
 		JMenuItem itemChangeDir = new JMenuItem("Choose Music Directory...");
 		JMenuItem itemExit = new JMenuItem("Exit Awesome ID3");
 		
 		itemExit.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				ID3Controller.getController().exitApplication();
 			}
 			
 		});
 		
 		menuMain.add(itemReload);
 		//menuMain.addSeparator(); //maybe it's prettier
 		menuMain.add(itemChangeDir);
 		menuMain.addSeparator();
 		menuMain.add(itemExit);
 		
 		menuBar.add(menuMain);
 		
 		this.setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * creates and initializes the tree used to present the directory structure.
 	 */
 	
 	private void createTree(){
 		// initializes the tree
 		//TODO: Initialize with correct directory
 		DefaultMutableTreeNode topNode = buildFileTree(new Directory(new File(FileSystemView.getFileSystemView().getHomeDirectory(),"/Music")));
 		//in "" you can add a subpath e.g. /Music/iTunes for mac users
 		
 		fileTree = new JTree(topNode);
 		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		fileTree.addTreeSelectionListener(this);
 		fileTree.addTreeExpansionListener(this);
 		// packs the tree into a scroll pane.
 		JScrollPane treePane = new JScrollPane(fileTree);
 		treePane.setPreferredSize(new Dimension(200, 10));
 		treePane.setMinimumSize(new Dimension(200, 10));
 
 		treePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		treePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		
 		splitPane.setLeftComponent(treePane);
 	}
 	
 	
 	private DefaultMutableTreeNode buildFileTree(FilePathInfo pathInfo) {
 		if(pathInfo.isDirectory() && (MP3File.containsMP3s(pathInfo.getFile()))){
 			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(pathInfo);
 			ArrayList<FilePathInfo> subFiles = (ArrayList<FilePathInfo>) pathInfo.listFiles();
 			for(FilePathInfo fpi : subFiles){
 				if(MP3File.containsMP3s(fpi.getFile())) {
 					rootNode.add(buildFileTree(fpi));
 				}
 			}
 			return rootNode;
 		} else {
 			return new DefaultMutableTreeNode(pathInfo);
 		}
 	}
 
 
 	/**
 	 * creates and initializes the fields used to display and modify the
 	 * detailed information of a mp3 file.
 	 */
 	
 	private void createDetailForm(){
 		
 		JPanel detailPanel = new JPanel();
 		detailPanel.setLayout(new BorderLayout());
 		
 		JPanel textDetailPanel = new JPanel();
 		GridBagLayout textDetailLayout = new GridBagLayout();
 		textDetailPanel.setLayout(textDetailLayout);
 		GridBagConstraints textDetailConstraints = 
 				new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 4, 2,
 						0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(10,20,0,5), 0, 0);
 		GridBagConstraints textDetailConstraintsFill = (GridBagConstraints) textDetailConstraints.clone();
 		textDetailConstraintsFill.weightx = 0.5;
 		//textDetailConstraintsFill.insets = new Insets(10,5,0,20);
 		
 		JLabel titleLabel = new JLabel("<html><b>Title</b></html>");
 		textDetailPanel.add(titleLabel, textDetailConstraints);
 		titleField = new JTextField(25);
 		textDetailPanel.add(titleField, textDetailConstraintsFill);
 		
 		JLabel albumLabel = new JLabel("<html><b>Album</b></html>");
 		textDetailPanel.add(albumLabel, textDetailConstraints);
 		albumField = new JTextField(25);
 		textDetailPanel.add(albumField, textDetailConstraintsFill);
 		
 		textDetailConstraints.gridy = 2;
 		textDetailConstraintsFill.gridy = 2;
 		
 		JLabel yearLabel = new JLabel("<html><b>Year</b></html>");
 		textDetailPanel.add(yearLabel, textDetailConstraints);
 		yearField = new JTextField(25);
 		textDetailPanel.add(yearField, textDetailConstraintsFill);
 		
 		JLabel artistLabel = new JLabel("<html><b>Artist</b></html>");
 		textDetailPanel.add(artistLabel, textDetailConstraints);
 		artistField = new JTextField(25);
 		textDetailPanel.add(artistField, textDetailConstraintsFill);
 		
 		detailPanel.add(textDetailPanel, BorderLayout.NORTH);
 		
 		coverIcon = new ImageIcon(getDemoCoverImage());
 		coverContainer = new JLabel();
 		coverContainer.setIcon(coverIcon);
 		JPanel outerCoverContainer = new JPanel(new GridLayout(1,1));
 		outerCoverContainer.add(coverContainer);
 		detailPanel.add(outerCoverContainer, BorderLayout.CENTER);
 		
 		splitPane.setRightComponent(detailPanel);
 		
 	}
 	
 	public static byte[] getDemoCoverImage()	{
 		byte[] byteStream = resources.TestImages.png;
 		return byteStream;
 	}
 
 	@Override
 	// The event handler for the tree
 	public void valueChanged(TreeSelectionEvent event) {
 		updateDetailForm();
 	}
 
 
 	private void updateDetailForm() {
 		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
 		if(selectedNode == null)
 			return;
 		Object userObject = selectedNode.getUserObject();
 		if(userObject instanceof MP3File){
 			MP3File mp3 = (MP3File) userObject;
 			titleField.setText(mp3.getTitle());
 			albumField.setText(mp3.getAlbum());
 			yearField.setText(mp3.getYear());
 			artistField.setText(mp3.getArtist());
 			coverIcon = new ImageIcon(mp3.getCover());
 			// mp3.getfile.getAbsolutePath
 		}
 	}
 
 
 	@Override
 	public void treeExpanded(TreeExpansionEvent event) {
 		recalculateDividerLocation();
 	}
 	
 	@Override
 	public void treeCollapsed(TreeExpansionEvent event) {
 		recalculateDividerLocation();		
 	}
 
 	private void recalculateDividerLocation() {
 		//+25 is for scrollbar
 		splitPane.setDividerLocation((fileTree.getPreferredSize().getWidth()+25) / (float) splitPane.getSize().getWidth());
 	}
 	
 }
