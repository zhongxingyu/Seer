 package dta_solver.adjointMethod;
 
 /**
  * Interface for a general gradient descent
  */
 public interface GradientDescentOptimizer {
 
   /**
    * @brief Fill in the gradient with the values of the gradient
    * @param gradient_f
    *          The array that will contain the gradient
    * @param control
    *          The point where the gradient is computed
    */
   public void gradient(double[] gradient_f, double[] control);
 
   /**
    * @brief Returns the evaluation of the cost function at point x
    */
   public double objective(double[] u);
 
   /**
    * @brief Get a valid point to begin the gradient descent
    */
   public double[] getStartingPoint();
 
  public void projectControl(double[] control);
 }
