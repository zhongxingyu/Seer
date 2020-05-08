 package spike;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class mane {
 	public static void main(String[] args) {
 		new Increaser("-", Color.blue).start();
 		new Increaser("-", Color.green).start();
 		new Increaser("-", Color.pink).start();
 		new Increaser("-", Color.red).start();
 		new Increaser("-", Color.yellow).start();
 		Inputer f = new Inputer(":");
 		f.start();
 	}
 }
 
 
 class Inputer extends Thread{ // två sådana här running at the same time ger skumma results imho
 	private Scanner scan;
 
 	public Inputer(String name) {
 		super(name);
 		
 		scan = new Scanner(System.in);
 		
 	}
 
 	public void run(){
		System.out.println("TYPE NOW!");
 		String s = "";
 		
 		while((s = scan.next()) != null){
 			System.out.println(getName() + "    " + s);
 		}
 	}
 	
 	
 	
 }
 
 
 class Increaser extends Thread{
 	private int width = 20;
 	private int height = 20;
 	private Color c;
 	private int incr;
 	
 	public Increaser(String name, Color c) {
 		super(name);
 		this.c = c;
 		this.incr = 1;
 		
 	}
 
 	public void run(){
 		
 		JFrame frame = new JFrame();
 		JPanel p = new JPanel();
 		p.setBackground(c);
 		frame.add(p);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 		frame.setPreferredSize(new Dimension(width,height));
 		frame.pack();
 		
 		while(true){
 			try {
 				sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			frame.setPreferredSize(new Dimension(width,height));
 			width += incr;
 			height += incr;
 			
 			if(width == 20){
 				incr = 1;
 			} else if(width == 500){
 				incr = -1;
 			}
 			
 			frame.pack();
 		}
 		
 	}
 	
 	
 	
 }
