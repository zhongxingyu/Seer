 package info.mikaelsvensson.ftpbackup.model.impl;
 
 import info.mikaelsvensson.ftpbackup.model.LocalFolderDestination;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 import javax.xml.bind.annotation.XmlAttribute;
 
 public class LocalFolderDestinationImpl implements LocalFolderDestination {
     private String path;
     private String id;
 
     public LocalFolderDestinationImpl() {
     }
 
     public LocalFolderDestinationImpl(final String path) {
         this.path = path;
     }
 
     @Override
     @XmlAttribute(name = "path")
     public String getPathToBaseFromRoot() {
         return path;
     }
 
     @Override
     @XmlAttribute
     public String getId() {
         return id;
     }
 
    public void setPathToBaseFromRoot(final String path) {
         this.path = path;
     }
 
     @Override
     public void setId(String id) {
         this.id = id;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof LocalFolderDestinationImpl)) {
             return false;
         }
 
         LocalFolderDestinationImpl that = (LocalFolderDestinationImpl) o;
 
         EqualsBuilder equalsBuilder = new EqualsBuilder();
         equalsBuilder.append(id, that.id);
         equalsBuilder.append(path, that.path);
         return equalsBuilder.isEquals();
     }
 
     @Override
     public int hashCode() {
         HashCodeBuilder builder = new HashCodeBuilder();
         builder.append(path);
         builder.append(id);
         return builder.toHashCode();
     }
 }
