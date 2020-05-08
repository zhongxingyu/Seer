 package fr.utc.nf28.moka.environment.items;
 
 
 public class ContentEntry {
 	private String mField;
 	private String mContent;
 
	public ContentEntry(){
 	}
 
 	public ContentEntry(String field, String content) {
 		mField = field;
 		mContent = content;
 	}
 
	public String getField(){
		return  mField;
 	}
 
	public void setField(String field){
 		mField = field;
 	}
 
	public String getContent(){
		return  mContent;
 	}
 
	public void setContent(String content){
 		mContent = content;
 	}
 }
