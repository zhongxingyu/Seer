 package org.corespring.rest;
 
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.corespring.resource.ContentCollection;
 import org.corespring.resource.Item;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * An {@link ItemQuery} is a query object which is passed to the CoreSpring API in order to return a subset of available
  * {@link Item}s.
  */
 public class ItemQuery implements Serializable {
 
   private static final long serialVersionUID = 1L;
 
   static final String BLOOMS_TAXONOMY_KEY = "bloomsTaxonomy";
  static final String CONTRIBUTORS_KEY = "contributor";
   static final String GRADE_LEVEL_KEY = "gradeLevel";
   static final String DEMONSTRATED_KNOWLEDGE_KEY = "demonstratedKnowledge";
   static final String ITEM_TYPE_KEY = "itemType";
   static final String KEY_SKILLS_KEY = "keySkills";
   static final String PRIMARY_SUBJECT_KEY = "primarySubject.category";
   static final String STANDARDS_KEY = "standards.dotNotation";
   static final String COLLECTIONS_KEY = "collectionId";
 
   private final String searchString;
   private final Collection<String> bloomsTaxonomies;
   private final Collection<String> contributors;
   private final Collection<String> demonstratedKnowledge;
   private final Collection<String> gradeLevels;
   private final Collection<String> itemTypes;
   private final Collection<String> keySkills;
   private final Collection<String> subjects;
   private final Collection<String> standards;
   private final Collection<String> collections;
 
   private final ObjectMapper objectMapper = new ObjectMapper();
 
   private ItemQuery(Builder builder) {
     this.searchString = builder.searchString;
     this.bloomsTaxonomies = builder.bloomsTaxonomies;
     this.contributors = builder.contributors;
     this.demonstratedKnowledge = builder.demonstratedKnowledge;
     this.gradeLevels = builder.gradeLevels;
     this.itemTypes = builder.itemTypes;
     this.keySkills = builder.keySkills;
     this.subjects = builder.subjects;
     this.standards = builder.standards;
     this.collections = builder.collections;
   }
 
   @Override
   public String toString() {
     try {
       List<String> clauses = new ArrayList<String>();
       addIfNotEmpty(clauses, searchString());
       addIfNotEmpty(clauses, asInJson(BLOOMS_TAXONOMY_KEY, this.bloomsTaxonomies));
       addIfNotEmpty(clauses, asInJson(CONTRIBUTORS_KEY, this.contributors));
       addIfNotEmpty(clauses, asInJson(GRADE_LEVEL_KEY, this.gradeLevels));
       addIfNotEmpty(clauses, asInJson(DEMONSTRATED_KNOWLEDGE_KEY, this.demonstratedKnowledge));
       addIfNotEmpty(clauses, asInJson(ITEM_TYPE_KEY, this.itemTypes));
       addIfNotEmpty(clauses, asInJson(KEY_SKILLS_KEY, this.keySkills));
       addIfNotEmpty(clauses, asInJson(PRIMARY_SUBJECT_KEY, this.subjects));
       addIfNotEmpty(clauses, asInJson(STANDARDS_KEY, this.standards));
       addIfNotEmpty(clauses, asInJson(COLLECTIONS_KEY, this.collections));
 
 
       StringBuilder stringBuilder = new StringBuilder("{");
       for (int i = 0; i < clauses.size(); i++) {
         stringBuilder.append(clauses.get(i));
         if (i != clauses.size() - 1) {
           stringBuilder.append(",");
         }
       }
 
       return stringBuilder.append("}").toString();
     } catch (JsonProcessingException e) {
       throw new RuntimeException(e);
     }
   }
 
   private String searchString() throws JsonProcessingException {
     if (searchString != null) {
       List<Map<String, Map<String, String>>> or = new ArrayList<Map<String, Map<String, String>>>() {
         {
           String[] fields = new String[]{"title", "standards.dotNotation", "copyrightOwner", "contributor", "author"};
 
           for (final String field : fields) {
             this.add(new HashMap<String, Map<String, String>>() {
               {
                 this.put(field, new HashMap<String, String>() {
                   {
                     this.put("$regex", "\\" + searchString);
                     this.put("$options", "i");
                   }
                 });
               }
             });
           }
         }
       };
       return new StringBuilder("\"$or\":").append(objectMapper.writeValueAsString(or)).toString();
     } else {
       return "";
     }
   }
 
   private void addIfNotEmpty(Collection<String> strings, String string) {
     if (!string.equals("")) {
       strings.add(string);
     }
   }
 
   /**
    * Converts Strings to a JSON representation of that collection embedded in object keyed by "$in".
    *
    * For example, given Strings "test1", "test2", the result would be "{\"$in\":[\"\test1",\"test2\"]}".
    */
   private String asInJson(String key, Collection<String> strings) throws JsonProcessingException {
     return strings.isEmpty() ? "" :
         new StringBuilder("\"").append(key).append("\":").append("{\"$in\":")
             .append(objectMapper.writeValueAsString(strings)).append("}").toString();
   }
 
   public static class Builder {
 
     private String searchString;
     private Collection<String> bloomsTaxonomies = new HashSet<String>();
     private Collection<String> contributors = new HashSet<String>();
     private Collection<String> demonstratedKnowledge = new HashSet<String>();
     private Collection<String> gradeLevels = new HashSet<String>();
     private Collection<String> itemTypes = new HashSet<String>();
     private Collection<String> keySkills = new HashSet<String>();
     private Collection<String> subjects = new HashSet<String>();
     private Collection<String> standards = new HashSet<String>();
     private Collection<String> collections = new HashSet<String>();
 
     public void Builder() {
     }
 
     public Builder searchString(String searchString) {
       this.searchString = searchString;
       return this;
     }
 
     public Builder bloomsTaxonomy(String bloomsTaxonomy) {
       this.bloomsTaxonomies.add(bloomsTaxonomy);
       return this;
     }
 
     public Builder contributor(String contributor) {
       this.contributors.add(contributor);
       return this;
     }
 
     public Builder demonstratedKnowledge(String demonstratedKnowledge) {
       this.demonstratedKnowledge.add(demonstratedKnowledge);
       return this;
     }
 
     public Builder gradeLevel(String gradeLevel) {
       this.gradeLevels.add(gradeLevel);
       return this;
     }
 
     public Builder itemType(String itemType) {
       this.itemTypes.add(itemType);
       return this;
     }
 
     public Builder keySkill(String keySkill) {
       this.keySkills.add(keySkill);
       return this;
     }
 
     public Builder subject(String subjectCategory) {
       this.subjects.add(subjectCategory);
       return this;
     }
 
     public Builder standard(String standard) {
       this.standards.add(standard);
       return this;
     }
 
     public Builder collection(String collectionId) {
       this.collections.add(collectionId);
       return this;
     }
 
     public Builder collection(ContentCollection collection) {
       this.collections.add(collection.getId());
       return this;
     }
 
     public ItemQuery build() {
       return new ItemQuery(this);
     }
 
   }
 
 }
