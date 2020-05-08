 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mvc.views;
 
import views.*;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 /**
  *
  * @author David
  */
 public class RightTopView {
     private JScrollPane jPanelRightTop;
     private JPanel jPanelTimeline;
     private JPanel jPanelCanvas;
     private JPanel jPanelInsideScrollPane;
     private JLayeredPane jLayeredPane1;
     private JPanel jPanel5;
     
     private JButton jButtonFloating;
     private JButton jButtonInsideCanvas;
     private JButton jButtonTimeline;
     
     public RightTopView(JPanel jPanelRight){
         jPanelRightTop = new JScrollPane();
         jPanelInsideScrollPane = new JPanel();
         jLayeredPane1 = new JLayeredPane();
         jButtonFloating = new JButton();
         jPanel5 = new JPanel();
         jPanelTimeline = new JPanel();
         jButtonTimeline = new JButton();
         jPanelCanvas = new JPanel();
         jButtonInsideCanvas = new JButton();
         
         jPanelRightTop.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 
         jPanelInsideScrollPane.setMinimumSize(new java.awt.Dimension(760, 210));
         jPanelInsideScrollPane.setPreferredSize(new java.awt.Dimension(760, 210));
 
         jLayeredPane1.setMinimumSize(new java.awt.Dimension(881, 100));
 
         jButtonFloating.setText("Activity number 1 (here you see the application of the layeredPanel)");
         jButtonFloating.setBounds(100, 130, 361, 23);
         jLayeredPane1.add(jButtonFloating, JLayeredPane.DRAG_LAYER);
 
         jPanel5.setLayout(new java.awt.BorderLayout());
 
         jPanelTimeline.setPreferredSize(new java.awt.Dimension(500, 50));
 
         jButtonTimeline.setText("jButton7");
 
         GroupLayout jPanelTimelineLayout = new GroupLayout(jPanelTimeline);
         jPanelTimeline.setLayout(jPanelTimelineLayout);
         jPanelTimelineLayout.setHorizontalGroup(
             jPanelTimelineLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(jPanelTimelineLayout.createSequentialGroup()
                 .addComponent(jButtonTimeline, GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanelTimelineLayout.setVerticalGroup(
             jPanelTimelineLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addComponent(jButtonTimeline, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
         );
 
         jPanel5.add(jPanelTimeline, java.awt.BorderLayout.NORTH);
 
         jPanelCanvas.setPreferredSize(new java.awt.Dimension(430, 160));
 
         jButtonInsideCanvas.setText("jButton8");
 
         GroupLayout jPanelCanvasLayout = new GroupLayout(jPanelCanvas);
         jPanelCanvas.setLayout(jPanelCanvasLayout);
         jPanelCanvasLayout.setHorizontalGroup(
             jPanelCanvasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(jPanelCanvasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonInsideCanvas, GroupLayout.PREFERRED_SIZE, 464, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(286, Short.MAX_VALUE))
         );
         jPanelCanvasLayout.setVerticalGroup(
             jPanelCanvasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(jPanelCanvasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonInsideCanvas, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(43, Short.MAX_VALUE))
         );
 
         jPanel5.add(jPanelCanvas, java.awt.BorderLayout.CENTER);
 
         jPanel5.setBounds(0, 0, 760, 210);
         jLayeredPane1.add(jPanel5, JLayeredPane.DEFAULT_LAYER);
 
         GroupLayout jPanelInsideScrollPaneLayout = new GroupLayout(jPanelInsideScrollPane);
         jPanelInsideScrollPane.setLayout(jPanelInsideScrollPaneLayout);
         jPanelInsideScrollPaneLayout.setHorizontalGroup(
             jPanelInsideScrollPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(jPanelInsideScrollPaneLayout.createSequentialGroup()
                 .addComponent(jLayeredPane1, GroupLayout.PREFERRED_SIZE, 760, GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanelInsideScrollPaneLayout.setVerticalGroup(
             jPanelInsideScrollPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addComponent(jLayeredPane1, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE)
         );
 
         jPanelRightTop.setViewportView(jPanelInsideScrollPane);
 
         jPanelRight.add(jPanelRightTop);
     }
 }
