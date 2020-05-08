 package com.mates120.myword;
 
 public class Word {
 	private String source;
 	private String value;
 	private String dictionaryName;
 
 	public Word(String source, String value, String dictionaryName)
 	{
 		this.source = source;
 		this.value = value;
 		this.dictionaryName = dictionaryName;
 	}
 	
 	public String getSource(){
 		return source;
 	}
 
 	public void setSource(String source){
 		this.source = source;
 	}
 
 
 	public void setValue(String value){
 		this.value = value;
 	}
 
 	public String getValue(){
 		return this.value;
 	}
 	
	public String getDictName()
	{
		return dictionaryName;
 	}
 
 	@Override
 	public String toString(){
 		return source;
 	}
 
 	public boolean equals(Word word){
 		boolean isEqual = false;
 		if((this.source.equals(word.getSource())) 
 				&& this.value.equals(word.getValue()))
 			isEqual = true;
 		return isEqual;
 	}
 }
