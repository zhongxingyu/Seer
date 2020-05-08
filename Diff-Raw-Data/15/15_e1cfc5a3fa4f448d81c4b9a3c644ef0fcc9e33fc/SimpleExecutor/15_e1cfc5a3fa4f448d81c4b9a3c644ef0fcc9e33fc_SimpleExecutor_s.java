 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
  
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */ 
 package org.lyllo.kickassplugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.widgets.Display;
 
 public class SimpleExecutor {
 
 
 	public static class Worker extends Thread {
 
 		private Process process;
 
 		public Worker(Process p) {
 			this.process = p;
 		}
 
 		public void run() {
 
 			BufferedReader br = null, brerr = null;
 			try {
 				InputStream is = process.getInputStream();
 				InputStream err = process.getErrorStream();
 				
 				br = new BufferedReader(new InputStreamReader(is));
 				brerr = new BufferedReader(new InputStreamReader(err));
 				String line, errorline = null;
 				while ((line = br.readLine()) != null || (errorline = brerr.readLine()) != null) {
 					
 					if (line != null){
 						final String output = line;
 						Display.getDefault().asyncExec(new Runnable() {
 							public void run() {
 								Activator.getConsole().println(output);
 							}
 						});
 					}
 
 					if (errorline != null){
 						final String erroutput = errorline;
 						Display.getDefault().asyncExec(new Runnable() {
 							public void run() {
 								Activator.getConsole().println("[err] " + erroutput);
 							}
 						});
 					}
 					
 				}
 				
 				process.waitFor();
 
 			} catch (final Exception ex){
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						Activator.getDefault().getLog().log(
 								new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
 										Messages.LAUNCH_ERROR, ex));
 					}
 				});
 			} finally {
 				if (br != null){
 					try {
 						br.close();
 					} catch (Exception ex){
 						
 					}
 				}
 				if (brerr != null){
 					try {
 						brerr.close();
 					} catch (Exception ex){
 						
 					}
 				}
 			}
 		}
 
 	}
 	public static void exec(String[] cmdLine, String destdir) {
 		exec(cmdLine, destdir, false);
 	}
 
 	public static void exec(String[] cmdLine, String destdir, boolean wait) {
 
 		try {
 			Activator.getDefault().getLog().log(
 					new Status(Status.INFO, Constants.PLUGIN_ID, "Executing " + Arrays.toString(cmdLine)));
 
 			Process p  = Runtime.getRuntime().exec(cmdLine, null, new File(destdir));
 
 			Worker worker = new Worker(p);
 			worker.start();
 			
 			if (wait){
 				worker.join();
 			}
 
 		} catch (Exception e) {
 			Activator.getDefault().getLog().log(
 					new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
 							Messages.LAUNCH_ERROR, e));
 		} 
 	}
 
 }
