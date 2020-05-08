 package SeljeIRC;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 
 
 /**
  * 
  * @author Hallvard Westman
  * 
  */
 
 public class SeljeIRC extends JFrame{
   
    
 	private static final long serialVersionUID = 1L; //Serializeing
         
         MainMenu mainMenu;      //Standard Menu
         
         static tabHandler channelTabs; //JTabbedPane containing all tabs
         static InputField inputField;  //Standard Input field for each tab
         BorderLayout totalLayout; //TotalLayouts
         static Colors colorSettings; // Colorsettings
         
         static ConnectionHandler connection;
         
         boolean isConnected;
 
 	public SeljeIRC(String title) throws BadLocationException{
             super(title);
             
        
             
             /*
              * Connection to server
              */
             
             channelTabs = new tabHandler();
             connection = new ConnectionHandler(channelTabs);
             
             
             colorSettings = new Colors();
             
             /*
              * Layout of main contentPane
              */
             totalLayout = new BorderLayout();
                 setLayout(totalLayout);
             
             /*
              * JTabbedPane containging all tabs
              */
             
                 add(channelTabs,BorderLayout.CENTER);    
                 
                 
             
             /*
              * Setting up the main contentPane menu
              * Passing channelTab-object for creation of new tabs
              */
             mainMenu= new MainMenu(channelTabs,connection);
                 setJMenuBar(mainMenu);
             
             /*
              * Inputfield that should be taking all input
              */
             //TODO pass channelTab-object
                 
    
                 
         /*
          * Basic operations on main contentPane
          */
         setVisible(true);
         pack();
         
         this.addWindowListener(new WindowAdapter()
         {
         public void windowClosing(WindowEvent e)
         {
         // your stuf here
            try{ 
            connection.closeConnection(); 
            }catch(Exception ex){
                System.out.printf("exception onclose");
            }
             
         //System.exit(0);
         }
         });
         
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
     }
         public static ConnectionHandler returnConnection(){
             return connection;
         }
         public void setStatusFocus(){
             SingleTab st = (SingleTab) channelTabs.getComponent(1);
             st.passFocusToField();
         }
 
         
         
         
         
 public static void main(String[] args) throws BadLocationException {
     
         /*
          * Setting up the mainframe, add only functionality related to .this
          */
         
         
          try{
              if(!System.getProperty("os.name").startsWith("Mac OS X"))	// Fuck you guys!
             	 UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");	// Making the bitches ugly
          }catch(Exception e){
             
          }
    
     
     
     
         SeljeIRC mainFrame = new SeljeIRC("SeljeIRC");
             
             
             mainFrame.setVisible(false);
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             mainFrame.setBounds(0,0,screenSize.width, screenSize.height);
             //mainFrame.setSize(new Dimension(1200, 800));
             
             
             mainFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
             
              SplashScreen splashScreen = new SplashScreen("logo2.jpg");
                 splashScreen.splash();
                 //splashScreen.setVisible(false);
             
             
             try {
                   Thread.sleep(1000); //TODO: CHANGE BACK TO 3000
                 }
                 catch(InterruptedException ex) {
                   System.out.println(ex);
                 }
             splashScreen.setVisible(false)  ;
             mainFrame.setVisible(true);
             try {
                   Thread.sleep(100); //gotta wait to set focus
                 }
                 catch(InterruptedException ex) {
                   System.out.println(ex);
                 }
             mainFrame.setStatusFocus();
      
  }
 }
 class ContentPanel extends JPanel {
   Image bgimage = null;
 
   ContentPanel() {
     MediaTracker mt = new MediaTracker(this);
     bgimage = Toolkit.getDefaultToolkit().getImage("src/Images/logo_noframe.png");
     mt.addImage(bgimage, 0);
     try {
       mt.waitForAll();
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
 
   protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     int imwidth = bgimage.getWidth(null);
     int imheight = bgimage.getHeight(null);
     g.drawImage(bgimage, 1, 1, null);
   }
 }
