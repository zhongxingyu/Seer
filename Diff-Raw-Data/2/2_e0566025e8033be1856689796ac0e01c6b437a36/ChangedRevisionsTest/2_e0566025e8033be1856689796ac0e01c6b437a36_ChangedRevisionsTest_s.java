 package jenkins.plugins.svn_revert;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogSet;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
 import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
 import org.mockito.Mock;
 
 import com.google.common.collect.Lists;
 
 @SuppressWarnings("rawtypes")
 public class ChangedRevisionsTest extends AbstractMockitoTestCase {
 
     private final List<EntryImpl> entries = Lists.newLinkedList();
     @Mock
     private AbstractBuild build;
     private final ChangeLogSet changeLogSet = new FakeChangeLogSet(build, entries);
 
     @Test
     public void shouldCalculateChangedRevisions() throws Exception {
         when(build.getChangeSet()).thenReturn(changeLogSet);
         givenChangedRevision(7);
         givenChangedRevision(9);
         givenChangedRevision(3);
 
         final Revisions revisions = new ChangedRevisions().getFor(build);
 
        assertThat(revisions, is(Revisions.create(3, 9)));
     }
 
     private void givenChangedRevision(final int revision) {
         final EntryImpl entry = mock(EntryImpl.class);
         when(entry.getCommitId()).thenReturn(Integer.toString(revision));
         entries.add(entry);
     }
 
 }
