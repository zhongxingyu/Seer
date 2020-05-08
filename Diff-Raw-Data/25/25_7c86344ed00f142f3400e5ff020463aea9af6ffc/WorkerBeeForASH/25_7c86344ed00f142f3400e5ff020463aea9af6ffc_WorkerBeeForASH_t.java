 package mainScribt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
import parser.parseSTMK;
 import ashGui.ASH_JFrame;
 
 public class WorkerBeeForASH implements Runnable {
 	
 	String textContent = "";
 	String pathToFiles = "U:/kostromdecklung - OSD Team Liste/ASH_files/01-2011";
 	String pathToDestinationFiles = "";
 //	String filenamePDF = "U:/kostromdecklung - OSD Team Liste/ASH_files//Anschreiben_6.pdf";
 	
	parseSTMK parser;
 	PDFTextParser pdfTextParserObj;
 	String result = "";
 	
 	File[] listOfFiles;
 	File tmpFile;
 	FileReader in;
 	FileWriter out;
 	
 	int count = 0;
 	// debug
 	int alreadyExisted = 0;
 	int single = 0;
 	
 	public void work() {
 		
 		ASH_JFrame.instance().getContentPane().setOutput("Bee is starting to work...");
 		
 		findFiles(pathToFiles);
 		
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].getName().toLowerCase().endsWith("pdf")) {
 				
 				convertPdfToTxt(listOfFiles[i].getName());
 				
				parser = new parseSTMK();
 				parser.setString(textContent);
 				try {
 					parser.parse();
 					System.out.print(listOfFiles[i] + "\n" +
 							"Nachname: " + parser.getNachname() + "\t" +
 							"Datum: " + parser.getDatum() + "\t" + 
 							"Kennzahl: " + parser.getKennzahl() + "\n" + 
 							"Leistung: " + parser.getLeistung() + " kWp\t" + 
 							"LeistungNeu: " + parser.getLeistungNeu() + " kWp\t" +
 							"Differenz: " + parser.getDifferenz() + " " + parser.getErweiterung() +							
 							"\n\n");
 					
 					ASH_JFrame.instance().getContentPane().setOutput(listOfFiles[i] + "\n" +
 							"Nachname: " + parser.getNachname() + "\t" +
 							"Datum: " + parser.getDatum() + "\t" + 
 							"Kennzahl: " + parser.getKennzahl() + "\n" + 
 							"Leistung: " + parser.getLeistung() + " kWp\t" + 
 							"LeistungNeu: " + parser.getLeistungNeu() + " kWp\t" +
 							"Differenz: " + parser.getDifferenz() + " " + parser.getErweiterung() +							
 							"\n\n");
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
				result += "ST" + ";" + 
 						parser.getNachname() + ";" + 
 						parser.getDatum() + ";" + 
 						parser.getKennzahl() + ";" + 
 		    			parser.getLeistung() + ";" + 
 		    			parser.getDifferenz() + ";" + 
 		    			parser.getErweiterung() + ";" + 
 		    			parser.getLeistungNeu() + ";" + 
 		    			listOfFiles[i].getName() +
 		    			"\n";
 				pdfTextParserObj.writeTexttoFile(result, this.pathToFiles + "//result.csv");
 				
 				// create new folder, copy files to that folder and rename them
 				
 				File f = new File(pathToFiles + "/arrangedCopies");
 				try{
 					if(f.mkdir());
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 				
 				tmpFile = new File(pathToFiles + "/arrangedCopies//" + parser.getNachname() + 
 						"_" + parser.getDatum() + ".pdf");
 				
 //				File oldFile = new File(pathToFiles);
 //				listOfFiles[i].renameTo(oldFile);
 				
 				// rename file and copy to to arrangedCopies folder if filename doesn't exist
 				if (ASH_JFrame.instance().getUmbenennen()) {
 					if (!tmpFile.exists()) {
 						single++;
 						try {
 							copy(pathToFiles + "//" + listOfFiles[i].getName(), (this.pathToDestinationFiles + "//" + parser.getNachname() + 
 									"_" + parser.getDatum() + ".pdf"));
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					} 
 					// if filname exists: put count into name. increase count until filname doesn't exist
 					else if (tmpFile.exists()) {
 						alreadyExisted ++;
 						String name = "";
 						boolean exists = true;
 						
 						while (exists) {
 							
 							count ++;
 							name = pathToDestinationFiles + "//" + parser.getNachname() + 
 									"(" + count + ")" + 
 									"_" + parser.getDatum() + ".pdf";
 							tmpFile = new File(name);
 							
 							if (tmpFile.exists()) {
 								exists = true;
 							} else {
 								exists = false;
 								count = 0;
 							}
 						}
 						try {
 							copy(pathToFiles + "//" + listOfFiles[i].getName(), name);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					ASH_JFrame.instance().getContentPane().setOutput("Done. \n" + single + " Bescheide waren eindeutig, \n" + 
 							alreadyExisted + " Bescheide wurden umbenannt.\n" + 
 							"------------------------------------\n" + 
 							(single+alreadyExisted) + " Bescheide gesammt.");
 				}
 				else {
 					ASH_JFrame.instance().getContentPane().setOutput("Done");
 				}
 			}
 			
 		}
 		
 		
 	}
 	
 	public void convertPdfToTxt(String filenamePDF) {
 		
         pdfTextParserObj = new PDFTextParser();
         
         pdfTextParserObj.setFolder(pathToFiles);
         
         textContent = pdfTextParserObj.pdftoText(filenamePDF);
         
         if (textContent == null) {
         	System.out.println("PDF to Text Conversion failed.");
         }
         else {
 //        	System.out.println("\nThe text parsed from the PDF Document....\n" + textContent);
 //        	pdfTextParserObj.writeTexttoFile(pdfToText, arguments[1]);
         	
         	
         }
     }
 	
 	/**
 	 * read all files in a folder
 	 */
 	private void findFiles(String path) {
 		
 //		File folder = new File("U:/kostromdecklung - OSD Team Liste/ASH_files");
 		File folder = new File(path);
         listOfFiles = folder.listFiles();
         
 	}
 	
 	/**
 	 * copy a file 
 	 * 
 	 * @param fromFileName
 	 * @param toFileName
 	 * @throws IOException
 	 */
 	public static void copy(String fromFileName, String toFileName)
 		      throws IOException {
 		    File fromFile = new File(fromFileName);
 		    File toFile = new File(toFileName);
 
 		    if (!fromFile.exists())
 		      throw new IOException("FileCopy: " + "no such source file: "
 		          + fromFileName);
 		    if (!fromFile.isFile())
 		      throw new IOException("FileCopy: " + "can't copy directory: "
 		          + fromFileName);
 		    if (!fromFile.canRead())
 		      throw new IOException("FileCopy: " + "source file is unreadable: "
 		          + fromFileName);
 
 		    if (toFile.isDirectory())
 		      toFile = new File(toFile, fromFile.getName());
 
 		    if (toFile.exists()) {
 		      if (!toFile.canWrite())
 		        throw new IOException("FileCopy: "
 		            + "destination file is unwriteable: " + toFileName);
 		      System.out.print("Overwrite existing file " + toFile.getName()
 		          + "? (Y/N): ");
 		      System.out.flush();
 		      BufferedReader in = new BufferedReader(new InputStreamReader(
 		          System.in));
 		      String response = in.readLine();
 		      if (!response.equals("Y") && !response.equals("y"))
 		        throw new IOException("FileCopy: "
 		            + "existing file was not overwritten.");
 		    } else {
 		      String parent = toFile.getParent();
 		      if (parent == null)
 		        parent = System.getProperty("user.dir");
 		      File dir = new File(parent);
 		      if (!dir.exists())
 		        throw new IOException("FileCopy: "
 		            + "destination directory doesn't exist: " + parent);
 		      if (dir.isFile())
 		        throw new IOException("FileCopy: "
 		            + "destination is not a directory: " + parent);
 		      if (!dir.canWrite())
 		        throw new IOException("FileCopy: "
 		            + "destination directory is unwriteable: " + parent);
 		    }
 
 		    FileInputStream from = null;
 		    FileOutputStream to = null;
 		    try {
 		      from = new FileInputStream(fromFile);
 		      to = new FileOutputStream(toFile);
 		      byte[] buffer = new byte[4096];
 		      int bytesRead;
 
 		      while ((bytesRead = from.read(buffer)) != -1)
 		        to.write(buffer, 0, bytesRead); // write
 		    } finally {
 		      if (from != null)
 		        try {
 		          from.close();
 		        } catch (IOException e) {
 		          ;
 		        }
 		      if (to != null)
 		        try {
 		          to.close();
 		        } catch (IOException e) {
 		          ;
 		        }
 		    }
 		  }
 
 	public String getPathToFiles() {
 		return pathToFiles;
 	}
 
 	public void setPathToFiles(String pathToFiles) {
 		this.pathToFiles = pathToFiles;
 	}
 
 	public String getPathToDestinationFiles() {
 		return pathToDestinationFiles;
 	}
 
 	public void setPathToDestinationFiles(String pathToDestinationFiles) {
 		this.pathToDestinationFiles = pathToDestinationFiles;
 	}
 
 	@Override
 	public void run() {
 		work();
 	}
 	
 }
