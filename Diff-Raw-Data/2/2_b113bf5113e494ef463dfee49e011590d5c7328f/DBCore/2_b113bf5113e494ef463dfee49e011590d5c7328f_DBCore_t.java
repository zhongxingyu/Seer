 package br.ufsm.dsweb.db;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class DBCore {
 	private static String DB_PATH = "";
 
 	private interface RowReader {
 		public boolean doSomething(String row, boolean has_next);
 	}
 	
 	private static boolean createFileIfNotExists(String filename) {
 		boolean ret = true;
 		File file = new File(DB_PATH+filename);
 		if(!file.exists()) {
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				ret = false;
 			}
 		}
 		return ret;
 	}
 	
 	private static String readFile(String filename, RowReader row_reader) {
 		if(!createFileIfNotExists(filename)) {
 			return "";
 		}
 		String line = "";
 		try {
 			FileInputStream fis = new FileInputStream(filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 			
 			line = br.readLine();
 			while(line != null) {
 				String next = br.readLine();
 				if(!row_reader.doSomething(line, next != null)) {
 					break;
 				}
 				line = next;
 			}
 			br.close();
 			fis.close();
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return line == null ? "" : line;
 	}
 	
 	public static void appendToFile(String filename, String row) {
 		if(!createFileIfNotExists(filename)) {
 			return;
 		}
 		try {
 			FileWriter fw = new FileWriter(filename, true);
			fw.write(row+"\n");
 			fw.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	public static String getRowByCol(String filename, final int column, final String value) {
 		String row = readFile(filename, new RowReader() {
 			@Override
 			public boolean doSomething(String row, boolean has_next) {
 				boolean keep = true;
 				String[] vals = row.split(",");
 				if(vals[column].equals(value)) {
 					keep = false;
 				}
 				return keep;
 			}
 		});
 		
 		return row;
 	}
 	public static String getLastRow(String filename) {
 		return readFile(filename, new RowReader() {			
 			@Override
 			public boolean doSomething(String row, boolean has_next) {
 				if(!has_next) {
 					return false;
 				}
 				return true;
 			}
 		});
 	}
 }
