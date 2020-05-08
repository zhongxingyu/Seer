 package org.atlasapi.equiv.handlers;
 
 import java.util.Map;
 
 import org.atlasapi.equiv.ContentRef;
 import org.atlasapi.equiv.EquivalenceSummary;
 import org.atlasapi.equiv.EquivalenceSummaryStore;
 import org.atlasapi.equiv.results.EquivalenceResult;
 import org.atlasapi.equiv.results.description.ReadableDescription;
 import org.atlasapi.equiv.results.description.ResultDescription;
 import org.atlasapi.equiv.results.scores.ScoredCandidate;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Publisher;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 
 public class EpisodeFilteringEquivalenceResultHandler implements EquivalenceResultHandler<Item> {
 
     private final EquivalenceResultHandler<Item> delegate;
     private final EquivalenceSummaryStore summaryStore;
 
     public EpisodeFilteringEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, EquivalenceSummaryStore summaryStore) {
         this.delegate = delegate;
         this.summaryStore = summaryStore;
     }
 
     @Override
     public void handle(EquivalenceResult<Item> result) {
 
         ReadableDescription desc = (ReadableDescription) result.description().startStage(String.format("Episode parent filter"));
         
         ParentRef container = result.subject().getContainer();
         if (container == null) {
             desc.appendText("Item has no Container").finishStage();
             return;
         }
         
         String containerUri = container.getUri();
         Optional<EquivalenceSummary> possibleSummary = summaryStore.summariesForUris(ImmutableSet.of(containerUri)).get(containerUri);
         if (!possibleSummary.isPresent()) {
             desc.appendText("Item Container summary not found").finishStage();
             return;
         }
 
         ImmutableMap<Publisher,ContentRef> equivalents = possibleSummary.get().getEquivalents();
         Map<Publisher, ScoredCandidate<Item>> strongEquivalences = filter(result.strongEquivalences(), equivalents, desc);
         desc.finishStage();
         delegate.handle(new EquivalenceResult<Item>(result.subject(), result.rawScores(), result.combinedEquivalences(), strongEquivalences, desc));
 
     }
 
     private Map<Publisher, ScoredCandidate<Item>> filter(Map<Publisher, ScoredCandidate<Item>> strongItems, final Map<Publisher,ContentRef> containerEquivalents, final ResultDescription desc) {
         return ImmutableMap.copyOf(Maps.filterValues(strongItems, new Predicate<ScoredCandidate<Item>>() {
             @Override
             public boolean apply(ScoredCandidate<Item> scoredCandidate) {
                 Item candidate = scoredCandidate.candidate();
                 ParentRef candidateContainer = candidate.getContainer();
                 if (candidateContainer == null) {
                     return true;
                 }
 
                 String candidateContainerUri = candidateContainer.getUri();
                 ContentRef validContainer = containerEquivalents.get(candidate.getPublisher());
                 if (validContainer == null || validContainer.getCanonicalUri().equals(candidateContainerUri)) {
                     return true;
                 }
                 desc.appendText("%s removed. Unacceptable container: %s", scoredCandidate, candidateContainerUri);
                 return false;
             }
         }));
     }
 
 }
