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
		if (c == null) {
			this.r = 1;
			this.g = 1;
			this.b = 1;
			this.a = 1;
			return;
		}
		this.r = c.getRed();
		this.g = c.getGreen();
		this.b = c.getBlue();
		this.a = c.getAlpha();
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
