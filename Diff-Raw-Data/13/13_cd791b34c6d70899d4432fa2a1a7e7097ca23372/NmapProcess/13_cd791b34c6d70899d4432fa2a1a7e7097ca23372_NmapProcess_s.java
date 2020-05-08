 package com.waitwha.nmap.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import com.waitwha.logging.LogManager;
 import com.waitwha.util.ArrayUtils;
 
 /**
  * <b>NmapTools</b>: NmapProcess<br/>
  * <small>Copyright (c)2013 Mike Duncan &lt;<a href="mailto:mike.duncan@waitwha.com">mike.duncan@waitwha.com</a>&gt;</small><p />
  *
  * <pre>
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * </pre>
  *
  * TODO Document this class/interface.
  *
  * @author Mike Duncan <mike.duncan@waitwha.com>
  * @version $Id$
  * @package com.waitwha.nmap.utils
  */
 public final class NmapProcess 
 	implements Runnable {
 
 	private static final Logger log = LogManager.getLogger(NmapProcess.class);
 	private static NmapProcess process;
 	private File path;
 	private String[] args;
 	private boolean running;
 	private int exitCode;
 	
 	private NmapProcess(File path, String[] args)  {
 		this.path = path;
 		this.args = args;
 		this.running = false;
 		this.exitCode = 1;
 	}
 	
 	private NmapProcess(String path, String[] args)  {
 		this(new File(path), args);
 	}
 	
 	public static final NmapProcess getInstance(String path, String[] args)  {
 		if(process == null)
 			process = new NmapProcess(path, args);
 		
 		return process;
 	}
 	
 	public boolean isRunning()  {
 		return running;
 	}
 	
 	public int exec()  {
 		Thread t = new Thread(this);
 		t.start();
 		try  {
 			t.join();
 		}catch(Exception e) {}
 		
 		return this.exitCode;
 	}
 	
 	@Override
 	public void run() {
 		this.running = true;
 		this.exitCode = -1;
 		
 		try  {
 			log.info(String.format("Starting %s process with arguments: %s", this.path, ArrayUtils.merge(this.args, " ")));
 			ProcessBuilder builder = 
					new ProcessBuilder(this.path.toString(), ArrayUtils.merge(this.args, " "));
 			builder.redirectErrorStream(true);
 			Process p = builder.start();
 			BufferedReader procReader = 
 					new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String output = "";
 			while((output = procReader.readLine()) != null)
 				log.fine(output);
 			
 			exitCode = p.exitValue();
 			log.info(String.format("%s process completed with exit code: %d", this.path, this.exitCode));
 			
 		}catch(Exception e) {
 			log.warning(String.format("Failed to execute/complete %s process: %s %s", this.path, e.getClass().getName(), e.getMessage()));
 			
 		}
 		
 		this.running = false;
 	}
 
 }
