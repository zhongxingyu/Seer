 package pat.game.MS;
 
 import javax.swing.JToggleButton;
 
 public class Field {
 
 	private boolean isBomb;
 	private int bombValue;
 	private JToggleButton bu;
 	private int x, y;
 	private boolean flaged;
 	
 	public boolean isFlaged() {
 		return flaged;
 	}
 
 	public void setFlaged(boolean flaged) {
 		this.flaged = flaged;
 	}
 
 	public Field(JToggleButton bu, int x, int y) {
 		super();
 		this.bu = bu;
 		this.x = x;
 		this.y = y;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	private boolean opened;
 
 	public boolean isOpened() {
 		return opened;
 	}
 
 	public void setOpened(boolean opened) {
 		this.opened = opened;
 	}
 
 	public JToggleButton getBu() {
 		return bu;
 	}
 
 
 	public void setBomb(boolean isBomb) {
 		this.isBomb = isBomb;
 	}
 
 	public int getBombValue() {
 		return bombValue;
 	}
 
 	public void setBombValue(int bombValue) {
 		this.bombValue = bombValue;
 	}
 
 	public boolean isBomb() {
 		return isBomb;
 	}
 
 }
