 package org.bitrepository.integrityservice.reports;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 public class BasicIntegrityReporter implements IntegrityReporter {
 
     private final String collectionID;
     private Long deletedFilesCount = 0L;
     //Treemaps ensures alphapetical sorting.
     private final Map<String, Long> missingFiles = new TreeMap<String, Long>();
     private final Map<String, Long> checksumIssues = new TreeMap<String, Long>();
     private final Map<String, Long> missingChecksums = new TreeMap<String, Long>();
     private final Map<String, Long> obsoleteChecksums = new TreeMap<String, Long>();
     
     public BasicIntegrityReporter(String collectionID) {
         this.collectionID = collectionID;
     }
     
     @Override
     public boolean hasIntegrityIssues() {
         return !(
                 missingFiles.isEmpty() &&
                 checksumIssues.isEmpty() &&
                 missingChecksums.isEmpty() &&
                 obsoleteChecksums.isEmpty()
         );
     }
 
     @Override
     public String generateReport() {
         StringBuilder report = new StringBuilder();
         if(deletedFilesCount != 0L) {
             report.append("Detected " + deletedFilesCount + " files as removed from the collection.");
         }
         for(String pillar : missingFiles.keySet()) {
             if(missingFiles.get(pillar) != 0) {
                 report.append("\n" + pillar + " is missing " + missingFiles.get(pillar) + " file");
                 if (missingFiles.get(pillar) > 1) report.append("s");
                 report.append(".");
             }
         }
         
         for(String pillar : checksumIssues.keySet()) {
             if(checksumIssues.get(pillar) != 0) {
                 report.append("\n" + pillar + " has " + checksumIssues.get(pillar) + " potentially corrupt file");
                 if (checksumIssues.get(pillar) > 1) report.append("s");
                 report.append(".");
             }
         }
         
         for(String pillar : missingChecksums.keySet()) {
             if(missingChecksums.get(pillar) != 0) {
                 report.append("\n" + pillar + " is missing " + missingChecksums.get(pillar) + " checksum");
                 if (missingChecksums.get(pillar) > 1) report.append("s");
                 report.append(".");
             }
         }
         
         for(String pillar : obsoleteChecksums.keySet()) {
             if(obsoleteChecksums.get(pillar) != 0) {
                 report.append("\n" + pillar + " has " + obsoleteChecksums.get(pillar) + " obsolete checksum");
                 if (obsoleteChecksums.get(pillar) > 1) report.append("s");
                 report.append(".");
             }
         }
        return report.toString();
     }
 
     @Override
     public String generateSummaryOfReport() {
         if(!hasIntegrityIssues()) {
             return "No integrity issues found";
         } else {
             return "The following integrity issues where found:" +   generateReport();
         }
     }
 
     @Override
     public String getCollectionID() {
         return collectionID;
     }
 
     @Override
     public void reportDeletedFile(String fileID) {
         deletedFilesCount++;
     }
 
     @Override
     public void reportMissingFile(String fileID, String pillarID) {
         if(missingFiles.containsKey(pillarID)) {
             missingFiles.put(pillarID, (missingFiles.get(pillarID) + 1));
         } else {
             missingFiles.put(pillarID, 1L);
         }
     }
 
     @Override
     public void reportChecksumIssue(String fileID, String pillarID) {
         if(checksumIssues.containsKey(pillarID)) {
             checksumIssues.put(pillarID, (checksumIssues.get(pillarID) + 1));
         } else {
             checksumIssues.put(pillarID, 1L);
         }
     }
 
     @Override
     public void reportMissingChecksum(String fileID, String pillarID) {
         if(missingChecksums.containsKey(pillarID)) {
             missingChecksums.put(pillarID, (missingChecksums.get(pillarID) + 1));
         } else {
             missingChecksums.put(pillarID, 1L);
         }
     }
 
     @Override
     public void reportObsoleteChecksum(String fileID, String pillarID) {
         if(obsoleteChecksums.containsKey(pillarID)) {
             obsoleteChecksums.put(pillarID, (obsoleteChecksums.get(pillarID) + 1));
         } else {
             obsoleteChecksums.put(pillarID, 1L);
         }
     }
 }
