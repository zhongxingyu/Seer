 package us.percept.pile.view;
 
 import com.intellij.uiDesigner.core.GridConstraints;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 /**
  * Author: spartango
  * Date: 8/7/13
  * Time: 3:32 PM.
  */
 public class ModeView extends JPanel {
     private static final Logger logger = LoggerFactory.getLogger(ModeView.class);
 
     static {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         try {
             InputStream streamL = ModeView.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
             Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT,
                                                  streamL);
             streamL.close();
            InputStream streamR = ModeView.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
             Font robotoLight = Font.createFont(Font.TRUETYPE_FONT,
                                                streamR);
             streamR.close();
 
             ge.registerFont(robotoLight);
             ge.registerFont(robotoRegular);
             logger.info("Fonts added: " + robotoLight.getName() + " & " + robotoRegular.getName());
         } catch (FontFormatException | IOException e) {
             logger.warn("Failed to load Roboto font");
         }
     }
 
     private JLabel exploreLabel;
     private JLabel archiveLabel;
     private JLabel queueLabel;
 
     private java.util.List<ModeViewListener> listeners;
 
     public ModeView() {
         this.listeners = new ArrayList<ModeViewListener>();
         setup();
     }
 
     public void addListener(ModeViewListener l) {
         listeners.add(l);
     }
 
     public void removeListener(ModeViewListener l) {
         listeners.remove(l);
     }
 
     private void notifyQueueMode() {
         for (ModeViewListener listener : listeners) {
             listener.onQueueMode();
         }
     }
 
     private void notifyArchiveMode() {
         for (ModeViewListener listener : listeners) {
             listener.onArchiveMode();
         }
     }
 
     private void notifyExploreMode() {
         for (ModeViewListener listener : listeners) {
             listener.onExploreMode();
         }
     }
 
     public void setup() {
         this.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
 
         // Bottom Buttons (for mode)
         Font labelFont = new Font("Roboto Regular", Font.PLAIN, 17);
         exploreLabel = new JLabel();
         exploreLabel.setFont(labelFont);
         exploreLabel.setText("Explore");
         exploreLabel.setForeground(new Color(56, 117, 206));
         exploreLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifyExploreMode();
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 exploreLabel.setForeground(new Color(247, 178, 61));
 
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 exploreLabel.setForeground(new Color(56, 117, 206));
             }
         });
 
         this.add(exploreLabel,
                  new GridConstraints(0,
                                      0,
                                      1,
                                      1,
                                      GridConstraints.ANCHOR_CENTER,
                                      GridConstraints.FILL_HORIZONTAL,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      null,
                                      null,
                                      null,
                                      0,
                                      false));
         archiveLabel = new JLabel();
         archiveLabel.setFont(labelFont);
         archiveLabel.setHorizontalAlignment(11);
         archiveLabel.setText("Archives");
         archiveLabel.setForeground(new Color(75, 168, 79));
         archiveLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifyArchiveMode();
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 archiveLabel.setForeground(new Color(247, 178, 61));
 
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 archiveLabel.setForeground(new Color(75, 168, 79));
             }
         });
 
         this.add(archiveLabel,
                  new GridConstraints(0,
                                      2,
                                      1,
                                      1,
                                      GridConstraints.ANCHOR_CENTER,
                                      GridConstraints.FILL_HORIZONTAL,
                                      1,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      null,
                                      null,
                                      null,
                                      0,
                                      false));
 
         queueLabel = new JLabel();
         queueLabel.setFont(labelFont);
         queueLabel.setHorizontalAlignment(0);
         queueLabel.setText("Queue");
         queueLabel.setForeground(new Color(225, 73, 63));
         queueLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifyQueueMode();
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 queueLabel.setForeground(new Color(247, 178, 61));
 
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 queueLabel.setForeground(new Color(225, 73, 63));
             }
         });
         this.add(queueLabel,
                  new GridConstraints(0,
                                      1,
                                      1,
                                      1,
                                      GridConstraints.ANCHOR_CENTER,
                                      GridConstraints.FILL_HORIZONTAL,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      null,
                                      null,
                                      null,
                                      0,
                                      false));
     }
 
 
 }
