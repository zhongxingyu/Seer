 /**
  *    Copyright 2013 Thomas Naeff (github.com/thnaeff)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package ch.thn.gedcom.data;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import ch.thn.gedcom.GedcomFormatter;
 import ch.thn.gedcom.store.GedcomStoreBlock;
 import ch.thn.gedcom.store.GedcomStoreLine;
 import ch.thn.gedcom.store.GedcomStoreStructure;
 import ch.thn.util.tree.TreeNode;
 import ch.thn.util.tree.TreeNodeException;
 
 /**
  * @author Thomas Naeff (github.com/thnaeff)
  *
  */
 public class GedcomNode extends TreeNode<String, GedcomLine> {
 
 	/** The delimiter for multiple step values used in {@link #followPath(String...)} **/
 	public static final String PATH_OPTION_DELIMITER = ";";
 
 	/** Create all the available lines automatically */
 	public static final int ADD_ALL = 0;
 	/** Only create mandatory lines automatically */
 	public static final int ADD_MANDATORY = 1;
 	/** Do not create any lines automatically */
 	public static final int ADD_NONE = 2;
 
 	private GedcomStoreBlock storeBlock = null;
 
 	private GedcomStoreLine storeLine = null;
 	
 	private String tagOrStructureName = null;
 	private String tag = null;
 	private boolean lookForXRefAndValueVariation = false;
 	private boolean withXRef = false;
 	private boolean withValue = false;
 
 	/**
 	 * 
 	 * 
 	 * @param storeBlock The block to get the line from
 	 * @param tagOrStructureName
 	 * @param tag
 	 * @param lookForXRefAndValueVariation
 	 * @param withXRef
 	 * @param withValue
 	 */
 	protected GedcomNode(GedcomStoreBlock storeBlock, String tagOrStructureName, 
 			String tag, boolean lookForXRefAndValueVariation, boolean withXRef, boolean withValue) {
 		super(tagOrStructureName, null);
 
 		this.tagOrStructureName = tagOrStructureName;
 		this.tag = tag;
 		this.lookForXRefAndValueVariation = lookForXRefAndValueVariation;
 		this.withXRef = withXRef;
 		this.withValue = withValue;
 
 		//Line with that tag or structure name does not exist
 		if (!storeBlock.hasStoreLine(tagOrStructureName)) {
 			String s = "";
 
 			if (storeBlock.getParentStoreLine() == null) {
 				s = "Structure " + storeBlock.getStoreStructure().getStructureName();
 			} else {
 				s = "Store block " + storeBlock.getParentStoreLine().getId();
 			}
 
 			throw new GedcomCreationError(s + " does not have a tag " + 
 					tagOrStructureName + ". Available tags: " + 
 					GedcomFormatter.makeOrList(storeBlock.getAllLineIDs(), null, null));
 		}
 
 		storeLine = storeBlock.getStoreLine(tagOrStructureName);
 
 		if (storeLine.hasStructureName()) {			
 			//It is a structure line, thus it does not have a child block but it 
 			//is only a "link" to the structure 
 			this.storeBlock = storeBlock.getStoreStructure().getStore()
 					.getGedcomStructure(tagOrStructureName, tag, 
 							lookForXRefAndValueVariation, withXRef, withValue).getStoreBlock();
 		} else {
 			this.storeBlock = storeLine.getChildBlock();
 		}
 		
 		if (storeLine.hasStructureName()) {
 			setNodeValue(new GedcomStructureLine(storeLine, tag, this));
 		} else {
 			setNodeValue(new GedcomTagLine(storeLine, tagOrStructureName, this));
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param storeBlock
 	 */
 	protected GedcomNode(GedcomStoreStructure storeStructure) {
 		super(storeStructure.getStoreBlock().getStoreStructure().getStructureName(), null);
 		this.storeBlock = storeStructure.getStoreBlock();
 		this.tagOrStructureName = storeStructure.getStoreBlock().getStoreStructure().getStructureName();
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public GedcomNode newLine() {
 		if (isHeadNode()) {
 			//The head node is the starting point of the tree
 			return null;
 		}
 
 		//Add child node to the parent node, using the parameters of this node
 		if (lookForXRefAndValueVariation) {
 			return ((GedcomNode)getParentNode()).addChildLine(tagOrStructureName, tag, 
 					withXRef, withValue);
 		} else {
 			return ((GedcomNode)getParentNode()).addChildLine(tagOrStructureName, tag);
 		}
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param tagOrStructureName
 	 * @return
 	 */
 	public GedcomNode addChildLine(String tagOrStructureName) {
 		return addChildLine(tagOrStructureName, null, false, false, false);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param tagOrStructureName
 	 * @param tag
 	 * @return
 	 */
 	public GedcomNode addChildLine(String tagOrStructureName, String tag) {
 		return addChildLine(tagOrStructureName, tag, false, false, false);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param tagOrStructureName
 	 * @param tag
 	 * @param withXRef
 	 * @param withValue
 	 * @return
 	 */
 	public GedcomNode addChildLine(String tagOrStructureName, String tag, 
 			boolean withXRef, boolean withValue) {
 		return addChildLine(tagOrStructureName, tag, true, false, false);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param tagOrStructureName
 	 * @param tag
 	 * @param lookForXRefAndValueVariation
 	 * @param withXRef
 	 * @param withValue
 	 * @return
 	 */
 	private GedcomNode addChildLine(String tagOrStructureName, String tag, 
 			boolean lookForXRefAndValueVariation, boolean withXRef, boolean withValue) {
 				
 		if (maxNumberOfLinesReached(tagOrStructureName)) {
 			return null;
 		}
 		
 		GedcomNode newNode = new GedcomNode(storeBlock, tagOrStructureName, tag, 
 				lookForXRefAndValueVariation, withXRef, withValue);
 		
 		int newStoreLinePos = 0;
 		
 		//Get the node position only if it is not a header. The header has position 
 		//0 anyways
 		if (!isHeadNode()) {
 			newStoreLinePos = newNode.getStoreLine().getPos();
 		}
 		
 		//Look for the position where to add the new line. The position is defined 
 		//through the parsed lineage linked grammar with the line order
 		for (int i = getNumberOfChildNodes() - 1; i >= 0 ; i--) {
 			if (newStoreLinePos >= getChildNode(i).getNodeValue().getStoreLine().getPos()) {
 				//Add the new line right before the line with a higher store line position
 				try {
 					addChildNodeAt(i + 1, newNode);
 				} catch (TreeNodeException e) {
 					return null;
 				}
 				
 				return newNode;
 			}
 			
 		}
 				
 		//Add the new line at the end
 		try {
 			addChildNode(newNode);
 		} catch (TreeNodeException e) {
 			return null;
 		}
 		
 		return newNode;
 	}
 	
 	/**
 	 * Returns the parent line of this line
 	 * 
 	 * @return
 	 */
 	public GedcomNode getParentLine() {
 		return (GedcomNode)getParentNode();
 	}
 	
 	/**
 	 * Returns the child line with the given tag or structure name. Since there 
 	 * can be more than one line with the same tag or structure name, its line number 
 	 * has to be given.
 	 * 
 	 * @param tagOrStructureName
 	 * @param index
 	 * @return
 	 */
 	public GedcomNode getChildLine(String tagOrStructureName, int lineNumber) {
 		return (GedcomNode)getChildNode(tagOrStructureName, lineNumber);
 	}
 	
 	/**
 	 * Returns the child line with the given structure name and tag variation
 	 * 
 	 * @param structureName
 	 * @param tag
 	 * @param lineNumber
 	 * @return
 	 */
 	public GedcomNode getChildLine(String structureName, String tag, int lineNumber) {
 		return getChildLine(structureName, tag, false, false, false, lineNumber);
 	}
 	
 	/**
 	 * Returns the child line with the given structure name and variation
 	 * 
 	 * @param structureName
 	 * @param tag
 	 * @param withXRef
 	 * @param withValue
 	 * @param lineNumber
 	 * @return
 	 */
 	public GedcomNode getChildLine(String structureName, String tag, 
 			boolean withXRef, boolean withValue, int lineNumber) {
 		return getChildLine(structureName, tag, true, withXRef, withValue, lineNumber);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param structureName
 	 * @param tag
 	 * @param lookForXRefAndValueVariation
 	 * @param withXRef
 	 * @param withValue
 	 * @param lineNumber
 	 * @return
 	 */
 	private GedcomNode getChildLine(String structureName, String tag, 
 			boolean lookForXRefAndValueVariation, boolean withXRef, boolean withValue, int lineNumber) {
 		
 		LinkedList<TreeNode<String, GedcomLine>> children = getChildNodes(structureName);
 		
		if (children.size() == 0) {
 			return null;
 		}
 		
 		int matchCount = -1;
 		
 		//Search for the child node which matches the parameters
 		for (TreeNode<String, GedcomLine> child : children) {
 			if (structureName.equals(((GedcomNode)child).getTagOrStructureName())
 					&& tag.equals(((GedcomNode)child).getTag())) {
 				matchCount++;;
 			}
 			
 			if (matchCount > 0 && lookForXRefAndValueVariation) {
 				if (withXRef == ((GedcomNode)child).getWithXRef() 
 						&& withValue == ((GedcomNode)child).getWithValue()) {
 					//Keep the match count
 				} else {
 					//Not a match with xref and value -> get rid of match
 					matchCount--;
 				}
 			}
 			
 			if (matchCount == lineNumber) {
 				return (GedcomNode) child;
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public String getParentNodeKey() {
 		if (!isHeadNode()) {
 			return ((GedcomNode)getParentNode()).getNodeKey();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Sets the value of this node
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public GedcomNode setTagLineValue(String value) {
 		if (!getNodeValue().isTagLine()) {
 			return null;
 		}
 		
 		getNodeValue().getAsTagLine().setValue(value);
 		return this;
 	}
 	
 	/**
 	 * Returns the value of the line at this node
 	 * 
 	 * @return 
 	 */
 	public String getTagLineValue() {
 		return getNodeValue().getAsTagLine().getValue();
 	}
 	
 	/**
 	 * Sets the xref of this node
 	 * 
 	 * @param xref
 	 * @return
 	 */
 	public GedcomNode setTagLineXRef(String xref) {
 		if (!getNodeValue().isTagLine()) {
 			return null;
 		}
 		
 		getNodeValue().getAsTagLine().setXRef(xref);
 		return this;
 	}
 	
 	/**
 	 * Returns the xref of the line at this node
 	 * 
 	 * @return 
 	 */
 	public String getTagLineXRef() {
 		return getNodeValue().getAsTagLine().getXRef();
 	}
 
 	/**
 	 * 
 	 *
 	 * @param tagName
 	 * @param value
 	 * @return
 	 */
 	public boolean hasLineWithValue(String value) {
 		LinkedList<TreeNode<String, GedcomLine>> nodes = getChildNodes();
 		
 		for (TreeNode<String, GedcomLine> node : nodes) {
 			GedcomLine line = node.getNodeValue();
 			if (line.isTagLine()
 					&& line.getAsTagLine().getValue() != null
 					&& line.getAsTagLine().getValue().equals(value)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param tagName
 	 * @param xref
 	 * @return
 	 */
 	public boolean hasLineWithXRef(String xref) {
 		LinkedList<TreeNode<String, GedcomLine>> nodes = getChildNodes();
 		
 		for (TreeNode<String, GedcomLine> node : nodes) {
 			GedcomLine line = node.getNodeValue();
 			if (line.isTagLine()
 					&& line.getAsTagLine().getXRef() != null
 					&& line.getAsTagLine().getXRef().equals(xref)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * This method checks if a new line with the given tag or structure name
 	 * can be added to this block. The method does not do any extended checks, like
 	 * if the maximum number of the line is already reached. It only checks if
 	 * there is such a line defined for this block in the lineage-linked grammar
 	 *
 	 * @param tagOrStructureName The tag or structure name to look for
 	 * @return True if such a line can be added
 	 */
 	public boolean canAddLine(String tagOrStructureName) {
 		//-> multiple variations do not matter, because the min/max values apply 
 		//for all variations of a structure
 		return storeBlock.hasStoreLine(tagOrStructureName);
 	}
 
 	/**
 	 * This method returns the maximum number of lines allowed for the line
 	 * with the given tag or structure name.<br>
 	 * Since all the variations have the 
 	 * same min/max limits, there is no need for specifying the variation and 
 	 * only the tag or structure name is enough.
 	 *
 	 * @param tagOrStructureName
 	 * @return Returns the maximum number or allowed lines or 0 if there is no
 	 * maximum defined. -1 is returned if there is no line with the given tag
 	 * or structure line available for this block.
 	 */
 	public int maxNumberOfLines(String tagOrStructureName) {
 		//It does not need the tag for multiple variations, since all the
 		//variations are the same
 
 		if (!storeBlock.hasStoreLine(tagOrStructureName)) {
 			return -1;
 		}
 
 		return storeBlock.getStoreLine(tagOrStructureName).getMax();
 	}
 
 	/**
 	 * This method checks if the maximum number of lines of the line with the given
 	 * tag or structure name has been reached already
 	 *
 	 * @param tagOrStructureName
 	 * @return
 	 */
 	public boolean maxNumberOfLinesReached(String tagOrStructureName) {
 		if (!hasChildNode(tagOrStructureName)) {
 			return false;
 		}
 		
 		int lineCount = getNumberOfChildNodes(tagOrStructureName);
 		int max = maxNumberOfLines(tagOrStructureName);
 		
 		return (max != 0 && lineCount >= max);
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public GedcomStoreBlock getStoreBlock() {
 		return storeBlock;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public GedcomStoreLine getStoreLine() {
 		return storeLine;
 	}
 
 
 	/**
 	 * This method checks if the given name is a structure name which is defined
 	 * for this block. If this method returns true, it should be possible to
 	 * add a structure line with the given name.
 	 *
 	 * @param name The name to check
 	 * @return Returns true if the given name is a structure name, or false if it is not
 	 * a structure name or if it does not exist.
 	 */
 	public boolean nameIsPossibleStructure(String name) {
 		if (!getStoreBlock().hasStoreLine(name)) {
 			return false;
 		}
 
 		return getStoreBlock().getStoreLine(name).hasStructureName();
 	}
 
 	/**
 	 * This method checks if the given name is a tag name which is defined
 	 * for this block. If this method returns true, it should be possible to
 	 * add a tag line with the given name.
 	 *
 	 * @param name The name to check
 	 * @return Returns true if the given name is a tag name, or false if it is not
 	 * a tag name or if it does not exist.
 	 */
 	public boolean nameIsPossibleTag(String name) {
 		if (!getStoreBlock().hasStoreLine(name)) {
 			return false;
 		}
 
 		return getStoreBlock().getStoreLine(name).hasTags();        
 	}
 
 	/**
 	 * Returns true if the current object can have child lines as defined in the
 	 * lineage linked grammar
 	 *
 	 * @return
 	 */
 	public boolean canHaveChildren() {
 		return getStoreBlock().hasChildLines();
 	}
 
 	/**
 	 * Returns the minimum number of lines of the type of this line which are
 	 * required in one block
 	 *
 	 * @return
 	 */
 	public int getMinNumberOfLines() {
 		return storeLine.getMin();
 	}
 
 	/**
 	 * Returns the maximum number of lines of the type of this line which are
 	 * allowed in one block. A returned number of 0 indicates that there is
 	 * not maximum limit (given as M in the lineage linked grammar).
 	 *
 	 * @return
 	 */
 	public int getMaxNumberOfLines() {
 		return storeLine.getMax();
 	}
 	
 	/**
 	 * Adds all the lines which are available for this node. However, some 
 	 * structures which have multiple variations can not be added automatically 
 	 * because the user has to make the decision which variation should be added.
 	 * 
 	 * @param recursive If this flag is set to <code>true</code>, all child lines 
 	 * are also added for each added line -> the whole tree is built.
 	 */
 	public void addAllChildLines(boolean recursive) {
 		if (storeBlock == null) {
 			return;
 		}
 		
 		LinkedList<GedcomStoreLine> allLines = storeBlock.getStoreLines();
 		for (GedcomStoreLine line : allLines) {
 			try {
 				if (recursive) {
 					GedcomNode node = addChildLine(line);
 					
 					if (node != null) {
 						node.addAllChildLines(recursive);
 					}
 				} else {
 					addChildLine(line);
 				}
 			} catch (GedcomCreationError e) {
 				//No warnings when adding all the lines
 //				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Adds all the lines which are defined as mandatory in the gedcom lineage-linked 
 	 * grammar for this node. However, some structures which have multiple variations 
 	 * can not be added automatically because the user has to make the decision 
 	 * which variation should be added.
 	 * 
 	 * @param recursive If this flag is set to <code>true</code>, all mandatory child lines 
 	 * are also added for each added line -> the whole tree is built with mandatory lines.
 	 */
 	public void addMandatoryChildLines(boolean recursive) {
 		if (storeBlock == null) {
 			return;
 		}
 		
 		LinkedList<GedcomStoreLine> mandatoryLines = storeBlock.getMandatoryLines();
 		for (GedcomStoreLine line : mandatoryLines) {
 			try {
 				if (recursive) {
 					GedcomNode node = addChildLine(line);
 					
 					if (node != null) {
 						node.addMandatoryChildLines(recursive);
 					}
 				} else {
 					addChildLine(line);
 				}
 			} catch (GedcomCreationError e) {
 				//No warnings when adding all the lines
 //				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Tries to add the given store line. A given store line can not be added 
 	 * if it has multiple variations, because it can not make the decision 
 	 * which variation to use.
 	 * 
 	 * @param line
 	 * @return
 	 */
 	private GedcomNode addChildLine(GedcomStoreLine line) {
 		if (line.hasVariations()) {
 			throw new GedcomCreationError("Can not add child line " + 
 					line.getStructureName() + ". " + 
 					"Line has multiple variations.");
 		}
 		
 		if (line.getTagNames().size() > 1) {
 			throw new GedcomCreationError("Can not add child line " + 
 					line.getTagNames() + ". " + 
 					"Line has multiple tags available.");
 		}
 		
 		String tag = null;
 		
 		if (line.getTagNames().size() == 1) {
 			tag = line.getTagNames().iterator().next();
 		}
 		
 		if (!line.hasStructureName()) {
 			return addChildLine(tag);
 		} else {
 			boolean lookForXRefAndValueVariation = line.hasVariations();
 			return addChildLine(line.getStructureName(), tag, 
 					lookForXRefAndValueVariation, line.hasXRefNames(), line.hasValueNames());
 		}
 	}
 	
 	
 	@Override
 	public boolean isInvisibleNode() {
 		if (super.isInvisibleNode()) {
 			return true;
 		}
 		
 		//Do not print structure lines
 		if (getNodeValue().isStructureLine()) {
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public boolean printNode() {
 		if (!getTreePrinter().skipHiddenNodes()) {
 			return true;
 		}
 		
 		if (!super.printNode()) {
 			return false;
 		}
 		
 		if (isHeadNode()) {
 			return true;
 		}
 		
 		return !skipLinePrint(this, false, false);
 	}
 	
 	/**
 	 * Checks if the line has to be skipped. A line has to be skipped if value/xref
 	 * are required but not set/empty (depending on the given flags). However, 
 	 * if there is a line in a lower level which has a value/xref, then this 
 	 * line has to be printed in order to print the line with value on the lower level.
 	 *
 	 * @param node
 	 * @param printEmptyLines
 	 * @param printLinesWithNoValueSet
 	 * @return
 	 */
 	private boolean skipLinePrint(GedcomNode node, boolean printEmptyLines,
 			boolean printLinesWithNoValueSet) {
 		GedcomLine line = node.getNodeValue();
 		boolean skip = false;
 
 		if (line.isTagLine()) {
 			GedcomTagLine tagLine = line.getAsTagLine();
 
 			if (!printEmptyLines) {
 				//Skip empty lines which actually require a value
 				if ((tagLine.isValueSet() || tagLine.isXRefSet()) && tagLine.isEmpty()) {
 					skip = true;
 				}
 			}
 
 			if (!printLinesWithNoValueSet) {
 				//Skip lines which have no value and xref set, but which require a value
 				if (!tagLine.isValueSet() && !tagLine.isXRefSet()) {
 					skip = true;
 				}
 			}
 		} else {
 			//Its a structure line
 		}
 
 		//Any child lines which shouldn't be skipped? If yes, then this line
 		//should not be skipped because otherwise the child line with content
 		//will not be printed. If all the child lines can be skipped, this line
 		//does not need to be printed if skip has already been set to true
 
 		if (skip && node.hasChildNodes()) {
 			LinkedList<TreeNode<String, GedcomLine>> nodes = node.getChildNodes();
 
 			for (TreeNode<String, GedcomLine> n : nodes) {
 				
 				if (!skipLinePrint((GedcomNode)n, printEmptyLines, printLinesWithNoValueSet)) {
 					//A tag line found which should not be skipped
 					return false;
 				} else {
 					//If it is a structure line, just pass the skip state
 					//on to the caller (structure lines are not considered
 					//when checking if lines have to be skipped or not)
 					if (line.isStructureLine()) {
 						return true;
 					}
 				}
 
 			}
 		}
 
 		return skip;
 	}
 	
 	/**
 	 * Tries to follow the given path. The path needs to exist in order to follow 
 	 * it.<br>
 	 * <br>
 	 * For more information about how to use the path array, read 
 	 * {@link #followPath(boolean, boolean, boolean, String...)}
 	 * 
 	 * @param path The path to follow.
 	 * @return The {@link GedcomNode} of the last object in the path, or <code>null</code> if 
 	 * following the path did not work.
 	 * @see #followPath(boolean, boolean, boolean, String...)
 	 */
 	public GedcomNode followPath(String... path) {
 		return followPath(false, false, false, path);
 	}
 	
 	/**
 	 * Tries to follow the given path. If the path does not exist, it tries to 
 	 * create the path.<br>
 	 * <br>
 	 * For more information about how to use the path array, read 
 	 * {@link #followPath(boolean, boolean, boolean, String...)}
 	 * 
 	 * @param path The path to follow.
 	 * @return The {@link GedcomNode} of the last object in the path, or <code>null</code> if 
 	 * following the path did not work.
 	 * @see #followPath(boolean, boolean, boolean, String...)
 	 */
 	public GedcomNode followPathCreate(String... path) {
 		return followPath(true, false, false, path);
 	}
 	
 	/**
 	 * Tries to follow the given path. If such a path already exists, a new path 
 	 * is created at the most end possible of the path. If no such path exists, 
 	 * it just tries to create the path.<br>
 	 * <br>
 	 * For more information about how to use the path array, read 
 	 * {@link #followPath(boolean, boolean, boolean, String...)}
 	 * 
 	 * @param path The path to follow.
 	 * @return The {@link GedcomNode} of the last object in the path, or <code>null</code> if 
 	 * following the path did not work.
 	 * @see #followPath(boolean, boolean, boolean, String...)
 	 */
 	public GedcomNode createPathEnd(String... path) {
 		return followPath(false, true, false, path);
 	}
 	
 	/**
 	 * Tries to create the given path.<br>
 	 * <br>
 	 * For more information about how to use the path array, read 
 	 * {@link #followPath(boolean, boolean, boolean, String...)}
 	 * 
 	 * @param path The path to follow.
 	 * @return The {@link GedcomNode} of the last object in the path, or <code>null</code> if 
 	 * following the path did not work.
 	 * @see #followPath(boolean, boolean, boolean, String...)
 	 */
 	public GedcomNode createPath(String... path) {
 		return followPath(false, false, true, path);
 	}
 
 	/**
 	 * Follows the path given with <code>path</code>. Each array position describes
 	 * one path step, and each step can contain multiple values describing the
 	 * step. The following lines each show one step in the path, (multiple values 
 	 * are separated by {@value #PATH_OPTION_DELIMITER}):<br>
 	 * - "tag or structure name"<br>
 	 * - "tag or structure name;line number"<br>
 	 * - "structure name;tag"<br>
 	 * - "structure name;tag;with xref;with value;line number"<br>
 	 * - "structure name;tag;with xref;with value"<br>
 	 * ("with xref" and "with value" have to be given as "true" or "false")<br>
 	 * <br>
 	 * If multiple step values are given, they have to be separated with the
 	 * {@link #PATH_OPTION_DELIMITER}. Multiple step values are needed if the
 	 * next path step can not be identified with one step value only. A tag line
 	 * for example can be added multiple times, thus when accessing that line, the
 	 * tag and the line number have to be given. Also, some structures exist in
 	 * different variations (with/without xref, with/without value, ...) and might
 	 * have to be accessed with multiple values for one path step.<br>
 	 * If a path can not be followed, this method throws an {@link GedcomPathAccessError}
 	 * with an error text and the path which caused the error. The error text might
 	 * give a hint to what has gone wrong.
 	 *
 	 * @param createNewIfNotExisting If set to <code>true</code>, it tries to 
 	 * create the path if it does not exist yet
 	 * @param createNewEnd If set to <code>true</code>, it tries to create a new 
 	 * path at the very most end possible. This means that if no such path exists 
 	 * yet, the path is created and if such a path already exists, a new one is 
 	 * created.
 	 * @param createPath Create the whole given path.
 	 * @param path The path to follow.
 	 * @return The {@link GedcomNode} of the last object in the path, or <code>null</code> if 
 	 * following the path did not work.
 	 * @throws GedcomPathAccessError If a path piece can not be accessed because it 
 	 * does not exist
 	 * @throws GedcomCreationError If new path pieces have to be created but they
 	 * can not be created (because of invalid structure/tag names, or there is no 
 	 * node which can have another line added).
 	 */
 	private GedcomNode followPath(boolean createNewIfNotExisting, boolean createNewEnd, 
 			boolean createPath, String... path) {
 		
 		if (path == null || path.length == 0) {
 			//Nothing to do
 			return this;
 		}
 		
 		GedcomNode currentNode = this;
 		GedcomNode lastNodeWithSplitPossibility = null;
 		
 		int lastIndexWithSplitPossibility = -1;
 		int pathIndex = 0;
 		
 		for (; pathIndex < path.length; pathIndex++) {
 			PathStepPieces pp = new PathStepPieces();
 			if (!pp.parse(path[pathIndex])) {
 				//Nothing to do
 				continue;
 			}
 			
 			if ((createNewEnd || createNewIfNotExisting) 
 					&& !currentNode.maxNumberOfLinesReached(pp.tagOrStructureName)) {
 				lastNodeWithSplitPossibility = currentNode;
 				lastIndexWithSplitPossibility = pathIndex;
 			}
 			
 			if (createPath) {
 				//Create path
 				if (pp.tag == null) {
 					currentNode = currentNode.addChildLine(pp.tagOrStructureName);
 				} else {
 					currentNode = currentNode.addChildLine(pp.tagOrStructureName, pp.tag, 
 							pp.lookForXRefAndValueVariation, pp.withXRef, pp.withValue);
 				}
 				
 				if (currentNode == null) {
 					throw new GedcomPathCreationError(path, pathIndex, 
 							"Can not create path '" + path[pathIndex] + "'.");
 				}
 			} else {
 				//Follow path
 				if (pp.tag == null) {
 					currentNode = currentNode.getChildLine(pp.tagOrStructureName, pp.lineNumber);
 				} else {
 					currentNode = currentNode.getChildLine(pp.tagOrStructureName, pp.tag, 
 							pp.lookForXRefAndValueVariation, pp.withXRef, pp.withValue, pp.lineNumber);
 				}
 				
 				if ((createNewEnd || createPath || createNewIfNotExisting) 
 						&& currentNode == null) {
 					//Failed to follow path
 					break;
 				}
 				
 				if (currentNode == null) {
 					throw new GedcomPathAccessError(path, pathIndex, 
 							"Can not access path '" + path[pathIndex] + "'.");
 				}
 			}
 			
 		}
 		
 		if (createNewEnd || (createNewIfNotExisting && currentNode == null)) {
 			//createNewEnd: Create a new end without caring if such a path is already 
 			//there or not. 
 			//createNewIfNotExisting: Following the path was not possible -> create it
 			
 			if (lastNodeWithSplitPossibility == null) {
 				throw new GedcomPathCreationError(path, lastIndexWithSplitPossibility, 
 						"Can not create a new path " + Arrays.toString(path) + 
 						" in " + this + ". The maximum number of lines has been reached.");
 			} else {
 				//Create new path, starting at the last possible split point
 				String[] newPath = new String[path.length - lastIndexWithSplitPossibility];
 				System.arraycopy(path, lastIndexWithSplitPossibility, newPath, 0, newPath.length);
 				return lastNodeWithSplitPossibility.createPath(newPath);
 			}
 		}
 		
 		return currentNode;
 	}
 	
 	
 	/**
 	 * @return tagOrStructureName
 	 */
 	protected String getTagOrStructureName() {
 		return tagOrStructureName;
 	}
 	
 	/**
 	 * @return tag
 	 */
 	protected String getTag() {
 		return tag;
 	}
 	
 	/**
 	 * 
 	 * @return lookForXRefAndValueVariation
 	 */
 	protected boolean getLookForXRefAndValueVariation() {
 		return lookForXRefAndValueVariation;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @return withXRef
 	 */
 	protected boolean getWithXRef() {
 		return withXRef;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @return withValue
 	 */
 	protected boolean getWithValue() {
 		return withValue;
 	}
 
 
 	@Override
 	public String toString() {
 		if (getNodeValue() == null) {
 			if (tag != null) {
 				return tagOrStructureName + " (" + tag + ")";
 			} else {
 				return tagOrStructureName;
 			}
 		} else {
 			return getNodeValue().toString();
 		}
 	}
 	
 	
 	
 	
 	/**************************************************************************
 	 * 
 	 * 
 	 *
 	 * @author Thomas Naeff (github.com/thnaeff)
 	 *
 	 */
 	private class PathStepPieces {
 		protected String tagOrStructureName = null;
 		protected String tag = null;
 		protected boolean lookForXRefAndValueVariation = false;
 		protected boolean withXRef = false;
 		protected boolean withValue = false;
 		protected int lineNumber = 0;
 		
 		
 		public boolean parse(String pathPiece) {
 			String[] pathPieceParts = pathPiece.split(PATH_OPTION_DELIMITER);
 			
 			if (pathPieceParts.length == 0) {
 				return false;
 			}
 			
 			tagOrStructureName = pathPieceParts[0];
 			
 			if (pathPieceParts.length > 1) {
 				try {
 					lineNumber = Integer.parseInt(pathPieceParts[1]);
 				} catch (NumberFormatException e) {
 					//If it is not a line number, it must be a tag
 					tag = pathPieceParts[1];
 				}
 				
 				//Only continue if there was a tag
 				if (tag != null && pathPieceParts.length > 2) {
 					lookForXRefAndValueVariation = true;
 					withXRef = Boolean.parseBoolean(pathPieceParts[2]);
 					
 					if (pathPieceParts.length > 3) {
 						withValue = Boolean.parseBoolean(pathPieceParts[3]);
 					}
 					
 					if (pathPieceParts.length > 4) {
 						lineNumber = Integer.parseInt(pathPieceParts[4]);
 					}
 				}
 			}
 			
 			return true;
 		}
 		
 		
 	}
 
 }
