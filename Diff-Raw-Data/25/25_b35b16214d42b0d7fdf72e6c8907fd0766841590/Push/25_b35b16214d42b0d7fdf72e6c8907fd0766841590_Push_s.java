 package sim.functions;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JButton;
 
 import sim.gui.elements.Variable;
 import sim.structures.Stack;
 
 public class Push extends Function {
 	Push(Stack s, Variable v){
 		super("Push");
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("WINWINWIN!");
 	}
 }
