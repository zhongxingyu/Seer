 package uk.ac.ox.oucs.search2.filter;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import uk.ac.ox.oucs.search2.result.SearchResult;
 
 import java.util.Iterator;
 
 /**
  * @author Colin Hebert
  */
 public class FilterChain {
     private final Iterator<SearchFilter> searchFilterIterator;
 
     public FilterChain(Iterable<SearchFilter> searchFilters) {
         searchFilterIterator = searchFilters.iterator();
     }
 
     public SearchResult filter(SearchResult searchResult) {
         if (searchFilterIterator.hasNext())
             return searchFilterIterator.next().filter(searchResult, this);
         else
             return searchResult;
     }
 }
