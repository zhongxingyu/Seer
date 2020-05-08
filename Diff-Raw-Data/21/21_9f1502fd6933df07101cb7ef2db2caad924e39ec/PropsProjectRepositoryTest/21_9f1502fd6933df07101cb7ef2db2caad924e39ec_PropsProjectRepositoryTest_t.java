 package greenhouse.project;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
import java.net.URL;
 
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableMap;
 
 public class PropsProjectRepositoryTest {
 
     @Test
     public void load() {
        URL resource = PropsProjectRepositoryTest.class.getResource(".");
        String projectsRoot = resource.getPath().toString() + "../../../../demo";

        File repo = new File(projectsRoot);
         PropsProjectRepository repository = new PropsProjectRepository(repo);
         ImmutableMap<String, Project> projects = repository.getProjects();
         assertEquals(1, projects.size());
 
         Project project = projects.get("example");
         assertNotNull(project);
        assertEquals("Example Project", project.getName());
        assertFalse(project.getRoot().getAbsolutePath().equals(""));
     }
 }
