/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package com.mewin.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import org.bukkit.Bukkit;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class FileOutputHandler extends Handler {
     private BufferedWriter out;
     private SimpleDateFormat sdf;
     
     public FileOutputHandler(File outputFile, String timeFormat)
     {
         try {
             out = new BufferedWriter(new FileWriter(outputFile, true));
             sdf = new SimpleDateFormat(timeFormat);
             
             out.write("--- Log started " + sdf.format(new Date()) + " ---");
             out.newLine();
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Could not create file writer :", ex);
         }
     }
 
     @Override
     public void publish(LogRecord record) {
         try {
             out.write("[" + sdf.format(new Date(record.getMillis())) + "] ");
             out.write("[" + record.getLevel().getLocalizedName() + "] ");
            out.write(record.getMessage());
             out.newLine();
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Could not write with file output stream: ", ex);
         }
     }
 
     @Override
     public void flush() {
         try {
            System.out.println("flush");
             out.flush();
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Could not flush file output stream: ", ex);
         }
     }
 
     @Override
     public void close() throws SecurityException {
         try {
             System.out.println("closed");
             out.write("--- Log ended " + sdf.format(new Date()) + " ---");
             out.newLine();
             out.close();
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Could not close file output stream: ", ex);
         }
     }
 }
