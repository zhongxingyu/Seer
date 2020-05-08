 package util.math;
 
 import org.lwjgl.util.vector.Vector3f;
 
 
 public class Plane
 {
 	public Vector3f normal = new Vector3f();
 	public Vector3f startingPoint = new Vector3f();
 	
 	public Plane() { }
 	
 	public Plane(Vector3f normal, Vector3f startingPoint)
 	{
 		this.normal.set(normal.getX(), normal.getY(), normal.getZ());
 		this.startingPoint.set(startingPoint.getX(), startingPoint.getY(), startingPoint.getZ());
 	}
 	
 	public Vector3f getNormal()
 	{
 		return new Vector3f(normal.x, normal.y, normal.z);
 	}
 	
 	public Vector3f getStartingPoint()
 	{
 		return new Vector3f(startingPoint.x, startingPoint.y, startingPoint.z);
 	}
 	
 	public void setNormal(Vector3f newNormal)
 	{
 		normal = MathHelper.cloneVector(newNormal);
 	}
 	
 	public void setStartingPoint(Vector3f newStartingPoint)
 	{
 		startingPoint = MathHelper.cloneVector(newStartingPoint);
 	}
 	
 	public void negateNormal()
 	{
 		this.normal.setX(this.normal.getX() * -1);
 		this.normal.setY(this.normal.getY() * -1);
 		this.normal.setZ(this.normal.getZ() * -1);
 	}
 	
 	public Vector3f getPoint(float x, float y)
 	{
 		return new Vector3f (x, y, (Vector3f.dot(normal, startingPoint) - normal.x * x - normal.y * y) / normal.z);
 	}
 	
 	public void transformToHesseNormalForm()
 	{
 		normal.normalise ();
 		//last factor of the coordinate-form
 		float lastFactor = Vector3f.dot(normal, (Vector3f) MathHelper.cloneVector(startingPoint).negate());
 		if(lastFactor > 0)
 			normal.negate ();
 	}
 	
 	/**
 	 * calculates the distance between a point and a plane
 	 * @param point the point
 	 * @return the distance
 	 */
 	public float calculateDistancePoint(Vector3f point)
 	{
 		this.transformToHesseNormalForm();
 		return (Vector3f.dot(this.normal, point) - Vector3f.dot(this.startingPoint, this.normal));
 	}
 	
 	/**
 	 * calculates the distance between a point and a plane
 	 * @param transformToHesseNormalFormFirst if this plane should be transformed
 	 * @param point the point
 	 * @return the distance
 	 */
 	public float calculateDistancePoint(boolean transformToHesseNormalFormFirst, Vector3f point)
 	{
 		if(transformToHesseNormalFormFirst)
 			this.transformToHesseNormalForm();
 		return (Vector3f.dot(this.normal, point) - Vector3f.dot(this.startingPoint, this.normal));
 	}
 	
 	/**
 	 * intersects a line with a plane
 	 * @param line the line
 	 * @param plane the plane
 	 * @return the Vector3f to the point if parallel method returns the nullVector
 	 */
 	public Vector3f intersectWithLine(Line line)
 	{
 		//Checks weather the line is parallel to the plane
 		if(Vector3f.dot(line.getDirection(), this.normal) == 0)
 			return null;
 		this.transformToHesseNormalForm();
 		//Calculates the factor for the direction-vector of the line
 		float factor = (Vector3f.dot(this.normal, this.startingPoint)-Vector3f.dot(line.getDirection(), line.getStartingPoint()))/
 						Vector3f.dot(line.getDirection(), this.normal);
 		return line.getPoint(factor);
 	}
 	
 	/**
 	 * gives back an array of 2 vectors with the intersectionPoints of the parabola with the plane
 	 * @param par the parabola
 	 * @return a Vector3f[2]-array of the 2 intersectionPoints: both null if no solution, second null if one solution, none null if 2 solutions
 	 */
 	public Vector3f[] intersectWithParabola(Parabola par)
 	{
 		this.transformToHesseNormalForm();
 		float discriminant = Vector3f.dot(normal, par.getDirection()) * Vector3f.dot(normal, par.getDirection()) - 4 *
 								Vector3f.dot(normal, par.getInfluence()) * (Vector3f.dot(normal, par.getStartingPoint()) -
 								Vector3f.dot(normal, startingPoint));
 		if(discriminant < 0)
 		{
 			Vector3f[] ret = {null, null};
 			return ret;
 		}
 		else if(discriminant == 0)
 		{
 			float factor = -Vector3f.dot(normal, par.getDirection()) / (2 * Vector3f.dot(normal, par.getInfluence()));
 			Vector3f[] ret = {par.getPoint(factor), null};
 			return ret;
 		}
 		else
 		{
 			float factor1 = (-Vector3f.dot(normal, par.getDirection()) + (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.getInfluence()));
 			float factor2 = (-Vector3f.dot(normal, par.getDirection()) - (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.getInfluence()));
 			Vector3f[] ret = {par.getPoint(factor1), par.getPoint(factor2)};
 			return ret;
 		}
 	}
 	
 	/**
 	 * intersects a plane with another one
 	 * @param plane the intersectionPlane plane
 	 * @return the intersectionLine of the 2 planes if parallel null will be returned
 	 */
 	public Line intersectWithPlane(Plane plane)
 	{
 		plane.transformToHesseNormalForm();
 		this.transformToHesseNormalForm();
 		//Checks weather the planes are parallel
 		if(plane.getNormal().x == this.getNormal().x && plane.getNormal().y == this.getNormal().y && plane.getNormal().z == this.getNormal().z)
 			return null;
 		Vector3f direction = new Vector3f();
 		Vector3f.cross(this.getNormal(), plane.getNormal(), direction);
 		direction.normalise();
 		float[][] matrix = new float[3][4];
 		matrix[0][0] = this.normal.x;
 		matrix[0][1] = this.normal.y;
 		matrix[0][2] = this.normal.z;
 		matrix[0][3] = Vector3f.dot(this.normal, this.startingPoint);
 		matrix[1][0] = plane.normal.x;
 		matrix[1][1] = plane.normal.y;
 		matrix[1][2] = plane.normal.z;
 		matrix[1][3] = Vector3f.dot(plane.normal, plane.startingPoint);
 		matrix[2][0] = direction.x;
 		matrix[2][1] = direction.y;
 		matrix[2][2] = direction.z;
 		matrix[2][3] = 0;
 		for(int i = 0; i <= 2; i++)
 			if(matrix[0][0] == 0)
 			{
 				float[] temp = {matrix[0][0], matrix[0][1], matrix[0][2], matrix[0][3]};
 				for(int a = 0; a<= 3; a++)
 				{
 					matrix[0][a] = matrix[1][a];
 					matrix[1][a] = matrix[2][a];
 					matrix[2][a] = temp[a];
 				}
 			}
 		if(matrix[1][1] == 0)
 		{
 			float[] temp = {matrix[1][0], matrix[1][1], matrix[1][2], matrix[1][3]};
 			for(int a = 0; a<= 3; a++)
 			{
 				matrix[1][a] = matrix[2][a];
 				matrix[2][a] = temp[a];
 			}
 		}
 		//just for backup: -->
 		if(matrix[0][0] == 0 || matrix[1][1] == 0 || matrix[2][2] == 0)
 			return null;
 		if(matrix[1][0] != 0)
 		{
 			float factor = -1 * matrix[0][0] / matrix[1][0];
 			for(int i = 0; i <= 3; i++)
 				matrix[1][i] = matrix[1][i] * factor + matrix[0][i];
 		}
 		if(matrix[2][0] != 0)
 		{
 			float factor = -1 * matrix[0][0] / matrix[2][0];
 			for(int i = 0; i <= 3; i++)
 				matrix[2][i] = matrix[2][i] * factor + matrix[0][i];
 		}
		if(matrix[1][1] == 0)
		{
			float[] temp = {matrix[1][0], matrix[1][1], matrix[1][2], matrix[1][3]};
			for(int a = 0; a<= 3; a++)
			{
				matrix[1][a] = matrix[2][a];
				matrix[2][a] = temp[a];
			}
		}
		//just for backup: -->
		if(matrix[0][0] == 0 || matrix[1][1] == 0 || matrix[2][2] == 0)
			return null;
 		if(matrix[2][1] != 0)
 		{
 			float factor = -1 * matrix[1][1] / matrix[2][1];
 			for(int i = 0; i <= 3; i++)
 				matrix[2][i] = matrix[2][i] * factor + matrix[1][i];
 		}
 		float z = matrix[2][3] / matrix[2][2];
 		float y = (matrix[1][3] - matrix[1][2] * z) / matrix[1][1];
 		float x = (matrix[0][3] - matrix[0][1] * y - matrix[0][2] * z) / matrix[0][0];
 		return new Line(x, y, z, direction);
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "normal: " + normal.toString() + "\nstartingPoint: " + startingPoint.toString();
 	}
 }
