 /*
  * Test java program to remember how to program in java
<<<<<<< HEAD:Test.java
  *
  * Change in master...
  * Experiment version
>>>>>>> experimental:Test.java
  */
 import javax.swing.JFrame;
 import java.awt.Toolkit;
 import java.awt.Dimension;
 
 public class Test extends JFrame {
 
     public Test() {
         setSize(300,200);
         setTitle("Test Window");
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         Toolkit toolkit = getToolkit();
         Dimension size = toolkit.getScreenSize();
         setLocation(size.width/2 - getWidth()/2, size.height/2 - getHeight()/2);
     }
 
     public static void main(String[] args)
     {
     Test test = new Test();
     test.setVisible(true);
     }
 }
 
