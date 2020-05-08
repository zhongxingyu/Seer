 package com.ssem.loan;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Scanner;
 
 import com.google.gson.Gson;
 
 public class LoanRepository {
 
     private static final String FILE_EXTENSION = ".loan";
     private final static String ROOT = "c:/temp/loan";
 
     public static Application fetch(String ticketId) {
         return fetch(Long.parseLong(ticketId));
     }
 
     public static Application fetch(long ticketId) {
         File file = fileFromApplication(ticketId);
         try {
             String output = new Scanner(file).useDelimiter("\\Z").next();
             return new Gson().fromJson(output, Application.class);
         } catch (FileNotFoundException e) {
             throw new ApplicationException("Ticket not found", e);
         }
     }
 
     public static long getNextId() {
         File file = new File(ROOT);
         File[] files = file.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(FILE_EXTENSION);
             }
         });
 
         return files.length + 1;
     }
 
     public static Ticket store(Application application) {
         try {
             FileOutputStream fileOutputStream = new FileOutputStream(fileFromApplication(application.getApplicationNo()));
             fileOutputStream.write(new Gson().toJson(application).getBytes());
             fileOutputStream.close();
             return new Ticket(application.getApplicationNo());
         } catch (FileNotFoundException e) {
             throw new ApplicationException("Could not store application", e);
         } catch (IOException e) {
             throw new ApplicationException("Could not store application", e);
         }
     }
 
     private static File fileFromApplication(long applicationNo) {
         return new File(ROOT + "/" + applicationNo + FILE_EXTENSION);
     }
 
 }
