 package org.neo4j.index.lucene;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.neo4j.commons.iterator.PrefetchingIterator;
 
 class DocToIdIterator extends PrefetchingIterator<Long>
 {
     private final Iterator<Document> docs;
     private final Collection<Long> exclude;
     private final IndexSearcherRef searcherOrNull;
     private final Set<Long> alreadyReturnedIds = new HashSet<Long>();
     
     DocToIdIterator( Iterator<Document> docs, Collection<Long> exclude,
         IndexSearcherRef searcherOrNull )
     {
         this.docs = docs;
         this.exclude = exclude;
         this.searcherOrNull = searcherOrNull;
     }
 
     @Override
     protected Long fetchNextOrNull()
     {
         Long result = null;
         while ( result == null )
         {
             if ( !docs.hasNext() )
             {
                 endReached();
                 break;
             }
             Document doc = docs.next();
             long id = Long.parseLong(
                 doc.getField( LuceneIndexService.DOC_ID_KEY ).stringValue() );
            if ( !exclude.contains( id ) )
             {
                 if ( alreadyReturnedIds.add( id ) )
                 {
                     result = id;
                 }
             }
         }
         return result;
     }
     
     private void endReached()
     {
         if ( this.searcherOrNull != null )
         {
             this.searcherOrNull.closeStrict();
         }
     }
 
     public int size()
     {
         return ( ( HitsIterator ) this.docs ).size();
     }
 }
