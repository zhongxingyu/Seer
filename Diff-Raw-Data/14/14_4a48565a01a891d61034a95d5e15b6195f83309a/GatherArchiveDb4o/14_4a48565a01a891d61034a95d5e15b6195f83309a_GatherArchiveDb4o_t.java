 package org.gatherdata.archiver.dao.db4o.model;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.gatherdata.archiver.core.model.GatherArchive;
 import org.gatherdata.commons.db.db4o.model.UniqueEntityDb4o;
 import org.gatherdata.commons.model.UniqueEntitySupport;
 import org.joda.time.DateTime;
 
 public class GatherArchiveDb4o extends UniqueEntityDb4o implements GatherArchive {
 
     private Serializable content;
     private Map<String, String> metadata;
 
     public Serializable getContent() {
         return this.content;
     }
 
     public void setContent(Serializable content) {
         this.content = content;
     }
 
     public Map<String, String> getMetadata() {
         if (metadata == null) {
             metadata = new HashMap<String, String>();
         }
         return this.metadata;
     }
 
     public void copy(GatherArchive template) {
         if ((template != null) && (template != this)) {
             super.copy(template);
             setContent(template.getContent());
             getMetadata().clear();
             getMetadata().putAll(template.getMetadata());
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof GatherArchive))
             return false;
         GatherArchive rhs = (GatherArchive) obj;
         return support.equals(this, rhs);
     }
 
     @Override
     public int hashCode() {
         return support.hashCode(this);
     }
 
     @Override
     public String toString() {
        return "GatherArchive [uid=" + uidAsAscii + "]";
     }
 
     
 }
