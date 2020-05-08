 package au.com.miskinhill.rdf;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.stereotype.Component;
 
 import au.com.miskinhill.rdftemplate.selector.Adaptation;
 import au.com.miskinhill.rdftemplate.selector.AdaptationResolver;
 import au.com.miskinhill.rdftemplate.selector.DefaultAdaptationResolver;
 
 @Component("adaptationResolver")
 public class MiskinHillAdaptationResolver implements AdaptationResolver {
     
     private final AdaptationResolver defaults = new DefaultAdaptationResolver();
     private final Map<String, Class<? extends Adaptation<?>>> adaptations = new HashMap<String, Class<? extends Adaptation<?>>>();
     
     public MiskinHillAdaptationResolver() {
         adaptations.put("representation-anchors", RepresentationAnchorsAdaptation.class);
         adaptations.put("representation-links", RepresentationLinksAdaptation.class);
         adaptations.put("representation-atom-links", RepresentationAtomLinksAdaptation.class);
         adaptations.put("year", YearAdaptation.class);
         adaptations.put("html", HTMLFragmentRepresentationAdaptation.class);
         adaptations.put("mods", MODSRepresentationAdaptation.class);
         adaptations.put("book-links", BookLinksAdaptation.class);
         adaptations.put("article-links", ArticleLinksAdaptation.class);
         adaptations.put("content", ContentAdaptation.class);
         adaptations.put("lcsh-cleanup", LCSHCleanupAdaptation.class);
        adaptations.put("issue-number", IssueNumberAdaptation.class);
     }
 
     @Override
     public Class<? extends Adaptation<?>> getByName(String name) {
         Class<? extends Adaptation<?>> theDefaultOne = defaults.getByName(name);
         if (theDefaultOne == null) {
             return adaptations.get(name);
         }
         return theDefaultOne;
     }
 
 }
