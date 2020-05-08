 /*==============================================================================================
 *  Name: CSV Performance Test report consolidating automation utility
  *  Author: Barbuta Mihail-Gabriel - barbuta.mihail@ro.ibm.com
  *  Company: IBM Romania - Global Delivery Center Bucharest
 *  Date: 2011-05-15 / Revision: 1.3
  *  
  *  Developed during Yahoo! Open Hack, Bucharest, 14-15 May 2011
  *	
  *	Description:
  *	A class that can be used in Performance Test automation for consolidating CSV performance
  *		reports. It targets a long-time unsolved problem with test automation, the time wasted
  *		on putting together 48 or more test reports daily. This class will fetch all individual
  *		reports from a directory, parse the performance data, and output it into a single report
  *		that can be imported in a spreadsheet or publishing software.
  *==============================================================================================
  */
 
 
 import java.io.*;
 import au.com.bytecode.opencsv.CSVReader;	// Open-source, commercial-friendly licensed
 import au.com.bytecode.opencsv.CSVWriter;	// 	libraries for handling CSV files
 
 public class CSV_ReportingConsolidator {
 	
 	public static void main(String[] args) throws IOException {
 
 		// Construct an array containing the list of files in the input folder
 		String path = "input/"; 					// Set the directory containing the CSV files
 		File folder = new File(path);				// Load the selected path
 		File[] listOfFiles = folder.listFiles(); 	// Retrieve the list of files from the directory
 		
 		// Serialize the reference headers to write the output CSV header
 		CSVReader referenceReader = new CSVReader(new FileReader("reference/example_fields.csv"));
 		String [] referenceHeaders = referenceReader.readNext();
 		CSVWriter writer = new CSVWriter(new FileWriter("output/Consolidated_CSV_Report.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
 		writer.writeNext(referenceHeaders);
 		
 		for (int i = 0; i < listOfFiles.length; i++) 
 		{
 			if (listOfFiles[i].isFile()) {
 				String filename = listOfFiles[i].getName();		// Retrieve the file name
 				System.out.println("== Processed: " + filename + " ===========================");
 				
 				if (!filename.endsWith("csv"))	{				// Check if the file has a CSV extension
 					System.out.println("The path contains non-csv files. Please remove them and try again.");
 					System.exit(1);								// Exit if non-CSV files are found
 				}
 				
 				String filePath=String.valueOf(path+filename);	// Combine the path with the filename
 				File file = new File(filePath);
 				CSVReader csvFile = new CSVReader(new FileReader(filePath));
 				String [] nextLine;								// CSV line data container
 				String [] Page = {"","","","","","","","",""};	// Holder for Page names
 				double PagePRT[] = {0,0,0,0,0,0,0,0,0};			// Holder for Page Response Times
 				String PTrun = "";		// Name of Performance Test Run
 				String startTime = "";	// Test start time
 				int rowIterator = 0;	// Used to loop between rows
 				int colIterator = 0;	// Used to loop between columns
 				int rowCount = 0;		// Used to count the total number of rows
 				double PRT = 0;			// Average Page Response Time
 		        double PRd = 0;			// Page Response Time Standard Deviation
 		        double ERT = 0;			// Average Element Response Time
 		        double ERd = 0;			// Element Response Time Standard Deviation
 		        int MRT = 0;			// Maximum Page Response Time
 		        int mRT = 0;			// Minimum Page Response Time
 				int elapsedTime = 0;	// Test Elapsed Time
 				int completedUsers = 0;	// Number of Completed Users
 				int TPA = 0;			// Total Page Attempts
 				int TPH = 0;			// Total Page Hits
 				int TEA = 0;			// Total Element Attempts
 				int TEH = 0;			// Total Element Hits
 				
 				// Fetch the total row count:
 				FileReader fr = new FileReader(file);
 		        LineNumberReader ln = new LineNumberReader(fr);
 		        while (ln.readLine() != null)	{	rowCount++;		}
 		        ln.close();				// Close the file reader
 		        
 		        // Fetch test identification data:
 		        nextLine = csvFile.readNext();
 		        PTrun = nextLine[1];	// Name of Performance Test Run
 		        nextLine = csvFile.readNext();
 		        startTime =nextLine[1];	// Performance Test Start Time
 		        
 		        // Skip 9 uninteresting rows:
 		        while (rowIterator < 9) {
 					nextLine = csvFile.readNext();
 					rowIterator++;
 				}
 		        
 		        // Read the page titles:
 		        while (colIterator < 9)	{
 		        	Page[colIterator] = nextLine[colIterator+18];
 		        	colIterator++;
 		        }
 		        
 		        // Skip all rows except the last 5 that contain the summary data:
 		        while (rowIterator < rowCount-5) {
 					nextLine = csvFile.readNext();
 					rowIterator++;
 				}
 				
 		        // Attempt to retrieve the summary data in the last rows, 3 rows allowance:
 		        if (nextLine[10].length() != 0)	{
 		        	PRT = Double.parseDouble(nextLine[10]);
 					PRd = Double.parseDouble(nextLine[11]);
 					ERT = Double.parseDouble(nextLine[16]);
 					ERd = Double.parseDouble(nextLine[17]);
 					MRT = Integer.parseInt(nextLine[12]);
 					mRT = Integer.parseInt(nextLine[13]);
 		        }
 				
 				nextLine = csvFile.readNext();
 				
 		        if (nextLine[10].length() != 0)	{
 		        	PRT = Double.parseDouble(nextLine[10]);
 					PRd = Double.parseDouble(nextLine[11]);
 					ERT = Double.parseDouble(nextLine[16]);
 					ERd = Double.parseDouble(nextLine[17]);
 					MRT = Integer.parseInt(nextLine[12]);
 					mRT = Integer.parseInt(nextLine[13]);
 		        }
 				
 				nextLine = csvFile.readNext();
 				
 		        if (nextLine[10].length() != 0)	{
 		        	PRT = Double.parseDouble(nextLine[10]);
 					PRd = Double.parseDouble(nextLine[11]);
 					ERT = Double.parseDouble(nextLine[16]);
 					ERd = Double.parseDouble(nextLine[17]);
 					MRT = Integer.parseInt(nextLine[12]);
 					mRT = Integer.parseInt(nextLine[13]);
 		        }
 				
 				nextLine = csvFile.readNext();
 
 				// Retrieve final summary data, which is always on the last row: 
				elapsedTime = Integer.parseInt(nextLine[0])/1000;
								
				int hours = elapsedTime / 3600,
				remainder = elapsedTime % 3600,
				minutes = remainder / 60,
				seconds = remainder % 60;

				String eTime = (hours < 10 ? "0" : "") + hours
				 + ":" + (minutes < 10 ? "0" : "") + minutes
				 + ":" + (seconds< 10 ? "0" : "") + seconds;
				
 				completedUsers = Integer.parseInt(nextLine[5]);
 				TPA = (int)Double.parseDouble(nextLine[8]);
 				TPH = (int)Double.parseDouble(nextLine[9]);
 				TEA = (int)Double.parseDouble(nextLine[14]);
 				TEH = (int)Double.parseDouble(nextLine[15]);
 				
 				csvFile.close();	// File recycled to reset the line parser
 				CSVReader csvFile2 = new CSVReader(new FileReader(filePath));
 				
 				// Reset iterators to allow re-usage:
 		        rowIterator = 0;
 				colIterator = 0;
 				
 				// Skip first 13 rows:
 				while (rowIterator < 13) {
 					nextLine = csvFile2.readNext();
 					rowIterator++;
 				}
 				
 				// Dynamically retrieve individual page response times, correlate with page names:
 				while (rowIterator < rowCount)	{
 					while (colIterator < 9)	{
 						if (nextLine[colIterator+18].length() != 0)	{
 							PagePRT[colIterator] = Double.parseDouble(nextLine[colIterator+18]);
 						}
 						colIterator++;
 					}
 		 			nextLine = csvFile2.readNext();
 					rowIterator++;
 					colIterator = 0;
 				}
 				
 				csvFile2.close();	// Final file closing
 				
 				// Reset iterators to allow re-usage:
 		        rowIterator = 0;
 				colIterator = 0;
 				
 				// Display statistics in console, enable only for debugging purposes:
 				/*
 				System.out.println(" Elapsed Time: " + elapsedTime
 									+ "\n Completed Users: " + completedUsers
 									+ "\n Total Page Attempts: " + TPA
 									+ "\n Total Page Hits: " + TPH
 									+ "\n Average Response Time For All Pages For Run: " + PRT
 									+ "\n Response Time Standard Deviation For All Pages For Run: " + PRd
 									+ "\n Maximum Response Time For All Pages For Run: " + MRT
 									+ "\n Minimum Response Time For All Pages For Run: " + mRT
 									+ "\n Total Page Element Attempts: " + TEA
 									+ "\n Total Page Element Hits: " + TEH
 									+ "\n Average Response Time For All Page Elements For Run: " + ERT
 									+ "\n Response Time Standard Deviation For All Page Elements For Run: " + ERd
 									+ "\n");
 				
 				// Display individual page response times in console:
 				while (colIterator < 9)	{
 					System.out.println("Page " + Page[colIterator] + " - Response Time: " + PagePRT[colIterator]);
 					colIterator++;
 				}
 				*/
 				
 				// Serialize all retrieved performance parameters:
 				String[] entries = {PTrun,startTime,
 						String.valueOf(completedUsers),
						eTime,
 						String.valueOf(TPA),
 						String.valueOf(TPH),
 						String.valueOf(PRT),
 						String.valueOf(PRd),
 						String.valueOf(MRT),
 						String.valueOf(mRT),
 						String.valueOf(TEA),
 						String.valueOf(TEH),
 						String.valueOf(ERT),
 						String.valueOf(ERd),"",
 						String.valueOf(PagePRT[0]),
 						String.valueOf(PagePRT[1]),
 						String.valueOf(PagePRT[2]),
 						String.valueOf(PagePRT[3]),
 						String.valueOf(PagePRT[4]),"",
 						String.valueOf(PagePRT[5]),
 						String.valueOf(PagePRT[6]),
 						String.valueOf(PagePRT[7]),
 						String.valueOf(PagePRT[8]),
 						};
 				
 				writer.writeNext(entries);	// Write performance parameters in CSV format
 			}
 		}
 		writer.close();						// Close the CSV writer
		
 	}
 }
