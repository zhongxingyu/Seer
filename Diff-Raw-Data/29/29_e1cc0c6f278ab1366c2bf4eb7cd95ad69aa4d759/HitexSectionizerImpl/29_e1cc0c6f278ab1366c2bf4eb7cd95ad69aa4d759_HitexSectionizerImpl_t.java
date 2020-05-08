 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp.services.gate;
 
 import gate.AnnotationSet;
 import gate.Corpus;
 import gate.Factory;
 import gate.creole.SerialAnalyserController;
 import gate.util.InvalidOffsetException;
 import gov.va.vinci.cm.Annotation;
 import gov.va.vinci.cm.DocumentInterface;
 import gov.va.vinci.v3nlp.NlpUtilities;
 import gov.va.vinci.v3nlp.registry.NlpComponent;
 import gov.va.vinci.v3nlp.services.NlpProcessingUnit;
 import hitex.gate.Sectionizer;
 import org.apache.commons.validator.GenericValidator;
 import org.apache.xerces.dom.DocumentImpl;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class HitexSectionizerImpl extends BaseGateService implements NlpProcessingUnit {
 
 
     /**
      * Process method
      *
      * @param config This is an xml configuration for custom section headers. Not needed
      *               if using the standard (vi tconfigured) section headers. If passing in a custom
      *               configuration, it should be in the format:
      *               <?xml version="1.0" encoding="ISO-8859-1" ?>
      *               <!--  This file contains header definitions for Sectionizer NLP component -->
      *               <headers>
      *               <header categories="OTHER" captGroupNum="0" >
      *               <![CDATA[(?i)\(BILATERAL\)\s{1,3}CAROTID\s{0,3}(:|;)]]>
      *               </header>
      *               <header categories="OTHER" captGroupNum="0" >
      *               <![CDATA[(?i)\(BILATERAL\)\s{1,3}FEMORAL\s{0,3}(:|;)]]>
      *               </header>
      *               <header categories="OTHER" captGroupNum="0" >
      *               <![CDATA[(?i)ABD\s{0,3}(:|;)]]>
      *               </header>
      *               <includes>
      *               <include>section to include in output</include>
      *               </includes>
      *               <excludes>
      *               <exclude>Section to exclude in output</exclude>
      *               </excludes>
      *               </headers>
      * @param d      The Document to processes.
      * @param previousModuleProvided the list of NLP Components that previous modules provided.
      * @return Corpus annotated with section_header and section annotations denoting the section_headers
      *         and section content.
      */
     @Override
     public DocumentInterface process(String config, DocumentInterface d, List<NlpComponent> previousModuleProvided) {
         SerialAnalyserController controller = null;
         Corpus corpus = null;
         gov.va.vinci.cm.Corpus results = new gov.va.vinci.cm.Corpus();
 
         try {
             controller = (SerialAnalyserController) Factory.createResource(
                     "gate.creole.SerialAnalyserController", Factory
                     .newFeatureMap(), Factory.newFeatureMap(), "V3NLP");
             controller.reInit();
 
             Sectionizer s = addSectionizer(config, controller);
 
             corpus = createGateCorpusFromCommonModel(d);
             controller.setCorpus(corpus);
 
             // run the application
             controller.execute();
 
             DocumentInterface resultDoc = processGateResults(corpus, (gov.va.vinci.cm.Document) d, 0,
                    getSectionizerConfigIncludes(config), getSectionizerConfigExcludes(config), "GATE:" + s.getClass().getCanonicalName());
             return (resultDoc);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         } finally {
             cleanupPipeLine(controller, corpus);
         }
     }
 
     /**
      * Create a sectionizer. Overridden in Spring with Method Injection to
      * return a new Sectionizer for every request.
      *
      * @return a Sectionizer.
      */
     public Sectionizer createSectionizer() {
         return null;
     }
 
     private Sectionizer addSectionizer(String customSectionizerRules,
                                 SerialAnalyserController controller) {
         Sectionizer s = this.createSectionizer();
 
         if (!GenericValidator.isBlankOrNull(customSectionizerRules)) {
             try {
                 Document d = sectionizerConfigHeaders(customSectionizerRules);
                 s.setHeadersList(d);
                 s.setIncludeUnclassified(false);
             } catch (Exception e) {
                 throw new RuntimeException(e.getMessage());
             }
         }
         controller.add(s);
         return s;
     }
 
     private static List<String> getElementList(String rawConfig, String elementName) {
         Document doc = null;
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
 
             doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawConfig)));
             if (doc == null) {
                 throw new RuntimeException("Custom sectionizer rules parsing returned null.");
             }
 
             DocumentImpl resultDocument = new DocumentImpl();
             NodeList toReturnList = doc.getElementsByTagName(elementName);
 
             /**
              * Parse inclusion/exclusion lists.
              */
             List<String> inclusionList = new ArrayList<String>();
             for (int i = 0; i < toReturnList.getLength(); i++) {
                 inclusionList.add(toReturnList.item(i).getTextContent().trim());
             }
 
             return inclusionList;
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage());
         }
     }
 
     public static List<String> getSectionizerConfigIncludes(String rawConfig) {
         return getElementList(rawConfig, "include");
     }
 
     public static List<String> getSectionizerConfigExcludes(String rawConfig) {
         return getElementList(rawConfig, "exclude");
     }
 
     public static Document sectionizerConfigHeaders(String rawConfig) {
         Document doc = null;
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
 
             doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawConfig)));
             if (doc == null) {
                 throw new RuntimeException("Custom sectionizer rules parsing returned null.");
             }
 
             DocumentImpl resultDocument = new DocumentImpl();
             Element resultRoot = resultDocument.createElement("headers");
 
             NodeList headerRecords = doc.getElementsByTagName("header");
 
             /**
              * Build an appropriate configuration file from the header config and inclusion/exclusion list.
              */
             for (int i = 0; i < headerRecords.getLength(); i++) {
                 Node n = headerRecords.item(i);
                 resultRoot.appendChild(resultDocument.importNode(n, true));
             }
 
             resultDocument.appendChild(resultRoot);
             return resultDocument;
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage());
         }
     }
 
 
     protected DocumentInterface processGateResults(gate.Corpus corpus, gov.va.vinci.cm.Document d,
                                                    Integer offset,
                                                    List<String> includes,
                                                    List<String> excludes,
                                                    String pedigree) throws InvalidOffsetException {
 
         gov.va.vinci.cm.Corpus results = new gov.va.vinci.cm.Corpus();
         List<String> docEnum = corpus.getDocumentNames();
         gate.Document gateDoc = (gate.Document) corpus.iterator().next();
 
         if (docEnum.size() != 1) {
             throw new RuntimeException("Processing module returned " + docEnum.size() + ". It should have returned one document.");
         }
 
         if (d == null) {
             d = new gov.va.vinci.cm.Document();
             d.setContent(gateDoc.getContent().toString());
         }
 
 
         List<Annotation> resultsToAdd =  processDocumentForReturn(gateDoc, offset, includes, excludes, pedigree);
         mergeAnnotations(d, resultsToAdd);
 
         return d;
     }
 
     /**
      * This returns a list of common model annotations from a gate document.
      *
      * @param gateDoc the gate document to get annotations from.
      * @param offset  Because we only send pieces of a larger document, this offset is the spot in the larger document
      *                where this piece resides. It is added to the gate annotation to get offsets relative to the larger document.
      * @return
      * @throws gate.util.InvalidOffsetException
      *
      */
     protected List<Annotation> processDocumentForReturn(gate.Document gateDoc, Integer offset, List<String> includes, List<String> excludes, String pedigree)
             throws InvalidOffsetException {
 
         AnnotationSet annotations = gateDoc.getAnnotations();
         List<Annotation> results = new ArrayList<Annotation>();
         Iterator<gate.Annotation> i = annotations.iterator();
 
         // No includes/excludes means no results.
         if (includes.size() == 0 && excludes.size() == 0) {
             return results;
         }
 
         // See if annotation is valid, and in the users selected list.
         while (i.hasNext()) {
             gate.Annotation a = i.next();
             String newCategories = "";
 
             if (((String) a.getFeatures().get("categories")) == null) {
                 continue;
             }
             String[] eachCat = ((String) a.getFeatures().get("categories")).split(",");
 
             for (String currentNodeCat : eachCat) {
                 currentNodeCat = currentNodeCat.trim();
                 if (excludes.contains(currentNodeCat)) {
                     //   System.out.println("Need to do exclusion:" + currentNodeCat);
                 } else if (includes.size() > 0 && !includes.contains(currentNodeCat)) {
                     //   System.out.println("Not in inclusion list, and inclusion list is populated:" + currentNodeCat);
                 } else {
                     newCategories += currentNodeCat + ", ";
                 }
             }
 
             // Only add the annotation if it has any categories.
             if (newCategories.trim().length() > 1) {
                 a.getFeatures().remove("categories");
                 a.getFeatures().put("categories", newCategories.substring(0, newCategories.length() - 2));
 
                 results.add(NlpUtilities.convertAnnotation(a,
                         gateDoc.getContent().getContent(a.getStartNode().getOffset(), a.getEndNode().getOffset()).toString(),
                         offset, pedigree));
             }
 
 
         }
         return results;
     }
 }
