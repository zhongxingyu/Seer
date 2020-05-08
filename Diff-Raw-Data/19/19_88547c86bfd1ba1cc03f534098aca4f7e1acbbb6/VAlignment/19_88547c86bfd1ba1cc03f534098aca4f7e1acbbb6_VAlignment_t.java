 package com.inspedio.enums;
 
 import javax.microedition.lcdui.Graphics;
 
 public class VAlignment extends InsEnum{
 
 	public static final VAlignment TOP = new VAlignment("TOP", 0, Graphics.TOP);
 	public static final VAlignment MIDDLE = new VAlignment("MIDDLE", 1, Graphics.VCENTER);
 	public static final VAlignment BOTTOM = new VAlignment("BOTTOM", 2, Graphics.BOTTOM);
 
 	public int anchor;
 	
 	protected VAlignment(String Name, int Value, int AnchorPoint) {
 		super(Name, Value);
 		this.anchor = AnchorPoint;
 	}
 }
