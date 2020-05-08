 package com.joelj.jenkins.claimblame;
 
 import com.google.common.collect.ImmutableSet;
 import com.thoughtworks.xstream.XStream;
 import hudson.XmlFile;
 import hudson.model.*;
 import hudson.model.listeners.SaveableListener;
 import hudson.util.XStream2;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Stores the claimed tests data on the filesystem in ${JENKINS_HOME}/claimblame/${JOB_NAME}/config.xml
  * TODO: Break out an abstract base class so that it's easier to create other extension points
  * User: joeljohnson
  * Date: 4/11/12
  * Time: 7:56 PM
  */
 public class FileSystemBlamer implements Blamer, Saveable {
 	private static final XStream XSTREAM = new XStream2();
 	private static final Logger LOGGER = Logger.getLogger(FileSystemBlamer.class.getName());
 	private transient boolean loaded = false;
 
 	private transient boolean inTransaction = false;
 
 	private String jobName;
 	public Map<String, Assignment> culprits;
 
 	@SuppressWarnings("UnusedDeclaration") //Called via reflection
 	FileSystemBlamer() {
 		this.culprits = new HashMap<String, Assignment>();
 	}
 
 	public void setJobName(String jobName) {
 		this.jobName = jobName;
 	}
 
 	@Override
 	public void setCulprit(String testName, User user) {
         if (user != null) {
             getCulprits().put(testName, new Assignment(user.getId()));
         } else {
             getCulprits().remove(testName);
         }
         try {
             save();
         } catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Failed to save.", e);
         }
     }
 
 	@Override
 	public User getCulprit(String testName) {
 		if(!loaded) {
 			load();
 		}
 		if(culprits.containsKey(testName)) {
 			return User.get(culprits.get(testName).getUserId(), false);
 		} else {
 			return null;
 		}
 	}
 
     @Override
 	public Set<String> getTests() {
         return getCulprits().keySet();
     }
 
 	@Override
 	public void setStatus(String testName, Status status) {
         if (getCulprits().containsKey(testName)) {
 			Assignment assignment = culprits.get(testName);
 			assignment.setStatus(status);
 			try {
 				save();
 			} catch (IOException e) {
 				LOGGER.log(Level.SEVERE, "Failed to save.", e);
 			}
 		}
 	}
 
 	@Override
 	public Status getStatus(String testName) {
 		if(!loaded) {
 			load();
 		}
 		if(culprits.containsKey(testName)) {
 			return culprits.get(testName).getStatus();
 		} else {
 			return Status.Unassigned;
 		}
 	}
 
 	@Override
 	public synchronized void save() throws IOException {
 		if(!inTransaction) {
 			write();
 		}
 	}
 
	private void write() throws IOException {
 		getConfigFile().write(this);
 		SaveableListener.fireOnChange(this, getConfigFile());
 	}
 
 	@Override
 	public void startTransaction() {
 		inTransaction = true;
 	}
 
 	@Override
 	public void endTransaction() throws IOException {
 		inTransaction = false;
 		write();
 	}
 
 	public static Set<String> getTrackedJobs() {
 		File rootDir = getRootDir();
 		File[] files = rootDir.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File file) {
 				return file.isDirectory();
 			}
 		});
 
 		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
 		if(files != null) {
 			for (File file : files) {
 				builder.add(file.getName());
 			}
 		}
 		return builder.build();
 	}
 
 	public synchronized void load() {
 		XmlFile config = getConfigFile();
 		if (config.exists()) {
 			try {
 				config.unmarshal(this);
 			} catch (IOException e) {
 				LOGGER.log(Level.SEVERE, "Failed to load " + config, e);
 			}
 		} else {
 			try {
 				save();
 			} catch (IOException e) {
 				LOGGER.log(Level.SEVERE, "Failed to load " + config, e);
 			}
 		}
 		loaded = true;
 	}
 
 	protected final XmlFile getConfigFile() {
 		return new XmlFile(XSTREAM, new File(getRootDir(), jobName + "/config.xml"));
 	}
 
 	protected static File getRootDir() {
 		return new File(Hudson.getInstance().getRootDir(), "claimBlame");
     }
 
     public Map<String, Assignment> getCulprits() {
         return culprits;
     }
 }
