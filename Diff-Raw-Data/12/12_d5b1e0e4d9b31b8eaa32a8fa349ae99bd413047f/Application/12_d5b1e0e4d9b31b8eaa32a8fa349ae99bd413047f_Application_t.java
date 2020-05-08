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
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.io.FileUtils;
 
 
 import play.Logger;
 import play.Play.Mode;
 import play.data.validation.Required;
 import play.db.jpa.Model;
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
 	
 	/**
 	 * Start the application
 	 * @param force Force start?
 	 */
 	public void start(boolean force) throws Exception {
 		if(!force && !enabled) {
 			throw new Exception("Can not start disabled application " + pid);
 		}
 		else if(isRunning()) {
 			throw new Exception("Application " + pid + " is already running.");
 		}
 		
 		// generate application.conf
 		ConfigurationManager.generateConfigurationFiles(this);
 		
		
 		ProcessManager.executeProcess(pid + "-start", ProcessManager.getFullPlayPath() + " start .", new File("apps/" + pid + "/"));
 		Logger.info("Started %s", pid);
 	}

	private void resolveDependencies() throws Exception {
		ProcessManager.executeCommand(pid + "-deps", ProcessManager.getFullPlayPath() + " deps --sync .", new File("apps/" + pid + "/"));
	}
 	
 	/**
 	 * Stop the application
 	 */
 	public void stop() throws Exception {
 		ProcessManager.executeProcess(pid + "-stop", ProcessManager.getFullPlayPath() + " stop .", new File("apps/" + pid + "/"));
 		Logger.info("Stopped %s", pid);
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
 
 	public synchronized String status() throws Exception {
 		return ProcessManager.executeCommand("status-" + pid, ProcessManager.getFullPlayPath() + " status .", false, new File("apps/" + pid + "/"));
 	}	
 }
