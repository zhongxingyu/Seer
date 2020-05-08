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

import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.operations.parts.tree.FlexAlphaParts;
import mostrare.operations.parts.tree.FlexBetaParts;
import mostrare.operations.parts.tree.FlexMParts;
import mostrare.operations.parts.tree.withConst_globalIt.DeltaPartsImpl_KBest;
import mostrare.operations.tree.ValuesCalc;
import mostrare.operations.tree.ValuesCalc_locCons;
import mostrare.operations.weights.tree.GradientsCalc;
import mostrare.operations.weights.tree.TreeGradientsCalcCompleteLog_locCons_globalIt_Impl;
import mostrare.util.ConfigurationTool;

/**
 * Generates computation objects for a specific task according to the crf and to the config.
 * 
 * @author missi
 */
public class TaskFactory
{
	private static TaskFactory			instance;
	private static ConfigurationTool	config;

	static
	{
		instance = new TaskFactory();
	}

	private TaskFactory()
	{
		config = ConfigurationTool.getInstance();
	}

	public static TaskFactory getInstance()
	{
		return instance;
	}

	public TrainTools createToolsForTraining(CRF crf)
	{
		ValuesCalc valuesCalc;
		FlexMParts mparts;
		FlexBetaParts betaParts;
		FlexAlphaParts alphaParts;
		GradientsCalc gradCalc;
		
		CRFWithConstraintNode crfConst = (CRFWithConstraintNode) crf;
		valuesCalc = ValuesCalc_locCons.getInstance();
		mparts = new mostrare.operations.parts.tree.withConst_globalIt.FlexMPartsImpl(crfConst);
		betaParts = new mostrare.operations.parts.tree.withConst_globalIt.FlexBetaPartsImpl(crfConst, mparts);
		alphaParts = new mostrare.operations.parts.tree.withConst_globalIt.FlexAlphaPartsImpl(crfConst, mparts, betaParts);
		gradCalc = new TreeGradientsCalcCompleteLog_locCons_globalIt_Impl(crfConst, mparts, betaParts, alphaParts, valuesCalc);

		return new TrainToolsImpl(valuesCalc, mparts, alphaParts, betaParts, gradCalc);
	}

	public AnnotateTools createToolsForAnnotation(CRF crf)
	{
		ValuesCalc valuesCalc;
		FlexMParts mparts;
		FlexBetaParts betaParts;
		FlexAlphaParts alphaParts;
		DeltaPartsImpl_KBest deltaParts;
		
		CRFWithConstraintNode crfConst = (CRFWithConstraintNode) crf;
	
		valuesCalc = ValuesCalc_locCons.getInstance();
		mparts = new mostrare.operations.parts.tree.withConst_globalIt.FlexMPartsImpl(crfConst);
		betaParts = new mostrare.operations.parts.tree.withConst_globalIt.FlexBetaPartsImpl(crfConst, mparts);
		alphaParts = new mostrare.operations.parts.tree.withConst_globalIt.FlexAlphaPartsImpl(crfConst, mparts, betaParts);
		deltaParts = new mostrare.operations.parts.tree.withConst_globalIt.DeltaPartsImpl_KBest (crfConst, mparts);

		return new AnnotateToolsImpl(valuesCalc, mparts, alphaParts, betaParts, deltaParts);
	}
}
