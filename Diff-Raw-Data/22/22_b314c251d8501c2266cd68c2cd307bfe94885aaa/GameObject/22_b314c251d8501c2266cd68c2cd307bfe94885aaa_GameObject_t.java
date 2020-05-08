 package org.racenet.framework;
 
 public class GameObject extends Polygon {
 
 	public static final short FUNC_NONE = 0;
 	public static final short FUNC_LAVA = 1;
 	public short func = FUNC_NONE;
 	
 	public GameObject(Vector2 ... edges) {
 		
 		super(edges);
 	}
	
	public void setFunc(short func) {
		
		this.func = func;
	}
 }
