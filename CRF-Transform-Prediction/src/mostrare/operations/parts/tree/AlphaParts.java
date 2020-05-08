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
 * @author missi
 */
public interface AlphaParts
{
	/**
	 * Computes every alpha value. Must be called before {@link #getLogAlpha(Node, int)}.
	 */
	public abstract void fillLogAlphas();

	/**
	 * Returns logAlpha[<code>node</code>.index,<code>annotationIndex</code>].
	 * 
	 * @param node
	 * @param annotationIndex
	 * @return logAlpha[<code>node</code>.index,<code>annotationIndex</code>].
	 */
	public abstract double getLogAlpha(Node node, int annotationIndex);

}