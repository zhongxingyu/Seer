 package gui;
 
 import images.ImageTag;
 import images.TaggableImage;
 
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
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 import application.ImageClassifier;
 import application.ImageLoader;
 import application.ImagePdfExporter;
 
 /**
  * This class presents the main window of the application.
  */
 public class ApplicationWindow extends JFrame implements ActionListener, WindowListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 	
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
 	
 	private static List<TaggableImage> importedImageList;
 
 	private ImageGridPanel imageGrid;
 	private ImageCanvas mainImageViewCanvas;
 	private JPanel rightPanel;
 	private ImageMetadataPanel imageMetadataPanel;
 	private ProgressWindow pw;
 	
 	private JButton importButton;
 	private JButton exportButton;
 	private JButton flagImageButton;
 	private JButton nextImageButton;
 	private JButton prevImageButton;
 	private JButton autobutton;
 	private JButton analyzeAllButton;
 
 	public static boolean noConnection = false;
 	public static BufferedImage WAI_LOGO;
 	public static BufferedImage IMPORT_PLACEHOLDER;
 	public static final Color WAI_BLUE = new Color(0, 126, 166);
 
 	/**
 	 * Constructs a new application window.
 	 */
 	public ApplicationWindow() {
 		setTitle("WAINZ UAVTool");
 		setLayout(new FlowLayout());
 		setResizable(false);
 		addWindowListener(this);
 		initialiseMenus();
 		initialiseWindow();
 		imageLoader = new ImageLoader();
 		setImportedImageList(new ArrayList<TaggableImage>());
 	    
 		pack();
 		
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension scrnsize = toolkit.getScreenSize();
 		setBounds((scrnsize.width - getWidth()) / 2, (scrnsize.height - getHeight()) / 2, getWidth(), getHeight());
 		
 		setVisible(true);
 		checkSetting();
 	}
 
 	/**
 	 * Sets up the menus and menu items for the application window.
 	 */
 	private void initialiseMenus() {
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
 		help.add(manual);
 		help.add(about);
 		
 		//Setting shortcuts and Mnemonics for File Menu
 		importItem.setMnemonic('I');
 		importItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.Event.CTRL_MASK));
 		exportItem.setMnemonic('E');
 		exportItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.Event.CTRL_MASK));
 		quitItem.setMnemonic('W');
 		quitItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.Event.CTRL_MASK));
 		
 		//Setting shortcuts and Mnemonics for Options Menu
 		flagItem.setMnemonic('F');
 		flagItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK));
 		unflagItem.setMnemonic('U');
 		unflagItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.Event.CTRL_MASK));
 		pdfReportItem.setMnemonic('R');
 		pdfReportItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.Event.CTRL_MASK));
 		preferencesItem.setMnemonic('P');
 		preferencesItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.Event.CTRL_MASK));
 		
 		//Setting shortcuts and Mnemonics for Help Menu
 		manual.setMnemonic('M');
 		manual.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.Event.CTRL_MASK));
 		about.setMnemonic('A');
 		about.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.Event.CTRL_MASK));
 		
 		setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Sets up the panels and buttons for the applicaton window.
 	 */
 	private void initialiseWindow() {
 		try {
 			String importPlaceholderPath = "lib/import-placeholder.jpg";
 			IMPORT_PLACEHOLDER = ImageIO.read(new File(importPlaceholderPath));
 		} catch (IOException e) {
 			System.out.println("Error reading WAI Logo");
 		}
 		
 		// initializes the image thumbnail panel of the window
 		imageGrid = new ImageGridPanel(null, this);
 		JScrollPane leftPane = new JScrollPane(imageGrid);
 		leftPane.setPreferredSize(leftPaneSize);
 		leftPane.setMaximumSize(leftPaneSize);
 
 		mainImageViewCanvas = new ImageCanvas(imageGrid);
 		mainImageViewCanvas.setSize(IMAGE_CANVAS_SIZE);
 		mainImageViewCanvas.setPreferredSize(IMAGE_CANVAS_SIZE);
 		mainImageViewCanvas.setMaximumSize(IMAGE_CANVAS_SIZE);
 		
 		imageGrid.setCanvas(mainImageViewCanvas);
 		
 		// sets up the size and layout of the right panel
 		rightPanel = new JPanel();
 		rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		
 		// sets up all buttons on the right panel of the window
 		JPanel imageButtonPanel = new JPanel();
 		imageButtonPanel.setLayout(new GridLayout(1, 4));
 		imageButtonPanel.setSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setPreferredSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setMaximumSize(IMAGE_BUTTON_PANEL_SIZE);
 		
 		// previous button
 		ImageIcon prevBtnImage = new ImageIcon("lib/prev-image-btn.png");
 		Image xa = prevBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		prevBtnImage = new ImageIcon(xa);
 		prevImageButton = new JButton(prevBtnImage);
 		prevImageButton.addActionListener(this);
 		prevImageButton.setActionCommand("Previous Image");
 		imageButtonPanel.add(prevImageButton);
 		
 		// flag and unflag button
 		ImageIcon flagBtnImage = new ImageIcon("lib/flag-image-btn.png");
 		xa = flagBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		flagBtnImage = new ImageIcon(xa);
 		flagImageButton = new JButton(flagBtnImage);
 		flagImageButton.addActionListener(this);
 		flagImageButton.setActionCommand("Flag Image");
 		imageButtonPanel.add(flagImageButton);
 		
 		// auto analyze button
 		ImageIcon autoButtonImage = new ImageIcon("lib/auto-analyse-btn-gold.png");
 		xa = autoButtonImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		autoButtonImage = new ImageIcon(xa);
 		autobutton = new JButton(autoButtonImage);
 		autobutton.setEnabled(false);
 		autobutton.setActionCommand("Auto Analyse");
 		autobutton.addActionListener(this);
 		imageButtonPanel.add(autobutton);
 		
 		// next image button
 		ImageIcon nextBtnImage = new ImageIcon("lib/next-image-btn.png");
 		xa = nextBtnImage.getImage().getScaledInstance(IMAGE_BUTTON_PANEL_SIZE.width/4, IMAGE_BUTTON_PANEL_SIZE.height, Image.SCALE_SMOOTH);
 		nextBtnImage = new ImageIcon(xa);
 		nextImageButton = new JButton(nextBtnImage);
 		nextImageButton.setActionCommand("Next Image");
 		nextImageButton.addActionListener(this);
 		imageButtonPanel.add(nextImageButton);
 		
 		//image metadata panel
 		imageMetadataPanel = new ImageMetadataPanel(IMAGE_METADATA_PANEL_SIZE);
 		
 		// adds all inner panels to the right panel
 		rightPanel.add(mainImageViewCanvas);
 		rightPanel.add(imageButtonPanel);
 		rightPanel.add(imageMetadataPanel);
 		
 		// sets up the left panel of the window
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		
 		JPanel importExportPanel = new JPanel();
 		importExportPanel.setLayout(new GridLayout(1, 2));
 		importExportPanel.setPreferredSize(IMEX_BUTTON_PANEL_SIZE);
 		importExportPanel.setMaximumSize(IMEX_BUTTON_PANEL_SIZE);	
 		
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
 		
 		// disable buttons
 		setButtonsStatus(false);
 		
 		add(leftPanel);
 		add(rightPanel);
 		
 		pack();
 	}
 	
 	/**
 	 * Changes the status of all buttons according to the specification.
 	 * @param enabled
 	 */
 	private void setButtonsStatus(boolean enabled) {
 		prevImageButton.setEnabled(enabled);
 		flagImageButton.setEnabled(enabled);
 		autobutton.setEnabled(enabled);
 		nextImageButton.setEnabled(enabled);
 	}
 	
 	/**
 	 * Changes the image of flag/unflag button according to the specification.
 	 * @param flag
 	 */
 	public void toggleFlagButton(boolean flag) {
 		if(flag) {
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
 		
 		if(action.equals("Import")) {
 			final ApplicationWindow aw = this;
 			new Thread(new Runnable() {
 				public void run() {
 					List<TaggableImage> importSet = imageLoader.importImages(aw);
 					pw = new ProgressWindow(rightPanel, 0);
 					if (importSet == null) {
 						pw.dispose();
 						return;
 					}
 					setImportedImageList(importSet);
 					imageGrid.setImageList(getImportedImageList());
 					imageGrid.initialise(pw);
 					imageGrid.repaint();
 					mainImageViewCanvas.repaint();
 					if (!getImportedImageList().isEmpty()) {
 						analyzeAllButton.setEnabled(true);
 					}
 					repaint();
 					pw.dispose();
 				}
 			}).start();
 			
 		}
 		else if(action.equals("Export")) {
 			if(!ask) { // indicates save location has been specified already
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
 					JOptionPane.showMessageDialog(this, "Flagged Images have been exported successfully");
 				} catch(IOException ioe) {
 					System.out.println("Export failed: " + ioe.getMessage());
 				}
 				return;
 			}
 			
 			// prompts dialog for user to choose save location
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
 					JOptionPane.showMessageDialog(this, "Flagged Images have been exported successfully");
 				} catch(IOException ioe) {
 					System.out.println("Export failed: " + ioe.getMessage());
 				}
 			}
 		}
 		else if (action.equals("Quit")) {
 			if(!warning) System.exit(0);
 			int n = JOptionPane.showConfirmDialog(
 				    this,
 				    "Would you like to exit now?",
 				    "Quit",
 				    JOptionPane.YES_NO_OPTION);
 			if(n == 0){System.exit(0);}
 		}
 		else if (action.equals("Flag Image")) {
 			// flag currently selected image
 			imageGrid.getSelectedImage().setTag(ImageTag.INFRINGEMENT);
 			imageGrid.repaint();
 			toggleFlagButton(false);
 		}
 		else if (action.equals("Unflag Image")) {
 			// unflag the selected image
 			imageGrid.getSelectedImage().setTag(ImageTag.UNTAGGED);
 			imageGrid.repaint();
 			toggleFlagButton(true);
 		}
 		else if (action.equals("Preferences")) {
 			checkSetting();
 			new PreferenceDialog(this);
 			checkSetting();
 		}
 		else if (action.equals("Manual")) {
 			//manual features
 		}
 		else if (action.equals("About")) {
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
 		else if (action.equals("Previous Image")) {
 			imageGrid.browse("previous");
 		}
 		else if(action.equals("Next Image")) {
 			imageGrid.browse("next");
 		}
 		else if(action.equals("Auto Analyse")) {
 			pw = new ProgressWindow(rightPanel, 1);
 			new Thread(new Runnable() {
 				public void run() {
 					BufferedImage processedImage = new ImageClassifier().findRiverImage(imageGrid.getSelectedImage().getSource().getPath());
 					imageGrid.getSelectedImage().setImage(processedImage);
 					imageGrid.update();
 					processedImage.flush();
 					pw.dispose();
 				}
 			}).start();
 		}
 		else if(action.equals("Analyze All")) {
 			pw = new ProgressWindow(rightPanel, 1);
 			new Thread(new Runnable() {
 				public void run() {
 					int interval = 100/importedImageList.size() + 1;
 					int progress = 1;
 					BufferedImage processedImage = null;
 					for(ImageThumbPanel itp: imageGrid.getPanels()) {
 						if(itp.getImage() == null) break;
 						processedImage = new ImageClassifier().findRiverImage(itp.getImage().getSource().getPath());
 						itp.getImage().setImage(processedImage);
 						processedImage.flush();
 						pw.setValue((progress++) * interval);
 					}
 					imageGrid.update();
 					pw.dispose();
 				}
 			}).start();
 		}
 		else if(action.equals("PDF Report")) { //jm 081012
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
 			new ImagePdfExporter(exportPath, selectedImage, description);
 		}
 	}
 	
 	public void windowClosing(WindowEvent e) {
 		if(!warning) System.exit(0);
 		int n = JOptionPane.showConfirmDialog(
 			    this,
 			    "Would you like to exit now?",
 			    "Quit",
 			    JOptionPane.YES_NO_OPTION);
 		if(n == 0){System.exit(0);}
 	}
 	
 	public void windowOpened(WindowEvent e) {}
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
 	
 	/**
 	 * This listener is added to each imageThumbPanel.
 	 */
 	public void mouseClicked(MouseEvent e) {
 		setButtonsStatus(imageGrid.getSelectedImage()!=null);
 		String data = (imageGrid.getSelectedImage()!=null?imageGrid.getSelectedImage().getMetaData():null);
 		imageMetadataPanel.setCurrentImage(imageGrid.getSelectedImage());
 		imageMetadataPanel.setMetaDataLabelText(data);
 	    repaint();
 	}
 	
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 	public void checkSetting() {
 		noConnection = false;
 		Scanner sc;
 		try {
 			sc = new Scanner(new File("settings.data"));
 			if(sc.next().equals("minimize=true")) { minimize = true;}
 			else minimize = false;
 			if(sc.next().equals("warning=true")) {warning = true;}
 			else warning = false;
 			if(sc.next().equals("proxy=true")) useProxy=true;
 			else useProxy=false;
 			String s = sc.nextLine();
 			s = sc.nextLine();
 			System.out.println(s);
 			proxyUrl = s.split("=", 2)[1];
 			proxyPort = Integer.parseInt(sc.nextLine().split("=",2)[1]);
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
 	
 	public JPanel getRightPanel() { return rightPanel; }
 	
 }
