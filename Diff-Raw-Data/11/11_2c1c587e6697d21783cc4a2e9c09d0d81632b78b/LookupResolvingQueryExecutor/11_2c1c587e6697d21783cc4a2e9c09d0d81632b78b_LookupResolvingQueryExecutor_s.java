 package org.atlasapi.query.content;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 import org.atlasapi.persistence.lookup.entry.LookupRef;
 
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
 
     private final LookupEntryStore lookupResolver;
     private final KnownTypeContentResolver contentResolver;
 
     public LookupResolvingQueryExecutor(KnownTypeContentResolver contentResolver, LookupEntryStore lookupResolver) {
         this.contentResolver = contentResolver;
         this.lookupResolver = lookupResolver;
     }
 
     @Override
     public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
         return resolveLookupEntries(query, lookupResolver.entriesForUris(uris));
     }
 
     @Override
     public Map<String, List<Identified>> executeIdQuery(Iterable<String> ids, final ContentQuery query) {
         return resolveLookupEntries(query, lookupResolver.entriesForIds(ids));
     }
 
     private Map<String, List<Identified>> resolveLookupEntries(final ContentQuery query, Iterable<LookupEntry> lookupEntries) {
        ImmutableMap<String, LookupEntry> lookup = Maps.uniqueIndex(lookupEntries, LookupEntry.TO_ID);
         
         Map<String, Set<LookupRef>> lookupRefs = Maps.transformValues(lookup, LookupEntry.TO_EQUIVS);
 
        Iterable<LookupRef> filteredRefs = Iterables.filter(Iterables.concat(lookupRefs.values()), enabledPublishers(query.getConfiguration()));
         
         if (Iterables.isEmpty(filteredRefs)) {
             return ImmutableMap.of();
         }
         
         
         final ResolvedContent allResolvedResults = contentResolver.findByLookupRefs(filteredRefs);
         
         return Maps.transformEntries(lookup, new EntryTransformer<String, LookupEntry, List<Identified>>(){
 
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
             if(equivRef.id().equals(uri)){
                 return true;
             }
         }
         return false;
     }
 
     private List<Identified> setEquivalentToFields(List<Identified> resolvedResults) {
         ImmutableSet<String> uris = ImmutableSet.copyOf(Iterables.transform(resolvedResults, Identified.TO_URI));
         for (Identified ided : resolvedResults) {
             ided.setEquivalentTo(Sets.difference(uris, ImmutableSet.of(ided.getCanonicalUri())));
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
