 package com.socaldevs.jeopardy;
 
 import java.io.File;
 
 public class XmlStorage implements DataStorage{
 	
 	
 	public XmlStorage(File file){
 	}
 	
 	public XmlStorage(String path){
 		this(new File(path));
 	}
 
 	@Override
 	public Question getQuestion(int col, int row) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public void put(String question, String answer, int row, int col, int value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 }
