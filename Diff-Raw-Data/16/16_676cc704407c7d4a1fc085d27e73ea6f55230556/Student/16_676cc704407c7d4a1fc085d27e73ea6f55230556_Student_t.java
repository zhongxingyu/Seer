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
 	private static final Logger									logger	= Logger.getLogger(Student.class);
 	/**
 	 * Consider to add new attributes to the clone() method
 	 * if you want to store them persistent!
 	 */
 	private transient CalcVector									actualState;
 	private transient LinkedHashMap<Integer, CalcVector>	historyStates;
 	private CalcVector												changeVector;
 	
 	
 	public Student(int vectorInitSize) {
 		float[] changeVectorF = new float[vectorInitSize];
 		for (int i = 0; i < vectorInitSize; i++) {
 			changeVectorF[i] = 1f;
 		}
 		this.changeVector = new CalcVector(changeVectorF);
 		init();
 	}
 	
 	
 	public Student(CalcVector actualState, CalcVector changeVector) {
 		this.actualState = actualState;
 		this.changeVector = changeVector;
 		this.historyStates = new LinkedHashMap<Integer, CalcVector>();
 	}
 	
 	
 	private void init() {
 		this.actualState = new CalcVector(changeVector.size());
 		for (int i = 0; i < changeVector.size(); i++) {
 			float changeVectorFloat = changeVector.getValueAt(i);
 			this.actualState.setValueAt(i, changeVectorFloat * 50);
 		}
 		this.historyStates = new LinkedHashMap<Integer, CalcVector>();
 	}
 	
 	
 	private Object readResolve() {
 		init();
 		return this;
 	}
 
 
 	/**
 	 * Only for testing yet. Should be tested and discussed
 	 * 
 	 * @param index index in vector
 	 * @param value value to add (negative to sub)
 	 * @author NicolaiO
 	 */
 	@Override
 	public void donInput(int index, float value, int time) {
 		CalcVector cv = new CalcVector(4);
 		cv.setValueAt(index, value);
 		changeVector.printCalcVector("Don Input: preChangeVector: ");
 		addToChangeVector(index, value);
 		addToStateVector(cv, 0, 0);
 		changeVector.printCalcVector("Don Input: postChangeVector: ");
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
 	public Entry<Integer, CalcVector> nearestHistoryState(int time) {
 		Entry<Integer, CalcVector> latest = null;
 		for (Entry<Integer, CalcVector> historyState : historyStates.entrySet()) {
 			if (historyState.getKey() < time) {
 				if (latest == null || latest.getKey() < historyState.getKey())
 					latest = historyState;
 			}
 		}
 		return latest;
 	}
 	
 	
 	/**
 	 * deletes all history states after a given time
 	 * 
 	 * @param time
 	 * @return
 	 * @author dirk
 	 */
 	public void deleteHistoryStateFrom(int time) {
 		LinkedList<Integer> toDelete = new LinkedList<Integer>();
 		for (Entry<Integer, CalcVector> historyState : historyStates.entrySet()) {
 			if (historyState.getKey() > time) {
 				toDelete.add(historyState.getKey());
 			}
 		}
 		for (Integer state : toDelete) {
 			historyStates.remove(state);
 		}
 	}
 	
 
 	/**
 	 * calculates the next state for the actual state vector
 	 * @param addVector
 	 * @param influence
 	 * @author dirk
 	 */
 	public void calcNextSimulationStep(CalcVector addVector, Influence influence, int time, int x, int y) {
 		
 		saveHistoryStates(time);
 		
 		// parameter matrix * actual state
 		// double parameterInf = 0.00000001;
 		// addVector.addCalcVector(influence.getInfluencedParameterVector(this.getActualState().clone(), parameterInf));
 		// if (y == 1 && x == 1)
 		// addVector.printCalcVector("Sim(1,1): matrix influenced");
 		
 		// usual behavior of the student ( usualBehav * behaviorInf )
 		
 		if (y == 1 && x == 1)
 			changeVector.printCalcVector("Sim(1,1): Change vector");
 		addVector.multiply(this.getChangeVector());
 		if (y == 1 && x == 1)
 			addVector.printCalcVector("Sim(1,1): student influenced");
 		
 		// time depending
 		// TODO: bring all values to an average value by time
 		// addVector.multiply(0.1);
 		this.addToStateVector(addVector, x, y);
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
 		if (y == 1 && x == 1)
 			addVector.printCalcVector("Add(1,1): addVector");
 		if (y == 1 && x == 1)
 			actualState.printCalcVector("Add(1,1): preActualState");
 		for (int i = 0; i < addVector.size(); i++) {
 			double sValue = actualState.getValueAt(i);
 			double vValue = addVector.getValueAt(i);
 			// if the add value is positive, take the percentage missing to 100, and multiply it with 2
 			// i.e. acutalState = 30, addVector = 20 -> (100-30)*2/100 -> 1,4*20 = 28 -> 58
 			// i.e. acutalState = 80, addVector = 20 -> (100-80)*2/100 -> 0,4*20 = 8 -> 88
 			// i.e. acutalState = 95, addVector = 20 -> (100-95)*2/100 -> 0,1*20 = 2 -> 97
 			// i.e. acutalState = 98, addVector = 20 -> (100-98)*2/100 -> 0,04*20 = 0.8 -> 98.8
 			if (vValue > 0) {
 				actualState.setValueAt(i, actualState.getValueAt(i) + (float) (vValue * ((100 - sValue) * 2 / 100)));
 			} else {
 				actualState.setValueAt(i, actualState.getValueAt(i) + (float) (vValue * ((sValue) * 2 / 100)));
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
 	public void addToChangeVector(int index, float value) {
 		changeVector.setValueAt(index, changeVector.getValueAt(index) + value / 100);
 		// changeVector.addCalcVector(addVector);
 		// for (int i = 0; i < changeVector.size(); i++) {
 		// if (changeVector.getValueAt(i) < 100)
 		// changeVector.setValueAt(i, 100);
 		// if (changeVector.getValueAt(i) > 0)
 		// changeVector.setValueAt(i, 0);
 		// }
 	}
 	
 	
 	@Override
 	public void printAcutalState() {
 		String out = "Students state: ";
 		for (int i = 0; i < actualState.size(); i++)
 			out += actualState.getValueAt(i) + ", ";
 		out = out.substring(0, out.length() - 2);
 		logger.debug(out);
 	}
 	
 	
 	/**
 	 * another interface that is needed but should be reconsidered
 	 * 
 	 * @return
 	 * @author NicolaiO
 	 */
 	@Override
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
 	@Override
 	public CalcVector getActualState() {
 		return this.actualState;
 	}
 	
 	
 	/**
 	 * Sets the actualState vector
 	 * 
 	 * @return
 	 * @author dirk
 	 */
 	@Override
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
 		if (value < 1.5)
 			this.changeVector.setValueAt(index, 1.5f);
 		else if (value > 0.5)
 			this.changeVector.setValueAt(index, 0.5f);
 		changeVector.setValueAt(index, changeVector.getValueAt(index) + value / 100);
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
 	@Override
 	public Student clone() {
 		Student student = new Student(this.actualState.clone(), this.changeVector.clone());
 		student.setHistoryStates(this.historyStates);
 		return student;
 	}
 	
 	
 	/**
 	 * adds a new state to the history states
 	 * @param time
 	 * @param currentState
 	 * @author dirk
 	 */
 	public void saveHistoryStates(int time) {
 		historyStates.put(time, actualState.clone());
 	}
 	
 	
 	@Override
 	public LinkedHashMap<Integer, CalcVector> getHistoryStates() {
 		return historyStates;
 	}
 
 
 	public void setHistoryStates(LinkedHashMap<Integer, CalcVector> historyStates) {
 		this.historyStates = historyStates;
 	}
 	
 	
 }
