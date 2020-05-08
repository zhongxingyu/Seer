 package sos;
 
 import java.util.*;
 
 /**
  * This class contains the simulated operating system (SOS). Realistically it
  * would run on the same processor (CPU) that it is managing but instead it uses
  * the real-world processor in order to allow a focus on the essentials of
  * operating system design using a high level programming language.
  * 
  * @author Preben Ingvaldsen
  * @author Et Begert
  * @author Aaron Dobbe
  * @author Kyle DeFrancia
  * @author Daniel Jones
  * @author Raphael Ramos
  * @author Scott Matsuo
  * @author Fernando Freire
  * 
  * @version April 18, 2013
  * 
  */
 
 public class SOS implements CPU.TrapHandler {
 	// ======================================================================
 	// Constants
 	// ----------------------------------------------------------------------
 
 	// These constants define the system calls this OS can currently handle
 	public static final int SYSCALL_EXIT = 0; /* exit the current program */
 	public static final int SYSCALL_OUTPUT = 1; /* outputs a number */
 	public static final int SYSCALL_GETPID = 2; /* get current process id */
 	public static final int SYSCALL_OPEN = 3; /* access a device */
 	public static final int SYSCALL_CLOSE = 4; /* release a device */
 	public static final int SYSCALL_READ = 5; /* get input from device */
 	public static final int SYSCALL_WRITE = 6; /* send output to device */
 	public static final int SYSCALL_EXEC = 7; /* spawn a new process */
 	public static final int SYSCALL_YIELD = 8; /*
 												 * yield the CPU to another
 												 * process
 												 */
 	public static final int SYSCALL_COREDUMP = 9; /*
 												 * print process state and exit
 												 */
 
 	// Success and error code constants
 	public static final int SUCCESS = 0;
 	public static final int DEVICE_NOT_FOUND = -1;
 	public static final int DEVICE_NOT_SHARABLE = -2;
 	public static final int DEVICE_ALREADY_OPEN = -3;
 	public static final int DEVICE_NOT_OPEN = -4;
 	public static final int DEVICE_READ_ONLY = -5;
 	public static final int DEVICE_WRITE_ONLY = -6;
 
 	/** This process is used as the idle process' id */
 	public static final int IDLE_PROC_ID = 999;
 
 	// Amount to increase process priority by for I/O operations
 	public static final int READ_PRIORITY = 1;
 	public static final int WRITE_PRIORITY = 1;
 
 	// Priority aging values
 	public static final int AGING_PRIORITY = 4;
 	public static final int AGING_TIME = 1; // number of clock interrupts that
 											// pass
 	                                        // between aging promotions
 
 	// Amount by which a process's priority must exceed the current process's
 	// priority in order to take over
 	public static final int PRIORITY_THRESHOLD = 500;
 
 	// ======================================================================
 	// Member variables
 	// ----------------------------------------------------------------------
 
 	/**
 	 * This flag causes the SOS to print lots of potentially helpful status
 	 * messages
 	 **/
 	public static final boolean m_verbose = true;
 
 	/**
 	 * ID for the next process to load.
 	 **/
 	private int m_nextProcessID;
 
 	/**
 	 * The CPU the operating system is managing.
 	 **/
 	private CPU m_CPU = null;
 
 	/**
 	 * The RAM attached to the CPU.
 	 **/
 	private RAM m_RAM = null;
 
 	/**
 	 * The current process run by the CPU.
 	 **/
 	private ProcessControlBlock m_currProcess = null;
 
 	/**
 	 * All processes currently active.
 	 **/
 	private Vector<ProcessControlBlock> m_processes = null;
 
 	/**
 	 * All devices currently registered.
 	 **/
 	private Vector<DeviceInfo> m_devices = null;
 
 	/**
 	 * All programs currently running.
 	 **/
 	private Vector<Program> m_programs = null;
 
 	/**
 	 * Contains a list of all the blocks of RAM that are not currently allocated
 	 * to a process
 	 */
 	private Vector<MemBlock> m_freeList = null;
 
 	/**
 	 * Instance of an MMU...
 	 */
 	private MMU m_MMU = null;
 
 	/**
 	 * The size of SOS's page table. Incidentally also the max address of the
 	 * page table.
 	 */
 	private int m_sizeOfPageTable;
 
 	/*
 	 * ======================================================================
 	 * Constructors & Debugging
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * Initialize member variables
 	 */
 	public SOS(CPU c, RAM r, MMU mmu) {
 		// Init member list
 		m_CPU = c;
 		m_RAM = r;
 		m_MMU = mmu;
 		m_nextProcessID = 1001;
 		m_CPU.registerTrapHandler(this);
 		m_processes = new Vector<ProcessControlBlock>();
 		m_devices = new Vector<DeviceInfo>();
 		m_programs = new Vector<Program>();
 		m_freeList = new Vector<MemBlock>();
 		initPageTable();
 		m_freeList.add(new MemBlock(m_sizeOfPageTable, m_MMU.getSize()));
 	}// SOS ctor
 
 	/**
 	 * Does a System.out.print as long as m_verbose is true
 	 **/
 	public static void debugPrint(String s) {
 		if (m_verbose) {
 			System.out.print(s);
 		}
 	}
 
 	/**
 	 * Does a System.out.println as long as m_verbose is true
 	 **/
 	public static void debugPrintln(String s) {
 		if (m_verbose) {
 			System.out.println(s);
 		}
 	}
 
 	/*
 	 * ======================================================================
 	 * Process Management Methods
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * printProcessTable **DEBUGGING**
 	 * 
 	 * prints all the processes in the process table
 	 */
 	private void printProcessTable() {
 		debugPrintln("");
 		debugPrintln("Process Table (" + m_processes.size() + " processes)");
 		debugPrintln("======================================================================");
 		for (ProcessControlBlock pi : m_processes) {
 			debugPrintln("    " + pi);
 		}// for
 		debugPrintln("----------------------------------------------------------------------");
 
 	}// printProcessTable
 
 	/**
 	 * removeCurrentProcess
 	 * 
 	 * Removes the currently running process from the process table, and selects
 	 * a new one to run.
 	 */
 	public void removeCurrentProcess() {
 		debugPrintln("Removing process ID " + m_currProcess.getProcessId());
 		m_processes.remove(m_currProcess);
 		freeCurrProcessMemBlock();
 		scheduleNewProcess();
 	}// removeCurrentProcess
 
 	/**
 	 * getRandomProcess
 	 * 
 	 * selects a non-Blocked process at random from the ProcessTable.
 	 * 
 	 * @return a reference to the ProcessControlBlock struct of the selected
 	 *         process -OR- null if no non-blocked process exists
 	 */
 	ProcessControlBlock getRandomProcess() {
 		// Calculate a random offset into the m_processes list
 		int offset = ((int) (Math.random() * 2147483647)) % m_processes.size();
 
 		// Iterate until a non-blocked process is found
 		ProcessControlBlock newProc = null;
 		for (int i = 0; i < m_processes.size(); i++) {
 			newProc = m_processes.get((i + offset) % m_processes.size());
 			if (!newProc.isBlocked()) {
 				return newProc;
 			}
 		}// for
 
 		return null; // no processes are Ready
 	}// getRandomProcess
 
 	/**
 	 * getNextProcess
 	 * 
 	 * selects a process from the process table to run. The scheduler ages
 	 * processes that have been starving, as well as promoting processes that do
 	 * a lot of I/O.
 	 * 
 	 * @return the process that should be scheduled next
 	 */
 	ProcessControlBlock getNextProcess() {
 		// If enough clock interrupts have passed, age any processes in the
 		// Ready state
 		if ((m_CPU.getTicks() / CPU.CLOCK_FREQ) % AGING_TIME == 0) {
 			for (int i = 0; i < m_processes.size(); i++) {
 				ProcessControlBlock nextProc = m_processes.elementAt(i);
 				if (!nextProc.isBlocked() && !nextProc.equals(m_currProcess)) {
 					nextProc.increasePriority(AGING_PRIORITY);
 				}
 			}
 		}
 
 		// Get the highest-priority process
 		ProcessControlBlock bestProc = null;
 		int bestPriority = -1;
 		// default to the current process as long as it's not blocked and
 		// still in the process table.
 		if (!m_currProcess.isBlocked() && m_processes.contains(m_currProcess)) {
 			bestProc = m_currProcess;
 			bestPriority = m_currProcess.getPriority() + PRIORITY_THRESHOLD;
 		}
 		for (int i = 0; i < m_processes.size(); i++) {
 			ProcessControlBlock nextProc = m_processes.elementAt(i);
 			if (!nextProc.isBlocked() && nextProc.getPriority() > bestPriority) {
 				bestProc = nextProc;
 				bestPriority = nextProc.getPriority();
 			}
 		}
 		return bestProc;
 	}
 
 	/**
 	 * scheduleNewProcess
 	 * 
 	 * Select a new process to run and load it into the CPU.
 	 */
 	public void scheduleNewProcess() {
 		printProcessTable();
 
 		if (m_processes.size() == 0) {
 			// No processes to run, just end the simulation
 			System.exit(0);
 		}
 
 		int oldID = m_currProcess.getProcessId();
 
 		ProcessControlBlock nextProc = getNextProcess();
 		if (nextProc == null) {
 			createIdleProcess();
 			return;
 		}
 		int newID = nextProc.getProcessId();
 		debugPrintln("Process ID " + oldID + " moving to Ready; process ID "
 		        + newID + " running");
 
 		// Load up the next process
 		if (!nextProc.equals(m_currProcess)) {
 			m_currProcess.save(m_CPU);
 			m_currProcess = nextProc;
 			m_currProcess.restore(m_CPU);
 		}
 
 	}// scheduleNewProcess
 
 	/**
 	 * createIdleProcess
 	 * 
 	 * creates a one instruction process that immediately exits. This is used to
 	 * buy time until device I/O completes and unblocks a legitimate process.
 	 * 
 	 * @see allocBlock()
 	 * 
 	 */
 	public void createIdleProcess() {
 		int progArr[] = { 0, 0, 0, 0, // SET r0=0
 		        0, 0, 0, 0, // SET r0=0 (repeated instruction to account for
 							// vagaries in student implementation of the CPU
 							// class)
 		        10, 0, 0, 0, // PUSH r0
 		        15, 0, 0, 0 }; // TRAP
 
 		// Ensure that our program receives enough space to store itself.
 		// This means getting the required number of pages and multiplying
 		// it by the page size.
 		int requiredPages = (16 / m_MMU.getPageSize()) + 1;
 		int allocSize = requiredPages * m_MMU.getPageSize();
 
 		// Initialize the starting position for this program
 		int memBlock = allocBlock(allocSize);
 
 		if (memBlock == -1) {
 			System.out.println("Failed to allocate idle process of size " + 16
 			        + ".");
 			System.exit(-1);
 		}
 
 		int baseAddr = memBlock;
 
 		// Load the program into RAM
 		for (int i = 0; i < progArr.length; i++) {
 			m_MMU.write(baseAddr + i, progArr[i]);
 		}
 
 		// Save the register info from the current process (if there is one)
 		if (m_currProcess != null) {
 			m_currProcess.save(m_CPU);
 		}
 
 		// Set the appropriate registers
 		m_CPU.setPC(baseAddr);
 		m_CPU.setSP(baseAddr + progArr.length + 10);
 		m_CPU.setBASE(baseAddr);
 		m_CPU.setLIM(baseAddr + progArr.length + 20);
 
 		// Save the relevant info as a new entry in m_processes
 		m_currProcess = new ProcessControlBlock(IDLE_PROC_ID);
 		m_processes.add(m_currProcess);
 
 	}// createIdleProcess
 
 	/**
 	 * addProgram
 	 * 
 	 * registers a new program with the simulated OS that can be used when the
 	 * current process makes an Exec system call. (Normally the program is
 	 * specified by the process via a filename but this is a simulation so the
 	 * calling process doesn't actually care what program gets loaded.)
 	 * 
 	 * @param prog
 	 *            the program to add
 	 * 
 	 */
 	public void addProgram(Program prog) {
 		m_programs.add(prog);
 	}// addProgram
 
 	/*
 	 * ======================================================================
 	 * Program Management Methods
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * createProcess
 	 * 
 	 * loads a program into RAM
 	 * 
 	 * @param prog
 	 *            the program to load into memory
 	 * @param allocSize
 	 *            the size allocated to the program in memory
 	 */
 	public void createProcess(Program prog, int allocSize) {
 		debugPrintln("Creating process ID " + m_nextProcessID);
 
 		// export the program and set necessary values in the CPU
 		int[] programInstructions = new int[prog.getSize()];
 		programInstructions = prog.export();
 
 		// If a process is currently running, save its registers
 		if (m_currProcess != null) {
 			m_currProcess.save(m_CPU);
 		}
 
 		// Ensure that our program receives enough space to store itself.
 		// This means getting the required number of pages and multiplying
 		// it by the page size.
 		int requiredPages = (allocSize / m_MMU.getPageSize()) + 1;
 		allocSize = requiredPages * m_MMU.getPageSize();
 
 		int memBlock = allocBlock(allocSize);
 
 		if (memBlock == -1) {
			printPageTable();
 			// We increment the PC to prevent an accidental shift back in
 			// syscallExec
 			m_CPU.setPC(m_CPU.getPC() + CPU.INSTRSIZE);
 			return;
 		}
 
 		m_CPU.setBASE(memBlock);
 		m_CPU.setLIM(m_CPU.getBASE() + allocSize);
 		m_CPU.setPC(m_CPU.getBASE());
 		m_CPU.setSP(m_CPU.getLIM() - 1);
 
 		// Make sure there is sufficient space in RAM
 		if (m_CPU.getLIM() >= m_MMU.getSize()) {
			printPageTable();
			System.err.println("Whelp");
 			System.exit(0);
 		}
 
 		// move through the allocated memory and load the program instructions
 		// into RAM,
 		// one at a time, breaking if we go past the memory limit
 		for (int i = 0; i < programInstructions.length; i++) {
 			m_MMU.write(memBlock + i, programInstructions[i]);
 		}
 
 		// Load up the new process
 		ProcessControlBlock newProc = new ProcessControlBlock(m_nextProcessID);
 		m_processes.add(newProc);
 		m_currProcess = newProc;
 		m_currProcess.save(m_CPU);
 		printMemAlloc();
 
 		// Prepare for the next process to load
 		m_nextProcessID++;
 	}// createProcess
 
 	/**
 	 * selectBlockedProcess
 	 * 
 	 * select a process to unblock that might be waiting to perform a given
 	 * action on a given device. This is a helper method for system calls and
 	 * interrupts that deal with devices.
 	 * 
 	 * @param dev
 	 *            the Device that the process must be waiting for
 	 * @param op
 	 *            the operation that the process wants to perform on the device.
 	 *            Use the SYSCALL constants for this value.
 	 * @param addr
 	 *            the address the process is reading from. If the operation is a
 	 *            Write or Open then this value can be anything
 	 * 
 	 * @return the process to unblock -OR- null if none match the given criteria
 	 */
 	public ProcessControlBlock selectBlockedProcess(Device dev, int op, int addr) {
 		ProcessControlBlock selected = null;
 		for (ProcessControlBlock pi : m_processes) {
 			if (pi.isBlockedForDevice(dev, op, addr)) {
 				selected = pi;
 				break;
 			}
 		}// for
 
 		return selected;
 	}// selectBlockedProcess
 
 	/**
 	 * pop
 	 * 
 	 * pops top value of off the stack
 	 * 
 	 * @return 0 if the stack is empty; else return the value on top of stack
 	 */
 	private int pop() {
 		if (m_CPU.getSP() < m_CPU.getLIM() - 1) {
 			m_CPU.setSP(m_CPU.getSP() + 1);
 			return m_MMU.read(m_CPU.getSP());
 		} else {
 			System.out.println("ERROR: STACK IS EMPTY");
 			System.exit(1);
 			return 0;
 		}
 	}
 
 	/**
 	 * push
 	 * 
 	 * pushes the value in the specified register on the stack
 	 * 
 	 * @param value
 	 *            the value to put on the stack
 	 * @return false if the stack is full, else true
 	 */
 	private boolean push(int value) {
 		if (m_CPU.getSP() >= m_CPU.getBASE()) {
 			m_MMU.write(m_CPU.getSP(), value);
 			m_CPU.setSP(m_CPU.getSP() - 1);
 			return true;
 		}
 		System.out.println("ERROR: STACK IS FULL");
 		return false;
 	}
 
 	// Method used by SOS for system calls: depending on what the value of
 	// SYSCALL_ID is, will call a helper method that corresponds to
 	// that value
 	public void systemCall() {
 		int syscallId = pop();
 		switch (syscallId) {
 		case SYSCALL_EXIT:
 			syscallExit();
 			break;
 		case SYSCALL_OUTPUT:
 			syscallOutput();
 			break;
 		case SYSCALL_GETPID:
 			syscallPID();
 			break;
 		case SYSCALL_COREDUMP:
 			syscallDump();
 			break;
 		case SYSCALL_OPEN:
 			syscallOpen();
 			break;
 		case SYSCALL_CLOSE:
 			syscallClose();
 			break;
 		case SYSCALL_READ:
 			syscallRead();
 			break;
 		case SYSCALL_WRITE:
 			syscallWrite();
 			break;
 		case SYSCALL_EXEC:
 			syscallExec();
 			break;
 		case SYSCALL_YIELD:
 			syscallYield();
 			break;
 		}
 
 	}
 
 	/*
 	 * ======================================================================
 	 * Interrupt Handlers
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * interruptIllegalMemoryAccess
 	 * 
 	 * Called when a process attempts to access memory outside its allocated
 	 * space. Prints an error and exits.
 	 * 
 	 * @param addr
 	 *            the address that was attempted to be accessed
 	 */
 	@Override
 	public void interruptIllegalMemoryAccess(int addr) {
 		System.out.println("Illegal memory access at: " + addr);
 		System.exit(0);
 	}
 
 	/**
 	 * interruptDivideByZero
 	 * 
 	 * Called when a process attempts to divide by zero. Prints an error and
 	 * exits.
 	 */
 	@Override
 	public void interruptDivideByZero() {
 		System.out.println("Cannot divide by zero");
 		System.exit(0);
 	}
 
 	/**
 	 * interruptIllegalInstruction
 	 * 
 	 * Called when a process attempts to execute an illegal instruction. Prints
 	 * an error and exits.
 	 * 
 	 * @param instr
 	 *            the illegal instruction that triggered the interrupt
 	 */
 	@Override
 	public void interruptIllegalInstruction(int[] instr) {
 		System.out.println("Illegal instruction given: " + instr);
 		System.exit(0);
 	}
 
 	/**
 	 * interruptIOReadComplete
 	 * 
 	 * Called by the CPU when a Read operation has been completed. Delivers the
 	 * read data to the process that requested it, then unblocks that process
 	 * and pushes a success code to its stack.
 	 * 
 	 * @param devID
 	 *            the ID of the device that was read from
 	 * @param addr
 	 *            the address on the device that was read from
 	 * @param data
 	 *            the data that was read
 	 */
 	public void interruptIOReadComplete(int devID, int addr, int data) {
 		// Find referenced device
 		for (DeviceInfo devInfo : m_devices) {
 			if (devInfo.getId() == devID) {
 				ProcessControlBlock blockedProcess = selectBlockedProcess(
 				        devInfo.getDevice(), SYSCALL_READ, addr);
 				blockedProcess.unblock();
 
 				// Push data and success code onto the process's stack
 				int sp = blockedProcess.getRegisterValue(CPU.SP);
 				m_MMU.write(sp, data);
 				sp--;
 				blockedProcess.setRegisterValue(CPU.SP, sp);
 				m_MMU.write(sp, SUCCESS);
 				sp--;
 				blockedProcess.setRegisterValue(CPU.SP, sp);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * interruptIOWriteComplete
 	 * 
 	 * Called by the CPU when a Write operation has been completed. Unblocks the
 	 * process that issued the command and pushes a success code to its stack.
 	 * 
 	 * @param devID
 	 *            the ID of the device that was written to
 	 * @param addr
 	 *            the address on the device that was written to
 	 */
 	public void interruptIOWriteComplete(int devID, int addr) {
 		// Find referenced device
 		for (DeviceInfo devInfo : m_devices) {
 			if (devInfo.getId() == devID) {
 				ProcessControlBlock blockedProcess = selectBlockedProcess(
 				        devInfo.getDevice(), SYSCALL_WRITE, addr);
 				blockedProcess.unblock();
 
 				// Push success code onto the process's stack
 				int sp = blockedProcess.getRegisterValue(CPU.SP);
 				m_MMU.write(sp, SUCCESS);
 				sp--;
 				blockedProcess.setRegisterValue(CPU.SP, sp);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * interruptClock
 	 * 
 	 * Handles a clock interrupt by calling the scheduler.
 	 */
 	public void interruptClock() {
 		// If we're currently running the idle process, just let it die
 		// (this avoids problems with processes placing themselves on the
 		// idle process's memory space)
 		if (m_currProcess.getProcessId() == IDLE_PROC_ID)
 			return;
 		scheduleNewProcess();
 	}
 
 	/*
 	 * ======================================================================
 	 * System Calls
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * syscallExit
 	 * 
 	 * Called when handling a SYSCALL_EXIT. Ends the process and schedules a new
 	 * one.
 	 */
 	private void syscallExit() {
 		removeCurrentProcess();
 	}
 
 	/**
 	 * syscallOutput
 	 * 
 	 * Called when handling a SYSCALL_OUTPUT. Pops the top value off of the
 	 * process's stack and prints it to console.
 	 */
 	private void syscallOutput() {
 		System.out.println("OUTPUT: " + pop());
 	}
 
 	/**
 	 * syscallPID
 	 * 
 	 * Called when handling a SYSCALL_GETPID. Pushes the current process's ID
 	 * (currently defined to be 42) to its stack.
 	 */
 	private void syscallPID() {
 		push(m_currProcess.getProcessId());
 	}
 
 	/**
 	 * syscallDump
 	 * 
 	 * Called when handling a SYSCALL_COREDUMP. Prints the contents of the
 	 * registers and top three values on the stack, then exits.
 	 */
 	private void syscallDump() {
 		System.out.println("CORE DUMPING:");
 		m_CPU.regDump();
 		System.out.println(pop() + "\n" + pop() + "\n" + pop());
 		syscallExit();
 	}
 
 	/**
 	 * syscallOpen
 	 * 
 	 * Pops a device ID off the stack, then adds the current process to that
 	 * device.
 	 */
 	private void syscallOpen() {
 		int devId = pop();
 		for (DeviceInfo deviceInfo : m_devices) {
 			if (deviceInfo.getId() == devId) {
 				if (!deviceInfo.getDevice().isSharable()
 				        && !deviceInfo.unused()) {
 					// Reserve the device for this process, but block the
 					// process until the device is next available
 					deviceInfo.addProcess(m_currProcess);
 					push(SUCCESS);
 					m_currProcess.block(m_CPU, deviceInfo.getDevice(),
 					        SYSCALL_OPEN, 0);
 					scheduleNewProcess();
 					return;
 				} else if (deviceInfo.containsProcess(m_currProcess)) {
 					// The process has already opened this device
 					push(DEVICE_ALREADY_OPEN);
 					return;
 				} else {
 					deviceInfo.addProcess(m_currProcess);
 					push(SUCCESS);
 					return;
 				}
 			}
 		}
 		// If we're here, the device doesn't exist
 		push(DEVICE_NOT_FOUND);
 	}
 
 	/**
 	 * syscallClose
 	 * 
 	 * Pops a device ID off the stack, then removes the current process from
 	 * that device.
 	 */
 	private void syscallClose() {
 		int devId = pop();
 		for (DeviceInfo deviceInfo : m_devices) {
 			if (deviceInfo.getId() == devId) {
 				if (!deviceInfo.containsProcess(m_currProcess)) {
 					// The process has not opened this device
 					push(DEVICE_NOT_OPEN);
 					return;
 				}
 				deviceInfo.removeProcess(m_currProcess);
 				push(SUCCESS);
 
 				// Check to see if any other processes are waiting
 				// on this device. If so, set one of them to the
 				// Running state.
 				ProcessControlBlock nextProc = selectBlockedProcess(
 				        deviceInfo.getDevice(), SYSCALL_OPEN, 0);
 				if (nextProc != null) {
 					nextProc.unblock();
 				}
 				return;
 			}
 		}
 		// If we're here, the device doesn't exist
 		push(DEVICE_NOT_FOUND);
 	}
 
 	/**
 	 * syscallRead
 	 * 
 	 * Pops an address and device ID from the stack. Then reads from the given
 	 * address on the specified device, pushing the result to the stack.
 	 */
 	private void syscallRead() {
 		int addr = pop();
 		int devId = pop();
 
 		for (DeviceInfo deviceInfo : m_devices) {
 			if (deviceInfo.getId() == devId) {
 				Device device = deviceInfo.getDevice();
 				// If device is unavailable, move process to Ready and schedule
 				// a new one
 				if (!device.isAvailable()) {
 					int pc = m_CPU.getPC();
 					m_CPU.setPC(pc - CPU.INSTRSIZE);
 					push(devId);
 					push(addr);
 					push(SYSCALL_READ);
 					scheduleNewProcess();
 					return;
 				}
 
 				if (!deviceInfo.containsProcess(m_currProcess)) {
 					// The process has not opened this device
 					push(DEVICE_NOT_OPEN);
 					return;
 				} else if (!device.isReadable()) {
 					// Device is write-only
 					push(DEVICE_WRITE_ONLY);
 					return;
 				}
 
 				device.read(addr);
 				m_currProcess.increasePriority(READ_PRIORITY);
 				m_currProcess.block(m_CPU, device, SYSCALL_READ, addr);
 				scheduleNewProcess();
 				return;
 			}
 		}
 		// If we're here, the device doesn't exist
 		push(DEVICE_NOT_FOUND);
 	}
 
 	/**
 	 * syscallWrite
 	 * 
 	 * Pops the value, address, and device ID from the stack. Then writes the
 	 * given data to the specified device.
 	 */
 	private void syscallWrite() {
 		int value = pop();
 		int addr = pop();
 		int devId = pop();
 
 		for (DeviceInfo deviceInfo : m_devices) {
 			if (deviceInfo.getId() == devId) {
 				Device device = deviceInfo.getDevice();
 				// If device is unavailable, move process to Ready and schedule
 				// a new one
 				if (!device.isAvailable()) {
 					int pc = m_CPU.getPC();
 					m_CPU.setPC(pc - CPU.INSTRSIZE);
 					push(devId);
 					push(addr);
 					push(value);
 					push(SYSCALL_WRITE);
 					scheduleNewProcess();
 					return;
 				}
 
 				if (!deviceInfo.containsProcess(m_currProcess)) {
 					// The process has not opened this device
 					push(DEVICE_NOT_OPEN);
 					return;
 				} else if (!device.isWriteable()) {
 					// Device is read-only
 					push(DEVICE_READ_ONLY);
 					return;
 				}
 				device.write(addr, value);
 				m_currProcess.increasePriority(WRITE_PRIORITY);
 				m_currProcess.block(m_CPU, device, SYSCALL_WRITE, addr);
 				scheduleNewProcess();
 				return;
 			}
 		}
 		// If we're here, the device doesn't exist
 		push(DEVICE_NOT_FOUND);
 	}
 
 	/**
 	 * syscallExec
 	 * 
 	 * creates a new process. The program used to create that process is chosen
 	 * semi-randomly from all the programs that have been registered with the OS
 	 * via {@link #addProgram}. Limits are put into place to ensure that each
 	 * process is run an equal number of times. If no programs have been
 	 * registered then the simulation is aborted with a fatal error.
 	 * 
 	 */
 	private void syscallExec() {
 		// If there is nothing to run, abort. This should never happen.
 		if (m_programs.size() == 0) {
 			System.err.println("ERROR!  syscallExec has no programs to run.");
 			System.exit(-1);
 		}
 
 		// find out which program has been called the least and record how many
 		// times it has been called
 		int leastCallCount = m_programs.get(0).callCount;
 		for (Program prog : m_programs) {
 			if (prog.callCount < leastCallCount) {
 				leastCallCount = prog.callCount;
 			}
 		}
 
 		// Create a vector of all programs that have been called the least
 		// number
 		// of times
 		Vector<Program> cands = new Vector<Program>();
 		for (Program prog : m_programs) {
 			cands.add(prog);
 		}
 
 		// Select a random program from the candidates list
 		Random rand = new Random();
 		int pn = rand.nextInt(m_programs.size());
 		Program prog = cands.get(pn);
 
 		// Determine the address space size using the default if available.
 		// Otherwise, use a multiple of the program size.
 		int allocSize = prog.getDefaultAllocSize();
 		if (allocSize <= 0) {
 			allocSize = prog.getSize() * 2;
 		}
 
 		// Load the program into RAM
 		createProcess(prog, allocSize);
 
 		m_CPU.setPC(m_CPU.getPC() - CPU.INSTRSIZE);
 	}// syscallExec
 
 	/**
 	 * syscallYield
 	 * 
 	 * Called when a program can yield to another process.
 	 */
 	private void syscallYield() {
 		scheduleNewProcess();
 	}// syscallYield
 
 	/*
 	 * ======================================================================
 	 * Memory Block Management Methods
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * printMemAlloc *DEBUGGING*
 	 * 
 	 * outputs the contents of m_freeList and m_processes to the console and
 	 * performs a fragmentation analysis. It also prints the value in RAM at the
 	 * BASE and LIMIT registers. This is useful for tracking down errors related
 	 * to moving process in RAM.
 	 * 
 	 * SIDE EFFECT: The contents of m_freeList and m_processes are sorted.
 	 * 
 	 */
 	private void printMemAlloc() {
 		// If verbose mode is off, do nothing
 		if (!m_verbose)
 			return;
 
 		// Print a header
 		System.out
 		        .println("\n----------========== Memory Allocation Table ==========----------");
 
 		// Sort the lists by address
 		Collections.sort(m_processes);
 		Collections.sort(m_freeList);
 
 		// Initialize references to the first entry in each list
 		MemBlock m = null;
 		ProcessControlBlock pi = null;
 		ListIterator<MemBlock> iterFree = m_freeList.listIterator();
 		ListIterator<ProcessControlBlock> iterProc = m_processes.listIterator();
 		if (iterFree.hasNext())
 			m = iterFree.next();
 		if (iterProc.hasNext())
 			pi = iterProc.next();
 
 		// Loop over both lists in order of their address until we run out of
 		// entries in both lists
 		while ((pi != null) || (m != null)) {
 			// Figure out the address of pi and m. If either is null, then
 			// assign
 			// them an address equivalent to +infinity
 			int pAddr = Integer.MAX_VALUE;
 			int mAddr = Integer.MAX_VALUE;
 			if (pi != null)
 				pAddr = pi.getRegisterValue(CPU.BASE);
 			if (m != null)
 				mAddr = m.getAddr();
 
 			// If the process has the lowest address then print it and get the
 			// next process
 			if (mAddr > pAddr) {
 				int size = pi.getRegisterValue(CPU.LIM)
 				        - pi.getRegisterValue(CPU.BASE);
 				System.out.print(" Process " + pi.processId + " (addr=" + pAddr
 				        + " size=" + size + " words");
 				System.out.print(" / " + (size / m_MMU.getPageSize())
 				        + " pages)");
 				System.out.print(" @BASE="
 				        + m_MMU.read(pi.getRegisterValue(CPU.BASE)) + " @SP="
 				        + m_MMU.read(pi.getRegisterValue(CPU.SP)));
 				System.out.println();
 				if (iterProc.hasNext()) {
 					pi = iterProc.next();
 				} else {
 					pi = null;
 				}
 			}// if
 			else {
 				// The free memory block has the lowest address so print it and
 				// get the next free memory block
 				System.out.println("    Open(addr=" + mAddr + " size="
 				        + m.getSize() + ")");
 				if (iterFree.hasNext()) {
 					m = iterFree.next();
 				} else {
 					m = null;
 				}
 			}// else
 		}// while
 
 		// Print a footer
 		System.out
 		        .println("-----------------------------------------------------------------");
 
 	}// printMemAlloc
 
 	/*
 	 * ======================================================================
 	 * Virtual Memory Methods
 	 * ----------------------------------------------------------------------
 	 */
 
 	/**
 	 * initPageTable
 	 * 
 	 * Initialize the page table to reflect the contents of RAM. Also set the
 	 * size of the page table so that we may reference this elsewhere in our OS
 	 * 
 	 */
 	private void initPageTable() {
 
 		m_sizeOfPageTable = m_MMU.getNumPages();
 
 		for (int i = 0; i < m_sizeOfPageTable; i++) {
 			m_RAM.write(i, i);
 		}
 
 	}// initPageTable
 
 	/**
 	 * printPageTable *DEBUGGING*
 	 * 
 	 * prints the page table in a human readable format
 	 * 
 	 */
 	private void printPageTable() {
 		// If verbose mode is off, do nothing
 		if (!m_verbose)
 			return;
 
 		// Print a header
 		System.out
 		        .println("\n----------========== Page Table ==========----------");
 
 		for (int i = 0; i < m_MMU.getNumPages(); i++) {
 			int entry = m_MMU.read(i);
 			int status = entry & m_MMU.getStatusMask();
 			int frame = entry & m_MMU.getPageMask();
 
 			System.out.println("" + i + "-->" + frame);
 		}
 
 		// Print a footer
 		System.out
 		        .println("-----------------------------------------------------------------");
 
 	}// printPageTable
 
 	/**
 	 * registerDevice
 	 * 
 	 * adds a new device to the list of devices managed by the OS
 	 * 
 	 * @param dev
 	 *            the device driver
 	 * @param id
 	 *            the id to assign to this device
 	 * 
 	 */
 	public void registerDevice(Device dev, int id) {
 		m_devices.add(new DeviceInfo(dev, id));
 	}// registerDevice
 
 	/**
 	 * allocBlock
 	 * 
 	 * @param size
 	 *            the requested allocation size
 	 * @return the starting address of our allocated block OR return -1 if we
 	 *         couldn't allocate memory.
 	 */
 	private int allocBlock(int size) {
 
 		int totalFree = 0;
 
 		for (MemBlock mb : m_freeList) {
 
 			totalFree += mb.m_size;
 
 			// We found a free block that's juuuuuust right.
 			if (mb.m_size == size) {
 				m_freeList.remove(mb);
 				return mb.getAddr();
 			} else if (mb.m_size > size) {
 				// We found a free block that's Good Enough(tm).
 				int returnAddress = mb.getAddr();
 
 				// Shift our free block rather than removing
 				// and allocating a new one.
 				mb.m_size -= size;
 				mb.m_addr += size;
 
 				return returnAddress;
 			}
 		}// for-each
 
 		// We ran out of memory :(
 		if (totalFree < size) {
 			return -1;
 		}
 
 		// Clean up our memory subsystem
 		mergeFraggedProcesses();
 		mergeFraggedMemory();
 
 		// Keep running until we find a proper memory block.
 		return allocBlock(size);
 	}// allocBlock
 
 	/**
 	 * mergeFraggedMemory
 	 * 
 	 * This function compares a memory block with its immediate predecessor. If
 	 * the two blocks are next to each other, we merge them; otherwise
 	 * continues.
 	 * 
 	 */
 	private void mergeFraggedMemory() {
 		Collections.sort(m_freeList);
 		for (int i = 1; i < m_freeList.size(); i++) {
 
 			MemBlock previousBlock = m_freeList.elementAt(i - 1);
 			MemBlock currentBlock = m_freeList.elementAt(i);
 
 			if (previousBlock.m_addr + currentBlock.m_size == currentBlock.m_addr) {
 				previousBlock.m_size += currentBlock.m_size;
 				m_freeList.removeElement(currentBlock);
 				--i;
 			}
 		}
 	}// mergeFraggedMemory
 
 	/**
 	 * mergeFraggedProcesses
 	 * 
 	 * Merge fragmented processes into a continuous block of memory.
 	 * 
 	 */
 	// This suppress statement gets rid of those annoying
 	// yellow squiggles every time we access a static variable
 	// in m_CPU.
 	@SuppressWarnings("static-access")
 	private void mergeFraggedProcesses() {
 		Collections.sort(m_processes);
 
 		// This points to the end of our process block,
 		// anything after endProcBlock is free memory.
 		int endProcBlock = m_sizeOfPageTable;
 		for (ProcessControlBlock pcb : m_processes) {
 
 			int procSize = pcb.registers[m_CPU.LIM] - pcb.registers[CPU.BASE];
 
 			pcb.move(endProcBlock);
 			endProcBlock += procSize;
 		}// for-each
 
 		// All of our processes are in one block of RAM, so clear the existing
 		// free
 		// memory vector and fill it with the remaining free RAM.
 		m_freeList.clear();
 		m_freeList.add(new MemBlock(endProcBlock, m_MMU.getSize()));
 	}// mergeFraggedProcesses
 
 	/**
 	 * freeCurrProcessMemBlock
 	 * 
 	 * Frees the current processes memory block and then merges any contiguous
 	 * free memory blocks.
 	 * 
 	 */
 	// This suppress statement gets rid of those annoying
 	// yellow squiggles every time we access a static variable
 	// in m_CPU.
 	@SuppressWarnings("static-access")
 	private void freeCurrProcessMemBlock() {
 
 		int currBase = m_currProcess.registers[m_CPU.BASE];
 		int currLim = m_currProcess.registers[m_CPU.LIM];
 
 		// Add a free memory block at the current BASE address and
 		// make it the size of our current process.
 		m_freeList.add(new MemBlock(currBase, currLim - currBase));
 
 		mergeFraggedProcesses();
 		mergeFraggedMemory();
 
 	}// freeCurrProcessMemBlock
 
 	// ======================================================================
 	// Inner Classes
 	// ----------------------------------------------------------------------
 
 	/**
 	 * class ProcessControlBlock
 	 * 
 	 * This class contains information about a currently active process.
 	 */
 	private class ProcessControlBlock implements
 	        Comparable<ProcessControlBlock> {
 		/**
 		 * a unique id for this process
 		 */
 		private int processId = 0;
 
 		/**
 		 * These are the process' current registers. If the process is in the
 		 * "running" state then these are out of date
 		 */
 		private int[] registers = null;
 
 		/**
 		 * If this process is blocked a reference to the Device is stored here
 		 */
 		private Device blockedForDevice = null;
 
 		/**
 		 * If this process is blocked a reference to the type of I/O operation
 		 * is stored here (use the SYSCALL constants defined in SOS)
 		 */
 		private int blockedForOperation = -1;
 
 		/**
 		 * If this process is blocked reading from a device, the requested
 		 * address is stored here.
 		 */
 		private int blockedForAddr = -1;
 
 		/**
 		 * the time it takes to load and save registers, specified as a number
 		 * of CPU ticks
 		 */
 		private static final int SAVE_LOAD_TIME = 30;
 
 		/**
 		 * Used to store the system time when a process is moved to the Ready
 		 * state.
 		 */
 		private int lastReadyTime = -1;
 
 		/**
 		 * Used to store the number of times this process has been in the ready
 		 * state
 		 */
 		private int numReady = 0;
 
 		/**
 		 * Used to store the maximum starve time experienced by this process
 		 */
 		private int maxStarve = -1;
 
 		/**
 		 * Used to store the average starve time for this process
 		 */
 		private double avgStarve = 0;
 
 		/**
 		 * Current priority (greater number -> higher priority)
 		 */
 		private int priority = 0;
 
 		/**
 		 * constructor
 		 * 
 		 * @param pid
 		 *            a process id for the process. The caller is responsible
 		 *            for making sure it is unique.
 		 */
 		public ProcessControlBlock(int pid) {
 			this.processId = pid;
 		}
 
 		/**
 		 * @return the current process' id
 		 */
 		public int getProcessId() {
 			return this.processId;
 		}
 
 		/**
 		 * save
 		 * 
 		 * saves the current CPU registers into this.registers
 		 * 
 		 * @param cpu
 		 *            the CPU object to save the values from
 		 */
 		public void save(CPU cpu) {
 			// A context switch is expensive. We simluate that here by
 			// adding ticks to m_CPU
 			m_CPU.addTicks(SAVE_LOAD_TIME);
 
 			// Save the registers
 			int[] regs = cpu.getRegisters();
 			this.registers = new int[CPU.NUMREG];
 			for (int i = 0; i < CPU.NUMREG; i++) {
 				this.registers[i] = regs[i];
 			}
 
 			// Assuming this method is being called because the process is
 			// moving
 			// out of the Running state, record the current system time for
 			// calculating starve times for this process. If this method is
 			// being called for a Block, we'll adjust lastReadyTime in the
 			// unblock method.
 			numReady++;
 			lastReadyTime = m_CPU.getTicks();
 
 		}// save
 
 		/**
 		 * restore
 		 * 
 		 * restores the saved values in this.registers to the current CPU's
 		 * registers
 		 * 
 		 * @param cpu
 		 *            the CPU object to restore the values to
 		 */
 		public void restore(CPU cpu) {
 			// A context switch is expensive. We simluate that here by
 			// adding ticks to m_CPU
 			m_CPU.addTicks(SAVE_LOAD_TIME);
 
 			// Restore the register values
 			int[] regs = cpu.getRegisters();
 			for (int i = 0; i < CPU.NUMREG; i++) {
 				regs[i] = this.registers[i];
 			}
 
 			// Record the starve time statistics
 			int starveTime = m_CPU.getTicks() - lastReadyTime;
 			if (starveTime > maxStarve) {
 				maxStarve = starveTime;
 			}
 			double d_numReady = (double) numReady;
 			avgStarve = avgStarve * (d_numReady - 1.0) / d_numReady;
 			avgStarve = avgStarve + (starveTime * (1.0 / d_numReady));
 		}// restore
 
 		/**
 		 * block
 		 * 
 		 * blocks the current process to wait for I/O. This includes saving the
 		 * process' registers. The caller is responsible for calling
 		 * {@link CPU#scheduleNewProcess} after calling this method.
 		 * 
 		 * @param cpu
 		 *            the CPU that the process is running on
 		 * @param dev
 		 *            the Device that the process must wait for
 		 * @param op
 		 *            the operation that the process is performing on the
 		 *            device. Use the SYSCALL constants for this value.
 		 * @param addr
 		 *            the address the process is reading from. If the operation
 		 *            is a Write or Open then this value can be anything
 		 */
 		public void block(CPU cpu, Device dev, int op, int addr) {
 			blockedForDevice = dev;
 			blockedForOperation = op;
 			blockedForAddr = addr;
 
 		}// block
 
 		/**
 		 * unblock
 		 * 
 		 * moves this process from the Blocked (waiting) state to the Ready
 		 * state.
 		 * 
 		 */
 		public void unblock() {
 			// Reset the info about the block
 			blockedForDevice = null;
 			blockedForOperation = -1;
 			blockedForAddr = -1;
 
 			// Assuming this method is being called because the process is
 			// moving
 			// from the Blocked state to the Ready state, record the current
 			// system time for calculating starve times for this process.
 			lastReadyTime = m_CPU.getTicks();
 
 		}// unblock
 
 		/**
 		 * isBlocked
 		 * 
 		 * @return true if the process is blocked
 		 */
 		public boolean isBlocked() {
 			return (blockedForDevice != null);
 		}// isBlocked
 
 		/**
 		 * isBlockedForDevice
 		 * 
 		 * Checks to see if the process is blocked for the given device,
 		 * operation and address. If the operation is not an open, the given
 		 * address is ignored.
 		 * 
 		 * @param dev
 		 *            check to see if the process is waiting for this device
 		 * @param op
 		 *            check to see if the process is waiting for this operation
 		 * @param addr
 		 *            check to see if the process is reading from this address
 		 * 
 		 * @return true if the process is blocked by the given parameters
 		 */
 		public boolean isBlockedForDevice(Device dev, int op, int addr) {
 			if ((blockedForDevice == dev) && (blockedForOperation == op)) {
 				if (op == SYSCALL_OPEN) {
 					return true;
 				}
 
 				if (addr == blockedForAddr) {
 					return true;
 				}
 			}// if
 
 			return false;
 		}// isBlockedForDevice
 
 		/**
 		 * compareTo
 		 * 
 		 * compares this to another ProcessControlBlock object based on the BASE
 		 * addr register. Read about Java's Collections class for info on how
 		 * this method can be quite useful to you.
 		 */
 		public int compareTo(ProcessControlBlock pi) {
 			return this.registers[CPU.BASE] - pi.registers[CPU.BASE];
 		}
 
 		/**
 		 * getRegisterValue
 		 * 
 		 * Retrieves the value of a process' register that is stored in this
 		 * object (this.registers).
 		 * 
 		 * @param idx
 		 *            the index of the register to retrieve. Use the constants
 		 *            in the CPU class
 		 * @return one of the register values stored in in this object or -999
 		 *         if an invalid index is given
 		 */
 		public int getRegisterValue(int idx) {
 			if ((idx < 0) || (idx >= CPU.NUMREG)) {
 				return -999; // invalid index
 			}
 
 			return this.registers[idx];
 		}// getRegisterValue
 
 		/**
 		 * setRegisterValue
 		 * 
 		 * Sets the value of a process' register that is stored in this object
 		 * (this.registers).
 		 * 
 		 * @param idx
 		 *            the index of the register to set. Use the constants in the
 		 *            CPU class. If an invalid index is given, this method does
 		 *            nothing.
 		 * @param val
 		 *            the value to set the register to
 		 */
 		public void setRegisterValue(int idx, int val) {
 			if ((idx < 0) || (idx >= CPU.NUMREG)) {
 				return; // invalid index
 			}
 
 			this.registers[idx] = val;
 		}// setRegisterValue
 
 		/**
 		 * @return the last time this process was put in the Ready state
 		 */
 		public long getLastReadyTime() {
 			return lastReadyTime;
 		}
 
 		/**
 		 * @return Process's current priority
 		 */
 		public int getPriority() {
 			return priority;
 		}
 
 		/**
 		 * Adjusts the process's priority upward.
 		 * 
 		 * @param amount
 		 *            amount to increase the priority by
 		 */
 		public void increasePriority(int amount) {
 			priority += amount;
 		}
 
 		/**
 		 * overallAvgStarve
 		 * 
 		 * @return the overall average starve time for all currently running
 		 *         processes
 		 * 
 		 */
 		public double overallAvgStarve() {
 			double result = 0.0;
 			int count = 0;
 			for (ProcessControlBlock pi : m_processes) {
 				if (pi.avgStarve > 0) {
 					result = result + pi.avgStarve;
 					count++;
 				}
 			}
 			if (count > 0) {
 				result = result / count;
 			}
 
 			return result;
 		}// overallAvgStarve
 
 		/**
 		 * toString **DEBUGGING**
 		 * 
 		 * @return a string representation of this class
 		 */
 		public String toString() {
 			// Print the Process ID and process state (READY, RUNNING, BLOCKED)
 			String result = "Process id " + processId + " ";
 			if (isBlocked()) {
 				result = result + "is BLOCKED for ";
 				// Print device, syscall and address that caused the BLOCKED
 				// state
 				if (blockedForOperation == SYSCALL_OPEN) {
 					result = result + "OPEN";
 				} else {
 					result = result + "WRITE @" + blockedForAddr;
 				}
 				for (DeviceInfo di : m_devices) {
 					if (di.getDevice() == blockedForDevice) {
 						result = result + " on device #" + di.getId();
 						break;
 					}
 				}
 				result = result + ": ";
 			} else if (this == m_currProcess) {
 				result = result + "is RUNNING: ";
 			} else {
 				result = result + "is READY: ";
 			}
 
 			// Print the register values stored in this object. These don't
 			// necessarily match what's on the CPU for a Running process.
 			if (registers == null) {
 				result = result + "<never saved>";
 				return result;
 			}
 
 			for (int i = 0; i < CPU.NUMGENREG; i++) {
 				result = result + ("r" + i + "=" + registers[i] + " ");
 			}// for
 			result = result + ("PC=" + registers[CPU.PC] + " ");
 			result = result + ("SP=" + registers[CPU.SP] + " ");
 			result = result + ("BASE=" + registers[CPU.BASE] + " ");
 			result = result + ("LIM=" + registers[CPU.LIM] + " ");
 
 			// Print the starve time statistics for this process
 			result = result + "\n\t\t\t";
 			result = result + " Max Starve Time: " + maxStarve;
 			result = result + " Avg Starve Time: " + avgStarve;
 
 			return result;
 		}// toString
 
 		/**
 		 * move
 		 * 
 		 * Moves this process to the given address.
 		 * 
 		 * @param newBase
 		 *            the new address to move to
 		 * @return true if the move was successful, false otherwise
 		 */
 		// This suppress statement gets rid of those annoying
 		// yellow squiggles every time we access a static variable
 		// in m_CPU.
 		@SuppressWarnings("static-access")
 		public boolean move(int newBase) {
 
 			// If we are currently the running process,
 			// make sure we save our registers.
 			if (this == m_currProcess) {
 				m_currProcess.save(m_CPU);
 			}
 
 			int base = this.registers[m_CPU.BASE];
 			int lim = this.registers[m_CPU.LIM];
 
 			// Check for invalid memory access
 			if ((newBase < 0) || ((newBase + lim) > m_MMU.getSize())) {
 				return false;
 			}
 
 			// From the base to the limit of our process,
 			// move everything according to our shiftAmount
 			for (int i = 0; i + base < lim; i++) {
 				// If the current base is a multiple of page size, we need
 				// to swap the frame references.
 				if ((i + base) % m_MMU.getPageSize() == 0) {
 					int currFrame = getFrameNum(i + base);
 					int newFrame = getFrameNum(i + newBase);
 					m_RAM.write(newFrame, currFrame);
 					m_RAM.write(currFrame, newFrame);
 				}
 			}
 
 			int shiftAmount = newBase - base; // the amount we need to shift our
 											  // process by
 			// Don't forget to move the registers as well.
 			this.registers[m_CPU.BASE] += shiftAmount;
 			this.registers[m_CPU.LIM] += shiftAmount;
 			this.registers[m_CPU.SP] += shiftAmount;
 			this.registers[m_CPU.PC] += shiftAmount;
 
 			// Restore our current process.
 			if (this == m_currProcess) {
 				m_currProcess.restore(m_CPU);
 			}
 
 			debugPrintln("Process " + this.processId + " has moved from "
 			        + base + " to " + newBase);
 			return true;
 		}// move
 
 	}// class ProcessControlBlock
 
 	private int getFrameNum(int virtAddr) {
 		int pageNum = (virtAddr & m_MMU.getPageMask()) >>> m_MMU
 		        .getOffsetSize();
 		return m_RAM.read(pageNum);
 	}
 
 	/**
 	 * class DeviceInfo
 	 * 
 	 * This class contains information about a device that is currently
 	 * registered with the system.
 	 */
 	private class DeviceInfo {
 		/** every device has a unique id */
 		private int id;
 		/** a reference to the device driver for this device */
 		private Device device;
 		/** a list of processes that have opened this device */
 		private Vector<ProcessControlBlock> procs;
 
 		/**
 		 * constructor
 		 * 
 		 * @param d
 		 *            a reference to the device driver for this device
 		 * @param initID
 		 *            the id for this device. The caller is responsible for
 		 *            guaranteeing that this is a unique id.
 		 */
 		public DeviceInfo(Device d, int initID) {
 			this.id = initID;
 			this.device = d;
 			this.procs = new Vector<ProcessControlBlock>();
 		}
 
 		/** @return the device's id */
 		public int getId() {
 			return this.id;
 		}
 
 		/** @return this device's driver */
 		public Device getDevice() {
 			return this.device;
 		}
 
 		/** Register a new process as having opened this device */
 		public void addProcess(ProcessControlBlock pi) {
 			procs.add(pi);
 		}
 
 		/** Register a process as having closed this device */
 		public void removeProcess(ProcessControlBlock pi) {
 			procs.remove(pi);
 		}
 
 		/** Does the given process currently have this device opened? */
 		public boolean containsProcess(ProcessControlBlock pi) {
 			return procs.contains(pi);
 		}
 
 		/** Is this device currently not opened by any process? */
 		public boolean unused() {
 			return procs.size() == 0;
 		}
 
 	}// class DeviceInfo
 
 	/**
 	 * class MemBlock
 	 * 
 	 * This class contains relevant info about a memory block in RAM.
 	 * 
 	 */
 	private class MemBlock implements Comparable<MemBlock> {
 		/** the address of the block */
 		private int m_addr;
 		/** the size of the block */
 		private int m_size;
 
 		/**
 		 * ctor does nothing special
 		 */
 		public MemBlock(int addr, int size) {
 			m_addr = addr;
 			m_size = size;
 		}
 
 		/** accessor methods */
 		public int getAddr() {
 			return m_addr;
 		}
 
 		public int getSize() {
 			return m_size;
 		}
 
 		/**
 		 * compareTo
 		 * 
 		 * compares this to another MemBlock object based on address
 		 */
 		public int compareTo(MemBlock m) {
 			return this.m_addr - m.m_addr;
 		}
 
 	}// class MemBlock
 
 };// class SOS
