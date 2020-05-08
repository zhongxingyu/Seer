/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.tree;

/**
 * @author missi
 */
public interface Tree
{
	/**
	 * Returns the root of the tree.
	 * 
	 * @return the root of the tree.
	 */
	public Node getRoot();

	/**
	 * Adds a <code>node</code> with the provided <code>index</code>.
	 * 
	 * @param node
	 */
	public void addNode(Node node);

	/**
	 * Returns the node associated with the provided <code>index</code>.
	 * 
	 * @param index
	 * @return the node associated with the provided <code>index</code>. If the
	 *         <code>index</code> is not associated with a node, it returns <code>null</code>.
	 */
	public Node getNode(int index);

	/**
	 * Returns the number of the nodes in the tree.
	 * 
	 * @return the number of the nodes in the tree.
	 */
	public int getNodesNumber();

	/**
	 * Annotates the tree using the provided <code>annotations</code>. The keys of the map must
	 * correspond to the index of the nodes of the tree. If it is not the case, the method returns
	 * <code>false</code>.
	 * 
	 * @param annotations
	 * @return <code>true</code> if the tree has been successfully annotated.
	 */
	public boolean annotate(int[] annotations);

	/**
	 * Associates an index to each node the tree contains.
	 */
	public void indexesNodes();
	
	//
	public Node[] getNodes();
	
	public int getIndex();
	
	public void setIndex(int index);
	
	public void setFileNmae(String name);
	
	public String getFileName();
	
	public void setNumberofTransform(int transformNumber);
	
	public int getNumberofTransform();
}
