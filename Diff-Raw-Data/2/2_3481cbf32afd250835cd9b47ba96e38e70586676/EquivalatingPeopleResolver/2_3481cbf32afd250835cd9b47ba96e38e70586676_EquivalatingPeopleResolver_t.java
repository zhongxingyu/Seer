 package org.atlasapi.persistence.content.people;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.equiv.OutputContentMerger;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.PeopleQueryResolver;
 import org.atlasapi.persistence.content.PeopleResolver;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class EquivalatingPeopleResolver implements PeopleQueryResolver {
 
     private final PeopleResolver peopleResolver;
     private final LookupEntryStore peopleLookupEntryStore;
     private final OutputContentMerger outputContentMerger;
     
     public EquivalatingPeopleResolver(PeopleResolver peopleResolver, LookupEntryStore peopleLookupEntryStore) {
         this.peopleResolver = peopleResolver;
         this.peopleLookupEntryStore = peopleLookupEntryStore;
         this.outputContentMerger = new OutputContentMerger();
     }
     
     @Override
     public Optional<Person> person(String uri, ApplicationConfiguration configuration) {
         Iterable<LookupEntry> entriesForIdentifiers = peopleLookupEntryStore.entriesForIdentifiers(ImmutableList.of(uri), true);
         if(Iterables.isEmpty(entriesForIdentifiers)) {
             return Optional.<Person>absent();
         }
         
        return Optional.fromNullable(Iterables.getFirst(outputContentMerger.merge(configuration, resolvePeople(configuration, entriesForIdentifiers)), null));
     }
 
     @Override
     public Optional<Person> person(Long id, ApplicationConfiguration configuration) {
         Iterable<LookupEntry> entriesForIdentifiers = peopleLookupEntryStore.entriesForIds(ImmutableList.of(id));
         if(Iterables.isEmpty(entriesForIdentifiers)) {
             return Optional.<Person>absent();
         }
         
         return Optional.fromNullable(Iterables.getFirst(outputContentMerger.merge(configuration, resolvePeople(configuration, entriesForIdentifiers)), null));
     }
     
     private List<Person> resolvePeople(final ApplicationConfiguration configuration, Iterable<LookupEntry> lookupEntries) {
         
         ImmutableMap<String, LookupEntry> lookup = Maps.uniqueIndex(Iterables.filter(lookupEntries, new Predicate<LookupEntry>() {
 
             @Override
             public boolean apply(LookupEntry input) {
                 return configuration.isEnabled(input.lookupRef().publisher());
             }
         }), LookupEntry.TO_ID);
 
         Map<String, Set<LookupRef>> lookupRefs = Maps.transformValues(lookup, LookupEntry.TO_EQUIVS);
 
         Iterable<LookupRef> filteredRefs = Iterables.filter(Iterables.concat(lookupRefs.values()), enabledPublishers(configuration));
 
         if (Iterables.isEmpty(filteredRefs)) {
             return ImmutableList.of();
         }
         
 
         return setEquivalentToFields(ImmutableList.copyOf(peopleResolver.people(filteredRefs)));
         
     }
     
     private List<Person> setEquivalentToFields(List<Person> resolvedResults) {
         Map<Described, LookupRef> equivRefs = Maps.newHashMap();
         for (Identified ided : resolvedResults) {
             if (ided instanceof Described) {
                 Described described = (Described) ided;
                 equivRefs.put(described, LookupRef.from(described));
             }
         }
         Set<LookupRef> lookupRefs = ImmutableSet.copyOf(equivRefs.values());
         for (Entry<Described, LookupRef> equivRef : equivRefs.entrySet()) {
             equivRef.getKey().setEquivalentTo(Sets.difference(lookupRefs, ImmutableSet.of(equivRef.getValue())));
         }
         return resolvedResults;
     }
     
     private Predicate<LookupRef> enabledPublishers(ApplicationConfiguration config) {
         final Set<Publisher> enabledPublishers = config.getEnabledSources();
         return new Predicate<LookupRef>() {
 
             @Override
             public boolean apply(LookupRef input) {
                 return enabledPublishers.contains(input.publisher());
             }
         };
     }
 
 }
