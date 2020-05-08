 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tokenizer.util;
 
 import com.tokenizer.controller.*;
 import com.tokenizer.model.BigConcurentHashMap;
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.HashMap;
 import java.util.concurrent.Callable;
 
 /**
  *
  * @author irfannurhakim
  */
 public class FileReader implements Callable {
 
     private FileWalker fileWalker;
     private Path path;
     private int count;
 
     public FileReader() {
     }
 
     public FileReader(Path path, int count) {
         this.path = path;
         this.count = count;
     }
 
     public void setCaller(FileWalker fileWalker) {
         this.fileWalker = fileWalker;
     }
 
     public FileWalker getCaller() {
         return fileWalker;
     }
 
     /**
      * This is callable method, so while the FileReader object instantiated this
      * method will performed. Generally, this method slice the structure of each
      * email document to five parts: Date, From, To, Subject and Body. After
      * document has success sliced, then send result to HashMap and invoke
      * callback to acknowledge the main thread that job was done.
      *
      * @return Boolean
      * @throws IOException
      * @throws InterruptedException
      */
     @Override
     public Boolean call() throws IOException, InterruptedException {
 
         String line = Files.readAllLines(this.path, StandardCharsets.UTF_8).toString().toLowerCase().replaceAll("x-to|x-from", "");
         //System.out.println(line);
 
        /**
          * raw -> array 0 head, array 1 tail
          */
         
         String[] raw = line.split("mime-version: ", 2);
         
         String[] rawh = raw[0].split("date: ", 2);
 
         String[] date = rawh[1].split("from: ", 2);
         HashMap<String, Integer> dateMap = dateTokenizer.getListDate(date[0]);
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.dateConcurentMap, dateMap);
        //System.out.println("date=" + date[0]);
 
         if (date.length == 1) {
             date[1] = "";
         }
 
         String[] from;
         if (date[1].contains("to: ")) {
             from = date[1].split("to: ", 2);
         } else {
             from = date[1].split("subject: ", 2);
         }
 
         HashMap<String, Integer> fromMap = FromTokenizer.getListFrom(from[0].replaceAll(", ", " "));
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.fromConcurentMap, fromMap);
        //System.out.println("from=" + from[0]);
 
         if (from.length == 1) {
             from[1] = "";
         }
 
         String[] to = new String[2];
         if (date[1].contains("to: ")) {
             if (from[1].contains("subject: ")) {
                 to = from[1].split("subject: ", 2);
             } else {
                 to = from;
             }
         } else {
             to[0] = "";
         }
 
         HashMap<String, Integer> toMap = toTokenizer.getListTo(to[0]);
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.toConcurentMap, toMap);
        //System.out.println("to=" + to[0]);
         
         if (to.length == 1) {
             to[1] = "";
         }
         
         if(to[1] == null){
             to[1] = "";
         }
         
 
         HashMap<String, Integer> subjectMap = subject_bodyTokenizer.getListTerm(to[1]);
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.subjectConcurentMap, subjectMap);
        //System.out.println("subjet=" + to[1]);
         
         String[] body = raw[1].split("(\\.pst)|(\\.nsf)", 2);
         if (body.length == 1) {
             body[1] = "";
         }
 
         HashMap<String, Integer> bodyMap = subject_bodyTokenizer.getListTerm(body[1]);
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.bodyConcurentMap, bodyMap);
        //System.out.println("body=" + body[1]);
         
         HashMap<String, Integer> allFieldMap = AllFieldTokenizer.allFieldTermList(dateMap, toMap, fromMap, subjectMap, bodyMap);        
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.allConcurentMap, allFieldMap);
         //System.out.println(allFieldMap);
 
 
         //System.out.println(path.toString());
         fileWalker.callback(dateMap, fromMap, toMap, subjectMap, bodyMap, allFieldMap, count);
         return true;
     }
 }
