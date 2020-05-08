 package uk.ac.ox.oucs.search2.backwardcompatibility;
 
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import uk.ac.ox.oucs.search2.AbstractIndexingService;
 import uk.ac.ox.oucs.search2.content.Content;
 
 /**
  * @author Colin Hebert
  */
 public class BackIndexingService extends AbstractIndexingService {
     private SearchIndexBuilder searchIndexBuilder;
 
     @Override
     public void indexContent(String eventHandlerName, Iterable<Content> content) {
         throw new UnsupportedOperationException("The previous search service doesn't support manual indexation");
     }
 
     @Override
     public void unindexContent(String eventHandlerName, Iterable<Content> content) {
         throw new UnsupportedOperationException("The previous search service doesn't support manual removal");
     }
 
     @Override
     public void indexSite(String eventHandlerName, Iterable<Content> content, String site) {
         searchIndexBuilder.refreshIndex(site);
     }
 
     @Override
     public void reindexSite(String eventHandlerName, Iterable<Content> content, String site) {
         searchIndexBuilder.rebuildIndex(site);
     }
 
     @Override
    public void unindexSite(String eventHandlerName, String site){
         throw new UnsupportedOperationException("The previous search service doesn't support manual site removal");
     }
 
     @Override
     public void indexAll(String eventHandlerName, Iterable<Content> content) {
         searchIndexBuilder.refreshIndex();
     }
 
     @Override
     public void reindexAll(String eventHandlerName, Iterable<Content> content) {
         searchIndexBuilder.rebuildIndex();
     }
 
     @Override
     public void unindexAll(String eventHandlerName) {
         throw new UnsupportedOperationException("The previous search service doesn't support manual index cleaning removal");
     }
 
     public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
         this.searchIndexBuilder = searchIndexBuilder;
     }
 }
