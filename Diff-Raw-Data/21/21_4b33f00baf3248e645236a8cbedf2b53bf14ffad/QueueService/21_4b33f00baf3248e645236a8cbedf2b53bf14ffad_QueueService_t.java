 package com.sound.service.sound.impl;
 
 import java.io.File;
 import java.io.IOException;
import java.net.URL;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.sound.constant.Constant;
 import com.sound.dao.QueueNodeDAO;
 import com.sound.dao.SoundDAO;
 import com.sound.dao.UserDAO;
 import com.sound.exception.SoundAuthException;
 import com.sound.exception.SoundException;
 import com.sound.model.Sound;
 import com.sound.model.Sound.QueueNode;
 import com.sound.model.Sound.SoundFormat;
 import com.sound.model.Sound.SoundProfile;
 import com.sound.model.SoundLocal;
 import com.sound.model.User;
 import com.sound.model.enums.SoundState;
 import com.sound.processor.exception.AudioProcessException;
 import com.sound.processor.factory.ProcessorFactory;
 import com.sound.processor.itf.Extractor;
 import com.sound.processor.model.Wave;
 import com.sound.service.sound.itf.SoundService;
 import com.sound.service.storage.itf.RemoteStorageService;
 
 @Service
 @Scope("singleton")
 public class QueueService implements com.sound.service.sound.itf.QueueService{
   @Autowired
   SoundDAO soundDAO;
   
   @Autowired
   UserDAO userDAO;
 
   @Autowired
   QueueNodeDAO queueNodeDAO;
   
   @Autowired
   SoundService soundService;
 
   @Autowired
   ProcessorFactory processFactory;
 
   @Autowired
   RemoteStorageService remoteStorageService;
 
   @Override
   public SoundLocal processSound(User user, String soundUrl, QueueNode node) throws SoundException, SoundAuthException,
       AudioProcessException {
     Extractor extractor = processFactory.getExtractor("wav");
     String soundInfoString = remoteStorageService.getSoundInfo(node.getFileName());
 
     if (null == soundInfoString || !soundInfoString.contains("streams")) {
       throw new SoundException("EMPTY_STREAM");
     }
 
     File waveFile = null;
 
     try {
       ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 
       JsonNode rootNode = mapper.readTree(soundInfoString);
       JsonNode formatNode = rootNode.path("format").path("tags");
       SoundFormat soundFormat = mapper.readValue(formatNode, SoundFormat.class);
       soundFormat.setDuration((float) rootNode.findValue("duration").asDouble());
 
       if (!checkUserRight(user, soundFormat)) {
         throw new SoundAuthException(soundFormat.getAlbum_artist(), soundFormat.getArtist(), soundFormat.getComposer());
       }
 
       // Check total sounds sum
       List<Sound> sounds = soundDAO.find("profile.owner", user);
       long totalDuration = 0;
       for (Sound sound : sounds) {
         totalDuration += sound.getProfile().getDuration();
       }
       if (user.getUserRoles().contains(Constant.USER_ROLE)
           || user.getUserRoles().contains(Constant.PRO_ROLE_OBJ)) {
         if (totalDuration > user.getUserRoles().get(0).getAllowedDuration() * 60) {
           throw new SoundException("TOTAL_LIMIT_ERROR");
         }
       }
 
       // Check weekly sum. User can't upload sounds more than Constant.WEEKLY_ALLOWED_DURATION limitaion in recent one
       // week
       Map<String, Object> cratiaries = new HashMap<String, Object>();
       cratiaries.put("profile.owner", user);
       Calendar c = Calendar.getInstance();
       c.add(Calendar.DATE, -7);
       Date startDate = c.getTime();
       sounds = soundDAO.getSoundsByCreatedTime(cratiaries, startDate);
       totalDuration = 0;
       for (Sound sound : sounds) {
         totalDuration += sound.getProfile().getDuration();
       }
       if (totalDuration > Constant.WEEKLY_ALLOWED_DURATION) {
         throw new SoundException("WEEKLY_LIMIT_ERROR");
       }
 
       SoundLocal soundLocal = new SoundLocal();
       soundLocal.setFileName(node.getFileName());
       soundLocal.setSoundFormat(soundFormat);
 
       Wave wave = null;
      wave = extractor.extractWaveByTotal(new URL(soundUrl), null);
       waveFile = new File(System.getProperty("java.io.tmpdir") + node.getFileName() + ".png");
       waveFile.createNewFile();
       mapper.writeValue(waveFile, wave);
       this.remoteStorageService.uploadFile("wave", node.getFileName() + ".png",
           waveFile.getAbsolutePath());
 
       Sound sound = soundDAO.findOne("profile.remoteId", node.getFileName());
       if (null == sound)
       {
         sound = new Sound();
         SoundProfile profile = new SoundProfile();
         profile.setOwner(user);
         profile.setName(node.getOriginFileName());
         profile.setRemoteId(node.getFileName());
         profile.setStatus(SoundState.PROCESSING.getStatusName());
         profile.setDuration(soundFormat.getDuration());
         profile.setCreatedTime(new Date());
         profile.setProcessed(true);
         sound.setProfile(profile);
       }
       else
       {
         sound.getProfile().setDuration(soundFormat.getDuration());
         sound.getProfile().setProcessed(true);
       }
       
       soundDAO.save(sound);
       
       user.getUserSocial().setSoundDuration((long) (user.getUserSocial().getSoundDuration() + soundFormat.getDuration()));
       userDAO.save(user);
 
       return soundLocal;
     } catch (IOException e) {
       throw new SoundException("NO_FORMATINFO");
     } finally {
       if (null != waveFile) {
         FileUtils.deleteQuietly(waveFile);
       }
     }
   }
 
   @Override
   public List<QueueNode> listQueue() {
     return queueNodeDAO.find("status", "live");
   }
 
   @Override
   public void enqueue(QueueNode queueNode) {
     QueueNode node = queueNodeDAO.findOne("fileName", queueNode.getFileName());
     if (null == node) {
       node = new QueueNode();
     }
     node.setOwner(queueNode.getOwner());
     node.setFileName(queueNode.getFileName());
     node.setOriginFileName(queueNode.getOriginFileName());
     node.setStatus(queueNode.getStatus());
     node.setCreatedDate(new Date());
  
     queueNodeDAO.save(node);
   }
   
   @Override
   public void dequeue(QueueNode node) {
     node.setStatus("processed");
     queueNodeDAO.save(node);
   }
 
 
   private boolean checkUserRight(User user, SoundFormat soundFormat) {
     if (null == soundFormat.getAlbum_artist() && null == soundFormat.getArtist())
     {
       return true;
     }
     
     if (null != soundFormat.getAlbum_artist()
         && Arrays.asList(soundFormat.getAlbum_artist().split("/")).contains(
             user.getProfile().getAlias())) {
       return true;
     }
 
     if (null != soundFormat.getAlbum_artist()
         && Arrays.asList(soundFormat.getAlbum_artist().split("/")).contains(
             user.getProfile().getLastName() + user.getProfile().getFirstName())) {
       return true;
     }
 
     if (null != soundFormat.getAlbum_artist()
         && Arrays.asList(soundFormat.getAlbum_artist().split("/")).contains(
             user.getProfile().getFirstName() + " " + user.getProfile().getLastName())) {
       return true;
     }
 
     if (null != soundFormat.getArtist()
         && Arrays.asList(soundFormat.getArtist().split("/")).contains(user.getProfile().getAlias())) {
       return true;
     }
 
     if (null != soundFormat.getArtist()
         && Arrays.asList(soundFormat.getArtist().split("/")).contains(
             user.getProfile().getLastName() + user.getProfile().getFirstName())) {
       return true;
     }
 
     if (null != soundFormat.getArtist()
         && Arrays.asList(soundFormat.getArtist().split("/")).contains(
             user.getProfile().getFirstName() + " " + user.getProfile().getLastName())) {
       return true;
     }
 
     if (null != soundFormat.getComposer()
         && Arrays.asList(soundFormat.getComposer().split("/")).contains(
             user.getProfile().getAlias())) {
       return true;
     }
 
     if (null != soundFormat.getComposer()
         && Arrays.asList(soundFormat.getComposer().split("/")).contains(
             user.getProfile().getLastName() + user.getProfile().getFirstName())) {
       return true;
     }
 
     if (null != soundFormat.getComposer()
         && Arrays.asList(soundFormat.getComposer().split("/")).contains(
             user.getProfile().getFirstName() + " " + user.getProfile().getLastName())) {
       return true;
     }
 
     if (null != soundFormat.getPerformer()
         && Arrays.asList(soundFormat.getPerformer().split("/")).contains(
             user.getProfile().getAlias())) {
       return true;
     }
 
     if (null != soundFormat.getPerformer()
         && Arrays.asList(soundFormat.getPerformer().split("/")).contains(
             user.getProfile().getLastName() + user.getProfile().getFirstName())) {
       return true;
     }
 
     if (null != soundFormat.getPerformer()
         && Arrays.asList(soundFormat.getPerformer().split("/")).contains(
             user.getProfile().getFirstName() + " " + user.getProfile().getLastName())) {
       return true;
     }
 
     return false;
   }
   
 //  public static void main(String[] args) throws AudioProcessException, IOException
 //  {
 //    ObjectMapper mapper = new ObjectMapper();
 //    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 //    Extractor extractor = new WavExtractor();
 //    Wave wave = extractor.extractWaveByTotal(new BufferedInputStream(new FileInputStream(new File("C:\\server\\apache-tomcat-7.0.40\\temp\\test.wav"))), null);
 //    File waveFile = new File(System.getProperty("java.io.tmpdir")+"aaa" + ".png", "");
 //    waveFile.createNewFile();
 //    System.out.println(waveFile.getAbsolutePath());
 //    mapper.writeValue(waveFile, wave);
 //  }
 }
