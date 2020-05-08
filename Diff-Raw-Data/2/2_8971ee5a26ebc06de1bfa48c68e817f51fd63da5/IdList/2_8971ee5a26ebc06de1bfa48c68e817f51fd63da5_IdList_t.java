 package com.tos_prophet;
 
 import java.util.ArrayList;
 
 public class IdList {
 	private static ArrayList<String> idlist;
 	private IdList(){
 	}
 	static public void addList(String str){
 		String[] names = str.split("\r\n");
 		idlist = new ArrayList<String>();
 		for(int i=0;i<names.length;i++){
 			idlist.add(names[i]);
 		}
 	}
 	static public ArrayList<String> getIdList(){
 		return idlist;
 	}
 	
 	static public String findNameById(int i){
		return idlist.get(i-1);
 	}
 	
 }
