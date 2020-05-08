 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.spnt.recognition.service.test;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioInputStream;
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.spantus.core.FrameValues;
 import org.spantus.externals.recognition.bean.CorpusEntry;
 import org.spantus.externals.recognition.bean.CorpusFileEntry;
 import org.spantus.externals.recognition.bean.FeatureData;
 import org.spantus.externals.recognition.corpus.CorpusRepositoryFileImpl;
 import org.spantus.work.wav.AudioManagerFactory;
 
 /**
  *
  * @author mondhs
  */
 public class CorpusRepositoryFileImplTest {
     CorpusRepositoryFileImpl corpusRepository;  
     @Before
     public void onSetup(){
         corpusRepository = new CorpusRepositoryFileImpl();
         corpusRepository.setRepositoryPath("./target/test-classes/corpus");
     }
     
     @Test
     public void testCRUDCorpusEntry(){
         //given 
         CorpusEntry corpusEntry = new CorpusEntry();
         corpusEntry.setName("Name1");
         FeatureData fd = new FeatureData();
         fd.setName("Feature1");
         fd.setValues(new FrameValues(new Float[]{1F, 2F, 3F}));
         corpusEntry.getFeatureMap().put(fd.getName(), fd);
         int initialSize = corpusRepository.findAllEntries().size();
         
         //when
         CorpusEntry savedCorpusEntry =corpusRepository.save(corpusEntry);
         Long savedId =  savedCorpusEntry.getId();
         int savedSize = corpusRepository.findAllEntries().size();
 
         CorpusEntry updatedCorpusEntry =corpusRepository.update(savedCorpusEntry);
         Long updatedId =  updatedCorpusEntry.getId();
         int updatedSize = corpusRepository.findAllEntries().size();
 
         CorpusEntry deltedCorpusEntry =corpusRepository.delete(updatedCorpusEntry);
         int deletedSize = corpusRepository.findAllEntries().size();
         Long deletedId = deltedCorpusEntry.getId();
 
 
         //then
         Assert.assertNotNull(savedId);
         Assert.assertEquals(updatedId, savedId);
         Assert.assertEquals(1, savedSize-initialSize);
         Assert.assertEquals(deletedId, savedId);
         Assert.assertEquals(1, updatedSize-deletedSize);
 
     }
     @Test
     public void testUpdateDeleteWav(){
         //given 
        File inputWavFile = new File("../../../data/test.wav");
         URL wavUrl = null;
         try {
             wavUrl = inputWavFile.toURI().toURL();
         } catch (MalformedURLException ex) {
             Assert.fail("not working: " + ex.getMessage());
         }
         Assert.assertNotNull("file not found in " 
                 +inputWavFile.getAbsoluteFile()
                 , wavUrl);
         AudioInputStream ais = AudioManagerFactory.createAudioManager().findInputStream(
                 wavUrl,
                 null, null);
         CorpusEntry corpusEntry = new CorpusEntry();
         corpusEntry.setName("Name1");
         FeatureData fd = new FeatureData();
         fd.setName("Feature1");
         fd.setValues(new FrameValues(new Float[]{1F, 2F, 3F}));
         corpusEntry.getFeatureMap().put(fd.getName(), fd);
         
         //when
         CorpusEntry savedCorpusEntry =corpusRepository.save(corpusEntry);
         CorpusFileEntry updated = (CorpusFileEntry)corpusRepository.update(savedCorpusEntry, ais);
         boolean updatedWavExist = updated.getWavFile().exists();
         String wavFilePath =updated.getWavFile().getAbsolutePath();
         CorpusFileEntry deleted = (CorpusFileEntry)corpusRepository.delete(updated);
         //then
         String fileName = MessageFormat.format("{0}/{1}-{2,number,#}.wav", 
                 corpusRepository.getRepoDir(),
                 updated.getName(), updated.getId());
        
         Assert.assertTrue(updatedWavExist);
         Assert.assertTrue(wavFilePath+" does not ends with " + fileName,
                 wavFilePath.endsWith(fileName));
         Assert.assertFalse("wav file not exist", deleted.getWavFile().exists());
     }
     
 }
