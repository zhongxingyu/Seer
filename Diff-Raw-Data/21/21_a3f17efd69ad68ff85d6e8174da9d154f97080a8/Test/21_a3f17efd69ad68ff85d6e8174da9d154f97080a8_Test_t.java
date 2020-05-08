 package Tester;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import util.CSVFile;
 import util.HeapFile;
 import util.Query;
 
 
 
 public class Test {
 
 	public static void main (String args[]){
 
 		/* If there is just 1 argument, we print all records
 		 * from the heap file in a CSV
 		 */
 		if (args.length == 1){
 			HeapFile heapFile = new HeapFile(args[0], true, null, null);
 
 			CSVFile csvTarget = new CSVFile("example_result.acsv");
 			// Write the information(Schema and Records to the 
 			// CSVFile in the byte format.
 			csvTarget.writeRecordToCSVFileUsingBufferedWriter(heapFile);
 
 		}
 
 		/*
 		 * If there is more than one argument, then
 		 * we either append files OR query the heap
 		 * file.
 		 */
 		if (args.length > 1){
 
 			
 			//checking if -i token is the second argument
 			if (args[1].equals("-i")){
 				// if so, we want to insert record
 				// we also may want to build an index
 				// but first, we add the new records
 				HeapFile hfTarget;
 				
 				/**
 				 * we need to check if the given heapfile 
 				 * already exists. and if it does, we need to check the the schemas of the
 				 * input CSV and the heapFile match. if the heapfile doesnt exist, then we make
 				 * the schema of the new heapfile the schema of the csv then add records from
 				 * the csv to the heap
 				 */
 				// first get the name of the csvSource
 				CSVFile csvSource = new CSVFile(args[args.length-1]);
 				BufferedReader br;
 				try {
 					br = new BufferedReader(new FileReader(args[args.length-1]));
 					// get schema of SCV
 					csvSource.schema = csvSource.getSchemaFromFile(br);
 					csvSource.getSchemaArrayFromSchema();
 					br.close();
 
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				//Check if heap file already exists.
 				File f = new File(args[0]);
 				if (!f.exists()){
 					/*
 					 * If file doesn't exist, we create a new file with
 					 * the schema from the CSV File.
 					 */
 					 hfTarget = new HeapFile(args[0], false, csvSource.schema, csvSource.schemaArray);
 					hfTarget.writeHeaderInformationToFile();
 					hfTarget.writeCsvContentsToHeapFile(csvSource);
 				}
 				else{
 					/*
 					 * If file exists, we compare the schema of HeapFile 
 					 * with the Schema of the CSVFile input.
 					 */
 					 hfTarget = new HeapFile(args[0], true, null, null);
 					if (!(hfTarget.schema.equalsIgnoreCase(csvSource.schema)))
 						System.out.println("Error: Schema of the CSV and Heap files do not match");
 					else{
 						/*
 						 * If schema matches, we make append the records to the 
 						 * heapfile and increment the number of records in the
 						 * Heapfile header.
 						 */
 						hfTarget.writeCsvContentsToHeapFile(csvSource);
 					}
 				}
 
 				//  the records have now been added to the heapFile
 
 				// now, we build index(es) if there is any
 				// so, we scan the command line to see and if there are any, we add to arrayList buildIndexes
 				ArrayList<Integer> buildIndexes = new ArrayList<Integer>();
 				int x = 1;
 				while(x < args.length - 2){
 					if(args[x].contains("-b")){
 						int indexToBuild = Integer.parseInt(args[x].substring(args.length-1));
 						buildIndexes.add(indexToBuild);
 					}
 					x++;
 				}
 				
 				// now, we call the heapFile buildIndex function to build the indexes, if any
 				x = 0;
 				while(x < buildIndexes.size()){
					hfTarget.buildIndexOnColumn(buildIndexes.get(x));
 					x++;
 				}
 				// and we are done!
 				return;
 
 			}
 			else if (!args[1].equals("-i")){
 				/*
 				 *  we want to either 
 				 *  QUERY THE HEAPFILE or 
 				 *  BUILD AN INDEX ( or MULTIPLE INDEXES)
 				 */
 			
 				// first prepare the heapfile so we can build indexes and/or query
 				HeapFile hfTarget = new HeapFile(args[0], true, null, null);
 
 				// first we check if there are any indexes to build
 				// and if there are, we want to build them before we query
 				ArrayList<Integer> buildIndexes = new ArrayList<Integer>();
 				int x = 1;
 				while(x < args.length){
 					
 					if(args[x].contains("-b")){
 						System.out.println("index to build found in command line");
 						int indexToBuild = Integer.parseInt(args[x].substring(args[x].length() -1));
 						System.out.println("that index is " + indexToBuild);
 						buildIndexes.add(indexToBuild);
 					}
 					x++;
 				}
 				
 				// now, we call the heapFile buildIndex function to build the indexes, if any
 				x = 0;
 				while(x < buildIndexes.size()){
					hfTarget.buildIndexOnColumn(buildIndexes.get(x));
 					x++;
 				}
 				// the indexes have been built
 				// no we can query if we need to
 			
 				
 			
 				//prepare the the heap file
 				HeapFile heapFile = new HeapFile(args[0], true, null, null);
 				// construct new query
 				Query query = new Query(heapFile, args);
 				// process query. query first checks to see if the command line has a condition or projection
 				// and if it does, the query is processed. if not, the program is finished.
 				query.processQuery();
 				return;
 				
 
 			} // end of query brackets
 
 
 
 		} // end of args.length > 1
 
 	}
 } // end of main
 
 
