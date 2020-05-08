 /*******************************************************************************
  * Copyright (c) May 29, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.util.Collection;
 
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.CommandType;
 import org.zend.sdkcli.ICommand;
 import org.zend.sdkcli.internal.options.DetectOptionUtility;
 
 /**
  *
  */
 public class UsageCommand implements ICommand {
 
 	@Override
 	public boolean execute(CommandLine cmdLine) {
 
 		String verb;
 		String dirObj;
 		if ("help".equals(cmdLine.getArgument(0))) {
 			verb = cmdLine.getArgument(1);
 			dirObj = cmdLine.getArgument(2);
 		} else {
 			verb = cmdLine.getArgument(0);
 			dirObj = cmdLine.getArgument(1);
 		}
 		CommandLine helpCmd = null;
 		ICommand cmd = null;
 
 		helpCmd = new CommandLine(new String[] { verb, dirObj });
 		cmd = CommandFactory.createCommand(helpCmd);
 
 		printCommandUsage();
 
 		if (helpCmd != null && cmd != null && (!(cmd instanceof UsageCommand))) {
 			CommandType type = CommandType.byCommandLine(helpCmd);
 			printCommandUsage(type);
 			printCommandOptions(type);
 			return true;
 		}
 
 		printAvailableCommands();
 
 		System.out.println();
 		return true;
 	}
 
 	private void printCommandUsage() {
 		System.out.println("Usage:");
 		System.out.println("  zend action [action options] [global options]");
 		System.out.println();
 		System.out.println("Global options:");
 
 		final Options options = new Options();
		DetectOptionUtility.addOption(AbstractCommand.class, options, false);
 
 		for (Object obj : options.getOptions()) {
 			Option o = (Option) obj;
 			System.out.printf("  -%-3s %s\n", o.getOpt(), o.getDescription());
 		}
 		System.out.println();
 		System.out
 				.println("Valid actions are composed of a verb and an optional direct object:");
 	}
 
 	private void printAvailableCommands() {
 		for (CommandType type : CommandType.values()) {
 			ICommand cmd = CommandFactory.createCommand(type);
 
 			printCommandUsage(type);
 		}
 	}
 
 	private void printCommandUsage(CommandType type) {
 		String dirObj = type.getDirectObject();
 		if (dirObj == null) {
 			dirObj = "";
 		}
 		System.out.printf("- %-10s %-15s : %s", type.getVerb(), dirObj,
 				type.getInfo());
 		System.out.println();
 
 	}
 
 	private void printCommandOptions(CommandType type) {
 		Options opts = new Options();
 		System.out.println();
 
 		// get command specific options
 		DetectOptionUtility.addOption(CommandFactory.createCommand(type)
 				.getClass(), opts, true);
 
 		if (opts.getOptions().size() > 0) {
 			System.out.println("Options:");
 			Collection collection = opts.getOptions();
 			for (Object o : collection) {
 				Option opt = (Option) o;
 				System.out
 						.printf("  -%-3s %s%s\n", opt.getOpt(), opt
 								.getDescription(),
 								opt.isRequired() ? " [required]" : "");
 			}
 		} else {
 			System.out.println("  No options");
 		}
 
 		System.out.println();
 	}
 
 	public Options getOptions() {
 		return null;
 	}
 }
