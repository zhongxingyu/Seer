 package project.efg.client.utils.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.SwingConstants;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 
 import project.efg.client.impl.gui.EFGThumbNailDimensions;
 import project.efg.client.impl.gui.ImagePanel;
 import project.efg.client.utils.nogui.ImageInfo;
 import project.efg.client.utils.nogui.WorkspaceResources;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.utils.EFGUtils;
 
 
 /**
  * @version $Revision: 1.1.1.1 $
  * @author Benot Mah (bmahe@w3.org)
  */
 public class DnDFileBrowserMain extends JDialog {
 
 
 	/**
 	 * @author kasiedu
 	 *
 	 */
 	public class MagickHomeListener implements ActionListener {
 		//private DnDFileBrowserMain main;
 		/**
 		 * @param main
 		 */
 		public MagickHomeListener(DnDFileBrowserMain main) {
 			//this.main = main;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
  			
  			//	     			this.fileLocationProperty = 
  			//	     			currentFileLocationProperty = 
  			  //. isCurrentPropertyChecked = "efg.serverlocation.checked"
  			 //prompt = "Prompt me for server location every time"  
  			/*JFrame frame,
 			String serverLocator,
 			boolean modal,
 			String fileLocationProperty,
 			String currentFileLocationProperty, 
 			String isCurrentPropertyChecked,
 			String prompt*/
 			Properties props = EFGUtils.getEnvVars();
 			String property = null;
 			if (props != null) {
 				String propertyToUse = 
 					EFGImportConstants.EFGProperties.getProperty(
 							"efg.images.magickhome.variable"
 							);
 				property = props.getProperty(propertyToUse);
 			}
 			String pathToServer = 
 				EFGImportConstants.EFGProperties.getProperty(
 						"efg.imagemagicklocation.lists",
 						property);
 			ServerLocator locator = new ServerLocator(
 					importMenu,
 					pathToServer,
 					true,
 					"efg.imagemagicklocation.lists",
 					"efg.imagemagicklocation.current",
 					"efg.imagemagicklocation.checked",
 					"Prompt Me For Image Magick Location Every Time");
 				locator.setVisible(true);
 		}
 
 	}
 
 
 	/**
 	 * 
 	 */
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(DnDFileBrowserMain.class);
 		} catch (Exception ee) {
 		}
 	}
 	private static final long serialVersionUID = 1L;
 	final private JPopupMenu popup = new JPopupMenu();
 	final private ImageInfo imageInfo = new ImageInfo();
 
 	private static JLabel currentDimLabel; 
 	final private JButton deleteBtn = new JButton(EFGImportConstants.EFGProperties
 			.getProperty("FileTreeBrowserMain.deleteBtn"));
 
 	/*final private JButton doneBtn = new JButton(EFGImportConstants.EFGProperties
 			.getProperty("FileTreeBrowserMain.doneBtn"));
 */
 	JProgressBar progressBar = new JProgressBar();
 
 	JComponent imageView;
 
 	JLabel imageLabel;
 	JPanel displayPanel;
 	URL helpURL;
 	private JPanel iPanel;
 
 	//JEditorPane htmlPane;
 	Vector userItems = new Vector();
 	static String thumsStr; 
 	public String imageL = EFGImportConstants.EFGProperties
 			.getProperty("FileTreeBrowserMain.imageL");
 
 	//
 	// Constructor
 	//
 	FileBrowser browser;
 	private String currentImagesDirectory;
 	protected JFrame importMenu;
 	
 
 
 	private JScrollPane browserPane;
 	private String isLinux;
 	/**
 	 * 
 	 * @param frame
 	 * @param title
 	 * @param modal
 	 */
 	public DnDFileBrowserMain(JFrame importMenu, 
 			String title, 
 			boolean modal) {
 		super(importMenu, modal);
 		this.importMenu = importMenu;
 		thumsStr = 
 			EFGImportConstants.EFGProperties.getProperty("maximum_dimension_string");
 		//always set to false
 
 		//switch
 		this.isLinux = EFGImportConstants.EFGProperties.getProperty("efg2.system.os","windowsflavor");
 	
 		this.currentImagesDirectory = 
 			this.computeCurrentMediaResourceDirectory();
 		
 		//fix me
 		this.setTitle("Drag and Drop Image Folders here");
 		this.setModal(true);
 		imageView = addImageDisplayPanel();	
 		initHelp();
 		this.progressBar.setSize(300, 300);
 			
 		this.iPanel = this.addTreeBkgdImagePanel();
 		this.browserPane = new JScrollPane(this.iPanel);
 		JScrollPane imageViewPane = new JScrollPane(this.imageView);
 		JSplitPane pane = 
 			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				browserPane, imageViewPane);
 		pane.setOneTouchExpandable(true);
 		pane.setDividerLocation(300);
 		this.addMenus();	
 		
 		this.getContentPane().add(pane, BorderLayout.CENTER);
 		this.getContentPane().add(addButtons(), BorderLayout.SOUTH);
 		this.setSize(700, 600);
 		addWindowListener(new WndCloser(this));
 		this.setLocationRelativeTo(importMenu);
 	
 	}
 	private String computeCurrentMediaResourceDirectory() {
 		String property = 
 			EFGImportConstants.EFGProperties.getProperty("efg.mediaresources.home.current");
 		
 		if(property == null || property.trim().equals("")) {
 			WorkspaceResources.computeMediaResourcesHome();
 		}
 		property = 
 			EFGImportConstants.EFGProperties.getProperty("efg.mediaresources.home.current");
 		if(property == null || property.trim().equals("")) {
 			this.close();
 		}
 		return property;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	private JComponent addImageDisplayPanel() {
 		// Create the HTML viewing pane.
 		displayPanel = new JPanel();
 		displayPanel.setLayout(new BorderLayout());
 		this.progressBar.setSize(300, 300);
 		this.progressBar.setStringPainted(true);
 		this.progressBar.setString(""); // but don't paint it
 		this.imageLabel = new JLabel(imageL, SwingConstants.CENTER);
 		this.imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		this.imageLabel.setHorizontalTextPosition(JLabel.CENTER);
 		this.displayPanel.add(this.imageLabel, BorderLayout.CENTER);
 	
 		return displayPanel;
 	}
 
 
 
 	private JPanel addTreeBkgdImagePanel() {
 
 		this.browser = DnDFileBrowser.getFileBrowser(this.currentImagesDirectory,
 				progressBar,this.importMenu);
 		this.browser.setRootVisible(false);
 		this.browser.setSelectionRow(0);
 		
 		this.browser.addTreeSelectionListener(new FileTreeSelectionListener(
 				this.browser));
 		
 		this.browser.expandRow(0);
 
 		JPanel iPanel = null;
 			
 			if(this.isLinux.equalsIgnoreCase("islinuxflavor")){
 				iPanel = new JPanel();
 				
 			}
 			else{
 				iPanel =new ImagePanel(EFGImportConstants.IMAGE_DROP_BACKGROUND_IMAGE);
 			}
 			
 		iPanel.setLayout(new BorderLayout());
 		iPanel.add(this.browser,BorderLayout.CENTER);
 		iPanel.setBackground(Color.white);
 	
 		
 		this.browser.setOpaque(false);
 		this.browser.addMouseListener(new EditMouseListener(this));
 		iPanel.setOpaque(true);
 		return iPanel;
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	private void addMenus(){
 		
 		
 		JMenu fileMenu = 
 			new JMenu("File");		
 		JMenu helpMenu = 
 			new JMenu("Help");		
 		JMenuItem thumbNailMenu = 
 			new JMenuItem("Thumbnails");
 		JMenuItem magickHomeMenu = 
 			new JMenuItem("Reset Magick Home");
 		magickHomeMenu.setToolTipText("Set the directory where ImageMagick is located");
 		JMenuItem preferencesMenu = 
 			new JMenuItem("Change/View Preferences");
 
 		JMenuItem deleteMenu = 
 			new JMenuItem("Delete");
 		JMenuItem closeMenu = 
 			new JMenuItem("Close");
 		JMenuItem helpItem = 
 			new JMenuItem("Help Contents");
 
 		thumbNailMenu.addActionListener(new ThumbsListener(this));
 		magickHomeMenu.addActionListener(new MagickHomeListener(this));
 		preferencesMenu.addActionListener(new PreferencesListener(this.importMenu, false, true));
 		
 		
 		if(this.isLinux.equalsIgnoreCase("islinuxflavor")){
             String property = 
             	EFGImportConstants.EFGProperties.getProperty(
             			"efg.image.last.file",null);
  
             if(property != null) {
             	String[] properties = {property};
             	properties = WorkspaceResources.convertURIToString(properties);
             	if(properties != null) {
             		property = properties[0];
             	}
             }
             else{
             	property = ".";
             }
 			//if it is a linux like thing show new import
 			final JMenuItem newLinuxMenu = new JMenuItem(EFGImportConstants.EFGProperties
 					.getProperty("new.linux.menu"));
 		
 			newLinuxMenu.addActionListener(
 					new ImageFileListener(
 							(DnDFileBrowser)this.browser,
 							this.importMenu,
 							property, 
 							EFGImportConstants.EFGProperties.getProperty(
 									"efg.file.images.message")));
 					/*new FileChooserListener(
 							this.browser,
 							this.importMenu,
 							property,
 							EFGImportConstants.EFGProperties.getProperty("efg.file.images.message"),
 							JFileChooser.FILES_AND_DIRECTORIES));*/
 			fileMenu.add(newLinuxMenu);	
 			fileMenu.addSeparator();
 		}
 
 		fileMenu.add(thumbNailMenu);
 		fileMenu.add(magickHomeMenu);
 		fileMenu.add(preferencesMenu);
 		
 		deleteMenu.addActionListener(new DeleteListener(this.browser));
 		deleteBtn.setToolTipText(EFGImportConstants.EFGProperties
 				.getProperty("FileTreeBrowserMain.deleteBtn.tooltip"));
 		helpItem.addActionListener(new HelpEFG2ItemListener
 				(EFGImportConstants.IMAGE_DEPLOY_HELP));
 		
 		closeMenu.addActionListener(new DoneListener(this));
 		
 		fileMenu.add(deleteMenu);
 		helpMenu.add(helpItem);
 		
 		fileMenu.addSeparator();	
 		fileMenu.add(closeMenu);
 		
 		JMenuBar mBar = new JMenuBar();
 		mBar.add(fileMenu);
 		mBar.add(helpMenu);
 		this.setJMenuBar(mBar);
 		this.createPopUp();
 	}
 	/**
 	 * 
 	 *
 	 */
 	public void close() {
 		this.dispose();
 	}
 
 	/**
 	 * @param currentSelection
 	 */
 	public static void setCurrentDimLabel(String currentSelection) {
 		currentDimLabel.setText(thumsStr + " " + currentSelection);
 	}
 
 
 	private void setDefaultThumbnailDimensions() {
 	
 			String[] dimensions = WorkspaceResources.getDefaultDimensions();
 			//String maxDim = null;
 			StringBuffer buffer = new StringBuffer();
 			for(int i= dimensions.length;i > 0;i--) {
 
 				if(i < dimensions.length) {
 					buffer.append(",");
 				}
 				buffer.append(dimensions[i-1]);
 			}
 			EFGImportConstants.EFGProperties.setProperty(
 					"efg.thumbnails.dimensions.lists",
 					buffer.toString());
 			EFGImportConstants.EFGProperties.setProperty(
 					"efg.thumbnails.dimensions.current",
 					dimensions[dimensions.length -1]);
 		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	private JPanel addButtons() {
 		
 		String maxDim = 
 			EFGImportConstants.EFGProperties.getProperty("efg.thumbnails.dimensions.current");
 		
 		if(maxDim == null || maxDim.trim().equals("")) {
 			setDefaultThumbnailDimensions();
 			maxDim = 
 				EFGImportConstants.EFGProperties.getProperty("efg.thumbnails.dimensions.current");
 		}
 		currentDimLabel = new JLabel(thumsStr + " " + maxDim + " ", JLabel.LEADING);
 		currentDimLabel.setForeground(Color.blue);
 		JPanel btnPanel =  new JPanel(new GridLayout(0, 1));
 		
 		
 		
 		JLabel currentDirectory = new JLabel(); 
 		currentDirectory.setText("Current Mediaresource directory: " + 
 				WorkspaceResources.toFileName(this.currentImagesDirectory));
 		
 		btnPanel.add(currentDirectory);
 		
 		btnPanel.add(currentDimLabel);
 		return btnPanel;
 
 	}
 	/**
 	 * 
 	 *
 	 */
 	private void initHelp() {
 
 		helpURL = this.getClass().getResource(
 				EFGImportConstants.IMAGE_DEPLOY_HELP);
 		if (helpURL == null) {
 			log.error("Couldn't open help file: "
 					+ EFGImportConstants.IMAGE_DEPLOY_HELP);
 			return;
 		}
 		displayURL(helpURL);
 	}
 	/**
 	 * 
 	 * @param url
 	 */
 	private void displayURL(URL url) {
 	
 	}
 	/**
 	 * 
 	 *
 	 */
 	public void createPopUp() {
 		JMenuItem thumbNailMenu = 
 			new JMenuItem("Thumbnails");
 		
 		
 		JMenuItem preferencesMenu = 
 			new JMenuItem("Change/View Preferences");
 		
 		thumbNailMenu.addActionListener(new ThumbsListener(this));
 		preferencesMenu.addActionListener(new PreferencesListener(this.importMenu, false, true));
 
 		
 		JMenuItem deleteMenu = new JMenuItem(this.deleteBtn.getText());
 		deleteMenu.addActionListener(new DeleteListener(this.browser));
 		
 		JMenuItem closeMenu = 
 			new JMenuItem("Close");
 		closeMenu.addActionListener(new DoneListener(this));
 		
 		this.popup.add(thumbNailMenu);
 		this.popup.add(preferencesMenu);
 		this.popup.add(deleteMenu);
 		this.popup.add(closeMenu);
 	}
 	/**
 	 * 
 	 * @author jacob.asiedu
 	 *
 	 */
 	class  WndCloser extends WindowAdapter{
 		/**
 		 * 
 		 */
 		private DnDFileBrowserMain main;
 		/**
 		 *	 
 		 * @param main
 		 */
 		public WndCloser(DnDFileBrowserMain main) {
 			this.main = main;
 		}
 		/**
 		 * 
 		 */
 		public void windowClosing(WindowEvent e) {	
 			this.main.close();			
 		}
 	}
 	class EditMouseListener extends MouseAdapter {
 
 		private DnDFileBrowserMain treeBrowser;
 		/**
 		 * 
 		 * @param treeBrowser
 		 */
 		public EditMouseListener(DnDFileBrowserMain treeBrowser) {
 			this.treeBrowser = treeBrowser;
 		}
 		/**
 		 * 
 		 */
 		public void mousePressed(MouseEvent e) {
 
 			if (e.isPopupTrigger()) {
 				showPopUp(e);
 			}
 		}
 		/**
 		 * 
 		 * @param e
 		 */
 		private void showPopUp(MouseEvent e) {
 			TreePath path = browser.getPathForLocation(e.getX(), e.getY());
 			if (path != null) {
 				browser.getSelectionModel().setSelectionPath(path);
				popup.show(this.treeBrowser, e.getX(), e.getY());
 			}
 		}
 		/**
 		 * 
 		 */
 		public void mouseReleased(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				showPopUp(e);
 			}
 		}
 
 	}
 
 	
 	class FileTreeSelectionListener implements TreeSelectionListener {
 		private FileBrowser tree;
 
 		public FileTreeSelectionListener(FileBrowser tree) {
 			super();
 			this.tree = tree;
 		}
 
 		public void valueChanged(TreeSelectionEvent e) {
 			try {
 				Object o = tree.getLastSelectedPathComponent();
 
 				if (o instanceof FileNode) {// only do this when it
 					// is an instance
 					FileNode node = (FileNode) o;
 					String path = node.getFile().getAbsolutePath();
 					if (path == null) {
 						imageLabel.setText(imageL);
 					} else {
 						String images_home = 
 							EFGImportConstants.EFGProperties.getProperty(
 									"efg.images.home");
 						String thumbshome = 
 							EFGImportConstants.EFGProperties.getProperty(
 									"efg.mediaresources.thumbs.home");
 										
 						path = path.replaceAll(
 								images_home,
 								thumbshome);
 						
 						File imageFile = new File(path);
 						if(imageFile.isDirectory()){//not an image
 							return;
 						}
 						BufferedImage image = javax.imageio.ImageIO.read(imageFile);
 						StringBuffer buffer = 
 							new StringBuffer("<html>");
 						buffer.append(ImageInfo.getFileInfoHtml(imageFile,imageInfo));
 						buffer.append("</html>");
 						
 						
 						imageLabel.setIcon(new ImageIcon(image));
 						imageLabel.setText(buffer.toString());
 					}					
 				}
 			} catch (Exception ee) {
 				imageLabel.setIcon(null);
 				imageLabel.setText("");
 			}
 			displayPanel.revalidate();
 		}
 	}
 
 	class DeleteListener implements ActionListener {
 		private FileBrowser tree;
 
 		public DeleteListener(FileBrowser tree) {
 			this.tree = tree;
 		}
 
 		public void actionPerformed(ActionEvent evt) {
 			imageLabel.setIcon(null);
 			imageLabel.setText("");
 			this.tree.deleteSelectedFiles();
 		}
 
 	}
 
 	class DoneListener implements ActionListener {
 		private DnDFileBrowserMain treeBrowser;
 		
 		public DoneListener(DnDFileBrowserMain dndFile) {
 			this.treeBrowser = dndFile;
 		}
 		public void actionPerformed(ActionEvent evt) {
 			
 			this.treeBrowser.close();
 		}
 	}
 
 	
 	class ThumbsListener implements ActionListener {
 		//private DnDFileBrowserMain dnd;
 		/**
 		 * 
 		 */
 		public ThumbsListener(DnDFileBrowserMain dnd) {
 			//this.dnd = dnd;
 		}
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
 			EFGThumbNailDimensions thd = 
 				new EFGThumbNailDimensions(importMenu,
 						"Enter Thumbnail Dimension",true);
 			thd.setVisible(true);
 			
 			String currentDim = EFGImportConstants.EFGProperties.getProperty(
 			"efg.thumbnails.dimensions.current");
 			DnDFileBrowserMain.setCurrentDimLabel(currentDim);
 		}
 	}
 	class ImageFileListener implements ActionListener{
 		
 
 		private String previousFileLocation;
 		private String title;
 		private JFrame parent;
 		private DnDFileBrowser tree;
 		
 		/**
 		 * 
 		 */
 		public ImageFileListener(DnDFileBrowser tree,
 				JFrame parent,
 				String previousFileLocation, 
 				String title) {
 			this.tree = tree;
 			this.previousFileLocation = previousFileLocation;
 			this.title = title;
 			this.parent = parent;
 		
 			
 		}
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
             JFileChooser chooser = new JFileChooser();
             chooser.setFileHidingEnabled(false);
             chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             chooser.setMultiSelectionEnabled(true);
             chooser.setDialogType(JFileChooser.OPEN_DIALOG);
             chooser.setDialogTitle(this.title);
             chooser.setCurrentDirectory(new File(this.previousFileLocation));
             if (
                 chooser.showOpenDialog(
                 		this.parent)
                 == JFileChooser.APPROVE_OPTION
                 ) {
 	            File[] files = chooser.getSelectedFiles();
             	if(files != null && files.length > 0){
             		log.debug("Number Files Selected: " + files.length);
                 		                	
             		List data = convertFilesToList(files);
 
                			EFGImportConstants.EFGProperties.setProperty(
                 				"efg.images.last.file",
                 				WorkspaceResources.convertFileNameToURLString(
                 						files[0].getParentFile().getAbsolutePath()));
                		this.tree.handleSelectedImages(data,
                				null);	
             	}
             	else{
             		log.debug("No Files Selected");
             	}
             }
 			
 		}
 		/**
 		 * @param files
 		 * @return
 		 */
 		private List convertFilesToList(File[] files) {
 			List list = new ArrayList(files.length);
 			for (int i = 0; i < files.length; i++) {
 				log.debug("Adding File: " + files[i].getAbsolutePath() );
 				list.add(files[i]);
 			}
 			return list;
 		}
 }
 
 }
