 package org.apache.maven.jxr.util;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Location;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.ExecTask;
 import org.apache.tools.ant.taskdefs.GUnzip;
 import org.apache.tools.ant.taskdefs.Get;
 import org.apache.tools.ant.taskdefs.Untar;
 
 /**
  * <a href="http://ant.apache.org/">Ant</a> task for <a href="http://www.graphviz.org/">Graphviz</a> program.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public class DotTask
     extends Task
 {
     /** Windows Graphviz download URL **/
     public static final String GRAPHVIZ_WINDOWS_URL = "http://www.graphviz.org/pub/graphviz/ARCHIVE/graphviz-win-2.14.1.bin.tar.gz";
 
     private static final String TARGZ = GRAPHVIZ_WINDOWS_URL.substring( GRAPHVIZ_WINDOWS_URL.lastIndexOf( '/' ) + 1 );
 
     private static final String TAR = TARGZ.substring( 0, TARGZ.lastIndexOf( '.' ) );
 
     private static final String GRAPHVIZ_DIR = "graphviz-2.14.1"; // inside the tar.gz
 
    private static final String DEFAULT_OUTPUT_FORMAT = "svg";
 
     /** The dot executable. */
     private File dotExe;
 
     /** The input file. */
     private File in;
 
     /** The output file. */
     private File out;
 
     /** The dest dir. */
     private File destDir;
 
     /** The output format. */
     private String format = DEFAULT_OUTPUT_FORMAT;
 
     /**
      * Set the dot executable.
      *
      * @param dotExe the new dot executable.
      */
     public void setDotExe( File dotExe )
     {
         this.dotExe = dotExe;
     }
 
     /**
      * Set the input file.
      *
      * @param in the new input file.
      */
     public void setIn( File in )
     {
         this.in = in;
     }
 
     /**
      * Set the output file.
      *
      * @param out the new out file.
      */
     public void setOut( File out )
     {
         this.out = out;
     }
 
     /**
      * Set the output directory.
      *
      * @param dest the new output directory.
      */
     public void setDestDir( File dest )
     {
         this.destDir = dest;
     }
 
     /**
      * Set the wanted format.
      *
      * @param format the new format.
      */
     public void setFormat( String format )
     {
         this.format = format;
     }
 
     /** {@inheritDoc} */
     public String getTaskName()
     {
         return "dot";
     }
 
     /** {@inheritDoc} */
     public String getDescription()
     {
         return "Process Graphviz DOT file.";
     }
 
     /** {@inheritDoc} */
     public void init()
         throws BuildException
     {
         super.init();
     }
 
     /**
      * {@inheritDoc}
      * @throws BuildException if any
      * @throws DotNotPresentInPathBuildException if DOT not present in the path.
      */
     public void execute()
         throws BuildException
     {
         // Input checks
         if ( getIn() == null )
         {
             throw new BuildException( "Missing mandatory attribute 'in'.", getLocation() );
         }
         if ( !getIn().exists() || !getIn().isFile() )
         {
             throw new BuildException( "Input file '" + getIn() + "' not found or not a file.", getLocation() );
         }
 
         if ( getDestDir() == null )
         {
             if ( getOut() == null )
             {
                 throw new BuildException( "Missing mandatory attribute 'out'.", getLocation() );
             }
             if ( getOut().exists() && getOut().isDirectory() )
             {
                 throw new BuildException( "Output file '" + getOut() + "' is a dir.", getLocation() );
             }
         }
         else
         {
             if ( getDestDir().exists() && !getDestDir().isDirectory() )
             {
                 throw new BuildException( "Dest directory is a file.", getLocation() );
             }
             if ( !getDestDir().exists() && !getDestDir().mkdirs() )
             {
                 throw new BuildException( "Cannot create the dest directory.", getLocation() );
             }
         }
 
         // Calling ExecTask
         ExecTask exec = new ExecTask();
         exec.setProject( getProject() );
         exec.setTaskName( "exec" );
         exec.init();
         if ( getDotExe() == null )
         {
             exec.setExecutable( "dot" );
         }
         else
         {
             if ( !getDotExe().exists() || !getDotExe().isFile() )
             {
                 throw new BuildException( "DOT executable '" + getDotExe() + "' not found or not a file.",
                                           getLocation() );
             }
 
             exec.setExecutable( getDotExe().getAbsolutePath() );
         }
         File output;
         if ( getDestDir() == null )
         {
             output = getOut();
         }
         else
         {
            output = new File( getDestDir(), getIn().getName() + "." + format );
         }
         exec.setDir( output.getParentFile() );
         exec.createArg().setLine(
                                   "-T" + getFormat() + " " + "-o" + output.getAbsolutePath() + " "
                                       + getIn().getAbsolutePath() );
 
         exec.setOutputproperty( "exec.output" );
         try
         {
             exec.execute();
         }
         catch ( BuildException e )
         {
             String msg = e.getMessage();
 
             // No DOT executable in path
             if ( msg.indexOf( "Execute failed" ) != -1 )
             {
                 // Unix/Mac OS
                 if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) == -1 )
                 {
                     throw new DotNotPresentInPathBuildException( "Graphviz DOT executable "
                         + "(http://www.graphviz.org/) is not present in your path. "
                         + "Please install it in your path or specify a 'dotExe' parameter.", e );
                 }
 
                 // Windows OS
                 log( "Graphviz DOT executable (http://www.graphviz.org/) is not present in your path. "
                     + "Try to download it...", Project.MSG_WARN );
 
                 String tmpdir = System.getProperty( "java.io.tmpdir" );
                 File tmp = new File( tmpdir, "graphviz" );
                 if ( !tmp.exists() && !tmp.mkdirs() )
                 {
                     throw new BuildException( "Cannot create the temp dir: " + tmp.getAbsolutePath() );
                 }
                 downloadGraphviz( getProject(), tmp );
                 installGraphviz( getProject(), tmp );
                 File dotExeFile = new File( tmp, GRAPHVIZ_DIR + "/bin/dot.exe" );
 
                 // Reexecute the task
                 exec.setExecutable( dotExeFile.getAbsolutePath() );
                 exec.execute();
             }
             else
             {
                 throw e;
             }
         }
 
        // Verify if no error in the output
        if ( getProject().getProperty( "exec.output" ) != null )
         {
            if ( getProject().getProperty( "exec.output" ).indexOf( "Execute failed" ) != -1 )
             {
                throw new BuildException( getProject().getProperty( "exec.output" ), getLocation() );
             }
 
            if ( getProject().getProperty( "exec.output" ).indexOf( "Error:" ) != -1 )
             {
                log( getProject().getProperty( "exec.output" ), Project.MSG_ERR );
                 throw new BuildException( "Error when calling dot.", getLocation() );
             }
         }
     }
 
     // ----------------------------------------------------------------------
     // Private
     // ----------------------------------------------------------------------
 
     /**
      * Getter for the dotExe
      *
      * @return the dotExe
      */
     private File getDotExe()
     {
         return this.dotExe;
     }
 
     /**
      * Getter for the in
      *
      * @return the in
      */
     private File getIn()
     {
         return this.in;
     }
 
     /**
      * Getter for the out
      *
      * @return the out
      */
     private File getOut()
     {
         return this.out;
     }
 
     /**
      * Getter for the dest
      *
      * @return the dest
      */
     private File getDestDir()
     {
         return this.destDir;
     }
 
     /**
      * Getter for the format
      *
      * @return the format
      */
     private String getFormat()
     {
         return this.format;
     }
 
     /**
      * Download in the specified <code>outputDir</code> the Graphviz package.
      *
      * @param antProject not null
      * @param outputDir not null
      * @throws BuildException if any
      * @see #GRAPHVIZ_WINDOWS_URL
      * @TODO add proxy support
      */
     private static void downloadGraphviz( Project antProject, File outputDir )
         throws BuildException
     {
         if ( new File( outputDir, TARGZ ).exists() )
         {
             antProject.log( "The file " + new File( outputDir, TARGZ ) + " was already downloaded. Ignored get.",
                             Project.MSG_DEBUG );
             return;
         }
 
         Get get = new Get();
         get.setProject( antProject );
         get.setTaskName( "get" );
         get.init();
         try
         {
             get.setSrc( new URL( GRAPHVIZ_WINDOWS_URL ) );
         }
         catch ( MalformedURLException e )
         {
             // nop
         }
         get.setDest( new File( outputDir, TARGZ ) );
         get.execute();
     }
 
     /**
      * Install the Graphviz package in the specified <code>outputDir</code>
      *
      * @param antProject not null
      * @param outputDir not null
      * @throws BuildException if any
      */
     private static void installGraphviz( Project antProject, File outputDir )
         throws BuildException
     {
         // Already exist - skip
         if ( new File( outputDir, GRAPHVIZ_DIR ).exists() )
         {
             antProject.log( "The dir " + new File( outputDir, GRAPHVIZ_DIR ) + " already exists. Ignored untar.",
                             Project.MSG_DEBUG );
             return;
         }
 
         File targz = new File( outputDir, TARGZ );
 
         GUnzip gunzip = new GUnzip();
         gunzip.setProject( antProject );
         gunzip.setTaskName( "gunzip" );
         gunzip.init();
         gunzip.setSrc( targz );
         gunzip.execute();
 
         Untar untar = new Untar();
         untar.setProject( antProject );
         untar.setTaskName( "untar" );
         untar.init();
         untar.setSrc( new File( outputDir, TAR ) );
         untar.setDest( outputDir );
         untar.execute();
     }
 
     /**
      * Signals that the dot executable is not present in the path
      */
     public class DotNotPresentInPathBuildException
         extends BuildException
     {
         /**
          * Constructs am exception with no descriptive information.
          */
         public DotNotPresentInPathBuildException()
         {
             super();
         }
 
         /**
          * Constructs an exception with the given descriptive message.
          *
          * @param message
          */
         public DotNotPresentInPathBuildException( String message )
         {
             super( message );
         }
 
         /**
          * Constructs an exception with the given message and exception as
          * a root cause.
          *
          * @param message
          * @param cause
          */
         public DotNotPresentInPathBuildException( String message, Throwable cause )
         {
             super( message, cause );
         }
 
         /**
          * Constructs an exception with the given message and exception as
          * a root cause and a location in a file.
          *
          * @param message
          * @param cause
          * @param location
          */
         public DotNotPresentInPathBuildException( String message, Throwable cause, Location location )
         {
             super( message, cause, location );
         }
     }
 }
