 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package templateTests;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
import views.LeftBottomView;
import views.LeftTopView;
import views.RightBottomView;
import views.RightTopView;
 
 /**
  *
  * @author David
  */
 public class NewMain extends JFrame{
 
     private JPanel jPanelLeft;
     private JPanel jPanelRight;
     
     public LeftTopView lefttopview;
     public LeftBottomView leftbottomview;
     public RightTopView righttopview;
     public RightBottomView rightbottomview;
     
     public NewMain(){
         initViews();
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new NewMain().setVisible(true);
             }
         });
         
     }
     
     public void initViews(){
         
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(400, 400));
         setPreferredSize(new java.awt.Dimension(900, 600));
         
         //creation of left panel and its views
         jPanelLeft = new JPanel();
         
         jPanelLeft.setMinimumSize(new java.awt.Dimension(100, 100));
         jPanelLeft.setPreferredSize(new java.awt.Dimension(100, 577));
         jPanelLeft.setLayout(new java.awt.GridLayout(0, 1));
         
         lefttopview = new LeftTopView(jPanelLeft);
         leftbottomview = new LeftBottomView(jPanelLeft);
         
         getContentPane().add(jPanelLeft, java.awt.BorderLayout.WEST);
         
         
         //creation of left panel and its views
         jPanelRight = new JPanel();
         jPanelRight.setLayout(new java.awt.GridLayout(0, 1));
         
         righttopview = new RightTopView(jPanelRight);
         rightbottomview = new RightBottomView(jPanelRight);
         
         getContentPane().add(jPanelRight, java.awt.BorderLayout.CENTER);
         pack();
     }
 }
