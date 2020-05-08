 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 23, 2012
  * Author(s): bene
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course.suggestions;
 
 import java.util.LinkedList;
 
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.helper.XMLParam;
 
 
 /**
  * The Suggestion object represents one suggestion.
  * Together with the suggestions message, its priority, the parameter ranges and the influences for simulation
  * step calculations are stored.
  * 
  * Note: this class has a natural ordering that is inconsistent with equals meaning just because compareTo() returns 0
  * does not imply that the two objects compared are equal. It only states that both Suggestion objects have the same
  * priority.
  * 
  * @author bene
  * 
  */
 public class Suggestion implements Comparable<Suggestion> {
 	private String			message;
 	private int				priority;
 	private XMLParam[]	parameters;
 	
 	
 	/**
 	 * Creates a new Suggestion object with the given parameters.
 	 * 
 	 * @param message The message of the Suggestion
 	 * @param priority The priority of this Suggestion
 	 * @param parameters The Array which contains the XMLParam objects with the range and influence information
 	 * @author bene
 	 */
 	public Suggestion(String message, int priority, XMLParam[] parameters) {
 		this.message = message;
 		this.priority = priority;
 		this.parameters = parameters;
 	}
 
 
 	/**
 	 * Creates a new Suggestion object.
 	 * 
 	 * @param range The array which contains the information for which ranges of course parameters this Suggestion should
 	 *           be displayed. NOTE: size has to be [number of course parameters][2] and [index][0] is the lower value
 	 *           whereas [index][1] is the upper value.
 	 * @param message The message actually displayed in the GUI
 	 * @param priority The priority of the Suggestion (required for sorting)
 	 * @param influenceValues The values that determine how executing this Suggestion affects the simulation.
 	 * @param paramNames The names of the course parameters.
 	 * @author bene
 	 */
 	public Suggestion(float[][] range, String message, int priority, float[] influenceValues,
 			LinkedList<String> paramNames) {
 		if (range.length != influenceValues.length || range.length != paramNames.size()) {
 			throw new IllegalArgumentException("Arrays need to have same length");
 		}
 		if (range.length == 0) {
 			throw new IllegalArgumentException("Arrays cannot be empty");
 		}
 		if (range[0].length != 2) {
 			throw new IllegalArgumentException("Range array needs to be of size [parameter count][2]");
 		}
 		this.message = message;
 		this.priority = priority;
 		this.parameters = new XMLParam[range.length];
 		for (int i = 0; i < this.parameters.length; i++) {
 			this.parameters[i] = new XMLParam(paramNames.get(i), influenceValues[i], range[i][0], range[i][1]);
 		}
 	}
 	
 	
 	/**
 	 * Returns the message text of this Suggestion.
 	 * 
 	 * @return Message text of this Suggestion.
 	 * @author bene
 	 */
 	public String getMessage() {
 		return message;
 	}
 	
 	
 	/**
 	 * Returns the low end of the range at the specified index.
 	 * 
 	 * @param index Index of the parameter.
 	 * @return Low end of the range.
 	 * @author bene
 	 */
 	public float getLowValueAt(int index) {
 		if (index >= this.parameters.length) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		return this.parameters[index].getRangeLowerEnd();
 	}
 	
 	
 	/**
 	 * Returns the high end of the range at the specified index.
 	 * 
 	 * @param index Index of the parameter.
 	 * @return High end of the range.
 	 * @author bene
 	 */
 	public float getHighValueAt(int index) {
 		if (index >= this.parameters.length) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		return this.parameters[index].getRangeUpperEnd();
 	}
 	
 	
 	/**
 	 * Returns true if paramValue is in the range at paramIndex or false if not.
 	 * 
 	 * @param paramIndex Index of the parameter.
 	 * @param paramValue Value of the parameter for comparison to the range values.
 	 * @return true if paramValue is in the range at paramIndex or false if not
 	 * @author bene
 	 */
 	public boolean paramIsInRange(int paramIndex, float paramValue) {
 		if (paramIndex >= this.parameters.length) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		return this.parameters[paramIndex].getRangeLowerEnd() <= paramValue
 				&& this.parameters[paramIndex].getRangeUpperEnd() >= paramValue;
 	}
 	
 	
 	/**
 	 * Returns the priority of this Suggestion.
 	 * 
 	 * @return Priority of this Suggestion
 	 * @author bene
 	 */
 	public int getPriority() {
 		return this.priority;
 	}
 	
 	
 	/**
 	 * Returns the influence vector of this suggestion.
 	 * 
 	 * @return Influence CalcVector for use in simulation.
 	 * @author bene
 	 */
 	public CalcVector getInfluenceVector() {
 		float[] tmp = new float[parameters.length];
 		for (int i = 0; i < tmp.length; i++) {
 			tmp[i] = parameters[i].getInfluence();
 		}
 		return new CalcVector(tmp);
 	}
 	
 	
 	public XMLParam getXMLParamAt(int index) {
 		if (index >= this.parameters.length) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		return this.parameters[index];
 	}
 	
 	
 	public String[] getParamNames() {
 		String[] result = new String[this.parameters.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = this.parameters[i].getName();
 		}
 		return result;
 	}
 	
	public void removeUnusedParameters(String[] paramNames) {
		XMLParam[] newParams = new XMLParam[paramNames.length];
 		int k = 0;
 		for (int i = 0; i < this.parameters.length && k < newParams.length; i++) {
 			boolean isNeeded = false;
			for (int j = 0; j < paramNames.length; i++) {
				if (this.parameters[i].getName().compareTo(paramNames[j]) == 0) {
 					isNeeded = true;
 					break;
 				}
 			}
 			if (isNeeded) {
 				newParams[k] = this.parameters[i];
 				k++;
 			}
 		}
 	}
 
 	
 	/**
 	 * Compares this Suggestions priority with the Suggestion s' priority. Returns 0 if both are equal, a negative value
 	 * if this Suggestions priority is lower than that of s and a positive value if this Suggestions priority is higher
 	 * than that of s.
 	 */
 	@Override
 	public int compareTo(Suggestion s) {
 		return this.priority - s.priority;
 	}
 }
