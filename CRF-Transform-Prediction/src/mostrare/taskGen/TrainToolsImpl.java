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
import mostrare.operations.weights.tree.GradientsCalc;

/**
 * @author missi
 */
class TrainToolsImpl extends AbstractTaskTools implements TrainTools
{

	protected GradientsCalc	gradCalc;

	TrainToolsImpl(ValuesCalc valuesCalc, FlexMParts mparts, FlexAlphaParts alphaParts,
			FlexBetaParts betaParts, GradientsCalc gradCalc)
	{
		super(valuesCalc, mparts, alphaParts, betaParts);
		this.gradCalc = gradCalc;
	}

	@Override
	public GradientsCalc getGradientsCalc()
	{
		return gradCalc;
	}

}
