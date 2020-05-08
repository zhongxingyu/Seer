 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public class Banner extends JPanel {
     public Banner(File logoDir) {
         super();
 
         setBackground(null);
         setLayout(new BorderLayout());
 
         // read logo dir
         File logos[] = logoDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().endsWith(".png");
             }
         });
 
        if (logos.length == 0) {
             JLabel l = new JLabel("Diamond!");
             l.setForeground(Color.WHITE);
             add(l);
         } else {
             Box b = Box.createHorizontalBox();
             
             // sort logos
             Arrays.sort(logos);
             for (File file : logos) {
                 ImageIcon icon = null;
                 try {
                     BufferedImage img = ImageIO.read(file);
                     if (img != null) {
                         double scale = Util.getScaleForResize(img.getWidth(),
                                 img.getHeight(), 300, 200);
                         icon = new ImageIcon(Util.scaleImage(img, scale));
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 b.add(new JLabel(icon));
                 b.add(Box.createHorizontalGlue());
             }
 //            b.add(Box.createHorizontalGlue());
             add(b);
         }
     }
 }
