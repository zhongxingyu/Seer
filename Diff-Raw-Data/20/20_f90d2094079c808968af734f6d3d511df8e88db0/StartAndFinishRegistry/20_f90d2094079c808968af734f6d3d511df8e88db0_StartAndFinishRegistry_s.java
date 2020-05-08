 package org.eclipse.uml2.diagram.sequence.model.builder;
 
 import java.util.HashMap;
 
 import org.eclipse.uml2.uml.ExecutionSpecification;
 import org.eclipse.uml2.uml.Interaction;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.OccurrenceSpecification;
 
 
 public class StartAndFinishRegistry {
 	private HashMap<OccurrenceSpecification, ExecutionSpecification> myStarts;
 	private HashMap<OccurrenceSpecification, ExecutionSpecification> myFinishes;
 	private final Interaction myInteraction;
 
 	public StartAndFinishRegistry(Interaction interaction){
 		myInteraction = interaction;
 	}
 
 	public void forceRemap(){
 		myStarts = null;
 		myFinishes = null;
 	}
 	
 	public ExecutionSpecification findStartedExecution(OccurrenceSpecification occurrence){
 		if (myStarts == null || myFinishes == null){
 			doRemap();
 		}
 		return myStarts.get(occurrence);
 	}
 	
 	public ExecutionSpecification findFinishedExecution(OccurrenceSpecification occurrence){
 		if (myStarts == null || myFinishes == null){
 			doRemap();
 		}
 		return myFinishes.get(occurrence);
 	}
 	
 	public ExecutionSpecification findExecution(OccurrenceSpecification occurrence){
 		if (myStarts == null || myFinishes == null){
 			doRemap();
 		}
 		ExecutionSpecification startedAt = myStarts.get(occurrence);
 		if (startedAt != null){
 			return startedAt;
 		}
 		return (startedAt != null) ? startedAt : myFinishes.get(occurrence);
 	}
 
 	private void doRemap(){
 		myStarts = new HashMap<OccurrenceSpecification, ExecutionSpecification>();
 		myFinishes = new HashMap<OccurrenceSpecification, ExecutionSpecification>();
		for (InteractionFragment next : myInteraction.getFragments()){
 			if (next instanceof ExecutionSpecification){
 				ExecutionSpecification nextExecution = (ExecutionSpecification)next;
 				checkDistinctEnds(nextExecution);
 				
 				OccurrenceSpecification start = nextExecution.getStart();
 				OccurrenceSpecification finish = nextExecution.getFinish();
 
 				checkTwoOwnersSameKind(start, nextExecution, myStarts.get(start), true);
 				checkTwoOwnersSameKind(finish, nextExecution, myFinishes.get(finish), false);
 				checkTwoOwnersDifferentKinds(start, nextExecution, myFinishes.get(start));
 				checkTwoOwnersDifferentKinds(finish, myStarts.get(finish), nextExecution);
 				
 				myStarts.put(start, nextExecution);
 				myFinishes.put(finish, nextExecution);
 			}
 		}
 	}
 
 	private void checkDistinctEnds(ExecutionSpecification execution) {
 		if (execution.getStart() == null){
 			throw new IllegalStateException("Execution without a start: " + execution);
 		}
 		if (execution.getFinish() == null){
 			throw new IllegalStateException("Execution without a finish: " + execution);
 		}
 		if (execution.getStart() == execution.getFinish()){
 			throw new IllegalStateException("The same start and finish found for execution : " + execution);
 		}
 	}
 
 	private void checkTwoOwnersSameKind(OccurrenceSpecification problem, ExecutionSpecification firstOwner, ExecutionSpecification secondOwner, boolean bothStartsNotFinishes){
 		if (secondOwner != null && secondOwner != firstOwner){
 			throw new IllegalStateException(//
 				"OccurrenceSpecification: " + problem + " is " + (bothStartsNotFinishes ? "start" : "finish") + " for more than one Executions: " + firstOwner + ", and: " + secondOwner);
 		}
 	}
 	
 	private void checkTwoOwnersDifferentKinds(OccurrenceSpecification problem, ExecutionSpecification knownStart, ExecutionSpecification knownFinish){
 		if (knownStart != null && knownFinish != null){
 			throw new IllegalStateException(//
 					"OccurrenceSpecification: " + problem + " is start for : " + knownStart + ", but finish for: " + knownFinish);
 		}
 	}
 
 
 
 
 }
