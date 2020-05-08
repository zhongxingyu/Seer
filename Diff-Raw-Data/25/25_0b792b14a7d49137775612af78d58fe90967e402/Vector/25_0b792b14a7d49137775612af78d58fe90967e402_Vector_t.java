 package common;
 
 // implementation of a 2D Vector
 
 public class Vector implements IVector {
 
 	/**
 	 * The vectors components
 	 */
 	private float[] components;
 
 	/**
 	 * Is the vectors length already calculated
 	 */
 	private boolean lengthCalculated = false;
 
 	/**
 	 * The vectors length
 	 */
 	private float length;
 
 	/**
 	 * @param array
 	 *            of floats, the vectors components
 	 */
 	public Vector(float[] components) {
 		if (components != null) {
 			if (components.length == 2) {
 				this.components = components.clone();
 			} else {
 				this.components = new float[2];
 				int i;
 				for (i = 0; i < components.length; i++) {
 					if (i < 2) {
 						this.components[i] = components[i];
 					} else {
 						break;
 					}
 				}
 				if (i < 2) {
 					for (int j = i; j < 2; j++) {
 						this.components[j] = 0;
 					}
 				}
 			}
 		} else {
 			this.components = new float[] { 0, 0 };
 		}
 	}
 
 	/**
 	 * @param the
 	 *            vector to be added to this vector
 	 * @return a vector as a result of the operation
 	 */
 	@Override
 	public Vector add(IVector vector) {
 		return new Vector(new float[] { components[0] + vector.getComponent(0),
 				components[1] + vector.getComponent(1) });
 	}
 
 	/**
 	 * @param the
 	 *            vector to calculate the crossproduct with this vector
 	 * @return a vector as a result of the operation
 	 * 
 	 *         the cross product of 2 2d vectors is usually (0,0,z) the
 	 *         corresponding 2d vector is (0,0) hence the function will always
 	 *         return (0,0)
 	 */
 	@Override
 	public Vector cross(IVector vector) {
 		return new Vector(new float[] { 0, 0 });
 	}
 
 	/**
	 * @param the vector to calculate the dotproduct with this vector
 	 * @return a float as a result of the operation
 	 */
 	@Override
 	public float dot(IVector vector) {
 		float scalar = 0;
 		for (int i = 0; i < this.components.length; i++) {
 			scalar += this.components[i] * vector.getComponent(i);
 		}
 		return scalar;
 	}
 
 	/**
 	 * @param index
 	 *            of a component of the vector
 	 * @return the components value
 	 */
 	@Override
 	public float getComponent(int index) {
 		if (index >= this.components.length) {
 			return 0;
 		} else {
 			return this.components[index];
 		}
 	}
 
 	/**
 	 * @return the length of the vector
 	 */
 	@Override
 	public float norm() {
 		if (!lengthCalculated) {
 			for (int i = 0; i < this.components.length; i++) {
 				length += this.components[i] * this.components[i];
 			}
 			this.length = (float) Math.sqrt(length);
 			lengthCalculated = true;
 		}
 		return this.length;
 	}
 
 	/**
 	 * @param the
 	 *            vector to be subtracted from this vector
 	 * @return a vector as a result of the operation
 	 */
 	@Override
 	public Vector sub(IVector vector) {
 		return new Vector(new float[] { components[0] - vector.getComponent(0),
 				components[1] - vector.getComponent(1) });
 	}
 
 	/**
 	 * @return a vector of the same direction with the length 1
 	 */
 	@Override
 	public Vector normalize() {
 		float[] newComponents = new float[this.components.length];
 		for (int i = 0; i < this.components.length; i++) {
 			newComponents[i] = this.components[i] / this.norm();
 		}
 		return new Vector(newComponents);
 	}
 
 	/**
 	 * @return a clone of this vector
 	 */
 	@Override
 	public Vector clone() {
 		return new Vector(this.components);
 	}
 
 	/**
 	 * @param factor
 	 *            to multiply this vector with
 	 * @return a scaled copy of the actual vector
 	 */
 	@Override
 	public Vector multiply(float factor) {
 		float[] newComponents = new float[this.components.length];
 		for (int i = 0; i < this.components.length; i++) {
 			newComponents[i] = this.components[i] * factor;
 		}
 		return new Vector(newComponents);
 	}
 
 	/**
 	 * Print the coordinates of the vector
 	 */
 	@Override
 	public String toString() {
 		return "x: " + this.components[0] + ", y: " + this.components[1];
 	}
 
 	/**
 	 * @param an
 	 *            angle in the radiant notation [0,2*PI]
 	 * @return a vector rotated by the input angle
 	 */
 	@Override
 	public Vector rotate(float angle) {
 		float newAngle = this.getAngle() + angle;
 		return new Vector(new float[] {
 				(float) Math.cos(newAngle) * this.norm(),
 				(float) Math.sin(newAngle) * this.norm() });
 	}
 
 	/**
 	 * Return the angle to the X axis 
 	 * @return an angle in radiant
 	 */
 	@Override
 	public float getAngle() {
 		IVector newThis = this.normalize();
 		IVector xAxis = new Vector(new float[] { 1, 0 });
 		float cosAlpha = newThis.dot(xAxis);
 		float direction = (float) Math.atan2(newThis.getComponent(1), newThis
 				.getComponent(0));
 		float angle = (float) Math.acos(cosAlpha);
 		if (direction >= 0) {
 			return angle;
 		} else {
 			return (float) (Math.PI * 2) - angle;
 		}
 	}
 
 	@Override
 	public int compareTo(IVector arg0) {
 		Boolean isEqual = true;
 		for (int i = 0; i < this.components.length; i++) {
 			if (this.components[i] != arg0.getComponent(i)) {
 				isEqual = false;
 			}
 		}
 		if (isEqual) {
 			return 0;
 		} else if (this.norm() > arg0.norm()) {
 			return 1;
 		} else {
 			return -1;
 		}
 	}
 	
 	public static LinearCombination getLinearCombination (IVector center, IVector directionA, IVector directionB, IVector point){
 		IVector position = point.sub(center);	
 		float denominator = 
 			directionA.getComponent(0) * directionB.getComponent(1)
 			- directionA.getComponent(1) * directionB.getComponent(0);
 		
 		float lambda = -(directionB.getComponent(0) * position.getComponent(1)
 				- directionB.getComponent(1) * position.getComponent(0)) /
 				denominator;
 		
 		float mu = (directionA.getComponent(0) * position.getComponent(1)
 				- directionA.getComponent(1) * position.getComponent(0)) /
 				denominator;
 		return new LinearCombination (mu, lambda);
 	}
 }
 
 
