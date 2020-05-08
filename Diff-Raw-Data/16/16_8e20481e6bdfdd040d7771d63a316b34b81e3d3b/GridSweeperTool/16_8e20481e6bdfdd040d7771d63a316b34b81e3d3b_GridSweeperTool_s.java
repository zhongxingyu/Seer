 package edu.umich.lsa.cscs.gridsweeper;
 
 import static edu.umich.lsa.cscs.gridsweeper.DLogger.*;
 
 import java.io.FileNotFoundException;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.util.*;
 import java.util.logging.*;
 import java.util.regex.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.StringUtils.*;
 import edu.umich.lsa.cscs.gridsweeper.GridSweeper.RunType;
 import edu.umich.lsa.cscs.gridsweeper.parameters.*;
 
 /**
  * {@code GridSweeperTool} is the principal class of the command-line
  * gsweep tool (and its gdrone, etc. variants). This class implements
  * a command-line interface on top of the {@code GridSweeper} class, which
  * implements actual program functionality.
  * @author Ed Baskerville
  *
  */
 public class GridSweeperTool
 {
 	enum ArgState
 	{
 		START,
 		ADAPTER,
 		NAME,
 		NUM_RUNS,
 		SEED,
 		MODEL,
 		INPUT,
 		OUTPUT,
 		EMAIL,
 		RESULTS
 	}
 	
 	static String className;
 	
 	static Matcher equalMatcher;
 	static Matcher numMatcher;
 
 	static
 	{
 		className = GridSweeperTool.class.getName();
 		
 		// Matches non-empty LHS = non-empty RHS, groups trimmed LHS and trimmed RHS
 		equalMatcher = Pattern.compile("\\A\\s*([^=]+?)\\s*=\\s*([^=]+?)\\s*\\z").matcher("");
 		//equalMatcher = Pattern.compile("([^=]+)=([^=]+)").matcher("");
 		
 		// Matches signed decimal number
 		numMatcher = Pattern.compile("\\A(-?\\d*\\.?\\d*)\\z").matcher("");
 		
 	}
 	
 	GridSweeper gs;
 	
 	String experimentPath;
 	String outputPath;
 	Experiment experiment;
 
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			GridSweeperTool tool = new GridSweeperTool();
 			tool.run(preParseArgs(args));
 		}
 		catch(GridSweeperException e)
 		{
 			System.err.println("Could not run GridSweeper. " + e.getMessage());
 		}
 	}
 	
 	private static StringList preParseArgs(String[] args) throws GridSweeperException
 	{
 		StringList ppArgs = new StringList(args.length);
 		
 		for(String arg : args)
 		{
 			if(arg.length() > 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-')
 			{
 				String opt = arg.substring(0, 2);
 				String optArg = arg.substring(2);
 				
 				ppArgs.add(opt);
 				ppArgs.add(optArg);
 			}
 			else ppArgs.add(arg);
 		}
 		
 		return ppArgs;
 	}
 	
 	public GridSweeperTool() throws GridSweeperException
 	{
 		gs = new GridSweeper();
 	}
 	
 	/**
 	 * Does everything: loads the experiment, runs the experiment, and (soon)
 	 * monitors the experiment.
 	 * @param args Command-line arguments.
 	 * @throws GridSweeperException If the GRIDSWEEPER_ROOT environment variable
 	 * is not set, or if parsing, loading, setup, running, or monitoring jobs
 	 * generate any other uncaught exceptions.
 	 */
 	public void run(StringList args)
 	{
 		boolean debug = false;
 		for(String arg : args)
 		{
 			if(arg.equals("-D") || arg.equals("--debug"))
 			{
 				DLogger.addConsoleHandler(Level.ALL);
 				debug = true;
 			}
 		}
 		
 		try
 		{
 			System.err.println("");
 			
 			loadExperiment(args);
 			
 			gs.setExperiment(experiment);
 			
 			// Set up and submit experiment
 			gs.submitExperiment();
 			
 			// Detach stdout and stderr so logout is possible
 			if(debug)
 			{
 				DLogger.removeConsoleHandler();
 				DLogger.addStreamHandler(Level.ALL, gs.msgOut);
 			}
 			gs.daemonize();
 			
 			// Finish running jobs
 			gs.finish();
 		}
 		catch(GridSweeperException e)
 		{
 			gs.msgOut.println(e.getMessage());
 			
 			if(debug)
 			{
 				e.printStackTrace(gs.msgOut);
 			}	
 		}
 	}
 
 	private void loadExperiment(StringList args) throws GridSweeperException
 	{
 		Settings cliSettings = new Settings();
 		Settings adapterSettings = new Settings();
 		Settings fileTransferSettings = new Settings();
 		List<Sweep> cliSweeps = new ArrayList<Sweep>();
 		parseArgs(args, cliSettings, adapterSettings, fileTransferSettings, cliSweeps);
 		
 		// Load experiment file
 		loadExperimentFile();
 		
 		// Override name if specified at command line
 		String name = cliSettings.getProperty("Name");
 		if(name != null)
 		{
 			experiment.setName(name);
 			cliSettings.remove("Name");
 		}
 		
 		// Override numRuns if specified at command line
 		String numRuns = cliSettings.getProperty("NumRuns");
 		if(numRuns != null)
 		{
 			try
 			{
 				int numRunsI = Integer.parseInt(numRuns);
 				experiment.setNumRuns(numRunsI);
 			}
 			catch(NumberFormatException e)
 			{
 				throw new GridSweeperException("Invalid number of runs specfication " + numRuns + ".");
 			}
 			cliSettings.remove("NumRuns");
 		}
 		
 		// Override location in seed table if specified at command line
 		String seedStr = cliSettings.getProperty("Seed");
 		if(seedStr != null)
 		{
 			String[] pieces = seedStr.split("\\s*:\\s*");
 			if(pieces.length != 2)
 			{
 				throw new GridSweeperException("Invalid seed table location " + seedStr);
 			}
 			String rowStr = pieces[0];
 			String colStr = pieces[1];
 			try
 			{
 				int row = Integer.parseInt(rowStr); 
 				int col = Integer.parseInt(colStr);
 				
 				experiment.setFirstSeedRow(row);
 				experiment.setSeedCol(col);
 			}
 			catch(NumberFormatException e)
 			{
 				throw new GridSweeperException("Invalid seed table location " + seedStr);
 			}
 		}
 		
 		// Combine settings from command-line arguments and experiment
 		experiment.getSettings().putAll(cliSettings);
 		
 		experiment.getSettings().putAllForClass(
 				adapterSettings,
 				experiment.getSettings().getProperty("AdapterClass"));
 		
 		experiment.getSettings().putAllForClass(
 				fileTransferSettings,
 				experiment.getSettings().getProperty("FileTransferSystemClass"));
 		
 		// Load adapter settings by prepending class prefix
 		for(Object keyObj : adapterSettings.keySet())
 		{
 			String key = (String)keyObj;
 			experiment.getSettings().setProperty(
 					experiment.getSettings().getProperty("AdapterClass")
 					+ "." + key, adapterSettings.getProperty(key));
 		}
 		
 		for(Sweep sweep : cliSweeps)
 		{
 			experiment.getRootSweep().add(sweep);
 		}
 		
 		if(experiment.getName() == null)
 		{
 			throw new GridSweeperException("Experiment name not provided; " +
 					"could not run experiment.");
 		}
 		
 		// Write experiment XML as provided by --output option
 		if(outputPath != null) try
 		{
 			experiment.writeToFile(outputPath, false);
 		}
 		catch(FileNotFoundException e)
 		{
 			throw new GridSweeperException(
 					"Could not write to output path " + outputPath + ".", e);
 		}		
 	}
 	
 	/**
 	 * <p>Parses command-line arguments.</p>
 	 * 
 	 * <p>Normally, this allows for a wide range of arguments to be provided,
 	 * as documented in the manpage.</p>
 	 * 
 	 * <p>If the first argument is {@code -r} or
 	 * {@code --reproduce}, the program is put into reproduce mode, and
 	 * accepts only the experiment file, optionally followed by the case name,
 	 * and if the case name is provided, optionally followed by 
 	 * the run numbers to reproduce, as a comma-separated list or as a range
 	 * list of the form start:increment:end.</p>
 	 * @param args Command-line arguments.
 	 */
 	void parseArgs(StringList args, Settings cliSettings, 
 			Settings adapterSettings, Settings fileTransferSettings,
 			List<Sweep> cliSweeps)
 		throws GridSweeperException
 	{
 		ArgState state = ArgState.START;
 		
		for(String arg : args)
 		{
 			finer("parsing argument: " + arg);
 			switch(state)
 			{
 				case START:
 					if(arg.equals("-a") || arg.equals("--adapter"))
 						state = ArgState.ADAPTER;
 					else if(arg.equals("-n") || arg.equals("--name"))
 						state = ArgState.NAME;
 					else if(arg.equals("-N") || arg.equals("--num-runs"))
 						state = ArgState.NUM_RUNS;
 					else if(arg.equals("-S") || arg.equals("--seed"))
 						state = ArgState.SEED;
 					else if(arg.equals("-m") || arg.equals("--model"))
 						state = ArgState.MODEL;
 					else if(arg.equals("-i") || arg.equals("--input"))
 						state = ArgState.INPUT;
 					else if(arg.equals("-o") || arg.equals("--output"))
 						state = ArgState.OUTPUT;
 					else if(arg.equals("-O") || arg.equals("--output-only"))
 					{
 						gs.setRunType(RunType.NORUN);
 						state = ArgState.OUTPUT;
 					}
 					else if(arg.equals("-e") || arg.equals("--email-address"))
 					{
 						state = ArgState.EMAIL;
 					}
 					else if(arg.equals("-r") || arg.equals("--results-dir"))
 					{
 						state = ArgState.RESULTS;
 					}
 					else if(arg.equals("-d") || arg.equals("--dry"))
 					{
 						gs.setRunType(RunType.DRY);
 					}
 					else if(arg.equals("-D") || arg.equals("--debug"))
 					{
 						// Do nothing; checked for first thing in main
 					}
 					else
 					{
 						Sweep sweep = parseSweepArg(arg);
 						assert(sweep != null);
 						cliSweeps.add(sweep);
 					}
 					break;
 				case ADAPTER:
 					cliSettings.put("AdapterClass", arg);
 					state = ArgState.START;
 					break;
 				case NAME:
 					cliSettings.put("Name", arg);
 					state = ArgState.START;
 					break;
 				case NUM_RUNS:
 					cliSettings.put("NumRuns", arg);
 					state = ArgState.START;
 					break;
 				case SEED:
 					cliSettings.put("Seed", arg);
 					state = ArgState.START;
 					break;
 				case MODEL:
 					adapterSettings.put("model", arg);
 					state = ArgState.START;
 					break;
 				case INPUT:
 					experimentPath = arg;
 					state = ArgState.START;
 					break;
 				case OUTPUT:
 					outputPath = arg;
 					state = ArgState.START;
 					break;
 				case EMAIL:
 					cliSettings.put("EmailAddress", arg);
 					state = ArgState.START;
 					break;
 				case RESULTS:
 					cliSettings.put("ResultsDirectory", arg);
 					state = ArgState.START;
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * <p>Attempts to parse a non-switch command line argument
 	 * as a parameter sweep specification. See the manpage for supported
 	 * sweep types.</p>
 	 * @param arg The argument representing the sweep
 	 * @return The parsed sweep.
 	 * @throws GridSweeperException When the argument does not represent a valid sweep.
 	 */
 	Sweep parseSweepArg(String arg) throws GridSweeperException
 	{
 		// Code below is the decision tree for parsing sweeps.
 		
 		// Check for single equals sign, required for all sweep types,
 		// and assigns LHS and RHS.
 		equalMatcher.reset(arg);
 		if(!equalMatcher.matches())
 		{
 			parseFail(arg);
 		}
 		String lhs = equalMatcher.group(1);
 		String rhs = equalMatcher.group(2);
 		
 		// Check for pieces separated by whitespace on LHS
 		String[] lhsWords = lhs.split("\\s+");
 		assert(lhsWords.length > 0);
 		if(lhsWords.length > 1)
 		{
 			// Parse as a parallel sweep (multiple parameter names on LHS)
 			return parseParallelSweep(arg, lhsWords, rhs);
 		}
 		else
 		{
 			// Parse as a single-parameter sweep
 			return parseSingleSweep(arg, lhs, rhs);
 		}
 	}
 
 	Sweep parseParallelSweep(String arg, String[] names, String rhs) throws GridSweeperException
 	{
 		// Split at colon boundaries to see if this is a range sweep list
 		String[] rhsColonPieces = rhs.split("\\s*:\\s*");
 		assert(rhsColonPieces.length > 0);
 		
 		if(rhsColonPieces.length > 1)
 		{
 			// Interpret as a range sweep list.
 			return parseParallelRangeSweep(arg, names, rhsColonPieces);
 		}
 		else
 		{
 			// Interpret as a parallel single-value list
 			return parseParallelListSweep(arg, names, rhs);
 		}
 	}
 
 	Sweep parseParallelRangeSweep(String arg, String[] names, String[] rhsColonPieces) throws GridSweeperException
 	{
 		// The number of pieces must be equal to
 		// 3 * names.length (start/end/incr for each parameter)
 		// - (names.length - 1) (pieces at boundaries between
 		//                       ranges are shared)
 		// = 2 * names.length + 1 
 		if(rhsColonPieces.length != 2 * names.length + 1)
 		{
 			parseFail(arg);
 		}
 		
 		ParallelCombinationSweep sweep = new ParallelCombinationSweep();
 		
 		// Look for range sweeps components in the correct locations,
 		// splitting at whitespace boundaries as necessary
 		
 		try
 		{
 			BigDecimal start;
 			BigDecimal incr;
 			BigDecimal end;
 			
 			String[] endStart = null;
 			
 			// Extract the sweep start, end, increment for each parameter
 			for(int i = 0; i < names.length; i++)
 			{
 				if(i == 0)
 				{
 					// Take the first piece for the range start
 					start = new BigDecimal(rhsColonPieces[0]);
 				}
 				else
 				{
 					// Take the second half of the piece
 					// whose first piece was used for the last range end
 					start = new BigDecimal(endStart[1]);
 				}
 				
 				// Range increment always appears
 				// as a lone piece at indexes 1, 3, 5, ...
 				incr = new BigDecimal(rhsColonPieces[i*2 + 1]);
 				
 				if(i == names.length - 1)
 				{
 					// Take the last piece for the range end
 					end = new BigDecimal(rhsColonPieces[i*2 + 2]);
 				}
 				else
 				{
 					// Split the last piece into two, using the
 					// first for the range end and saving
 					// the second for the next range start
 					endStart = rhsColonPieces[i*2 + 2].split("\\s+");
 					if(endStart.length != 2) parseFail(arg);
 					end = new BigDecimal(endStart[0]);
 				}
 				
 				sweep.add(new RangeListSweep(names[i], start, end, incr));
 			}
 		}
 		catch(NumberFormatException e)
 		{
 			parseFail(arg);
 		}
 		
 		// Check to make sure all sweeps generate the same
 		// number of values
 		boolean first = true;
 		int numValsReq = 0;
 		for(Sweep rangeSweep : sweep.getChildren())
 		{
 			int numVals = 0;
 			try { numVals = rangeSweep.generateMaps().size(); }
 			catch(Exception e){assert(false);}
 			
 			if(first)
 			{
 				numValsReq = numVals;
 				first = false;
 			}
 			else if(numVals != numValsReq)
 				parseFail(arg);
 		}
 		
 		return sweep;
 	}
 	
 	Sweep parseParallelListSweep(String arg, String[] names, String rhs) throws GridSweeperException
 	{
 		ParallelCombinationSweep sweep = new ParallelCombinationSweep();
 		
 		// Split at comma boundaries
 		String[] commaPieces = rhs.split("\\s*,\\s*");
 		assert(commaPieces.length > 0);
 		
 		List<String[]> commaWhitespacePieces = new ArrayList<String []>();
 		boolean allMatchNameCount = true;
 		for(String commaPiece : commaPieces)
 		{
 			// If empty string, give up
 			if(commaPiece.equals("")) parseFail(arg);
 			
 			String[] whitespacePieces = commaPiece.split("\\s+");
 			assert(whitespacePieces.length > 0);
 			
 			// If any lengths are different from the number of names,
 			// this can't be a list in the format
 			// p1 p2 p3 = p1v1 p2v1 p3v1, p1v2 p2v2 p3v2, p1v3 p2v3 p3v3, ...
 			if(allMatchNameCount && whitespacePieces.length != names.length) allMatchNameCount = false;
 			
 			commaWhitespacePieces.add(whitespacePieces);
 		}
 		
 		// If every whitespace split has the same number of pieces as the name count,
 		// then we'll interpret this as a list of this type:
 		// p1 p2 p3 = p1v1 p2v1 p3v1, p1v2 p2v2 p3v2, p1v3 p2v3 p3v3, ...
 		if(allMatchNameCount)
 		{
 			for(int i = 0; i < names.length; i++)
 			{
 				StringList values = new StringList();
 				for(String[] whitespacePieces : commaWhitespacePieces)
 				{
 					String piece = whitespacePieces[i];
 					if(piece.equals("")) parseFail(arg);
 					values.add(unescape(piece));
 				}
 				
 				ListSweep subSweep = new ListSweep(names[i], values);
 				sweep.add(subSweep);
 			}
 		}
 		else
 		{
 			// Otherwise, we'll assume the list is of this type:
 			// p1 p2 p3 p4 p5 = p1v1,p1v2,p1v3,p1v4 p2v1,p2v2,p2v3,p2v4 p3v1,p3v2,p3v3,p3v4 p4v1,p4v2,p4v3,p4v4 p5v1,p5v2,p5v3,p5v4
 			// meaning the commaWhitespacePieces arrays will have counts, e.g.,
 			// 1 1 1 2 1 1 2 1 1 2 1 1 2 1 1 1
 			
 			// First, figure out how many values each list should have
 			// by looking for the first instance of a 2-count array
 			int numVals = 0;
 			int size = commaWhitespacePieces.size();
 			for(int i = 0; i < size; i++)
 			{
 				numVals++;
 				String[] whitespacePieces = commaWhitespacePieces.get(i);
 				if(whitespacePieces.length == 2) break;
 				else if(whitespacePieces.length != 1) parseFail(arg);
 			}
 			
 			// Verify that the total length of the commonWhitespacePieces list is correct
 			// Analogous to the situation with parallel range list sweeps,
 			// there should be this many items:
 			// numVals * names.length
 			// - (names.length - 1) (pieces at boundaries between
 			//                         ranges are shared)
 			// = (numVals - 1) * names.length + 1
 			// Sanity check for example above: numVals = 4, names.length = 5,
 			// so # items should = (4 - 1) * 5 + 1 = 16. Check.
 			if(size != (numVals - 1) * names.length + 1) parseFail(arg);
 			
 			// For each parameter name...
 			for(int i = 0; i < names.length; i++)
 			{
 				StringList values = new StringList();
 				
 				// Get each value in the list, with special treatment at boundaries
 				for(int j = 0; j < numVals; j++)
 				{
 					String piece;
 					if(j == 0)
 					{
 						String[] whitespacePieces = commaWhitespacePieces.get(i * (numVals - 1));
 						if(i == 0)
 						{
 							if(whitespacePieces.length != 1) parseFail(arg);
 							piece = whitespacePieces[0];
 						}
 						else
 						{
 							if(whitespacePieces.length != 2) parseFail(arg);
 							piece = whitespacePieces[1];
 						}
 					}
 					else if(j == numVals - 1)
 					{
 						String[] whitespacePieces = commaWhitespacePieces.get((i + 1) * (numVals - 1));
 						if(i == names.length - 1)
 						{
 							if(whitespacePieces.length != 1) parseFail(arg);
 						}
 						else
 						{
 							if(whitespacePieces.length != 2) parseFail(arg);
 						}
 						piece = whitespacePieces[0];
 					}
 					else
 					{
 						String[] whitespacePieces = commaWhitespacePieces.get(i * (numVals - 1) + j);
 						if(whitespacePieces.length != 1) parseFail(arg);
 						piece = whitespacePieces[0];
 					}
 					
 					if(piece.equals("")) parseFail(arg);
 					values.add(unescape(piece));
 				}
 				
 				sweep.add(new ListSweep(names[i], values));
 			}
 		}
 		
 		return sweep;
 	}
 	
 	Sweep parseSingleSweep(String arg, String name, String rhs) throws GridSweeperException
 	{
 		// Check for pieces separated by colons on RHS
 		String[] rhsColonPieces = rhs.split("\\s*:\\s*");
 		assert(rhsColonPieces.length > 0);
 		if(rhsColonPieces.length > 1)
 		{
 			// Parse as a range sweep
 			return parseRangeSweep(arg, name, rhsColonPieces);
 		}
 		else
 		{
 			// Parse as a list or single-value sweep
 			
 			// Check for pieces separated by commas on RHS
 			String[] rhsCommaPieces = rhs.split("\\s*,\\s*");
 			assert(rhsCommaPieces.length > 0);
 			if(rhsCommaPieces.length > 1)
 			{
 				// Parse as a list sweep
 				return parseListSweep(arg, name, rhsCommaPieces);
 			}
 			else
 			{
 				return parseSingleValueSweep(arg, name, rhs); 
 			}
 		}
 	}
 
 	Sweep parseRangeSweep(String arg, String name, String[] rhsColonPieces) throws GridSweeperException
 	{
 		// Must have exactly three pieces
 		if(rhsColonPieces.length != 3)
 		{
 			parseFail(arg);
 		}
 		
 		// Reject if any pieces are the empty string or non-numeric
 		for(int i = 0; i < rhsColonPieces.length; i++)
 		{
 			if(rhsColonPieces[i].equals(""))
 			{
 				parseFail(arg);
 			}
 			
 			numMatcher.reset(rhsColonPieces[i]);
 			if(!numMatcher.matches())
 			{
 				parseFail(arg);
 			}
 		}
 		
 		BigDecimal start = new BigDecimal(rhsColonPieces[0]);
 		BigDecimal incr = new BigDecimal(rhsColonPieces[1]);
 		BigDecimal end = new BigDecimal(rhsColonPieces[2]);
 		
 		return new RangeListSweep(name, start, end, incr);
 	}
 
 	Sweep parseListSweep(String arg, String name, String[] values) throws GridSweeperException
 	{
 		// Whitespace-trim, unescape, and reject if any pieces are the empty string
 		for(int i = 0; i < values.length; i++)
 		{
 			values[i] = unescape(values[i].trim());
 			if(values[i].equals(""))
 			{
 				parseFail(arg);
 			}
 		}
 		
 		return new ListSweep(name, new StringList(values));
 	}
 
 	Sweep parseSingleValueSweep(String arg, String name, String value) throws GridSweeperException
 	{
 		return new SingleValueSweep(name, unescape(value));
 	}
 	
 	private void parseFail(String arg) throws GridSweeperException
 	{
		throw new GridSweeperException("Invalid sweep specification: " + arg);
 	}
 	
 	/**
 	 * Loads the experiment from the provided XML file.
 	 * @throws GridSweeperException If the experiment path is not provided,
 	 * or if the file cannot be loaded or parsed.
 	 */
 	private void loadExperimentFile() throws GridSweeperException
 	{
 		Settings sharedSettings = Settings.sharedSettings();
 		
 		if(experimentPath == null)
 		{
 			experiment = new Experiment(sharedSettings);
 		}
 		
 		else try
 		{
 			experiment = new Experiment(sharedSettings,
 					new java.net.URL("file", "", experimentPath));
 		}
 		catch (MalformedURLException e)
 		{
 			throw new GridSweeperException("Could not find experiment file "
 					+ experimentPath + ".", e);
 		}
 	}
 }
