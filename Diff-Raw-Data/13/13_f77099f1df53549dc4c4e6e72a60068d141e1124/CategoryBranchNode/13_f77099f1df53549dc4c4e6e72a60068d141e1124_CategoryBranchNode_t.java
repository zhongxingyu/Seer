 package org.yarquen.category;
 
 /**
  * Branch node
  * 
  * @author Jorge Riquelme Santana
  * @date 23/01/2013
  * @version $Id$
  * 
  */
 public class CategoryBranchNode {
 	private String code;
 	private String name;
 
 	public CategoryBranchNode() {
 	}
 
 	public CategoryBranchNode(String code, String name) {
 		setCode(code);
 		setName(name);
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setCode(String code) {
 		if (code.contains(CategoryBranch.CODE_SEPARATOR)) {
 			throw new IllegalArgumentException(
 					code
 							+ " isn't a valid code (it can't contain the code separator: '"
 							+ CategoryBranch.CODE_SEPARATOR + "')");
 		} else {
 			this.code = code;
 		}
 	}
 
 	public void setName(String name) {
 		if (name != null && name.contains(CategoryBranch.NAME_SEPARATOR)) {
 			throw new IllegalArgumentException(
 					name
 							+ "isn't a valid name (it can't contain the name separator: '"
 							+ CategoryBranch.NAME_SEPARATOR + "')");
 		} else {
 			this.name = name;
 		}
 	}
	
	public void setName(String name,Boolean fromSafeSource) {
		if(!fromSafeSource)
			this.setName(name);
		this.name = name;
	}
 }
