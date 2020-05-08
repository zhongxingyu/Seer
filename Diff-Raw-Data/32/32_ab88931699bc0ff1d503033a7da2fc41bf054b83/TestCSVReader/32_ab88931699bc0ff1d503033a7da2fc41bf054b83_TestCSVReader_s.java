 package org.imaginea.botbot.api;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 

 import au.com.bytecode.opencsv.CSVReader;
 
 public class TestCSVReader {
 	CSVReader reader = null;
 	List<String[]> rows=null;
 	public TestCSVReader(String filePath) throws Exception{
 		reader = new CSVReader((new FileReader(filePath)),',');
		rows=reader.readAll();
 	}
 	
 	public TestCSVReader(InputStream ipPath) throws Exception{
		reader = new CSVReader(new InputStreamReader(ipPath),',');
		rows=reader.readAll();
 	}
 	
 	public String getData(int row,int column){
 		String data=null;
 		if(row < 0 && row > rows.size()){
 			return "";
 		}
 		String[] rowData= rows.get(row);
 		if(column < 0 || column >=rowData.length){
 			return "";
 		}
 		
 		data=rowData[column];
 		
 		return data;
 	}
 	
 	public String[] getRow(int row){
 		if(row < 0 && row > rows.size()){
 			return new String[0];
 		}
 		return rows.get(row);
 	}
 	
 	public int getLines(){
 		return rows.size();
 	}
 	
 }
