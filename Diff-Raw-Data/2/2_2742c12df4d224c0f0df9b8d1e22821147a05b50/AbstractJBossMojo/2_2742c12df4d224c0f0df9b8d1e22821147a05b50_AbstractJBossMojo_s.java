 package org.codehaus.mojo.jboss;
 
 /*
  * Copyright 2005 Jeff Genender.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * This class provides the general functionality for interacting with a local JBoss server.
  */
 public abstract class AbstractJBossMojo
     extends AbstractMojo
 {
 
     /**
      * The location of JBoss Home. This is a required configuration parameter (unless JBOSS_HOME is set).
      * 
      * @parameter expression="${env.JBOSS_HOME}"
      * @required
      */
     protected String jbossHome;
 
     /**
      * The name of the server profile to use when starting the server. This might be something like "all", "default", or
      * "minimal".
      * 
      * @parameter default-value="default" expression="${jboss.serverName}"
      */
     protected String serverName;
 
     /**
      * Check that JBOSS_HOME is correctly configured.
      * 
      * @throws MojoExecutionException
      */
     protected void checkConfig()
         throws MojoExecutionException
     {
         getLog().debug( "Using JBOSS_HOME: " + jbossHome );
         if ( jbossHome == null || jbossHome.equals( "" ) )
         {
             throw new MojoExecutionException( "Neither JBOSS_HOME nor the jbossHome configuration parameter is set!" );
         }
 
     }
 
     /**
      * Call the JBoss startup or shutdown script.
      * 
      * @param commandName - The name of the command to run
      * @param params - The command line parameters
      * @throws MojoExecutionException
      */
     protected void launch( String commandName, String params )
         throws MojoExecutionException
     {
 
         checkConfig();
         String osName = System.getProperty( "os.name" );
         Runtime runtime = Runtime.getRuntime();
         
         try
         {
             Process proc = null;
             if ( osName.startsWith( "Windows" ) )
             {
                 String command[] =
                    { "cmd.exe", "/C", "cd " + jbossHome + "\\bin & set JBOSS_HOME=\"" + jbossHome + "\" & " + commandName + ".bat " + " " + params };
                 proc = runtime.exec( command );
                 dump( proc.getInputStream() );
                 dump( proc.getErrorStream() );
             }
             else
             {
                 String command[] = 
                     { "sh", "-c", "cd " + jbossHome + "/bin; export JBOSS_HOME=\"" + jbossHome + "\"; ./" + commandName + ".sh " + " " + params };
                 proc = runtime.exec( command );
             }
 
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Unable to execute command: " + e.getMessage(), e );
         }
     }
 
     protected void dump( final InputStream input )
     {
         new Thread( new Runnable()
         {
             public void run()
             {
                 try
                 {
                     byte[] b = new byte[1000];
                     while ( ( input.read( b ) ) != -1 )
                     {
                     }
                 }
                 catch ( IOException e )
                 {
                     e.printStackTrace();
                 }
             }
         } ).start();
     }
 
 }
