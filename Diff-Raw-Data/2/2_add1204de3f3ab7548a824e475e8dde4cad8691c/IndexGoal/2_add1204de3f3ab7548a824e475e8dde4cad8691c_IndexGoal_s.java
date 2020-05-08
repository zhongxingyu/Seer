 /*
  * Copyright 2008 Kindleit Technologies. All rights reserved. This file, all
  * proprietary knowledge and algorithms it details are the sole property of
  * Kindleit Technologies unless otherwise specified. The software this file
  * belong with is the confidential and proprietary information of Kindleit
  * Technologies. ("Confidential Information"). You shall not disclose such
  * Confidential Information and shall use it only in accordance with the terms
  * of the license agreement you entered into with Kindleit.
  */
 package net.kindleit.gae;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * @author jpeynado@kindleit.net
  * Goal for run a WAR project on the GAE dev server.
  *
 * @goal undate-indexes
  * @executionStrategy once-per-session
  * @requiresOnline
  *
  */
 public class IndexGoal extends EngineGoalBase {
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().info("Updating Project Indexes...");
     runAppCfg("update_indexes", appDir);
   }
 }
 
 
