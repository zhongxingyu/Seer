 /**
  * 
  */
package com.hackathon.ping.git;
 
 import java.io.File;
 
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.lib.Config;
 import org.eclipse.jgit.lib.RepositoryBuilder;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 import org.eclipse.jgit.transport.RemoteConfig;
 
 
 /**
  * @author schristou88
  *
  */
 public class GitRepo {
 	
 	private String url;
 	private File clonedPath;
 	
 	public GitRepo(String url) {
 		this.url = url;
 		
 //		FileRepositoryBuilder builder = new FileRepositoryBuilder();
 		
 		Config repoConfig = new Config();
 //		repoConfig.setString("remote", , "url", url);
 //		RemoteConfig.getAllRemoteConfigs(repoConfig);
 	}
 	
 }
