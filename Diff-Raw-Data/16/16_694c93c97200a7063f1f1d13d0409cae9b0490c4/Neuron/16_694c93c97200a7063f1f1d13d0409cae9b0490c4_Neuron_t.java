 package de.unikassel.ann.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.unikassel.ann.model.func.ActivationFunction;
 
 /**
  * @author anton
  *
  */
 public class Neuron {
 
 	private Layer layer;
 	
 	private Integer index = -1;
 	
 	private ActivationFunction function;
 
 	private Double outputValue;
 		
 	private Double delta;
 	
 	private List<Synapse> incomingSynapses;
 	
 	private List<Synapse> outgoingSynapses;
 
 	private boolean bias;
 	
 	public Neuron(ActivationFunction standardFunc, double outputValue, boolean bias) {
 		this.outputValue = outputValue;
 		this.bias = bias;
 		if (bias) {
			this.outputValue = 1.0d;
 		}
 		this.function = standardFunc;
 		incomingSynapses = new ArrayList<Synapse>();
 		outgoingSynapses = new ArrayList<Synapse>();
 	}
 	
 	public Neuron(ActivationFunction function, boolean bias) {
 		this(function, 0.0d, bias);
 	}
 
 	public List<Synapse> getIncomingSynapses() {
 		return incomingSynapses;
 	}
 	
 	public List<Synapse> getOutgoingSynapses() {
 		return outgoingSynapses;
 	}
 
 	public void setOutputValue(Double val) {
 		if (bias) {
 			throw new IllegalAccessError("cannot set input value for bias neuron");
 		}
 		outputValue = val;
 	}
 	public Double getOutputValue() {
 		return outputValue;
 	}
 
 	public void addOutgoingSynapse(Synapse synapse) {
 		outgoingSynapses.add(synapse);
 		if ((synapse.getFromNeuron() != null && synapse.getFromNeuron().equals(this)) == false) {
 			synapse.setFromNeuron(this);
 		}
 	}
 	
 	public void addIncomingSynapse(Synapse synapse) {
 		incomingSynapses.add(synapse);
 		if ((synapse.getToNeuron() != null && synapse.getToNeuron().equals(this)) == false) {
 			synapse.setToNeuron(this);
 		}
 	}
 	
 	/**
 	 * Sets the delta or error value
 	 * @param error
 	 */
 	public void setDelta(double error) {
 		this.delta = error;
 	}
 
 	/**
 	 * Calculates the activation function with the value and set the result as outpuvalue<br>
 	 * {@link #outputValue} = f (val)
 	 */
 	public void activate(Double val) {
 		if (bias) {
 			outputValue = 1.0d;
 		} else {
 			outputValue = function.calc(val);
 		}
 	}
 	
 
 	public void setLayer(Layer layer) {
 		this.layer = layer;
 		if (layer.getNeurons() != null && layer.getNeurons().contains(this) == false) {
 			layer.addNeuron(this);
 		}
 		
 	}
 
 	public Layer getLayer() {
 		return layer;
 	}
 
 	public void setIndex(int size) {
 		index = size;
 	}
 	
 	public Integer getIndex() {
 		return index;
 	}
 
 	/**
 	 * @return the delta or error
 	 */
 	public double getDelta() {
 		return delta;
 	}
 
 	public boolean isBias() {
 		return bias;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append(getPosition());
 		sb.append(bias ? " (B)" : "");
 		
 		sb.append("output = ");
 		sb.append(outputValue == null ? "N/A" : outputValue);
 		sb.append("\n");
 		return sb.toString();
 	}
 	
 	public String getPosition() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("[");
 		sb.append(layer.getIndex());
 		sb.append("]");
 		
 		sb.append("[");
 		sb.append(index);
 		sb.append("]");
 		return sb.toString();
 	}
 
 }
