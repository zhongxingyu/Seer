 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.valhalla.tools.process;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.valhalla.tools.process.classloader.SpawnerClassLoader;
 
 /**
  * This class will spawn a new process and will execute the main method for the passed class.  
  * It can also execute a given method but will require that the class contain a no-arg 
  * constructor.
  * 
  * @author Claudio Corsi
  */
 public class Spawner 
 {
 	private static final Logger log = LoggerFactory.getLogger(Spawner.class);
 	
 	private String className;
 	private String methodName;
 	private SpawnerClassLoader classLoader;
 	private String spawnedClassPath = "";
 	private Process process;
 	private ServerSocket server;
 	private String identifier = "Spawner" + System.currentTimeMillis();
 
 	private volatile boolean processExited;
 
 	private List<String> jvmArgs = Collections.emptyList();
 	
 	public Spawner(String className) {
 		this(className,null);
 	}
 	
 	public Spawner(String className, String methodName) {
 		this.className = className;
 		this.methodName = methodName;
 	}
 	
 	public void setJVMArgs(List<String> jvmArgs) {
 		this.jvmArgs = jvmArgs;
 	}
 	
 	public void spawnProcess() throws IOException, InterruptedException {
 		log.debug("INSIDE spawnProcess");
 		// Setup the classloader and determine the port used to process the incoming requests.
 		// TODO: make the java command configurable.
 		String javaHome = System.getProperty("java.home");
 		String javaExe = javaHome + File.separator + "bin" + File.separator + "java";
 		int port = -1;
 		String hostname = null;
 		ClassLoader parent = Thread.currentThread().getContextClassLoader();
 		parent = (ClassLoader.getSystemClassLoader() == parent) ? null : parent;
 		if (parent != null) {
 			server = new ServerSocket(0);
 			port = server.getLocalPort();
 			hostname = InetAddress.getLocalHost().getHostName();
 			classLoader = new SpawnerClassLoader(server, parent, identifier);
 			classLoader.start();
 		}
 		List<String> command = new LinkedList<String>();
 		command.add(javaExe);
 		command.addAll(jvmArgs);
 		command.add("-cp");
 		command.add(System.getProperty("java.class.path") + File.pathSeparator + spawnedClassPath);
 		command.add(Spawned.class.getName());
 		// Create a remote class loader only if the current thread has a context class loader set.
 		if (parent != null) {
 			command.add(Options.PORT);
 			command.add(String.valueOf(port));
 			command.add(Options.HOST);
 			command.add(hostname);
 		} else {
 			command.add(Options.NOCLASSLOADER);
 		}
 		command.add(Options.CLASSNAME);
 		command.add(className);
 		if (methodName != null) {
 			command.add(Options.METHODNAME);
 			command.add(methodName);
 		}
 		// Store the system properties to a file that will be loaded by the spawned application.
 		Properties props = System.getProperties();
 		File file = File.createTempFile("spawned", ".properties");
 		Writer out = new FileWriter(file);
 		props.store(out, "Spawned properties");
 		out.close();
 		command.add(Options.PROPERTYFILE);
 		command.add(file.getAbsolutePath());
 		log.info("command: {}", command);
 		ProcessBuilder processBuilder = new ProcessBuilder(command);
 		processBuilder = processBuilder.redirectErrorStream(true);
 		process = processBuilder.start();
 		BufferedReader input = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
 		new Thread( new Runnable() {
 			
 			private BufferedReader input;
 			
 			Runnable setInput(BufferedReader input) {
 				this.input = input;
 				return this;
 			}
 			
 			public void run() {
 				log.debug("Inside run");
 				String line;
 				try {
 					while((line = input.readLine()) != null) {
 						log.info("stdout: {}", line);
 					}
 				} catch (IOException e) {
 					log.debug("An exception was raised while trying to read the next line", e);
 				}
 			}
 		}.setInput(input)) {
 			{
 				setName(Spawner.this.identifier + getName());
 				start();
 			}
 		};
 
 		new Thread() {
			private File file;

 			public void run() {
 				try {
 					int result = process.waitFor();
 					log.info("Exiting process with: {}", result);
 				} catch (InterruptedException e) {
 					log.error("An exception was generated when waiting for process to exit", e);
 				} finally {
 					Spawner.this.processExited = true;
					if (file.exists()) {
						file.delete();
					}
 				}
 			}

			public void setFile(File file) {
				this.file = file;
				setName(Spawner.this.identifier + getName());
				start();
			}
		}.setFile(file);
		
 	}
 	
 	public void stopProcess() {
 		// This will stop the process from continuing by terminating the process.
 		process.destroy();
 		
 		exitProcess();
 	}
 	
 	public void exitProcess() {
 		if( classLoader != null ) {
 			// Just to get rid of the warning....
 			classLoader.getClass();
 		}
 		// Send a request to shutdown the spawned process and return...
 		classLoader = null;
 	}
 	
     	/**
 	 * @param spawnedClassPath the spawnedClassPath to set
 	 */
 	public void setSpawnedClassPath(String spawnedClassPath) {
 		this.spawnedClassPath = spawnedClassPath;
 	}
 
 	/**
 	 * @param identifier
 	 */
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 
 	/**
 	 * @return the processExited
 	 */
 	public boolean isProcessExited() {
 		return processExited;
 	}
 
 }
