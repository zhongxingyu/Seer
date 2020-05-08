 package com.Litterfeldt.AStory.models;
 
 import android.media.MediaMetadataRetriever;
 import android.os.Environment;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 public class FileSystem {
     private static FileSystem mInstance = null;
 
     public static FileSystem getInstance(){
         if (mInstance == null) {
             mInstance = new FileSystem();
         }
         return mInstance;
     }
     private  File defaultDir;
     private static MediaMetadataRetriever mmr;
     private static final Set<String> acceptedFormats = new HashSet<String>(Arrays.asList(
             new String[]{"mp3", "m4a", "aac", "flac"}
     ));
 

     public FileSystem(){
         defaultDir = new File(Environment.getExternalStorageDirectory() +"/Audiobooks");
         if(!defaultDir.exists()){
             defaultDir.mkdir();
             defaultDir = new File(Environment.getExternalStorageDirectory() +"/Audiobooks");
         }
         mmr = new MediaMetadataRetriever();
     }
     public ArrayList<ArrayList<String>> allocateBookFolderContent(){
         ArrayList<ArrayList<String>> bookPathList = new ArrayList<ArrayList<String>>();
 
         File[] files = defaultDir.listFiles();
 
         if (files == null){
             return null;
         }else {
             for (File f : files){
                 if(f.isDirectory()){
                     ArrayList<String> dirList = new ArrayList<String>();
                     for (File d : f.listFiles()){
                         String fullPath = d.getAbsolutePath();
                         String fileType = fullPath.substring(fullPath.lastIndexOf(".")+1);
                         if(acceptedFormats.contains(fileType.toLowerCase())){
                             dirList.add(fullPath);
                         }
                     }
                     bookPathList.add(dirList);
                 }
                 else if (f.isFile()){
                     String fullpath = f.getAbsolutePath();
                     String filetype = fullpath.substring(fullpath.lastIndexOf(".")+1);
 
                     if(acceptedFormats.contains(filetype.toLowerCase())){
                         ArrayList<String> book = new ArrayList<String>();
                         book.add(fullpath);
                         bookPathList.add(book);
                     }}}
             return bookPathList;
         }
     }
     public Book mockBookFromPath(ArrayList<String> paths){
         String firstPath = paths.get(0);
         mmr.setDataSource(firstPath);
         String bookName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
         String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
         byte[] img = mmr.getEmbeddedPicture();
 
         ArrayList<Chapter> chapters = new ArrayList<Chapter>();
         for (String s : paths){
             mmr.setDataSource(s);
             String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
             String chapterNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
 
            Chapter c = new Chapter(0,s,Integer.valueOf(chapterNum), Integer.valueOf(duration));
             chapters.add(c);
         }
         Book book = new Book(0,bookName,author,chapters,img);
         return book;
     }
 }
