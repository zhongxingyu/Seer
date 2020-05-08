 import javax.swing.JFrame;
 import javax.swing.JOptionPane; 
 
 public class ClientTest {
    public static void main(String[] args) {
      String ipaddresa;
       ipaddresa = JOptionPane.showInputDialog ( "Enter IP:" ); 
       Client objec;
      objec = new Client(ipaddresa);
       objec.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       objec.startRunning();
    }
 }
