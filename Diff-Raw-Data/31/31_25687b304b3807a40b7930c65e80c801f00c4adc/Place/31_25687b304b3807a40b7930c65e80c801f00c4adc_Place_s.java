 package org.pit.fetegeo.importer.objects;
 
 import org.pit.fetegeo.importer.FetegeoImportTask;
 import org.pit.fetegeo.importer.processors.CleverWriter;
 import org.pit.fetegeo.importer.processors.HashMaker;
 import org.pit.fetegeo.importer.processors.LocationProcessor;
 
 import java.io.File;
 
 /**
  * Author: Pit Apps
  * Date: 10/25/12
  * Time: 5:37 PM
  */
 public class Place extends GenericTag {
 
   private Long population;
   private Long countryId;
   private Integer adminLevel;
   private static Long placeId = 0l;
   private static Long placeNameId = 0l;
 
   private static final CleverWriter placeWriter;
   private static final CleverWriter placeNameWriter;
 
   static {
     placeNameWriter = FetegeoImportTask.container.add(new CleverWriter(new File(Constants.OUT_PATH, "place_name.txt")));
     placeWriter = FetegeoImportTask.container.add(new CleverWriter(new File(Constants.OUT_PATH, "place.txt")));
   }
 
   private Long getPopulation() {
     return population;
   }
 
   public void setPopulation(Long population) {
     this.population = population;
   }
 
   private Long getCountryId() {
     return countryId;
   }
 
   public void setCountryId(Long countryId) {
     this.countryId = countryId;
   }
 
   public void setAdminLevel(Integer adminLevel) {
     this.adminLevel = adminLevel;
   }
 
   private Integer getAdminLevel() {
     return adminLevel;
   }
 
   public void write() {
     placeWriter.writeField(placeId);                                                 // place_id
     super.write(placeWriter);                                                        // OSM_ID, TYPE_ID
    placeWriter.writeField(this.getPostCodeId());                                    // postcode_id
     placeWriter.writeField(this.getCountryId());                                     // country_id
     placeWriter.writeField(LocationProcessor.findLocation());                        // location
     placeWriter.writeField(this.getAdminLevel());                                    // admin_level
     placeWriter.writeField(this.getPopulation());                                    // population
 
     for (Name name : this.getNameList()) {
       placeNameWriter.writeField(placeNameId++);                                          // place_name_id
       placeNameWriter.writeField(placeId);                                                // place_id
       placeNameWriter.writeField(name.getLanguageId());                                   // lang_id
       placeNameWriter.writeField(GenericTag.getTypeMap().get(name.getNameType()));        // type_id
       placeNameWriter.writeField(name.getName());                                         // name
       placeNameWriter.writeField(HashMaker.getMD5Hash(name.getName()));                   // name_hash
       placeNameWriter.endRecord();
     }
     placeId++;
     placeWriter.endRecord();
   }
 
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("{ ");
     sb.append("place_id : ").append(placeId);
     sb.append("place_name_id : ").append(placeNameId);
     sb.append("country_id : ").append(countryId);
     sb.append("population : ").append(population);
     sb.append("[ names: ");
     sb.append(this.getNameList().toString());
     sb.append("]}");
     return sb.toString();
   }
 
 }
