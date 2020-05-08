 package uk.ac.shef.dynamicdocument.document;
 
 import uk.ac.shef.dynamicdocument.Word;
 
 public class DynamicWord extends Word
 {
 	public DynamicWord(String word, int lev, int connect)
 	{
 		super(word,lev,connect);
 	}
 
 	public DynamicWord(String word)
 	{
 		this(word,false);
 	}
 	
 	public DynamicWord(String word, boolean bold)
 	{
 		this("",0,0);
 		
 		setText(word);
 		setConnect(Word.DISTINCT);
 		setManipulationLevel(Word.ALWAYS_APPLY);
 		setBold(bold);
 	}
 	
 	public void setFontSize(int size)
 	{
		super.setFontSize(size);
 	}
 }
