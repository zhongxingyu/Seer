 /*
  * Copyright (c) 2013, MaestroDev. All rights reserved.
  */
 package com.maestrodev.maestro.plugins.gitblit;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.gitblit.client.GitblitClient;
 import com.gitblit.client.GitblitRegistration;
 import com.gitblit.models.RepositoryModel;
 import com.maestrodev.maestro.plugins.MaestroWorker;
 
 public class GitblitWorker extends MaestroWorker {
 
     private static final Logger logger = Logger.getLogger(GitblitWorker.class.getName());
     
     public GitblitWorker() {
 	
     }
         
     
     /** 
      * Creates a new repository.
      */
     public void createRepository() {
 	GitblitClient client = getGitblitClient();
 	
 	String repositoryName = getField("repository_name");
 	
 	RepositoryModel repository = new RepositoryModel(repositoryName, 
 		getField("description"), 
 		getField("owner"), null);
 	try {
 	    if (!client.createRepository(repository, null)) {
		logger.log(Level.WARNING, "Unable to create repository");
		setError("Unable to create repository");
 	    }
 	} catch (IOException e) {
	    logger.log(Level.WARNING, "Error creating repository", e);
	    setError("Error creating repository: " + e.getMessage());
 	}
	writeOutput(String.format("Created %s repository owned by %s on Gitblit server at %s", 
		repositoryName, getField("owner"), getField("url")));
     }
     
     
     protected GitblitClient getGitblitClient() {
 	GitblitRegistration registration = 
 		new GitblitRegistration(getField("server_name"), 
 			getField("url"), 
 			getField("account"), 
 			getField("password").toCharArray());
 	return new GitblitClient(registration);
     }
     
     
     
     
 }
