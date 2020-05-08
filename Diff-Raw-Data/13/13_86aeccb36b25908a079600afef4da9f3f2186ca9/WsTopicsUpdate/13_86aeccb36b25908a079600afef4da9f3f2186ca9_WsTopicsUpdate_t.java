 package org.atlasapi.remotesite.worldservice;
 
 import static org.atlasapi.media.entity.Publisher.WORLD_SERVICE;
 import static org.atlasapi.persistence.content.ContentCategory.ITEMS;
 import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Topic;
 import org.atlasapi.media.entity.TopicRef;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.listing.ContentLister;
 import org.atlasapi.persistence.content.listing.ContentListingCriteria;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.atlasapi.remotesite.worldservice.model.WsTopics;
 import org.atlasapi.remotesite.worldservice.model.WsTopics.TopicWeighting;
 
import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.media.MimeType;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 
 public class WsTopicsUpdate extends ScheduledTask {
 
     private final WsTopicsClient topicsClient;
     private final TopicStore topicStore;
     private final ContentLister contentLister;
     private final ContentWriter contentWriter;
     private final AdapterLog log;
     
     private final ContentListingCriteria wsItemContentCriteria = defaultCriteria().forContent(ImmutableList.copyOf(ITEMS)).forPublisher(WORLD_SERVICE).build();
     private final String namespace = "dbpedia";
     
     public WsTopicsUpdate(WsTopicsClient topicsClient, TopicStore topicStore, ContentLister contentLister, ContentWriter contentWriter, AdapterLog log) {
         this.topicsClient = topicsClient;
         this.topicStore = topicStore;
         this.contentLister = contentLister;
         this.contentWriter = contentWriter;
         this.log = log;
     }
     
     @Override
     protected void runTask() {
         
         log.record(infoEntry().withSource(getClass()).withDescription("Starting WS Topics Update"));
         
         Maybe<Map<String, WsTopics>> possibleTopics = topicsClient.getLatestTopics();
         
         if(possibleTopics.isNothing()) {
             reportStatus("Got no topics");
             return;
         }
         
         Map<String, WsTopics> topics = possibleTopics.requireValue();
         
         log.record(infoEntry().withSource(getClass()).withDescription("Retrieved %s topic sets", topics.size()));
         reportStatus(String.format("%s topic sets", topics.size()));
         
         Iterator<Content> wsContent = contentLister.listContent(wsItemContentCriteria);
      
         int seen = 0;
         int updated = 0;
         while(wsContent.hasNext()) {
             Item content = (Item) wsContent.next();
            try {
                updated += updateTopicsFor(topics, content);
                reportStatus(String.format("%s topic sets. %s items seen, %s updated", topics.size(), ++seen, updated));
            } catch (Exception e) {
                log.record(errorEntry().withCause(e).withDescription("Error updating topics of %s",content.getCanonicalUri()).withSource(getClass()));
                throw Throwables.propagate(e);
            }
         }
         
         log.record(infoEntry().withSource(getClass()).withDescription("Updated topics of %s items", updated));
         reportStatus(String.format("Updated topics of %s items", updated));
     }
 
     public int updateTopicsFor(Map<String, WsTopics> topics, Item content) {
         for (Version version : content.getVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 if (MimeType.AUDIO_WAV.equals(encoding.getAudioCoding())) {
                     for (Location location : encoding.getAvailableAt()) {
                         Builder<TopicRef> topicRefs = ImmutableList.builder();
                         WsTopics itemTopics = topics.get(location.getUri());
                         if (itemTopics != null) {
                             for (TopicWeighting topicWeighting : itemTopics.getTopics()) {
                                 TopicRef topicRef = topicRefFor(topicWeighting);
                                 if(topicRef != null) {
                                     topicRefs.add(topicRef);
                                 }
                             }
                             content.setTopicRefs(topicRefs.build());
                             contentWriter.createOrUpdate(content);
                             return 1;
                         }
                     }
                 }
             }
         }
         return 0;
     }
 
     private TopicRef topicRefFor(TopicWeighting topicWeighting) {
         String value = topicWeighting.getTopicValue().replace("%28","(").replace("%29",")").replace("%27","'");
         Maybe<Topic> possibleTopic = topicStore.topicFor(namespace, value);
         if (possibleTopic.hasValue()) {
             Topic topic = possibleTopic.requireValue();
             topic.setPublisher(Publisher.DBPEDIA);
             topic.setTitle(value.substring(28).replace("_", " "));
             topic.setType(Topic.Type.SUBJECT);
             topicStore.write(topic);
             return new TopicRef(topic, topicWeighting.getWeighting(), false);
         }
         return null;
     }
 
 }
