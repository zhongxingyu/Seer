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
 // UTGBPortable.java
 // Since: Jan 30, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.utgenome.shell.tomcat.TomcatServer;
 import org.utgenome.shell.tomcat.TomcatServerConfiguration;
 import org.xerial.core.XerialException;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.OptionParser;
 import org.xerial.util.opt.OptionParserException;
 
 /**
  * The stand-alone UTGB server launcher
  * 
  * @author leo
  * 
  */
 public class UTGBPortable implements TomcatServerLauncher {
 
 	private static Logger _logger = Logger.getLogger(UTGBPortable.class);
 
 	/**
 	 * entry point of the UTGBServer
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			UTGBPortable server = new UTGBPortable(args);
 			server.start();
 		}
 		catch (OptionParserException e) {
 			_logger.error(e);
 		}
 		catch (Exception e) {
 			_logger.error(e);
 			e.printStackTrace(System.err);
 		}
 	}
 
 	// private ThreadManager threadManager = new ThreadManager();
 	private ExecutorService threadPool = Executors.newFixedThreadPool(1);
 
 	private UTGBPortableConfig config = new UTGBPortableConfig();
 	private OptionParser parser = new OptionParser(config);
 
 	/**
 	 * Create an instance of the UTGBServer
 	 * 
 	 * @param args
 	 *            command-line arguments
 	 * @throws OptionParserException
 	 */
 	public UTGBPortable(String[] args) throws OptionParserException {
 
 		// parse the command-line arguments
 		parser.parse(args);
 	}
 
 	public UTGBPortable(UTGBPortableConfig config) {
 		this.config = config;
 	}
 
 	private ArrayList<ServerListener> listenerList = new ArrayList<ServerListener>();
 
 	public void addServerListener(ServerListener listener) {
 		this.listenerList.add(listener);
 	}
 
 	/**
 	 * Starts the web server
 	 * 
 	 * @throws XerialException
 	 */
 	public void start() throws XerialException {
 
 		if (config.useGUI)
 			runInGUIMode();
 		else
 			runInCUIMode();
 
 		try {
 			while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
 			}
 		}
 		catch (InterruptedException e) {
 			_logger.error(e);
 			threadPool.shutdownNow();
 		}
 	}
 
 	public void start(int terminationTime, TimeUnit timeUnit) throws XerialException {
 		// add a shutdown hook when JVM terminates or GUI window is closed
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				stopTomcatServer(config);
 			}
 		});
 
 		if (config.useGUI)
 			runInGUIMode();
 		else
 			runInCUIMode();
 
 		threadPool.shutdownNow();
 		try {
 			while (!threadPool.awaitTermination(terminationTime, timeUnit)) {
 			}
 		}
 		catch (InterruptedException e) {
 
 		}
 		finally {
 			threadPool.shutdownNow();
 		}
 	}
 
 	protected void runInGUIMode() {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					UTGBPortableWidget portableWidget = new UTGBPortableWidget(config);
 					portableWidget.setTomcatServerLauncher(UTGBPortable.this);
 					portableWidget.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 					portableWidget.setLocation((int) d.getWidth() / 4, (int) d.getHeight() / 4);
 					portableWidget.setVisible(true);
 					portableWidget.pushStart();
 				}
 			});
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 
 	}
 
 	protected void runInCUIMode() throws XerialException {
 
 		startTomcatServer(config);
 
 		// wait until Ctrl+C terminates the program
 		try {
 			while (!Thread.currentThread().isInterrupted() && serverStatus != ServerStatus.ERROR) {
 				Thread.sleep(1000L);
 			}
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static enum ServerStatus {
 		STOPPED, STARTED, ERROR
 	}
 
 	private TomcatServer tomcatServer = null;
 	private ServerStatus serverStatus = ServerStatus.STOPPED;
 
 	private class TomcatStarter implements Callable<ServerStatus> {
 		private UTGBPortableConfig config;
 
 		public TomcatStarter(UTGBPortableConfig config) {
 			this.config = config;
 			_logger.debug("tomcat starter: " + (SwingUtilities.isEventDispatchThread() ? "event dispatch thread" : "normal thread"));
 		}
 
 		public ServerStatus call() throws Exception {
 
 			// start the server
 			for (ServerListener listener : listenerList)
 				listener.beforeStart();
 			_logger.debug("before start");
 			tomcatServer.start();
 			serverStatus = ServerStatus.STARTED;
 			tomcatServer.addContext(config.contextPath, new File(config.projectRoot, config.workingDir).getAbsolutePath());
 
 			for (ServerListener listener : listenerList)
 				listener.afterStart();
 
 			return ServerStatus.STARTED;
 		}
 	}
 
 	/**
 	 * starts the Tomcat server
 	 * 
 	 * @throws XerialException
 	 */
 	public void startTomcatServer(UTGBPortableConfig utgbPortableConfig) throws XerialException {
 		switch (serverStatus) {
 		case STOPPED:
 			// create a new instance of the TomcatServer
 			TomcatServerConfiguration tomcatConfig = new TomcatServerConfiguration();
 			// tomcatConfig.setCatalinaBase(utgbPortableConfig.workingDir);
 			tomcatConfig.setPort(utgbPortableConfig.portNumber);
 			tomcatServer = new TomcatServer(tomcatConfig);
 
 			_logger.debug(SwingUtilities.isEventDispatchThread() ? "event dispatch thread" : "normal thread");
 			_logger.info("starting a Tomcat server: \n" + utgbPortableConfig.toString());
 
 			TomcatStarter starter = new TomcatStarter(utgbPortableConfig);
 			Future<ServerStatus> future = threadPool.submit(starter);
 			try {
 				serverStatus = future.get();
 			}
 			catch (Exception e) {
 				_logger.error(e);
 				serverStatus = ServerStatus.ERROR;
				threadPool.shutdownNow();
 			}
 
 			break;
 		case STARTED:
 			break;
 		}
 	}
 
 	/**
 	 * stops the Tomcat server
 	 */
 	public void stopTomcatServer(UTGBPortableConfig config) {
 		switch (serverStatus) {
 		case STOPPED:
 			break;
 		case STARTED:
 			try {
 				for (ServerListener listener : listenerList)
 					listener.beforeStop();
 
 				tomcatServer.stop();
 				tomcatServer = null;
 
 				for (ServerListener listener : listenerList)
 					listener.afterStop();
 			}
 			catch (XerialException e) {
 				e.printStackTrace();
 			}
 			finally {
 				serverStatus = ServerStatus.STOPPED;
 			}
 			break;
 		}
 	}
 
 }
