 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import java.io.File;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 import org.gradle.jarjar.com.google.common.collect.Lists;
 import org.gradle.tooling.BuildLauncher;
 import org.gradle.tooling.GradleConnectionException;
 import org.gradle.tooling.GradleConnector;
 import org.gradle.tooling.ProjectConnection;
 import org.gradle.tooling.ResultHandler;
 import org.jboss.forge.furnace.util.Strings;
 
 /**
  * @author Adam Wyłuda
  */
 public class GradleManagerImpl implements GradleManager
 {
    private static class ResultHolder
    {
       private volatile boolean result;
    }
 
    @Override
    public boolean runGradleBuild(String directory, String task, String profile, String... arguments)
    {
       String gradleHome = System.getenv("GRADLE_HOME");
 
       GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(new File(directory));
       if (!Strings.isNullOrEmpty(gradleHome))
       {
         connector = connector.useGradleUserHomeDir(new File(gradleHome));
       }
       
       ProjectConnection connection = connector.connect();
 
       BuildLauncher launcher = connection.newBuild().forTasks(task);
 
       List<String> argList = Lists.newArrayList(arguments);
 
       if (!Strings.isNullOrEmpty(profile))
       {
          argList.add("-Pprofile=" + profile);
       }
 
       launcher = launcher.withArguments(argList.toArray(new String[argList.size()]));
 
       final ResultHolder holder = new ResultHolder();
       final CountDownLatch latch = new CountDownLatch(1);
 
       launcher.run(new ResultHandler<Object>()
       {
          @Override
          public void onComplete(Object result)
          {
             holder.result = true;
             latch.countDown();
          }
 
          @Override
          public void onFailure(GradleConnectionException failure)
          {
             holder.result = false;
             latch.countDown();
          }
       });
 
       try
       {
          latch.await();
       }
       catch (InterruptedException e)
       {
          e.printStackTrace();
       }
 
       return holder.result;
    }
 }
