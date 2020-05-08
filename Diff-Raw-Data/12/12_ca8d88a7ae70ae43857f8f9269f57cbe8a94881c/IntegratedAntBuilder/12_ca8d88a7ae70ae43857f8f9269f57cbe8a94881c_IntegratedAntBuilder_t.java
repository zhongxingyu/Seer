 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.hibu.atek.tordf.building;
 
 import antmanager.IlabLogger;
 import java.io.File;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 
 /**
  *Reads log.xml from project directory
  * @author Tord
  */
 public class IntegratedAntBuilder implements AntBuilder
 {
     Project project;
     private File baseDir;
     File buildFile;
     int builderid = 1;
     String dAdress;
 
     public IntegratedAntBuilder(){init();}
     public IntegratedAntBuilder(File baseDir,String addr) {
         this.baseDir = baseDir;
         //this.builderid = builderid;
         this.dAdress = addr;
         setBaseDir(baseDir);
         init();
     }
 
     public void init()
     {        
         project = new Project();
         //File projectDir = DefaultProjectDirectoryProvider.GetDefaultProvider().ProjectDirectory();
          //"C:\\Users\\Tord\\Documents\\NetBeansProjects\\AntManager\\"+);
         
 
 
         //LogFileProvider lfp =  DefaultLogfileProvider.GetDefaultProvider();
         //File projectDir = DefaultProjectDirectoryProvider.GetDefaultProvider().ProjectDirectory();
         //File f = lfp.GetLogfile();
         //baseDir = projectDir;
         //buildFile = new File(baseDir,"build.xml");
         
         
         DefaultLogger logger = new DefaultLogger();
         logger.setMessageOutputLevel(Project.MSG_INFO);
         logger.setErrorPrintStream(System.err);
         logger.setOutputPrintStream(System.out);
         project.addBuildListener(logger);
         project.addBuildListener(new IlabLogger());
         project.setProperty("ant.file", buildFile.getAbsolutePath());
        project.addTaskDefinition("if", ise.antelope.tasks.IfTask.class);
         project.setProperty("item", "ant");
         // fix this
         //project.setProperty("JAVA_HOME", "C:\\Program Files\\Java\\");
         project.setProperty("inputdir", "src/items/ant");
         project.setProperty("outputdir", "build/items/ant");
         project.setProperty("graphics.prefix", "../../");
         project.setProperty("dAddress",dAdress);
         
         project.setBaseDir(getBaseDir());
         project.init();
         //File buildFile = new File(baseDir,"build.xml");//projectDir.getAbsolutePath() +"/build.xml");
         ProjectHelper.configureProject(project, buildFile);
         //Compile();
       // rest of program goes here
 
     }
 
     public boolean Compile() {
         try {
            //needs javac tool in classpath don't remove
             project.executeTarget("compile");

         } catch (Exception e) {
             System.err.println(e.getMessage());
             return false;
         }
         return true;
         // rest of program goes here
         // rest of program goes here
     }
     public boolean Run() {
         try {
             project.executeTarget("run");
         } catch (Exception e) {
             System.err.println(e.getMessage());
                    return false;
         }
         return true;
         // rest of program goes here
         // rest of program goes here
     }
 
     public boolean Deploy() {
         try {
             project.executeTarget("deploy");
         } catch (Exception e) {
             System.err.println(e.getMessage());
                    return false;
         }
         return true;
     }
 
     /**
      * @return the baseDir
      */
     public File getBaseDir() {
         return baseDir;
     }
 
     /**
      * @param baseDir the baseDir to set
      */
     public void setBaseDir(File baseDir) {
         this.baseDir = baseDir;
         buildFile = new File(getBaseDir(),"build.xml");
     }
 
 }
