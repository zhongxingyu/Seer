 package nachos.vm;
 
 import nachos.machine.*;
 import nachos.threads.*;
 import nachos.userprog.*;
 import nachos.vm.*;
 
 /**
  * A <tt>UserProcess</tt> that supports demand-paging.
  */
 public class VMProcess extends UserProcess {
 	/**
 	 * Allocate a new process.
 	 */
 	public VMProcess() {
 		super();
 		if (kernel == null) {
 			kernel = (VMKernel) ThreadedKernel.kernel;
 			if (kernel == null)// if it still is null we have a problem
 			{
 				// Deal with problem some how.
 				// If this failes does it mean there was no more
 				// space for it?
 				System.out.println("VM kernel allocation failed");
 			}
 		}
 	}
 
 	/**
 	 * Save the state of this process in preparation for a context switch.
 	 * Called by <tt>UThread.saveState()</tt>.
 	 */
 	public void saveState() {
 		super.saveState();
 	}
 
 	/**
 	 * Restore the state of this process after a context switch. Called by
 	 * <tt>UThread.restoreState()</tt>.
 	 */
 	public void restoreState() {
 		// super.restoreState();
 	}
 
 	/**
 	 * Initializes page tables for this process so that the executable can be
 	 * demand-paged.
 	 * 
 	 * @return <tt>true</tt> if successful.
 	 */
 	protected boolean loadSections() {
 		return super.loadSections();
 	}
 
 	/**
 	 * Release any resources allocated by <tt>loadSections()</tt>.
 	 */
 	protected void unloadSections() {
 		super.unloadSections();
 	}
 
 	void handleTLBMiss( int vaddr )
 	{
 		//need a kernel lock here
 		
 		
 		//need a kernel release here
 	}
 		
 
 	/**
 	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
 	 * . The <i>cause</i> argument identifies which exception occurred; see the
 	 * <tt>Processor.exceptionZZZ</tt> constants.
 	 * 
 	 * @param cause
 	 *            the user exception that occurred.
 	 */
 	public void handleException(int cause) {
 		Processor processor = Machine.processor();
 
 		switch (cause) {
 		case Processor.exceptionTLBMiss:
			
			handleTLBMiss(Machine.processor().readRegister(Processor.regBadVAddr));
 			
 			//todo:
 			//if it wasn't in swap file, then load it
 			//You may need to allocate a new page
 			
 			break;
 		default:
 			super.handleException(cause);
 			break;
 		}	
 	}
 
 	private static VMKernel kernel = null;
 	private static final int pageSize = Processor.pageSize;
 	private static final char dbgProcess = 'a';
 	private static final char dbgVM = 'v';
 }
