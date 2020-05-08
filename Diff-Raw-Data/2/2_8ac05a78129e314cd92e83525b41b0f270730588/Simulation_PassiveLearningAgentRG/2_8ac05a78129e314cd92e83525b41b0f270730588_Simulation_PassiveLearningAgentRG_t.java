 package edu.usu.cs.ka.currentsystem.simulator;
 
 import java.util.*;
 import java.math.BigInteger;
 
 import edu.usu.cs.ka.currentsystem.agentsystem.*;
 import edu.usu.cs.ka.currentsystem.utilities.*;
 import edu.usu.cs.pddl.domain.*;
 import edu.usu.cs.pddl.domain.incomplete.*;
 
 
 public class Simulation_PassiveLearningAgentRG
 {	
 	public static int numSuccesses = 0;
 	
 	Planner planners;
 	DomainExpert expert;
 	Agent_RG agent;
 		
 	Simulation_PassiveLearningAgentRG(String[] args)
 	{	
 		if (args.length !=3) { usage(args); System.exit(1); }
 		
 		planners = new Planner(args[0], args[1]);
 		expert = new DomainExpert(args[0], args[1], args[2]);
 	
 		planners.setProblem(expert.getProblem());
 	}
 	
 	public boolean isSolvableDomain()
 	{
 		planners.setProblem(expert.getProblem());
 		if(planners.getPlan("amir") == null) return false;
 		if(planners.getPlan("pode1") == null) return false;
 		
 		numSuccesses++;
 		return true;
 	}
 	
 	public static void main(String[] args)
 	{	
 		Simulation_PassiveLearningAgentRG sim = new Simulation_PassiveLearningAgentRG(args);
 		
	if(!sim.isSolvableDomain()) return;
 		
 		System.out.print(args[0] + "_" + args[2] + " " + sim.planners.getInitialModelCount());
 
 		sim.runSimPassiveLearning_RG("amir",  args, true, false);
 		sim.runSimPassiveLearning_RG("pode1", args, true, false);
 		sim.runSimPassiveLearning_RG("jdd", args, true, false);
 		
 		System.out.println();
 	}
 		
 	private void runSimPassiveLearning_RG(String plannerType, String [] args, boolean isForgiving, boolean isAssumptive)
 	{
 		agent = new Agent_RG(args[0], args[1]);
 		planners.setProblem(agent.getProblem()); //Sets planner's problem to agent's incomplete version.
 		//The planner's problem's actionList auto-updates from Agent to Planner by this reference.
 		planners.resetNumTimesPlannerCalledCount();
 		
 		Set<Proposition> currState, nextState;
 		IncompleteActionInstance currAction;
 		List<ActionInstance> plan;
 		int numFailedActions = 0;
 
 		agent.startStopwatch();
 		
 		currState = nextState = agent.getProblem().getInitialState();
 		
 		plan = planners.getPlan(plannerType); //Should never be null to start
 		
 		while((agent.getNumActionsTaken() < 1000) && (planners.getNumTimesPlannerCalled() < 100))
 		{			
 			currAction = (IncompleteActionInstance) plan.remove(0);
 			
 			if(agent.isActionApplicable(currAction, currState, plan))
 			{		
 				nextState = expert.applyAction(currState, currAction);
 				
 				agent.learnAboutActionTaken(currAction, currState, nextState);
 							
 				if(nextState.containsAll(agent.getProblem().getGoalAction().getPreconditions()))
 					break;
 			}
 			
 			if(agent.isActionFailure(currAction, currState, nextState) || plan.size() == 0 || !agent.isActionApplicable(currAction, currState, plan))
 			{	
 				if(agent.isActionFailure(currAction, currState, nextState)) 
 					numFailedActions++;
 				
 				agent.getProblem().setInitialState(nextState);
 				agent.removeFailFromKBForNewPlan();
 				
 				plan = planners.getPlan(plannerType);//Note that the problem has been updated within agent								
 				if(plan == null || plan.size() == 0) break;
 			}
 			
 			currState = nextState;		
 		}
 				
 		agent.stopStopwatch();
 
 		if(nextState.containsAll(agent.getProblem().getGoalAction().getPreconditions()))
 		{	
 			System.out.print(" " + plannerType + " " + planners.getNumTimesPlannerCalled());
 			System.out.print(" " + agent.getNumActionsTaken() + " " + numFailedActions);
 			System.out.print(" " + agent.getTimeToSolve() + " " + planners.getFinalModelCount());
 		}
 		else
 		{			
 			if((agent.getNumActionsTaken() == 1000) || (planners.getNumTimesPlannerCalled() == 500))
 				System.out.print(" " + plannerType + " " + planners.getNumTimesPlannerCalled() + " " + agent.getNumActionsTaken() + " X X X");
 			else
 				System.out.print(" " + plannerType + " ? ? ? ? ?");
 		}
 	}
 	
 	private void usage(String[] args) 
 	{
 		System.err.println("args: " + args.toString());
 		System.err.println("Simulation_TestAgentAndDomainExpertStub args:");
 		System.err.println("\t[0]<domain-pddl-file> [1]<problem-pddl-file> [2]<simSeed>");
 	}
 }
