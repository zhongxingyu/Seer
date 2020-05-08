 package linker;
 
 import java.io.File;
 import java.sql.Timestamp;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import models.Commit;
 import models.Issue;
 import models.Item;
 import models.extractor.patch.Patch;
 import models.extractor.sourcecode.CodeRegion;
 
 import comm.ComResources;
 import comm.ComResources.TrackerProject;
 
 import db.Resources;
 import db.SocialDb;
 import db.TechnicalDb;
 import extractor.Extractor;
 
 public abstract class Linker
 {
 	public class SnippetMatch {
 		public float matchPercent;
 		public String fileName;
 		public SnippetMatch(float matchPercent, String fileName)
 		{
 			this.matchPercent = matchPercent;
 			this.fileName = fileName;
 		}
 	}
 	public class LinkedExtraction {
 		public float Confidence;
 		public Commit commit;
 		public LinkedExtraction(float confidence, Commit commit)
 		{
 			this.Confidence = confidence;
 			this.commit = commit;
 		}
 	}
 	
 	protected TrackerProject supportedProject; 
 	protected SocialDb		comDb;
 	protected TechnicalDb	linkerDb;
 	protected Extractor extractor;
 	protected ExecutorService execPool = Executors.newFixedThreadPool(10);
 
 	public Linker(SocialDb comDb, TechnicalDb linkerDb)
 	{
 		this.comDb = comDb;
 		this.linkerDb = linkerDb;
 		this.extractor = new Extractor();
 	}
 	
 	/**
 	 * Entry point for the linker to start running our linking algorithm from items->commits.
 	 */
 	public void Link() {
 		if (ComResources.DEBUG)
 			comDb.deleteLinks();
 	}
 
 	/**
 	 * <p>Links {@link models.Commit} to {@link models.Issue} by parsing commit messages and <br>
 	 * finding the bug numbers.</p>
 	 */
 	public void LinkFromCommitMessages() { }
 	
 	/**
 	 * <p>Links {@link models.Item} to {@link models.Commit} by parsing through the data
 	 * <br> in the Item's body and Summary to give {@link models.Extraction} objects that
 	 * <br> can be linked to Commits.</p>
 	 */
 	public void LinkFromItems() 
 	{
 		
 		List<Item> items = comDb.getAllItems();
 		
 		Set<Item> itemSet = new HashSet<Item>();
 		int itemCount = 0;
 		Set<Future<?>> tasks = new HashSet<Future<?>>();
 		for(final Item i : items)
 		{
 			if (itemCount >= 1000)
 			{
 				tasks.add(runWorker(itemSet));
 				itemCount = 0;
 				itemSet = new HashSet<Item>();
 			}
 			else
 			{
 				itemCount++;
 				itemSet.add(i);
 			}
 		}
 		if (itemCount > 0)
 		{
 			// do remainder
 			tasks.add(runWorker(itemSet));
 		}
 		waitUntilFinished(tasks);
 	}
 	
 	public void waitUntilFinished(Set<Future<?>>tasks) {
 		execPool.shutdown();
 		for (Future<?> f : tasks) {
 		    try
 			{
 				f.get();
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			catch (ExecutionException e)
 			{
 				e.printStackTrace();
 			} 
 		}
 	}
 	
 	public Future<?> runWorker(Set<Item> itemSet)
 	{
 		try {
 			LinkerThreadWorker worker = new LinkerThreadWorker(this);
			worker.initItems(itemSet.toArray(new Item[0]));
 			return execPool.submit(worker);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Set<LinkedExtraction> GetRelevantCommitsByPatch(Patch patch, Timestamp date)
 	{
 		Set<LinkedExtraction> results = new HashSet<LinkedExtraction>();
 		List<Commit> commitsAroundItem = linkerDb.getCommitsAroundDate(date);
 		for (Commit commit : commitsAroundItem)
 		{
 			Set<String> filesChangedAtCommit = linkerDb.getChangesetForCommit(commit.getCommit_id());
 			for (String fileName : filesChangedAtCommit)
 			{
 				if (fileName.contains(File.separator))
 					fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);
 				if (fileName.equals(patch.getModifiedFile()))
 				{
 					results.add(new LinkedExtraction(ComResources.PATCH_MATCH_PERCENT, commit));
 					Resources.log("Match found %s: %s", commit.getComment(), patch.getModifiedFile());
 				}
 			}
 		}
 		return results;
 	}
 	
 	public Set<LinkedExtraction> GetRelevantCommitsByCodeRegion(CodeRegion region, Timestamp date)
 	{
 		Set<LinkedExtraction> results = new HashSet<LinkedExtraction>();
 		List<Commit> commitsAroundItem = linkerDb.getCommitsAroundDate(date);
 		
 		for (Commit commit : commitsAroundItem)
 		{
 			SnippetMatch snippetMatch = findSnippetFile(region.getText(), commit.getCommit_date(), linkerDb.getChangesetForCommit(commit.getCommit_id()), commit, false);
 			if (snippetMatch == null)
 				return results;
 			else
 			{
 				
 				results.add(new LinkedExtraction(snippetMatch.matchPercent, commit));
 				Resources.log("Match found %s: %s", commit.getComment(), snippetMatch.fileName);
 			}
 		}
 		return results;
 	}
 	
 	/**
 	 * Returns a list of commits and their confidence that are linked to a given extraction by 
 	 * the files given from that extraction.  Uses algorithm (matchCount / totalChangedFiles) to get confidence
 	 * @param filenames
 	 * @param date
 	 * @return
 	 */
 	public Set<LinkedExtraction> GetRelevantCommitsForFiles(List<String> filenames, Timestamp date)
 	{
 		Set<LinkedExtraction> results = new HashSet<LinkedExtraction>();
 		List<Commit> commitsAroundItem = linkerDb.getCommitsAroundDate(date);
 		for (Commit commit : commitsAroundItem)
 		{
 			Set<String> filesChangedAtCommit = linkerDb.getChangesetForCommit(commit.getCommit_id());
 			Set<String> strippedFiles = stripFullFilePaths(filesChangedAtCommit);
 			
 			float matchCount = 0.0f;
 			for (String fileInExtraction: filenames)
 			{
 				if (strippedFiles.contains(fileInExtraction))
 					matchCount++;
 			}
 			if (matchCount > 0)
 			{
 				results.add(new LinkedExtraction((float)(matchCount / (float)filesChangedAtCommit.size()), commit));
 				Resources.log("Match found %s: %f", commit.getComment(), (matchCount / filesChangedAtCommit.size()));
 			}
 		}
 		return results;
 	}
 	
 	/**
 	 * <p>Links {@link models.Item} to {@link models.Issue} by parsing the date of the item<br>
 	 * and the date of commits linked to the Item's thread.  Then after finding a 'correct'<br>
 	 * commit, the item->commit link get's placed in the {@link db.TechnicalDb}.  This algorithm <br>
 	 * is only performed on the items that are children of threads.<p>
 	 */
 	public void LinkFromIssueThreadItems() { 
 		int offset = 0;
 		List<Issue> issues = comDb.getIssues(ComResources.DB_LIMIT, offset);
 		
 		// Batch selecting
 		for(;;) {
 			for(Issue issue: issues) {
 				System.out.println("Linking items for issue: " + issue.getIssueNum());
 				List<String> commitIDs = comDb.getCommitIDForIssue(issue);
 				List<Item> items = comDb.getItemsInThread(issue.getItemID());
 				if(commitIDs.isEmpty() || items.isEmpty())
 					continue;
 				for(Item item: items) {
 					for(String commitID: commitIDs) {
 						Commit commit = linkerDb.getCommit(commitID);
 						if(commit == null)
 							continue;
 						if(item.getItemDate().before(commit.getCommit_date())) {
 							comDb.insertLink(new models.Link(item.getItemId(), commit.getCommit_id(), 1.0f));
 						}
 					}
 				}
 			}
 			
 			if(issues.size() < ComResources.DB_LIMIT)
 				break;
 			else {
 				offset += ComResources.DB_LIMIT;
 				issues = comDb.getIssues(ComResources.DB_LIMIT, offset);
 			}
 		}
 	}
 	
 	public SnippetMatch findSnippetFile(String snippet, Timestamp date, Set<String> files, Commit commit, boolean exactMatchOnly) {
 		// Look for snippet in all the files using exact matches
 		if(exactMatchOnly) {
 			for(String file: files) {
 				String rawFile = linkerDb.getRawFileFromDiffTree(file, commit.getCommit_id(), linkerDb.getCommitPathToRoot(commit.getCommit_id()));
 				if(rawFile.contains(snippet))
 					return new SnippetMatch(1.0f, file);
 			}
 		}
 		// Look for snippet in all the files using substring matching and cut off
 		else {
 			for(String file: files) {
 				String rawFile = linkerDb.getRawFileFromDiffTree(file, commit.getCommit_id(), linkerDb.getCommitPathToRoot(commit.getCommit_id()));
 				int match = longestSubstr(rawFile, snippet);
 				float matchPercent = (float)((float)match/(float)snippet.length());
 				if (matchPercent > ComResources.STRING_MATCHING_THRESHOLD)
 					return new SnippetMatch(matchPercent, file);
 			}
 		}
 		return null;
 	}
 	
 	private Set<String> stripFullFilePaths (Set<String> filePaths)
 	{
 		Set<String> strippedFiles = new HashSet<String>();
 		for (String s : filePaths)
 		{
 			if (s.contains(File.separator))
 				s = s.substring(s.lastIndexOf(File.separator)+1);
 			strippedFiles.add(s);
 		}
 		return strippedFiles;
 	}
 	
 	private int longestSubstr(String first, String second) {
 	    if (first == null || second == null || first.length() == 0 || second.length() == 0)
 	        return 0;
 	 
 	    int maxLen = 0;
 	    int fl = first.length();
 	    int sl = second.length();
 	    int[][] table = new int[fl][sl];
 	 
 	    for (int i = 0; i < fl; i++) {
 	        for (int j = 0; j < sl; j++) {
 	            if (first.charAt(i) == second.charAt(j)) {
 	                if (i == 0 || j == 0) {
 	                    table[i][j] = 1;
 	                }
 	                else {
 	                    table[i][j] = table[i - 1][j - 1] + 1;
 	                }
 	                if (table[i][j] > maxLen) {
 	                    maxLen = table[i][j];
 	                }
 	            }
 	        }
 	    }
 	    return maxLen;
 	}
 }
