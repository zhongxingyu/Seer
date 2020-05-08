 package net.sf.nvn.plugins.mstest;
 
 import static net.sf.nvn.commons.StringUtils.quote;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * A Maven plug-in for testing .NET solutions and/or projects with MSTest.
  * 
  * @goal mstest
  * @phase test
  * @description A Maven plug-in for testing .NET solutions and/or projects with
  *              MSTest.
  * @requiresDependencyResolution
  */
 public class MSTestMojo extends AbstractExeMojo
 {
     /**
      * One or more assemblies that contains tests.
      * 
      * @parameter
      */
     File[] testContainers;
 
     /**
      * One or more files with the extension "vsmdi" that contains test metadata.
      * 
      * @parameter
      */
     File[] testMetaDatas;
 
     /**
      * A list of tests, as specified in the test metadata file, to be run.
      * 
      * @parameter
      */
     String[] testLists;
 
     /**
      * A list of individual tests to run.
      * 
      * @parameter
      */
     String[] tests;
 
     /**
      * Set to true to skip the tests.
      * 
      * @parameter expression="${skipTests}" default-value="false"
      */
     boolean skipTests;
 
     /**
      * Run tests within the MSTest.exe process. This choice improves test run
      * speed but increases risk to the MSTest.exe process.
      * 
      * @parameter default-value="false"
      */
     boolean noIsolation;
 
     /**
      * A run configuration file.
      * 
      * @parameter
      */
     File runConfig;
 
     /**
      * Save the test run results to this file.
      * 
      * @parameter
      */
     File resultsFile;
 
     /**
      * Instructs MSTest to run only a single test whose name matches one of the
      * tests you supply in the tests parameter.
      * 
      * @parameter
      */
     boolean unique;
 
     /**
      * Display additional test case properties, if they exist.
      * 
      * @parameter
      */
     String[] details;
 
     /**
      * Display no startup banner and copyright message.
      * 
      * @parameter default-value="false"
      */
     boolean noLogo;
 
     /**
      * The name of a Team Foundation Server to publish the test results to.
      * 
      * @parameter
      */
     String publish;
 
     /**
      * Specify the results file name to be published. If no results file name is
      * specified, use the file produced by the current run.
      * 
      * @parameter
      */
     File publishResultsFile;
 
     /**
      * Publish test results using this build ID.
      * 
      * @parameter
      */
     String publishBuild;
 
     /**
      * Specify the flavor of the build against which test results should be
      * published.
      * 
      * @parameter
      */
     String flavor;
 
     /**
      * Specify the platform of the build against which test results should be
      * published.
      * 
      * @parameter
      */
     String platform;
 
     /**
      * Specify the name of the team project to which the build belongs.
      * 
      * @parameter
      */
     String teamProject;
 
     @Override
     void preExecute() throws MojoExecutionException
     {
         if (!forwardToTestProjects())
         {
             initTestContainer();
         }
     }
 
     boolean forwardToTestProjects() throws MojoExecutionException
     {
         if (super.isHierarchicalSolution())
         {
             return false;
         }
 
         if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
         {
             return false;
         }
 
         List<File> tcList = new ArrayList<File>();
 
         for (MavenProject mp : getModules())
         {
             if (!mp.getPackaging().equalsIgnoreCase("mstest"))
             {
                 continue;
             }
 
             if (getMSBuildModules().containsKey(mp.getArtifactId()))
             {
                 MSBuildProject msbp =
                     getMSBuildModules().get(mp.getArtifactId());
                 File tc =
                     msbp.getBuildArtifact(getActiveBuildConfigurationName());
                 tc =
                     new File(msbp.getProjectFile().getParentFile(), tc
                         .getPath());
                 tcList.add(tc);
             }
         }
 
         if (tcList.size() > 0)
         {
             this.testContainers = tcList.toArray(new File[0]);
         }
 
         return true;
     }
 
     void initTestContainer()
     {
        if (this.testContainers != null && this.testContainers.length > 0)
         {
             return;
         }
 
         if (!this.mavenProject.getPackaging().equals("mstest"))
         {
             return;
         }
 
         if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
         {
             return;
         }
 
         this.testContainers =
             new File[]
             {
                 super.msbuildProject
                     .getBuildArtifact(getActiveBuildConfigurationName())
             };
     }
 
     @Override
     boolean shouldExecute()
     {
         if (this.skipTests)
         {
             info("tests are skipped");
             return false;
         }
 
         if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
         {
             return true;
         }
 
         if (this.testContainers != null && this.testContainers.length > 0)
         {
             return true;
         }
 
         info("no tests found");
         return false;
     }
 
     @Override
     String getArgs(int execution)
     {
         StringBuilder cmdLineBuff = new StringBuilder();
 
         if (this.testContainers != null && this.testContainers.length > 0)
         {
             for (File f : this.testContainers)
             {
                 cmdLineBuff.append("/testcontainer:");
                 cmdLineBuff.append(getPath(f));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
         {
             for (File f : this.testMetaDatas)
             {
                 cmdLineBuff.append("/testmetadata:");
                 cmdLineBuff.append(getPath(f));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.testLists != null && this.testLists.length > 0)
         {
             for (String s : this.testLists)
             {
                 cmdLineBuff.append("/testlist:");
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.noIsolation)
         {
             cmdLineBuff.append("/noisolation");
             cmdLineBuff.append(" ");
         }
 
         if (this.tests != null && this.tests.length > 0)
         {
             for (String s : this.tests)
             {
                 cmdLineBuff.append("/test:");
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.runConfig != null)
         {
             cmdLineBuff.append("/runconfig:");
             cmdLineBuff.append(getPath(this.runConfig));
             cmdLineBuff.append(" ");
         }
 
         if (this.resultsFile != null)
         {
             cmdLineBuff.append("/resultsfile:");
             cmdLineBuff.append(getPath(this.resultsFile));
             cmdLineBuff.append(" ");
         }
 
         if (this.unique)
         {
             cmdLineBuff.append("/unique");
             cmdLineBuff.append(" ");
         }
 
         if (this.details != null && this.details.length > 0)
         {
             for (String s : this.details)
             {
                 cmdLineBuff.append("/detail:");
                 cmdLineBuff.append(quote(s));
                 cmdLineBuff.append(" ");
             }
         }
 
         if (this.noLogo)
         {
             cmdLineBuff.append("/nologo");
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.publish))
         {
             cmdLineBuff.append("/publish:");
             cmdLineBuff.append(quote(this.publish));
             cmdLineBuff.append(" ");
         }
 
         if (this.publishResultsFile != null)
         {
             cmdLineBuff.append("/publishresultsfile:");
             cmdLineBuff.append(getPath(this.publishResultsFile));
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.publishBuild))
         {
             cmdLineBuff.append("/publishbuild:");
             cmdLineBuff.append(quote(this.publishBuild));
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.teamProject))
         {
             cmdLineBuff.append("/teamproject:");
             cmdLineBuff.append(quote(this.teamProject));
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.platform))
         {
             cmdLineBuff.append("/platform:");
             cmdLineBuff.append(quote(this.platform));
             cmdLineBuff.append(" ");
         }
 
         if (StringUtils.isNotEmpty(this.flavor))
         {
             cmdLineBuff.append("/flavor:");
             cmdLineBuff.append(quote(this.flavor));
             cmdLineBuff.append(" ");
         }
 
         String clbs = cmdLineBuff.toString();
 
         return clbs;
     }
 
     @Override
     String getMojoName()
     {
         return "mstest";
     }
 
     @Override
     boolean isProjectTypeValid()
     {
         return isSolution() || isCSProject() || isVBProject();
     }
 
     @Override
     File getDefaultCommand()
     {
         return new File("mstest.exe");
     }
 
     @Override
     void postExecute(MojoExecutionException executionException)
         throws MojoExecutionException
     {
         // Do nothing
     }
 }
