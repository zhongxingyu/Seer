 package net.sf.nvn.plugins.vdproj;
 
 import java.io.File;
 import junit.framework.Assert;
 import org.apache.maven.project.MavenProject;
 import org.junit.Test;
 
 /**
  * The test class for VdprojMojo.
  * 
  * @author akutz
  * 
  */
 public class VdprojMojoTest
 {
     private VdprojMojo loadMojo() throws Exception
     {
         VdprojMojo mojo = new VdprojMojo();
         mojo.mavenProject = new MavenProject();
         mojo.mavenProject.setBasedir(new File("."));
         mojo.buildConfiguration = "Debug";
         mojo.devEnv = new File("devenv.exe");
         mojo.vdProjFile = new File("MySetupProject.vdproj");
         mojo.timeout = new Long(300000);
         mojo.inheritEnvVars = true;
         return mojo;
     }
 
     @Test
     public void buildCommandLineStringTest() throws Exception
     {
         VdprojMojo mojo = loadMojo();
         mojo.prepareForExecute();
         Assert.assertEquals(
            "devenv.exe /Build Debug /Project MySetupProject MySetupProject.vdproj",
             mojo.buildCommandLineString());
     }
 }
