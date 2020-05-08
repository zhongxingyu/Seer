 package lh.koneke.thomas.graphics;
 
 public class Colour implements DrawingObject {
 	float r;
 	float g;
 	float b;
 	float a;
 	
 	public Colour(float r, float g, float b, float a) {
 		this.r = r;
 		this.g = g;
 		this.b = b;
 		this.a = a;
 	}
 	
 	public Colour(Colour c) {
		this.r = c.r;
		this.g = c.g;
		this.b = c.b;
		this.a = c.a;
 	}
 	
 	public float getRed() {
 		return r;
 	}
 	
 	public float getGreen() {
 		return g;
 	}
 	
 	public float getBlue() {
 		return b;
 	}
 	
 	public float getAlpha() {
 		return a;
 	}
 	
 	public void setRed(float f) {
 		this.r = f;
 	}
 	
 	public void setGreen(float f) {
 		this.g = f;
 	}
 	
 	public void setBlue(float f) {
 		this.b = f;
 	}
 	
 	public void setAlpha(float f) {
 		this.a = f;
 	}
 }
