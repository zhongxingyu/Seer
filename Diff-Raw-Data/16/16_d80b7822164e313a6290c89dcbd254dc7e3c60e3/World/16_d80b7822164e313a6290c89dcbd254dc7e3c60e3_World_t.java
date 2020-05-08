 package masSim.world;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import masSim.agent.IAgent;
 import masSim.main.*;
 import masSim.agent.*;
 import masSim.taems.*;
 
 public class World {
 	
 	private int ticks = 0;
 	public final static int maxTicks = 10000;
 	
 	/** Agents that inhabit the current world */
 	private List<IAgent> agents;
 	
 	private IAgent ag1;
 	private IAgent ag2;
 	private ITask transTask;
 	//private List<ITask> tasks;
 	
 	// Blank constructor
 	public World(){	
 	}
 	
 	// Constructor
 	public World(int total_agents){
 		agents = new ArrayList<IAgent>();
 		int total_tasks = total_agents;
 		ag1 = new Agent(0, "Blackbird");
 		ag2 = new Agent(1, "Kiowa");
 		agents.add(ag1);
 		agents.add(ag2);
 		// Create a fixed number of agents
 		/*
 		int index = 0;
 		while(index < total_agents){
 			Agent agent = new Agent(index);
 			agents.add(agent);
 			index++;
 		}
 		System.out.println( index + " agents loaded...");	
 		*/
 		// Creation of sample tasks
 		/*
 		tasks = new ArrayList<ITask>();
 
 		for (index = 0; index < total_tasks; index++){
 			SeqSumQAF seqSum = new SeqSumQAF();
 			String taskName = "Cover_" + index;
 			ITask newTask = new Task(taskName, seqSum);
 			tasks.add(newTask);
 		}
 		*/
 		// transport task creation
 		SeqSumQAF seqSum = new SeqSumQAF();
 		transTask = new Task("Transport", seqSum);
 		ITask transSub1 = new Task("TransPickUp", seqSum);
 		transTask.addTask(transSub1);
 		Method tr1 = new Method("Fly to source", 5);
 		Method tr2 = new Method("Land at source", 2);
 		Method tr3 = new Method("Pick up at source", 3);
 		
 		transTask.addLeaf(tr1);
 		transTask.addLeaf(tr2);
 		transTask.addLeaf(tr3);
 		
 		transSub1.addMethod(tr1);
 		transSub1.addMethod(tr2);
 		transSub1.addMethod(tr3);
 		
 		ITask transSub2 = new Task("TransDeliver", seqSum);
 		transTask.addTask(transSub2);
 		Method tr4 = new Method("Fly to dest", 15);
 		Method tr5 = new Method("Land at dest", 2);
 		Method tr6 = new Method("Unload at dest", 3);
 		transTask.addLeaf(tr4);
 		transTask.addLeaf(tr5);
 		transTask.addLeaf(tr6);
 		
 		transSub1.addMethod(tr4);
 		transSub1.addMethod(tr5);
 		transSub1.addMethod(tr6);
 		
 		ag1.addTaskAbility(transTask);
 	}
 		
 	// To do...
 	// Class for non-local tasks
 	public void update(double dt){
 		//try {
 			
 			System.out.println("World updated with dt: " + dt );
 			for(IAgent ag : agents) {
 				ag.update(1);
 			}
 			if(ticks == 11) ag1.assignTask(transTask);
 			ticks++;
 			
 			System.out.println("Tick: " + ticks);	
 			if(ticks > 50) {
 				System.out.println("Exiting masSim");
 				System.exit(0);
 			}
 			/*
 			if (tasks.isEmpty()){
 				MasSim.setAlive(false);
 			}
 			
 			} catch (InterruptedException e) {
 				System.out.println("Thread interrupted");
 			}
 			 */
 	}
 }

