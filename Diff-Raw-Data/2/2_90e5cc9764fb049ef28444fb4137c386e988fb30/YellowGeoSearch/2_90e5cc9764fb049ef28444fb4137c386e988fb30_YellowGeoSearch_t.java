 /*
  * Copyright (2005) Schibsted S�k AS
  */
 package no.schibstedsok.front.searchportal.command;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
import no.schibstedsok.front.searchportal.query.token.TokenMatch;
 import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
 import no.schibstedsok.front.searchportal.query.RunningQuery;
 import no.schibstedsok.front.searchportal.result.FastSearchResult;
 import no.schibstedsok.front.searchportal.result.SearchResult;
 import no.schibstedsok.front.searchportal.result.YellowSearchResult;
 
 public class YellowGeoSearch extends FastSearchCommand {
     private static Log log = LogFactory.getLog(YellowGeoSearch.class);
 
     private boolean ignoreGeoNav = false;
 
     private boolean isLocal;
 
     private boolean isTop3 = false;
 
     private boolean ypkeywordsgeo = false;
     
     public YellowGeoSearch(final SearchCommand.Context cxt, final Map parameters) {
         super(cxt, parameters);
     }
 
     protected Map getNavigators() {
 
         if (ignoreGeoNav && super.getNavigators() != null) {
             Map m = new HashMap();
             m.putAll(super.getNavigators());
             m.remove("geographic");
             log.debug("XYZ" + m.size());
             return m;
         }
       
         return super.getNavigators();
     }
 
     public SearchResult execute() {
 
         boolean viewAll = false;
 
         if (getParameters().containsKey("ypviewall")) {
             viewAll = true;
         }
 
         if (isLocalSearch() && !viewAll) {
             log.debug("Search is local");    
             
             // The search containing all hits. Including non-local.
             ignoreGeoNav = true;
             isLocal = false;
             
             ypkeywordsgeo = true;
             FastSearchResult nationalHits = (FastSearchResult) super.execute();
             ypkeywordsgeo = false;
 
             ignoreGeoNav = false;
             isTop3 = true;
             FastSearchResult top3 = (FastSearchResult) super.execute(); 
             isTop3 = false;
 
             // Perform local search.
             ignoreGeoNav = false;
             isLocal = true;
             FastSearchResult localResult = (FastSearchResult) super.execute();
             
             YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, isLocalSearch() && !viewAll);
             return result;    
         } else if (!viewAll) {
             isLocal = false;
             isTop3 = true;
             FastSearchResult top3 = (FastSearchResult) super.execute(); 
             isTop3 = false;
             ypkeywordsgeo = true;
             FastSearchResult nationalHits = (FastSearchResult) super.execute();
             ypkeywordsgeo = false;
 
             YellowSearchResult result = new YellowSearchResult(this, null, nationalHits, top3, false);
             return result;    
         } else {
 
             log.debug("Search is viewall");
             ypkeywordsgeo = false;
 
 		isLocal = true;
             ignoreGeoNav = true;
             FastSearchResult localResult = (FastSearchResult) super.execute();
             ignoreGeoNav = false;
             isLocal = false;
 
             isTop3 = true;
             FastSearchResult top3 = (FastSearchResult) super.execute(); 
             isTop3 = false;
 
             isLocal = false;
             ypkeywordsgeo = true;
             FastSearchResult nationalHits = (FastSearchResult) super.execute();
             log.debug("ASD " + nationalHits.getHitCount());
             ypkeywordsgeo = false;
 
             
             YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, false);
             return result;    
         }
     }
 
     private boolean isLocalSearch() {
         return getQuery().getGeographicMatches().size() > 0;
     }
 
     
     private TokenMatch getLastGeoMatch() {
         List matches = getQuery().getGeographicMatches();
 
         if (matches.size() > 0) {
             return (TokenMatch) matches.get(matches.size() - 1);
         } else {
             return null;
         }
     }
     
     protected String getSortBy() {
         if (isLocal) {
             return "yellowpages2 +ypnavn";
         } else {
             return "yellowpages2geo +ypnavn";
         }
     }
 
     public String getTransformedQuery() {
         if (isTop3) {
             return super.getTransformedQuery().replaceAll("yellowphon:", "");
         }
         
         if (isLocal) {
             return super.getTransformedQuery();
         } else {
             return super.getTransformedQuery().replaceAll("yellowphon", "yellowgeophon");
         }
     }
 
     protected int getResultsToReturn() {
         if (isTop3) {
             return 3;
         } else {
         // FIXME getResultsToReturn
         return super.getResultsToReturn();
         }
         }
 
     protected String getAdditionalFilter() {
         if (ypkeywordsgeo && getLastGeoMatch() != null) {
             return "+ypkeywordsgeo:" + getLastGeoMatch().getMatch();
         } else {
             return null;
         }
 
     }
 }
