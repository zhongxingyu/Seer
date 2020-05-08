 package org.atlasapi.query.v2;
 
 import static org.atlasapi.application.ApplicationConfiguration.defaultConfiguration;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule.ScheduleChannel;
 import org.atlasapi.output.AtlasErrorSummary;
 import org.atlasapi.output.AtlasModelWriter;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.ids.NumberToShortStringCodec;
 import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
 import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;
 
 @Controller
 public class ScheduleController extends BaseController<Iterable<ScheduleChannel>> {
     
     private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
     private final ScheduleResolver scheduleResolver;
 	private ChannelResolver channelResolver;
 	
     private final NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
     
     public ScheduleController(ScheduleResolver scheduleResolver, ChannelResolver channelResolver, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter<Iterable<ScheduleChannel>> outputter) {
         super(configFetcher, log, outputter);
         this.scheduleResolver = scheduleResolver;
         this.channelResolver = channelResolver;
     }
 
     @RequestMapping("/3.0/schedule.*")
     public void schedule(
             @RequestParam(required=false) String to, @RequestParam(required=false) String from, 
             @RequestParam(required=false) String on, 
             @RequestParam(required=false) String channel, @RequestParam(value="channel_id",required=false) String channelId, 
             @RequestParam(required=false) String publisher, 
             HttpServletRequest request, HttpServletResponse response) throws IOException {
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
             
             String apiKey = request.getParameter("apiKey");
             boolean apiKeySupplied = apiKey != null;
             Maybe<ApplicationConfiguration> requestedConfig = possibleAppConfig(request);
             if (apiKeySupplied && requestedConfig.isNothing()) {
                     throw new IllegalArgumentException("Unknown API key: " + apiKey);
             }
             ApplicationConfiguration appConfig = requestedConfig.valueOrDefault(ApplicationConfiguration.defaultConfiguration());
             
             boolean publishersSupplied = !Strings.isNullOrEmpty(publisher);
 
             Set<Publisher> publishers = getPublishers(publisher, appConfig);
             
             if (publishers.isEmpty()) {
                 throw new IllegalArgumentException("You must specify at least one publisher that you have permission to view");
             }
             
             if (Strings.isNullOrEmpty(channelId) && Strings.isNullOrEmpty(channel) || !Strings.isNullOrEmpty(channelId) && !Strings.isNullOrEmpty(channel)) {
                 throw new IllegalArgumentException("You must specify exactly one of channel and channel_id");
             }
             Set<Channel> channels = Strings.isNullOrEmpty(channel) ? channelsFromIds(channelId) : channelsFromKeys(channel);
             if (channels.isEmpty()) {
                 throw new IllegalArgumentException("You must specify at least one channel that exists using the channel or channel_id parameter");
             }
             
             ApplicationConfiguration mergeConfig = apiKeySupplied && !publishersSupplied ? appConfig : null;
             modelAndViewFor(request, response, scheduleResolver.schedule(fromWhen, toWhen, channels, publishers, mergeConfig).scheduleChannels(), appConfig);
         } catch (Exception e) {
             errorViewFor(request, response, AtlasErrorSummary.forException(e));
         }
     }
     
     private Set<Publisher> getPublishers(String publisher,
                                          ApplicationConfiguration appConfig) {
         if (!Strings.isNullOrEmpty(publisher)) {
             return Sets.intersection(publishersFrom(publisher), appConfig.getEnabledSources());
         }
        if (appConfig.precedenceEnabled()) {
            return ImmutableSet.of(appConfig.orderdPublishers().get(0));
        } 
        throw new IllegalArgumentException("Need precedence enabled");
     }
 
     private Set<Channel> channelsFromIds(String channelId) {
         return ImmutableSet.copyOf(Iterables.transform(Iterables.filter(Iterables.transform(URI_SPLITTER.split(channelId), new Function<String, Maybe<Channel>>() {
             @Override
             public Maybe<Channel> apply(String input) {
                 return channelResolver.fromId(idCodec.decode(input).longValue());
             }
         }),Maybe.HAS_VALUE),Maybe.<Channel>requireValueFunction()));
     }
 
     private Set<Channel> channelsFromKeys(String channelString) {
         ImmutableSet.Builder<Channel> channels = ImmutableSet.builder();
         for (String channelKey: URI_SPLITTER.split(channelString)) {
             Maybe<Channel> channel = channelResolver.fromKey(channelKey);
             if (channel.hasValue()) {
                 channels.add(channel.requireValue());
             }
         }
         return channels.build();
     }
 }
