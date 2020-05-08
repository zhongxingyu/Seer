 package com.teradata.qaf.tset.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 public class SQLWriter extends BaseWriter{
 
 	private static Logger logger = Logger.getLogger(SQLWriter.class.getName());
 	//private static String ddlFileName = "TSETInfoTables/ddl.sql";
 	private static String ddlFileName;
 	
 	public static void writeSQL(List<String> sqlList) throws Exception{
 		//File f = new File(ddlFileName);
 		File f = (new SQLWriter()).openFile(ddlFileName);
 		FileWriter fw = null;
 		BufferedWriter bw = null;
 		try {
 			fw = new FileWriter(f);
 			bw = new BufferedWriter(fw);
 			Iterator<String> it = sqlList.iterator();
 			while(it.hasNext()) {
 				
 				//fw.append(it.next() + ";\r\n");
 				
 				String tempSql = it.next();
				
				if (tempSql == null) continue;
				
 				if (tempSql.endsWith(";")) {
 					//fw.append(tempSql + "\r\n");
 					bw.append(tempSql + "\r\n");
 				} else {
 					//fw.append(tempSql + ";\r\n");
 					bw.append(tempSql + ";\r\n");
 				}
 				
 			}
 		} catch (IOException e) {
 			bw.close();
 			fw.close();
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			logger.error("ERROR while writing exported DDL file(s), " +
 					"ROLLBACK automatically and handle the exception outside.");
 			throw new IOException();
 		} finally {
 			try {
 				bw.flush();
 				bw.close();
 				//fw.flush();
 				fw.close();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 				logger.error(e.getMessage());
 			}
 			
 		}
 	}
 	
 	public static void setFileName(String fileName) {
 		ddlFileName = fileName;
 	}
 	
 }
