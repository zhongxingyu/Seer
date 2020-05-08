 package gui;
 
 public interface TransitionElement {
 	public void setDefaultLocation(int x, int y);
 	public void setLocation(int x, int y);
 	public void setX(int x);
 	public void setY(int y);
 	public int getX();
 	public int getY();
 	public int getDefaultX();
 	public int getDefaultY();
	public int getWidth();
 	public void enable();
 	public void disable();
 }
