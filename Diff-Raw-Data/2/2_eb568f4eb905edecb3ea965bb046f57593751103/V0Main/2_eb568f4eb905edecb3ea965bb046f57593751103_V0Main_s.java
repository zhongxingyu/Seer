 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class V0Main implements ActionListener
 {
 	static JFrame jf;
 	
 	public V0Main()
 	{
 		
 	}
 	
 	
 	public static void main(String[] args)
 	{
 		System.out.println("Options:");
 		System.out.println("\t1 -- Launch #1: The kit robot picking kits from the conveyer, putting them on the stand, and the reverse.");
 		System.out.println("\t2 -- Launch #2: The parts robot picking up parts from the nests and putting them in the kit includes vision - use of camera.");
 		System.out.println("\t3 -- Launch #3: Parts dumped into bins and fed down the lanes.");
 		System.out.print("\n\n");
 
 		Scanner s = new Scanner(System.in);
 		int choice = -1;
 
 		while (true)
 		{	
 			System.out.print("Choose a number: ");
 			
 			String inStr = s.next().toLowerCase().trim();
 			
 			try {
 				choice = Integer.parseInt(inStr);
 				if (choice < 1 || choice > 3)
 					throw new Exception();
 				break;
 			} catch (Exception e) {
 			}
 		}
 		
 		jf = new JFrame("CSCI 200 -- Team 11");
		JPanel jp;
 
 		switch (choice)
 		{
 		case 1:
 			jp = new V01();
 			break;
 		case 2:
 			jp = new V02();
 			break;
 		case 3:
 			jp = new V03();
 			break;
 		default:
 			jp = null;
 			break;
 		}
 		
 		jf.add(jp);
 		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		jf.pack();
 		jf.setVisible(true);
 		
 		Timer t = new Timer(50, new V0Main());
 		t.start();
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) 
 	{
 		jf.repaint();
 	}
 }
