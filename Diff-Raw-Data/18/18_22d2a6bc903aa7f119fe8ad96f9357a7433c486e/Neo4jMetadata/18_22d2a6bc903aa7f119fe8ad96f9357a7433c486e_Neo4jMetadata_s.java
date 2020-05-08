 package com.zenika.dorm.core.dao.neo4j;
 
 import com.zenika.dorm.core.model.DormMetadata;
 import com.zenika.dorm.core.model.DormMetadataExtension;
 import com.zenika.dorm.core.model.impl.Usage;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 @XmlAccessorType(XmlAccessType.PROPERTY)
 @XmlRootElement
 public class Neo4jMetadata extends Neo4jNode implements DormMetadata {
 
     public static final Usage RELATIONSHIP_TYPE = Usage.create("METADATA");
 
     private Neo4jMetadataExtension extension;
     private DormMetadataExtension dormExtension;
 
     private String qualifier;
     private String version;
     private String type;
 
     public Neo4jMetadata() {
     }
 
     public Neo4jMetadata(DormMetadata metadata) {
         version = metadata.getVersion();
         type = metadata.getType();
         qualifier = convertFullqualifier(metadata.getQualifier());
         extension = new Neo4jMetadataExtension(metadata.getExtension());
     }
 
     public static URI generateIndexURI(String fullQualifier, Neo4jIndex index) throws URISyntaxException {
         String template = index.getTemplate();
         template = template.replace("{key}", "qualifier");
         template = template.replace("{value}", convertFullqualifier(fullQualifier));
         return new URI(template);
     }
 
     @XmlElement
     @Override
     public String getQualifier() {
         return qualifier;
     }
 
     public String getVersion() {
         return version;
     }
 
     @Override
     public String getType() {
         return type;
     }
 
     @JsonIgnore
     public Neo4jMetadataExtension getNeo4jExtension() {
         return extension;
     }
 
     @JsonIgnore
     @Override
     public DormMetadataExtension getExtension(){
         throw new UnsupportedOperationException();
     }
 
 
     public void setQualifier(String qualifier) {
         this.qualifier = qualifier;
     }
 
     @JsonIgnore
     public void setExtension(Neo4jMetadataExtension extension) {
         this.extension = extension;
     }
 
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     @Override
     public String toString() {
         return "Neo4jMetadata{" +
                 "extension=" + extension +
                 ", qualifier='" + qualifier + '\'' +
                 ", version='" + version + '\'' +
                 '}';
     }
 
     @JsonIgnore
     @Override
     public void setProperties() {
 //        this.setFullQualifier(getResponse().getData().getFullQualifier());
 //        this.setQualifier(getResponse().getData().getQualifier());
 //        this.setVersion(getResponse().getData().getVersion());
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Neo4jMetadata metadata = (Neo4jMetadata) o;
 
         if (qualifier != null ? !qualifier.equals(metadata.qualifier) : metadata.qualifier != null)
             return false;
         if (version != null ? !version.equals(metadata.version) : metadata.version != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = qualifier != null ? qualifier.hashCode() : 0;
         result = 31 * result + (version != null ? version.hashCode() : 0);
         return result;
     }
 
     public static String convertFullqualifier(String str) {
         return str.replace("/", ";");
     }
 }
