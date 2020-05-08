 package com.upgradingdave.plugin;
 
 /*
  * Copyright 2011 Dave Paroulek (upgradingdave.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
import org.apache.log4j.Logger;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 //import javabrowser.JavaBrowser;
 
 /**
  * Goal that starts a Java Browser Server
  * 
  * @goal start
  * 
  */
 public class MyMojo extends AbstractMojo {
	
	protected static Logger log = Logger.getLogger(MyMojo.class); 
	
 	public void execute() throws MojoExecutionException {
 		try {
 			// JavaBrowser jb = new JavaBrowser();
 			// jb.startServer();
 
 			// System.out.println("Press any key to stop the javabrowser server");
 			// byte name[] = new byte[100];
 			// System.in.read(name);
			
			log.debug("Attempting to start javabrowser");
 			moveWar();
 			runWithJetty();
 
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 
 	public static void main(String args[]) {
 		try {
 			MyMojo mojo = new MyMojo();
			mojo.execute();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Not sure how to get jetty to load a war from the classpath, so first move
 	 * the war into target
 	 */
 	public void moveWar() {
 		File newWar = new File("target/javabrowser.war");
 		if (newWar.exists()) {
 			newWar.delete();
 		}
 
 		InputStream in = this.getClass().getClassLoader()
 				.getResourceAsStream("javabrowser.war");
 		OutputStream out = null;
 		try {
 			out = new FileOutputStream(newWar);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if(out != null)
 					out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
     public void runWithJetty() throws Exception {
         int port = Integer.getInteger("jetty.port", 9000).intValue();
         Server server = new Server();
 
         Connector connector = new SelectChannelConnector();
         connector.setPort(port);
         server.setConnectors(new Connector[] { connector });
 
         WebAppContext webapp = new WebAppContext();
         webapp.setContextPath("/");
         webapp.setWar("target/javabrowser.war");
         webapp.setTempDirectory(new File("target/javabrowser-tmp"));
         // webapp.setDefaultsDescriptor(jetty_home+"/etc/webdefault.xml");
 
         server.setHandler(webapp);
 
         server.start();
         server.join();
        log.info("JavaBrowser Server started on port "+port+", have fun!");
     }
 
 }
