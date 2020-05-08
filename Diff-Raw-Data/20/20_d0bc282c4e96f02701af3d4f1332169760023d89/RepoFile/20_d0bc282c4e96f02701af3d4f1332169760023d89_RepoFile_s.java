 package data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.eclipse.jgit.api.BlameCommand;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.blame.BlameResult;
 import org.eclipse.jgit.diff.Edit;
 import org.eclipse.jgit.diff.EditList;
 import org.eclipse.jgit.diff.HistogramDiff;
 import org.eclipse.jgit.diff.RawText;
 import org.eclipse.jgit.diff.RawTextComparator;
 import org.eclipse.jgit.errors.AmbiguousObjectException;
 import org.eclipse.jgit.errors.IncorrectObjectTypeException;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.errors.RevisionSyntaxException;
 import org.eclipse.jgit.internal.storage.file.FileRepository;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.PersonIdent;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
 import org.eclipse.jgit.treewalk.filter.PathFilter;
 import org.eclipse.jgit.treewalk.filter.TreeFilter;
 
 public class RepoFile implements Iterable<Line> {
 
 	private String fileName;
 	private String commitId;
 
 	// change to whatever data structure is most suitable
 	private ArrayList<Line> lines;
 
 	/*
 	 * @param name is a the files relative path
 	 * @param commitId must be a commit within this file's history otherwise
 	 * this function will result in an error.
 	 */
 	public RepoFile(FileRepository repo, String name, String commitId)
 			throws GitAPIException, RevisionSyntaxException,
 			AmbiguousObjectException, IncorrectObjectTypeException, IOException {
 		this.fileName = name;
 		this.commitId = commitId;
 		// create list of line objects
 		this.lines = populateLineInfo(repo);
 	}
 
 	/*
 	 * Populates lines Array.
 	 */
 	private ArrayList<Line> populateLineInfo(FileRepository repo)
 			throws RevisionSyntaxException, MissingObjectException,
 			IncorrectObjectTypeException, AmbiguousObjectException,
 			IOException, GitAPIException {
 		
 		// obtain blame information
 		BlameCommand blamer = new BlameCommand(repo);
 		ObjectId commitID = repo.resolve(commitId);
 		blamer.setStartCommit(commitID);
 		blamer.setFilePath(fileName);
 		BlameResult blame = blamer.call();
 		RawText result = blame.getResultContents();
 		
 		// getting number of changes information
 		RevWalk revWalk = new RevWalk(repo);
 		RevCommit root = revWalk.parseCommit(commitID);
 		revWalk.markStart(root);
 		revWalk.setTreeFilter(AndTreeFilter.create(PathFilter.create(fileName),
 				TreeFilter.ANY_DIFF));
 
 		ArrayList<Integer> lineChanges = new ArrayList<Integer>(result.size());
 		HashMap<Integer, String> changesLog = new HashMap<Integer, String>();
 		
 		for (int i = 0; i < result.size(); ++i) {
 			lineChanges.add(0);
 		}
 		
 		for (RevCommit currentCommit : revWalk) {
 			RawText text = getText(blamer, currentCommit);
 			//old new
 			EditList diffList = getEditList(text, result);
 			
 			for (Edit edits : diffList) {
 				int start = edits.getBeginB();
 				int end = edits.getEndB();
 				int startOld = edits.getBeginA();
 				int endOld = edits.getEndA();
 				String changesStr = "";
				
				for(int i = startOld; i <= endOld; ++i){
 					changesStr = changesStr + text.getString(i) + "\n";
 				}
 				
 				if (changesLog.containsKey(start)) {
 					if (!changesLog.get(start).equals(changesStr)) {
 						changesLog.put(start, changesStr);
						for (int i = start; i <= end; ++i) {
 							lineChanges.set(i, lineChanges.get(i) + 1);
 						}
 					}
 				} else {
 					changesLog.put(start, changesStr);
					for (int i = start; i <= end; ++i) {
 						lineChanges.set(i, lineChanges.get(i) + 1);
 					}
 				}
 			}
 		}
 		
 		// populate list
 		ArrayList<Line> lines = new ArrayList<Line>(result.size());
 
 		for (int i = 0; i < result.size(); ++i) {
 			RevCommit currentCommit = blame.getSourceCommit(i);
 			PersonIdent currentAuthor = blame.getSourceAuthor(i);
 			// pre check if can make a line object
 			if (pre_check_line(currentCommit, currentAuthor,
 					result.getString(i))) {
 				// if can make a line make it
 				lines.add(new Line(currentCommit.getName(), currentAuthor
 						.getName(), i, result.getString(i), lineChanges.get(i)));
 			} else
 				return lines;// System.out.println("can make a line");// we
 								// handle it later
 		}
 		return lines;
 	}
 	
 	private EditList getEditList(RawText textOld, RawText textNew)
 			throws GitAPIException, IOException {
 		EditList diffList = new EditList();
 		diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT,
 				textOld, textNew));
 		return diffList;
 	}
 
 	private RawText getText(BlameCommand blamer, RevCommit commit) throws GitAPIException {
 		// text for older elements
 		blamer.setFilePath(fileName);
 		blamer.setStartCommit(commit);
 		BlameResult result = blamer.call();
 		return result.getResultContents();
 	}
 
 	private boolean pre_check_line(RevCommit currentCommit,
 			PersonIdent currentAuthor, String string) {
 		if (currentCommit == null || currentAuthor == null || string == null) {
 			return false;
 		} else
 			return true;
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * returns number of lines in file
 	 */
 	public int size() {
 		return lines.size();
 	}
 
 	@Override
 	public Iterator<Line> iterator() {
 		return lines.iterator();
 	}
 
 	/*
 	 * returns file name of files
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 
 	/*
 	 * return commitId given
 	 */
 	public String getCommitId() {
 		return commitId;
 	}
 
 }
