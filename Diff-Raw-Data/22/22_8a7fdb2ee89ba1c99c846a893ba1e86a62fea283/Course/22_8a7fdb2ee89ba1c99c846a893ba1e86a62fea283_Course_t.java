 /* 
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 14, 2012
  * Author(s): NicolaiO
  *
  * *********************************************************
  */
 package edu.dhbw.sos.course;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import edu.dhbw.sos.course.influence.EInfluenceType;
 import edu.dhbw.sos.course.influence.Influence;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.course.lecture.Lecture;
 import edu.dhbw.sos.course.student.EmptyPlace;
 import edu.dhbw.sos.course.student.IPlace;
 import edu.dhbw.sos.course.student.Student;
 import edu.dhbw.sos.helper.CalcVector;
 
 /**
  * This class is a data holder for the data belonging to the course
  * 
  * @author DirkK
  */
 public class Course {
 	private IPlace[][] students;
 	private Influence influence;
 	private Lecture lecture;
 	private HashMap<Integer, IPlace[][]> historyStates;
 	private LinkedList<String> properties;
 	private String name; // useful here? than add get/set
 	
 	public Course() {
 		students = new IPlace[5][7];
 		properties = new LinkedList<String>();
 		properties.add("Tireness");
 		properties.add("Loudness");
 		properties.add("Attention");
 		properties.add("Quality");
 		for (int y=0; y < 5;y++) {
 			for (int x = 0; x < 7;x++) {
 				if(y==3||x==4) {
 					students[y][x] = new EmptyPlace();
 				} else {
 					students[y][x] = new Student(properties);
 //					((Student)students[y][x]).
 				}
 			}
 		}
 		influence = new Influence();
 		historyStates = new HashMap<Integer, IPlace[][]>();
		lecture = new Lecture(new Date());
 	}
 	
 	/**
 	 * adds a new state to the history states
 	 * @param time
 	 * @param currentState
 	 * @author dirk
 	 */
 	public void addHistoryState(int time, IPlace[][] currentState) {
 		historyStates.put(time, currentState);
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
 	public Entry<Integer, IPlace[][]> historyStateInInterval(int start, int end) {
 		Entry<Integer, IPlace[][]> latest = null;
 		for(Entry<Integer, IPlace[][]> historyState : historyStates.entrySet())
 		{
 			if(historyState.getKey()>start && historyState.getKey()<end) {
 				if(latest==null || latest.getKey()<historyState.getKey())
 					latest = historyState;
 			}
 		}
 		return latest;
 	}
 	
 	public void simulationStep(int currentTime, int speed) {
 		// check if there was an interaction from the don
 			Entry<Integer, IPlace[][]> donInteraction = historyStateInInterval(currentTime - speed, currentTime);
 			if (donInteraction != null) {
 				students = donInteraction.getValue();
 			}
 			//the new array for the calculated students
 			IPlace[][] newState = new IPlace[students.length][students[0].length];
 			
 			//student independent calculations
 			CalcVector preChangeVector = new CalcVector(properties);
 			
 			// - - - breakReaction -> inf(Break) * breakInf
 			double breakInf = 0.01;
			if(lecture.getTimeBlocks().getTimeBlockAtTime(currentTime/1000).getType() == BlockType.pause) {
 				preChangeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.BREAK_REACTION, breakInf));
 			}
 			
 			// - - - timeDending -> inf(Time) * currentTime/1000 * timeInf
 			double timeInf = 0.001;
 			double timeTimeInf = timeInf * currentTime / 1000;
 			preChangeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.TIME_DEPENDING, timeTimeInf));
 			
 			// iterate over all students
 			for (int y = 0; y < students.length; y++) {
 				for (int x = 0; x < students[y].length; x++) {
 					Student newStudent = ((Student) students[y][x]).clone();
 					newState[y][x] = (IPlace) newStudent;
 					
 					// calculation
 					CalcVector changeVector = new CalcVector(properties);
 					changeVector.addCalcVector(preChangeVector);
 					
 					// - influence
 					// - - environment
 					// - - - neighbor -> inf(Neighbor) * state(studentLeft) * neighbor + inf(Neighbor) * state(studentRight) * neighbor
 					double neighborInf = 0.0001;
 					if(x>0) {
 						changeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.BREAK_REACTION, neighborInf));
 								//TODO: A function to multiply two vectors (each value with each other)
 								//.multiplyWithMatrix(new Matrix(l, ((Student)students[i][j-1]).getActualState().getValues()));
 					}
 					
 					// - - parameter
 					double parameterInf = 0.001;
 					changeVector.addCalcVector(influence.getInfluencedParameterVector(newStudent.getActualState(), parameterInf));
 					
 					// - usual behavior of the student -> usualBehav * timeInf
 					double behaviorInf = 0.001;
 					CalcVector temp = new CalcVector(properties);
 					newStudent.getActualState().addCalcVector(newStudent.getChangeVector().clone().multiplyWithDouble(behaviorInf));
 					
 					//adds the changes stored in the changeVector to the new student
 					newStudent.getActualState().addCalcVector(changeVector);
 					
 					//time depending
 					//TODO: bring all values to an average value by time
 				}
 			}
 			//give the reference from newState to real students array
 			students = newState;
 	}
 	
 	// --- GETTERS and SETTERS ---
 	
 	public IPlace[][] getStudents() {
 		return students;
 	}
 
 	public void setStudents(IPlace[][] students) {
 		this.students = students;
 	}
 
 	public Influence getInfluence() {
 		return influence;
 	}
 
 	public void setInfluence(Influence influence) {
 		this.influence = influence;
 	}
 
 	public Lecture getLecture() {
 		return lecture;
 	}
 
 	public void setLecture(Lecture lecture) {
 		this.lecture = lecture;
 	}
 
 	public LinkedList<String> getProperties() {
 		return properties;
 	}
 
 	public void setProperties(LinkedList<String> properties) {
 		this.properties = properties;
 	}
 
 }
