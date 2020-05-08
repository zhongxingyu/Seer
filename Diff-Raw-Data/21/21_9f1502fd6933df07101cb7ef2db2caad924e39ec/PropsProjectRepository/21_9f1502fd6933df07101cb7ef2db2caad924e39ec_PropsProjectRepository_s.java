 package greenhouse.project;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.common.collect.ImmutableMap;
 
 public class PropsProjectRepository implements ProjectRepository {
 
     private ImmutableMap<String, Project> projects;
 
     public PropsProjectRepository(File projectsDir) {
         File[] directories = projectsDir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File file) {
                 return file.isDirectory();
             }
         });
 
         Map<String, Project> projectsMap = new HashMap<String, Project>();
         for (File root : directories) {
             Project project = Project.load(root);
             projectsMap.put(project.getKey(), project);
         }
        projectsMap = ImmutableMap.copyOf(projectsMap);
     }
 
     @Override
     public ImmutableMap<String, Project> getProjects() {
         return projects;
     }
 
     @Override
     public void add(Project project) {
         project.save();
         projects = ImmutableMap.<String, Project> builder().putAll(projects).put(project.getKey(), project).build();
     }
 
 }
