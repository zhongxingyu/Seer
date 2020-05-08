 package org.spantus.extr.wordspot.service.impl.test;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.junit.Assert;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.spantus.core.beans.RecognitionResult;
 import org.spantus.core.beans.SignalSegment;
 import org.spantus.core.junit.SlowTests;
 import org.spantus.core.threshold.ClassifierEnum;
 import org.spantus.extr.wordspot.domain.SegmentExtractorServiceConfig;
 import org.spantus.extr.wordspot.guava.RecognitionResultSignalSegmentOrder;
 import org.spantus.extr.wordspot.service.impl.SyllableSpottingServiceImpl;
 import org.spantus.extr.wordspot.service.impl.WordSpottingListenerLogImpl;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Ordering;
 
 public class WordSpottingServiceImplTest extends AbstractSegmentExtractorTest {
 
     SyllableSpottingServiceImpl wordSpottingServiceImpl;
     private File repositoryPathWord;
     private File repositoryPathSyllable;
     private String searchWord;
     private String[] acceptableSyllables;
 
     protected static final Ordering<Entry<RecognitionResult, SignalSegment>> order = new RecognitionResultSignalSegmentOrder();
     
     protected File createRepositoryPathWord(File rootPath){
         return  new File(rootPath, "CORPUS/word");
     }
     
     protected File createRepositoryPathSyllable(File rootPath){
         return  new File(rootPath, "CORPUS/phone");
     }
 
     @Before
     @Override
     public void setUp() throws Exception {
         super.setUp();
         repositoryPathWord =createRepositoryPathWord(getRepositoryPathRoot());
         repositoryPathSyllable = createRepositoryPathSyllable(getRepositoryPathRoot());
         setSearchWord("padeda");
         setAcceptableSyllables(new String[]{"pa", "de", "da"});
         wordSpottingServiceImpl = new SyllableSpottingServiceImpl(repositoryPathSyllable.getAbsolutePath());
         wordSpottingServiceImpl.setSegmentExtractorService(getSegmentExtractorService());
         wordSpottingServiceImpl.setServiceConfig(serviceConfig);
     }
 
 
     @Override
     protected void changeOtherParams(SegmentExtractorServiceConfig config) {
         super.changeOtherParams(config);
         getSegmentExtractorService().getServiceConfig().setClassifier(ClassifierEnum.rulesOnline);
     }
 
    @Test @Ignore
     @Category(SlowTests.class)
     public void test_wordSpotting() throws MalformedURLException {
         //given
         //Long length = 1000L * AudioManagerFactory.createAudioManager().findLength(getWavFile().toURI().toURL()).longValue();
         WordSpottingListenerLogImpl listener = new WordSpottingListenerLogImpl(getSearchWord(), getAcceptableSyllables(), repositoryPathWord.getAbsolutePath());
         listener.setServiceConfig(serviceConfig);
         URL url = getWavFile().toURI().toURL();
         //when 
         //long started = System.currentTimeMillis();
         wordSpottingServiceImpl.wordSpotting(url, listener);
         //long ended = System.currentTimeMillis();
         Map<RecognitionResult, SignalSegment> segments = listener.getWordSegments();
         String resultsStr = extractResultStr(segments);
 
         //then
         //Assert.assertTrue("read time " + length + ">"+(ended-started), length > ended-started);
         Assert.assertEquals("Recognition", "padeda", resultsStr);
 
     }
 
     public File getRepositoryPathSyllable() {
         return repositoryPathSyllable;
     }
 
     public void setRepositoryPathSyllable(File repositoryPathSyllable) {
         this.repositoryPathSyllable = repositoryPathSyllable;
     }
 
     public File getRepositoryPathWord() {
         return repositoryPathWord;
     }
 
     public void setRepositoryPathWord(File repositoryPathWord) {
         this.repositoryPathWord = repositoryPathWord;
     }
 
     public String getSearchWord() {
         return searchWord;
     }
 
     public void setSearchWord(String searchWord) {
         this.searchWord = searchWord;
     }
 
     public String[] getAcceptableSyllables() {
         return acceptableSyllables;
     }
 
     public void setAcceptableSyllables(String[] acceptableSyllables) {
         this.acceptableSyllables = acceptableSyllables;
     }
 
     protected String extractResultStr(Map<RecognitionResult, SignalSegment> segments) {
     	List<Entry<RecognitionResult, SignalSegment>> orderedValues = order.sortedCopy(segments.entrySet());
         Joiner joiner = Joiner.on(";").skipNulls();
         Collection<String> resultsCollection = Collections2.transform(orderedValues,
                 new Function<Map.Entry<RecognitionResult, SignalSegment>, String>() {
                     @Override
                     public String apply(Entry<RecognitionResult, SignalSegment> input) {
                         return input.getKey().getInfo().getName()//+  input.getValue().getMarker().getStart()
                         		;
                     }
                 });
 
         String resultsStr = joiner.join(resultsCollection);
         return resultsStr;
     }
 }
