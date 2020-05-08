 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl;
 
 import org.eclipse.osgi.framework.console.CommandInterpreter;
 import org.eclipse.osgi.framework.console.CommandProvider;
 import org.osgi.framework.InvalidSyntaxException;
import org.paxle.desktop.IDialogueServices.Dialogues;
 
 public class DICommandProvider implements CommandProvider {
 
 	private final DesktopServices desktop;
 	private final DialogueServices dialogues;
 	
 	public DICommandProvider(final DesktopServices desktop, final DialogueServices dialogue) {
 		this.desktop = desktop;
 		this.dialogues = dialogue;
 	}
 	
 	public void _desktop(final CommandInterpreter ci) throws InvalidSyntaxException {
 		handleDesktop(ci, ci.nextArgument());
 	}
 	
 	private void handleDesktop(final CommandInterpreter ci, final String action) throws InvalidSyntaxException {
 		if (action == null) {
 			ci.println("No argument given!");
 			return;
 		} else if (action.equals("open")) {
 			final String which = ci.nextArgument();
 			if (which == null) {
 				final Dialogues[] dialogues = Dialogues.values();
 				for (int i=0; i<dialogues.length;) {
 					ci.print(dialogues[i].name().toLowerCase());
 					if (++i < dialogues.length)
 						ci.print(", ");
 				}
 				ci.println();
 			} else {
 				final Dialogues dialogue;
 				try {
 					dialogue = Dialogues.valueOf(which.toUpperCase());
 				} catch (RuntimeException e) {
 					ci.println("dialogue '" + which + "' not available");
 					return;
 				}
 				dialogues.openDialogue(dialogue);
 			}
 		} else if (action.equals("tray")) {
 			desktop.setTrayMenuVisible(!desktop.isTrayMenuVisible());
 		} else {
 			ci.println("parameter '" + action + "' not understood");
 			return;
 		}
 	}
 	
 	public String getHelp() {
 		final StringBuilder buf = new StringBuilder();
 		final String newLine = System.getProperty("line.separator", "\r\n");
 		buf.append(newLine)
 		   .append("---Controlling the desktop bundle---").append(newLine)
 		   .append("\tdesktop - DesktopIntegration-related commands").append(newLine)
 		   .append("\t   open ... - open dialogues, when invoked without args lists available ones").append(newLine)
 		   .append("\t   tray     - toggle tray menu visibility").append(newLine);
 		return buf.toString();
 	}
 }
