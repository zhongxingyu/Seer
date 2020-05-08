 package awesome;
 
 
 import javax.swing.*;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.awt.event.*;
 
 public class ID3View extends JFrame implements TreeSelectionListener, TreeExpansionListener  {
 	
 	private static final long serialVersionUID = 3797307884995261587L;
 	private JTextField titleField;
 	private JTextField albumField;
 	private JTextField yearField;
 	private JTextField artistField;
 
 	private JTree fileTree;
 	private JLabel coverContainer;
 	
 	private JSplitPane splitPane;
 	
 	private JPopupMenu coverMenu;
 	
 	public ID3View() {
 		setSize(700, 500);
 		setTitle("Awesome ID3");
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
 		//TODO: Change to ID3Controller.exitApplication()
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				MP3File mp3 = getSelectedMP3();
 				if(mp3 != null) saveToMP3File(mp3);
 				AwesomeID3.getController().exitApplication();
 			}
 		});
 		
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		splitPane.setDividerLocation(-1);
 		getContentPane().add(splitPane, BorderLayout.CENTER);
 		
 		createTree();
 		createMenu();
 		createDetailForm();
 		createCoverMenu();
 	}
 		
 	public boolean askUserForDirtyFiles() { 
 	      int result = JOptionPane.showConfirmDialog(null, 
 	      "Would you like to save the dirty files?", 
 	      "Save Files", 
 	      JOptionPane.YES_NO_OPTION); 	      
 	      
 	      switch(result) { 
 	         case JOptionPane.YES_OPTION: return true;  	 
 	 
 	         default: 
 	        	 return false; 
 	      } 
 	      
 	   } 
 	
 	
 	private void createCoverMenu() {
 		coverMenu = new JPopupMenu();
 		
 		JMenuItem replaceCoverItem = new JMenuItem("Add/Replace Cover...");
 		replaceCoverItem.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MP3File mp3 = getSelectedMP3();
 				if(mp3 != null){
 					JFileChooser fileChooser = new JFileChooser();
 					fileChooser.setAcceptAllFileFilterUsed(false);
					fileChooser.setFileFilter(new AllImagesFileFilter());
 					if(fileChooser.showOpenDialog(ID3View.this) == JFileChooser.APPROVE_OPTION){
 						File file = fileChooser.getSelectedFile();
 						ID3Parser.readCoverFromFile(mp3, file);
 						coverContainer.setIcon(new ImageIcon(mp3.getCover()));
 						coverContainer.setText("");
 					}
 				}
 			}
 		});
 		
 		JMenuItem deleteCoverItem = new JMenuItem("Delete Cover");
 		deleteCoverItem.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MP3File mp3 = getSelectedMP3();
 				if(mp3 != null){
 					mp3.deleteCover();
 					updateDetailForm();
 				}
 			}
 		});
 
 		coverMenu.add(replaceCoverItem);
 		coverMenu.add(deleteCoverItem);
 		
 		coverContainer.setComponentPopupMenu(coverMenu);
 	}
 
 
 	/**
 	 * creates and initializes the menu bar of the frame and its subcomponents.
 	 */
 	
 	private void createMenu(){
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu menuMain = new JMenu("File");
 		
 		JMenuItem itemSave = new JMenuItem("Save Changes");
 		JMenuItem itemReload = new JMenuItem("Reload MP3 Files");
 		JMenuItem itemChangeDir = new JMenuItem("Choose Music Directory...");
 //		unnecessary in my eyes because every user just closes the window
 //		by clicking on the X
 //		JMenuItem itemExit = new JMenuItem("Exit Awesome ID3");
 //		
 //		itemExit.addActionListener(new ActionListener(){
 //
 //			@Override
 //			public void actionPerformed(ActionEvent arg0) {
 //				AwesomeID3.getController().exitApplication();
 //			}
 //			
 //		});
 		
 		itemSave.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				MP3File mp3 = getSelectedMP3();				
 				if(mp3 != null) 
 					saveToMP3File(mp3);
 				
 				try {
 					AwesomeID3.getController().getMusicLibrary().saveAllDirtyFiles(); //save all modified files					
 				} catch (IOException e) {
 					presentException(e);
 				}
 			}
 			
 		});
 		
 		menuMain.add(itemSave);
 		menuMain.addSeparator();
 		menuMain.add(itemReload);
 		//menuMain.addSeparator(); //maybe it's prettier
 		menuMain.add(itemChangeDir);
 //		menuMain.addSeparator();
 //		menuMain.add(itemExit);
 		
 		menuBar.add(menuMain);
 		
 		this.setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * creates and initializes the tree used to present the directory structure.
 	 */
 	
 	private void createTree(){
 		// initializes the tree
 		DefaultMutableTreeNode topNode = buildFileTree(AwesomeID3.getController().getMusicLibrary().getRootDirectory());
 		
 		fileTree = new JTree(topNode);
 		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		fileTree.addTreeSelectionListener(this);
 		fileTree.addTreeExpansionListener(this);
 		// packs the tree into a scroll pane.
 		JScrollPane treePane = new JScrollPane(fileTree);
 		treePane.setPreferredSize(new Dimension(150, 10));
 		treePane.setMinimumSize(new Dimension(150, 10));
 		
 		treePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		treePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		//add the tree to the left side of the split pane
 		splitPane.setLeftComponent(treePane);
 	}
 	
 	
 	private DefaultMutableTreeNode buildFileTree(FilePathInfo pathInfo) {
 		if(pathInfo.isDirectory()){
 			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(pathInfo);
 			ArrayList<FilePathInfo> subFiles = (ArrayList<FilePathInfo>) pathInfo.listFiles(); //get all children
 			for(FilePathInfo fpi : subFiles){
 				rootNode.add(buildFileTree(fpi));
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
 		
 		coverContainer = new JLabel();
 		coverContainer.setIcon(null);
 		
 		FlowLayout coverLayout = new FlowLayout();
 		coverLayout.setAlignment(FlowLayout.CENTER);
 		JPanel outerCoverContainer = new JPanel(coverLayout);
 		outerCoverContainer.add(coverContainer);
 		detailPanel.add(outerCoverContainer, BorderLayout.CENTER);
 		
 		splitPane.setRightComponent(detailPanel);
 		
 	}
 	
 	public void presentException(Exception ex){
 		JOptionPane.showMessageDialog(this,
 				ex.getMessage(), 
 				"An Error Occured",                                      
                 JOptionPane.ERROR_MESSAGE);
 	}
 
 	@Override
 	// The event handler for the tree
 	public void valueChanged(TreeSelectionEvent event) {
 		TreePath pathOld = event.getOldLeadSelectionPath();
 		if(pathOld != null && pathOld.getLastPathComponent() != null){
 			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) pathOld.getLastPathComponent();
 			if(treeNode.getUserObject() instanceof MP3File){
 				MP3File mp3 = (MP3File) treeNode.getUserObject();
 				saveToMP3File(mp3);
 			}
 		}
 		updateDetailForm();
 		
 		boolean enableFields = getSelectedMP3() != null;
 		
 		albumField.setEnabled(enableFields);
 		artistField.setEnabled(enableFields);
 		titleField.setEnabled(enableFields);
 		yearField.setEnabled(enableFields);
 		
 		if(!enableFields){
 			albumField.setText(null);
 			artistField.setText(null);
 			titleField.setText(null);
 			yearField.setText(null);
 		}
 	}
 
 
 	private void saveToMP3File(MP3File mp3) {
 		mp3.setAlbum(albumField.getText());
 		mp3.setArtist(artistField.getText());
 		mp3.setTitle(titleField.getText());
 		mp3.setYear(yearField.getText());
 		// cover is already saved
 	}
 
 
 	private void updateDetailForm() {
 		MP3File mp3 = getSelectedMP3();
 		if(mp3 != null){
 			titleField.setText(mp3.getTitle());
 			albumField.setText(mp3.getAlbum());
 			yearField.setText(mp3.getYear());
 			artistField.setText(mp3.getArtist());
 			if(mp3.getCover() != null){
 				coverContainer.setText("");
 				coverContainer.setIcon(new ImageIcon(mp3.getCover()));
 			} else {
 				coverContainer.setText("Right-Click here to add Cover");
 				coverContainer.setIcon(null);
 			}
 			
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
 	
 	private MP3File getSelectedMP3(){
 
 		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
 		if(selectedNode == null)
 			return null;
 		Object userObject = selectedNode.getUserObject();
 		if(userObject instanceof MP3File){
 			return (MP3File) userObject;
 		} else {
 			return null;
 		}
 	}
 	
 }
