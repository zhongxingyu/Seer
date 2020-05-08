 package game;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 /**
  * Main menu, the OpenGL portion of the game will be started from here
  * @author tim
  *
  */
 public class MainMenu extends JPanel implements ActionListener {
     private static final long serialVersionUID = -3569270152514757138L;
     //background image
     BufferedImage background;
     int y, x;
     //paths to all images used
     ImageIcon play, play_selected, settings, settings_selected, quit, quit_selected;
     private static final String BACKGROUND_PATH = "assets/images/background.jpg";
     private static final String PLAY_PATH  = "assets/images/play.png";
     private static final String SETTINGS_PATH = "assets/images/settings.png";
     private static final String QUIT_PATH = "assets/images/quit.png";
     private static final String PLAY_PATH_SELECTED  = "assets/images/play_selected.png";
     private static final String SETTINGS_PATH_SELECTED = "assets/images/settings_selected.png";
     private static final String QUIT_PATH_SELECTED = "assets/images/quit_selected.png";
 
     //buttons
     JButton playButton, joinGameButton, settingsButton, quitButton;
 
     public boolean menuVisible;
 
     public MainMenu() throws IOException {        
         //loads all images from assets/images/
         try {
             background = ImageIO.read(new File(BACKGROUND_PATH));
             play = new ImageIcon(PLAY_PATH);
             settings = new ImageIcon(SETTINGS_PATH);
             quit = new ImageIcon(QUIT_PATH);
             play_selected = new ImageIcon(PLAY_PATH_SELECTED);
             settings_selected = new ImageIcon(SETTINGS_PATH_SELECTED);
             quit_selected = new ImageIcon(QUIT_PATH_SELECTED);
         } catch (IOException e) {
             System.out.println("Can't find image in assets");
             e.printStackTrace();
         }
 
         //creates new frame
         JFrame f = new JFrame();
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.add(this);
         f.setSize(900,900);
 
         //adds gui elements
         createGUI();
 
         //sets the menu visibility as true
         f.setVisible(true);
         menuVisible = true;
     }
 
     /**
      * creates and adds gui components
      * 
      */
     private void createGUI() {
         this.setLayout( new FlowLayout(FlowLayout.RIGHT, 60, 10) );
 
         playButton = new JButton(play);   
         playButton.setOpaque(false);
         playButton.setBorderPainted(false);
         playButton.setContentAreaFilled(false);
         playButton.setBorder(null);
         playButton.setRolloverIcon(play_selected);
         playButton.addActionListener(this);
 
         joinGameButton = new JButton("Join Game");
         joinGameButton.addActionListener(this);
 
         settingsButton = new JButton(settings);
         settingsButton.setOpaque(false);
         settingsButton.setBorderPainted(false);
         settingsButton.setContentAreaFilled(false);
         settingsButton.setBorder(null);
         settingsButton.setRolloverIcon(settings_selected);
         settingsButton.addActionListener(this);
 
         quitButton = new JButton(quit);
         quitButton.setOpaque(false);
         quitButton.setBorderPainted(false);
         quitButton.setContentAreaFilled(false);
         quitButton.setBorder(null);
         quitButton.setRolloverIcon(quit_selected);
         quitButton.addActionListener(this);
 
         add(playButton);
         add(joinGameButton);
         add(settingsButton);
         add(quitButton);
 
     }
     /**
      * Paints the background image to the panel
      * It is scaled in relation to the screen size
      */
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D)g;
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         //scales image
         int w = getWidth();
         int h = getHeight();
         int iw = background.getWidth();
         int ih = background.getHeight();
         double xScale = (double)w/iw;
         double yScale = (double)h/ih;
         double scale = //Math.min(xScale, yScale);    // scale to fit
             Math.max(xScale, yScale);  // scale to fill
         int  width = (int)(scale*iw);
         int  height = (int)(scale*ih);
         x = (w - width)/2;
         y = (h - height)/2;
         //paints image
         g2.drawImage(background, x, y, width, height, this);
         g2.drawImage(background, x, y, width, height, this);
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == playButton) {
             Game.init();
             this.setVisible(false);
             menuVisible = false;
             Game.start();
         }
         if(e.getSource() == joinGameButton) {
             // TODO a better way to populate the server list and an option to enter a custom address
             String[] servers = {"localhost", "openspace.overthere.org"};
             String server = (String)JOptionPane.showInputDialog(this,
                     "Select a server to join",
                     "Join a Network Game",
                     JOptionPane.PLAIN_MESSAGE,
                     null, // Icon
                     servers,
                     null // initial value
             );
 
             if (server == null) // user hit cancel
                 return;
 
             Game.joinServer(server);
             this.setVisible(false);
             menuVisible = false;
             Game.start(); 
         }
         if(e.getSource() == settingsButton) {
             JOptionPane.showMessageDialog(this, "Doesn't do anything yet");
         }
         if(e.getSource() == quitButton) {
             System.exit(0);
         }
 
     }
     public static void main (String []args) throws IOException {
        @SuppressWarnings("unused")
         MainMenu menu = new MainMenu();
         //Game.init();
         //Game.start(); 
     }
 }
