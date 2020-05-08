 package vooga.rts.networking;
 
 import javax.swing.JFrame;
 
 public class TestMain {
 
     public TestMain () {
     }
 
     public static void main(String[] args) {
         JFrame frame = new JFrame();
         frame.add(new ServerBrowserDisplay());
         frame.pack();
         frame.setVisible(true);
     }
 }
