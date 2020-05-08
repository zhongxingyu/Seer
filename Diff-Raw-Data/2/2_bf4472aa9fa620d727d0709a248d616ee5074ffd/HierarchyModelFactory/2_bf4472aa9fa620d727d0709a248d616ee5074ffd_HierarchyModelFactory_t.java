 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package org.caesarj.ui.views.hierarchymodel;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.aspectj.asm.IHierarchy;
 import org.aspectj.asm.IProgramElement;
 import org.caesarj.compiler.asm.CaesarProgramElement;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.runtime.AdditionalCaesarTypeInformation;
 import org.caesarj.ui.resources.CaesarJPluginResources;
 import org.caesarj.ui.util.CaesarJClassUtil;
 
 /**
  * This factory is responsible for creating the Hierarchy models for the hierarchy view.
  * 
  * It has methods for creating both the Tree model and the List model.
  * 
  * 
  * @author Thiago Tonelli Bartolomei <bart@macacos.org>
  * 
  */
 public class HierarchyModelFactory {
 
 	/**
 	 * The extension appended to implementation classes for cclasses
 	 */
 	public static final String IMPL_EXTENSION = "_Impl";
 	
 	protected IHierarchy structureModel = null;
 	
 	protected String filename = null;
 	
 	protected String classname = null;
 	
 	protected String outputdir = null;
 	
 	boolean implicitFilter = false;
 	
 	boolean isSuperView = false;
 	
 	/**
 	 * The utility class for reading caesar classes
 	 */
 	protected CaesarJClassUtil util = null;
 	
 	/**
 	 * Private constructor to prevent instantiation
 	 */
 	private HierarchyModelFactory() {
 	}
 	
 	/**
 	 * Constructs a new Model Factory for building Tree models.
 	 * Initializes the CaesarJClassUtil instance;
 	 * 
 	 * @param structureModel
 	 * @param filename
 	 * @param outputdir
 	 * @param implicitFilter
 	 * @param isSuperView
 	 */
 	protected HierarchyModelFactory(IHierarchy structureModel, String filename, String outputdir, boolean implicitFilter, boolean isSuperView) {
 		this.structureModel = structureModel;
 		this.filename = filename;
 		this.outputdir = outputdir;
 		this.implicitFilter = implicitFilter;
 		this.isSuperView = isSuperView;
 		// Create a util to read classes
 		util = new CaesarJClassUtil(outputdir);
 	}
 	
 	/**
 	 * Constructs a new Model Factory for building List models.
 	 * Initializes the CaesarJClassUtil instance;
 	 * 
 	 * @param classname
 	 * @param outputdir
 	 */
 	protected HierarchyModelFactory(String classname, String outputdir) {
 		this.classname = classname;
 		this.outputdir = outputdir;
 		// Create a util to read classes
 		util = new CaesarJClassUtil(outputdir);
 	}
 	
 	/**
 	 * Creates a factory instance and uses this instance to build a new TreeModel.
 	 * This method never throws exceptions nor returns null. If an error in the building
 	 * process occurs, it returns a RootNode with kind EMPTY.
 	 * 
 	 * @param structureModel
 	 * @param filename
 	 * @param outputdir
 	 * @param implicitFilter
 	 * @param superView
 	 * @return
 	 */
 	public static RootNode createHierarchyTreeModel(IHierarchy structureModel, String filename, String outputdir, boolean implicitFilter, boolean isSuperView) {
 		HierarchyModelFactory instance = new HierarchyModelFactory(structureModel, filename, outputdir, implicitFilter, isSuperView);
 		try {
 			return instance.buildHierarchyTreeModel();
 		} catch (Exception e) {
 			e.printStackTrace();
 			// This method should never throw exceptions, but return an empty node
 			return new RootNode(HierarchyNode.EMPTY);
 		}
 	}
 	
 	/**
 	 * Creates a factory instance and uses this instance to build a new ListModel.
 	 * This method never throws exceptions nor returns null. If an error in the building
 	 * process occurs, it returns a LinearNode with kind EMPTY.
 	 * 
 	 * @param classname
 	 * @param outputdir
 	 * @return
 	 */
 	public static LinearNode createHierarchyListModel(String classname, String outputdir) {
 		HierarchyModelFactory instance = new HierarchyModelFactory(classname, outputdir);
 		try {
 			return instance.buildHierarchyListModel();
 		} catch (Exception e) {
 			// This method should never throw exceptions, but return an empty node
 			return new LinearNode(HierarchyNode.EMPTY);
 		}
 	}
 	
 	/**
 	 * Creates a list of nodes with the mixin classes found in the file specified by
 	 * te directory and classname. The string "dir + separator + file" must contain the absolute
 	 * file name for the class file.
 	 * 
 	 * This method can throw runtime exceptions. It is very prudent to catch it.
 	 * 
 	 * @return the head node for the mixin list
 	 */
 	protected LinearNode buildHierarchyListModel() {
 		
 		// Create the Caesar Class (CCLass) Object
     	CClass clazz = CaesarJClassUtil.loadCClass(outputdir, classname);
 
     	// Using the CClass object we can get the information about the types
     	AdditionalCaesarTypeInformation info = clazz.getAdditionalTypeInformation();
 
     	// Get the mixin list for the class
     	String[] mixinList = info.getMixinList();
     	
     	// If the list doesn't contain mixins, return null (that will cause the view to display a message)
     	if (mixinList.length <= 0) {
     		return null;
     	}
     	
     	// Iterate the list creating a double linked list of nodes
     	
     	// the first node is created now
     	LinearNode node1 = new LinearNode();
 		node1.setKind(HierarchyNode.LIST);
 		node1.setName(mixinList[0]);
 		LinearNode node2 = new LinearNode();
 		// Iterate. Note that we start from 1, because we already have the first node
 		for (int i = 1; mixinList.length > i; i++) {
 			node2 = node1;
 			node1 = new LinearNode();
 			node2.setPreNode(node1);
 			node1.setNextNode(node2);
 			node1.setKind(HierarchyNode.LIST);
 			node1.setName(mixinList[i]);
 		}
 		
 		// In the end, the node 1 is the head of the list
 		return node1;
 	}
 	
 	/**
 	 * Creates the hierarchy tree model and returns the root node
 	 * 
 	 * @return
 	 */
 	protected RootNode buildHierarchyTreeModel() {
 
 		// Get the sources
 		Vector sources = getSources();
 		
 		// Creates the root node, which keeps all nodes
 		RootNode root = new RootNode();
 		root.setKind(HierarchyNode.ROOT);
 		StandardNode node = null;
 
 		// Iterate on the sources
 		Iterator i = sources.iterator();
 		while(i.hasNext()) {
 			// Set the next source
 			String src = (String) i.next();
 			
 			// Read the class and get info
 			CClass clazz = util.loadCClass(src);
 			AdditionalCaesarTypeInformation info = clazz.getAdditionalTypeInformation();
 			
 			// Create the node for the class
 			StandardNode classNode = new StandardNode();
 			classNode.setKind(HierarchyNode.CLASS);
 			classNode.setName(info.getQualifiedName());
 			classNode.setTypeInformation(info);
 			classNode.checkFurtherBinding();
 			
 			// Add the class node to the root
 			classNode.setParent(root);
 			root.addChild(classNode);
 			
 			// Get the superclasses
 			String[] superClasses = info.getSuperClasses();
 			
 			if (superClasses.length > 0) {
 				// If there is a super class, create the "super icon"	
 				StandardNode superNode = new StandardNode(
 						HierarchyNode.PARENTS,
 						CaesarJPluginResources.getResourceString("HierarchyView.hierarchy.superClass"),
 						classNode);
 				
 				// For each super class, create a node and add to the supernode
 				for (int j = 0; superClasses.length > j; j++) {
 					// Create the node
 					node = new StandardNode();
 					node.setKind(HierarchyNode.SUPER);
 					node.setName(superClasses[j]);
 					node.setTypeInformation(util.loadCClass(superClasses[j]).getAdditionalTypeInformation());
 					
 					// add to supernode
 					node.setParent(superNode);
 					superNode.addChild(node);
 				}
 			}
 			
 			// Get the nested classes
 			String[] nestedClasses = info.getNestedClasses();
 			
 			// Sort the nested classes to be sure they appear in alphabetical order (string compare) 
 			Arrays.sort(nestedClasses);
 			
 			if (nestedClasses.length > 0) {
 				// If there is a super class, create the "contains icon"
 				StandardNode nestedNode;
 				if (isSuperView) {
 					nestedNode = new StandardNode(
 						HierarchyNode.PARENTS,
 						CaesarJPluginResources.getResourceString("HierarchyView.hierarchy.containsSuper"),
 						classNode);	
 				} else {
 					nestedNode = new StandardNode(
 							HierarchyNode.PARENTS,
 							CaesarJPluginResources.getResourceString("HierarchyView.hierarchy.containsSub"),
 							classNode);					
 				}
 
 				// For each super class, create a node and add to the supernode
 				for (int j = 0; nestedClasses.length > j; j++) {
 					// Read the class and get info
 					clazz = util.loadCClass(nestedClasses[j]);
 					AdditionalCaesarTypeInformation nestedInfo = clazz.getAdditionalTypeInformation();
 					nestedNode.setTypeInformation(nestedInfo);
 					
 					// Create the node
 					node = new StandardNode();
 					node.setTypeInformation(nestedInfo);
 					node.setKind(HierarchyNode.NESTED);
 					node.setName(nestedClasses[j]);
 					node.checkFurtherBinding();
 					
 					// Populate the node with the super classes or sub classes
 					// depending on the selection
 					if (isSuperView) {
 						findAllSuperClasses(node);
 					} else {
 						findAllSubClasses(nestedClasses, node);
 					}
 					
 					// If we are in sub type mode, add all nodes. If we are in super type,
 					// check if this node was not already included.
 					if (! isSuperView || ! isNodeInSuperlist(nestedClasses[j], nestedClasses) ) {
 						nestedNode.addChild(node);
 						node.setParent(nestedNode);
 					}
 				}
 
 				// If we are in subtype mode, remove the nodes that are duplicated
 				if (! isSuperView) {
 					// Get all children of the nested node
 					StandardNode[] helpNestedClasses = (StandardNode[]) nestedNode.getChildrenVector().toArray(new StandardNode[0]);
 					// Iterate over the children, removing duplicated nodes
 					for (int j = 0; helpNestedClasses.length > j; j++) {
 						for (int k = 0; helpNestedClasses.length > k; k++) {
 							if ((j != k) && helpNestedClasses[k].hasSubNode(helpNestedClasses[j])) {
 								// Remove the node
 								helpNestedClasses[j].setParent(null);
 								nestedNode.removeChild(helpNestedClasses[j]);
 							}
 						}
 					}
 				}
 				
 				// If the implicit filter is turned on, remove the implicit nodes
 				if (implicitFilter) {
 					removeImplicitNodes(nestedNode);
 				}
 			}
 		}
 		return root;
 	}
 	
 	/**
 	 * Uses the structure model and searches the filename (using the findNode method). Using
 	 * this root node, create a list of sources for the hierarchy tree.
 	 * 
 	 * @return
 	 */
 	protected Vector getSources() {
 		
 		// Create the list to keep the sources
 		Vector srcs = new Vector();
 
 		// Gets the root structure node for the input
 		IProgramElement javaRootNode = findNode(structureModel.getRoot(), filename);
 		
 		// If the root could not be found, return an empty list
 		if (javaRootNode != null) {
 			// Get all children from the root node as an iterator
 			Iterator i = javaRootNode.getChildren().iterator();
 		
 			// Check which of the children should be a source for us
 			while(i.hasNext()) {
 				Object next = i.next();
 				// Check that the element is a caesar node
 				if (next instanceof CaesarProgramElement) {
 					// Check if the element is a virtual class or aspect
 					CaesarProgramElement node = (CaesarProgramElement) next;
 					
 					if (node.getCaesarKind() == CaesarProgramElement.Kind.VIRTUAL_CLASS ||
 						node.getCaesarKind() == CaesarProgramElement.Kind.ASPECT) {
 						// Get the classname from the node
 						String classname = node.getName();
 						// Strip the implementation termination if needed
 						if (classname.indexOf(IMPL_EXTENSION) != -1) {
 							classname = classname.substring(0, classname.indexOf(IMPL_EXTENSION));
 						}
 						// Add to the source list
						srcs.add(node.getPackageName().replaceAll("\\.", "/")	+ File.separator + classname);
 					}
 				}
 			}
 		}
 		// Return the vector
 		return srcs;
 	}
 	
 	/**
 	 * Recursive method for finding a node with name "name" between the children of "node".
 	 * 
 	 * Used when getting sources (getSources)
 	 */
 	protected IProgramElement findNode(IProgramElement node, String name) {
 		
 		if (node == null || name == null) {
 			return null;
 		}
 		// End of recursion, found the node
 		if (node.getName().equals(name)) {
 			return node;
 		} 
 		// Try to find between the children
 		Iterator i = node.getChildren().iterator();
 		while (i.hasNext()) {
 		    IProgramElement rec = findNode((IProgramElement) i.next(), name);
 			if (rec != null)
 				return rec;
 		}
 		// Couldn't find, return null
 		return null;
 	}
 
 	/**
 	 * Checks if the node is included as a super class of any of the nested classes
 	 * 
 	 * @param qualifiedNameOfNode
 	 * @param nestedClasses
 	 * @return true, if the node is a super class of any of the nested classes. False otherwise
 	 */
 	protected boolean isNodeInSuperlist(String qualifiedNameOfNode, String[] nestedClasses) {
 		
 		for (int i = 0; nestedClasses.length > i ; i++) {
 			CClass clazz = util.loadCClass(nestedClasses[i]);
 			AdditionalCaesarTypeInformation info = clazz.getAdditionalTypeInformation();
 			// Check if the element is in the array of super classes
 			if (isElementInArray(qualifiedNameOfNode, info.getSuperClasses()))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Checks if the element is in the array, using the compareTo method.
 	 * 
 	 * @param element an string representing the element
 	 * @param array an array of elements
 	 * @return true, if the element is in the array. False otherwise. 
 	 */
 	protected boolean isElementInArray(String element, String[] array) {
 
 		for (int i = 0; array.length > i; i++) {
 			if (element.compareTo(array[i]) == 0)
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Iterates the node getting all super classes and adding to it.
 	 * 
 	 * @param node
 	 */
 	protected void findAllSuperClasses(StandardNode node) {
 
 		// Get the information. The node must already be filled
 		AdditionalCaesarTypeInformation info = node.getTypeInformation();
 		
 		// Get all super classes
 		String[] superNodes = info.getSuperClasses();
 		
 		// For each super class, create the node and recursivelly find its supernodes
 		for (int i = 0; superNodes.length > i; i++) {
 			// Read the class and create the supernode
 			StandardNode superNode = new StandardNode();
 			CClass clazz = util.loadCClass(superNodes[i]);
 			superNode.setTypeInformation(clazz.getAdditionalTypeInformation());
 			superNode.setName(superNodes[i]);
 			superNode.setKind(StandardNode.NESTEDSUPER);
 			superNode.checkFurtherBinding();
 			
 			// Add the supernode to the node
 			superNode.setParent(node);
 			node.addChild(superNode);
 			
 			// Go recursive on the supernode
 			findAllSuperClasses(superNode);
 		}
 	}
 
 	/**
 	 * 
 	 * @param nestedClasses
 	 * @param node
 	 */
 	protected void findAllSubClasses(String[] nestedClasses, StandardNode node) {
 
 		String additionalInfo = "";
 		
 		// Iterate on the nested classes
 		for (int i = 0; nestedClasses.length > i; i++) {
 			CClass clazz = util.loadCClass(nestedClasses[i]);
 			AdditionalCaesarTypeInformation	helpInfo = clazz.getAdditionalTypeInformation();
 			String[] superClasses = helpInfo.getSuperClasses();
 			
 			if (isElementInArray(node.getName(), superClasses)) {
 				// Create a subnode
 				StandardNode subNode = new StandardNode(
 						HierarchyNode.NESTEDSUB, 
 						nestedClasses[i], 
 						node,
 						helpInfo);
 				subNode.checkFurtherBinding();
 				
 				for (int k = 0; superClasses.length > k; k++) {
 					if (k == 0)
 						additionalInfo = filterName(superClasses[k]);
 					else
 						additionalInfo = additionalInfo + " & "	+ filterName(superClasses[k]);
 				}
 				if (superClasses.length > 1)
 					subNode.setAdditionalName(additionalInfo);
 				
 				findAllSubClasses(nestedClasses, subNode);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @return
 	 */
 	protected String filterName(String name) {
 		try {
 			String help = new String(name);
 			int slashPos = name.lastIndexOf("/");
 			int dollarPos = name.lastIndexOf("$");
 			if (slashPos > 0)
 				help = name.substring(slashPos + 1);
 			if (dollarPos > slashPos) {
 				help = name.substring(dollarPos + 1);
 			}
 			return help;
 		} catch (Exception e) {
 			return "Error";
 		}
 	}
 
 	/**
 	 * Removes all the implicit nodes from the tree starting with node.
 	 * A node is only removed if its kind is not PARENTES, because we want
 	 * to keep the first nestedNode.
 	 * 
 	 * @param node
 	 */
 	protected void removeImplicitNodes(StandardNode node) {
 		// Get all children
 		Object[] children = node.getChildren();
 		
 		// If the node is implicity, remove itself (aditional test is to keep the nestedNode )
 		if (node.typeInformation.isImplicit() && ! node.getKind().equals(HierarchyNode.PARENTS)) {
 			// Remove the node from its parent
 			StandardNode parent = (StandardNode) node.getParent();
 			parent.removeChild(node);
 			
 			// Iterate on the children, making them children of the parent
 			for (int i = 0; i < children.length; i++) {
 				StandardNode child = (StandardNode) children[i];
 				parent.addChild(child);
 				child.setParent(parent);
 			}
 		}
 		// Go recursively on all children
 		for (int i = 0; i < children.length; i++) {
 			removeImplicitNodes((StandardNode) children[i]);
 		}
 	}
 }
