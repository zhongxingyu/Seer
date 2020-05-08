 /**
  * 
  */
 package org.cotrix.web.codelistmanager.shared.modify.code;
 
 import org.cotrix.web.codelistmanager.shared.modify.ModifyCommand;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class UpdateCodeCommand implements ModifyCommand, CodeCommand  {
 	
 	protected String codeId;
 	protected String name;
 
 	/**
 	 * @param name
 	 */
 	public UpdateCodeCommand(String codeId, String name) {
 		this.codeId = codeId;
 		this.name = name;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the codeId
 	 */
 	public String getCodeId() {
 		return codeId;
 	}
 }
