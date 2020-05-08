 /*
  * This file is part of INPcom
  * Copyright (C) 2012  Tecnics Associats
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Author:
  *   David Erill <daviderill79@gmail.com>
  */
 
 package com.tecnicsassociats.gvsig.inpcom;
 
 import com.tecnicsassociats.gvsig.inpcom.util.Utils;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.JOptionPane;
 
 
 public class ModelPostgis extends Model {
 
     protected static Connection connectionPostgis;   	
     private ModelDbf modelDbf;
     private String insertSql;
 	private ArrayList<String> tokens;
 	private ArrayList<ArrayList<String>> tokensList;	
 	private File fileRpt;
 	private String projectName;
 	private int lineNumber;   // Number of lines read
 	private ArrayList<String> pollutants;
 	
 	
     public ModelPostgis(String action) {
     	this.sExport = action;
     }
 
     public ModelPostgis() {
     }    
 
     public void execute(String execType) {
         this.execute(execType, "0", "0", "0");
     }
 
     
     public void execute(String execType, String export, String exec, String import_) {
 
         this.execType = execType;
 
         // Get properties file
         if (!enabledPropertiesFile()) {
             return;
         }
 
         // Sets initial configuration files
         configIni();
         
         // Get log file
         if (execType.equals(Constants.EXEC_GVSIG)) {
             logger = Utils.getLogger(appPath);
         } else{
         	logger = getLogger(appPath);
            System.out.println("Log File: \n" + logger.getName());               	
         }
             
         // Get Postgis connection
         if (!enabledPostgis()) {
             return;
         }
 
         if (execType.equals(Constants.EXEC_GVSIG)) {
             // Open main form
             modelDbf = new ModelDbf(this.sExport);            
             Utils.openForm(modelDbf, this);
         } else if (execType.equals(Constants.EXEC_CONSOLE)) {
 
             // Connect to sqlite database
             String sqliteFile = iniProperties.getProperty("SWMM_DB_POSTGIS_ACTUAL");
            System.out.println(sqliteFile);
             if (!connectDB(sqliteFile)) {
                 return;
             }
 
             // Process all Postgis tables and output to INP file
             if (export.equals("1")) {
                 processALL(null);
             }
 
             if (exec.equals("1")) {
                 execSWMM(null, null);
             }
 
             if (import_.equals("1")) {
                 File file = new File(iniProperties.getProperty("FILE_RPT"));            	
                 importRpt(file, "test");
             }
 
         }
 
     }
 
     
     private boolean enabledPostgis() {
     	
         if (connectionPostgis == null) {
            //String connectionString = "jdbc:postgresql://servidor:5432/demo_sbd?user=tecnics&password=tecnics.01";
            String connectionString = iniProperties.getProperty("POSTGIS_CONNECTION_STRING");
             try {
                 connectionPostgis = DriverManager.getConnection(connectionString);
             } catch (SQLException e) {
                 try {
                     connectionPostgis = DriverManager.getConnection(connectionString);
                 } catch (SQLException e1) {
                     Utils.showError(e.getMessage(), "", "inp_descr");
                     return false;
                }
                //logger.info(connectionString);        		
             }
         }
         return true;
         
     }
 
 
     // Read content of the table saved it in an Array
     private ArrayList<Map<String, String>> getTableData(String tableName) {
 
         Map<String, String> mDades;
         ArrayList<Map<String, String>> mAux = new ArrayList<Map<String, String>>();
         String sql = "SELECT * FROM " + schema + "." + tableName;
         PreparedStatement ps;
         try {
             ps = connectionPostgis.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             ResultSetMetaData rsmd = rs.getMetaData();
             // Get columns name
             int fields = rsmd.getColumnCount();
             String[] columns = new String[fields];
             for (int i = 0; i < fields; i++) {
                 columns[i] = rsmd.getColumnName(i + 1);
             }
             String value;
             while (rs.next()) {
                 mDades = new HashMap<String, String>();
                 for (int i = 0; i < fields; i++) {
                     Object o = rs.getObject(i + 1);
                     if (o != null) {
                         value = o.toString();
                         mDades.put(columns[i], value);
                     }
                 }
                 mAux.add(mDades);
             }
             rs.close();
         } catch (SQLException e) {
             Utils.showError("inp_error_sql", sql, "inp_descr");
         }
         return mAux;
 
     }
 
 
     // Main procedure
     public void processALL(File fileInp) {
 
         if (execType.equals(Constants.EXEC_GVSIG)) {        
             logger.info("exportINP");
         }
 
         try {
         	
             // Get default INP output file
             if (fileInp == null) {
                 String sFile = iniProperties.getProperty(sExport + "INP");
                 sFile = folderConfig + File.separator + sFile;
                 fileInp = new File(sFile);
             }
                 
             // Get some properties
             polygons_target_id = Integer.parseInt(iniProperties.getProperty(sExport + "POLYGONS_TARGET_ID"));
             default_size = Integer.parseInt(iniProperties.getProperty(sExport + "SIZE_DEFAULT"));
 
             // Open template and output file
             rat = new RandomAccessFile(this.fileTemplate, "r");
             raf = new RandomAccessFile(fileInp, "rw");
             raf.setLength(0);
 
             // Get content of target table
             String sql = "SELECT target.id as target_id, target.name as target_name, lines, dbf.name as dbf_name "
                 + "FROM target INNER JOIN dbf ON target.dbf_id = dbf.id";
             Statement stat = connectionSqlite.createStatement();
             ResultSet rs = stat.executeQuery(sql);
             while (rs.next()) {
                 System.out.println(rs.getInt("target_id") + "  " + rs.getString("dbf_name"));
                 processTarget(rs.getInt("target_id"), rs.getString("dbf_name"), rs.getInt("lines"));
             }
             rs.close();
             rat.close();
             raf.close();
 
             // Ending message
             if (execType.equals(Constants.EXEC_GVSIG)) {                
                 Utils.showMessage("inp_end", fileInp.getAbsolutePath(), "inp_descr", execType);                
                 logger.info(fileInp.getAbsolutePath());
             } else{
                 System.out.println(fileInp.getAbsolutePath());
             }
 
         } catch (IOException e) {
             Utils.showError("inp_error_io", e.getMessage(), "inp_descr");
         } catch (SQLException e) {
             Utils.showError("inp_error_execution", e.getMessage(), "inp_descr");
         }
 
     }
 
 
     // Process target specified by id parameter
     private void processTarget(int id, String tableName, int lines) throws IOException, SQLException {
 
         // Go to the first line of the target
         for (int i = 1; i <= lines; i++) {
             String line = rat.readLine();
             raf.writeBytes(line + "\r\n");
         }
 
         // If table is null or doesn't exit then exit function
         if (!checkTable(tableName) && !checkView(tableName)) {
             return;
         }
 
         // Target polygons: Write only if check is selected
         if (bPolygons == false && id == polygons_target_id) {
             return;
         }
 
         // Get data of the specified Postgis table
         this.lMapDades = getTableData(tableName);
         if (this.lMapDades.isEmpty()) {
             return;
         }
 
         // Get table columns to write into this target
         mHeader = new LinkedHashMap<String, Integer>();
         String sql = "SELECT name, space FROM target_fields WHERE target_id = " + id + " ORDER BY pos";
         Statement stat = connectionSqlite.createStatement();
         ResultSet rs = stat.executeQuery(sql);
         while (rs.next()) {
             mHeader.put(rs.getString("name").trim().toLowerCase(), rs.getInt("space"));
         }
         rs.close();
 
         ListIterator<Map<String, String>> it = this.lMapDades.listIterator();
         Map<String, String> m; // Current Postgis row data
         //int index = 0;
         String sValor = null;
         int size = 0;
         // Iterate over Postgis table
         while (it.hasNext()) {
             m = it.next();
             Set<String> set = mHeader.keySet();
             Iterator<String> itKey = set.iterator();
             // Iterate over fields specified in table target_fields
             while (itKey.hasNext()) {
                 String sKey = (String) itKey.next();
                 sKey = sKey.toLowerCase();
                 size = mHeader.get(sKey);
                 // Write to the output file if the field exists in Postgis table
                 if (m.containsKey(sKey)) {
                     sValor = (String) m.get(sKey);
                     raf.writeBytes(sValor);
                     // Complete spaces with empty values
                     for (int j = sValor.length(); j <= size; j++) {
                         raf.writeBytes(" ");
                     }
                 } // If key doesn't exist write empty spaces
                 else {
                     for (int j = 0; j <= size; j++) {
                         raf.writeBytes(" ");
                     }
                 }
             }
 
             //TODO: Target polygons: Write id and coordinates of the current row
 //			if (id == polygons_target_id && sValor != null){
 //				if (fShp[fileIndex] != null && fShp[fileIndex].exists()) {
 //					VectorialDriver vd = getDriver(fShp[fileIndex]);
 //					writePoint(vd, index, sValor, size);
 //					index++;
 //				}
 //				else{
 //					//System.out.println("Shape null");
 //					//Utils.showError("inp_error_notfound", fShp[index].getPath(), "inp_descr");
 //				}
 //			} 
 //			else{
             raf.writeBytes("\r\n");
 //			}
 
         }
 
     }
 
 
     // Check if the table exists
     private boolean checkTable(String tableName) {
         String sql = "SELECT * FROM pg_tables WHERE upper(tablename) = '" + tableName + "'";
         try {
             Statement stat = connectionPostgis.createStatement();
             ResultSet rs = stat.executeQuery(sql);
             return (rs.next());
         } catch (SQLException e) {
             logger.severe(e.getMessage());
             return false;
         }
     }
     
     
     // Check if the view exists
     private boolean checkView(String viewName) {
         String sql = "SELECT * FROM pg_views WHERE upper(viewname) = '" + viewName + "'";
         try {
             Statement stat = connectionPostgis.createStatement();
             ResultSet rs = stat.executeQuery(sql);
             return (rs.next());
         } catch (SQLException e) {
             logger.severe(e.getMessage());
             return false;
         }
     }    
     
 
     // Exec SWMM
     public boolean execSWMM(File fileInp, File fileRpt) {
 
         if (execType.equals(Constants.EXEC_GVSIG)) {                
             logger.info("execSWMM");
         }
 
 //		file = folder + "swmm5.exe " + folder + "222.inp " + folder + "2.rpt " + folder + "2.out";
 
         String folder = iniProperties.getProperty("FOLDER_EPA_SWMM");
         String exe = iniProperties.getProperty("EXE_EPA_SWMM");
         String exeCmd = folder + File.separator + exe;
         File exeFile = new File(exeCmd);
         // Check if exeFile exists
 		if (!exeFile.exists()){
 			Utils.showError("inp_error_notfound", exeCmd, "inp_descr");
 			return false;
 		}
 
         String sFile;
         // Get files
         if (fileInp == null) {
             sFile = iniProperties.getProperty(sExport + "INP");
             sFile = folderConfig + File.separator + sFile;
             fileInp = new File(sFile);
             sFile = sFile.replace(".inp", ".rpt");
             fileRpt = new File(sFile);
         }
         if (!fileInp.exists()){
 			Utils.showError("inp_error_notfound", fileInp.getAbsolutePath(), "inp_descr");     
 			return false;
         }
        if (!fileRpt.exists()){
			Utils.showError("inp_error_notfound", fileRpt.getAbsolutePath(), "inp_descr");     
			return false;
        }        
         sFile = fileRpt.getAbsolutePath().replace(".rpt", ".out");
         File fileOut = new File(sFile);
 
         exeCmd += " " + fileInp.getAbsolutePath() + " " + fileRpt.getAbsolutePath() + " " + fileOut.getAbsolutePath();
 
         // Ending message
         if (execType.equals(Constants.EXEC_GVSIG)) {                
             logger.info(exeCmd);            
         } else{
             System.out.println(exeCmd);
         }        
 
         // Exec process
     	Process p;
 		try {
 			p = Runtime.getRuntime().exec(exeCmd);
 	        p.waitFor();			
 		} catch (IOException e) {
 			Utils.showError("inp_error_io", exeCmd, "inp_descr");
 		} catch (InterruptedException e) {
 			Utils.showError("inp_error_io", exeCmd, "inp_descr");
 		}
 
         // Ending message
         if (execType.equals(Constants.EXEC_GVSIG)) {                
             Utils.showMessage("inp_end", fileOut.getAbsolutePath(), "inp_descr", execType);                
             logger.info(fileOut.getAbsolutePath());
         } else{
             System.out.println(fileOut.getAbsolutePath());
         }		
         return true;
 
     }
 
 
     // Import RPT file into Postgis tables
     public void importRpt(File fileRpt, String projectName) {
         
     	this.fileRpt = fileRpt;
     	this.projectName = projectName;
     	
     	// TODO: Show RPT content
 
         if (execType.equals(Constants.EXEC_GVSIG)) {                
             logger.info("importRpt");
         }
         
     	// Check if Project Name exists in rpt_result_id
     	boolean exists = false;
     	if (existsProjectName()){
     		exists = true;
             if (execType.equals(Constants.EXEC_GVSIG)) {         		
             	int reply = Utils.confirmDialog("project_exists", "inp_descr");    		
             	if (reply == JOptionPane.NO_OPTION){
             		return;
             	}
             }
     	}
     	
 		// Open RPT file
 		try {
 			rat = new RandomAccessFile(fileRpt, "r");
 		} catch (FileNotFoundException e) {
 			Utils.showError("inp_error_notfound", fileRpt.getAbsolutePath(), "inp_descr");
 			return;
 		}			
         
         // Get info from rpt_target into memory
         TreeMap<Integer, RptTarget> targets = new TreeMap<Integer, RptTarget>();
         String sql = "SELECT * FROM " + schema + ".rpt_target ORDER BY id";
         try {
     		connectionPostgis.setAutoCommit(false);        	
             Statement stat = connectionPostgis.createStatement();
             ResultSet rs = stat.executeQuery(sql);
             while (rs.next()) {
                 RptTarget rpt = new RptTarget(rs);
                 targets.put(rpt.getId(), rpt);
             }
             rs.close();
         } catch (SQLException e) {
             Utils.showError(e.getMessage(), "", "inp_descr");
             return;
         }
         
         // Iterate over targets
         Iterator<Entry<Integer, RptTarget>> it = targets.entrySet().iterator();
         while (it.hasNext()) {
         	insertSql = "";
             Map.Entry<Integer, RptTarget> mapEntry = it.next();
             RptTarget rpt = mapEntry.getValue();
         	if (processRpt(rpt)){
 	    		if (exists){
 	    			sql = "DELETE FROM " + schema + "." + rpt.getTable() + " WHERE result_id = '" + projectName + "'";
 	    			executeSql(sql, false);
 	    		}
 	    		System.out.println(insertSql);        		
 				if (!executeSql(insertSql, false)){
 					return;
 				}
         	} else{
         		logger.info("Target not found: " + rpt.getId() + " - " + rpt.getDescription());
         	}
         }
         
         // Ending process
         if (exists){
     		sql = "UPDATE " + schema + ".rpt_result_id SET exec_date = Now()";
         } else{
     		sql = "INSERT INTO " + schema + ".rpt_result_id VALUES ('" + projectName + "')";
         }
 		executeSql(sql, true);
 		
         // Ending message
         if (execType.equals(Constants.EXEC_GVSIG)) {                
             Utils.showMessage("import_end", "", "inp_descr", execType);                
         } else{
             System.out.println("Process executed");
         }		
 	
     }
 
 
 	private boolean existsProjectName() {
 		String sql = "SELECT * FROM " + schema + ".rpt_result_id WHERE result_id = '" + projectName + "'";
 		try {
 			PreparedStatement ps = connectionPostgis.prepareStatement(sql);
 	        ResultSet rs = ps.executeQuery();
 	        return rs.next();
 		} catch (SQLException e) {
             Utils.showError(e.getMessage(), "", "inp_descr");
 			return false;
 		}
 	}
 
 		
 	private boolean processRpt(RptTarget rpt) {
 
 		// Read lines until rpt.getDescription() is found		
 		boolean found = false;
 		String line;
 		String aux;
 		logger.info("Target: " + rpt.getId() + " - " + rpt.getDescription());
 		while (!found){
 			lineNumber++;
 			try {
 				// If pointer has reached EOF, return to first position
 				if (rat.getFilePointer() >= rat.length()){
 					rat.seek(0);
 					return false;
 				}
 				line = rat.readLine().trim();
 				if (line.length() >= rpt.getDescription().length()){
 					aux = line.substring(0, rpt.getDescription().length());
 					if (aux.equals(rpt.getDescription())){
 						found = true;
 					}
 				}
 			} catch (IOException e) {
 				Utils.showError("inp_error_io", "", "inp_descr");
 			}
 		}
 		
         if (rpt.getType() == 3 || rpt.getType() == 5 || rpt.getType() == 6){
         	getPollutants(rpt);
         }
 		
 		// Jump number of lines specified in rpt.getTitleLines()
 		for (int i = 1; i <= rpt.getTitleLines(); i++) {
 			try {
 				lineNumber++;
 				line = rat.readLine();
 			} catch (IOException e) {
 				Utils.showError("inp_error_io", "", "inp_descr");
 			}
 		}		
 		
 		// Read following lines until blank line is found
 		tokensList = new ArrayList<ArrayList<String>>();		
 		parseLines(rpt);
 		if (rpt.getType() == 2 || rpt.getType() == 3){
 			processTokens(rpt);				
 		}
 		
 		return true;
 		
 	}	
 	
 
 	private void getPollutants(RptTarget rpt) {
 		
 		// Open RPT file again
 		if (rpt.getType() == 3){
 			try {
 				rat = new RandomAccessFile(fileRpt, "r");
 			} catch (FileNotFoundException e) {
 				Utils.showError("inp_error_notfound", fileRpt.getAbsolutePath(), "inp_descr");
 				return;
 			}
 		}
 		
 		String line = "";
 		int jumpLines;
 		try {		
 			if (rpt.getType() == 3){			
 				for (int i = 1; i < lineNumber - 1; i++) {
 					System.out.println(rat.readLine().trim());
 				}
 				line = rat.readLine().trim();		
 				lineNumber--;
 			} else{
 				jumpLines = (rpt.getType() == 5) ? 4 : 5;
 				for (int i = 1; i <= jumpLines; i++) {				
 					line = rat.readLine().trim();
 					System.out.println(line);
 				}
 			}
 			boolean blankLine = (line.length() == 0);
 			if (!blankLine){
 				Scanner scanner = new Scanner(line);
 				if (rpt.getType() == 3 || rpt.getType() == 6){
 					int jumpScanner	= (rpt.getType() == 3) ? 1 : 4;
 					for (int i = 0; i < jumpScanner; i++) {
 						scanner.next();						
 					}
 				}
 				parseLine1(scanner, rpt, false);		
 				pollutants = new ArrayList<String>();	
 				for (int i = 0; i < tokens.size(); i++) {
 					pollutants.add(tokens.get(i));
 				}
 			}
 		} catch (IOException e) {
 			Utils.showError("inp_error_io", "", "inp_descr");
 		}		
 		
 	}
 	
 	
 	// Parse all lines
 	private void parseLines(RptTarget rpt) {
 		
 		tokens = new ArrayList<String>();			
 		boolean blankLine = false;		
 		while (!blankLine){
 			try {
 				lineNumber++;
 				String line = rat.readLine().trim();
 				//System.out.println(line);
 				blankLine = (line.length()==0);
 				if (!blankLine){
 					Scanner scanner = new Scanner(line);
 					if (rpt.getType() == 1){
 						parseLine1(scanner, rpt, false);
 						processTokens(rpt);							
 					}		
 					else if (rpt.getType() == 2){					
 						parseLine2(scanner, rpt, true);
 					}
 					else if (rpt.getType() == 3){	
 						tokens = new ArrayList<String>();
 						parseLine2(scanner, rpt, false);
 						tokensList.add(tokens);
 					}					
 					else if (rpt.getType() == 4){					
 						parseLine1(scanner, rpt, true);
 						processTokens(rpt);							
 					}	
 					else if (rpt.getType() == 5){					
 						tokens = new ArrayList<String>();
 						parseLine1(scanner, rpt, false);
 						processTokens5(rpt);						
 					}				
 					else if (rpt.getType() == 6){					
 						tokens = new ArrayList<String>();
 						parseLine1(scanner, rpt, false);
 						processTokens6(rpt);						
 					}							
 				}
 			} catch (IOException e) {
 				Utils.showError("inp_error_io", "", "inp_descr");
 			}
 		}		
 		
 	}
 		
 
 	// Parse values of current line
 	private void parseLine1(Scanner scanner, RptTarget rpt, boolean together) {
 		
 		// Parse line
 		tokens = new ArrayList<String>();	
 		String token = "";
 		while (scanner.hasNext()){
 			if (together){
 				token += " " + scanner.next();
 			}
 			else{
 				token = scanner.next();
 				tokens.add(token);
 			}
 		}
 		if (together){
 			tokens.add(token.trim());
 		}
 		
 	}	
 	
 	
 	// Parse values of current line that contains ".." in it
 	private void parseLine2(Scanner scanner, RptTarget rpt, boolean together) {
 		
 		// Parse line
 		String token;
 		boolean valid = false;
 		String aux = "";
 		int numTokens = 0;
 		while (scanner.hasNext()){
 			token = scanner.next();
 			if (valid == true){
 				numTokens++;
 				if (numTokens <= rpt.getTokens()){
 					if (together){
 						aux += token + " ";
 					}
 					else{
 						tokens.add(token);						
 					}
 				}
 			}
 			if (token.contains("..")){
 				valid = true;
 			}
 		}
 		if (valid == true && together){
 			tokens.add(aux.trim());
 			//System.out.println(aux);				
 		}	
 		
 	}
 	
 	
 	private void processTokens(RptTarget rpt) {
 
 		String fields = "result_id, ";
 		String values = "'" + projectName + "', ";
 		String sql = "SELECT * FROM " + schema + "." + rpt.getTable();
 		try {
 	        PreparedStatement ps = connectionPostgis.prepareStatement(sql);
 	        ResultSet rs = ps.executeQuery();
 	        ResultSetMetaData rsmd = rs.getMetaData();	
 	        rs.close();
 	        int j;
 	        for (int i = 0; i < tokens.size(); i++){
 	        	j = i + 2;
 	        	switch (rsmd.getColumnType(j)) {
 				case Types.NUMERIC:
 				case Types.DOUBLE:
 				case Types.INTEGER:
 					values += tokens.get(i) + ", ";
 					break;					
 				case Types.VARCHAR:
 					values += "'" + tokens.get(i) + "', ";
 					break;					
 				default:
 					values += "'" + tokens.get(i) + "', ";
 					break;
 				}
 				fields += rsmd.getColumnName(j) + ", ";        	
 	        }
 		} catch (SQLException e) {
 			Utils.showError(e.getMessage(), sql, "inp_descr");
 		}
 	
 		fields = fields.substring(0, fields.length() - 2);
 		values = values.substring(0, values.length() - 2);
 		sql = "INSERT INTO " + schema + "." + rpt.getTable() + " (" + fields + ") VALUES (" + values + ");\n";
 		insertSql += sql;
 		
 	}
 		
 	
 	private void processTokens5(RptTarget rpt) {
 
 		String fields = "result_id, subc_id, poll_id, value";
 		String fixedValues = "'" + projectName + "', '" + tokens.get(0) + "', ";
 		String sql;
 		String values;
 		Double units;
 		// Iterate over pollutants
 		if (tokens.size() > pollutants.size()){
 			for (int i = 0; i < pollutants.size(); i++) {
 				units = Double.valueOf(tokens.get(i + 1));
 				values = fixedValues + "'" + pollutants.get(i) + "', " + units;
 				sql = "INSERT INTO " + schema + "." + rpt.getTable() + " (" + fields + ") VALUES (" + values + ");\n";
 				insertSql += sql;		        
 			}
 		}
 		
 	}	
 
 	
 	private void processTokens6(RptTarget rpt) {
 		
 		String fields = "result_id, node_id, flow_freq, avg_flow, max_flow, total_vol";
 		String fields2 = "result_id, node_id, poll_id, value";		
 		String fixedValues = "'" + projectName + "', '" + tokens.get(0) + "', ";
 		String values;
 		String sql;
 		Double units;
 	
 		// If found separator o resume line skip them
 		if (tokens.size() < 5 || tokens.get(0).equals("System")){
 			return;
 		}
 		
 		// Iterate over first 5 fields
 		values = fixedValues;
 		for (int j = 1; j < 5; j++) {
 			values += tokens.get(j) + ", ";
 		}
 		values = values.substring(0, values.length() - 2);		
 		sql = "INSERT INTO " + schema + "." + rpt.getTable() + " (" + fields + ") VALUES (" + values + ");\n";
 		insertSql += sql;				
 		
 		// Iterate over pollutants
 		for (int i = 0; i < pollutants.size(); i++) {
 			int j = i + 5;
 			units = Double.valueOf(tokens.get(j));
 			values = fixedValues + "'" + pollutants.get(i) + "', " + units;
 			sql = "DELETE FROM " + schema + ".rpt_outfallload_sum " +
 				"WHERE result_id = '" + projectName + "' AND node_id = '"  + tokens.get(0) + "' AND poll_id = '" + pollutants.get(i) + "';\n";
 			insertSql += sql;	
 			sql = "INSERT INTO " + schema + ".rpt_outfallload_sum (" + fields2 + ") VALUES (" + values + ");\n";
 			insertSql += sql;		        
 		}
 		
 	}		
 	
 	
 	private boolean executeSql(String sql, boolean commit) {
 		try {
 			Statement ps = connectionPostgis.createStatement();
 	        ps.executeUpdate(sql);
 	        if (logger != null){
 	        	logger.info(sql);
 	        }
 	        if (commit){
 	        	connectionPostgis.commit();
 	        }
 			return true;
 		} catch (SQLException e) {
 			Utils.showError(e.getMessage(), "", "inp_descr");
 			return false;
 		}
 	}
 		
 	
 }
