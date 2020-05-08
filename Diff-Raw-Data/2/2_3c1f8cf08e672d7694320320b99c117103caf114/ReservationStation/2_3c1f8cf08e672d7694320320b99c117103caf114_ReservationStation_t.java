 package com.CompArch;
 
 public class ReservationStation {
 
 	private Simulator sim;
 	private IAU iau;
 	
 	private int depth;
 	
 	// Location in the 
 	private int next;
 	private int total;
 	
 	// Instruction memory section
 	private int[][] instructBuffer;
 	
 	// Location of each instruction in the reorder buffer
 	private int robLoc[];
 	
 	// Whether inputs are available
 	private boolean[][] available;
 	
 	public boolean isFree ()
 	{
 		if (total > 0 || !iau.free)
 			return false;
 		else
 			return true;
 	}
 
 	public ReservationStation (Simulator s, int size)
 	{
 		depth = size;
 		next = 0;
 		total = 0;
 		instructBuffer = new int[size][4];
 		robLoc = new int[size];
 		iau = new IAU(s);
 		available = new boolean[size][2];
 		for (int i = 0; i < size; i++) {
 			available[i][0] = false;
 			available[i][1] = false;
 		}
 		sim = s;
 	}
 	
 	// Takes instruction, returns true if added to buffer, false if buffer full
 	public boolean receive (int[] instruction)
 	{
 		if (total == depth)
 		{
 			return false;
 		}
 		
 		/* Get the register values if needed*/
 		int toReserve[] = instruction;
 		
 		// Check if instruction is an overwrite
 		boolean isWrite = instruction[0] == 1;
 		boolean isWipe = instruction[0] == 5 && instruction[1] == instruction[2] 
 				&& instruction[2] == instruction[3]; 
 		
 		int overWrite = -1;
 		
 		if (isWrite || isWipe)
 		{
 			overWrite = sim.rrt.getReg(instruction[1]);
 		}
 		
 		if (instruction[0] > 0 && instruction[0] < 19)
 		{
			//toReserve[1] = sim.rrt.getReg(instruction[1]);
 		}
 		
 		total++;
 		
 		int dest = (next + total - 1) % depth;
 		/*System.out.println("--");
 		System.out.println(next + " " + total + " " + depth);
 		System.out.println((next + total - 1) % depth);
 		System.out.println("--");*/
 		
 		instructBuffer[dest] = instruction;
 		
 		// Add instruction to the reorder buffer
 		robLoc[dest] = sim.rob.insert(instruction, overWrite);
 		
 		// Get available operands
 		/*
 		if (sim.regFile.isFree(instruction[2]))
 		{
 			instructBuffer[dest][2] = sim.regFile.get(instruction[2]);
 			available[dest][0] = true;
 		}
 		
 		int in = instruction[0];
 		
 		if (in != 3 && in != 9 && in != 11 && in != 13 && in != 14 && in != 16)
 		{
 			if (sim.regFile.isFree(instruction[3]))
 			{
 				instructBuffer[dest][3] = sim.regFile.get(instruction[3]);
 				available[dest][1] = true;
 			}
 		}
 		
 		if (in != 0 && in != 19)
 		{
 			//Mark as issued
 		}
 		*/
 		return true;
 	}
 	
 	public void tick ()
 	{
 		this.dispatch();
 		iau.tick();
 	}
 	
 	void dispatch ()
 	{
 		// perform dependancy and IAU availability checking, if ready then send
 		/*System.out.println(instructBuffer[next][0] + " " + instructBuffer[next][1] + " " + 
 				instructBuffer[next][2] + " " + instructBuffer[next][3]);*/
 		boolean depends = false;
 		
 		/*
 		if (!sim.regFile.isFree(instructBuffer[next][2]))
 		{
 			depends = true;
 			System.out.println(instructBuffer[next][2] + " NOT FREE1");
 		}
 		int in = instructBuffer[next][0];
 		if (in != 3 && in != 9 && in != 11 && in != 13 && in != 14 && in != 16)
 		{
 			if (!sim.regFile.isFree(instructBuffer[next][3]))
 			{
 				depends = true;
 				System.out.println(instructBuffer[next][3] + " NOT FREE2");
 			}
 		}
 		depends = false;*/
 		
 		if (iau.free && total > 0 && !depends)
 		{
 			//System.out.println(instructBuffer[next][2] + " " + instructBuffer[next][3] + " FREE");
 			/*System.out.println("WORKING: " + total);
 			System.out.println("running: " + instructBuffer[next][0] + " " + instructBuffer[next][1]
 					+ " " + instructBuffer[next][2] + " " + instructBuffer[next][3]);*/
 			
 			iau.read(instructBuffer[next][0], instructBuffer[next][1], instructBuffer[next][2], 
 					instructBuffer[next][3], robLoc[next]);
 			next++;
 			next = next % depth;
 			total--;
 		}
 		
 		//System.out.println("---");
 			
 	}
 	
 	
 }
