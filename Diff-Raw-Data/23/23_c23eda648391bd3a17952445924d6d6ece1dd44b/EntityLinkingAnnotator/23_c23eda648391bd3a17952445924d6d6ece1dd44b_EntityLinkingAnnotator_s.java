 /*
  * Copyright (c) 2010, Hamish Morgan.
  * All Rights Reserved.
  */
 package uk.ac.susx.mlcl.erl.snlp;
 
 import com.google.api.services.freebase.Freebase2;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import edu.stanford.nlp.ling.CoreAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations;
 import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.Annotator;
 import edu.stanford.nlp.util.CoreMap;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import org.slf4j.LoggerFactory;
 import uk.ac.susx.mlcl.erl.kb.CachedKnowledgeBase;
 import uk.ac.susx.mlcl.erl.kb.FreebaseKB;
 import uk.ac.susx.mlcl.erl.kb.KnowledgeBase;
 
 /**
  *
  * @author hamish
  */
 public class EntityLinkingAnnotator implements Annotator {
 
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Freebase2.class);
     private final KnowledgeBase knowledgeBase;
 
     public EntityLinkingAnnotator(KnowledgeBase knowledgeBase) {
         this.knowledgeBase = CachedKnowledgeBase.wrap(knowledgeBase);
     }
 
     static EntityLinkingAnnotator newInstance() throws IOException {
 
         KnowledgeBase kb = FreebaseKB.newInstance();
         return new EntityLinkingAnnotator(kb);
 
     }
 
     /**
      *
      * @return
      */
     public Set<Class<? extends CoreAnnotation<?>>> getRequiredAnnotations() {
         final Set<Class<? extends CoreAnnotation<?>>> requirements =
                 new HashSet<Class<? extends CoreAnnotation<?>>>();
         requirements.add(TokensAnnotation.class);
         requirements.add(NamedEntityTagAnnotation.class);
         return requirements;
     }
 
     /**
      * Get a collection of all the annotation types that are produced by this annotator.
      * <p/>
      * @return annotations produced by the annotator
      */
     public Set<Class<? extends CoreAnnotation<?>>> getSuppliedAnnotations() {
         return Collections.<Class<? extends CoreAnnotation<?>>>singleton(
                 EntityKbIdAnnotation.class);
     }
 
     /**
      *
      * @param annotation
      */
     @Override
     public void annotate(final Annotation document) {
         Preconditions.checkNotNull(document, "annotation");
 
         // Find all the entity mentions in the document
         final List<List<CoreLabel>> mentions = Lists.newArrayList();
         for (final CoreMap sentence : document.get(SentencesAnnotation.class)) {
             final List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 
             // Search through the sentence finding contiguous sequences of tokens with the same
             // entity type label. If the label not [O]ther then add the sequence to the mentions
             int first = 0;
             while (first < tokens.size()) {
                 final String currentLabel = tokens.get(first).get(NamedEntityTagAnnotation.class);
                 int last = first + 1;
 
                 while (last < tokens.size() && tokens.get(last).get(NamedEntityTagAnnotation.class).equals(currentLabel)) {
                     last++;
                 }
 
                 if (!currentLabel.equals("O")) {
                     mentions.add(tokens.subList(first, last));
                 }
 
                 first = last;
             }
         }
 
         // For each mention, create a query strings as the undering character sequence
         // covered by the tokens
         final Map<String, List<CoreLabel>> query2labels = Maps.newHashMap();
 
         final String documentText = document.get(CoreAnnotations.TextAnnotation.class);
         for (final List<CoreLabel> phrase : mentions) {
             final String text = documentText.substring(
                     phrase.get(0).beginPosition(),
                     phrase.get(phrase.size() - 1).endPosition() + 1);
 
             // if map already contains the text then append the CoreLabels, otherwise create a 
             // new list there (don't use existing one since it's just a view
             if (query2labels.containsKey(text)) {
                 query2labels.get(text).addAll(phrase);
             } else {
                 query2labels.put(text, Lists.newArrayList(phrase));
             }
         }
 
         // Search the knowledge base with all of the unique query strings
         final Map<String, List<String>> results;
         try {
             results = knowledgeBase.batchSearch(query2labels.keySet());
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
             LOG.error(ex.getLocalizedMessage(), ex);
             throw new RuntimeException(ex);
         }
 
         // Now add the kb id as an annotation to each mention token
         for (final String query : query2labels.keySet()) {
             if (!results.containsKey(query)) {
                 LOG.warn("Failed to find search result for: " + query);
                 continue;
             }
 
             final List<String> candidateIds = results.get(query);
             // TODO: Implement a more sensible method of choosing the best KB-ID
             final String id = candidateIds.isEmpty() ? "/NIL" : candidateIds.get(0);
 
             for (final CoreLabel token : query2labels.get(query)) {
                 token.set(EntityKbIdAnnotation.class, id);
             }
 
         }
     }
 
 
     public static final class EntityKbIdAnnotation implements CoreAnnotation<String> {
 
         @Override
         public Class<String> getType() {
             return String.class;
         }
     }
 
     public static class Factory implements edu.stanford.nlp.util.Factory<Annotator>, Serializable {
 
         private static final long serialVersionUID = 1L;
 
         @Override
         public Annotator create() {
             try {
                 return EntityLinkingAnnotator.newInstance();
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
         }
     }
 }
