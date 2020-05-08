 package com.livejournal.karino2.whiteboardcast;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created by karino on 10/23/13.
  */
 public class SlideList {
     List<File> listedFiles;
     File[] actualFiles;
     public SlideList(List<File> listed, File[] actuals) {
         listedFiles = listed;
         actualFiles = actuals;
     }
 
     public void syncListedActual() {
         removeNonExistingFileFromList();
         addNonRegistedFileToList();
     }
 
     public List<File> getFiles() {
         return listedFiles;
     }
 
     public void upFiles(int[] indexes) {
         int insertAboveTo = indexes[0];
         List<File> moveCandidate = idsToFiles(indexes);
         listedFiles.removeAll(moveCandidate);
         listedFiles.addAll(Math.max(0, insertAboveTo - 1), moveCandidate);
     }
 
     public void downFiles(int[] indexes) {
         int insertBelowTo = indexes[indexes.length-1];
         List<File> moveCandidate = idsToFiles(indexes);
         listedFiles.removeAll(moveCandidate);
        listedFiles.addAll(Math.min(listedFiles.size(), insertBelowTo + 1 - moveCandidate.size()), moveCandidate);
     }
 
     public void addFiles(List<File> adds) {
         listedFiles.addAll(adds);
     }
 
     private List<File> idsToFiles(int[] indexes) {
         List<File> moveCandidate = new ArrayList<File>();
         for(int i : indexes) {
             moveCandidate.add(listedFiles.get(i));
         }
         return moveCandidate;
     }
 
     private void removeNonExistingFileFromList() {
         Set<File> actuals = new HashSet(Arrays.asList(actualFiles));
 
         ArrayList<File> removeCandidate = new ArrayList<File>();
         for(File file: listedFiles) {
             if(!actuals.contains(file))
                 removeCandidate.add(file);
         }
         listedFiles.removeAll(removeCandidate);
     }
 
     private void addNonRegistedFileToList() {
         Set<File> listed = new HashSet(listedFiles);
         for(File file : actualFiles) {
             if(!listed.contains(file))
                 listedFiles.add(file);
         }
     }
 
     public void add(File file) {
         listedFiles.add(file);
     }
 }
