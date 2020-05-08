 /*
  * Copyright (c) 2012 by Samuel Berner (samuel.berner@gmail.com), all rights reserved
  * Created on Jul 2, 2012
  */
 package figurabia.io.workspace;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 public class LocalFileWorkspace extends AbstractWorkspace {
 
     private final File workspaceDirectory;
     private final int prefixToRemove;
 
     public LocalFileWorkspace(File workspaceDirectory) {
         this.workspaceDirectory = workspaceDirectory;
         prefixToRemove = workspaceDirectory.getAbsolutePath().length();
     }
 
     @Override
     public List<String> list(String basePath) {
         File baseDir = new File(workspaceDirectory, basePath);
 
         return filesToBasePaths(baseDir.listFiles());
     }
 
     private List<String> filesToBasePaths(File[] files) {
         if (files == null)
             return Collections.emptyList();
         List<String> paths = new ArrayList<String>();
         for (File f : files) {
            if (f.isFile())
                paths.add(fileToResPath(f));
         }
         return paths;
     }
 
     private String fileToResPath(File f) {
         return f.getAbsolutePath().substring(prefixToRemove);
     }
 
     private File resPathToFile(String resourcePath) {
         return new File(workspaceDirectory, resourcePath);
     }
 
     @Override
     public File fileForReading(String resourcePath) {
         return resPathToFile(resourcePath);
     }
 
     @Override
     public File fileForWriting(String resourcePath) {
         return resPathToFile(resourcePath);
     }
 
     @Override
     public void delete(String resourcePath) {
         File f = resPathToFile(resourcePath);
         if (!f.isFile())
             throw new IllegalArgumentException("Given resourcePath " + resourcePath + " does not exist as a file.");
         if (!f.delete())
             throw new WorkspaceException("Resource " + resourcePath + " could not be successfully deleted");
         notifyWorkspaceUpdateListeners(resourcePath, ChangeType.DELETED);
     }
 
     @Override
     public boolean exists(String resourcePath) {
         return resPathToFile(resourcePath).exists();
     }
 
     @Override
     public void move(String oldPath, String newPath) {
         if (exists(newPath))
             throw new WorkspaceException("Cannot move resource " + oldPath + " to " + newPath
                     + " because it already exists.");
 
         try {
             FileUtils.moveFile(resPathToFile(oldPath), resPathToFile(newPath));
         } catch (IOException e) {
             throw new WorkspaceException("Error moving resource " + oldPath + " to " + newPath, e);
         }
         notifyWorkspaceUpdateListeners(oldPath, ChangeType.DELETED);
         notifyWorkspaceUpdateListeners(newPath, ChangeType.CREATED);
     }
 
     @Override
     public void copyPath(String path, String copyPath) {
         if (!exists(path))
             return;
         if (exists(copyPath))
             throw new WorkspaceException("Cannot copy resource path because destination already exists: " + copyPath);
 
         try {
             FileUtils.copyDirectory(resPathToFile(path), resPathToFile(copyPath));
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         notifyWorkspaceUpdateListeners(copyPath, ChangeType.PATH_CREATED);
     }
 
     @Override
     public void deletePath(String path) {
         File f = resPathToFile(path);
         if (!f.exists())
             throw new IllegalArgumentException("Cannot delete file that does not exist: " + path);
         if (!FileUtils.deleteQuietly(f))
             throw new WorkspaceException("Resource " + path + " could not be successfully deleted");
         notifyWorkspaceUpdateListeners(path, ChangeType.PATH_DELETED);
     }
 
 }
