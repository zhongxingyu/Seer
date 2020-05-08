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
 
 import static org.atlasapi.equiv.generators.ScalingEquivalenceGenerator.scale;
 import static org.atlasapi.equiv.results.EquivalenceResultBuilder.resultBuilder;
 import static org.atlasapi.equiv.results.extractors.FilteringEquivalenceExtractor.filteringExtractor;
 import static org.atlasapi.equiv.update.ResultWritingEquivalenceUpdater.resultWriter;
 
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
 import org.atlasapi.equiv.generators.FilmEquivalenceGenerator;
 import org.atlasapi.equiv.generators.ItemBasedContainerEquivalenceGenerator;
 import org.atlasapi.equiv.generators.SequenceItemEquivalenceScorer;
 import org.atlasapi.equiv.generators.TitleMatchingContainerEquivalenceGenerator;
 import org.atlasapi.equiv.generators.TitleMatchingItemEquivalenceScorer;
 import org.atlasapi.equiv.results.EquivalenceResultBuilder;
 import org.atlasapi.equiv.results.ScoredEquivalent;
 import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
 import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
 import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
 import org.atlasapi.equiv.results.extractors.EquivalenceFilter;
 import org.atlasapi.equiv.results.extractors.MinimumScoreEquivalenceExtractor;
 import org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor;
 import org.atlasapi.equiv.results.extractors.PublisherFilteringExtractor;
 import org.atlasapi.equiv.results.persistence.MongoEquivalenceResultStore;
 import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
 import org.atlasapi.equiv.results.probe.EquivalenceProbeStore;
 import org.atlasapi.equiv.results.probe.EquivalenceResultProbeController;
 import org.atlasapi.equiv.results.probe.MongoEquivalenceProbeStore;
 import org.atlasapi.equiv.results.www.EquivalenceResultController;
 import org.atlasapi.equiv.results.www.RecentResultController;
 import org.atlasapi.equiv.update.BasicEquivalenceUpdater;
 import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
 import org.atlasapi.equiv.update.LookupWritingEquivalenceUpdater;
 import org.atlasapi.equiv.update.RootEquivalenceUpdater;
 import org.atlasapi.equiv.update.tasks.ContentEquivalenceUpdateTask;
 import org.atlasapi.equiv.update.tasks.FilmEquivalenceUpdateTask;
 import org.atlasapi.equiv.update.tasks.FilteringContentLister;
 import org.atlasapi.equiv.update.www.ContentEquivalenceUpdateController;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.SearchResolver;
 import org.atlasapi.persistence.content.listing.ContentLister;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.lookup.LookupWriter;
 import org.atlasapi.persistence.lookup.TransitiveLookupWriter;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.joda.time.Duration;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.scheduling.RepetitionRule;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.time.DayOfWeek;
 
 @Configuration
 public class EquivModule {
 
 	private @Autowired DatabasedMongo db;
 	private @Value("${equiv.updater.enabled}") String updaterEnabled;
 
 	private static final RepetitionRule EQUIVALENCE_REPETITION = RepetitionRules.weekly(DayOfWeek.MONDAY, new LocalTime(9, 00));
     
     private @Autowired ScheduleResolver scheduleResolver;
     private @Autowired SearchResolver searchResolver;
     private @Autowired ContentLister contentLister;
     private @Autowired ContentResolver contentResolver;
     private @Autowired DatabasedMongo mongo;
     private @Autowired AdapterLog log;
     private @Autowired SimpleScheduler taskScheduler;
 
     public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
         return new RecentEquivalenceResultStore(new MongoEquivalenceResultStore(db));
     }
     
     public @Bean ContentEquivalenceUpdater<Item> itemUpdater() {
         Set<ContentEquivalenceGenerator<Item>> itemGenerators = ImmutableSet.<ContentEquivalenceGenerator<Item>>of(
                 new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(10)),
                 new TitleMatchingItemEquivalenceScorer(),
                 new SequenceItemEquivalenceScorer()
         );
         EquivalenceResultBuilder<Item> resultBuilder = standardResultBuilder();
         
         ContentEquivalenceUpdater<Item> itemUpdater = new BasicEquivalenceUpdater<Item>(itemGenerators, resultBuilder, log);
         itemUpdater = resultWriter(itemUpdater, equivalenceResultStore());
         itemUpdater = new LookupWritingEquivalenceUpdater<Item>(itemUpdater, lookupWriter());
         return itemUpdater;
     }
 
     private <T extends Content> EquivalenceResultBuilder<T> standardResultBuilder() {
         EquivalenceCombiner<T> combiner = new NullScoreAwareAveragingCombiner<T>();
         
         EquivalenceExtractor<T> extractor = PercentThresholdEquivalenceExtractor.<T> fromPercent(90);
         extractor = filteringExtractor(extractor, new EquivalenceFilter<T>() {
             @Override
             public boolean apply(ScoredEquivalent<T> input, T target) {
                 return input.score().isRealScore() && input.score().asDouble() > 0.2;
             }
         });
         extractor = filteringExtractor(extractor, new EquivalenceFilter<T>() {
             @Override
             public boolean apply(ScoredEquivalent<T> input, T target) {
                 T equivalent = input.equivalent();
                 return (equivalent.getSpecialization() == null || target.getSpecialization() == null || Objects.equal(equivalent.getSpecialization(), target.getSpecialization())) 
                     && (equivalent.getMediaType() == null || target.getMediaType() == null || Objects.equal(equivalent.getMediaType(), target.getMediaType()));
             }
         });
         extractor = new PublisherFilteringExtractor<T>(extractor);
         
         return resultBuilder(combiner, MinimumScoreEquivalenceExtractor.minimumFrom(extractor, 0.2));
     }
     
     public @Bean ContentEquivalenceUpdater<Container<?>> containerUpdater() {
         Set<ContentEquivalenceGenerator<Container<?>>> containerGenerators = ImmutableSet.<ContentEquivalenceGenerator<Container<?>>>of(
                 scale(new ItemBasedContainerEquivalenceGenerator(itemUpdater(), contentResolver), new Function<Double, Double>() {
                     @Override
                     public Double apply(Double input) {
                         return Math.min(1, input * 20);
                     }
                 }), 
                 scale(new TitleMatchingContainerEquivalenceGenerator(searchResolver), new Function<Double, Double>() {
                     @Override
                     public Double apply(Double input) {
                         return input > 0 ? input / 2 : input;
                     }
                 })
         );
         EquivalenceResultBuilder<Container<?>> resultBuilder = standardResultBuilder();
         
         ContentEquivalenceUpdater<Container<?>> containerUpdater = new BasicEquivalenceUpdater<Container<?>>(containerGenerators, resultBuilder, log);
         containerUpdater = resultWriter(containerUpdater, equivalenceResultStore());
         containerUpdater = new LookupWritingEquivalenceUpdater<Container<?>>(containerUpdater, lookupWriter());
         
         return containerUpdater;
     }
 
     public @Bean ContentEquivalenceUpdater<Content> contentUpdater() {
         return new RootEquivalenceUpdater(containerUpdater(), itemUpdater());
     }
     
     public @Bean LookupWriter lookupWriter() {
         return new TransitiveLookupWriter(new MongoLookupEntryStore(db));
     }
     
     private ContentEquivalenceUpdateTask publisherUpdateTask(final Publisher publisher) {
         ContentLister filteringLister = new FilteringContentLister(contentLister, new Predicate<Content>() {
             @Override
             public boolean apply(Content input) {
                if(Publisher.PA.equals(input.getPublisher())) {
                     return !(input instanceof Film); 
                 }
                return publisher.equals(input.getPublisher());
             }
         });
        return new ContentEquivalenceUpdateTask(filteringLister, contentUpdater(), log, db).withSchedulingKey(publisher.key()+"-equivalence");
     }
     
     public @Bean FilmEquivalenceUpdateTask filmUpdateTask() {
         EquivalenceResultBuilder<Film> standardResultBuilder = standardResultBuilder();
         Set<ContentEquivalenceGenerator<Film>> generators = ImmutableSet.<ContentEquivalenceGenerator<Film>>of(
 //                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(1)),
                 new FilmEquivalenceGenerator(searchResolver)
         );
         ContentEquivalenceUpdater<Film> basicUpdater = new BasicEquivalenceUpdater<Film>(generators, standardResultBuilder, log);
         ContentEquivalenceUpdater<Film> updater = new LookupWritingEquivalenceUpdater<Film>(resultWriter(basicUpdater , equivalenceResultStore()), lookupWriter());
         return new FilmEquivalenceUpdateTask(contentLister, updater, log, db);
     }
     
     @PostConstruct
     public void scheduleUpdater() {
         if(Boolean.parseBoolean(updaterEnabled)) {
             taskScheduler.schedule(publisherUpdateTask(Publisher.PA).withName("PA Equivalence Updater"), EQUIVALENCE_REPETITION);
             taskScheduler.schedule(publisherUpdateTask(Publisher.BBC).withName("BBC Equivalence Updater"), EQUIVALENCE_REPETITION);
             taskScheduler.schedule(publisherUpdateTask(Publisher.C4).withName("C4 Equivalence Updater"), EQUIVALENCE_REPETITION);
             taskScheduler.schedule(filmUpdateTask().withName("Film Equivalence Updater"), EQUIVALENCE_REPETITION);
             taskScheduler.schedule(new ChildRefUpdateTask(contentLister, mongo).withName("Child Ref Update"), RepetitionRules.NEVER);
         }
     }
     
     //Controllers...
     public @Bean ContentEquivalenceUpdateController updateController() {
         return new ContentEquivalenceUpdateController(contentUpdater(), contentResolver);
     }
     
     public @Bean EquivalenceResultController resultController() {
         return new EquivalenceResultController(equivalenceResultStore(), equivProbeStore(), contentResolver);
     }
     
     public @Bean RecentResultController recentController() {
         return new RecentResultController(equivalenceResultStore());
     }
     
     //Probes...
     public @Bean EquivalenceProbeStore equivProbeStore() { 
         return new MongoEquivalenceProbeStore(db);
     }
     
     public @Bean EquivalenceResultProbeController equivProbeController() {
         return new EquivalenceResultProbeController(equivalenceResultStore(), equivProbeStore());
     }
 
 }
