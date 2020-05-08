 /*
  * Copyright (c) 2010, Hamish Morgan.
  * All Rights Reserved.
  */
 package uk.ac.susx.mlcl.erl.snlp;
 
 import eu.ac.susx.mlcl.erl.xml.AnnotationToXML;
 import com.google.common.io.Closeables;
 import edu.stanford.nlp.StanfordNLPTest;
 import edu.stanford.nlp.ling.CoreAnnotations;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.util.CoreMap;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Properties;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import uk.ac.susx.mlcl.erl.test.AbstractTest;
 
 /**
  *
  * @author hamish
  */
 public class EntityLinkingAnnotatorTest extends AbstractTest {
 
     public EntityLinkingAnnotatorTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of annotate method, of class EntityLinkingAnnotator.
      */
     @Test
     public void testAnnotate() throws IOException, ClassNotFoundException {
 
 
 //        Properties props = new Properties();
 //        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
 //        props.put("ner.applyNumericClassifiers", "false");
 //        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
 
         EntityLinkingAnnotator instance = EntityLinkingAnnotator.newInstance();
 
 //        pipeline.addAnnotator(instance);
 
         Annotation document = StanfordNLPTest.loadAnnotation(
                 new File(TEST_DATA_PATH,
                          "freebase_brighton.txt-tok-sent-pos-lemma-ner.s.gz"));
 
         System.out.println("document: " + document);

        // XXX: This fails because the TextAnnotation is somehow empty at the document level after
        // deserialization. (The annotation exists, but it's an empty string... >:-( )
         instance.annotate(document);
 
         System.out.println(document);
 
         List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
 
         for (CoreMap sentence : sentences) {
             // traversing the words in the current sentence
             // a CoreLabel is a CoreMap with additional token-specific methods
             for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                 String word = token.get(CoreAnnotations.TextAnnotation.class);
                 String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                 String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                 String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                 String el = token.get(EntityLinkingAnnotator.EntityKbIdAnnotation.class);
 
 
                 System.out.printf("[%d,%d] %s\t%s\t%s\t%s\t%s%n",
                                   token.beginPosition(), token.endPosition(),
                                   word, pos, lemma, ne, el);
             }
 
         }
 
         StanfordNLPTest.saveAnnotation(document,
                                        new File(TEST_DATA_PATH,
                                                 "freebase_brighton.txt-tok-sent-pos-lemma-er-el.s.gz"));
 
 
         OutputStream os = null;
         try {
             os = new BufferedOutputStream(new FileOutputStream(
                     new File(TEST_DATA_PATH,
                              "freebase_brighton.txt-tok-sent-pos-lemma-er-el.xml")));
             Properties props = new Properties();
             props.put("annotators", "tokenize");
             StanfordCoreNLP s = new StanfordCoreNLP(props);
 
             s.xmlPrint(document, os);
             os.flush();
         } finally {
             Closeables.closeQuietly(os);
         }
 
 //        
 //        System.out.println("annotate");
 //        Annotation annotation = null;
 //        EntityLinkingAnnotator instance = null;
 //        instance.annotate(annotation);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
     }
 
     /**
      * Test of annotate method, of class EntityLinkingAnnotator.
      */
     @Test
     public void foo() throws IOException, ClassNotFoundException, InstantiationException, Exception {
 
 
         Annotation document = StanfordNLPTest.loadAnnotation(
                 new File(TEST_DATA_PATH,
                          "freebase_brighton.txt-tok-sent-pos-lemma-er-el.s.gz"));
 
         AnnotationToXML ax = AnnotationToXML.builder().build();
 
         ax.xmlPrint(document, System.out);
     }
 
     @Test
     public void foo2() throws IOException, ClassNotFoundException, InstantiationException, Exception {
 
 
         Annotation document = StanfordNLPTest.loadAnnotation(
                 new File(TEST_DATA_PATH,
                          "freebase_brighton.txt-tok-sent-parse.s.gz"));
 
         AnnotationToXML ax = AnnotationToXML.builder().build();
 
         ax.xmlPrint(document, System.out);
     }
 
     @Test
     public void foo3() throws IOException, ClassNotFoundException, InstantiationException, Exception {
 
 
         Annotation document = StanfordNLPTest.loadAnnotation(
                 new File(TEST_DATA_PATH,
                          "freebase_brighton.txt-tok-sent-parse-lemma-ner-dcoref.s.gz"));
 
 
         AnnotationToXML ax = AnnotationToXML.builder().build();
 
         ax.xmlPrint(document, System.out);
     }
 //    
 //    /**
 //     * Test of newInstance method, of class EntityLinkingAnnotator.
 //     */
 //    @Test
 //    public void testNewInstance() throws Exception {
 //        System.out.println("newInstance");
 //        EntityLinkingAnnotator expResult = null;
 //        EntityLinkingAnnotator result = EntityLinkingAnnotator.newInstance();
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
 //    }
 //
 //    /**
 //     * Test of getRequiredAnnotations method, of class EntityLinkingAnnotator.
 //     */
 //    @Test
 //    public void testGetRequiredAnnotations() {
 //        System.out.println("getRequiredAnnotations");
 //        EntityLinkingAnnotator instance = null;
 //        Set expResult = null;
 //        Set result = instance.getRequiredAnnotations();
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
 //    }
 //
 //    /**
 //     * Test of getSuppliedAnnotations method, of class EntityLinkingAnnotator.
 //     */
 //    @Test
 //    public void testGetSuppliedAnnotations() {
 //        System.out.println("getSuppliedAnnotations");
 //        EntityLinkingAnnotator instance = null;
 //        Set expResult = null;
 //        Set result = instance.getSuppliedAnnotations();
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
 //    }
 //
 //
 //    /**
 //     * Test of linkFor method, of class EntityLinkingAnnotator.
 //     */
 //    @Test
 //    public void testLinkFor() {
 //        System.out.println("linkFor");
 //        Annotation annotation = null;
 //        CoreLabel token = null;
 //        EntityLinkingAnnotator instance = null;
 //        String expResult = "";
 //        String result = instance.linkFor(annotation, token);
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
 //    }
 }
