 /* this is the csvImport Class */
 
 package ClassAdminBackEnd;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.csvreader.CsvReader;
 
public class CsvImport extends FileImport{
 
 	CsvReader reader;
 
 	// constructor
 	public CsvImport() {
 	}
 
 	// checks if file exists
	public Boolean fileExists(String in) {
 		try {
 			reader = new CsvReader(in);
 			return true;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	// captures headers and records in the csv
 	public ArrayList recordData() {
 		ArrayList data = new ArrayList(); // returned arraylist
 		ArrayList headers = null; // header arraylist
 		ArrayList records = null; // record arraylist containing arraylists of
 									// records
 		try {
 
 			if (reader.readHeaders()) // if headers exist place in header
 										// arraylist
 			{
 				int headerCount = reader.getHeaderCount();
 
 				headers = new ArrayList(headerCount);
 
 				for (int i = 0; i < headerCount; i++) // place all headers in
 														// header arraylist
 				{
 					headers.add(reader.getHeader(i));
 				}
 				data.add(headers); // add header arraylist to retured arraylist
 
 				records = new ArrayList(); // create second arraylist for
 											// records
 
 				try {
 					while (reader.readRecord()) // while records are in csv file
 												// read records
 					{
 						ArrayList record = new ArrayList(headerCount); // create
 																		// record
 																		// with
 																		// record
 																		// fields
 						for (int i = 0; i < headerCount; i++) // place all
 																// record field
 																// data in
 																// record
 																// arraylist
 						{
 							try {
 								String s = reader.get(i);
 								record.add(s); // add record field to record
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 						records.add(record); // add record to records arraylist
 						record = null; // free memory
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				data.add(records); // add record arraylist to returned arraylist
 
 				headers = null;
 				records = null;
 
 				return data;
 			} else {
 				System.out.println("ELSE"); // no headers exist
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return data; // weet nie wat ek hier moet return nie :D
 
 	}
 
 	public ArrayList getHeaders(ArrayList csv) // return header arraylist from
 												// csv arraylist
 	{
 		ArrayList headers = (ArrayList) csv.get(0);
 		return headers;
 	}
 
 	public ArrayList getRecords(ArrayList csv) // return record arraylist from
 												// csv arraylist
 	{
 		ArrayList records = (ArrayList) csv.get(1);
 		return records;
 	}
 
 	public ArrayList getRecord(ArrayList csv, int index) // return specified
 															// record from csv
 															// arraylist
 	{
 		ArrayList records = (ArrayList) csv.get(1); // get records arraylist
 		ArrayList record = (ArrayList) records.get(index); // get specified
 															// record in record
 															// arraylist
 		return record;
 	}
 
 	public String getRecordFieldValue(ArrayList csv, int recordIndex,
 			int fieldIndex) // return specified field from record in csv file
 	{
 		ArrayList records = (ArrayList) csv.get(1); // get records arraylist
 		ArrayList record = (ArrayList) records.get(recordIndex); // get
 																	// specified
 																	// record in
 																	// records
 																	// arraylist
 		String field = (String) record.get(fieldIndex); // get specified field
 														// in record
 		return field;
 	}
 
 	public void printHeaders(ArrayList headers) // print all headers from
 												// headers arraylist
 	{
 		for (int j = 0; j < headers.size(); j++) {
 			System.out.print(headers.get(j).toString() + "\t");
 		}
 		System.out.println();
 	}
 
 	public void printRecords(ArrayList records) // print all records from
 												// records arraylist
 	{
 		for (int i = 0; i < records.size(); i++) {
 			ArrayList record = (ArrayList) records.get(i); // get record
 															// arraylist from
 															// records
 
 			for (int j = 0; j < record.size(); j++) // get field from record
 													// arraylist
 			{
 				System.out.print(record.get(j).toString() + "\t"); // print
 																	// field
 																	// data
 			}
 			System.out.println(); // new line for next record
 
 		}
 	}
 
 	public void printRecord(ArrayList record) // print all information from
 												// specified record
 	{
 		for (int j = 0; j < record.size(); j++) {
 			System.out.print(record.get(j).toString() + "\t"); // print field
 																// data
 		}
 		System.out.println(); // new line XD
 	}
 
 	public void print(ArrayList in) // print entire csv structure from csv
 									// arraylist
 	{
 
 		ArrayList headers = (ArrayList) in.get(0); // get header arraylist
 		ArrayList records = (ArrayList) in.get(1); // get records arraylist
 
 		for (int j = 0; j < headers.size(); j++) // print all headers on header
 													// arraylist
 		{
 			System.out.print(headers.get(j).toString() + "\t");
 		}
 		System.out.println();
 
 		for (int i = 0; i < records.size(); i++) // get each record arraylist in
 													// records arraylist
 		{
 			ArrayList record = (ArrayList) records.get(i); // specify record
 
 			for (int j = 0; j < record.size(); j++) // print all field data from
 													// record arraylist
 			{
 				System.out.print(record.get(j).toString() + "\t");
 			}
 			System.out.println(); // next record to follow
 
 		}// get records
 	}
 }
