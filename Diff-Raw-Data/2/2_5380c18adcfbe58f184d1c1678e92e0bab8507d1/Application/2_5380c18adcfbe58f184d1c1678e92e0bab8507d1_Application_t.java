 /*
  * Copyright 2011 Matthias van der Vlies
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package models;
 
 import java.io.File;
 import java.net.ConnectException;
 import java.util.Set;
 import java.util.concurrent.TimeoutException;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.Play;
 import play.Play.Mode;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import play.libs.WS;
 import play.libs.WS.WSRequest;
 import scm.VersionControlSystem;
 import scm.VersionControlSystemFactory;
 import scm.VersionControlSystemFactory.VersionControlSystemType;
 import core.ConfigurationManager;
 import core.ProcessManager;
 import core.ProcessManager.ProcessType;
 
 /**
  * JPA entity for defining an application
  */
 @Entity
 @Table(name="applications")
 public class Application extends Model {
 	
 	private static final int ONE_SECOND = 1000;
 
 	/**
 	 * Program ID
 	 */
 	@Required
 	@Column(updatable = false, unique = true, nullable = false)
 	public String pid;
 	
 	/**
 	 * Type of VCS used for checkout
 	 */
 	@Column(updatable = false, nullable = false)
 	@Required
 	public VersionControlSystemType vcsType;
 	
 	/**
 	 * URL to be used for the VCS
 	 */
 	@Column(updatable = false, nullable = false)
 	@Required
 	public String vcsUrl;
 	
 	/**
 	 * Is the application checked out by the container?
 	 */
 	public Boolean checkedOut = false;
 	
 	/**
 	 * Is the application enabled? i.e. started/stopped
 	 */
 	public Boolean enabled;
 	
 	/**
 	 * What Play! mode should be used for running the application
 	 */
 	@Column(updatable = true, nullable = false)
 	@Required
 	public Mode mode;
 	
 	/**
 	 * Configuration properties used for application.conf generation
 	 */
 	@OneToMany(fetch=FetchType.EAGER, mappedBy="application")
 	public Set<ApplicationProperty> properties;
 	
 	public static Integer getCommandTimeout() {
 		return Integer.valueOf(Play.configuration.getProperty("command.timeout"));
 	}
 	
 	/**
 	 * Start the application
 	 * @param force Force start?
 	 * @param enable Set application enabled
 	 */
 	public void start(boolean force, boolean enable) throws Exception {
 		if(!force && !enabled) {
 			throw new Exception("Can not start disabled application " + pid);
 		}
 		else if(isRunning()) {
 			throw new Exception("Application " + pid + " is already running.");
 		}
 		
 		// generate application.conf
 		ConfigurationManager.generateConfigurationFiles(this);
 		
 		// Store play start pid for kept pid process
 		final String startPid = pid + ProcessManager.PROCESS_START_POSTFIX;
 		
 		try {
 			// Some processes may take some time to boot (pre-compiling, @OnApplicationStart jobs)
 			// So we will be making some HTTP requests to check if it's up
 			final ApplicationProperty address = ApplicationProperty.findHostProperty(this);
 			final ApplicationProperty port = ApplicationProperty.findPortProperty(this);
 			final String url = "http://" + (address == null ? "127.0.0.1" : address.value) + ":" + port.value;
 
 			// Let's first see if there already is another application running on this port
 			checkForOtherApplication(url);
 			
 			ProcessManager.executeCommand(startPid, ProcessManager
 					.getFullPlayPath()
 					+ " start .", new StringBuffer(), new File("apps/" + pid
 					+ "/"), true);
 
 			// Send 'ping' HTTP requests to verify the application
 			checkApplicationIsRunning(url);
 
 			// final check just to make sure it really started
 			ProcessManager.executeCommand(pid + "-status", ProcessManager
 					.getFullPlayPath()
 					+ " status .", new StringBuffer(), new File("apps/" + pid
 					+ "/"), false);
 			
 			if(enable) {
 				enabled = true;
 				save();
 				
 				// flush the state to the database because we are going to remove the kept id
 				em().flush();
 			}
 			
 			Logger.info("Started %s", pid);
 		}
 		catch(TimeoutException e) {
 			Logger.info("Could not determine whether %s started, time-out value: %s reached", pid, getCommandTimeout());
 			Logger.info("Check status manually and remove server.pid manually when needed");
 			throw e;
 		}
 		catch(Exception e) {
 			Logger.info(e, "Failed to start %s", pid);
 			
 			// Try to delete server.pid
 			final File serverPid = new File("apps/" + pid + "/server.pid");
			if(force && serverPid.exists() && !new File("apps/" + pid + "/server.pid").delete()) {
 				throw new Exception("Unable to remove server.pid for falsely started application, remove manually");
 			}
 			
 			throw e;
 		}
 		finally {
 			ProcessManager.removeKeptPid(startPid);
 		}
 	}
 
 	/**
 	 * Verify whether an application is started by sending HTTP 'pings' for a specified time-out period
 	 * @throws TimeoutException Is thrown when the time-out period is expired
 	 */
 	private void checkApplicationIsRunning(final String url)
 			throws InterruptedException, TimeoutException {
 		int n = 0;
 		int timeout = getCommandTimeout();
 		while(n < timeout) {
 			try {
 				final WSRequest request = WS.url(url);
 				request.timeout("1s"); // low time-out so we make sure the time-out cycle is as long as we define it to be
 				request.get();
 				break;
 			}
 			catch(RuntimeException e) {
 				Thread.sleep(ONE_SECOND);
 				n++;
 			}
 		}
 		
 		if(n == timeout) {
 			throw new TimeoutException("Time-out value reached");
 		}
 	}
 
 	/**
 	 * Check for any other application that may be present on the port and throw an exception if there is any.
 	 */
 	private void checkForOtherApplication(final String url) throws Exception {
 		try {
 			final WSRequest request = WS.url(url);
 			request.timeout("1s"); // set time-out to a low value to make sure
 									// we time-out when there is a connect but
 									// no answer, for example with SSH
 			request.get();
 			
 			throw new Exception("There is already another application bound to " + url);
 		}
 		catch(Exception e) {
 			// very dirty, but Play! wraps all upper level exceptions, so there really isn't any other way
 			if(e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof ConnectException) {				
 				// this is good
 				Logger.info("Port seems to be free");
 			}
 			else {
 				// this means that there is an application there 
 				// there is either a timeout, a HTTP app, or another protocol than HTTP
 				throw new Exception("There is already another application bound to " + url + ": " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Run play deps command for the application
 	 */
 	private void resolveDependencies() throws Exception {
 		ProcessManager.executeCommand(pid + "-deps", ProcessManager
 				.getFullPlayPath()
 				+ " deps --sync .", new StringBuffer(), new File("apps/" + pid
 				+ "/"), false);
 	}
 	
 	/**
 	 * Stop the application
 	 */
 	public void stop() throws Exception {
 		ProcessManager.executeProcess(pid + "-stop", ProcessManager.getFullPlayPath() + " stop .", new File("apps/" + pid + "/"), false);
 		Logger.info("Application %s stopped", pid);
 	}
 	
 	/**
 	 * Restart the application
 	 */
 	public void restart() throws Exception {
 		// if the application still has enabled as true a stop will kill the process and the process manager will restart it
 		stop();
 	}
 	
 	/**
 	 * Is the application running?
 	 */
 	@Transient
 	public boolean isRunning() throws Exception {
 		if(!checkedOut) {
 			throw new Exception("Application " + pid + " has not yet been checked out from SCM");
 		}
 		return ProcessManager.isProcessRunning(pid, ProcessType.PLAY);
 	}
 	
 	public boolean isBooting() throws Exception {
 		return ProcessManager.isKeptPidAvailable(pid + ProcessManager.PROCESS_START_POSTFIX);
 	}
 	
 	/**
 	 * Pull most recent version from VCS
 	 */
 	public void pull() throws Exception {
 		// pull before touching the process (or we risk killing a process on updating failure)
 		final VersionControlSystem vcs = VersionControlSystemFactory.getVersionControlSystem(vcsType);
 		vcs.cleanup(pid); // cleanup working directory
 		vcs.update(pid); // pull changes from git
 		
 		resolveDependencies();
 		
 		// if the application was already running this will force the process manager to restart the process
 		stop();
 	}
 
 	/**
 	 * Fetch application from SCM for the first time
 	 */
 	public void checkout() throws Exception {
 		if(checkedOut) {
 			throw new Exception("Application " + pid + " is already checked out");
 		}
 		
 		VersionControlSystemFactory.getVersionControlSystem(vcsType).checkout(pid, vcsUrl);
 		
 		resolveDependencies();
 		
 		checkedOut = true;
 		save();
 		
 		ConfigurationManager.readCurrentConfigurationFromFile(this);
 	}
 	
 	/**
 	 * Removes checkout after deleting an application
 	 */
 	public void clean() throws Exception {
 		Logger.info("Removing SCM checkout for %s", pid);
 		
 		try {
 			stop();
 		}
 		catch(Exception e) {
 			// ignore
 		}
 		
 		FileUtils.deleteDirectory(new File("apps/" + pid));
 	}
 
 	/**
 	 * Run the 'play status' command for this application and return its output
 	 */
 	public synchronized String status() throws Exception {
 		return ProcessManager.executeCommand("status-" + pid, ProcessManager
 				.getFullPlayPath()
 				+ " status .", new StringBuffer(), false, new File("apps/"
 				+ pid + "/"), false);
 	}	
 }
