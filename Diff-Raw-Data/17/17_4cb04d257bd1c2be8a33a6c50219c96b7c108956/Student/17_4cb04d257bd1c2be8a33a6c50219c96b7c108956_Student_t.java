 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 6, 2012
  * Author(s): Benedikt Zirbes
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course.student;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.influence.Influence;
 import edu.dhbw.sos.helper.CalcVector;
 
 
 /**
  * this class represents a student
  * 
  * @author bene, dirk
  * 
  */
 public class Student implements IPlace, Cloneable {
 	private CalcVector									actualState;
 	private LinkedHashMap<Integer, CalcVector>	historyStates		= new LinkedHashMap<Integer, CalcVector>();
 	private HashMap<Integer, Student>				historyDonInput	= new HashMap<Integer, Student>();
 	private CalcVector									changeVector;
 	private static final Logger						logger				= Logger.getLogger(Student.class);
 	
 	
 	public Student(int vectorInitSize) {
 		this.actualState = new CalcVector(vectorInitSize);
 		this.changeVector = new CalcVector(vectorInitSize);
 	}
 	
 	public Student(CalcVector actualState, CalcVector changeVector) {
 		this.actualState = actualState;
 		this.changeVector = changeVector;
 	}
 	
 	
 	/**
 	 * Only for testing yet. Should be tested and discussed
 	 * 
 	 * @param index index in vector
 	 * @param value value to add (negative to sub)
 	 * @author NicolaiO
 	 */
 	public void donInput(int index, float value, int time) {
 		CalcVector cv = new CalcVector(4);
 		cv.setValueAt(index, value);
 		addHistoryDonInput(time);
 		addToChangeVector(cv);
 		addToStateVector(cv, 0, 0);
 	}
 	
 	/**
 	 * adds a new state to the history states
 	 * @param time
 	 * @param currentState
 	 * @author dirk
 	 */
 	public void addHistoryDonInput(int time) {
 		historyDonInput.put(time, this.clone());
 	}
 	
 	/**
 	 * takes a start and end time
 	 * if there was an interaction from the don in this time period the latest interaction will be returned
 	 * otherwise null will be returned
 	 * @param start time in milliseconds
 	 * @param end time in milliseconds
 	 * @return
 	 * @author dirk
 	 */
 	public Entry<Integer, Student> historyDonInputInInterval(int start, int end) {
 		Entry<Integer, Student> latest = null;
 		for (Entry<Integer, Student> historyState : historyDonInput.entrySet()) {
 			if (historyState.getKey() > start && historyState.getKey() < end) {
 				if (latest == null || latest.getKey() < historyState.getKey())
 					latest = historyState;
 			}
 		}
 		return latest;
 	}
 	
 	
 	/**
 	 * calculates the next state for the actual state vector
 	 * @param changeVector
 	 * @param influence
 	 * @author dirk
 	 */
	public void calcNextSimulationStep(CalcVector changeVector, Influence influence, int x, int y, int time) {
		
		saveHistoryStates(time);
 		
 		// - - parameter
 		double parameterInf = 0.001;
 		changeVector.addCalcVector(influence.getInfluencedParameterVector(this.getActualState().clone(), parameterInf));
 		
 		if (y == 0 && x == 0)
 			changeVector.printCalcVector("matrix influenced");
 		
 		// - usual behavior of the student -> usualBehav * timeInf
 		double behaviorInf = 0.001;
 		changeVector.addCalcVector(this.getChangeVector().clone().multiplyWithDouble(behaviorInf));
 		
 		if (y == 0 && x == 0)
 			changeVector.printCalcVector("student influenced");
 		
 		// time depending
 		// TODO: bring all values to an average value by time
 		
 		this.addToStateVector(changeVector, x, y);
 	}
 	
 	/**
 	 * 
 	 * adds a change vector to the state vector of a student
 	 * 
 	 * @param addVector 
 	 * @param x TO DELETE, only for simulation debug
 	 * @param y TO DELETE, only for simulation debug
 	 * @author dirk
 	 */
 	public void addToStateVector(CalcVector addVector, int x, int y) {
 		if (y == 0 && x == 0)
 			addVector.printCalcVector("ADD Vector");
 		if (y == 0 && x == 0)
 			actualState.printCalcVector("Actual State");
 		for (int i = 0; i < addVector.size(); i++) {
 			double sValue = actualState.getValueAt(i);
 			double vValue = addVector.getValueAt(i);
 			// if the add value is positive, take the percentage missing to 100, and multiply it with 2
 			// i.e. acutalState = 30, addVector = 20 -> (100-30)*2/100 -> 1,4*20 = 28 -> 58
 			// i.e. acutalState = 80, addVector = 20 -> (100-80)*2/100 -> 0,4*20 = 8 -> 88
 			// i.e. acutalState = 95, addVector = 20 -> (100-95)*2/100 -> 0,1*20 = 2 -> 97
 			// i.e. acutalState = 98, addVector = 20 -> (100-98)*2/100 -> 0,04*20 = 0.8 -> 98.8
 			if (vValue > 0) {
 				actualState.setValueAt(i, actualState.getValueAt(i) + (int) (vValue * ((100 - sValue) * 2 / 100)));
 			} else {
 				actualState.setValueAt(i, actualState.getValueAt(i) + (int) (vValue * ((sValue) * 2 / 100)));
 			}
 			if (actualState.getValueAt(i) < 0) {
 				actualState.setValueAt(i, 0);
 			}
 			if (actualState.getValueAt(i) > 100) {
 				actualState.setValueAt(i, 100);
 			}
 		}
 	}
 	
 	/**
 	 * modify the change vector after a don input
 	 * @param addVector
 	 * @author dirk
 	 */
 	public void addToChangeVector(CalcVector addVector) {
 		changeVector.addCalcVector(addVector);
 		for(int i=0; i<changeVector.size(); i++) {
 			if(changeVector.getValueAt(i)<100)
 				changeVector.setValueAt(i, 100);
 			if(changeVector.getValueAt(i)>0)
 				changeVector.setValueAt(i, 0);
 		}
 	}
 	
 	
 	public void printAcutalState() {
 		String out = "Students state: ";
 		for (int i = 0; i < actualState.size(); i++)
 			out += actualState.getValueAt(i) + ", ";
 		out = out.substring(0, out.length() - 2);
 		logger.info(out);
 	}
 	
 	
 	/**
 	 * another interface that is needed but should be reconsidered
 	 * 
 	 * @return
 	 * @author NicolaiO
 	 */
 	public int getAverageState() {
 		int res = 0;
 		for (int i = 0; i < actualState.size(); i++) {
 			res += actualState.getValueAt(i);
 		}
 		if (actualState.size() != 0)
 			res /= actualState.size();
 		return res;
 	}
 	
 	
 	/**
 	 * Returns the actualState of this student to allow further access to it.
 	 * 
 	 * @return
 	 * @author bene
 	 */
 	public CalcVector getActualState() {
 		return this.actualState;
 	}
 	
 	
 	/**
 	 * Sets the actualState vector
 	 * 
 	 * @return
 	 * @author dirk
 	 */
 	public void setActualState(CalcVector cv) {
 		this.actualState = cv;
 	}
 	
 	
 	/**
 	 * Returns the changeVector of this student to allow further access to it.
 	 * 
 	 * NOTE: this is only for testing. Should be replaced by a separate method in Student.
 	 * 
 	 * @return
 	 * @author bene
 	 */
 	public CalcVector getChangeVector() {
 		return this.changeVector;
 	}
 	
 	
 	/**
 	 * Adds value to the value of the parmeter at position index.
 	 * 
 	 * @param index
 	 * @param value
 	 * @return
 	 * @author bene
 	 */
 	public void addValueToChangeVector(int index, float value) {
 		if (index >= changeVector.size()) {
 			throw new IndexOutOfBoundsException();
 		}
 		changeVector.setValueAt(index, changeVector.getValueAt(index) + value);
 	}
 	
 	
 	/**
 	 * Adds value to the value of the parmeter at position index.
 	 * 
 	 * @param index
 	 * @param value
 	 * @return
 	 * @author bene
 	 */
 	public void addValueToStateVector(int index, float value) {
 		if (index >= changeVector.size()) {
 			throw new IndexOutOfBoundsException();
 		}
 		actualState.setValueAt(index, actualState.getValueAt(index) + value);
 	}
 	
 	
 	/**
 	 * Returns a string representation of this student object for saving it to a file. Note that only the actualState
 	 * vector and the attribute isEmpty are saved.
 	 * 
 	 * @return
 	 * @author bene
 	 */
 	public String getSaveableString(LinkedList<String> params) {
 		String ret = "<student isEmpty=\"0\">";
 		
 		for (int i = 0; i < this.actualState.size(); i++) {
 			ret += "<attribute name=\"" + params.get(i) + "\" value=\"" + this.actualState.getValueAt(i)
 					+ "\"></attribute>";
 		}
 		ret += "</student>";
 		return ret;
 	}
 	
 	
 	/**
 	 * Creates an exact clone of this student object. All values are copied into a completely new object (no references!)
 	 */
 	public Student clone() {
 		return new Student(this.actualState.clone(),this.changeVector.clone());
 	}
 	
 	
 	/**
 	 * adds a new state to the history states
 	 * @param time
 	 * @param currentState
 	 * @author dirk
 	 */
 	public void saveHistoryStates(int time) {
 		historyStates.put(time, actualState);
 	}
 	
 	
 	@Override
 	public LinkedHashMap<Integer, CalcVector> getHistoryStates() {
 		return historyStates;
 	}
 
 }
