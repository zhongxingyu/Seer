 package ro.finsiel.eunis.jrfTables.species.references;
 
 
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import net.sf.jrf.column.columnoptions.NullableColumnOption;
 import net.sf.jrf.column.columnspecs.DateColumnSpec;
 import net.sf.jrf.column.columnspecs.IntegerColumnSpec;
 import net.sf.jrf.column.columnspecs.StringColumnSpec;
 import net.sf.jrf.domain.AbstractDomain;
 import net.sf.jrf.domain.PersistentObject;
 import ro.finsiel.eunis.exceptions.CriteriaMissingException;
 import ro.finsiel.eunis.exceptions.InitializationException;
 import ro.finsiel.eunis.search.AbstractSearchCriteria;
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 import ro.finsiel.eunis.search.Paginable;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.species.references.ReferencesSearchCriteria;
 import ro.finsiel.eunis.search.species.references.ReferencesSortCriteria;
 
 
 public class SpeciesBooksDomain extends AbstractDomain implements Paginable {
 
     /** Criterias applied for searching */
     private AbstractSearchCriteria[] searchCriteria = new AbstractSearchCriteria[0]; // 0 length means not criteria set
 
     /** Criterias applied for sorting */
     private AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0]; // 0 length means unsorted
 
     /** Cache the results of a count to avoid overhead queries for counting */
     private Long _resultCount = new Long(-1);
 
     private boolean showInvalidatedSpecies = false;
     private List finalResults = null;
 
     public SpeciesBooksDomain(AbstractSearchCriteria[] searchCriteria, boolean showInvalidatedSpecies) {
         this.searchCriteria = searchCriteria;
         this.showInvalidatedSpecies = showInvalidatedSpecies;
     }
 
     /****/
     public PersistentObject newPersistentObject() {
         return new SpeciesBooksPersist();
     }
 
     /****/
     public void setup() {
         // These setters could be used to override the default.
         // this.setDatabasePolicy(new null());
         // this.setJDBCHelper(JDBCHelperFactory.create());
 
         this.setTableName("DC_INDEX");
         this.setReadOnly(true);
         this.setTableAlias("A");
 
         this.addColumnSpec(new IntegerColumnSpec("ID", "getId", "setId", DEFAULT_TO_ZERO, NATURAL_PRIMARY_KEY));
         this.addColumnSpec(new IntegerColumnSpec("ID_LINK", "getIdLink", "setIdLink", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("NAME", "getName", "setName", DEFAULT_TO_EMPTY_STRING));
         this.addColumnSpec(new StringColumnSpec("EDITOR", "getEditor", "setEditor", DEFAULT_TO_EMPTY_STRING));
        this.addColumnSpec(new DateColumnSpec("DATE", new NullableColumnOption(), "getDate", "setDate", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("TITLE", "getTitle", "setTitle", DEFAULT_TO_EMPTY_STRING));
         this.addColumnSpec(new StringColumnSpec("PUBLISHER", "getPublisher", "setPublisher", DEFAULT_TO_EMPTY_STRING));
         this.addColumnSpec(new StringColumnSpec("URL", "getUrl", "setUrl", DEFAULT_TO_EMPTY_STRING));
     }
 
     /** This method is used to retrieve a sub-set of the main results of a query given its start index offset and end
      * index offset.
      * @param offsetStart The start offset (i.e. 0). If offsetStart = offSetEnd then return the whole list
      * @param pageSize The end offset (i.e. 1). If offsetStart = offSetEnd then return the whole list
      * @param sortCriteria The criteria used for sorting
      * @return A list of objects which match query criteria
      */
     public List getResults(int offsetStart, int pageSize, AbstractSortCriteria[] sortCriteria) throws CriteriaMissingException {
         this.sortCriteria = sortCriteria;
         if (searchCriteria.length < 1) {
             throw new CriteriaMissingException(
             "Unable to search because no search criteria was specified...");
         }
 
         // Add the LIMIT clause to return only the wanted results
         List results = new Vector();
 
         if (pageSize != 0) { // Doesn't make sense for pageSize = 0.
             if (finalResults != null) {
                 int i = 0;
 
                 while ((i <= pageSize - 1)
                         && (offsetStart + i < finalResults.size())) {
                     if (offsetStart + i < finalResults.size()) {
                         results.add(finalResults.get(offsetStart + i));
                     }
                     i++;
                 }
             } else {
                 results = finalResults;
             }
         } else {
             results = finalResults;
         }
 
         // Add order by
         results = _prepareWhereSort(results);
 
         _resultCount = new Long(-1); // After each query, reset the _resultCount, so countResults do correct numbering.
         return results;
     }
 
     public List getSpeciesForAReference(String id) throws CriteriaMissingException {
 
         if (searchCriteria.length < 1) {
             throw new CriteriaMissingException(
             "Unable to search because no search criteria was specified...");
         }
         // Prepare the WHERE clause
         StringBuffer filterSQL = _prepareWhereSearch();
         String condition = "";
 
         if (id != null) {
             String lineCondition = "A.ID_DC = " + id;
 
             condition = (filterSQL.length() > 0
                     ? " AND " + filterSQL.toString() + " AND " + lineCondition
                             + " "
                             : " AND " + lineCondition + " ");
         } else {
             condition = (filterSQL.length() > 0
                     ? " AND " + filterSQL.toString()
                             : "");
         }
 
         String SQL = "(SELECT H.ID_SPECIES AS ID, H.ID_SPECIES_LINK AS ID_LINK, H.SCIENTIFIC_NAME AS NAME, "
             + "H.SCIENTIFIC_NAME AS EDITOR, "
             + new Date(1998, 1, 1).getYear() + " AS DATE, "
             + "H.SCIENTIFIC_NAME AS TITLE, H.SCIENTIFIC_NAME AS PUBLISHER, "
             + "H.SCIENTIFIC_NAME AS URL " + "FROM CHM62EDT_SPECIES H "
             + "INNER JOIN CHM62EDT_NATURE_OBJECT B ON H.ID_NATURE_OBJECT=B.ID_NATURE_OBJECT "
             + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
             + "WHERE 1=1 "
             + condition + " GROUP BY H.SCIENTIFIC_NAME) " + "UNION "
             + "(SELECT H.ID_SPECIES AS ID, H.ID_SPECIES_LINK AS ID_LINK, H.SCIENTIFIC_NAME AS NAME, "
             + "H.SCIENTIFIC_NAME AS EDITOR, "
             + new Date(1998, 1, 1).getYear() + " AS DATE, "
             + "H.SCIENTIFIC_NAME AS TITLE, H.SCIENTIFIC_NAME AS PUBLISHER, "
             + "H.SCIENTIFIC_NAME AS URL " + "FROM CHM62EDT_SPECIES H "
             + "INNER JOIN CHM62EDT_REPORTS B ON H.ID_NATURE_OBJECT=B.ID_NATURE_OBJECT "
             + "INNER JOIN CHM62EDT_REPORT_TYPE K ON B.ID_REPORT_TYPE = K.ID_REPORT_TYPE "
             + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
             + "WHERE 1=1 "
             + condition
             + " AND K.LOOKUP_TYPE IN ('DISTRIBUTION_STATUS','LANGUAGE','CONSERVATION_STATUS','SPECIES_GEO','LEGAL_STATUS','SPECIES_STATUS','POPULATION_UNIT','TREND') "
             + " GROUP BY H.SCIENTIFIC_NAME) " + "UNION "
             + "(SELECT H.ID_SPECIES AS ID, H.ID_SPECIES_LINK AS ID_LINK, H.SCIENTIFIC_NAME AS NAME, "
             + "H.SCIENTIFIC_NAME AS EDITOR, "
             + new Date(1998, 1, 1).getYear() + " AS DATE, "
             + "H.SCIENTIFIC_NAME AS TITLE, H.SCIENTIFIC_NAME AS PUBLISHER, "
             + "H.SCIENTIFIC_NAME AS URL " + "FROM CHM62EDT_SPECIES H "
             + "INNER JOIN CHM62EDT_TAXONOMY B ON H.ID_TAXONOMY=B.ID_TAXONOMY "
             + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
             + "WHERE  1=1 "
             + condition + " GROUP BY H.SCIENTIFIC_NAME) " + " ORDER BY NAME"
             + " LIMIT 0,"
             + new Integer(Utilities.MAX_POPUP_RESULTS).toString();
 
         List species = this.findCustom(SQL.toString());
 
         if (species != null && species.size() > 0) {
             return species;
         } else {
             return new Vector();
         }
     }
 
     /**
      * Prepair a string for search
      * @param s string to be converted
      * @return the new string
      */
     public String convertor(Object s) {
         if (null == s) {
             return " IS NULL";
         } else {
             return " ='" + s + "'";
         }
     }
 
     /** This method is used to count the total list of results from a query. It is used to find all for use in pagination.
      * Having the total number of results and the results displayed per page, the you could find the number of pages i.e.
      * @return Number of results found
      */
     public Long countResults() throws CriteriaMissingException {
         if (-1 == _resultCount.longValue()) {
             _resultCount = _rawCount();
         }
         return _resultCount;
     }
 
     /** This method does the raw counting (meaning that will do a DB query for retrieving results count). You should check
      * in your code that this method is called (in ideal way) only once and results are cached. This is what
      * countResults() method does in this class
 
      * @return
      * @throws ro.finsiel.eunis.exceptions.CriteriaMissingException
      */
     private Long _rawCount() throws CriteriaMissingException {
         if (searchCriteria.length < 1) {
             throw new CriteriaMissingException(
             "Unable to search because no search criteria was specified...");
         }
         if (finalResults == null) {
 
             // Prepare the WHERE clause
             StringBuffer filterSQL = _prepareWhereSearch();
 
             String condition = (filterSQL.length() > 0
                     ? " AND " + filterSQL.toString()
                             : "");
 
             String SQL = "SELECT A.ID_DC AS ID,A.ID_DC AS ID_LINK,A.SOURCE AS NAME,A.EDITOR AS EDITOR,"
                 + "A.CREATED AS DATE,A.TITLE AS TITLE,A.PUBLISHER AS PUBLISHER,A.URL AS URL "
                 + "FROM CHM62EDT_SPECIES H "
                 + "INNER JOIN CHM62EDT_NATURE_OBJECT B ON H.ID_NATURE_OBJECT=B.ID_NATURE_OBJECT "
                 + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
                 + "WHERE 1=1 " + condition
                 + " GROUP BY A.SOURCE,A.EDITOR,A.CREATED,A.TITLE,A.PUBLISHER "
                 + "UNION "
                 + "SELECT A.ID_DC AS ID,A.ID_DC AS ID_LINK,A.SOURCE AS NAME,A.EDITOR AS EDITOR,"
                 + "A.CREATED AS DATE,A.TITLE AS TITLE,A.PUBLISHER AS PUBLISHER,A.URL AS URL "
                 + "FROM CHM62EDT_SPECIES H "
                 + "INNER JOIN CHM62EDT_REPORTS B ON H.ID_NATURE_OBJECT=B.ID_NATURE_OBJECT "
                 + "INNER JOIN CHM62EDT_REPORT_TYPE K ON B.ID_REPORT_TYPE = K.ID_REPORT_TYPE "
                 + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
                 + "WHERE 1=1 " + condition
                 + " AND K.LOOKUP_TYPE IN ('DISTRIBUTION_STATUS','LANGUAGE','CONSERVATION_STATUS','SPECIES_GEO','LEGAL_STATUS',"
                 + "'SPECIES_STATUS','POPULATION_UNIT','TREND') "
                 + "GROUP BY A.SOURCE,A.EDITOR,A.CREATED,A.TITLE,A.PUBLISHER "
                 + "UNION "
                 + "SELECT A.ID_DC AS ID,A.ID_DC AS ID_LINK,A.SOURCE AS NAME,A.EDITOR AS EDITOR,"
                 + "A.CREATED AS DATE,A.TITLE AS TITLE,A.PUBLISHER AS PUBLISHER,A.URL AS URL "
                 + "FROM CHM62EDT_SPECIES H "
                 + "INNER JOIN CHM62EDT_TAXONOMY B ON H.ID_TAXONOMY=B.ID_TAXONOMY "
                 + "INNER JOIN DC_INDEX A ON B.ID_DC = A.ID_DC "
                 + "WHERE  1=1 " + condition
                 + " GROUP BY A.SOURCE,A.EDITOR,A.CREATED,A.TITLE,A.PUBLISHER ";
 
             List references = this.findCustom(SQL.toString());
 
             finalResults = references;
             if (null != references) {
                 return new Long(references.size());
             } else {
                 return new Long(0);
             }
         } else {
             return new Long(finalResults.size());
         }
 
     }
 
     /** This helper method is used to construct the string after WHERE...based on search criterias set. In another words
      * constructs .....WHERE...>>B.ID_GEOSCOPE_LINK=XXX OR B.ID_GEOSCOPE_LINK=YYY OR B.ID_GEOSCOPE_LINK=ZZZ .....
      * @return SQL string
      * @throws ro.finsiel.eunis.exceptions.CriteriaMissingException If no search criteria search or wrong criteria is set.
      */
     private StringBuffer _prepareWhereSearch() throws CriteriaMissingException {
         StringBuffer filterSQL = new StringBuffer();
 
         filterSQL.append(" ((A.SOURCE IS NOT NULL AND TRIM(A.SOURCE) <> '') OR ");
         filterSQL.append(" (A.EDITOR IS NOT NULL AND TRIM(A.EDITOR) <> '') OR ");
         filterSQL.append(
         " (A.CREATED IS NOT NULL AND TRIM(A.CREATED) <> '') OR ");
         filterSQL.append(" (A.TITLE IS NOT NULL AND TRIM(A.TITLE) <> '') OR ");
         filterSQL.append(
         " (A.PUBLISHER IS NOT NULL AND TRIM(A.PUBLISHER) <> '')) ");
 
         filterSQL.append(
                 Utilities.showEUNISInvalidatedSpecies("AND H.VALID_NAME",
                         showInvalidatedSpecies));
 
         if (searchCriteria.length <= 0) {
             throw new CriteriaMissingException(
             "No criteria set for searching, so I bailed out...");
         }
         for (int i = 0; i < searchCriteria.length; i++) {
             filterSQL.append(" AND ");
             ReferencesSearchCriteria aCriteria = (ReferencesSearchCriteria) searchCriteria[i]; // upcast
 
             filterSQL.append(
                     aCriteria.toSQL());
         }
         return filterSQL;
     }
 
     /** Prepare the ORDER BY clause used to do the sorting. Basically it adds the ORDER clause with the criterias set in
      * the sortCriteria[] array.
      * @return SQL representation of the sorting.
      */
 
     private List _prepareWhereSort(List res) {
         List filterSQL = res;
 
         try {
 
             if (sortCriteria.length > 0) {
                 int i = 0;
 
                 do {
 
                     ReferencesSortCriteria criteria = (ReferencesSortCriteria) sortCriteria[i]; // Notice the upcast here
 
                     if (!criteria.getCriteriaAsString().equals("none")) { // Do not add if criteria is sort to NOT SORT
                         if (!criteria.getAscendencyAsString().equals("none")) { // Don't add if ascendency is set to none, nasty hacks
                             String byWhat = "source";
 
                             if (criteria.getSortCriteria().intValue()
                                     == ReferencesSortCriteria.SORT_AUTHOR.intValue()) {
                                 byWhat = "source";
                             }
                             if (criteria.getSortCriteria().intValue()
                                     == ReferencesSortCriteria.SORT_DATE.intValue()) {
                                 byWhat = "date";
                             }
                             if (criteria.getSortCriteria().intValue()
                                     == ReferencesSortCriteria.SORT_EDITOR.intValue()) {
                                 byWhat = "editor";
                             }
                             if (criteria.getSortCriteria().intValue()
                                     == ReferencesSortCriteria.SORT_PUBLISHER.intValue()) {
                                 byWhat = "publisher";
                             }
                             if (criteria.getSortCriteria().intValue()
                                     == ReferencesSortCriteria.SORT_TITLE.intValue()) {
                                 byWhat = "title";
                             }
                             SortListOfPersistentObject sorter = new SortListOfPersistentObject();
 
                             if (criteria.getAscendency().intValue()
                                     == AbstractSortCriteria.ASCENDENCY_ASC.intValue()) {
                                 filterSQL = sorter.sort(filterSQL, false, byWhat);
                             } else {
                                 filterSQL = sorter.sort(filterSQL, true, byWhat);
                             }
 
                         }
                     }
                     i++;
                 } while (i < sortCriteria.length);
             }
 
         } catch (InitializationException e) {
             e.printStackTrace(); // To change body of catch statement use Options | File Templates.
         } finally {
             return filterSQL;
         }
     }
 
     /** This method returns all the criterias used to do the search in database
      * @return An array of criteria objects used to do the search. Please note that in order to do at least one (1) search
      * you should pass an array of at least of length of 1 or the search wouldn't (or at least shouldn't) be done.
      * Also as a trick, by passing more than one (1) object the search should do 'search in results' type of search, so
      * basically an array of length with more than (>) 1 will do addiotional filtering.
      */
     public AbstractSearchCriteria[] getSearchCriteria() {
         return searchCriteria;
     }
 
     /** Sets the search criteria used to do the filters...
      * @param criteria Search criteria used to do the filtering of results.
      * You could use this method to implement an navigation type of search in results. Take for example:
      * Search for species whom scientific name is Salmo trutta, then filter after 'trutta'. you could to an navigation bar
      * which 'remembers' searched results as follows: "Salmo trutta" > "trutta" > .... implemented as links, so user
      * pressing on back links, goes back to the search criteria...
      */
     public void setSearchCriteria(AbstractSearchCriteria[] criteria) {
         this.searchCriteria = criteria;
     }
 
     /** This method must be implementing by inheriting classes and should return the representation of all
      * AbstractSearchCriteria objects as an single URL, for example if implementing class has 2 params: county/region
      * then this method should return:
      * country=XXX&region=YYY&country=ZZZ&region=WWW ...., in order to put all objects on the request to forward params to
      * next page and provide the ability to reconstruct there the objects.
      * @return An URL compatible representation of this object.
      */
     public String getSearchCriteriaAsURL() {
         return null;
     }
 
     /** Retrieve the currently criteria used for sorting
      * @return Sort criteria used
      */
     public AbstractSortCriteria[] getSortCriteria() {
         return new AbstractSortCriteria[0];
     }
 
     /** Set the new sort criteria
      * @param criterias The new criterias (columns) after which the results will be sorted.
      */
     public void setSortCriteria(AbstractSortCriteria[] criterias) {}
 }
