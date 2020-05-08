 package org.atlasapi.remotesite.voila;
 
 import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
 import static org.atlasapi.remotesite.redux.UpdateProgress.FAILURE;
 import static org.atlasapi.remotesite.redux.UpdateProgress.SUCCESS;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Topic;
 import org.atlasapi.media.entity.TopicRef;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
 import org.atlasapi.remotesite.redux.UpdateProgress;
 import org.atlasapi.remotesite.voila.ContentWords.ContentWordsList;
 import org.atlasapi.remotesite.voila.ContentWords.WordWeighting;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 
 public class ContentTwitterTopicsUpdater {
 
     private static final String TWITTER_NS = "twitter";
     private final ContentResolver contentResolver;
    private final TopicStore topicStore;
    private final TopicQueryResolver topicResolver;
     private final ContentWriter contentWriter;
     private final CannonTwitterTopicsClient cannonTopicsClient;
     private final AdapterLog log;
 
    public ContentTwitterTopicsUpdater(CannonTwitterTopicsClient cannonTopicsClient, ContentResolver contentResolver, TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, AdapterLog log) {
         this.cannonTopicsClient = cannonTopicsClient;
         this.contentResolver = contentResolver;
         this.topicStore = topicStore;
        this.topicResolver = topicResolver;
         this.contentWriter = contentWriter;
         this.log = log;
     }
 
     public UpdateProgress updateTopics(List<String> contentIds) {
         
         Optional<ContentWordsList> possibleContentWords = cannonTopicsClient.getContentWordsForIds(contentIds);
         
         if (!possibleContentWords.isPresent()) {
             return new UpdateProgress(0, contentIds.size());
         }
 
         ContentWordsList contentWords = possibleContentWords.get();
         
         ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(urisForWords(contentWords));
         
         UpdateProgress result = UpdateProgress.START;
         for (ContentWords contentWordSet : contentWords) {
             try {
                 Maybe<Identified> possibleContent = resolvedContent.get(contentWordSet.getUri());
                 if(possibleContent.hasValue()) {
                     Content content = (Content) possibleContent.requireValue();
                     content.setTopicRefs(getTopicRefsFor(contentWordSet).addAll(filter(content.getTopicRefs())).build());
                     write(content);
                     result = result.reduce(SUCCESS);
                 } else {
                     log.record(warnEntry().withSource(getClass()).withDescription("Couldn't resolve content for %s", contentWordSet.getUri()));
                     result = result.reduce(FAILURE);
                 }
             } catch (Exception e) {
                 log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to update topics for %s", contentWordSet.getUri()));
                 result = result.reduce(FAILURE);
             }
         }
         
         return result;
     }
 
     private Iterable<? extends TopicRef> filter(List<TopicRef> topicRefs) {
         return Iterables.filter(topicRefs, new Predicate<TopicRef>() {
             @Override
             public boolean apply(TopicRef input) {
                Maybe<Topic> possibleTopic = topicResolver.topicForId(input.getTopic());
                 if (possibleTopic.hasValue()) {
                     Topic topic = possibleTopic.requireValue();
                     return !(TWITTER_NS.equals(topic.getNamespace()) && Publisher.METABROADCAST.equals(topic.getPublisher()));
                 }
                 return false;
             }
         });
     }
 
     public void write(Content content) {
         if (content instanceof Container) {
             contentWriter.createOrUpdate((Container) content);
         } else {
             contentWriter.createOrUpdate((Item) content);
         }
     }
 
     public Builder<TopicRef> getTopicRefsFor(ContentWords contentWordSet) {
         Builder<TopicRef> topicRefs = ImmutableSet.builder();
         for (WordWeighting wordWeighting : ImmutableSet.copyOf(contentWordSet.getWords())) {
             Maybe<Topic> possibleTopic = topicStore.topicFor(TWITTER_NS, wordWeighting.getContent());
             if (possibleTopic.hasValue()) {
                 Topic topic = possibleTopic.requireValue();
                 topic.setPublisher(Publisher.METABROADCAST);
                 topic.setType(Topic.Type.SUBJECT);
                 topicStore.write(topic);
                 topicRefs.add(new TopicRef(topic, wordWeighting.getWeight()/100.0f, false));
             }
         }
         return topicRefs;
     }
 
     public Iterable<String> urisForWords(ContentWordsList contentWords) {
         return ImmutableSet.copyOf(Iterables.transform(contentWords.getResults(), new Function<ContentWords, String>() {
             @Override
             public String apply(ContentWords input) {
                 return input.getUri();
             }
         }));
     }
 
 }
