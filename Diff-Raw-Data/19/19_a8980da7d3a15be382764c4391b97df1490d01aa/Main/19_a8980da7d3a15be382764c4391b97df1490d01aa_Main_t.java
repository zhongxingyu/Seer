 package home;
 
import javax.swing.JOptionPane;

 import view.HumanView;
 
 public class Main
 {
     public static void main(String... args) throws Exception
     {
        String name = JOptionPane.showInputDialog("Enter your name:");
        new HumanView(name).start();
     }
 }
