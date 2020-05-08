 package org.Zeitline.InputFilters;
 
 import org.Zeitline.Event.AtomicEvent;
 import org.Zeitline.Event.L2TEvent;
 import org.Zeitline.Event.MACTimeEvent;
 import org.Zeitline.GUI.IFormGenerator;
 import org.Zeitline.Plugin.Input.InputFilter;
 import org.Zeitline.Source;
 import org.Zeitline.Timestamp.ITimestamp;
 import org.Zeitline.Timestamp.Timestamp;
 
 import java.awt.*;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.LinkedList;
 
 public class L2TFilter extends InputFilter {
 
     private static final String NAME = "Log2Timeline Filter";
     private static final String DESCRIPTION = "Reads in standard CSV output file by Kristinn Gudjonsson's log2timeline tool.";
     private static final String INPUT_FILE_EXTENSION = ".txt"; // ADD: '.csv'
 
     private static final String FIELDS_SEPARATOR = ",";
     private static final int FIELDS_NUMBER = 17;
 
     private RandomAccessFile fileInput;
     private int descLinesNo = 0;
     protected LinkedList event_queue = new LinkedList();
 
     public L2TFilter(IFormGenerator formGenerator) {
         super(formGenerator, NAME, INPUT_FILE_EXTENSION,DESCRIPTION);
     }
 
     @Override
     public Source init(String filename, Component parent) {
         try {
             // WHY IS IT RandomAccessFile?! 14/04/2012
             //
             //	    fileInput = new BufferedReader(new FileReader(filename));
             fileInput = new RandomAccessFile(filename, "r");
         } catch (IOException ioe) {
             return null;
         }
 
         if (FirstLineIsExtra(fileInput))
             descLinesNo = 1;
 
         return new Source(NAME, filename, Source.GRANULARITY_SEC);
     }
 
     private boolean FirstLineIsExtra(RandomAccessFile fileInput) {
         try {
             String desc = fileInput.readLine();
             String[] fields = desc.split(FIELDS_SEPARATOR);
             if (fields.length != FIELDS_NUMBER)
                 return false;
             
             if (fields[0].equals("date") && fields[1].equals("time") && fields[10].equals("desc") && fields[16].equals("extra"))
                 return true;
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     @Override
     public AtomicEvent getNextEvent() {
         String line;
         String[] fields;
 
         if (event_queue.isEmpty()) {
 //            try {
 //                while ((line = fileInput.readLine()) != null) {
 //                    fields = line.split(",");
 //
 //                    if (fields.length != 17)
 //                        System.err.println("Line not in a proper format: " + line);
 //                    else
 //                        break;
 //
 //                }
 //            } catch (IOException e) {
 //                e.printStackTrace();
 //                return null;
 //            }
 
 
             while (true) {
 
                 try {
                     line = fileInput.readLine();
                 } catch (IOException ioe) {
                     return null;
                 }
 
                 if (line == null) return null;
 
                 fields = line.split(FIELDS_SEPARATOR);
 
                 if (fields.length != FIELDS_NUMBER)
                     System.err.println("Line not in a proper format: " + line);
                 else
                     break;
             }
 
             String date = fields[0];
             String time = fields[1];
             ITimestamp timestamp = CreateTimestamp(date, time);
 
             return new L2TEvent(timestamp, fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8], fields[9],
                    fields[10], Integer.decode(fields[11]), fields[12], fields[13], fields[14], fields[15], fields[16],
                     formGenerator);
         }
         else {
             return (MACTimeEvent) event_queue.removeFirst();
         }
     }
 
     private ITimestamp CreateTimestamp(String date, String time) {
        String[] dateFields = date.split("/"); // format: MM/DD/YYYY
         String[] timeFields = time.split(":"); // format: HH:MM:SS
 
         if (dateFields.length != 3 || timeFields.length != 3)
             return null;
 
        // Timestamp class needs updating so that the code below doesn't need to subtract any values!
        //
        return new Timestamp(Integer.decode(dateFields[2]) - 1900, Integer.decode(dateFields[0]) - 1, Integer.decode(dateFields[1]),
                Integer.decode(timeFields[0]), Integer.decode(timeFields[1]), Integer.decode(timeFields[2]),
                 0); // nanoseconds
     }
 
     @Override
     public long getExactCount() {
         return 0;
     }
 
     ///
     // Subtracting 1 from the count due to the extra description line at the top
     //
 
     @Override
     public long getTotalCount() {
         try {
             return fileInput.length() - descLinesNo;
         } catch (IOException ie) {
             return 0;
         }
     }
 
     @Override
     public long getProcessedCount() {
         try {
             return fileInput.getFilePointer() - descLinesNo;
         } catch (IOException ie) {
             return 0;
         }
     }
 }
