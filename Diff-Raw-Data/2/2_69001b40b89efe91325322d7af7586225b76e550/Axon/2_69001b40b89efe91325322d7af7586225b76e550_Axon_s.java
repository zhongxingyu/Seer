 package de.thm.mni.nn.perceptron.impl;
 
 /**
  * Axon Representation for a Neuronal Network. Training is implemented inside of
  * this class.
  * 
  * @author Tobias Knoth
  * 
  */
 public class Axon {
 	/**
 	 * Source Neuron of this Axon
 	 */
 	private Neuron source;
 
 	/**
 	 * Target Neuron of the Axon
 	 */
 	private Neuron target;
 
 	/**
 	 * Actual Weight of the Axon
 	 */
 	private double weight;
 
 	/**
 	 * Through Training actualized Weight of the Axon
 	 */
 	private double weight_new;
 
 	// CAPSULATION METHODS
 
 	/**
 	 * Returns the Source Neuron of the Axon.
 	 * 
 	 * @return The Axons Source Neuron.
 	 */
 	public Neuron getSource() {
 		return source;
 	}
 	
 	/**
 	 * Sets the Source Neuron of the Axon.
 	 * 
 	 * @param source 
 	 * 				The source of the Axon.
 	 */
 	private void setSource(Neuron source) {
 		this.source = source;
 	}
 
 	/**
 	 * Returns the actual Weight of the Axon.
 	 * 
 	 * @return Weight of the Axon.
 	 */
 	public double getWeight() {
 		return weight;
 	}
 
 	/**
 	 * Sets the actual Weight of the Axon
 	 * 
 	 * @param weight
 	 *            Weight to set.
 	 */
 	public void setWeight(double weight) {
 		this.weight = weight;
 	}
 	
 	/**
 	 * Returns the Target Neuron of the Axon.
 	 * 
 	 * @return The Axons Target Neuron.
 	 */
 	
 	public Neuron getTarget() {
 		return target;
 	}
 
 	/**
 	 * Sets the target Neuron of the Axon.
 	 * 
 	 * @param target The target of the Axon.
 	 */
 	
 	private void setTarget(Neuron target) {
 		this.target = target;
 	}
 
 	// CONSTRUCTORS
 
 	/**
 	 * Constructor requiring two Neurons and the weight of the connection.
 	 * 
 	 * @param source
 	 *            Source-Neuron of the Axon.
 	 * @param target
 	 *            Target-Neuron of the Axon.
 	 * @param seedmin
 	 *            Lower boundary of the random weight.
 	 * @param seedmax
 	 *            Upper boundary of the random weight.
 	 */
 	public Axon(Neuron source, Neuron target, double seedmin, double seedmax) {
 		this.setSource(source);
 		this.setTarget(target);
 		setRandonWeight(seedmin, seedmax);
 		target.connectNewDendrite(this);
 	}
 	/**
 	 * Calculates the initial weight of the Axon and rounds 
 	 * the number to three decimal places
 	 * 
 	 * @param seedMin
 	 * 				Minimal value of the random double number
 	 * @param seedMax
 	 * 				Maximal value of the random double number
 	 */
 	
 	public void setRandonWeight(double seedMin, double seedMax) {
 		if (seedMin > seedMax)
 		{
 			double tmp = seedMin;
 			seedMin = seedMax;
 			seedMax = tmp;
 		}
		double randomValue = Math.random() * ((seedMax - seedMin) + 1) + seedMin;
 		this.weight = Math.round(randomValue*1000)/1000d;	
 	}
 
 	/**
 	 * Calculates the new weight of the Axon by using the specified Training
 	 * Function
 	 * 
 	 * @return Functionvalue of the Training Function.
 	 */
 
 	public double trainWeight() {
 		throw new UnsupportedOperationException(
 				"Calculation of Weight not yet implemented");
 	}
 }
