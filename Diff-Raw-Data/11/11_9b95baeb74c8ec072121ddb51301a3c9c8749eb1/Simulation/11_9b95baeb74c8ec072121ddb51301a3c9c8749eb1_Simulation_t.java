 package edu.usu.cs.ka.qa.currentsystem.simulator;
 
 import java.util.*;
 import java.math.BigInteger;
 
 import edu.usu.cs.ka.qa.currentsystem.agentsystem.*;
 import edu.usu.cs.ka.qa.currentsystem.utilities.*;
 import edu.usu.cs.pddl.domain.*;
 import edu.usu.cs.pddl.domain.incomplete.*;
 import edu.usu.cs.planner.ffrisky.util.RiskCounter;
 
 
 public class Simulation
 {		
 	Planner planners;
 	DomainExpert expert;
 	Agent agent;
 	
 	static int timeLimit;
 	String resultString;
 	
 	//Results stuff
 	static Long startTime;
 	static Long finishTime;
 		
 	Simulation(String[] args, int simSeed)
 	{	
 		if (args.length != 3) { System.out.println(" " + args.length); usage(args); System.exit(1); }
 		
 		timeLimit = Integer.valueOf(args[2]) * 1000;
 		
 		expert = new DomainExpert(args[0], args[1], simSeed);
 		planners = new Planner(args[0], args[1]);
 
 		resultString = "";	
 	}
 	
 
 	/**
 	 * Choose any planner, as all perform comparably on the classical version of the problem.
 	 * @return boolean is Solvable
 	 */
 	static boolean isSolvableTest;
 	public boolean isSolvableDomain()
 	{	
 		isSolvableTest = true;
 		
 		planners.setProblem(expert.getProblem());
 
 		Random randomGenerator = new Random();
 		int randomInt = randomGenerator.nextInt(3);
 		switch(randomInt)
 		{
			case 0: if(runPlannerThread("amir") == null) 	return false; break;
			case 1: if(runPlannerThread("pode1") == null) 	return false; break;
			case 2: if(runPlannerThread("jdd") == null) 	return false; break;
 		}
 
 		expert.restoreActionsToStateBeforePlannerCall();
 		isSolvableTest = false;
 		
 		return true;
 	}
 	
 	static boolean gotAResult;
 	public static void main(String[] args)
 	{	
 		int numSuccesses = 0;
 		
 		System.out.println();
 		System.out.println("domainFile: " + args[0]);
 		System.out.println("problemFile: " + args[1]);
 		System.out.println("thread timeLimit: " + args[2]);
 		System.out.println("tests startTime: " + startStopwatch());
 		System.out.println();
 
 		for(int simSeed = 0; (simSeed < 1000) && (numSuccesses < 10); simSeed++)
 		{
 			try
 			{
 				Simulation sim = new Simulation(args, simSeed);
 				if(sim.isSolvableDomain())
 				{
 					gotAResult = false;
 					
 					sim.resultString += args[0] + "_" + simSeed + " " + sim.planners.getInitialModelCount() + " RG";
 			
 					try{ sim.runSimulation("amir",  args, "RG"); } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " amir E E E E E";}
 					try{ sim.runSimulation("pode1", args, "RG"); } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " pode1 E E E E E";}
 					try{ sim.runSimulation("jdd", args, "RG");   } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " jdd E E E E E";}
 					
 					sim.resultString += " CL";
 					
 					try{ sim.runSimulation("amir",  args, "CL"); } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " amir E E E E E";}
 					try{ sim.runSimulation("pode1", args, "CL"); } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " pode1 E E E E E";}
 					try{ sim.runSimulation("jdd", args, "CL");   } catch(Exception e){/*e.printStackTrace();*/ sim.resultString += " jdd E E E E E";}
 					
 					if(gotAResult)
 					{
 						System.out.println(sim.resultString);
 						numSuccesses++;
 					}
 				}
 			}catch(Exception e){System.out.println("\nUnhandled Exception"); e.printStackTrace();}
 		}
 		
 		System.out.println();
 		System.out.println("numSuccesses    : " + numSuccesses);
 		System.out.println("tests finishTime: " + stopStopwatch());
 		System.out.println("tests totalTime : " + (finishTime - startTime)/1000.0);
 	}
 	
 	boolean timeout;
 	private void runSimulation(String plannerType, String [] args, String agentType)
 	{	
 		//System.out.println("\n" + plannerType + " " + agentType);
 		
 		boolean endlessLoop = false;
 		        timeout 	= false;
 		
 		if(agentType.equals("RG")) 		
 			agent = new Agent_RG(args[0], args[1]);
 		else if(agentType.equals("CL"))	
 			agent = new Agent_CL(args[0], args[1]);
 		
 		planners.setProblem(agent.getProblem()); //Sets planner's problem to agent's incomplete version.
 		//The planner's problem's actionList auto-updates from Agent to Planner by this reference.
 		
 		planners.resetNumTimesPlannerCalledCount();
 		
 		Set<Proposition> currState, nextState;
 		IncompleteActionInstance currAction;
 		List<ActionInstance> plan;
 
 		agent.startStopwatch();
 		
 		currState = nextState = agent.getProblem().getInitialState();
 		
 		plan = runPlannerThread(plannerType); //plan = planners.getPlan(plannerType); //Should never be null or 0 to start
 		agent.restoreActionsToStateBeforePlannerCall();	
 		while((agent.getNumActionsTaken() < 1000) && (planners.getNumTimesPlannerCalled() < 100) && (plan != null) && (plan.size() != 0))
 		{	
 			boolean actionTaken = false;
 			
 			currAction = (IncompleteActionInstance) plan.remove(0);	
 			if(agent.isActionApplicable(currAction, currState, plan))
 			{	
 				actionTaken = true;
 				
 				nextState = expert.applyAction(currState, currAction);
 				agent.learnAboutActionTaken(currAction, currState, nextState);
 				if(nextState.containsAll(agent.getProblem().getGoalAction().getPreconditions()))
 					break;
 			}
 			
 			if(agent.isActionFailure(currAction, currState, nextState) || plan.size() == 0 || !agent.isActionApplicable(currAction, currState, plan))
 			{	
 				if((agent.isActionFailure(currAction, currState, nextState) && actionTaken) || 
 				   (agent.existsActionFailureInPastEntailFailVar() && !actionTaken))
 						agent.incrementFailedActionsCount();
 				
 				if((agentType.equals("CL")) && (agent.getNumFailedActions() > 0))
 					break;
 				
 				agent.getProblem().setInitialState(nextState);
 				agent.removeFailFromKBForNewPlan();
 				
 				plan = runPlannerThread(plannerType); //plan = planners.getPlan(plannerType);//Note that the problem has been updated within agent
 				
 				if(timeout) 
 					break;
 
 				if(plan == null || plan.size() == 0)
 					break;
 				
 				if(plan.get(0).equals(currAction))
 				{
 					endlessLoop = true;
 					//System.out.print(" @");
 					break;
 				}
 				
 				agent.restoreActionsToStateBeforePlannerCall();
 			}
 			
 			currState = nextState;	
 		}
 				
 		agent.stopStopwatch();
 
 		if(nextState.containsAll(agent.getProblem().getGoalAction().getPreconditions()))
 		{	
 			gotAResult = true;
 			
 			resultString += " " + plannerType + " " + planners.getNumTimesPlannerCalled();
 			resultString += " " + agent.getNumActionsTaken() + " " + agent.getNumFailedActions();
 			resultString += " " + agent.getTimeToSolve() + " " + planners.getFinalModelCount();
 		}
 		else
 		{			
 			if((agent.getNumActionsTaken() == 1000) || (planners.getNumTimesPlannerCalled() == 500))
 				resultString += " " + plannerType + " " + planners.getNumTimesPlannerCalled() + " " + agent.getNumActionsTaken() + " X X X";
 			else if(endlessLoop)
 				resultString += " " + plannerType + " L L L L L";
 			else if(timeout)
 				resultString += " " + plannerType + " T T T T T";
 			else
 				resultString += " " + plannerType + " ? ? ? ? ?";
 		}
 	}
 	
 	
 	/**
 	 * To speed up batch testing, planner is allowed 8x the time used to find the classical version of the problem.
 	 * This is a sweet spot determined by ka/simulator/Test_PlannerRawPerformance.
 	 * 
 	 * @param plannerType
 	 * @return
 	 */
 	private List<ActionInstance> runPlannerThread(String plannerType)
 	{
 		ExecThread execThread = new ExecThread(Thread.currentThread(), planners, plannerType);
 		
 		long start = System.currentTimeMillis();
 		long now = System.currentTimeMillis();
 		execThread.start();
 		
 		int	maxTime = 0;
 		
 		if(isSolvableTest) 	maxTime = timeLimit / 4;
 		else				maxTime = timeLimit * 2;
 
 		while ((now - start) < maxTime)
 		{
 			try { Thread.sleep(500); } catch (Exception e){}
 			now = System.currentTimeMillis();
 			if(execThread.done) 
 				break;
 		}
 		
 		execThread.stop();	
 		if((now - start) >= maxTime)
 			timeout = true;
 
 		return execThread.plan;
 	}
 	
 	class ExecThread extends Thread 
 	{
 		Thread callingThread;
 		public boolean done = false;
 		
 		Planner planner;
 		String plannerType;
 		List<ActionInstance> plan = null;
 		
 		ExecThread(Thread CallingThread, Planner p, String pType)
 		{
 			planner = p;
 			plannerType = pType;
 			callingThread = CallingThread;
 			done = false;
 		}
 		
 		public void run()
 		{			
 			try { plan = planner.getPlan(plannerType); }
 			catch (java.lang.OutOfMemoryError e){}
 			catch (Exception e){}
 			
 			callingThread.interrupt();
 			done = true;
 		}
 	}
 	
 	private static Long startStopwatch(){ startTime = System.currentTimeMillis(); return startTime; }
 	private static Long stopStopwatch() { finishTime = System.currentTimeMillis();return finishTime;}
 	
 	private void usage(String[] args) 
 	{
 		System.err.println("args: " + args[0] + " " + args[1] + " " + args[2]);
 		System.err.println("Simulation_PassiveLearningAgent args:");
 		System.err.println("\t[0]<domain-pddl-file> [1]<problem-pddl-file> [2]<threadLimit>");
 	}
 	
 	/**
 	 * Used to show that planners change action descriptions for parcprinter and pathways domains.
 	 * @param when
 	 */
 	public void badActionMaintenenceChecker(String when)
 	{
 		List<ActionInstance> expertHTlist  = Actions_Utility.getListFrom_Int_IAI_HT(expert.actionsCV_HT);		
 		List<ActionInstance> agentHTList = Actions_Utility.getListFrom_String_IAI_HT(agent.actionsHT);
 		
 		System.out.println(when);
 		System.out.print("Agent           List: "); Actions_Utility.showActionsCountPADAndPossPADs(agent.getActions());
 		System.out.print("Agent             HT: "); Actions_Utility.showActionsCountPADAndPossPADs(agentHTList);
 		System.out.print("Agent   problem List: "); Actions_Utility.showActionsCountPADAndPossPADs(agent.getProblem().getActions());
 		System.out.print("Expert          List: "); Actions_Utility.showActionsCountPADAndPossPADs(expert.getActions());
 		System.out.print("Expert            HT: "); Actions_Utility.showActionsCountPADAndPossPADs(expertHTlist);
 		System.out.print("Expert  problem List: "); Actions_Utility.showActionsCountPADAndPossPADs(expert.getProblem().getActions());
 		System.out.print("Planner problem List: "); Actions_Utility.showActionsCountPADAndPossPADs(planners.getProblem().getActions());
 		System.out.println();
 	}
 }
