 package ro.finsiel.eunis.jrfTables.species.names;
 
 
 import net.sf.jrf.domain.PersistentObject;
 
 import java.util.StringTokenizer;
 
 
 /**
  * @author finsiel
  * @version 1.2
  * @since 23.01.2003
  */
 public class ScientificNamePersist extends PersistentObject implements Comparable<ScientificNamePersist> {
 
     /** This is a database field. */
     private Integer i_idSpecies = null;
 
     /** This is a database field. */
     private Integer i_idNatureObject = null;
 
     /** This is a database field. */
     private String i_scientificName = null;
 
     /** This is a database field. */
     private Short i_validName = null;
 
     /** This is a database field. */
     private Integer i_idSpeciesLink = null;
 
     /** This is a database field. */
     private String i_typeRelatedSpecies = null;
 
     /** This is a database field. */
     private Short i_temporarySelect = null;
 
     /** This is a database field. */
     private String i_speciesMap = null;
 
     /** This is a database field. */
     private Integer i_idGroupspecies = null;
 
     /** This is a database field. */
     private String i_idTaxcode = null;
 
     /** This is a database field. */
     private String i_imagePath = null;
 
     /** This is a database field resulted from joins */
     private String commonName = null;
 
     /** This is a database field resulted from joins */
     private String taxonomicNameOrder = null;
 
     /** This is a database field resulted from joins */
     private String taxonomicNameFamily = null;
 
     /** This is a database field resulted from joins */
     private String taxonomyLevel = null;
     private String taxonomyTree = null;
     private String taxonomyName = null;
 
     /**
      * Normal constructor
      */
     public ScientificNamePersist() {
         super();
     }
  
     public int compareTo(ScientificNamePersist other) {
         return getScientificName().compareTo(other.getScientificName());
     }
  
     @Override
     public String toString() {
         return getScientificName();
     }
 
     /** Getter for a database field */
     public String getTaxonomicNameOrder() {
         String ret = "";
         String level = this.getTaxonomyLevel();
 
        if (level != null && level.equalsIgnoreCase("order_column")) {
             ret = this.getTaxonomyName();
         } else {
             String str = this.getTaxonomyTree();
 
             if (str != null) {
                 StringTokenizer st = new StringTokenizer(str, ",");
 
                 while (st.hasMoreTokens()) {
                     StringTokenizer sts = new StringTokenizer(st.nextToken(),
                             "*");
                     String classification_id = sts.nextToken();
                     String classification_level = sts.nextToken();
                     String classification_name = sts.nextToken();
 
                     if (classification_level != null
                             && classification_level.equalsIgnoreCase(
                                    "order_column")) {
                         ret = classification_name;
                         break;
                     }
                 }
             }
         }
         return ret;
     }
 
     /** Getter for a database field */
     public String getTaxonomicNameFamily() {
         String ret = "";
         String level = this.getTaxonomyLevel();
 
         if (level != null && level.equalsIgnoreCase("family")) {
             ret = this.getTaxonomyName();
         } else {
             String str = this.getTaxonomyTree();
 
             if (str != null) {
                 StringTokenizer st = new StringTokenizer(str, ",");
 
                 while (st.hasMoreTokens()) {
                     StringTokenizer sts = new StringTokenizer(st.nextToken(),
                             "*");
                     String classification_id = sts.nextToken();
                     String classification_level = sts.nextToken();
                     String classification_name = sts.nextToken();
 
                     if (classification_level != null
                             && classification_level.equalsIgnoreCase("family")) {
                         ret = classification_name;
                         break;
                     }
                 }
             }
         }
         return ret;
     }
 
     /** Getter for a database field */
     public String getCommonName() {
         return commonName;
     }
 
     /** Getter for a database field */
     public Integer getIdGroupspecies() {
         return i_idGroupspecies;
     }
 
     /** Getter for a database field */
     public Integer getIdNatureObject() {
         return i_idNatureObject;
     }
 
     /** Getter for a database field */
     public Integer getIdSpecies() {
         return i_idSpecies;
     }
 
     /** Getter for a database field */
     public Integer getIdSpeciesLink() {
         return i_idSpeciesLink;
     }
 
     /** Getter for a database field */
     public String getIdTaxcode() {
         return i_idTaxcode;
     }
 
     /** Getter for a database field */
     public String getImagePath() {
         return i_imagePath;
     }
 
     /** Getter for a database field */
     public String getScientificName() {
         return i_scientificName;
     }
 
     /** Getter for a database field */
     public String getSpeciesMap() {
         return i_speciesMap;
     }
 
     /** Getter for a database field */
     public Short getTemporarySelect() {
         return i_temporarySelect;
     }
 
     /** Getter for a database field */
     public String getTypeRelatedSpecies() {
         return i_typeRelatedSpecies;
     }
 
     /** Getter for a database field */
     public Short getValidName() {
         return i_validName;
     }
 
     /**
      * Setter for a database field
      * @param taxonomicNameOrder
      */
     public void setTaxonomicNameOrder(String taxonomicNameOrder) {
         this.taxonomicNameOrder = taxonomicNameOrder;
     }
 
     /**
      * Setter for a database field
      * @param taxonomicNameFamily
      */
     public void setTaxonomicNameFamily(String taxonomicNameFamily) {
         this.taxonomicNameFamily = taxonomicNameFamily;
     }
 
     /**
      * Setter for a database field
      * @param commonName
      */
     public void setCommonName(String commonName) {
         this.commonName = commonName;
     }
 
     /**
      * Setter for a database field.
      * @param idGroupspecies
      **/
     public void setIdGroupspecies(Integer idGroupspecies) {
         i_idGroupspecies = idGroupspecies;
     }
 
     /**
      * Setter for a database field.
      * @param idNatureObject
      **/
     public void setIdNatureObject(Integer idNatureObject) {
         i_idNatureObject = idNatureObject;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param idSpecies
      **/
     public void setIdSpecies(Integer idSpecies) {
         i_idSpecies = idSpecies;
         // Changing a primary key so we force this to new.
         this.forceNewPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param idSpeciesLink
      **/
     public void setIdSpeciesLink(Integer idSpeciesLink) {
         i_idSpeciesLink = idSpeciesLink;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param idTaxcode
      **/
     public void setIdTaxcode(String idTaxcode) {
         i_idTaxcode = idTaxcode;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param imagePath
      **/
     public void setImagePath(String imagePath) {
         i_imagePath = imagePath;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param scientificName
      **/
     public void setScientificName(String scientificName) {
         i_scientificName = scientificName;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param speciesMap
      **/
     public void setSpeciesMap(String speciesMap) {
         i_speciesMap = speciesMap;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param temporarySelect
      **/
     public void setTemporarySelect(Short temporarySelect) {
         i_temporarySelect = temporarySelect;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param typeRelatedSpecies
      **/
     public void setTypeRelatedSpecies(String typeRelatedSpecies) {
         i_typeRelatedSpecies = typeRelatedSpecies;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param validName
      **/
     public void setValidName(Short validName) {
         i_validName = validName;
         this.markModifiedPersistentState();
     }
 
     public String getTaxonomyTree() {
         return taxonomyTree;
     }
 
     public void setTaxonomyTree(String taxonomyTree) {
         this.taxonomyTree = taxonomyTree;
     }
 
     public String getTaxonomyLevel() {
         return taxonomyLevel;
     }
 
     public void setTaxonomyLevel(String taxonomyLevel) {
         this.taxonomyLevel = taxonomyLevel;
     }
 
     public String getTaxonomyName() {
         return taxonomyName;
     }
 
     public void setTaxonomyName(String taxonomyName) {
         this.taxonomyName = taxonomyName;
     }
 }
