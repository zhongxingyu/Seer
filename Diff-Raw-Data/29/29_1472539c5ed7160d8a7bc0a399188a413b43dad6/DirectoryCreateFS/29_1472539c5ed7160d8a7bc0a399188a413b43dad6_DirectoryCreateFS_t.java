 package filesystem;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 
 import machine.Configuration;
 import machine.Lib;
 
 public class DirectoryCreateFS {
 
 	public static int[] fat;
 	public static int freeBlockIndex;
 	
 	
 	/**
 	 * This is a script to create a filesystem from the files in the files directory
 	 * 
 	 * 
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		System.out.println("Creating File System");
 		
 		fat = new int[Configuration.numberOfBlocks];
 		freeBlockIndex = 0;
 		
 		new File(Configuration.diskFileName).delete();
 		
 		RandomAccessFile fs = new RandomAccessFile(Configuration.diskFileName, "rw");
 
 		// write nulls to file
 		blankDisk(fs);
 		
 		// open boot block and write first kilobyte to file
 		writeBootBlock(fs);
 		
 		// get list of files from root directory
 		File testDir = new File("files");
 
 		// blank fat table
 		Arrays.fill(fat, -2);
 
		writeFiles(testDir, fs, 0, testDir.listFiles().length);
 		
 		// write fat table out to disk
 		fs.seek(Configuration.bootBlockLength);
 		for(int i = 0; i < Configuration.numberOfBlocks; i++){
 			//fs.seek(1024 + (i*4));
 			fs.write(Lib.bytesFromInt(fat[i]));
 		}
 	}
 	
 	/**
 	 * Recursive function to load directory into file system
 	 * 
 	 * 
 	 * @param root
 	 * @param fs
 	 * @param previous used for the .. entry in the directory
 	 * @return the length in blocks that this directory takes up
 	 * @throws IOException
 	 */
 	private static int writeFiles(File root, RandomAccessFile fs, int previousBlock, int previousLength) throws IOException{
 		File[] files = root.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File file) {	
 				if(file.getName().startsWith(".") || file.isDirectory()){
 					return false;
 				}else{
 					
 					return true;
 				}
 			}
 		});
 		
 		FileTableEntry[] entries = new FileTableEntry[files.length];
 	
 		byte[] block = new byte[Configuration.blockSize];
 		
 		int read = 0;
 
 		int startBlock = freeBlockIndex;
 		
 		// seek the start of the current free block and reserve a block for the directory table
 		fs.seek((Configuration.systemBlocks+startBlock+1) * Configuration.blockSize);
 		
 		fat[startBlock] = -1;
 		
 		freeBlockIndex++;
 		
 		for(int i = 0; i < files.length; i++){
 			File file = files[i];
 			
 			System.out.println("Adding - " + file.getName());
 			
 			entries[i] = new FileTableEntry(file.getName(), freeBlockIndex, (int)file.length());
 			
 			FileInputStream fis = new FileInputStream(file);
 			
 			// seek the start of the file
 			fs.seek((Configuration.systemBlocks * Configuration.blockSize) + (freeBlockIndex * Configuration.blockSize));
 			
 			while((read = fis.read(block)) != -1){
 				fs.write(block, 0, read);
 
 				fat[freeBlockIndex] = ++freeBlockIndex;
 			}
 			
 			fat[freeBlockIndex-1] = -1;
 		}
 		
 		// write directories setting start after each?
 		File[] dirs = root.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File file) {	
 				if(!file.getName().startsWith(".") && file.isDirectory()){
 					return true;
 				}else{
 					
 					return false;
 				}
 			}
 		});
 		
 		int[] dirBlocks = new int[dirs.length];
 		
 		for(int i = 0; i < dirs.length; i++){
 			System.out.println(dirs[i].getName());
 			
 			dirBlocks[i] = writeFiles(dirs[i], fs, startBlock, entries.length+2);
 		}
 		
 		// write entry for directory
 		
 		fs.seek((Configuration.systemBlocks+startBlock) * Configuration.blockSize);
 		
 		// add . and .. entries
 		fs.write(new FileTableEntry(".", startBlock, entries.length+2, true).toBytes());
 		fs.write(new FileTableEntry("..", previousBlock, previousLength, true).toBytes());
 		
 		// add file entries
 		for(int i = 0; i < entries.length; i++){
 			fs.write(entries[i].toBytes());
 		}
 		
 		// add directory entries
 		for(int i = 0; i < dirs.length; i++){
 			fs.write(new FileTableEntry(dirs[i].getName(), dirBlocks[i], dirs[i].listFiles().length, true).toBytes());
 		}
 		
 		return startBlock;
 	}
 
 	/**
 	 * Write the boot block to the first part of the disk file
 	 * 
 	 * @param fs
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private static void writeBootBlock(RandomAccessFile fs)
 			throws FileNotFoundException, IOException {
 		File boot_block = new File("boot_block");
 		
 		FileInputStream bbfis = new FileInputStream(boot_block);
 		
 		byte[] bbbytes = new byte[Configuration.bootBlockLength];
 		bbfis.read(bbbytes);
 		
 		fs.seek(0);
 		fs.write(bbbytes);
 	}
 
 	/**
 	 * Blank the disk before we write anything to it
 	 * 
 	 * @param fs
 	 * @throws IOException
 	 */
 	private static void blankDisk(RandomAccessFile fs) throws IOException {
 		byte[] nulls = new byte[Configuration.blockSize];
 		
 		for(int i = 0; i < Configuration.numberOfBlocks+Configuration.systemBlocks; i++){
 			fs.write(nulls);
 		}
 	}
 
 }
