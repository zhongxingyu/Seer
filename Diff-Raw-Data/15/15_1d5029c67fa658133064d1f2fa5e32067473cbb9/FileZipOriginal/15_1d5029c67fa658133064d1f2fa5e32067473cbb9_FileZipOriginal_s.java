 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /** 
  * This command line tool takes two arguments
  * 1. The Root of the Windchill folder. e.g: "Z:/Windchill" (exactly as shown. NO forward slash (/) after "Windchill")
  * 2. a date, in this format yymmdd. e.g: 121130 (November 30th, 2012). This would fetch everything from the November 30th, 2012 until today
  *  @author asaif
  * 

  */
 
 public class FileZipOriginal {
 
 	private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
 
 	private static Calendar cal = Calendar.getInstance();
 	private static SimpleDateFormat Date_format = new SimpleDateFormat("yyMMdd");
 	private static int currentDate = Integer.parseInt(Date_format.format(cal.getTime())); //current date in Int format
 	/**
 	 * date inputed 
 	 */
 	private static int date; 
 	private static String windchillFolder;
 
 
 	public static void main(String[] args) throws Exception {
 
 		System.out.println("Today's Date: " + currentDate);
 		try 
 		{	
 			windchillFolder = args[0];
 			date = Integer.parseInt(args[1]);
 			new File(windchillFolder+"/logs/TechPack").mkdir();
 			new FileZipOriginal(windchillFolder+"/logs", windchillFolder+"/logs/TechPack/ServerLogs.zip", true, true);
 			new FileZipOriginal(windchillFolder+"/codebase", windchillFolder+"/logs/TechPack/CodebaseFiles.zip", true,"codebaseFiles.txt");
 			new FileZipOriginal(windchillFolder+"/codebase/com/lcs/wc/client", windchillFolder+"/logs/TechPack/ClientFiles.zip", true,"clientFiles.txt");
 			new FileZipOriginal(windchillFolder+"/codebase/com/lcs/wc/client/web", windchillFolder+"/logs/TechPack/ClientWebFiles.zip", true,"clientWebFiles.txt");
 			new FileZipOriginal(windchillFolder+"/codebase/rfa/jsp/main", windchillFolder+"/logs/TechPack/FlexPDM_Version.zip", true,"FlexPDM_Version.txt");
 			new FileZipOriginal(windchillFolder+"/logs/TechPack", windchillFolder+"/logs/TechPack.zip", true);
 		}
		catch (ArrayIndexOutOfBoundsException e) //When no arguments are given ArrayIndexOutofBoundsException is thrown.
 		{
 			System.out.println("Please specify The Root of the Windchill folder e.g: \"Z:/Windchill\" and a date in this format: yymmdd");
 		}
 	}
 
 	/**
 	 * ZIP the server files (MethodServer and ServerManager)
 	 * @param fileToZip
 	 * @param zipFile
 	 * @param excludeContainingFolder
 	 * @param isServerLogs
 	 * @throws IOException
 	 */
 	public FileZipOriginal(String fileToZip, String zipFile, boolean excludeContainingFolder, boolean isServerLogs)
 			throws IOException {   
 
 			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));   
 			File srcFile = new File(fileToZip);
 			
 			if(excludeContainingFolder && srcFile.isDirectory()) {
 				for(String fileName : srcFile.list()) {
 					for(int i = date; i<=currentDate; i++){		//date entered is always smaller than the current date
 						/*Matches any filename that contains the date*/
						boolean b = Pattern.matches(".*"+i+".*", fileName); 
						if(b){
 							addToZip("", fileToZip + "/" + fileName, zipOut);
 						}
 					}
 				}
 			}
 			zipOut.flush();
 			zipOut.close();
 			System.out.println("Successfully created " + zipFile);
 	}
 
 	/**
 	 * ZIP Folders containing property Files
 	 * @param fileToZip
 	 * @param zipFile
 	 * @param excludeContainingFolder
 	 * @param fileList
 	 * @throws IOException
 	 */
 	public FileZipOriginal(String fileToZip, String zipFile, boolean excludeContainingFolder, String fileList)
 			throws IOException {  
 
 		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));    
 
 		File srcFile = new File(fileToZip);
 
 		if(excludeContainingFolder && srcFile.isDirectory()) {
 			for(String fileName : srcFile.list()) {	
 
 				/*Reading text File*/
 				FileReader file = null;
 				BufferedReader reader = null;
 				try {
 					file = new FileReader("src/parsable/"+fileList); 
 					reader = new BufferedReader(file);
 					String line = "";
 					while ((line = reader.readLine()) != null) { 
 						boolean b = Pattern.matches(line, fileName); /*Matches filename with names in text file*/
 						if(b){
 							addToZip("", fileToZip + "/" + fileName, zipOut); 
 						}
 					}
 				} catch (FileNotFoundException e) {
 					throw new RuntimeException("File not found");
 				} catch (IOException e) {
 					throw new RuntimeException("IO Error occured");
 				} finally {
 					if (file != null) {
 						try {
 							file.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				reader.close();
 			} 
 			/*Done Reading text File*/
 		}
 		zipOut.flush();
 		zipOut.close();
 		System.out.println("Successfully created " + zipFile);	
 	}
 
 	/**
 	 * ZIP the FINAL TechPack
 	 * @param fileToZip
 	 * @param zipFile
 	 * @param excludeContainingFolder
 	 * @throws IOException
 	 */
 
 	public FileZipOriginal(String fileToZip, String zipFile, boolean excludeContainingFolder)
 			throws IOException {        
 		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));    
 
 		File srcFile = new File(fileToZip);
 
 		if(excludeContainingFolder && srcFile.isDirectory()) {
 			for(String fileName : srcFile.list()) {
 				addToZip("", fileToZip + "/" + fileName, zipOut);	
 			}
 		}
 		zipOut.flush();
 		zipOut.close();
 		System.out.println("Successfully created " + zipFile);
 	}
 
 	/**
 	 * This private method that adds File to the Zip
 	 * @param path
 	 * @param srcFile
 	 * @param zipOut
 	 * @throws IOException
 	 */
 	private void addToZip(String path, String srcFile, ZipOutputStream zipOut)
 			throws IOException {        
 		File file = new File(srcFile);
 		String filePath = "".equals(path) ? file.getName() : path + "/" + file.getName();
 
 		if (file.isDirectory()) {
 			for (String fileName : file.list()) { 
 
 				addToZip(filePath, srcFile + "/" + fileName, zipOut);
 			}
 		} else {
 			System.out.println("Adding File: " + srcFile);
 			zipOut.putNextEntry(new ZipEntry(filePath));
 			FileInputStream in = new FileInputStream(srcFile);
 
 			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
 			int len;
 			while ((len = in.read(buffer)) != -1) {
 				zipOut.write(buffer, 0, len);
 			}
 
 			in.close();
 		}
 	}
 }
