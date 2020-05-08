 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.gpeh;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.amanzi.awe.gpeh.parser.Events;
 import org.amanzi.awe.gpeh.parser.Parameters;
 import org.amanzi.awe.gpeh.parser.internal.GPEHEvent.Event;
 import org.amanzi.neo.loader.core.saver.ISaver;
 import org.amanzi.neo.loader.core.saver.MetaData;
 
 /**
  * Class to save GPEH-data in csv-format
  * <p>
  *
  * </p>
  * @author Kasnitskij_V
  * @since 1.0.0
  */
 public class GpehCSVSaver implements ISaver<GpehTransferData> {
     // opened files
     private Map<Integer,CsvFile> openedFiles = new HashMap<Integer,CsvFile>();
     // output directory to saving csv-files
     private String outputDirectory = null;
     
     // global timestamp, event id and csvfile to working in future  
     private long globalTimestamp = 0;
     private int globalEventId = 0;
     private CsvFile csvFileToWork = null;
     
     // headers to write in file
     private ArrayList<String> headers = new ArrayList<String>();
     
     // it's constant to present timestamp
     private final static String TIMESTAMP = "timestamp";
     // it's constant to present format of date to name of file
     private final static String SIMPLE_DATE_FORMAT = "yyyyMMddHHmm";
     // it's constant to present format of file
     private final static String FILE_FORMAT = ".txt";
     // it's constant using in building name of file
     private final static SimpleDateFormat simpleDateFormat = 
                                     new SimpleDateFormat(SIMPLE_DATE_FORMAT);
     private PrintStream outputStream;
     
     // time of before loading and after loading
     private long beforeLoading = 0, afterLoading = 0;
     // starting timestamp
     private long startTimestamp = 0;
     // count of loaded files
     private int countOfLoadedFiles = 0;
     // count of events
     private long count = 0;
     // true if file to write is new
     private boolean isNewFile = false;
     
     @Override
     public void init(GpehTransferData element) {
         outputDirectory = element.get(GpehTransferData.OUTPUT_DIRECTORY).toString();
         beforeLoading = System.currentTimeMillis();
     }
 
     @Override
     public void save(GpehTransferData element) {
         count++;
         long timestamp = (Long)element.get(TIMESTAMP);
         if (globalTimestamp != timestamp) {
             if (globalTimestamp != 0)
                 closeOpenFiles();
             
             if (startTimestamp == 0)
             	startTimestamp = timestamp;
             
             globalTimestamp = timestamp;
             isNewFile = true;
         }
         
         // read id of event
         Event event = (Event)element.remove(GpehTransferData.EVENT);
         int eventId = event.getId();
         
         if (openedFiles.get(eventId) == null) {
             
             Events events = Events.findById(eventId);
             
            Date date = new Date(globalTimestamp - simpleDateFormat.getTimeZone().getOffset(timestamp));
             
             String meContext = (String) element.get(GpehTransferData.ME_CONTEXT);
             String wayToFile = outputDirectory + "\\" + "Event_" + eventId + "_" + 
                                 (simpleDateFormat.format(date)) + "_" + meContext + FILE_FORMAT;
             
             // create new file 
             File file = new File(wayToFile);
             CsvFile csvFile;
                 try {
                     csvFile = new CsvFile(file);
                 } catch (IOException e1) {
                     throw new RuntimeException("Sorry. Can not create new file. Loading stopped.");
                 }
 
             // add id to file to associate his with some event
             csvFile.setEventId(eventId);
             
             openedFiles.put(eventId,csvFile);
             
             // create headers
             Parameters[] parameters = events.getAllParametersWithTimestamp();
             
             // create array list of headers
             ArrayList<String> headers = new ArrayList<String>();
             for (Parameters parameter : parameters) {
                 headers.add(parameter.name());
             }
             
             // add headers to csvfile
             csvFile.setHeaders(headers);
             // write headers to csvfile
             try {
                 csvFile.writeHeaders(headers);
                 headers.remove(Parameters.EVENT_PARAM_TIMESTAMP_HOUR.toString());
                 headers.remove(Parameters.EVENT_PARAM_TIMESTAMP_MINUTE.toString());
                 headers.remove(Parameters.EVENT_PARAM_TIMESTAMP_SECOND.toString());
                 headers.remove(Parameters.EVENT_PARAM_TIMESTAMP_MILLISEC.toString());
             } catch (IOException e) {
                 throw new RuntimeException("Sorry. Can not write data to file. Loading stopped.");
             }
         }
         
         // get headers from csvfile
         if (globalEventId != eventId || isNewFile) {
             isNewFile = false;
             globalEventId = eventId;
             csvFileToWork = openedFiles.get(eventId);
             headers = csvFileToWork.getHeaders();
         }
         
         String scannerId = null;
         try {
         	scannerId = element.get(Parameters.EVENT_PARAM_SCANNER_ID.toString()).toString();
         }
         catch (Exception e) {
         }
         // delete scanner id from headers
         headers.remove(Parameters.EVENT_PARAM_SCANNER_ID.toString());
         
         // create array list of data
         ArrayList<String> data = new ArrayList<String>();
         data.add(scannerId);
         data.add(Long.toString(event.getHour()));
         data.add(Long.toString(event.getMinute()));
         data.add(Long.toString(event.getSecond()));
         data.add(Long.toString(event.getMillisecond()));
 
         String currentHeaderValue = null;
         for (String header : headers) {
             if (element.get(header) != null) {
                 if (header.equals(Parameters.EVENT_PARAM_MESSAGE_CONTENTS.toString())) {
                     currentHeaderValue = new String((byte[])element.get(header));
                 }
                 else {
                     currentHeaderValue = element.get(header).toString();
                 }
             }
                 
             data.add(currentHeaderValue);
             
             currentHeaderValue = null;
         }
 
         // write data to needing csvfile
         try {
             csvFileToWork.writeData(data);
         } catch (IOException e) {
             throw new RuntimeException("Sorry. Can not write data to file. Loading stopped.");
         }
     }
 
     @Override
     public void finishUp(GpehTransferData element) {
     	outputStream.println("Number of events = " + count);
         closeOpenFiles();
         afterLoading = System.currentTimeMillis();
         long timeOfLoading = afterLoading - beforeLoading;
         outputStream.println(countOfLoadedFiles + " files converted");
         
         outputStream.println("Full time of loading = " + 
         		(timeOfLoading) + " millisecond");
         
         outputStream.println("Speed of loading = " + (int)(count/timeOfLoading) + " events/millisecond");
         
         String periodDateFormat = new java.text.SimpleDateFormat("hh:mm").format(startTimestamp) + " - " +
         							new java.text.SimpleDateFormat("hh:mm").format(globalTimestamp + 900000);
         outputStream.println("Period of converted files: " + periodDateFormat + "(HOURS:MINUTES)");
     }
 
     @Override
     public PrintStream getPrintStream() {
     	if (outputStream==null){
             return System.out;
         }
         return outputStream;
     }
 
     @Override
     public void setPrintStream(PrintStream outputStream) {
     	this.outputStream = outputStream;
     }
 
     @Override
     public Iterable<MetaData> getMetaData() {
         return null;
     }
     
     // all files is closed here
     private void closeOpenFiles() {
         for (CsvFile csvFile : openedFiles.values()) {
             try {
                 csvFile.close();
             } catch (IOException e) {
                 throw new RuntimeException("Sorry. Can not close file and can not write data to file. Loading stopped.");
             }
         }
         countOfLoadedFiles += openedFiles.size();
         openedFiles.clear();
     }
 
 }
