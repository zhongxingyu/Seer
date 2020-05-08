 package wikipedia.analysis.drilldown;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.joda.time.DateMidnight;
 
 import util.MapSorter;
 import wikipedia.http.PageHistoryFetcher;
 import wikipedia.http.PageLinkInfoFetcher;
 import wikipedia.http.WikiAPIClient;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public final class RelatedResultsFetcher {
 
     private static final int NBR_WEEKS = 4;
 
     private final String lang;
     private final String searchTerm;
     private final NumberOfRecentEditsFetcher numberOfRecentEditsFetcher;
 
     public RelatedResultsFetcher(final String searchTerm, final String lang) {
         this.searchTerm = searchTerm;
         this.lang = lang;
         numberOfRecentEditsFetcher = new NumberOfRecentEditsFetcher(lang);
     }
 
     public Map<String, Integer> getActivityMap() {
         return getActivityMap(new BasicSearch(lang, searchTerm).executeSearch());
     }
 
     public Map<String, Integer> getActivityMap(final Collection<String> pages) {
         Map<String, Integer> activityResults = Maps.newHashMap();
         int counter = 1;
         System.out.println("------------------------------------------------");
         System.out.println("NEW MAP");
         System.out.println("------------------------------------------------");
         for (String pageTitle : pages) {
             System.out.println(counter++);
             int nbrEdits = numberOfRecentEditsFetcher.getNumberOfEditsInLastWeeks(NBR_WEEKS, pageTitle);
             activityResults.put(pageTitle, nbrEdits);
         }
         return activityResults;
     }
 
     public static void main(final String[] args) {
         final String lang = "en";
         RelatedResultsFetcher fetcher = new RelatedResultsFetcher("Justin Bieber", lang);
         WikiAPIClient wikiAPIClient = new WikiAPIClient(new DefaultHttpClient());
         Set<String> allSeenNodes = Sets.newHashSet();
 
         Map<String, Integer> initialSearchResults = fetcher.getActivityMap();
         allSeenNodes.addAll(initialSearchResults.keySet());
         Set<String> nodesInCurrentIteration = Sets.newHashSet();
 
         //get all outgoing links for topentries
         final Set<String> round1 = getTopEntries(8, initialSearchResults).keySet();
         for (String topEntry : round1) {
             PageLinkInfoFetcher plif = new PageLinkInfoFetcher(topEntry, lang, new DateMidnight(2011, 6, 1).toDateTime(), wikiAPIClient);
             nodesInCurrentIteration.addAll(plif.getLinkInformation().getFilteredLinks());
         }
         allSeenNodes.addAll(nodesInCurrentIteration);
 
 
         final Set<String> round2 = getTopEntries(8 * 2, fetcher.getActivityMap(nodesInCurrentIteration)).keySet();
         nodesInCurrentIteration = Sets.newHashSet();
         for (String topEntry : round2) {
             PageLinkInfoFetcher plif = new PageLinkInfoFetcher(topEntry, lang, new DateMidnight(2011, 6, 1).toDateTime(), wikiAPIClient);
             nodesInCurrentIteration.addAll(plif.getLinkInformation().getFilteredLinks());
         }
         allSeenNodes.addAll(nodesInCurrentIteration);
 
 
 
 //        final Set<String> round3 = getTopEntries(8 * 3, fetcher.getActivityMap(nodesInCurrentIteration)).keySet();
 //        nodesInCurrentIteration = Sets.newHashSet();
 //        for (String topEntry : round3) {
 //            PageLinkInfoFetcher plif = new PageLinkInfoFetcher(topEntry, lang, new DateMidnight(2011, 6, 1).toDateTime(), wikiAPIClient);
 //            nodesInCurrentIteration.addAll(plif.getLinkInformation().getFilteredLinks());
 //        }
 //        allSeenNodes.addAll(nodesInCurrentIteration);
 
         System.out.println(allSeenNodes.size());
         System.out.println(allSeenNodes);
 
         prepareNodesForNetwork(allSeenNodes);
     }
 
     private static void prepareNodesForNetwork(final Set<String> allSeenNodes) {
         NumberOfRecentEditsFetcher fetcher = new NumberOfRecentEditsFetcher("en"); //FIX ME NON STATIC!
         Map<Integer, String> idsAndPages = Maps.newHashMap();
 
         for (String pageTitle : allSeenNodes) {
            int id = fetcher.getPageID(pageTitle);
            idsAndPages.put(id, pageTitle);
         }
 
         new PageHistoryFetcher(idsAndPages, "en").fetchCompleteCategories();
     }
 
     private static Map<String, Integer> getTopEntries(final int nbrResults,
                                       final Map<String, Integer> results) {
         LinkedHashMap<String, Integer> sortetedResults = Maps.newLinkedHashMap(new MapSorter<String, Integer>().sortByValue(results));
         LinkedHashMap<String, Integer> filteredResults = Maps.newLinkedHashMap();
         List<String> sortedKeys = Lists.newArrayList(sortetedResults.keySet());
         for (int i = 0; i < nbrResults; i++) {
             final String currentKey = sortedKeys.get(i);
             filteredResults.put(currentKey, sortetedResults.get(currentKey));
         }
         return filteredResults;
     }
 }
