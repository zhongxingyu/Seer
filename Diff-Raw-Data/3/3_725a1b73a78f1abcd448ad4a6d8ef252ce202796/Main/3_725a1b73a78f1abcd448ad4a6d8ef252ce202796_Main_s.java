 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import com.nginious.http.cmd.CommandLineArguments;
 import com.nginious.http.cmd.CommandLineException;
 
 /**
  * Main start class for HTTP server. Parses command line parameters, configures and starts
  * HTTP server.
  * 
  * <p>
  * The following command line parameters are accepted
  * 
  * <ul>
  * <li>-i [interfaces] | --interfaces=[interfaces] (all) - Comma separated list of network interfaces that server listens to.</li>
  * <li>-p [port] | --port=[port] (80) - Port that server listens to.</li>
  * <li>-d [dir] | --webappsDir=[dir] (webapps) - Directory for web applications.</li>
  * <li>-a [password] | --adminPassword=[password] (admin) - Web applications REST service admin password.</li>
  * <li>-s [type] | --session=[type] (memory) - Session manager type (memory|cookie).</li>
  * </ul>
  * </p>
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  */
 public class Main {
 	
 	/**
 	 * Starts up ProjectX by configuring and starting the HTTP server. Configuration arguments are
 	 * read from command line arguments.
 	 * 
 	 * @param argv command line arguments
 	 */
 	public static void main(String[] argv) {
 		HttpServerConfiguration config = new HttpServerConfiguration();
 		CommandLineArguments args = CommandLineArguments.createInstance(config);
 		HttpServerImpl server = null;
 		
 		try {
 			args.parse(argv);
 			
 			HttpServerFactory factory = HttpServerFactory.getInstance();
 			server = (HttpServerImpl)factory.create(config);
 			server.start();
 			
 			ShutdownHook hook = new ShutdownHook(server);
 			Runtime.getRuntime().addShutdownHook(new Thread(hook));
 		} catch(CommandLineException e) {
 			args.help(new PrintWriter(System.out));
 		} catch(IOException e) {
 			System.out.println("Unable to start server, see logs/server.log for more information");
 		}
 	}
 }
