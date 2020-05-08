 /*
  *	File: @(#)PafSimpleDimTree.java 		Package: com.pace.base.mdb 	Project: Paf Base Libraries
  *	Created: Nov 21, 2006  		By: AFarkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.mdb;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.pace.base.IPafCompressedObj;
 
 /**
  * This is a simplified version of the Paf Dim Member Tree
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class PafSimpleDimTree implements IPafCompressedObj {
 
 	private String id = null;
     private String[] traversedMembers;
     private String rootKey;
     private String[] aliasTableNames  = null;
     private PafSimpleDimMember[] memberObjects;
    private boolean isDiscontig = false;
     
     private String compAliasTableNames;
     private String compMemberIndex;
     private String compParentChild;
 //    private String compTraversedNames;
     private boolean isCompressed;
     
     private String elementDelim = "^";
     private String groupDelim = "|^";
     
 
 	public void compressData() {
 		
 		String entry;
 		StringBuffer parentChild = new StringBuffer( 30 * memberObjects.length );
 
 		Map<String, String> memberIndexLookup = new HashMap<String, String>(memberObjects.length*2);
 		Map<String, String> parentKeyLookup = new HashMap<String, String>(memberObjects.length*2);
 		int index = 0;
 		String sIndex;
 		String parentIndex;
 		String fullChildKey;
 		String shortMemberKey;
 		String fullMemberKey;
 		String fullParentKey;
 		
 		
 		// Compress alias tables. The order of the alias table names in the aliasTableNames
 		// property, dictates how the alias keys will be sorted on every tree member properties
 		// object. The order of the alias values for each member corresponds to the alias
 		// key order (TTN-1350).
 		if (aliasTableNames != null && aliasTableNames.length > 0) {
 				compAliasTableNames = delimitArray(aliasTableNames, elementDelim);				
 		}
 					
 
 		// Build parentChild and memgerIndex strings. Traverse the member objects which
 		// are already ordered based on an in-order traversal. 
 		// 
 		// To support shared members, full keys (member prefixed by parent) are
 		// used in internal collections to keep track of each specific member's 
 		// index (surrogate key).
 		
 		for ( PafSimpleDimMember m : memberObjects ) {
 
 			// capture current index to string and increment
 			sIndex = Integer.toString(index++);
 			
 			// get short and full member key
 			shortMemberKey = m.getKey();
 			fullMemberKey = m.getParentKey() + elementDelim + shortMemberKey;
 
 			// map each full child key to full parent key index
 			for (String shortChildKey: m.getChildKeys()) {
 				fullChildKey = shortMemberKey + elementDelim + shortChildKey;
 				parentKeyLookup.put(fullChildKey, fullMemberKey);
 			}
 			// map member and string index
 			memberIndexLookup.put(fullMemberKey, sIndex);
 			
 			// get parent index
 			fullParentKey = parentKeyLookup.get(fullMemberKey);
 			if (fullParentKey != null) {
 				parentIndex = memberIndexLookup.get(fullParentKey);
 			} else {
 				parentIndex = null;
 			}
 			
 			// add record to parent/child collection (parent index, member index,
 			// generation, level, default alias, alias 1, alias 2, ......)
 			entry = delimitArray(
 					new String[] { parentIndex, sIndex, 
 					Integer.toString(m.getPafSimpleDimMemberProps().getGenerationNumber()), 
 					Integer.toString(m.getPafSimpleDimMemberProps().getLevelNumber()),}
 				, elementDelim);
 			
 			entry += elementDelim + delimitArray( m.getPafSimpleDimMemberProps().getAliasValues(), elementDelim);
 			
 			parentChild.append(entry);
 			parentChild.append(groupDelim);
 		}
 		
 		compParentChild = parentChild.toString();
 
 		// process member index lookup into string
 		StringBuffer memberIndex = new StringBuffer( 8 * memberObjects.length );
 		for (String fullKey : memberIndexLookup.keySet()) {
 			String key = fullKey.substring(fullKey.indexOf(elementDelim) + 1);
 			memberIndex.append(delimitArray(new String[] { key, memberIndexLookup.get(fullKey) }, elementDelim));
 			memberIndex.append(groupDelim);
 		}
 		
 		compMemberIndex = memberIndex.toString();
 		
 		memberObjects = null;		
 		traversedMembers = null;
 		isCompressed = true;
 		
 	}
 
 	
 	
 	
 	private String delimitArray(String [] items, String delimeter) {
 		StringBuffer sb = new StringBuffer(items.length * 8);
 		
 		for (String s : items) {
 			sb.append(s);
 			sb.append(delimeter);
 		}
 		
 		if (sb.length() > 0)
 			sb.delete(  sb.length()-delimeter.length(), sb.length() );
 		
 		return sb.toString();
 	}
 	
 	public void uncompressData() {
 		// not needed on the server side.
 	};
 	
 	
 	
     public boolean isCompressed() {
     	return isCompressed;
     }
     
     public void setCompressed(boolean isCompressed){
     	this.isCompressed = isCompressed;
     }    
     
     
     /**
 	 *	Return the PafSimpleBaseTree id
 	 *
 	 * @return Returns the PafSimpleBaseTree id
 	 */
 	public String getId() {
         return id;
     }
 	/**
 	 *	Set the PafSimpleBaseTree id
 	 *
 	 * @param id The PafSimpleBaseTree id
 	 */
     public void setId(String id) {
         this.id = id;
     }
 
 
 	/**
 	 *	Return the array of member keys in traversal order
 	 *
 	 * @return Returns the array of member keys in traversal order
 	 */
     public String[] getTraversedMembers() {
         return this.traversedMembers;
     }
 	/**
 	 *	Set the array of members in traversal order
 	 *
 	 * @param traversedMembers The array of members in traversal order to set
 	 */
     public void setTraversedMembers(String[] traversedMembers) {
         this.traversedMembers = traversedMembers;
     }
 
     /**
 	 *	Return the pafSimpleBaseTree root key
 	 *
 	 * @return Returns the pafSimpleBaseTree root key
 	 */
     public String getRootKey() {
         return rootKey;
     }
 	/**
 	 *	Set the pafSimpleBaseTree root
 	 *
 	 * @param rootKey The pafSimpleBaseTree root to set
 	 */
    public void setRootKey(String rootKey) {
         this.rootKey = rootKey;
     }
 
 	/**
 	 *	Return the alias table names
 	 *
 	 * @return Returns the alias table names
 	 */
 	public String[] getAliasTableNames() {
 		return aliasTableNames;
 	}
 	/**
 	 *	Set the alias table names
 	 *
 	 * @param aliasTableNames The alias table names to set
 	 */
 	public void setAliasTableNames(String[] aliasTableNames) {
 		this.aliasTableNames = aliasTableNames;
 	}
 	/**
 	 * @return the memberObjects
 	 */
 	public PafSimpleDimMember[] getMemberObjects() {
 		return memberObjects;
 	}
 	/**
 	 * @param memberObjects the memberObjects to set
 	 */
 	public void setMemberObjects(PafSimpleDimMember[] memberObjects) {
 		this.memberObjects = memberObjects;
 	}
	
	/**
	 * @return the isDiscontig
	 */
	public boolean isDiscontig() {
		return isDiscontig;
	}
	/**
	 * @param isDiscontig the isDiscontig to set
	 */
	public void setDiscontig(boolean isDiscontig) {
		this.isDiscontig = isDiscontig;
	}

 	public String getCompAliasTableNames() {
 		return compAliasTableNames;
 	}
 	public void setCompAliasTableNames(String compAliasTableNames) {
 		this.compAliasTableNames = compAliasTableNames;
 	}
 	public String getCompMemberIndex() {
 		return compMemberIndex;
 	}
 	public void setCompMemberIndex(String compMemberIndex) {
 		this.compMemberIndex = compMemberIndex;
 	}
 	public String getCompParentChild() {
 		return compParentChild;
 	}
 	public void setCompParentChild(String compParentChild) {
 		this.compParentChild = compParentChild;
 	}
 //	public String getCompTraversedNames() {
 //		return compTraversedNames;
 //	}
 //	public void setCompTraversedNames(String compTraversedNames) {
 //		this.compTraversedNames = compTraversedNames;
 //	}
 
 
 	public String getElementDelim() {
 		return elementDelim;
 	}
 
 
 	public void setElementDelim(String elementDelim) {
 		this.elementDelim = elementDelim;
 	}
 
 
 	public String getGroupDelim() {
 		return groupDelim;
 	}
 
 
 	public void setGroupDelim(String groupDelim) {
 		this.groupDelim = groupDelim;
 	}
 
 
 
 }
