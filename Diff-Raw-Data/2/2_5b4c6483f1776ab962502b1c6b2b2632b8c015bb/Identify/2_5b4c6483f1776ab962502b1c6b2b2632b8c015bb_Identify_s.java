 /*
 This file is part of leafdigital browserstats.
 
 browserstats is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 browserstats is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with browserstats.  If not, see <http://www.gnu.org/licenses/>.
 
 Copyright 2010 Samuel Marshall.
 */
 package com.leafdigital.browserstats.identify;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.leafdigital.browserstats.shared.CommandLineTool;
 import com.leafdigital.util.xml.XMLException;
 
 /** Analyses a user-agent file to find out what browsers it has. */
 public class Identify extends CommandLineTool implements UserAgentReader.Handler
 {
 	private AgentList list;
 
 	private File folder = null;
 	private boolean overwrite, stdout;
 
 	private TestType test = TestType.NONE;
 	private String[] testParams = null;
	private int minCount, matched, unmatched, matchedCount, unmatchedCount;
 
 	private IdentifyResults results;
 
 	private Map<Group, Map<String, Integer>> partialMatches =
 		new TreeMap<Group, Map<String, Integer>>();
 
 	/** Special test constants to check options. */
 	private enum TestType
 	{
 		/** No test */
 		NONE(-1),
 
 		/** Run the agent list self-test */
 		SELFTEST(0),
 		/** Show all unmatched user agents with counts above the parameter */
 		UNMATCHED(1),
 		/** Show all partiall-matched user agents */
 		PARTIAL(0),
 		/** Show all user-agents matching the given type, engine, name, version, os */
 		SHOWAGENT(5),
 		/** Identify an agent given on command line */
 		IDENTIFY(1),
 		/** Identify patterns of string that match each agent. */
 		SHOWAGENTSELECTED(5);
 
 		private int params;
 		TestType(int params)
 		{
 			this.params = params;
 		}
 
 		/** @return Number of parameters required by the option */
 		int getParams()
 		{
 			return params;
 		}
 	};
 
 	/**
 	 * @param args Command-line arguments
 	 */
 	public static void main(String[] args)
 	{
 		(new Identify()).run(args);
 	}
 
 	private Identify()
 	{
 		try
 		{
 			list = new AgentList();
 		}
 		catch(XMLException e)
 		{
 			System.err.println("Error parsing agent list:\n\n" + e.getMessage());
 			failed();
 		}
 		catch(InvalidElementException e)
 		{
 			System.err.println("Invalid agent list:\n\n" + e.getMessage());
 			failed();
 		}
 	}
 
 	@Override
 	protected int processArg(String[] args, int i)
 	{
 		if(args[i].equals("-folder"))
 		{
 			checkArgs(args, i, 1);
 			folder = new File(args[i+1]);
 			if(!folder.exists() || !folder.isDirectory())
 			{
 				throw new IllegalArgumentException("Folder does not exist: " + folder);
 			}
 			return 2;
 		}
 		if(args[i].equals("-overwrite"))
 		{
 			overwrite = true;
 			return 1;
 		}
 		if(args[i].equals("-stdout"))
 		{
 			stdout = true;
 			return 1;
 		}
 		if(args[i].equals("-test"))
 		{
 			checkArgs(args, i, 1);
 			try
 			{
 				test = TestType.valueOf(args[i+1].toUpperCase());
 			}
 			catch(IllegalArgumentException e)
 			{
 				throw new IllegalArgumentException(
 					"Unrecognised test type: " + args[i+1]);
 			}
 			checkArgs(args, i, 1+test.getParams());
 			testParams = new String[test.getParams()];
 			for(int j=0; j<test.getParams(); j++)
 			{
 				testParams[j] = args[i+2+j];
 			}
 
 			return 2+test.getParams();
 		}
 		return 0;
 	}
 
 	@Override
 	protected void validateArgs() throws IllegalArgumentException
 	{
 	}
 
 	@Override
 	protected boolean requiresInput()
 	{
 		return test != TestType.SELFTEST && test != TestType.IDENTIFY;
 	}
 
 	private Pattern selectType, selectEngine, selectName, selectVersion, selectOs;
 	private HashSet<String> selectMatches;
 
 	private AgentPatterns agentPatterns = new AgentPatterns();
 
 	@Override
 	protected void go()
 	{
 		switch(test)
 		{
 		case SELFTEST:
 			if(!list.selfTest())
 			{
 				// Abort when test fails
 				return;
 			}
 			return;
 
 		case IDENTIFY:
 			System.out.println(list.match(testParams[0]));
 			break;
 
 		case UNMATCHED:
 			try
 			{
 				minCount = Integer.parseInt(testParams[0]);
 			}
 			catch(NumberFormatException e)
 			{
 				System.err.println("-test unmatched requires integer parameter");
 				return;
 			}
 			// Continue with processing; we will handle the rest inside doFile
 			break;
 
 		case SHOWAGENT:
 		case SHOWAGENTSELECTED:
 			try
 			{
 				selectType = Pattern.compile(testParams[0]);
 				selectEngine = Pattern.compile(testParams[1]);
 				selectName = Pattern.compile(testParams[2]);
 				selectVersion = Pattern.compile(testParams[3]);
 				selectOs = Pattern.compile(testParams[4]);
 				selectMatches = new HashSet<String>();
 			}
 			catch(PatternSyntaxException e)
 			{
 				System.err.println("-test showagent/patterns regular expression not valid: " + e.getPattern());
 				return;
 			}
 			agentPatterns = new AgentPatterns();
 			break;
 		}
 
 		try
 		{
 			File[] input = getInputFiles();
 			if(input == null)
 			{
 				doFile(null);
 			}
 			else
 			{
 				for(File f : input)
 				{
 					doFile(f);
 				}
 			}
 
 			switch(test)
 			{
 			case UNMATCHED:
 				// Display summary about matching
 				System.err.println();
 				System.err.println("Summary results");
 				System.err.println("---------------");
 				System.err.println();
 				System.err.println("    Matched agents: " + matched + " (" + ((matched*100)/(matched+unmatched)) + "%)");
 				System.err.println("  Unmatched agents: " + unmatched);
 				System.err.println("  Matched requests: " + matchedCount + " (" + ((matchedCount*100)/(matchedCount+unmatchedCount)) + "%)");
 				System.err.println("Unmatched requests: " + unmatchedCount);
 				break;
 
 			case PARTIAL:
 				// Display partial matches
 				if(partialMatches.isEmpty())
 				{
 					System.out.println("No partial matches");
 				}
 				else
 				{
 					int agents = 0, requests = 0;
 					for(Map.Entry<Group, Map<String, Integer>> entry : partialMatches.entrySet())
 					{
 						System.out.println(entry.getKey());
 						for(Map.Entry<String, Integer> agent : entry.getValue().entrySet())
 						{
 							agents++;
 							requests += (int)agent.getValue();
 							System.out.printf("%5d: %s\n", agent.getValue(), agent.getKey());
 						}
 						System.out.println();
 					}
 					System.err.println("Summary results");
 					System.err.println("---------------");
 					System.err.println();
 					System.err.println("  Partial match agents: " + agents + " (" + ((agents*100)/(matched+unmatched)) + "%)");
 					System.err.println("Partial match requests: " + requests + " (" + ((requests*100)/(matchedCount+unmatchedCount)) + "%)");
 				}
 				break;
 
 			case SHOWAGENTSELECTED:
 				agentPatterns.display();
 				break;
 			}
 		}
 		catch(IOException e)
 		{
 			System.err.println(e.getMessage());
 		}
 	}
 
 	private void doFile(File f) throws IOException
 	{
 		// Read data
 		try
 		{
 			new UserAgentReader(f, this);
 		}
 		catch(IOException e)
 		{
 			throw new IOException("Error processing input:\n\n" + e.getMessage());
 		}
 
 		// Test types don't write result
 		switch(test)
 		{
 		case UNMATCHED:
 		case PARTIAL:
 		case SHOWAGENT:
 		case SHOWAGENTSELECTED:
 			return;
 		}
 
 		// Get target file for result
 		File target;
 		if(f==null || stdout)
 		{
 			target = null;
 		}
 		else
 		{
 			String targetName = f.getName();
 			if(targetName.endsWith(".useragents"))
 			{
 				targetName = targetName.substring(
 					0, targetName.length()-".useragents".length());
 			}
 			targetName += ".knownagents";
 			if(folder!=null)
 			{
 				target = new File(folder, targetName);
 			}
 			else
 			{
 				target = new File(f.getParentFile(), targetName);
 			}
 
 			if(!overwrite && target.exists())
 			{
 				throw new IOException("Target file already exists: " + target);
 			}
 		}
 
 		// Write results to file
 		results.write(target);
 	}
 
 	@Override
 	public void agentCategories(String[] categories)
 	{
 		results = new IdentifyResults(categories);
 	}
 
 	@Override
 	public void agentCounts(String agent, int count, int[] categoryCounts)
 	{
 		MatchElement matchElement = list.match(agent);
 		Agent match;
 		if(matchElement instanceof Agent || matchElement == null)
 		{
 			match = (Agent)matchElement;
 		}
 		else
 		{
 			// Partial match of an exclusive group. Display warnings
 			if(test == TestType.PARTIAL)
 			{
 				Group key = (Group)matchElement;
 				Map<String, Integer> map = partialMatches.get(key);
 				if(map == null)
 				{
 					map = new TreeMap<String, Integer>();
 					partialMatches.put(key, map);
 				}
 				map.put(agent, count);
 			}
 			match = null;
 		}
 
 		// Handle test option
 		switch(test)
 		{
 		case UNMATCHED:
 		case PARTIAL:
 			if(match==null)
 			{
 				unmatched++;
 				unmatchedCount += count;
 
 				if(count > minCount && test == TestType.UNMATCHED)
 				{
 					System.out.printf("%5d: %s\n", count, agent);
 				}
 			}
 			else
 			{
 				matched++;
 				matchedCount += count;
 			}
 			break;
 
 		case SHOWAGENT:
 		case SHOWAGENTSELECTED:
 			if(match!=null && selectType.matcher(match.getType()).find()
 				&& selectEngine.matcher(match.getEngine()).find()
 				&& selectName.matcher(match.getName()).find()
 				&& selectVersion.matcher(match.getVersion()).find()
 				&& selectOs.matcher(match.getOs()).find() )
 			{
 				if(test == TestType.SHOWAGENT)
 				{
 					if(selectMatches.add(agent))
 					{
 						System.out.println(agent);
 					}
 				}
 				else
 				{
 					agentPatterns.agent(agent);
 				}
 			}
 			break;
 		}
 
 		results.addCounts(match, count, categoryCounts);
 	}
 }
