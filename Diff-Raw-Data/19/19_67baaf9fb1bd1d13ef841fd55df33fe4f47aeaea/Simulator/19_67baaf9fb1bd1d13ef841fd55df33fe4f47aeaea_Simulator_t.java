 package edu.usu.cs.incompletecompleteagentsim;
 
 import java.util.*;
 
 
 import edu.usu.cs.pddl.domain.incomplete.IncompleteActionInstance;
 import edu.usu.cs.pddl.domain.incomplete.Proposition;
 import edu.usu.cs.planner.ffrisky.util.PddlImporter;
 import edu.usu.cs.pddl.domain.incomplete.*;
 import edu.usu.cs.pddl.goalseffects.*;
 import edu.usu.cs.pddl.domain.translation.*;
 import edu.usu.cs.pddl.domain.*;
 import edu.usu.cs.pddl.domain.incomplete.*;
 import edu.usu.cs.planner.ffrisky.util.PddlImporter;
 import edu.usu.cs.search.*;
 
 public class Simulator 
 {
 	boolean debug;
 	
 	private static Random random;
 	double probability;
 	
 	List<ActionInstance> actionInstances;
 	Hashtable<Integer, IncompleteActionInstance> incompleteActionInstances_completeVersion_Hashtable;
 	
 	Simulator(List<ActionInstance> actionInstancesOfAgent, int seed)
 	{
 		debug = true;
 		
 		random = new Random(seed);
 		probability = .5;
 		
 		System.out.println("\n-----------------------------------------------------");
 		System.out.println("IN SIMULATOR CONSTRUCTOR\n");
 		System.out.println("seed: " + seed);
 		System.out.println("probability:" + probability);
 		
 		createActionsWithNoPossiblesByProbabilityAndSeed(actionInstancesOfAgent);
 		
 		System.out.println("\n-----------------------------------------------------");
 		System.out.println("SIMULATOR ACTIONS AVAILABLE (no possibles): ");
 		
 		incompleteActionInstances_completeVersion_Hashtable = new Hashtable<Integer, IncompleteActionInstance>();
 		for(ActionInstance act: actionInstances)
 		{
 			IncompleteActionInstance a = (IncompleteActionInstance) act;
 			incompleteActionInstances_completeVersion_Hashtable.put(a.getIndex(), a);
 			
 			System.out.println();
 			Agent.printIncompleteVersionOfActionInstance(a);
 		}
 		
 		System.out.println("\nEND - SIMULATOR ACTIONS AVAILABLE");
 		System.out.println("-----------------------------------------------------");
 		
 		System.out.println("\nLEAVING SIMULATOR CONSTRUCTOR...");
 		System.out.println("-----------------------------------------------------\n");
 	}
 	
 	public List<ActionInstance> getActionInstances(){ return actionInstances; }
 	
 	public Set<Proposition> updateState(Set<Proposition> currentState, IncompleteActionInstance incompleteActionChosen)
 	{
 		System.out.println("\n----------------------------------------------------------------");
 		System.out.println("SIM updates state based on agents action using its enhanced info");
 		System.out.println(" about pres and effects. The updated state shows the changes.");
 		System.out.println(" Agent will have to discover which of the possible pres/effects");
 		System.out.println(" for each of his actions are real for this particular domain.");
 		System.out.println(" Sim DOES NOT tell Agent that the action was a success/failure.");
 		System.out.println(" (Note: an accepted action might not change state -");
 		System.out.println("        consider the same action taken twice in a row or");
 		System.out.println("        actions whose add effects already exist and");
 		System.out.println("        whose delete effects already don't exist...\n");
 		
 		Set<Proposition> newState = new HashSet<Proposition>(currentState);
 		
 		IncompleteActionInstance a = incompleteActionInstances_completeVersion_Hashtable.get(incompleteActionChosen.getIndex());			
 		Agent.printIncompleteVersionOfActionInstance(a);
 				
 		if(currentState.containsAll(a.getPreconditions()))
 		{
 			newState.removeAll(a.getDeleteEffects());
 			newState.addAll(a.getAddEffects());
 		}
 		
 		System.out.println("----------------------------------------------------------------\n");
 		return newState;
 	}
 	
 	public boolean giveFeedbackForQuestion (QA_Learning.QA_ActionAndPropChoice actionWithQ)
 	{
 		System.out.println("\n----------------------------------------------------------------");
 		System.out.println("SIM gives feedback about an action selected by agent");
 		System.out.println(" that contains possibles.");
 		System.out.println(" This feedback will tell the agent whether or not a particular");
 		System.out.println(" possible is in fact an actual precondition or add/delete effect.\n");
 		
 		
 		IncompleteActionInstance a = null;
 		
 		a = incompleteActionInstances_completeVersion_Hashtable.get(actionWithQ.action.getIndex());
 		
 		if(a == null)
 		{
 			System.out.println("ERROR: Sim did not find complete version of action: " +  actionWithQ.action.getName());
 			System.out.println("THIS SHOULD NEVER HAPPEN!");
 			return false;
 		}
 		else
 			Agent.printIncompleteVersionOfActionInstance(a);
 			
 		//An incomplete action has 6 lists.  
 		//The known pres/adds/deletes are 1-3.
 		//The possible pre's/add/deletes are 4-6.
 		//See Agent constants for each list.
 		//The agent has told the sim which possibles list the prop came from.
 		//The sim looks up the prop in its appropriate knowns list.
 		int listToCheck = actionWithQ.listOriginOfProp - 3; 
 		
 		boolean isFound = false;
 		switch(listToCheck)
 		{
 			case (Agent.KNOWNPRECONDITIONSLIST):
 				if(a.getPreconditions().contains(actionWithQ.propToLearnAbout))
 				{
 					System.out.println("Sim found: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's preconditions list.");
 					isFound = true;
 				}
 				else
 				{
 					System.out.println("Sim did not find: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's preconditions list.");
 				}
 				break;
 			case (Agent.KNOWNADDEFFECTSLIST):
 				if(a.getAddEffects().contains(actionWithQ.propToLearnAbout))
 				{
 					System.out.println("Sim found: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's add effects list.");
 					isFound = true;
 				}
 				else
 				{
 					System.out.println("Sim did not find: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's add effects list.");
 				}
 				break;
 			case (Agent.KNOWNDELETEEFFECTSLIST):
 				if(a.getDeleteEffects().contains(actionWithQ.propToLearnAbout))
 				{
 					System.out.println("Sim found: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's delete effects list.");
 					isFound = true;
 				}
 				else
 				{
 					System.out.println("Sim did not find: " + actionWithQ.propToLearnAbout);
 					System.out.println(" in the complete action version's delete effects list.");
 				}
 				break;
 			default:
 				System.out.println("ERROR: Sim did not find right list.");
 				System.out.println(" THIS SHOULD NEVER HAPPEN!");
 		}
 				
 		System.out.println("----------------------------------------------------------------\n");
 		
 		return isFound;
 	}
 	
 	public void printIncompleteVersionOfActionInstances()
 	{
 		for(ActionInstance action : actionInstances)
 		{
 			IncompleteActionInstance a = (IncompleteActionInstance) action;
 			printIncompleteVersionOfActionInstance(a);
 		}
 	}
 	
 	public void printIncompleteVersionOfActionInstance(IncompleteActionInstance a)
 	{
 		System.out.println("  Name        : " + a.getName());					//String
 		System.out.println("  Index       : " + a.getIndex());					//int
 		System.out.println("  Pres        : " + a.getPreconditions());			//Set<Proposition>
 		System.out.println("  Poss Pres   : " + a.getPossiblePreconditions());	//Set<Proposition>
 		System.out.println("  Adds        : " + a.getAddEffects());				//Set<Proposition>
 		System.out.println("  Poss Adds   : " + a.getPossibleAddEffects());		//Set<Proposition>
 		System.out.println("  Deletes     : " + a.getDeleteEffects());			//Set<Proposition>
 		System.out.println("  Poss Dels   : " + a.getPossibleDeleteEffects());	//Set<Proposition>
 		System.out.println("   ActionRisks: " + a.getActionRisks());				//int
 		System.out.println("   ArgMapping : " + a.getArgMapping());				//Map<FormalArgument, PDDLObject>
 		System.out.println("   Cost       : " + a.getCost());				    //double
 		System.out.println("   Definition : " + a.getDefinition());				//ActionDef
 		System.out.println();
 	}
 	
 	private void createActionsWithNoPossiblesByProbabilityAndSeed(List<ActionInstance> actionInstancesOfAgent)
 	{
 		actionInstances = new ArrayList<ActionInstance>();
 		
 		HashSet<Proposition> newPreconditionsSet;
 		HashSet<Proposition> newAddEffectsSet;
 		HashSet<Proposition> newDeleteEffectsSet;
 		HashSet<Proposition> newEmptySet;
 	
 		for(ActionInstance a : actionInstancesOfAgent)
 		{
 			IncompleteActionInstance ia = (IncompleteActionInstance) a;
 			
 			System.out.println("\n----------------------------------------------------------------");
 			System.out.println("CREATING SIM (COMPLETE) VERSION OF AGENT'S ACTION:");
 			System.out.println("\nOriginal incomplete action: ");
 			printIncompleteVersionOfActionInstance(ia);
 			
 			newPreconditionsSet = new HashSet<Proposition>(ia.getPreconditions());
 			newAddEffectsSet 	= new HashSet<Proposition>(ia.getAddEffects());
 			newDeleteEffectsSet = new HashSet<Proposition>(ia.getDeleteEffects());
 			newEmptySet 		= new HashSet<Proposition>();
 			
 			//Preconditions
 			for(Proposition p : ia.getPossiblePreconditions())
 			{			
 				//if the rand gen yield a double higher than the probability, then add this
 				//possible as a real precondition, else discard
 				Double nextDouble = random.nextDouble();
 				if(debug) System.out.println("nextRandDouble: " +  nextDouble);
 				if(nextDouble > probability) 
 				{
 					newPreconditionsSet.add(p);
 					if(debug) System.out.println(" " + p + " added to known pre's.");
 				}
 				else
 					if(debug) System.out.println(" " + p +" not added to known pre's.");
 			}
 			
 			//Adds
 			for(Proposition p : ia.getPossibleAddEffects())
 			{			
 				//if the rand gen yield a double higher than the probability, then add this
 				//possible as a real add effect, else discard
 				Double nextDouble = random.nextDouble();
 				if(debug) System.out.println("nextRandDouble: " +  nextDouble);
 				if(nextDouble > probability) 
 				{
 					newAddEffectsSet.add(p);
 					if(debug) System.out.println(" " + p + " added to known addEffects.");
 				}
 				else
 					if(debug) System.out.println(" " + p +" not added to known addEffects.");
 			}
 			
 			//Deletes
 			for(Proposition p : ia.getPossibleDeleteEffects())
 			{			
 				//if the rand gen yield a double higher than the probability, then add this
 				//possible as a real delete effect, else discard
 				Double nextDouble = random.nextDouble();
 				if(debug) System.out.println("nextRandDouble: " +  nextDouble);
 				if(nextDouble > probability) 
 				{
 					newDeleteEffectsSet.add(p);
 					if(debug) System.out.println(" " + p + " added to known deleteEffects.");
 				}
 				else
 					if(debug) System.out.println(" " + p +" not added to known deleteEffects.");
 			}
 			
 			//Create a new actionInstance with above changes and stick it into sim's actions list
 			//String name, Set<Proposition> preconditions,
 			//Set<Proposition> addEffects, Set<Proposition> deleteEffects,
 			//Set<Proposition> possiblePreconditions,
 			//Set<Proposition> possiblePositiveEffects,
 			//Set<Proposition> possibleNegativeEffects,
 			//int index)
 			IncompleteActionInstance newIA = new IncompleteActionInstance(ia.getName(), newPreconditionsSet,
 					newAddEffectsSet, newDeleteEffectsSet, newEmptySet, newEmptySet, newEmptySet, ia.getIndex());
 			actionInstances.add(newIA);
 			
 			System.out.println("\nNew complete version of action for sim: ");
 			printIncompleteVersionOfActionInstance(newIA);
 			
 			System.out.println("END - CREATING SIM (COMPLETE) VERSION OF AGENT'S ACTION:");
 			System.out.println("----------------------------------------------------------------");	
 		}			
 	}
 }
