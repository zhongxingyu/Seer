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
package mostrare.operations;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * Provides log-sum-exp methods.
 * 
 * @author missi
 */
public class TrickUtil
{
	public static double logSumExp(double[] values)
	{
		// get max value
		double currentMax = values[0];
		for (int i = 1; i < values.length; i += 1)
		{
			double val = values[i];
			if (currentMax < val)
				currentMax = val;
		}
		if (currentMax == Double.NEGATIVE_INFINITY)
			return Double.NEGATIVE_INFINITY;
		// use the trick
		double res = currentMax;
		double tempRes = 0.0;
		for (int i = 0; i < values.length; i += 1)
			tempRes = tempRes + StrictMath.exp(values[i] - currentMax);
		res = res + StrictMath.log(tempRes);
		return res;
	}

	public static double logSumExp(DoubleMatrix1D mat)
	{
		// get max value
		double currentMax = mat.getQuick(0);
		for (int i = 1; i < mat.size(); i += 1)
		{
			double val = mat.getQuick(i);
			if (currentMax < val)
				currentMax = val;
		}
		if (currentMax == Double.NEGATIVE_INFINITY)
			return Double.NEGATIVE_INFINITY;
		// use the trick
		double res = currentMax;
		double tempRes = 0.0;
		for (int i = 0; i < mat.size(); i += 1)
			tempRes = tempRes + StrictMath.exp(mat.getQuick(i) - currentMax);
		res = res + StrictMath.log(tempRes);
		return res;
	}

	public static double logSumExp(double[] values, int start, int end)
	{
		// get max value
		double currentMax = values[start];
		for (int i = start + 1; i < end; i += 1)
		{
			double val = values[i];
			if (currentMax < val)
				currentMax = val;
		}
		if (currentMax == Double.NEGATIVE_INFINITY)
			return Double.NEGATIVE_INFINITY;
		// use the trick
		double res = currentMax;
		double tempRes = 0.0;
		for (int i = start; i < end; i += 1)
			tempRes = tempRes + StrictMath.exp(values[i] - currentMax);
		res = res + StrictMath.log(tempRes);
		return res;
	}

}
