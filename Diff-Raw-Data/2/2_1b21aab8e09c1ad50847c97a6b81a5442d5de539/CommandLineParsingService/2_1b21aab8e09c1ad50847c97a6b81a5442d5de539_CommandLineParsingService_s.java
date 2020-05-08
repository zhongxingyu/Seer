 /*******************************************************************************
  * Copyright (c) 2009 Intuit, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.opensource.org/licenses/eclipse-1.0.php
  * 
  * Contributors:
  *     Intuit, Inc - initial API and implementation
  *******************************************************************************/
 package com.intuit.ginsu.cli;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import com.beust.jcommander.IDefaultProvider;
 import com.beust.jcommander.JCommander;
 import com.google.inject.Inject;
 import com.intuit.ginsu.commands.CommandNull;
 import com.intuit.ginsu.commands.ICommand;
 
 /**
  * @author rpfeffer
  * @dateCreated Mar 26, 2011
  * 
  *              This file provides services for parsing command line input
  *              from the terminal. It wraps JCommander and maintains a
  *              runnable command state. Anyone that would like to implement
  *              a command that is runnable by this class should see the 
  *              documentation at http://jcommander.org/#Complex as well 
  *              as implement the ICommand interface.
  * 
  */
 public class CommandLineParsingService implements IInputParsingService {
 
 	// According to the best practices listed at:
	// http://code.​google.com/​docreader/#p=​google-guice&​s=​google-guice&​t=​Minimize​Mutability
 	// we should try keep all injected objects immutable
 	private final JCommander jCommander;
 	private final MainArgs mainArgs;
 	private final PrintWriter printWriter;
 	private final Map<String, ICommand> supportedCommands;
 	private ICommand command;
 	
 
 	@Inject
 	public CommandLineParsingService(PrintWriter printWriter, 
 			JCommander jCommander, MainArgs mainArgs,
 			HashMap<String, ICommand> supportedCommands) {
 		this.printWriter = printWriter;
 		this.mainArgs = mainArgs;
 		this.jCommander = jCommander;
 		this.supportedCommands = supportedCommands;
 
 		//until we successfully call parse, this will be the object we get back
 		this.command = new CommandNull();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.intuit.ginsu.cli.IInputParsingService#parseInput(java.lang.String[])
 	 */
 	public void parseInput(String[] input) {
 
 		try
 		{
 			this.jCommander.parse(input);
 			this.command = this.getParsedCommand();
 		}
 		catch (Throwable e)
 		{
 			//print a message out to the user
 			this.printWriter.println(e.getMessage());
 			StringBuilder stringBuilder = new StringBuilder();
 			this.jCommander.usage(stringBuilder);
 			this.printWriter.println(stringBuilder.toString());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.ginsu.cli.IInputParsingService#getCommand()
 	 */
 	public ICommand getCommand() {
 		return this.command;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.intuit.ginsu.cli.IInputParsingService#getMainCommand()
 	 */
 	public Hashtable<String, Object> getConfigurationOverride() {
 		// TODO Auto-generated method stub
 		return this.mainArgs.getConfigurationOverride();
 	}
 	
 	private ICommand getParsedCommand() throws Exception
 	{
 		ICommand parsedCommand = this.supportedCommands.get(this.jCommander.getParsedCommand());
 		if(parsedCommand == null)
 		{
 			throw new Exception("You must supply at least one supported command.");
 		}
 		return parsedCommand;
 	}
 
 	public void setDefaultProvider(IDefaultProvider defaultProvider) {
 		this.jCommander.setDefaultProvider(defaultProvider);
 	}
 
 }
