 /*
  * LayeredUnit.java
  *
  * Created on 2005�~1��8��, �U�� 10:52
  */
 
 package guri.screenmodel;
 
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 
 /**
  * 
  * @author luzi82
  */
 public class UnitLayering implements Unit {
 
 	protected final Unit[] sprites;
 
 	protected final boolean[] visible;
 
 	protected final int size;
 
 	public boolean underLayerDetect = true;
 
 	/** Creates a new instance of LayeredUnit */
 	public UnitLayering(int size) {
 		this.size = size;
 		sprites = new Unit[size];
 		visible = new boolean[size];
 		for (int i = 0; i < size; i++) {
 			visible[i] = true;
 		}
 	}
 
 	public void setUnit(int layer, Unit e) {
 		sprites[layer] = e;
 	}
 
	public void setUnit(Enum layer, Unit e) {
 		setUnit(layer.ordinal(), e);
 	}
 
 	public void setVisible(int layer, boolean b) {
 		visible[layer] = b;
 	}
 
	public void setVisible(Enum layer, boolean b) {
 		setVisible(layer.ordinal(), b);
 	}
 
 	public boolean visible(int layer) {
 		return visible[layer];
 	}
 
	public boolean visible(Enum layer) {
 		return visible(layer.ordinal());
 	}
 
 	public void setUnderLayerDetect(boolean b) {
 		underLayerDetect = b;
 	}
 
 	public boolean underLayerDetect() {
 		return underLayerDetect;
 	}
 
 	public void paint(Graphics2D g) {
 		for (int i = 0; i < size; i++) {
 			if (!visible[i])
 				continue;
 			if (sprites[i] == null)
 				continue;
 			sprites[i].paint(g);
 		}
 	}
 
 	public boolean mouseEvent(MouseEvent me) {
 		boolean ret = false;
 		int i;
 		for (i = 0; i < size; ++i) {
 			if (ret && !underLayerDetect)
 				break;
 			if (!visible[i])
 				continue;
 			if (sprites[i] == null)
 				continue;
 			if (sprites[i].mouseEvent(me))
 				ret = true;
 		}
 		return ret;
 	}
 }
