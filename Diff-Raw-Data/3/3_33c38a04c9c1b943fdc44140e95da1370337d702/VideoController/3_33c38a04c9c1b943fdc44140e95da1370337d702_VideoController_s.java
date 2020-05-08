 package com.washingtonpost.videocomments.web;
 
 import com.washingtonpost.videocomments.model.VideoComment;
 import com.washingtonpost.videocomments.service.VideoCommentsService;
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 @Controller
 public class VideoController {
 
     @Autowired
     private VideoCommentsService videoCommentsService;
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public void create(HttpServletResponse response) throws IOException {
         Long id = videoCommentsService.createNewComment();
         response.getWriter().write(id.toString());
     }
 
     @RequestMapping(value = "/complete", method = RequestMethod.GET)
     public void complete(@RequestParam("id") long id, HttpServletResponse response) throws IOException {
         videoCommentsService.complete(id);
         response.getWriter().write("OK");
     }
 
 
     @RequestMapping(value = "/video", method = RequestMethod.GET)
     public void video(@RequestParam("id") long id, HttpServletResponse response) throws IOException {
         if (id < 0) {
             throw new IllegalArgumentException("");
         }
         checkComment(id);
         response.setContentType("video/x-flv");
         File file = new File(videoCommentsService.getPath() + File.separator + id + ".flv");
         if (!file.exists()) {
             throw new RuntimeException("Not found");
         }
         InputStream stream = null;
         try {
             stream = new FileInputStream(file);
             IOUtils.copy(stream, response.getOutputStream());
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
     private void checkComment(long id) {
         VideoComment comment = videoCommentsService.loadComment(id);
         if (!comment.isComplete()) {
             throw new IllegalArgumentException("Not created yet");
         }
     }
 
     @RequestMapping(value = "/thumbnail", method = RequestMethod.GET)
     public void thumbnail(@RequestParam("id") long id, HttpServletResponse response) throws IOException {
         if (id < 0) {
             throw new IllegalArgumentException("");
         }
         checkComment(id);
         response.setContentType("image/jpeg");
         File file = new File(videoCommentsService.getPath() + File.separator + id + ".jpg");
         if (!file.exists()) {
             throw new RuntimeException("Not found");
         }
         InputStream stream = null;
         try {
             stream = new FileInputStream(file);
             IOUtils.copy(stream, response.getOutputStream());
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
 
     @RequestMapping(value = "/thumbnail", method = RequestMethod.POST)
     public void thumbnail(@RequestParam("id") long id, MultiPartFileUpload fileUpload, HttpServletResponse response) throws IOException {
         VideoComment comment = videoCommentsService.loadComment(id);
         if (comment.isComplete()) {
             throw new IllegalArgumentException("Is complete");
         }
         File file = new File(videoCommentsService.getPath(), id + ".jpg");
         fileUpload.getFile().transferTo(file);
     }
 
     @RequestMapping(value = "/video", method = RequestMethod.POST)
    public void video(MultiPartFileUpload fileUpload, HttpServletResponse response) throws IOException {
        Long id = fileUpload.getId();
         VideoComment comment = videoCommentsService.loadComment(id);
         if (comment.isComplete()) {
             throw new IllegalArgumentException("Is complete");
         }
         File file = new File(videoCommentsService.getPath(), id + ".flv");
         fileUpload.getFile().transferTo(file);
     }
 
 //    @ExceptionHandler(Exception.class)
 //    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 //    public void serviceExceptionHandler(Exception e, HttpServletResponse response) throws IOException {
 //        logger.error("Error", e);
 //    }
 
     public void setVideoCommentsService(VideoCommentsService videoCommentsService) {
         this.videoCommentsService = videoCommentsService;
     }
 }
