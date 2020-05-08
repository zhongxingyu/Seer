 import java.awt.Rectangle;
 
 
 public class ClipListItem {
 	private Rectangle clipRect = null;
 	private String clipName = null;
 	
 	public ClipListItem(Rectangle clipRect, String clipName) {
 		this.clipRect = clipRect;
 		this.clipName = clipName;
 	}
 	
 	public Rectangle getClipRect() {
 		return clipRect;
 	}
 	
	public void setClipRect(Rectangle clipRect) {
		this.clipRect = (Rectangle)clipRect.clone();
	}
	
 	@Override
 	public String toString() {
 		return clipName;
 	}

 }
