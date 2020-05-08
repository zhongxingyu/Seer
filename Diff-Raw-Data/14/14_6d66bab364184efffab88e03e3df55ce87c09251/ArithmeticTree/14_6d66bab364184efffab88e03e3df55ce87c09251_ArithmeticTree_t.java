 package sim.demos;
 
 import java.awt.Rectangle;
 
 import javax.swing.JFrame;
 
 import sim.structures.Tree;
 
 public class ArithmeticTree {
 	public static void main(String[] args) {
 		
 		new ArithmeticTree();
 		
 	}
 	
	@SuppressWarnings("unused")
 	public ArithmeticTree(){
 	JFrame frame = new JFrame();
 	frame.setTitle("StructAlgoSim 0.1 - Arithmetic tree");
 	frame.setSize(800,550);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	frame.setLayout(null);
 	
 	Tree tree = new Tree(new Rectangle(200,200,200,200));
 	String infix = "2+2*2";
 	String postfix = "2 2 2 +*";
 	
 	frame.validate();
 	frame.setVisible(true);
 }
 }
