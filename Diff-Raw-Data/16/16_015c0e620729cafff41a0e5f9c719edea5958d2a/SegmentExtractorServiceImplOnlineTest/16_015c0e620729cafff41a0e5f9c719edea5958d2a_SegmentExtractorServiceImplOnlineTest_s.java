 package org.spantus.extr.wordspot.service.impl.test;
 
 //import static org.junit.Assert.fail;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.sound.sampled.AudioInputStream;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.spantus.core.IValues;
 import org.spantus.core.beans.FrameValuesHolder;
 import org.spantus.core.beans.FrameVectorValuesHolder;
 import org.spantus.core.beans.SignalSegment;
 import org.spantus.core.extractor.dao.MarkerDao;
 import org.spantus.core.marker.Marker;
 import org.spantus.core.marker.MarkerSet;
 import org.spantus.core.marker.MarkerSetHolder;
 import org.spantus.core.service.CorpusService;
 import org.spantus.core.threshold.ClassifierEnum;
 import org.spantus.core.wav.AudioManagerFactory;
 import org.spantus.externals.recognition.bean.CorpusFileEntry;
 import org.spantus.externals.recognition.services.RecognitionServiceFactory;
 import org.spantus.extr.wordspot.domain.SegmentExtractorServiceConfig;
 import org.spantus.extractor.impl.ExtractorEnum;
 import org.spantus.work.services.WorkServiceFactory;
 import org.spantus.work.services.impl.MarkerProxyDao;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 
 /**
  *
  * @author Mindaugas Greibus
  * @since 0.3 Created: May 7, 2012
  *
  */
 public class SegmentExtractorServiceImplOnlineTest extends AbstractSegmentExtractorTest {
 
     @Override
     protected void changeOtherParams(SegmentExtractorServiceConfig config) {
         super.changeOtherParams(config);
         getSegmentExtractorService().getServiceConfig().setClassifier(ClassifierEnum.rulesOnline);
     }
 
     @Test
     public void testExtractSegmentOnline() throws MalformedURLException {
         //given
         URL url = getWavFile().toURI().toURL();
 
         //when
         Collection<SignalSegment> resultOnline = getSegmentExtractorService().extractSegmentsOnline(url);
         ArrayList<SignalSegment> result = new ArrayList<SignalSegment>(resultOnline);
 
         //then
         dumpResults(result);
         Assert.assertEquals("Total segments", 5, resultOnline.size());
         Map<String, FrameValuesHolder> valueMap = result.get(0).getFeatureFrameValuesMap();
         Map<String, FrameVectorValuesHolder> vectorMap = result.get(0).getFeatureFrameVectorValuesMap();
 
         Assert.assertNotNull("First segment", result.get(0).getMarker());
         Assert.assertEquals("First segment starts", 246, result.get(0).getMarker().getStart(),  0);
         Assert.assertEquals("First segment length", 429, result.get(0).getMarker().getEnd(),  1);
         Assert.assertEquals("Extracted feature", 2, valueMap.size());
         Assert.assertEquals("Extracted feature length", 60, valueMap.get("smooth_" + ExtractorEnum.SPECTRAL_FLUX_EXTRACTOR.name()).getValues().size());
         Assert.assertEquals("Extracted vector feature", 1, vectorMap.size());
 //        Assert.assertEquals("Extracted feature length", 56, vectorMap.get(MarkerSegmentatorListenerImpl.SIGNAL_WINDOWS).getValues().size());
         Joiner joiner = Joiner.on(";").skipNulls();
         String recognized = joiner.join(Collections2.transform(result, new Function<SignalSegment,String>(){
                    @Override
                    public String apply(SignalSegment input) {
                        return input.getName();
                    }
                }));
         Assert.assertEquals("segment recognition", "pa;de;da;skirt;zodz", recognized);
     }
 
     @Test
     public void testExtractSegmentOffline() throws MalformedURLException {
         //given
         URL url = getWavFile().toURI().toURL();
         //when
         Collection<SignalSegment> result = getSegmentExtractorService().extractSegmentsOffline(url);
         SignalSegment firstSegment = result.iterator().next();
         Map<String, FrameValuesHolder> valueMap = firstSegment.getFeatureFrameValuesMap();
         //then
         Assert.assertEquals("Total segments", 5, result.size());
         Assert.assertNotNull("First segment", firstSegment.getMarker());
        Assert.assertEquals("First segment starts", 255, firstSegment.getMarker().getStart(), 0);
        Assert.assertEquals("First segment length", 203, firstSegment.getMarker().getLength(),  0);
         Assert.assertEquals("Extracted feature", 3, valueMap.size());
        Assert.assertEquals("Extracted feature", 60, valueMap.get("smooth_" + ExtractorEnum.SPECTRAL_FLUX_EXTRACTOR.name()).getValues().size());
 //		Assert.assertEquals("Extracted vector feature",1, vectorMap.size());
     }
 
     private void dumpResults(ArrayList<SignalSegment> result) throws MalformedURLException {
 //        SignalSegmentDao signalSegmentDao = WorkServiceFactory.createSignalSegmentDao();
         MarkerDao markerDao = WorkServiceFactory.createMarkerDao();
         long timeStamp = System.currentTimeMillis();
         MarkerSetHolder holder = new MarkerSetHolder();
         MarkerSet phones = new MarkerSet();
         holder.getMarkerSets().put(MarkerSetHolder.MarkerSetHolderEnum.phone.name(), phones);
         for (SignalSegment signalSegment : result) {
             Marker marker = signalSegment.getMarker();
 //            signalSegmentDao.write(signalSegment, new File("./target/", signalSegment.getName() + "-" + timeStamp + ".json"));
             phones.getMarkers().add(marker);
             Map<String, IValues> fvv = new HashMap<String, IValues>();
             fvv.put(ExtractorEnum.MFCC_EXTRACTOR.name(), signalSegment.findAllFeatures().get(ExtractorEnum.MFCC_EXTRACTOR.name()));
             AudioInputStream ais = AudioManagerFactory.createAudioManager().findInputStreamInMils(getWavFile().toURI().toURL(), marker.getStart(),
                     marker.getLength());
             CorpusFileEntry corpusFileEntry = new CorpusFileEntry();
             corpusFileEntry.setName(signalSegment.getName());
             corpusFileEntry.setMarker(marker);
             corpusFileEntry.putAll(fvv);
             CorpusService service = RecognitionServiceFactory.createCorpusService("./target/corpus");
             service.learn(corpusFileEntry, ais);
         }
         markerDao.write(holder, new File("./target/markerSetHolder-" + timeStamp + "." + MarkerProxyDao.MSPNTXML));
     }
 }
