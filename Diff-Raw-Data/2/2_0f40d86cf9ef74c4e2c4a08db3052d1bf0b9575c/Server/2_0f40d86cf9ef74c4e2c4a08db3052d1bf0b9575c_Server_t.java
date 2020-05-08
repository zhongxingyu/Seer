 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // Server.java
 // Since: Jan 10, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import org.utgenome.config.UTGBConfig;
 import org.xerial.util.FileUtil;
 import org.xerial.util.log.Logger;
 
 import java.io.*;
 import java.util.Properties;
 import java.util.TreeMap;
 
 /**
  * A UTGB sub command for starting up a UTGB portable server
  * 
  * @author leo
  * 
  */
 public class Server extends UTGBShellCommand {
 
 	private static Logger _logger = Logger.getLogger(Server.class);
 
 	private UTGBPortableConfig option = new UTGBPortableConfig();
 
 	public Server() {
 
 	}
 
 	@Override
 	public void execute(String[] args) throws Exception {
 
 		if (!isInProjectRoot())
 			throw new UTGBShellException("not in the track project root");
 
 		// create war/utgb (GWT module) folder
 		FileUtil.mkdirs(new File(getProjectRoot(), "war/" + option.gwtModule));
 
 		// copy resources
 		Properties prop = new Properties();
 		prop.setProperty("gwt.module", option.gwtModule);
		maven("war:exploded", prop);
 
 		UTGBConfig config = loadUTGBConfig();
 		String projectName = config.projectName;
 		if (option.contextPath == null)
 			option.contextPath = "/" + (projectName != null ? projectName : "utgb");
 
 		// create context.xml
 		option.projectRoot = new File(globalOption.projectDir == null ? "" : globalOption.projectDir).getAbsolutePath();
 		createContextXML(projectName, option.projectRoot, true);
 
 		if (_logger.isDebugEnabled())
 			_logger.debug(option);
 
 		// File change polling
 		if (option.pollingFileChange) {
 			Thread pollingThread = new Thread(new Runnable() {
 
 				private TreeMap<File, Long> table = new TreeMap<File, Long>();
 
 				public void run() {
 					File[] watchFolder = new File[] { new File("src/main/webapp"), new File("src/main/template"), new File("target/classes") };
 
 					while (true) {
 						try {
 							if (watch(watchFolder)) {
 								Maven.runMaven("war:exploded", new File(option.projectRoot));
 								Thread.sleep(1000);
 							}
 						}
 						catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				public boolean watch(File[] files) {
 					for (File each : files) {
 						if (watch(each))
 							return true;
 					}
 					return false;
 				}
 
 				public boolean watch(File f) {
 					if (!f.isDirectory()) {
 						if (!table.containsKey(f)) {
 							table.put(f, f.lastModified());
 							return false;
 						}
 						else {
 							long lastModified = table.get(f);
 							if (lastModified < f.lastModified()) {
 								table.put(f, f.lastModified());
 								return true;
 							}
 							else
 								return false;
 						}
 					}
 					else {
 						File[] files = f.listFiles();
 						if (files == null)
 							return false;
 						for (File each : files) {
 							boolean updated = watch(each);
 							if (updated)
 								return true;
 						}
 						return false;
 					}
 				}
 			});
 			pollingThread.setDaemon(true);
 			pollingThread.start();
 		}
 
 		UTGBPortable server = new UTGBPortable(option);
 
 		server.addServerListener(new ServerListener() {
 			public void beforeStart() {
 				try {
 					_logger.info("synchronizing the folder contents from " + option.webAppDir + " to " + option.workingDir);
 
 					rsync(getProjectResourcePath(option.webAppDir).getAbsolutePath(), getProjectResourcePath(option.workingDir).getAbsolutePath());
 				}
 				catch (Exception e) {
 					_logger.error(e);
 
 				}
 
 			}
 
 			public void afterStart() {
 				_logger.info("Started the server");
 			}
 
 			public void afterStop() {
 				_logger.info("Server has teminated");
 			}
 
 			public void beforeStop() {
 				_logger.info("Terminating the server");
 			}
 		});
 		server.start();
 
 	}
 
 	public static void rsync(File src, File dest) throws UTGBShellException, IOException {
 		if (!src.exists())
 			return;
 
 		if (src.isDirectory()) {
 
 			String dirName = src.getName();
 			// ignore .svn, .cvs folders
 			if (dirName.endsWith(".svn") || dirName.endsWith(".cvs"))
 				return;
 
 			// prepare the target directory
 			if (dest.exists()) {
 				if (!dest.isDirectory())
 					throw new UTGBShellException("cannot create a directory " + getPath(dest) + ": a file with the same name already exists.");
 			}
 			else {
 				_logger.info("create a directory: " + getPath(dest));
 				dest.mkdirs();
 			}
 
 			// copy contents under the direcotry
 			for (File childFile : src.listFiles()) {
 				rsync(childFile, new File(dest, childFile.getName()));
 			}
 		}
 		else {
 			// copy the src file
 			boolean toOverwrite = dest.exists() ? src.lastModified() > dest.lastModified() : true;
 			if (!toOverwrite)
 				return;
 
 			// prepare the parent folder
 			File parentFolder = dest.getParentFile();
 			if (!parentFolder.exists()) {
 				_logger.info("create a directory: " + getPath(parentFolder));
 				parentFolder.mkdirs();
 			}
 			_logger.info("create a file: " + getPath(dest));
 			BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
 			FileOutputStream out = new FileOutputStream(dest);
 			byte[] buffer = new byte[1024];
 			int bytesRead = 0;
 			while ((bytesRead = in.read(buffer)) > 0) {
 				out.write(buffer, 0, bytesRead);
 			}
 			out.flush();
 			out.close();
 		}
 
 	}
 
 	public static void rsync(String src, String dest) throws UTGBShellException, IOException {
 		rsync(new File(src), new File(dest));
 	}
 
 	@Override
 	public String name() {
 		return "server";
 	}
 
 	@Override
 	public String getOneLinerDescription() {
 		return "start up a portable web server";
 	}
 
 	@Override
 	public Object getOptionHolder() {
 		return option;
 	}
 
 }
