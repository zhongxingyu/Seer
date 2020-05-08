 /*******************************************************************************
  * Copyright (c) 2010, Schley Andrew Kutz All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * - Neither the name of the Schley Andrew Kutz nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 
 package net.sf.nvn.plugin;
 
 import static net.sf.nvn.commons.StringUtils.quote;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import net.sf.nvn.commons.DependencyUtils;
 import net.sf.nvn.commons.dotnet.PlatformType;
 import net.sf.nvn.commons.dotnet.v35.msbuild.BuildConfiguration;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * A MOJO for building .NET solutions and/or projects with MSBuild.
  * 
  * @goal msbuild
  * @phase compile
  * @description A MOJO for building .NET solutions and/or projects with MSBuild.
  * @requiresDependencyResolution
  */
 public class MSBuildMojo extends AbstractExeMojo
 {
     /**
      * The path to the solution or project to build.
      * 
      * @parameter
      */
     File buildFile;
 
     /**
      * A list of additional directories to resolve your project's references
      * against.
      */
     @SuppressWarnings("unchecked")
     List referencePaths;
 
     /**
      * Hides the startup banner and copyright message.
      * 
      * @parameter default-value="false"
      */
     boolean noLogo;
 
     /**
      * Inserts command line settings from a text file. For more information see
      * <a href="http://msdn.microsoft.com/en-us/library/ms404301.aspx">MSBuild
      * Response Files</a>.
      * 
      * @parameter
      */
     File[] commandFiles;
 
     /**
      * Does not auto-include the MSBuild.rsp file.
      * 
      * @parameter default-value="false"
      */
     boolean noAutoResponse;
 
     /**
      * Builds these targets in this project.
      * 
      * @parameter
      */
     String[] targets;
 
     /**
      * Sets or overrides these project-level properties, where name is the
      * property name and value is the property value.
      * 
      * @parameter
      */
     Properties properties;
 
     /**
      * <p>
      * Specifies the logger to use to log events from MSBuild.
      * </p>
      * 
      * <p>
      * The logger syntax is:
      * <em>[LoggerClass,]LoggerAssembly[;LoggerParameters]</em>.
      * </p>
      * 
      * <p>
      * The LoggerClass syntax is:
      * <em>[PartialOrFullNamespace.]LoggerClassName</em>.
      * </p>
      * <p>
      * You do not have to specify the logger class if there is exactly one
      * logger in the assembly.
      * </p>
      * <p>
      * The LoggerAssembly syntax is:
      * <em>{AssemblyName[,StrongName] | AssemblyFile}</em>.
      * </p>
      * <p>
      * Logger parameters are optional and are passed to the logger exactly as
      * you type them. For example:
      * <em>XMLLogger,C:\Loggers\MyLogger.dll;OutputAsHTML</em>
      * </p>
      * 
      * @parameter
      */
     String[] loggers;
 
     /**
      * <p>
      * Use this logger to log events from MSBuild.
      * </p>
      * <p>
      * The &lt;logger&gt; syntax is:
      * <em>[&lt;logger class&gt;,]<logger assembly>[;&lt;logger
      * parameters&gt;]</em>.
      * </p>
      * <p>
      * The &lt;logger class&gt; syntax is: <em>[&lt;partial or full
      * namespace&gt;.]&lt;logger class name&gt;</em>
      * </p>
      * <p>
      * The &lt;logger assembly&gt; syntax is:
      * <em>{&lt;assembly name&gt;[,&lt;strong name&gt;] |
      * &lt;assembly file&gt;}</em>
      * </p>
      * <p>
      * The &lt;logger parameters&gt; are optional, and are passed to the logger
      * exactly as you typed them. Examples:
      * <ul>
      * <li><em>XMLLogger,MyLogger,Version=1.0.2,Culture=neutral</em></li>
      * <li><em>MyLogger,C:\My.dll*ForwardingLogger,C:\Logger.dll</em></li>
      * </ul>
      * </p>
      * 
      * @parameter
      */
     String[] distributedLoggers;
 
     /**
      * Specifies the parameters to pass to the console logger. The available
      * parameters are as follows:
      * 
      * <ul>
      * <li><strong>PerformanceSummary</strong>: Displays the time spent in
      * tasks, targets, and projects.</li>
      * <li><strong>NoSummary</strong>: Hides the error and warning summary
      * displayed at the end of a build.</li>
      * <li><strong>NoItemAndPropertyList</strong>: Hides the list of items and
      * properties displayed at the start of each project build in diagnostic
      * verbosity.</li>
      * </ul>
      * 
      * @parameter
      */
     String consoleLoggerParameters;
 
     /**
      * Displays this amount of information in the build log. Individual loggers
      * display events based upon the verbosity level. A logger can also be
      * configured to ignore the verbosity setting. The available verbosity
      * levels are:
      * 
      * <ul>
      * <li><strong>q[uiet]</strong></li>
      * <li><strong>m[inimal]</strong></li>
      * <li><strong>n[ormal]</strong></li>
      * <li><strong>d[etailed]</strong></li>
      * <li><strong>diag[nostic]</strong></li>
      * </ul>
      * 
      * @parameter
      */
     String verbosity;
 
     /**
      * Disables the default console logger and does not log events to the
      * console.
      * 
      * @parameter default-value="false"
      */
     boolean noConsoleLogger;
 
     /**
      * Validates the project file against the default schema.
      * 
      * @parameter default-value="false"
      */
     boolean validate;
 
     /**
      * Validates the project file against the specified schema.
      * 
      * @parameter
      */
     File schema;
 
     /**
      * Specifies the number of worker processes that are involved in the build.
      * 
      * @parameter
      */
     Integer maxCpuCount;
 
     /**
      * List of extensions to ignore when the project file to build is being
      * determined.
      * 
      * @parameter
      */
     String[] ignoreProjectExtensions;
 
     /**
      * Logs the build output to a single file ("msbuild.log") in the current
      * directory. The location of the file and other parameters for the
      * fileLogger can be specified through the addition of the
      * <em>fileLoggerParameters</em> parameter.
      * 
      * @parameter
      */
     boolean fileLogger;
 
     /**
      * Logs the build output to multiple log files, one log file per MSBuild
      * node. The initial location for these files is the current directory. By
      * default the files are called <em>MSBuild&lt;nodeid&gt;.log</em>. The
      * location of the files and other parameters for the fileLogger can be
      * specified with the addition of the <em>fileLoggerParameters</em>
      * parameter. If a log file name is set through the fileLoggerParameters
      * switch the distributed logger will use the fileName as a template and
      * append the node id to this fileName to create a log file for each node.
      * 
      * @parameter
      */
     boolean distributedFileLogger;
 
     /**
      * Specifies the parameters for the file logger and distributed file logger.
      * The available parameters are:
      * 
      * <ul>
      * <li><strong>LogFile</strong>: The path to the log file into which the
      * build log is written. The distributed file logger uses this as a prefix
      * for its log file names.</li>
      * <li><strong>Append</strong>: Determines if the build log is appended to
      * or overwrite the log file. When you set the switch, the build log is
      * appended to the log file. When you do not set the switch, the contents of
      * an existing log file are overwritten. The default is not to append to the
      * log file.</li>
      * <li><strong>Verbosity</strong>: Overrides the default verbosity setting
      * of detailed.</li>
      * <li><strong>Encoding</strong>: Specifies the encoding for the file, for
      * example, <em>UTF-8</em>.</li>
      * </ul>
      * 
      * @parameter
      */
     String[] fileLoggerParameters;
 
     /**
      * <p>
      * Specifies the version of the Toolset to use to build the project. This
      * command lets you build a project by using a version different from that
      * specified in the <a
      * href="http://msdn.microsoft.com/en-us/library/bcxfsh87.aspx">Project
      * Element (MSBuild)</a>. Valid values for version are as follows:
      * </p>
      * 
      * <ul>
      * <li><strong>2.0</strong></li>
      * <li><strong>3.0</strong></li>
      * <li><strong>3.5</strong></li>
      * </ul>
      * 
      * <p>
      * For more information about Toolsets, see <a
      * href="http://msdn.microsoft.com/en-us/library/bb397456.aspx">Building for
      * Specific .NET Frameworks.</a>
      * </p>
      * 
      * @parameter
      */
     String toolsVersion;
 
     /**
      * Enables or Disables the re-use of MSBuild nodes. If true then the nodes
      * remain after the build completes and are reused by subsequent builds,
      * otherwise the nodes do not remain after the build completes.
      * 
      * @parameter
      */
     boolean nodeReuse;
 
     @Override
     boolean shouldExecute()
     {
         return true;
     }
 
     @Override
     void preExecute() throws MojoExecutionException
     {
         loadBuildFile();
 
         initBuildProperties();
 
         initReferencePathProperty();
 
         initTargets();
     }
 
     @Override
     String getArgs(int execution)
     {
         StringBuilder cmdLineBuff = new StringBuilder();
 
         if (this.noLogo)
         {
             cmdLineBuff.append("/nologo");
             cmdLineBuff.append(" ");
         }
 
         if (this.commandFiles != null && this.commandFiles.length != 0)
         {
             for (File f : this.commandFiles)
             {
                 cmdLineBuff.append("@");
                 cmdLineBuff.append(getPath(f));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.noAutoResponse)
         {
             cmdLineBuff.append("/noautoresponse");
         }
 
         cmdLineBuff.append("/target:");
 
         for (String s : this.targets)
         {
             cmdLineBuff.append(quote(s));
             cmdLineBuff.append(";");
         }
 
         cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
         cmdLineBuff.append(" ");
 
        cmdLineBuff.append("/property:");

         for (Object k : this.properties.keySet())
         {
             cmdLineBuff.append(quote(String.valueOf(k)));
             cmdLineBuff.append("=");
             String s = String.valueOf(this.properties.get(k));
             cmdLineBuff.append(quote(s));
            cmdLineBuff.append(";");
         }
 
         cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
         cmdLineBuff.append(" ");
 
         if (this.loggers != null && this.loggers.length != 0)
         {
             for (String s : this.loggers)
             {
                 cmdLineBuff.append("/logger:");
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.distributedLoggers != null
             && this.distributedLoggers.length != 0)
         {
             for (String s : this.distributedLoggers)
             {
                 cmdLineBuff.append("/distributedlogger:");
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (!StringUtils.isEmpty(this.consoleLoggerParameters))
         {
             cmdLineBuff.append("/consoleloggerparameters:");
             cmdLineBuff.append(this.consoleLoggerParameters);
             cmdLineBuff.append(" ");
         }
 
         if (!StringUtils.isEmpty(this.verbosity))
         {
             cmdLineBuff.append("/verbosity:");
             cmdLineBuff.append(this.verbosity);
             cmdLineBuff.append(" ");
         }
 
         if (this.noConsoleLogger)
         {
             cmdLineBuff.append("/noconsolelogger");
             cmdLineBuff.append(" ");
         }
 
         if (this.validate && this.schema == null)
         {
             cmdLineBuff.append("/validate");
             cmdLineBuff.append(" ");
         }
         else if (this.schema != null)
         {
             cmdLineBuff.append("/validate:");
             cmdLineBuff.append(quote(this.schema.getPath()));
             cmdLineBuff.append(" ");
         }
 
         if (this.ignoreProjectExtensions != null
             && this.ignoreProjectExtensions.length != 0)
         {
             cmdLineBuff.append("/ignoreprojectextensions:");
 
             for (String s : this.ignoreProjectExtensions)
             {
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(";");
             }
 
             cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
             cmdLineBuff.append(" ");
         }
 
         if (this.fileLogger)
         {
             cmdLineBuff.append("/fileLogger");
             cmdLineBuff.append(" ");
         }
 
         if (this.distributedFileLogger)
         {
             cmdLineBuff.append("/distributedFileLogger");
             cmdLineBuff.append(" ");
         }
 
         if (this.fileLoggerParameters != null
             && this.fileLoggerParameters.length != 0)
         {
             cmdLineBuff.append("/fileloggerparameters:");
 
             for (String s : this.fileLoggerParameters)
             {
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(";");
             }
 
             cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.toolsVersion))
         {
             cmdLineBuff.append("/toolsversion:");
             cmdLineBuff.append(quote(this.toolsVersion));
             cmdLineBuff.append(" ");
         }
 
         if (this.nodeReuse)
         {
             cmdLineBuff.append("/nodeReuse:true");
             cmdLineBuff.append(" ");
         }
         else
         {
             cmdLineBuff.append("/nodeReuse:false");
             cmdLineBuff.append(" ");
         }
 
         cmdLineBuff.append(getPath(this.buildFile));
 
         String clbs = cmdLineBuff.toString();
 
         return clbs;
     }
 
     /**
      * Finds the build file (either a sln, csproj, or vbproj file).
      * 
      * @return The build file.
      * @throws MojoExecutionException When an error occurs.
      */
     @SuppressWarnings("unchecked")
     File findBuildFile() throws MojoExecutionException
     {
         Collection slnFiles =
             FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
             {
                 "sln"
             }, false);
 
         if (slnFiles != null && slnFiles.size() > 0)
         {
             return (File) slnFiles.iterator().next();
         }
 
         Collection projFiles =
             FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
             {
                 "csproj", "vbproj"
             }, false);
 
         if (projFiles != null && projFiles.size() > 0)
         {
             return (File) projFiles.iterator().next();
         }
 
         throw new MojoExecutionException(
             "Error finding solution or project file");
     }
 
     /**
      * Initialize the build's target.
      */
     void initTargets()
     {
         if (this.targets != null && this.targets.length > 0)
         {
             return;
         }
 
         this.targets = new String[]
         {
             "Build"
         };
     }
 
     /**
      * Initialize this build's properties.
      */
     void initBuildProperties()
     {
         if (this.properties == null)
         {
             this.properties = new Properties();
         }
 
         BuildConfiguration abc = getBuildConfig();
         PlatformType abp = getBuildPlatform();
 
         String config = "Debug";
         String platform = "Any CPU";
         File outputPath = new File("bin\\Debug");
 
         if (abc != null)
         {
             config = abc.getName();
             outputPath = abc.getOutputPath();
         }
 
         if (abp != null)
         {
             platform = getBuildPlatform().toString();
             if (platform.equals("AnyCPU"))
             {
                 platform = "Any CPU";
             }
         }
 
         if (!this.properties.containsKey("Configuration"))
         {
             this.properties.put("Configuration", config);
         }
 
         if (!this.properties.containsKey("Platform"))
         {
 
             this.properties.put("Platform", platform);
         }
 
         this.properties.put("OutputPath", getPath(outputPath));
     }
 
     /**
      * Initializes the reference paths from the given project's dependencies.
      * 
      * @param project The maven project.
      * @throws MojoExecutionException
      */
     @SuppressWarnings("unchecked")
     void initReferencePaths(MavenProject project) throws MojoExecutionException
     {
         List deps = project.getDependencies();
 
         if (deps == null)
         {
             debug("not processing project dependencies because they're null: "
                 + project.getName());
             return;
         }
 
         if (deps.size() == 0)
         {
             debug("not processing project dependencies because they're zero length: "
                 + project.getName());
             return;
         }
 
         if (this.referencePaths == null)
         {
             this.referencePaths = new ArrayList();
         }
 
         for (Object od : deps)
         {
             Dependency d = (Dependency) od;
 
             if (StringUtils.isEmpty(d.getType()))
             {
                 continue;
             }
 
             if (!d.getType().matches("dll|exe"))
             {
                 continue;
             }
 
             File file =
                 DependencyUtils.getArtifactFile(
                     super.factory,
                     super.localRepository,
                     d);
 
             if (file == null)
             {
                 continue;
             }
 
             if (!this.referencePaths.contains(file.getParentFile()))
             {
                 this.referencePaths.add(file.getParentFile());
 
                 String assemblyName =
                     DependencyUtils.getAssemblyName(
                         super.factory,
                         super.localRepository,
                         super.mavenProject.getRemoteArtifactRepositories(),
                         super.resolver,
                         d);
 
                 DependencyUtils.copyToAssemblyNamedFiles(file, assemblyName);
             }
         }
     }
 
     /**
      * Initializes the reference paths from this project and any parent or child
      * projects.
      * 
      * @throws MojoExecutionException When an error occurs.
      */
     void initReferencePaths() throws MojoExecutionException
     {
         initReferencePaths(super.mavenProject);
 
         // if (super.mavenProject.hasParent())
         // {
         // initReferencePaths(super.mavenProject.getParent());
         // }
     }
 
     /**
      * Initializes the ReferencePath property for msbuild.
      * 
      * @throws MojoExecutionException When an error occurs.
      */
     void initReferencePathProperty() throws MojoExecutionException
     {
         initReferencePaths();
 
         String rp = "";
 
         if (this.properties != null)
         {
             if (this.properties.containsKey("ReferencePath"))
             {
                 rp = this.properties.getProperty("ReferencePath");
             }
         }
 
         String[] rps = rp.split("\\,|\\;");
 
         StringBuilder rpsb = new StringBuilder();
 
         if (rps.length > 0)
         {
             for (String s : rps)
             {
                 rpsb.append(s);
 
                 if (s != rps[rps.length - 1])
                 {
                    rpsb.append(":");
                 }
             }
         }
 
         if (this.referencePaths != null)
         {
             for (Object of : this.referencePaths)
             {
                 File f = (File) of;
 
                 String fp = getPath(f, false);
 
                 debug("added reference path " + fp);
 
                 rpsb.append(fp);
 
                 if (of != this.referencePaths
                     .get(this.referencePaths.size() - 1))
                 {
                    rpsb.append(":");
                 }
             }
         }
 
         if (rpsb.length() > 0)
         {
             String newRps = rpsb.toString();
             this.properties.put("ReferencePath", quote(newRps));
         }
     }
 
     /**
      * Load the build file to use.
      * 
      * @throws MojoExecutionException When an error occurs.
      */
     void loadBuildFile() throws MojoExecutionException
     {
         if (this.buildFile == null)
         {
             this.buildFile = findBuildFile();
         }
     }
 
     @Override
     String getMojoName()
     {
         return "msbuild";
     }
 
     @Override
     boolean isProjectTypeValid()
     {
         return isCSProject() || isVBProject();
     }
 
     @Override
     File getDefaultCommand()
     {
         return new File("msbuild.exe");
     }
 
     @Override
     void postExecute(MojoExecutionException executionException)
         throws MojoExecutionException
     {
         // Do nothing
     }
 }
