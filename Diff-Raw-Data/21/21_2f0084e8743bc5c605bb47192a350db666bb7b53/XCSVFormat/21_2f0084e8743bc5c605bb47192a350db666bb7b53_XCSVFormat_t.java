 package com.util;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
public class XCSVFormat implements Serializable{	
	
	private static final long serialVersionUID = 1L;
 	
 	String[] columns = null;
 	String[] types = null;
 	
 	
 	public XCSVFormat(String csv_desc_file){
 		this.columns = XParser.getTemplateDescAsArray(csv_desc_file);		
 				
 	}	
 	/**
 	 * convert columns and values into pairs and to string 
 	 * @param values
 	 * @return
 	 * @throws Exception
 	 */
 	public String pairToString(String values[],ArrayList<String> filter) throws Exception{
 		if(this.columns == null)
 			throw new Exception("CSV File format is null!");
 		
 		String metaData = "";
 		for(int i=0;i<columns.length;i++){
 			if(filter != null){
 				if(filter.contains(columns[i]))
 					continue;
 			}			
 			metaData += columns[i]+"="+values[i];
 			if(i < columns.length-1)
 				metaData += ";";
 		}
 		
 		return metaData;
 	}
 	/**
 	 * parse the value to key and value
 	 * @param pairData
 	 * @return <column, value>
 	 * @throws Exception
 	 */
 	public Hashtable<String,String> fromPairString(String pairData) throws Exception{
 		
 		if(this.columns == null)
 			throw new Exception("CSV File format is null!");
 		if(pairData == null)
 			throw new Exception("the pairData is null");
 		
 		Hashtable<String,String> pairs = new Hashtable<String,String>();
 		try{
 			
 			String[] items = pairData.split(";");
 			for(int i=0;i<items.length;i++){
 				String[] key_value = items[i].split("=");
 				pairs.put(key_value[0], key_value[1]);
 			}			
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 				
 		
 		return pairs;
 	}
 	
 	
 	public int getLatitudeIndex() throws Exception{
 		if(this.columns == null)
 			throw new Exception("CSV File format is null!");
 
 		for(int i=0;i<this.columns.length;i++){
 			if(columns[i].equals("lat")){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public int getLongitudeIndex() throws Exception{
 		if(this.columns == null)
 			throw new Exception("CSV File format is null!");
 
 		for(int i=0;i<this.columns.length;i++){
 			if(columns[i].equals("long")){
 				return i;
 			}
 		}
 		return -1;
 	}	
 	
 	public int getIdIndex() throws Exception{
 		if(this.columns == null)
 			throw new Exception("CSV File format is null!");
 
 		for(int i=0;i<this.columns.length;i++){
 			if(columns[i].equals("id")){
 				return i;
 			}
 		}
 		return -1;
 	}		
 	
 	
 	
 	public String[] getTypes() {
 		return types;
 	}
 	
 
 	public String[] getColumns(){
 		return columns;
 	}
 	
 	public String getTypeByColumn(String column){
 		for(int i=0;i<columns.length;i++){
 			if(columns[i].equals(column)){
 				return this.types[i];
 			}
 		}
 		return null;
 	}
 	
 
 	public String columnString(){
 		if(this.columns != null){
 			String str = "";
 			for(String c: columns){
 				str += c;
 				str += ";";
 			}
 			return str;
 		}
 		return null;
 	}
 	
 
 }
