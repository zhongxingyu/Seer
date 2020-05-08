 package me.kevinwells.darxen.shp;
 
 import java.io.InputStream;
 
 public class Shapefile {
 	
 	private int mInputId;
 	private long hShp;
 	
 	private InputStream fShp;
 	private InputStream fShx;
 	
 	private int entities;
 	private int shapeType;
 
 	public Shapefile(InputStream fShp, InputStream fShx) {
 		this.fShp = fShp;
 		this.fShx = fShx;
 		init(fShp, fShx);
 	}
 	
 	private native void init(InputStream fShp, InputStream fShx);
	public native void close();
 	public native ShapefileObject get(int i);
 	
 	public int getShapeCount() {
 		return entities;
 	}
 
 	public int getType() {
 		return shapeType;
 	}
 	
 	static {
 		System.loadLibrary("shp");
 	}
 }
