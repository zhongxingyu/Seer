 package simulanator;
 
 import static assemblernator.ErrorReporting.makeError;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import ulutil.IOFormat;
 
 import assemblernator.Assembler;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 
 /**
  * A class representing an entire machine state.
  * 
  * @author Josh Ventura
  * @date Apr 28, 2012; 1:09:45 AM
  */
 public class Machine {
 	/** The size of the memory of the URBAN machine. */
 	public static final int memorySizeInWords = 4096;
 
 	/** Our eight machine registers. */
 	private int[] registers;
 	/** Our seven index registers: Index register 0 is unused. */
 	private int[] indexRegisters;
 	/** Our entire memory; all 16 kibibytes of it. */
 	private int[] memory;
 	/** The program stack: a Stack of Integers. */
 	public Stack<Integer> stack = new Stack<Integer>();
 
 
 	// ========================================================================
 	// === STREAM AND LISTENER INTERFACES =====================================
 	// ========================================================================
 
 	/** Interface to handle input. */
 	public interface URBANInputStream {
 		/**
 		 * Prompt the user for a string, blocking until the user submits it.
 		 * 
 		 * @return A string given by the user.
 		 */
 		String getString();
 	}
 
 	/** Interface to handle input. */
 	public interface URBANOutputStream {
 		/**
 		 * Display a string to the user.
 		 * 
 		 * @param str
 		 *            The string to display.
 		 */
 		void putString(String str);
 	}
 
 	/** Interface for listening for memory changes */
 	public interface MemoryListener {
 		/**
 		 * @param startAddr
 		 *            The first address of the modified block.
 		 * @param endAddr
 		 *            The last address in the modified block.
 		 */
 		void updatedMemory(int startAddr, int endAddr);
 	}
 
 	/** Interface for listening for memory changes */
 	public interface RegisterListener {
 		/**
 		 * @param index
 		 *            True if the modification affects index registers, false if
 		 *            it affects arithmetic registers.
 		 * @param firstRegister
 		 *            The ID of the first modified register.
 		 * @param lastRegister
 		 *            The ID of the last modified register.
 		 */
 		void updatedRegisters(boolean index, int firstRegister, int lastRegister);
 	}
 
 	/** Interface for listening for fetch-decode-execute cycles */
 	public interface ThreadListener {
 		/**
 		 * Called when a new thread is created.
 		 * 
 		 * @param threadID
 		 *            The ID of the new thread that was created.
 		 */
 		void createThread(int threadID);
 
 		/**
 		 * @param threadID
 		 *            The ID of the thread whose LC has changed.
 		 * @param newlc
 		 *            The new value of the location counter.
 		 */
 		void updatedLC(int threadID, int newlc);
 
 		/** Called after the fetch-decode-execute cycle completes */
 		void fetchDecodeExecute();
 
 		/**
 		 * Called when a thread terminates.
 		 * 
 		 * @param threadID
 		 *            The ID of the thread that was destroyed.
 		 */
 		void destroyThread(int threadID);
 	}
 
 	// ========================================================================
 	// === MACHINE STREAMS AND LISTENERS ======================================
 	// ========================================================================
 
 	/** An error channel to which any access violations can be reported. */
 	public ErrorHandler hErr;
 	/** Out input stream, through which the user will be prompted for input. */
 	public URBANInputStream input;
 	/** An output stream to which messages can be printed. */
 	public URBANOutputStream output;
 	/** Our memory listeners */
 	private ArrayList<MemoryListener> memoryListeners = new ArrayList<MemoryListener>();
 	/** Our register listeners */
 	private ArrayList<RegisterListener> registerListeners = new ArrayList<RegisterListener>();
 	/** Our thread listeners */
 	private ArrayList<ThreadListener> threadListeners = new ArrayList<ThreadListener>();
 
 	/**
 	 * @param ml
 	 *            The memory listener to add.
 	 */
 	public void addMemoryListener(MemoryListener ml) {
 		memoryListeners.add(ml);
 	}
 
 	/**
 	 * @param rl
 	 *            The register listener to add.
 	 */
 	public void addRegisterListener(RegisterListener rl) {
 		registerListeners.add(rl);
 	}
 
 	/**
 	 * @param tl
 	 *            The register listener to add.
 	 */
 	public void addThreadListener(ThreadListener tl) {
 		threadListeners.add(tl);
 	}
 
 	// ========================================================================
 	// === MACHINE STATE ======================================================
 	// ========================================================================
 	/** The current program counter. */
 	private int lc;
 	/** The current instruction. */
 	public int instruction;
 	/** True if the machine is still running. */
 	public boolean running;
 	/** The zero-based ID of this thread. */
 	private int threadID;
 
 	/** A map of active threads. */
 	TreeMap<Integer, Machine> threads;
 	/** The main thread */
 	Machine mainThread;
 
 	/**
 	 * @param err
 	 *            The error handler to which any problems are reported.
 	 * @param uis
 	 *            The URBAN input stream for this machine.
 	 * @param uos
 	 *            The URBAN output stream for this machine.
 	 */
 	public Machine(ErrorHandler err, URBANInputStream uis, URBANOutputStream uos) {
 		hErr = err;
 		input = uis;
 		output = uos;
 		registers = new int[8];
 		indexRegisters = new int[8];
 		memory = new int[memorySizeInWords];
 		mainThread = this;
 		threads = new TreeMap<Integer, Machine>();
 	}
 
 	/**
 	 * Fork constructor, for use in SYS_FORK.
 	 * 
 	 * @param forkFrom
 	 *            The machine to be forked.
 	 * @param threadLC
 	 *            The LC at which to begin this thread.
 	 */
 	public Machine(Machine forkFrom, int threadLC) {
 		hErr = forkFrom.hErr;
 		input = forkFrom.input;
 		output = forkFrom.output;
 		memoryListeners = forkFrom.memoryListeners;
 		registerListeners = forkFrom.registerListeners;
 		threadListeners = forkFrom.threadListeners;
 
 		registers = forkFrom.registers;
 		indexRegisters = forkFrom.indexRegisters;
 		memory = forkFrom.memory;
 		mainThread = forkFrom.mainThread;
 		threads = forkFrom.threads;
 
 		lc = threadLC;
 	}
 
 	/**
 	 * Creates a string dump of this machine state, according to spec.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 28, 2012; 1:23:42 AM
 	 * @modified UNMODIFIED
 	 * @tested Apr 28, 2012; 1:41:00 AM: Tested with empty memory.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param level
 	 *            The detail level for this dump; a value of one dumps all
 	 *            registers, a value of two dumps the memory, and a value of
 	 *            three dumps both.
 	 * @return A String containing the dump of this machine state.
 	 * @specRef C1
 	 * @specRef DUMP Format
 	 */
 	public String dump(int level) {
 		String res = "";
 		if ((level & 1) != 0) {
 			res += "LC " + getLC() + "  WORD="
 					+ IOFormat.formatHexInteger(instruction, 8) + "\n";
 			for (int i = 0; i < 8; ++i)
 				res += "R" + i + "="
 						+ IOFormat.formatHexInteger(registers[i], 8)
 						+ (i % 4 == 3 ? "\n" : " ");
 			
 			for (int i = 1; i < 8; ++i)
 				res += "XR" + i + "="
						+ IOFormat.formatHexInteger(indexRegisters[i], 8)
 						+ (i == 4 || i == 7 ? "\n" : " ");
 			
 			res += "\n";
 		}
 		if ((level & 2) != 0) {
 			for (int row = 0; row < memorySizeInWords; row += 8) {
 				res += IOFormat.formatHexInteger(row, 4) + ": ";
 				for (int col = row; col < row + 8 && col < memorySizeInWords; ++col)
 					res += " " + IOFormat.formatHexInteger(memory[col], 8);
 				res += "\n";
 			}
 		}
 		return res;
 	}
 
 	/** @see java.lang.Object#toString() */
 	@Override public String toString() {
 		return dump(3);
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 18, 2012; 7:50:40 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 */
 	public void runThread() {
 		(new Thread() {
 			@Override public void run() {
 				Machine m = new Machine(Machine.this, lc);
 				m.run();
 			}
 		}).start();
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 29, 2012; 1:36:39 AM
 	 */
 	public void runAnchored() {
 		run();
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 29, 2012; 1:24:53 AM
 	 * @param execStart
 	 *            The start LC.
 	 */
 	public void runThread(int execStart) {
 		lc = execStart;
 		runThread();
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 23, 2012; 4:26:42 PM
 	 * @param m
 	 *            The calling machine thread.
 	 * @return The first available thread ID.
 	 */
 	private synchronized int getAvailableThreadId(Machine m) {
 		int lowkey = 0;
 		SortedMap<Integer, Machine> TSthreads = Collections
 				.synchronizedSortedMap(threads);
 		for (Entry<Integer, Machine> e : TSthreads.entrySet()) {
 			if (e.getKey() > lowkey)
 				break;
 			++lowkey;
 		}
 		TSthreads.put(lowkey, m);
 		return lowkey;
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 23, 2012; 4:39:52 PM
 	 * @param threadID
 	 *            The ID of the thread to remove.
 	 */
 	private synchronized void removeThread(int threadID) {
 		SortedMap<Integer, Machine> TSthreads = Collections
 				.synchronizedSortedMap(threads);
 		TSthreads.remove(threadID);
 	}
 
 	/**
 	 * Threaded method to run. from the current LC.
 	 * 
 	 * @author Josh Ventura
 	 * @date May 23, 2012; 4:04:53 PM
 	 */
 	private void run() {
 		threadID = mainThread.getAvailableThreadId(this);
 		running = true;
 		System.out.println("Starting run from lc = " + lc);
 		for (ThreadListener tl : threadListeners)
 			tl.createThread(threadID);
 		while (running) {
 			if (lc < 0 || lc >= memory.length) {
 				hErr.reportError(
 						makeError("runLCwentOOR", Integer.toString(lc, 16)),
 						-1, -1);
 				running = false;
 				break;
 			}
 			instruction = memory[lc++];
 			for (ThreadListener tl : threadListeners)
 				tl.updatedLC(threadID, lc);
 			Integer opcode = (instruction & 0xFC000000) >>> 26;
 			Instruction ins = Assembler.byteCodes.get(opcode);
 			if (ins == null) {
 				hErr.reportError(
 						makeError("runInvOPCode",
 								IOFormat.formatBinInteger(opcode, 6)), -1, -1);
 			}
 			else
 				ins.execute(instruction, this);
 			for (ThreadListener tl : threadListeners)
 				tl.fetchDecodeExecute();
 		}
 		for (ThreadListener tl : threadListeners)
 			tl.destroyThread(threadID);
 		mainThread.removeThread(threadID);
 	}
 
 	/**
 	 * @param addr
 	 *            The address of the desired memory word.
 	 * @return The word at the given address.
 	 */
 	public int getMemory(int addr) {
 		if (addr < 0 || addr > memorySizeInWords)
 			hErr.reportError(
 					makeError("runMemOOR", IOFormat.formatHexInteger(addr, 4)),
 					-1, -1);
 		return memory[addr];
 	}
 
 	/**
 	 * Standard setter; fires memory change.
 	 * 
 	 * @param addr
 	 *            The address of the desired memory word.
 	 * @param word
 	 *            The new word to place at that address.
 	 */
 	public void setMemory(int addr, int word) {
 		memory[addr] = word;
 		for (MemoryListener ml : memoryListeners)
 			ml.updatedMemory(addr, addr);
 	}
 
 	/**
 	 * Standard setter, but does not fire memory change.
 	 * 
 	 * @param addr
 	 *            The address of the desired memory word.
 	 * @param word
 	 *            The new word to place at that address.
 	 */
 	public void setMemoryDiscretely(int addr, int word) {
 		memory[addr] = word;
 	}
 
 	/** @return The current location counter. */
 	public int getLC() {
 		return lc;
 	}
 
 	/**
 	 * Standard setter; fires location counter change.
 	 * 
 	 * @param lc
 	 *            The new location counter.
 	 */
 	public void setLC(int lc) {
 		this.lc = lc;
 		for (ThreadListener tl : threadListeners)
 			tl.updatedLC(threadID, lc);
 	}
 
 	/**
 	 * Standard setter; fires register change.
 	 * 
 	 * @param register
 	 *            The ID of the register to set, 0-7.
 	 * @param word
 	 *            The new value for the register.
 	 */
 	public void setRegister(int register, int word) {
 		registers[register] = word;
 		for (RegisterListener rl : registerListeners)
 			rl.updatedRegisters(false, register, register);
 	}
 
 	/**
 	 * Standard setter; fires register change.
 	 * 
 	 * @param register
 	 *            The ID of the index register to set, 1-7.
 	 * @param word
 	 *            The new value for the register.
 	 */
 	public void setIndexRegister(int register, int word) {
 		indexRegisters[register] = word;
 		for (RegisterListener rl : registerListeners)
 			rl.updatedRegisters(true, register, register);
 	}
 
 	/**
 	 * @param reg
 	 *            The register to get.
 	 * @return The value of the index register with the given ID.
 	 */
 	public int getIndexRegister(int reg) {
 		return indexRegisters[reg];
 	}
 
 	/**
 	 * @param reg
 	 *            The register to get.
 	 * @return The value of the index register with the given ID.
 	 */
 	public int getRegister(int reg) {
 		return registers[reg];
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 23, 2012; 3:49:01 PM
 	 * @return An immutable copy of the thread map.
 	 */
 	public SortedMap<Integer, Machine> getThreadData() {
 		return Collections.unmodifiableSortedMap(threads);
 	}
 }
