 package kernel;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 
 import coff.CoffLoadException;
 import coff.Loader;
 
 import hardware.Interrupt;
 import hardware.Timer;
 import emulator.Memory;
 import emulator.MipsException;
 import emulator.Processor;
 import filesystem.FileTableEntry;
 import filesystem.FileSystem;
 
 import machine.Configuration;
 import machine.Lib;
 import machine.Page;
 import machine.Machine;
 import static emulator.MipsException.*;
 
 /**
  * Main class - handles interrupts, system calls, scheduling and memory management.
  * 
  * @author pauljohnson
  *
  */
 public class Kernel implements Runnable{
 	// list of system processes
 	public PCB[] processes = new PCB[Configuration.maxProcesses];
 	
 	// current running process
 	public PCB process;
 	
 	// has the kernel been initialized?
 	private boolean initialized = false;
 	
 	// the process scheduler
 	Scheduler scheduler;
 	
 	// the algorithm to use for page replacement
 	PageReplacement pageReplacer;
 	
 	// the filesystem
 	FileSystem fs;
 	
 	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2,
 	syscallJoin = 3, syscallCreate = 4, syscallOpen = 5,
 	syscallRead = 6, syscallWrite = 7, syscallClose = 8,
 	syscallUnlink = 9, syscallKernelInit = 13, syscallMoreMemory = 14, syscallFork = 15;
 	
 	private Machine machine;
 	
 	public Kernel(Machine machine){
 		this.machine = machine;
 	}
 	
 	/**
 	 * This doesn't have anything to do with threading - its just used by the process to 
 	 * handle interrupts and exceptions
 	 */
 	public void run() {
 		Processor processor = machine.processor();
 		int cause = processor.readRegister(Processor.regCause);
 		
 		switch(cause){		
 		case exceptionSyscall:
 			syscall();
 			
 			machine.processor().advancePC();
 			break;
 		case exceptionPageFault:
 			pageFault();
 			
 			//machine.processor().advancePC();
 			break;
 		case exceptionTLBMiss:
 			tlbMiss();
 			break;
 		case exceptionReadOnly:
 		case exceptionBusError:
 		case exceptionAddressError:
 		case exceptionOverflow:
 		case exceptionIllegalInstruction:
 			exception();
 			break;
 		case exceptionInterrupt:
 			interrupt();
 			
 			if(process.ticks > process.quantum){
 				//machine.processor().advancePC();
 			}
 			break;
 		default:
 			throw new KernelFault("Unknown Processor Exception");
 		}
 		
 		// schedule the next process to run
 		schedule();
 
 	}
 	
 	/**
 	 * Schedules the next process if necessary
 	 * 
 	 */
 	public void schedule(){
 		// do nothing
 		if(process != null && process.ticks < process.quantum && process.state == PCB.running){
 			return;
 		}
 				
 		// save current process
 		if(process != null){		
 			if(process.state == PCB.running)
 				process.state = PCB.ready;
 			
 			// save registers
 			for (int i = 0; i < Processor.numUserRegisters; i++){
 				process.userRegisters[i] = machine.processor().readRegister(i);
 			}
 		}
 		
 		// get next process
 		PCB nextProcess = this.scheduler.schedule(process);
 		
 		if(process != null)
 			process.ticks = 0;
 		
 		if(nextProcess == null){
 			throw new KernelFault("No Processes available for scheduling");
 		}
 		
 		// restore next process
 		for (int i = 0; i < Processor.numUserRegisters; i++){
 			machine.processor().writeRegister(i, nextProcess.userRegisters[i]);
 		}
 		
 		nextProcess.state = PCB.running;
 		
 		machine.memory().setPageTable(nextProcess.pageTable);
 		
 		process = nextProcess;
 		
 		//System.out.println(process.name + " pid " + process.pid + " On Processor");
 	}
 	
 	/**
 	 * Handle a processor exception
 	 */
 	public void exception(){
 		throw new KernelFault("Exception in Processor");
 	}
 
 	/**
 	 * Handle a processor interrupt
 	 */
 	public void interrupt(){
 		Interrupt interrupt = machine.interrupting;
 		
 		if(interrupt instanceof Timer){
 			process.ticks++;
 			
 			decrementIOWaiters();
 			
 			// reset used flags on pages - used by some page replacement algorithms
 //			for(PCB pcb : processes){
 //				if(pcb != null){
 //					for(Page page : pcb.pageTable){
 //						if(page != null){
 //							page.used = false;
 //						}
 //					}
 //				}
 //			}
 		}
 		
 		machine.interrupting = null;
 		interrupt.acknowledge();
 	}
 	
 
 
 	/**
 	 * Handle a page fault
 	 */
 	public void pageFault(){
 		// find page that needs to be brought into memory
 		int badVaddr = machine.processor().readRegister(Processor.regBadVAddr);
 		int virtualPageNumber = Memory.pageFromAddress(badVaddr);
 
 		Page virtualPage = machine.memory().pages[virtualPageNumber];
 		
 		// if the page doesn't exist yet we need to bring it into memory
 		if(virtualPage == null){
 			virtualPage = new Page(virtualPageNumber, -1, false, false, false, false);
 			process.pageTable[virtualPageNumber] = virtualPage;
 			virtualPage.pid = process.pid;
 		}
 		
 		// use the page replacement algorithm to find a physical page number to put the page in and 
 		// a page to replace if necessary
 		pageReplacer.replace(virtualPage);
 		Page replacedPage = pageReplacer.getReplacedPage();
 		
 //		if(replacedPage != null){
 //			System.out.println("Loading page " + virtualPageNumber + " of process " + process.name + " " + process.pid + " Replacing " + replacedPage.vpn + " from process " + processes[replacedPage.pid].name + " Into "  + pageReplacer.getPhysicalPageNumber());
 //		}else{
 //			System.out.println("Loading page " + virtualPageNumber  + " of process " + process.name + " Into " + pageReplacer.getPhysicalPageNumber());
 //		}
 			
 		// If we need to save a page
 		savePage(replacedPage);
 		
 		int physicalPageNumber = pageReplacer.getPhysicalPageNumber();
 			
 		// load page from disk if necessary
 		loadPage(virtualPage, physicalPageNumber);
 	}
 	
 	/** 
 	 * Handle TLB miss - not implemented
 	 */
 	public void tlbMiss(){
 		
 	}
 	
 	/**
 	 * Handle a syscall from a program
 	 * 
 	 */
 	public void syscall(){
 		
 		Processor processor = machine.processor();
 		
 		int syscall = processor.readRegister(Processor.regV0);
 		
 		switch(syscall){
 		case syscallHalt:
 			handleHalt();
 						
 			break;
 		case syscallExit:
 			handleExit();
 			break;
 		case syscallExec:
 			handleExec();
 			
 			simulateIOWait();
 			
 			break;
 		case syscallJoin:
 			handleJoin();
 			break;
 		case syscallCreate:
 			handleCreate();
 			
 			simulateIOWait();
 			break;
 		case syscallOpen:
 			handleOpen();
 			
 			simulateIOWait();
 			break;
 		case syscallRead:
 			handleRead();
 			
 			simulateIOWait();
 			break; 
 		case syscallWrite:
 			handleWrite();
 			
 			simulateIOWait();
 			break; 
 		case syscallClose:
 			handleClose();
 			
 			simulateIOWait();
 			break;
 		case syscallUnlink:
 			handleUnlink();
 			simulateIOWait();
 			break;
 		case syscallFork:
 			handleFork();
 			break; 
 		case syscallKernelInit:
 			if(!initialized){
 				initialize();
 			}else{
 				throw new KernelFault("Kernel already initialized");
 			}
 			break;
 		default:
 			throw new KernelFault("Unknown Syscall");
 		}
 	}
 
 	/**
 	 * Clones a process
 	 */
 	private void handleFork() {
 		PCB child = new PCB();
 		
 
 		
 		child.name = process.name;
 		child.state = PCB.ready;
 		child.ticks = 0;
 		child.userRegisters = Arrays.copyOf(machine.processor().registers, machine.processor().registers.length);
 		child.parent = process.pid;
 		child.joining = -1;
 		child.statusPointer =-1;
 		
 		// copy page table
 		child.pageTable = new Page[Configuration.numVirtualPages];
 		
 		int child_pid = addProcess(child);
 
 		for (int i = 0; i < process.pageTable.length; i++) {
 			Page oldPage = process.pageTable[i];
 
 			if (oldPage == null) {
 				continue;
 			}
 
 			// copy pages
 			child.pageTable[i] = new Page(oldPage.vpn, -1, false, false, false, false);
 			
 			child.pageTable[i].dirty = oldPage.dirty;
 			child.pageTable[i].used = oldPage.used;
 			child.pageTable[i].pid = child.pid;
 			
 			// copy pages memory to child page - when it is swapped out it will be saved
 			int memoryPointer = Memory.makeAddress(oldPage.vpn, 0);
 			checkInMemory(memoryPointer);
 			
 			// read from memory
 			byte[] buffer = new byte[Configuration.pageSize];
 			
 			for(int x = 0; x < Configuration.pageSize; x++){
 				try {
 					buffer[x] = (byte)machine.memory().readMem(memoryPointer+x, 1);
 				} catch (MipsException e) {
 					throw new KernelFault("Bad address");
 				}
 			}
 			
 			// write to memory
 			machine.memory().setPageTable(child.pageTable);
 			
 			// set process to this so that it creates pages in the right process
 			PCB oldProc = process;
 			process = child;
 			
 			checkInMemory(memoryPointer);
 			
 			for(int x = 0; x < Configuration.pageSize; x++){
 				try {
 					machine.memory().writeMem(memoryPointer+x, 1, buffer[x]);
 				} catch (MipsException e) {
 					throw new KernelFault("Bad address");
 				}
 			}
 			
 			process = oldProc;
 			machine.memory().setPageTable(process.pageTable);
 		}
 			
 		child.userRegisters[Processor.regV0] = 0;
 		
 		child.userRegisters[Processor.regPC] += 4;
 		
 		machine.processor().writeRegister(Processor.regV0, child_pid);
 	}
 
 	/**
 	 * Syscall handler methods
 	 */
 
 	private void handleUnlink() {
 		String name = getStringFromMemory(Processor.regA0);
 		
 		int rval = fs.unlink(name);
 		
 		machine.processor().writeRegister(Processor.regV0, rval);
 	}
 	
 	private void handleExec() {
 		int namePointer = machine.processor().readRegister(Processor.regA0);
 		
 		String name = getStringFromMemoryPointer(namePointer);
 		
 		int argc = machine.processor().readRegister(Processor.regA1);
 		
 		int argsPointer = machine.processor().readRegister(Processor.regA2);
 		
 		// read args from memory
 		String[] args = new String[argc];
 		
 		for(int i = 0; i < argc; i++){
 			try {
 				int argPointer = machine.memory().readMem(argsPointer+(i*4), 4);
 				
 				args[i] = getStringFromMemoryPointer(argPointer);
 			} catch (MipsException e) {
 				throw new KernelFault("bad address");
 			}
 		}
 		
 		handleExec(name, args);
 	}
 	
 	/**
 	 * Lazy version of exec for use by initialize
 	 * 
 	 * @param name
 	 * @param args
 	 */
 	private void handleExec(String name, String[] args){
 		PCB new_process = new PCB();
 		
 		new_process.name = name;
 		
 		new_process.pageTable = new Page[Configuration.numVirtualPages];
 		
 		addProcess(new_process);
 		
 		if(process == null){
 			new_process.parent = -1;
 		}else{
 			new_process.parent = process.pid;
 		}
 		
 
 		int fid = fs.open(name, new_process);
 		
 		if(fid == -1){
 			throw new KernelFault("Program not found");
 		}
 		
 		// swap the new processes page table into memory so we can read data into it
 		machine.memory().setPageTable(new_process.pageTable);
 		PCB old_process = process;
 		process = new_process;
 	
 		try {
 			Loader loader = new Loader();
 			
 			String[] nameArgs = new String[args.length+1];
 			
 			nameArgs[0] = name;
 			System.arraycopy(args, 0, nameArgs, 1, args.length);
 			
 			System.out.println("Loading " + name);
 			
 			loader.load(fid, new_process, fs, this, machine.memory(), nameArgs);
 			
 			// by default, everything's 0
 			for (int i = 0; i < Processor.numUserRegisters; i++)
 				new_process.userRegisters[i] = 0;
 
 			// initialize PC and SP according
 			new_process.userRegisters[Processor.regPC] = loader.programCounter;
 			new_process.userRegisters[Processor.regSP] = loader.stackPointer;
 			
 			new_process.userRegisters[Processor.regNextPC] = new_process.userRegisters[Processor.regPC] + 4;
 			
 			// initialize the first two argument registers to argc and argv
 			new_process.userRegisters[Processor.regA0] = loader.argc;
 			new_process.userRegisters[Processor.regA1] = loader.argv;
 			
 			// reset page table
 			
 			process = old_process;
 			
 			if(process != null){
 				machine.memory().setPageTable(process.pageTable);
 			}
 			
 			// close file
 			fs.close(fid, new_process);
 		} catch (CoffLoadException e) {
 			throw new KernelFault("Tried to execute file that isn't a coff file");
 		} catch (MipsException e) {
 			throw new KernelFault("Tried to execute file that isn't a coff file");
 		}
 		
 		machine.processor().writeRegister(Processor.regV0, new_process.pid);
 	}
 
 	private void handleClose() {
 		int rval;
 		int fid = machine.processor().readRegister(Processor.regA0);
 		
 		rval = fs.close(fid, process);
 		
 		machine.processor().writeRegister(Processor.regV0, rval);
 	}
 
 	private void handleWrite() {
 		int fid = machine.processor().readRegister(Processor.regA0);
 		int startPointer = machine.processor().readRegister(Processor.regA1);
 		int length = machine.processor().readRegister(Processor.regA2);
 		
 		int read = -1;
 		
 		switch(fid){
 		case 1:
 			// handle standard out
 			
 			// make sure processes buffer is in memory
 			checkInMemory(startPointer);
 			
 			// read char from stdin
 			read = -1;
 			
 			try {
 				for(int i = 0; i < length; i++){
 					int t = machine.memory().readMem(startPointer+i, 1);
 				
 					//System.out.println(t);
 				
 					System.out.write((byte)t);
 				}
 				
 				read = length;
 			} catch (MipsException e) {
 				throw new KernelFault("Bad address");
 			}
 			break;
 		default:
 			read = fs.write(fid, length, startPointer, this, process);
 		}
 		
 		machine.processor().writeRegister(Processor.regV0, read);
 	}
 
 	private void handleRead() {
 		int fid = machine.processor().readRegister(Processor.regA0);
 		
 		int bufferPointer = machine.processor().readRegister(Processor.regA1);
 		int length = machine.processor().readRegister(Processor.regA2);
 		
 		int read = -1;
 		
 		switch(fid){
 		case 0:
 			// handle standard in
 			
 			// make sure processes buffer is in memory
 			checkInMemory(bufferPointer);
 			
 			// read char from stdin
 			read = 1;
 			
 			try {
 				int t = System.in.read();
 				
 				machine.memory().writeMem(bufferPointer, 1, t);
 				
 			} catch (IOException e) {
 				throw new KernelFault("Unable to read from standard in");
 			} catch (MipsException e) {
 				throw new KernelFault("Bad address");
 			}
 		default:
 			read = fs.read(fid, length, bufferPointer, this, process);
 		}
 
 		machine.processor().writeRegister(Processor.regV0, read);
 	}
 
 	private void handleHalt() {
 		if(process.pid  == 0){
 			machine.halt();
 		}else{
 			processes[process.pid] = null;
 			
 			// handle joining processes
 			
 			// set return value on joining processes
 			for(int i = 0; i < Configuration.maxProcesses; i++){
 				if(processes[i] != null && processes[i].joining == process.pid){
 					PCB joining = processes[i];
 					
 					joining.state = PCB.ready;
 					
 					joining.joining = -1;
 					joining.statusPointer = 0;
 					
 					joining.userRegisters[Processor.regV0] = 0;
 				}
 			}
 		}
 	}
 
 	private void handleCreate() {
 		String name = getStringFromMemory(Processor.regA0);
 		
 		int rval = fs.create(name, process);
 		
 		machine.processor().writeRegister(Processor.regV0, rval);
 	}
 
 	private void handleOpen() {
 		String name = getStringFromMemory(Processor.regA0);
 		
 		int rval = fs.open(name, process);
 		
 		machine.processor().writeRegister(Processor.regV0, rval);
 	}
 
 	private String getStringFromMemory(int register) {
 		int namePointer = machine.processor().readRegister(register);
 		
 		return getStringFromMemoryPointer(namePointer);
 	}
 	
 	private String getStringFromMemoryPointer(int namePointer){
 		
 		checkInMemory(namePointer);
 		
 		int i = 0;
 		
 		byte b;
 		
 		StringBuilder sb = new StringBuilder();
 		
 		try {
 			while((b = (byte)machine.memory().readMem(namePointer+i, 1)) != 0){
 				sb.append((char)b);
 				
 				i++;
 			}
 		} catch (MipsException e) {
 			throw new KernelFault("bad address");
 		}
 		
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * Check if a page for an address is in memory, if it isn't then trigger
 	 * a page fault to bring it into memory
 	 * 
 	 * @param namePointer
 	 * @return page that has been placed in memory
 	 */
 	public Page checkInMemory(int namePointer) {
 		if(!machine.memory().vmEnabled){
 			return null;
 		}
 		
 		// check if page containing pointer is in memory
 		int vpn = Memory.pageFromAddress(namePointer);
 		
 		Page page = machine.memory().pages[vpn];
 		
 		if(page == null || !page.present){
 			//System.out.println("Faulting on check");
 			machine.processor().writeRegister(Processor.regBadVAddr, namePointer);
 			pageFault();
 		}
 		return page;
 	}
 	
 	private void handleJoin() {
 		int childPid = machine.processor().readRegister(Processor.regA0);
 		int statusPointer = machine.processor().readRegister(Processor.regA1);
 		
 		PCB child = processes[childPid];
 		
 		if(child == null || child.parent != process.pid){
 			machine.processor().writeRegister(Processor.regV0, -1);
 			return;
 		}
 		
 		process.state = PCB.waiting;
 		
 		process.joining = child.pid;
 		process.statusPointer = statusPointer;
 	}
 
 	/**
 	 * Called by the boot block to initialize the system
 	 */
 	public void initialize(){
 		this.initialized = true;
 		
 		this.scheduler = (Scheduler) Lib.constructObject(Configuration.scheduler);
 		
 		this.pageReplacer = (PageReplacement) Lib.constructObject(Configuration.replacer);
 		
 		// create file system
 		this.fs = (FileSystem) Lib.constructObject(Configuration.fileSystem);
 		
 		this.fs.initialize(machine);
 		
 		// enable virtual memory
 		machine.memory().vmEnabled = true;
 		
 		// create first process
 		handleExec(Configuration.shellProgramName, Configuration.processArgs);
 		
 		// create idle process
 		handleExec("idle.coff", new String[]{});
 	
 		// start timer to generate context switching interrupts
 		new Thread(machine.timer, "Timer Thread").start();
 	}
 
 	private void handleExit() {
 		System.out.println("Process " + process.name + " exiting");
 		
 		int status = machine.processor().readRegister(Processor.regA0);
 		
 		processes[process.pid] = null;
 		
 		// halt machine if last process has exited
 		if(process.pid == 0){
 			machine.halt();
 		}
 		
 		// close files
 		for(int i = 0; i < Configuration.maxFiles; i++){
 			if(process.files[i] != null){
 				fs.close(i, process);
 			}
 		}
 		
 		// set return value on joining processes
 		for(int i = 0; i < Configuration.maxProcesses; i++){
 			if(processes[i] != null && processes[i].joining == process.pid){
 				PCB joining = processes[i];
 				
 				joining.state = PCB.ready;
 				
 				// write return result of process to the joining processes status pointer
 				
 				// set the current process to joining so memory works correctly
 				PCB oldProcess = process;
 				process = joining;
 				
 				machine.memory().setPageTable(joining.pageTable);
 				
 				checkInMemory(joining.statusPointer);
 				
 				byte[] statusBytes = Lib.bytesFromInt(status);
 				for(int j = 0; j < statusBytes.length; j++){
 					try {
 						machine.memory().writeMem(joining.statusPointer+j, 1, statusBytes[j]);
 					} catch (MipsException e) {
 						throw new KernelFault("unable to write to memory");
 					}
 				}
 	
 				process = oldProcess;
 				machine.memory().setPageTable(oldProcess.pageTable);
 				
 				joining.joining = -1;
 				joining.statusPointer = 0;
 				
 				joining.userRegisters[Processor.regV0] = 1;
 			}
 		}
 		
 		// set parents to 0;
 		for(int i = 0; i < Configuration.maxProcesses; i++){
 			if(processes[i] != null && processes[i].parent == process.pid){
 				processes[i].parent = -1;
 			}
 		}
 		
 		// remove page file from disk
 		fs.unlink(process.pid + "_" + process.name);
 		
 		// remove pages
 		pageReplacer.removeProcess(process);
 
 		// remove from scheduler
 		scheduler.removeProcess(process);
 		
 		process = null;
 	}
 	
 	public int addProcess(PCB pcb){
 		for(int i = 0; i < Configuration.maxFiles;i++){
 			if(processes[i] == null){
 				pcb.pid = i;
 				processes[i] = pcb;
 				scheduler.addProcess(pcb);
 				pageReplacer.addProcess(pcb);
 				return i;
 			}
 		}
 		
 		throw new KernelFault("Too many processes open");
 	}
 	
 	// list of processes we are simulating IO waiting on
 	private List<PCB> ioWaiters = new ArrayList<PCB>();
 	
 	public void simulateIOWait(){
 		process.state = PCB.waiting;
 		// wait for one quantum
 		process.waitTicks = Configuration.quantum;
 		
 		ioWaiters.add(process);
 	}
 	
 	// used by the IO wait simulation
 	private void decrementIOWaiters() {
 		for(PCB pcb : ioWaiters){
 			
 			if(pcb.waitTicks == 0){
 				pcb.state = PCB.ready;
 			}else{
 				pcb.waitTicks--;
 			}
 		}
 	}
 	
 	/**
 	 * save a page to disk if necessary
 	 * 
 	 * @param tempProc 
 	 */
 	void savePage(Page page){	
 		if(page != null){
 			if(page.dirty || !page.saved){
 				PCB proc = processes[page.pid];
 			
 				machine.memory().setPageTable(proc.pageTable);
 				
 				for (int j = 0; j < Configuration.pageSize; j++) {
 					int t = Memory.makeAddress(page.vpn, j);
 					try {
 						page.data[j] = (byte) machine.memory().readMem(t, 1);
 					} catch (MipsException e) {
 						throw new KernelFault("bad memory address");
 					}
 				}
 				
 				machine.memory().setPageTable(process.pageTable);
 			
 				page.saved = true;
 			}
 			
 			page.ppn = -1;
 			page.present = false;
 			page.readOnly = false;
 			page.used = false;
 			page.dirty = false;
 		}
 		
 		machine.memory().setPageTable(process.pageTable);
 	}
 	
 	/**
 	 * Load a page from memory to the given physical page number
 	 * @param virtualPage
 	 */
 	void loadPage(Page virtualPage, int physicalPageNumber) {
 		// set details in page table entry
 		virtualPage.ppn = physicalPageNumber;
		virtualPage.pid = process.pid;
 		virtualPage.present = true;
 		virtualPage.readOnly = false;
 		virtualPage.used = false;
 		virtualPage.dirty = false;
 		
 		if(virtualPage.saved){
			
			PCB pageProc = processes[virtualPage.pid];
			
 			// write directly to memory
 			machine.memory().vmEnabled = false;
 			
 			int address = virtualPage.ppn * Configuration.pageSize;
 			
 			for(int i = 0; i < Configuration.pageSize; i++){
 				try {
 					machine.memory().writeMem(address + i, 1, virtualPage.data[i]);
 				} catch (MipsException e) {
 					throw new KernelFault("bad memory address");
 				}
 			}
 			machine.memory().vmEnabled = true;
 			
 		}else{
 			// do nothing - could be a new blank page -
 			// isn't on disk/memory - has to be saved first
 		}
 		
 		// set false because when we are writing to memory 
 		// this will be set dirty when the page actually isn't
 		virtualPage.dirty = false;
 	}
 }
