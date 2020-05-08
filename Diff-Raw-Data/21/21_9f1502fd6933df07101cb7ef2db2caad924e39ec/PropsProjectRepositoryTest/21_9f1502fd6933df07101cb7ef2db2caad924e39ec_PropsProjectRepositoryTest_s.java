 package greenhouse.project;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableMap;
 
 public class PropsProjectRepositoryTest {
 
     @Test
     public void load() {
        File repo = new File("D:\\git-repos\\greenhouse\\src\\test\\resources\\greenhouse\\project\\test.properties");
         PropsProjectRepository repository = new PropsProjectRepository(repo);
         ImmutableMap<String, Project> projects = repository.getProjects();
         assertEquals(1, projects.size());
 
         Project project = projects.get("example");
         assertNotNull(project);
        assertEquals("Example", project.getName());
        assertEquals("D:\\git-repos\\greenhouse\\example", project.getRoot().getAbsolutePath());
     }
 }
