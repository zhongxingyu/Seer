 package tomato.math;
 
 public interface IVec2D {
 	
 	public double getLength();
 
 	public IVec2D addition(IVec2D v);
 
 	/**
 	 * Returns a new IVec2D, which is the current IVec2D multiplied with a factor.
 	 * @param factor The factor to be multiplied with.
 	 * @return returned = factor * current.
 	 */
 	public IVec2D multiply(double factor);
 
 	public IVec2D getConnectionVector(IVec2D b);
 
	public double pointProduct(IVec2D v);
 
 	public IVec2D substract(IVec2D v);
 
	public IVec2D getUnitVector();
 
 	public double getX();
 
 	public void setX(double x);
 
 	public double getY();
 
 	public void setY(double y);
 
 }
