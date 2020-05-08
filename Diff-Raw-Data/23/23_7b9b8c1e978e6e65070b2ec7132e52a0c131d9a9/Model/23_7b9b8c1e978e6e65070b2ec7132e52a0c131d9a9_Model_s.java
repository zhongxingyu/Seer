 package model;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 
 import model.steps.*;
 
 
 public class Model extends Observable 
 {	
 	/**
 	 * Returns all events considering the data which is already asked
 	 */
 	public HashMap<String, String>[] getEvents()
 	{	
 		return (new EventCollector()).getEvents();
 	}
 	
 	/**
 	 * Returns the current question order belonging to the data already asked
 	 */
 	public InformationGatherStepModel[] getInformationGatherTrace()
 	{
 		return (new TraceBuilder()).build();
 	}
 	
 	/**
 	 * Returns the next Step which needs to be asked in the current question order
 	 */
 	public InformationGatherStepModel getNextStep()
 	{
 		InformationGatherStepModel[] steps = getInformationGatherTrace();
 		InformationGatherStepModel result = null;
 		for(int i=0; i<steps.length; i++) {
 			if(steps[i].isAlredyFilled() == false) {
 				result = steps[i];
 				break;
 			}
 		}
 		return result;
 	}
 
 }
