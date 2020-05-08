 package nachos.userprog;
 
 import nachos.machine.*;
 import nachos.threads.*;
 import nachos.userprog.*;
 
 import java.io.EOFException;
 import java.util.HashMap;
 
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
     /**
      * Allocate a new process.
      */
     public UserProcess() {
 		int numPhysPages = Machine.processor().getNumPhysPages();
 		pageTable = new TranslationEntry[numPhysPages];
 		for (int i=0; i<numPhysPages; i++)
 		    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
 		
 		globalLock.acquire();
 		PID = nextPID++;
 		totalPID++;
 		globalLock.release();
 		
 		fileTable[0] = UserKernel.console.openForReading();
 		fileTable[1] = UserKernel.console.openForWriting();
 		
     }
     
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
 		
 		new UThread(this).setName(name).fork();
 	
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
     
     public boolean notInValidRange(int vaddr, int length) {
     	return (vaddr < 0 || length > Machine.processor().makeAddress(numPages - 1, pageSize - 1) - vaddr);
     	
     }
 
     /**
      * Read a null-terminated string from this process's virtual memory. Read
      * at most <tt>maxLength + 1</tt> bytes from the specified address, search
      * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
      * without including the null terminator. If no null terminator is found,
      * returns <tt>null</tt>.
      *
      * @param	vaddr	the starting virtual address of the null-terminated
      *			string.
      * @param	maxLength	the maximum number of characters in the string,
      *				not including the null terminator.
      * @return	the string read, or <tt>null</tt> if no null terminator was
      *		found.
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
 		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
 		
 		// if the given virtual address is out of range, just return 0 because it is invalid
 		if (notInValidRange(vaddr, length)) {
 			return 0;
 		}		
 	
 		byte[] memory = Machine.processor().getMemory();		
 		int numBytesTransferred = 0;					
 		int startVPN = Machine.processor().pageFromAddress(vaddr); // starting = vaddr (first byte of virtual memory to read)
 		int endVPN = Machine.processor().pageFromAddress(vaddr + length); // ending = vaddr + length (first byte to read + number of bytes total to read)			
 		int readOffset = Machine.processor().offsetFromAddress(vaddr);
 		int readLength = 0; // how much we read
 		int startingPhysAddr = 0; // starting physical addr in memory of what we are reading
 		int endyingPhysAddr = 0; // ending physical addr in memory of what we are reading	
 		
 		try {
 			TranslationEntry currentPage;
 			for(int i = startVPN; i <= endVPN; i++) {
 				// get the current page from pageTable
 				currentPage = pageTable[i];
 								
 				// if this page isn't valid, just break the loop
 				if(!pageTable[i].valid) {
 					break;
 				}
 				
 				// get the starting physical address
 				startingPhysAddr = (pageTable[i].ppn * pageSize) + offset;
 				
 				// get the number of bytes to read
 				// we can read up to either the length given or the entire
 				// page minus its offset, depending on which is smaller
 				readLength = Math.min(length, pageSize - readOffset);
 				
 				// check the range of the starting physical address to make sure it's valid
 				if(startingPhysAddr > 0 && startingPhysAddr <= memory.length) {
 					endyingPhysAddr = (currentPage.ppn + 1) * pageSize;					
 					readLength = Math.min(readLength, memory.length - startingPhysAddr); // we want to read as much as possible		
 					
 					// copy memory --> data
 					System.arraycopy(memory, startingPhysAddr, data, offset + numBytesTransferred, readLength);
 					
 					numBytesTransferred += readLength;
 					
 					// mark as used
 					currentPage.used = true;													
 				}							
 			}		
 			return numBytesTransferred;
 		}
 		catch(Exception e) {
 			return numBytesTransferred;
 		}
 		
 		// for now, just assume that virtual addresses equal physical addresses
 		/*if (vaddr < 0 || vaddr >= memory.length)
 		    return 0;
 	
 		int amount = Math.min(length, memory.length-vaddr);
 		System.arraycopy(memory, vaddr, data, offset, amount);
 	
 		return amount;*/			
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
 		
 		// if the given virtual address is out of range, just return 0 because it is invalid
 		if (notInValidRange(vaddr, length)) {
 			return 0;
 		}
 	
 		byte[] memory = Machine.processor().getMemory();		
 		int numBytesTransferred = 0;
 		int startVP = Machine.processor().pageFromAddress(vaddr); //starting = vaddr (since it's first byte of virtual memory to read)
 		int endVP = Machine.processor().pageFromAddress(vaddr + length); //ending = vaddr + length (first byte to read + number of bytes total to read)
 		int writeOffset = Machine.processor().offsetFromAddress(vaddr); 
 		int writeLength = 0; // how much we wrote
 		int startingPhysAddr = 0; // starting physical addr in memory of where we are writing to
 		int endingPhysAddr = 0; // ending physical addr in memory of where we are writing to
 		
 		try {
 			TranslationEntry currentPage;
 			for(int i = startVP; i < endVP; i++) {
 				currentPage = pageTable[i];
 				
 				// if this page isn't valid or is read-only, just break the loop
 				if(!currentPage.valid || currentPage.readOnly) {
 					break;
 				}
 				
 				// get the starting physical addr
 				startingPhysAddr = (currentPage.ppn * pageSize) + writeOffset;
 								
 				// get the number of bytes to write
 				// we can write up to either the length given or the entire
 				// page minus its offset, depending on which is smaller
 				writeLength = Math.min(length, pageSize - writeOffset);
 				
 				if(startingPhysAddr > 0 && startingPhysAddr <= memory.length) {
 					endingPhysAddr = (currentPage.ppn + 1) * pageSize;			
 					writeLength = Math.min(writeLength, memory.length - startingPhysAddr); // we want to write as much as possible
 					
 					// copy data --> memory 
 					System.arraycopy(data, offset + numBytesTransferred,  memory,  startingPhysAddr,  writeLength);
 					
 					numBytesTransferred += writeLength;
 					
 					// mark as used and dirty
 					currentPage.used = true;
 					currentPage.dirty = true;
 				}							
 			}
 			
 			return numBytesTransferred;
 		}
 		catch(Exception e) {
 			return numBytesTransferred;
 		}
 		
 		// for now, just assume that virtual addresses equal physical addresses
 		/*if (vaddr < 0 || vaddr >= memory.length)
 		    return 0;
 	
 		int amount = Math.min(length, memory.length-vaddr);
 		System.arraycopy(data, offset, memory, vaddr, amount);
 	
 		return amount;*/
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
 		if (numPages > Machine.processor().getNumPhysPages()) {
 		    coff.close();
 		    Lib.debug(dbgProcess, "\tinsufficient physical memory");
 		    return false;
 		}
 		
 		UserKernel.freePagesLock.acquire();
 		
 		// create this user process's page table
 		pageTable = new TranslationEntry[numPages];
 		
 		// populate this user process's page table
 		for(int i = 0; i < numPages; i++) {
 			pageTable[i] = new TranslationEntry(i, UserKernel.freePages.poll(), true, false, false, false);
 		}
 		
 		UserKernel.freePagesLock.release();
 
 		// load sections (slightly modified this but most of it was provided)
 		for (int s=0; s<coff.getNumSections(); s++) {
 			CoffSection section = coff.getSection(s);
 	    
 			Lib.debug(dbgProcess, "\tinitializing " + section.getName() + " section (" + section.getLength() + " pages)");
 
 			TranslationEntry COFFTranslationEntry;			
 			for (int i=0; i<section.getLength(); i++) {
 				// get corresponding COFF Translation based on VPN
 		    	int vpn = section.getFirstVPN()+i;
 		    	COFFTranslationEntry = pageTable[vpn];		
 		    	
 		    	// cannot assume vpn = ppn anymore
 		    	section.loadPage(i, COFFTranslationEntry.ppn);
 		    	
 		    	// check if read-only sections and mark appropriately
 		    	if(section.isReadOnly()) {
 		    		COFFTranslationEntry.readOnly = true;
 		    	}				
 	    	}
 		}
 	
 		return true;
     }
 
     /**
      * Release any resources allocated by <tt>loadSections()</tt>.
      */
     protected void unloadSections() {
     	byte[] memory = Machine.processor().getMemory();
     	int startingPhysAddr = 0; // starting physical addr of what we are going to unload
     	int endingPhysAddr = 0; // ending physical addr of what we are going to unload
     	
     	UserKernel.freePagesLock.acquire();
     	
     	for(int i = 0; i < numPages; i++) {
     		// clear its memory using its physical address (physical page 
     		// number * page size) as starting point until the next page as 
     		// ending point
     		startingPhysAddr = pageTable[i].ppn * pageSize; 
     		endingPhysAddr = (pageTable[i].ppn + 1) * pageSize;    		
     		for(int j = startingPhysAddr; j < endingPhysAddr; j++) {
     			memory[j] = 0; // clear this spot in memory
     		}
     		
     		// re-add this page to the list of available pages
     		UserKernel.freePages.add(new Integer(pageTable[i].ppn));
     	}
     	
     	UserKernel.freePagesLock.release();
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
      * Checks to make sure the VA is valid
      * 
      * @param Virtual Address vaddr
      * @return True if valid VA, False if invalid VA
      */
     protected boolean isValidAddress(int vaddr) {
     	int virtualPN = Processor.pageFromAddress(vaddr);
     	return virtualPN < numPages && virtualPN >= 0;
     }
     
     /**
      * Finds a file descriptor location that is unused
      * 
      * @return Index of valid file descriptor or -1 if none exist.
      */
     protected int getDescriptor() {
     	for (int i = 2; i < 16; i++) {
     		if (fileTable[i] == null) {
     			return i;
     		}
     	}
     	return -1;
     }
     
     /**
      * Checks whether the file descriptor is valid
      * 
      * @param descIndex - file descriptor index in the fileTable 
      * @return True if the descriptor exists
      */
     private boolean isValidDescriptor(int descIndex) {
     	if ((descIndex < 0) || (descIndex >= 16)) { //Array Range check
     		return false;
     	} else {
     		return fileTable[descIndex] != null; //Valid Entry check
     	}
     }
 
     /**
      * Handle the halt() system call. 
      */
     private int handleHalt() {
     	if (PID != 0) { //Only root process can call halt
     		return -1;
     	} else {
     		Machine.halt();
     		Lib.assertNotReached("Machine.halt() did not halt machine!");
     		return 0;		
     	}
     }
     /**
      * Handles the creat(filename) syscall
      * 
      * @param filePtr
      * 			Pointer to the file name
      * @return 
      * 			Location of the file descriptor in fileTable
      */
     private int handleCreat(int filePtr) {
     	return handleHelper(filePtr, true);
     }
     
     /**
      * Handles the open(filename) syscall.
      * 
      * @param filePtr 
      * 			Pointer to the file name
      * @return
      * 			Location of the file descriptor in fileTable
      */
     private int handleOpen(int filePtr) {
     	return handleHelper(filePtr, false);
     }
     
     /**
      * 
      * 
      * @param filePtr
      * 			Pointer to the file name
      * @param flag
      * 			Flag determining if its a creat or open call
      * @return
      * 			Location of the file descriptor in fileTable
      */
     private int handleHelper(int filePtr, boolean flag) {
     	if (!isValidAddress(filePtr)) {
     		return -1;
     	}
     	int openDesc = getDescriptor();
     	
     	if (openDesc < 0) { //Already have 16 files open
     		return -1;
     	}
     	
     	String fileName = readVirtualMemoryString(filePtr, 256);
     	OpenFile newFile = UserKernel.fileSystem.open(fileName, flag);
     	
     	if (newFile == null) { //Cannot open file
     		return -1;
     	}
     	
     	fileTable[openDesc] = newFile;
     	
     	return openDesc;
     }
     /**
      * Read from an open file into the buffer
      * 
      * @param fileNo
      * 			Location in fileTable
      * @param bufferPtr
      * 			Pointer to the buffer in VM
      * @param numOfBytes
      * 			How many bytes to write
      * @return
      * 			Total bytes read, -1 on error
      */
     private int handleRead(int fileNo, int bufferPtr, int numOfBytes) {
     	if (!isValidDescriptor(fileNo)) {
     		return -1;
     	}
     	
     	if (!isValidAddress(bufferPtr)) {
     		return -1;
     	}
     	
     	int bytesLeft = numOfBytes;
     	int bytesRead = 0;
     	int readSize = 0;
     	
     	while (bytesLeft != 0) {
     		if (bytesLeft >= pageSize) {
     			readSize = pageSize;
     		} else {
     			readSize = bytesLeft;
     		}
     		
     		byte[] buffer = new byte[readSize];
     		int nextRead = fileTable[fileNo].read(buffer, 0, readSize);
     		
     		if (nextRead < 0) {
     			return -1;
     		}
     		
     		int nextWrite = writeVirtualMemory(bufferPtr, buffer, 0, nextRead);
     		
     		if (nextRead != nextWrite) {
     			return -1;
     		}
     		
     		bytesLeft -= readSize;
     		bytesRead += nextWrite;
     		bufferPtr += nextWrite;
     	}
     	
     	return bytesRead;
     	
     }
     
     /**
      * Write from buffer into an open file
      * 
      * @param fileNo
      * 			Location in fileTable
      * @param bufferPtr
      * 			Pointer to the buffer in VM
      * @param numOfBytes
      * 			How many bytes to write
      * @return
      * 			Total bytes written, -1 on error
      */
     private int handleWrite(int fileNo, int bufferPtr, int numOfBytes) {
     	if (!isValidDescriptor(fileNo)) {
     		return -1;
     	}
     	
     	if (!isValidAddress(bufferPtr)) {
     		return -1;
     	}
     	int bytesLeft = numOfBytes;
     	int bytesWritten = 0;
     	int readSize = 0;
     	
     	while (bytesLeft != 0) {
     		if (bytesLeft >= pageSize) {
     			readSize = pageSize;
     		} else {
     			readSize = bytesLeft;
     		}
     		
     		byte[] buffer = new byte[readSize];
     		int nextRead = readVirtualMemory(bufferPtr, buffer, 0, readSize);
     		
     		if (nextRead != readSize) {
     			return -1;
     		}
     		
     		int nextWrite = fileTable[fileNo].write(buffer, 0,  readSize);
     		if (nextWrite != nextRead) {
     			return -1;
     		}
     		
     		bytesLeft -= readSize;
     		bytesWritten += nextRead;
     		bufferPtr += nextRead;
     	}
     	
     	return bytesWritten;
     }
     
     /**
      * Closes a file in fileTable and frees its space for new files
      * 
      * @param fileNo
      * 			Index of the file
      * @return
      * 			0 on success, -1 on failure
      */
     private int handleClose(int fileNo) {
     	if (!isValidDescriptor(fileNo)) {
     		return -1;
     	}
     	fileTable[fileNo].close();
     	fileTable[fileNo] = null;
     	return 0;
     }
     
     /**
      * Marks file pending deletion
      * 
      * @param filePtr
      * 			Pointer to string with file name
      * @return
      * 			0 on success, -1 on failure
      */
     private int handleUnlink(int filePtr) {
     	if (!isValidAddress(filePtr)) {
     		return -1;
     	}
     	String fileName = readVirtualMemoryString(filePtr, 256);
     	
     	if (UserKernel.fileSystem.remove(fileName)) {
     		return 0;
     	}
     	
     	return -1;
 
     }
     
     private int handleExec(int fileLocation, int numArgs, int argsLocation){
     	if(!isValidAddress(fileLocation) || !isValidAddress(argsLocation)){
     		return -1;
     	}
     	
     	//get the filename
     	String fileName = readVirtualMemoryString(fileLocation, 256);
     	if (fileName == null || !fileName.endsWith(".coff")){
     		return -1;
     	}
     	
     	//Create byte array to store memory locations
     	byte[] memArray = new byte[numArgs * 4];
     	int readBytes = readVirtualMemory(argsLocation, memArray);
     	if (readBytes != (numArgs * 4)){
     		return -1;
     	}
     	
     	String[] argumentList = new String[numArgs];
     	
     	for(int i = 0; i < numArgs ; i++){
         	//Take each memory location, see its validity
     		int memLoc = Lib.bytesToInt(memArray, i*4);
     		if (!isValidAddress(memLoc)){
     			return -1;
     		}
     		//get string from location
     		String s = readVirtualMemoryString(memLoc, 256);
     		if (s == null){
     			return -1;
     		}
     		//put it in arglist array
     		argumentList[i] = s;
     	}	
     	
     	//create and execute child
     	UserProcess child = newUserProcess();
     	child.parent = this;
     	childState childProcess = new childState(child);
     	if (children == null){
     		return -1;
     	}
     	children.put(child.PID, childProcess);
     	child.execute(fileName, argumentList);
     	
     	return child.PID;
     }
 
     private int handleJoin(int pid, int statusLocation){
     	if (!isValidAddress(statusLocation)){
     		return -1;
     	}
     	if (children == null || !children.containsKey(pid)){
     		return -1;
     	}
     	childState child = children.get(pid);
     	if (child == null){
     		return -1;
     	}
     	if (child.isRunning()){
     		joinLock.acquire();
     		while (child.isRunning()){
     			joinCondition.sleep();
     		}
     		joinLock.release();
     	}
     	children.remove(pid);
     	int exit = child.exitStatus;
     	if (exit == Integer.MAX_VALUE){
     		return 0;
     	}
     	
 		writeVirtualMemory(statusLocation, Lib.bytesFromInt(exit));
 		return 1;
     }
 
     private int handleExit(Integer status){
     	joinLock.acquire();
     	
     	if (this.children == null){
     		return -1;
     	}
     	//inform parent that I am exiting
     	childState myState = this.parent.children.get(this.PID);
     	if (myState == null){
     		System.out.println("SOMETHING WENT BAD in join");
     		return -1;
     	}
     	
      	//Set my exit status and "notify" parent
    	this.parent.children.remove(this.PID);
    	myState.exitWithStatus(status);
    	this.parent.children.put(this.PID, myState);
    	
     	for (childState child: children.values()){
     		if (child.isRunning()){
     			child.process.parent = null; //disown
     		}
     	}
     	
     	this.children = null;
     	this.unloadSections();
     	joinCondition.wakeAll();
     	joinLock.release();
     	
     	if (this.PID == 0){
     		Kernel.kernel.terminate();
     	}
     	
     	KThread.finish();
     	return 0;
     	
     }
     private static final int
         syscallHalt = 0,
 		syscallExit = 1,
 		syscallExec = 2,
 		syscallJoin = 3,
 		syscallCreat = 4,
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
 
 		case syscallCreat:
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
 		    Lib.assertNotReached("Unknown system call:" + syscall);
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
     
     private class childState {
     	UserProcess process = null;
     	Integer exitStatus = null;
     	childState(UserProcess process){
     		this.process = process;
     	}
     	boolean isRunning(){
     		return this.process == null;
     	}
     	void exitWithStatus(Integer status){
     		this.process = null;
     		if (status == null){
     			this.exitStatus = Integer.MAX_VALUE;
     		} else {
     			this.exitStatus = status;
     		}
     	}
     }
     
     /** Lock for our global variables */
     private static Lock globalLock = new Lock();
     
     /** Process ID */
     private static int totalPID;
     private static int nextPID;
     protected int PID;
     
     /** Array of file descriptors */
     protected OpenFile[] fileTable = new OpenFile[16];
     
     
 
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
     
     /** variable to create hierarchy for processes **/ 
     private UserProcess parent;
     private HashMap<Integer, childState> children = new HashMap<Integer, childState>();
     protected Lock joinLock; 
     protected Condition joinCondition;
 }
