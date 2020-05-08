 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Author: odiab
  * Date: 8/6/13
  * Time: 7:34 PM
  * Organizes files according to schedule
  */
 public class FileOrganizer {
   private File directory;
   private static final String NEW_PATIENT_FILE_NAME = "Unknown";
 
   public FileOrganizer(File directory)
   {
     this.directory = directory;
   }
 
 
   /**
    * Updates filename and moves old file based on appointment
    * @param entry Schedule entry to use metadata of
    * @param oldFile Old file to base copy from
    * @param targetFolder Target folder
    * @return The new file, or null on failure
    */
   private File updateFilename(ScheduleEntry entry, File oldFile, File targetFolder)
   {
     File newFile = constructFile(entry, targetFolder, FilenameUtils.getExtension(oldFile.getName()));
 
     try {
       FileUtils.copyFile(oldFile, newFile);
     } catch (IOException e) {
       // do nothing
     }
 
     return newFile;
   }
 
   /**
    * Creates new file from designated "new patient file" based on appointment
    * @param entry Schedule entry to use metadata of
    * @param targetFolder Target folder
    * @return The new file, or null on failure
    */
   private File createNewPatientFile(ScheduleEntry entry, File targetFolder)
   {
     return updateFilename(entry, findNewPatientFile(), targetFolder);
   }
 
   /**
    * Chooses a folder to put file in.
    * @param day Day of folder
    * @param doctor Doctor corresponding to folder
    * @return Decided folder
    */
   private File selectFolder(Day day, Doctor doctor) throws IOException
   {
     String baseName = doctor.getPrefix() + day.getDay();
     String folderName = baseName;
 
     // keep trying to find or make empty folder
     for (int i = 1; true; ++i) {
       File folder = new File(folderName);
       if (!folder.exists()) {
         boolean result = folder.mkdir();
         if (!result) {
           throw new IOException("Couldn't make directory, aborting");
         }
         return folder;
       }
 
       if (folder.list().length == 0) {
         return folder;
       }
 
       folderName = baseName + " " + i;
     }
   }
 
   /**
    * Capitalizes individual word
    * @param word Word to capitalize
    * @return Capitalized word
    */
   private String capitalizeWord(String word)
   {
     if (word.length() == 0) return word;
     word = word.trim();
     return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
   }
 
   /**
    * Capitalizes all words in arbitrary string
    * @param line String to capitalize
    * @return Capitalized string
    */
   private String capitalize(String line)
   {
     String result = "";
     String[] words = line.trim().split(" ");
     for (String word : words) {
       result = capitalizeWord(word) + " ";
     }
 
     return result.trim();
   }
 
   /**
    * Creates filename for file based on information provided
    * @param entry Schedule entry to get data from
    * @param targetFolder Folter to place in
    * @param extension Extension to use
    * @return new File
    */
   private File constructFile(ScheduleEntry entry, File targetFolder, String extension) {
     SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
     return new File (
           targetFolder.getAbsolutePath() + File.separator + capitalize(entry.getPatientLast())
           + ", " + capitalize(entry.getPatientFirst()) + ", "
          + formatter.format(entry.getApptTime()) + extension
     );
   }
 
   /**
    * Finds the designated new patient file
    * @return The new patient file File
    */
   private File findNewPatientFile()
   {
     FileCache cache = DiabPatientOrganizer.getCache();
     if (cache.contains(NEW_PATIENT_FILE_NAME)) {
       return cache.get(NEW_PATIENT_FILE_NAME);
     }
 
     FileSearcher searcher = new FileSearcher(directory);
     return searcher.findLatestFile(NEW_PATIENT_FILE_NAME);
   }
 
   private UpdateData organizeDoctorDaySchedule(Schedule schedule, Doctor doctor, Day day)
   {
     FileSearcher searcher = new FileSearcher(directory);
     Map<ScheduleEntry, File> scheduleFiles = searcher.searchSchedule(schedule);
 
     File targetFolder;
     try {
       targetFolder = selectFolder(day, doctor);
     } catch (IOException e) {
       e.printStackTrace();
       return null;
     }
 
     List<File> newPatients = new ArrayList<File>();
     for (int i = 0; i < schedule.size(); ++i) {
       ScheduleEntry entry = schedule.get(i);
 
       // Handle missing patients
       if (!scheduleFiles.containsKey(entry)) {
         newPatients.add(createNewPatientFile(entry, targetFolder));
       } else {
         updateFilename(entry, scheduleFiles.get(entry), targetFolder);
       }
     }
 
     return new UpdateData(targetFolder, newPatients);
   }
 
   private List<UpdateData> organizeDoctorSchedule(Schedule schedule, Doctor doctor)
   {
     Map<Day, Schedule> splitSchedule = splitScheduleByDay(schedule);
 
     List<UpdateData> foldersUpdated = new ArrayList<UpdateData>();
     for (Day day : splitSchedule.keySet()) {
       UpdateData data = organizeDoctorDaySchedule(splitSchedule.get(day), doctor, day);
       if (data == null) {
         return null;
       }
       foldersUpdated.add(data);
     }
 
     return foldersUpdated;
   }
 
   /**
    * Organizes files in the filesystem based on the schedule
    * @param schedule Schedule of patients to organize
    * @return List of folders patient files saved to
    */
   public List<UpdateData> organizeSchedule(Schedule schedule)
   {
     List<UpdateData> foldersUpdated = new ArrayList<UpdateData>();
 
     for (Doctor doctor : Doctor.values()) {
       List<UpdateData> doctorFolders = organizeDoctorSchedule(schedule.getDoctor(doctor), doctor);
       if (doctorFolders == null) {
         return null;
       }
       foldersUpdated.addAll(doctorFolders);
     }
 
     return foldersUpdated;
   }
 
   private Map<Day, Schedule> splitScheduleByDay(Schedule schedule) {
     Map<Day, Schedule> splitSchedule = new HashMap<Day, Schedule>();
 
     for (Day day : Day.values()) {
       Schedule daySchedule = schedule.getDay(day);
       if (daySchedule.size() > 0) {
         splitSchedule.put(day, daySchedule);
       }
     }
 
     return splitSchedule;
   }
 }
