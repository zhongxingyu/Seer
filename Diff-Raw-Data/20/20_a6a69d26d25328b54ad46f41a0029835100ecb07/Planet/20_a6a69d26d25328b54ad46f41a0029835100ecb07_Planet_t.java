 package ltg.phenomena.helioroom;
 
import android.util.Log;

 public class Planet {
 	
 	private String name = null;
 	private String color = null;
 	private String colorName = null;
 	private int classOrbitalTime = -1;
 	private float speed = -1;
 	private Degree startPosition = null; 
 	private Degree currentPosition = null;
 	// Graphic data
 	private float x = 0;
 	private float y = 0;
 	
 	
 	public Planet(String name, String color, String colorName, int classOrbitalTime, int startPosition) {
 		this.name = name;
 		this.color = color;
 		this.colorName = colorName;
 		this.classOrbitalTime = classOrbitalTime;
 		this.speed = 6 / (float) classOrbitalTime;
 		this.startPosition = new Degree(startPosition);
 		this.currentPosition = this.startPosition;
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public String getColor() {
 		return color;
 	}
 	
 	
 	public String getColorName() {
 		return colorName;
 	}
 
 
 	public int getClassOrbitalTime() {
 		return classOrbitalTime;
 	}	
 
 	
 	public Degree getStartPosition() {
 		return startPosition;
 	}
 	
 	public float getX() {
 		return this.x;
 	}
 
 	public float getY() {
 		return this.y;
 	}
 	
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 
 	public void computePosition(float timeDelta) {
 		currentPosition = new Degree(startPosition.getValue() - speed * timeDelta);
		x = (float) Math.sin(Math.toRadians(currentPosition.getValue()));
		y = (float) Math.cos(Math.toRadians(currentPosition.getValue()));
	}


	public String getCurrentPosition() {
		return String.valueOf(currentPosition.getValue());
 	}
 }
