 /* Created on May 14, 2013 by Florian Leitner.
  * Copyright 2013. All rights reserved. */
 package txtfnnl.uima.analysis_component;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.AnalysisComponent;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.descriptor.ExternalResource;
 import org.uimafit.util.JCasUtil;
 
 import txtfnnl.uima.AnalysisComponentBuilder;
 import txtfnnl.uima.resource.LineBasedStringMapResource;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 import txtfnnl.uima.tcas.TokenAnnotation;
 import txtfnnl.utils.Offset;
 
 /**
  * Filter (or select/whitelist) {@link SemanticAnnotation semantic annotations} based on the
  * contained and surrounding {@link TokenAnnotation tokens}, including the PoS tag of the semantic
  * annotation's (header) token.
  * <p>
  * The "surrounding" of the semantic annotation consists of the last non-overlapping token before
  * the annotation, the first non-overlapping token after the annotation, and possible affixes. An
  * annotation's prefix is the text span between the begin of the overlapping token and the actual
  * annotation; I.e., a prefix only exists if the annotation starts after the token. The same is the
  * case for a suffix, spanning from the end of the annotation to the end of the overlapping token.
  * 
  * @author Florian Leitner
  */
 public class TokenBasedSemanticAnnotationFilter extends JCasAnnotator_ImplBase {
   // public configuration
   public static final String PARAM_ANNOTATOR_URI = "AnnotatorUri";
   @ConfigurationParameter(name = PARAM_ANNOTATOR_URI,
       description = "The TextAnnotation URI to filter or select.")
   private String annotatorUri;
   public static final String PARAM_NAMESPACE = "Namespace";
   @ConfigurationParameter(name = PARAM_NAMESPACE,
       description = "The TextAnnotation NS to filter or select.")
   private String namespace;
   public static final String PARAM_IDENTIFIER = "Identifier";
   @ConfigurationParameter(name = PARAM_IDENTIFIER,
       description = "The TextAnnotation ID to filter or select.")
   private String identifier;
   public static final String PARAM_SELECT_TOKENS = "SelectTokenMatches";
   @ConfigurationParameter(name = PARAM_SELECT_TOKENS,
       description = "Select (by setting this parameter to true) or filter (default) annotations "
           + "based on the token matches.",
       defaultValue = "false")
   private boolean doSelect;
   public static final String PARAM_POS_TAGS = "PosTags";
   @ConfigurationParameter(name = PARAM_POS_TAGS, description = "List of required PoS tag matches.")
   private String[] posTags;
   private Set<String> posTagSet = null;
   /**
    * Surrounding tokens and affixes to compare to the semantic annotation being checked.
    * <p>
    * StringMap keys: "before", "after", "prefix", and "suffix"; Anything else will be ignored.
    */
   public static final String MODEL_KEY_TOKEN_SETS = "TokenSets";
   @ExternalResource(key = MODEL_KEY_TOKEN_SETS, mandatory = false)
   private LineBasedStringMapResource<Set<String>> tokenSets;
   private Set<String> beforeSet = null;
   private Set<String> afterSet = null;
   private Set<String> prefixSet = null;
   private Set<String> suffixSet = null;
   // internal state
   private Logger logger;
 
   public static class Builder extends AnalysisComponentBuilder {
     protected Builder(Class<? extends AnalysisComponent> klass) {
       super(klass);
     }
 
     public Builder() {
       this(TokenBasedSemanticAnnotationFilter.class);
     }
 
     /** Limit the semantic annotations to check to this annotator URI (default: any URI). */
     public Builder setAnnotatorUri(String uri) {
       setOptionalParameter(PARAM_ANNOTATOR_URI, uri);
       return this;
     }
 
     /** Limit the semantic annotations to check to this namespace (default: any namespace). */
     public Builder setNamespace(String ns) {
       setOptionalParameter(PARAM_NAMESPACE, ns);
       return this;
     }
 
     /** Limit the semantic annotations to check to this identifier (default: any ID). */
     public Builder setIdentifier(String id) {
       setOptionalParameter(PARAM_IDENTIFIER, id);
       return this;
     }
 
     /**
      * If set, the PoS tag of the head token of the checked semantic annotation (and that covers
      * one or more tokens) must match to an entry in this list, otherwise the annotation is removed
      * (i.e., whitelisting).
      * <p>
      * If the semantic annotation spans multiple tokens, its <strong>last</strong> ("head") token
      * is used as reference. Unlike other parameters, if a semantic annotation has no PoS, and this
      * parameter is set, it is <em>always</em> filtered. In other words,
      * {@link TokenBasedSemanticAnnotationFilter.Builder#whitelist() whitelisting} has no influence
      * on this parameter, because if a PoS tag list is set, this always indicates "whitelisting" of
      * the relevant tags. <strong>However</strong>, if the semantic annotation has no tokens, it is
      * not not filtered by this parameter, even if a PoS tag list was set!
      * 
      * @param posTags to select/whitelist
      * @return the configuation builder
      */
     public Builder setPosTags(String[] posTags) {
       setOptionalParameter(PARAM_POS_TAGS, posTags);
       return this;
     }
 
     /**
      * Instead of filtering (removing) matches of tokens, filter (remove) all semantic annotations
      * that have no match to <string>any</strong> of the defined sets, thereby <em>selecting</em>
      * for annotation with matches.
      */
     public Builder whitelist() {
       setOptionalParameter(PARAM_SELECT_TOKENS, true);
       return this;
     }
 
     /**
      * Filter (or select) annotations where the token <code>before</code> or <code>after</code> the
      * annotation or the annotation's <code>prefix</code> or <code>suffix</code> matches to any
      * String defined in the sets of this resource keyed with any of these names.
      * <p>
      * The resource is assumed to be a {@link LineBasedStringMapResource} mapping to collections of
      * String Sets. The mappings should be keyed by the above codewords. Any mapping using another
      * codeword is simply ignored.
      */
     public Builder setSurroundingTokens(ExternalResourceDescription desc) {
       setOptionalParameter(MODEL_KEY_TOKEN_SETS, desc);
       return this;
     }
   }
 
   /** Create a new AE configuration builder. */
   public static Builder configure() {
     return new Builder();
   }
 
   /** Initialize the filter/selection sets. */
   @Override
   public void initialize(UimaContext ctx) throws ResourceInitializationException {
     super.initialize(ctx);
     logger = ctx.getLogger();
     posTagSet = makeSetIfProvided(posTags);
     beforeSet = tokenSets.get("before");
     afterSet = tokenSets.get("after");
     prefixSet = tokenSets.get("prefix");
     suffixSet = tokenSets.get("suffix");
     logger.log(Level.INFO, "received {0} PoS tags and {1}/{2}/{3}/{4} tokens", new Object[] {
         (posTagSet == null) ? 0 : posTagSet.size(), (beforeSet == null) ? 0 : beforeSet.size(),
         (prefixSet == null) ? 0 : prefixSet.size(), (suffixSet == null) ? 0 : suffixSet.size(),
         (afterSet == null) ? 0 : afterSet.size(), });
   }
 
   /** Initialization helper to create the sets. */
   private Set<String> makeSetIfProvided(String[] items) {
     if (items != null && items.length > 0) {
       Set<String> s = new HashSet<String>();
       for (String i : items)
         s.add(i);
       return s;
     } else {
       return null;
     }
   }
 
   /**
    * Process the filtering or selection of the semantic annotations.
    * 
    * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
    */
   @Override
   public void process(JCas jcas) throws AnalysisEngineProcessException {
     FSMatchConstraint cons = SemanticAnnotation.makeConstraint(jcas, annotatorUri, namespace,
         identifier);
     FSIterator<Annotation> iter = jcas.createFilteredIterator(
         SemanticAnnotation.getIterator(jcas), cons);
     Map<SemanticAnnotation, Collection<TokenAnnotation>> coveredTokens = JCasUtil.indexCovered(
         jcas, SemanticAnnotation.class, TokenAnnotation.class);
     Map<SemanticAnnotation, Collection<TokenAnnotation>> innerTokens = JCasUtil.indexCovering(
         jcas, SemanticAnnotation.class, TokenAnnotation.class);
     TokenSurrounding surr = null;
     Offset last = null;
     List<SemanticAnnotation> removalBuffer = new LinkedList<SemanticAnnotation>();
     while (iter.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) iter.next();
       if (!ann.getOffset().equals(last)) {
         surr = new TokenSurrounding(jcas, ann, coveredTokens, innerTokens);
       }
       if (surr.current == null) {
         if (doSelect) removalBuffer.add(ann);
         else logger.log(Level.INFO, ann.toString() + " not covered by tokens");
       } else if (posTagSet != null && remove(surr.current.getPos(), posTagSet, true)) {
         removalBuffer.add(ann);
       } else if (beforeSet != null &&
           ((surr.before == null && doSelect) || (surr.before != null && remove(
               surr.before.getCoveredText(), beforeSet, doSelect)))) {
         removalBuffer.add(ann);
       } else if (afterSet != null &&
           ((surr.after == null && doSelect) || (surr.after != null && remove(
               surr.after.getCoveredText(), afterSet, doSelect)))) {
         removalBuffer.add(ann);
       } else if (suffixSet != null) {
         String affix = surr.current.getCoveredText().substring(
             ann.getEnd() - surr.current.getBegin());
         if (affix.length() > 0 && remove(affix, suffixSet, doSelect)) {
           removalBuffer.add(ann);
         } else {
           checkPrefix(ann, surr, removalBuffer);
         }
       } else if (prefixSet != null) {
         checkPrefix(ann, surr, removalBuffer);
       }
     }
     logger.log(Level.FINE, "removing " + removalBuffer.size() + " semantic annotations");
     for (SemanticAnnotation ann : removalBuffer)
       ann.removeFromIndexes();
   }
 
   /**
    * A structure to hold the "surrounding" (before, at, and after) token state relative to a given
    * semantic annotation.
    */
   private static class TokenSurrounding {
     /** The token before the relevant semantic annotation. */
     final TokenAnnotation before;
     /** The (last) token covering the relevant semantic annotation. */
     final TokenAnnotation current;
     /**
      * The first token covering the relevant semantic annotation or <code>null</code>.
      * <p>
      * This value only gets set if there are multiple tokens that span the relevant
      * {@link SemanticAnnotation}.
      */
     final TokenAnnotation first;
     /** The token after the relevant semantic annotation. */
     final TokenAnnotation after;
 
     /**
      * Establish the surrounding in the CAS given the semantic annotation.
      * <p>
      * To make this lookup more efficient, a pre-established mapping of tokens covered by semantic
      * annotations as well as semantic annotations contained within tokens need to be provided as
      * additional arguments.
      */
     public TokenSurrounding(JCas jcas, SemanticAnnotation ann,
         Map<SemanticAnnotation, Collection<TokenAnnotation>> tokensCoveredBySemAnn,
         Map<SemanticAnnotation, Collection<TokenAnnotation>> tokensContainingSemAnns) {
       Collection<TokenAnnotation> tokens = tokensCoveredBySemAnn.get(ann);
       if (tokens == null) tokens = tokensContainingSemAnns.get(ann);
      if (tokens == null || tokens.size() == 0) {
         before = null;
         current = null;
         first = null;
         after = null;
       } else {
         if (tokens.size() > 1) {
           TokenAnnotation[] multi = tokens.toArray(new TokenAnnotation[tokens.size()]);
           Arrays.sort(multi, new Comparator<TokenAnnotation>() {
             public int compare(TokenAnnotation a, TokenAnnotation b) {
               return a.getOffset().compareTo(b.getOffset());
             }
           });
           current = multi[multi.length - 1];
           first = multi[0];
         } else {
           current = tokens.iterator().next();
           first = null;
         }
         List<TokenAnnotation> r = JCasUtil.selectPreceding(jcas, TokenAnnotation.class, ann, 1);
         if (r.size() == 1) before = r.get(0);
         else before = null;
         r = JCasUtil.selectFollowing(jcas, TokenAnnotation.class, ann, 1);
         if (r.size() == 1) after = r.get(0);
         else after = null;
       }
     }
   }
 
   /** Add the annotation to the removal buffer if the prefix match condition is met. */
   private void checkPrefix(SemanticAnnotation ann, TokenSurrounding surr,
       List<SemanticAnnotation> removalBuffer) {
     String prefix;
     if (surr.first == null) prefix = surr.current.getCoveredText().substring(0,
         ann.getBegin() - surr.current.getBegin());
     else prefix = surr.first.getCoveredText().substring(0, ann.getBegin() - surr.first.getBegin());
     if (prefix.length() > 0 && remove(prefix, prefixSet, doSelect)) removalBuffer.add(ann);
   }
 
   /** Return <code>true</code> if the value for the given set indicates removal of the annotation. */
   private static boolean remove(String value, Set<String> testSet, boolean doSelect) {
     if (doSelect && !testSet.contains(value)) return true;
     else if (!doSelect && testSet.contains(value)) return true;
     else return false;
   }
 }
