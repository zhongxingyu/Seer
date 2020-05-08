 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /** Zip
  * 
  * Compress the given files into a zip. Simple as that!
  * 
  * @author Justin Greet
  *
  */
 public class Zip implements MigratableProcess {
 	// If the arguments are invalid, don't run.
 	private boolean validArgs;
 	// The file to write to.
 	private TransactionalFileOutputStream outFile;
	//The target zip stream
	private ZipOutputStream zipOutput;
 	//The files to write
 	private String[] filesToWrite;
 	//The index into the filesToWrite array
 	private int filesIndex = 0;
 	
 	private volatile boolean suspending;
 
 	/** The constructor for Zip.
 	 * 
 	 * @param args
 	 * args[0]: The name/location of the resulting zip.
 	 * args[1 - n]: The files to compress.
 	 */
 	public Zip(String[] args) {
 		try {
 			validArgs = true;
 			parseArgs(args);
 		} catch(IllegalArgumentException e) {
 			validArgs = false;
 			System.out.println(e.getMessage());
 			System.out.println("Zip usage: $targetFilename $file1 $file2 ...");
 		}
 	}
 	
 	/** parseArgs(args)
 	 * 
 	 * parseArgs makes sure the arguments passed to WebCrawler are correct,
 	 * and throws an exception otherwise.
 	 * 
 	 * @param args An array of the arguments to the function.
 	 * args[0]: The destination location of the zip file.
 	 * args[1 - n]: The files to compress into the zip. 
 	 */
 	private void parseArgs(String[] args) {
 		if (args.length <= 1) throw new IllegalArgumentException("Not enough arguments.");
 		String targetFilename = args[0];
 		if (!targetFilename.endsWith(".zip")) throw new IllegalArgumentException("The target filename must end in .zip");
 		outFile = new TransactionalFileOutputStream(targetFilename, false);
		zipOutput = new ZipOutputStream(outFile);
 		//Store all of the given files in an array.
 		filesToWrite = new String[args.length - 1];
 		for (int i = 1; i < args.length; i++) {
 			filesToWrite[i - 1] = args[i];
 		}
 	}
 	
 	/** run()
 	 * 
 	 * Simply runs the compression.
 	 */
 	@Override
 	public void run() {
 		if (validArgs) {
 			zip();
 		}
 		else {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/** zip()
 	 * 
 	 * Look at each file in the filesToWrite array, and add them to the
 	 * zip if they're valid. The safe point occurs in between the writings of separate files.
 	 */
 	private void zip() {
 		File curFile;
 		String curFilename;
 		ZipEntry curEntry;
 		FileInputStream curInput;
 		
 		while (filesIndex < filesToWrite.length && !suspending) {
 			curFilename = filesToWrite[filesIndex];
 			curFile = new File(curFilename);
 			//If the current file doesn't exist, simply let the user know and continue.
 			if (!curFile.exists() || curFile.isDirectory()) {
 				System.out.println("File " + curFilename + " does not exist. Continuing...");
 				filesIndex += 1;
 				continue;
 			}
 			//If we get here, the current file exists and can thus be compressed.
 			curEntry = new ZipEntry(curFilename);
 			try {
 				zipOutput.putNextEntry(curEntry);
 				curInput = new FileInputStream(curFilename);
 				byte[] buffer = new byte[1024];
 				int len;
 				// Note that we don't need transactional writing because the process can never
 				// be migrated at this stage.
 	    		while ((len = curInput.read(buffer)) > 0) {
 	    			zipOutput.write(buffer, 0, len);
 	    		}
 	 
 	    		curInput.close();
 	    		zipOutput.closeEntry();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			filesIndex += 1;	
 		}
 		//After all the files are written, close the zip output stream.
 		if (!suspending && filesIndex == filesToWrite.length) {
 			try {
 				zipOutput.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		suspending = false;
 	}
 	
 	/** suspend()
 	 * 
 	 * Bring the process to a safe state to make it serializable.
 	 */
 	public void suspend() {
 		suspending = true;
 		while (suspending);
 	}
 
 }
