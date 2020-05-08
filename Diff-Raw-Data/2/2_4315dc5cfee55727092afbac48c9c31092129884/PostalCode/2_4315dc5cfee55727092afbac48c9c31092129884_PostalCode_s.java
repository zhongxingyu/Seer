 package org.pit.fetegeo.importer.objects;
 
 import org.pit.fetegeo.importer.processors.CleverWriter;
 import org.pit.fetegeo.importer.processors.LocationProcessor;
 
 /**
  * Author: Pit Apps
  * Date: 10/31/12
  * Time: 5:09 PM
  */
 public class PostalCode extends GenericTag {
 
   private static Long postCodeId = -1l;
   private final String postCode;
 
   public PostalCode(String postCode) {
     this.postCode = postCode;
     postCodeId++;
   }
 
   public Long getPostCodeId() {
     return postCodeId;
   }
 
   public void write(CleverWriter postCodeWriter) {
     postCodeWriter.writeField(postCodeId);
 
     super.write(postCodeWriter);
 
     postCodeWriter.writeField(LocationProcessor.findLocation(this)); // location
     postCodeWriter.writeField(postCode);                             // main
    postCodeWriter.writeField("");                                   // sup //TODO: IMPLEMENT THIS
     postCodeWriter.endRecord();
   }
 }
