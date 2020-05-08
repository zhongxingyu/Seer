 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Random;
 import java.util.logging.Logger;
 
 public class ManyFilesTester {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ManyFilesTester tester = new ManyFilesTester();
 		final int NBR_FILES = 1000000;
 		//tester.deleteFiles();
 		//tester.writeFiles(NBR_FILES);
 		tester.readFiles(500, NBR_FILES);
 	}
 
 	/**
 	 * Delete all test files and the temp directory
 	 */
 	private void deleteFiles() {
 
 		final int LOG_INCREMENT = 10000;
 		File tempDir = new File("tempDir");
 		String[] filenames = tempDir.list();
 		int count = 0;
 		int logCount = 0;
 		for (int i = 0; i < filenames.length; i++) {
 			File f = new File(tempDir, filenames[i]);
 			f.delete();
 			count++;
 			if (++logCount > LOG_INCREMENT) {
 				System.out.println("Deleted files: " +count);
 				logCount = 0;
 			}
 		}
 		System.out.println("Deleted " +count +" files");
 		tempDir.delete();
 	}
 
 	/**
 	 * Read the specified files
 	 * @param nbrFilesToRead
 	 */
 	private void readFiles(int nbrFilesToRead, int totalNbrFiles) {
 
 		Random rnd = new Random();
 		File tempDir = new File("tempDir");
 		for (int i = 0; i < nbrFilesToRead; i++) {
 			// Create filename
 			StringBuilder sb = new StringBuilder();
 			sb.append(rnd.nextInt(totalNbrFiles));
 			sb.append(".txt");
 			String filename = sb.toString();
 			long startTime = System.currentTimeMillis();
 			File f = new File(tempDir, filename);
 			byte[] bytes = readFile(f);
 			long endTime = System.currentTimeMillis();
 			System.out.println("Millis to read file " +filename +": " +""+(endTime - startTime));
 		}
 		
 	}
 	
 	/**
 	 * Read the specified file
 	 * @param f
 	 * @return the bytes read
 	 */
 	private byte[] readFile(File f) {
 		byte[] bytes = new byte[1000];
 		try {
 			FileInputStream fis = new FileInputStream(f);
 			fis.read(bytes);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return bytes;
 	}
 
 	/**
 	 * Write the number of files specified in a temp directory
 	 * 
 	 * @param nbrFiles
 	 *            The number of files to create
 	 */
 	private void writeFiles(int nbrFiles) {
 
 		String contents = "This is the content of the file";
 		byte[] bytes = contents.getBytes();
 
 		File tempDir = new File("tempDir");
 		tempDir.mkdir();
 		final int LOG_INCREMENT = 10000;
 		int logCount = 0;
 		long totalDelay = 0L; // Total nbr milliseconds to write the files in a
 								// loop
 		for (int i = 0; i < nbrFiles; i++) {
 			File f = new File(tempDir, i + ".txt");
 			long startTime = System.currentTimeMillis();
 			writeFile(f, bytes);
 			long endTime = System.currentTimeMillis();
 			totalDelay += (endTime - startTime);
 			logCount++;
 			if (logCount > LOG_INCREMENT) {
 				System.out.println("Write group: "
 						+ Math.floor(i / LOG_INCREMENT)
 						+ " average delay (millis): " + totalDelay);
 				logCount = 0;
 				totalDelay = 0L;
 			}
 		}
 	}
 
 	/**
 	 * Write the bytes to the specified file
 	 * @param f
 	 * @param bytesToWrite
 	 */
 	private void writeFile(File f, byte[] bytesToWrite) {
 
 		FileOutputStream fos = null;
 		try {
 			try {
 				fos = new FileOutputStream(f);
 				fos.write(bytesToWrite, 0, bytesToWrite.length);
 			} finally {
 				fos.flush();
 				fos.close();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method is added to do some playing around with egit
 	 */
 	private void testMethod() {
 		System.out.println("Hello from testMethod()");
 	}
 	
 	/**
 	 * This method is added to test multiple branches
 	 */
 	private void testFeatureMethod() {
		
 	}
 }
