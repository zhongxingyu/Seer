 package nachos.userprog;
 
 import nachos.machine.*;
 import nachos.threads.*;
 
 import java.io.EOFException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * Encapsulates the state of a user process that is not contained in its
  * user thread (or threads). This includes its address translation state, a
  * file table, and information about the program being executed.
  *
  * <p>
  * This class is extended by other classes to support additional functionality
  * (such as additional syscalls).
  *
  * @see	nachos.vm.VMProcess
  * @see	nachos.network.NetProcess
  */
 public class UserProcess {
 	//Fields
 	/** The program being run by this process. */
 	protected Coff coff;
 	/** This process's page table. */
 	protected TranslationEntry[] pageTable;
 	/** The number of contiguous pages occupied by the program. */
 	protected int numPages;
 	/** The number of pages in the program's stack. */
 	protected final int stackPages = 8;
 	private int initialPC, initialSP;
 	private int argc, argv;
 	private static final int pageSize = Processor.pageSize;
 	private static final char dbgProcess = 'a';
 	
 	//New Fields
 	//Part I - static
 	private static int processCounter = 0;
 	private static final int MAX_SIZE = 16; //the max number of files that one UserProcess can open
 	// Part I - nonstatic
 	private int processID;
 	private OpenFile[] openFileList;
 	//Part III - static
 	private static Lock lock = new Lock();
 	private static int numUserProcesses = 0;
 	//Part III - nonstatic
 	private UserProcess parent;
 	private HashMap<Integer, UserProcess> children;
 	private int exitStatus;
 	private UThread thread;
 
 	//Constructor
 	public UserProcess() {
 		int numPhysPages = Machine.processor().getNumPhysPages();
 		pageTable = new TranslationEntry[numPhysPages];
 		for (int i=0; i<numPhysPages; i++)
 			pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
 		
 		//Initialize new fields
 		//Part I
 		lock.acquire();
 		processID = processCounter++;
 		numUserProcesses++;
 		lock.release();
 
 		openFileList = new OpenFile[MAX_SIZE];
 		openFileList[0] = UserKernel.console.openForReading();
 		openFileList[1] = UserKernel.console.openForWriting();
 		
 		//Part III
 		parent = null;
 		children = new HashMap<Integer, UserProcess>();
 		exitStatus = 0;
 		thread = null;
 	}
 	
 	//Action Methods
 	/**
 	 * Allocate and return a new process of the correct class. The class name
 	 * is specified by the <tt>nachos.conf</tt> key
 	 * <tt>Kernel.processClassName</tt>.
 	 *
 	 * @return	a new process of the correct class.
 	 */
 	public static UserProcess newUserProcess() {
 		return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
 	}
 
 	/**
 	 * Execute the specified program with the specified arguments. Attempts to
 	 * load the program, and then forks a thread to run it.
 	 *
 	 * @param	name	the name of the file containing the executable.
 	 * @param	args	the arguments to pass to the executable.
 	 * @return	<tt>true</tt> if the program was successfully executed.
 	 */
 	public boolean execute(String name, String[] args) {
 		if (!load(name, args))
 			return false;
 		
 		thread = new UThread(this);
 		thread.setName(name).fork();
 
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
 
 	/**
      * Create empty byte[] with size maxLength+1. Then read at most maxLength+1
      * bytes from this process's virtual memory, starting from specified address.
      * Search for null terminator in byte[]. Return String(byte[], 0, length)
      * without including the null terminator. If no null terminator is found,
      * returns null.
 	 *
 	 * @param	vaddr	    the starting virtual address of the null-terminated string.
 	 * @param	maxLength	the maximum number of characters in the string, not including the null terminator.
 	 * @return	the string read, or null if no null terminator was found.
 	 */
 	public String readVirtualMemoryString(int vaddr, int maxLength) {
 		Lib.assertTrue(maxLength >= 0);
 
 		byte[] bytes = new byte[maxLength+1];
 
 		int bytesRead = readVirtualMemory(vaddr, bytes);
 
 		for (int length=0; length<bytesRead; length++) {
 			if (bytes[length] == 0)
 			return new String(bytes, 0, length);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Transfer data from this process's virtual memory to all of the specified
 	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
 	 *
 	 * @param	vaddr	the first byte of virtual memory to read.
 	 * @param	data	the array where the data will be stored.
 	 * @return	the number of bytes successfully transferred.
 	 */
 	public int readVirtualMemory(int vaddr, byte[] data) {
 		return readVirtualMemory(vaddr, data, 0, data.length);
 	}
 
 	/**
 	 * Transfer data from this process's virtual memory to the specified array.
 	 * This method handles address translation details. This method must
 	 * <i>not</i> destroy the current process if an error occurs, but instead
 	 * should return the number of bytes successfully copied (or zero if no
 	 * data could be copied).
 	 *
 	 * @param	vaddr	the first byte of virtual memory to read.
 	 * @param	data	the array where the data will be stored.
 	 * @param	offset	the first byte to write in the array.
 	 * @param	length	the number of bytes to transfer from virtual memory to
 	 *			the array.
 	 * @return	the number of bytes successfully transferred.
 	 */
 	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
 		byte[] memory = Machine.processor().getMemory();
 
 		//total amount of pages read/written
 		int totalAmount = 0;
 
 		while((length>0)&&(vaddr<numPages*pageSize)&&(vaddr > 0)){
 			//if the current virtual address being looked at is out of the scope of the pageTable we return the bytes we've written up until then
 			int vpn = vaddr/pageSize;
 			
 			//if a readOnly or invalid page is encountered then we return the bytes we've written up until then since we cannot write to this page
 			if(!pageTable[vpn].valid || pageTable[vpn].readOnly){
 				return totalAmount;
 			}
 			//sets the page to used and dirty
 			pageTable[vpn].dirty = true;
 			pageTable[vpn].used = true;
 			int ppn = pageTable[vpn].ppn;
 			int byteStart = vaddr%pageSize;
 			
 			//copy either upto the page or however many bytes are left - amount is number of bytes copied to fill current page
 			int amount = Math.min((vpn+1)*pageSize - vaddr, length);
 
 			int paddr = ppn*pageSize + byteStart;
 
 			//write to the physical page corresponding to the current virtual page
 			System.arraycopy(memory, paddr, data, offset, amount);
 			offset += amount;
 			vaddr += amount;
 			length -= amount;
 			totalAmount += amount;
 		}
 
 		return totalAmount;
 	}
 
 	/**
 	 * Transfer all data from the specified array to this process's virtual
 	 * memory.
 	 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
 	 *
 	 * @param	vaddr	the first byte of virtual memory to write.
 	 * @param	data	the array containing the data to transfer.
 	 * @return	the number of bytes successfully transferred.
 	 */
 	public int writeVirtualMemory(int vaddr, byte[] data) {
 		return writeVirtualMemory(vaddr, data, 0, data.length);
 	}
 
 	/**
 	 * Transfer data from the specified array to this process's virtual memory.
 	 * This method handles address translation details. This method must
 	 * <i>not</i> destroy the current process if an error occurs, but instead
 	 * should return the number of bytes successfully copied (or zero if no
 	 * data could be copied).
 	 *
 	 * @param	vaddr	the first byte of virtual memory to write.
 	 * @param	data	the array containing the data to transfer.
 	 * @param	offset	the first byte to transfer from the array.
 	 * @param	length	the number of bytes to transfer from the array to
 	 *			virtual memory.
 	 * @return	the number of bytes successfully transferred.
 	 */
 
 	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
 		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
 
 		byte[] memory = Machine.processor().getMemory();
 
 		//total amount of pages read/written
 		int totalAmount = 0;
 
 		while((length>0)&&(vaddr<numPages*pageSize)&&(vaddr > 0)){
 			//if the current virtual address being looked at is out of the scope of the pageTable we return the bytes we've written up until then
 			int vpn = vaddr/pageSize;
 			
 			//if a readOnly or invalid page is encountered then we return the bytes we've written up until then since we cannot write to this page
 			if(!pageTable[vpn].valid || pageTable[vpn].readOnly){
 				return totalAmount;
 			}
 			//sets the page to used and dirty
 			pageTable[vpn].dirty = true;
 			pageTable[vpn].used = true;
 			int ppn = pageTable[vpn].ppn;
 			int byteStart = vaddr%pageSize;
 
 			//copy either upto the page or however many bytes are left - amount is number of bytes copied to fill current page
 			int amount = Math.min((vpn+1)*pageSize - vaddr, length);
 
 			int paddr = ppn*pageSize + byteStart;
 
 			//write to the physical page corresponding to the current virtual page
 			System.arraycopy(data, offset, memory, paddr, amount);
 			offset += amount;
 			vaddr += amount;
 			length -= amount;
 			totalAmount += amount;
 		}
 
 		return totalAmount;
 	}
 
 	/**
 	 * Load the executable with the specified name into this process, and
 	 * prepare to pass it the specified arguments. Opens the executable, reads
 	 * its header information, and copies sections and arguments into this
 	 * process's virtual memory.
 	 *
 	 * @param	name	the name of the file containing the executable.
 	 * @param	args	the arguments to pass to the executable.
 	 * @return	<tt>true</tt> if the executable was successfully loaded.
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
 		}
 		catch (EOFException e) {
 			executable.close();
 			Lib.debug(dbgProcess, "\tcoff load failed");
 			return false;
 		}
 
 		// make sure the sections are contiguous and start at page 0
 		numPages = 0;
 		for (int s=0; s<coff.getNumSections(); s++) {
 			CoffSection section = coff.getSection(s);
 			if (section.getFirstVPN() != numPages) {
 				coff.close();
 				Lib.debug(dbgProcess, "\tfragmented executable");
 				return false;
 			}
 			numPages += section.getLength();
 		}
 
 		// make sure the argv array will fit in one page
 		byte[][] argv = new byte[args.length][];
 		int argsSize = 0;
 		for (int i=0; i<args.length; i++) {
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
 		numPages += stackPages;
 		initialSP = numPages*pageSize;
 
 		// and finally reserve 1 page for arguments
 		numPages++;
 
 		if (!loadSections())
 			return false;
 
 		// store arguments in last page
 		int entryOffset = (numPages-1)*pageSize;
 		int stringOffset = entryOffset + args.length*4;
 
 		this.argc = args.length;
 		this.argv = entryOffset;
 		
 		for (int i=0; i<argv.length; i++) {
 			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
 			Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
 			entryOffset += 4;
 			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
 				   argv[i].length);
 			stringOffset += argv[i].length;
 			Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
 			stringOffset += 1;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Allocates memory for this process, and loads the COFF sections into
 	 * memory. If this returns successfully, the process will definitely be
 	 * run (this is the last step in process initialization that can fail).
 	 *
 	 * @return	<tt>true</tt> if the sections were successfully loaded.
 	 */
 	protected boolean loadSections() {
 		UserKernel.pagesLock.acquire();
 
 		//if there are not enough free physical pages at the time we return false
 		if(UserKernel.pages.size() < numPages){
 			UserKernel.pagesLock.release();
 			coff.close();
 			return false;
 		}
 		UserKernel.pagesLock.release();
 		
 		//initialize the pageTable
 		//pageTable = new TranslationEntry[numPages];
 		int additionalPage = 0;
 		//the following is a critical section since we only want to modify the list of free pages one process at a time
 		UserKernel.pagesLock.acquire();
 		for (int s=0; s<coff.getNumSections(); s++) {
 			CoffSection section = coff.getSection(s);
 
 			//for each page in a section we create a new TranslationEntry with the corresponding Physical Page number (retrieved from UserKernel.pages list) and load that section into the physical page
 			for (int i=0; i<section.getLength(); i++) {
 				int vpn = section.getFirstVPN()+i;
 				int ppn = ((Integer)UserKernel.pages.pop()).intValue();
 				pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section.isReadOnly(), false, false);
 				section.loadPage(i, ppn);
 				additionalPage++;
 			}
 		}
 		for (int j = additionalPage; j < numPages; j++){
 			pageTable[j] = new TranslationEntry(j, ((Integer)UserKernel.pages.pop()).intValue(), true, false, false, false);
 		}
 		UserKernel.pagesLock.release();
 		return true;
 	}
 	
 
 	/**
 	 * Release any resources allocated by <tt>loadSections()</tt>.
 	 */
 	protected void unloadSections() {
 	   UserKernel.pagesLock.acquire();
 		for(int i=0; i<pageTable.length; i++){
 			pageTable[i].valid = false;
 			UserKernel.pages.add(new Integer(pageTable[i].ppn));
 		}
 		UserKernel.pagesLock.release();
 	}	
 
 	/**
 	 * Initialize the processor's registers in preparation for running the
 	 * program loaded into this process. Set the PC register to point at the
 	 * start function, set the stack pointer register to point at the top of
 	 * the stack, set the A0 and A1 registers to argc and argv, respectively,
 	 * and initialize all other registers to 0.
 	 */
 	public void initRegisters() {
 		Processor processor = Machine.processor();
 
 		// by default, everything's 0
 		for (int i=0; i<processor.numUserRegisters; i++)
 			processor.writeRegister(i, 0);
 
 		// initialize PC and SP according
 		processor.writeRegister(Processor.regPC, initialPC);
 		processor.writeRegister(Processor.regSP, initialSP);
 
 		// initialize the first two argument registers to argc and argv
 		processor.writeRegister(Processor.regA0, argc);
 		processor.writeRegister(Processor.regA1, argv);
 	}
 
 	/**
 	 * Halt the Nachos machine by calling Machine.halt(). Only the root process
 	 * (the first process, executed by UserKernel.run()) should be allowed to
 	 * execute this syscall. Any other process should ignore the syscall and return
 	 * immediately.
 	 */
 	private int handleHalt() {
 		if (this.processID != 0) //this UserProcess is not root
 			return 0;
 		
 		Machine.halt();
 		
 		Lib.assertNotReached("Machine.halt() did not halt machine!");
 		return 0;
 	}
 	
 	/**
 	 * Attempt to open the named disk file, creating it if it does not exist,
 	 * and return a file descriptor that can be used to access the file.
 	 * Note that creat() can only be used to create files on disk; creat() will
 	 * never return a file descriptor referring to a stream.
 	 * 
 	 * @param a0 fileNameAddress - first byte of virtual memory to read
 	 * @return Returns the new file descriptor, or -1 if an error occurred.
 	 */
 	private int handleCreate(int a0) {
 		if (a0 < 0) //invalid fileNameAddr
 			return -1;
 		
 		String filename = readVirtualMemoryString(a0, 256); //a0: vaddr, 256: maxLength
 		if (filename == null) //invalid filename (no null terminator was found)
 			return -1;
 
 		int fileDescriptor = -1;
 		for (int i=2; i<MAX_SIZE; i++) { //0 and 1 are reserved for stdin and stdout
 			if (openFileList[i] == null) {
 				OpenFile openfile = ThreadedKernel.fileSystem.open(filename, true); //try to open file with the filename, if no such file then create one with length 0
                 if (openfile == null)
                     return -1;
 				openFileList[i] = openfile;
 				fileDescriptor = i;
 				return fileDescriptor;
 			}
 		}
 		return fileDescriptor; //the # of files that are open is over MAX_SIZE
 	}
 
 	/**
 	 * Attempt to open the named file and return a file descriptor.
 	 * Note that open() can only be used to open files on disk; open() will never
 	 * return a file descriptor referring to a stream.
 	 * 
 	 * @param a0 fileNameAddress
 	 * @return Returns the new file descriptor, or -1 if an error occurred.
 	 */
 	private int handleOpen(int a0) {
 		if (a0 < 0) //invalid fileNameAddr
 			return -1;
 		
 		String filename = readVirtualMemoryString(a0, 256); //a0: vaddr, 256: maxLength
 		if (filename == null) //invalid filename (no null terminator was found)
 			return -1;
 		
 		int fileDescriptor = -1;
 		for (int i=2; i<MAX_SIZE; i++) { //never return a file descriptor referring to a stream
 			if (openFileList[i] == null) {
 				OpenFile openfile = ThreadedKernel.fileSystem.open(filename, false);
 				if (openfile == null)
 					return -1;
 				openFileList[i] = openfile;
 				fileDescriptor = i;
 				return fileDescriptor;
 			}
 		}
 		return fileDescriptor; //the # of files that are open is over MAX_SIZE
 	}
 
 
 	/**
 	 * Attempt to read up to count bytes from the file or stream referred to by 
      * fileDescriptor, then write data into this process's virtual memory specified 
      * by vaddr.
 	 * On success, the number of bytes read is returned. If the file descriptor
 	 * refers to a file on disk, the file position is advanced by this number.
 	 * It is not necessarily an error if this number is smaller than the number of
 	 * bytes requested. If the file descriptor refers to a file on disk, this
 	 * indicates that the end of the file has been reached. If the file descriptor
 	 * refers to a stream, this indicates that the fewer bytes are actually
 	 * available right now than were requested, but more bytes may become available
 	 * in the future. Note that read() never waits for a stream to have more data;
 	 * it always returns as much as possible immediately.
 	 * On error, -1 is returned, and the new file position is undefined. This can
 	 * happen if fileDescriptor is invalid, if part of the buffer is read-only or
 	 * invalid, or if a network stream has been terminated by the remote host and
 	 * no more data is available.
 	 * 
 	 * @param a0 fileDescriptor
 	 * @param a1 vaddr
 	 * @param a2 count (amount byte to read from the file)
 	 * @return number of bytes written to virtual memory
 	 */
 	private int handleRead(int a0, int a1, int a2) {
 		if (a0 < 0 || a0 > 15 || a1 < 0 || a2 < 0) //fileDescriptor out of range or invalid buffer addr or invalid count
 			return -1;
 		
 		OpenFile openfile = openFileList[a0];
 		if (openfile == null) //no such file exists in openFileList
 			return -1;
 		
 		byte[] tempBuffer = new byte[a2];
 		int bytesRead = openfile.read(tempBuffer, 0, a2); //read openfile from offset 0 to length a2, and save into tempBuffer
 		if (bytesRead == -1) //openfile.read returned error
 			return -1;
 		
 		int bytesWrite = writeVirtualMemory(a1, tempBuffer, 0, bytesRead); //write tempBuffer into a1(vaddr) with offset 0 and tempBuffer.length. Then returns amount of bytes written
 		if (bytesWrite != bytesRead)
 			return -1;
 		return bytesWrite;
 	}
 
 	/**
 	 * Attempt to write up to count bytes from buffer, which is in this process's
      * virtual memory specified by vaddr, to the file or stream referred to by 
      * fileDescriptor. write() can return before the bytes are actually flushed 
      * to the file or stream. A write to a stream can block, however, if kernel 
      * queues are temporarily full.
 	 * On success, the number of bytes written is returned (zero indicates nothing
 	 * was written), and the file position is advanced by this number. It IS an
 	 * error if this number is smaller than the number of bytes requested. For
 	 * disk files, this indicates that the disk is full. For streams, this
 	 * indicates the stream was terminated by the remote host before all the data
 	 * was transferred.
 	 * On error, -1 is returned, and the new file position is undefined. This can
 	 * happen if fileDescriptor is invalid, if part of the buffer is invalid, or
 	 * if a network stream has already been terminated by the remote host.
 	 * 
 	 * @param a0 fileDescriptor
 	 * @param a1 vaddr
 	 * @param a2 count
 	 * @return number of bytes written to the OpenFile
 	 */
 	private int handleWrite(int a0, int a1, int a2) {
 		if (a0 < 0 || a0 > 15 || a1 < 0 || a2 < 0) //fileDescriptor out of range or invalid buffer addr or invalid count
 			return -1;
 		
 		OpenFile openfile = openFileList[a0];
 		if (openfile == null) //no such file exists in openFileList
 			return -1;
 		
 		byte[] tempBuffer = new byte[a2];
 		int bytesRead = readVirtualMemory(a1, tempBuffer, 0, a2);
 		int bytesWrite = openfile.write(tempBuffer, 0, bytesRead);
 		if (bytesWrite == -1 || bytesWrite != a2) //openfile.write returned error or bytesWrite smaller than a2
 			return -1;
 		
 		return bytesWrite;
 	}
 
 	/**
 	 * Close a file descriptor, so that it no longer refers to any file or stream
 	 * and may be reused.
 	 * If the file descriptor refers to a file, all data written to it by write()
 	 * will be flushed to disk before close() returns.
 	 * If the file descriptor refers to a stream, all data written to it by write()
 	 * will eventually be flushed (unless the stream is terminated remotely), but
 	 * not necessarily before close() returns.
 	 * The resources associated with the file descriptor are released. If the
 	 * descriptor is the last reference to a disk file which has been removed using
 	 * unlink, the file is deleted (this detail is handled by the file system
 	 * implementation).
 	 * 
 	 * @param a0 fileDescriptor
 	 * @return Returns 0 on success, or -1 if an error occurred.
 	 */
 	private int handleClose(int a0) {
 		if (a0 < 0 || a0 > 15) //fileDescriptor out of range
 			return -1;
 		
 		if (openFileList[a0] == null) //no such file exists in openFileList
 			return -1;
 		
 		openFileList[a0].close();
 		openFileList[a0] = null;
 		return 0;
 	}
 
 	/**
 	 * Delete a file from the file system. If no processes have the file open, the
 	 * file is deleted immediately and the space it was using is made available for
 	 * reuse.
 	 * If any processes still have the file open, the file will remain in existence
 	 * until the last file descriptor referring to it is closed. However, creat()
 	 * and open() will not be able to return new file descriptors for the file
 	 * until it is deleted.
 	 * 
 	 * @param a0 fileNameAddr
 	 * @return Returns 0 on success, or -1 if an error occurred.
 	 */
 	private int handleUnlink(int a0) {
 		if (a0 < 0) //invalid vaddr
 			return -1;
 		
 		String filename = readVirtualMemoryString(a0,256);
 		if (filename == null) //invalid filename (no null terminator was found)
 			return -1;
 		
 		for (int i=0; i<MAX_SIZE; i++) {
 			if (openFileList[i] != null && openFileList[i].getName() == filename) {
 				openFileList[i].close();
 				openFileList[i] = null;
 			}
 		}
 
 		boolean deleted = ThreadedKernel.fileSystem.remove(filename);
 		if (deleted == false) //invalid filename(a0) or FileRemover returned false
 			return -1;
 		return 0;
 	}
 
 	/**
 	 * Terminate the current process immediately. Any open file descriptors
 	 * belonging to the process are closed. Any children of the process no longer
 	 * have a parent process.
 	 *
 	 * status is returned to the parent process as this process's exit status and
 	 * can be collected using the join syscall. A process exiting normally should
 	 * (but is not required to) set status to 0.
 	 *
 	 * exit() never returns.
 	 *
 	 * @param a0 status
 	 * @return None
 	 */
 	private void handleExit(int a0) {
 		lock.acquire(); //critical section start
 		for (UserProcess child: children.values()) {
 			if(child != null)
 				child.parent = null;
 		}
 		children.clear();
 		if (this.parent != null) {
 			this.parent.children.remove(this.processID);
 		}
 		lock.release(); //critical section end
 		
 		for (int i = 0; i < openFileList.length; i++) { //close all openfiles that this process have
 			if (openFileList[i] != null)
 				handleClose(i);
 		}
 
 		unloadSections();
 		this.exitStatus = a0;
 		
 		lock.acquire();
 		if (numUserProcesses == 1) { //only one UserProcess left -> terminate kernel
 			Kernel.kernel.terminate();
 		}
 		numUserProcesses--;
 		lock.release();
 		UThread.finish();
 	}
 
 	/**
 	 * Execute the program stored in the specified file, with the specified
 	 * arguments, in a new child process. The child process has a new unique
 	 * process ID, and starts with stdin opened as file descriptor 0, and stdout
 	 * opened as file descriptor 1.
 	 * file is a null-terminated string that specifies the name of the file
 	 * containing the executable. Note that this string must include the ".coff"
 	 * extension.
 	 * argc specifies the number of arguments to pass to the child process. This
 	 * number must be non-negative.
 	 * argv is an array of pointers to null-terminated strings that represent the
 	 * arguments to pass to the child process. argv[0] points to the first
 	 * argument, and argv[argc-1] points to the last argument.
 	 * exec() returns the child process's process ID, which can be passed to
 	 * join(). On error, returns -1.
 	 *
 	 * @param a0 file (char *file)
 	 * @param a1 argc (int)
 	 * @param a2 argv (char *argv[])
 	 */
 	private int handleExec(int filenameVirtualAddr, int argCounter, int argOffset) {
 		if (filenameVirtualAddr < 0 || argCounter < 0)
 			return -1;
 
 		String filename = readVirtualMemoryString(filenameVirtualAddr, 256);
 		if (filename == null || !filename.endsWith(".coff")) //invalid filename (no null terminator was found)
 			return -1;
 
 		String[] arguments = new String[argCounter]; //empty array of String argument
 		byte[] buffer;
 		int bytesRead;
 		int argumentAddress;
 		for (int i=0; i<argCounter; i++) {
 			buffer = new byte[4];
			bytesRead = readVirtualMemory(argOffset+(i*4), buffer);
 			
 			if (bytesRead != 4) //bytesRead not equal to the size of char*
 				return -1;
 			argumentAddress = Lib.bytesToInt(buffer, 0);
 			arguments[i] = readVirtualMemoryString(argumentAddress, 256);
 			
 			if (arguments[i] == null) //invalid file(argument) name
 				return -1;
 		}
 
 		UserProcess child = newUserProcess();
 		boolean result = child.execute(filename, arguments);
 		if (result == false) //execute fail
 			return -1;
 		
 		child.parent = this;
 		this.children.put(child.processID, child);
 		return child.processID;
 	}
 
 	/**
 	 * Suspend execution of the current process until the child process specified
 	 * by the processID argument has exited. If the child has already exited by the
 	 * time of the call, returns immediately. When the current process resumes, it
 	 * disowns the child process, so that join() cannot be used on that process
 	 * again.
 	 *
 	 * processID is the process ID of the child process, returned by exec().
 	 *
 	 * status points to an integer where the exit status of the child process will
 	 * be stored. This is the value the child passed to exit(). If the child exited
 	 * because of an unhandled exception, the value stored is not defined.
 	 *
 	 * If the child exited normally, returns 1. If the child exited as a result of
 	 * an unhandled exception, returns 0. If processID does not refer to a child
 	 * process of the current process, returns -1.
 	 *
 	 * @param a0 processID (int processID)
 	 * @param a1 status (int *status)
 	 * @return 1: normal child exit, 0: unhandled exception child exit, -1: processID does not refer to child process
 	 */
 	private int handleJoin(int a0, int a1) {
 		UserProcess child = this.children.get(a0);
 		if (child == null) //there is no child process with processID(a0)
 			return -1;
 		
 		child.thread.join();
 		
 		if (a1 >= 0) {
 			byte[] buffer = Lib.bytesFromInt(child.exitStatus);
 			int bytesWrite = writeVirtualMemory(a1, buffer, 0, buffer.length);
 			
 			Lib.assertTrue(buffer.length == 4);
 			
 			if (bytesWrite == -1 || bytesWrite != buffer.length) //unhandled exception
 				return 0;
 		}
 		
 		return 1;
 		/*
 		int myStatus = this.exitStatus;
 		if (myStatus == 0)
 			return 1; //child process exited normally
 		return 0;
 		*/
 	}
 
 	private static final int
 		syscallHalt = 0,
 		syscallExit = 1,
 		syscallExec = 2,
 		syscallJoin = 3,
 		syscallCreate = 4,
 		syscallOpen = 5,
 		syscallRead = 6,
 		syscallWrite = 7,
 		syscallClose = 8,
 		syscallUnlink = 9;
 
 	/**
 	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
 	 * <i>syscall</i> argument identifies which syscall the user executed:
 	 *
 	 * <table>
 	 * <tr><td>syscall#</td><td>syscall prototype</td></tr>
 	 * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
 	 * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
 	 * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
 	 * 								</tt></td></tr>
 	 * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
 	 * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
 	 * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
 	 * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
 	 *								</tt></td></tr>
 	 * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
 	 *								</tt></td></tr>
 	 * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
 	 * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
 	 * </table>
 	 * 
 	 * @param	syscall	the syscall number.
 	 * @param	a0	the first syscall argument.
 	 * @param	a1	the second syscall argument.
 	 * @param	a2	the third syscall argument.
 	 * @param	a3	the fourth syscall argument.
 	 * @return	the value to be returned to the user.
 	 */
 	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
 		switch (syscall) {
 			case syscallHalt:
 				return handleHalt();
 			case syscallExit:
 				handleExit(a0);
 				break;
 			case syscallExec:
 				return handleExec(a0, a1, a2);
 			case syscallJoin:
 				return handleJoin(a0, a1);
 			case syscallCreate:
 				return handleCreate(a0);
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
 
 			default:
 				Lib.debug(dbgProcess, "Unknown syscall " + syscall);
 				Lib.assertNotReached("Unknown system call!");
 		}
 		return 0;
 	}
 
 	/**
 	 * Handle a user exception. Called by
 	 * <tt>UserKernel.exceptionHandler()</tt>. The
 	 * <i>cause</i> argument identifies which exception occurred; see the
 	 * <tt>Processor.exceptionZZZ</tt> constants.
 	 *
 	 * @param	cause	the user exception that occurred.
 	 */
 	public void handleException(int cause) {
 		Processor processor = Machine.processor();
 
 		switch (cause) {
 			case Processor.exceptionSyscall:
 				int result = handleSyscall(processor.readRegister(Processor.regV0),
 							   processor.readRegister(Processor.regA0),
 							   processor.readRegister(Processor.regA1),
 							   processor.readRegister(Processor.regA2),
 							   processor.readRegister(Processor.regA3)
 							   );
 				processor.writeRegister(Processor.regV0, result);
 				processor.advancePC();
 				break;					   
 			default:
 				Lib.debug(dbgProcess, "Unexpected exception: " +
 					  Processor.exceptionNames[cause]);
 				Lib.assertNotReached("Unexpected exception");
 		}
 	}
 }
