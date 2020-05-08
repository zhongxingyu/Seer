 package edu.illinois.gitsvn.infra.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CSVwriter {
 	private List<List<String>> rows;
	private List<String> headers;
 	
 	public CSVwriter(){
 		rows = new ArrayList<>();
 	}
 	
 	public void addHeader(List<String> headers){
		this.headers = headers;
 	}
 	
	public List<String> getHeader() {
		return headers;
 	}
 
 	public void addRow(List<String> asList) {
 		// TODO Auto-generated method stub
 	}
 	
	public List<List<String>> getRows() {
		// TODO Auto-generated method stub
		return null;
	}
	
 	public void dumpToFile(String path){
 		
 	}

 }
