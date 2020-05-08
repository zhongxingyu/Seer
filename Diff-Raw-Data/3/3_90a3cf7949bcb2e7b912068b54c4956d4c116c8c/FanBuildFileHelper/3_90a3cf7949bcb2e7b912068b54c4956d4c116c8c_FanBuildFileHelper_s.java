 /*
  * Thibaut Colar Jun 16, 2010
  */
 package net.colar.netbeans.fan.project;
 
 import fan.sys.Env;
 import fan.sys.Field;
 import fan.sys.LocalFile;
 import fan.sys.Type;
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.colar.netbeans.fan.FanUtilities;
 import org.netbeans.api.project.Project;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  *
  * @author thibautc
  */
 public class FanBuildFileHelper
 {
 
     public static final String BUILD_FILE = "build.fan";
     public final static Pattern POD_NAME_PATTERN = Pattern.compile("podName\\s*=\\s*\"(\\S+)\"");
 
     public static FileObject getBuildFile(Project prj)
     {
         return prj.getProjectDirectory().getFileObject(BUILD_FILE);
     }
 
     public static FileObject getBuildFileData(Project prj)
     {
         // TODO: cache unless timestamp changed
         return prj.getProjectDirectory().getFileObject(BUILD_FILE);
     }
 
     /**
      * Try to resolve the pod name given a source path
      *
      * Changed from using fantom script parser, because it always fails on partial files.
      * It is very possible for the file to be unparseable (incomplete)
      *
      * @param path
      * @return
      */
     public static String getPodForPath(String path)
     {
         File folder = new File(path).getParentFile();
         File scriptFolder = folder;
         String pod = null;
         while (folder != null)
         {
             if (new File(folder, "build.fan").exists())
             {
                 try
                 {
                     File buildFan = new File(folder, "build.fan");
                     LocalFile fanFile = (LocalFile)(new LocalFile(buildFan).normalize());
                     
                     // Try asking fantom to compile the script and give the name                    
                     try
                     {
                         Type type = Env.cur().compileScript(fanFile).pod().type("Build");
                         if(type != null)
                         {
                             Field field = type.field("podName");
                             if(field!=null)
                             {
                                 String name = field.get(type.make()).toString();
                                 if(name!=null && name.length()>0)
                                     return name;
                             }
                         }
                     }
                     catch(Throwable t) 
                     {
                         //FanUtilities.GENERIC_LOGGER.exception("Failed to parse build script: "+buildFan.getAbsolutePath(), t);
                         // might fail if script is incomplete or otherwise invalid
                     }
                     
                    // TODO: Decided what to do with buildscripts ????
                    
                     String buildText = FileUtil.toFileObject(FileUtil.normalizeFile(buildFan)).asText();
                     Matcher m = POD_NAME_PATTERN.matcher(buildText);
                     if (m.find())
                     {
                         pod = m.group(1);
                         // found it, break out of loop
                         break;
                     } else
                     {
                         // backup plan if that failed
                         pod = folder.getName();
                         //break;  -> no, try see if there is another bbuild.fan higher up.
                     }
                 } catch (IOException e)
                 {
                     e.printStackTrace();
                 }
             }
             folder = folder.getParentFile();
         }
         if (pod == null)
         {
             FanUtilities.GENERIC_LOGGER.error("Could not find pod for: " + path);
             // Must be a script, make-up a "pod" from folder name .. should probably normalize it
             return "_SCRIPT_" + scriptFolder.getName();
         }
         return pod;
     }
 }
