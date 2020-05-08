 package edu.usu.cs.incompletecompleteagentsim;
 
 import edu.usu.cs.pddl.goalseffects.*;
 import edu.usu.cs.pddl.domain.translation.*;
 import edu.usu.cs.pddl.domain.*;
 import edu.usu.cs.pddl.domain.incomplete.*;
 import edu.usu.cs.planner.ffrisky.util.PddlImporter;
import edu.usu.cs.search.astar.*;
 import edu.usu.cs.search.*;
 
 import java.util.*;
 
 public class RandomQAandExplorationLearningAgentSimTrial 
 {
 	
 	Domain incompleteDomain_agent;
 	Domain completeDomain_simulator;
 	Problem problem;
 	
 	String[] args2;
 	RandomQAandExplorationLearningAgentSimTrial(String[] args)
 	{
 		if (args.length != 2)
 			usage();
 		
 		Double probability = 1.0;
 		Integer seed = 0;
 		
 		args2 = new String[2];
 		args2[0] = args[0];
 		args2[1] = args[1];
 				
 		DomainAndProblemMaker domainMaker = new DomainAndProblemMaker(args2);
 		
 		incompleteDomain_agent = domainMaker.getOriginalIncompleteDomain();
 		//completeDomain_simulator = domainMaker.getModifiedCompleteDomain();
 		problem = domainMaker.getProblem();
 		
 		//TO CONFIRM THE DOMAINS AND PROBLEMS INITIALLY CREATED
 		//printDomainsAndProblem();
 	}
 	
 	void printDomainsAndProblem()
 	{		
 		Set<Proposition> currentState = problem.getInitialState();
 		System.out.println("************************************************************************");
 		System.out.println("BEGIN - INITIAL STATE:\n");
 		for (Proposition p : currentState)
 			System.out.print(p + " ");
 		System.out.println("\n\nEND - INITIAL STATE");
 		System.out.println("************************************************************************\n");
 		
 		System.out.println("************************************************************************");
 		System.out.println("BEGIN - INCOMPLETEACTION INSTANCE - GOAL ACTION:\n");
 		Agent.printIncompleteVersionOfActionInstance(problem.getGoalAction());
 		System.out.println("\nTHUS, GOAL STATE INCLUDES: " + problem.getGoalAction().getPreconditions());
 		System.out.println("\nEND - INCOMPLETEACTION INSTANCE - GOAL ACTION:\n");
 		System.out.println("************************************************************************\n");	
 	}
 	
 	public static void main (String[] args)
 	{
 		RandomQAandExplorationLearningAgentSimTrial env = new RandomQAandExplorationLearningAgentSimTrial(args);
 		
 		//TO CONFIRM THE DOMAINS AND PROBLEMS INITIALLY CREATED
 		//env.printDomainsAndProblem();
 		
 		//Random QA/Exploration Agent/Sim Trial using domain, problem, seed, and probability given.
 		env.randomQAAndExplorationAgentSimTrialOverGivenDomainAndProblem();
 	}
 	
 	public void randomQAAndExplorationAgentSimTrialOverGivenDomainAndProblem()
 	{
 		System.out.println("************************************************************************");
 		System.out.println("BEGIN - SIM-AGENT INTERACTION\n");
 		//Could send in the rand seed - currently 0 within these classes
 		Agent agent = new Agent(incompleteDomain_agent, problem);
 		Simulator sim = new Simulator(agent.getIncompleteActionInstancesAsActionInstances(), Integer.valueOf(args2[3]));
 
 		Set<Proposition> currentState = problem.getInitialState();
 		System.out.println("\n----------------------------------");
 		System.out.println("INITIAL STATE:       " + currentState);
 		System.out.println("GOAL STATE INCLUDES: " + problem.getGoalAction().getPreconditions());
 		System.out.println("----------------------------------");
 		
 		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		//THE INTERACTION BETWEEN SIM/AGENT
 		//the case where there is no action available from current state is uncaught 
 		//for this random sim/agent interaction
 		int loop = 1;
 		agent.startStopwatch();
 		while(!currentState.containsAll(problem.getGoalAction().getPreconditions()))
 		{
 			System.out.println("\n----------------------------------------------------------------");
 			System.out.println("LOOP ITERATION #: " + (loop++));
 			
 			Set<Proposition> newState;
 			if(agent.selectTypeOfLearning().equals(Agent.LearningType.EXPLORATION))
 			{
 				//Learning by Exploration
 				IncompleteActionInstance incompleteActionChosen = agent.explore_side.chooseAction_Exploration(currentState);
 				newState = sim.updateState(currentState, incompleteActionChosen);
 				agent.explore_side.learnAboutActionTaken_Exploration(newState, currentState, incompleteActionChosen);
 				currentState = newState;
 			}
 			else
 			{
 				//Learning by QA
 				QA_Learning.QA_ActionAndPropChoice agentsActionAndPropChoice = agent.qa_side.chooseIncompleteActionAndProp_QA();
 				boolean isPossiblePropActual = sim.giveFeedbackForQuestion(agentsActionAndPropChoice);
 				agent.qa_side.incorporateKnowledgeGained_QA(isPossiblePropActual, agentsActionAndPropChoice);
 			}
 			
 			System.out.println("\nPRESS ENTER TO CONTINUE...");
 		    Scanner sc = new Scanner(System.in);
 		       while(!sc.hasNextLine());
 		    
 		    System.out.println("----------------------------------------------------------------");
 		    System.out.println("CURRENT STATE:       " + currentState);
 		    System.out.println("GOAL STATE INCLUDES: " + problem.getGoalAction().getPreconditions());
 		    System.out.println("----------------------------------------------------------------");
 		}
 		agent.stopStopwatch();
 		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		
 		//FINAL RESULTS
 		System.out.println("\n----------------------------------");
 		System.out.println("GOAL ACHIEVED:           " + problem.getGoalAction().getPreconditions());
 		System.out.println("EXISTS IN CURRENT STATE: " + currentState.toString());
 		System.out.println("----------------------------------");
 		
 		System.out.println("\nEND - SIM-AGENT INTERACTION");
 		System.out.println("************************************************************************\n");
 		
 		System.out.println("************************************************************************");
 		System.out.println("FINAL RESULTS\n");
 		agent.printDeepResults();
 		System.out.println("\nEND - FINAL RESULTS");
 		System.out.println("************************************************************************\n");
 	}
 	
 	private void usage() 
 	{
 		System.err.println("SimulatorAgentEnvironment usage:");
 		System.err.println("\t<domain-pddl-file> <problem-pddl-file>");
 		System.err.println("\t FUTURE: <probability(double)> <seed(int)>");
 		System.err.println("\t CURRENT: set to 1.0, 0.");
 		System.exit(1);
 	}
 }
