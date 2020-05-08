 package org.atlasapi.equiv.generators;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.equiv.results.description.ResultDescription;
 import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
 import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
 import org.atlasapi.equiv.results.scores.Score;
 import org.atlasapi.equiv.results.scores.ScoredEquivalents;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.persistence.content.ContentResolver;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.base.Maybe;
 
 public class RadioTimesFilmEquivalenceGenerator implements ContentEquivalenceGenerator<Item> {
 
     private final Pattern rtFilmUriPattern = Pattern.compile("http://radiotimes.com/films/(\\d+)");
     private final String paFilmUriPrefix = "http://pressassociation.com/films/";
     
     private final ContentResolver resolver;
 
     public RadioTimesFilmEquivalenceGenerator(ContentResolver resolver) {
         this.resolver = resolver;
     }
     
     @Override
     public ScoredEquivalents<Item> generate(Item content, ResultDescription desc) {
         checkArgument(content instanceof Film, "Content not Film:" + content.getCanonicalUri());
         
        ScoredEquivalentsBuilder<Item> results = DefaultScoredEquivalents.fromSource("RT->PA");
         
         Matcher uriMatcher = rtFilmUriPattern.matcher(content.getCanonicalUri());
         if (uriMatcher.matches()) {
             String paUri = paFilmUriPrefix + uriMatcher.group(1);
             Maybe<Identified> resolvedContent = resolver.findByCanonicalUris(ImmutableSet.of(paUri)).get(paUri);
             if (resolvedContent.hasValue() && resolvedContent.requireValue() instanceof Film) {
                 results.addEquivalent((Film)resolvedContent.requireValue(), Score.ONE);
             }
         }
         
         return results.build();
     }
 
     @Override
     public String toString() {
         return "RadioTimes Film Generator";
     }
 }
