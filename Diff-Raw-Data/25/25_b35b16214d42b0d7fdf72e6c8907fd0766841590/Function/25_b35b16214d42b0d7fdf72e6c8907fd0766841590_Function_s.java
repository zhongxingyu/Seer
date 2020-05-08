 package sim.functions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 
 import sim.gui.elements.GuiElement;
 
 
 public abstract class Function extends GuiElement implements ActionListener{
 
 	final int WIDTH = 50;
 	final int HEIGHT = 50;
 	
 	JButton b;
 	public Function(String s){
 		super();
 		b = new JButton(s);
 		b.addActionListener(this);
 		setSize(WIDTH, HEIGHT);
 	}
 	public void actionPerformed(ActionEvent e){
 		
 	}
 }
