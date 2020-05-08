 package nruth.fingar.tests;
 
 import static org.junit.Assert.*;
 import static nruth.fingar.domain.NamedNote.*;
 import java.util.*;
 
 import nruth.fingar.Arrangement;
 import nruth.fingar.FINGAR;
 import nruth.fingar.domain.*;
 
 import org.junit.*;
 
 import sun.security.util.PendingException;
 
 
 /**
  * High level GA prototype feature testing will go here
  * @author nicholasrutherford
  *
  */
 public class MonophonicScales {
 	/**
 	 * given a C major scale
 	 * process alternative arrangements
 	 * and return a ranked list of the best solutions
 	 */
 	
 	/**
 	 * check known solution is included
 	 */
 	@Test
 	public void c_major_scale_alternatives(){
 		Score c_major_scale = c_major_scale();
 		
 		//process alternatives		
 		//and check the list contains a known solution
 		List<Arrangement> results = FINGAR.getArrangementsFor(c_major_scale);
 	}
 	
 	/**
	 * check soultions are ranked
 	 */
 	@Test
 	public void are_solutions_ranked(){
 		fail("not implemented");
 	}
 	
 	public Score c_major_scale(){
 		LinkedList<Note> notes = new LinkedList<Note>();
 		float[][] timing = new float[8][1];
 		int time = 0;
 		for(NamedNote name : new NamedNote[]{C,D,E,F,G}){
 			notes.add(new Note(name, 1));
 			timing[time] = new float[]{time++, 1f};
 		}
 		for(NamedNote name : new NamedNote[]{A,B,C}){
 			notes.add(new Note(name, 2));
 			timing[time] = new float[]{time++, 1f};
 		}
 		
 		return new Score(notes.toArray(new Note[notes.size()]), timing);
 	}
 }
