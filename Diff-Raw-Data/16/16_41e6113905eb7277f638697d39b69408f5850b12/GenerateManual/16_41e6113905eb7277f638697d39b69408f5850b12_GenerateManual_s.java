 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkhelp.docs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.CommandType;
 import org.zend.sdkcli.internal.commands.AbstractCommand;
 import org.zend.sdkcli.internal.logger.CliLogger;
 import org.zend.sdkcli.internal.options.DetectOptionUtility;
 import org.zend.sdklib.logger.Log;
 
 /**
  * A tool to generate the most updated command line tables
  * 
  * @author Roy, 2011
  * 
  */
 public class GenerateManual {
 
 	public static void main(String[] args) throws IOException {
 
 		PrintStream printStream = System.out;
 		if (args.length > 0) {
 			printStream = new PrintStream(new File(args[0]));
 		}
 
 		globalOptions(printStream, AbstractCommand.class);
 
 		printStream.print("<h3>Target actions and options</h3>");
 		tableCommands(printStream, CommandType.ADD_TARGET,
 				CommandType.REMOVE_TARGET, CommandType.DETECT_TARGET,
 				CommandType.LIST_TARGETS);
 
 		printStream.print("<h3>Project actions and options</h3>");
 		tableCommands(printStream, CommandType.CREATE_PROJECT,
 				CommandType.UPDATE_PROJECT);
 
 		printStream.print("<h3>Application actions and options</h3>");
 		tableCommands(printStream, CommandType.DEPLOY_APPLICATION,
 				CommandType.UPDATE_APPLICATION, CommandType.REMOVE_APPLICATION,
 				CommandType.REDEPLOY_APPLICATION,
 				CommandType.LIST_APPLICATIONS,
 				CommandType.DISCOVER_APPLICATION, CommandType.CREATE_PACKAGE);
 
 		printStream.print("<h3>Repository actions and options</h3>");
 		tableCommands(printStream, CommandType.ADD_REPOSITORY,
 				CommandType.REMOVE_REPOSITORY, CommandType.LIST_REPOSITORIES,
 				CommandType.GENERATE_REPOSITORIES);
 
 		printStream.close();
 	}
 
 	private static void globalOptions(PrintStream printStream,
 			Class<AbstractCommand> class1) throws IOException {
 
 		Log.getInstance().registerLogger(new CliLogger());
 
 		final Options options = new Options();
		DetectOptionUtility.addOption(class1, options, true);
 
 		final Collection os = options.getOptions();
 		String[][] blocks = new String[os.size()][];
 		int i = 0;
 		for (Object object : os) {
 			Option o = (Option) object;
 			blocks[i++] = new String[] { o.getOpt(), o.getDescription() };
 		}
 		final InputStream resourceAsStream = GenerateManual.class
 				.getResourceAsStream("global.options");
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(
 				resourceAsStream));
 		HtmlTemplate.writeTemplate(reader, printStream, blocks);
 	}
 
 	private static void tableCommands(PrintStream printStream,
 			CommandType... types) throws IOException {
 
 		ArrayList<String[]> blocks = new ArrayList<String[]>();
 
 		for (CommandType type : types) {
 			final Options options = new Options();
 			DetectOptionUtility.addOption(CommandFactory.createCommand(type)
 					.getClass(), options, true);
 
 			final Collection os = options.getOptions();
 
 			String header = "<td rowspan=\"" + os.size() + "\"><code>"
 					+ type.getVerb() + " " + type.getDirectObject()
 					+ "</code></td>";
 
 			for (Object object : os) {
 				Option o = (Option) object;
 				blocks.add(new String[] { header, o.getOpt(),
 						o.hasArg() ? "&lt;" + o.getArgName() + "&gt;" : "",
 						o.getDescription(), o.isRequired() ? "Required" : "" });
 				header = "";
 			}
 		}
 		final InputStream resourceAsStream = GenerateManual.class
 				.getResourceAsStream("actions.options");
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(
 				resourceAsStream));
 		HtmlTemplate.writeTemplate(reader, printStream,
 				(String[][]) blocks.toArray(new String[blocks.size()][]));
 	}
 }
