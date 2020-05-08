 package risk.view;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import risk.common.ImagePanel;
//import net.miginfocom.swing.MigLayout;
 import javax.swing.JLayeredPane;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import javax.swing.JButton;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Color;
 
 public class MapPanel extends JPanel {
     ImageIcon image=new ImageIcon(getClass().getResource("/risk/view/Risk_small_names.jpg"));
     JLayeredPane map= new JLayeredPane();
     /**
      * Create the panel.
      */
     public MapPanel() {
         
         setLayout(new BorderLayout());
         add(map, BorderLayout.CENTER);      
         
         ImagePanel backGround= new ImagePanel(image.getImage());
         backGround.setBounds(0, 0, 1152, 648);
         map.add(backGround);
         
         JPanel panel = new JPanel();
         panel.setOpaque(false);
         map.setLayer(panel, 1);
         panel.setBounds(0, 0, 1152, 648);
         map.add(panel);
         panel.setLayout(null);
         
         JButton alaska = new JButton("23");
         alaska.setForeground(Color.WHITE);
         alaska.setBackground(Color.RED);
         alaska.setBounds(86, 95, 50, 20);
         panel.add(alaska);
         
         JButton nw_ter = new JButton("");
         nw_ter.setBounds(160, 82, 50, 20);
         panel.add(nw_ter);
         
         JButton greenLand = new JButton("");
         greenLand.setBounds(416, 48, 50, 20);
         panel.add(greenLand);
         
         JButton alberta = new JButton("");
         alberta.setBounds(185, 147, 50, 20);
         panel.add(alberta);
         
     }
 }
