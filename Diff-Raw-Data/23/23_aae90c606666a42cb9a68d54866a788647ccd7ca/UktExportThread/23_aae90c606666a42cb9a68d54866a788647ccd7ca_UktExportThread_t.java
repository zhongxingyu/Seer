 package com.worthsoln.patientview.uktransplant;
 
import com.Ostermiller.util.CSVPrinter;
import com.worthsoln.patientview.ParserThread;
import com.worthsoln.patientview.model.Patient;
import com.worthsoln.utils.LegacySpringUtils;

import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class UktExportThread implements Runnable, ParserThread {
 
     private String prebit;
     private String directory;
     private String archiveDirectory;
     private int minutesBetweenWait;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private ServletContext servletContext;
 
     public UktExportThread() {
     }
 
     public UktExportThread(ServletContext context, String uktDirectory,
                            String uktArchiveDirectory, int minutesBetweenWait) {
         this.directory = uktDirectory;
         this.archiveDirectory = uktArchiveDirectory;
         this.minutesBetweenWait = minutesBetweenWait;
         this.servletContext = context;
     }
 
     public void run() {
         try {
             while (true) {
                 try {
                     File uktDir = new File(directory);
                     File uktExportFile = new File(uktDir, "ukt_rpv_export.txt");
                     CSVPrinter csv = new CSVPrinter(new FileWriter(uktExportFile));
                     csv.setAlwaysQuote(true);
                     csv.writeln(getPatients());
                     csv.flush();
                     csv.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 Thread.sleep(1000 * 60 * minutesBetweenWait);
                 Date now = new Date(System.currentTimeMillis());
                 System.out.println("UktExportThread " + dateFormat.format(now));
             }
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     private String[][] getPatients() {
         List patientList = LegacySpringUtils.getPatientManager().getUktPatients();
         String[][] patientArray = new String[patientList.size()][5];
         for (int i = 0; i < patientList.size(); i++) {
             Patient patient = (Patient) patientList.get(i);
            patientArray[i][0] = (patient.getNhsno() == null) ? "" : patient.getNhsno();
            patientArray[i][1] = (patient.getSurname() == null) ? "" : patient.getSurname().replaceAll("\"", "");
            patientArray[i][2] = (patient.getForename() == null) ? "" : patient.getForename().replaceAll("\"", "");
            patientArray[i][3] = (patient.getDateofbirth() == null) ? "" : patient.getDateofbirth();
            patientArray[i][4] = (patient.getPostcode() == null) ? "" : patient.getPostcode();
         }
         return patientArray;
     }
 
     public String getPrebit() {
         return prebit;
     }
 
     public void setPrebit(String prebit) {
         this.prebit = prebit;
     }
 
     public String getDirectory() {
         return directory;
     }
 
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     public String getArchiveDirectory() {
         return archiveDirectory;
     }
 
     public void setArchiveDirectory(String archiveDirectory) {
         this.archiveDirectory = archiveDirectory;
     }
 
     public int getMinutesBetweenWait() {
         return minutesBetweenWait;
     }
 
     public void setMinutesBetweenWait(int minutesBetweenWait) {
         this.minutesBetweenWait = minutesBetweenWait;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public void setServletContext(ServletContext servletContext) {
         this.servletContext = servletContext;
     }
 }
