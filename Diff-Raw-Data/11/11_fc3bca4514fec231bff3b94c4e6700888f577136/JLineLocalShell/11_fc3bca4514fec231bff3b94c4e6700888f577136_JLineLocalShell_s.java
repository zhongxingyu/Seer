 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.shell.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.HashSet;
 import java.util.Set;
 
 import jline.ConsoleReader;
 
 import org.eclipse.virgo.kernel.shell.CommandExecutor;
 import org.eclipse.virgo.kernel.shell.LinePrinter;
 import org.eclipse.virgo.kernel.shell.internal.completers.CommandCompleterRegistry;
 import org.eclipse.virgo.kernel.shell.internal.completers.CommandRegistryBackedJLineCompletor;
 import org.eclipse.virgo.kernel.shell.internal.completers.DelegatingJLineCompletor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A wrapper around a JLine {@link ConsoleReader} that knows how to talk to 
  * the kernel and handle shutdown from the client.
  * </p>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * thread-safe
  *
  */
 final class JLineLocalShell implements Runnable, LocalShell {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JLineLocalShell.class);
 
     private static final String PROMPT = ":> ";
 
     private static final String EXIT_MSG = "Goodbye.";
 
     private final CommandProcessor commandProcessor;
 
     private final InputStream in;
 
     private final PrintStream out;
 
     private final Set<ExitCallback> callbacks = new HashSet<ExitCallback>();
 
     private final CommandRegistry commandRegistry;
 
     private final CommandCompleterRegistry completerRegistry;
 
     public JLineLocalShell(CommandRegistry commandRegistry, CommandCompleterRegistry completerRegistry, CommandProcessor commandProcessor,
         InputStream in, PrintStream out) {
         this.commandRegistry = commandRegistry;
         this.completerRegistry = completerRegistry;
         this.commandProcessor = commandProcessor;
         this.in = in;
         this.out = out;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addExitCallback(ExitCallback exitCallback) {
         if (exitCallback != null) {
             this.callbacks.add(exitCallback);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void run() {
         ConsoleReader console;
         try {
             console = new ConsoleReader(this.in, new LineSeparatorEnforcingWriter(new PrintWriter((this.out))));
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
         LOGGER.info("Launching a new shell in thread %s", Thread.currentThread().getName());
 
         console.setUseHistory(true);
         console.setUsePagination(true);
         console.setDefaultPrompt(PROMPT);
 
         console.addCompletor(new CommandRegistryBackedJLineCompletor(this.commandRegistry));
         console.addCompletor(new DelegatingJLineCompletor(this.completerRegistry));
         
         LinePrinter consoleLinePrinter = new ConsoleLinePrinter(console);
 
         CommandExecutor commandExecutor = new SessionCommandExecutor(this.commandProcessor.createSession());
         
         try {
             this.printHeader(consoleLinePrinter);
             
             while (commandExecutor.execute(console.readLine(), consoleLinePrinter)) { 
                 consoleLinePrinter.println();
             }
          
             consoleLinePrinter.println(EXIT_MSG);
             informCallbacksOfExit();
             
         } catch (IOException e) {
             this.out.println("Error occurred while writing to the shell. Please restart the shell bundle.");
             throw new IllegalStateException("The Shell was unable to recover from an IO error", e);
         }
     }
 
     private void informCallbacksOfExit() {
         for (ExitCallback exitCallback : this.callbacks) {
             exitCallback.onExit();
         }
     }
 
     /**
      * Each line in this header must be kept under 80 characters in order to avoid line wrapping problems on some
      * consoles.
      * 
      * Generated with the help of http://www.network-science.de/ascii/ using the 'slant' font.
      *
      * @param linePrinter
      * @throws IOException
      */
     private void printHeader(LinePrinter linePrinter) throws IOException {
        linePrinter.println();
         linePrinter.println("    _    ___");
        linePrinter.println("   | |  / (_)________ _____");
         linePrinter.println("   | | / / / ___/ __ `/ __ \\");
         linePrinter.println("   | |/ / / /  / /_/ / /_/ /");
         linePrinter.println("   |___/_/_/   \\__, /\\____/");
         linePrinter.println("              /____/");
         linePrinter.println();
         linePrinter.println("Type 'help' to see the available commands.");
     }
     
     private static final class LineSeparatorEnforcingWriter extends Writer {
         
         private final Writer delegate;
         
         LineSeparatorEnforcingWriter(Writer delegate) {
             this.delegate = delegate;
         }
 
         /** 
          * {@inheritDoc}
          */
         @Override
         public void close() throws IOException {
             this.delegate.close();
         }
 
         /** 
          * {@inheritDoc}
          */
         @Override
         public void flush() throws IOException {
             this.delegate.flush();
         }
 
         /** 
          * {@inheritDoc}
          */
         @Override
         public void write(char[] cbuf, int off, int len) throws IOException {
             for (int i = off; i < len; i++) {
                 char c = cbuf[i];
                 if (c == '\n') {
                     this.delegate.write('\r');
                 }
                 this.delegate.write(c);
             }
         }
     }
 }
