 package org.atlasapi.media.entity.simple;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 
@XmlRootElement(namespace = PLAY_SIMPLE_XML.NS, name = "contentGroups")
@XmlType(name = "contentGroups", namespace = PLAY_SIMPLE_XML.NS)
 public class ContentGroupQueryResult {
 
     private List<ContentGroup> groups = Lists.newArrayList();
 
     public void add(ContentGroup group) {
         groups.add(group);
     }
 
     @XmlElements(
     @XmlElement(name = "contentGroup", type = ContentGroup.class, namespace = PLAY_SIMPLE_XML.NS))
     public List<ContentGroup> getContentGroups() {
         return groups;
     }
 
     public void setContentGroups(Iterable<ContentGroup> contentGroups) {
         this.groups = Lists.newArrayList(contentGroups);
     }
 
     public boolean isEmpty() {
         return groups.isEmpty();
     }
 
     @Override
     public int hashCode() {
         return groups.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (this instanceof ContentGroupQueryResult) {
             ContentGroupQueryResult other = (ContentGroupQueryResult) obj;
             return groups.equals(other.groups);
         }
         return false;
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this).addValue(groups).toString();
     }
 }
