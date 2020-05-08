 package memeograph.ui;
 
 import java.awt.BorderLayout;
 import javax.swing.JFrame;
 import memeograph.Tree;
 
 public class MemeoFrame extends JFrame{
 		
     public MemeoFrame(Tree graph){
         super("Memeographer!");
 
         setLayout(new BorderLayout());
         MemeoPApplet papplet = new MemeoPApplet(graph);
         add(papplet, BorderLayout.CENTER);
 
         setSize(800, 600);
         setLocationRelativeTo(null);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        papplet.init();
     }
 
 }
