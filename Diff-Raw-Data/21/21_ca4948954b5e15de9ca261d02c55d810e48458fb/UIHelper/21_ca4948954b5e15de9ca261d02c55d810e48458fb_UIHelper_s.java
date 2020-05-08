 package org.isatools.errorreporter.ui.utils;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created by the ISA team
  *
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 18/03/2011
  *         Time: 16:39
  */
 public class UIHelper {
     public static final Font VER_8_PLAIN = new Font("Verdana", Font.PLAIN, 8);
     public static final Font VER_8_BOLD = new Font("Verdana", Font.BOLD, 8);
     public static final Font VER_9_PLAIN = new Font("Verdana", Font.PLAIN, 9);
     public static final Font VER_9_BOLD = new Font("Verdana", Font.BOLD, 9);
     public static final Font VER_10_PLAIN = new Font("Verdana", Font.PLAIN, 10);
     public static final Font VER_10_BOLD = new Font("Verdana", Font.BOLD, 10);
     public static final Font VER_11_PLAIN = new Font("Verdana", Font.PLAIN, 11);
     public static final Font VER_11_BOLD = new Font("Verdana", Font.BOLD, 11);
 
     public static final Color GREY_COLOR = new Color(51, 51, 51);
     public static final Color RED_COLOR = new Color(191, 30, 45);
     public static final Color LIGHT_GREY_COLOR = new Color(153, 153, 153);
     public static final Color LIGHT_GREEN_COLOR = new Color(140, 198, 63);
     public static final Color DARK_GREEN_COLOR = new Color(0, 104, 56);
     public static final Color BG_COLOR = Color.WHITE;
 
     public static JLabel createLabel(String text, Font f, Color c, int position) {
         JLabel newLab = new JLabel(text, position);
 
         newLab.setBackground(BG_COLOR);
         newLab.setFont(f);
         newLab.setForeground(c);
         return newLab;
     }
 }
