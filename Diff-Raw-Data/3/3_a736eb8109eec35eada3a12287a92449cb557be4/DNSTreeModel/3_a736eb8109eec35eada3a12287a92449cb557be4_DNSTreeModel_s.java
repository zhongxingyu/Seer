 /* **************************************************************************
  *                                                                          *
  *  Copyright (C)  2011  Nils Foken, Andr Kielich,                        *
  *                       Peter Kossek, Hans Laser                           *
  *                                                                          *
  *  Nils Foken       <nils.foken@it2009.ba-leipzig.de>                      *
  *  Andr Kielich   <andre.kiesslich@it2009.ba-leipzig.de>                 *
  *  Peter Kossek     <peter.kossek@it2009.ba-leipzig.de>                    *
  *  Hans Laser       <hans.laser@it2009.ba-leipzig.de>                      *
  *                                                                          *
  ****************************************************************************
  *                                                                          *
  *  This file is part of 'javadns'.                                         *
  *                                                                          *
  *  This project is free software: you can redistribute it and/or modify    *
  *  it under the terms of the GNU General Public License as published by    *
  *  the Free Software Foundation, either version 3 of the License, or       *
  *  any later version.                                                      *
  *                                                                          *
  *  This project is distributed in the hope that it will be useful,         *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of          *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
  *  GNU General Public License for more details.                            *
  *                                                                          *
  *  You should have received a copy of the GNU General Public License       *
  *  along with this project. If not, see <http://www.gnu.org/licenses/>.    *
  *                                                                          *
  ****************************************************************************/
 
  package de.baleipzig.javadns;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.naming.directory.Attribute;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  * A 2-level tree model for the {@link DomainRecord}.
  * The first level contains the hosts, the second level contains the host's records. 
  */
 public class DNSTreeModel implements TreeModel {
 	private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();
 	private Object root;
 
 	public DNSTreeModel(Object root) {
 		this.root = root;
 	}
 	
 	@Override
 	public Object getRoot() {
 		return root;
 	}
 
 	@Override
 	public boolean isLeaf(Object node) {
 		// root and hosts are no leaves
 		if (node.equals(root) || DomainRecord.getRecords().containsKey(node)) {
 			return false;
 		}
 		// attributes are leaves
 		return true;
 		
 	}
 	
 	@Override
 	public int getChildCount(Object parent) {
 		HashMap<String, HashMap<String, Attribute>> records = DomainRecord
 				.getRecords();
 		// children of root = hosts
 		if (parent.equals(root)) {
 			return records.size(); 
 		}
 		// children of host = attributes
 		return records.get(parent).size();
 	}
 	
 	@Override
 	public Object getChild(Object parent, int index) {
 		HashMap<String, HashMap<String, Attribute>> records = DomainRecord
 				.getRecords();
 		// parent is root --> child is host
 		if (parent.equals(root)) {
 			Object[] hostArray = records.keySet().toArray();
 			Arrays.sort(hostArray);
 			return hostArray[index];
 		}
 		// parent is host --> child is attribute
 		else {
 			Object[] attributeKeys = records.get(parent).keySet().toArray();
 			return records.get(parent).get(attributeKeys[index]);
 		}
 	}
 	
 	@Override
 	public int getIndexOfChild(Object parent, Object child) {
 		HashMap<String, HashMap<String, Attribute>> records = DomainRecord
 				.getRecords();
 		// parent is root --> child is host
 		if (parent.equals(root)) {
 			Object[] hostArray = records.keySet().toArray();
 			Arrays.sort(hostArray);
 			for (int i=0; i<hostArray.length; i++) {
 				if (child.equals(hostArray[i])) {
 					return i;
 				}
 			}
 		}
 		// parent is host --> child is attribute
 		else {
 			Object[] attributeKeys = records.get(parent).keySet().toArray();
 			for (int i=0; i<attributeKeys.length; i++) {
 				if (records.get(parent).get(attributeKeys[i]).equals(child)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 	
 	@Override
 	public void addTreeModelListener(TreeModelListener listener) {
 		treeModelListeners.addElement(listener);
 	}
 
 	@Override
 	public void removeTreeModelListener(TreeModelListener listener) {
 		treeModelListeners.removeElement(listener);
 	}
 
 	@Override
 	public void valueForPathChanged(TreePath path, Object newValue) {
 		System.out.println("*** valueForPathChanged : " + path + " --> "
 				+ newValue);
 	}
 	
 	/**
 	 * Notify all listeners that the tree's strucutre has changed.
 	 * @param nodeDown The node where the change has happened below.
 	 */
 	protected void fireTreeStructureChanged(Object nodeDown) {
 		TreeModelEvent evt = new TreeModelEvent(this, new Object[] { nodeDown });
 		for (TreeModelListener tml : treeModelListeners) {
 			tml.treeStructureChanged(evt);
 		}
 	}
 
 }
