 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import dataparser.Dataparser;
 
 import Model.CoAuthor;
 import Model.enumPackage.PublicationTypeEnum;
 import java.util.Calendar;
 
 public class CollaborationController {
 
     public HashMap<String, ArrayList<CoAuthor>> getCollaborationList() {
         System.out.println("getCollaborationList");
         Dataparser dp = Dataparser.getInstance();
         List<String> authors = dp.getListOfAuthors();
         HashMap<String, ArrayList<CoAuthor>> retval = new HashMap<String, ArrayList<CoAuthor>>();
         ArrayList<CoAuthor> coauthors;
         for (String aut : authors) {
             String key = aut + " (" + dp.getNumberOfCoAuthorsForAuthor(aut) + ")";
             coauthors = new ArrayList<CoAuthor>();
             for (String coaut : dp.getCoAuthorsForAuthor(aut)) {
                 coauthors.add(new CoAuthor(coaut, dp.getNumberOfCoAuthorsForAuthor(coaut)));
             }
             retval.put(key, coauthors);
         }
 
         return retval;
     }
 
     private int getMinYear() {
         List<String> years = Dataparser.getInstance().getDistinctPublicationYears();
         int minYear = Calendar.getInstance().get(Calendar.YEAR); // current year
         for (String year : years) {
             if (Integer.parseInt(year) < minYear) {
                 minYear = Integer.parseInt(year);
             }
         }
         return minYear;
     }
 
     private int getCurrentYear() {
         return Calendar.getInstance().get(Calendar.YEAR); // current year
     }
 
     // parameters:
     // int yearFrom = minimum year that should be included; -1 if we have no lower limit
     // int yearTo = maximum year that should be included; -1 if we have no upper limit (current year is the limit)
     // String type = the type we want to filter for; "all" if we want to have all
     public HashMap<String, ArrayList<CoAuthor>> getCollaborationList(int yearFrom, int yearTo, String type) {
         if (yearFrom == -1) {
             yearFrom = getMinYear();
         }
         if (yearTo == -1) {
             yearTo = getCurrentYear();
         }
         type= PublicationTypeEnum.uglify(type);
 
         ArrayList<Integer> years = new ArrayList<Integer>();
         for (int idx = yearFrom; idx <= yearTo; idx++) {
             years.add(idx);
         }
 
         Dataparser dp = Dataparser.getInstance();
         List<String> authors = dp.getListOfAuthors();
         HashMap<String, ArrayList<CoAuthor>> retval = new HashMap<String, ArrayList<CoAuthor>>();
         ArrayList<CoAuthor> coauthors;
         for (String aut : authors) {
            String key = aut + " (" + dp.getNumberOfCoAuthorsForAuthorInYears(aut, years) + ")";
             coauthors = new ArrayList<CoAuthor>();
 
             ArrayList<String> coAuthorsOfAuthor;
             if (type.equals("all")) { // with type or without
                 coAuthorsOfAuthor = dp.getCoAuthorsForAuthorInYears(aut, years);
             } else {
                 coAuthorsOfAuthor = dp.getCoAuthorsForAuthorInYearsByType(aut, years, type);
             }
             for (String coaut : coAuthorsOfAuthor) {
                coauthors.add(new CoAuthor(coaut, dp.getNumberOfCoAuthorsForAuthorInYears(coaut, years)));
             }
             retval.put(key, coauthors);
         }
 
         return retval;
 
 
     }
 }
