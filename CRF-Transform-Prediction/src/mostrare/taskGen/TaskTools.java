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
package mostrare.taskGen;

import mostrare.operations.parts.tree.FlexAlphaParts;
import mostrare.operations.parts.tree.FlexBetaParts;
import mostrare.operations.parts.tree.FlexMParts;
import mostrare.operations.tree.ValuesCalc;

/**
 * Returns computation objects for a task.
 * 
 * @author missi
 */
public interface TaskTools
{
	/**
	 * Returns log-likelihood computer.
	 * @return
	 */
	public ValuesCalc getValuesCalc();

	/**
	 * Returns m coef values.
	 * @return
	 */
	public FlexMParts getMParts();

	/**
	 * Returns alpha values.
	 * @return
	 */
	public FlexAlphaParts getAlphaParts();

	/**
	 * Returns beta values.
	 * @return
	 */
	public FlexBetaParts getBetaParts();

}
