 package fr.thumbnailsdb;
 
 import java.io.File;
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
                     String dir1 = fileToDirectory(dg.get(i));
                     String dir2 = fileToDirectory(dg.get(j));
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
 
     private String fileToDirectory(String n) {
         int folderIndex = n.lastIndexOf('/');
         if (folderIndex <0) {
             //it's probably a windows path
             folderIndex = n.lastIndexOf('\\');
         }
        // File file = new File(n);
 //            //File parentDir = file.getParentFile(); // to get the parent dir
         return n.substring(0,folderIndex);//file.getParent(); // to get the parent dir name
     }
 
 
     public DuplicateFolderGroup getDetails(String f1, String f2) {
         String couple = f1 + " <-> " + f2;
         return folderWithDuplicates.get(couple);
     }
 
     public Collection asSortedCollection(String[] filter, int max) {
        TreeSet<DuplicateFolderGroup> list = new TreeSet<DuplicateFolderGroup>(new Comparator<DuplicateFolderGroup>() {
            //	@Override
            public int compare(DuplicateFolderGroup o1, DuplicateFolderGroup o2) {
                return Double.compare(o2.totalSize, o1.totalSize);
            }
        });
         for (DuplicateFolderGroup d : folderWithDuplicates.values()) {
             if (match(d, filter)) {
                 list.add(d);
             }
         }
         ArrayList<DuplicateFolderGroup> al = new ArrayList<DuplicateFolderGroup>(list.size());
         Iterator<DuplicateFolderGroup> it = list.iterator();
         int i = 0;
         while (it.hasNext() && i<max) {
             al.add(it.next());
             i++;
 
         }
         return al;
     }
 
     private boolean match(DuplicateFolderGroup d, String[] filter) {
         for (String s : filter) {
             if (d.folder1.contains(s) || d.folder2.contains(s)) {
                 return true;
             }
         }
         return false;
     }
 
 
     public int size() {
         return folderWithDuplicates.keySet().size();
     }
 
 }
