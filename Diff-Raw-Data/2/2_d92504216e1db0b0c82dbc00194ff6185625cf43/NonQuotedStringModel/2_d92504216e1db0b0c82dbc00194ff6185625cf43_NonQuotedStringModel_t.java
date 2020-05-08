 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.types;
 
 import java.io.Serializable;
 
 import org.modelcc.*;
 
 /**
  * Non Quoted String Model.
  * @author elezeta
  * @serial
  */
@Pattern(regExp="([a-zA-Z0-9_\\-\\!\\@\\$\\#\\`\\~\\%\\^\\&\\*\\(\\)\\=\\+\\[\\]\\{\\}\\\\\\|\\:\\'\\,\\.\\<\\>\\/\\?]([a-zA-Z0-9_\\-\\!\\@\\$\\#\\`\\~\\%\\^\\&\\*\\(\\)\\=\\+\\[\\]\\{\\}\\\\\\|\\:\\'\\\"\\,\\.\\<\\>\\/\\?]*[a-zA-Z0-9_\\-\\!\\@\\$\\#\\`\\~\\%\\^\\&\\*\\(\\)\\=\\+\\[\\]\\{\\}\\\\\\|\\:\\'\\,\\.\\<\\>\\/\\?])?)?")
 public class NonQuotedStringModel extends StringModel implements IModel,Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
 	@Value
 	protected String val;
 
 	public NonQuotedStringModel() {
 	}
 
 	public NonQuotedStringModel(String val) {
 		this.val = val;
 	}
 
 	@Override
 	public String getValue() {
 		return val;
 	}
 	
 	@Override
 	public String toString() {
 		return val;
 	}
 	
 }
