 package org.netmelody.menodora.core.locator;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.NameFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 
 public final class FileSystemLocator implements Locator {
 
     private final File root;
     private final String pattern;
 
     public FileSystemLocator(File root, String pattern) {
         this.root = root;
         this.pattern = pattern;
     }
     
     @Override
     public List<String> locate() {
         final List<File> files = new ArrayList<File>();
         
         int separatorIndex = pattern.lastIndexOf("/");
         if (-1 == separatorIndex) {
             files.addAll(FileUtils.listFiles(root, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE));
         }
         else {
             files.addAll(FileUtils.listFiles(root,
                                              new WildcardFileFilter(pattern.substring(separatorIndex + 1)),
                                              new NameFileFilter(pattern.substring(0, separatorIndex))));
         }
         
         
         final int rootLength = root.getPath().length();
         final List<String> result = new ArrayList<String>();
         for (File file : files) {
            result.add(file.getPath().substring(rootLength).replaceAll("\\" + File.separator, "/"));
         }
         
         return result;
     }
 }
