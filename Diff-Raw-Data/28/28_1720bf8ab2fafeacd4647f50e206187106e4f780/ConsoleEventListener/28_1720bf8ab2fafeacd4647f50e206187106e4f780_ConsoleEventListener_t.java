 package com.mutation.events.listener;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.mutation.Mutant;
import com.mutation.events.ClassesUnderTestResolved;
 import com.mutation.events.DriverFinished;
 import com.mutation.events.DriverStarted;
 import com.mutation.events.IEvent;
 import com.mutation.events.IEventListener;
 import com.mutation.events.MutantsGenerated;
 import com.mutation.events.TestsExecuted;
 import com.mutation.util.StopWatch;
 
 public class ConsoleEventListener  implements IEventListener {
 	int numSurvivers;
 	int numKilled;
	int numClassesUnderTest;
 	Set<Mutant> totalMutants = new HashSet<Mutant>();
 	Set<Mutant> suvivers = new HashSet<Mutant>();
 	private StopWatch watch = new StopWatch();
 	public void notifyEvent(IEvent event) {
 		System.out.println(new Date() +": "  +event);
 		if(event instanceof DriverStarted){
 			watch.start();
 		}else if(event instanceof DriverFinished){
 			double elapsed = watch.stop();
 			System.out.println("# Finished. total time: " +elapsed/1000 +" sec.");
			System.out.println("# numClassesUnderTest: " +numClassesUnderTest);
 			System.out.println("# numSurvivers: " +numSurvivers +" numKilled: " +numKilled);
 			System.out.println("# numSurvivers: " +suvivers.size() +" totalNumMutants: " +totalMutants.size());
 		}else if(event instanceof TestsExecuted){
 			TestsExecuted te = (TestsExecuted) event;
 			if(te.isMutantSurvived()){
 				numSurvivers ++;
 			}else{
 				suvivers.remove(te.getMutant());
 				numKilled++;
 			}
 		}else if(event instanceof MutantsGenerated){
 			MutantsGenerated e = (MutantsGenerated)event;
 			totalMutants.addAll(e.getGeneratedMutants());
 			suvivers.addAll(e.getGeneratedMutants());
		}else if(event instanceof ClassesUnderTestResolved){
			ClassesUnderTestResolved e = (ClassesUnderTestResolved)event;
			numClassesUnderTest = e.getClassUnderTestNames().size();
 		}
 	}
 	public void destroy() throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
