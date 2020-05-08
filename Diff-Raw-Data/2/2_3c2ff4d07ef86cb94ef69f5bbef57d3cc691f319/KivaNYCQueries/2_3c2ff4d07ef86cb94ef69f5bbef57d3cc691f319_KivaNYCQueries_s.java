 package com.nfolkert.kiva.utils.queries;
 
 import com.nfolkert.json.JSONObject;
 import com.nfolkert.json.JSONException;
 import com.nfolkert.kiva.utils.*;
 import com.nfolkert.googlemaps.GeoCodeManager;
 import com.nfolkert.utils.Pair;
 
 import java.util.regex.Pattern;
 import java.util.Set;
 import java.util.HashSet;
 import java.text.DecimalFormat;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  */
 public class KivaNYCQueries
 {
     public static String kKivaNYCTeam = "new_york";
     public static int kKivaNYCTeamId = 288;
 
     public static Pattern _datePattern =
             Pattern.compile("^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d).*$");
 
     private static Set<String> _nyTeamMembers = null;
 
     private static final DecimalFormat sFormatter = new DecimalFormat("#,##0.0");
     private static final String kUnionSquare = "170 Union Square E New York, NY 10003";
 
 
     public static void buildNYTeamMembers()
             throws Exception
     {
         _nyTeamMembers = new HashSet<String>();
         final Set<String> res = new HashSet<String>();
         new KivaFetcher().fetchTeamLenders(new KivaFetchHandler.Complete()
         {
             @Override
             public void handle(JSONObject object)
                     throws Exception
             {
                 final String id = object.optString("lender_id");
                 if (id != null)
                     res.add(id);
             }
         }, kKivaNYCTeamId);
 
         _nyTeamMembers.addAll(res);
     }
 
     public static boolean isOnNYCTeam(String userId)
             throws Exception
     {
         if (_nyTeamMembers == null)
             buildNYTeamMembers();
         return _nyTeamMembers.contains(userId);
     }
 
     public static boolean isPittsburghLender(String whereabouts)
             throws Exception
     {
         if (whereabouts == null) return false;
         final Pair<Double,Double> latAndLong = GeoCodeManager.getGeoCode(whereabouts);
         if (latAndLong == null) return false;
 
         double lat = latAndLong.getHead();
         double lon = latAndLong.getTail();
 
         if (isCloseTo("Pittsburgh, PA", lat, lon, 30))
             return true;
 
         return false;
     }
 
     public static boolean isNYCLender(String whereabouts)
             throws Exception
     {
         if (whereabouts == null) return false;
         final Pair<Double,Double> latAndLong = GeoCodeManager.getGeoCode(whereabouts);
         if (latAndLong == null) return false;
 
         double lat = latAndLong.getHead();
         double lon = latAndLong.getTail();
 
         if (isCloseTo("New York, NY", lat, lon, 20) ||
             isCloseTo("Brooklyn, NY", lat, lon, 10) ||
             isCloseTo("Bronx, NY", lat, lon, 10) ||
             isCloseTo("Staten Island, NY", lat, lon, 5) ||
             isCloseTo("Queens, NY", lat, lon, 10))
             return true;
 
         return false;
     }
 
     public static double getMilesFrom(String locationOne, String locationTwo)
             throws Exception
     {
         final Pair<Double, Double> loc1 = GeoCodeManager.getGeoCode(locationOne);
         final Pair<Double, Double> loc2 = GeoCodeManager.getGeoCode(locationTwo);
         final double distanceKm = GeoCodeManager.distanceBetween(loc1.getHead(), loc1.getTail(), loc2.getHead(), loc2.getTail());
         return GeoCodeManager.toMiles(distanceKm);
     }
 
     private static boolean isCloseTo(String otherLocation, double lat, double lon, double withinMiles)
             throws Exception
     {
         final Pair<Double, Double> otherLoc = GeoCodeManager.getGeoCode(otherLocation);
         final double distanceKm = GeoCodeManager.distanceBetween(lat, lon, otherLoc.getHead(), otherLoc.getTail());
         return GeoCodeManager.toMiles(distanceKm) < withinMiles;
     }
 
     public static String trimDate(String dateString)
     {
         final int startTime = dateString.indexOf('T');
         if (startTime < 0) return dateString;
         return dateString.substring(0, startTime);
     }
 
     public static boolean isLenderJoinedSince(JSONObject lender, String yyyy_mm_dd)
             throws JSONException
     {
         final String memberSince = lender.optString("member_since");
         if (memberSince == null) return false;
         String date = memberSince.substring(0, "YYYY-MM-DD".length());
         int cmp = date.compareTo(yyyy_mm_dd);
         return cmp >= 0;
     }
 
     public static void getNYCTeamMembers()
             throws Exception
     {
         System.out.println("Name\tLender Page\tJoined Team\tJoined Kiva\tLocation\tDistance\tOccupation\tNumber of Loans\tPersonal Webpage");
 
         final KivaFetcher fetcher = new KivaFetcher();
         fetcher.fetchTeamLenders(new KivaFetchHandler.Complete()
         {
             public void handle(JSONObject lenderSummary)
                     throws Exception
             {
                 String lenderId = lenderSummary.getString("lender_id");
                 JSONObject lender = fetcher.getLenderById(lenderId);
                 String teamJoinDate = lenderSummary.optString("team_join_date");
                 printTeamLenderRow(lender, teamJoinDate);
             }
         }, kKivaNYCTeamId);
     }
 
     /**
      * How to run the query:
      * Delete the items from the NewestLenders query cache
      */
     public static void main(String[] args)
             throws Exception
     {
         // First time you run, need to clear the appropriate cache for ordered queries
         // KivaResultManager.clearCache(KivaQueryType.values());
 
         // This cache should be cleared when retrieving latest new york lenders
         KivaResultManager.clearCache(KivaQueryType.NewestLenders);
 
         // This cache should be cleared when retrieving team lenders (not sure if it's necessary)
         // KivaResultManager.clearCache(KivaQueryType.TeamLenders);
 
         try
         {
             // Uncomment to get all NYC team members:
             // getNYCTeamMembers();
 
             // Not sure what this is for:
             // getNYCLendersFromKiva(1, 100);
 
             // Uncomment to get latest lenders joined
            getLatestNYCLendersFromKiva(1, 10);
             // getLatestPittsburghLendersFromKiva(1, 1200);
         }
         finally
         {
             KivaResultManager.saveCache();
             KivaResultManager.dumpCache(false);
             GeoCodeManager.saveCache();
             GeoCodeManager.dumpCache(false);
         }
     }
 
     private static void printLenderRow(JSONObject lender)
             throws Exception
     {
         String name = lender.optString("name");
         String id = lender.optString("lender_id");
         String date = lender.optString("member_since");
         String where = lender.optString("whereabouts");
         String job = lender.optString("occupation");
         String loans = lender.optString("loan_count");
         String web = lender.optString("personal_url");
 
         name = name == null ? "" : name;
         id = id == null ? "" : id;
         date = date == null ? "" : trimDate(date);
         where = where == null ? "" : where;
         job = job == null ? "" : job;
         loans = loans == null ? "" : loans;
         web = web == null ? "" : web;
 
         String milesFrom = "".equals(where) ? "" : sFormatter.format(getMilesFrom(where, kUnionSquare));
 
         String lenderPage = id == null ? "" : "http://www.kiva.org/lender/" + id;
 
         String row = StringUtils.join(new String[]{
                 name, lenderPage, "", "", date, where, milesFrom, job, loans, web
         }, "\t");
 
         System.out.println(row);
     }
 
     private static void printTeamLenderRow(JSONObject lender, String teamJoinDate)
             throws Exception
     {
         String name = lender.optString("name");
         String id = lender.optString("lender_id");
         String date = lender.optString("member_since");
         String where = lender.optString("whereabouts");
         String job = lender.optString("occupation");
         String loans = lender.optString("loan_count");
         String web = lender.optString("personal_url");
 
         name = name == null ? "" : name;
         id = id == null ? "" : id;
         date = date == null ? "" : trimDate(date);
         where = where == null ? "" : where;
         job = job == null ? "" : job;
         loans = loans == null ? "" : loans;
         web = web == null ? "" : web;
         teamJoinDate = teamJoinDate == null ? "" : trimDate(teamJoinDate);
 
         String milesFrom = "".equals(where) ? "" : sFormatter.format(getMilesFrom(where, kUnionSquare));
 
         String lenderPage = id == null ? "" : "http://www.kiva.org/lender/" + id;
 
         String row = StringUtils.join(new String[]{
                 name, lenderPage, teamJoinDate, date, where, milesFrom, job, loans, web
         }, "\t");
 
         System.out.println(row);
     }
 
     private static void getNYCLendersFromKiva(final int startAtPage, final int maxPages)
             throws Exception
     {
         final int maxPage = startAtPage + (maxPages-1);
 
         final int[] currentPage = new int[]{startAtPage};
 
         System.out.println("Name\tLender Page\tJoined\tLocation\tDistance\tOccupation\tNumber of Loans\tPersonal Webpage");
 
         final KivaFetcher fetcher = new KivaFetcher();
         fetcher.fetchLenders(new KivaFetchHandler()
         {
             public boolean continueQuery(JSONObject jobj)
             {
                 // System.out.println("Scanned page " + currentPage[0]);
                 currentPage[0]++;
                 return (currentPage[0] <= maxPage);
             }
 
             public void handle(JSONObject lenderSummary)
                     throws Exception
             {
                 String whereabouts = lenderSummary.optString("whereabouts");
                 String id = lenderSummary.optString("lender_id");
                 if (id == null) return;
 
                 if (isNYCLender(whereabouts) && !isOnNYCTeam(id))
                 {
                     JSONObject lender = fetcher.getLenderById(id);
                     printLenderRow(lender);
                 }
             }
         }, startAtPage);
 
         // System.out.println("Scanned to page " + (currentPage[0] - 1));
     }
 
     private static void getLatestPittsburghLendersFromKiva(final int startAtPage, final int maxPages)
             throws Exception
     {
         final int maxPage = startAtPage + (maxPages-1);
 
         final int[] currentPage = new int[]{startAtPage};
 
         System.out.println("Name\tLender Page\tClaimed\tContacted\tJoined\tLocation\tDistance\tOccupation\tNumber of Loans\tPersonal Webpage");
 
         final KivaFetcher fetcher = new KivaFetcher();
         fetcher.fetchNewestLenders(new KivaFetchHandler()
         {
             public boolean continueQuery(JSONObject jobj)
             {
                 // System.out.println("Scanned page " + currentPage[0]);
                 currentPage[0]++;
                 return (currentPage[0] <= maxPage);
             }
 
             public void handle(JSONObject lenderSummary)
                     throws Exception
             {
                 String whereabouts = lenderSummary.optString("whereabouts");
                 String id = lenderSummary.optString("lender_id");
                 if (id == null) return;
 
                 if (isPittsburghLender(whereabouts)) // Do we have a team id for pittsburgh to filter?
                 {
                     JSONObject lender = fetcher.getLenderById(id);
                     printLenderRow(lender);
                 }
             }
         }, startAtPage);
 
         // System.out.println("Scanned to page " + (currentPage[0] - 1));
     }
 
     private static void getLatestNYCLendersFromKiva(final int startAtPage, final int maxPages)
             throws Exception
     {
         final int maxPage = startAtPage + (maxPages-1);
 
         final int[] currentPage = new int[]{startAtPage};
 
         System.out.println("Name\tLender Page\tClaimed\tContacted\tJoined\tLocation\tDistance\tOccupation\tNumber of Loans\tPersonal Webpage");
 
         final KivaFetcher fetcher = new KivaFetcher();
         fetcher.fetchNewestLenders(new KivaFetchHandler()
         {
             public boolean continueQuery(JSONObject jobj)
             {
                 // System.out.println("Scanned page " + currentPage[0]);
                 currentPage[0]++;
                 return (currentPage[0] <= maxPage);
             }
 
             public void handle(JSONObject lenderSummary)
                     throws Exception
             {
                 String whereabouts = lenderSummary.optString("whereabouts");
                 String id = lenderSummary.optString("lender_id");
                 if (id == null) return;
 
                 if (isNYCLender(whereabouts) && !isOnNYCTeam(id))
                 {
                     JSONObject lender = fetcher.getLenderById(id);
                     printLenderRow(lender);
                 }
             }
         }, startAtPage);
 
         // System.out.println("Scanned to page " + (currentPage[0] - 1));
     }
 
     private static void getLatestNYCLendersFromDump()
             throws Exception
     {
         new KivaDump().runQuery(KivaQueryType.Lenders, new KivaQueryHandler()
         {
             public void handle(JSONObject lender)
                     throws Exception
             {
                 if (isNYCLender(lender.optString("whereabouts")) && isLenderJoinedSince(lender, "2009-12-09"))
                 {
                     System.out.println(lender);
                 }
             }
         });
     }
 }
