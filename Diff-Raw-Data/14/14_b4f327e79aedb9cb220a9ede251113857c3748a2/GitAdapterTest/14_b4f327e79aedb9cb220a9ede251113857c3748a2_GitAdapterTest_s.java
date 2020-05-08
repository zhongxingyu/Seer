 package org.codefirst.mistilteinn.vcs;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CheckoutCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.ListBranchCommand;
 import org.eclipse.jgit.api.LogCommand;
 import org.eclipse.jgit.api.ResetCommand;
 import org.eclipse.jgit.api.ResetCommand.ResetType;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.junit.Test;
 
 public class GitAdapterTest {
 
     @Test
     public void testTicket() throws Exception {
         int ticketId = 100;
 
         Repository mockedRepository = mock(Repository.class);
         GitAdapter gitAdapter = spy(new GitAdapter(mockedRepository));
         Git mockedGit = mock(Git.class);
         doReturn(mockedGit).when(gitAdapter).getGit();
         CheckoutCommand checkoutCommand = mock(CheckoutCommand.class);
         doReturn(checkoutCommand).when(mockedGit).checkout();
 
         doReturn(true).when(gitAdapter).branchExists("id/" + ticketId);
 
         assertThat(gitAdapter.ticket(ticketId), is(true));
 
         verify(checkoutCommand).setName("id/" + ticketId);
         verify(checkoutCommand).call();
     }
 
     @Test
     public void testBranchExists() throws Exception {
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         String branchName = "branch";
 
         Git mockedGit = mock(Git.class);
 
         ListBranchCommand mockedCommand = mock(ListBranchCommand.class);
 
         Ref mockedRef = mock(Ref.class);
         doReturn(branchName).when(mockedRef).getName();
         List<Ref> refs = new ArrayList<Ref>();
         refs.add(mockedRef);
 
         doReturn(mockedGit).when(gitAdapter).getGit();
         doReturn(mockedCommand).when(mockedGit).branchList();
         doReturn(refs).when(mockedCommand).call();
 
         assertThat(gitAdapter.branchExists(branchName), is(true));
     }
 
     @Test
     public void testBranchNotExists() throws Exception {
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         String branchName = "branch";
 
         Git mockedGit = mock(Git.class);
 
         ListBranchCommand mockedCommand = mock(ListBranchCommand.class);
 
         Ref mockedRef = mock(Ref.class);
         doReturn(branchName).when(mockedRef).getName();
         List<Ref> refs = new ArrayList<Ref>();
         refs.add(mockedRef);
 
         doReturn(mockedGit).when(gitAdapter).getGit();
         doReturn(mockedCommand).when(mockedGit).branchList();
         doReturn(refs).when(mockedCommand).call();
 
         assertThat(gitAdapter.branchExists(branchName + "a"), is(false));
     }
 
     @Test
     public void testGetNowMessage() throws Exception {
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         Calendar cal = Calendar.getInstance();
         cal.set(2012, 1, 1, 1, 3, 4);
         doReturn(new Date(cal.getTimeInMillis())).when(gitAdapter).getDate();
 
         String message = gitAdapter.getNowMessage();
         assertThat(message, is("[from now] 2012/02/01 01:03:04"));
     }
 
     @Test
     public void testNow() throws Exception {
         String nowMessage = "now message";
 
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         Git mockedGit = mock(Git.class);
         doReturn(mockedGit).when(gitAdapter).getGit();
         doReturn(nowMessage).when(gitAdapter).getNowMessage();
 
         AddCommand mockedAddCommand = mock(AddCommand.class);
         doReturn(mockedAddCommand).when(mockedGit).add();
         CommitCommand mockedCommitCommand = mock(CommitCommand.class);
         doReturn(mockedCommitCommand).when(mockedGit).commit();
 
         gitAdapter.now();
 
         verify(mockedAddCommand).call();
         verify(mockedCommitCommand).call();
         verify(mockedCommitCommand).setMessage(nowMessage);
     }
 
     @Test
     public void testFixup() throws Exception {
         String commitMessage = "commit message";
 
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         Git mockedGit = mock(Git.class);
         doReturn(mockedGit).when(gitAdapter).getGit();
 
         // mock log command
         RevCommit mockedCommit = mock(RevCommit.class);
         doReturn("[from now] 2011/01/23 01:23:45").when(gitAdapter).getShortMessage(mockedCommit);
         List<RevCommit> commits = new ArrayList<RevCommit>();
         commits.add(mockedCommit);
 
         LogCommand mockedLogCommand = mock(LogCommand.class);
         doReturn(mockedLogCommand).when(mockedGit).log();
         doReturn(commits).when(mockedLogCommand).call();
 
         // mock add command
         AddCommand mockedAddCommand = mock(AddCommand.class);
         doReturn(mockedAddCommand).when(mockedGit).add();
 
         // mock commit command
         CommitCommand mockedCommitCommand = mock(CommitCommand.class);
         doReturn(mockedCommitCommand).when(mockedGit).commit();
 
         gitAdapter.fixup(commitMessage);
 
         verify(mockedGit).add();
        verify(mockedGit).reset();
         verify(mockedGit).log();
         verify(mockedGit).commit();
 
         verify(mockedCommitCommand).setMessage(commitMessage);
     }
 
     @Test
     public void testAddAll() throws Exception {
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         Git mockedGit = mock(Git.class);
         doReturn(mockedGit).when(gitAdapter).getGit();
 
         AddCommand mockedAddCommand = mock(AddCommand.class);
         doReturn(mockedAddCommand).when(mockedGit).add();
 
         gitAdapter.addAll();
 
         verify(mockedAddCommand).addFilepattern(".");
         verify(mockedAddCommand).call();
     }
 
     @Test
     public void testResetTo() throws Exception {
         ResetType resetType = ResetType.MIXED;
 
         GitAdapter gitAdapter = spy(new GitAdapter(mock(Repository.class)));
         Git mockedGit = mock(Git.class);
         doReturn(mockedGit).when(gitAdapter).getGit();
 
         // mock reset command
         ResetCommand mockedResetCommand = mock(ResetCommand.class);
         doReturn(mockedResetCommand).when(mockedGit).reset();
 
         RevCommit commit = mock(RevCommit.class);
 
         gitAdapter.resetTo(commit, resetType);
 
         verify(mockedResetCommand).setMode(resetType);
         verify(mockedResetCommand).call();
     }
 }
