 package com.washingtonpost.videocomments.service;
 
 
 import com.washingtonpost.videocomments.dao.VideoCommentsDao;
 import com.washingtonpost.videocomments.model.VideoComment;
 import org.apache.commons.io.IOUtils;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.UUID;
 
 public class VideoCommentsService {
 
     private VideoCommentsDao videoCommentsDao;
 
     private AmazonService amazonService;
 
     private String path;
 
     private ThreadPoolTaskExecutor taskExecutor;
 
     public UUID createNewComment() {
         VideoComment videoComment = new VideoComment();
         videoCommentsDao.create(videoComment);
 
         return videoComment.getId();
     }
 
     public VideoComment loadComment(UUID id) {
         VideoComment comment = videoCommentsDao.load(id);
         return comment;
     }
 
 
     public void complete(UUID id) {
         VideoComment videoComment = videoCommentsDao.load(id);
         if (!videoComment.isComplete()) {
             taskExecutor.execute(new UploadFilesTask(videoComment));
         }
     }
 
     public void setVideoCommentsDao(VideoCommentsDao videoCommentsDao) {
         this.videoCommentsDao = videoCommentsDao;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
         this.taskExecutor = taskExecutor;
     }
 
     public void setAmazonService(AmazonService amazonService) {
         this.amazonService = amazonService;
     }
 
     public void addVideo(UUID uuid) {
         VideoComment videoComment = videoCommentsDao.load(uuid);
         if (!videoComment.isComplete()) {
             videoComment.setHasVideo(true);
             videoComment.setUpdatedAt(new Date());
             videoCommentsDao.update(videoComment);
         }
     }
 
     public void addThumbnail(UUID uuid) {
         VideoComment videoComment = videoCommentsDao.load(uuid);
         if (!videoComment.isComplete()) {
             videoComment.setHasThumbnail(true);
             videoComment.setUpdatedAt(new Date());
             videoCommentsDao.update(videoComment);
         }
     }
 
 
     private class UploadFilesTask implements Runnable {
 
         private final VideoComment videoComment;
 
         private UploadFilesTask(VideoComment videoComment) {
             this.videoComment = videoComment;
         }
 
 
         @Override
         public void run() {
             File videoFile = new File(path, videoComment.getId().toString() + ".flv");
            File thumbnailFile = new File(path, videoComment.getId().toString() + ".flv");
             String publishname = videoComment.getId().toString();
             try {
                 uploadFile(videoFile, publishname + ".flv");
                 uploadFile(thumbnailFile, publishname + ".jpg");
 //                videoFile.delete();
 //                thumbnailFile.delete();
                 //delete files
                 videoComment.setComplete(true);
                 videoComment.setUpdatedAt(new Date());
                 videoCommentsDao.update(videoComment);
 
             } catch (IOException e) {
 
             }
         }
 
         private void uploadFile(File file, String key) throws IOException {
             FileInputStream fileInputStream = null;
             try {
                 if (file.exists() && file.isFile()) {
                     fileInputStream = new FileInputStream(file);
                     amazonService.upload(fileInputStream, key, file.length());
                 }
             } catch (IOException e) {
                 IOUtils.closeQuietly(fileInputStream);
                 throw e;
             }
         }
     }
 }
