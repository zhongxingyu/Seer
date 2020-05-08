 package sim;
 
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 
 import sim.functions.Push;
 import sim.gui.elements.GuiStack;
 import sim.gui.elements.Variable;
 
 public class StructAlgoSim {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		frame.setTitle("StructAlgoSim 0.1");
 		frame.setSize(700,600);
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new FlowLayout());
 		GuiStack s = new GuiStack();
 		frame.add(s);
 		frame.add(new Push(s,new Variable("",true));
 		frame.validate();
 	}
 
 }
