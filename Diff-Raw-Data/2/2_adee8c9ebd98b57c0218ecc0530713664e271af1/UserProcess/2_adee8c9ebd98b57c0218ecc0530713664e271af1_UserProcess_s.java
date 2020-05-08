 package nachos.userprog;
 
 import nachos.machine.*;
 import nachos.threads.*;
 
 import java.io.EOFException;
 import java.util.*;
 
 /**
  * Encapsulates the state of a user process that is not contained in its user
  * thread (or threads). This includes its address translation state, a file
  * table, and information about the program being executed.
  * 
  * <p>
  * This class is extended by other classes to support additional functionality
  * (such as additional syscalls).
  * 
  * @see nachos.vm.VMProcess
  * @see nachos.network.NetProcess
  */
 public class UserProcess {
 	/**
 	 * Allocate a new process.
 	 */
 	public UserProcess() {
         pageTable = new TranslationEntry[Machine.processor().getNumPhysPages()];
         for (int i = 0; i < pageTable.length; ++i)
             pageTable[i] = new TranslationEntry(i, 0, false, false, false, false);
         numPages = 0;
 
         openFiles = new HashMap<Integer, OpenFile>();
         openFiles.put(new Integer(0), UserKernel.console.openForReading());
         openFiles.put(new Integer(1), UserKernel.console.openForWriting());
 
         fileId = 1024;
 
         processLock.acquire();
         pid = ++pidCounter;
         processes.put(new Integer(pid), this);
         processLock.release();
 
         parent = null;
         thread = null;
 	}
 
 	/**
 	 * Allocate and return a new process of the correct class. The class name is
 	 * specified by the <tt>nachos.conf</tt> key
 	 * <tt>Kernel.processClassName</tt>.
 	 * 
 	 * @return a new process of the correct class.
 	 */
 	public static UserProcess newUserProcess() {
 		return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
 	}
 
     private void setParent(UserProcess p) {
         parent = p;
     }
 
     private UserProcess getParent() {
         return parent;
     }
 
 	/**
 	 * Execute the specified program with the specified arguments. Attempts to
 	 * load the program, and then forks a thread to run it.
 	 * 
 	 * @param name
 	 *            the name of the file containing the executable.
 	 * @param args
 	 *            the arguments to pass to the executable.
 	 * @return <tt>true</tt> if the program was successfully executed.
 	 */
 	public boolean execute(String name, String[] args) {
 		if (!load(name, args))
 			return false;
 
         processLock.acquire();
         ++activeProcesses;
         Lib.debug(dbgProcess, "process created: " + activeProcesses);
         processLock.release();
 
         setParent(UserKernel.currentProcess());
 		thread = (UThread) (new UThread(this).setName(name));
         thread.fork();
 
 		return true;
 	}
 
 	/**
 	 * Save the state of this process in preparation for a context switch.
 	 * Called by <tt>UThread.saveState()</tt>.
 	 */
 	public void saveState() {
 	}
 
 	/**
 	 * Restore the state of this process after a context switch. Called by
 	 * <tt>UThread.restoreState()</tt>.
 	 */
 	public void restoreState() {
 		Machine.processor().setPageTable(pageTable);
 	}
 
     public int getPid() {
         return pid;
     }
 
     private boolean allocate(int vpn, int desiredPages, boolean readOnly) {
         LinkedList<TranslationEntry> allocated = new LinkedList<TranslationEntry>();
 
         for (int i = 0; i < desiredPages; ++i) {
             if (vpn >= pageTable.length)
                 return false;
 
             int ppn = UserKernel.newPage();
             if (ppn == -1) {
                 Lib.debug(dbgProcess, "\tcannot allocate new page");
 
                 for (TranslationEntry te: allocated) {
                     pageTable[te.vpn] = new TranslationEntry(te.vpn, 0, false, false, false, false);
                     UserKernel.deletePage(te.ppn);
                     --numPages;
                 }
 
                 return false;
             } else {
                 TranslationEntry a = new TranslationEntry(vpn + i,
                         ppn, true, readOnly, false,false);
                 allocated.add(a);
                 pageTable[vpn + i] = a;
                 ++numPages;
             }
         }
         return true;
     }
 
     private void releaseResource() {
         for (int i = 0; i < pageTable.length; ++i)
             if (pageTable[i].valid) {
                 UserKernel.deletePage(pageTable[i].ppn);
                 pageTable[i] = new TranslationEntry(pageTable[i].vpn, 0, false, false, false, false);
             }
         numPages = 0;
     }
 
     private void finish(int cause) {
         status = cause;
 
         for (OpenFile of: openFiles.values())
             of.close();
 
         for (UserProcess p: processes.values())
             if (p.getParent() == this)
                 p.setParent(null);
 
         releaseResource();
 
         boolean halt = false;
         processLock.acquire();
         --activeProcesses;
         Lib.debug(dbgProcess, "process destroyed " + (cause == 0 ? "normally with code " + code : "abnormally with status " + status));
         if (activeProcesses == 0)
             halt = true;
         processLock.release();
 
         if (halt)
             Machine.halt();
         else
             thread.finish();
     }
 
 	/**
 	 * Read a null-terminated string from this process's virtual memory. Read at
 	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for
 	 * the null terminator, and convert it to a <tt>java.lang.String</tt>,
 	 * without including the null terminator. If no null terminator is found,
 	 * returns <tt>null</tt>.
 	 * 
 	 * @param vaddr
 	 *            the starting virtual address of the null-terminated string.
 	 * @param maxLength
 	 *            the maximum number of characters in the string, not including
 	 *            the null terminator.
 	 * @return the string read, or <tt>null</tt> if no null terminator was
 	 *         found.
 	 */
 	public String readVirtualMemoryString(int vaddr, int maxLength) {
 		Lib.assertTrue(maxLength >= 0);
 
 		byte[] bytes = new byte[maxLength + 1];
 
 		int bytesRead = readVirtualMemory(vaddr, bytes);
 
 		for (int length = 0; length < bytesRead; length++) {
 			if (bytes[length] == 0)
 				return new String(bytes, 0, length);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Transfer data from this process's virtual memory to all of the specified
 	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
 	 * 
 	 * @param vaddr
 	 *            the first byte of virtual memory to read.
 	 * @param data
 	 *            the array where the data will be stored.
 	 * @return the number of bytes successfully transferred.
 	 */
 	public int readVirtualMemory(int vaddr, byte[] data) {
 		return readVirtualMemory(vaddr, data, 0, data.length);
 	}
 
     private TranslationEntry lookUpPageTable(int vpn) {
         if (pageTable == null)
             return null;
 
         if (vpn >= 0 && vpn < pageTable.length)
             return pageTable[vpn];
         else
             return null;
     }
 
     private TranslationEntry translate(int vaddr) {
         return lookUpPageTable(UserKernel.vpn(vaddr));
     }
 
 	/**
 	 * Transfer data from this process's virtual memory to the specified array.
 	 * This method handles address translation details. This method must
 	 * <i>not</i> destroy the current process if an error occurs, but instead
 	 * should return the number of bytes successfully copied (or zero if no data
 	 * could be copied).
 	 * 
 	 * @param vaddr
 	 *            the first byte of virtual memory to read.
 	 * @param data
 	 *            the array where the data will be stored.
 	 * @param offset
 	 *            the first byte to write in the array.
 	 * @param length
 	 *            the number of bytes to transfer from virtual memory to the
 	 *            array.
 	 * @return the number of bytes successfully transferred.
 	 */
 	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
 		Lib.assertTrue(offset >= 0 && length >= 0
 				&& offset + length <= data.length);
 
         TranslationEntry te = translate(vaddr);
         if (te == null || !te.valid)
             return 0;
 
         int addrOffset = UserKernel.offset(vaddr);
         int paddr = UserKernel.addr(te.ppn, addrOffset);
 
 		byte[] memory = Machine.processor().getMemory();
 
 		int amount = Math.min(length, pageSize - addrOffset);
 		System.arraycopy(memory, paddr, data, offset, amount);
 
         if (amount < length)
             return amount + readVirtualMemory(UserKernel.addr(te.vpn + 1, 0),
                     data, offset + amount, length - amount);
         else
     		return amount;
 	}
 
 	/**
 	 * Transfer all data from the specified array to this process's virtual
 	 * memory. Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
 	 * 
 	 * @param vaddr
 	 *            the first byte of virtual memory to write.
 	 * @param data
 	 *            the array containing the data to transfer.
 	 * @return the number of bytes successfully transferred.
 	 */
 	public int writeVirtualMemory(int vaddr, byte[] data) {
 		return writeVirtualMemory(vaddr, data, 0, data.length);
 	}
 
 	/**
 	 * Transfer data from the specified array to this process's virtual memory.
 	 * This method handles address translation details. This method must
 	 * <i>not</i> destroy the current process if an error occurs, but instead
 	 * should return the number of bytes successfully copied (or zero if no data
 	 * could be copied).
 	 * 
 	 * @param vaddr
 	 *            the first byte of virtual memory to write.
 	 * @param data
 	 *            the array containing the data to transfer.
 	 * @param offset
 	 *            the first byte to transfer from the array.
 	 * @param length
 	 *            the number of bytes to transfer from the array to virtual
 	 *            memory.
 	 * @return the number of bytes successfully transferred.
 	 */
 	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
 		Lib.assertTrue(offset >= 0 && length >= 0
 				&& offset + length <= data.length);
 
         TranslationEntry te = translate(vaddr);
         if (te == null || !te.valid || te.readOnly)
             return 0;
 
         int addrOffset = UserKernel.offset(vaddr);
         int paddr = UserKernel.addr(te.ppn, addrOffset);
 
 		byte[] memory = Machine.processor().getMemory();
 
 		int amount = Math.min(length, pageSize - addrOffset);
 		System.arraycopy(data, offset, memory, paddr, amount);
 
         if (amount < length)
             return amount + writeVirtualMemory(UserKernel.addr(te.vpn + 1, 0),
                     data, offset + amount, length - amount);
         else
     		return amount;
 	}
 
 	/**
 	 * Load the executable with the specified name into this process, and
 	 * prepare to pass it the specified arguments. Opens the executable, reads
 	 * its header information, and copies sections and arguments into this
 	 * process's virtual memory.
 	 * 
 	 * @param name
 	 *            the name of the file containing the executable.
 	 * @param args
 	 *            the arguments to pass to the executable.
 	 * @return <tt>true</tt> if the executable was successfully loaded.
 	 */
 	private boolean load(String name, String[] args) {
 		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
 
 		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
 		if (executable == null) {
 			Lib.debug(dbgProcess, "\topen failed");
 			return false;
 		}
 
 		try {
 			coff = new Coff(executable);
 		} catch (EOFException e) {
 			executable.close();
 			Lib.debug(dbgProcess, "\tcoff load failed");
 			return false;
 		}
 
 		// make sure the sections are contiguous and start at page 0
 		numPages = 0;
 		for (int s = 0; s < coff.getNumSections(); s++) {
 			CoffSection section = coff.getSection(s);
 			if (section.getFirstVPN() != numPages) {
 				coff.close();
 				Lib.debug(dbgProcess, "\tfragmented executable");
 				return false;
 			}
             if (!allocate(numPages, section.getLength(), section.isReadOnly())) {
                 releaseResource();
                 return false;
             }
 		}
 
 		// make sure the argv array will fit in one page
 		byte[][] argv = new byte[args.length][];
 		int argsSize = 0;
 		for (int i = 0; i < args.length; i++) {
 			argv[i] = args[i].getBytes();
 			// 4 bytes for argv[] pointer; then string plus one for null byte
 			argsSize += 4 + argv[i].length + 1;
 		}
 		if (argsSize > pageSize) {
 			coff.close();
 			Lib.debug(dbgProcess, "\targuments too long");
 			return false;
 		}
 
 		// program counter initially points at the program entry point
 		initialPC = coff.getEntryPoint();
 
 		// next comes the stack; stack pointer initially points to top of it
         if (!allocate(numPages, stackPages, false)) {
             releaseResource();
             return false;
         }
 		initialSP = numPages * pageSize;
 
 		// and finally reserve 1 page for arguments
         if (!allocate(numPages, 1, false)) {
             releaseResource();
             return false;
         }
 
 		if (!loadSections())
 			return false;
 
 		// store arguments in last page
 		int entryOffset = (numPages - 1) * pageSize;
 		int stringOffset = entryOffset + args.length * 4;
 
 		this.argc = args.length;
 		this.argv = entryOffset;
 
 		for (int i = 0; i < argv.length; i++) {
 			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
 			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
 			entryOffset += 4;
 			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
 			stringOffset += argv[i].length;
 			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
 			stringOffset += 1;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Allocates memory for this process, and loads the COFF sections into
 	 * memory. If this returns successfully, the process will definitely be run
 	 * (this is the last step in process initialization that can fail).
 	 * 
 	 * @return <tt>true</tt> if the sections were successfully loaded.
 	 */
 	protected boolean loadSections() {
 		if (numPages > Machine.processor().getNumPhysPages()) {
 			coff.close();
 			Lib.debug(dbgProcess, "\tinsufficient physical memory");
 			return false;
 		}
 
 		// load sections
 		for (int s = 0; s < coff.getNumSections(); s++) {
 			CoffSection section = coff.getSection(s);
 
 			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
 					+ " section (" + section.getLength() + " pages)");
 
 			for (int i = 0; i < section.getLength(); i++) {
 				int vpn = section.getFirstVPN() + i;
 
                 TranslationEntry te = lookUpPageTable(vpn);
                 if (te == null)
                     return false;
 				section.loadPage(i, te.ppn);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Release any resources allocated by <tt>loadSections()</tt>.
 	 */
 	protected void unloadSections() {
 	}
 
 	/**
 	 * Initialize the processor's registers in preparation for running the
 	 * program loaded into this process. Set the PC register to point at the
 	 * start function, set the stack pointer register to point at the top of the
 	 * stack, set the A0 and A1 registers to argc and argv, respectively, and
 	 * initialize all other registers to 0.
 	 */
 	public void initRegisters() {
 		Processor processor = Machine.processor();
 
 		// by default, everything's 0
 		for (int i = 0; i < Processor.numUserRegisters; i++)
 			processor.writeRegister(i, 0);
 
 		// initialize PC and SP according
 		processor.writeRegister(Processor.regPC, initialPC);
 		processor.writeRegister(Processor.regSP, initialSP);
 
 		// initialize the first two argument registers to argc and argv
 		processor.writeRegister(Processor.regA0, argc);
 		processor.writeRegister(Processor.regA1, argv);
 	}
 
 	/**
 	 * Handle the halt() system call.
 	 */
 	private int handleHalt() {
 
         if (pid == 1) {
     		Machine.halt();
     		Lib.assertNotReached("Machine.halt() did not halt machine!");
         }
 
 		return 0;
 	}
 
     private int nextFileId() {
         return ++fileId;
     }
 
     private int openFile(int a0, boolean create) {
         String file = readVirtualMemoryString(a0, maxArgLen);
         if (file == null)
             return -1;
 
         OpenFile of = ThreadedKernel.fileSystem.open(file, create);
         if (of == null)
             return -1;
 
         int id = nextFileId();
         openFiles.put(new Integer(id), of);
 
         return id;
     }
 
     private int handleCreat(int a0) {
         return openFile(a0, true);
     }
 
     private int handleOpen(int a0) {
         return openFile(a0, false);
     }
 
     private int handleRead(int a0, int a1, int a2) {
         OpenFile of = openFiles.get(a0);
         if (of == null)
             return -1;
 
         byte[] buffer = new byte[a2];
         int ret = of.read(buffer, 0, a2);
 
         if (ret == -1)
             return -1;
 
         if (writeVirtualMemory(a1, buffer, 0, ret) != ret)
             return -1;
 
         return ret;
     }
 
     private int handleWrite(int a0, int a1, int a2) {
         OpenFile of = openFiles.get(a0);
         if (of == null)
             return -1;
 
         byte[] buffer = new byte[a2];
         if (readVirtualMemory(a1, buffer, 0, a2) != a2)
             return -1;
 
         int ret = of.write(buffer, 0, a2);
         return ret;
     }
 
     private int handleClose(int a0) {
         if (!openFiles.containsKey(new Integer(a0)))
             return -1;
 
         openFiles.remove(new Integer(a0)).close();
         return 0;
     }
 
     private int handleUnlink(int a0) {
         String file = readVirtualMemoryString(a0, maxArgLen);
         if (file == null)
             return -1;
 
         if (ThreadedKernel.fileSystem.remove(file))
             return 0;
         else
             return -1;
     }
 
     private int handleExec(int a0, int a1, int a2) {
         String file = readVirtualMemoryString(a0, maxArgLen);
         if (file == null)
             return -1;
 
         if (a1 < 0)
             return -1;
 
         String[] args = new String[a1];
         for (int i = 0; i < a1; ++i) {
             byte[] buffer = new byte[4];
             if (readVirtualMemory(a2 + i * 4, buffer) != buffer.length)
                 return -1;
             int addr = Lib.bytesToInt(buffer, 0);
             args[i] = readVirtualMemoryString(addr, maxArgLen);
             if (args[i] == null)
                 return -1;
         }
 
         UserProcess child = UserProcess.newUserProcess();
         if (!child.execute(file, args))
             return -1;
 
         return child.getPid();
     }
 
     private int handleJoin(int a0, int a1) {
         if (!processes.containsKey(new Integer(a0)))
             return -1;
 
         UserProcess child = processes.get(new Integer(a0));
         if (child.getParent() != this)
             return -1;
 
         child.thread.join();
         child.setParent(null);
 
         if (child.status == 0) {
             writeVirtualMemory(a1, Lib.bytesFromInt(child.code));
             return 1;
         } else
             return 0;
     }
 
     private int handleExit(int a0) {
         finish(0);
         return 0;
     }
 
 	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2,
 			syscallJoin = 3, syscallCreate = 4, syscallOpen = 5,
 			syscallRead = 6, syscallWrite = 7, syscallClose = 8,
 			syscallUnlink = 9;
 
 	/**
 	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
 	 * <i>syscall</i> argument identifies which syscall the user executed:
 	 * 
 	 * <table>
 	 * <tr>
 	 * <td>syscall#</td>
 	 * <td>syscall prototype</td>
 	 * </tr>
 	 * <tr>
 	 * <td>0</td>
 	 * <td><tt>void halt();</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>1</td>
 	 * <td><tt>void exit(int status);</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>2</td>
 	 * <td><tt>int  exec(char *name, int argc, char **argv);
      * 								</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>3</td>
 	 * <td><tt>int  join(int pid, int *status);</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>4</td>
 	 * <td><tt>int  creat(char *name);</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>5</td>
 	 * <td><tt>int  open(char *name);</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>6</td>
 	 * <td><tt>int  read(int fd, char *buffer, int size);
      *								</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>7</td>
 	 * <td><tt>int  write(int fd, char *buffer, int size);
      *								</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>8</td>
 	 * <td><tt>int  close(int fd);</tt></td>
 	 * </tr>
 	 * <tr>
 	 * <td>9</td>
 	 * <td><tt>int  unlink(char *name);</tt></td>
 	 * </tr>
 	 * </table>
 	 * 
 	 * @param syscall
 	 *            the syscall number.
 	 * @param a0
 	 *            the first syscall argument.
 	 * @param a1
 	 *            the second syscall argument.
 	 * @param a2
 	 *            the third syscall argument.
 	 * @param a3
 	 *            the fourth syscall argument.
 	 * @return the value to be returned to the user.
 	 */
 	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
 		switch (syscall) {
 		case syscallHalt:
 			return handleHalt();
 
         case syscallCreate:
             return handleCreat(a0);
 
         case syscallOpen:
             return handleOpen(a0);
 
         case syscallRead:
             return handleRead(a0, a1, a2);
 
         case syscallWrite:
             return handleWrite(a0, a1, a2);
 
         case syscallClose:
             return handleClose(a0);
 
         case syscallUnlink:
             return handleUnlink(a0);
 
         case syscallExec:
             return handleExec(a0, a1, a2);
 
         case syscallJoin:
             return handleJoin(a0, a1);
 
         case syscallExit:
             return handleExit(a0);
 
 		default:
 			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
 			Lib.assertNotReached("Unknown system call!");
 		}
 		return 0;
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
 		case Processor.exceptionSyscall:
 			int result = handleSyscall(processor.readRegister(Processor.regV0),
 					processor.readRegister(Processor.regA0), processor
 							.readRegister(Processor.regA1), processor
 							.readRegister(Processor.regA2), processor
 							.readRegister(Processor.regA3));
 			processor.writeRegister(Processor.regV0, result);
 			processor.advancePC();
 			break;
 
 		default:
 			Lib.debug(dbgProcess, "Unexpected exception: "
 					+ Processor.exceptionNames[cause]);
             finish(cause);
 			Lib.assertNotReached("Unexpected exception");
 		}
 	}
 
 	/** The program being run by this process. */
 	protected Coff coff;
 
 	/** This process's page table. */
 	protected TranslationEntry[] pageTable;
 	/** The number of pages occupied by the program. */
 	protected int numPages;
 
 	/** The number of pages in the program's stack. */
 	protected final int stackPages = 8;
 
 	private int initialPC, initialSP;
 	private int argc, argv;
 
 	private static final int pageSize = Processor.pageSize;
 	private static final char dbgProcess = 'a';
 
     private Map<Integer, OpenFile> openFiles;
     private int fileId;
     private static final int maxArgLen = 256;
 
     private static int pidCounter = 0;
     private int pid;
 
     private static Map<Integer, UserProcess> processes = new HashMap<Integer, UserProcess>();
     private static Lock processLock = new Lock();
     private static int activeProcesses = 0;
 
     private int status = 0, code = 0;
 
     private UThread thread;
     private UserProcess parent;
 }
