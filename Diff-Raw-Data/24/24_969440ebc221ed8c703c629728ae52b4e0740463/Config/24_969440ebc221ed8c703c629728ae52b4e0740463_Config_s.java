 package net.praqma.hudson;
 
 import java.io.PrintStream;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 
 public class Config {
 
     /*public static final int __UNKNOWN_PROMOTE = 99;
     public static final int __NO_PROMOTE = 100;
     public static final int __PROMOTE_STABLE = 101;
     public static final int __PROMOTE_UNSTABLE = 102;
     //Logger*/
     protected static Logger logger = PraqmaLogger.getLogger();
 
     private Config() {
     }
 
     public static List<String> getLevels() {
         logger.trace_function();
         List<String> levels = Project.getPromotionLevels();
         return levels;
     }
 
     public static void setContext() {
         boolean useTestbase = false;
         if (useTestbase) {
             /*
              * Examples to use from testbase.xml: stream =
              * "STREAM_TEST1@\PDS_PVOB" component = "COMPONENT_TEST1@\PDS_PVOB"
              * Level to poll = "INITIAL"
              */
             Cool.setContext(UCM.ContextType.XML);
             System.out.println("PUCM is running on a testbase");
         } else {
             UCM.setContext(UCM.ContextType.CLEARTOOL);
         }
     }
 
     /*Below method is obsolete - remove when everything works*/
     /**
      * @deprecated
      */
     public static Stream devStream(String pvob) throws ScmException {
         Stream devStream = null;
         try {
             devStream = UCMEntity.getStream("Hudson_Server_dev@" + pvob, false);
         } catch (UCMException e) {
             throw new ScmException("Could not get developer stream. " + e.getMessage());
         }
         return devStream;
     }
 
     public static Stream getIntegrationStream(Baseline bl, String buildProject) throws ScmException {
         Stream stream = null;
         Project project = null;
 
 
         /* If the build project was not given as a parameter to the job, try to find
          * hudson, Hudson, jenkins or Jenkins */
         if (buildProject == null) {
             try {
                project = UCMEntity.getProject("hudson@" + bl.getPvob(), false);
             } catch (UCMException eh) {
                 try {
                    project = UCMEntity.getProject("Hudson@" + bl.getPvob(), false);
                 } catch (UCMException eH) {
                     try {
                        project = UCMEntity.getProject("jenkins@" + bl.getPvob(), false);
                     } catch (UCMException ej) {
                         try {
                            project = UCMEntity.getProject("Jenkins@" + bl.getPvob(), false);
                         } catch (UCMException eJ) {
                             logger.warning("The build Project was not found.");
                             //hudsonOut.println("The build project was not found. You can create the project with: \"cleartool mkproject -c \"Your special build Project\" -in rootFolder@\\your_pvob yourBuildProject@\\your_pvob\"."
                             //        + "\nSince there's no build project in ClearCase, pucm have made an extra stream in your integration stream to build in");
 
                             /* Use the integration stream */
                             try {
                                 project = bl.getStream().getProject();
                             } catch (UCMException ucme) {
                                 throw new ScmException("Could not get the build Project.");
                             }
                         }
                     }
                 }
             }
         } else {
             try {
                project = UCMEntity.getProject(buildProject + "@" + bl.getPvob(), false);
             } catch (Exception e) {
                 //throw new ScmException( "Could not find project 'hudson' in " + pvob + ". You can install the Poject with: \"cleartool mkproject -c \"The special Hudson Project\" -in rootFolder@\\your_pvob hudson@\\your_pvob\"." );
                 logger.warning("The build Project was not found.");
                 //hudsonOut.println("The build project was not found. You can create the project with: \"cleartool mkproject -c \"Your special build Project\" -in rootFolder@\\your_pvob yourBuildProject@\\your_pvob\"."
                 //        + "\nSince there's no build project in ClearCase, pucm have made an extra stream in your integration stream to build in");
 
                 try {
                     project = bl.getStream().getProject();
                 } catch (UCMException ucme) {
                     throw new ScmException("Could not get the Project.");
                 }
             }
         }
 
         try {
             stream = project.getIntegrationStream();
         } catch (Exception e) {
             throw new ScmException("Could not get integration stream from " + project.getShortname());
         }
 
         return stream;
     }
 
     public static String getPvob(Stream stream) {
 
        return "@" + stream.getPvob();
     }
 }
