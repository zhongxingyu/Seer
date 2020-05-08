 package com.rocel.adg.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import net.iharder.dnd.FileDrop;
 
 import com.rocel.adg.Globals;
 import com.rocel.adg.controlers.IDropControler;
 
 public class DropFrame extends JFrame {
 	private static final int WINDOW_WIDTH = 255;//600;
 	private static final int WINDOW_HEIGHT = 300;//600;
 	private static final String TITLE = "Android Drawable Generator";
 
 	private static final long serialVersionUID = 1L;
 	private IDropControler controler;
 	private JPanel contentPanel;
 	private JLabel dropFileLabel;
 	
 	private URL imageurl;
 	private Image imgDrop;
 	private Image imgWaiting;
 	private Image imgSave;
 	private JLabel subtitleLabel;
 	
 	public DropFrame(IDropControler controler) throws HeadlessException {
 		super();
 		this.controler = controler;
 		createUI();
 	}
 
 	private void createUI() {
 		this.setTitle(TITLE);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		this.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
 		this.setResizable(false);
		//center window in the middle the screen because why the fuck it gets *somewhere* in the top left?!
 		this.setLocationRelativeTo(null);
 		URL imageurl = getClass().getResource(Globals.PATH_ICON_IMG);
		System.out.println("imageurl:"+ imageurl);
		System.out.println("icon path:"+ Globals.PATH_ICON_IMG);
         Image appIcon = Toolkit.getDefaultToolkit().getImage(imageurl);
 		this.setIconImage(appIcon);
 		this.getContentPane().add(getCenterContent());
 	}
 
 	private Component getCenterContent() {
 		contentPanel = new JPanel(new BorderLayout());
 		contentPanel.setBackground(new Color(238,238,238));
 		
 		imageurl = getClass().getResource(Globals.PATH_DROP_IMG);
         imgDrop = Toolkit.getDefaultToolkit().getImage(imageurl);
 
 		imageurl = getClass().getResource(Globals.PATH_WAITING_IMG);
         imgWaiting = Toolkit.getDefaultToolkit().getImage(imageurl);
 
 		imageurl = getClass().getResource(Globals.PATH_SAVE_IMG);
         imgSave = Toolkit.getDefaultToolkit().getImage(imageurl);
         
         dropFileLabel = new JLabel("", JLabel.CENTER);
 		dropFileLabel.setIcon(new ImageIcon(imgDrop));
 		dropFileLabel.setBackground(Color.RED);
 		
 		contentPanel.add(dropFileLabel,BorderLayout.CENTER);
 		
 		subtitleLabel = new JLabel("Drop your XHDPI file/folder here", JLabel.CENTER);
 		subtitleLabel.setBorder( new EmptyBorder( 0, 6, 16, 6) );
 		
 		contentPanel.add(subtitleLabel,BorderLayout.SOUTH);
 		
 		new FileDrop(contentPanel, new FileDrop.Listener() {
 			public void filesDropped(File[] files) {
 				displayWaiting();
 				ResizableThread thread = new ResizableThread(files);
 				thread.start();
 			}
 		});
 		return contentPanel;
 	}
 	
 	private void displayWaiting(){
 		dropFileLabel.setIcon(new ImageIcon(imgWaiting));
 	}
 	
 	protected void saveFileDownloader() {
 		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
 		fileChooser.setSelectedFile(new File(Globals.EXPORT_ZIP_NAME));
 	    FileFilter filter = new FileNameExtensionFilter(Globals.EXPORT_ZIP_FILTER, Globals.EXPORT_ZIP_EXTENSION);
 		fileChooser.setFileFilter(filter);
 		int returnVal = fileChooser.showSaveDialog(null);
 		if (fileChooser.getSelectedFile() != null) {
 			File fileToSave = fileChooser.getSelectedFile();
 			controler.saveFile(fileToSave.getAbsolutePath());
 		}
 	}
 
 	private void displaySaveUI() {
 		dropFileLabel.setIcon(new ImageIcon(imgDrop));
 	}
 	
 	public void display() {
 		this.setVisible(true);
 	}
 
 	class ResizableThread extends Thread{
 		File[] files;
 		
 		public ResizableThread(File[] files){
 			this.files = files;
 		}
 		
 		@Override
 		public void run() {
 			if (controler.generateDrawables(files)) {
 				saveFileDownloader();
 				displaySaveUI();
 			}
 		}
 
 	}
 }
