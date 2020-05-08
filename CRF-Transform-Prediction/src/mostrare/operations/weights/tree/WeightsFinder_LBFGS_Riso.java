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
package mostrare.operations.weights.tree;

import jat.alg.opt.LBFGS.LBFGS;
import mostrare.crf.tree.CRF;
import mostrare.operations.tree.ValuesCalc;
import mostrare.tree.Corpus;
import mostrare.util.ConfigurationTool;

import org.apache.log4j.Logger;

/**
 * Uses the LBFGS implantation of <a href="http://riso.sourceforge.net">RISO</a>. The code used
 * here comes from <a href="http://jat.sourceforge.net">Jat</a> (because it has commentaries :)).
 * 
 * @author missi
 */
public class WeightsFinder_LBFGS_Riso extends AbstractWeightsFinder
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger				= Logger
															.getLogger(WeightsFinder_LBFGS_Riso.class);
	/**
	 * Maximum iterations number
	 */
	private final int			BFGS_CORR			= 7;

	private final double		MACHINE_PRECISION	= 1.0e-16;

	private final double		ACCURACY			= 1.0e-8;

	private boolean				verboseMode;

	private ValuesCalc			valuesCalc;

	public WeightsFinder_LBFGS_Riso(GradientsCalc gradCalc, ValuesCalc valuesCalc)
	{
		setGradientsCalc(gradCalc);
		this.valuesCalc = valuesCalc;
		verboseMode = false;
	}

	public WeightsFinder_LBFGS_Riso(GradientsCalc gradCalc, ValuesCalc valuesCalc,
			boolean verboseMode)
	{
		this(gradCalc, valuesCalc);
		this.verboseMode = verboseMode;
	}

	@Override
	public void argMaxLogLikelihood(Corpus corpus, CRF crf)
	{
		int MAX_ITER = ConfigurationTool.getInstance().getIterMax();

		double[] gradients1;
		double[] gradients1Pure;
		double[] gradients1Inverse;
		double[] gradients2;
		double[] gradients3;
		double[] gradients3Observation;

		int features1Number = crf.getFeatures1Number();
		int features1NumberPure = crf.getFeatures1PureNumber();
		int features1NumberInverse = crf.getFeatures1InverseNumber();
		int features2Number = crf.getFeatures2Number();
		int features3Number = crf.getFeatures3Number();
		int features3NumberObservation = crf.getFeatures3ObservationNumber();

		gradients1Inverse = new double[features1NumberInverse];
		gradients1 = new double[features1Number];
		gradients1Pure = new double[features1NumberPure];
		gradients2 = new double[features2Number];
		gradients3 = new double[features3Number];
		gradients3Observation = new double[features3NumberObservation];

		double logLikeLihood;
		// number of steps already made
		int icall = 0;
		// number of variables (= number of features = number of weights)
		int nbVariables = features1Number + features1NumberPure + features1NumberInverse + 
				features2Number + features3Number + features3NumberObservation;

		if (verboseMode)
			System.out.println("Features number: " + nbVariables);

		//
		double[] diag = new double[nbVariables];
		// configure possible outputs
		int[] iprint = new int[2];
		iprint[0] = -1;
		iprint[1] = -3;
		int[] iflag = new int[1];
		iflag[0] = 0;

		// puts the weights of each feature in an unique array
		double[] weights = new double[nbVariables];
		for (int i = 0; i < features1Number; i += 1)
			weights[i] = crf.getWeight1(i);
		
		for (int i = 0; i < features1NumberPure; i += 1)
			weights[i+features1Number] = crf.getWeight1Pure(i);
		
		for (int i = 0; i < features1NumberInverse; i += 1)
			weights[i+features1Number+features1NumberPure] = crf.getWeight1Inverse(i);
		
		for (int i = 0; i < features2Number; i += 1)
			weights[i + features1Number + features1NumberPure + features1NumberInverse] = crf.getWeight2(i);
		
		for (int i = 0; i < features3Number; i += 1)
			weights[i + features1Number + features1NumberPure + features1NumberInverse + features2Number] = crf.getWeight3(i);
		
		for (int i = 0; i < features3NumberObservation; i += 1)
			weights[i + features1Number + features1NumberPure + features1NumberInverse + features2Number+ features3Number] 
					= crf.getWeight3Observation(i);

		do
		{
			// get the value of logLikelihood and of gradients

			logLikeLihood = -calcGradients(corpus, gradients1, gradients1Pure, gradients1Inverse, gradients2, gradients3, gradients3Observation);
			if (verboseMode)
				System.out.println("Step " + icall + " : log-likelihood = " + (-logLikeLihood));

			// concat the gradients and modify values to use LBFGS
			double[] gradients = new double[nbVariables];
			for (int i = 0; i < features1Number; i += 1)
				gradients[i] = -gradients1[i];
			
			for (int i = 0; i < features1NumberPure; i += 1)
				gradients[i + features1Number] = -gradients1Pure[i];
			
			for (int i = 0; i < features1NumberInverse; i += 1)
				gradients[i + features1Number + features1NumberPure] = -gradients1Inverse[i];
			
			for (int i = 0; i < features2Number; i += 1)
				gradients[i + features1Number + features1NumberPure + features1NumberInverse] = -gradients2[i];
			
			for (int i = 0; i < features3Number; i += 1)
				gradients[i + features1Number + features1NumberPure + features1NumberInverse + features2Number] = -gradients3[i];
			
			for (int i = 0; i < features3NumberObservation; i += 1)
				gradients[i + features1Number + features1NumberPure + features1NumberInverse + features2Number + features3Number] = -gradients3Observation[i];
			//
			try
			{
				LBFGS.lbfgs(nbVariables, BFGS_CORR, weights, logLikeLihood, gradients, false, diag,
						iprint, ACCURACY, MACHINE_PRECISION, iflag);
			}
			catch (LBFGS.ExceptionWithIflag e)
			{
				System.err
						.println("lbfgs stopped irregularly. See log file for further information.");
				String errorMsg = "CRF: lbfgs failed.\n";
				if (e.iflag == -1)
				{
					errorMsg += "Possible reasons could be: \n \t 1. Bug in the feature generation or data handling code\n\t 2. Not enough features to make observed feature value==expected value\n";
				}
				logger.error(errorMsg, e);
				break;
			}

			// updates the feature weights
			for (int i = 0; i < features1Number; i += 1)
				crf.setWeight1(i, weights[i]);
			
			for (int i = 0; i < features1NumberPure; i += 1)
				crf.setWeight1Pure(i, weights[i + features1Number]);
			
			for (int i = 0; i < features1NumberInverse; i += 1)
				crf.setWeight1Inverse(i, weights[i + features1Number + features1NumberPure]);
			
			for (int i = 0; i < features2Number; i += 1)
				crf.setWeight2(i, weights[i + features1Number + features1NumberPure + features1NumberInverse]);
			
			for (int i = 0; i < features3Number; i += 1)
				crf.setWeight3(i, weights[i + features1Number + features1NumberPure + features1NumberInverse + features2Number]);

			for (int i = 0; i < features3NumberObservation; i += 1)
				crf.setWeight3Observation(i, weights[i + features1Number + features1NumberPure + features1NumberInverse + features2Number + features3Number]);
			
			icall += 1;
			
		}
		while ((iflag[0] != 0) && (icall < MAX_ITER));
		/*
		if (verboseMode)
			System.out.println("Step " + icall + " : log-likelihood = "
					+ (valuesCalc.logLikelihood(corpus, crf)));
	*/
	}

}
