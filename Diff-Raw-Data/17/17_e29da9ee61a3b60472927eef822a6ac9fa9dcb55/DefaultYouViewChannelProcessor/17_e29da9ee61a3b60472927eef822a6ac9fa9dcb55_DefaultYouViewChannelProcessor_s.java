 package org.atlasapi.remotesite.youview;
 
 import java.util.List;
 
 import nu.xom.Elements;
 
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
 import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
 import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.scheduling.UpdateProgress;
 
 public class DefaultYouViewChannelProcessor implements YouViewChannelProcessor {
 
     private final ScheduleWriter scheduleWriter;
     private final YouViewElementProcessor processor;
     private final BroadcastTrimmer trimmer;
     private final Logger log = LoggerFactory.getLogger(DefaultYouViewChannelProcessor.class);
     
     public DefaultYouViewChannelProcessor(ScheduleWriter scheduleWriter, YouViewElementProcessor processor, BroadcastTrimmer trimmer) {
         this.scheduleWriter = scheduleWriter;
         this.processor = processor;
         this.trimmer = trimmer;
     }
     
     @Override
     public UpdateProgress process(Channel channel, Elements elements, DateTime startTime) {
         
         List<ItemRefAndBroadcast> broadcasts = Lists.newArrayList();
         Builder<String, String> acceptableBroadcastIds = ImmutableMap.builder();
         
         UpdateProgress progress = UpdateProgress.START;
         for (int i = 0; i < elements.size(); i++) {
            ItemRefAndBroadcast itemAndBroadcast = processor.process(elements.get(i));
            if (itemAndBroadcast != null) {
                broadcasts.add(itemAndBroadcast);
                acceptableBroadcastIds.put(itemAndBroadcast.getBroadcast().getSourceId(),itemAndBroadcast.getItemUri());
                progress = progress.reduce(UpdateProgress.SUCCESS);
            } else {
                 progress = progress.reduce(UpdateProgress.FAILURE);
             }
         }
         if (trimmer != null) {
             trimmer.trimBroadcasts(new Interval(startTime, startTime.plusDays(1)), channel, acceptableBroadcastIds.build());
         }
         if (broadcasts.isEmpty()) {
             log.info(String.format("No broadcasts for channel %s (%s) on %s", channel.getTitle(), getYouViewId(channel), startTime.toString()));
         } else {
             try {
                 scheduleWriter.replaceScheduleBlock(Publisher.YOUVIEW, channel, broadcasts);
             } catch (IllegalArgumentException e) {
                 log.error(e.getMessage(), e);
             }
         }
         
         return progress;
     }
 
     private String getYouViewId(Channel channel) {
         for (String alias : channel.getAliasUrls()) {
             if (alias.contains("youview.com")) {
                 return alias;
             }
         }
         throw new RuntimeException("Channel " + channel.getTitle() + " does not have a YouView alias (" + channel.toString() + ")");
     }
 }
