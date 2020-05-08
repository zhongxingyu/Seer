 package nachos.vm;
 
 import nachos.machine.*;
 import nachos.threads.*;
 import nachos.userprog.*;
 import nachos.vm.*;
 import java.util.*;
 
 /**
  * A kernel that can support multiple demand-paging user processes.
  */
 public class VMKernel extends UserKernel {
 	/**
 	 * Allocate a new VM kernel.
 	 */
 	public VMKernel() {
 		super();
 	}
 
 	/**
 	 * Initialize this kernel.
 	 */
 	public void initialize(String[] args) {
 		super.initialize(args);
 
 		int numPhysPages = Machine.processor().getNumPhysPages();
 		coreMap = new pageFrame[numPhysPages];
 		for (int i = 0; i < numPhysPages; i++)
 			coreMap[i] = new pageFrame();
 	}
 
 	/**
 	 * Test this kernel.
 	 */
 	public void selfTest() {
 		super.selfTest();
 	}
 
 	/**
 	 * Start running user programs.
 	 */
 	public void run() {
 		super.run();
 	}
 
 	/**
 	 * Terminate this kernel. Never returns.
 	 */
 	public void terminate() {
 		super.terminate();
 	}
 	
 	/*Uses the simpliest replacement policy FIFO
 	 * to decide what to evict next from the TLB.
 	 * 
 	 * return an index of the tlb that may be overwritten by
 	 * 		  machine.process().writeTLBentry( i, t );
 	 * */
 	private int replacementPolicy(){	
 		TranslationEntry emptyT = new TranslationEntry(0, 0, true, false, false, false);
 		//pick a tlb entry to evict and replace it with a new TranslationEntry();
 		Machine.processor().writeTLBEntry(evictionIndex, emptyT);
 		int TranslationEntryYouMayOverWrite= evictionIndex;//
 		evictionIndex = (evictionIndex + 1)% (Machine.processor().getTLBSize());
 		
 		return TranslationEntryYouMayOverWrite;
 	}
 	
 	private void syncTLB(boolean contextSwitch) {
 		// iterate through the entire tlb and start syncing
 		for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
 			// get the TableEntry stored at the ith tlb location
 			TranslationEntry TLBEntry = Machine.processor().readTLBEntry(i);
 
 			// If this TLB Entry is valid we must sync it up with the
 			// corresponding Table Entry stored in physical memory
 			if (TLBEntry.valid) {
 				TranslationEntry coreEntry = coreMap[TLBEntry.ppn].te;
 				// not sure if this is right cause dorian said:
 				// "sync tlb settings (used/dirty) -- e.g., if either
 				// is true, set true"
 				coreEntry.dirty |= TLBEntry.dirty;
 				coreEntry.used |= TLBEntry.used;
 
 				// we must set valid bit to false if contextSwitch is
 				// true
 				if (contextSwitch)
 					TLBEntry.valid = false;
 
 				// if contextSwitch is false then just set used/dirty
 				// to false to make that
 				// table entry available to read and write to
 				else {
 					TLBEntry.dirty = false;
 					TLBEntry.used = false;
 				}
 
 				// make sure to put/save changes back into TLB
 				Machine.processor().writeTLBEntry(i, TLBEntry);
 			}
 		}
 	}
 
 	public class pageFrame {
 		private VMProcess process;
 		private TranslationEntry te;
 		private int pinned;
 		private boolean unpinned;
 	}
 
 	// Make a swapFile class to make it easier to create a swapFile and accessit
 	private class SwapFile {
 		private OpenFile swapf = null;
 		private LinkedList<Integer> PageTableIDs = new LinkedList<Integer>();
 		private LinkedList<Integer> unusedFileSpace = new LinkedList<Integer>();
 
 		public SwapFile(String filename) {
			// swapf = FileSystem.open(filename, true);
 
 		}
 
 		// insert a page table into the swap file
 		// param @ ppn is the physical page number of the table entry you want
 		// to insert into the file
 		public void insertPageIntoFile(int ppn) {
 			int spn = 0;
 			if (unusedFileSpace != null)
 				spn = unusedFileSpace.pop();
 			else
 				spn = PageTableIDs.getLast() + 1;
 			int ps = Machine.processor().pageSize;
 			swapf.write(spn * ps, mainMemory, ppn * ps, ps);
 			// if there is a failure here you might want to exit
 
 			PageTableIDs.push(spn);
 		}
 	}
 
 	// dummy variables to make javac smarter
 	private static VMProcess dummy1 = null;
 	protected static byte[] mainMemory = Machine.processor().getMemory();
 
 	public static pageFrame[] coreMap = null;
 	private static final char dbgVM = 'v';
 	private static int evictionIndex =0;
 }
