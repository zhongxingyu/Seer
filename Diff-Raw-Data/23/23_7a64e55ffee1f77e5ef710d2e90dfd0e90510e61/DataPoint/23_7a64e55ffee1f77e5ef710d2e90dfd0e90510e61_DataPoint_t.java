 package com.shsrobotics.reinforcementlearning.util;
 
 /**
  * A set of key-value pairs for inputs and outputs. Used for optimizers and
  * estimators.
  * <p/>
  * @author Team 2412.
  */
 public class DataPoint {
 
 	/**
 	 * A list of input keys.
 	 */
 	private String[] inputKeys;
 
 	/**
 	 * Input values. These are matched with input keys.
 	 */
 	private double[] input;
 
 	/**
 	 * A list of output keys.
 	 */
 	private String[] outputKeys;
 
 	/**
 	 * Output values. These are matched with output keys.
 	 */
 	private double[] output;
 
 	/**
 	 * A set of variables.
 	 * <p/>
 	 * @param inputKeys the input keys.
 	 * @param input the input values.
 	 * @param outputKeys the output keys.
 	 * @param output the output values.
 	 */
 	public DataPoint(String[] inputKeys, double[] input, String[] outputKeys, double[] output) {
 		this.inputKeys = inputKeys;
 		this.input = input;
 		this.outputKeys = outputKeys;
 		this.output = output;
 	}
 	
 	/**
 	 * A set of variables.
 	 * <p/>
 	 * @param input the input values.
 	 * @param output the output value.
 	 */
 	public DataPoint(double[] input, double output) {
 		String[] inputKeys = new String[input.length];
 		String[] outputKeys = {"Output"};
 		double[] outputArray = {output};
 		for (int i = 0; i < input.length; i++) {
 			inputKeys[i] = String.valueOf(i);
 		}
 		this.inputKeys = inputKeys;
 		this.input = input;
 		this.outputKeys = outputKeys;
 		this.output = outputArray;
 	}
 	
 	/**
 	 * A set of variables.
 	 * <p/>
 	 * @param keys the input keys.
 	 * @param values  the input values.
 	 * @param isInput true if is an input point, false if is an output point.
 	 */
 	public DataPoint(String[] keys, double[] values, boolean isInput) {
 		if (isInput) {
 			this.inputKeys = keys;
 			this.input = values;
 			this.outputKeys = null;
 			this.output = null;
 		} else {
 			this.inputKeys = null;
 			this.input = null;
 			this.outputKeys = keys;
 			this.output = values;
 		}
 	}
 	
 	
 	/**
 	 * Get the input value associated with a key
 	 * <p/>
 	 * @param key the key.
 	 * @return the value.
 	 */
 	public double getInput(String key) {
 		return input[indexOf(key)];
 	}
 
 	/**
 	 * Get the output value associated with a key
 	 * <p/>
 	 * @param key the key.
 	 * @return the value.
 	 */
 	public double getOutput(String key) {
 		return output[indexOf(key)];
 	}
 
 	/**
 	 * Set a key-value pair.
 	 * <p/>
 	 * @param key the key.
 	 * @param value the value to set the key to.
 	 */
	public void setInput(String key, double value) {
		double[] newInput = input.clone();
		newInput[indexOf(key)] = value;
		input = newInput.clone();
 	}
 	
 	/**
 	 * Set a key-value pair.
 	 * <p/>
 	 * @param index the index of a key.
 	 * @param value the value to set the key to.
 	 */
 	public void setOutput(int index, double value) {
		double[] newOutput = input.clone();
		newOutput[index] = value;
		output = newOutput.clone();
 	}
 	
 	/**
 	 * Set a key-value pair.
 	 * <p/>
 	 * @param index the index of a key.
 	 * @param value the value to set the key to.
 	 */
 	public void setInput(int index, double value) {
		double[] newInput = input.clone();
		newInput[index] = value;
		input = newInput.clone();
 	}
 
 	/**
 	 * Get all of the input keys.
 	 * <p/>
 	 * @return the keys.
 	 */
 	public String[] getInputKeys() {
 		return inputKeys;
 	}
 
 	/**
 	 * Get all of the input values.
 	 * <p/>
 	 * @return the values.
 	 */
 	public double[] getInputs() {
 		return input;
 	}
 
 	/**
 	 * Get all of the output keys.
 	 * <p/>
 	 * @return the keys.
 	 */
 	public String[] getOutputKeys() {
 		return outputKeys;
 	}
 
 	/**
 	 * Get all of the output values.
 	 * <p/>
 	 * @return the values.
 	 */
 	public double[] getOutputs() {
 		return output;
 	}
 
 	/**
 	 * Finds the value from an array of string keys
 	 * <p/>
 	 * @param value the value to look for.
 	 */
 	private int indexOf(String value) {
 		for (int i = 0; i < inputKeys.length; i++) {
 			if (value.equals(inputKeys[i])) { // found
 				return i;
 			}
 		}
 		return -1; // not found
 	}
 }
