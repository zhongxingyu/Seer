 package gui;
 
 import images.ImageTag;
 import images.TaggableImage;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.FlowLayout;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ToolTipManager;
 
 import application.ImageLoader;
 
 
 public class ApplicationWindow extends JFrame implements ActionListener, WindowListener, MouseListener {
 	
 	private static DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode();
 	private static Dimension dim = new Dimension(mode.getWidth(), mode.getHeight());
 	//private static Dimension dim = new Dimension(960, 720);
 	
 	private static final Dimension RIGHT_PANEL_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 110);
 	private static final Dimension IMAGE_CANVAS_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 455); //this one will be dynamic
 	private static final Dimension IMAGE_BUTTON_PANEL_SIZE = new Dimension(dim.width * 3 / 5, 45);
 	private static final Dimension IMEX_BUTTON_PANEL_SIZE = new Dimension(dim.width * 1 / 5, 45);
 	private static final Dimension IMAGE_METADATA_PANEL_SIZE = new Dimension(dim.width * 3 / 5, 300);
 	private static final Dimension leftPaneSize = new Dimension(dim.width * 1 / 5, dim.height - 155);
 	
 	private ImageLoader imageLoader;
 	private static final long serialVersionUID = 1L;
 	
 	
 	private static List<TaggableImage> importedImageList;
 
 	private ImageGridPanel imageGrid;
 	
 	private ImageCanvas mainImageViewCanvas;
 	
 	private JPanel imageMetadataPanel;
 	
 	private JButton importButton;
 	private JButton exportButton;
 	private JButton flagImageButton;
 	private JButton unflagImageButton;
 	private JButton nextImageButton;
 	private JButton prevImageButton;
 	private BufferedImage METADATA_PLACEHOLDER;
 	
 	public static BufferedImage WAI_LOGO;
 	public static BufferedImage IMPORT_PLACEHOLDER;
 	public static final Color WAI_BLUE = new Color(0, 126, 166);
 
 	public ApplicationWindow(){
 		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
 		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 		setImportedImageList(new ArrayList<TaggableImage>());
 		setLayout(new FlowLayout());
 		setResizable(false);
 		addWindowListener(this);
 		initialiseMenus();
 		initialiseWindow();
 		initialiseApplication();
 	
 		pack();
 		setVisible(true);
 	}
 
 	public void initialiseApplication(){
 		imageLoader = new ImageLoader();
 	}
 
 	public void initialiseMenus(){
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		menuBar.add(file);
 		JMenuItem importItem = new JMenuItem("Import");
 		JMenuItem exportItem = new JMenuItem("Export");
 		JMenuItem quitItem = new JMenuItem("Quit");
 		importItem.addActionListener(this);
 		exportItem.addActionListener(this);
 		quitItem.addActionListener(this);
 		file.add(importItem);
 		file.add(exportItem);
 		file.add(quitItem);
 
 		JMenu option = new JMenu("Option");
 		menuBar.add(option);
 
 		JMenuItem flagItem = new JMenuItem("Flag Image");
 		JMenuItem unflagItem = new JMenuItem("Unflag Image");
 		JMenuItem preferencesItem = new JMenuItem("Preferences");
 		flagItem.addActionListener(this);
 		unflagItem.addActionListener(this);
 		preferencesItem.addActionListener(this);
 		
 				
 		option.add(flagItem);
 		option.add(unflagItem);
 		option.add(preferencesItem);
 
 		JMenu help = new JMenu("Help");
 		menuBar.add(help);
 
 		JMenuItem about = new JMenuItem("About");
 		JMenuItem manual = new JMenuItem("Manual");
 		about.addActionListener(this);
 		manual.addActionListener(this);
 			
 		//Setting shortcuts and Mnemonics for Options Menu
 		flagItem.setMnemonic('F');
 		flagItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK));
 		unflagItem.setMnemonic('U');
 		unflagItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.Event.CTRL_MASK));
 		preferencesItem.setMnemonic('P');
 		preferencesItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.Event.CTRL_MASK));
 		
 		//Setting shortcuts and Mnemonics for File Menu
 		importItem.setMnemonic('I');
 		importItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.Event.CTRL_MASK));
 		exportItem.setMnemonic('E');
 		exportItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.Event.CTRL_MASK));
 		quitItem.setMnemonic('W');
 		quitItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.Event.CTRL_MASK));
 		
 		//Setting shortcuts and Mnemonics for Help Menu
 		about.setMnemonic('A');
 		about.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.Event.CTRL_MASK));
 		manual.setMnemonic('M');
 		manual.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.Event.CTRL_MASK));
 			
 				
 		help.add(manual);
 		help.add(about);
 		setJMenuBar(menuBar);
 	}
 	
 	private void initialiseWindow(){
 		try {
 			String importPlaceholderPath = "lib/import-placeholder.jpg";
 			IMPORT_PLACEHOLDER = ImageIO.read(new File(importPlaceholderPath));
 		} catch (IOException e) {
 			System.out.println("Error reading WAI Logo");
 		}
 		
 		imageGrid = new ImageGridPanel(null, this);
 		JScrollPane leftPane = new JScrollPane(imageGrid);
 		leftPane.setPreferredSize(leftPaneSize);
 
 		mainImageViewCanvas = new ImageCanvas(imageGrid);
 		mainImageViewCanvas.setSize(IMAGE_CANVAS_SIZE);
 		mainImageViewCanvas.setPreferredSize(IMAGE_CANVAS_SIZE);
 		mainImageViewCanvas.setMaximumSize(IMAGE_CANVAS_SIZE);
 		
 		imageGrid.setCanvas(mainImageViewCanvas);
 		
 		JPanel rightPanel = new JPanel();
 		rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS)); //changed to FlowLayout jm 180912
 		
 		//JLabel infoLabel = new JLabel(infoIcon);
 		//infoLabel.setBounds(10, 10, infoIcon.getIconWidth(), infoIcon.getIconHeight());
 		//infoLabel.addMouseListener(this);
 		
 		JPanel imageButtonPanel = new JPanel();
 		imageButtonPanel.setLayout(new GridLayout(1, 4)); //changed to GridLayout jm 180912
 		//imageButtonPanel.setSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setPreferredSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setMaximumSize(IMAGE_BUTTON_PANEL_SIZE);
 		
 		//previous button
 		ImageIcon prevBtnImage = new ImageIcon("lib/prev-image-btn.png");
 		prevImageButton = new JButton(prevBtnImage);
 		prevImageButton.addActionListener(this);
 		prevImageButton.setActionCommand("Previous Image");
 		imageButtonPanel.add(prevImageButton);
 		
 		//flag/unflag button
 		ImageIcon flagBtnImage = new ImageIcon("lib/flag-image-btn.png");
 		ImageIcon unflagBtnImage = new ImageIcon("lib/unflag-image-btn.png");
 		flagImageButton = new JButton(flagBtnImage);
 		unflagImageButton = new JButton(unflagBtnImage);
 		flagImageButton.addActionListener(this);
 		unflagImageButton.addActionListener(this);
 		flagImageButton.setActionCommand("Flag Image");
 		unflagImageButton.setActionCommand("Unflag Image");
 		imageButtonPanel.add(flagImageButton);
 		imageButtonPanel.add(unflagImageButton);
 		
 		
 		//auto button
 		JButton autobutton = new JButton("Auto Analyse");
 		autobutton.setEnabled(false);
 		imageButtonPanel.add(autobutton);
 		
 		//next button
 		ImageIcon nextBtnImage = new ImageIcon("lib/next-image-btn.png");
 		nextImageButton = new JButton(nextBtnImage);
 		nextImageButton.setActionCommand("Next Image");
 		nextImageButton.addActionListener(this);
 		imageButtonPanel.add(nextImageButton);
 
 		//enable buttons
 		flagImageButton.setEnabled(false);
 		unflagImageButton.setEnabled(false);
 		prevImageButton.setEnabled(false);
 		nextImageButton.setEnabled(false);
 		
 		//meta-data pane below buttons
 		String metadataPlaceholderPath = "lib/metadata-panel-default.png";
 		try {METADATA_PLACEHOLDER = ImageIO.read(new File(metadataPlaceholderPath));} catch (IOException e) {e.printStackTrace();}
 		imageMetadataPanel = new JPanel();
 		imageMetadataPanel.setLayout(new BorderLayout());
 		imageMetadataPanel.setPreferredSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setMaximumSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setBackground(new Color(153, 157, 158));
 		imageMetadataPanel.add(new JLabel(new ImageIcon(METADATA_PLACEHOLDER.getScaledInstance(IMAGE_METADATA_PANEL_SIZE.width, IMAGE_METADATA_PANEL_SIZE.height, Image.SCALE_FAST))), BorderLayout.NORTH);
 		
 		
 		rightPanel.add(mainImageViewCanvas);
 		rightPanel.add(imageButtonPanel);
 		rightPanel.add(imageMetadataPanel);
 		
 		//leftPanel
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		JPanel importExportPanel = new JPanel();
 		importExportPanel.setLayout(new GridLayout(1, 2));
 		ImageIcon importBtnImage = new ImageIcon("lib/import-images-btn.png");
 		importButton = new JButton(importBtnImage);
 		importButton.setActionCommand("Import");
 		importButton.addActionListener(this);
 		importExportPanel.add(importButton);
 		ImageIcon exportBtnImage = new ImageIcon("lib/export-images-btn.png");
 		exportButton = new JButton(exportBtnImage);
 		exportButton.addActionListener(this);
 		exportButton.setActionCommand("Export");
 		importExportPanel.add(exportButton);
 		importExportPanel.setPreferredSize(IMEX_BUTTON_PANEL_SIZE);
 		importExportPanel.setMaximumSize(IMEX_BUTTON_PANEL_SIZE);
 		
 		leftPanel.add(importExportPanel);
 		leftPanel.add(leftPane);
 		
 		add(leftPanel);
 		add(rightPanel);
 		pack();
 		
 		setVisible(true);
 	}
 
 	public Canvas getMainCanvas(){
 		return mainImageViewCanvas;
 	}
 	
 	public void setButtonsEnabled(boolean enabled){
 		flagImageButton.setEnabled(enabled);
 		unflagImageButton.setEnabled(enabled);
 		prevImageButton.setEnabled(enabled);
 		nextImageButton.setEnabled(enabled);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 		//Debug:
 		//System.out.println(action); //jm 070912
 
 		if (action.equals("Import")) {
 			//import features
 			setImportedImageList(imageLoader.importImages(this));
 			imageGrid.setImageList(getImportedImageList());
 			imageGrid.initialise();
 			imageGrid.repaint();
 			mainImageViewCanvas.repaint();
 			repaint();
 		}
 		else if (action.equals("Export")) {
 			//export features
 		}
 		else if (action.equals("Quit")) {
 			//quit popup
 			int n = JOptionPane.showConfirmDialog(
 				    this,
 				    "Would you like to exit now?",
 				    "Quit",
 				    JOptionPane.YES_NO_OPTION);
 			if(n == 0){System.exit(0);}
 		}
 		else if (action.equals("Flag Image")) {
 			//flag currently selected image
 			imageGrid.getSelectedImage().setTag(ImageTag.INFRINGEMENT);
 			imageGrid.repaint();
 		}
 		else if (action.equals("Unflag Image")) {
 			//unflag the selected image
 			imageGrid.getSelectedImage().setTag(ImageTag.UNTAGGED);
 			imageGrid.repaint();
 		}
 		else if (action.equals("Preferences")) {
 			//open preferences window
 		}
 		else if (action.equals("Manual")) {
 			//manual features
 		}
 		else if (action.equals("About")) {
 			//about dialog
 		}
 		else if (action.equals("Show Metadata")) {
 			JOptionPane.showMessageDialog(
 					null,
 					imageGrid!=null&&imageGrid.getSelectedImage()!=null&&imageGrid.getSelectedImage().getMetaData()!=null?
 							imageGrid.getSelectedImage().getMetaData():
 								"NO METADATA");
 		}
 		else if (action.equals("Previous Image")) {
 			imageGrid.browse("previous");
 		}
 		else if(action.endsWith("Next Image")) {
 			imageGrid.browse("next");
 		}
 	}
 
 	public void windowOpened(WindowEvent e) {}
 	public void windowClosing(WindowEvent e) {
 		//TODO Uncomment later and ovveride default action
 		///int promptOnClose = JOptionPane.showConfirmDialog(this, "Are you sure you want to close?");
 		
 		//if (promptOnClose ==0){
 			
 		System.exit(0);
 		//}
 	}
 	public void windowClosed(WindowEvent e) {
 		
 	}
 	public void windowIconified(WindowEvent e) {}
 	public void windowDeiconified(WindowEvent e) {}
 	public void windowActivated(WindowEvent e) {}
 	public void windowDeactivated(WindowEvent e) {}
 
 	public static List<TaggableImage> getImportedImageList() {
 		return importedImageList;
 	}
 
 	private void setImportedImageList(List<TaggableImage> importedImageList) {
 		ApplicationWindow.importedImageList = importedImageList;
 	}
 	public void mouseClicked(MouseEvent e) {
 		setButtonsEnabled(imageGrid.getSelectedImage()!=null);
 	}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 }
