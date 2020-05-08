 package fr.frozen.iron.client.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.Font;
 
 import fr.frozen.game.FontManager;
 
 
 public abstract class Component implements MouseListener {
 
 	protected static Font font = FontManager.getFont("componentFont");
 
 	protected List<ActionListener> listeners;
 	protected boolean selected = false;
 	protected boolean visible = true;
 	
 	protected Vector2f pos;
 	protected Vector2f dim;
 	
 	protected Component(int x, int y, int w, int h) {
 		listeners = new ArrayList<ActionListener>();
 		pos = new Vector2f(x, y);
 		dim = new Vector2f(w ,h);
 	}
 	
 	public Vector2f getLocation() {
 		return pos;
 	}
 	
 	public boolean isVisible() {
 		return visible;
 	}
 	
 	public Vector2f getDim() {
 		return dim;
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	public void setSelected(boolean val) {
 		selected = val;
 	}
 	
 	public void setDim(Vector2f dim) {
 		this.dim = dim;
 	}
 	
 	public void setDim(int x, int y) {
 		dim = new Vector2f(x, y);
 	}
 	
 	public int getWidth() {
 		return (int)dim.getX();
 	}
 	
 	public int getHeight() {
 		return (int)dim.getY();
 	}
 	
 	
 	public boolean contains(int x, int y) {
		return x >= pos.getX() && x < pos.getX() + getWidth() && y >= pos.getY() && y < pos.getY() + getHeight();
 	}
 	
 	public void setVisible(boolean val) {
 		visible = val;
 	}
 	
 	public void setLocation(int x, int y) {
 		pos.setX(x);
 		pos.setY(y);
 	}
 	
 	public synchronized void addActionListener(ActionListener al) {
 		listeners.add(al);
 	}
 	
 	public synchronized void removeActionListener(ActionListener al) {
 		listeners.remove(al);
 	}
 	
 	protected synchronized void notifyActionListeners() {
 		ActionEvent event = new ActionEvent(this);
 		for (ActionListener al : listeners) {
 			al.actionPerformed(event);
 		}
 	}
 	
 	public abstract boolean update(float deltaTime);
 	public abstract void render(float deltaTime);
 }
