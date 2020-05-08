 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
  
 package com.organic.maynard.outliner;
 
 import com.organic.maynard.xml.XMLTools;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 public class EnhancedTextMacro extends MacroImpl {
 
 	// Constants
 	private static final String E_PATTERN = "pattern";
 	
 	// Instance Fields
 	private String replacementPattern = "";
 
 	// Class Fields
	private static EnhancedTextMacroConfig macroConfig = new EnhancedTextMacroConfig();
 
 	
 	// The Constructors
 	public EnhancedTextMacro() {
 		this("");
 	}
 
 	public EnhancedTextMacro(String name) {
 		super(name, true, Macro.COMPLEX_UNDOABLE);
 	}
 
 
 	// Accessors
 	public String getReplacementPattern() {return replacementPattern;}
 	public void setReplacementPattern(String replacementPattern) {this.replacementPattern = replacementPattern;}
 
 
 	// Macro Interface	
 	public MacroConfig getConfigurator() {return this.macroConfig;}
 	public void setConfigurator(MacroConfig macroConfig) {}
 
 	public NodeRangePair process(NodeRangePair nodeRangePair) {
 		Node node = nodeRangePair.node;
 		
 		// Create the mini tree from the replacement pattern
 		if (replacementPattern.equals("")) {
 			return null;
 		}
 		Node replacementNode = PadSelection.pad(replacementPattern, node.getTree(), node.getDepth(), PlatformCompatibility.LINE_END_UNIX).getFirstChild();
 		
 		// Walk the tree backwards.
 		boolean walking = true;
 		Node walkNode = replacementNode.getLastDecendent();
 		while (walking) {
 			String value = walkNode.getValue();
 			if (value.equals("{$value}")) {
 				Node clonedNode = node.cloneClean();
 				clonedNode.setDepthRecursively(walkNode.getDepth());
 				
 				int index = walkNode.currentIndex();
 				Node parent = walkNode.getParent();
 				parent.removeChild(walkNode,index);
 				parent.insertChild(clonedNode,index);
 				
 				walkNode = clonedNode;
 			}
 			
 			Node prevNode = walkNode.prev();
 			if (walkNode == prevNode) {
 				walking = false;
 			}
 			walkNode = prevNode;
 		}
 		
 		nodeRangePair.node = replacementNode;
 		nodeRangePair.startIndex = -1;
 		nodeRangePair.endIndex = -1;
 		return nodeRangePair;
 	}
 
 	
 	// Saving the Macro
 	protected void prepareFile (StringBuffer buf) {
 		buf.append(XMLTools.getXmlDeclaration(null) + "\n");
 		buf.append(XMLTools.getElementStart(E_PATTERN) + XMLTools.escapeXMLText(getReplacementPattern()) + XMLTools.getElementEnd(E_PATTERN)+ "\n");
 	}
 
 
 	// Sax DocumentHandler Implementation
 	protected void handleCharacters(String elementName, String text) {
 		if (elementName.equals(E_PATTERN)) {
 			setReplacementPattern(text);
 		}
 	}
 }
