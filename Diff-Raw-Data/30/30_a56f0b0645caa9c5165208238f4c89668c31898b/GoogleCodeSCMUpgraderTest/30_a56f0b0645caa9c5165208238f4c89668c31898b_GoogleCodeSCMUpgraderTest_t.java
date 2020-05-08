 package hudson.plugins.googlecode.scm;
 
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import hudson.matrix.MatrixProject;
 import hudson.model.FreeStyleProject;
 import hudson.model.TopLevelItem;
 import hudson.plugins.googlecode.GoogleCodeProjectProperty;
 import hudson.scm.NullSCM;
 import hudson.scm.SCM;
 import hudson.scm.SubversionSCM.ModuleLocation;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 public class GoogleCodeSCMUpgraderTest {
 
     @Test public void assertNonProjectItemsAreIgnored() {
         TopLevelItem topLevelItem = mock(TopLevelItem.class);
         new GoogleCodeSCMUpgrader().onLoaded(Arrays.asList(topLevelItem));        
         verifyZeroInteractions(topLevelItem);
     }
 
     @Test public void assertProjectWithNullScmIsIgnored() {
         MatrixProject project = mock(MatrixProject.class);
         SCM scm = mock(NullSCM.class);
         when(project.getScm()).thenReturn(scm);
 
         new GoogleCodeSCMUpgrader().onLoaded(Arrays.asList(project, mock(TopLevelItem.class)));
         
         verify(project).getScm();
         verifyNoMoreInteractions(project);
         verifyZeroInteractions(scm);
     }
 
     @Test public void assertProjectWithGoogleCodeScmExIsIgnored() {
         FreeStyleProject project = mock(FreeStyleProject.class);
         SCM scm = mock(GoogleCodeSCMEx.class);
         when(project.getScm()).thenReturn(scm);
 
         new GoogleCodeSCMUpgrader().onLoaded(Arrays.asList(project, mock(TopLevelItem.class)));
 
         verify(project).getScm();   
         verifyNoMoreInteractions(project);
         verifyZeroInteractions(scm);
     }
 
     @Test public void assertProjectsWithGoogleCodeScmIsReplaced() throws IOException {
         MatrixProject project = mock(MatrixProject.class);
         GoogleCodeSCM scm = new GoogleCodeSCM("trunk");
         
         when(project.getScm()).thenReturn(scm);
         when(project.getProperty(GoogleCodeProjectProperty.class)).thenReturn(new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockitopp"));
 
         List<TopLevelItem> items = Arrays.asList(project, mock(TopLevelItem.class));
         new GoogleCodeSCMUpgrader().onLoaded(items);
 
         verify(project).getScm();
         verify(project).getName();
         verify(project).getProperty(GoogleCodeProjectProperty.class);
         verify(project).setScm(isA(GoogleCodeSCMEx.class));
         verify(project).save();
         verifyNoMoreInteractions(project);
     }
 
     @Test public void assertScmIsResetInProjectsWhenProjectSaveFails() throws IOException {
         MatrixProject project = mock(MatrixProject.class);
         GoogleCodeSCM scm = new GoogleCodeSCM("trunk");
         
         when(project.getScm()).thenReturn(scm);
         when(project.getProperty(GoogleCodeProjectProperty.class)).thenReturn(new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockitopp"));
         doThrow(new IOException()).when(project).save();
 
         List<TopLevelItem> items = Arrays.asList(project, mock(TopLevelItem.class));
         new GoogleCodeSCMUpgrader().onLoaded(items);
 
         verify(project).getScm();
         verify(project).getName();
         verify(project).getProperty(GoogleCodeProjectProperty.class);
         verify(project).setScm(isA(GoogleCodeSCMEx.class));
         verify(project).save();
         verify(project).setScm(scm);
         verifyNoMoreInteractions(project);
     }
 
     @Test public void assertScmIsResetInProjectsWhenScmCopyFails() throws IOException {
         MatrixProject project = mock(MatrixProject.class);
         GoogleCodeSCM scm = new GoogleCodeSCM("trunk");
         
         when(project.getScm()).thenReturn(scm);
         when(project.getProperty(GoogleCodeProjectProperty.class)).thenReturn(new GoogleCodeProjectProperty(""));
         doThrow(new IOException()).when(project).save();
 
         List<TopLevelItem> items = Arrays.asList(project, mock(TopLevelItem.class));
         new GoogleCodeSCMUpgrader().onLoaded(items);
 
         verify(project).getScm();
         verify(project).getName();
         verify(project).getProperty(GoogleCodeProjectProperty.class);
         verify(project).setScm(scm);
         verifyNoMoreInteractions(project);
     }
     
     @Test public void assertGoogleCodeScmIsCopiedToGoogleCodeScmEx() {
         GoogleCodeSCM scm = new GoogleCodeSCM("trunk");
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockito");
 
         GoogleCodeSCMEx scmCopy = new GoogleCodeSCMUpgrader().copy(scm, property);
         assertThat(scmCopy.getLocations().length, is(1));
         ModuleLocation location = scmCopy.getLocations()[0];
        assertThat(location.getURL(), is("https://mockito.googlecode.com/svn/trunk"));
         assertThat(location.getLocalDir(), is("."));
     }
 }
