 package couch25k;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.rms.RecordEnumeration;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 import javax.microedition.rms.RecordStoreNotOpenException;
 
 import couch25k.workouts.WorkoutView;
 
 /**
  * RecordStore operations for couch25k.
  */
 public class Couch25KStore {
     private RecordStore completion;
     private RecordStore options;
 
     public Couch25KStore() {
         try {
             completion = RecordStore.openRecordStore("completion", true);
             options = RecordStore.openRecordStore("options", true);
         } catch (RecordStoreException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Loads completion dates, placing them in appropriate array indices for use
      * in the workout view.
      */
     public Date[] loadCompletionDates(int workoutCount) {
         Date[] completionDates = new Date[workoutCount];
         try {
             RecordEnumeration re = completion.enumerateRecords(null, null, false);
             while (re.hasNextElement()) {
                 DataInputStream inputStream = getDataInputStream(re.nextRecord());
                 int week = inputStream.readInt();
                 int workout = inputStream.readInt();
                 Date completedAt = new Date(inputStream.readLong());
                 completionDates[WorkoutView.getWorkoutIndex(week, workout)] = completedAt;
             }
         } catch (RecordStoreNotOpenException e) {
             e.printStackTrace();
         } catch (RecordStoreException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return completionDates;
     }
 
     /** Records completion of a workout and returns the completion date. */
     public void completeWorkout(int week, int workout, Date completionDate) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream outputStream = new DataOutputStream(baos);
         try {
             outputStream.writeInt(week);
             outputStream.writeInt(workout);
             outputStream.writeLong(completionDate.getTime());
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Add the record
         byte[] b = baos.toByteArray();
         try {
             completion.addRecord(b, 0, b.length);
         }
         catch (RecordStoreException e) {
             e.printStackTrace();
         }
     }
 
     /** Loads configuration options */
     public Hashtable loadConfig() {
         Hashtable config = new Hashtable();
         try {
             RecordEnumeration re = options.enumerateRecords(null, null, false);
             while (re.hasNextElement()) {
                 DataInputStream inputStream = getDataInputStream(re.nextRecord());
                 String option = inputStream.readUTF();
                 String value = inputStream.readUTF();
                 config.put(option, value);
             }
         } catch (RecordStoreNotOpenException e) {
             e.printStackTrace();
         } catch (RecordStoreException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return config;
     }
 
     /** Adds or updates configuration options. */
     public void saveConfig(Hashtable config) {
         Vector seenOptions = new Vector();
         try {
             // Update options if they are already stored
             RecordEnumeration re = options.enumerateRecords(null, null, false);
             while (re.hasNextElement()) {
                 // Get the record's id
                 int recordId = re.nextRecordId();
                 // Determine which option the record id corresponds to
                DataInputStream inputStream = getDataInputStream(re.nextRecord());
                 String option = inputStream.readUTF();
                 // Update the record with the new option value
                 byte[] b = getOptionBytes(option, (String)config.get(option));
                 options.setRecord(recordId, b, 0, b.length);
                 seenOptions.addElement(option);
             }
             // Add options if they haven't been stored yet
             Enumeration optionKeys = config.keys();
             while (optionKeys.hasMoreElements()) {
                 String option = (String)optionKeys.nextElement();
                 if (!seenOptions.contains(option)) {
                     byte[] b = getOptionBytes(option, (String)config.get(option));
                     options.addRecord(b, 0, b.length);
                 }
             }
         } catch (RecordStoreNotOpenException e) {
             e.printStackTrace();
         } catch (RecordStoreException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /** Creates a DataInputStream from bytes. */
     private DataInputStream getDataInputStream(byte[] record) {
         ByteArrayInputStream bais = new ByteArrayInputStream(record);
         return new DataInputStream(bais);
     }
 
     /** Creates bytes representing a configuration option. */
     private byte[] getOptionBytes(String option, String value) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream outputStream = new DataOutputStream(baos);
         outputStream.writeUTF(option);
         outputStream.writeUTF(value);
         return baos.toByteArray();
     }
 
     /** Closes the record stores. */
     public void close() {
         try {
             completion.closeRecordStore();
             options.closeRecordStore();
         } catch (RecordStoreException e) {
             e.printStackTrace();
         }
     }
 }
