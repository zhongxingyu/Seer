 package txtfnnl.uima.analysis_component;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import org.apache.uima.analysis_engine.AnalysisEngine;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.impl.ChildUimaContext_impl;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.impl.JCasImpl;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import org.easymock.EasyMock;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.uimafit.factory.AnalysisEngineFactory;
 import org.uimafit.testing.util.DisableLogging;
 
 import txtfnnl.uima.Views;
 import txtfnnl.uima.analysis_component.LinkGrammarAnnotator.Builder;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 import txtfnnl.uima.tcas.SyntaxAnnotation;
 import txtfnnl.utils.Offset;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ LinkParser.class, LinkGrammarAnnotator.class, ChildUimaContext_impl.class,
     AnalysisEngineFactory.class, SyntaxAnnotation.class, JCasImpl.class })
 public class TestLinkGrammarAnnotator {
   static Builder aeBuilder;
   static AnalysisEngine sentenceAnnotator;
   JCas baseJCas;
   JCas textJCas;
   static final String SENTENCE = "ARL, a regulator of cell death localized inside the "
       + "nucleus, has been shown to bind the p53 promoter.";
   // output for testSentence as produced with default settings using LGP 4.7.6:
   // link-parser -constituents=3 -verbosity=0 -timeout=15
   static final String RESULT = "(S (NP (NP ARL) , (NP (NP a regulator) (PP (NP of cell "
       + "death) (VP localized (PP inside (NP the nucleus))))) ,) (VP has (VP been (VP shown "
       + "(S (VP to (VP bind (NP the p53 promoter))))))) .)";
 
   @Before
   public void setUp() throws Exception {
     DisableLogging.disableLogging();
     // set up AE descriptor under test
     PowerMock.createPartialMock(JCasImpl.class, "addFsToIndexes");
     if (sentenceAnnotator == null)
       sentenceAnnotator = AnalysisEngineFactory.createAnalysisEngine(
           SentenceAnnotator.configure().create(), Views.CONTENT_TEXT.toString());
     aeBuilder = LinkGrammarAnnotator.configure();
     baseJCas = sentenceAnnotator.newJCas();
     textJCas = baseJCas.createView(Views.CONTENT_TEXT.toString());
   }
 
   @After
   public void tearDown() {
     DisableLogging.enableLogging(java.util.logging.Level.WARNING);
   }
 
   @Test
   public void testConfigure() throws Exception {
     PowerMock.mockStaticPartial(AnalysisEngineFactory.class, "createPrimitiveDescription");
     AnalysisEngineDescription mock = PowerMock.createMock(AnalysisEngineDescription.class);
     EasyMock.expect(AnalysisEngineFactory.createPrimitiveDescription(LinkGrammarAnnotator.class))
         .andReturn(mock);
     PowerMock.replay(AnalysisEngineFactory.class);
     Assert.assertEquals(mock, aeBuilder.create());
     PowerMock.verify(AnalysisEngineFactory.class);
   }
 
   @Test
   public void testConfigureWithDirectory() throws Exception {
     File dummy = File.createTempFile("dummy", "file").getParentFile();
     PowerMock.mockStaticPartial(AnalysisEngineFactory.class, "createPrimitiveDescription");
     AnalysisEngineDescription mock = PowerMock.createMock(AnalysisEngineDescription.class);
     EasyMock.expect(
         AnalysisEngineFactory.createPrimitiveDescription(LinkGrammarAnnotator.class,
             LinkGrammarAnnotator.PARAM_DICTIONARIES_PATH, dummy.getCanonicalPath())).andReturn(
         mock);
     PowerMock.replay(AnalysisEngineFactory.class);
     Assert.assertEquals(mock, aeBuilder.setDictionaryPath(dummy).create());
     PowerMock.verify(AnalysisEngineFactory.class);
   }
 
   @Test
   public void testConfigureWithTimeout() throws Exception {
     PowerMock.mockStaticPartial(AnalysisEngineFactory.class, "createPrimitiveDescription");
     AnalysisEngineDescription mock = PowerMock.createMock(AnalysisEngineDescription.class);
     EasyMock.expect(
         AnalysisEngineFactory.createPrimitiveDescription(LinkGrammarAnnotator.class,
             LinkGrammarAnnotator.PARAM_TIMEOUT_SECONDS, 123)).andReturn(mock);
     PowerMock.replay(AnalysisEngineFactory.class);
     Assert.assertEquals(mock, aeBuilder.setTimeout(123).create());
     PowerMock.verify(AnalysisEngineFactory.class);
   }
 
   @Test
   public void testIntialize() throws Exception {
     File path = File.createTempFile("linkparser.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     LinkParser parser = PowerMock.createMock(LinkParser.class);
     mockParser(parser, path, 15, logger);
     PowerMock.replay(parser, LinkParser.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDictionaryPath(path).setTimeout(15)
         .create();
     AnalysisEngineFactory.createAnalysisEngine(description, Views.CONTENT_TEXT.toString());
     PowerMock.verify(parser, LinkParser.class);
     PowerMock.verify(logger, Logger.class);
   }
 
   @Test
   public void testProcess() throws Exception {
     textJCas.setDocumentText(SENTENCE);
     sentenceAnnotator.process(baseJCas);
     File path = File.createTempFile("linkparser.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     LinkParser parser = PowerMock.createMock(LinkParser.class);
     mockParser(parser, path, 15, logger);
     mockProcess(parser, logger, SENTENCE, RESULT);
     logger.log(EasyMock.eq(Level.FINE),
         EasyMock.eq("dropping outer constituent {0} of {1} on span ''{2}''"),
         EasyMock.aryEq(new Object[] { "S", "VP", "to bind the p53 promoter" }));
     // replay!
     PowerMock.replay(parser, LinkParser.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDictionaryPath(path).setTimeout(15)
         .create();
     AnalysisEngine ae = AnalysisEngineFactory.createAnalysisEngine(description,
         Views.CONTENT_TEXT.toString());
     ae.process(baseJCas);
     PowerMock.verify(parser, LinkParser.class);
     PowerMock.verify(logger, Logger.class);    // OLD
   }
 
   @Test
   public void testAnnotate() throws Exception {
     textJCas.setDocumentText("A word.");
     sentenceAnnotator.process(baseJCas);
     File path = File.createTempFile("linkparser.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     LinkParser parser = PowerMock.createMock(LinkParser.class);
     mockParser(parser, path, 15, logger);
     SyntaxAnnotation ann = mockAnnotation(0, 6, "NP");
     EasyMock.expect(ann.getIdentifier()).andReturn("NP");
     EasyMock.expect(ann.getCoveredText()).andReturn("A word");
     ann.setIdentifier("VP");
     mockProcess(parser, logger, "A word.", "(S (NP (VP A word)).)");
     logger.log(EasyMock.eq(Level.FINE),
         EasyMock.eq("dropping outer constituent {0} of {1} on span ''{2}''"),
         EasyMock.aryEq(new Object[] { "NP", "VP", "A word" }));
     // replay!
     PowerMock.replay(parser, LinkParser.class);
     PowerMock.replay(ann, SyntaxAnnotation.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDictionaryPath(path).setTimeout(15)
         .create();
     AnalysisEngine ae = AnalysisEngineFactory.createAnalysisEngine(description,
         Views.CONTENT_TEXT.toString());
     ae.process(baseJCas);
     PowerMock.verify(parser, LinkParser.class);
     PowerMock.verify(ann, SyntaxAnnotation.class);
     PowerMock.verify(logger, Logger.class);
   }
 
   private void mockProcess(LinkParser parser, Logger logger, String sentence, String result)
       throws IOException {
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages",
         "UIMA_analysis_engine_process_begin__FINE",
         "txtfnnl.uima.analysis_component.LinkGrammarAnnotator");
     EasyMock.expect(parser.process(sentence)).andReturn(result);
     logger.log(Level.FINE, "sentence: ''{0}''", sentence);
     logger.log(Level.FINE, "constituents: {0}", result);
     logger.log(Level.FINER, "ignoring full-sentence length constituent {0}", "S");
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_process_end__FINE",
         "txtfnnl.uima.analysis_component.LinkGrammarAnnotator");
   }
 
   private void mockParser(LinkParser parser, File path, int timeout, Logger logger)
       throws Exception, IOException {
     PowerMock.stub(PowerMock.method(ChildUimaContext_impl.class, "getLogger")).toReturn(logger);
     PowerMock.expectNew(LinkParser.class, path.getCanonicalPath(), timeout, logger).andReturn(
         parser);
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_begin__CONFIG",
         "txtfnnl.uima.analysis_component.LinkGrammarAnnotator");
    logger.log(Level.CONFIG, "initialized LinkGrammar parser");
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_successful__CONFIG",
         "txtfnnl.uima.analysis_component.LinkGrammarAnnotator");
   }
 
   private SyntaxAnnotation mockAnnotation(int start, int end, String tag) throws Exception {
     SyntaxAnnotation ann = PowerMock.createMockAndExpectNew(SyntaxAnnotation.class, textJCas,
         new Offset(start, end));
     ann.setAnnotator(LinkGrammarAnnotator.URI);
     ann.setConfidence(1.0);
     ann.setNamespace(LinkGrammarAnnotator.NAMESPACE);
     ann.setIdentifier(tag);
     EasyMock.expect(ann.getView()).andReturn(textJCas.getCas());
     EasyMock.expect(ann.getAddress()).andReturn(1);
     return ann;
   }
 }
