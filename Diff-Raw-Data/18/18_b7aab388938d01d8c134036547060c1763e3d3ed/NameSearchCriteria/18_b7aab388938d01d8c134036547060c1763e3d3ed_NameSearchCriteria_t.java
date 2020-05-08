 package ro.finsiel.eunis.search.sites.names;
 
 import java.util.Hashtable;
 
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.sites.SitesSearchCriteria;
 import ro.finsiel.eunis.search.sites.SitesSearchUtility;
 
 /**
  * Search criteria used for sites->names search.
  * 
  * @author finsiel
  */
 public class NameSearchCriteria extends SitesSearchCriteria {
 
     /** Site name. */
     private String englishName = null;
 
     /** Relation between scientificName or vernacularName (starts, contains, is). */
     private Integer relationOp = null;
 
     /** Filters applied only to main searches. */
     private String country = null;
     private String yearMin = null;
     private String yearMax = null;
 
     /** Mappings from search criteria -> SQL Statement. */
     private Hashtable sqlMappings = null;
 
     /** Mappings from search criteria -> Human language. */
     private Hashtable humanMappings = null;
 
     /**
      * Ctor for main search criteria.
      * 
      * @param englishName
      *            Searched string.
      * @param relationOp
      *            Relation operator.
      * @param country
      *            Country filter.
      * @param minDesignationDate
      *            Minimum designation year.
      * @param maxDesignationDate
      *            Maximum designation year.
      */
     public NameSearchCriteria(String englishName, Integer relationOp, String country, String minDesignationDate,
             String maxDesignationDate) {
         _initHumanMappings();
         _initSQLMappings();
         this.country = country;
         this.yearMin = minDesignationDate;
         this.yearMax = maxDesignationDate;
         this.englishName = englishName;
         this.relationOp = relationOp;
     }
 
     /**
      * Second constructor used to construct filter search criterias (search in results).
      * 
      * @param criteriaSearch
      *            Search string.
      * @param criteriaType
      *            What we search for. Can be CRITERIA_GROUP/ORDER/SCIENTIFIC_NAME.
      * @param oper
      *            Type of relation between criteriaSearch & criteriaType. Can be OPERATOR_IS/CONTAINS/STARTS.
      */
     public NameSearchCriteria(String criteriaSearch, Integer criteriaType, Integer oper) {
         _initHumanMappings();
         _initSQLMappings();
         if (null != criteriaSearch && null != criteriaType && null != oper) {
             this.criteriaSearch = criteriaSearch;
             this.criteriaType = criteriaType;
             this.oper = oper;
         }
     }
 
     /** Init the mappings used to compose the SQL query. */
     private void _initSQLMappings() {
         if (null != sqlMappings) {
             return;
         }
         sqlMappings = new Hashtable();
         sqlMappings.put(CRITERIA_ENGLISH_NAME, "A.NAME ");
         sqlMappings.put(CRITERIA_SOURCE_DB, "A.SOURCE_DB ");
         sqlMappings.put(CRITERIA_SIZE, "A.AREA ");
         sqlMappings.put(CRITERIA_COUNTRY, "C.AREA_NAME_EN ");
         sqlMappings.put(CRITERIA_ID, "A.ID_SITE ");
     }
 
     /** Init the human mappings so you can represent this object in human language. */
     private void _initHumanMappings() {
         if (null != humanMappings) {
             return;
         }
         humanMappings = new Hashtable();
         humanMappings.put(CRITERIA_ENGLISH_NAME, "Site name");
         humanMappings.put(CRITERIA_SOURCE_DB, "Database source");
         humanMappings.put(CRITERIA_SIZE, "Area size ");
         humanMappings.put(CRITERIA_COUNTRY, "Country ");
         humanMappings.put(CRITERIA_ID, "Site id");
     }
 
     /**
      * This method must be implementing by inheriting classes and should return the representation of an object as an URL, for
      * example if implementing class has 2 params: county/region then this method should return: country=XXX&region=YYY, in order to
      * put the object on the request to forward params to next page.
      * 
      * @return An URL compatible representation of this object.
      */
     public String toURLParam() {
         StringBuffer res = new StringBuffer();
 
         if (null != englishName && null != relationOp) {
             res.append(Utilities.writeURLParameter("englishName", englishName));
             res.append(Utilities.writeURLParameter("relationOp", relationOp.toString()));
         }
         if (null != country) {
             // AND C.AREA_NAME_EN='FRANCE'
             res.append(Utilities.writeURLParameter("country", country));
         }
         if (null != yearMin) {
             res.append(Utilities.writeURLParameter("yearMin", yearMin));
         }
         if (null != yearMax) {
             res.append(Utilities.writeURLParameter("yearMax", yearMax));
         }
         // Search in results
         if (null != criteriaSearch) {
             res.append(Utilities.writeURLParameter("criteriaSearch", criteriaSearch));
         }
         if (null != criteriaType) {
             res.append(Utilities.writeURLParameter("criteriaType", criteriaType.toString()));
         }
         if (null != oper) {
             res.append(Utilities.writeURLParameter("oper", oper.toString()));
         }
         return res.toString();
     }
 
     /**
      * Transform this object into an SQL representation without fuzzy search.
      * 
      * @return SQL string representing this object.
      */
     public String toSQL() {
         return toSQL(false);
     }
 
     /**
      * Transform this object into an SQL representation.
      * 
      * @return SQL string representing this object.
      */
     public String toSQL(boolean fuzzySearch) {
         StringBuffer sql = new StringBuffer();
 
         if (null != englishName && null != relationOp) {
             sql.append("((");
             sql.append(Utilities.prepareSQLOperator((String) sqlMappings.get(CRITERIA_ENGLISH_NAME), englishName,
                     Utilities.OPERATOR_CONTAINS));
             sql.append(") OR (");
             sql.append(Utilities.prepareSQLOperator((String) sqlMappings.get(CRITERIA_ID), englishName,
                     Utilities.OPERATOR_CONTAINS));
            
            int substringLength = 3;
            if (fuzzySearch && englishName.length() >=substringLength) {
                 sql.append(") OR (");
                String subSearchString = englishName.substring(0, substringLength);
                 sql.append(" " + sqlMappings.get(CRITERIA_ENGLISH_NAME) + "  LIKE '%" + subSearchString + "%' AND levenshtein('"
                         + subSearchString + "', " + sqlMappings.get(CRITERIA_ENGLISH_NAME) + ") <= 10 ");
             }
 
             sql.append("))");
             if (null != country) {
                 // AND C.AREA_NAME_EN='FRANCE'
                 sql.append(" AND (C.AREA_NAME_EN='" + country + "')");
             }
             if (null != yearMin) {
                 sql.append(" AND (LENGTH(DESIGNATION_DATE) > 0) ");
                 sql.append(" AND (CAST(CONCAT(IF(A.source_db='BIOGENETIC',left(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CDDA_INTERNATIONAL',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CDDA_NATIONAL',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='EMERALD',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='DIPLOMA',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='NATURA2000',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CORINE',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='NATURENET',right(DESIGNATION_DATE,4),'')) AS SIGNED) >= " + yearMin + ") "
                         + " AND A.DESIGNATION_DATE IS NOT NULL" + " AND A.DESIGNATION_DATE <> ''");
             }
             if (null != yearMax) {
                 sql.append(" AND (LENGTH(DESIGNATION_DATE) > 0) ");
                 sql.append(" AND (CAST(CONCAT(IF(A.SOURCE_DB='BIOGENETIC',left(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CDDA_INTERNATIONAL',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CDDA_NATIONAL',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='EMERALD',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='DIPLOMA',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='NATURA2000',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='CORINE',right(DESIGNATION_DATE,4),''), "
                         + "IF(A.SOURCE_DB='NATURENET',right(DESIGNATION_DATE,4),'')) AS SIGNED) <= " + yearMax + ") "
                         + " AND A.DESIGNATION_DATE IS NOT NULL" + " AND A.DESIGNATION_DATE <> ''");
             }
         }
         // Search in results
         if (null != criteriaSearch && null != criteriaType && null != oper) {
             if (0 == criteriaType.compareTo(CRITERIA_SOURCE_DB)) {
                 sql.append(Utilities.prepareSQLOperator((String) sqlMappings.get(criteriaType),
                         SitesSearchUtility.translateSourceDBInvert(criteriaSearch), oper));
             } else {
                 sql.append(Utilities.prepareSQLOperator((String) sqlMappings.get(criteriaType), criteriaSearch, oper));
             }
         }
         return sql.toString();
     }
 
     /**
      * This method implements a procedure from morphing the object into an web page FORM representation. What I meant to say is that
      * I can say about an object for example: < INPUT type='hidden" name="searchCriteria" value="natrix"> < INPUT type='hidden"
      * name="oper" value="1"> < INPUT type='hidden" name="searchType" value="1">
      * 
      * @return Web page FORM representation of the object
      */
     public String toFORMParam() {
         StringBuffer res = new StringBuffer();
 
         if (null != englishName && null != relationOp) {
             res.append(Utilities.writeFormParameter("englishName", englishName));
             res.append(Utilities.writeFormParameter("relationOp", relationOp.toString()));
         }
         if (null != country) {
             // AND C.AREA_NAME_EN='FRANCE'
             res.append(Utilities.writeFormParameter("country", country));
         }
         if (null != yearMin) {
             res.append(Utilities.writeFormParameter("yearMin", yearMin));
         }
         if (null != yearMax) {
             res.append(Utilities.writeFormParameter("yearMax", yearMax));
         }
         // Search in results
         if (null != criteriaSearch) {
             res.append(Utilities.writeFormParameter("criteriaSearch", criteriaSearch));
         }
         if (null != criteriaType) {
             res.append(Utilities.writeFormParameter("criteriaType", criteriaType.toString()));
         }
         if (null != oper) {
             res.append(Utilities.writeFormParameter("oper", oper.toString()));
         }
         return res.toString();
     }
 
     /**
      * This method supplies a human readable string representation of this object. for example "Country is Romania"... so an
      * representation of this object could be displayed on the page.
      * 
      * @return A human readable representation of an object.
      */
     public String toHumanString() {
         StringBuffer ret = new StringBuffer();
 
         if (null != englishName && null != relationOp) {
             ret.append(Utilities.prepareHumanString("Site code contains or name ", englishName, relationOp));
         }
         if (null != country) {
             // AND C.AREA_NAME_EN='FRANCE'
             ret.append(", in country " + country);
         }
         if (null != yearMin && null != yearMax) {
             ret.append(", and year between: " + yearMin + " and " + yearMax);
         } else {
             if (null != yearMin) {
                 ret.append(", and year greater than " + yearMin);
             }
             if (null != yearMax) {
                 ret.append(", and year smaller that " + yearMax);
             }
         }
         // Search in results
         if (null != criteriaSearch && null != criteriaType && null != oper) {
             if (0 == criteriaType.compareTo(CRITERIA_SOURCE_DB)) {
                 ret.append(Utilities.prepareHumanString((String) humanMappings.get(criteriaType),
                         SitesSearchUtility.translateSourceDB(criteriaSearch), oper));
             } else {
                 ret.append(Utilities.prepareHumanString((String) humanMappings.get(criteriaType), criteriaSearch, oper));
             }
         }
         return ret.toString();
     }
 
     public String getEnglishName() {
         return englishName;
     }
 }
