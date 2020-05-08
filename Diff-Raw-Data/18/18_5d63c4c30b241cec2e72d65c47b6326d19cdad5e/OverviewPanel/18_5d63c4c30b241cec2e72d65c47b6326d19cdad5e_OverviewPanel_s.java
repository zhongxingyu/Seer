 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 
 import geometry.Rect;
 import graphview.GraphScene;
 import graphview.GraphSceneListener;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import javax.swing.JPanel;
 
 /**
  *
  * @author FlyPig
  */
 public class OverviewPanel extends JPanel{
     GraphScene scene;
     
     public OverviewPanel(GraphScene scene){
         this.scene=scene;
         initComponents();
         
         scene.addListener(new GraphSceneListener() {
             @Override
             public void onSceneRedraw() {
                 updateUI();
             }
         });
     }
     
     private void initComponents() {
         
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
         layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
     }
  
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         scene.drawOverview(g, Rect.fromRectangle2D(getBounds()));
     }
 }
