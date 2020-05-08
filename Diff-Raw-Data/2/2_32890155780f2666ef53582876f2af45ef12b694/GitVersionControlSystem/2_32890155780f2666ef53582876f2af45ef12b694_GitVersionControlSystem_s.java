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
 
 package scm;
 
 import java.io.File;
 
 import play.Play;
 
 import core.ProcessManager;
 
 /**
  * Implementation for a GIT Version Control System
  */
 public class GitVersionControlSystem implements VersionControlSystem {
 
 	public static String getFullGitPath() {
 		final String path = Play.configuration.getProperty("path.git");
 		// return setting from application.conf or assume command is on the instance's path
 		return path == null || path.isEmpty() ? "git" : path;
 	}
 	
 	@Override
 	public String checkout(final String pid, final String gitUrl) throws Exception {
 		final String checkoutPid = "git-clone-" + pid;
 		return ProcessManager.executeCommand(checkoutPid, getFullGitPath()
 				+ " clone " + gitUrl + " apps/" + pid, new StringBuffer(), false);
 	}
 	
 	/**
 	 * Fetch remote changes, must ALWAYS be called after cleanup()
 	 */
 	@Override
 	public String update(final String pid) throws Exception {
 		final String checkoutPid = "git-pull-" + pid;
 		final StringBuffer output = new StringBuffer();
 
 		try {
 			ProcessManager.executeCommand(checkoutPid, getFullGitPath()
					+ " pull origin master", output, new File("apps/" + pid), false);
 		}
 		catch(Exception e) {
 			// Git will print to stderr when pull is already up-to-date or on fast-forward merge
 			if(!output.toString().contains("Already up-to-date") && !output.toString().contains("Fast-forward")) {
 				throw e;
 			}
 		}
 		
 		return output.toString();
 	}
 	
 	@Override
 	public String cleanup(final String pid) throws Exception {
 		final String cleanupPid = "git-cleanup-" + pid;
 		final StringBuffer output = new StringBuffer();
 		ProcessManager.executeCommand(cleanupPid, getFullGitPath()
 				+ " --git-dir=apps/" + pid + "/.git --work-tree=apps/" + pid
 				+ " checkout -- conf/application.conf", output, false);
 		ProcessManager.executeCommand(cleanupPid, getFullGitPath() + " gc",
 				output, new File("apps/" + pid), false);
 		return output.toString(); 
 	}
 }
