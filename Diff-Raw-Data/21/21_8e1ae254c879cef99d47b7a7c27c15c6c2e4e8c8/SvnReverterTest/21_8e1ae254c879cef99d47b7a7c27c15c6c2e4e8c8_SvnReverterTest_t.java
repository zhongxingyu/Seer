 package jenkins.plugins.svn_revert;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.mockito.Mock;
 
 @SuppressWarnings("rawtypes")
@Ignore
 public class SvnReverterTest extends AbstractMockitoTestCase {
 
     private SvnReverter reverter;
 
     @Mock
     private Messenger messenger;
     @Mock
     private AbstractBuild build;
     @Mock
     private AbstractBuild rootBuild;
 
     @Mock
     private AbstractProject rootproject;
 
     @Mock
     private AbstractProject project;
 
     @Before
     public void setup() {
         when(build.getRootBuild()).thenReturn(rootBuild);
         when(build.getProject()).thenReturn(project);
         when(project.getRootProject()).thenReturn(rootproject);
         reverter = new SvnReverter(build, messenger);
     }
 
     @Test
     public void shouldLogIfRepoIsNotSubversion() throws Exception {
         reverter.revert();
         verify(messenger).informNotSubversionSCM();
     }
 
 }
