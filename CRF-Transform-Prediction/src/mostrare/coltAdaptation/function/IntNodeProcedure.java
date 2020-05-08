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
package mostrare.coltAdaptation.function;

import mostrare.tree.Node;

/**
 * Interface that represents a procedure object: a procedure that takes two arguments and does not
 * return a value. This class is an adaptation of <code>cern.colt.function.ObjectProcedure</code>
 * of the colt API.
 * 
 * @author missi
 */
public interface IntNodeProcedure
{
	/**
	 * Applies a procedure to two arguments. Optionally can return a boolean flag to inform the
	 * object calling the procedure.
	 * <p>
	 * Example: forEach() methods often use procedure objects. To signal to a forEach() method
	 * whether iteration should continue normally or terminate (because for example a matching
	 * element has been found), a procedure can return <code>false</code> to indicate termination
	 * and <code>true</code> to indicate continuation.
	 * 
	 * @param first
	 *            first argument passed to the procedure.
	 * @param second
	 *            second argument passed to the procedure.
	 * @return a flag to inform the object calling the procedure.
	 */
	abstract public boolean apply(int first, Node second);
}
