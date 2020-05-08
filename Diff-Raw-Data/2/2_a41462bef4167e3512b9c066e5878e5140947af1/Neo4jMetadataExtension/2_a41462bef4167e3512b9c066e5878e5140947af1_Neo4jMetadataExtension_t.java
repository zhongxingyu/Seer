 package com.zenika.dorm.core.dao.neo4j;
 
 import com.zenika.dorm.core.graph.impl.Usage;
 import com.zenika.dorm.core.model.DormMetadataExtension;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.Map;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 @XmlAccessorType(XmlAccessType.PROPERTY)
 @XmlRootElement
 public class Neo4jMetadataExtension extends Neo4jNode implements DormMetadataExtension {
 
     public static final Usage RELATIONSHIP_TYPE = Usage.create("EXTENSION");
 
     private String qualifier;
     private String extension;
 
     public Neo4jMetadataExtension() {
 
     }
 
     public Neo4jMetadataExtension(DormMetadataExtension extension) {
         this.extension = extension.getExtensionName();
         this.qualifier = extension.getQualifier();
     }
 
     @Override
     public String getQualifier() {
         return qualifier;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getExtensionName() {
         return extension;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setQualifier(String qualifier) {
         this.qualifier = qualifier;
     }
 
    public void setExtensionName(String extension) {
         this.extension = extension;
     }
 
     @Override
     public String toString() {
         return "Neo4jMetadataExtension{" +
                 "qualifier='" + qualifier + '\'' +
                 ", extension='" + extension + '\'' +
                 '}';
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Neo4jMetadataExtension)) return false;
 
         Neo4jMetadataExtension extension1 = (Neo4jMetadataExtension) o;
 
         if (extension != null ? !extension.equals(extension1.extension) : extension1.extension != null)
             return false;
         if (qualifier != null ? !qualifier.equals(extension1.qualifier) : extension1.qualifier != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = qualifier != null ? qualifier.hashCode() : 0;
         result = 31 * result + (extension != null ? extension.hashCode() : 0);
         return result;
     }
 
     @JsonIgnore
     @Override
     public void setProperties() {
 //        this.setExtension(getResponse().getData().getExtension());
 //        this.setQualifier(getResponse().getData().getQualifier());
     }
 
     @Override
     public DormMetadataExtension createFromMap(Map<String, String> properties) {
         throw new UnsupportedOperationException();
     }
 }
