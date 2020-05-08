 package phm1.NewJ;
 
 import java.awt.*;
 
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 public class GUI {
 	// This really is a bit useless now but getting rid of it would break things, so I'll take the hacky approach now and clean up later :P - J
 	Model model;
 	DiagramPanel dPanel;
 	ButtonPanel bPanel;
 	MyMouseListener mouseListener;
 	Menus menus;
 	MyMenuListener menuListener;
 	MainFrame mainFrame;
 	
 	public Model getModel() {
 		return model;
 	}
 
 	public DiagramPanel getdPanel() {
 		return dPanel;
 	}
 
 	public ButtonPanel getbPanel() {
 		return bPanel;
 	}
 
 	public MyMouseListener getMouseListener() {
 		return mouseListener;
 	}
 
 	public Menus getMenus() {
 		return menus;
 	}
 
 	public MyMenuListener getMenuListener() {
 		return menuListener;
 	}
 	
 	public MainFrame getMainFrame(){
 		return mainFrame;
 	}
 
 	GUI(Model m) {
 		this.model = m;
 	}
 	
 	public void addClass(NJClass c) {
 		model.addClass(c);
 		repaint();
 	}
 	
 	public boolean classNameInUse(String name){
 		return model.getClassByName(name) != null;
 	}
 	
 	public void initialise(){
 		//initalise everything - p
 		model = new Model();
 		dPanel = new DiagramPanel(this);
 		bPanel = new ButtonPanel(this);
 		menus = new Menus(dPanel, model);
 		mouseListener= new MyMouseListener(dPanel, bPanel);
		mainFrame = new MainFrame(model, dPanel, bPanel, menus, mouseListener);
 		mainFrame.setVisible(true);
 	}
 	
 	public void drawAll(Graphics g) {
 		for (NJClass c : model.getClasses()) {
 			c.draw(g);
 			c.drawConnections(g);
 		}
 	}
 	
 	public void deleteAll() {
 		model.clear();
 		repaint();
 	}
 	
 	public void deleteSelected() {
 		for(NJClass c : model.getClasses()){
 			if(c.getSelected()){
 				model.removeClass(c);
 				return;
 			}
 		}
 		repaint();
 	}
 	
 	public void repaint(){
 		dPanel.repaint();
 	}
 	
 	public NJClass getSelected() {
 		for(NJClass c : model.getClasses()) {
 			if(c.getSelected()) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public void unselectAll() {
 		for (NJClass c : model.getClasses()) {
 			c.setSelected(false);
 		}
 	}
 	
 	public NJClass clickedInBox(int x, int y) {
 		//finds the box that has been clicked on, if any - p
 		int xPlusA;
 		int yPlusB;
 		for (NJClass c : model.getClasses()){ //loop through boxes - p
 
 			xPlusA = c.getX()+c.getA();
 			yPlusB = c.getY()+c.getB();
 			
 			if ((c.getX() < x) && (x < xPlusA) && (c.getY() < y) && (y < yPlusB)) {
 				// We found it! Let's get outta here - J
 				return c;
 			}
 		}
 		// Just return null if we didn't return earlier - J
 		return null;
 	}
 	
 	public void newMethod(NJClass c, String s) {
 		
 	}
 	
 	public String classNamePrompt(){
 		String className;
 
 		className = JOptionPane.showInputDialog("Enter class name");
 		//pops up a dialog box to get the name for the new class
 		if(classNameInUse(className)){
 			JOptionPane.showMessageDialog(getMainFrame(), "That class name is already in use.", "Error", JOptionPane.ERROR_MESSAGE);
 			className = classNamePrompt();
 		}
 		if (className == null || className.length() == 0) { 
 			className = "Untitled" + Integer.toString(getModel().getClassCount() + 1);
 		}
 		return className;
 	}
 }
