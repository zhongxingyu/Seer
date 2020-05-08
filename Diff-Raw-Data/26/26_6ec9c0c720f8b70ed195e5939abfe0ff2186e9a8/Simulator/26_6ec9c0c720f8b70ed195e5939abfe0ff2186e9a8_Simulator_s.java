 package com.CompArch;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Simulator {
 	
 	// Used for output
 	int maxReg = 0;
 	int maxMem = 0;
 	
 	// Used for stats
 	int cycleTotal = 0;
 	
 	// Program counter
 	public int PC;
 	
 	// Instruction memory section
 	public int[][]instructMem;
 	
 	// Data memory section
 	public int[] dataMem;
 	
 	public RegisterFile regFile;
 	
 	// Reservation station array
 	private ReservationStation rs[];
 	
 	// Branch controller of the processor
 	private BranchController bc;
 	
 	public static void main(String[] args) {
 		System.out.println("Launching simulator");
 		System.out.println("Running program");
 
 		Simulator sim = new Simulator(100,100,200,2);
 
 		File file = new File(args[0]);
 
 		System.out.println("Assembling code");
 		
 		if (!file.isFile())
 		{
 			System.out.println("Code file did not exist");
 			return;
 		}
 		else
 		{
 			assembler asm = new assembler();
 			asm.read(file, "torun.out");
 		}
 
 		// Load instructions
 		if (args.length > 0)
 			sim.loadInstruct("torun.out");
 		
 		// If memory values given load them
 		if (args.length == 2)
 			sim.loadData(args[1]);
 		
 		System.out.println("BEFORE:\n");
 		
 		System.out.println("INSTRUCTIONS\n---------");
 		sim.printInstruct();
 		System.out.println("---------");
 		System.out.println("DATA\n---------");
 		sim.printData();
 		System.out.println("---------");
 		
 		sim.run();
 		
 		System.out.println("\nAFTER:\n");
 		
 		System.out.println("INSTRUCTIONS\n---------");
 		sim.printInstruct();
 		System.out.println("---------");
 		System.out.println("REG\n---------");
 		sim.printReg();		
 		System.out.println("---------");
 		System.out.println("DATA\n---------");
 		sim.printData();
 		System.out.println("---------");
 		System.out.print("Total cycles: " + sim.cycleTotal);
 	}
 	
 	Simulator (int registers, int instructions, int dataSize, int rsNum){
 		// Set up components
 		rs = new ReservationStation[rsNum];
 		for (int i = 0; i < rsNum; i++)
 			rs[i] = new ReservationStation(this, 4);
 		
 		bc = new BranchController(this);
 		
 		// Set up registers
 		PC = 0;
 		instructMem = new int[instructions][4];
 		dataMem = new int[dataSize];
 		
 		regFile = new RegisterFile(registers);
 	}
 	
 	// Tick the processor
 	void tick () {
 		cycleTotal++;
 		for (int i = 0; i < rs.length; i++)
 			rs[i].tick();
 		
 		bc.tick();
 	}
 	
 	// Load values from file to instruction memory
 	void loadInstruct (String file)	{
 		System.out.println("Loading Instruction File: " + file);
 		try {
             BufferedReader br = new BufferedReader(new FileReader (file));
             String line = br.readLine();
             int i = 0;
             while (line != null) {
                 String[] nums = line.split(" ");
                 instructMem[i][0] = new Integer(nums[0]);
                 instructMem[i][1] = new Integer(nums[1]);
                 instructMem[i][2] = new Integer(nums[2]);
                 instructMem[i][3] = new Integer(nums[3]);
                 i++;
                 line = br.readLine();
             }
         }
 		catch (IOException e) {
             System.err.println("Error: " + e);
         }
 	}
 	
 	// Load values from file to data memory
 	void loadData (String file) {
 		System.out.println("Loading Data File: " + file);
 		try {
             BufferedReader br = new BufferedReader(new FileReader (file));
             String line = br.readLine();
             int i = 0;
             while (line != null) {
                 dataMem[i] = new Integer(line);
                 i++;
                 line = br.readLine();
             }
             maxMem = i-1;
         }
 		catch (IOException e) {
             System.err.println("Error: " + e);
         }
 	}
 	
 	void printData () {
 		for (int i = 0; i <= maxMem; i++){
 			System.out.println(i+": " + dataMem[i]);
 		}
 	}
 	
 	void printInstruct () {
 		int i = 0;
 		while (instructMem[i][0] != 0){
 			System.out.println(i+": " + instructMem[i][0] + " " 
 					+ instructMem[i][1] + " " + instructMem[i][2] + " " + instructMem[i][3]);
 			i++;
 		}
 	}
 	
 	void printReg () {
 		regFile.printReg();
 		/*
 		for (int i = 0; i <= maxReg; i++)
 		{
 			System.out.println(reg[i]);
 		}
 		*/
 	}
 	
 	// Fetch decode and execute an instruction, returns true if instruction executed, otherwise false
 	boolean fetch(int[] instruct) {
 		boolean result;
 		
 		// Log the maximum written to register
 		if (instruct[1] > maxReg && instruct[0] != 0 && instruct[0] != 19)
 			maxReg = instruct[1];
 		
 		if (instruct[0] != 0 && instruct[0] != 19)
 			regFile.issue(instruct[1]);
 		
 		// Memory load
 		if (instruct[0] <= 2)
 		{
 			result = true;
 			mem(instruct[0], instruct[1], instruct[2], instruct[3]);
 		}
 		
 		// IAU instruction
 		else if (instruct[0] <= 16) {
 			//result = iau.free;
 			//if (result)
 				//iau.read(instruct[0], instruct[1], instruct[2], instruct[3]);
 			result = rs[0].receive(instruct);
 		}
 		else {
 			result = bc.free;
 			if (result)
 				bc.read(instruct[0], instruct[1], instruct[2], instruct[3]);
 		}
 		
 		return result;
 	}
 	
 	// Handles memory loads and writes
 	void mem(int instruct, int r1, int r2, int offset)
 	{
 		// Increment the clock for the memory access (cost - 1)
 		cycleTotal += 3;
 		if (instruct == 1){
 			//reg[r1] = dataMem[reg[r2] + offset];
 			regFile.set(r1, dataMem[regFile.get(r2) + offset]);
 		}
 		else if (instruct == 2){
 			//dataMem[reg[r2] + offset] = reg[r1];
 			dataMem[regFile.get(r2) + offset] = regFile.get(r1);
 			if (regFile.get(r2) + offset > maxMem)
 				maxMem = regFile.get(r2) + offset;
 		}
 	}	
 	
	// Run the processor with the current instruction and memory content
	void run () {
		
		// check if any reservation stations are free
		boolean rsFree = true;
 		for (int i = 0; i < rs.length; i++)
 		{
 			if (!rs[i].isFree())
				rsFree = false;
 		}
 		
 		while (instructMem[PC][0] != 0 || !rsFree /* !iau.free */|| !bc.free) {
 			boolean next = false;
 			if (rsFree/* iau.free*/ && bc.free)
 				next = fetch(instructMem[PC]);
 			tick();
 			if (next)
 				PC++;
 		}
 	}
 }
