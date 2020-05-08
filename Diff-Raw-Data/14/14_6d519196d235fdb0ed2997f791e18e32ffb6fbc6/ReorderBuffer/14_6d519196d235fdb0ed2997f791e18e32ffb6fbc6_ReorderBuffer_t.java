 package com.CompArch;
 
 public class ReorderBuffer {
 	private int instruct[];
 	private int dest[];
 	private int result[];
 	private boolean valid[];
 	
 	// Logs if instruction is overwriting a particular register's value (for register renaming)
 	private int overWrite[];
 	private int size;
 	private int head;
 	private int tail;
 	private Simulator sim;
 
 	ReorderBuffer (Simulator s, int length)
 	{
 		sim = s;
 		head = 0;
 		tail = 0;
 		size = length;
 		instruct = new int [length];
 		dest = new int [length];
 		result = new int [length];
 		valid = new boolean [length];
 		overWrite = new int [length];
 		for (int i = 0; i < length; i++)
 		{
 			valid[i] = false;
 			overWrite[i] = -1;
 		}
 	}
 	
 	void setResult (int index, int val)
 	{
 		result[index] = val;
 		valid[index] = true;
 	}
 	
 	/** insert an instruction into the reorder buffer, along with the register it is overwriting */
 	int insert (int[] instruction, int over)
 	{
 		instruct[head] = instruction[0];
 		
 		overWrite[head] = over;
 		
 		// Dest if branch operation
 		if (instruction[0] == 17 | instruction[0] == 18)
 			dest[head] = instruction[3];
 		else
 			dest[head] = instruction[1];
 				
 		int result = head;
 		
 		head++;
 		
 		if (head >= size)
 			head = 0;
 		
 		return result;
 	}
 	
 	void tick()
 	{
 		//Check if current instruction is valid, if so then write it and move on to the next
 		if (valid[tail])
 		{
 			sim.regFile.set(dest[tail], result[tail]);
 			System.out.println("WROTE TO: " + dest[tail] + " : " + result[tail]);
			System.out.println("FREEING: " + overWrite[tail]);
			sim.rrt.free(overWrite[tail]);
 			valid[tail] = false;
 			tail++;
 			if (tail >= size)
 				tail = 0;
 		}
 	}
 }
