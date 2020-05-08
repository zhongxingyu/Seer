 package ifs.jl;
 
 import ifs.datamodel.BasicTable;
 import ifs.datamodel.Table;
 import ifs.program.Constants;
 import ifs.resources.LocateResource;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 public class CheckRefs {
	private static final String SKIPPEDLINES = "skippedlines.txt";
 	private static final String DBFILE = "db.csv";
 	private static final String PROGFILE = "progs.csv";
 	private static final String OUTPUTFILE = "output.txt";
 
 	private ArrayList<String> databases;
 	private ArrayList<String> programs;
 	private HashMap<String,ArrayList<Table>> references;
 	private ArrayList<Integer> unmatchedLines;
 
 	public CheckRefs() {
 		databases = readDbs();
 		programs = readProgs();
 		references = fillReferences(programs);
 		unmatchedLines = new ArrayList<Integer>();
 	}
 
 	public void run() {
 		try {
 			FileInputStream fstream = new FileInputStream(Constants.SOURCE_CODE);
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine = br.readLine();
 			int lineNr = -1;
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				lineNr++;
 				if(lineNr == 883426){
 					System.out.print("STOP");
 				}
 				String[] text = strLine.replaceAll("  +", " ").split(" ");
 				if(strLine.matches(".*DT;.*") || strLine.matches(".*DETERMINE.*") || strLine.matches(".*LU;.*")){
 					saveTable(text, Table.DETERMINE);
 				} else if(strLine.matches(".*FL;.*") || strLine.matches(".*FLAG.*")){
 					saveTable(text, Table.FLAG);
 				} else if(strLine.matches(".*AUTO\\.ENTRY.*") || strLine.matches(".*AE;.*")){
 					saveTable(text, Table.AUTO_ENTRY);
 				} else if(strLine.matches(".*PURGE.*") || strLine.matches(".*PU;.*")){
 					saveTable(text, Table.PURGE);
 				} else {
 					unmatchedLines.add(lineNr);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		printOutput(references);
 		printSkippedLines();
 	}
 	
 	private void saveTable(String[] text, String crud) {
 		ArrayList<String> tables = findDBTables(text, crud);
 		if(crud.equals(Table.FLAG) && tables.size()>1){
 			pushInReferences(text[0].substring(2), tables.get(0), Table.DETERMINE);
 			pushInReferences(text[0].substring(2), tables.get(1), crud);
 		} else if(tables.size() > 1) {
 			if( crud.equals(Table.DETERMINE)){
 				for(int i=0; i < tables.size(); i++){
 					pushInReferences(text[0].substring(2), tables.get(i), crud);
 				}
 			} else {
 				System.err.println("MULTIPLE TABLES ON ONE LINE");
 			}
 		} else if(!tables.isEmpty()){
 			pushInReferences(text[0].substring(2), tables.get(0), crud);
 		}				
 	}
 	
 	private void pushInReferences(String program, String table, String crud){
 		ArrayList<Table> dbs = new ArrayList<Table>();
 		dbs = references.get(program);
 		if(dbs != null){
 			Table table1 = new BasicTable(table,crud);
 			if(!dbs.contains(table1)){
 				dbs.add(table1);
 			} else {
 				Iterator<Table> iterator = dbs.iterator();
 				while (iterator.hasNext()) {
 					Table next = iterator.next();
 					if (next.equals(table1)) {
 						next.pushCrudOperation(crud);
 					}
 				}
 			}
 		}
 	}
 
 	private ArrayList<String> findDBTables(String[] text, String crud){
 		ArrayList<String> dbs = new ArrayList<String>();
 		int i = 1;
 		int j = -1;
 		boolean found = false;
 		while(!found && i<text.length){
 			if(text[i].equals(":")){
 				break;
 			}
 			if(databases.contains(text[i].trim())){
 				if(!dbs.contains(text[i].trim())){
 					dbs.add(text[i].trim());
 				}
 				found = true;
 				j = -1;
 			} else {
 				j = 0;
 				while(!found && j < databases.size()){
 					if(text[i].trim().matches(databases.get(j) + "\\.+.+") && crud.equals(Table.FLAG)){
 						//For FLAG
 						found = true;
 					} else if(text[i].trim().matches("P" + databases.get(j) + "[a-zA-Z0-9]+.*") && crud.equals(Table.DETERMINE)){
 						//For DT
 						found = true;
 					} else if(text[i].trim().matches(".*(" + databases.get(j) + ").*") && crud.equals(Table.DETERMINE)){
 						//For LU
 						found = true;
 					} else {
 						j++;
 					}
 				}
 				i++;
 			}
 			if(found && j!=-1){
 				if(!dbs.contains(databases.get(j))){
 					dbs.add(databases.get(j));
 				}
 				found = false;
 			}
 		}
 		return dbs;
 	}
 
 	private ArrayList<String> readDbs() {
 		ArrayList<String> outputList = new ArrayList<String>();
 		try {
 			// Open the file
 			FileInputStream fstream = new FileInputStream(
 					LocateResource.getResource(DBFILE));
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine = br.readLine();
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				String[] output = strLine.split(";");
 				outputList.add(output[1]);
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 		return outputList;
 	}
 
 	private ArrayList<String> readProgs() {
 		ArrayList<String> outputList = new ArrayList<String>();
 		try {
 			// Open the file
 			FileInputStream fstream = new FileInputStream(
 					LocateResource.getResource(PROGFILE));
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine = br.readLine();
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				String[] output = strLine.split(";");
 				outputList.add(output[1]);
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 		return outputList;
 	}
 	
 	private HashMap<String,ArrayList<Table>> fillReferences(ArrayList<String> progs){
 		HashMap<String, ArrayList<Table>> refs = new HashMap<String, ArrayList<Table>>();
 		for(int i = 0; i < progs.size(); i++){
 			ArrayList<Table> tabs = new ArrayList<Table>();
 			refs.put(progs.get(i), tabs);
 		}
 		return refs;
 	}
 	
 	public void printOutput(HashMap<String,ArrayList<Table>> references) {
 		//print output
 		try {
 			// Create file
 			FileWriter fstream = new FileWriter(LocateResource.getResource(OUTPUTFILE));
 			BufferedWriter out = new BufferedWriter(fstream);
 			Iterator<Entry<String, ArrayList<Table>>> it = references.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, ArrayList<Table>> e = it.next();
 				out.write(e.getKey());
 				out.newLine();
 				for(Table s : e.getValue()){
 					out.write(s.getTableName() + "," + s.getCrudOperation());
 					out.newLine();
 				}
 				out.write("----------------------------");
 				out.newLine();
 			}
 			// Close the output stream
 			out.close();
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Function prints all lines that where not included in the output. These lines are used to validate if no usefull info
 	 * was emitted from the output.
 	 */
 	private void printSkippedLines() {
 		//print skipped lines
 		try {
 			// Open the file
 			FileInputStream fstream = new FileInputStream(Constants.SOURCE_CODE);
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine = br.readLine();
 			int linenr = 0;
 			int lnr = 0;
 			Iterator<Integer> iterator = unmatchedLines.iterator();
 			//
			File newFile = new File(LocateResource.getResource(SKIPPEDLINES));
 			newFile.createNewFile();
 			FileWriter filestream = new FileWriter(newFile);
 			BufferedWriter out = new BufferedWriter(filestream);
 			// Read File Line By Line
 			boolean proceedIterator = true;
 			while ((strLine = br.readLine()) != null) {
 				
 				
 				if (iterator.hasNext() && proceedIterator) {
 					Integer integer = iterator.next();
 					lnr = integer.intValue();
 					while (lnr == linenr-1) {
 						integer = iterator.next();
 						lnr = integer.intValue();
 					}
 					proceedIterator = false;
 				}
 				
 				if (lnr == linenr) {
 						out.write(strLine);
 						out.newLine();
 						proceedIterator = true;
 				}
 				linenr++;
 			}
 			// Close the input stream
 			in.close();
 			out.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 }
