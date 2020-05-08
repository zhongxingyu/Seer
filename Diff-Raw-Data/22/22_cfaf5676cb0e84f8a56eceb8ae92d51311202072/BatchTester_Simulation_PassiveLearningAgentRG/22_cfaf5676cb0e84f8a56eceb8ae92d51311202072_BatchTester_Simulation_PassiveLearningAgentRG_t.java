 package edu.usu.cs.ka.currentsystem.batchtesters;
 
 import edu.usu.cs.ka.currentsystem.simulator.Simulation_PassiveLearningAgentRG;
 
 public class BatchTester_Simulation_PassiveLearningAgentRG 
 {
 	private static final int numFiles = 10;
 
 	public static void main (String[] args)
 	{				
 		int maxGridSize = 16;
 		args = new String[3];
 		String pathToDomains = "testfiles/incomplete/bridges/";
 		String pathToProblems = "testfiles/incomplete/bridges/";
 		String extension = ".pddl";
 	
 		System.out.println("<domainFile_simSeed> <initial#Models> (3x:) <planner> <#TimesPlannerCalled> <Total#Actions> <#failedActions> <totalTime> <final#Models>");
 		System.out.println("*: s = s', S v F case.");
 		System.out.println("#: NullPointerException in AbstractPlanningGraph.\n");
 		
 		//Just mess around with the for loop values...
 		for(int gridSize = 2; gridSize <= 8; gridSize*=2) //2-32
 		{
 			for(double bridgeDensity = 0.0; bridgeDensity <= 1.0; bridgeDensity += 0.25) //0.0 - 1.0
 			{	
 				for(int version = 1; version <= 3; version++)//1-3
 				{
					for(int numFile = 1; numFile <= 1; numFile++) //numFiles 1-10
 					{
						for(int simSeed = 0; simSeed < 10000 && Simulation_PassiveLearningAgentRG.numSuccesses < 1; simSeed++)
 						{	
 							args[0] = pathToDomains + "bridges_v" + version + "_" + gridSize + "_" + bridgeDensity + "_" + numFile + extension;
 							args[1] = pathToProblems + "bridges_problem" + extension;
 							args[2] = String.valueOf(simSeed);
 							
 							try{ Simulation_PassiveLearningAgentRG.main(args); }
 							catch(Error e){ System.out.println("\nError\n"); e.printStackTrace(); }
 							
 							System.runFinalization();
 							System.gc();
 						}//end for seed
 						
 						if(Simulation_PassiveLearningAgentRG.numSuccesses == 0)
 							System.out.println(args[0]);
 						else
 							Simulation_PassiveLearningAgentRG.numSuccesses = 0;
 						
 						System.out.println();	
 					}//end for num file
 					System.out.println();
 				}//end for bridge density
 			}//end for version
 		}//end for size
 		
 		System.out.println("DONE");
 	}
 }
 
