 package camibrate;
 
 import java.awt.Color;
 
 public class RGBRange {
 	private int minR, minG, minB, maxR, maxG, maxB;
 	
 	public RGBRange(){
 		minR = 256;
 		maxR = -1;
 		minG = 256;
 		maxG = -1;
 		minB = 256;
 		maxB = -1;
 	}
 	
 	public void print(){
 		System.out.println("("+minR+":"+maxR+","+minG+":"+maxG+","+minB+":"+maxB+")");
 	}
 /*	
 	public boolean testColor(Color c){
 		if(c.getRed() >= minR && c.getRed() <= maxR){
 			if(c.getGreen() >= minG && c.getGreen() <= maxG){
 				if(c.getBlue() >= minB && c.getBlue() <= maxB){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 */	
 	//4 function calls instead of the previous 6 >.< I'm such a NEEERRRRDDDDD!!!!!
 	public boolean testColor(Color c){
		return testColor(c.getRed(),c.getGreen(),c.getBlue());
 	}
 	
 	public boolean testColor(int r, int g, int b){
 		if(r >= minR && r <= maxR){
 			if(g >= minG && g <= maxG){
 				if(b >= minB && b <= maxB){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void insertColor(Color c){
 		if(c.getRed() < minR){
 			minR = c.getRed();
		}
		if(c.getRed() > maxR){
 			maxR = c.getRed();
 		}
 		if(c.getGreen() < minG){
 			minG = c.getGreen();
		}
		if(c.getGreen() > maxG){
 			maxG = c.getGreen();
 		}
 		if(c.getBlue() < minB){
 			minB = c.getBlue();
		}
		if(c.getBlue() > maxB){
 			maxB = c.getBlue();
 		}
 	}
 
 	public Color getMinColor(){
 		return new Color(minR,minG,minB);
 	}
 	
 	public Color getMaxColor(){
 		return new Color(minR,minG,minB);
 	}
 	
 	public Color getAveredgeColor(){
 		return new Color((minR+maxR)/2,(minG+maxG)/2,(minB+maxB)/2);
 	}
 	
 	public int getMinR() {
 		return minR;
 	}
 
 	public void setMinR(int minR) {
 		this.minR = minR;
 	}
 
 	public int getMinG() {
 		return minG;
 	}
 
 	public void setMinG(int minG) {
 		this.minG = minG;
 	}
 
 	public int getMinB() {
 		return minB;
 	}
 
 	public void setMinB(int minB) {
 		this.minB = minB;
 	}
 
 	public int getMaxR() {
 		return maxR;
 	}
 
 	public void setMaxR(int maxR) {
 		this.maxR = maxR;
 	}
 
 	public int getMaxG() {
 		return maxG;
 	}
 
 	public void setMaxG(int maxG) {
 		this.maxG = maxG;
 	}
 
 	public int getMaxB() {
 		return maxB;
 	}
 
 	public void setMaxB(int maxB) {
 		this.maxB = maxB;
 	}
 
 }
