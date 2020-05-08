 package ro.finsiel.eunis.jrfTables.species.names;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.sf.jrf.column.columnspecs.IntegerColumnSpec;
 import net.sf.jrf.column.columnspecs.ShortColumnSpec;
 import net.sf.jrf.column.columnspecs.StringColumnSpec;
 import net.sf.jrf.domain.AbstractDomain;
 import net.sf.jrf.domain.PersistentObject;
 import net.sf.jrf.join.OuterJoinTable;
 import net.sf.jrf.join.joincolumns.StringJoinColumn;
 
 import org.apache.commons.lang.StringUtils;
 
 import ro.finsiel.eunis.exceptions.CriteriaMissingException;
 import ro.finsiel.eunis.search.AbstractSearchCriteria;
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 import ro.finsiel.eunis.search.Paginable;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.species.names.NameSearchCriteria;
 import ro.finsiel.eunis.search.species.names.NameSortCriteria;
 
 public class SimilarNameDomain extends AbstractDomain implements Paginable {
 
     /** Criterias applied for searching. 0 length means not criteria set */
     private AbstractSearchCriteria[] searchCriteria = new AbstractSearchCriteria[0];
 
     /** Criterias applied for sorting. 0 length means unsorted */
     private AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
 
     /** Cache the results of a count to avoid overhead queries for counting */
     private Long _resultCount = new Long(-1);
 
     private boolean showEUNISInvalidatedSpecies = false;
     private boolean searchSynnonyms = false;
     private boolean searchVernacular = false;
 
     private String speciesName = null;
     private int cnt = 0;
 
     private List<ScientificNamePersist> results;
 
     /**
      * Normal constructor
      * 
      * @param searchCriteria
      *            Search criteria used to query the database
      * @param sortCriteria
      *            Sort criteria used to sort the results from database
      */
     public SimilarNameDomain(AbstractSearchCriteria[] searchCriteria, AbstractSortCriteria[] sortCriteria, boolean searchSynonyms,
             boolean showEUNISInvalidatedSpecies, Boolean searchVernacular) {
         this.searchCriteria = searchCriteria;
         this.sortCriteria = sortCriteria;
         this.showEUNISInvalidatedSpecies = showEUNISInvalidatedSpecies;
         this.searchSynnonyms = searchSynonyms;
         this.searchVernacular = false;
         if (searchVernacular != null) {
             this.searchVernacular = searchVernacular.booleanValue();
         }
 
         try {
             getList();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Used by JRF Framework
      * 
      * @return The PersistentObject representing a row from results
      */
     public PersistentObject newPersistentObject() {
         return new ScientificNamePersist();
     }
 
     /**
      * Used by JRF Framework
      */
     protected void setup() {
 
         this.setTableName("CHM62EDT_SPECIES");
         this.setReadOnly(true);
         this.setTableAlias("A");
 
         // Table declaration
         this.addColumnSpec(new IntegerColumnSpec("ID_SPECIES", "getIdSpecies", "setIdSpecies", DEFAULT_TO_ZERO,
                 NATURAL_PRIMARY_KEY));
         this.addColumnSpec(new IntegerColumnSpec("ID_NATURE_OBJECT", "getIdNatureObject", "setIdNatureObject", DEFAULT_TO_ZERO,
                 REQUIRED));
         this.addColumnSpec(new StringColumnSpec("SCIENTIFIC_NAME", "getScientificName", "setScientificName",
                 DEFAULT_TO_EMPTY_STRING, REQUIRED));
         this.addColumnSpec(new ShortColumnSpec("VALID_NAME", "getValidName", "setValidName", null, REQUIRED));
         this.addColumnSpec(new IntegerColumnSpec("ID_SPECIES_LINK", "getIdSpeciesLink", "setIdSpeciesLink", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("TYPE_RELATED_SPECIES", "getTypeRelatedSpecies", "setTypeRelatedSpecies",
                 DEFAULT_TO_NULL));
         this.addColumnSpec(new ShortColumnSpec("TEMPORARY_SELECT", "getTemporarySelect", "setTemporarySelect", null));
         this.addColumnSpec(new StringColumnSpec("SPECIES_MAP", "getSpeciesMap", "setSpeciesMap", DEFAULT_TO_NULL));
         this.addColumnSpec(new IntegerColumnSpec("ID_GROUP_SPECIES", "getIdGroupspecies", "setIdGroupspecies", DEFAULT_TO_ZERO,
                 REQUIRED));
         this.addColumnSpec(new StringColumnSpec("ID_TAXONOMY", "getIdTaxcode", "setIdTaxcode", DEFAULT_TO_NULL));
 
         // Joined tables
         OuterJoinTable groupSpecies;
 
         groupSpecies = new OuterJoinTable("CHM62EDT_GROUP_SPECIES B", "ID_GROUP_SPECIES", "ID_GROUP_SPECIES");
         groupSpecies.addJoinColumn(new StringJoinColumn("COMMON_NAME", "commonName", "setCommonName"));
         this.addJoinTable(groupSpecies);
 
         OuterJoinTable taxCodeFamily;
 
         taxCodeFamily = new OuterJoinTable("CHM62EDT_TAXONOMY C", "ID_TAXONOMY", "ID_TAXONOMY");
         taxCodeFamily.addJoinColumn(new StringJoinColumn("NAME", "taxonomyName", "setTaxonomyName"));
         taxCodeFamily.addJoinColumn(new StringJoinColumn("LEVEL", "taxonomyLevel", "setTaxonomyLevel"));
         taxCodeFamily.addJoinColumn(new StringJoinColumn("TAXONOMY_TREE", "taxonomyTree", "setTaxonomyTree"));
         taxCodeFamily.addJoinColumn(new StringJoinColumn("NAME", "taxonomicNameFamily", "setTaxonomicNameFamily"));
         taxCodeFamily.addJoinColumn(new StringJoinColumn("NAME", "taxonomicNameOrder", "setTaxonomicNameOrder"));
         this.addJoinTable(taxCodeFamily);
     }
 
     private List<ScientificNamePersist> getList() throws CriteriaMissingException {
         if (searchVernacular) {
             String query =
                     "" + " SELECT A.ID_SPECIES," + " A.ID_NATURE_OBJECT," + " A.SCIENTIFIC_NAME AS scientificName,"
                             + " A.VALID_NAME AS validName," + " A.ID_SPECIES_LINK," + " A.TYPE_RELATED_SPECIES,"
                             + " A.TEMPORARY_SELECT," + " A.SPECIES_MAP," + " A.ID_GROUP_SPECIES," + " A.ID_TAXONOMY,"
                             + " B.COMMON_NAME AS commonName," + " C.NAME AS taxonomyName," + " C.LEVEL, " + " C.TAXONOMY_TREE, "
                             + " C.NAME AS taxonomicNameFamily, " + " C.NAME AS taxonomicNameOrder"
                             + " FROM CHM62EDT_SPECIES A LEFT"
                             + " OUTER JOIN CHM62EDT_GROUP_SPECIES B ON A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY LEFT"
                             + " OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY" + " WHERE <where>" + " UNION "
                             + " SELECT A.ID_SPECIES," + " A.ID_NATURE_OBJECT," + " A.SCIENTIFIC_NAME AS scientificName,"
                             + " A.VALID_NAME AS validName," + " A.ID_SPECIES_LINK," + " A.TYPE_RELATED_SPECIES,"
                             + " A.TEMPORARY_SELECT," + " A.SPECIES_MAP," + " A.ID_GROUP_SPECIES," + " A.ID_TAXONOMY,"
                             + " B.COMMON_NAME AS commonName," + " C.NAME AS taxonomyName," + " C.LEVEL," + " C.TAXONOMY_TREE, "
                             + " C.NAME AS taxonomicNameFamily, " + " C.NAME AS taxonomicNameOrder" + " FROM CHM62EDT_REPORTS G"
                             + " INNER JOIN CHM62EDT_REPORT_TYPE F ON G.ID_REPORT_TYPE=F.ID_REPORT_TYPE"
                             + " INNER JOIN CHM62EDT_SPECIES A ON G.ID_NATURE_OBJECT=A.ID_NATURE_OBJECT"
                             + " LEFT OUTER JOIN CHM62EDT_GROUP_SPECIES B ON A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY"
                             + " INNER JOIN CHM62EDT_REPORT_ATTRIBUTES I ON G.ID_REPORT_ATTRIBUTES=I.ID_REPORT_ATTRIBUTES"
                             + " WHERE <whereVern>" + " AND F.LOOKUP_TYPE = 'LANGUAGE' AND I.NAME ='VERNACULAR_NAME'";
 
             results = getVeracularList(query);
         } else {
             if (searchCriteria.length < 1) {
                 throw new CriteriaMissingException("Unable to search because no search criteria was specified...");
             }
             results = getVeracularList(null);
         }
         return results;
     }
 
     public List<ScientificNamePersist> getResults(int offsetStart, int pageSize, AbstractSortCriteria[] sortCriteria)
             throws CriteriaMissingException {
 
         int asc = 1;
 
         for (AbstractSortCriteria aCriteria : sortCriteria) {
             NameSortCriteria criteria = (NameSortCriteria) aCriteria;
 
             asc = criteria.getAscendency();
         }
         if (sortCriteria != null && sortCriteria.length > 0) {
             if (asc == 2) {
                 Collections.sort(results, Collections.reverseOrder());
             } else {
                 Collections.sort(results);
             }
         }
         if (results != null && results.size() > pageSize) {
             int offsetEnd = offsetStart + pageSize;
 
             if (offsetEnd >= results.size()) {
                 offsetEnd = results.size();
             }
             results = results.subList(offsetStart, offsetEnd);
         }
         // _resultCount = new Long( -1 );// After each query, reset the _resultCount, so countResults do correct numbering.
         return results;
     }
 
     private List<ScientificNamePersist> getVeracularList(String query) {
         List<ScientificNamePersist> ret = new ArrayList<ScientificNamePersist>();
 
         try {
             if (query != null && query.length() > 0) {
                 String sql = query;
 
                 sql = sql.replaceAll("<where>", _prepareWhereSearch(3).toString());
                 sql = sql.replaceAll("<whereVern>", _prepareWhereSearchVernacular(3).toString());
                 List<ScientificNamePersist> list1 = this.findCustom(sql);
 
                 ret.addAll(list1);
 
                 sql = query;
                 sql = sql.replaceAll("<where>", _prepareWhereSearch(4).toString());
                 sql = sql.replaceAll("<whereVern>", _prepareWhereSearchVernacular(4).toString());
                 List<ScientificNamePersist> list2 = this.findCustom(sql);
 
                 ret.addAll(list2);
             } else {
                 StringBuffer filterSQL = _prepareWhereSearch(3);
                 List<ScientificNamePersist> list1 = this.findWhere(filterSQL.toString());
 
                 ret.addAll(list1);
 
                 filterSQL = _prepareWhereSearch(4);
                 List<ScientificNamePersist> list2 = this.findWhere(filterSQL.toString());
 
                 ret.addAll(list2);
             }
 
             List<ScientificNamePersist> list_all = new ArrayList<ScientificNamePersist>();
 
             if (speciesName != null && speciesName.length() > 1) {
                 String three = speciesName.substring(0, 3);
 
                list_all = this.findWhere("A.SCIENTIFIC_NAME LIKE '%" + three + "%'");
                 List<ScientificNamePersist>[] array = new ArrayList[10];
 
                 for (ScientificNamePersist specie : list_all) {
                     String name = specie.getScientificName();
 
                     if (speciesName != null) {
                         int diff = StringUtils.getLevenshteinDistance(speciesName, name);
 
                         if (diff < 10) {
                             List<ScientificNamePersist> list = array[diff];
 
                             if (list == null) {
                                 list = new ArrayList<ScientificNamePersist>();
                             }
                             list.add(specie);
                             array[diff] = list;
                         }
                     }
                 }
 
                 for (int i = 0; i < 10; i++) {
                     List<ScientificNamePersist> list = array[i];
 
                     if (list != null) {
                         ret.addAll(list);
                     }
                 }
             }
             ret = removeDuplicates(ret);
             cnt = ret.size();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ret;
     }
 
     private List<ScientificNamePersist> removeDuplicates(List<ScientificNamePersist> list) {
         List<ScientificNamePersist> ret = new ArrayList<ScientificNamePersist>();
         List<Integer> ids = new ArrayList<Integer>();
 
         for (ScientificNamePersist specie : list) {
             Integer id = specie.getIdSpecies();
 
             if (!ids.contains(id)) {
                 ret.add(specie);
                 ids.add(id);
             }
         }
         return ret;
     }
 
     /**
      * This method is used to count the total list of results from a query. It is used to find all for use in pagination. Having the
      * total number of results and the results displayed per page, the you could find the number of pages i.e.
      * 
      * @return Number of results found
      */
     public Long countResults() throws CriteriaMissingException {
         _resultCount = new Long(cnt);
         return _resultCount;
     }
 
     /**
      * This helper method is used to construct the string after WHERE...based on search criterias set. In another words constructs
      * .....WHERE...>>B.ID_GEOSCOPE_LINK=XXX OR B.ID_GEOSCOPE_LINK=YYY OR B.ID_GEOSCOPE_LINK=ZZZ .....
      * 
      * @return SQL string
      * @throws ro.finsiel.eunis.exceptions.CriteriaMissingException
      *             If no search criteria search or wrong criteria is set.
      */
     private StringBuffer _prepareWhereSearch(Integer relation) throws CriteriaMissingException {
         StringBuffer filterSQL = new StringBuffer();
 
         if (searchCriteria.length <= 0) {
             throw new CriteriaMissingException("No criteria set for searching. Search interrupted.");
         }
         for (int i = 0; i < searchCriteria.length; i++) {
             NameSearchCriteria aCriteria = (NameSearchCriteria) searchCriteria[i];
 
             if (aCriteria.isMainCriteria()) {
                 filterSQL.append(Utilities.prepareSQLOperator("A.SCIENTIFIC_NAME", aCriteria.getScientificName(), relation));
                 speciesName = aCriteria.getScientificName();
             } else {
                 if (i > 0) {
                     filterSQL.append(" AND ");
                 }
                 // upcast
                 filterSQL.append(aCriteria.toSQL());
             }
         }
         // Here we've changed the way search is done as follows:
         // - if user checks 'Search in synonyms' then no additional condition is neccesary.
         // - if user unchecks 'Search in synonyms' then we add an additional condition:
         // "AND A.TYPE_RELATED_SPECIES='Species'" thus filtering and returning only 'species and NOT synonyms'.
         if (!searchSynnonyms) {
             filterSQL.append(" AND A.TYPE_RELATED_SPECIES='Species' ");
         }
         // The condition below is changed so that 'A.TYPE_RELATED_SPECIES' takes precedence for 'A.VALID_NAME'
         // - If an user is LOGGED we return also VALID_NAME == 0 species
         // - If an user is NOT LOGGED, we return also VALID_NAME == 0 species BUT ONLY
         // those who have "TYPE_RELATED_SPECIES='Syn'" ( synonyms).
         // filterSQL.append(Utilities.showEUNISInvalidatedSpecies("AND A.VALID_NAME", showEUNISInvalidatedSpecies));
         if (!showEUNISInvalidatedSpecies) {
             filterSQL.append(" AND ( ( A.VALID_NAME > 0 ) OR ( A.VALID_NAME = 0 AND A.TYPE_RELATED_SPECIES='Synonym' ) ) ");
         }
         return filterSQL;
     }
 
     private StringBuffer _prepareWhereSearchVernacular(Integer relation) throws CriteriaMissingException {
         StringBuffer filterSQL = new StringBuffer();
 
         if (searchCriteria.length <= 0) {
             throw new CriteriaMissingException("No criteria set for searching. Search interrupted.");
         }
         for (int i = 0; i < searchCriteria.length; i++) {
             NameSearchCriteria aCriteria = (NameSearchCriteria) searchCriteria[i];
 
             if (aCriteria.isMainCriteria()) {
                 filterSQL.append(Utilities.prepareSQLOperator("I.VALUE", aCriteria.getScientificName(), relation));
             } else {
                 if (i > 0) {
                     filterSQL.append(" AND ");
                 }
                 filterSQL.append(aCriteria.toSQL());
             }
         }
         if (!showEUNISInvalidatedSpecies) {
             filterSQL.append(" AND A.VALID_NAME > 0 ");
         }
         return filterSQL;
     }
 
 }
