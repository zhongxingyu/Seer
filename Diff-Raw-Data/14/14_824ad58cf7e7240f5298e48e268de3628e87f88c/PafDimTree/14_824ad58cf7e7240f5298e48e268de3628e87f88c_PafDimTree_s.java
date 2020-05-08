 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 
 /**
  * An n-ary tree structure for storing multidimensional members and their associated properties.
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 /**
  * @author Alan
  *
  */
 /**
  * @author Alan
  *
  */
 public abstract class PafDimTree {
 
     // general variables
     private String id = null;
     private String[] aliasTableNames  = null;
     private Map<String, PafDimMember> members = null;
     private SortedMap<Integer, List<PafDimMember>> membersByLevel = new TreeMap<Integer, List<PafDimMember>>();   
     private SortedMap<Integer, List<PafDimMember>> membersByGen = new TreeMap<Integer, List<PafDimMember>>();       
     private List<PafDimMember> sharedMembers = null; // Null value indicates collection is uninitialized, Empty set indicates no shared members
     private PafDimMember rootNode = null;
 
     // static logger
     private static Logger logger = Logger.getLogger(PafDimTree.class);
     
    
     /**
      * Tree type enum representing all valid dimension trees
      *
      * @version	x.xx
      * @author AFarkas
      *
      */
     public enum DimTreeType {Attribute, Base}
     
     /**
      * The list of options for retrieving members from a dimension tree
      *
      * @version	x.xx
      * @author AFarkas
      *
      */
     public enum TreeTraversalMethod {
     	LEVEL_DESCENDANTS, GEN_DESCENDANTS, LEVEL, GENERATION
     }
 
     /**
      * Indicates if associated parameter represents a member level or generation
      *
      * @version	x.xx
      * @author AFarkas
      *
      */
     public enum LevelGenType {
     	LEVEL, GEN
     }
 
    /**
      *	Return the dimension tree type
      *
      * @return DimTreeType
      */
     public abstract DimTreeType getTreeType();
         
     /**
      *  Return a "Simple" version of the PafDimTree
      *
      * @return PafSimpleBaseTree
      * @throws PafException 
      */
     public abstract PafSimpleDimTree getSimpleVersion() throws PafException;
 
 	/**
      *	Get copy of paf dim tree using specified branch as root of new tree
      *
      * @param branch
      * @return Subtree of PafBaseTree
      * 
 	 * @throws PafException 
      */
     public abstract PafDimTree getSubTreeCopy(String branch) throws PafException;
     
     /**
      *	Get copy of paf dim tree using specified branch as root of new tree
      *
      * @param branch
      * @param depth
      * @return Subtree of PafDimTree
      * @throws PafException 
      */
     public abstract PafDimTree getSubTreeCopy(String branch, int depth) throws PafException;
      
 	/**
      *	Get copy of paf dim attribute tree using specified root down to specified generation
 	 *
 	 * @param root
 	 * @param lowestGen
 	 * @return PafDimTree
 	 * @throws PafException 
 	 * 
 	 */
 	public abstract PafDimTree getSubTreeCopyByGen(String root, int lowestGen) throws PafException;
 
 
  	/**
 	 *	Return the Alias table name for the specified index (0 to n-1)
 	 *
 	 * @param tableNo Essbase alias table index number  
 	 * @return Returns the Alias table name for the specified index (0 to n-1)
 	 */
 	public String getAliasTableName(int tableNo) {
 		return aliasTableNames[tableNo];
 	}
 
 	/**
 	 *	Return the list Essbase alias table names
 	 *
 	 * @return Returns the list of Essbase alias table names
 	 */
 	public String[] getAliasTableNames() {
 		return aliasTableNames;
 	}
 	/**
 	 *	Set the list of Essbase alias table names
 	 *
 	 * @param aliasTableNames The list of Essbase alias table names to set
 	 */
 	public void setAliasTableNames(String[] aliasTableNames) {
 		this.aliasTableNames = aliasTableNames;
 	}
 
     /**
      *  Get the list of shared members
      *
      * @return List<PafDimMember>
      */
     public List<PafDimMember> getSharedMembers() {
     	
     	// Rebuild this collection as needed. This list of shared members does not get 
     	// preserved in the tree cache, so it may be necessary to reproduce if the tree 
     	// was pulled directly from hibernate instead of being re-pulled from the 
     	// multidimensional database.
     	//
     	if (sharedMembers == null) {
     		logger.debug("Rebuilding the shared members list for tree rooted at: " 
     				+ this.getRootNode().getKey() + " in dimension: " + this.getId());
     		
     		// Shared members are currently only supported on base trees.
     		initSharedMembers();    		
     		if (getTreeType() == DimTreeType.Base) {  
     			// The entire list of dim members must be pulled using a tree traversal 
     			// since shared members aren't contained in the primary members hash map.
     			List<PafDimMember> allMembers = getMembers(TreeTraversalOrder.PRE_ORDER);
     			for (PafDimMember dimMember : allMembers) {
     				// Only add member to shared members collection, if it is not 
     				// contained in the members collection. In the case of sub-trees,
     				// a member that is defined in Essbase as shared could be contained
     				// in the members collection, it it was the only occurrence of 
     				// of that member in the sub-tree.
     				if (members.get(dimMember.getKey()) != dimMember) {
     				addToSharedMbrs(dimMember);
     			}
     		}
     	}
     	}
 
     	return sharedMembers;
     }
 
     /**
      *  Get the names of all shared members
      *
      * @return Set<String>
      */
     public Set<String> getSharedMemberNames() {
     	
     	Set<String> sharedMemberNames = new HashSet<String>();
     	
     	// Cycle through all shared members and add each member name
     	// to return set.
     	for (PafDimMember sharedMember : this.getSharedMembers()) {
     		sharedMemberNames.add(sharedMember.getKey());
     	}
 
     	// Return shared member names
     	return sharedMemberNames;
     }
 
 
 	/**
 	 *  Initialize shared members collection
 	 *
 	 */
 	public void initSharedMembers() {
 		// TODO Auto-generated method stub
    		sharedMembers = new ArrayList<PafDimMember>();
 	}
 
 	/**
 	 * @param sharedMembers the sharedMembers to set
 	 */
 	protected void setSharedMembers(List<PafDimMember> sharedMembers) {
 		this.sharedMembers = sharedMembers;
 	}
  
 	/**
      *  Return the children of the specified paf tree member,
      *  as well as the specified tree member. This is a convenience 
      *  method that calls getIChildren (memberName, parentFirst)
      *  with the parentFirst parm set to true.
      *
      * @param memberName Name of member to return children for
      * 
      * @return ArrayList of child paf dim members
      */
     public List<PafDimMember> getIChildren (String memberName) {       
     	return getIChildren(memberName, true);
     }
   
     /**
      *  Return the children of the specified paf tree member,
      *  as well as the specified tree member
      *
      * @param memberName Name of member to return children for
      * @param parentFirst Indicates if parent member shold be listed before it's children
      * 
      * @return ArrayList of child paf dim members
      */
     public List<PafDimMember> getIChildren (String memberName, boolean parentFirst) {
         
         // Get list of member children
         List<PafDimMember> mbrList = new ArrayList<PafDimMember>();
         mbrList.addAll(getChildren(memberName));
         
         // Add parent to member list based on value of 'parentFirst' parm
         if (parentFirst) {
             // ParentFirst is true - Add parent to top of list
         	mbrList.add(0, getMember(memberName));
         } else {
             // ParentFirst is false - Add parent to bottom of list
         	mbrList.add(getMember(memberName));
         }
         
         // Return member and its children
         return mbrList;
     }
         
  	/**
 	 *  Return the children of the specified paf dim tree member
 	 *
 	 * @param memberName Name of member to return children for
 	 * 
 	 * @return ArrayList of child paf dim members
 	 */
 	public List<PafDimMember> getChildren (String memberName) {
 		
 		PafDimMember currentNode = null;
 		
 		logger.debug("Getting children of member [" + memberName + "]");
 		
 		// Get current node
 		currentNode = getMember(memberName);
 		
 		// Return list of member children
 		return currentNode.getChildren();
 		
 	}
 
 	/**
 	 *  Return the first child of the specified paf dim tree member
 	 *
 	 * @param memberName Name of member to return the first child for
 	 * 
 	 * @return PafDimMember 
 	 */
 	public PafDimMember getFirstChild (String memberName) {
 		
 		PafDimMember firstChild = null;
 		
         logger.debug("Getting first child of member [" + memberName + "]");
                 
         // Get children
         List<PafDimMember> children = getChildren(memberName);
         
         // Return first child
         if (children.size() > 0) {
         	firstChild = children.get(0);
         }
         return firstChild;  
     }
 
     /**
      *  Return the last child of the specified paf dim tree member
      *
      * @param memberName Name of member to return the last child for
      * 
      * @return PafDimMember 
      */
     public PafDimMember getLastChild (String memberName) {
         
     	PafDimMember lastChild = null;
     	
         logger.debug("Getting first child of member [" + memberName + "]");
                 
         // Get children
         List<PafDimMember> children = getChildren(memberName);
         
         // Return first child
         if (children.size() > 0) {
         	lastChild = children.get(children.size() - 1);
         }
         return lastChild;  
     }
 
     /**
 	 *	Return the PafBaseTree id
 	 *
 	 * @return Returns the PafBaseTree id
 	 */
 	public String getId() {
         return id;
     }
 
 	/**
 	 *	Set the PafBaseTree id
 	 *
 	 * @param id The PafBaseTree id
 	 */
     public void setId(String id) {
         this.id = id;
     }
     
     /**
      *  Return the PafDimTree member matching the specified key
      *
      * @param key The name of the member to look for
      * @return The specified PafDimTree member (type PafDimMember)
      */
     public PafDimMember getMember(String key) {
         
         if (!getMembers().containsKey(key)) {
             // Member not found - throw exception
             String errMsg = "Unable to get member ["
                 + key + "] - member not found in paf dimension tree";
             logger.error(errMsg);
             IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
             throw iae;          
         }
         
         PafDimMember pafDimMember = getMembers().get(key);
         return pafDimMember;
     }
     
     /**
      * @return Returns the members.
      */
     protected Map<String,PafDimMember> getMembers() {
     	return members;
     }
 
     /**
      *	Determines if the specified member key exists in the PafDimTree
      *
      * @param key Member key
      * @return True if member exists, else False
      */
     public boolean hasMember(String key) {
     	return getMembers().containsKey(key);
     }
 
      /**
      *	Determines if this PafDimTree contains any shared members
      *
      * @return True if shared members exists
      */
     public boolean hasSharedMembers() {
     	if (this.getSharedMembers().size() > 0) {
     		return true;
     	} else {
     		return false;
     }
     }
 
    /**
     *  Set the members.
     *
     * @param members The members to set
     */
     protected void setMembers(Map<String,PafDimMember> members) {
        this.members = members;
    }
 
     /**
      *  Return the rootNode.
      *
      * @return Returns the rootNode.
      */
     public PafDimMember getRootNode() {
     	return rootNode;
     }
     /**
      *  Set the rootNode.
      *
      * @param rootNode The rootNode to set.
      */
     public void setRootNode(PafDimMember rootNode) {
     	this.rootNode = rootNode;
     }
  
  
     /**
      *  Add child member to PafDimTree
      *
      * @param parentKey Name of parent member
      * @param childNode Child member node
      * @throws PafException
      */
     public void addChild(String parentKey, PafDimMember childNode) throws PafException {
         
     	// Get parent node
     	PafDimMember parentNode = getMembers().get(parentKey);
     	if (parentNode == null) {
     		// Parent node not found - throw PafError
     		String errMsg = "Paf Exception: addChild() error - parent node: [" + parentKey + "] not found";
     		logger.error(errMsg);
     		PafException pfe = new PafException(errMsg, PafErrSeverity.Error);  
     		throw pfe;
     	}
 
     	
     	// Add child node to PafBaseTree
     	addChild(parentNode, childNode);
  
     }
     
     /**
      *  Add child member to PafDimTree
      *
      * @param parentNode Parent member node
      * @param childNode Child member node
      * @throws PafException
      */
     public void addChild(PafDimMember parentNode, PafDimMember childNode) throws PafException {
 
     	// Add child node to PafDimTree
     	parentNode.addChild(childNode);
 
     	// Add child to members HashMap if it child key doesn't already exist. Normally the members 
     	// collection would not contain any shared members. However, in a sub tree that contains an 
     	// alternate hierarchy, it is expected that the member collection would be comprised of one
     	// or more shared members.
     	String childKey = childNode.getKey(); 
     	if (!getMembers().containsKey(childKey)) {
 
     		// Add child node to members collection
     		getMembers().put(childKey, childNode);
 
 //    		// Add child node to level and generation tree
 //    		addToLvlTree(childNode);
 //    		addToGenTree(childNode);
 
     	} else {
 
     		// Child node already exists. If the new child node is not a shared member then replace
     		// the existing node with the new child node. This is to ensure that the members 
     		// collection holds the stored version of a given member, in the case where the same
     		// member occurs more than once in the tree.
     		if (!childNode.isShared()) {
 
 //    			// Delete existing node from level and generation tree
 //        		PafDimMember existingNode = getMember(childKey);
 //    			delFromLvlTree(existingNode);
 //    			delFromGenTree(existingNode);
 //
     			// Add new child node to members collection
     			getMembers().put(childKey, childNode);
 
 //    			// Add new child node to level and generation tree
 //    			addToLvlTree(childNode);
 //    			addToGenTree(childNode);    			
     		}  		
     	}
 
     			// Add new child node to level and generation tree
     			addToLvlTree(childNode);
     			addToGenTree(childNode);    			
 
 		// Add new child node to shared members collection (all members go through this logic,
     	// but only shared members will be added to collection).
 		addToSharedMbrs(childNode);
     }
     
 	/**
 	 *	Add a child to the PafSimpleDimTree
 	 *
 	 * @param simpleParent Parent of child being added
 	 * @param simpleChild PafSimpleBaseMember being added
 	 * @param childNo The child number associated with the child member being added
 	 * @param childCount Total number of children for parent member
 	 */
 	protected void addSimpleChild(PafSimpleDimMember simpleParent, PafSimpleDimMember simpleChild, int childNo, int childCount) {
 		
 		String[] childKeys = null;
 
 		logger.debug("Adding child " + simpleChild.getKey() + " to PafSimpleBaseTree...");
 
 		// Set parent node
 		simpleChild.setParentKey(simpleParent.getKey());
 		
 		// If this is the first child for the parent node, create new array of child nodes
 		if (childNo == 0)  {
 			childKeys = new String[childCount];
 		} else {
 			// Else get existing children
 			childKeys = simpleParent.getChildKeys();			
 		}
 				
 		// Add child node to parent node
 		childKeys[childNo] = simpleChild.getKey();
 		simpleParent.setChildKeys(childKeys);
 
 	}
 
 	protected void addToGenTree(PafDimMember member) {
 		int gen = member.getMemberProps().getGenerationNumber();
 		if (!membersByGen.containsKey(gen))
 			membersByGen.put(gen, new ArrayList<PafDimMember>());
 		membersByGen.get(gen).add(member);
 	}    
 
 	/**
      *  Remove member from generation tree
      *
      * @param member Tree member
      */
     private void delFromGenTree(PafDimMember member) {
 
     	// Remove member from generation tree map (if generation entry exists). 
     	// Also remove the generation entry if it no longer contains
     	// any members. (TTN-1298). 
     	int gen = member.getMemberProps().getGenerationNumber(); 
     	List<PafDimMember> genMembers = membersByGen.get(gen); 
     	if (genMembers != null) {
     		genMembers.remove(member); 
     		if (genMembers.isEmpty()) {
     			membersByGen.remove(gen);
     		}
     	}
     }
 
    protected void addToLvlTree(PafDimMember member) {
     	int lvl = member.getMemberProps().getLevelNumber();
     	if (!membersByLevel.containsKey(lvl))
     		membersByLevel.put(lvl, new ArrayList<PafDimMember>());
     	membersByLevel.get(lvl).add(member);
     }
     
    /**
     *  Remove tree member from level tree
     *
     * @param member Tree member
     */
    private void delFromLvlTree(PafDimMember member) {
 
 	   // Remove member from level tree map (if level entry exists). 
 	   // Also remove the level entry if it no longer contains any 
 	   // members. (TTN-1298). 
 	   int level = member.getMemberProps().getLevelNumber();
 	   List<PafDimMember> levelMembers = membersByLevel.get(level);
 	   if (levelMembers != null) {
 		   levelMembers.remove(member);
 		   if (levelMembers.isEmpty()) {
 			   membersByLevel.remove(level);
 		   }
 	   }
    }
 
  
    /**
      *  Add to shared members collection
      *
      * @param member
      */
     protected void addToSharedMbrs(PafDimMember member) {
     	
     	// Initialize shared member collection, if necessary
     		if (sharedMembers == null) {
 			initSharedMembers();
     		}    		
 
 		// Add member to shared members collection if it is shared
     	if (member.isShared()) {
     		sharedMembers.add(member); 
     	}
     }
 
     
 	/**
      * Change level of specified member and reposition in level tree
      *
      * @param member Tree member
      * @param newLevel New level value
      */
     public void changeMemberLevel(PafDimMember member, int newLevel)  {
 
     	// Delete member from level tree
     	delFromLvlTree(member);
     	
     	// Update level
     	member.getMemberProps().setLevelNumber(newLevel);
     	
     	// Add member back to level tree using new level value
     	addToLvlTree(member);
 
     }
     
     /**
      * Change generation of specified member and reposition in gen tree
      *
      * @param member Tree member
      * @param newGen New generation value
      */
     public void changeMemberGen(PafDimMember member, int newGen)  {
 
     	// Delete member from level tree
     	delFromGenTree(member);
     	
     	// Update generation
     	member.getMemberProps().setLevelNumber(newGen);
     	
     	// Add member back to generation tree using new generation value
     	addToGenTree(member);
 
     }
     
 
     /**
 	 *  Filter tree against set of valid members. This is a convenience 
 	 *  method for filterTree(validMemberNames, isRollupFiltered) where
 	 *  isRollupFiltered has been set to true.
 	 *
 	 * @param validMemberNames Set of valid member names
 	 * @return Pruned members (Set<String)
 	 */
 	public Set<String> filterTree(Set<String> validMemberNames) {
 		return filterTree(validMemberNames, true);
 	}
 
 	/**
 	 *  Filter tree against set of valid members
 	 *
 	 * @param validMemberNames Set of valid member names
 	 * @param isRollupFiltered Indicates if non level 0 members should be validated
 	 * 
 	 * @return Pruned members (Set<String)
 	 */
 	public Set<String> filterTree(Set<String> validMemberNames, boolean isRollupFiltered) {
 
 		Set<String> prunedMemberNames = new HashSet<String>();
 
 		// Prune invalid members from the tree. The member search
 		// list must be initialized via a tree traversal since 
 		// shared members aren't contained in the members hash map or 
 		// generation & level collections. A pre-order traversal is used 
 		// to ensure that we process the nodes in top-down, left-right, order.
 		List<PafDimMember> members = getMembers(TreeTraversalOrder.PRE_ORDER);
 		for (PafDimMember member : members) {
 			if (!validMemberNames.contains(member.getKey())) {
 				// Member is invalid
 				if (isRollupFiltered || member.getMemberProps().getLevelNumber() == 0) {
 					// Only filter member if level 0 or rollup filtering is selected
 					removeBranch(member);
 					prunedMemberNames.add(member.getKey());
 				}
 			}
 		}
 		
 		// Remove all occurrences of pruned members from shared members collection (TTN-1355)
         List<PafDimMember> sharedMembers = new ArrayList<PafDimMember>(getSharedMembers());
         for (PafDimMember sharedMember : sharedMembers) {
         	if (prunedMemberNames.contains(sharedMember.getKey())) {
         		getSharedMembers().remove(sharedMember);
         	}
         }
 		
 				
 		// Return names of pruned members
 		return prunedMemberNames;
 	}
 
 	/**
 	 *  Find any invalid members in the tree. This is a convenience
 	 *  method for filterInvalidMembers(validMemberNames, isRollupValidated) where
 	 *  isRollupValidated has been set to true.
 	 *
 	 * @param validMembers Set of valid members
 	 * @return Set<String>
 	 */
 	public Set<String> findInvalidMembers(Set<String> validMembers) {
 		return findInvalidMembers(validMembers, true);
 	}
 
 	/**
 	 *  Find any invalid members in the tree
 	 *
 	 * @param validMembers Set of valid members
 	 * @param isRollupValidated Indicates if non level 0 members should be validated
 	 * 
 	 * @return Set<String>
 	 */
 	public Set<String> findInvalidMembers(Set<String> validMembers, boolean isRollupValidated) {
 		
 		Set<String> invalidMembers = new HashSet<String>();
 		
 		for (String memberName : getMemberKeys()) {
 			if (!validMembers.contains(memberName)) {
 				// Member name is invalid
 				if (isRollupValidated || getMember(memberName).getMemberProps().getLevelNumber() == 0) {
 					// Only invalidate member if level 0 or if rollup validation is selected
 					invalidMembers.add(memberName);
 				}
 			}	
 		}
 
 		return invalidMembers;
 	}
 	
 	/**
 	 *  Return the ancestor of the specified member at the specified generation
 	 *
 	 * @param member Tree member
 	 * @param gen Member generation
 	 * 
 	 * @return PafDimMember
 	 */
 	public PafDimMember getAncestor(PafDimMember member, int gen) {
 		
          PafDimMember ancestor = member;
          int ancestorGen = ancestor.getMemberProps().getGenerationNumber();
         
          // Validate gen
          if (gen < 1) {
         	 String errMsg = "Illegal generation value of [" + gen + "] passed to getAncestor."
         	 	+ " The generation value must be greater than 0"; 
         	 throw new IllegalArgumentException(errMsg);
          }
          
          // Search for ancestor that matches the specified generation
          while (ancestorGen > gen) {
         	 ancestor = ancestor.getParent();
         	 ancestorGen = ancestor.getMemberProps().getGenerationNumber();        
          }
         
          // Return ancestor
         return ancestor;
 	}
 
 	/**
      *	Returns a list of ancestor member objects for the member name passed in.
      *  This method should not be used for shared members.
      *
      * @param memberName
      * @return List<PafDimMember>
      */
     public List<PafDimMember> getAncestors(String memberName) {
         
         List<PafDimMember> anc = new ArrayList<PafDimMember>();
         PafDimMember current = getMember(memberName);
         
         if (current.getParent() == null) 
             return anc;
         
         current = current.getParent();
         
         anc.add(current);
         while (current.getParent() != null) {
             current = current.getParent();
             anc.add(current);
         }
         
         return anc;
     }
 
     /**
      *	Returns a list of ancestor member objects for the member passed in.
      *  This method supports shared members.
      *
      * @param member
      * @return List<PafDimMember>
      */
     public List<PafDimMember> getAncestors(PafDimMember member) {
         
         List<PafDimMember> anc = new ArrayList<PafDimMember>();
         PafDimMember current = member;
         
         if (current.getParent() == null) 
             return anc;
         
         current = current.getParent();
         
         anc.add(current);
         while (current.getParent() != null) {
             current = current.getParent();
             anc.add(current);
         }
         
         return anc;
     }
 
     /**
 	 *	Return the list of members that are needed to get a cumulative
 	 *  total for the selected member. This is a convenience method
 	 *  that calls getCumMembers(memberName, level) with the "level"
 	 *  parameter set to 0.
 	 * 
 	 * @param memberName Member name
 	 *
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getCumMembers(String memberName) {
 		return getCumMembers(memberName, 0);
 	}
 	
 	/**
 	 *	Return the list of members that are needed to get a cumulative
 	 *  total for the selected member at the specified level
 	 * 
 	 * @param memberName Member name
 	 * @param cumLevel The member level to use for generating the list of cum members
 	 *
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getCumMembers(String memberName, int cumLevel) {
 						
 		List<PafDimMember> cumMembers = null;
 		PafDimMember lastCumMember = null;
 		
 		// Get member properties
 		PafDimMember member = getMember(memberName);
 		PafDimMemberProps memberProps = member.getMemberProps();
 		int memberLevel = memberProps.getLevelNumber();
 			
 		// Check for negative cum level
 		if (cumLevel < 0) {
 			String errMsg = "getCumMembers Error - An negative cum level of [" + cumLevel 
 						+ "] was specfied";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Determine the last member in cum range. 
 		if (memberLevel <= cumLevel) {
 			// If member level = cum level, then last member in cum range = selected member.
 			//
 			// A Cum level that is greater than the level of the selected member is invalid 
 			// and will be handled as if the cum level = the cum level of the selected member.
 				lastCumMember = member;
 		} else {
 			// Else set the last member in cum range as the last descendant at specified level
 			List<PafDimMember> descendants = getDescendants(member.getKey(), LevelGenType.LEVEL, cumLevel);
 			lastCumMember = descendants.get(descendants.size() - 1);
 		} 
 		
 		// Return all members in cum range, at the specified level (left peers)
 		cumMembers = getILPeers(lastCumMember.getKey());
 		return cumMembers;
 	}
 
 
 	/**
 	 *	Return the list of members that are at the same level as the specified member,
 	 *  as well as the specified member
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getIPeers(String memberName) {
 		
 		// Get level of specified member
 		PafDimMember member = getMember(memberName);
 		int level = member.getMemberProps().getLevelNumber();
 		
 		// Return list of peers (all members at same level)
 		List<PafDimMember> peers = getMembersAtLevel(getRootNode().getKey(), (short) level);
 		return peers;
 	}
 
 	/**
 	 *	Return the list of members that are at the same level as the specified member
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getPeers(String memberName) {
 		
 		// Get list of peers
 		List<PafDimMember> peers = getIPeers(memberName);
 		
 		// Remove member from list
 		peers.remove(getMember(memberName));
 		
 		// Return remaining peers
 		return peers;
 	}
 
 	/**
 	 *	Return the list of members that are at the same level as the specified member
 	 *  but come before it in the hierarchy, followed by the specified member
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getILPeers(String memberName) {
 		
 		List<PafDimMember> peers = getLPeers(memberName);
 		peers.add(getMember(memberName));
 		return peers;
 	}
 
 	/**
 	 *	Return the list of members that are at the same level as the specified member
 	 *  but come before it in the hierarchy.
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getLPeers(String memberName) {
 			
 		// Get a list of all peers
 		List<PafDimMember> peers = getIPeers(memberName);
        
         // Prune list down to just those that come before specified member
 		int memberIndex = peers.indexOf(getMember(memberName));
		List<PafDimMember> LPeers = peers.subList(0, memberIndex);
 		return LPeers;
 	}
 
 	/**
 	 *	Return the specified member followed by the list of members that are at
 	 *  the same level as the specified member
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getIRPeers(String memberName) {
 		
 		List<PafDimMember> peers = getRPeers(memberName);
 		peers.add(0, getMember(memberName));
 		return peers;
 	}
 
 	/**
 	 *	Get the nth peer of the specified member.
 	 *
 	 * @param memberName Member name
 	 * @param index Signed integer that indicates the index of the desired peer
 	 * 
 	 * @return List of Paf Dim Members
 	 */
 	public PafDimMember getPeer(String memberName, int index) {
 		return getPeer(memberName, index, false);
 	}
 
 	/**
 	 *	Get the nth peer of the specified member. A null value will be returned
 	 *  if the index points to an out of bounds location and bWrap is set
 	 *  to false.
 	 *
 	 * @param memberName Member name
 	 * @param index Signed integer that indicates the index of the desired peer
 	 * @param bWrap Indicates if this method should wrap around the list of peers to get the desired member
 	 * 
 	 * @return Peer member
 	 */
 	public PafDimMember getPeer(String memberName, int index, boolean bWrap) {
 						
 		int offset = 0;
 		List<PafDimMember> peers = null;
 
 		
 		// An index of 0 returns the current member
 		if (index == 0) {
 			return getMember(memberName);
 		}
 		
 		// Get a list of peers based on sign of index and 
 		// set member offset appropriately
 		if (index > 0) {	
 			// Index > 0 - Use forward (right) peers
 			peers = getRPeers(memberName);
 			offset = index - 1;
 			
 			// Check for out of bounds condition
 			int peerCount = peers.size();
 			if (offset > peerCount - 1) {
 				if (bWrap) {
 					peers = getIPeers(memberName);
 					offset = offset % peers.size();
 				} else {
 					return null;
 				}
 			}			
 		} else {			
 			// Index < 0 - Use previous (left) peers
 			peers = getLPeers(memberName);
 			offset = peers.size() + index;
 			
 			// Check for out of bounds condition
 			int peerCount = peers.size();
 			if (offset < 0) {
 				if (bWrap) {
 					peers = getIPeers(memberName);
 					offset = peerCount + (offset % peerCount);
 				} else {
 					return null;
 				}
 			}			
 		}
         
 		// Return desired peer
 		return peers.get(offset);
 	}
 
 	/**
 	 *	Return the list of members that are at the same level as the specified member
 	 *  but come after it in the hierarchy.
 	 *
 	 * @param memberName Member name
 	 * @return List of Paf Dim Members
 	 */
 	public List<PafDimMember> getRPeers(String memberName) {
 			
 		// Get a list of all peers
 		List<PafDimMember> peers = getIPeers(memberName);
         
         // Prune list down to just those that come after specified member
		int memberIndex = peers.indexOf(getMember(memberName));
 		List<PafDimMember> RPeers = new ArrayList<PafDimMember>();
		RPeers = peers.subList(memberIndex +1, peers.size());
 		return RPeers;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param member
 	 * @return PafDimMember
 	 */
 	public PafDimMember getPrevSibling(PafDimMember member) {
 		return getPrevSibling(member, false);
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param member
 	 * @param wrapAround
 	 * @return PafDimMember
 	 */
 	public PafDimMember getPrevSibling(PafDimMember member, boolean wrapAround) {
 		// if no parent this is the root, and can't have any siblings
 		if (member.getParent() == null) return null;
 		
 		// try the easy way...
 		List<PafDimMember> siblings = member.getParent().getChildren();
 		int index = siblings.indexOf(member);
 		if (index > 0)
 			return siblings.get(index-1);
 		
 		// hard way, needs optimization in the at level method
 		siblings = this.getMembersAtGen(getRootNode().getKey(), (short) member.getMemberProps().getGenerationNumber());
 		index = siblings.indexOf(member);
 		if (index > 0)
 			return siblings.get(index-1);		
 		
 		// boundary condition, if wraparound return last term
 		if (wrapAround) return siblings.get(siblings.size()-1);
 		
 		return null;
 	}
 
 	
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param member
 	 * @return PafDimMember
 	 */
 	public PafDimMember getNextSibling(PafDimMember member) {
 		return getNextSibling(member, false);
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param member
 	 * @param wrapAround
 	 * @return PafDimMember
 	 */
 	public PafDimMember getNextSibling(PafDimMember member, boolean wrapAround) {
 		// if no parent this is the root, and can't have any siblings
 		if (member.getParent() == null) return null;
 		
 		// try the easy way...
 		List<PafDimMember> siblings = member.getParent().getChildren();
 		int index = siblings.indexOf(member);
 		if (index < (siblings.size() - 1))
 			return siblings.get(index+1);
 		
 		// hard way, needs optimization in the at level method
 		siblings = this.getMembersAtGen(getRootNode().getKey(), (short) member.getMemberProps().getGenerationNumber());
 		index = siblings.indexOf(member);
 		if (index < (siblings.size() - 1))
 			return siblings.get(index+1);		
 		
 		// boundary condition, if wraparound return last term
 		if (wrapAround) return siblings.get(0);
 		
 		return null;
 	}
 
 	/**
      *  Return all descendant members of the selected branch, as well as the 
      *  selected branch. This method is a convenience method that calls 
      *  getIDescendants(branchName, level) with the parentFirst property set to true.
      *
      * @param branchName Name of tree branch to get descendants for
      * @return ArrayList of descendant paf dim members
      */
     public ArrayList<PafDimMember> getIDescendants(String branchName) {
         
         // Return list of descendants
         return getIDescendants(branchName, true);
         
     }
     
     /**
      *  Return all descendant members of the selected branch, as well as the 
      *  selected branch. This method is a convenience method that calls 
      *  getIDescendants(branchName, levelGen, parentFirst) with the "levelGenType"
      *  set to "LEVEL" and the "levelGen" parameter set to 0.
      *
      * @param branchName Name of tree branch to get descendants for
      * @param parentFirst Indicates if parent member(s) shold be listed before their children
      * 
      * @return ArrayList of descendant paf dim members
      */
     public ArrayList<PafDimMember> getIDescendants(String branchName, boolean parentFirst) {
         
         // Return list of descendants
         return getIDescendants(branchName, LevelGenType.LEVEL, 0, parentFirst);
         
     }
     
     /**
      *  Return descendant members of selected branch down to specified level
      *  or generation, as well as  the selected branch. This is a convenience method that calls
      *  getIDescendants(branchName, levelGen, parentFirst) with the 'parentFirst'
      *  property set to true.
      *
      * @param branchName Name of tree branch to get descendants for
      * @param levelGenType Indicates if level or generation is used for member filter
      * @param levelGen Bottom level/generation of selected members
      * 
      * @return ArrayList of descendant paf dim members
      */
     public ArrayList<PafDimMember> getIDescendants(String branchName, LevelGenType levelGenType, int levelGen) {
     	return getIDescendants(branchName, levelGenType, levelGen, true);  
     }
 
     /**
      *  Return descendant members of selected branch down to specified level
      *  or generation, as well as  the selected branch.
      *
      * @param branchName Name of tree branch to get descendants for
      * @param levelGenType Indicates if level or generation is used for member filter
      * @param levelGen Bottom level/generation of selected members
      * @param parentFirst Indicates if parent member(s) shold be listed before their children
      *
      * @return ArrayList of descendant paf dim members
      */
     public ArrayList<PafDimMember> getIDescendants(String branchName, LevelGenType levelGenType, int levelGen, boolean parentFirst) {
         
         // Get list of descendants
         ArrayList<PafDimMember> mbrList = new ArrayList<PafDimMember>();
         mbrList.addAll(getDescendants(branchName, levelGenType, levelGen, parentFirst));
         
         // Add branch member to member list based on value of 'parentFirst' parm
         if (parentFirst) {
             // ParentFirst is true - Add branch to top of list
         	mbrList.add(0, getMember(branchName));
         } else {
             // ParentFirst is false - Add branch to bottom of list
         	mbrList.add(getMember(branchName));
         }
 
         // Return list of descendants
         return mbrList;        
     }
     
     /**
      *  Return all descendants of root node of tree. This method is a convenience method
      *  that calls getDescendants(branchName) with the "branchName" parameter set 
      *  to "rootNode.getKey()".
      *
      * @param branchName Name of tree branch to get descendants for
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getDescendants() {
          return getDescendants(rootNode.getKey());       
     }
     
     /**
      *  Return all descendant members of selected branch. This method is a convenience method
      *  that calls getDescendants(branchName, parentFirst) with the "parentFirst" parameter set 
      *  to true.
      *
      * @param branchName Name of tree branch to get descendants for
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getDescendants(String branchName) {
          return getDescendants(branchName, true);       
     }
     
     /**
      *  Return all descendant members of selected branch. This method is a convenience method
      *  that calls getDescendants(branchName, levelGen, parentFirst) with the "levelGenType" 
      *  parameter set to "LEVEL" and the "level" parameter set to 0.
      *
      * @param branchName Name of tree branch to get descendants for
      * @param parentFirst Indicates if parent member shold be listed before it's children
      * 
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getDescendants(String branchName, boolean parentFirst) {
         return getDescendants(branchName, LevelGenType.LEVEL, 0, parentFirst);
     }
 
     /**
      *  Return descendant members of selected branch down to specified level/generation. This
      *  is a convenience method that calls getDescendants(branchName, levelGen, levelGenType, parentFirst)
      *  with the 'parentFirst' parm set to true.
      *
      * @param branchName Name of tree branch to get descendants for
      * @param levelGenType Indicates if level or generation is used for member filter
      * @param levelGen Bottom level or generation of selected members
      * 
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getDescendants(String branchName, LevelGenType levelGenType, int levelGen) {
     	return getDescendants(branchName, levelGenType, levelGen, true);
     }
 
     /**
      *  Return descendant members of selected branch down to specified level/generation
      *
      * @param branchName Name of tree branch to get descendants for
      * @param levelGenType Indicates if level or generation is used for member filter
      * @param levelGen Bottom level or generation of selected members
      * @param parentFirst Indicates if parent member shold be listed before it's children
      * 
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getDescendants(String branchName, LevelGenType levelGenType, int levelGen, boolean parentFirst) {
         
         List<PafDimMember> memberList = null;
         TreeTraversalMethod traversalMethod = null;
         logger.debug("Getting descendants of member [" + branchName + "] down to "
         		+ levelGenType + ": " + levelGen);
         
         // Check if branch exists
         getMember(branchName);
         
         // Set tree traversal method
         if (levelGenType == LevelGenType.LEVEL) {
         	traversalMethod = TreeTraversalMethod.LEVEL_DESCENDANTS;
         } else {
         	traversalMethod = TreeTraversalMethod.GEN_DESCENDANTS;
         }
         
         // Traverse tree
         memberList = treeTraversal(getMembers().get(branchName), new ArrayList<PafDimMember>(), traversalMethod, levelGen, parentFirst);
         
         // Return list of descendants
         return memberList;
         
     }
     
     /**
      *  Traverse paf member tree and return list of members based
      *  on selected traversal method. 
      *
      * @param member Current PafDimMember
      * @param memberList Pending list of traversed members
      * @param traversalMethod Type of member traversal to perform
      * @param levelGen Target member level/generation
      * @param parentFirst Indicates if parent member shold be listed before it's children (only applies only to 'descendant' traversal methods)
      * 
      * @return Member list
      * @throws IllegalArgumentException 
      */
     private List<PafDimMember> treeTraversal(PafDimMember member, List<PafDimMember> memberList, TreeTraversalMethod traversalMethod, int levelGen, boolean parentFirst) throws IllegalArgumentException {
         
     	// Validate genLevel parameter
     	if (traversalMethod == TreeTraversalMethod.LEVEL || traversalMethod == TreeTraversalMethod.LEVEL_DESCENDANTS) {
     		// Level type traversal
     		if (levelGen < 0) {
     			String errMsg = "Invalid [levelGen] value of: " + levelGen + " - level must be greater than or equal to 0";
     			logger.error(errMsg);
     			throw (new IllegalArgumentException(errMsg));
     		}
     	}  else {
     		// Generation type traversal
     		if (levelGen < 1) {
     			String errMsg = "Invalid [levelGen] value of: " + levelGen + " - generation must be greater than 0";
     			logger.error(errMsg);
     			throw (new IllegalArgumentException(errMsg));
     		}
     	}
  
 
     	// Perform appropriate tree traversal based on selected traversal method
         switch (traversalMethod) {
         case LEVEL_DESCENDANTS: 
         	for (PafDimMember child : member.getChildren() ) {
         		int memberLevel = child.getMemberProps().getLevelNumber();
         		if (memberLevel >= levelGen) {
         			// ParentFirst is true - show parent before child
         			if (parentFirst) {
         				memberList.add(child);
         			}
         			
         			// Current member's level is greater than requested level - get all the descendants
         			if (memberLevel > levelGen) {
         				memberList = treeTraversal(child, memberList, traversalMethod, levelGen, parentFirst);                         
         			}
         			
         			// ParentFirst is false - show parent after child
         			if (!parentFirst) {
         				memberList.add(child);
         			}
         		}
         	}
         	break;
         case GEN_DESCENDANTS: 
         	for (PafDimMember child : member.getChildren() ) {
         		int memberGen = child.getMemberProps().getGenerationNumber();
         		if (memberGen <= levelGen) {
         			// ParentFirst is true - show parent before child
         			if (parentFirst) {
         				memberList.add(child);
         			}
         			
         			// Current member's generation is less than requested generation - get all the descendants
         			if (memberGen < levelGen) {
         				memberList = treeTraversal(child, memberList, traversalMethod, levelGen, parentFirst);                         
         			}
         			
         			// ParentFirst is false - show parent after child
         			if (!parentFirst) {
         				memberList.add(child);
         			}
         		}
         	}
         	break;
         case LEVEL: 
         	int memberLevel = member.getMemberProps().getLevelNumber();
         	if (memberLevel == levelGen) {
         		// Current member is at the same level as the request level - just return member
         		memberList.add(member);
         	} else if (memberLevel > levelGen) {
     			// Current member's level is greater than requested level - get members at the requested level, under each child 
         		for (PafDimMember child : member.getChildren() ) {
         			memberList = treeTraversal(child, memberList, traversalMethod, levelGen, parentFirst);                         
         		}
         	}
         	break;
         case GENERATION:
         	int memberGen = member.getMemberProps().getGenerationNumber();
         	if (memberGen == levelGen) {
         		// Current member is at the same generation as the request generation - just return member
         		memberList.add(member);
         	} else if (memberGen < levelGen) {
     			// Current member's generation is lower than requested generation - get members at the requested generation, under each child 
         		for (PafDimMember child : member.getChildren() ) {
         			memberList = treeTraversal(child, memberList, traversalMethod, levelGen, parentFirst);                         
         		}
         	}
         	break;      	
         }
         
         // Return list of members
         return memberList;
     }
        
     /**
      *  Return the first descendant of the specified paf dim tree member at the specified level
      *
      * @param memberName Name of member to return the first descendant for
      * @param level Level number of descendant
      * 
      * @return PafDimMember 
      */
  	public PafDimMember getFirstDescendant(String memberName, short level) {
  		
  		PafDimMember firstDescendant = null;
  		
  		logger.debug("Getting first descendant of member [" + memberName + "] at level [" + level +"]");
  		
  		// Get descendants
  		List<PafDimMember> descendants = getMembersAtLevel(memberName, level);
  		
  		// Return first descendant
  		if (descendants.size() > 0) {
  			firstDescendant = descendants.get(0);
  		}
  		return firstDescendant;  
  	}
 
     /**
      *  Return the last descendant of the specified paf dim tree member at the specified level
      *
      * @param memberName Name of member to return the first descendant for
      * @param level Level number of descendant
      * 
      * @return PafDimMember 
      */
  	public PafDimMember getLastDescendant(String memberName, short level) {
  		
  		PafDimMember lastDescendant = null;
  		
  		logger.debug("Getting last descendant of member [" + memberName + "] at level [" + level +"]");
  		
  		// Get descendants
  		List<PafDimMember> descendants = getMembersAtLevel(memberName, level);
  		
  		// Return last descendant
  		if (descendants.size() > 0) {
  			lastDescendant = descendants.get(descendants.size() - 1);
  		}
  		return lastDescendant;  
  	}
  
  
 	/**
 	 *	Return list of member names defined in PafBaseTree
 	 *
 	 * @return Array of member names
 	 */
 	public String[] getMemberKeys() {	
 		
 		String[] memberKeys = new String[getMembers().size()];
 		
 		getMembers().keySet().toArray(memberKeys);
 		return (memberKeys);
 	}
 
 	
 	/**
 	 * Return the number of members in the tree
 	 * 
 	 * @return the number of members in the tree
 	 */
 	public int getTreeSize() {
 		return members.size();
 	}
 	
 	/**
      *  Return the descendants of selected branch at the specified generation
      *
      * @param branchName Name of tree branch to get descendants for
      * @param gen Generation of selected members
      * 
      * @return ArrayList of descendant paf dim tree members
      */
     public List<PafDimMember> getMembersAtGen(String branchName, int gen) {
         
         List<PafDimMember> memberList = null;
         
         logger.debug("Getting all members at generation: " + gen + " under member [" + branchName + "]");
         // Check if branch exists
         getMember(branchName);
         
         // Traverse tree
         memberList = treeTraversal(getMembers().get(branchName), new ArrayList<PafDimMember>(), TreeTraversalMethod.GENERATION, gen, true);
         
         // Return list of descendants
         return memberList;
         
     }
 
     public SortedMap<Integer, List<PafDimMember>> getMembersByGen() {
         if (membersByGen.size() == 0) {  
             logger.info("Rebuilding member by generation tree for tree rooted at: " + this.getRootNode().getKey());
             // new members have been added since last requested or freshly retrieved from
             // cached database version which can't preserve this structure easily
             // regenerate
             for (PafDimMember m : getMembers().values()) {
                 addToGenTree(m);
             }
         }
         
         return membersByGen;
     }
 
     /**
      *  Return the descendants of selected branch at the specified level
      *
      * @param branchName Name of tree branch to get descendants for
      * @param level Level of selected members
      * @return ArrayList of descendant paf dim members
      */
     public List<PafDimMember> getMembersAtLevel(String branchName, int level) {
         
         List<PafDimMember> memberList = null;
         
         logger.debug("Getting all members at level: " + level + " under member [" + branchName + "]");
         // Check if branch exists
         getMember(branchName);
         
         // Traverse tree
         memberList = treeTraversal(getMembers().get(branchName), new ArrayList<PafDimMember>(), TreeTraversalMethod.LEVEL, level, true);
         
         // Return list of descendants
         return memberList;
         
     }
     
     /**
      *	Returns the members at the lowest level in the tree, who are
      *  descendants of the specified branch.
      *
      * @param branchName
      * @return List of paf dim members
      */
     public List<PafDimMember> getMembersAtLowestLevel(String branchName) {
         
         List<PafDimMember> memberList = null;
         
         short level = (short) this.getLowestAbsLevelInTree();
         
         logger.debug("Getting all members at level: " + level + " under member [" + branchName + "]");
         // Check if branch exists
         getMember(branchName);
         
         // Traverse tree
         memberList = treeTraversal(getMembers().get(branchName), new ArrayList<PafDimMember>(), TreeTraversalMethod.LEVEL_DESCENDANTS, level, true);
         
         // Return list of descendants
         return memberList;
         
     }    
     
     
   /**
      *  Returns all the member names from the PafBaseTree using the 
      *  specified tree traversal method. This is a convience method that 
      *  calls getMemberNames(traversalOrder, level) with the "level"
      *  parameter set to 0
      *
      * @param traversalOrder Pre-order or post-order
      * @return List of member names 
      */
     public List<String> getMemberNames(TreeTraversalOrder traversalOrder) {
         
         return getMemberNames(traversalOrder, 0);
     }
     
     /**
      *  Returns all the member names from the PafBaseTree down to the
      *  specified level using the specified tree traversal method
      *
      * @param traversalOrder Pre-order or post-order
      * @param level Maximum level of returned members
      * @return List of member names 
      */
     public List<String> getMemberNames(TreeTraversalOrder traversalOrder, int level) {
         
         List<String> memberList = null;
         
         logger.debug("Getting member names...");
         memberList = memberTraversal(getRootNode(), new ArrayList<String>(getMembers().size()), traversalOrder, level, "" );
         
         return memberList;
     }
     
     /**
      *  Converts a list of paf dim members to a list of member names
      *  
      * @param dimMembers List of paf dim members
      * @return List of member names
      */
     public List<String> getMemberNames(List<PafDimMember> dimMembers) {
     	
     	List<String> memberList = new ArrayList<String>();
     	for (PafDimMember dimMember : dimMembers) {
     		memberList.add(dimMember.getKey());
     	}
     	
     	return memberList;
     }
     
     
     /**
      *  Returns all the member aliases from the PafBaseTree using the 
      *  specified tree traversal method. This is a convenience method that 
      *  calls getMemberAliases(traversalOrder, aliasTableName) with the 
      *  "aliasTableName" parameter set to "Default"
      *
      * @param traversalOrder Pre-order or post-order
      * @return List of member aliases
       */
     public List<String> getMemberAliases(TreeTraversalOrder traversalOrder) {
         
         return getMemberAliases(traversalOrder, "Default");
     }
     
     /**
      *  Returns all the member aliases from the PafBaseTree using the 
      *  specified tree traversal method. This is a convenience method that 
      *  calls getMemberAliases(traversalOrder, aliasTableName, level) with
      *  the "level" parameter set to 0
      *
      * @param traversalOrder Pre-order or post-order
      * @param aliasTableName Alias table to use for member aliases
      * @return List of member aliases 
      */
     public List<String> getMemberAliases(TreeTraversalOrder traversalOrder, String aliasTableName) {
         
         return getMemberAliases(traversalOrder, aliasTableName, 0);
     }
     
     /**
      *  Returns all the member aliases from the PafBaseTree using the 
      *  specified tree traversal method. This is a convenience method that 
      *  calls getMemberAliases(traversalOrder, aliasTableName, level) with
      *  the "aliasTableName" parameter set to "Default"
      *
      * @param traversalOrder Pre-order or post-order
      * @param level Maximum level of returned members
      * @return List of member names 
      */
     public List<String> getMemberAliases(TreeTraversalOrder traversalOrder, int level) {
         
         return getMemberAliases(traversalOrder, "Default", level);
     }
  
     /**
      *  Returns all the member aliases from the PafBaseTree down to the
      *  specified level using the specified tree traversal method
      *
      * @param traversalOrder Pre-order or post-order
      * @param aliasTableName Alias table to use for member aliases
      * @param level Maximum level of returned members
      * @return List of member aliases
      */
     public List<String> getMemberAliases(TreeTraversalOrder traversalOrder, String aliasTableName, int level) {
         
         List<String> memberList = null;
         
         // Validate aliasTableName
         if (!validateAliasTable(aliasTableName)) {
         	// throw illegal argument exception
         	String errMsg = "Error encountered while attempting to get member aliases - Alias table name of: ["
         		+ aliasTableName + "] is invalid";
         	IllegalArgumentException iae = new IllegalArgumentException(errMsg);
         	throw iae;
         }
         logger.debug("Getting member aliases...");
         memberList = memberTraversal(getRootNode(), new ArrayList<String>(getMembers().size()), traversalOrder, level, aliasTableName);
         
         return memberList;
     }
        
 	/**
      *  Perform a traversal of the PafDimTree returning the member
      *  names using the selected traversal method. If an alias table name
      *  is specified, then member aliases from that table will be returned
      *  instead.
      *
      * @param curentNode PafDimTree node to traverse
      * @param memberList ArraryList containing pending list of traversed members 
      * @param traversalOrder Tree traversal order (Pre-Order / Post-Order) 
      * @param level Maximum level of returned members
      * @param aliasTableName Alias table to use for pulling member aliases
      * @return ArrayList of members or member aliases in selected traversal order
      */
     private List<String> memberTraversal(PafDimMember currentNode, List<String> memberList, TreeTraversalOrder traversalOrder, int level, String aliasTableName) {
         
     	String member = null;
         List<PafDimMember> children = null;
  
         // Determine current member name/alias
     	if (aliasTableName.equals("")) {
     		member = currentNode.getKey(); 
     	} else {
     		member = currentNode.getMemberProps().getMemberAlias(aliasTableName);
     	}
 
         // Pre-Order Logic
         if (traversalOrder == TreeTraversalOrder.PRE_ORDER) {
            	// Add current member name/alias to member list
         	memberList.add(member); 
         }
         
         // If specified level has not been reached, iterate through any children
         if (currentNode.getMemberProps().getLevelNumber() > level) {
             // Get children
             children = currentNode.getChildren();
             if (children.size() > 0) {
                 for (PafDimMember child:children) {
                     memberList = memberTraversal(child, memberList, traversalOrder, level, aliasTableName);
                 }
             }
         }
         
         // Post-Order Logic
         if (traversalOrder == TreeTraversalOrder.POST_ORDER) {
         	// Add current member name/alias to member list
         	memberList.add(member); 
         }
         
         // Return pending member list
         return memberList;
     }
     
     
     
     /**
      * 
      * This method performs a traversal starting at the member specified in the current tree.
      * It then returns the lowest level members under the specified branch. This is the equivalent
      * of a "relative" level 0 call. In the case that the branch has no children it returns itself
      * 
      * 
      * @param branchName
      * @return List<PafDimMember>
      */
     public List<PafDimMember> getLowestMembers(String branchName) {
     	
     	List<PafDimMember> members = new ArrayList<PafDimMember>();
     	List<PafDimMember> floorMembers = new ArrayList<PafDimMember>();
     	
     	PafDimMember start = this.getMember(branchName);
     	members = getMembers(start, members, TreeTraversalOrder.POST_ORDER);
     	
     	for (PafDimMember m : members) {
     		if (!m.hasChildren())
     			floorMembers.add(m);
     	}
     	return floorMembers;
     }
     
     /**
      * 
      * This method performs a traversal starting at the member specified in the current tree.
      * It then returns the lowest level members under the specified branch. This is the equivalent
      * of a "relative" level 0 call. In the case that the branch has no children it returns itself
      * 
      * 
      * @param branchName
      * @return List<String>
      */
 	public List<String> getLowestMemberNames(String branchName) {
 
 		List<PafDimMember> members = new ArrayList<PafDimMember>();
     	List<String> floorMembers = new ArrayList<String>();
     	
     	PafDimMember start = this.getMember(branchName);
     	members = getMembers(start, members, TreeTraversalOrder.POST_ORDER);
     	
     	for (PafDimMember m : members) {
     		if (!m.hasChildren())
     			floorMembers.add(m.getKey());
     	}
     	return floorMembers;
 	}
 
 
 	/**
      *  Perform a traversal of the PafDimTree returning the member
      *  names using the selected traversal method. 
      *
       * @param traversalOrder Tree traversal order (Pre-Order / Post-Order / In-Order) 
      * 
      * @return ArrayList of members in selected traversal order
      */
     public List<PafDimMember> getMembers(TreeTraversalOrder traversalOrder) {
     	return getMembers(this.rootNode, new ArrayList<PafDimMember>(), traversalOrder);
     }
 
 	/**
      *  Perform a traversal of the PafDimTree returning the member
      *  names using the selected traversal method. 
      *
      * @param curentNode PafDimTree node to traverse
      * @param traversalOrder Tree traversal order (Pre-Order / Post-Order / In-Order) 
      * 
      * @return ArrayList of members in selected traversal order
      */
     public List<PafDimMember> getMembers(PafDimMember currentNode, TreeTraversalOrder traversalOrder) {
     	return getMembers(currentNode, new ArrayList<PafDimMember>(), traversalOrder);
     }
 
     /**
      *  Perform a traversal of the PafDimTree returning the member
      *  names using the selected traversal method. 
      *
      * @param curentNode PafDimTree node to traverse
      * @param memberList ArraryList containing pending list of traversed members 
      * @param traversalOrder Tree traversal order (Pre-Order / Post-Order / In-Order) 
      * 
      * @return ArrayList of members in selected traversal order
      */
     private List<PafDimMember> getMembers(PafDimMember currentNode, List<PafDimMember> memberList, TreeTraversalOrder traversalOrder) {
         
     	PafDimMember member = null;
         List<PafDimMember> children = null;
  
 
     	member = currentNode; 
 
         // Pre-Order Logic
         if (traversalOrder == TreeTraversalOrder.PRE_ORDER) {
            	// Add current member name/alias to member list
         	memberList.add(member); 
         }
         
         // Get children
         children = currentNode.getChildren();
         if (children.size() > 0) {
             for (PafDimMember child:children) {
                 memberList = getMembers(child, memberList, traversalOrder);
             }
         }
 
         // Post-Order Logic
         if (traversalOrder == TreeTraversalOrder.POST_ORDER) {
         	// Add current member name/alias to member list
         	memberList.add(member); 
         }
         
         // Return pending member list
         return memberList;
     }
     
     
     
     /**
      *	Validate the alias table name
      *
      * @param aliasTableName
      * @return True if alias table name is valid
      */
     protected boolean validateAliasTable(String aliasTableName) {
 
     	for (String tableName: getAliasTableNames()) {
     		if (tableName.equals(aliasTableName))
     			return true;
     	}
     	return false;
     }
 
     /**
      *	Method_description_goes_here
      *
      * @return SortedMap<Integer, List<PafDimMember>>
      */
     public SortedMap<Integer, List<PafDimMember>> getMembersByLevel() {
     	if (membersByLevel.size() == 0) {  
     		logger.info("Rebuilding member by level tree for tree rooted at: " + this.getRootNode().getKey());
     		// new members have been added since last requested or freshly retrieved from
     		// cached database version which can't preserve this structure easily
     		// regenerate
     		for (PafDimMember m : getMembers().values()) {
     			addToLvlTree(m);
     		}
     	}
 
     	return membersByLevel;
     }
 
     /**
      *	Method_description_goes_here
      *
      * @return int
      */
     public int getHighestAbsLevelInTree() {
     	return membersByLevel.lastKey();
     }
 
     /**
      *	Method_description_goes_here
      *
      * @return int
      */
     public int getLowestAbsLevelInTree() {
     	return membersByLevel.firstKey();
     }
 
     /**
      *	Method_description_goes_here
      *
      * @return List<PafDimMember>
      */
     public List<PafDimMember> getLowestLevelMembers() {
     	return membersByLevel.get(membersByLevel.firstKey());
     }
 
     /**
      *	Return lower member generation referenced in tree.
      *
      * @return int
      */
     public int getLowestGenInTree() {
     	return membersByGen.firstKey();
     }
 
     /**
      *	Return highest member generation referenced in tree.
      *
      * @return int
      */
     public int getHighestGenInTree() {
     	return membersByGen.lastKey();
     }
 
    /**
 	 *  Return highest ancestor (below the root) of the specified member
 	 *
 	 * @param member Tree member
 	 * @return PafDimMember
 	 */
 	public PafDimMember getHighestAncestor(PafDimMember member) {
 		return getAncestor(member, 2);
 	}
 
 /**
      *	Method_description_goes_here
      *
      * @param branchKey
      */
     public void removeBranch(String branchKey) {
     	PafDimMember mbrToRemove = getMembers().get(branchKey);
 
     	// silently succeed, an optional fail on missing signature might add value here.
     	if (mbrToRemove == null) return;
     	
     	// recursively call on children
     	if (mbrToRemove.hasChildren()) {
     		// build iterable list copy of children to avoid concurrent modification issues
     		List<PafDimMember> children = new ArrayList<PafDimMember>();
     		children.addAll(mbrToRemove.getChildren());
     		for (PafDimMember mbrChild : children) {
     			removeBranch(mbrChild.getKey());
     		}
     	}
 
     	// now I'm a leaf for sure
     	removeLeaf(mbrToRemove);
     }
 
     /**
      *	Removed specified branch from dim tree
      *
      * @param branchKey
      */
     public void removeBranch(PafDimMember branch) {
 
      	// recursively call on children
     	if (branch.hasChildren()) {
     		// build iterable list copy of children to avoid concurrent modification issues
     		List<PafDimMember> children = new ArrayList<PafDimMember>();
     		children.addAll(branch.getChildren());
     		for (PafDimMember mbrChild : children) {
     			removeBranch(mbrChild);
     		}
     	}
 
     	// now I'm a leaf for sure
     	removeLeaf(branch);
     }
 
     /**
      *	Method_description_goes_here
      *
      * @param leafMbr Member to remove
      */
     private void removeLeaf(PafDimMember leafMbr) {
     	
        	String memberName = leafMbr.getKey();
 
        	// Remove from parents collection.
     	if (leafMbr.getParent() != null) {
     		leafMbr.getParent().getChildren().remove(leafMbr);
     		leafMbr.setParent(null);
     	}
 
     	// Remove leaf member from generation tree map (TTN-1298)
     	delFromGenTree(leafMbr);
 
        	// Remove leaf member from level tree map (TTN-1298)
     	delFromLvlTree(leafMbr);
     	    	
 		// Remove from member tree map. Because of shared member support,
     	// multiple leafs can share the same member key.
      	if (members.containsKey(memberName)) {
     		if (members.get(memberName) == leafMbr) {
     			members.remove(memberName);
     		}
     	}
     }
     
     /**
      *	Prune branch and its ancestors
      *
      * @param branch
      */
     public void pruneAncestors(PafDimMember branch) {
 
     	// Remove any branch with no children. Recursively call
     	// on all ancestors. Parent must be gotten before the 
     	// call to removeBranch, since removeBranch nulls out the
     	// parent property.
     	if (!branch.hasChildren()) {
     		PafDimMember parent = branch.getParent();
     		removeBranch(branch);
     		if (parent != null) {
     			pruneAncestors(parent);
     		}
     	}
     }
 
 
 
     /*
      *	Return the members of the PafDimTree using a pre-order traversal
      *
      * @see java.lang.Object#toString()
      */
     public String toString() {
 
     	StringBuffer stringBuffer = new StringBuffer("\n");	
      	List<PafDimMember> members = getMembers(TreeTraversalOrder.PRE_ORDER);
     	for (PafDimMember member : members) {
     		// indentation routine
     		int gen = member.getMemberProps().getGenerationNumber();
     		for (int i = 0; i < gen; i++)
     			stringBuffer.append("\t");
     		stringBuffer.append(member + "\n");
     	}	
     	return stringBuffer.toString();
     }
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(aliasTableNames);
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((members == null) ? 0 : members.hashCode());
 		result = prime * result
 				+ ((membersByGen == null) ? 0 : membersByGen.hashCode());
 		result = prime * result
 				+ ((membersByLevel == null) ? 0 : membersByLevel.hashCode());
 		result = prime * result
 				+ ((rootNode == null) ? 0 : rootNode.hashCode());
 		result = prime * result
 				+ ((sharedMembers == null) ? 0 : sharedMembers.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof PafDimTree))
 			return false;
 		
 		// Check individual tree properties
 		PafDimTree compare = (PafDimTree) obj;
 		
 		// -- Check id
 		if (id == null) {
 			if (compare.id != null)
 				return false;
 		} else if (!id.equals(compare.id))
 			return false;
 		
 		// -- Check treeType
 		if (getTreeType() != compare.getTreeType())
 			return false;
 		
 		// -- Check aliasTableNames
 		if (!Arrays.equals(aliasTableNames, compare.aliasTableNames))
 			return false;
 		
 		// -- Check rootNode
 		if (rootNode == null) {
 			if (compare.rootNode != null)
 				return false;
 		} else if (!rootNode.equals(compare.rootNode))
 			return false;
 		
 		// -- Check members
 		if (members == null) {
 			if (compare.members != null)
 				return false;
 		} else if (!members.equals(compare.members))
 			return false;
 
 		// -- Check membersByGen
 		if (membersByGen == null) {
 			if (compare.membersByGen != null)
 				return false;
 		} else if (!membersByGen.equals(compare.membersByGen)) {
 			if (compare.membersByGen == null) {
 				return false;
 			} else if (!membersByGen.keySet().equals(compare.membersByGen.keySet())) {
 				return false;
 			} else {
 				// Check contents at each generation. Ordering differences will be ignored
 				// since they don't impact functionality.
 				for (int gen : membersByGen.keySet()) {
 					List<PafDimMember>members = membersByGen.get(gen);
 					List<PafDimMember>compMembers = compare.membersByGen.get(gen);
 					if (members != compMembers) {
 						if (!members.containsAll(compMembers) || !compMembers.containsAll(members)) {
 							return false;
 						} 
 					}
 				}
 			}
 		}
 		
 		// -- Check membersByLevel
 		if (membersByLevel == null) {
 			if (compare.membersByLevel != null)
 				return false;
 		} else if (!membersByLevel.equals(compare.membersByLevel)) {
 			if (compare.membersByLevel == null) {
 				return false;
 			} else if (!membersByLevel.keySet().equals(compare.membersByLevel.keySet())) {
 				return false;
 			} else {
 				// Check contents at each level. Ordering differences will be ignored
 				// since they don't impact functionality.
 				for (int lvl : membersByLevel.keySet()) {
 					List<PafDimMember>members = membersByLevel.get(lvl);
 					List<PafDimMember>compMembers = compare.membersByLevel.get(lvl);
 					if (members != compMembers) {
 						if (!members.containsAll(compMembers) || !compMembers.containsAll(members)) {
 							return false;
 						} 
 					}
 				}
 			}
 		}
 		
 		// -- Check sharedMembers
 //		if (sharedMembers == null) {
 //			if (compare.sharedMembers != null)
 //				return false;
 //		} else if (!sharedMembers.equals(compare.sharedMembers))
 //			return false;
 		if (!getSharedMembers().equals(compare.getSharedMembers())) {
 			return false;
 		}
 		
 		return true;
 	}
 
 }
