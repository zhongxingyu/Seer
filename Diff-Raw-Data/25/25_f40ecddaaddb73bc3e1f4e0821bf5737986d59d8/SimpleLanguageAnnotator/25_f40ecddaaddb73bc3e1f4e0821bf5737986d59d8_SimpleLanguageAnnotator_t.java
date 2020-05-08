 package org.apache.uima.annotator.language;
 
 import org.apache.uima.EnglishDictionaryEntry;
 import org.apache.uima.ItalianDictionaryEntry;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.text.AnnotationIndex;
 import org.apache.uima.jcas.JCas;
 
 public class SimpleLanguageAnnotator extends JCasAnnotator_ImplBase {
 
 
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
 
     /* get all DictionaryEntry annotations */
     AnnotationIndex italianDictionaryAnnotationsIndex = aJCas.getAnnotationIndex(ItalianDictionaryEntry.type);
     AnnotationIndex englishDictionaryAnnotationsIndex = aJCas.getAnnotationIndex(EnglishDictionaryEntry.type);
     String language;
 
    if (italianDictionaryAnnotationsIndex.size() == englishDictionaryAnnotationsIndex.size())
       language = "x-unspecified";
    else if (italianDictionaryAnnotationsIndex.size() < englishDictionaryAnnotationsIndex.size())
       language = "en";
     else
       language = "it";
 
     aJCas.setDocumentLanguage(language);
 
   }
 
 }
