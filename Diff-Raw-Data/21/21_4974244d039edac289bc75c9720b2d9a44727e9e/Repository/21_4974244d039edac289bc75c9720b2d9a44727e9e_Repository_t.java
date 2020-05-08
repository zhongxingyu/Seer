 package pp.eclipse.open;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import pp.eclipse.Preferences;
 import pp.eclipse.open.parse.Parser;
 
 public class Repository
 {
     private final IContainer root;
     private final Parser parser;
     private final Map<String, List<Item>> persistent;
     
     private final Map<String, Container> cache = new HashMap<String, Container>();
     
     private final Preferences preferences;
 
     public Repository(IContainer root, Preferences preferences, Parser parser, Map<String, List<Item>> persistent)
     {
         this.root = root;
         this.preferences = preferences;
         this.parser = parser;
         this.persistent = persistent;
     }
 
     public List<Container> list(final IProgressMonitor monitor)
         throws CoreException
     {
         final List<Container> containers = new ArrayList<Container>();
         monitor.beginTask("Searching", cache.size() != 0 ? cache.size() : 1000);
         RepositoryVisitor visitor = new RepositoryVisitor(preferences.skipPattern(), cancelled(monitor), containers, parser, cache, persistent);
 		root.accept(visitor, 0);
         return containers;
     }
 
     private static Callable<Boolean> cancelled(final IProgressMonitor monitor) {
 		return new Callable<Boolean>() {
 			public Boolean call() throws Exception {
 				return monitor.isCanceled();
 			}
 		};
 	}
 
 	public boolean validate(Item item) {
 	    return root.exists(item.path());
     }
 
 	public Item validateOnRestore(Item item) {
 	    Container container = cache.get(item.path().toPortableString());
 	    if (container == null) {
 	        return item;
 	    }
 	    Item bestMatch = null;
 	    for (Item containerItem : container.items) {
 	        if (containerItem.externalid().equals(item.externalid())) {
 	            if (containerItem.line() == item.line()) {
 	                return item;
 	            }
 	            if (bestMatch == null) {
 	                bestMatch = containerItem;
 	            } else if (Math.abs(bestMatch.line() - item.line()) > Math.abs(containerItem.line() - item.line())) {
 	                bestMatch = containerItem;
 	            }
 	        }
 	    }
 	    return bestMatch;
 	}
 }
