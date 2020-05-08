 package edu.umich.eecs.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.Scanner;
 
 import edu.umich.eecs.dto.Cell;
 import edu.umich.eecs.dto.CellSpan;
 import edu.umich.eecs.service.MDCCellSpanService;
 import edu.umich.eecs.util.Tic;
 
 /**
  * This class imports data from the files generated from the
  * Mobility Data Challenge dataset.
  * @author Pedro
  *
  */
 public class MDCDataImporter {
 	
 	private MDCCellSpanService cellSpanSvc;
 	private Scanner scanner;
 	private int personID;
 	private int countCorruptedEntries = 0; // the number of 01-Jan-2004 entries skipped
 	
 	public int getCountCorruptedEntries() {
 		return countCorruptedEntries;
 	}
 
 	/**
 	 * Creates a data importer for a specific CSV file.
 	 * @param path The path to the file. The format of the filename should be x_yyyy.csv, where
 	 * x must be the person ID and yyyy can be anything.
 	 * @param cellSpanSvc The database connection service.
 	 * @throws FileNotFoundException
 	 */
 	public MDCDataImporter(String path, int personID, MDCCellSpanService cellSpanSvc) throws FileNotFoundException {
 		File file = new File(path);
 		this.scanner = new Scanner(file);
 		this.cellSpanSvc = cellSpanSvc;
 		this.personID = personID; 
 	}
 	
 	/**
 	 * Goes through the whole CSV file provided in the constructor and inserts each
 	 * cellspan in the database. 
 	 * @throws ParseException 
 	 */
 	public void insertIntoDatabase() throws ParseException {
 
 		//
 		// The CSV file has one cell tower sampling per line, in this format (the first line is always there):
 		//		userid	time	tz	country_code	network_code	cell_id	area_code	signal	signaldbm
 		//		126	1254947825	-7200	228	1	1276315	19	2	109
 		//		126	1254947885	-7200	228	1	3305	2727	1	101
 		//
 		//	Note that, unlike the RM dataset, samples are generated regardless of whether there was an
 		//  actual transition or not.However, in the database we store the span of time during which the user
 		//  was connected to a tower.
 		//
 		// Things to keep in mind:
 		//  - One of the implications is that we ignore the last connection of the CSV file; we only
 		//	  care about the time at which the previous connection ended.
 		//  - There are a bunch of consecutive entries with the same timestamp. Some of them have different
 		//	  cell tower data. We record that as a cellspan with duration 0.
 		//
 		
 		int transitionId = 0;
 		Timestamp lastCellStartTime = null;
 		Cell lastCellSeen = null;
 		// first line is the header
 		scanner.nextLine();
 		while(scanner.hasNext()) {
 			//
 			// First we read the values, then see if we have changed cells. If so, we record a new CellSpan.
 			//
 			int personID = scanner.nextInt();
 			assert personID == this.personID;
 			
 			Timestamp time = new Timestamp(Long.parseLong(scanner.next()) * 1000);
 			scanner.next(); // timezone is not used
 			int countryID = scanner.nextInt();
 			int networkID = scanner.nextInt();
 			int cellID = scanner.nextInt();
 			int areaID = scanner.nextInt();
 			scanner.next(); scanner.next(); // signal and signaldbm
 			
			Cell currentCell = new Cell(countryID, areaID, cellID, networkID);
 			
 			//
 			// This is true for all but the first line.
 			//
 			if(lastCellSeen != null) {
 				assert lastCellStartTime.before(time) || lastCellStartTime.equals(time); // assert we are moving forward in time
 				
 				if(!lastCellSeen.equals(currentCell)) {
 					
 					//
 					// It's a transition.
 					//
 
 					CellSpan cellSpan = new CellSpan(
 							personID,
 							lastCellSeen,
 							transitionId,
 							lastCellStartTime,
 							time);
 					cellSpanSvc.saveCP(cellSpan);
 
 					transitionId++;
 					
 					lastCellSeen = currentCell;
 					lastCellStartTime = time;
 				} else {
 					
 					//
 					// No need to do anything.
 					//
 					
 				}
 			} else {
 				assert lastCellStartTime == null;
 				
 				lastCellSeen = currentCell;
 				lastCellStartTime = time;
 			}
 		}
 		System.out.println(transitionId + " transitions.");
 	}
 
 	public static void main(String[] args) throws ParseException, FileNotFoundException {
 		Tic clock = new Tic(); clock.setVerbose(true);
 		//
 		// Replace this with the path to where the personid/gsm.csv files are.
 		//
 		
 		String pathToCsvFiles = "C:\\Users\\Pedro\\Desktop\\589 project data\\mdc2012-373-taskopen\\mdc2012-373-taskopen\\";
 		MDCCellSpanService cellSpanService = new MDCCellSpanService();
 
 		try {
 			//
 			// Every person has a directory with her own ID. Inside that directory is a file called
 			// "gsm.csv" with the data we want.
 			//
 			File dir = new File(pathToCsvFiles);
 			int count = 0;
 			String[] contents = dir.list();
 			for (String personDir : contents) {
 				
 				MDCDataImporter dataImporter = new MDCDataImporter(
 						pathToCsvFiles + personDir + "\\gsm.csv", Integer.parseInt(personDir),
 						cellSpanService);
 				dataImporter.insertIntoDatabase();
 				System.out.println(++count + "/" + contents.length + " CSV files inserted in the DB");
 			}
 		} finally {
 			cellSpanService.tearDown();
 		}
 		clock.toc();
 	}
 }
