 package machine.turing;
 
 import java.io.Serializable;
 
 public class Textbox implements Serializable {
 	private static final long serialVersionUID = 6069216510208599325L;
 	
 	protected String text;
 	protected int x;
 	protected int y;
 	protected int width;
 	protected int height;
 	
 	public Textbox() {}
 	
 	public Textbox(String text) {
 		this.text = text;
 	}
 	
 	public Textbox(String text, int x, int y, int width, int height) {
 		this.text = text;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
 	}
 	
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	public String getText() {
 		return this.text;
 	}
 	
 	public int getX() {
 		return this.x;
 	}
 	
 	public int getY() {
 		return this.y;
 	}
 	
 	public int getWidth() {
 		return this.width;
 	}
 	
 	public int getHeight() {
 		return this.height;
 	}
 	
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public void setWidth(int width) {
 		this.width = width;
 	}
 	
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 	public String toString() {
 		return this.text;
 	}
 }
