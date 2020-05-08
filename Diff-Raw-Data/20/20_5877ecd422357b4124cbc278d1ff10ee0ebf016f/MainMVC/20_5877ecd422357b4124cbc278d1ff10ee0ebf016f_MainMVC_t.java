 package hg.hist.MVC;
 
 import javax.swing.JFrame;
 
 public class MainMVC {
 	public static void main(String[] args)  {
 		// TODO Auto-generated method stub		
 		
 		Controller controller = new Controller();
 		
 		View frame = new View(controller);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(1200, 700);
 		frame.setVisible(true);
 			
 		}
 
 		
 }
