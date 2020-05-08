 /*******************************************************************************
  * This file is part of the Virgo Web Server.
  *
  * Copyright (c) 2010 Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgicommand.internal;
 
 import java.io.IOException;
 
 import org.eclipse.osgi.framework.console.CommandInterpreter;
 import org.eclipse.osgi.framework.console.CommandProvider;
 import org.eclipse.virgo.kernel.shell.CommandExecutor;
 import org.eclipse.virgo.kernel.shell.LinePrinter;
 
 /**
  * This {@link CommandProvider} extends the osgi.console with the command "vsh ..." which accesses the kernel shell commands.
  * <p />
  *
  * <strong>Concurrent Semantics</strong><br />
  * thread-safe
  *
  * @author Steve Powell
  */
 public final class OsgiKernelShellCommand implements CommandProvider {
     
     CommandExecutor commandExecutor;
     
     public OsgiKernelShellCommand(CommandExecutor commandExecutor) {
         this.commandExecutor = commandExecutor;
     }
     
     public void _vsh(CommandInterpreter commandInterpreter) {
         String commandLine = getCommandLine(commandInterpreter);
         LinePrinter linePrinter = new CommandInterpreterLinePrinter(commandInterpreter);
         try {
             boolean continueCommands = this.commandExecutor.execute(commandLine, linePrinter);
             if (!continueCommands) {
                 commandInterpreter.println("vsh: command '" + commandLine + "' requested exit");
             }
         } catch (IOException e) {
             commandInterpreter.println("vsh: command '" + commandLine + "' threw an exception...");
             commandInterpreter.printStackTrace(e);
         }
     }
     
     private static String getCommandLine(CommandInterpreter commandInterpreter) {
         StringBuilder sb = new StringBuilder();
         String arg = commandInterpreter.nextArgument();
         while (arg!=null) {
             sb.append(arg).append(" ");
             arg = commandInterpreter.nextArgument();
         }
         return sb.toString();
     }
 
     /** 
      * {@inheritDoc}
      */
     public String getHelp() {
        return "\tvsh - Virgo shell commands; 'vsh help' to list available commands\n";
     }
 
     /**
      * {@link LinePrinter} which uses a {@link CommandInterpreter} to output lines
      */
     private static final class CommandInterpreterLinePrinter implements LinePrinter {
         private final CommandInterpreter commandInterpreter;
         public CommandInterpreterLinePrinter(CommandInterpreter commandInterpreter) {
             this.commandInterpreter = commandInterpreter;
         }
         public LinePrinter println(String line) throws IOException {
             this.commandInterpreter.println(line);
             return this;
         }
         public LinePrinter println() throws IOException {
             this.commandInterpreter.println();
             return this;
         }
     }
 }
