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

import java.lang.reflect.InvocationTargetException;

import mostrare.operations.tree.ValuesCalc;

import org.apache.log4j.Logger;

/**
 * @author missi
 */
public class WeightsFinderFactory
{
	/**
	 * Logger for this class
	 */
	private static final Logger			logger			= Logger
																.getLogger(WeightsFinderFactory.class);

	private static WeightsFinderFactory	instance		= null;

	public static final String			RISO_VERSION	= "Riso";

	enum LBFGSVersion {
		Riso(WeightsFinder_LBFGS_Riso.class);

		private Class	classz;

		LBFGSVersion(Class classz)
		{
			this.classz = classz;
		}

		public WeightsFinder create(GradientsCalc gradCalc, ValuesCalc valuesCalc,
				boolean verboseMode)
		{
			try
			{
				return (WeightsFinder) classz.getConstructor(GradientsCalc.class, ValuesCalc.class,
						boolean.class).newInstance(gradCalc, valuesCalc, verboseMode);
			}
			catch (IllegalArgumentException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			catch (SecurityException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			catch (InstantiationException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			catch (IllegalAccessException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			catch (InvocationTargetException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			catch (NoSuchMethodException e)
			{
				logger.error("Can't create WeightsFinder.", e);
			}
			return null;
		}
	}

	static
	{
		instance = new WeightsFinderFactory();
	}

	private WeightsFinderFactory()
	{

	}

	public static WeightsFinderFactory getInstance()
	{
		return instance;
	}

	public WeightsFinder create(GradientsCalc gradCalc, ValuesCalc valuesCalc,
			String LBFGS_version, boolean verboseMode)
	{
		LBFGSVersion lbfgs = LBFGSVersion.valueOf(LBFGS_version);
		if (lbfgs == null)
			return null;
		return lbfgs.create(gradCalc, valuesCalc, verboseMode);
	}

	public boolean isVersionUsable(String version)
	{
		try
		{
			return LBFGSVersion.valueOf(version) != null;
		}
		catch (IllegalArgumentException e)
		{
			logger.error(version + " is not associated with a LBFGS implem.", e);
			return false;
		}
	}
}
