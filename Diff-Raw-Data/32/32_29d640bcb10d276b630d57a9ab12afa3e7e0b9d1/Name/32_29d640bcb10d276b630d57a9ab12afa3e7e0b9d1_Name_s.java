 package org.pit.fetegeo.importer.objects;
 
 import org.pit.fetegeo.importer.processors.LanguageProcessor;
 
 import java.util.Map;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 2:19 PM
  */
 public class Name {
 
   private String nameType;
   private final String name;
   private Long languageId;
   private final boolean localised;
 
   public Name(String name, String nameType) {
     this.name = name;
     this.nameType = nameType;
 
     // Set language
     if (nameType.startsWith("name:")) {
      this.languageId = LanguageProcessor.findLanguageId(nameType.split(":")[1]);
       this.nameType = "name:";                                                     // we're using 'name:' as a type for localised names
       this.localised = true;
     } else {
       this.localised = false;
     }
 
     // Check if the type has been added already
     Map<String, Long> typeMap = GenericTag.getTypeMap();
     if (!typeMap.containsKey(this.nameType)) {
       typeMap.put(this.nameType, (long) typeMap.size());
     }
   }
 
   public String getNameType() {
     return nameType;
   }
 
   public String getName() {
     return name;
   }
 
   public Long getLanguageId() {
     return languageId;
   }
 
   public boolean isLocalised() {
     return localised;
   }
 
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("{ ");
     sb.append("name : ").append(name);
     sb.append("nameType : ").append(nameType);
     sb.append("languageId : ").append(languageId);
     sb.append("localised : ").append(localised);
     sb.append("}");
     return sb.toString();
   }
 }
