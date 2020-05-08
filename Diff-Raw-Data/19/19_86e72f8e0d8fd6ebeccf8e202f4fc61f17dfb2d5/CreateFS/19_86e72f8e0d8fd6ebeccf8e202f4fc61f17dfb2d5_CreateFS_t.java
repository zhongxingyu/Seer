 package filesystem;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 
 import machine.Configuration;
 import machine.Lib;
 
 public class CreateFS {
 
 	/**
 	 * This is a script to create a filesystem from the files in the files directory
 	 * 
 	 * 
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		RandomAccessFile fs = new RandomAccessFile(Configuration.diskFileName, "rw");
 
 		// write nulls to file
 		byte[] nulls = new byte[Configuration.blockSize];
 		
 		for(int i = 0; i < Configuration.numberOfBlocks+Configuration.systemBlocks; i++){
 			fs.write(nulls);
 		}
 		
 		
 		// open boot block and write first kilobyte to file
 		File boot_block = new File("boot_block");
 		
 		FileInputStream bbfis = new FileInputStream(boot_block);
 		
 		byte[] bbbytes = new byte[Configuration.bootBlockLength];
 		bbfis.read(bbbytes);
 		
 		fs.seek(0);
 		fs.write(bbbytes);
 		
 		// get list of files from c directory
 		File testDir = new File("files/");
 		
		File[] files = testDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(".")){
					return false;
				}else{
					return true;
				}
			}
		});
 		
 		// get list of files from files directory
 		
 		
 		
 		FileTableEntry[] entries = new FileTableEntry[files.length];
 		
 		int currentBlock = 0;
 		
 		byte[] block = new byte[Configuration.blockSize];
 		
 		int read = 0;
 		
 		int[] fat = new int[Configuration.numberOfBlocks];
 		Arrays.fill(fat, -2);
 		
 		fs.seek(Configuration.fileOffset);
 		
 		for(int i = 0; i < entries.length; i++){
 			File file = files[i];
 			
 			//System.out.println(file.getName());
 			
 			entries[i] = new FileTableEntry(file.getName(), currentBlock, (int)file.length());
 			
 			FileInputStream fis = new FileInputStream(file);
 			
 			fs.seek((Configuration.systemBlocks * Configuration.blockSize) + (currentBlock * Configuration.blockSize));
 			
 			
 			while((read = fis.read(block)) != -1){
 				fs.write(block, 0, read);
 			
 				fat[currentBlock] = ++currentBlock;
 			}
 			
 			fat[currentBlock-1] = -1;
 		}
 		
 		// write fat table out to disk
 		fs.seek(Configuration.bootBlockLength);
 		for(int i = 0; i < Configuration.numberOfBlocks; i++){
 			//fs.seek(1024 + (i*4));
 			fs.write(Lib.bytesFromInt(fat[i]));
 		}
 		
 		// write superblock to disk
 		fs.seek(Configuration.bootBlockLength + Configuration.fatLength + Configuration.fileCountOffset);
 		fs.write(Lib.bytesFromInt(files.length));
 		
 		
 		fs.seek(Configuration.bootBlockLength + Configuration.fatLength + Configuration.superBlockSize);
 		
 		for(int i = 0; i < entries.length; i++){
 			//fs.seek(tableOffset + (i * Configuration.fileEntrySize));
 			
 			fs.write(entries[i].toBytes());
 		}
 	}
 
 }
