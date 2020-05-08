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
 
 package org.codehaus.mojo.jboss;
 
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
      * The server name.
      * 
      * @parameter default-value="default"
      */
     protected String serverName;
 
     protected void checkConfig()
         throws MojoExecutionException
     {
         getLog().debug( "Using JBOSS_HOME: " + jbossHome );
         if ( jbossHome == null || jbossHome.equals( "" ) )
         {
             throw new MojoExecutionException( "Neither JBOSS_HOME nor the jbossHome configuration parameter is set!" );
         }
 
     }
 
     protected void launch( String fName, String params )
         throws MojoExecutionException
     {
 
         try
         {
             checkConfig();
             String osName = System.getProperty( "os.name" );
             Runtime runtime = Runtime.getRuntime();
 
             Process p = null;
             if ( osName.startsWith( "Windows" ) )
             {
                 String command[] = { "cmd.exe", "/C", "cd " + jbossHome + "\\bin & " + fName + ".bat " + " " + params };
                 p = runtime.exec( command );
                 dump( p.getInputStream() );
                 dump( p.getErrorStream() );
             }
             else
             {
                 String command[] = { "sh", "-c", "cd " + jbossHome + "/bin; ./" + fName + ".sh " + " " + params };
                 p = runtime.exec( command );
             }
 
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Mojo error occurred: " + e.getMessage(), e );
         }
     }
 
     protected void dump( final InputStream a_input )
     {
         new Thread( new Runnable()
         {
             public void run()
             {
                 try
                 {
                     byte[] b = new byte[1000];
                     while ( ( a_input.read( b ) ) != -1 )
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
