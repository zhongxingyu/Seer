 package sim.gui.elements;
 
 import java.awt.Point;
 
 import javax.swing.JButton;
 
 
 
 public class GuiFunction extends GuiElement{
 	JButton b;
 	public GuiFunction(int x, int y, int w, int h,String s){
 		super();
 		b = new JButton(s);
 		add(b);
		b.setBounds(x,y,w,h);
		b.setBounds(x,y,w,h);
 	}
 	public JButton getButton(){
 		return b;
 	}
 }
