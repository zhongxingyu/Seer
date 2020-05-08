 package gui;
 
 import images.TaggableImage;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
import java.awt.DisplayMode;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 import application.ImageLoader;
 
 
 public class ApplicationWindow extends JFrame implements ActionListener, WindowListener {
	private static DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode();
	private static Dimension dim = new Dimension(mode.getWidth(), mode.getHeight());
 	
 	
 	private static final Dimension RIGHT_PANEL_SIZE = new Dimension(dim.width * 3 / 5, dim.height - 110);
 	private ImageLoader imageLoader;
 	private static final long serialVersionUID = 1L;
 	private static final Dimension leftPaneSize = new Dimension(dim.width * 1 / 5, dim.height - 110);
 	
 	private List<TaggableImage> importedImageList;
 
 	private ImageGridPanel imageGrid;
 	private Canvas mainImageViewCanvas;
 	private int currentImage = 0;
 
 	public ApplicationWindow(){
 		importedImageList = new ArrayList<TaggableImage>();
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
 		//initialise empty image grid jm 080912
 		imageGrid = new ImageGridPanel(null);
 		JScrollPane leftPane = new JScrollPane(imageGrid);
 
 		JPanel rightPanel = new JPanel();
 		leftPane.setPreferredSize(leftPaneSize);
 
 		rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
 		rightPanel.setBackground(new Color(0, 225, 0));
 		
 		mainImageViewCanvas = new Canvas(){
 			private static final long serialVersionUID = 2491198060037716312L;
 
 			public void paint(Graphics g){
 				setSize(RIGHT_PANEL_SIZE);
 				Image currentImage = currentImage();
 				if(currentImage==null)return;
 				double width = getWidth();
 				double imageWidth = currentImage.getWidth(this);
 				double height = getHeight();
 				double imageHeight = currentImage.getHeight(this);
 				
 				if(imageHeight>height){
 					width = imageWidth * (height/imageHeight);
 					System.out.println(1);
 				}
 				else if(imageWidth>width){
 					height = imageHeight * (width/imageWidth);
 					System.out.println(2);
 				}
 				
 				g.drawImage(currentImage(), 0, 0, (int)width, (int)height, this);
 			}
 		};
 		mainImageViewCanvas.setSize(rightPanel.getPreferredSize());
 		rightPanel.add(mainImageViewCanvas);
 		
 		add(leftPane);
 		add(rightPanel);
 		this.setAlwaysOnTop(true);
 		pack();
 		setVisible(true);
 		
 	}
 	
 	private Image currentImage(){
 		if(importedImageList != null){
 		if(importedImageList.size()==0)
 			return null;
 		return importedImageList.get(currentImage).getImage();
 	}
 		return null;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 		//Debug:
 		System.out.println(action); //jm 070912
 
 		if (action.equals("Import")) {
 			//import features
 			importedImageList = imageLoader.importImages(this);
 			imageGrid.setImageList(importedImageList);
 			imageGrid.initialise();
 			imageGrid.repaint();
 			mainImageViewCanvas.repaint();
 		}
 		if (action.equals("Export")) {
 			//export features
 		}
 		if (action.equals("Quit")) {
 			//quit popup
 			int n = JOptionPane.showConfirmDialog(
 				    this,
 				    "Would you like to exit now?",
 				    "Quit",
 				    JOptionPane.YES_NO_OPTION);
 			if(n == 0){System.exit(0);}
 		}
 		if (action.equals("Flag Image")) {
 			//flag currently selected image
 		}
 		if (action.equals("Unflag Image")) {
 			//unflag the selected image
 		}
 		if (action.equals("Preferences")) {
 			//open preferences window
 		}
 		if (action.equals("Manual")) {
 			//manual features
 		}
 		if (action.equals("About")) {
 			//about dialog
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
 }
