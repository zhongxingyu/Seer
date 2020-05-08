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
package mostrare.operations.parts.tree;

import mostrare.tree.Node;

/**
 * 
 * @author missi
 *
 */
public interface MParts
{

	/**
	 * Returns the stored value relative to <code>logM1(nodeIndex,annotationIndex)</code>.
	 * 
	 * @param nodeIndex
	 * @param annotationIndex
	 *            the index of the annotation
	 * @return the stored value relative to <code>logM1(nodeIndex,annotationIndex)</code>.
	 */
	public abstract double getLogM1Quick(int nodeIndex, int annotationIndex);

	/**
	 * Calculates the value of <code>logM1(node.index,annotationIndex)</code>.
	 * 
	 * @param node
	 *            node
	 * @param annotationIndex
	 *            the index of the annotation
	 * @return the value of <code>logM1(node.index,annotationIndex)</code>.
	 */
	public abstract double calcLogM1(Node node, int annotationIndex);

	/**
	 * Returns the value of <code>logM1(nodeIndex,annotationIndex)</code>. It returns a stored
	 * value if it is not 0.0 or it calculates M1.
	 * 
	 * @param nodeIndex
	 * @param annotationIndex
	 *            the index of the annotation
	 * @return the value of <code>logM1(nodeIndex,annotationIndex)</code>.
	 */
	public abstract double getLogM1(int nodeIndex, int annotationIndex);

	/**
	 * Returns the stored value relative to
	 * <code>logM2(childIndex,parentAnnotationIndex,childAnnotationIndex)</code>.
	 * 
	 * @param childIndex
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @return the stored value relative to
	 *         <code>logM2(childIndex,parentAnnotationIndex,childAnnotationIndex)</code>.
	 */
	public abstract double getLogM2Quick(int childIndex, int parentAnnotationIndex,
			int childAnnotationIndex);

	/**
	 * Calculates the value of
	 * <code>logM2(child.index,parentAnnotationIndex,childAnnotationIndex)</code>.
	 * 
	 * @param child
	 *            the child node
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @return the value of
	 *         <code>logM2(child.index,parentAnnotationIndex,childAnnotationIndex)</code>.
	 */
	public abstract double calcLogM2(Node child, int parentAnnotationIndex, int childAnnotationIndex);

	/**
	 * Returns the value of
	 * <code>logM2(childIndex,parentAnnotationIndex,childAnnotationIndex)</code>. It returns a
	 * stored value if it is not 0.0 or it calculates M2.
	 * 
	 * @param childIndex
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @return the value of
	 *         <code>logM2(childIndex,parentAnnotationIndex,childAnnotationIndex)</code>.
	 */
	public abstract double getLogM2(int childIndex, int parentAnnotationIndex,
			int childAnnotationIndex);

	/**
	 * Returns the stored value relative to
	 * <code>logM3(childIndex,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 * 
	 * @param childIndex
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @param siblingAnnotationIndex
	 *            the index of the sibling annotation
	 * @return the stored value relative to
	 *         <code>logM2(childIndex,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 */
	public abstract double getLogM3Quick(int childIndex, int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex);

	/**
	 * Calculates the value of
	 * <code>logM3(child.index,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 * 
	 * @param node
	 *            the child node
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @param siblingAnnotationIndex
	 *            the index of the sibling annotation
	 * @return the value of
	 *         <code>logM3(child.index,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 */
	public abstract double calcLogM3(Node node, int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex);

	/**
	 * Returns the value of
	 * <code>logM3(childIndex,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 * It returns a stored value if it is not 0.0 or it calculates M3.
	 * 
	 * @param childIndex
	 * @param parentAnnotationIndex
	 *            the index of the parent annotation
	 * @param childAnnotationIndex
	 *            the index of the child annotation
	 * @param siblingAnnotationIndex
	 *            the index of the sibling annotation
	 * @return the value of
	 *         <code>logM3(childIndex,parentAnnotationIndex,childAnnotationIndex,siblingAnnotationIndex)</code>.
	 */
	public abstract double getLogM3(int childIndex, int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex);

	/**
	 * Calculates every possible value of logM1.
	 */
	public abstract void fillLogM1();

	/**
	 * Calculates every possible value of logM2.
	 */
	public abstract void fillLogM2();

	/**
	 * Calculates every possible value of logM3.
	 */
	public abstract void fillLogM3();

	public abstract void fill();

}