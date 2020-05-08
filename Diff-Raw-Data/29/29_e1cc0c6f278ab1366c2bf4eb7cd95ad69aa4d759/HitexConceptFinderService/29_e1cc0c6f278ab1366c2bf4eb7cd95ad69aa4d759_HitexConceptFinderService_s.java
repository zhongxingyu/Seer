 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp.services.gate;
 
 import gate.Factory;
 import gate.ProcessingResource;
 import gate.creole.SerialAnalyserController;
 import gov.va.vinci.cm.Annotation;
 import gov.va.vinci.cm.Document;
 import gov.va.vinci.cm.DocumentInterface;
 import gov.va.vinci.v3nlp.registry.NlpComponent;
 import gov.va.vinci.v3nlp.services.NlpProcessingUnit;
 import hitex.gate.regex.ConceptFinder;
 import org.apache.commons.validator.GenericValidator;
 
 import java.util.List;
 
 /**
  * Hitex concept finder implementation using Regular Expressions.
  */
 public class HitexConceptFinderService extends BaseGateService implements NlpProcessingUnit {
     protected ProcessingResource resource;
 
     public HitexConceptFinderService(ProcessingResource resource) {
         this.resource = resource;
     }
 
     /**
      * Processing method
      *
      * @param config           The config string should be xml in the following format:
      *                         <?xml version="1.0" encoding="ISO-8859-1" ?>
      *                         <concepts>
      *                         <concept>
      *                         <def><![CDATA[(?i)pain]]></def>
      *                         <capt_group_num>0</capt_group_num>
      *                         <type>RegEx</type>
      *                         <name>Label</name>
      *                         <features>
      *                         <feature>
      *                         <name>code</name>
      *                         <value><![CDATA[]]></value>
      *                         </feature>
      *                         </features>
      *                         </concept>
      *                         </concepts>
      *                         <p/>
      *                         This would look for the regular expression pain in the corpus. Assuming sectionizing had been done, it is
      *                         limited to DIAG and ADM sections. You can change <sections> to <sections exlucde='true'> to ignore sections.
      * @param originalDocument The Document to be processed
      * @return the annotated corpus.
      */
     public DocumentInterface process(String config, DocumentInterface originalDocument, List<NlpComponent> previousModuleProvided) {
         SerialAnalyserController controller = null;
         ConceptFinder regexConceptFinder = (ConceptFinder) resource;
         gov.va.vinci.cm.Corpus results = new gov.va.vinci.cm.Corpus();
         gate.Corpus crp = null;
 
         List<Annotation> toProcess = getProcessList(originalDocument, previousModuleProvided);
 
         try {
             /** Create a controller to parse all annotations. **/
             controller = (SerialAnalyserController) Factory.createResource(
                     "gate.creole.SerialAnalyserController", Factory
                     .newFeatureMap(), Factory.newFeatureMap(), "V3NLP");
 
             for (Annotation a : toProcess) {
                 controller.reInit();
 
                 /** Create gate corpus. **/
                 crp = Factory.newCorpus("V3NLP Corpus From Common Model");
 
                 String key = (String) originalDocument.getDocumentName();
                 if (key == null) {
                     key = originalDocument.getDocumentId();
                 }
                 gate.Document doc = Factory.newDocument(a.getContent());
                 doc.setName(key);
                 crp.add(doc);
                 /** End create gate corpus. **/
 
                 controller.setCorpus(crp);
                 addRegEx(getRegExFilter(config), controller, regexConceptFinder);
 
                 // run the application
                 controller.execute();
 
                 // Add results to the document;
                processGateResults(crp, (Document) originalDocument, a.getBeginOffset(), "GATE|" +resource.getClass().getCanonicalName());
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         } finally {
             cleanupPipeLine(controller, crp);
         }
         return originalDocument;
     }
 
     private String getRegExFilter(String config) {
         StringBuffer filter = new StringBuffer();
         if (GenericValidator.isBlankOrNull(config)) {
             return "";
         }
         for (String line : config.split(
                 "\r\n|\r|\n")) {
             if (!org.apache.commons.validator.GenericValidator
                     .isBlankOrNull(line)) {
                 filter.append(line + "\n");
                 if (line.trim().equals("</concepts>")) {
                     return filter.toString();
                 }
             }
         }
 
         return filter.toString();
     }
 
     private void addRegEx(String regExFilter,
                           SerialAnalyserController controller,
                           ConceptFinder regexConceptFinder) {
 
         regexConceptFinder.setRulesXML(regExFilter);
         regexConceptFinder.setUseSections(false);
         controller.add(regexConceptFinder);
     }
 
     @Override
     public void initialize() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void destroy() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
