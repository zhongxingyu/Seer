 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOCase;
 import org.apache.commons.io.filefilter.*;
 
 import java.io.File;
 import java.util.*;
 
 /**
  * Author: odiab
  * Date: 6/21/13
  * Time: 5:57 PM
  * Finds latest patient records (Files) corresponding to a Schedule of patients.
  */
 public class FileSearcher {
   private final File directory;
   public FileSearcher(File directory)
   {
     this.directory = directory;
   }
 
   /**
    * Generates a filter that matches filenames that contain at least one
    * pair of first and last names for each ScheduleEntry in the Schedule
    * @param schedule Schedule to make a name filter from
    * @return Filter that matches any names present in the Schedule
    */
   private IOFileFilter generateNameFilter(Schedule schedule) {
     if (schedule.size() == 0) {
       return FalseFileFilter.INSTANCE;
     }
 
     // Generate filters for every ScheduleEntry.
     List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
     for (int i = 0; i < schedule.size(); ++i) {
       ScheduleEntry entry = schedule.get(i);
       IOFileFilter firstFilter;
       IOFileFilter lastFilter;
       try {
         firstFilter = new RegexFileFilter("(?i).*" + entry.getPatientFirst() + ".*");
         lastFilter = new RegexFileFilter("(?i).*" + entry.getPatientLast() + ".*");
       } catch (Exception e) {
         // something invalid about the first of last name, like a leading wildcard.
         // probably can't find those, anyway
         continue;
       }
 
       filters.add(FileFilterUtils.and(firstFilter, lastFilter));
     }
 
     // combine each ScheduleEntry's filters together
     IOFileFilter orFilter = filters.get(0);
     for (int i = 1; i < filters.size(); ++i) {
       orFilter = FileFilterUtils.or(orFilter, filters.get(i));
     }
 
     return orFilter;
   }
 
   /**
    * Finds latest file according to search terms. Expensive, so use with caution
    * @param searchTerms Array of search terms
    * @return File corresponding to the latest one, or null if none found
    */
   public File findLatestFile(String... searchTerms)
   {
     // create filter
     IOFileFilter filter = TrueFileFilter.INSTANCE;
     for (String searchTerm : searchTerms) {
       filter = FileFilterUtils.and(
             filter, new WildcardFileFilter("*" + searchTerm + "*", IOCase.INSENSITIVE)
       );
     }
 
     // find latest
     Iterator<File> iter = FileUtils.iterateFiles(directory, filter, null);
     File latest = null;
     while (iter.hasNext()) {
       File file = iter.next();
       if (latest == null || latest.lastModified() < file.lastModified()) {
         latest = file;
       }
     }
 
     return latest;
   }
 
   /**
    * Searches through entries in schedule for name referred to in by the file,
    * and updates the latestFiles map if the found file is newer.
    * @param schedule Schedule to search through
    * @param latestFiles Mapping between entries and the current-known latest files for them
    * @param file File to consider
    */
   private void findAndUpdateLatestFile(Schedule schedule,
                                               Map<ScheduleEntry, File> latestFiles,
                                               File file) {
     String filename = file.getName().toLowerCase();
     for (int i = 0; i < schedule.size(); ++i) {
       ScheduleEntry entry = schedule.get(i);
       if (filename.contains(entry.getPatientFirst().toLowerCase())
             && filename.contains(entry.getPatientLast().toLowerCase())) {
         File latest = latestFiles.get(entry);
         if (latest == null || latest.lastModified() < file.lastModified()) {
           latestFiles.put(entry, file);
         }
         return;
       }
     }
   }
 
   /**
    * Searches the filesystem for files corresponding to the schedule, and returns
    * the latest files for each entry.
    * @param schedule Schedule to find files of
    * @return Latest files for each ScheduleEntry in the schedule. Missing files are null
    */
   public Map<ScheduleEntry, File> searchSchedule(Schedule schedule) {
     Map<ScheduleEntry, File> result = new HashMap<ScheduleEntry, File>();
     IOFileFilter nameFilter = generateNameFilter(schedule);
 
     IOFileFilter directoryFilter = new WildcardFileFilter("20??");
     for (Day day : Day.values()) {
      directoryFilter = FileFilterUtils.and(
             directoryFilter,
             new WildcardFileFilter("*" + day.getDay() + "*", IOCase.INSENSITIVE)
       );
     }
 
     // Iterate through results and assign latest files to their appropriate entry.
     // Recurse through subdirectories
     Iterator<File> iter = FileUtils.iterateFiles(directory, nameFilter, directoryFilter);
     while (iter.hasNext()) {
       File file = iter.next();
       findAndUpdateLatestFile(schedule, result, file);
     }
 
     return result;
   }
 }
