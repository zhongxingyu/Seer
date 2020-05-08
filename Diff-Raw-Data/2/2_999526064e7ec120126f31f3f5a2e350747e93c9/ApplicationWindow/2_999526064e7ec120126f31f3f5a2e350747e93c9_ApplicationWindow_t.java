 package gui;
 
 import images.ImageTag;
 import images.TaggableImage;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
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
 import javax.swing.JButton;
 import javax.swing.JFrame;
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
 	
 	private static final Dimension RIGHT_PANEL_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 110);
	private static final Dimension IMAGE_CANVAS_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 455); //this one will be dynamic
 	private static final Dimension IMAGE_BUTTON_PANEL_SIZE = new Dimension(dim.width * 3 / 5, 45);
 	private static final Dimension IMAGE_METADATA_PANEL_SIZE = new Dimension(dim.width * 3 / 5, 300);
 	
 	private ImageLoader imageLoader;
 	private static final long serialVersionUID = 1L;
 	private static final Dimension leftPaneSize = new Dimension(dim.width * 1 / 5, dim.height - 110);
 	
 	private static List<TaggableImage> importedImageList;
 
 	private ImageGridPanel imageGrid;
 	
 	private Canvas mainImageViewCanvas;
 	private static Font mainImageViewCanvasFont = new Font("Arial", Font.BOLD, 14);
 	private static Font defaultImageViewCanvasFont = new Font("Arial", Font.BOLD, 18);
 	
 	private JPanel imageMetadataPanel;
 	
 	private JButton nextImageButton;
 	private JButton prevImageButton;
 	
 	private static BufferedImage WAI_LOGO;
 	private static final Color WAI_BLUE = new Color(0, 126, 166);
 
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
 			String logoPath = "lib/wai-default.jpg";
 			WAI_LOGO = ImageIO.read(new File(logoPath));
 		} catch (IOException e) {
 			System.out.println("Error reading WAI Logo");
 		}
 		
 		JPanel rightPanel = new JPanel();
 		rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS)); //changed to FlowLayout jm 180912
 		
 		mainImageViewCanvas = new Canvas() {
 			private static final long serialVersionUID = 2491198060037716312L;
 		     
 			public void paint(Graphics g){
 				Color background;
 				setSize(IMAGE_CANVAS_SIZE);
 				setPreferredSize(IMAGE_CANVAS_SIZE);
 				setMaximumSize(IMAGE_CANVAS_SIZE);
 				Image currentImage;
 				if(imageGrid.getSelectedImage() == null) {
 					
 					//render the default WAINZ image jm 180912
 					background = Color.WHITE;
 					g.setColor(background);
 					g.fillRect(0, 0, getWidth(), getHeight());
 					int drawHeight = WAI_LOGO.getHeight();
 					int drawWidth = WAI_LOGO.getWidth();
 					int widthOffset = getWidth() - drawWidth;
 					int heightOffset = getHeight() - drawHeight;
 					int xPos = widthOffset / 2;
 					int yPos = heightOffset / 2;
 					
 					g.drawImage(WAI_LOGO, xPos, yPos, drawWidth, drawHeight, null);
 					return;
 				}
 				
 				background = Color.BLACK;
 				currentImage = imageGrid.getSelectedImage().getImage();
 				
 				if (currentImage == null) return; //error
 				
 				int canvasWidth = getWidth();
 				int canvasHeight = getHeight() - 30; //leave room for label at bottom jm 180912
 				int imageWidth = currentImage.getWidth(this);
 				int imageHeight = currentImage.getHeight(this);
 				int drawWidth, drawHeight = 0;
 				double aspectRatio = imageWidth / (1.0 * imageHeight);
 				
 				System.out.println("aspectRatio: " + aspectRatio);
 				
 				if (imageWidth > canvasWidth || imageHeight > canvasHeight) {
 					//image is bigger than canvas so we need to scale it 
 					if(aspectRatio > 1){
 						// wider than it is tall, scale to fit on canvas
 						drawWidth = canvasWidth;
 						drawHeight = (int)(drawWidth/aspectRatio * 1.0);
 					} else { 
 						// taller than it is wide, height should be the same as canvasHeight
 						// and width scaled down appropriately
 						drawHeight = canvasHeight;
 						drawWidth = (int)(aspectRatio*drawHeight);
 					}
 				} else {
 					drawWidth = imageWidth;
 					drawHeight = imageHeight;
 				}
 				
 				System.out.println("drawWidth: " + drawWidth);
 				System.out.println("drawHeight: " + drawHeight);
 				
 				// get position required for painting to center the image jm 180912
 				int widthOffset = canvasWidth - drawWidth;
 				int heightOffset = canvasHeight - drawHeight;
 				int xPos = widthOffset / 2;
 				int yPos = heightOffset / 2;
 				
 				System.out.println("widthOffset: " + widthOffset);
 				System.out.println("heightOffset: " + heightOffset);
 				System.out.println("xPos: " + xPos);
 				System.out.println("yPos: " + yPos);
 				
 				// paint canvas background black, paint image jm 180912
 				g.setColor(Color.BLACK);
 				g.fillRect(0, 0, getWidth(), getHeight());
 				g.drawImage(currentImage, xPos, yPos, drawWidth, drawHeight, this);
 				
 				// paint image filename label underneath image jm 180912
 				String filename = imageGrid.getSelectedImage().getFileName();
 				g.setFont(mainImageViewCanvasFont);
 				FontMetrics fm = g.getFontMetrics();
 				int strWidth = fm.stringWidth(filename);
 				int strX = (canvasWidth-strWidth)/2;
 				g.setColor(Color.WHITE);				
 				g.drawString(imageGrid.getSelectedImage().getFileName(), strX, getHeight() - 10); //use actual canvas height
 			}
 		};
 		
 		//JLabel infoLabel = new JLabel(infoIcon);
 		//infoLabel.setBounds(10, 10, infoIcon.getIconWidth(), infoIcon.getIconHeight());
 		//infoLabel.addMouseListener(this);
 		
 		JPanel imageButtonPanel = new JPanel();
 		imageButtonPanel.setLayout(new GridLayout(1, 4)); //changed to GridLayout jm 180912
 		imageButtonPanel.setPreferredSize(IMAGE_BUTTON_PANEL_SIZE);
 		imageButtonPanel.setMaximumSize(IMAGE_BUTTON_PANEL_SIZE);
 		
 		//previous button
 		prevImageButton = new JButton("Previous Image");
 		prevImageButton.addActionListener(this);
 		imageButtonPanel.add(prevImageButton);
 		
 		//flag/unflag button
 		JButton flagButton = new JButton("Flag Image");
 		JButton unflagButton = new JButton("Unflag Image");
 		flagButton.addActionListener(this);
 		unflagButton.addActionListener(this);
 		imageButtonPanel.add(flagButton);
 		imageButtonPanel.add(unflagButton);
 		
 		//next button
 		nextImageButton = new JButton("Next Image");
 		nextImageButton.addActionListener(this);
 		imageButtonPanel.add(nextImageButton);
 	
 		
 		//meta-data pane below buttons
 		imageMetadataPanel = new JPanel();
 		imageMetadataPanel.setPreferredSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setSize(IMAGE_METADATA_PANEL_SIZE);
 		imageMetadataPanel.setBackground(Color.RED);
 		
 		rightPanel.add(mainImageViewCanvas);
 		rightPanel.add(imageButtonPanel);
 		rightPanel.add(imageMetadataPanel);
 		
 		//leftPanel
 		imageGrid = new ImageGridPanel(null, this);
 		
 		JScrollPane leftPane = new JScrollPane(imageGrid);
 		leftPane.setPreferredSize(leftPaneSize);
 		
 		//debug jm 
 		//mainImageViewCanvas.setBackground(Color.BLUE);
 		//rightPanel.setBackground(Color.green);
 
 		add(leftPane);
 		add(rightPanel);
 		pack();
 		setVisible(true);
 	}
 	
 	public Canvas getMainCanvas(){
 		return mainImageViewCanvas;
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
 		else if (action.equals("Show Metadata")){
 			JOptionPane.showMessageDialog(
 					null,
 					imageGrid!=null&&imageGrid.getSelectedImage()!=null&&imageGrid.getSelectedImage().getMetaData()!=null?
 							imageGrid.getSelectedImage().getMetaData():
 								"NO METADATA");
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
 	public void mouseClicked(MouseEvent e) {}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 }
