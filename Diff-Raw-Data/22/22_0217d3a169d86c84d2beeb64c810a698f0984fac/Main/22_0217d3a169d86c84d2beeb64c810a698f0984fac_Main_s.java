 import java.io.File;
import java.io.IOException;
 import java.text.MessageFormat;
import java.util.Iterator;
 import java.util.List;

import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.diff.DiffEntry.ChangeType;
 import org.eclipse.jgit.diff.DiffFormatter;
 import org.eclipse.jgit.diff.RawTextComparator;
 import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
 import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
 import org.eclipse.jgit.util.io.DisabledOutputStream;
 
/**
 * Show hot to open an exisiting repository.
 */
 public class Main {
 	
     public static void main(String [] args) {
     	
     	String strPath = "/Users/amatig/Code/git/amati_test/";
     	String strCommit = "3d32c595a740ab70006cd13247377c57bea8eb9a";
     	
 	    File gitWorkDir = new File(strPath);
 	    File gitDir = new File(gitWorkDir, Constants.DOT_GIT);
 	    try {
 	        FileRepositoryBuilder builder = new FileRepositoryBuilder();
 	        
 	        Repository repository = builder.setGitDir(gitDir)
 	        		.readEnvironment() // scan environment GIT_* variables
 	        		.findGitDir() // scan up the file system tree
 	        		.build();
 	        
 	        RevWalk rw = new RevWalk(repository);
 	        
 	        ObjectId head = repository.resolve(Constants.HEAD);
 	        RevCommit commit = rw.parseCommit(head);
 	        RevCommit parent = rw.parseCommit(ObjectId.fromString(strCommit));
 	        
 	        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
 	        df.setRepository(repository);
 	        df.setDiffComparator(RawTextComparator.DEFAULT);
 	        df.setDetectRenames(true);
 	        
 	        List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
 	        for (DiffEntry diff : diffs) {
 	        	String oper = diff.getChangeType().name();
 	        	ChangeType decodeOper = DiffEntry.ChangeType.valueOf(oper);
 	        	String origP = diff.getOldPath();
 	        	String destP = diff.getNewPath();
 	        	
 	        	if (decodeOper == DiffEntry.ChangeType.ADD) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        	} else if (decodeOper == DiffEntry.ChangeType.DELETE) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "RM", origP));
 	        	} else if (decodeOper == DiffEntry.ChangeType.MODIFY) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        	} else if (decodeOper == DiffEntry.ChangeType.RENAME) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "RM", origP));
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        	} else if (decodeOper == DiffEntry.ChangeType.COPY) {
 	        		
 	        	} else {
 	        		
 	        	}
 	        }
 	        
 	    } catch (Exception e) {
 	        
 	    }
 	    
     }
     
 }
 
