 package org.pit.fetegeo.importer.objects;
 
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.pit.fetegeo.importer.processors.CleverWriter;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 4:03 PM
  */
 public abstract class GenericTag {
 
   private Long id;
   private String type;
   private List<Name> nameList;
  private Long postCodeId;
   private static final Map<String, Long> typeMap;
 
   static {
     typeMap = new HashMap<String, Long>();
   }
 
   public Long getId() {
     return id;
   }
 
   public void setId(Long id) {
     this.id = id;
   }
 
   public String getType() {
     return type;
   }
 
   public void setType(String type) {
     this.type = type;
   }
 
   List<Name> getNameList() {
     return nameList;
   }
 
   public void setNameList(List<Name> nameList) {
     this.nameList = nameList;
   }
 
  Long getPostCodeId() {
    return postCodeId;
  }

  public void setPostCodeId(Long postCodeId) {
    this.postCodeId = postCodeId;
  }

   public static Map<String, Long> getTypeMap() {
     return typeMap;
   }
 
   public abstract void write();
 
   void write(CleverWriter copyFileWriter) {
     copyFileWriter.writeField(this.getId());                  // write OSM_ID
     copyFileWriter.writeField(typeMap.get(this.getType()));   // write TYPE_ID
   }
 
 }
