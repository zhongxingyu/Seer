 /**
  * 
  */
 package ifs.program;
 
 import ifs.datamodel.Program;
 import ifs.datamodel.Table;
 import ifs.engine.ProgramTablesEngine;
 import ifs.engine.TablesEngine;
 import ifs.jl.CheckRefs;
 import ifs.resources.LocateResource;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 /**
  * @author TDEWEERD
  * db.csv
  */
 
 public class Mapper {
 	private static final String FS_DATABASE_CSV = "ifs_database.csv";
 	private static final String OUTPUTFILE = "table_owner_program.csv";
 	private static final String INPUTFILE = "output.txt";
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		System.out.printf ("Time at start = %s", new Date() );
 		//print to output.txt -prog / db-
 		try {
 			//CheckReferences cr = new CheckReferences(new FileInputStream(Constants.SOURCE_CODE));
 			CheckRefs cr = new CheckRefs();
 			cr.run();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			TablesEngine.getTablesCollection(new File(LocateResource.getResource(FS_DATABASE_CSV)));
 		
 			ProgramTablesEngine.generateTableToProgramsMapping(new File(LocateResource.getResource(INPUTFILE)));
 			//printOutput(h);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.printf ("Time at end = %s", new Date() );
 	}
 	
 	public static void printOutput(HashMap<Table, ArrayList<Program>> h) {
 
 		try {
 			// Create file
			FileWriter fstream = new FileWriter(LocateResource.getResource(OUTPUTFILE));
 			BufferedWriter out = new BufferedWriter(fstream);
 			Iterator<Entry<Table, ArrayList<Program>>> it = h.entrySet().iterator();
 			out.write("Table;C;R;U;D;Owner;Program");
 			out.newLine();
 			while (it.hasNext()) {
 				Entry<Table, ArrayList<Program>> e = it.next();
 				if (!TablesEngine.tableAndOwner.containsKey(e.getKey().getTableName()))
 					continue;
 				
 				for(Program s : e.getValue()){
 					//table
 					out.write(e.getKey().getTableName());
 					out.write(';');
 					//tablecrudoperation
 					out.write(s.getCrudOperation('C'));
 					out.write(';');
 					out.write(s.getCrudOperation('R'));
 					out.write(';');
 					out.write(s.getCrudOperation('U'));
 					out.write(';');
 					out.write(s.getCrudOperation('D'));
 					out.write(';');
 					//owner
 					out.write(TablesEngine.tableAndOwner.get(e.getKey().getTableName()));
 					out.write(';');					
 					//program
 					out.write(s.getProgramName());
 					out.write(';');
 					out.newLine();
 				}
 			}
 			// Close the output stream
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
