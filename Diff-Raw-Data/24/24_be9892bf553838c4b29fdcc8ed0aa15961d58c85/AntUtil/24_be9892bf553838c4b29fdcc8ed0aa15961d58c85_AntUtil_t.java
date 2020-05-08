 package util;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
 public class AntUtil {
 
     public static <T extends Task> T getTask(Class<T> clazz, String name) {
         try {
             T task = clazz.newInstance();
 
             Project project = new Project();
             project.init();
             task.setProject(project);
             task.setTaskType(name);
             task.setTaskName(name);
             return task;
 
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 
 }
