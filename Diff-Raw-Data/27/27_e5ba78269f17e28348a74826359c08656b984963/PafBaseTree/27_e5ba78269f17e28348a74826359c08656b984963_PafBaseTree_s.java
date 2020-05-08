 /*
  *  File: @(#)PafBaseTree.java    Package: com.pace.base.mdb  Project: Paf Base Libraries
  *  Created: Aug 3, 2005          By: Alan Farkas
  *  Version: x.xx
  *
  *  Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *  This software is the confidential and proprietary information of Palladium Group, Inc.
  *  ("Confidential Information"). You shall not disclose such Confidential Information and 
  *  should use it only in accordance with the terms of the license agreement you entered into
  *  with Palladium Group, Inc.
  *
  *
  *
  Date            Author          Version         Changes
  xx/xx/xx        xxxxxxxx        x.xx            ..............
  * 
  */
 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.data.Intersection;
 
 /**
  * A Dimension Tree that holds a "real" (stored) dimension
  * @author Alan Farkas
  * @version x.xx
  */
 public class PafBaseTree extends PafDimTree {
     
 	// Associated Dimension, Attribute Mapping Level
 	private Map<String, Integer> attributeDimInfo = new HashMap<String, Integer>();
 	
 	// Performance constants
     private static final int defaultInitialCapacity = 100;
     private static final float defaultLoadFactor = (float).75;
 
     // Static logger
     private static Logger logger = Logger.getLogger(PafBaseTree.class);
     
     // Dummy constructor
     public PafBaseTree() {}
     
 	/**
      * @param root Root Node to use in initializing the PafBaseTree
 	 * @param aliasTableNames List of Essbase table names
      */
     public PafBaseTree(PafBaseMember root, String[] aliasTableNames)  {
         
         // Add root node to PafBaseMember tree using default values for initial
         // capacity and load factor
         this(root, aliasTableNames, defaultInitialCapacity, defaultLoadFactor);
     }
     
     /**
      * @param root Root Node to use in initializing the PafBaseTree
 	 * @param aliasTableNames List of Essbase table names
      * @param initialCapacity PafBaseTree HashMap initial capacity
      */
     public PafBaseTree(PafBaseMember root, String[] aliasTableNames, int initialCapacity) {
         
         // Add root node to PafBaseMember tree using default value for load factor
         this(root, aliasTableNames, initialCapacity, defaultLoadFactor);
     }
     
     /**
      * @param root Root Node to use in initializing the PafBaseTree
 	 * @param aliasTableNames List of Essbase table names
      * @param initialCapacity PafBaseTree HashMap initial capacity
      * @param loadFactor PafBaseTree HashMap initial load factor
      */
     public PafBaseTree(PafBaseMember root, String[] aliasTableNames, int initialCapacity, float loadFactor) {
         
         logger.debug ("Creating instance of PafBaseTree - root is: " + root);
 
         // Add root node to PafBaseTree
         setMembers(new HashMap<String,PafDimMember>(initialCapacity, loadFactor));
         getMembers().put(root.getKey(), root);
         setRootNode(root);
         setId(root.getKey());
         
         // Set instance variables
         setAliasTableNames(aliasTableNames);
         
         addToLvlTree(root);
         addToGenTree(root);
         
 		// Add to member property collections
         addToMbrPropCollections(root);
 
                     
     }
         
  
 	/**
 	 *	Return dim tree type
 	 *
 	 * @return DimTreeType
 	 */
 	public DimTreeType getTreeType() {
 		return DimTreeType.Base;
 	}
 
   /**
      *  Return the PafBaseTree member matching the specified key
      *
      * @param key The name of the member to look for
      * @return The specified PafAttributeTree member (type PafAttributeMember)
      */
     public PafBaseMember getMember(String key) {       
     	return (PafBaseMember) super.getMember(key);
     }
     
    /**
      *  Return the rootNode.
      *
      * @return Returns the rootNode.
      */
     public PafBaseMember getRootNode() {
     	return (PafBaseMember) super.getRootNode();
     }
     /**
      *  Set the rootNode.
      *
      * @param rootNode The rootNode to set.
      */
     protected void setRootNode(PafBaseMember rootNode) {
     	super.setRootNode(rootNode);
     }
 
     
     /**
      *  Add child node to PafBaseTree
      *
      * @param parentKey Parent of child node to add
      * @param memberKey Name of child node to add
      * @throws PafException
      */
     public void addChildByName(String parentKey, String memberKey) throws PafException {
         
         PafBaseMember pafBaseMember = new PafBaseMember(memberKey);
         addChild(parentKey, pafBaseMember);
     }
      
     /**
      *	Copy child nodes from one tree branch to tree branch to another, in the same 
      *  or different tree
      *
      * @param tree Tree that children will be added to
      * @param sourceMember Branch being copied
      * @param copyMember Branch to copy children to
      * @param lowestLvl Specifies the depth of the branch being copied
      * 
      * @throws PafException
      */
     public void addChildCopies(PafBaseTree tree, PafBaseMember sourceMember, PafBaseMember copyMember, int lowestLvl) throws PafException {
     	addChildCopies(tree, sourceMember, copyMember, lowestLvl, null);
     }
     
     /**
      *	Copy child nodes from one tree branch to tree branch to another, in the same 
      *  or different tree. Any members not in the optional member filter will be 
      *  skipped.
      * 
      * @param tree Tree that children will be added to
      * @param sourceMember Branch being copied
      * @param copyMember Branch to copy children to
      * @param lowestLvl Specifies the depth of the branch being copied
      * @param memberFilter Optional member filter
      * 
      * @throws PafException
      */
     public void addChildCopies(PafBaseTree tree, PafBaseMember sourceMember, PafBaseMember copyMember, int lowestLvl, List<String> optionalMemberFilter) throws PafException {
 
     	PafBaseMember baseChild = null, childCopy = null;
     	boolean isFiltered = false;
     	
     	// Check for member filtering
     	if (optionalMemberFilter != null && !optionalMemberFilter.isEmpty()) {
     		isFiltered = true;
     	}
     	
     	// Add children
     	for (PafDimMember child : sourceMember.getChildren() ) {
 
     		if (child.getMemberProps().getLevelNumber() >= lowestLvl) {
     			
     			// Skip member if not contained in optional member filter
     			if (isFiltered && !optionalMemberFilter.contains(child.getKey()))
     				continue;
     			
     			// Clone child
     			baseChild = (PafBaseMember) child;
     			childCopy = baseChild.getShallowDiscCopy();
   			
     			// If the copy branch is a shared hierarchy, set the share option one 
     			// each child to SHARED (TTN-1347)
     			if (copyMember.isShared()) {
     				childCopy.getMemberProps().setShareOption(copyMember.getMemberProps().getShareOption());
     			}
     			
     			// In the case of shared members, it is necessary to re-calculate the generation 
     			// property of each child as: the parent generation + 1. (TTN-1347)
     			if (copyMember.isShared()) {
     				int parentGen = copyMember.getMemberProps().getGenerationNumber();
     				//this.changeMemberGen(childCopy, parentGen + 1);
     				childCopy.getMemberProps().setGenerationNumber(parentGen + 1);
     			}
     			
     			// Add child node to branch copy
 //    			tree.addChild(copyMember.getKey(), childCopy);
     			tree.addChild(copyMember, childCopy); // TTN-1347
     			
     			// Add descendants of current child to tree (recursive call)
     			addChildCopies(tree, (PafBaseMember) child, childCopy, lowestLvl, optionalMemberFilter);           
     		}                    
     	}
     }
     
 
     /**
      *	Method_description_goes_here
      *
      * @param tree
      * @param sourceMember
      * @param copyMember
      * @param lowestGen
      * 
      * @throws PafException
      */
     private void addChildCopiesByGen(PafBaseTree tree, PafBaseMember sourceMember, PafBaseMember copyMember, int lowestGen) throws PafException {
     	PafBaseMember baseChild = null, childCopy = null;
     	
     	for (PafDimMember child : sourceMember.getChildren() ) {
     		if (child.getMemberProps().getGenerationNumber() <= lowestGen) {
     			baseChild = (PafBaseMember) child;
     			childCopy = baseChild.getShallowDiscCopy();
     			tree.addChild(copyMember, childCopy);
     			addChildCopiesByGen(tree, (PafBaseMember) child, childCopy, lowestGen);           
     		}                    
     	}
     }
     
     
     /**
      *	Return attribute information for this base dimension
      *
      * @return Map <String, Integer>
      */
     private Map<String, Integer> getAttributeDimInfo() {
     	return attributeDimInfo;
     }
 
     /**
 	 * @param attributeDimInfo the attributeDimInfo to set
 	 */
 	public void setAttributeDimInfo(Map<String, Integer> attributeDimInfo) {
 		this.attributeDimInfo = attributeDimInfo;
 	}
 
 	/**
      *	Return set of names for any attribute dimensions assigned to this base dimension
      *
      * @return Set<String>
      */
     public Set<String> getAttributeDimNames() {
 
     	Set<String> attrDimNames = new HashSet<String>();
 
     	// Get set of associated attributes assigned to base dimension;
     	if (!getAttributeDimInfo().isEmpty()) {
     		attrDimNames = getAttributeDimInfo().keySet();
     	}
 
     	// Return associated attribute dimension names, or empty set if no match is found
     	return attrDimNames;
     }
 
    /**
      *	Return level in base tree to which the specified attribute dimension has been assigned
      *
      * @param attrDimName Attribute dimension name
      * @return int
      */
     public int getAttributeMappingLevel(String attrDimName) {
     	
     	Integer mappedLevel = null;
      	
     	// Check if info exists for specified attribute dimension
     	if (!getAttributeDimInfo().containsKey(attrDimName)) {
     		String errMsg = "Unable to get level info for attribute dimension: " 
     			+ attrDimName + " - the attribute dimension is not mapped to this base tree (" 
     			+ getId() + ")";
     		logger.error(errMsg);
     		IllegalArgumentException iae = new IllegalArgumentException(errMsg);
     		throw (iae);   		
     	}
 
     	// Get mapped base dimension level
     	mappedLevel = getAttributeDimInfo().get(attrDimName);
    	
     	// Return mapped level
     	return mappedLevel;
     }
     
 	/**
      *	Return the attribute members corresponding to the specified level-0 
      * 	or upper-level base member and specified Attribute Dimension
      *
      * @param baseMemberName Base member name
      * @param attrDimName Attribute dimension name
      *
      * @return Set<String>
      */
     public Set<String> getAttributeMembers(String baseMemberName, String attrDimName)  {
         
     	Set<String> attributeMembers = new HashSet<String>();
 		PafBaseMember baseMember = null;
 		PafBaseMemberProps memberProps = null;
 
 		// Get the level in the base tree to which the specified attribute had been assigned
 		int attrLevel = getAttributeMappingLevel(attrDimName);
 
 		// Retrieve base member and get level number
 		baseMember = getMember(baseMemberName);
 		memberProps = baseMember.getMemberProps();
 		int baseMemberLevel = memberProps.getLevelNumber();
 		
 		// Is base member at the level where attributes have been defined?
 		if (baseMemberLevel == attrLevel) {
 			// If so, return attributes if they exist, else return an empty set
 			Map<String,String> associatedAttributes = memberProps.getAssociatedAttributes();
 			if (associatedAttributes.containsKey(attrDimName)) {
 				attributeMembers.add(associatedAttributes.get(attrDimName));
 			}
 			return attributeMembers;	
 		}
 		
 		// If the base member level < attribute mapping level then pass back
 		// empty set since this base member is below the level where attributes
 		// are assigned.
 		if (baseMemberLevel < attrLevel) {
 			return attributeMembers;	
 		}
 		
 		// Else, return all attribute values for descendants of current base member, 
 		// that exist at attribute mapping level
 		List<PafDimMember> descendants = getMembersAtLevel(baseMember.getKey(), (short) attrLevel);
 		for (PafDimMember descendant:descendants) {
 			PafBaseMember baseDescendant = (PafBaseMember) descendant;
 			Map<String,String> associatedAttributes = baseDescendant.getMemberProps().getAssociatedAttributes();
 			// Append attribute value for specfied attribute dimension, if it exists
 			if (associatedAttributes.containsKey(attrDimName)) {
 				attributeMembers.add(associatedAttributes.get(attrDimName));
 			}
 		}
         return attributeMembers;
     }
 
 	/**
      *	Return the valid level 0 attribute member combinations for the specified 
      *  level-0 or upper-level Base Member and specified Attribute Dimensions 
 	 *
      * @param baseMemberName Base member name
      * @param attrDimNames Attribute dimension names
      * 
 	 * @return Set<Intersection>
 	 */
 	public Set<Intersection> getAttributeCombinations(final String baseMemberName, final String[] attrDimNames) {
 
 		Set<Intersection> emptySet = new HashSet<Intersection>();
 		
 		// Throw exception, if attribute dim names is null or the array is empty
 		if ( attrDimNames == null || attrDimNames.length == 0 ) {
 			String errMsg = "getAttributeCombinations error - attribute dim names are null or empty";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		int attrDimCount = attrDimNames.length;
      	Set<Intersection> attrCombinations = new HashSet<Intersection>();
 
 		// Check if all attributes are mapped to the same base tree level
 		Integer mappingLevel = null;
 		for (String attrDimName:attrDimNames) {
 			int level = getAttributeMappingLevel(attrDimName);	
 			if (mappingLevel !=null) {
 				if (level != mappingLevel) {
 					// Mapping levels aren't consistent - return empty set
 					return emptySet;
 				} 
 			} else {
 				// First attribute dimension - initialize mapppingLevel
 				mappingLevel = level;
 			}
 		}
 
 		// Get the list of base members to which these attributes have been
 		// directly mapped to.
 		List<String> baseMemberNames = new ArrayList<String>();
 		int memberLevel = getMember(baseMemberName).getMemberProps().getLevelNumber();
 		if (memberLevel == mappingLevel) {
 			baseMemberNames.add(baseMemberName);
 		} else {
 			List<PafDimMember> baseMembers = getMembersAtLevel(baseMemberName, mappingLevel);
 			for (PafDimMember baseMember:baseMembers) {
 				baseMemberNames.add(baseMember.getKey());
 			}
 		}
 		
 		// Get the associated attributes for each base member
 		NextMember:
 			for (String memberName:baseMemberNames) {
 				//Set<String> remainingMembers = new HashSet<String>();
 				String[] attributes = new String[attrDimCount];
 				for (int i = 0; i < attrDimCount; i++) {
 					Set<String> attribute = getAttributeMembers(memberName, attrDimNames[i]);
 
 					// Skip to next member if no attributes were found for current attribute dimension
 					if (attribute.isEmpty()) {
 						continue NextMember;
 					}
 
 					attributes[i] = attribute.toArray(new String[0])[0];
 				}
 				Intersection attrCombo = new Intersection(attrDimNames, attributes);
 				attrCombinations.add(attrCombo);
 			}
 		
     	// Return valid attribute combinations
 		return attrCombinations;
 	}
 
 	/**
 	 *	Get copy of paf base tree using specified branch as root of new tree
 	 *
 	 * @param branch
 	 * @return Subtree of PafBaseTree
 	 * 
 	 * @throws PafException 
 	 */
     public PafBaseTree getSubTreeCopy(String branch) throws PafException  {
                 
         return getSubTreeCopy(branch, 0 );
     }
       
     /**
      *	Get copy of paf tree using specified branch as root of new tree
      *
      * @param branch
      * @param depth
      * @return Subtree of PafBaseTree
      * 
      * @throws PafException 
      */
     public PafBaseTree getSubTreeCopy(String branch, int depth) throws PafException  {
         PafBaseTree newTree = null;
         PafBaseMember root = getMember(branch);
 
         newTree = new PafBaseTree(root.getShallowDiscCopy(), getAliasTableNames());
         newTree.setAttributeDimInfo(getAttributeDimInfo());
 
         newTree.addChildCopies(newTree, root, newTree.getMember(branch), depth );
 
 
         return newTree;
     }
     
     /**
      *	Method_description_goes_here
      *
      * @param branch
      * @param lowestGen
      * @return PafBaseTree
      * @throws PafException 
      */
     public PafBaseTree getSubTreeCopyByGen(String branch, int lowestGen) throws PafException {
         PafBaseTree newTree = null;
         PafBaseMember root = getMember(branch);
 
         newTree = new PafBaseTree(root.getShallowDiscCopy(), getAliasTableNames());
         newTree.setAttributeDimInfo(getAttributeDimInfo());
         newTree.addChildCopiesByGen(newTree, root, newTree.getMember(branch), lowestGen );
 
         return newTree;
     }
 
 	/**
 	 * Create a discontiguous copy of this tree containing the supplied member lists
 	 * 
 	 * @param discontigMemberLists List of discontiguous member lists that map to members in this tree
 	 * @param rootName Name of root member
 	 * @param rootAlias Alias of synthetic root member
 	 * 
 	 * @return Discontiguous tree
 	 * @throws PafException 
 	 */
 	public PafBaseTree getDiscSubTreeCopy(List<List<String>> discontigMemberLists, String rootName, String rootAlias) throws PafException {
 	
 		PafBaseTree newTree = null;
 
 		// Create new root node
 		PafBaseMember newRootNode = getMember(rootName).getShallowDiscCopy();
 		PafDimMemberProps memberProps = newRootNode.getMemberProps();		
 		memberProps.setSynthetic(true);
 		memberProps.setAllAliases(getAliasTableNames(), rootAlias);
 
 		// Create new tree
 		newTree = new PafBaseTree(newRootNode, getAliasTableNames());
         newTree.setAttributeDimInfo(getAttributeDimInfo());
         newTree.setDiscontig(true);
 		
 		// Add all discontiguous branches - assume the members in each list are 
         // arranged in a post-order sort. Filter out any occurrence of the 
         // tree root, since it's already added.
         for (int i = 0; i < discontigMemberLists.size(); i++) {
        	List<String> memberList = discontigMemberLists.get(i);
         	String branchRootName = memberList.get(0);
         	if (branchRootName.equals(rootName)) {
         		memberList.remove(memberList.indexOf(rootName));
         		if (memberList.isEmpty()) continue;
         	}
         	newTree.addChildCopies(newTree, getMember(branchRootName).getParent(), newRootNode, 0, memberList );
         }
 
         return newTree;
 	}
 
 
 	/**
      *  Return a "Simple" version of the PafBaseTree
      *
      * @return PafSimpleBaseTree
      * @throws PafException 
      */
     public PafSimpleBaseTree getSimpleVersion() throws PafException  {
         
     	List<PafSimpleDimMember> simpleTraversalList = new ArrayList<PafSimpleDimMember>(); 
      	String[] aliasTableNames = new String[getAliasTableNames().length];
      	String[] traversedMembers = null;
        	
     	PafBaseMember root = null;
     	PafSimpleDimMemberProps simpleDimMemberProps = null;
 		PafSimpleDimMember simpleRoot = null;
         PafSimpleBaseTree simpleBaseTree = null;
         
         logger.debug ("Getting 'simple' version of " + this.getClass().getSimpleName() + "...");
 
         // Create Paf Simple Root Member
         root = getRootNode();
         simpleRoot = root.getSimpleVersion();
         simpleDimMemberProps = root.getMemberProps().getSimpleVersion();
 		simpleRoot.setPafSimpleDimMemberProps(simpleDimMemberProps);
 		   
         // Create Paf Simple Tree
 		logger.debug("Creating PafSimpleBaseTree with root");
 		System.arraycopy(getAliasTableNames(), 0, aliasTableNames, 0, aliasTableNames.length);
 		Arrays.sort(aliasTableNames);
 		simpleBaseTree = new PafSimpleBaseTree(getId(), simpleRoot.getKey(), aliasTableNames);
 		simpleTraversalList.add(simpleRoot);
 
 		// Walk PafBaseTree and fill PafSimpleBaseTree with simplified versions of PafBaseMember descendants
 		logger.debug("Filling PafSimpleBaseTree - Walking PafBaseTree...");
 		List<PafDimMember> children = root.getChildren();
 	    for (int i = 0; i < children.size(); i++) {
 	    	PafBaseMember child = (PafBaseMember) children.get(i);
 	    	simpleBaseTree = fillSimpleBaseMemberTree(simpleBaseTree, simpleRoot, child, i, children.size(), simpleTraversalList );
 	    }
 
 		// Put member objects into traversal order
 		simpleBaseTree.setMemberObjects(simpleTraversalList.toArray(new PafSimpleDimMember[0]));
 	    
 	    
 	    // Create member traversal array
 		logger.debug("Creating member traversal array...");
 	    traversedMembers = new String[simpleTraversalList.size()];
 	    int i = 0;
 	    for (PafSimpleDimMember simpleDimMember : simpleTraversalList) {
 	    	traversedMembers[i++] = simpleDimMember.getKey();
 	    }
 	    simpleBaseTree.setTraversedMembers(traversedMembers);
 	    
 	    
 	        
 	    // Return PafSimpleBaseTree
 		logger.debug("Returning PafSimpleBaseTree (Process Complete)");
 	    return simpleBaseTree;
        
     }
     
          
 	/**
 	 *	Recursively populate PafSimpleBaseTree with the children of the specified member
 	 *
 	 * @param simpleBaseTree Tree containing Simple Paf members
 	 * @param simpleParent Parent of current member
 	 * @param pafBaseMember Current member of PafBaseTree whose children are being traversed
 	 * @param childNo The child number associated with the current member
 	 * @param childCount Total number of children for parent member
 	 * @param simpleDimMembers HashMap of existing member names with pointers to each corresponding PafSimpleBaseMember
 	 * @param simpleTraversalList List of traversed simple members
 	 * @return PafSimpleBaseTree
 	 * @throws PafException 
 	 */
 	private PafSimpleBaseTree fillSimpleBaseMemberTree(PafSimpleBaseTree simpleBaseTree, PafSimpleDimMember simpleParent, PafBaseMember pafBaseMember, int childNo, int childCount,
 			List<PafSimpleDimMember> simpleTraversalList) throws PafException {
 
  		PafSimpleDimMember simpleDimMember = null;
     	PafSimpleDimMemberProps simpleDimMemberProps = null;
 
     	
     	// Add current member to tree
       	simpleDimMember = pafBaseMember.getSimpleVersion();
     	addSimpleChild(simpleParent, simpleDimMember, childNo, childCount);
     	simpleTraversalList.add(simpleDimMember);
     	
     	// Set Essbase properties for current member
     	simpleDimMemberProps = pafBaseMember.getMemberProps().getSimpleVersion();
     	simpleDimMember.setPafSimpleDimMemberProps(simpleDimMemberProps);
     	
     	// Iterate through children
     	List<PafDimMember> children = pafBaseMember.getChildren();
 	    for (int i = 0; i < children.size(); i++) {
 	    	PafBaseMember pafChild = (PafBaseMember) children.get(i);
     		simpleBaseTree = fillSimpleBaseMemberTree(simpleBaseTree, simpleDimMember, pafChild, i, children.size(), simpleTraversalList );
     	}
     	
 		// Return loaded PafSimpleBaseTree
 		return simpleBaseTree;
 		
 	}
 	
  	/**
 	 *	Return list of Paf Members in PafBaseTree
 	 *
 	 * @return Array of Paf Base Members
 	 */
 	public PafBaseMember[] getMemberValues() {
 
 		PafBaseMember[] memberValues = new PafBaseMember[getMembers().size()];
 		
 		getMembers().values().toArray(memberValues);
 		return (memberValues);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime
 				* result
 				+ ((attributeDimInfo == null) ? 0 : attributeDimInfo.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (!(obj instanceof PafBaseTree))
 			return false;
 		
 		// Check individual tree properties
 		PafBaseTree compare = (PafBaseTree) obj;
 		
 		// -- Check attributeDimInfo
 		if (attributeDimInfo == null) {
 			if (compare.attributeDimInfo != null)
 				return false;
 		} else if (!attributeDimInfo.equals(compare.attributeDimInfo))
 			return false;
 		
 		return true;
 	}
 
 
 }
 
