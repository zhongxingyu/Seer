 package sim.functions;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import sim.gui.elements.GuiElement;
 import sim.gui.elements.GuiFunction;
 import sim.structures.Array;
 import sim.structures.LinkedList;
 import sim.structures.Tree;
 import sim.structures.Variable;
 
 public class Get implements ActionListener{
 	
 	Variable i;
 	Variable target;
 	Object source;
 	boolean singleChar;
 
 	GuiFunction gui;
 	public GuiElement getGuiElement(){
 		return gui;
 	}
 	public Variable getTarget() {
 		return target;
 	}
 	public void setTarget(Variable l) {
 		this.target = l;
 	}
 	public Object getSource() {
 		return source;
 	}
 	public void setSource(Object l) {
 		this.source= l;
 	}
 	public Variable getIndexVariable() {
 		return i;
 	}
 	public void setIndexVariable(Variable i) {
 		this.i = i;
 	}
 	public boolean getSingleChar(){
 		return singleChar;
 	}
 	public void setSingleChar(boolean singleChar){
 		this.singleChar = singleChar;
 	}
 
 	public Get(Rectangle bounds, boolean singleChar){
 		//TODO add direction here
 		gui = new GuiFunction(bounds,"Get");
 		gui.getButton().addActionListener(this);
 		this.target = null;
 		this.source = null;
 		this.singleChar=singleChar;
 	}
 	/**
 	 * Constructor.
 	 * 
 	 * @param bounds = the dimensions of the graphical element
 	 * @param dir = direction of arrow
 	 * @param input = input var
 	 * @param output = output var
 	 */
 	public Get(Rectangle bounds, Variable l,Variable o, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Get");
 		gui.getButton().addActionListener(this);
 		this.source=l;
 		this.i=i;
 		this.target=o;
 		this.singleChar=singleChar;
 	}
 	public Get(Rectangle bounds, Array l,Variable o, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Get");
 		gui.getButton().addActionListener(this);
 		this.source=l;
 		this.target=o;
 		this.singleChar=singleChar;
 	}
 	public Get(Rectangle bounds, LinkedList l,Variable o, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Get");
 		gui.getButton().addActionListener(this);
 		this.source=l;
 		this.target=o;
 		this.i = i;
 		this.singleChar=singleChar;
 	}
 	public Get(Rectangle bounds, Tree l,Variable o, Variable i, boolean singleChar) {
 		gui = new GuiFunction(bounds,"Get");
 		gui.getButton().addActionListener(this);
 		this.source=l;
 		this.i=i;
 		this.target=o;
 		this.singleChar=singleChar;
 	}
 	/**
 	 * Will remove the first char from the input string and append it to the output string. 
 	 */
 	@Override
	public void actionPerformed(ActionEvent e) {		
 		if(i != null && target != null){
 			if(source instanceof Variable && target instanceof Variable){
 				if(singleChar){
 					String val = ((Variable)target).getValue();
 					String ch = val.substring(0, 1);
 					((Variable)target).setValue(val.substring(1));
 					String tarVal = ((Variable) source).getValue();
 					tarVal += ch;
 					((Variable) source).setValue(tarVal);
 				}
 				else {
 					((Variable) source).setValue(((Variable)target).getValue());
 				}
 			}else if(source instanceof Tree){
 				try{
 						int index = Integer.parseInt(i.getValue());
 						String s = ((Tree)source).getValueAt(index);
 						if(s!=null)
 							target.setValue(s);
 				}catch(NumberFormatException nfe){
 					JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers.");
 				}
 			}else if(source instanceof LinkedList){
 
 				try{
 					int index = Integer.parseInt(i.getValue());
 					String s = ((LinkedList)source).getValueAt(index);
 					if(s!=null)
 						target.setValue(s);
 
 				}catch(NumberFormatException nfe){
 					JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers.");
 				}
 			}else if(source instanceof Array){
 				if(i.getValue().indexOf(",") > 0){
 					String[] index = i.getValue().split(",");
 					try{
 						int indexY = Integer.parseInt(index[0]);
 						int indexX = Integer.parseInt(index[1]);
 
 						if(((Array) source).getDimensions() == 2){
 							target.setValue((String) ((Array) source).getValueAt(indexY,indexX));
 						}else{
 							target.setValue((String) ((Array) source).getValueAt(indexY));
 						}
 					}catch(Exception nfe){
 						JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers separated by a comma (,)");
 					}
 				}else{
 					try{
 						int indexY = Integer.parseInt(i.getValue());
 						target.setValue((String) ((Array) source).getValueAt(indexY));
 					}catch(Exception nfe){
 						JOptionPane.showConfirmDialog(gui, "Illegal character: you can only enter numbers separated by a comma (,)");
 					}
 				}
 			}
 		}
 	}
 }
 
