 package sim.structures;
 
 import java.util.Vector;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiStack;
 
 public class Stack {
 	
 	private Vector<Object> s = new Vector<Object>();
 	
 	GuiStack gui; 
 	public GuiElement getGuiElement(){
 		return gui;
 	}
 	
 	public Stack(){
<<<<<<< HEAD
 		gui = new GuiStack(s.toArray());
=======
		gui = new GuiStack(this.toArray());
		push("mordi");
		push("mordi");
>>>>>>> a92478ca3ed4e279ba478b18da192942c052e3f3
 	}
 	public void push(Object obj){
 		s.add(obj);
		gui.setData(this.toArray());
 	}
 	
 	public Object pop(){
 		if(isEmpty()) return null;
 		Object obj = s.get(s.size()-1);
 		s.remove(obj);
		gui.setData(this.toArray());
 		return obj;
 	}
 	
 	public boolean isEmpty(){
 		if(s.size() == 0)
 			return true;
 		
 		return false;
 	}
 	
 	public Object[] toArray(){
 		Object[] obj = new Object[s.size()];
 		for(int i =0; i<s.size();i++){
 			obj[s.size()-i-1] = s.get(i);
 		}
 		return obj;
 	}
 }
