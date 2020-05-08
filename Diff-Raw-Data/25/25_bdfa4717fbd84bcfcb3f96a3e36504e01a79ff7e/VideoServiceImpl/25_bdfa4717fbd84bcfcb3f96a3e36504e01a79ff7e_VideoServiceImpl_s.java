 package com.ilopezluna.services;
 
 import com.ilopezluna.entities.Video;
 import com.ilopezluna.utils.HashUtil;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 
 public class VideoServiceImpl implements VideoService {
 
     @Inject
     private VideoDAO videoDAO;
 
     public void create(Video video) {
         try {
             String hash = HashUtil.makeSHA1Hash(video.toString());
             video.setHash(hash);
            video.setTimestamp(new Date());
             videoDAO.save(video);
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void validate(Video video) {
         video.setValidated(true);
         videoDAO.save(video);
     }
 }
