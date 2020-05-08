 package sim.gui.elements;
 
 import java.awt.Rectangle;
 
 import javax.swing.JTextField;
 
 @SuppressWarnings("serial")
 public class GuiVariable extends GuiElement{
 	private JTextField t;
 
 	public GuiVariable(Rectangle bounds,String value, boolean editable){
 		super();
		t = new JTextField(value, (int) (bounds.width/11.5));
 		t.setEditable(editable);
 		t.setBounds(0,0,bounds.width,bounds.height);
 		setBounds(bounds);
 		add(t);
 		validate();
 	}
 	public void setValue(String value){
 		t.setText(value);
 	}
 	public String getValue(){
 		return t.getText();
 	}
 }
