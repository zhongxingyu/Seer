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
package mostrare.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import mostrare.operations.weights.tree.WeightsFinderFactory;
import org.apache.log4j.Logger;

/**
 * @author missi
 */
public class ConfigurationTool
{
	/**
	 * Logger for this class
	 */
	private static final Logger			logger					= Logger
																		.getLogger(ConfigurationTool.class);

	private static final String			pathToConfigFile		= "config.xml";

	private static ConfigurationTool	instance				= null;

	private Properties					prop					= null;

//	private final String				PROP_PENALTY			= "penalty";
//
//	private final String				PROP_ITERMAX			= "iterMax";

	private final String				PROP_GRADIENTSEARCH		= "search";
	
//	private final String				OUTPUT_NUMER		= "topk";
		
	private static Double penalty=0.0; 
	
	private static Integer maxiteration=0;
	
	private static Double degreePrior = 0.0;
	
	private static Integer topk = 0;

	private ConfigurationTool() throws InvalidPropertiesFormatException, FileNotFoundException,
			IOException
	{
		prop = new Properties();
	//	prop.setProperty(PROP_PENALTY, "100000.0");
	//	prop.setProperty(PROP_PENALTY, "4.0");
	//	prop.setProperty(PROP_PENALTY, "500.0");
	//	prop.setProperty(PROP_PENALTY, "10.0");
	//	prop.setProperty(PROP_ITERMAX, "100");
	//	prop.setProperty(PROP_ITERMAX, "300");
		prop.setProperty(PROP_GRADIENTSEARCH, "Riso");
	//	prop.setProperty(OUTPUT_NUMER, "11");
	}

	public static ConfigurationTool getInstance()
	{
		if (instance == null)
			try
			{
				instance = new ConfigurationTool();
			}	
			catch (Exception e)
			{
				System.err.println("Doesn't succeed in reading the config file");
				logger.error("Doesn't succeed in reading the config file " + pathToConfigFile, e);
			}
		return instance;
	}

	public String getSearchMethod()
	{
		return prop.getProperty(PROP_GRADIENTSEARCH);
	}
	
//	public int getOutputNumber()
//	{	
//		return Integer.parseInt(prop.getProperty(OUTPUT_NUMER));	
//	}
	
	public void setOutputNumber(Integer outputnumber)
	{	
		ConfigurationTool.topk = outputnumber;
	}
	
	public Integer getOutputNumber()
	{	
		return ConfigurationTool.topk ;	
	}
	
	public void setPenalty(Double penaltyvalue) {
		
		ConfigurationTool.penalty=penaltyvalue;	
	}
	
	public Double getPenalty() {
		
		return ConfigurationTool.penalty;
	}
	
    public void setDegreeDistribution(Double degree) {
		
		ConfigurationTool.degreePrior=degree;	
	}
	
	public Double getDegreeDistribution() {
		
		return ConfigurationTool.degreePrior;
	}

//	public Double getPenalty()
//	{
//		String penalty = "";
//		try
//		{
//			penalty = prop.getProperty(PROP_PENALTY);
//			return penalty == null ? null : Double.parseDouble(penalty);
//		}
//		catch (NumberFormatException e)
//		{
//			logger.error(penalty + " is not a valid number.", e);
//			return null;
//		}
//	}

	public void setIterMax (Integer iteration) {	
		ConfigurationTool.maxiteration = iteration;	
	}
	
	public Integer getIterMax()
	{	
		return ConfigurationTool.maxiteration;	
	}
	
//	public Integer getIterMax()
//	{
//		String iterMax = "";
//		try
//		{
//			iterMax = prop.getProperty(PROP_ITERMAX);
//			return iterMax == null ? null : Integer.parseInt(iterMax);
//		}
//		catch (NumberFormatException e)
//		{
//			logger.error(iterMax + " is not a valid number.", e);
//			return null;
//		}
//	}

	public boolean isConfigurationValid()
	{
//		if (getPenalty() == null)
//		{
//			logger.error("Specify the log-likelihood penalty with the property \"" + PROP_PENALTY +
//					"\"");
//			return false;
//		}
//		if (getPenalty() == 0.0)
//		{
//			logger.error("Penalty value should not be equal to 0.0");
//			return false;
//		}
		if (getSearchMethod() == null)
		{
			logger.error("Specify the gradient search implementation with the property \"" +
					PROP_GRADIENTSEARCH + "\".");
			return false;
		}
		else if (!WeightsFinderFactory.getInstance().isVersionUsable(getSearchMethod()))
		{
			return false;
		}
		
//		if (getIterMax() == null)
//		{
//			logger.error("Specify the maximum number of iterations with the property \"" +
//					PROP_ITERMAX + "\"");
//			return false;
//		}
//		if (getIterMax() <= 0)
//		{
//			logger.error("Maximum number of iterations specified with the property \"" 
//					 + "\" should be strictly positive.");
//			return false;
//		}
		return true;
	}
}