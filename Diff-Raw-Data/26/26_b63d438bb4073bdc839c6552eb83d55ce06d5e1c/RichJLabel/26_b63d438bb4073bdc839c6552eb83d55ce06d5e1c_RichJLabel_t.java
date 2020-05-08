 /*
  * RichJLabel.java
  *
  * Created on 7. August 2007, 09:29
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package de.cismet.tools.gui;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.font.*;
 
 public class RichJLabel extends JLabel {
 
     private int tracking;
     public RichJLabel(String text, int tracking) { 
         super(text);
         this.tracking = tracking;
     }
     
     private int left_x, left_y, right_x, right_y;
     private Color left_color, right_color;
     public void setLeftShadow(int x, int y, Color color) {
         left_x = x;
         left_y = y;
         left_color = color;
     }
     
     public void setRightShadow(int x, int y, Color color) {
         right_x = x;
         right_y = y;
         right_color = color;
     }
     
     public Dimension getPreferredSize() {
         String text = getText();
         FontMetrics fm = this.getFontMetrics(getFont());
         
         int w = fm.stringWidth(text);
         w += (text.length()-1)*tracking;
         w += left_x + right_x;
         
         int h = fm.getHeight();
         h += left_y + right_y;
 
         return new Dimension(w,h);
     }
     
     public void paintComponent(Graphics g) {
         ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         
         char[] chars = getText().toCharArray();
 
         FontMetrics fm = this.getFontMetrics(getFont());
         int h = fm.getAscent();
         LineMetrics lm = fm.getLineMetrics(getText(),g);
         g.setFont(getFont());
         
         int x = 0;
         
         for(int i=0; i<chars.length; i++) {
             char ch = chars[i];
             int w = fm.charWidth(ch) + tracking;
 
             g.setColor(left_color);
            g.drawString(""+chars[i],x-left_x,h-left_y);  //NOI18N
             
             g.setColor(right_color);
            g.drawString(""+chars[i],x+right_x,h+right_y);  //NOI18N
 
             g.setColor(getForeground());
            g.drawString(""+chars[i],x,h);  //NOI18N
 
             x+=w;
         }
         
     }
     
     public static void main(String[] args) {
        RichJLabel label = new RichJLabel("76", 0);  //NOI18N
         
         
         /*
         // drop shadow w/ highlight
         label.setLeftShadow(1,1,Color.white);
         label.setRightShadow(2,3,Color.black);
         label.setForeground(Color.gray);
         label.setFont(label.getFont().deriveFont(140f));
         */
         
         
         // subtle outline
         label.setLeftShadow(1,1,Color.white);
         label.setRightShadow(1,1,Color.white);
         label.setForeground(Color.BLACK);
         label.setFont(label.getFont().deriveFont(26f));
         
         
         /*
         // 3d letters
         label.setLeftShadow(5,5,Color.white);
         label.setRightShadow(-3,-3, new Color(0xccccff));
         label.setForeground(new Color(0x8888ff));
         label.setFont(label.getFont().deriveFont(140f));
         */
 
        JFrame frame = new JFrame("RichJLabel hack");  //NOI18N
         frame.getContentPane().add(label);
         frame.pack();
         frame.setVisible(true);
     }
     
     public static void p(String str) {
         System.out.println(str);
     }
 }
