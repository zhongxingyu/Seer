 package jDistsim.ui.panel.workspace;
 
 import jDistsim.utils.logging.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 30.9.12
  * Time: 18:15
  */
 public class ModelSpacePanel extends JPanel {
 
     public ModelSpacePanel() {
         Logger.log("Initialize model space");
         initialize();
         setSize(1000, 1000);
     }
 
     private void initialize() {
         Logger.log(getWidth() + "x" + getHeight());
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGap(0, 1000, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGap(0, 1000, Short.MAX_VALUE)
         );
 
         //GroupLayout layout = new GroupLayout(this);
         //setLayout(layout);
         //layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING));
         //layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING));
 
         // layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.DEFAULT_SIZE).add(0, this.getPreferredSize().width, Short.MAX_VALUE));
         // layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING).add(0, this.getPreferredSize().height, Short.MAX_VALUE));
 
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(1000, 1000);
     }
 
     @Override
     protected void paintComponent(Graphics graphics2D) {
         super.paintComponent(graphics2D);
         graphics2D.setColor(Color.white);
         graphics2D.fillRect(0, 0, getWidth(), getHeight());
 
        for (int i = 0; i < getHeight(); i = i + 10) {
            if (i % 6 == 0) {
                graphics2D.setColor(new Color(210, 210, 210));
            } else {
                graphics2D.setColor(new Color(241, 241, 241));
            }
             graphics2D.drawLine(0, i, getWidth(), i);
         }
 

        for (int i = 0; i < getWidth(); i = i + 10) {
            if (i % 6 == 0) {
                graphics2D.setColor(new Color(210, 210, 210));
            } else {
                graphics2D.setColor(new Color(241, 241, 241));
            }
             graphics2D.drawLine(i, 0, i, getHeight());
         }
     }
 }
