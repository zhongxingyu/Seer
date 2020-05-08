 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.ArrayList;
 
 
 // Our code must be able to do this
 public class JavaZip
 {
 	private static final String TEST1 = "this is a test";
 	private static final String TEST2 = "this is a test, a much\nmore extensive test";
 	private static final String TEST3 = "this is a test, a much\nmore extensive test! HURAAH!";
	private static final String TEST4 = "this akwerlthwailt!";
 
 	public static void main(String[] args)
 	{
 		byte[] buffer = new byte[1024];
 		String workingDir = System.getProperty("user.dir");
 		File testFile = new File(workingDir, "test.txt");
 		File testFile2 = new File(workingDir, "test2.txt");
 		File testFile3 = new File(workingDir, "test3.txt");
		File testFile4 = new File(workingDir, "test4.txt");
 
 		List<File> testFiles = new ArrayList<File>();
 		testFiles.add(testFile);
 		testFiles.add(testFile2);
 		testFiles.add(testFile3);
		testFiles.add(testFile4);
 
 		for (File f : testFiles) {
 			if (f.exists())
 			  try {
 			    f.delete();
 			  } catch (Exception e) {}
 		}
 		
 		writeTestFile(testFile, TEST1);
 		writeTestFile(testFile2, TEST2);
 		writeTestFile(testFile3, TEST3);
		writeTestFile(testFile4, TEST4);
 		
 		File outputFile = new File(workingDir, "java.zip");
 		if (outputFile.exists()) {
 		  try {
 		    outputFile.delete();
 		  } catch (Exception e) {
 		    System.out.println("File not deleted");
 		  }
 		}
 
 		try
 		{
 			FileOutputStream outFile = new FileOutputStream(outputFile);
 			ZipOutputStream outZip = new ZipOutputStream(outFile);
 
 			InputStream inFile;
 
 			for (File f : testFiles) {		
 				inFile = new FileInputStream(f);					
 				ZipEntry entry = new ZipEntry(f.getName());
 				outZip.putNextEntry(entry);
 			
 				int len = 0;
 				while ((len = inFile.read(buffer)) > 0)
 				{
 					outZip.write(buffer, 0, len);
 					//for (byte b : buffer)
 					  //if (b != 0)
 					    //System.out.println(Integer.toHexString(b));
 				}
 
 				inFile.close();
 				outZip.closeEntry();
 			}
 			outZip.close();
 		}
 		catch (IOException e)
 		{
 		  System.out.println("Exception caught");
 		}
 	}
 	
 	public static void writeTestFile(File file, String text) {
 	  try {
   	  if (!file.exists()) {
         file.createNewFile();
       }
   
       FileWriter fw = new FileWriter(file.getAbsoluteFile());
       BufferedWriter bw = new BufferedWriter(fw);
       bw.write(text);
       bw.close();
 	  } catch (IOException e) {
 	    System.out.println("No test file");
 	  }
 	}
 	
 	public static void readBytesFromZip () {
 	  
 	}
 }
