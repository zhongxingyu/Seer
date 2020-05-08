 /**
  * Copyright (C) 2000 Maynard Demmon, maynard@organic.com
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
  
 package com.organic.maynard.outliner;
 
 import java.util.*;
 import java.io.*;
 import java.awt.datatransfer.*;
 
 public class NodeSet implements Cloneable {
 
 	private ArrayList nodes = new ArrayList();
 
 	// Good Info: http://developer.java.sun.com/developer/bugParade/bugs/4066902.html
 		
 
 	// The Constructors
 	public NodeSet() {}
 	
 	
 	// Accessors
 	public void addNode(Node node) {
 		nodes.add(node);
 	}
 	
 	public Node getNode(int i) {
 		return (Node) nodes.get(i);
 	}
 	
 	public void removeNode(int i) {
 		nodes.remove(i);
 	}
 	
 	public int getSize() {
 		return nodes.size();
 	}
 	
 	public boolean isEmpty() {
 		if (getSize() > 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	
 	// Cloneable Interface
 	public Object clone() {
 		NodeSet nodeSet = new NodeSet();
 		
 		for (int i = 0; i < nodes.size(); i++) {
 			Node node = (Node) nodes.get(i);
 			Node newNode = node.cloneClean();
 			
 			nodeSet.addNode(newNode);
 		}
 		
 		return nodeSet;
 	}
 
 	
 	// Overridden Methods
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		
 		for (int i = 0; i < nodes.size(); i++) {
 			Node node = (Node) nodes.get(i);
			
			// Since a node may be a root node, and depthPaddedValue doesn't throw in root level text,
			// let's put it back in.
			if (node.isRoot()) {
				for (int j = 0; j < node.getDepth(); j++) {
					buf.append(Preferences.DEPTH_PAD_STRING);
				}
				buf.append(node.getValue()).append(Preferences.LINE_END_STRING);
			}
			
 			node.depthPaddedValue(buf,  Preferences.LINE_END_STRING);
 		}
 		return buf.toString();
 	}	
 }
 
 
 class NodeSetTransferable extends StringSelection implements Transferable {
 	
 	private NodeSet nodeSet = null;
 	
 	public static DataFlavor nsFlavor;
 	
 	static {
 		try {
 			nsFlavor = new DataFlavor(Class.forName("com.organic.maynard.outliner.NodeSet"), "NodeSet");
 		} catch (ClassNotFoundException ex) {}
 	}
 	
	private static final int STRING = 0;
	private static final int NODESET = 1;
 	
 	private DataFlavor[] flavors = {
		DataFlavor.stringFlavor,
		nsFlavor
 	};
 	
 	
 	// The Constructors
 	public NodeSetTransferable(NodeSet nodeSet) {
 		super(nodeSet.toString());
 		this.nodeSet = nodeSet;
 	}
 
 
 	// Transferable Interface
 	public synchronized DataFlavor[] getTransferDataFlavors() {
 		return flavors;
 	}
 
 	public boolean isDataFlavorSupported(DataFlavor flavor) {
 		return (
 			flavor.equals(flavors[STRING]) || 
 			flavor.equals(flavors[NODESET])
 		);    
 	}
     
 	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 		if (flavor.equals(flavors[STRING])) {
 			return nodeSet.toString();
 			
 		} else if (flavor.equals(flavors[NODESET])) {
 			return nodeSet.clone();
 			
 		} else {
 			throw new UnsupportedFlavorException(flavor);
 		}
 	}
 }
