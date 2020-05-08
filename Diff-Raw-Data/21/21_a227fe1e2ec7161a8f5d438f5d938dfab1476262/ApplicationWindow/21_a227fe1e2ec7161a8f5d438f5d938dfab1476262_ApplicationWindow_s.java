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
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
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
 import javax.swing.border.EtchedBorder;
 
 import application.ImageClassifier;
 import application.ImageLoader;
 import application.ImagePdfExporter;
 
 
 public class ApplicationWindow extends JFrame implements ActionListener, WindowListener, MouseListener {
 	public static boolean minimize, warning, ask; 
 	public static int proxyPort;
 	public static String proxyUrl;
 	public static boolean useProxy;
 	public static String location;
 	
 	private static DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode();
 	private static Dimension dim = new Dimension(mode.getWidth(), mode.getHeight());
 	//private static Dimension dim = new Dimension(1024, 768);
 	
 	private static final Dimension RIGHT_PANEL_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 110);
 	private static final Dimension IMAGE_BUTTON_PANEL_SIZE = new Dimension(dim.width * 3 / 5, (RIGHT_PANEL_SIZE.height)/21);
 	private static final Dimension IMAGE_METADATA_PANEL_SIZE = new Dimension(dim.width * 3 / 5, (RIGHT_PANEL_SIZE.height)/3);
 	private static final Dimension IMAGE_CANVAS_SIZE = new Dimension(dim.width * 3 / 5, (RIGHT_PANEL_SIZE.height
 																						 -IMAGE_BUTTON_PANEL_SIZE.height
 																						 -IMAGE_METADATA_PANEL_SIZE.height));
 	private static final Dimension IMEX_BUTTON_PANEL_SIZE = new Dimension(dim.width * 1 / 5, (dim.height)/23);
 	private static final Dimension leftPaneSize = new Dimension(dim.width * 1 / 5, dim.height - 110 - 2*(dim.height/23));
 	private static final Dimension ANALYSE_ALL_BUTTON_SIZE = new Dimension(dim.width * 1 / 5, dim.height / 23);
 	
 	private ImageLoader imageLoader;
 	private static final long serialVersionUID = 1L;
 	
 	
 	private static List<TaggableImage> importedImageList;
 
 	private ImageGridPanel imageGrid;
 	
 	private ImageCanvas mainImageViewCanvas;
 
 	private JPanel imageMetadataPanel;
 	private JLabel metaDataLabel = new JLabel(" <p>Blank</p>");
 	
 	private JButton importButton;
 	private JButton exportButton;
 	private JButton flagImageButton;
 	private JButton nextImageButton;
 	private JButton prevImageButton;
 	private JButton autobutton;
 	private JButton analyzeAllButton;
 	private BufferedImage METADATA_PLACEHOLDER;
 	
 	public static BufferedImage WAI_LOGO;
 	public static BufferedImage IMPORT_PLACEHOLDER;
 	public static final Color WAI_BLUE = new Color(0, 126, 166);
 
 	public ApplicationWindow(){
 		checkSetting();
 		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
 		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 		setImportedImageList(new ArrayList<TaggableImage>());
 		setLayout(new FlowLayout());
 		setResizable(false);
 		addWindowListener(this);
 		initialiseMenus();
 		initialiseWindow();
 		initialiseApplication();
 	    setTitle("WAINZ UAVTool");
 		pack();
 		
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension scrnsize = toolkit.getScreenSize();
 		setBounds((scrnsize.width - getWidth()) / 2, (scrnsize.height - getHeight()) / 2, getWidth(), getHeight());
 		
 		setVisible(true);
 	}
 
 	public void initialiseApplication(){
 		imageLoader = new ImageLoader();
 	}
 
 	public void initialiseMenus(){
 		System.out.println("button width/height:  " + IMAGE_BUTTON_PANEL_SIZE.width/4 + ", " + IMAGE_BUTTON_PANEL_SIZE.height);
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		menuBar.add(file);
 		JMenuItem importItem = new JMenuItem("Import Images");
 		importItem.setActionCommand("Import");
 		JMenuItem exportItem = new JMenuItem("Export Images");
 		exportItem.setActionCommand("Export");
 		JMenuItem quitItem = new JMenuItem("Quit");
 		importItem.addActionListener(this);
 		exportItem.addActionListener(this);
 		quitItem.addActionListener(this);
 		file.add(importItem);
 		file.add(exportItem);
 		file.add(quitItem);
 
 		JMenu option = new JMenu("Image");
 		menuBar.add(option);
 
 		JMenuItem flagItem = new JMenuItem("Flag Image");
 		JMenuItem unflagItem = new JMenuItem("Unflag Image");
 		JMenuItem pdfReportItem = new JMenuItem("PDF Report");
 		JMenuItem preferencesItem = new JMenuItem("Preferences");
 		flagItem.addActionListener(this);
 		unflagItem.addActionListener(this);
 		preferencesItem.addActionListener(this);
 		pdfReportItem.addActionListener(this);
 				
 		option.add(flagItem);
 		option.add(unflagItem);
 		option.add(pdfReportItem);
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
 		leftPane.setMaximumSize(leftPaneSize);
 
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
 		imageButtonPanel.setSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setPreferredSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setMaximumSize(IMAGE_BUTTON_PANEL_SIZE);
 		
 		//imageButtonPanel.setPreferredSize(new Dimension(RIGHT_PANEL_SIZE.width, RIGHT_PANEL_SIZE.height/18));
 		//imageButtonPanel.setMaximumSize(new Dimension(RIGHT_PANEL_SIZE.width, RIGHT_PANEL_SIZE.height/18));
 		
 		//previous button
 		ImageIcon prevBtnImage = new ImageIcon("lib/prev-image-btn.png");
 		
 		//Image xa = prevBtnImage.getImage().getScaledInstance(RIGHT_PANEL_SIZE.width/5, RIGHT_PANEL_SIZE.height/18, java.awt.Image.SCALE_SMOOTH);
 		Image xa = prevBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		prevBtnImage = new ImageIcon(xa);
 		
 
 		prevImageButton = new JButton(prevBtnImage);
 		prevImageButton.addActionListener(this);
 		prevImageButton.setActionCommand("Previous Image");
 		imageButtonPanel.add(prevImageButton);
 		
 		
 		//flag/unflag button
 		ImageIcon flagBtnImage = new ImageIcon("lib/flag-image-btn.png");
 		xa = flagBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		flagBtnImage = new ImageIcon(xa);
 		
 		flagImageButton = new JButton(flagBtnImage);
 		
 		//unflagImageButton = new JButton(unflagBtnImage);
 		flagImageButton.addActionListener(this);
 		//unflagImageButton.addActionListener(this);
 		flagImageButton.setActionCommand("Flag Image");
 		//unflagImageButton.setActionCommand("Unflag Image");
 		imageButtonPanel.add(flagImageButton);
 		//imageButtonPanel.add(unflagImageButton);
 		
 		//auto button
 		ImageIcon autoButtonImage = new ImageIcon("lib/auto-analyse-btn-gold.png");
 		xa = autoButtonImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		autoButtonImage = new ImageIcon(xa);
 		autobutton = new JButton(autoButtonImage);
 		autobutton.setEnabled(false);
 		autobutton.setActionCommand("Auto Analyse");
 		autobutton.addActionListener(this);
 		imageButtonPanel.add(autobutton);
 		
 		//next button
 		ImageIcon nextBtnImage = new ImageIcon("lib/next-image-btn.png");
 		xa = nextBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		nextBtnImage = new ImageIcon(xa);
 		
 		nextImageButton = new JButton(nextBtnImage);
 		nextImageButton.setActionCommand("Next Image");
 		nextImageButton.addActionListener(this);
 		imageButtonPanel.add(nextImageButton);
 
 		
 		//meta-data pane below buttons
 		String metadataPlaceholderPath = "lib/metadata-panel-default.png";
 		try {
 			METADATA_PLACEHOLDER = ImageIO.read(new File(metadataPlaceholderPath));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		imageMetadataPanel = new JPanel();
 		imageMetadataPanel.setPreferredSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setMaximumSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setBackground(new Color(153, 157, 158));
 		if(imageGrid.getSelectedImage()== null)
 			imageMetadataPanel.add(new JLabel(new ImageIcon(METADATA_PLACEHOLDER.getScaledInstance(IMAGE_METADATA_PANEL_SIZE.width, 
 					IMAGE_METADATA_PANEL_SIZE.height, Image.SCALE_FAST))));
 		else
 			imageMetadataPanel.add(metaDataLabel);
 		
 		rightPanel.add(mainImageViewCanvas);
 		rightPanel.add(imageButtonPanel);
 		rightPanel.add(imageMetadataPanel);
 		
 		//leftPanel
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		JPanel importExportPanel = new JPanel();
 		importExportPanel.setLayout(new GridLayout(1, 2));
 		ImageIcon importBtnImage = new ImageIcon("lib/import-images-btn.png");
 		xa = importBtnImage.getImage().getScaledInstance(IMEX_BUTTON_PANEL_SIZE.width/2, IMEX_BUTTON_PANEL_SIZE.height, java.awt.Image.SCALE_SMOOTH);
 		importBtnImage = new ImageIcon(xa);
 		importButton = new JButton(importBtnImage);
 
 		importButton.setActionCommand("Import");
 		importButton.addActionListener(this);
 		importExportPanel.add(importButton);
 		
 		ImageIcon exportBtnImage = new ImageIcon("lib/export-images-btn.png");
 		xa = exportBtnImage.getImage().getScaledInstance(IMEX_BUTTON_PANEL_SIZE.width/2, IMEX_BUTTON_PANEL_SIZE.height, java.awt.Image.SCALE_SMOOTH);
 		exportBtnImage = new ImageIcon(xa);
 		exportButton = new JButton(exportBtnImage);
 		exportButton.addActionListener(this);
 		exportButton.setActionCommand("Export");
 		importExportPanel.add(exportButton);
 		importExportPanel.setPreferredSize(IMEX_BUTTON_PANEL_SIZE);
 		importExportPanel.setMaximumSize(IMEX_BUTTON_PANEL_SIZE);		
 		//importExportPanel.setPreferredSize(new Dimension(leftPaneSize.width, leftPaneSize.height/18));
 		//importExportPanel.setMaximumSize(new Dimension(leftPaneSize.width, leftPaneSize.height/18));
 		
 		JPanel analyseAllPanel = new JPanel();
 		ImageIcon analyseAllButtonImage = new ImageIcon("lib/analyse-all-btn.png");
 		xa = analyseAllButtonImage.getImage().getScaledInstance(ANALYSE_ALL_BUTTON_SIZE.width, ANALYSE_ALL_BUTTON_SIZE.height, java.awt.Image.SCALE_SMOOTH);
 		analyseAllButtonImage = new ImageIcon(xa);
 		analyzeAllButton = new JButton(analyseAllButtonImage);
 		analyzeAllButton.addActionListener(this);
 		analyzeAllButton.setActionCommand("Analyze All");
 		analyzeAllButton.setPreferredSize(ANALYSE_ALL_BUTTON_SIZE);
 		analyzeAllButton.setMaximumSize(ANALYSE_ALL_BUTTON_SIZE);
 		analyseAllPanel.add(analyzeAllButton);
 		
 		leftPanel.add(importExportPanel);
 		leftPanel.add(leftPane);
 		leftPanel.add(analyseAllPanel);
 		
 		//enable buttons
 		flagImageButton.setEnabled(false);
 		prevImageButton.setEnabled(false);
 		nextImageButton.setEnabled(false);
 		analyzeAllButton.setEnabled(false);
 				
 		
 		add(leftPanel);
 		add(rightPanel);
 		
 		pack();
 	}
 
 	public Canvas getMainCanvas(){
 		return mainImageViewCanvas;
 	}
 	
 	public void setButtonsEnabled(boolean enabled){
 		flagImageButton.setEnabled(enabled);
 		prevImageButton.setEnabled(enabled);
 		nextImageButton.setEnabled(enabled);
 		autobutton.setEnabled(enabled);
 		//analyzeAllButton.setEnabled(enabled); not controlled here jm 051012
 	}
 	
 	public void toggleFlagButton(boolean flag) {
 		if (flag) {
 			//set to flag
 			ImageIcon flagBtnImage = new ImageIcon("lib/flag-image-btn.png");
 			Image img = flagBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 			flagBtnImage = new ImageIcon(img);
 			flagImageButton.setIcon(flagBtnImage);
 			flagImageButton.setActionCommand("Flag Image");
 		} else {
 			//set to unflag
 			ImageIcon unflagBtnImage = new ImageIcon("lib/unflag-image-btn.png");
 			Image img = unflagBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 			unflagBtnImage = new ImageIcon(img);
 			flagImageButton.setIcon(unflagBtnImage);
 			flagImageButton.setActionCommand("Unflag Image");
 		}
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
 			if (!getImportedImageList().isEmpty()) {
 				analyzeAllButton.setEnabled(true);
 			}
 			repaint();
 		}
 		else if (action.equals("Export")) {
 			//export features
 			if(!ask) {
 				String exportPath = location;
 				int byteread = 0;
 				InputStream in = null;
 				OutputStream out = null;
 				try {
 					for(TaggableImage image: importedImageList) {
 						if(image.getTag() == ImageTag.INFRINGEMENT) {
 							in = new FileInputStream(image.getSource());
 							out = new FileOutputStream(new File(exportPath + "/" + image.getFileName()));
 							byte[] buffer = new byte[1024];
 							while ((byteread = in.read(buffer)) != -1) {
 								out.write(buffer, 0, byteread);
 							}
 						}
 					}
 				} catch(IOException ioe) {
 					System.out.println("Export failed: " + ioe.getMessage());
 				}
 				return;
 			}
 				
 			JFileChooser fc = new JFileChooser();
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			fc.setDialogTitle("Save images to");
 			int returnVal = fc.showSaveDialog(fc);
 			if(returnVal == JFileChooser.APPROVE_OPTION) {
 				String exportPath = fc.getSelectedFile().getPath();
 				int byteread = 0;
 				InputStream in = null;
 				OutputStream out = null;
 				try {
 					for(TaggableImage image: importedImageList) {
 						if(image.getTag() == ImageTag.INFRINGEMENT) {
 							in = new FileInputStream(image.getSource());
 							out = new FileOutputStream(new File(exportPath + "/" + image.getFileName()));
 							byte[] buffer = new byte[1024];
 							while ((byteread = in.read(buffer)) != -1) {
 								out.write(buffer, 0, byteread);
 							}
 						}
 					}
 				} catch(IOException ioe) {
 					System.out.println("Export failed: " + ioe.getMessage());
 				}
 			}
 		}
 		else if (action.equals("Quit")) {
 			//quit popup
 			if(!warning) System.exit(0);
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
 			toggleFlagButton(false);
 		}
 		else if (action.equals("Unflag Image")) {
 			//unflag the selected image
 			imageGrid.getSelectedImage().setTag(ImageTag.UNTAGGED);
 			imageGrid.repaint();
 			toggleFlagButton(true);
 		}
 		else if (action.equals("Preferences")) {
 			//open preferences window
 			checkSetting();
 			new PreferenceDialog(this);
 			checkSetting();
 		}
 		else if (action.equals("Manual")) {
 			//manual features
 		}
 		else if (action.equals("About")) {
 			//about dialog
 			Object[] option = {"Close"};
 			JOptionPane pane = new JOptionPane("<html><font size = 5>WAI UAVTool</font></html>\nVersion 1.0\n<html><br>Developed by James McCann, James Watling, Sam Etheridge, Yan Dai, Yang Yu</html>\n" +
 					"<html><br>WAI UAVTool is an automatic image classification tool</html>\n\n" + 
 					"Copyright (c) 2012 The Agrisoft Team\nLicense: GNU General Public License Version 2", 
 					JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, new ImageIcon("lib/about.jpg"), option);
 			JDialog dialog = pane.createDialog("About");
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 			if(((String)pane.getValue() != null)) {
 				if(((String)pane.getValue()).equals("Close")) {
 					dialog.dispose();
 				}
 			}
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
 		else if(action.equals("Next Image")) {
 			imageGrid.browse("next");
 		}
 		else if(action.equals("Auto Analyse")) {
 			BufferedImage processedImage = new ImageClassifier().findRiverImage(imageGrid.getSelectedImage().getSource().getPath());
 			imageGrid.getSelectedImage().setImage(processedImage);
 			imageGrid.update();
 			processedImage.flush();
 		}
 		else if(action.equals("Analyze All")) {
 			BufferedImage processedImage = null;
 			boolean first = true;
 			for(ImageThumbPanel itp: imageGrid.getPanels()) {
 				processedImage = new ImageClassifier().findRiverImage(itp.getImage().getSource().getPath());
 				itp.getImage().setImage(processedImage);
 				if(first) {
 					imageGrid.update();
 					first = false;
 				}
 				processedImage.flush();
 			}
 		}
 		else if (action.equals("PDF Report")){ //jm 081012
 			TaggableImage selectedImage = imageGrid.getSelectedImage();
 			if (selectedImage==null) {
 				JOptionPane.showMessageDialog(this, "Please select an Image to export!", "ERROR", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			//get export location
 			String exportPath = "";
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Choose save location for PDF");
 			int returnVal = fc.showSaveDialog(fc);
 			if(returnVal == JFileChooser.APPROVE_OPTION) {
 				exportPath = fc.getSelectedFile().getPath();
 			}
 			String description = "";
 			description = JOptionPane.showInputDialog("Enter a short description of this image to append to the report");
 			ImagePdfExporter export = new ImagePdfExporter(exportPath, selectedImage, description);
 		}
 	}
 
 	public void windowOpened(WindowEvent e) {}
 	public void windowClosing(WindowEvent e) {
 		if(!warning) System.exit(0);
 		int n = JOptionPane.showConfirmDialog(
 			    this,
 			    "Would you like to exit now?",
 			    "Quit",
 			    JOptionPane.YES_NO_OPTION);
 		if(n == 0){System.exit(0);}
 	}
 	
 	public void windowClosed(WindowEvent e) {}	
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
 		String data = (imageGrid.getSelectedImage()!=null?imageGrid.getSelectedImage().getMetaData():"EMPTY");
 		System.out.println(data);
 		metaDataLabel.setText(data);
 		imageMetadataPanel.removeAll();
 		imageMetadataPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		imageMetadataPanel.setLayout(new BorderLayout());
 		imageMetadataPanel.setSize(IMAGE_METADATA_PANEL_SIZE);
 		metaDataLabel.setSize(2*IMAGE_METADATA_PANEL_SIZE.width/3,IMAGE_METADATA_PANEL_SIZE.height);
 		metaDataLabel.setVerticalAlignment(JLabel.TOP);
 		imageMetadataPanel.add(metaDataLabel, BorderLayout.WEST);
 		imageMetadataPanel.setBackground(null);
 		
 	    try {
 			String latitudeS = metaDataLabel.getText().split("GPS Latitude - ", 2)[1].split("</td>", 2)[0];
 			String longitudeS = metaDataLabel.getText().split("GPS Longitude - ")[1].split("</td>",2)[0];
 			
 			Double latitudeNum1 = Double.parseDouble(latitudeS.split((char) 0x00B0+"", 2)[0]);
 			Double latitudeNum2 = Double.parseDouble(latitudeS.split((char) 0x00B0+"", 2)[1].split("' ", 2)[0]);
 			Double latitudeNum3 = Double.parseDouble(latitudeS.split("' ", 2)[1].split("\"", 2)[0]);
 			
 			Double longitudeNum1 = Double.parseDouble(longitudeS.split((char) 0x00B0+"", 2)[0]);
 			Double longitudeNum2 = Double.parseDouble(longitudeS.split((char) 0x00B0+"", 2)[1].split("' ", 2)[0]);
 			Double longitudeNum3 = Double.parseDouble(longitudeS.split("' ", 2)[1].split("\"", 2)[0]);
 			
 			Double latitude; 
 			Double longitude;
 			
 			if(latitudeNum1>=0)
 				latitude = latitudeNum1+latitudeNum2/60+latitudeNum3/3600;
 			else 
 				latitude = latitudeNum1-latitudeNum2/60-latitudeNum3/3600;
 			
 			if(longitudeNum1>=0)
 				longitude= longitudeNum1+longitudeNum2/60+longitudeNum3/3600;
 			else
 				longitude= longitudeNum1-longitudeNum2/60-longitudeNum3/3600;
 			
 			URLConnection con = null;
 			if(useProxy)
 				con = new URL("http",proxyUrl,proxyPort,"http://maps.google.com/maps/api/staticmap?" +
 					"center=Wellington,NZ&zoom=5&size="
 					+IMAGE_METADATA_PANEL_SIZE.width/3+"x"+
 					IMAGE_METADATA_PANEL_SIZE.height+
 					"&maptype=roadmap&sensor=false&" +
 					"markers=||"+latitude+",%20"+longitude).openConnection();
 			InputStream is = con.getInputStream();
 			byte bytes[] = new byte[con.getContentLength()];
 			Toolkit tk = getToolkit();
 			BufferedImage map = ImageIO.read(is);
 			tk.prepareImage(map, -1, -1, null);
 			imageMetadataPanel.add(new JLabel(new ImageIcon(map)),BorderLayout.EAST);
 		}
 	    catch (Exception e1) {
 	    	e1.printStackTrace();
 	    	JLabel errorLabel = new JLabel("<html><p>No Internet connection</p><p>Or no image metadata.</p></html>");
 	    	errorLabel.setVerticalAlignment(JLabel.TOP);
 			imageMetadataPanel.add(errorLabel,BorderLayout.EAST);
 		}
 		
 	    repaint();
 	}
 
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 	public void checkSetting() {
 		Scanner sc;
 		try {
 			sc = new Scanner(new File("settings.data"));
 			if(sc.next().equals("minimize=true")) { minimize = true;}
 			else minimize = false;
 			if(sc.next().equals("warning=true")) {warning = true;}
 			else warning = false;
 			if(sc.next().equals("proxy=true"))
 				useProxy=true;
 			else useProxy=false;
 			String a=sc.nextLine();
 			a=sc.nextLine();
 			System.out.println(a);
 			proxyUrl=a.split("=", 2)[1];
 			proxyPort=Integer.parseInt(sc.nextLine().split("=",2)[1]);
 			if(sc.next().equals("ask=true")) {
 				ask = true;
 				location = null;
 			} else {
 				ask = false;
 				location = sc.next();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
}
