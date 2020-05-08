 package com.receiver2d.editor.gui;
 
<<<<<<< HEAD
import com.receiver2d.engine.Vector2D;
 
 /**
  * A GUI element of the window type.
  */
 //TODO: break away from ENGINE DEPENDENCY
public class GUIWindow extends GUI {
	/**
	 * The dimensions of the window.
	 */
	public Vector2D dimensions;
	public GUIWindow(float x, float y, float width, float height) {
		super(x, y);
	}

=======
import java.util.ArrayList;

import com.receiver2d.engine.*;
 
 public class GUIWindow extends GUI {
 	/**
 	 * The children of this window, if it has any.
 	 */
 	public ArrayList<GUIWindow> children;
 	
 	/**
 	 * The parent of this window.
 	 */
 	public GUIWindow parent = null;
 	
 	/**
 	 * The dimensions of the window.
 	 */
 	public Vector2D dimensions;
 	
 	/**
 	 * Creates a new GUIWindow.
 	 * @param x The x-position of the window.
 	 * @param y The y-position of the window.
 	 */
 	public GUIWindow(float x, float y, float d_x, float d_y) {
 		super(x, y);
 		dimensions = new Vector2D(d_x, d_y);
 	}
 	
 	public GUIWindow setChild() {
 		return null; // TODO: fix
 	}
>>>>>>> fb796aefcecea2dee7b3e746de08d2434c70308f
 }
