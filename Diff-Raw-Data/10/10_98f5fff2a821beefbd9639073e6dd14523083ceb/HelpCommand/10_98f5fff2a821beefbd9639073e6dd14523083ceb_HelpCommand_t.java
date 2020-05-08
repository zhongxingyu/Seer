 /*
  * CLiC, Framework for Command Line Interpretation in Eclipse
  *
  *     Copyright (C) 2013 Worldline or third-party contributors as
  *     indicated by the @author tags or express copyright attribution
  *     statements applied by the authors.
  *
  *     This library is free software; you can redistribute it and/or
  *     modify it under the terms of the GNU Lesser General Public
  *     License as published by the Free Software Foundation; either
  *     version 2.1 of the License.
  *
  *     This library is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *     Lesser General Public License for more details.
  *
  *     You should have received a copy of the GNU Lesser General Public
  *     License along with this library; if not, write to the Free Software
  *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package com.worldline.clic.internal.commands.impl;
 
 import java.io.IOException;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSpec;
 
 import com.worldline.clic.commands.AbstractCommand;
 import com.worldline.clic.commands.CommandContext;
 import com.worldline.clic.internal.Activator;
 import com.worldline.clic.internal.ClicMessages;
 import com.worldline.clic.internal.commands.CommandRegistry;
 
 /**
  * The {@link HelpCommand} is an internal implementation of an
  * {@link AbstractCommand} to be used by the user in order to get help about any
  * available command from the framework. It can be seen as a <i>man</i> command
  * in Unix.
  * 
  * This command will internally rely on
  * {@link OptionParser#printHelpOn(java.io.Writer)} in order to build the help
  * message to be displayed.
  * 
  * @author aneveux / mvanbesien
  * @version 1.0
  * @since 1.0
  * 
  * @see AbstractCommand
  */
 public class HelpCommand extends AbstractCommand {
 
 	/**
 	 * An {@link OptionSpec} linked to the parameters we define allowing to
 	 * retrieve results in a type-safe way, avoiding unwanted casts...
 	 */
 	OptionSpec<String> command;
 
 	/**
 	 * In this methid, we configure the parser to accept one parameter named
 	 * <i>command</i> allowing to retrieve the command on which we need to bring
 	 * some help
 	 * 
 	 * @see super{@link #configureParser()}
 	 */
 	@Override
 	public void configureParser() {
 		command = parser.accepts("command").withRequiredArg()
 				.ofType(String.class).describedAs("command");
 	}
 
 	/**
 	 * For this command, we'll simply display some help about a specified
 	 * command, using the {@link OptionParser#printHelpOn(java.io.Writer)}
 	 * helper from JOpt-Simple
 	 * 
 	 * @see super{@link #execute(CommandContext)}
 	 */
 	@Override
 	public void execute(final CommandContext context) {
 		final AbstractCommand createdCommand = options.has(command) ? CommandRegistry
 				.getInstance().createCommand(options.valueOf(command))
 				: CommandRegistry.getInstance().createCommand("help");
 		try {
			context.write(options.has(command) ? ClicMessages.COMMAND_HELP
					.value(options.valueOf(command),
							CommandRegistry.getInstance()
									.getCommandDescription(
											options.valueOf(command)))
					: ClicMessages.COMMAND_HELP.value("help", CommandRegistry
							.getInstance().getCommandDescription("help")));
 			createdCommand.getParser().printHelpOn(context.getWriter());
 		} catch (final IOException e) {
 			context.write(ClicMessages.COMMAND_EXECUTION_ERROR.value(e
 					.getMessage()));
 			Activator.sendErrorToErrorLog(
 					ClicMessages.COMMAND_EXECUTION_ERROR.value(e.getMessage()),
 					e);
 		}
 	}
 
 }
