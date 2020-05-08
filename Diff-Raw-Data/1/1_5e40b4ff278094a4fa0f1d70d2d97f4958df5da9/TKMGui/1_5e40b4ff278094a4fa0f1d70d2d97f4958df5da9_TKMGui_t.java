 package TKM;
 
 /* TKM */
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
import CTCOffice.*;
 
 public class TKMGui extends JPanel {
 
     private TrackLayout lyt;
     private TrackMapPanel pMap;
     private CTCOffice off;
 
     public TKMGui(TrackLayout tl, CTCOffice off)
     {
         lyt = tl;
         office = off;
         loadGui();
     }
     
     public TKMGui(TrackLayout tl)
     {
         lyt = tl;
         loadGui();
     }
 
     public void loadGui() {
 
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
         pMap = new TrackMapPanel(lyt);
 
         setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
         //Create and set up the window.
         JFrame frame = new JFrame("TKM Proto");
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
         TKMControlPanel pControl = new TKMControlPanel(lyt);
 
         pControl.setMapPanel(pMap);
         pMap.setControlPanel(pControl);
  
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
 
         TrackLayout tl = new TrackLayout();
         tl.parseTrackDB("track_db.csv");
         
         TKMGui gui = new TKMGui(tl);
     }
 }
