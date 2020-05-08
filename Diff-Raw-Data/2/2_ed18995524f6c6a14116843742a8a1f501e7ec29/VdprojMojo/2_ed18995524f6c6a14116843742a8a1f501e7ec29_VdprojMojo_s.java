 package net.sf.nvn.plugins.vdproj;
 
 import static org.apache.commons.exec.util.StringUtils.quoteArgument;
 import java.io.File;
 import java.util.Collection;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * A Maven plug-in for building VisualStudio setup projects.
  * 
  * @author akutz
  * 
  * @goal package
  * @phase package
  * @description A Maven plug-in for building VisualStudio setup projects.
  */
 public class VdprojMojo extends AbstractExeMojo
 {
     /**
      * The build configuration.
      * 
      * @parameter expression="${vdproj.buildConfiguration}"
     *            default-value="debug"
      */
     String buildConfiguration;
 
     /**
      * The vdproj file to build.
      * 
      * @parameter expression="${vdproj.vdprojFile}"
      */
     File vdProjFile;
 
     /**
      * The devenv executable.
      * 
      * @parameter expression="${vdproj.devenv}" default-value="devenv.exe"
      */
     File devEnv;
 
     @Override
     public String buildCommandLineString()
     {
         if (StringUtils.isNotEmpty(super.commandLineArgs))
         {
             String cmd = this.devEnv.getName() + " " + this.commandLineArgs;
             return cmd;
         }
 
         StringBuilder cmdLineBuff = new StringBuilder();
 
         cmdLineBuff.append(quoteArgument(this.devEnv.getName()));
         cmdLineBuff.append(" ");
 
         cmdLineBuff.append("/Build");
         cmdLineBuff.append(" ");
         cmdLineBuff.append(quoteArgument(this.buildConfiguration));
         cmdLineBuff.append(" ");
 
         cmdLineBuff.append(quoteArgument(this.vdProjFile.getName()));
 
         String clbs = cmdLineBuff.toString();
         return clbs;
     }
 
     @Override
     public String getMojoName()
     {
         return "vdproj";
     }
 
     @Override
     public void prepareForExecute() throws MojoExecutionException
     {
         loadVdprojFile();
     }
 
     @Override
     public boolean shouldExecute() throws MojoExecutionException
     {
         if (!this.devEnv.exists())
         {
             getLog().error(
                 "nvn-" + getMojoName() + ": could not find "
                     + this.devEnv.getName());
             return false;
         }
 
         if (this.vdProjFile == null)
         {
             getLog().error(
                 "nvn-" + getMojoName() + ": could not find a vdproj file");
             return false;
         }
 
         return true;
     }
 
     public void loadVdprojFile()
     {
         if (this.vdProjFile == null)
         {
             this.vdProjFile = findVdprojFile();
         }
     }
 
     @SuppressWarnings("unchecked")
     public File findVdprojFile()
     {
         Collection vdprojFiles =
             FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
             {
                 "vdproj"
             }, false);
 
         if (vdprojFiles != null && vdprojFiles.size() > 0)
         {
             return (File) vdprojFiles.iterator().next();
         }
         else
         {
             return null;
         }
     }
 }
