 package com.android.interview.utilities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.os.Environment;
 
 public class Data {
 	
    private static Data instance = null;
    private static File root = null;
    private static String subject = null;
    private static String subjectPath = null;   
    private static String imageFolderPath = null;
    private static String videoFolderPath = null;
    private static String audioFolderPath = null;
    
    protected Data() {
       // Exists only to defeat instantiation.
    }
    
    public static Data getInstance() {
       if(instance == null) {
          instance = new Data();
       }
       return instance;
    }
    
    public void SetSubject(String subject) {
 	   Data.subject = subject;
    }
    
    public void SetRoot(File root) {
 	   Data.root = root;
    }
    
    public String[] GetSubjects() {	   	   	   	   	   
 	   File files = new File(root.getAbsolutePath());	   	   	   	  
 	   return files.list();
    }
    
    public void AddSubject(String subject) {
 	   // add the subject	   
 	   Data.subject = subject;	 
 	   
 	   Data.subjectPath = root.getAbsolutePath() + "/" + Data.subject;
 	   Data.imageFolderPath = Data.subjectPath + "/images";
 	   Data.videoFolderPath = Data.subjectPath + "/videos";
 	   Data.audioFolderPath = Data.subjectPath + "/audio_files";
 	   
 	   createFolder(Data.subjectPath);
 	   createFolder(Data.imageFolderPath);
 	   createFolder(Data.videoFolderPath);
 	   createFolder(Data.audioFolderPath);
 	   
    }
    
    public void DeleteSubject() {	  
 	   deleteFolder(Data.subjectPath);
    }
    
    public void SetNote(String note) {
 	   
    }
    
    public String GetNote() {
 	   return null;
    }
       
    public String GetPhotoURL() {
 	   File photos = new File(Data.imageFolderPath);
 	   
 	   
 	   
 	   return Data.imageFolderPath + "/image_"+photos.listFiles().length+".jpg";	   
    }
          
    public String GetAudioURL() {
 	   return null;
    }
    
    public String GetVideoURL() {
 	   return null;
    }
 
    
    public String[] GetPhotoURLs() {
 	   File files = new File(imageFolderPath);
 	   String[] photos = files.list();
 	   
 	   for(int i=0;i<photos.length;i++)
 	   {
 		   photos[i] = Data.imageFolderPath + "/" + photos[i];
 	   }
 	   
 	   return photos;
    }
    
    public List<String> GetAudioURLs() {
 	   return null;
    }
    
    public List<String> GetVideoURLs() {
 	   return null;
    }
    
    public void DeletePhoto(String photoUrl) {
 	   deleteFile(photoUrl);
    }
 
    public void DeleteAudio(String audioUrl) {
 	   deleteFile(audioUrl);
    }
    
    public void DeleteVideo(String videoUrl) {
 	   deleteFile(videoUrl);
    }
    
    private void createFolder(String folderPath) {	   
 	   boolean success = (new File(folderPath)).mkdir();
    }
    private void deleteFolder(String folderPath) {
 	   File file = new File(folderPath);
 	   if(file.isDirectory() && file.list().length>0)
 	   {
 		   deleteDirectory(file);
 	   }
 	   else 
 	   {
 		   file.delete();
 	   }
 	   	
    }
    
    private boolean deleteDirectory(File path) {
 	    if( path.exists() ) {
 	      File[] files = path.listFiles();
 	      for(int i=0; i<files.length; i++) {
 	         if(files[i].isDirectory()) {
 	           deleteDirectory(files[i]);
 	         }
 	         else {
 	           files[i].delete();
 	         }
 	      }
 	    }
 	    return( path.delete() );
    }
    
    private void deleteFile(String filePath) {
 	   
    }
   
       
    
    
    
 }
