 /**
  * 
  */
 package txtfnnl.uima.analysis_component;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import org.apache.uima.analysis_engine.AnalysisEngine;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.impl.RootUimaContext_impl;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import org.easymock.EasyMock;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.uimafit.factory.AnalysisEngineFactory;
 import org.uimafit.testing.util.DisableLogging;
 
 import txtfnnl.uima.analysis_component.GeniaTaggerAnnotator.Builder;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 
 /**
  * @author Florian Leitner
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ GeniaTaggerAnnotator.class, GeniaTagger.class, RootUimaContext_impl.class,
     AnalysisEngineFactory.class })
 public class TestGeniaTaggerAnnotator {
   Builder aeBuilder;
 
   @Before
   public void setUp() {
     DisableLogging.disableLogging();
     aeBuilder = GeniaTaggerAnnotator.configure();
   }
 
   @After
   public void tearDown() {
     DisableLogging.enableLogging(java.util.logging.Level.WARNING);
   }
 
   @Test
   public void testConfigure() throws Exception {
     PowerMock.mockStaticPartial(AnalysisEngineFactory.class, "createPrimitiveDescription");
     AnalysisEngineDescription mock = PowerMock.createMock(AnalysisEngineDescription.class);
     EasyMock.expect(AnalysisEngineFactory.createPrimitiveDescription(GeniaTaggerAnnotator.class))
         .andReturn(mock);
     PowerMock.replay(AnalysisEngineFactory.class);
     Assert.assertEquals(mock, aeBuilder.create());
     PowerMock.verify(AnalysisEngineFactory.class);
   }
 
   @Test
   public void testConfigureWithPath() throws Exception {
     File dummy = File.createTempFile("dummy", "file").getParentFile();
     PowerMock.mockStaticPartial(AnalysisEngineFactory.class, "createPrimitiveDescription");
     AnalysisEngineDescription mock = PowerMock.createMock(AnalysisEngineDescription.class);
     EasyMock.expect(
         AnalysisEngineFactory.createPrimitiveDescription(GeniaTaggerAnnotator.class,
             GeniaTaggerAnnotator.PARAM_DIRECTORY, dummy.getCanonicalPath())).andReturn(mock);
     PowerMock.replay(AnalysisEngineFactory.class);
     Assert.assertEquals(mock, aeBuilder.setDirectory(dummy).create());
     PowerMock.verify(AnalysisEngineFactory.class);
   }
 
   @Test
   public void testIntialize() throws Exception {
     File dictionariesPath = File.createTempFile("geniatagger.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     GeniaTagger tagger = PowerMock.createMock(GeniaTagger.class);
     mockTagger(tagger, dictionariesPath, logger);
     PowerMock.replay(tagger, GeniaTagger.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDirectory(dictionariesPath).create();
     AnalysisEngineFactory.createPrimitive(description);
     PowerMock.verify(tagger, GeniaTagger.class);
     PowerMock.verify(logger, Logger.class);
   }
 
   @Test
   public void testProcess() throws Exception {
     String sentence = "dummy phrase.";
     File dictionariesPath = File.createTempFile("geniatagger.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     GeniaTagger tagger = PowerMock.createMock(GeniaTagger.class);
     List<Token> result = new ArrayList<Token>();
     result.add(new Token("dummy\tSTEM\tpos1\tB-chunk\tner1"));
     result.add(new Token("phrase\tstem\tpos2\tI-chunk\tner2"));
     result.add(new Token(".\tStem\tpos3\tO\tner3"));
     mockTagger(tagger, dictionariesPath, logger);
     mockProcess(sentence, result, tagger, logger);
     // replay!
     PowerMock.replay(tagger, GeniaTagger.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDirectory(dictionariesPath).create();
     AnalysisEngine geniaAE = AnalysisEngineFactory.createPrimitive(description);
     AnalysisEngine sentenceAE = AnalysisEngineFactory.createPrimitive(SentenceAnnotator
         .configure().create());
     JCas jcas = sentenceAE.newJCas();
     jcas.setDocumentText(sentence);
     sentenceAE.process(jcas);
     geniaAE.process(jcas);
     PowerMock.verify(tagger, GeniaTagger.class);
     PowerMock.verify(logger, Logger.class);
   }
 
   @Test
   public void testDoubleQuoteFix() throws Exception {
     String sentence = "dummy \"phrase\".";
     File dictionariesPath = File.createTempFile("geniatagger.", "dir").getParentFile();
     Logger logger = PowerMock.createMock(Logger.class);
     GeniaTagger tagger = PowerMock.createMock(GeniaTagger.class);
     List<Token> result = new ArrayList<Token>();
     result.add(new Token("dummy\tSTEM\tpos1\tB-chunk\tner1"));
     result.add(new Token("``\tsTeM\tpos2\tI-chunk\tner2"));
     result.add(new Token("phrase\tstem\tpos3\tI-chunk\tner3"));
     result.add(new Token("``\tStEm\tpos4\tI-chunk\tner4"));
     result.add(new Token(".\tStem\tpos5\tO\tner5"));
     mockTagger(tagger, dictionariesPath, logger);
     mockProcess(sentence, result, tagger, logger);
     // replay!
     PowerMock.replay(tagger, GeniaTagger.class);
     PowerMock.replay(logger, Logger.class);
     AnalysisEngineDescription description = aeBuilder.setDirectory(dictionariesPath).create();
     AnalysisEngine geniaAE = AnalysisEngineFactory.createPrimitive(description);
     AnalysisEngine sentenceAE = AnalysisEngineFactory.createPrimitive(SentenceAnnotator
         .configure().create());
     JCas jcas = sentenceAE.newJCas();
     jcas.setDocumentText(sentence);
     sentenceAE.process(jcas);
     geniaAE.process(jcas);
     PowerMock.verify(tagger, GeniaTagger.class);
     PowerMock.verify(logger, Logger.class);
   }
 
   private void mockTagger(GeniaTagger tagger, File dictionariesPath, Logger logger)
       throws Exception, IOException {
     PowerMock.stub(PowerMock.method(RootUimaContext_impl.class, "getLogger")).toReturn(logger);
     PowerMock.expectNew(GeniaTagger.class, dictionariesPath.getCanonicalPath(), logger).andReturn(
         tagger);
    logger.log(Level.CONFIG, "initialized GENIA tagger");
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_begin__CONFIG",
         "txtfnnl.uima.analysis_component.GeniaTaggerAnnotator");
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_successful__CONFIG",
         "txtfnnl.uima.analysis_component.GeniaTaggerAnnotator");
   }
 
   private void mockProcess(String sentence, List<Token> result, GeniaTagger tagger, Logger logger)
       throws IOException {
     EasyMock.expect(tagger.process(sentence)).andReturn(result);
     logger.log(Level.FINE, "annotated {0} tokens", result.size());
    logger.log(Level.CONFIG, "no newline-based splitting");
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_begin__CONFIG",
         "txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator");
     logger.logrb(Level.CONFIG,
         "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl", "initialize",
         "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_init_successful__CONFIG",
         "txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator");
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages",
         "UIMA_analysis_engine_process_begin__FINE",
         "txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator");
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_process_end__FINE",
         "txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator");
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages",
         "UIMA_analysis_engine_process_begin__FINE",
         "txtfnnl.uima.analysis_component.GeniaTaggerAnnotator");
     logger.logrb(Level.FINE, "org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl",
         "process", "org.apache.uima.impl.log_messages", "UIMA_analysis_engine_process_end__FINE",
         "txtfnnl.uima.analysis_component.GeniaTaggerAnnotator");
   }
 }
