 package sim.functions;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiFunction;
 import sim.structures.Array;
 import sim.structures.LinkedList;
 import sim.structures.Variable;
 
 /**
  * Add - Instances of this class is used with structures such as linked-list or array to add an element to the end. (LIFO)
  * @author 
  */
 public class InsertBefore implements ActionListener {
 // Class variables //
 	Object l;
 	Variable v;
 	Variable i;
 	GuiFunction gui;
 	
 // Getters and setters //
 	public GuiElement getGuiElement(){
 		return gui;
 	}
 	
 // Class constructor //
 	/**
 	 * The class constructor. Initializes the graphical element - {@link GuiFunction}
 	 * @param bounds 	- The size and placement of the graphical element
 	 * @param l 		- The structure on which to perform the add()-action
 	 * @param v 		- The output variable
 	 * @param i			- The input variable (The index of array element that is to be "added before")
 	 */
 	public InsertBefore(Rectangle bounds, Object l, Variable v, Variable i) {
 		gui = new GuiFunction(bounds,"Insert Before");
 		gui.getButton().addActionListener(this);
 		this.l = l;
 		this.v = v;
 		this.i = i;
 	}
 	
 // Action Listener implementation //
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(l instanceof Array){
 			((Array) l).insertBefore(v.getValue(),Integer.parseInt(i.getValue()));
 		}else if(l instanceof LinkedList){
			((LinkedList) l).addLast(v.getValue());
 		}
 		//l.addFirst(v.getValue());
 		
 
 	}
 }
