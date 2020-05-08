 /**
  * 
  */
 package ifs.engine;
 
 import ifs.datamodel.BasicProgram;
 import ifs.datamodel.BasicTable;
 import ifs.datamodel.Program;
 import ifs.datamodel.Table;
 import ifs.program.Constants;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * @author TDEWEERD
  *
  */
 public class ProgramTablesEngine {
 
 	public static void generateTableToProgramsMapping(File file) throws IOException {
 		//HashMap<Table, ArrayList<Program>> tables = new HashMap<Table, ArrayList<Program>>();
 		
 		FileWriter fstream = new FileWriter(Constants.CSVOUTPUTFILE);
 		BufferedWriter out = new BufferedWriter(fstream);
		out.write("Table;C;R;U;D;Program");
 		out.newLine();
 		
 		BufferedReader bufRdr = null;
 		try {
 			bufRdr = new BufferedReader(new FileReader(file));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		String line = null;
 		Program program = null;
 		
 		boolean readingProgram = true;
 		
 		//read each line of text file
 		try {
 			while((line = bufRdr.readLine()) != null)
 			{
 				String[] split = line.split(",");
 				if (line.equals("----------------------------")) {
 					readingProgram = true;
 					continue;
 				}
 				if (readingProgram) {
 					program = new BasicProgram(line,"");
 					readingProgram = false;
 				} else {
 					Table table = new BasicTable(split[0], split[1]);
 					
 					if (!TablesEngine.tableAndOwner.containsKey(table.getTableName()))
 						continue;
 					//table
 					out.write(table.getTableName());
 					out.write(';');
 					//tablecrudoperation
 					out.write(table.getCrudOperation('C'));
 					out.write(';');
 					out.write(table.getCrudOperation('R'));
 					out.write(';');
 					out.write(table.getCrudOperation('U'));
 					out.write(';');
 					out.write(table.getCrudOperation('D'));
 					out.write(';');					
 					//program
 					out.write(program.getProgramName());
 					out.write(';');
//					//owner
//					out.write(TablesEngine.tableAndOwner.get(table.getTableName()));
//					out.write(';');
 					out.newLine();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//close the file
 		try {
 			if (bufRdr != null)
 				bufRdr.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		out.close();
 	}
 }
