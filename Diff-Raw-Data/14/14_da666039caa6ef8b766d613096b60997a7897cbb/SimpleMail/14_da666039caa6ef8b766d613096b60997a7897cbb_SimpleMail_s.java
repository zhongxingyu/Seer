 package simplemail;
 
 import javax.swing.JFrame;
 import simplemail.emailtransmit.EmailTransmissionDlg;
 
 /**
  *
  * @author Richard Kelly, Sean Kelly, David Kerr
  */
 public class SimpleMail {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         
         // Test for EmailTransmissionDlg
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(200, 200);
         
        EmailTransmissionDlg a = new EmailTransmissionDlg(frame, "Test");
         frame.setVisible(true);
         a.setVisible(true);
     }
 }
