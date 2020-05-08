 package sim.structures;
 
 import java.awt.Point;
 import java.util.Vector;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiStack;
 
 public class Stack {
 	
 	private Vector<Object> s = new Vector<Object>();
 	
 	GuiStack gui; 
 	public GuiElement getGuiElement(){
 		return gui;
 	}
 	public Stack(int x, int y, int w, int h){
 		gui = new GuiStack(x,y,w,h,toArray());
 	}
 	public void push(Object obj){
 		s.add(obj);
 		gui.setData(toArray());
 	}
 	public Object pop(){
 		if(isEmpty()) return null;
 		Object obj = s.get(s.size()-1);
 		s.remove(obj);
 		gui.setData(toArray());
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
