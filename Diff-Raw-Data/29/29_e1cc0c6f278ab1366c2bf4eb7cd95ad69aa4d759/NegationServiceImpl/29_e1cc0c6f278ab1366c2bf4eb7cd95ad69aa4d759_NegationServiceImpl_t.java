 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp.services.other;
 
 
 import gov.va.vinci.cm.Annotation;
 import gov.va.vinci.cm.AnnotationInterface;
 import gov.va.vinci.cm.DocumentInterface;
 import gov.va.vinci.cm.Feature;
 import gov.va.vinci.v3nlp.model.Span;
 import gov.va.vinci.v3nlp.negex.GenNegEx;
 import gov.va.vinci.v3nlp.registry.NlpComponent;
 import gov.va.vinci.v3nlp.services.NlpProcessingUnit;
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.core.io.Resource;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Negation service implementation for Interface.
  * Notes:
  * <ul>
  *     <li>This modules requires sentence splitter and either Hitex Concept Finder or Metamap</li>
  *     <li>It only looks at annotation with pedigree <strong>GATE|hitex.gate.regex.ConceptFinder</strong>
  *          or <strong>INTERFACE:metamap</strong> to check for negation. </li>
  *     <li>Annotations created have pedigree <strong>INTERFACE|gov.va.vinci.v3nlp.services.other.NegationServiceImpl</strong></li>
  *     <li>Uses Wendy Chapman's Negex Alogrithm.</li>
  * </ul>
  */
 public class NegationServiceImpl implements NlpProcessingUnit {
 
     Resource sentenceRulesFile;
     Resource negationRulesFile;
 
     GenNegEx genNegEx = new GenNegEx();
 
     /**
      * This is processed to not have empty lines or comments, from the default configuration file.
      */
     List<String> defaultNegationConfigurationList = new ArrayList<String>();
 
     @Override
     public DocumentInterface process(String config, DocumentInterface d, List<NlpComponent> previousModuleProvided) {
         List<String> negationRules = new ArrayList<String>();
         HashMap<String, AnnotationInterface> sentences = new HashMap<String, AnnotationInterface>();
         List<AnnotationInterface> conceptsToProcess = new ArrayList<AnnotationInterface>();
 
          // No Annotations, skip
         if (d.getAnnotations().getAll().size() < 1) {
             return d;
         }
 
         if (!GenericValidator.isBlankOrNull(config)) {
             for (String s : config.split("\r")) {
                 if (s.startsWith("##") || s.trim().length() == 0) {
                     continue;
                 }
                 negationRules.add(s); // Convert to ArrayList.
             }
         } else {
             if (defaultNegationConfigurationList.isEmpty()) {
                 for (String s : this.getDefaultNegationConfiguration()) {
                     if (s.startsWith("##") || s.trim().isEmpty()) {
                         continue;
                     }
                     negationRules.add(s);
                 }
                 defaultNegationConfigurationList = negationRules;
             } else {
                 negationRules = defaultNegationConfigurationList;
             }
         }
 
         // Step 1 : Build sentence map and list of concepts to process.
         for (AnnotationInterface ann : d.getAnnotations().getAll()) {
            if (((Annotation)ann).hasFeatureOfPedigree("GATE:hitex.gate.SentenceSplitter")) {
                  sentences.put(ann.getBeginOffset() + "-" + ann.getEndOffset(), ann);
             }
 
           if (((Annotation)ann).hasFeatureOfPedigree("GATE:hitex.gate.regex.ConceptFinder") || ((Annotation)ann).hasFeatureOfPedigree("INTERFACE:metamap")) {
                 conceptsToProcess.add(ann);
             }
         }
 
         // Step 2 iterate through concepts to process.
         for (AnnotationInterface ann : conceptsToProcess) {
 
             // Step 3 find sentence for this annotation.
             String sentence = findSentence(ann, sentences);
             try {
                 // Step 4 run negation algorithm on sentence / annotation
                 String result = genNegEx.negCheck(sentence, ann.getContent(),
                         negationRules, false);
                 if (result.trim().endsWith("negated")) {
                     Feature f = new Feature("type", "Negated");
                     f.getMetaData().setCreatedDate(new Date());
                    f.getMetaData().setPedigree("INTERFACE:" + this.getClass().getCanonicalName());
                     f.setFeatureName("Negation");
                     ((Annotation) ann).getFeatures().add(f);
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
 
         return d;
     }
 
     protected String findSentence(AnnotationInterface ann, HashMap<String, AnnotationInterface> sentences) {
         for (String key: sentences.keySet()) {
               String[] parts=key.split("-");
             if (ann.getBeginOffset() >= Integer.parseInt(parts[0])
                 && ann.getBeginOffset() <= Integer.parseInt(parts[1])) {
                     return sentences.get(key).getContent();
             }
         }
 
         return "";
     }
 
 
     @Override
     public void initialize() {
 
     }
 
     public ArrayList<String> getDefaultNegationConfiguration() {
         try {
             ArrayList<String> toReturn = new ArrayList<String>();
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     negationRulesFile.getInputStream()));
             String str = null;
             while ((str = in.readLine()) != null) {
                 toReturn.add(str);
             }
             return toReturn;
         } catch (Exception e) {
             throw new RuntimeException(e);
 
         }
     }
 
     /**
      * Open NLP 1.1 only returns sentence endings. This code is from OpenNLP 1.5
      * that converts those into more useful spans for each sentence.
      *
      * @param starts integer array of sentence starts
      * @param s      the text that the sentence starts belong to.
      * @return an array of spans for each sentence.
      */
     public Span[] convertToSpans(int[] starts, String s) {
         boolean leftover = starts[starts.length - 1] != s.length();
         Span[] spans = new Span[leftover ? starts.length + 1 : starts.length];
         for (int si = 0; si < starts.length; si++) {
             int start, end;
             if (si == 0) {
                 start = 0;
 
                 while (si < starts.length
                         && Character.isWhitespace(s.charAt(start)))
                     start++;
             } else {
                 start = starts[si - 1];
             }
             end = starts[si];
             while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) {
                 end--;
             }
             spans[si] = new Span(start, end);
         }
         if (leftover) {
             spans[spans.length - 1] = new Span(starts[starts.length - 1], s
                     .length());
         }
         return spans;
     }
 
     public Resource getSentenceRulesFile() {
         return sentenceRulesFile;
     }
 
     public void setSentenceRulesFile(Resource sentenceRulesFile) {
         this.sentenceRulesFile = sentenceRulesFile;
     }
 
     public Resource getNegationRulesFile() {
         return negationRulesFile;
     }
 
     public void setNegationRulesFile(Resource negationRulesFile) {
         this.negationRulesFile = negationRulesFile;
     }
 
     @Override
     public void destroy() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
