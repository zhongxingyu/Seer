 package wikipedia.analysis.drilldown;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import util.MapSorter;
 import wikipedia.analysis.pagenetwork.DeltaPrinter;
 import wikipedia.database.DBUtil;
 import wikipedia.http.PageHistoryFetcher;
 import wikipedia.http.PageLinkInfoFetcher;
 import wikipedia.http.WikiAPIClient;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public final class RelatedResultsFetcher {
 
     private static final int NBR_WEEKS = 4;
 
     private static final Logger LOG = LoggerFactory.getLogger(RelatedResultsFetcher.class.getName());
 
     private final String lang;
     private final String searchTerm;
     private final NumberOfRecentEditsFetcher numberOfRecentEditsFetcher;
     private final DBUtil database = new DBUtil();
 
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
 
     public static void main(final String[] args) throws IOException {
         final String lang = "en";
         RelatedResultsFetcher fetcher = new RelatedResultsFetcher("Dominique Strauss-Kahn", lang);
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
 
         Map<Integer, String> allPages = fetcher.prepareNodesForNetwork(allSeenNodes);
 
         List<DateTime> allTimeFrames = PageHistoryFetcher.getAllDatesForHistory(1,
                 PageHistoryFetcher.MOST_RECENT_DATE.toDateTime());
         DeltaPrinter dp = new DeltaPrinter(allPages, allTimeFrames, "en");
         String completeJSONForPage = dp.buildNetworksAndGenerateInfo();
         FileUtils.write(new File("out/search_dsk.json"), completeJSONForPage, "UTF-8");
     }
 
     private Map<Integer, String> prepareNodesForNetwork(final Set<String> allSeenNodes) {
         Map<Integer, String> idsAndPages = Maps.newHashMap();
         for (String pageTitle : allSeenNodes) {
             try {
                 int id = database.getPageIDFromCache(pageTitle, lang);
                idsAndPages.put(id, pageTitle);
             } catch (Exception e) {
                 e.printStackTrace();
                 LOG.info("Problem while getting: " + pageTitle);
             }
         }
 
         new PageHistoryFetcher(idsAndPages, "en", 1).fetchCompleteCategories();
         return idsAndPages;
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
