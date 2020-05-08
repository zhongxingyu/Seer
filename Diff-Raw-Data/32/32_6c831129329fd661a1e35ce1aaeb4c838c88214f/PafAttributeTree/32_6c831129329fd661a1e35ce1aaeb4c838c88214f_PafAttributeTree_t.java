 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 
 /**
  * A Dimension Tree that holds an Attribute Dimension
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class PafAttributeTree extends PafDimTree {
 
 	//	general variables
 	private short attributeType = 0;
 	private String baseDimName = null;
     
     // perfomance constants
     static final int defaultInitialCapacity = 100;
     static final float defaultLoadFactor = (float).75;
 
     // static logger
     private static Logger logger = Logger.getLogger(PafBaseTree.class);
     
     // dummy constructor
     public PafAttributeTree() {}
     
 	/**
      * @param root Root Node to use in initializing the PafAttributeTree
 	 * @param aliasTableNames List of Essbase table names
      */
     public PafAttributeTree(PafAttributeMember root, String[] aliasTableNames)  {
         
         // Add root node to PafBaseMember tree using default values for initial
         // capacity and load factor
         this(root, aliasTableNames, defaultInitialCapacity, defaultLoadFactor);
     }
     
     /**
      * @param root Root Node to use in initializing the PafAttributeTree
 	 * @param aliasTableNames List of Essbase table names
      * @param initialCapacity PafBaseTree HashMap initial capacity
      */
     public PafAttributeTree(PafAttributeMember root, String[] aliasTableNames, int initialCapacity) {
         
         // Add root node to PafBaseMember tree using default value for load factor
         this(root, aliasTableNames, initialCapacity, defaultLoadFactor);
     }
     
     /**
      * @param root Root Node to use in initializing the PafAttributeTree
 	 * @param aliasTableNames List of Essbase table names
      * @param initialCapacity PafBaseTree HashMap initial capacity
      * @param loadFactor PafBaseTree HashMap initial load factor
      */
     public PafAttributeTree(PafAttributeMember root, String[] aliasTableNames, int initialCapacity, float loadFactor) {
         
         logger.info ("Creating instance of PafAttributeTree");
  
         // Add root node to PafBaseTree
         setMembers(new HashMap<String,PafDimMember>(initialCapacity, loadFactor));
         getMembers().put(root.getKey(), root);
         setRootNode(root);
         setId(root.getKey());
         
         // Set instance variables
         setAliasTableNames(aliasTableNames);
         
         addToLvlTree(root);
         addToGenTree(root);
                     
     }
         
  
 	/**
 	 *	Return dim tree type
 	 *
 	 * @return DimTreeType
 	 */
 	public DimTreeType getTreeType() {
 		return DimTreeType.Attribute;
 	}
 	/**
  	 *	Return the attributeType.
  	 *
  	 * @return Returns the attributeType.
  	 */
  	public short getAttributeType() {
 		return attributeType;
 	}
 	/**
 	 *	Set the attributeType.
 	 *
 	 * @param attributeType
 	 */
 	public void setAttributeType(short attributeType) {
 		this.attributeType = attributeType;
 	}
 
 	/**
 	 *	Return the baseDimName.
 	 *
 	 * @return Returns the baseDimName.
 	 */
 	public String getBaseDimName() {
 		return baseDimName;
 	}
 	/**
 	 *	Set the baseDimName.
 	 *
 	 * @param baseDimName Corresponding base dimension name
 	 */
 	public void setBaseDimName(String baseDimName) {
 		this.baseDimName = baseDimName;
 	}
 
     /**
      *  Return the PafAttributeTree member matching the specified key
      *
      * @param key The name of the member to look for
      * @return The specified PafAttributeTree member (type PafAttributeMember)
      */
     public PafAttributeMember getMember(String key) {
         
     	return (PafAttributeMember) super.getMember(key);
     }
  
     /**
      *  Return the rootNode.
      *
      * @return Returns the rootNode.
      */
     public PafAttributeMember getRootNode() {
     	return (PafAttributeMember) super.getRootNode();
     }
     /**
      *  Set the rootNode.
      *
      * @param rootNode The rootNode to set.
      */
     protected void setRootNode(PafAttributeMember rootNode) {
     	super.setRootNode(rootNode);
     }
 
 
     /**
      *  Add child node to PafAttributeTree
      *
      * @param parentKey Parent of child node to add
      * @param memberKey Name of child node to add
      * @throws PafException
      */
     public void addChildByName(String parentKey, String memberKey) throws PafException {
         
     	PafAttributeMember pafAttributeMember = new PafAttributeMember(memberKey);
         addChild(parentKey, pafAttributeMember);
     }
          	
     /**
      *	Method_description_goes_here
      *
      * @param tree
      * @param sourceMember
      * @param copyMember
      * @param lowestLvl
      * @throws PafException
      */
     private void addChildCopies(PafAttributeTree tree, PafAttributeMember sourceMember, PafAttributeMember copyMember, int lowestLvl) throws PafException {
     	PafAttributeMember attrChild = null, childCopy = null;
         
         for (PafDimMember child : sourceMember.getChildren() ) {
             if (child.getMemberProps().getLevelNumber() >= lowestLvl) {
                 childCopy = attrChild.getShallowDiscCopy();
                 tree.addChild(copyMember.getKey(), childCopy);
                 addChildCopies(tree, (PafAttributeMember) child, childCopy, lowestLvl);           
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
 	 * @throws PafException
 	 */
 	private void addChildCopiesByGen(PafAttributeTree tree, PafAttributeMember sourceMember, PafAttributeMember copyMember, int lowestGen) throws PafException {
 		PafAttributeMember attrChild = null, childCopy = null;
 
 		for (PafDimMember child : sourceMember.getChildren() ) {
 			if (child.getMemberProps().getGenerationNumber() <= lowestGen) {
 				attrChild = (PafAttributeMember) child;
 				childCopy = attrChild.getShallowDiscCopy();
 				tree.addChild(copyMember.getKey(), childCopy);
 				addChildCopiesByGen(tree, (PafAttributeMember) child, childCopy, lowestGen);           
 			}                    
 		}
 	}
 
 	/**
      *	Get copy of paf attribute tree using specified branch as root of new tree
      *
      * @param branch
      * @return Subtree of PafBaseTree
      */
     public PafAttributeTree getSubTreeCopy(String branch)  {
     	
     	return getSubTreeCopy(branch, 0 );
     }
     
     /**
      *	Get copy of paf attribute tree using specified branch as root of new tree
      *
      * @param branch
      * @param depth
      * @return Subtree of PafAttributeTree
      */
     public PafAttributeTree getSubTreeCopy(String branch, int depth)  {
     	PafAttributeTree newTree = null;
         try {
             newTree = new PafAttributeTree(this.getMember(branch).getShallowDiscCopy(), getAliasTableNames());
             
             newTree.addChildCopies(newTree, this.getMember(branch), newTree.getMember(branch), depth );
         }
         catch (Exception ex) {
             logger.warn("Failure making clone");
             // TODO decide what to do with PafException in getSubTreeCopy
             // may now be runtime exception
         }
         
         return newTree;
     }
     
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param root
 	 * @param lowestGen
 	 * @return PafAttributeTree
 	 */
 	public PafAttributeTree getSubTreeCopyByGen(String root, int lowestGen) {
 		PafAttributeTree newTree = null;
 		try {
 			newTree = new PafAttributeTree(this.getMember(root).getShallowDiscCopy(), getAliasTableNames());
 
 			newTree.addChildCopiesByGen(newTree, this.getMember(root), newTree.getMember(root), lowestGen );
 		}
 		catch (Exception ex) {
 			logger.warn("Failure making clone");
 			// TODO decide what to do with PafException in getSubTreeCopy
 			// may now be runtime exception
 		}
 
 		return newTree;
 	}
 
 
 	/**
 	 *	Return the list of base member names corresponding to the specified attribute member
 	 *
 	 * @param attrMemberName Attribute member name
 	 * @return Set<PafBaseMember>
 	 */
 	public Set<String> getBaseMemberNames(String attrMemberName) {
 		
 		Set<String> baseMemberNames = new HashSet<String>();
 		PafAttributeMember attrMember = null;
 		PafAttributeMemberProps memberProps = null;
 
 		// Validate attribute member name
 		attrMember = getMember(attrMemberName);
 		
 		// Get member properties
 		memberProps = attrMember.getMemberProps();
 		
 		// If the specified attribute is at level 0 then retrieve the set of
 		// associated base members
 		if (memberProps.getLevelNumber() == 0) {
 			baseMemberNames = memberProps.getBaseMembers();
 		} else {
 			// Else append the all base members assigned to each level 0 attribute member descendant
 			List<PafDimMember> levelZeroAttributes = getMembersAtLevel(attrMember.getKey(), (short) 0);
 			for (PafDimMember attribute:levelZeroAttributes) {
 				baseMemberNames.addAll(getBaseMemberNames(attribute.getKey()));
 			}
 		}
 
 		// Return attribute dimension names, or empty set if no match is found
 		return baseMemberNames;
 	}
 
 
 	/**
 	 *  Return a "Simple" version of the PafAttribute Tree
 	 *
 	 * @return PafSimpleAttributeTree
 	 * @throws PafException 
 	 */
 	public PafSimpleAttributeTree getSimpleVersion() throws PafException  {
 
 		List<PafSimpleDimMember> simpleTraversalList = new ArrayList<PafSimpleDimMember>(); 
      	String[] aliasTableNames = new String[getAliasTableNames().length];
      	String[] traversedMembers = null;
        	
     	PafAttributeMember root = null;
     	PafSimpleDimMemberProps simpleDimMemberProps = null;
 		PafSimpleDimMember simpleRoot = null;
         PafSimpleAttributeTree simpleAttributeTree = null;
         
         logger.debug ("Getting 'simple' version of " + this.getClass().getSimpleName() + "...");
 
         // Create Paf Simple Root Member
         root = getRootNode();
         simpleRoot = root.getSimpleVersion();
         simpleDimMemberProps = root.getMemberProps().getSimpleVersion();
 		simpleRoot.setPafSimpleDimMemberProps(simpleDimMemberProps);
 		   
         // Create Paf Simple Tree
 		logger.debug("Creating PafSimpleAttributeTree with root");
 		System.arraycopy(getAliasTableNames(), 0, aliasTableNames, 0, aliasTableNames.length); 
 		Arrays.sort(aliasTableNames); 	// Sort alias table names in alpha-order proper view rendering (TTN-1350)
 		simpleAttributeTree = new PafSimpleAttributeTree(getId(), simpleRoot.getKey(), aliasTableNames);
 		simpleTraversalList.add(simpleRoot);
 
		// Add non-complex properties. (TTN-1748)
		simpleAttributeTree.setDiscontig(isDiscontig());
		
 		// Walk PafAttributeTree and fill PafSimpleAttributeTree with simplified versions of PafBaseMember descendants
 		logger.debug("Filling PafSimpleAttributeTree - Walking PafAttributeTree...");
 		List<PafDimMember> children = root.getChildren();
 	    for (int i = 0; i < children.size(); i++) {
 	    	PafAttributeMember child = (PafAttributeMember) children.get(i);
 	    	simpleAttributeTree = fillSimpleAttributeTree(simpleAttributeTree, simpleRoot, child, i, children.size(), simpleTraversalList );
 	    }
 
 		// Put member objects into traversal order
 	    simpleAttributeTree.setMemberObjects(simpleTraversalList.toArray(new PafSimpleDimMember[0]));
 	    
 	    // Create member traversal array
 		logger.debug("Creating member traversal array...");
 	    traversedMembers = new String[simpleTraversalList.size()];
 	    int i = 0;
 	    for (PafSimpleDimMember simpleDimMember : simpleTraversalList) {
 	    	traversedMembers[i++] = simpleDimMember.getKey();
 	    }
 	    simpleAttributeTree.setTraversedMembers(traversedMembers);
 	    
 	    // Return PafSimpleAttributeTree
 		logger.debug("Returning PafSimpleAttributeTree (Process Complete)");
 	    return simpleAttributeTree;
        
     }
            
 	/**
 	 *	Recursively populate PafSimpleAttributeTree with the children of the specified member
 	 *
 	 * @param simpleAttributeTree Tree containing Simple Paf members
 	 * @param simpleParent Parent of current member
 	 * @param pafAttributeMember Current member of PafBaseTree whose children are being traversed
 	 * @param childNo The child number associated with the current member
 	 * @param childCount Total number of children for parent member
 	 * @param simpleTraversalList List of traversed simple members
 	 * @return PafSimpleAttributeTree
 	 * @throws PafException 
 	 */
 	private PafSimpleAttributeTree fillSimpleAttributeTree(PafSimpleAttributeTree simpleAttributeTree, PafSimpleDimMember simpleParent, PafAttributeMember pafAttributeMember, int childNo, int childCount,
 			List<PafSimpleDimMember> simpleTraversalList) throws PafException {
 
  		PafSimpleDimMember simpleDimMember = null;
     	PafSimpleDimMemberProps simpleDimMemberProps = null;
 
     	
     	// Add current member to tree
       	simpleDimMember = pafAttributeMember.getSimpleVersion();
     	addSimpleChild(simpleParent, simpleDimMember, childNo, childCount);
     	simpleTraversalList.add(simpleDimMember);
     	
     	// Set Essbase properties for current member
     	simpleDimMemberProps = pafAttributeMember.getMemberProps().getSimpleVersion();
     	simpleDimMember.setPafSimpleDimMemberProps(simpleDimMemberProps);
     	
     	// Iterate through children
     	List<PafDimMember> children = pafAttributeMember.getChildren();
 	    for (int i = 0; i < children.size(); i++) {
 	    	PafAttributeMember pafChild = (PafAttributeMember) children.get(i);
     		simpleAttributeTree = fillSimpleAttributeTree(simpleAttributeTree, simpleDimMember, pafChild, i, children.size(), simpleTraversalList );
     	}
     	
 		// Return loaded PafSimpleBaseTree
 		return simpleAttributeTree;
 		
 	}
 		
 	/**
 	 *	Return list of Paf Members in PafAttributeTree
 	 *
 	 * @return Array of Paf Attribute Members
 	 */
 	public PafAttributeMember[] getMemberValues() {
 
 		PafAttributeMember[] memberValues = new PafAttributeMember[getMembers().size()];
 		
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
 		result = prime * result + attributeType;
 		result = prime * result
 				+ ((baseDimName == null) ? 0 : baseDimName.hashCode());
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
 		if (!(obj instanceof PafAttributeTree))
 			return false;
 		
 		// Check tree properties
 		PafAttributeTree compare = (PafAttributeTree) obj;
 		
 		// -- Check attributeType
 		if (attributeType != compare.attributeType)
 			return false;
 		
 		// -- Check baseDimName
 		if (baseDimName == null) {
 			if (compare.baseDimName != null)
 				return false;
 		} else if (!baseDimName.equals(compare.baseDimName))
 			return false;
 		
 		return true;
 	}
 	      
 
 }
