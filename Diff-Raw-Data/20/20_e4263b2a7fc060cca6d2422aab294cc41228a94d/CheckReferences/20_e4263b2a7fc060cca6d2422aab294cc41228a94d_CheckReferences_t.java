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
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StreamTokenizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 public class CheckReferences extends StreamTokenizer{
 
 	private static final String SKIPPEDLINES = "FindXrefs/src/ifs/resources/skippedlines.txt";
 	private static final String DBFILE = "db.csv";
 	private static final String PROGFILE = "progs.csv";
 	private static final String OUTPUTFILE = "output.txt";
 	
 	private ArrayList<String> databases;
 	private ArrayList<String> programs;
 	private ArrayList<Integer> unmatchedLines = new ArrayList<Integer>();
 	
 	public CheckReferences(InputStream in){
 		super(in);
 		databases = readDbs();
 		programs = readProgs();
 	}
 	
 	public void run() throws IOException{
 		eolIsSignificant(true);
 		int lineNr = -1;
 		String currentProg = "";
 		HashMap<String,ArrayList<Table>> references = new HashMap<String,ArrayList<Table>>();
 		ArrayList<Table> dbs = new ArrayList<Table>();
 		StringBuilder builder = new StringBuilder();
 		while(nextToken() != StreamTokenizer.TT_EOF){
 			if (ttype == StreamTokenizer.TT_EOL) {
 				builder = new StringBuilder();
 			}
 			if (sval != null)
 				builder.append(sval);
 			if(ttype == StreamTokenizer.TT_WORD){
 				if (lineno() != lineNr){
 					if(programs.contains(currentProg)){
 						references.put(currentProg, dbs);
 					}
 					if(!references.containsKey(sval)){
 						dbs = new ArrayList<Table>();
 						dbs.clear();
 						currentProg = sval;
 					} else {
 						dbs = references.get(sval);
 					}
 					lineNr = lineno();
 				} else {
 					int i = 0;
 					while(i < databases.size() && !match(i, builder, lineno())){
 						i++;
 					}
 					if(i != databases.size()){
 						Table table = new BasicTable(databases.get(i),crudOperation);
 						if(!dbs.contains(table)){
 							dbs.add(table);
 						} else {
 							Iterator<Table> iterator = dbs.iterator();
 							while (iterator.hasNext()) {
 								Table next = iterator.next();
 								if (next.equals(table)) {
 									next.pushCrudOperation(crudOperation);
 								}
 							}
 						}
 					}
 				}
 			}
 			}
 		printOutput(references);
 //		printSkippedLines();
 	}
 
 	private String crudOperation = "";
 	
 	private boolean match(int i, StringBuilder builder, int lineNr) {
 		String line = builder.toString();
 		
 		boolean match = false;
 		
 		if (sval.matches("P" + databases.get(i) + "[a-zA-Z0-9]+")) {
			if( ( line.matches(".*DT.*")) || (line.matches(".*DETERMINE.*")) || (line.matches(".*LU.*"))) {
 				match = true;
 				crudOperation = Table.DETERMINE;
 			}
 		} else if (sval.matches(databases.get(i) + "\\.+.+")) {
 			if( (line.matches(".*FL.*")) || (line.matches (".*FLAG.*") )) {
 				match = true;
 				crudOperation = Table.FLAG;
 			}
 		} else if (sval.matches(databases.get(i))) {
 			if(line.matches(".*AUTO\\.ENTRY.*") || line.matches(".*AE.*")) {
 				match = true;
 				crudOperation = Table.AUTO_ENTRY;
 			} else if( line.matches(".*PURGE.*") || line.matches(".*PU.*")) { 
 				match = true;
 				crudOperation = Table.PURGE;
			} else if( ( line.matches(".*DT.*")) || (line.matches(".*DETERMINE.*")) || (line.matches(".*LU.*"))) {
				match = true;
				crudOperation = Table.DETERMINE;
 			} else
 				unmatchedLines.add(lineNr);
 		}
 		
 		return match;
 	}
 	
 	private ArrayList<String> readDbs() {
 		ArrayList<String> outputList = new ArrayList<String>();
 		try {
 			// Open the file
 			FileInputStream fstream = new FileInputStream(LocateResource.getResource(DBFILE));
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
 			FileInputStream fstream = new FileInputStream(LocateResource.getResource(PROGFILE));
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
 			File newFile = new File(SKIPPEDLINES);
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
