 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.equiv;
 
 import static com.google.common.base.Predicates.in;
 import static com.google.common.base.Predicates.not;
 import static org.atlasapi.equiv.generators.AliasResolvingEquivalenceGenerator.aliasResolvingGenerator;
 import static org.atlasapi.media.entity.Publisher.AMAZON_UK;
 import static org.atlasapi.media.entity.Publisher.BBC;
 import static org.atlasapi.media.entity.Publisher.BBC_MUSIC;
 import static org.atlasapi.media.entity.Publisher.BBC_REDUX;
 import static org.atlasapi.media.entity.Publisher.FACEBOOK;
 import static org.atlasapi.media.entity.Publisher.ITUNES;
 import static org.atlasapi.media.entity.Publisher.LOVEFILM;
 import static org.atlasapi.media.entity.Publisher.NETFLIX;
 import static org.atlasapi.media.entity.Publisher.PA;
 import static org.atlasapi.media.entity.Publisher.PREVIEW_NETWORKS;
 import static org.atlasapi.media.entity.Publisher.RADIO_TIMES;
 import static org.atlasapi.media.entity.Publisher.RDIO;
 import static org.atlasapi.media.entity.Publisher.SOUNDCLOUD;
 import static org.atlasapi.media.entity.Publisher.SPOTIFY;
 import static org.atlasapi.media.entity.Publisher.TALK_TALK;
 import static org.atlasapi.media.entity.Publisher.YOUTUBE;
 import static org.atlasapi.media.entity.Publisher.YOUVIEW;
 import static org.atlasapi.persistence.lookup.TransitiveLookupWriter.generatedTransitiveLookupWriter;
 
 import java.io.File;
 import java.util.Set;
 
 import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ContainerCandidatesContainerEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ContainerCandidatesItemEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
 import org.atlasapi.equiv.generators.EquivalenceGenerator;
 import org.atlasapi.equiv.generators.FilmEquivalenceGenerator;
 import org.atlasapi.equiv.generators.RadioTimesFilmEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ScalingEquivalenceGenerator;
 import org.atlasapi.equiv.generators.SongTitleTransform;
 import org.atlasapi.equiv.generators.TitleSearchGenerator;
 import org.atlasapi.equiv.handlers.BroadcastingEquivalenceResultHandler;
 import org.atlasapi.equiv.handlers.EpisodeFilteringEquivalenceResultHandler;
 import org.atlasapi.equiv.handlers.EpisodeMatchingEquivalenceHandler;
 import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
 import org.atlasapi.equiv.handlers.EquivalenceSummaryWritingHandler;
 import org.atlasapi.equiv.handlers.LookupWritingEquivalenceHandler;
 import org.atlasapi.equiv.handlers.ResultWritingEquivalenceHandler;
 import org.atlasapi.equiv.results.combining.RequiredScoreFilteringCombiner;
 import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
 import org.atlasapi.equiv.results.extractors.MusicEquivalenceExtractor;
 import org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor;
 import org.atlasapi.equiv.results.filters.AlwaysTrueFilter;
 import org.atlasapi.equiv.results.filters.ConjunctiveFilter;
 import org.atlasapi.equiv.results.filters.ContainerHierarchyFilter;
 import org.atlasapi.equiv.results.filters.EquivalenceFilter;
 import org.atlasapi.equiv.results.filters.MediaTypeFilter;
 import org.atlasapi.equiv.results.filters.MinimumScoreFilter;
 import org.atlasapi.equiv.results.filters.PublisherFilter;
 import org.atlasapi.equiv.results.filters.SpecializationFilter;
 import org.atlasapi.equiv.results.persistence.FileEquivalenceResultStore;
 import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
 import org.atlasapi.equiv.results.scores.Score;
 import org.atlasapi.equiv.scorers.BroadcastItemTitleScorer;
 import org.atlasapi.equiv.scorers.ContainerHierarchyMatchingScorer;
 import org.atlasapi.equiv.scorers.CrewMemberScorer;
 import org.atlasapi.equiv.scorers.EquivalenceScorer;
 import org.atlasapi.equiv.scorers.SequenceContainerScorer;
 import org.atlasapi.equiv.scorers.SequenceItemScorer;
 import org.atlasapi.equiv.scorers.SeriesSequenceItemScorer;
 import org.atlasapi.equiv.scorers.SongCrewMemberExtractor;
 import org.atlasapi.equiv.scorers.TitleMatchingContainerScorer;
 import org.atlasapi.equiv.scorers.TitleMatchingItemScorer;
 import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
 import org.atlasapi.equiv.update.EquivalenceUpdater;
 import org.atlasapi.equiv.update.EquivalenceUpdaters;
 import org.atlasapi.equiv.update.NullEquivalenceUpdater;
 import org.atlasapi.equiv.update.SourceSpecificEquivalenceUpdater;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Song;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.SearchResolver;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 @Configuration
 public class EquivModule {
 
 	private @Value("${equiv.results.directory}") String equivResultsDirectory;
     
     private @Autowired ScheduleResolver scheduleResolver;
     private @Autowired SearchResolver searchResolver;
     private @Autowired ContentResolver contentResolver;
     private @Autowired ChannelResolver channelResolver;
     private @Autowired EquivalenceSummaryStore equivSummaryStore;
     private @Autowired LookupEntryStore lookupStore;
 
     public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
         return new RecentEquivalenceResultStore(new FileEquivalenceResultStore(new File(equivResultsDirectory)));
     }
 
     private EquivalenceResultHandler<Container> containerResultHandlers(Iterable<Publisher> publishers) {
         return new BroadcastingEquivalenceResultHandler<Container>(ImmutableList.of(
             new LookupWritingEquivalenceHandler<Container>(generatedTransitiveLookupWriter(lookupStore), publishers),
             new EpisodeMatchingEquivalenceHandler(contentResolver, equivSummaryStore, generatedTransitiveLookupWriter(lookupStore), publishers),
             new ResultWritingEquivalenceHandler<Container>(equivalenceResultStore()),
             new EquivalenceSummaryWritingHandler<Container>(equivSummaryStore)
         ));
     }
     
     private <T extends Content> EquivalenceFilter<T> standardFilter() {
         return standardFilter(ImmutableList.<EquivalenceFilter<T>>of());
     }
 
     private <T extends Content> EquivalenceFilter<T> standardFilter(Iterable<EquivalenceFilter<T>> additional) {
         return ConjunctiveFilter.valueOf(Iterables.concat(ImmutableList.of(
             new MinimumScoreFilter<T>(0.2),
             new MediaTypeFilter<T>(),
             new SpecializationFilter<T>(),
             new PublisherFilter<T>()
         ), additional));
     }
     
     private EquivalenceUpdater<Item> standardItemUpdater(Set<Publisher> acceptablePublishers, Set<? extends EquivalenceScorer<Item>> scorers) {
         return ContentEquivalenceUpdater.<Item> builder()
             .withGenerators(ImmutableSet.<EquivalenceGenerator<Item>> of(
                 new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, 
                     channelResolver, acceptablePublishers, Duration.standardMinutes(10))
             ))
             .withScorers(scorers)
             .withCombiner(new NullScoreAwareAveragingCombiner<Item>())
             .withFilter(this.<Item>standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Item> moreThanPercent(90))
             .withHandler(new BroadcastingEquivalenceResultHandler<Item>(ImmutableList.of(
                 EpisodeFilteringEquivalenceResultHandler.relaxed(
                     new LookupWritingEquivalenceHandler<Item>(generatedTransitiveLookupWriter(lookupStore), acceptablePublishers),
                     equivSummaryStore
                 ),
                 new ResultWritingEquivalenceHandler<Item>(equivalenceResultStore()),
                 new EquivalenceSummaryWritingHandler<Item>(equivSummaryStore)
             )))
             .build();
     }
     
     private EquivalenceUpdater<Container> topLevelContainerUpdater(Set<Publisher> publishers) {
         return ContentEquivalenceUpdater.<Container> builder()
             .withGenerators(ImmutableSet.of(
                 TitleSearchGenerator.create(searchResolver, Container.class, publishers),
                 ScalingEquivalenceGenerator.scale(
                     new ContainerChildEquivalenceGenerator(contentResolver, equivSummaryStore),
                     20)
                 ))
             .withScorers(ImmutableSet.<EquivalenceScorer<Container>> of(
                 new TitleMatchingContainerScorer()
             ))
             .withCombiner(new RequiredScoreFilteringCombiner<Container>(
                 new NullScoreAwareAveragingCombiner<Container>(),
                 ContainerChildEquivalenceGenerator.NAME
             ))
             .withFilter(this.<Container>standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Container>moreThanPercent(90))
             .withHandler(containerResultHandlers(publishers))
             .build();
     }
 
     @Bean 
     public EquivalenceUpdater<Content> contentUpdater() {
         
         Set<Publisher> musicPublishers = ImmutableSet.of(BBC_MUSIC, YOUTUBE, 
             SPOTIFY, SOUNDCLOUD, RDIO, AMAZON_UK);
         
         //Generally acceptable publishers.
         Set<Publisher> acceptablePublishers = Sets.difference(
             Publisher.all(), 
             Sets.union(ImmutableSet.of(PREVIEW_NETWORKS, BBC_REDUX, RADIO_TIMES, LOVEFILM, NETFLIX, YOUVIEW), musicPublishers)
         );
         
         EquivalenceUpdater<Item> standardItemUpdater = standardItemUpdater(acceptablePublishers, 
                 ImmutableSet.of(new TitleMatchingItemScorer(), new SequenceItemScorer()));
         EquivalenceUpdater<Container> topLevelContainerUpdater = topLevelContainerUpdater(acceptablePublishers);
 
         Set<Publisher> nonStandardPublishers = Sets.union(ImmutableSet.of(ITUNES, BBC_REDUX, RADIO_TIMES, FACEBOOK, LOVEFILM, NETFLIX, YOUVIEW, TALK_TALK, PA), musicPublishers);
         final EquivalenceUpdaters updaters = new EquivalenceUpdaters();
         for (Publisher publisher : Iterables.filter(Publisher.all(), not(in(nonStandardPublishers)))) {
             updaters.register(publisher, SourceSpecificEquivalenceUpdater.builder(publisher)
                 .withItemUpdater(standardItemUpdater)
                 .withTopLevelContainerUpdater(topLevelContainerUpdater)
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
         }
         
         Set<Publisher> paPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(YOUVIEW));
         
         updaters.register(PA, SourceSpecificEquivalenceUpdater.builder(PA)
                .withItemUpdater(standardItemUpdater(paPublishers))
                 .withTopLevelContainerUpdater(topLevelContainerUpdater(paPublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
         
         updaters.register(RADIO_TIMES, SourceSpecificEquivalenceUpdater.builder(RADIO_TIMES)
                 .withItemUpdater(rtItemEquivalenceUpdater())
                 .withTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
         
         Set<Publisher> youViewPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(YOUVIEW));
         updaters.register(YOUVIEW, SourceSpecificEquivalenceUpdater.builder(YOUVIEW)
                 .withItemUpdater(broadcastItemEquivalenceUpdater(youViewPublishers, Score.negativeOne()))
                 .withTopLevelContainerUpdater(broadcastItemContainerEquivalenceUpdater(youViewPublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
 
         Set<Publisher> reduxPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(BBC_REDUX));
 
         updaters.register(BBC_REDUX, SourceSpecificEquivalenceUpdater.builder(BBC_REDUX)
                 .withItemUpdater(broadcastItemEquivalenceUpdater(reduxPublishers, Score.nullScore()))
                 .withTopLevelContainerUpdater(broadcastItemContainerEquivalenceUpdater(reduxPublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
         
         Set<Publisher> facebookAcceptablePublishers = Sets.union(acceptablePublishers, ImmutableSet.of(FACEBOOK));
         updaters.register(FACEBOOK, SourceSpecificEquivalenceUpdater.builder(FACEBOOK)
                 .withItemUpdater(NullEquivalenceUpdater.<Item>get())
                 .withTopLevelContainerUpdater( facebookContainerEquivalenceUpdater(facebookAcceptablePublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
 
         updaters.register(ITUNES, SourceSpecificEquivalenceUpdater.builder(ITUNES)
                 .withItemUpdater(vodItemUpdater(acceptablePublishers).build())
                 .withTopLevelContainerUpdater(vodContainerUpdater(acceptablePublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
 
         Set<Publisher> lfPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(LOVEFILM));
         updaters.register(LOVEFILM, SourceSpecificEquivalenceUpdater.builder(LOVEFILM)
                 .withItemUpdater(vodItemUpdater(lfPublishers)
                         .withScorer(new SeriesSequenceItemScorer()).build())
                 .withTopLevelContainerUpdater(vodContainerUpdater(lfPublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
         
         Set<Publisher> netflixPublishers = ImmutableSet.of(BBC, NETFLIX);
         updaters.register(NETFLIX, SourceSpecificEquivalenceUpdater.builder(NETFLIX)
                 .withItemUpdater(vodItemUpdater(netflixPublishers).build())
                 .withTopLevelContainerUpdater(vodContainerUpdater(netflixPublishers))
                 .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                 .build());
 
         updaters.register(TALK_TALK, SourceSpecificEquivalenceUpdater.builder(TALK_TALK)
                 .withItemUpdater(vodItemUpdater(acceptablePublishers).build())
                 .withTopLevelContainerUpdater(vodContainerUpdater(acceptablePublishers))
                 .withNonTopLevelContainerUpdater(ContentEquivalenceUpdater.<Container>builder()
                     .withGenerator(new ContainerCandidatesContainerEquivalenceGenerator(contentResolver, equivSummaryStore))
                     .withScorer(new SequenceContainerScorer())
                     .withCombiner(new NullScoreAwareAveragingCombiner<Container>())
                     .withFilter(this.<Container>standardFilter(ImmutableList.<EquivalenceFilter<Container>>of(
                         new ContainerHierarchyFilter()
                     )))
                     .withExtractor(PercentThresholdEquivalenceExtractor.<Container> moreThanPercent(90))
                     .withHandler(new BroadcastingEquivalenceResultHandler<Container>(ImmutableList.of(
                         new LookupWritingEquivalenceHandler<Container>(generatedTransitiveLookupWriter(lookupStore), acceptablePublishers),
                         new ResultWritingEquivalenceHandler<Container>(equivalenceResultStore()),
                         new EquivalenceSummaryWritingHandler<Container>(equivSummaryStore)
                     )))
                     .build())
                 .build());
         
         Set<Publisher> itunesAndMusicPublishers = Sets.union(musicPublishers, ImmutableSet.of(ITUNES));
         ContentEquivalenceUpdater<Item> muiscPublisherUpdater = ContentEquivalenceUpdater.<Item>builder()
             .withGenerator(
                 new TitleSearchGenerator<Item>(searchResolver, Song.class, itunesAndMusicPublishers, new SongTitleTransform(), 100)
             ).withScorer(new CrewMemberScorer(new SongCrewMemberExtractor()))
             .withCombiner(new NullScoreAwareAveragingCombiner<Item>())
             .withFilter(AlwaysTrueFilter.<Item>get())
             .withExtractor(new MusicEquivalenceExtractor())
             .withHandler((EquivalenceResultHandler<Item>) new BroadcastingEquivalenceResultHandler<Item>(ImmutableList.of(
                 EpisodeFilteringEquivalenceResultHandler.relaxed(
                     new LookupWritingEquivalenceHandler<Item>(generatedTransitiveLookupWriter(lookupStore), itunesAndMusicPublishers),
                     equivSummaryStore
                 ),
                 new ResultWritingEquivalenceHandler<Item>(equivalenceResultStore()),
                 new EquivalenceSummaryWritingHandler<Item>(equivSummaryStore)
             )))
             .build();
         
         for (Publisher publisher : musicPublishers) {
             updaters.register(publisher, SourceSpecificEquivalenceUpdater.builder(publisher)
                     .withItemUpdater(muiscPublisherUpdater)
                     .withTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                     .withNonTopLevelContainerUpdater(NullEquivalenceUpdater.<Container>get())
                     .build());
         }
         
         return updaters; 
     }
 
     private EquivalenceUpdater<Container> facebookContainerEquivalenceUpdater(Set<Publisher> facebookAcceptablePublishers) {
         return ContentEquivalenceUpdater.<Container> builder()
             .withGenerators(ImmutableSet.of(
                 TitleSearchGenerator.create(searchResolver, Container.class, facebookAcceptablePublishers),
                 aliasResolvingGenerator(contentResolver, Container.class)
             ))
             .withScorers(ImmutableSet.<EquivalenceScorer<Container>> of())
             .withCombiner(NullScoreAwareAveragingCombiner.<Container> get())
             .withFilter(ConjunctiveFilter.valueOf(ImmutableList.of(
                 new MinimumScoreFilter<Container>(0.2),
                 new SpecializationFilter<Container>()
             )))
             .withExtractor(PercentThresholdEquivalenceExtractor.<Container> moreThanPercent(90))
             .withHandler(containerResultHandlers(facebookAcceptablePublishers))
             .build();
     }
 
     private EquivalenceUpdater<Container> vodContainerUpdater(Set<Publisher> acceptablePublishers) {
         return ContentEquivalenceUpdater.<Container> builder()
             .withGenerator(
                 TitleSearchGenerator.create(searchResolver, Container.class, acceptablePublishers)
             )
             .withScorers(ImmutableSet.of(
                 new TitleMatchingContainerScorer(),
                 new ContainerHierarchyMatchingScorer(contentResolver)
             ))
             .withCombiner(new RequiredScoreFilteringCombiner<Container>(
                 new NullScoreAwareAveragingCombiner<Container>(),
                 TitleMatchingContainerScorer.NAME)
             )
             .withFilter(this.<Container> standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Container> moreThanPercent(90))
             .withHandler(containerResultHandlers(acceptablePublishers))
             .build();
     }
 
     private ContentEquivalenceUpdater.Builder<Item> vodItemUpdater(Set<Publisher> acceptablePublishers) {
         return ContentEquivalenceUpdater.<Item> builder()
             .withGenerators(ImmutableSet.of(
                 new ContainerCandidatesItemEquivalenceGenerator(contentResolver, equivSummaryStore),
                 new FilmEquivalenceGenerator(searchResolver, acceptablePublishers, true)
             ))
             .withScorers(ImmutableSet.of(
                 new TitleMatchingItemScorer(),
                 new SequenceItemScorer()
             ))
             .withCombiner(new RequiredScoreFilteringCombiner<Item>(
                 new NullScoreAwareAveragingCombiner<Item>(),
                 TitleMatchingItemScorer.NAME
             ))
             .withFilter(this.<Item>standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Item> moreThanPercent(90))
             .withHandler(new BroadcastingEquivalenceResultHandler<Item>(ImmutableList.of(
                 EpisodeFilteringEquivalenceResultHandler.strict(
                     new LookupWritingEquivalenceHandler<Item>(
                         generatedTransitiveLookupWriter(lookupStore), acceptablePublishers),
                     equivSummaryStore
                 ),
                 new ResultWritingEquivalenceHandler<Item>(equivalenceResultStore()),
                 new EquivalenceSummaryWritingHandler<Item>(equivSummaryStore)
             )));
     }
 
     private EquivalenceUpdater<Container> broadcastItemContainerEquivalenceUpdater(Set<Publisher> sources) {
         return ContentEquivalenceUpdater.<Container> builder()
             .withGenerators(ImmutableSet.of(
                 TitleSearchGenerator.create(searchResolver, Container.class, sources),
                 new ContainerChildEquivalenceGenerator(contentResolver, equivSummaryStore)
             ))
             .withScorers(ImmutableSet.of(new TitleMatchingContainerScorer()))
             .withCombiner(new RequiredScoreFilteringCombiner<Container>(
                 new NullScoreAwareAveragingCombiner<Container>(),
                 ContainerChildEquivalenceGenerator.NAME))
             .withFilter(this.<Container>standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Container> moreThanPercent(90))
             .withHandler(containerResultHandlers(sources))
             .build();
     }
 
     private EquivalenceUpdater<Item> broadcastItemEquivalenceUpdater(Set<Publisher> sources, Score titleMismatch) {
         return standardItemUpdater(sources, ImmutableSet.of(
             new TitleMatchingItemScorer(), 
             new SequenceItemScorer(), 
             new BroadcastItemTitleScorer(contentResolver, titleMismatch)
         ));
     }
 
     private EquivalenceUpdater<Item> rtItemEquivalenceUpdater() {
         return ContentEquivalenceUpdater.<Item> builder()
             .withGenerators(ImmutableSet.of(
                 new RadioTimesFilmEquivalenceGenerator(contentResolver),
                 new FilmEquivalenceGenerator(searchResolver, ImmutableSet.of(Publisher.PREVIEW_NETWORKS), false)
             ))
             .withScorers(ImmutableSet.<EquivalenceScorer<Item>> of())
             .withCombiner(new NullScoreAwareAveragingCombiner<Item>())
             .withFilter(this.<Item>standardFilter())
             .withExtractor(PercentThresholdEquivalenceExtractor.<Item> moreThanPercent(90))
             .withHandler(new BroadcastingEquivalenceResultHandler<Item>(ImmutableList.of(
                 EpisodeFilteringEquivalenceResultHandler.relaxed(
                     new LookupWritingEquivalenceHandler<Item>(
                         generatedTransitiveLookupWriter(lookupStore), 
                         ImmutableSet.of(RADIO_TIMES, PA, PREVIEW_NETWORKS)),
                         equivSummaryStore
                 ),
                 new ResultWritingEquivalenceHandler<Item>(equivalenceResultStore()),
                 new EquivalenceSummaryWritingHandler<Item>(equivSummaryStore)
             )))
             .build();
     }
 
 }
