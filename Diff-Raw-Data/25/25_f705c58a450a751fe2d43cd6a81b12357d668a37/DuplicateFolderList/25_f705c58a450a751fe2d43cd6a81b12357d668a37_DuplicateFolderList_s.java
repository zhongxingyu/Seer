 package fr.thumbnailsdb;
 
 import java.io.File;
 import java.nio.file.Files;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fhuet
  * Date: 25/10/12
  * Time: 16:47
  * To change this template use File | Settings | File Templates.
  */
 public class DuplicateFolderList {
     Map<String, DuplicateFolderGroup> folderWithDuplicates = new HashMap<String, DuplicateFolderGroup>();
 
     public DuplicateFolderList() {
 
     }
 
     /**
      * @param dg a group of identical files from potentially different directories
      */
     public void addOrIncrement(DuplicateFileGroup dg) {
         //  ArrayList<String> folderList = (ArrayList<String>) dg.getParentFolderList();
         if (dg.size() > 1) {
             try {
                 dg.sort();
             } catch (Exception e) {
                 //   e.printStackTrace();
                 System.out.println("NPE for " + dg);
             }
             for (int i = 0; i < dg.size() - 1; i++) {
                 for (int j = i + 1; j < dg.size(); j++) {
                     String dir1 = Utils.fileToDirectory(dg.get(i));
                     String dir2 = Utils.fileToDirectory(dg.get(j));
                     String couple = dir1 + " <-> " + dir2;
                     DuplicateFolderGroup dfg = folderWithDuplicates.get(couple);
                     if (dfg != null) {
                         //     System.out.println(" Key found, incrementing");
                         //folderWithDuplicates.put(couple, dfg + 1);
                         dfg.increase();
                         dfg.addSize(dg.fileSize);
                         dfg.addFiles(dg.get(i), dg.get(j));
                     } else {
                         //   System.out.println(" Key not found, adding");
                         dfg = new DuplicateFolderGroup(dir1, dir2);
                         dfg.addSize(dg.fileSize);
                         dfg.addFiles(dg.get(i), dg.get(j));
                         folderWithDuplicates.put(couple, dfg);
                     }
                 }
             }
         }
     }
 
 
     public DuplicateFolderGroup getDetails(String f1, String f2) {
         String couple = f1 + " <-> " + f2;
         return folderWithDuplicates.get(couple);
     }
 
     /**
      * Construct a collection of max DuplicateFolderGroup elements
      * which pass filters in filter
      * Complete each DuplicateFolderGroup with metadata
      * @param filter
      * @param max
      * @return
      */
     public Collection asSortedCollection(String[] filter, int max) {
         ArrayList<DuplicateFolderGroup> list = new ArrayList<DuplicateFolderGroup>();
         //filter elements
         for (DuplicateFolderGroup d : folderWithDuplicates.values()) {
             if (match(d, filter)) {
                 list.add(d);
             }
         }
         ArrayList<DuplicateFolderGroup> al = new ArrayList<DuplicateFolderGroup>(list.size());
        Collections.sort(list,new Comparator<DuplicateFolderGroup>() {
             //	@Override
             public int compare(DuplicateFolderGroup o1, DuplicateFolderGroup o2) {
                 return Double.compare(o2.totalSize, o1.totalSize);
             }
         });
         Iterator<DuplicateFolderGroup> it = list.iterator();
         //select only max elements
         int i = 0;
        while (it.hasNext() && i<max) {
             DuplicateFolderGroup dfg = it.next();
             for (String s : new File(dfg.folder1).list()) {
            System.out.println("   ---  " + s);
             }
            dfg.setFilesInFolder1(new File(dfg.folder1).list().length);
            dfg.setFilesInFolder2(new File(dfg.folder2).list().length);
 
             al.add(dfg);
             i++;
         }
         return al;
     }
 
     private boolean match(DuplicateFolderGroup d, String[] filter) {
         for (String s : filter) {
             if (d.folder1.contains(s) || d.folder2.contains(s)) {
 //                System.out.println("DuplicateFolderList.match " + d.folder1 + " OR " + d.folder2 + " matches " + s);
                 return true;
             }
         }
 //        System.out.println("DuplicateFolderList.match " + d.folder1 + " OR " + d.folder2 + " no match");
         return false;
     }
 
 
     public int size() {
         return folderWithDuplicates.keySet().size();
     }
 
 }
