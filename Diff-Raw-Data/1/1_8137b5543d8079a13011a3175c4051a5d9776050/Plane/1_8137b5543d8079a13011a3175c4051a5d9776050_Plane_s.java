 package util.math;
 
 import org.lwjgl.util.vector.Vector3f;
 
 
 public class Plane
 {
 	Vector3f normal = new Vector3f();
 	Vector3f startingPoint = new Vector3f();
 	
 	public Plane() { }
 	
 	public Plane(Vector3f normal, Vector3f startingPoint)
 	{
 		this.normal = normal;
 		this.startingPoint = startingPoint;
 	}
 	
 	public Vector3f getPoint(float x, float y)
 	{
 		return new Vector3f (x, y, (Vector3f.dot(normal, startingPoint) - normal.x * x - normal.y * y) / normal.z);
 	}
 	
 	public void transformToHesseNormalForm()
 	{
 		normal.normalise ();
 		float lastFactor = normal.x * (-startingPoint.x) + normal.y * (-startingPoint.y) + normal.z * (-startingPoint.z);
 		if(lastFactor > 0)
 			normal.negate ();
 	}
 	
 	/**
 	 * calculates the distance between a point and a plane
 	 * @param point the point
 	 * @param plane the plane
 	 * @return the distance
 	 */
 	public float calculateDistancePoint(Vector3f point)
 	{
 		this.transformToHesseNormalForm();
 		return (this.normal.x * point.x + this.normal.y * point.y + this.normal.z * point.z - this.startingPoint.x * this.normal.x -
 				this.startingPoint.y * this.normal.y - this.startingPoint.z * this.normal.z);
 	}
 	
 	/**
 	 * intersects a line with a plane
 	 * @param line the line
 	 * @param plane the plane
 	 * @return the Vector3f to the point if parallel method returns the nullVector
 	 */
 	public Vector3f intersectWithLine(Line line)
 	{
 		//Checks weather the line is parallel to this
 		if(Vector3f.dot(line.direction, this.normal) == 0)
 			return new Vector3f(0, 0, 0);
 		this.transformToHesseNormalForm();
 		//Calculates the factor for the direction-vector of the line
 		float factor = (Vector3f.dot(this.normal, this.startingPoint)-Vector3f.dot(line.direction, line.startingPoint))/
 						Vector3f.dot(line.direction, this.normal);
 		return line.getPoint(factor);
 	}
 	
 	/**
 	 * gives back an array of 2 vectors with the intersectionPoints of the parabola with the plane
 	 * @param par the parabola
 	 * @return a Vector3f[2]-array of the 2 intersectionPoints: both null if no solution, second null if one solution, none null if 2 solutions
 	 */
 	public Vector3f[] intersectWithParabola(Parabola par)
 	{
 		float discriminant = Vector3f.dot(normal, par.dir) * Vector3f.dot(normal, par.dir) - 4 * Vector3f.dot(normal, par.inf) *
 							(Vector3f.dot(normal, par.startpoint) - Vector3f.dot(normal, startingPoint));
 		if(discriminant < 0)
 		{
 			Vector3f[] ret = {null, null};
 			return ret;
 		}
 		else if(discriminant == 0)
 		{
 			float factor = -Vector3f.dot(normal, par.dir) / (2 * Vector3f.dot(normal, par.inf));
 			Vector3f[] ret = {par.getPoint(factor), null};
 			return ret;
 		}
 		else
 		{
 			float factor1 = (-Vector3f.dot(normal, par.dir) + (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.inf));
 			float factor2 = (-Vector3f.dot(normal, par.dir) - (float)Math.sqrt(discriminant))/ (2 * Vector3f.dot(normal, par.inf));
 			Vector3f[] ret = {par.getPoint(factor1), par.getPoint(factor2)};
 			return ret;
 		}
 	}
 	
 	/**
 	 * intersects a plane with another one
 	 * @param plane the intersectionPlane plane
 	 * @return the intersectionLine of the 2 planes if parallel a Line with 0-initialization will be returned
 	 */
 	public Line intersectWithPlane(Plane plane)
 	{
 		plane.transformToHesseNormalForm();
 		this.transformToHesseNormalForm();
 		//Checks weather the planes are parallel
 		if(plane.normal.x == this.normal.x && plane.normal.y == this.normal.y && plane.normal.z == this.normal.z)
 			return new Line (0, 0, 0, new Vector3f(0,0,0));
 		Vector3f direction = new Vector3f();
 		direction.normalise();
 		Vector3f.cross(plane.normal, this.normal, direction);
 		//checks if plane1 is parallel to the x-axis
 		if(plane.normal.x == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(plane.normal.y == 0)
 			{
 				float z = plane.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the xz-plane
 			else if(plane.normal.z == 0)
 			{
 				float y = plane.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.y == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//checks if this is parallel to the x-axis too
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			//Check if this is parallel to the y-axis
 			else if(this.normal.y == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if plane1 is parallel to the y-axis
 		else if(plane.normal.y == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(plane.normal.x == 0)
 			{
 				float z = plane.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.z * z) / this.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(plane.normal.z == 0)
 			{
 				float x = plane.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(this.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the y-axis too
 			else if(this.normal.y == 0)
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 		}
 		//checks if plane1 is parallel to the z-axis
 		else if(plane.normal.z == 0)
 		{
 			//If plane1 is parallel to the xz-plane
 			if(plane.normal.x == 0)
 			{
 				float y = plane.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(this.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(this.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.y * y) / this.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(plane.normal.y == 0)
 			{
 				float x = plane.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(this.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(this.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(this.normal, this.startingPoint) - this.normal.x * x) / this.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(this.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(this.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.y / plane.normal.y)) /
 							(this.normal.z - (this.normal.y * plane.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.z * z) / plane.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(this.normal, this.startingPoint) -
 							(plane.normal.y * plane.startingPoint.y - plane.normal.z * plane.startingPoint.z) *	(this.normal.z / plane.normal.z)) /
 							(this.normal.x - (this.normal.y * plane.normal.x) / plane.normal.y);
 				float z = (Vector3f.dot(plane.normal, this.startingPoint) - plane.normal.x * x) / plane.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if this is parallel to the x-axis
 		else if(this.normal.x == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(this.normal.y == 0)
 			{
 				float z = this.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the xz-plane
 			else if(this.normal.z == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.y == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//checks if this is parallel to the x-axis too
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			//Check if this is parallel to the y-axis
 			else if(plane.normal.y == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(this.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		//checks if this is parallel to the y-axis
 		else if(this.normal.y == 0)
 		{
 			//If plane1 is parallel to the xy-plane
 			if(this.normal.x == 0)
 			{
 				float z = this.startingPoint.z;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.x;
 					return new Line(x, 0, z, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.z * z) / plane.normal.y;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.z == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the y-axis too
 			else if(plane.normal.y == 0)
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / plane.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 		}
 		//checks if this is parallel to the z-axis
 		else if(this.normal.z == 0)
 		{
 			//If plane1 is parallel to the xz-plane
 			if(this.normal.x == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.y == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(plane.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 		else
 		{
 			//If plane1 is parallel to the xz-plane
 			if(this.normal.x == 0)
 			{
 				float y = this.startingPoint.y;
 				//Check if this is parallel to the x-axis
 				 if(plane.normal.x == 0)
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 				//Check if plane 2 is parallel to the z-axis
 				else if(plane.normal.z == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float x = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.x;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set x value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.y * y) / plane.normal.z;
 					return new Line(0, y, z, direction);
 				}
 			}
 			//if plane1 is parallel to the yz-plane
 			else if(this.normal.y == 0)
 			{
 				float x = this.startingPoint.x;
 				//Check if this is parallel to the y-axis
 				 if(plane.normal.y == 0)
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 				//Check if plane 2 is parallel to the y-axis
 				else if(plane.normal.x == 0)
 				{
 					 //set z value to 0 and calculate the other values
 					float y = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.y;
 					return new Line(x, y, 0, direction);
 				}
 				else
 				{
 					 //set y value to 0 and calculate the other values
 					float z = (Vector3f.dot(plane.normal, plane.startingPoint) - plane.normal.x * x) / plane.normal.z;
 					return new Line(x, 0, z, direction);
 				}
 			}
 			//checks if this is parallel to the z-axis too
 			else if(plane.normal.z == 0)
 			{
 				 //set z value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.y;
 				return new Line(x, y, 0, direction);
 			}
 			//Check if this is parallel to the x-axis
 			else if(plane.normal.x == 0)
 			{
 				 //set x value to 0 and calculate the other values
 				float z = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.y / this.normal.y)) /
 							(plane.normal.z - (plane.normal.y * this.normal.z) / this.normal.y);
 				float y = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.z * z) / this.normal.y;
 				return new Line(0, y, z, direction);
 			}
 			else
 			{
 				 //set y value to 0 and calculate the other values
 				float x = (Vector3f.dot(plane.normal, plane.startingPoint) -
 							(this.normal.y * this.startingPoint.y - this.normal.z * this.startingPoint.z) *	(plane.normal.z / this.normal.z)) /
 							(plane.normal.x - (plane.normal.y * this.normal.x) / this.normal.y);
 				float z = (Vector3f.dot(this.normal, plane.startingPoint) - this.normal.x * x) / this.normal.z;
 				return new Line(x, 0, z, direction);
 			}
 		}
 	}
 }
