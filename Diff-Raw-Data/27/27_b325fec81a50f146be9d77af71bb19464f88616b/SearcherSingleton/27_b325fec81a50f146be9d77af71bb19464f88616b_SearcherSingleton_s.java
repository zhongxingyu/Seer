 package synchronize.gui;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.pivot.util.concurrent.Task;
 import org.apache.pivot.util.concurrent.TaskListener;
 import org.apache.pivot.wtk.TaskAdapter;
 
 import pdfsearch.IndexFactory;
 import pdfsearch.MMapIndexFactory;
 import pdfsearch.Searcher;
 
 public class SearcherSingleton {
 	private static SearcherSingleton instance;
 	private Searcher searcher;
 	
 	public static void initInstance(SyncWindow w) {
 		instance = new SearcherSingleton(w);
 	}
 	
 	public static SearcherSingleton getInstance() {
 		return instance;
 	}
 	
 	private SearcherSingleton(final SyncWindow window) {
		Path searchPath = FileSystems.getDefault().getPath("res", "pdfs");
 		Path indexPath = FileSystems.getDefault().getPath("res", "index");
 		IndexFactory factory = new MMapIndexFactory(indexPath);
 		
 		searcher = new Searcher(factory,searchPath);
 		// check if index is built - build it if not
 		if(!searcher.indexExists()){
 			System.out.println("Index is not built.");
 			IndexTask indexTask = new IndexTask(searcher,window);
 	        TaskListener<Integer> taskListener = new TaskListener<Integer>() {
 	            @Override
 	            public void taskExecuted(Task<Integer> task) {
	            	if(task.getResult() != 0)
 	            		System.out.println("Index built.");
	            	else
 	            		System.out.println("No files added to index.");

	        		// do first search on init to avoid index searcher caching overhead on user search
	        		try {
	        			searcher.search("piglet");
	        		} catch (IOException | ParseException e) {
	        			e.printStackTrace();
	        		}
 	        		
 	        		window.progress.setEnabled(false);
 	        		window.progress.setVisible(false);
 	            }
 
 	            @Override
 	            public void executeFailed(Task<Integer> task) {
 	                System.err.println(task.getFault());
 	            }
 	        };
 
 	        indexTask.execute(new TaskAdapter<Integer>(taskListener));
 		}
 	}
 	
 	public Searcher getSearcher() {
 		return searcher;
 	}
 }
