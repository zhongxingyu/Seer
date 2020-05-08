 package TKM;
 
 /* TKM */
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class TKMGui extends JPanel {
 
     private TrackLayout lyt;
     private TrackMapPanel pMap;
 
    public void loadGui() {

        lyt = new TrackLayout();
 
        lyt.parseTrackDB("track_db.csv");
 
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
         pMap = new TrackMapPanel(lyt);
 
         setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
         //Create and set up the window.
         JFrame frame = new JFrame("TKM Proto");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         TKMControlPanel pControl = new TKMControlPanel(lyt);
 
         pControl.setMapPanel(pMap);
  
         add(pControl);
         add(pMap);
 
         //Create and set up the content pane.
         JComponent newContentPane = this;
         newContentPane.setOpaque(true); //content panes must be opaque
         frame.setContentPane(newContentPane);
 
         //Display the window.
         frame.pack();
         frame.setSize(1000,1000);
         frame.setVisible(true);
     }
     
     public static void main(String[] args)
     {
         /* Create GUI */
        TKMGui gui = new TKMGui();
        gui.loadGui();
        //lyt.chooChoo();
     }
 }
