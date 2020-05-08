 package com.aavu.client.domain;
 
 import java.io.Serializable;
 
 import com.aavu.client.util.Logger;
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class Entry extends Occurrence implements Serializable,IsSerializable, ReallyCloneable {
 	
 	
 	private static final String INIT_STR = "<BODY contentEditable=true>";
 	private static final String INIT_STR_END = "</BODY>";
 	
 	public Entry (){
 		super();
 		
 		//important. helps activates edit area
 		setData(INIT_STR+INIT_STR_END);
 	}
 	public void setInnerHTML(String html){
 		setData(INIT_STR+html+INIT_STR_END);
 	}
 	public boolean isEmpty(){
 		//System.out.println("Empty Check |"+getData()+"|");
 		//System.out.println("Empty Check "+getData().length()+" "+(INIT_STR.length()+INIT_STR_END.length()));
 		 
 		return getData() == null ||
 				(getData().length() == INIT_STR.length()+INIT_STR_END.length()); 
 	}
 	
 	/**
 	 * Strip all <body> <head> stuff. Used by Freemarker, bc otherwise sitemesh think we have
 	 * multiple body elements and that's not pretty.  
 	 * 
 	 * be careful of:
 	 * contentEditable="true" vs =true
 	 * contentEditatble vs contenteditable
 	 * BODY vs body.
 	 * 
 	 * PEND HIGH Note, the catch is now a valid case, since we've switched to the GWT editor which doesn't need the
 	 * body tags. Because we've got mixed data, we'll just hope that errors are new data w/o body tag. 
 	 * 
 	 * @return
 	 */
 	public String getDataWithoutBodyTags(){
 		String s = "<body";
 		int start_body = getData().toLowerCase().indexOf(s);
		int start = getData().indexOf('>', start_body) + 1;
 		int end = getData().toUpperCase().indexOf(INIT_STR_END); 
 		
 		try{ 		
 			return getData().substring(start,end);		
 		}catch (StringIndexOutOfBoundsException e) {
 			//System.out.println("e. "+e.getMessage());
 			//System.out.println("!!!!!! "+getData()+" "+getData().length());
 			//System.out.println(start+" "+end);
			Logger.debug("Didn't Find Body Tag, hoping..");
 			return getData();
 		}
 	}
 	
 	//@Override
 	public Object clone() {				   		
 		return copyPropsIntoParam(new Entry());
 	}
 
 	
 }
