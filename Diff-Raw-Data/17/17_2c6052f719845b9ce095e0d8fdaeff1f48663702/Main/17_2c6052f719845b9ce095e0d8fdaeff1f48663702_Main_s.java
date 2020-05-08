 import javax.swing.JFrame;
 
 public class Main
 {
 	public static boolean connectionGUIStatus = false;
 
 	public static void main(String[] args)
 	{
 		ConnectionGUI cGUI = new ConnectionGUI();
 		
 		cGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cGUI.setSize(300, 180);
 		cGUI.setResizable(false);
 		cGUI.setVisible(true);
 		
 		while(!connectionGUIStatus)
 		{
 			try 
 			{
 				Thread.sleep((long)0.001);
 			} catch (InterruptedException e1) { e1.printStackTrace(); }
 		}
 		
 		GameGUI gGo = new GameGUI();
 
 		gGo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gGo.setSize(800, 600);
 		gGo.setResizable(false);
 		gGo.setVisible(true);
 	}
 }
