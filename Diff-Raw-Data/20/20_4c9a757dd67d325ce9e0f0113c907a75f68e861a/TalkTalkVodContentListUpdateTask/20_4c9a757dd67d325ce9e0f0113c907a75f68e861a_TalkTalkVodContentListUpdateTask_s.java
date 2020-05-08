 package org.atlasapi.remotesite.talktalk;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkVodListCallback;
 import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
 import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.scheduling.UpdateProgress;
 
 /**
  * {@link ScheduledTask} which retrieves the TV Structure via the provided
  * {@link TalkTalkClient} and processes each {@link ChannelType} in turn using
  * the provided {@link TalkTalkChannelProcessor}.
  */
 public class TalkTalkVodContentListUpdateTask extends ScheduledTask {
 
     private final String TALK_TALK_URI_PART = "http://talktalk.net/groups";
     
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final TalkTalkClient client;
     private final ContentGroupWriter groupWriter;
     private final ContentGroupResolver groupResolver;
     private final ContentResolver contentResolver;
 
     private final GroupType type;
     private final String identifier;
 
     private final TalkTalkUriCompiler uriCompiler = new TalkTalkUriCompiler();
     private final AtomicReference<UpdateProgress> progress
         = new AtomicReference<UpdateProgress>();
 
     public TalkTalkVodContentListUpdateTask(TalkTalkClient talkTalkClient,
             ContentGroupResolver resolver, ContentGroupWriter writer,
             ContentResolver contentResolver,
             GroupType type, String identifier) {
         this.client = checkNotNull(talkTalkClient);
         this.groupResolver = checkNotNull(resolver);
         this.groupWriter = checkNotNull(writer);
         this.contentResolver = checkNotNull(contentResolver);
         this.type = type;
         this.identifier = identifier;
     }
 
     @Override
     protected void runTask() {
         try {
             
             progress.set(UpdateProgress.START);
             ContentGroup group = resolveOrCreateContentGroup();
             List<ChildRef> refs = client.processVodList(type, identifier, new TalkTalkVodListCallback<List<ChildRef>>() {
 
                 private ImmutableList.Builder<ChildRef> refs = ImmutableList.builder();
 
                 @Override
                 public List<ChildRef> getResult() {
                     return refs.build();
                 }
 
                 @Override
                 public void process(VODEntityType entity) {
                     log.debug("processing entity {}", entity.getId());
                    String uri = uriCompiler.uriFor(entity);
                    Content content = resolve(uri);
                    if (content != null) {
                        refs.add(content.childRef());
                        progress.set(progress.get().reduce(UpdateProgress.SUCCESS));
                    } else {
                         progress.set(progress.get().reduce(UpdateProgress.FAILURE));
                     }
                    reportStatus(progress.toString());
                 }
             });
             
             group.setContents(refs);
             groupWriter.createOrUpdate(group);
             if (progress.get().hasFailures()) {
                 throw new RuntimeException(progress.get().toString());
             }
         } catch (TalkTalkException tte) {
             log.error("content update failed", tte);
             // ensure task is marked failed
             throw new RuntimeException(tte);
         }
     }
     
     private Content resolve(String uri) {
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(uri));
         Maybe<Identified> possibleContent = resolved.get(uri);
         if (possibleContent.hasValue() && possibleContent.requireValue() instanceof Content) {
             return (Content) possibleContent.requireValue();
         }
         return null;
     }
 
     private ContentGroup resolveOrCreateContentGroup() {
         String uri = String.format("%s/%s/%s", TALK_TALK_URI_PART, type.toString().toLowerCase(), identifier);
         ResolvedContent resolvedContent = groupResolver.findByCanonicalUris(ImmutableList.of(uri));
         Maybe<Identified> possibleGroup = resolvedContent.get(uri);
         return possibleGroup.hasValue() ? (ContentGroup) resolvedContent.get(uri).requireValue() 
                                         : new ContentGroup(uri, Publisher.TALK_TALK);
     }
     
 }
