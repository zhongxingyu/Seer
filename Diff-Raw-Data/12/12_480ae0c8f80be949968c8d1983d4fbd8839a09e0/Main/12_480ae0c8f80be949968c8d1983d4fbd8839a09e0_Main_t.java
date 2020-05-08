 import javax.swing.JFrame;
 
//Nu se face mare branza aici. Se initializeaza fereastra dupa care se adauga in continut
//un DrawPanel (care mosteneste JPanel).

 public class Main
 { 
     public static void main(String[] args)
     {
         JFrame frame = new JFrame("Curbe Bezier");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(new DrawPanel()); //Vezi DrawPanel.java pentru detalii
         frame.pack();
         frame.setSize(350, 350);
         frame.setVisible(true);
     }
 }
