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
 
 /**
  * This is the main worker class of the maestro gitblit plugin. It is used to
  * create new repositories on a Gitblit server.
  * 
  * @author Etienne Pelletier <epelletier@maestrodev.com>
  *
  */
 public class GitblitWorker extends MaestroWorker {
 
     private static final Logger logger = Logger.getLogger(GitblitWorker.class.getName());
     
     /**
      * Default constructor.
      */
     public GitblitWorker() {
 	
     }        
     
     /** 
      * Creates a new repository.
      */
     public void createRepository() {
 	GitblitClient client = getGitblitClient();
 	
 	String repositoryName = getField("repository_name");
 	String messageSuffix = String.format(" %s repository owned by %s on Gitblit server at %s", 
 		repositoryName, getField("owner"), getField("url"));
 	
 	
 	RepositoryModel repository = new RepositoryModel(repositoryName, 
 		getField("description"), 
 		getField("owner"), null);
 	try {
 	    if (!client.createRepository(repository, null)) {
 		logger.log(Level.WARNING, "Unable to create" + messageSuffix);
 		setError("Unable to create" + messageSuffix);
 	    }
 	} catch (IOException e) {
 	    logger.log(Level.WARNING, "Error creating" + messageSuffix, e);
	    setError("Error creating" + e.getMessage());
 	}
 	writeOutput("Created" + messageSuffix);
     }
     
     /**
      * Instantiates a new GitblitClient from parameters passed in the work item
      * fields.
      * 
      * @return a GitblitClient instance.
      */
     protected GitblitClient getGitblitClient() {
 	GitblitRegistration registration = 
 		new GitblitRegistration("", 
 			getField("url"), 
 			getField("account"), 
 			getField("password").toCharArray());
 	return new GitblitClient(registration);
     }
     
     
     
     
 }
