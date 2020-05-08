 /*
  * Copyright (c) 2011 Karsten Heiken <karsten@disposed.de>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.scattport.client;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
 
 /**
  * Daemon that checks for new jobs and runs them.
  * 
  * @author Karsten Heiken <karsten@disposed.de>
  */
 public class Client {
 
 	/** Delay between two heartbeats. */
 	public static int HEARTBEAT_INTERVAL;
 	/** Delay between checking for new jobs. */
 	public static int JOBFETCHER_INTERVAL;
 	/** Delay between checking the progress of running jobs. */
 	public static int PROGRESS_INTERVAL;
 	/** The address of the XML-RPC controller on the ScattPort server. */
 	public static String SERVER_ADDRESS;
 	/** The secret needed to talk to the server. */
 	private static String SECRET;
 	/** Set to false when we want to quit. */
 	public static Boolean running = true;
 	/** List of currently running jobs. */
 	public static volatile List<Job> runningJobs = new ArrayList<Job>();
 	protected static Properties properties;
 
 	static XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 	static XmlRpcClient client = new XmlRpcClient();
 
 	static Thread heartbeat = new Thread(new Heartbeat());
 	static Thread jobfetcher = new Thread(new JobFetcher());
 	static Thread progresswatcher = new Thread(new ProgressWatcher());
 	static Thread watchdog = new Thread(new Watchdog());
 
 	/**
 	 * Main loop.
 	 * 
 	 * @param args
 	 *            not used
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws XmlRpcException, IOException {
 
 		// load properties from external file
 		properties = new Properties();
 		BufferedInputStream stream;
 		try {
 			stream = new BufferedInputStream(new FileInputStream(
 					"settings.properties"));
 			properties.load(stream);
 			stream.close();
 		} catch (FileNotFoundException e) {
 			System.err.println("settings.properties could not be found!");
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("settings.properties could not be loaded!");
 			System.exit(1);
 		}
 
 		// setup variables with loaded properties
 		SERVER_ADDRESS = properties.getProperty("server.url");
 		SECRET = properties.getProperty("client.secret");
 
 		HEARTBEAT_INTERVAL = Integer.parseInt(properties.getProperty(
 				"heartbeat.interval", "60"));
 		JOBFETCHER_INTERVAL = Integer.parseInt(properties.getProperty(
 				"fetchjob.interval", "10"));
 		PROGRESS_INTERVAL = Integer.parseInt(properties.getProperty(
 				"progress.interval", "5"));
 
 		// start threads
 		heartbeat.start();
 		jobfetcher.start();
 		progresswatcher.start();
 		watchdog.start();
 	}
 
	@SuppressWarnings("rawtypes")
	public static HashMap exec(String function, Object... params) {
		HashMap result = new HashMap();
 		try {
 			// TODO: Get server from an external file
 			config.setServerURL(new URL(SERVER_ADDRESS));
 			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
 			client.setConfig(config);
 
 			params = (new Object[] { SECRET, params });
			result = (HashMap) client.execute(function, params);
 		} catch (XmlRpcException ex) {
 			System.err.println("The XML-RPC API call was not successful:");
 			ex.printStackTrace();
 		} catch (MalformedURLException ex) {
 			System.err.println("The XML-RPC API call was not successful:");
 			System.err
 					.println("The URL was malformed. Please check settings.properties.");
 			System.exit(2);
 		} catch (NullPointerException npe) {
 			npe.printStackTrace();
 		}
 
 		return result;
 	}
 
 	/**
 	 * @return the runningJobs
 	 */
 	public static List<Job> getRunningJobs() {
 		return runningJobs;
 	}
 
 	public static void addJob(Job job) {
 		runningJobs.add(job);
 	}
 
 	public static void deleteJob(Job job) {
 		runningJobs.remove(job);
 	}
 }
