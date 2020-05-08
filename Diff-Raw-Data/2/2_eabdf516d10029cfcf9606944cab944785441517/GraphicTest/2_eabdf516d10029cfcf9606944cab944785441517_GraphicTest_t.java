 package javachallenge.graphics;
 
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.Scanner;
 
 import javachallenge.common.Direction;
 import javachallenge.graphics.GraphicClient;
 import javachallenge.graphics.components.ProgressBar;
 import javachallenge.graphics.components.ScoreBar;
 import javachallenge.graphics.util.Position;
 import javachallenge.server.Map;
 import javachallenge.server.Game;
 
 import javax.swing.*;
 
 public class GraphicTest {
 	public static void main(String[] args) throws Exception{
 		Map map=Map.load("map/m1.txt");
		GraphicClient client=new GraphicClient(map);
 		//GraphicClient client = new GraphicClient(30, 14, new Position[] {}); 
 		/*JFrame frame=new JFrame();
 		frame.setLayout(null);
 		frame.getContentPane().setPreferredSize(new Dimension(400, 400));
 		//final ProgressBar bar=new ProgressBar(10,10,100,20, Color.green,Color.red,0);
 		ScoreBar bar=new ScoreBar(10,10,100,50,Color.green,Color.red,"kire khar") ;
 		frame.pack();
 		frame.setVisible(true);
 		frame.add(bar);*/
 		Scanner scanner=new Scanner(System.in);
 		while (true)
 		{
 			String com=scanner.next();
 			if (com.startsWith("s")) // spawn
 			{
 				int id=scanner.nextInt(),x=scanner.nextInt(),y=scanner.nextInt();
 				client.spawn(id,new Position(x,y));
 			}
 			else if (com.startsWith("k")) // kill
 			{	
 				int id=scanner.nextInt();
 				client.die(id);
 			}
 			else if (com.startsWith("m")) // move
 			{
 				int id=scanner.nextInt(),pos=scanner.nextInt();
 				client.move(id,Direction.values()[pos]);
 			}
 			else if (com.startsWith("f")) { // obtain flag
 				int id = scanner.nextInt();
 				client.obtainFlag (id);
 			}
 			else if (com.startsWith("h")) { // help
 				System.out.println("h[elp]");
 				System.out.println("e[xit]");
 				System.out.println("s[pawn] id x y");
 				System.out.println("k[ill] id");
 				System.out.println("m[ove] id dir");
 				System.out.println("f[lag] id");
 				System.out.println(client.units);
 				System.out.println(client.flags);
 			}
 			else if (com.startsWith("l")) {
 				String s = scanner.next();
 				client.log(s);
 			}
 			else if (com.startsWith("e")) // exit
 				break;
 		}
 		scanner.close();
 	}
 }
