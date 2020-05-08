 package org.atlasapi.media.entity.simple;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS, name="topics")
 @XmlType(name="topics", namespace=PLAY_SIMPLE_XML.NS)
 public class TopicQueryResult {
 
     private List<Topic> topics = Lists.newArrayList();
 
     public void add(Topic content) {
         topics.add(content);
     }
 
     @XmlElements({ 
        @XmlElement(name = "topic", type = Topic.class, namespace=PLAY_SIMPLE_XML.NS),
     })
     public List<Topic> getContents() {
         return topics;
     }
     
     public void setContents(Iterable<Topic> items) {
         this.topics = Lists.newArrayList(items);
     }
     
     public boolean isEmpty() {
         return topics.isEmpty();
     }
 
     @Override
     public int hashCode() {
         return topics.hashCode();
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (this instanceof TopicQueryResult) {
             TopicQueryResult other = (TopicQueryResult) obj;
             return topics.equals(other.topics);
         }
         return false;
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this).addValue(topics).toString();
     }
 }
