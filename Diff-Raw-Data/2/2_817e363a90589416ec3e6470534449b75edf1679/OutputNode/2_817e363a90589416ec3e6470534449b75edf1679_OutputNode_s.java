 package neuralnet.network;
 
 /**
  * Concrete output node
  */
 import java.util.Iterator;
 
 /**
  * Concrete output node
  * 
  * @author gsc
  * @author cbarca
  */
 public class OutputNode extends AbstractNode {
 
 	/**
 	 * Constructor
 	 * @param learning_rate
 	 * @param momentum
 	 */
 	public OutputNode(double learning_rate, double momentum) {
 		this.learning_rate = learning_rate;
 		this.momentum = momentum;
 	}
     
     /**
      * Return learning rate
      * @return learning rate
      */
     public double getLearningRate() {
     	return (learning_rate);
     }
     
     /**
      * Return momentum term
      * @return momentum term
      */
     public double getMomentum() {
     	return (momentum);
     }
 
     /**
      * Return node output error term 
      * @return node output error term
      */
     public double getOutputError() {
     	return(output_error);
     }
     
     /**
      * Define node output error term
      * @param arg new output error term
      */
     public void setOutputError(double arg) {
     	output_error = arg;
     }
     
     /**
      * Aggregate the node output error for all 
      * patterns ran through the train epoch
      * @param arg
      */
     private void aggOutputError(double arg) {
    	agg_output_error += arg;
     }
     
     /**
      * Return the sum (aggregation) of all output errors 
      * @return aggregated output error value
      */
     public double getAggOuputError() {
     	return agg_output_error;
     }
     
 	/**
 	 * Update node value by summing weighted inputs
 	 */
 	public void runNode() {
 		double total = 0.0;
 
 		Iterator<Arc> ii = input_arcs.iterator();
 		while (ii.hasNext()) {
 			Arc arc = ii.next();
 			total += arc.getWeightedInputValue();
 		}
 
 		value = sigmoidTransfer(total);
 	}
 
 	/**
 	 * Update input weights based on error (delta rule)
 	 */
 	public void trainNode() {
 		output_error = computeOutputError();
 		this.aggOutputError(output_error);
 		error = computeError();
 
 		Iterator<Arc> ii = input_arcs.iterator();
 		while (ii.hasNext()) {
 			Arc arc = ii.next();
 			double gradient = error * arc.getInputValue();
 			arc.passGradient(gradient, learning_rate);
 		}
 	}
     
     /**
      * Return sigmoid transfer value, result 0.0 < value < 1.0
      * @return sigmoid transfer value, result 0.0 < value < 1.0
      */
     public double sigmoidTransfer(double value) {
     	return (1.0 / (1.0 + Math.exp(-value)));
     }
     
     /**
      * Return sigmoid derivate value
      * @param value 
      * @return sigmoid derivate value, dF(value)
      */
     public double sigmoidDerivate(double value) {
     	return (value * (1.0 - value));
     }
     
     /**
      * Compute output node error
      * @return output node error
      */
     private double computeOutputError() {
     	return (output_error - value);
     }
     
     /**
      * Compute output node backprop error using the derivative of 
      * the sigmoid transfer function.
      * @return output node backrop error
      */
     private double computeError() {
     	return (this.sigmoidDerivate(value) * output_error);
     }
     
     /**
      * Return description of object
      * @return description of object
      */
     public String toString() {
     	return (toString("OutputNode:"));
     }
     
     /**
      * Return description of object
      * @return description of object
      */
     public String toString(String prefix) {
     	String result = prefix + super.toString() + " learning rate:" + learning_rate + " momentum:" + momentum;
 	
     	return (result);
     }
     
     /**
      * Learning rate is used to help compute error term.
      */
     double learning_rate;
     
     /**
      * Momentum term is used to compute weight in Arc
      */
     double momentum;
     
     /**
      * Output error for this node
      */
     double output_error = 0;
     
     /**
      * Aggregate output error
      */
     double agg_output_error = 0;
     
     /**
      * Eclipse generated
      */
     private static final long serialVersionUID = -8313299157918441811L;
 }
