 package adam.betts.tools;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import adam.betts.outputs.WriteProgram;
 import adam.betts.programs.Program;
 import adam.betts.programs.ProgramGenerator;
 import adam.betts.utilities.DefaultOptions;
 
 public class MainProgramGenerator
 {
 	private static Options options;
 	private static Option fanOutOption;
 	private static Option loopsOption;
 	private static Option selfLoopsOption;
 	private static Option returnsOption;
 	private static Option subprogramsOption;
 	private static Option depthOption;
 	private static Option breaksOption;
 	private static Option continuesOption;
 	private static Option numberOfVerticesOption;
 
 	public static void main (String[] args)
 	{
 		addOptions ();
 		parseCommandLine (args);
 		run ();
 	}
 
 	private static void addOptions ()
 	{
 		options = new Options ();
 		DefaultOptions.addDefaultOptions (options);
 		DefaultOptions.addOutputFormatOption (options);
 		DefaultOptions.addUDrawDirectoryOption (options);
 
 		fanOutOption = new Option ("F", "fan-out", true,
 				"Maximum number of successors of a vertex in range 2..5. Default is "
 						+ Globals.fanOut + ".");
 		fanOutOption.setRequired (false);
 		options.addOption (fanOutOption);
 
 		loopsOption = new Option ("l", "loops", true,
 				"Maximum number of loops in a single graph in range 0..10. Default is "
 						+ Globals.loops + ".");
 		loopsOption.setRequired (false);
 		options.addOption (loopsOption);
 
 		selfLoopsOption = new Option ("S", "self-loops", true,
 				"Maximum number of self-loops in a single graph in range 0..5. Default is "
 						+ Globals.selfLoops + ".");
 		selfLoopsOption.setRequired (false);
 		options.addOption (selfLoopsOption);
 
 		returnsOption = new Option ("r", "returns", true,
 				"Maximum number of returns in a single graph in range 1..4. Default is "
 						+ Globals.returns + ".");
 		returnsOption.setRequired (false);
 		options.addOption (returnsOption);
 
 		subprogramsOption = new Option ("s", "subprograms", true,
 				"Maximum number of sub-programs in the program. Default is " + Globals.subprograms
 						+ ".");
 		subprogramsOption.setRequired (false);
 		options.addOption (subprogramsOption);
 
		depthOption = new Option ("D", "depth", true,
 				"Maximum depth of the call graph. Default is " + Globals.depth + ".");
 		depthOption.setRequired (false);
 		options.addOption (depthOption);
 
 		breaksOption = new Option ("b", "breaks", true, "Include break-like structures in loops.");
 		breaksOption.setRequired (false);
 		options.addOption (breaksOption);
 
 		continuesOption = new Option ("C", "continue", true,
 				"Include continue-like structures in loops.");
 		continuesOption.setRequired (false);
 		options.addOption (continuesOption);
 
 		numberOfVerticesOption = new Option ("V", "vertices", true,
 				"Maximum number of vertices in a control flow graph. Default is "
 						+ Globals.vertices + ".");
 		numberOfVerticesOption.setRequired (false);
 		options.addOption (numberOfVerticesOption);
 	}
 
 	private static void parseCommandLine (String[] args)
 	{
 		final String toolName = "program-generator.jar";
 		CommandLineParser parser = new GnuParser ();
 		HelpFormatter formatter = new HelpFormatter ();
 		formatter.setWidth (128);
 		CommandLine line = null;
 		try
 		{
 			line = parser.parse (options, args);
 
 			if (line.hasOption (DefaultOptions.helpOption.getOpt ()))
 			{
 				formatter.printHelp (toolName, options);
 				System.exit (1);
 			} else
 			{
 				DefaultOptions.setDefaultOptions (line);
 				DefaultOptions.setOutputFormatOption (line);
 				DefaultOptions.setUDrawDirectoryOption (line);
 
 				Globals.breaks = line.hasOption (breaksOption.getOpt ());
 				Globals.continues = line.hasOption (continuesOption.getOpt ());
 
 				if (line.hasOption (fanOutOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (fanOutOption.getOpt ());
 					try
 					{
 						int fanOut = Integer.parseInt (arg);
 						if (fanOut < 2 || fanOut > 5)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.fanOut = fanOut;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ fanOutOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err.println (arg
 								+ " is not a valid fan out. It must be in the range 2..5.");
 						System.exit (1);
 					}
 				}
 
 				if (line.hasOption (loopsOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (loopsOption.getOpt ());
 					try
 					{
 						int loops = Integer.parseInt (arg);
 						if (loops < 0 || loops > 10)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.loops = loops;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ loopsOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid number of loops. It must be in the range 0..10.");
 						System.exit (1);
 					}
 				}
 
 				if (line.hasOption (selfLoopsOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (selfLoopsOption.getOpt ());
 					try
 					{
 						int selfLoops = Integer.parseInt (arg);
 						if (selfLoops < 0 || selfLoops > 5)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.selfLoops = selfLoops;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ selfLoopsOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid number of self loops. It must be in the range 0..5.");
 						System.exit (1);
 					}
 				}
 
 				if (line.hasOption (returnsOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (returnsOption.getOpt ());
 					try
 					{
 						int returns = Integer.parseInt (arg);
 						if (returns < 0 || returns > 4)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.returns = returns;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ returnsOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid number of returns. It must be in the range 0..4.");
 						System.exit (1);
 					}
 				}
 
 				if (line.hasOption (subprogramsOption.getOpt ()))
 				{
 
 					String arg = line.getOptionValue (subprogramsOption.getOpt ());
 					try
 					{
 						int subprograms = Integer.parseInt (arg);
 						if (subprograms < 1)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.subprograms = subprograms;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ subprogramsOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid number of subprograms. It must be a positive integer in the range 1.."
 										+ Integer.MAX_VALUE);
 					}
 				}
 
 				if (line.hasOption (depthOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (depthOption.getOpt ());
 					try
 					{
 						int depth = Integer.parseInt (arg);
 						if (depth > Globals.subprograms)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.depth = depth;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ depthOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid call graph depth. You need at least as many subprograms as the depth of the call graph.");
 						System.exit (1);
 					}
 				}
 
 				if (line.hasOption (numberOfVerticesOption.getOpt ()))
 				{
 					String arg = line.getOptionValue (numberOfVerticesOption.getOpt ());
 					try
 					{
 						int vertices = Integer.parseInt (arg);
 						if (vertices < 50 || vertices > 200)
 						{
 							throw new IllegalArgumentException ();
 						}
 						Globals.vertices = vertices;
 					} catch (NumberFormatException e)
 					{
 						System.err.println ("'" + arg + "' is not a valid argument to "
 								+ numberOfVerticesOption.getLongOpt ());
 						System.exit (1);
 					} catch (IllegalArgumentException e)
 					{
 						System.err
 								.println (arg
 										+ " is not a valid number of vertices. It must be a positive integer in the range 50..200");
 						System.exit (1);
 					}
 				}
 
 			}
 		} catch (ParseException e)
 		{
 			System.out.println (e.getMessage ());
 			formatter.printHelp (toolName, options);
 			System.exit (1);
 		}
 	}
 
 	private static void run ()
 	{
 		Program program = new ProgramGenerator ().getProgram ();
 		new WriteProgram (program);
 	}
 
 	public static class Globals
 	{
 		protected static int fanOut = 2;
 		protected static int loops = 0;
 		protected static int selfLoops = 0;
 		protected static int returns = 1;
 		protected static int subprograms = 4;
 		protected static int depth = 7;
 		protected static boolean breaks = false;
 		protected static boolean continues = false;
 		protected static int vertices = 50;
 
 		public final static int getFanOut ()
 		{
 			return fanOut;
 		}
 
 		public final static int getNumberOfLoops ()
 		{
 			return loops;
 		}
 
 		public final static int getNumberOfSelfLoops ()
 		{
 			return selfLoops;
 
 		}
 
 		public final static int getNumberOfReturns ()
 		{
 			return returns;
 		}
 
 		public final static int getNumberOfSubprograms ()
 		{
 			return subprograms;
 		}
 
 		public final static int getDepthOfCallGraph ()
 		{
 			return depth;
 		}
 
 		public final static boolean breaksAllowed ()
 		{
 			return breaks;
 		}
 
 		public final static boolean continuesAllowed ()
 		{
 			return continues;
 		}
 
 		public final static int getNumberOfVerticesInCFG ()
 		{
 			return vertices;
 		}
 	}
 }
