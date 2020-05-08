 package org.atlasapi.query.content;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Maps.EntryTransformer;
 import com.google.common.collect.Sets;
 
 public class LookupResolvingQueryExecutor implements KnownTypeQueryExecutor {
 
     private final KnownTypeContentResolver cassandraContentResolver;
     private final KnownTypeContentResolver mongoContentResolver;
     private final LookupEntryStore mongoLookupResolver;
 
     public LookupResolvingQueryExecutor(KnownTypeContentResolver cassandraContentResolver, KnownTypeContentResolver mongoContentResolver, LookupEntryStore mongoLookupResolver) {
         this.cassandraContentResolver = cassandraContentResolver;
         this.mongoContentResolver = mongoContentResolver;
         this.mongoLookupResolver = mongoLookupResolver;
     }
 
     @Override
     public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        Map<String, List<Identified>> results = resolveMongoEntries(query, mongoLookupResolver.entriesForUris(uris));
         if (results.isEmpty()) {
             results = resolveCassandraEntries(uris, query);
         }
         return results;
     }
 
     @Override
     public Map<String, List<Identified>> executeIdQuery(Iterable<Long> ids, final ContentQuery query) {
         Map<String, List<Identified>> mongoResults = resolveMongoEntries(query, mongoLookupResolver.entriesForIds(ids));
         return mongoResults;
     }
 
     private Map<String, List<Identified>> resolveMongoEntries(final ContentQuery query, Iterable<LookupEntry> lookupEntries) {
         final ApplicationConfiguration configuration = query.getConfiguration();
         ImmutableMap<String, LookupEntry> lookup = Maps.uniqueIndex(Iterables.filter(lookupEntries, new Predicate<LookupEntry>() {
 
             @Override
             public boolean apply(LookupEntry input) {
                 return configuration.isEnabled(input.lookupRef().publisher());
             }
         }), LookupEntry.TO_ID);
 
         Map<String, Set<LookupRef>> lookupRefs = Maps.transformValues(lookup, LookupEntry.TO_EQUIVS);
 
         Iterable<LookupRef> filteredRefs = Iterables.filter(Iterables.concat(lookupRefs.values()), enabledPublishers(configuration));
 
         if (Iterables.isEmpty(filteredRefs)) {
             return ImmutableMap.of();
         }
 
 
         final ResolvedContent allResolvedResults = mongoContentResolver.findByLookupRefs(filteredRefs);
 
         return Maps.transformEntries(lookup, new EntryTransformer<String, LookupEntry, List<Identified>>() {
 
             @Override
             public List<Identified> transformEntry(String uri, LookupEntry entry) {
                 if (!containsRequestedUri(entry.equivalents(), uri)) {
                     return ImmutableList.of();
                 }
                 Iterable<Identified> identifieds = Iterables.filter(Iterables.transform(entry.equivalents(), new Function<LookupRef, Identified>() {
 
                     @Override
                     public Identified apply(LookupRef input) {
                         return allResolvedResults.get(input.id()).valueOrNull();
                     }
                 }), Predicates.notNull());
 
                 return setEquivalentToFields(ImmutableList.copyOf(identifieds));
             }
         });
     }
 
     private boolean containsRequestedUri(Iterable<LookupRef> equivRefs, String uri) {
         for (LookupRef equivRef : equivRefs) {
             if (equivRef.id().equals(uri)) {
                 return true;
             }
         }
         return false;
     }
 
     private List<Identified> setEquivalentToFields(List<Identified> resolvedResults) {
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
 
     private Map<String, List<Identified>> resolveCassandraEntries(Iterable<String> uris, ContentQuery query) {
         final ApplicationConfiguration configuration = query.getConfiguration();
         ResolvedContent result = cassandraContentResolver.findByLookupRefs(Iterables.transform(uris, new Function<String, LookupRef>() {
 
             @Override
             public LookupRef apply(String input) {
                 return new LookupRef(input, null, null);
             }
         }));
         return Maps.transformValues(Maps.filterValues(result.asResolvedMap(), new Predicate<Identified>() {
 
             @Override
             public boolean apply(Identified input) {
                 return ((input instanceof Described)
                         && configuration.isEnabled(((Described) input).getPublisher()));
             }
         }), new Function<Identified, List<Identified>>() {
 
             @Override
             public List<Identified> apply(Identified input) {
                 return ImmutableList.of(input);
             }
         });
     }
 }
