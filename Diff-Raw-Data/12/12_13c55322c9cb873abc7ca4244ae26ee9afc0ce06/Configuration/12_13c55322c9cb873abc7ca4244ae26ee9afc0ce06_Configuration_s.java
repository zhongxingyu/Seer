 package machine;
 
 /**
  * Contains constants and configuration for the system.
  * 
  * @author pauljohnson
  *
  */
 public class Configuration {
 	/** class name for the scheduler */
 	public final static String scheduler = "kernel.BasicScheduler";
 
 	/** class name for the page replacer */
 	public final static String replacer = "kernel.BasicPageReplacement";
 	
 	/** class name for the file system */
 	public final static String fileSystem = "filesystem.BasicFileSystem";
 
 	public final static String filesDir = "files";
 	
 	public final static int maxFiles = 64;
 	public final static int maxProcesses = 256;
 	
 	/** Name of the program to load and execute on startup*/
	public static String shellProgramName = "sharedtest.coff";
 	
 	/** first process args */
 	public static String[] processArgs;
 	
 	/** Number of ticks for a process to be on the processor */
 	public static int quantum = 10;
 	
 	/** Size of a page, in bytes. */
 	public static final int pageSize = 0x400;
 	
 	/** Number of physical pages in memory. */
 	public static int numPhysPages = 4;
 	
 	public static int numVirtualPages = 32;
 
 	/** How often the clock ticks */
 	public static int switchTime = 5;
 	
 	/** delay for the hard drive */
 	public static int driveDelay = 10;
 	
 	/** name of the file that makes up the file system */
 	public static String diskFileName = "filesystem";
 	
 	/** File system block size*/
 	public static int blockSize = 1024;
 	
 	public static int systemBlocks = 6;
 	
 	/** Number of blocks in the file system */
 	public static int numberOfBlocks = 512;
 	
 	/** Length of the boot block */
 	public static int bootBlockLength = 1024;
 	
 	/** size of an entry in the FAT table*/
 	public static int fatEntrySize = 4;
 	
 	/** length of the fat table */
 	public static int fatLength = numberOfBlocks * fatEntrySize;
 	
 	/** size of a file entry */
 	public static int fileEntrySize = 32;
 	
 	/** length of the table of file entries entries */
 	public static int entryTableLength = maxFiles * 32;
 	
 	/** size of the superblock */
 	public static int superBlockSize = 1024;
 	
 	/** offset from the start of disk to where the file blocks start*/
 	public static int fileOffset = bootBlockLength + fatLength + superBlockSize + entryTableLength;
 	
 	/** Details for a file entry table */
 	// file name
 	public static int fileNameOffset = 0;
 	public static int fileNameLength = 16;
 	
 	// first block
 	public static int fileFirstBlockOffset = 28;
 	public static int fileFirstBlockLength = 4;
 	
 	// file length in bytes
 	public static int fileLengthOffset = 24;
 	public static int fileLengthLength = 4;
 	
 	/** Details for superblock */
 	
 	// length of root directory
 	public static int fileCountOffset = 0;
 	public static int fileCountLength = 4;
 	
 	/**
 	 * Configuration for coff loading
 	 * 
 	 */
 	// length of coff headers
 	public static final int headerLength = 20;
 	public static final int aoutHeaderLength = 28;
 	
 	public static final int totalHeaderLength = headerLength + aoutHeaderLength;
 	
 	// number of pages for the stack
 	public static final int stackPages = 8;
 	
 	public static final int coffSectionHeaderLength = 40;
 }
