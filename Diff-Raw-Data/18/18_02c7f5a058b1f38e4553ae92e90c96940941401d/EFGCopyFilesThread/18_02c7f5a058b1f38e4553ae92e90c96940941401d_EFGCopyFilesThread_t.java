 package project.efg.client.utils.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingConstants;
 
 import project.efg.client.utils.nogui.DropFileObject;
 
 
 
 /*
 * Borrowed from ProgressBarDemo.java is a 1.4 
 * application sample files distributed with the jDK.
  */
 public class EFGCopyFilesThread extends SwingWorker implements WindowListener{
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 
     private JProgressBar progressBar;
    
   
     private DnDFileBrowser browser;
     boolean isDone = false;
    File srcFile, destFile;
    JFrame frame;
    List objectsToDrop;
 
 
 //private FileNode destNode;
 public EFGCopyFilesThread(DnDFileBrowser browser,List objectsToDrop, JProgressBar bar) {
 	this.objectsToDrop = objectsToDrop;
 	this.browser = browser;
 	this.progressBar = bar;
 	init();
 }
     public EFGCopyFilesThread(DnDFileBrowser browser, File srcFile, 
     		File destFile, JProgressBar progressBar) {
      this(browser,srcFile,destFile,progressBar,null);
     }
 	public EFGCopyFilesThread(DnDFileBrowser browser, File srcFile, File destFile, JProgressBar progressBar, FileNode destNode) {
 		
     	this.objectsToDrop = new ArrayList();
     	DropFileObject drop = new DropFileObject(srcFile,destFile, destNode);
     	this.objectsToDrop.add(drop);
         this.browser = browser;
         this.progressBar = progressBar;
         
         this.init();
   
         
     
     
 	}
 	private void init(){
 	    JPanel panel = new JPanel(new BorderLayout());
         panel.setSize(550,500);
      JLabel label = new JLabel("Please wait while application copies image files!!", 
        		SwingConstants.CENTER);
       label.setSize(300,300);
         panel.add(this.progressBar, BorderLayout.CENTER);
       panel.add(label,BorderLayout.NORTH);
 //		Create and set up the window.
 	    this.frame = new JFrame("Copying Files");
 	    
 	    this.frame.addWindowListener(this);
 	    this.frame.setSize(600, 600);
 	    this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	   // this.frame.setLocationRelativeTo(this.browser);
	    this.frame.setLocation(this.browser.getLocationOnScreen().x - (this.frame.getWidth()/2), this.browser.getLocationOnScreen().y);

 	    //Create and set up the content pane.
 	   
 	    panel.setOpaque(true); //content panes must be opaque
 	    this.frame.getContentPane().add(panel);
 	  
 	    //Display the window.
 	    this.frame.pack();
 	    this.frame.setVisible(true);  
 	    this.frame.requestFocus();
 	  
 	}
 	/**
 	 * @param objectsToDrop
 	 * @param bar
 	 */
 	
 	public Object construct() {
 		progressBar.setCursor(null);
 		for(int i = 0; i < this.objectsToDrop.size();i++){
 			DropFileObject drop = (DropFileObject)this.objectsToDrop.get(i);
 			this.srcFile = drop.getSourceFile();
 			this.destFile = drop.getDestinationFile();
 			this.browser.copyFile(this.srcFile, this.destFile);
 		}
 			//Toolkit.getDefaultToolkit().beep();
 		this.progressBar.setValue(0);
 		CreateThumbNailsThread thumbsThread = 
 			new CreateThumbNailsThread(this.browser,this.objectsToDrop);
 		thumbsThread.start();
 	    isDone = true;
 		this.frame.dispose();
 		this.browser.setVisible(true);
 
 		return null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
 	 */
 	public void windowClosing(WindowEvent e) {
 		if(!isDone){
 			JOptionPane.showMessageDialog(this.frame, "Application is copying image files. Interrupting it could cause some problems!", "Done",
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
 	 */
 	public void windowActivated(WindowEvent e) {
 	
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
 	 */
 	public void windowClosed(WindowEvent e) {
 	
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
 	 */
 	public void windowDeactivated(WindowEvent e) {
 	
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
 	 */
 	public void windowDeiconified(WindowEvent e) {
 	
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
 	 */
 	public void windowIconified(WindowEvent e) {
 	
 		
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
 	 */
 	public void windowOpened(WindowEvent e) {
 		
 		
 	}
 	
 	
 
 }
