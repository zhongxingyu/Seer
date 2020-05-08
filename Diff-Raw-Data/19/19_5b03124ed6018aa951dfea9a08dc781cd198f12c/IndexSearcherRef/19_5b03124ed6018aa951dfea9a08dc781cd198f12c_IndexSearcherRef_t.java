 package org.neo4j.index.lucene;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.lucene.search.IndexSearcher;
 
 class IndexSearcherRef
 {
     private final String key;
     private final IndexSearcher searcher;
     private final AtomicInteger refCount = new AtomicInteger( 0 );
     private boolean isClosed;
     
     /**
      * We need this because we only want to close the reader/searcher if
      * it has been detached... i.e. the {@link LuceneDataSource} no longer
      * has any reference to it, only an iterator out in the client has a ref.
      * And when that client calls close() it should be closed.
      */
     private boolean detached;
     
     public IndexSearcherRef( String key, IndexSearcher searcher )
     {
         this.key = key;
         this.searcher = searcher;
     }
     
     IndexSearcher getSearcher()
     {
         return this.searcher;
     }
     
     String getKey()
     {
         return this.key;
     }
     
     void incRef()
     {
         this.refCount.incrementAndGet();
     }
     
    void dispose() throws IOException
     {
        if ( !this.isClosed )
        {
            this.searcher.close();
            this.searcher.getIndexReader().close();
            this.isClosed = true;
        }
     }
     
     void detachOrClose() throws IOException
     {
         if ( this.refCount.get() == 0 )
         {
             dispose();
         }
         else
         {
             this.detached = true;
         }
     }
     
     boolean close() throws IOException
     {
         if ( this.isClosed || this.refCount.get() == 0 )
         {
             return true;
         }
         
         boolean reallyClosed = false;
         if ( this.refCount.decrementAndGet() <= 0 && this.detached )
         {
             dispose();
             reallyClosed = true;
         }
         return reallyClosed;
     }
     
     boolean closeStrict()
     {
         try
         {
             return close();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
 }
