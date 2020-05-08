 package org.atlasapi.query.v2;
 
 import java.io.IOException;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.beans.AtlasErrorSummary;
 import org.atlasapi.beans.AtlasModelWriter;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;
 
 @Controller
 public class ScheduleController extends BaseController {
     
     private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
     private final ApplicationConfigurationFetcher configFetcher;
     private final ScheduleResolver scheduleResolver;
     
     public ScheduleController(ScheduleResolver scheduleResolver, KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
         super(executor, configFetcher, log, outputter);
         this.scheduleResolver = scheduleResolver;
         this.configFetcher = configFetcher;
     }
 
     @RequestMapping("/3.0/schedule.*")
     public void schedule(@RequestParam(required=false) String to, @RequestParam(required=false) String from, @RequestParam(required=false) String on, @RequestParam String channel, @RequestParam String publisher, HttpServletRequest request, HttpServletResponse response) throws IOException {
         try {
             DateTime fromWhen = null;
             DateTime toWhen = null;
 
             if (! Strings.isNullOrEmpty(on)) {
                 fromWhen = dateTimeInQueryParser.parse(on);
                 toWhen = dateTimeInQueryParser.parse(on);
             } else if (! Strings.isNullOrEmpty(to) && ! Strings.isNullOrEmpty(from)) {
                 fromWhen = dateTimeInQueryParser.parse(from);
                 toWhen = dateTimeInQueryParser.parse(to);
             } else {
                 throw new IllegalArgumentException("You must pass either 'on' or 'from' and 'to'");
             }
             
             Set<Publisher> publishers = publishers(publisher, configFetcher.configurationFor(request));
             if (publishers.isEmpty()) {
                 throw new IllegalArgumentException("You must specify at least one publisher that you have permission to view");
             }
             
             Set<Channel> channels = channels(channel);
             if (channels.isEmpty()) {
                 throw new IllegalArgumentException("You must specify at least one channel that exists");
             }
             
             Schedule schedule = scheduleResolver.schedule(fromWhen, toWhen, channels, publishers);
             modelAndViewFor(request, response, schedule.scheduleChannels());
         } catch (Exception e) {
             errorViewFor(request, response, AtlasErrorSummary.forException(e));
         }
     }
     
     private Set<Channel> channels(String channelString) {
         ImmutableSet.Builder<Channel> channels = ImmutableSet.builder();
         for (String channelKey: URI_SPLITTER.split(channelString)) {
             Maybe<Channel> channel = Channel.fromKey(channelKey);
             if (channel.hasValue()) {
                 channels.add(channel.requireValue());
             }
         }
         return channels.build();
     }
     
     private Set<Publisher> publishers(String publisherString, Maybe<ApplicationConfiguration> config) {
        Set<Publisher> appPublishers = ImmutableSet.copyOf(config.hasValue() ? config.requireValue().publishersInOrder() : ApplicationConfiguration.DEFAULT_CONFIGURATION.publishersInOrder());
         if (Strings.isNullOrEmpty(publisherString)) {
             return appPublishers;
         }
         
         ImmutableSet.Builder<Publisher> publishers = ImmutableSet.builder();
         for (String publisherKey: URI_SPLITTER.split(publisherString)) {
             Maybe<Publisher> publisher = Publisher.fromKey(publisherKey);
             if (publisher.hasValue()) {
                 publishers.add(publisher.requireValue());
             }
         }
         
         return Sets.intersection(publishers.build(), appPublishers);
     }
 }
