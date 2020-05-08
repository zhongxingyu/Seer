 package org.atlasapi.media.entity.simple;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS, name="channels")
 @XmlType(name="channels", namespace=PLAY_SIMPLE_XML.NS)
 public class ChannelQueryResult {
 
    private List<Channel> channels = Lists.newArrayList();
 
     public ChannelQueryResult(Iterable<Channel> channels) {
         this.channels = ImmutableList.copyOf(channels);
     }
 
     public ChannelQueryResult() {
     }
     
     @XmlElements(@XmlElement(name = "channel", type=Channel.class, namespace=PLAY_SIMPLE_XML.NS))
     public List<Channel> getChannels() {
         return channels;
     }
 
     public void setChannels(Iterable<Channel> channels) {
         this.channels = ImmutableList.copyOf(channels);
     }
 
 }
