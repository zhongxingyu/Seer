 package com.braintest.service.impl;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.braintest.exception.UnpackerNotFoundException;
 import com.braintest.service.UnpackManager;
 import com.braintest.service.UnpackService;
 
 /**
  * ArchiveManagerImpl
  *
  * @author den, @date 29.09.2012 0:53:11
  */
 public class UnpackManagerImpl implements UnpackManager {
 
     private final Map<String, UnpackService> unpackers = new HashMap<String, UnpackService>();
 
     {
         Map<String, UnpackService> temp = new HashMap<String, UnpackService>();
         temp.put("zip", new ZipUnpackService());
         temp.put("rar", null);
         setUnpackers(temp);
     }
 
     @Override
     public boolean hasArchiveInPath(String path) {
        // TODO: this check better to do through regexp
         for (String type : unpackers.keySet()) {
             if (path.indexOf("." + type) > -1) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean isArchive(String fileName) {
         String ext = FilenameUtils.getExtension(fileName);
         return unpackers.keySet().contains(ext);
     }
 
     @Override
     public String unpack(String path) throws UnpackerNotFoundException, IOException {
 
         StringBuilder archDir = new StringBuilder();
         String[] dirs = StringUtils.split(path, FileWalkerService.PATH_SEPARATOR);
 
         for (String dir : dirs) {
 
             archDir.append(FileWalkerService.PATH_SEPARATOR);
             archDir.append(dir);
 
             String ext = FilenameUtils.getExtension(dir);
 
             if (unpackers.containsKey(ext)) {
                 UnpackService unpacker = unpackers.get(ext);
                 if (unpacker != null) {
                     String unpackDir = unpacker.unpack(archDir.toString());
                     archDir = new StringBuilder(unpackDir);
                 } else {
                     throw new UnpackerNotFoundException("Unpacker for type " + ext + " not found");
                 }
             }
         }
         return archDir.toString();
     }
 
     public void setUnpackers(Map<String, UnpackService> unpackers) {
         this.unpackers.putAll(unpackers);
     }
 }
