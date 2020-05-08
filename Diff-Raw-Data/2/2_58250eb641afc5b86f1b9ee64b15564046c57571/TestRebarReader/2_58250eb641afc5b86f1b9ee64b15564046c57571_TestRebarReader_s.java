 /**
  * 
  */
 package edu.jhu.hlt.rebar.accumulo;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.accumulo.core.client.BatchScanner;
 import org.apache.accumulo.core.client.IteratorSetting;
 import org.apache.accumulo.core.client.Scanner;
 import org.apache.accumulo.core.client.ScannerBase;
 import org.apache.accumulo.core.client.TableNotFoundException;
 import org.apache.accumulo.core.data.Key;
 import org.apache.accumulo.core.data.Range;
 import org.apache.accumulo.core.data.Value;
 import org.apache.accumulo.core.iterators.user.WholeRowIterator;
 import org.apache.hadoop.io.Text;
 import org.apache.thrift.TException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.jhu.hlt.concrete.Communication;
 import edu.jhu.hlt.concrete.LangId;
 import edu.jhu.hlt.concrete.LanguagePrediction;
 import edu.jhu.hlt.concrete.Stage;
 import edu.jhu.hlt.concrete.StageType;
 import edu.jhu.hlt.rebar.Constants;
 import edu.jhu.hlt.rebar.Configuration;
 import edu.jhu.hlt.rebar.RebarException;
 import edu.jhu.hlt.rebar.Util;
 
 /**
  * @author max
  * 
  */
 public class TestRebarReader extends AbstractAccumuloTest {
 
   private RebarReader rr;
 
   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
     this.initialize();
     this.rr = new RebarReader(this.conn);
   }
 
   /**
    * @throws java.lang.Exception
    */
   @After
   public void tearDown() throws Exception {
     this.rr.close();
   }
 
   /**
    * Test method for {@link edu.jhu.hlt.rebar.accumulo.RebarReader#getAnnotatedDocuments(com.maxjthomas.dumpster.Stage)}.
    * 
    * @throws Exception
    * @throws RebarException
    */
   @Test
   public void testGetAnnotatedDocuments() throws RebarException, Exception {
     int nDocs = 3;
     List<Communication> docList = this.ingestDocuments(nDocs);
 
     Set<Communication> docsWithLid = new HashSet<>();
 
     List<LangId> langIdList = new ArrayList<>();
     Stage s = generateTestStage();
     try (RebarAnnotator ra = new RebarAnnotator(this.conn);) {
       for (Communication d : docList) {
         LangId mockLid = generateLangId(d);
         ra.addLanguageId(d, s, mockLid);
         langIdList.add(mockLid);
         Communication newDoc = new Communication(d);
         newDoc.lid = mockLid;
         docsWithLid.add(newDoc);
       }
     }
 
     int annotatedDocs = 0;
     Set<String> idSet;
     try (RebarStageHandler ash = new RebarStageHandler(this.conn);) {
       annotatedDocs = ash.getAnnotatedDocumentCount(s);
       idSet = ash.getAnnotatedDocumentIds(s);
     }
 
     assertEquals("Should get n annotated docs: (n = " + nDocs + ")", nDocs, annotatedDocs);
     BatchScanner bsc = this.rr.createScanner(s, idSet);
     assertEquals("Should get " + nDocs + " entries in this batch scanner.", 3, Util.countIteratorResults(bsc.iterator()));
     bsc.close();
 
     List<Communication> fetchedDocs = this.rr.getAnnotatedCommunications(s);
    assertEquals("Documents with LID should be the same.", new ArrayList<>(docsWithLid), fetchedDocs);
   }
 
   /**
    * Test method for {@link edu.jhu.hlt.rebar.accumulo.RebarReader#getAnnotatedDocuments(com.maxjthomas.dumpster.Stage)}.
    * 
    * @throws Exception
    * @throws RebarException
    */
   @Test
   public void testGetAnnotatedDocumentsStageDependency() throws RebarException, Exception {
     // create the stages
     Stage stageA = new Stage("stage_max_lid_test", "Testing stage for LID", Util.getCurrentUnixTime(), new HashSet<String>(), StageType.LANG_ID);
     Set<String> stageBDeps = new HashSet<>();
     stageBDeps.add(stageA.name);
     Stage stageB = new Stage("stage_max_lp_test", "Testing stage for LP", Util.getCurrentUnixTime(), stageBDeps, StageType.LANG_PRED);
 
     // ingest documents
     int nDocs = 3;
     List<Communication> docList = this.ingestDocuments(nDocs);
 
     // annotate documents
     try (RebarAnnotator ra = new RebarAnnotator(this.conn);) {
       for (Communication d : docList) {
         LangId mockLid = generateLangId(d);
         ra.addLanguageId(d, stageA, mockLid);
       }
     }
 
     List<Communication> fetchedDocs = this.rr.getAnnotatedCommunications(stageA);
 
     Set<Communication> docsWithLid = new HashSet<>();
     try (RebarAnnotator ra = new RebarAnnotator(this.conn);) {
       // add language prediction annotation
       for (Communication d : fetchedDocs) {
         LangId lid = d.lid;
         LanguagePrediction lp = new LanguagePrediction();
         Entry<String, Double> e = lid.languageToProbabilityMap.entrySet().iterator().next();
 
         switch (e.getKey()) {
         case "eng":
           lp.predictedLanguage = "English";
           break;
         case "fra":
           lp.predictedLanguage = "French";
           break;
         case "spa":
           lp.predictedLanguage = "Spanish";
           break;
         default:
           lp.predictedLanguage = "Unknown";
         }
 
         ra.addLanguagePrediction(d, stageB, lp);
         Communication newDoc = new Communication(d);
         newDoc.language = lp;
         docsWithLid.add(newDoc);
       }
     }
     
     int annotatedDocs = 0;
     try (RebarStageHandler ash = new RebarStageHandler(this.conn);) {
       annotatedDocs = ash.getAnnotatedDocumentCount(stageB);
     }
 
     assertEquals("Should get n annotated docs: (n = " + nDocs + ")", nDocs, annotatedDocs);
 
     fetchedDocs = this.rr.getAnnotatedCommunications(stageB);
     assertTrue("Documents with LID should be the same.", fetchedDocs.containsAll(docsWithLid));
   }
   
   /**
    * Test method for {@link edu.jhu.hlt.rebar.accumulo.RebarReader#getAnnotatedDocuments(com.maxjthomas.dumpster.Stage)}.
    * 
    * @throws Exception
    * @throws RebarException
    */
   @Test
   public void testGetAnnotatedDocumentsManyStages() throws RebarException, Exception {
     // create the stages
     Stage stageA = new Stage("stage_max_lid_test", "Testing stage for LID", Util.getCurrentUnixTime(), new HashSet<String>(), StageType.LANG_ID);
     Set<String> stageBDeps = new HashSet<>();
     stageBDeps.add(stageA.name);
     Stage stageB = new Stage("stage_max_lp_test", "Testing stage for LP", Util.getCurrentUnixTime(), stageBDeps, StageType.LANG_PRED);
     Stage stageC = new Stage("stage_max_lid_test_v2", "Testing stage for LID", Util.getCurrentUnixTime(), new HashSet<String>(), StageType.LANG_ID);
     Set<String> stageDDeps = new HashSet<>();
     stageDDeps.add(stageC.name);
     Stage stageD = new Stage("stage_max_lp_test_v2", "Testing stage for LP", Util.getCurrentUnixTime(), stageDDeps, StageType.LANG_PRED);
 
     // ingest documents
     int nDocs = 1;
     List<Communication> docList = this.ingestDocuments(nDocs);
 
     // annotate documents
     try (RebarAnnotator ra = new RebarAnnotator(this.conn);) {
       for (Communication d : docList) {
         LangId mockLid = generateLangId(d);
         ra.addLanguageId(d, stageA, mockLid);
         mockLid = generateLangId(d);
         ra.addLanguageId(d, stageC, mockLid);
       }
     }
 
     List<Communication> fetchedDocs = this.rr.getAnnotatedCommunications(stageA);
 
     Set<Communication> docsWithLid = new HashSet<>();
     Set<Communication> docsWithLidSetD = new HashSet<>();
     try (RebarAnnotator ra = new RebarAnnotator(this.conn);) {
       // add language prediction annotation
       for (Communication d : fetchedDocs) {
         LangId lid = d.lid;
         LanguagePrediction lp = new LanguagePrediction();
         Entry<String, Double> e = lid.languageToProbabilityMap.entrySet().iterator().next();
 
         switch (e.getKey()) {
         case "eng":
           lp.predictedLanguage = "English";
           break;
         case "fra":
           lp.predictedLanguage = "French";
           break;
         case "spa":
           lp.predictedLanguage = "Spanish";
           break;
         default:
           lp.predictedLanguage = "Unknown";
         }
 
         ra.addLanguagePrediction(d, stageB, lp);
         
         
         Communication newDoc = new Communication(d);
         newDoc.language = lp;
         docsWithLid.add(newDoc);
       }
       
       for (Communication d : this.rr.getAnnotatedCommunications(stageC)) {
         LanguagePrediction lpD = new LanguagePrediction();
         lpD.predictedLanguage = "Swahili";
         ra.addLanguagePrediction(d, stageD, lpD);
         
         Communication dDoc = new Communication(d);
         dDoc.language = lpD;
         docsWithLidSetD.add(dDoc);
       }
     }
     
     int annotatedDocs = 0;
     try (RebarStageHandler ash = new RebarStageHandler(this.conn);) {
       annotatedDocs = ash.getAnnotatedDocumentCount(stageB);
     }
     assertEquals("Should get n annotated docs: (n = " + nDocs + ")", nDocs, annotatedDocs);
     
     try (RebarStageHandler ash = new RebarStageHandler(this.conn);) {
       annotatedDocs = ash.getAnnotatedDocumentCount(stageD);
     }
     assertEquals("Should get n annotated docs: (n = " + nDocs + ")", nDocs, annotatedDocs);
 
     fetchedDocs = this.rr.getAnnotatedCommunications(stageB);
     assertEquals("Documents with LID should be the same.", new ArrayList<>(docsWithLid), fetchedDocs);
     
     fetchedDocs = this.rr.getAnnotatedCommunications(stageD);
     assertEquals("Documents with LID in set D should be the same.", new ArrayList<>(docsWithLidSetD), fetchedDocs);
   }
 }
