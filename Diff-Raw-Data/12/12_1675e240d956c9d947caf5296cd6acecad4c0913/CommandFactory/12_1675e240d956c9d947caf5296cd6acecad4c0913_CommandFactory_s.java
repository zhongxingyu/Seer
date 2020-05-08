 /*******************************************************************************
  * Copyright (c) May 17, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 
 package org.zend.sdkcli;
 
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.CreateProjectCommand;
 import org.zend.sdkcli.internal.commands.CreateTargetCommand;
 import org.zend.sdkcli.internal.commands.DeleteTargetCommand;
 import org.zend.sdkcli.internal.commands.DeployApplicationCommand;
 import org.zend.sdkcli.internal.commands.DetectTargetCommand;
 import org.zend.sdkcli.internal.commands.ListApplicationsCommand;
 import org.zend.sdkcli.internal.commands.ListTargetsCommand;
 import org.zend.sdkcli.internal.commands.UsageCommanLine;
 
 /**
  * Creates command instance.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class CommandFactory {
 
 	/**
 	 * Creates {@link ICommand} implementor instance based on a given command
 	 * line.
 	 * 
 	 * @param line
 	 *            - command line
 	 * @return command instance
 	 * @throws ParseError
 	 */
 	public static ICommand createCommand(CommandLine line) throws ParseError {
 		CommandType type = CommandType.byCommandLine(line);
 		ICommand command = null;
 		switch (type) {
 		case CREATE_PROJECT:
 			command = new CreateProjectCommand(line);
 			break;
 		case LIST_TARGETS:
 			command = new ListTargetsCommand(line);
 			break;
 		case CREATE_TARGET:
 			command = new CreateTargetCommand(line);
 			break;
 		case DELETE_TARGET:
 			command = new DeleteTargetCommand(line);
 			break;
 		case DETECT_TARGET:
 			command = new DetectTargetCommand(line);
 			break;
 		case LIST_APPLICATIONS:
 			command = new ListApplicationsCommand(line);
 			break;
 		case DEPLOY_APPLICATION:
 			command = new DeployApplicationCommand(line);
 			break;
 		default:
 			command = new UsageCommanLine();
 			break;
 		}
 		return command;
 	}
 
 }
