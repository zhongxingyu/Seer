 package org.eclipse.dltk.validators.internal.core.externalchecker;
 
 import java.util.List;
 
 public class ExternalCheckerProblem {
 
 	private String type;
 	private List tokens; 
 	
 	public ExternalCheckerProblem(String type, List tokens){
 		this.type = type;
 		this.tokens = tokens;
 	}
 		
 	public int getLineNumber() {
 		for(int i=0; i<tokens.size(); i++){
 			WildcardToken wtok = (WildcardToken)tokens.get(i);
 			String wtype = wtok.getType();
 			if(wtype=="linenumber"){			
 				return Integer.parseInt((String)(wtok.getValue()));
 			}
 		}
 		return 0;
 	}
 	
 	public String getDescription() {
		StringBuffer sb = new StringBuffer();
 		
 		for (int i=0; i< tokens.size(); i++){
 			sb.append(((WildcardToken)tokens.get(i)).getValue());
 		}
 		return sb.toString();
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getType() {
 		return type;
 	}
 }
