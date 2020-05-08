 package util.math;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class Line
 {
 	public Vector3f startingPoint = new Vector3f();
 	public Vector3f direction = new Vector3f();
 	
 	public Line() {	}
 	
 	public Line(Vector3f startingPoint, Vector3f direction)
 	{
		this.startingPoint = startingPoint;
		this.direction = direction;
 	}
 	
 	public Line(float x, float y, float z, Vector3f direction)
 	{
 		this(new Vector3f(x, y, z), direction);
 	}
 	
 	public Vector3f getPoint(float factor)
 	{
 		Vector3f temp = new Vector3f();
 		Vector3f.add((Vector3f)direction.scale(factor), startingPoint, temp);
 		return temp;
 	}
 }
