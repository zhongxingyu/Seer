 /*
  */
 package ar.com.sourcerain.labs.repo.git;
 
 import ar.com.sourcerain.labs.repo.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 import org.eclipse.jgit.api.DiffCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.*;
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.errors.NoWorkTreeException;
 import org.eclipse.jgit.errors.UnmergedPathException;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.treewalk.CanonicalTreeParser;
 /**
  *
  * @author Sebastian Javier Marchano sebasjm@sourcerain.com.ar
  */
 public class GitRepository implements Repository {
 
     private Git git;
     
     public GitRepository(Git git) {
         this.git = git;
     }
 
     @Override
     public void addAll() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addFile(File file) {
         try {
             git.add().addFilepattern( file.getName() ).call();
 //            dirty_status = true;
         } catch (NoFilepatternException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void commit(String message) {
         try {
             git.commit().setMessage(message).call();
         } catch (NoHeadException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoMessageException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (UnmergedPathException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ConcurrentRefUpdateException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JGitInternalException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (WrongRepositoryStateException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void stash() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Branch branch() {
         try {
                 return new GitBranch( git.getRepository().getBranch() );
         } catch (IOException ex) {
                 Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     @Override
     public Iterator<? extends Branch> branches() {
         return new MutableIterator( 
 				git.branchList().call().iterator(), 
 				GitBranch.class, 
 				new Class[] { Ref.class }, 
 				new Object[] {} );
     }
 
     @Override
     public Branch newBranch(String name) {
         try {
             return new GitBranch(git.branchCreate().setName(name).call());
         } catch (JGitInternalException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefAlreadyExistsException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefNotFoundException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvalidRefNameException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     @Override
     public void checkout(Branch branch) {
         try {
             git.checkout().setName(branch.name()).call();
         } catch (JGitInternalException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefAlreadyExistsException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefNotFoundException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvalidRefNameException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void checkout(Revision rev) {
         try {
             git.checkout()
                 .setName( branch().name() )
                 .setStartPoint( rev.getId() )
                 .call();
         } catch (JGitInternalException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefAlreadyExistsException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefNotFoundException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvalidRefNameException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void push(Repository repo) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void pull(Repository repo) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private Status status;
     @Override
     public Status status() {
             try {
                 status = new GitStatus(git.status().call());
             } catch (IOException ex) {
                 Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
             } catch (NoWorkTreeException ex) {
                 Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
             }
         return status;
     }
 
     @Override
     public void init() {
         try {
             FileUtils.deleteDirectory( home() );
             git.getRepository().create(false);
         } catch (IOException ex) {
             Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public File home() {
 		return git.getRepository().getDirectory().getParentFile();
     }
 
     @Override
     public Iterator<? extends Revision> lastLogs(int n) {
         Exception e;
         try {
             return new MutableIterator( 
 					git.log().call().iterator(), 
 					GitRevision.class, 
 					new Class[] {  RevCommit.class, org.eclipse.jgit.lib.Repository.class }, 
 					new Object[] { git.getRepository() } );
         } catch (NoHeadException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JGitInternalException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         throw new RuntimeException("git exception",e);
     }
 
     @Override
     public Iterator<? extends Revision> lastLogsFromFile(int n, String file) {
         Exception e;
         try {
             return new MutableIterator( 
 					git.log().addPath(file).call().iterator(), 
 					GitRevision.class, 
 					new Class[] { RevCommit.class }, 
 					new Object[] {} );
         } catch (NoHeadException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JGitInternalException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         throw new RuntimeException("git exception",e);
     }
 
     @Override
     public Iterator<? extends Diff> diff(Revision older, Revision newer) {
         Exception e;
         try {
             CanonicalTreeParser parser_old = older == null ? null : new CanonicalTreeParser( 
                     null,
                     git.getRepository().newObjectReader(), 
                     ((GitRevision)older).getGitTree() 
                 );
             
             CanonicalTreeParser parser_new = newer == null ? null : new CanonicalTreeParser( 
                     null,
                     git.getRepository().newObjectReader(), 
                     ((GitRevision)newer).getGitTree()   
                 );
             
             DiffCommand diff = git.diff()
                     .setOldTree( parser_old )
                     .setNewTree( parser_new );
             
             return new MutableIterator( 
                     diff.call().iterator(), 
                     DiffEntry.class, 
                    new Class[]{ GitDiff.class },
                    new Object[] {}
                 );
         } catch (GitAPIException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             e = ex; Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         throw new RuntimeException("git exception",e);
     }
 
     @Override
     public Iterator<? extends Diff> diff(Revision older) {
         return diff(null,null);
     }
 
     @Override
     public Iterator<? extends Diff> diff() {
         return diff(null);
     }
 
     @Override
     public void rebase() {
         Exception e;
         try {
             git.rebase(). call();
             return;
         } catch (NoHeadException ex) {
             e = ex;Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RefNotFoundException ex) {
             e = ex;Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JGitInternalException ex) {
             e = ex;Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         } catch (GitAPIException ex) {
             e = ex;Logger.getLogger(GitRepository.class.getName()).log(Level.SEVERE, null, ex);
         }
         throw new RuntimeException("git exception",e);
     }
 
 }
