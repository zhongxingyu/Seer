 
 import javax.imageio.ImageIO;
 import javax.swing.JInternalFrame;
 import javax.swing.JDesktopPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JFrame;
 import javax.swing.KeyStroke;
  
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
  
 /*
  * InternalFrameDemo.java requires:
  *   MyInternalFrame.java
  */
 public class ImageLabeller extends JFrame
                                implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ImageDesktop desktop;
     BufferedImage image = null;
     public ImageLabeller() throws Exception {
         super("InternalFrameDemo");
         image = ImageIO.read(new File("images/test.jpg"));
 
 		
         //Make the big window be indented 50 pixels from each edge
         //of the screen.
         int inset = 50;
    
         setBounds(inset,inset,image.getWidth(),
                 image.getHeight()
                   );
  
         //Set up the GUI.
         desktop = new ImageDesktop(image,this);
         
         //create first "window"
         setContentPane(desktop);
         setJMenuBar(createMenuBar());
         //Make dragging a little faster but perhaps uglier.
         
        // desktop.add(image);
     }
  
     protected JMenuBar createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
  
         //Set up the lone menu.
         JMenu menu = new JMenu("Document");
         menu.setMnemonic(KeyEvent.VK_D);
         menuBar.add(menu);
  
         //Set up the first menu item.
         JMenuItem menuItem = new JMenuItem("New");
         menuItem.setMnemonic(KeyEvent.VK_N);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_N, ActionEvent.ALT_MASK));
         menuItem.setActionCommand("new");
         menuItem.addActionListener(this);
         menu.add(menuItem);
  
         //Set up the second menu item.
         menuItem = new JMenuItem("Quit");
         menuItem.setMnemonic(KeyEvent.VK_Q);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_Q, ActionEvent.ALT_MASK));
         menuItem.setActionCommand("quit");
         menuItem.addActionListener(this);
         menu.add(menuItem);
  
         return menuBar;
     }
  
     //React to menu selections.
     public void actionPerformed(ActionEvent e) {
         if ("new".equals(e.getActionCommand())) { //new
             createFrame(30,30);
         } else { //quit
             quit();
         }
     }
  
     //Create a new internal frame.
     protected void createFrame(int x,int y) {
         LabelFrame frame = new LabelFrame(x,y);
         frame.setVisible(true); //necessary as of 1.3
         desktop.add(frame);
         try {
             frame.setSelected(true);
         } catch (java.beans.PropertyVetoException e) {}
     }
  
     //Quit the application.
     protected void quit() {
         System.exit(0);
     }
  
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
     private static void createAndShowGUI() {
         //Make sure we have nice window decorations.
         JFrame.setDefaultLookAndFeelDecorated(true);
  
         //Create and set up the window.
         try {
         ImageLabeller frame = new ImageLabeller();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         //Display the window.
         frame.setVisible(true);
         } catch (Exception e) {
         	System.out.println("fail");
         }
        
        
     }
  
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     }
 }
