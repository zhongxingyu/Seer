 package txtfnnl.uima.collection;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.uima.UIMAException;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.text.AnnotationIndex;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSArray;
 import org.apache.uima.jcas.cas.TOP;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.factory.AnalysisEngineFactory;
 
 import txtfnnl.uima.UIMAUtils;
 import txtfnnl.uima.Views;
 import txtfnnl.uima.tcas.RelationshipAnnotation;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 import txtfnnl.uima.tcas.SentenceAnnotation;
 
 /**
  * A CAS consumer that writes CSV content based on {@link RelationshipAnnotation relationship
  * annotations} with their source {@link SentenceAnnotation sentence} and target
  * {@link SemanticAnnotation entities}. Annotations will be written as "
  * <code>namespace:identifier#confidence</code>" triplets, where the first triplet is created from
  * the relationship annotation itself, while all other triplets are created from the linked
  * entities. If the {@link #PARAM_EVIDENCE_SENTENCES evidence sentences} are requested, instead,
  * the ( {@link #PARAM_REPLACE_NEWLINES newline-free}) sentence(s) where the entities in the
  * relationship are found are extracted in XML format, using ns:id tags from the relationship to
  * enclose the sentence(s) and similar ns:id tags for the annotated entities. If
  * {@link #PARAM_NORMALIZED_ENTITIES normalized entities} are requested, instead of using the
  * directly linked entities to the relationship, any {@link SemanticAnnotation semantic
  * annotations} within the linked entities' spans are used as the relationship entities, possibly
  * delimited by a regular expression that has to match the namespace of these "inner", normalized
  * entities.
  * 
  * @see TextWriter
  * @author Florian Leitner
  */
 public class RelationshipWriter extends TextWriter {
   /**
    * If <code>true</code> (the default), line-breaks within evidence sentences will be replaced
    * with white-spaces.
    * <p>
    * Detected line-breaks are Windows (CR-LF) and Unix line-breaks (LF only).
    */
   public static final String PARAM_REPLACE_NEWLINES = "ReplaceNewlines";
   @ConfigurationParameter(name = PARAM_REPLACE_NEWLINES, defaultValue = "true")
   private Boolean replaceNewlines;
   /** Separator to use between namespace, identifier, offset, and text fields (default: TAB). */
   public static final String PARAM_FIELD_SEPARATOR = "FieldSeparator";
   @ConfigurationParameter(name = PARAM_FIELD_SEPARATOR, defaultValue = "\t")
   private String fieldSeparator;
   static final String LINEBREAK = System.getProperty("line.separator");
   /** Separator to use between namespace, identifier, offset, and text fields (default: TAB). */
   public static final String PARAM_EVIDENCE_SENTENCES = "EvidenceSentences";
   @ConfigurationParameter(name = PARAM_EVIDENCE_SENTENCES, defaultValue = "false")
   private boolean extractEvidenceSentences;
   /**
    * Extract (normalized) semantic entities <b>within</b> the linked relationship entities instead
    * of the relationship entities themselves.
    */
   public static final String PARAM_NORMALIZED_ENTITIES = "NormalizedEntities";
   @ConfigurationParameter(name = PARAM_NORMALIZED_ENTITIES, defaultValue = "false")
   private boolean normalizedEntities;
   private NumberFormat decimals = null;
   private String spaces;
 
   /**
    * Configure a {@link RelationshipWriter} description. Note that if the {@link #outputDirectory}
    * is <code>null</code> and {@link #printToStdout} is <code>false</code> , a
    * {@link ResourceInitializationException} will occur when creating the AE.
    * 
    * @param outputDirectory path to the output directory (or null)
    * @param encoding encoding to use for the text (or null)
    * @param printToStdout whether to print to STDOUT or not
    * @param overwriteFiles whether to overwrite existing files or not
    * @param replaceNewlines whether to replace line-breaks in annotations with white-spaces or not
    * @param fieldSeparator to use between the output fields
    * @param extractEvidenceSentences whether to extract evidence sentences or not
    * @param normalizedEntities linked entities are normalized using inner semantic annotations
    * @return a configured AE description
    * @throws IOException
    * @throws UIMAException
    */
   @SuppressWarnings("serial")
   public static AnalysisEngineDescription configure(final File outputDirectory,
       final String encoding, final boolean printToStdout, final boolean overwriteFiles,
       final boolean replaceNewlines, final String fieldSeparator,
       final boolean extractEvidenceSentences, final boolean normalizedEntities)
       throws UIMAException, IOException {
     return AnalysisEngineFactory.createPrimitiveDescription(RelationshipWriter.class,
         UIMAUtils.makeParameterArray(new HashMap<String, Object>() {
           {
             put(PARAM_OUTPUT_DIRECTORY, outputDirectory);
             put(PARAM_ENCODING, encoding);
             put(PARAM_PRINT_TO_STDOUT, printToStdout);
             put(PARAM_OVERWRITE_FILES, overwriteFiles);
             put(PARAM_REPLACE_NEWLINES, replaceNewlines);
             put(PARAM_FIELD_SEPARATOR, fieldSeparator);
             put(PARAM_EVIDENCE_SENTENCES, extractEvidenceSentences);
             put(PARAM_NORMALIZED_ENTITIES, normalizedEntities);
           }
         }));
   }
 
   /**
    * Configure a {@link RelationshipWriter} description using all the defaults:
    * <ul>
    * <li>outputDirectory=<code>null</code> (instead, print to STDOUT)</li>
    * <li>encoding=<code>null</code> (i.e., use system default)</li>
    * <li>printToStdout=<code>true</code></li>
    * <li>overwriteFiles=<code>false</code></li>
    * <li>fieldSeparator=<code>TAB</code></li>
    * <li>replaceNewlines=<code>true</code></li>
    * <li>extractEvidenceSentences=<code>false</code></li>
    * <li>normalizedEntities=<code>null</code> (i.e., no normalized entities present)</li>
    * </ul>
    * 
    * @see #configure(File, String, boolean, boolean, boolean)
    * @return a configured AE description
    * @throws IOException
    * @throws UIMAException
    */
   public static AnalysisEngineDescription configure() throws UIMAException, IOException {
     return RelationshipWriter.configure(null, null, true, false, true, "\t", false, false);
   }
 
   @Override
   public void initialize(UimaContext ctx) throws ResourceInitializationException {
     super.initialize(ctx);
     logger = ctx.getLogger();
     decimals = DecimalFormat.getInstance();
     decimals.setMaximumFractionDigits(5);
     decimals.setMinimumFractionDigits(5);
     spaces = (LINEBREAK.length() == 1) ? " " : "  ";
   }
 
   @Override
   public void process(CAS cas) throws AnalysisEngineProcessException {
     JCas textJCas;
     // TODO: use default view
     try {
       textJCas = cas.getView(Views.CONTENT_TEXT.toString()).getJCas();
       setStream(cas.getView(Views.CONTENT_RAW.toString()));
     } catch (final CASException e) {
       throw new AnalysisEngineProcessException(e);
     } catch (final IOException e) {
       throw new AnalysisEngineProcessException(e);
     }
     // TODO: allow constraining the RA type
     // final FSMatchConstraint relCons = RelationshipAnnotation.makeConstraint(jcas,
     // relationshipAnnotator, relationshipNamespace, relationshipIdentifier);
     // FSIterator<TOP> relIt = textJCas.createFilteredIterator(textJCas.getJFSIndexRepository()
     // .getAllIndexedFS(RelationshipAnnotation.type), relCons);
     FSIterator<TOP> relIt = textJCas.getJFSIndexRepository().getAllIndexedFS(
         RelationshipAnnotation.type);
     AnnotationIndex<Annotation> idx = textJCas.getAnnotationIndex(SemanticAnnotation.type);
     while (relIt.hasNext()) {
       RelationshipAnnotation rel = (RelationshipAnnotation) relIt.next();
       SentenceAnnotation sentence = (SentenceAnnotation) rel.getSources(0);
       logger.log(Level.FINE, "{0} :: ''{1}''", new Object[] { rel, sentence.getCoveredText() });
       Set<SemanticAnnotation> entities = collectEntities(idx, rel.getTargets());
       try {
         if (extractEvidenceSentences) {
           String evidence = annotateEvidence(sentence, idx, entities);
           write(String.format("<%s:_%s c=\"%s\">", rel.getNamespace(), rel.getIdentifier(),
               decimals.format(rel.getConfidence())));
           write(evidence); // separated "evidence" to first write logs, then the evidence
           write(String.format("</%s:_%s>", rel.getNamespace(), rel.getIdentifier()));
           write(fieldSeparator);
         } else {
           write(String.format("%s:_%s#%s", rel.getNamespace(), rel.getIdentifier(),
               decimals.format(rel.getConfidence())));
           writeEntities(entities);
         }
         write(LINEBREAK);
       } catch (final IOException e) {
         throw new AnalysisEngineProcessException(e);
       }
     }
     try {
       unsetStream();
     } catch (final IOException e) {
       throw new AnalysisEngineProcessException(e);
     }
    logger.log(Level.INFO, "dumped results for {0}", cas.getView(Views.CONTENT_RAW.toString())
        .getSofaDataURI());
   }
 
   private String annotateEvidence(SentenceAnnotation sentence, AnnotationIndex<Annotation> idx,
       Set<SemanticAnnotation> entities) {
     FSIterator<Annotation> annIt = idx.subiterator(sentence, true, true);
     StringBuilder result = new StringBuilder();
     int offset = sentence.getBegin();
     final int base = sentence.getBegin();
     String s = sentence.getCoveredText();
     if (replaceNewlines) s = s.replace(LINEBREAK, spaces);
     Map<Integer, StringBuilder> closeTags = new HashMap<Integer, StringBuilder>();
     while (annIt.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) annIt.next();
       if (entities.contains(ann))
         offset = annotateEntity(result, ann, s, offset, base, closeTags);
     }
     int last = sentence.getEnd();
     offset = expandResult(result, s, offset, base, last, closeTags);
     return result.toString();
   }
 
   private int annotateEntity(StringBuilder result, SemanticAnnotation ann, String s, int offset,
       int base, Map<Integer, StringBuilder> closeTags) {
     Integer end = ann.getEnd();
     if (ann.getBegin() > offset)
       offset = expandResult(result, s, offset, base, ann.getBegin(), closeTags);
     result.append(String.format("<%s:_%s c=\"%s\">", ann.getNamespace(), ann.getIdentifier(),
         decimals.format(ann.getConfidence())));
     if (!closeTags.containsKey(end)) closeTags.put(end, new StringBuilder());
     closeTags.get(end).insert(0,
         String.format("</%s:_%s>", ann.getNamespace(), ann.getIdentifier()));
     return offset;
   }
 
   private int expandResult(StringBuilder result, String s, int offset, final int base, int last,
       Map<Integer, StringBuilder> closeTags) {
     if (closeTags.size() > 0) {
       for (; offset < last; ++offset) {
         if (closeTags.containsKey(offset)) {
           result.append(closeTags.get(offset));
           closeTags.remove(offset);
           if (closeTags.size() == 0) break;
         }
         result.append(s.charAt(offset - base));
       }
     }
     if (offset < last) {
       result.append(s.substring(offset - base, last - base));
       offset = last;
     }
     return offset;
   }
 
   private void writeEntities(Set<SemanticAnnotation> entities) throws IOException {
     for (SemanticAnnotation ann : entities) {
       write(fieldSeparator);
       write(String.format("%s:%s#%s", ann.getNamespace(), ann.getIdentifier(),
           decimals.format(ann.getConfidence())));
     }
   }
 
   private Set<SemanticAnnotation> collectEntities(AnnotationIndex<Annotation> idx,
       FSArray entityArray) {
     Set<SemanticAnnotation> entities = new HashSet<SemanticAnnotation>(entityArray.size());
     for (int i = entityArray.size() - 1; i >= 0; i--) {
       SemanticAnnotation ann = (SemanticAnnotation) entityArray.get(i);
       if (normalizedEntities) {
         logger.log(Level.FINE, "collecting normalizations in ''{0}''", ann.getCoveredText());
         FSIterator<Annotation> subIt = idx.subiterator(ann, true, true);
         String ns = (ann.getNamespace() == null) ? "" : ann.getNamespace();
         while (subIt.hasNext()) {
           SemanticAnnotation inner = (SemanticAnnotation) subIt.next();
           if (ns.equals(inner.getNamespace())) continue;
           // TODO: configure constraints for these inner semantic annotations
           // if (namespacePattern.matcher(ann.getNamespace()).matches()) entities.add(ann);
           entities.add(inner);
         }
       } else {
         entities.add(ann);
       }
     }
     return entities;
   }
 }
