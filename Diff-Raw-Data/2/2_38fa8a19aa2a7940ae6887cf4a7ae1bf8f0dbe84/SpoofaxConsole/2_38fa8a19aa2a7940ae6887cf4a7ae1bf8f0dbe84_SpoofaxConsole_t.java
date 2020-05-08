 /*
  * Copyright (c) 2011, Karl Trygve Kalleberg <karltk at strategoxt dot org>
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.ui;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Writer;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.console.IOConsole;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 import org.spoofax.interpreter.library.IOAgent;
 
 public class SpoofaxConsole extends IOConsole implements Runnable {
 	
 	private boolean isDisposed;
 	
 	public static final String CONSOLE_NAME =
 		"Spoofax Console";
 
 	public static class ConsoleIOAgent extends IOAgent {
 
 		final PrintWriter out;
 		final PrintWriter err;
 
 		public ConsoleIOAgent(IOConsoleOutputStream out,
 				IOConsoleOutputStream err) {
 			this.out = new PrintWriter(out);
 			this.err = new PrintWriter(err);
 		}
 
 		@Override
 		public Writer getWriter(int fd) {
 			if (fd == CONST_STDERR) {
 				return err;
 			} else if (fd == CONST_STDOUT) {
 				return out;
 			}
 			return super.getWriter(fd);
 		}
 
 	}
 	
 	@Override
 	protected void dispose() {
 		super.dispose();
 		this.isDisposed = true;
 	}
 	
 	public boolean isDisposed() {
 		return isDisposed;
 	}
 
 	public SpoofaxConsole() {
 		super(CONSOLE_NAME, ImageDescriptor.getMissingImageDescriptor());
 		Thread t = new Thread(this);
 		t.start();
 	}
 
 	@Override
 	public String getName() {
 		return "Spoofax Console";
 	}
 
 	@Override
 	public void run() {
 		try {
 			final IOConsoleOutputStream err = newOutputStream();
 			final IOConsoleOutputStream out = newOutputStream();
 			final IOConsoleOutputStream prompt = newOutputStream();
 
 			Display.getDefault().syncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					err.setColor(new Color(null, 255, 0, 0));
 					out.setColor(new Color(null, 234, 123, 195));
 					prompt.setColor(new Color(null, 95, 200, 23));
 				}
 			});
 			
 			ConsoleIOAgent ioAgent = new ConsoleIOAgent(out, err);
 			SpoofaxInterpreter intp = new SpoofaxInterpreter(ioAgent);
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					getInputStream()));
 			for (;;) {
 				try {
 					prompt.write("> ");
 					prompt.flush();
 					
 					String line = br.readLine();
					if (line == null || !intp.eval(line))
 						break;
 				} catch (IOException e) {
 					// assume that the console has been closed
 					break;
 				}
 			}
 		} catch (Exception e) {
 			InterpreterPlugin.logError("Fatal error in interpreter thread", e);
 		}
 	}
 
 }
