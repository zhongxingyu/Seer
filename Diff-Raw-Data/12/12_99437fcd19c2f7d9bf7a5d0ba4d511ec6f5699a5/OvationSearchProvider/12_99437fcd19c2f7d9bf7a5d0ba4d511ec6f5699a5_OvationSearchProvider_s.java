 package us.physion.ovation.ui.search;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import org.netbeans.spi.quicksearch.SearchProvider;
 import org.netbeans.spi.quicksearch.SearchRequest;
 import org.netbeans.spi.quicksearch.SearchResponse;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle.Messages;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import us.physion.ovation.DataContext;
 import us.physion.ovation.domain.AnalysisRecord;
 import us.physion.ovation.domain.Epoch;
 import us.physion.ovation.domain.EpochGroup;
 import us.physion.ovation.domain.Experiment;
 import us.physion.ovation.domain.Measurement;
 import us.physion.ovation.domain.OvationEntity;
 import us.physion.ovation.domain.Project;
 import us.physion.ovation.domain.Source;
 import us.physion.ovation.ui.editor.OpenNodeInBrowserAction;
 import us.physion.ovation.ui.interfaces.ConnectionProvider;
 
 @Messages({
     "# {0} - entity url",
     "Search_Open_Entity=Select {0}"
 })
 public class OvationSearchProvider implements SearchProvider {
     private final static Logger log = LoggerFactory.getLogger(OvationSearchProvider.class);
     
     public OvationSearchProvider(){
         //nothing
     }
 
     @Override
     public void evaluate(SearchRequest search, SearchResponse response) {
         String uri = search.getText();
         if (uri == null) {
             return;
         }
         DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
         OvationEntity ent = ctx.getObjectWithURI(uri);
        final List<URI> path = getURIPath(ent);
         //XXX: This is needed because in the views the root node has no URI and is hidden
        path.add(0, null);
         
         //TODO: Use EntityWrapper.inferDisplayName(OvationEntity) somehow.
         response.addResult(new Runnable() {
 
             @Override
             public void run() {
                 new OpenNodeInBrowserAction(path).actionPerformed(null);
             }
         }, Bundle.Search_Open_Entity(ent.getURI()));
     }
 
     private List<URI> getURIPath(OvationEntity ent) {
         if (ent == null) {
             log.debug("Cannot get URI path for null entity");
             return Collections.EMPTY_LIST;
         }
 
         if (ent instanceof AnalysisRecord) {
             AnalysisRecord ar = (AnalysisRecord) ent;
 
             return concatenate(getURIPath(ar.getParent()), ar.getURI());
         } else if (ent instanceof Measurement) {
             Measurement m = (Measurement) ent;
 
             return concatenate(getURIPath(m.getEpoch()), m.getURI());
         } else if (ent instanceof Epoch) {
             Epoch e = (Epoch) ent;
 
             return concatenate(getURIPath(e.getParent()), e.getURI());
         } else if (ent instanceof EpochGroup) {
             EpochGroup e = (EpochGroup) ent;
 
             return concatenate(getURIPath(e.getParent()), e.getURI());
         } else if (ent instanceof Experiment) {
             Experiment e = (Experiment) ent;
 
             //TODO: 1:many!
             return concatenate(getURIPath(firstNonNull(e.getProjects())), e.getURI());
         } else if (ent instanceof Source) {
             Source s = (Source) ent;
 
             //TODO: 1:many!
             return concatenate(getURIPath(firstNonNull(s.getParentSources())), s.getURI());
         } else if (ent instanceof Project) {
             return Collections.singletonList(ent.getURI());
         } else {
             log.warn("Cannot get URI path for unexpected class " + ent.getClass() + " " + ent);
             return Collections.EMPTY_LIST;
         }
     }
 
     private List<URI> concatenate(List<URI> parentPath, URI u) {
         if (parentPath == null || parentPath.isEmpty()) {
             return Collections.singletonList(u);
         } else {
             List<URI> all = new ArrayList<URI>();
             all.addAll(parentPath);
             all.add(u);
 
             return all;
         }
     }
 
     private static <T> T firstNonNull(Iterable<T> seq) {
         Iterator<T> it = seq.iterator();
         while (it.hasNext()) {
             T e = it.next();
             if (e != null) {
                 return e;
             }
         }
         return null;
     }
 }
