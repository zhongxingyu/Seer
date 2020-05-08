 package GUI;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class TooterProjectGUI {
     private JFrame mainFrame;
     private CardLayout cl;
     private JPanel mainPanel;
     private JPanel splashPanel;
     private JPanel playPanel;
     private JPanel choosePanel;
     private JPanel pianoPanel;
     private JPanel drumPanel;
     private JPanel guitarPanel;
 
     public TooterProjectGUI(){
         // Initialize main frames & panels
         cl = new CardLayout();
         mainFrame = new JFrame();
         mainPanel = new JPanel();
 
         // Set card layout (enables switching through panels)
         mainPanel.setLayout(cl);
 
         // Set size and color of window frame
         mainFrame.setSize(new Dimension(1000, 800));
         mainFrame.getContentPane().setBackground(Color.WHITE);
 
         // Initialize different page panels
         splashPanel = new SplashScreen(cl, mainPanel);
         playPanel = new PlaySongScreen(cl, mainPanel);
         choosePanel = new ChooseInstrument(cl, mainPanel);
         pianoPanel = new PianoInstrument(cl, mainPanel);
         drumPanel = new DrumInstrument(cl, mainPanel);
         guitarPanel = new GuitarInstrument(cl, mainPanel);
 
 
         Session.sessionListeners.add((SessionListener) pianoPanel);
 
         // Add components to the main Panel
         mainPanel.add(splashPanel, "splash");
         mainPanel.add(playPanel, "play");
         mainPanel.add(choosePanel, "choose");
         mainPanel.add(pianoPanel, "piano");
         mainPanel.add(guitarPanel, "guitar");
        drumPanel.add(drumPanel, "drums");
 
         // Beginning page to show
         cl.show(mainPanel, "splash");
 
         // Add components to the main Frame
         mainFrame.add(mainPanel);
 
         mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         mainFrame.setVisible(true);
     }
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable(){
             @Override
             public void run(){
                 new TooterProjectGUI();
             }
         });
     }
 }
