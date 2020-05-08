/*
 * Copyright (C) 2006 MOSTRARE INRIA Project This file is part of XCRF, an implementation of CRFs
 * for trees (http://treecrf.gforge.inria.fr) XCRF is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. XCRF is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with XCRF; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package xcrf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFGenerator;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.operations.weights.tree.WeightsFinder;
import mostrare.operations.weights.tree.WeightsFinderFactory;
import mostrare.taskGen.TaskFactory;
import mostrare.taskGen.TrainTools;
import mostrare.tree.Corpus;
import mostrare.tree.impl.CorpusFactory;
import mostrare.tree.impl.TreeAST;
import mostrare.tree.io.impl.CRFToJsonWriter;
import mostrare.tree.io.impl.DefaultTreeReader;
import mostrare.util.ConfigurationTool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LearnTree
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger							= Logger.getLogger(LearnTree.class);

	private static final char	OPT_VERBOSE						= 'v';
	
	public static double cofficentforsingle = 0.0;
	
	public static double cofficentformultiple = 0.0;

	public static void main(String args[])
	{
		Calendar c = new GregorianCalendar();
		long start = c.getTimeInMillis();

		// load log4j config
		PropertyConfigurator.configure("config.log4j");

		execute(args);

		c = new GregorianCalendar();
		long end = c.getTimeInMillis();
		System.out.println("Learning time:" + ((end - start) / 60000.0) + " min");
	}

	public static boolean execute(String[] args)
	{
		// parse the command line
		Options options = createOptions();
		CommandLine cmd;
		try
		{
			cmd = getCommandLine(args, options);
		}
		catch (ParseException e2)
		{
			logger.error("Command line parser fails", e2);
			usage(options);
			return false;
		}

		// test config file
		ConfigurationTool config = ConfigurationTool.getInstance();
		if (config == null)
			return false;
		if (!config.isConfigurationValid())
		{
			System.err.println("Configuration file is not valid.");
			return false;
		}

		boolean verboseMode = isVerbose(cmd);

		if (cmd.getArgs().length != 6)
		{
			System.err.println("Not the right number of arguments");
			usage(options);
			return false;
		}
		String crfPath = getCRFPath(cmd);
		String corpusPath = getCorpusPath(cmd);
		String resultPath = getResultPath(cmd);
		
		config.setPenalty(getPenaltyVale(cmd));
		config.setIterMax(getIterationNumber(cmd));
		config.setDegreeDistribution(getDegreeDistributionPrior(cmd));

		appli(cmd, crfPath, corpusPath, resultPath);

		// generate the crf
		CRFWithConstraintNode crf = CRFGenerator.getInstance().generateCRF(crfPath);
		if (crf == null) 
			return false;
		// then generate the objects that rely on the crf
		TrainTools trainObj = TaskFactory.getInstance().createToolsForTraining(crf);
		
		// Add a hook to save the crf when ctrl-c
		Thread hook = new LearnTree().new RunWhenShuttingDown(crf, resultPath);
		Runtime.getRuntime().addShutdownHook(hook);

		if (!new File(corpusPath).exists())
		{
			System.err.println(corpusPath + " is not a valid path.");
			Runtime.getRuntime().removeShutdownHook(hook);
			return false;
		}
		// create the corpus
		Corpus corpus = CorpusFactory.getInstance().buildCorpus(corpusPath,
				DefaultTreeReader.getInstance(), crf);
		if (corpus.getTreesNumber() == 0)
		{
			System.err.println("0 valid document that can be used with the crf in the corpus.");
			Runtime.getRuntime().removeShutdownHook(hook);
			return false;
		}

		try
		{
			if (crf.observationTestsShouldBeApplied())
			{
				crf.initOperationTestsEvaluation(corpus.getTreesNumber());

				for (int i = 0; i < corpus.getTreesNumber(); i += 1)
				{
					crf.applyObservationTests(corpus.getTree(i));
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to evaluate observation tests", e);
			System.err
					.println("Observation tests preprocess fails. See log file for further information.");
		}
		
		try
		{
			establishDistributionPrior(corpus, config.getDegreeDistribution());
			
			WeightsFinder weightsFinder = WeightsFinderFactory.getInstance().create(
					trainObj.getGradientsCalc(), trainObj.getValuesCalc(),
					config.getSearchMethod(), verboseMode);
			
			if (weightsFinder == null)
			{
				Runtime.getRuntime().removeShutdownHook(hook);
				return false;
			}
			
			weightsFinder.argMaxLogLikelihood(corpus, crf);
		}
		catch (Exception e)
		{
			System.err.println("Uncaught error. Ask developers for help.");
			Runtime.getRuntime().removeShutdownHook(hook);
			return false;
		}
		catch (OutOfMemoryError e)
		{
			System.err
					.println("Not enough available memory. Try to increase it with the java option -Xmx.");
			Runtime.getRuntime().removeShutdownHook(hook);
			return false;
		}
		
		try
		{
			new CRFToJsonWriter().writeCRFToJSON(crf, resultPath);
		}
		catch (Exception e)
		{
			System.err.println("Doesn't succeed in saving the result.");
			Runtime.getRuntime().removeShutdownHook(hook);
			return false;
		}

		Runtime.getRuntime().removeShutdownHook(hook);

		return true;
	}

	private static void appli(CommandLine cmd, String crfPath, String corpusPath, String resultPath)
	{
		System.out.println("Apply on:");
		System.out
				.println("\t.CRF specified by the file at " + new File(crfPath).getAbsolutePath());
		System.out.println("\t.Corpus specified by the directory at " +
				new File(corpusPath).getAbsolutePath());
		System.out.println("\t.Result stored in the file at " +
				new File(resultPath).getAbsolutePath());
	}

	private static void usage(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"Arguments are: [-v] [-d attributeNameForInternalNode attributeNameForTextLeaf attributeNameForAttribute | -c annotationReaderPath] crfPath corpusPath resultPath",
						options);
	}

	/**
	 * Parse the arguments <code>args</code> of the command line.
	 * 
	 * @param args
	 * @return the command line
	 * @throws ParseException
	 */
	private static CommandLine getCommandLine(String args[], Options options) throws ParseException
	{
		// parse
		CommandLineParser clParser = new PosixParser();
		return clParser.parse(options, args);
	}

	private static Options createOptions()
	{
		
	    Options options = new Options();

		return options;
	}

	private static boolean isVerbose(CommandLine cmd)
	{
		return cmd.hasOption(OPT_VERBOSE);
	}

	private static String getCRFPath(CommandLine cmd)
	{
		return cmd.getArgs()[0];
	}

	private static String getCorpusPath(CommandLine cmd)
	{
		return cmd.getArgs()[1];
	}

	private static String getResultPath(CommandLine cmd)
	{
		return cmd.getArgs()[2];
	}
	
	private static Double getPenaltyVale (CommandLine cmd)
	{
		return Double.parseDouble(cmd.getArgs()[3]);
	}
	
	private static Integer getIterationNumber (CommandLine cmd)
	{
		return Integer.parseInt(cmd.getArgs()[4]);
	}
	
	private static Double getDegreeDistributionPrior (CommandLine cmd)
	{
		return Double.parseDouble(cmd.getArgs()[5]);
	}

	public class RunWhenShuttingDown extends Thread
	{
		private CRF		crf;
		private String	resultPath;

		public RunWhenShuttingDown(CRF crf, String resultPath)
		{
			this.crf = crf;
			this.resultPath = resultPath;
		}

		public void run()
		{
			System.out.println("Control-C caught. Shutting down...");
			BufferedWriter b;
			try
			{
				File resultFile = new File(resultPath).getAbsoluteFile();
				resultFile.getParentFile().mkdirs();
				b = new BufferedWriter(new FileWriter(resultPath));
				b.write(crf.toString());
				b.flush();
				b.close();
			}
			catch (IOException e)
			{
				System.err.println("Doesn't succeed in saving the temporary result.");
			}
		}
	}
	
	private static void establishDistributionPrior(Corpus corpustostudy, double degreedistributionprior) {
		
		int numberofmultipletransform=0;
		int numberofsingletransform=0;
		
		for (int i = 0; i < corpustostudy.getTreesNumber(); i += 1)
		{
			TreeAST tree = (TreeAST) corpustostudy.getTree(i);
			
			if(tree.getNumberofTransform()>1)
				numberofmultipletransform+=1;
			else
				numberofsingletransform+=1;
		}
		
		double averagenumber=corpustostudy.getTreesNumber()/2.0;
		
		setCofficentSingle(Math.pow(averagenumber/numberofsingletransform, (double)degreedistributionprior));
		
		setCofficentMultiple(Math.pow(averagenumber/numberofmultipletransform, (double)degreedistributionprior));
	}
	
	public static double getCofficentSingle() {
		return LearnTree.cofficentforsingle;
	}
	
	public static void setCofficentSingle (double valueforsingle) {
		LearnTree.cofficentforsingle = valueforsingle;
	}

	public static double getCofficentMultiple() {
		return LearnTree.cofficentformultiple;
	}
	
	public static void setCofficentMultiple(double valueformultiple) {
		 LearnTree.cofficentformultiple = valueformultiple;
	}
 }
