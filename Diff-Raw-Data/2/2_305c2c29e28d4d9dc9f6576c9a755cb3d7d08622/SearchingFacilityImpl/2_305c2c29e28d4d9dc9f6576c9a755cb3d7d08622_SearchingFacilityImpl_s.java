 package net.cyklotron.cms.search.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.store.Directory;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.security.Subject;
 
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.IndexingFacility;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchingFacility;
 
 /**
  * Implementation of Search Service
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: SearchingFacilityImpl.java,v 1.6 2005-05-30 07:36:44 rafal Exp $
  */
 public class SearchingFacilityImpl implements SearchingFacility
 {
     /** logging facility */
     private Logger log;
 
     /** system anonymous subject */
     private IndexingFacility indexingFacility;
 
     // local //////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Creates the facility.
      * @param log
      */
     public SearchingFacilityImpl(
         Logger log,
         IndexingFacility indexingFacility)
     {
         this.log = log;
         this.indexingFacility = indexingFacility;
     }
 
     @Override
     public IndexSearcher getSearcher(PoolResource[] pools, Subject subject)
         throws SearchException
     {
         List<IndexResource> indexes = new ArrayList<>(pools.length * 8);
         for (int i = 0; i < pools.length; i++)
         {
             indexes.addAll(pools[i].getIndexes());
         }
         if (indexes.size() == 0)
         {
             log.warn("No indexes for searching defined for the chosen pool list");
         }
 
         return getSearcher(indexes, subject);
     }
 
     @Override
     public void returnSearcher(IndexSearcher searcher)
     {
         try
         {
            searcher.close();
         }
         catch(IOException e)
         {
             log.error("failed to close searcher", e);
         }
     }
     
     @Override
     public void clearSearcher(IndexResource index)
     {
         // searcher pooling was removed     
     }
     
     // implementation /////////////////////////////////////////////////////////////////////////////
 
 
     private IndexSearcher getSearcher(List indexes, Subject subject)
         throws SearchException
     {
         boolean useOnlyPublic = (Subject.ANONYMOUS == subject.getId());
         
         List searchers = new ArrayList(indexes.size());
         for (int i = 0; i < indexes.size(); i++)
         {
             IndexResource index = (IndexResource) (indexes.get(i));
             if(!useOnlyPublic || (useOnlyPublic && index.getPublic()))
             {
                 try
                 {
                     searchers.add(getSearcher(index));
                 }
                 catch (IOException e)
                 {
                     // fail but go on trying to search on correct searchers
                     log.warn("Error getting searcher for index '"+index.getPath()+"'", e);
                 }
             }
         }
 
         if(searchers.size() == 0)
         {
             return new NullSearcher();
         }
         
         try
         {
             Searcher[] searchables = new Searcher[searchers.size()];
             searchables = (Searcher[]) (searchers.toArray(searchables));
             Searcher searcher = new MultiSearcher(searchables);
             return searcher;
         }
         catch (IOException e)
         {
             throw new SearchException("Cannot create mulisearcher", e);
         }
     }
 
     private IndexSearcher getSearcher(IndexResource index)
         throws IOException, SearchException
     {
         Directory indexDirectory = indexingFacility.getIndexDirectory(index);
         IndexReader indexReader = IndexReader.open(indexDirectory);
         return new IndexSearcher(indexReader);
     }
 }
 
