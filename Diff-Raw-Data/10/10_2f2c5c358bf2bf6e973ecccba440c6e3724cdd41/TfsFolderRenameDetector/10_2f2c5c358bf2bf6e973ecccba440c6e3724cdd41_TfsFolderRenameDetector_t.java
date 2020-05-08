 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.core.tasks.pendDiff;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.jgit.errors.CorruptObjectException;
 import org.eclipse.jgit.errors.IncorrectObjectTypeException;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevObject;
 import org.eclipse.jgit.treewalk.TreeWalk;
 
 import com.microsoft.gittf.core.util.RepositoryPath;
 import com.microsoft.tfs.util.Check;
 
 public class TfsFolderRenameDetector
 {
     private final Repository repository;
     private final RevObject targetTree;
     private final RevObject sourceTree;
     private final List<RenameChange> fileRenames;
 
     private Set<RenameChange> sortedFileRenames = new TreeSet<RenameChange>(new RenameChangeOldPathCompartor());
     private Map<String, RenameChange> processedRenames = new HashMap<String, RenameChange>();
     private Set<String> processedOldPaths = new TreeSet<String>(Collections.reverseOrder());
 
     private List<RenameChange> resultRenames = new ArrayList<RenameChange>();
     private List<List<RenameChange>> resultBatchedRenames = new ArrayList<List<RenameChange>>();
 
     public TfsFolderRenameDetector(
         Repository repository,
         RevObject sourceTree,
         RevObject targetTree,
         List<RenameChange> fileRenames)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
         Check.notNull(sourceTree, "sourceTree"); //$NON-NLS-1$
         Check.notNull(targetTree, "targetTree"); //$NON-NLS-1$
         Check.notNull(fileRenames, "fileRenames"); //$NON-NLS-1$
 
         this.repository = repository;
         this.sourceTree = sourceTree;
         this.targetTree = targetTree;
         this.fileRenames = fileRenames;
     }
 
     public List<List<RenameChange>> getRenameBatches()
     {
         return resultBatchedRenames;
     }
 
     public List<RenameChange> getRenames()
     {
         return resultRenames;
     }
 
     public void compute()
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         buildSortedFileRenames();
 
         for (RenameChange rename : sortedFileRenames)
         {
             if (isFileOnlyRename(rename) || isDecendantDestinationRename(rename))
             {
                 addRenameToResult(rename.getOldPath(), rename);
             }
             else
             {
                 processRename(rename);
             }
         }
     }
 
     private void buildSortedFileRenames()
     {
         for (RenameChange rename : fileRenames)
         {
             sortedFileRenames.add(rename);
         }
     }
 
     private void processRename(RenameChange rename)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         processRenameLevel(rename.getOldPath(), rename.getNewPath());
 
         ensureRenameAccountedFor(rename);
     }
 
     private boolean processRenameLevel(String oldPath, String newPath)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         if (oldPath == null || oldPath.length() == 0 || newPath == null || newPath.length() == 0)
         {
             return false;
         }
 
         String oldPathToUse = updateOldPathWithProcessed(oldPath, newPath);
         String newPathToUse = newPath;
 
         if (oldPath.equals(newPath))
         {
             return false;
         }
 
         if (RepositoryPath.isAncestor(newPathToUse, oldPathToUse))
         {
             return true;
         }
 
         boolean addThisLayer = false;
         String oldFileName = RepositoryPath.getFileName(oldPathToUse);
         String newFileName = RepositoryPath.getFileName(newPathToUse);
 
         if (!oldFileName.equals(newFileName))
         {
             addThisLayer = true;
         }
 
         if (processRenameLevel(RepositoryPath.getParent(oldPath), RepositoryPath.getParent(newPathToUse)))
         {
             addThisLayer = true;
         }
 
         if (addThisLayer)
         {
             oldPathToUse = updateOldPathWithProcessed(oldPath, newPath);
             newPathToUse = newPath;
 
             if (processedOldPaths.contains(oldPath))
             {
                 return false;
             }
 
             if (!folderExistsInTree(newPathToUse, sourceTree) && !folderExistsInTree(oldPathToUse, targetTree))
             {
                 addRenameToResult(oldPath, oldPathToUse, newPathToUse);
 
                 return false;
             }
         }
 
         return false;
     }
 
     private void ensureRenameAccountedFor(RenameChange rename)
     {
         String updatedOldPath = updateOldPathWithProcessed(rename.getOldPath(), rename.getNewPath());
 
         if (!updatedOldPath.equals(rename.getNewPath()))
         {
             addRenameToResult(
                 rename.getOldPath(),
                 new RenameChange(updatedOldPath, rename.getNewPath(), rename.getObjectID(), rename.isEdit()));
         }
         else
         {
             if (rename.isEdit())
             {
                 ensureEditAccountedFor(rename);
             }
         }
     }
 
     private void ensureEditAccountedFor(RenameChange rename)
     {
         if (processedOldPaths.contains(rename.getOldPath()))
         {
             processedRenames.get(rename.getOldPath()).updateEditInformation(rename.getObjectID());
         }
         else
         {
             String updatedOldPath = updateOldPathWithProcessed(rename.getOldPath(), rename.getNewPath());
             addRenameToResult(
                 rename.getOldPath(),
                 new RenameChange(updatedOldPath, rename.getNewPath(), rename.getObjectID(), rename.isEdit()));
         }
     }
 
     private void addRenameToResult(String unprocessedOldPath, String oldPath, String newPath)
     {
         addRenameToResult(unprocessedOldPath, new RenameChange(oldPath, newPath, ObjectId.zeroId(), false));
     }
 
     private void addRenameToResult(String unprocessedOldPath, RenameChange rename)
     {
         if (processedOldPaths.contains(rename.getOldPath()))
         {
             RenameChange addedRenameObject = processedRenames.get(rename.getOldPath());
             if (addedRenameObject.isEdit() != rename.isEdit())
             {
                 addedRenameObject.updateEditInformation(rename.getObjectID());
             }
 
             return;
         }
 
         resultRenames.add(rename);
         processedOldPaths.add(unprocessedOldPath);
         processedRenames.put(unprocessedOldPath, rename);
 
         addRenameToBatchedResult(rename);
     }
 
     private void addRenameToBatchedResult(RenameChange rename)
     {
         int depth = RepositoryPath.getFolderDepth(rename.getNewPath());
 
         for (int toAdd = resultBatchedRenames.size(); toAdd <= depth; toAdd++)
         {
             resultBatchedRenames.add(new ArrayList<RenameChange>());
         }
 
         resultBatchedRenames.get(depth).add(rename);
     }
 
     private boolean isFileOnlyRename(RenameChange rename)
     {
         String oldParent = RepositoryPath.getParent(rename.getOldPath());
         String newParent = RepositoryPath.getParent(rename.getNewPath());
 
         return oldParent.equals(newParent);
     }
 
     private boolean isDecendantDestinationRename(RenameChange rename)
     {
         String oldParent = RepositoryPath.getParent(rename.getOldPath());
         String newParent = RepositoryPath.getParent(rename.getNewPath());
 
         return RepositoryPath.isAncestor(newParent, oldParent);
     }
 
     private String updateOldPathWithProcessed(String oldPath, String newPath)
     {
         for (String processedPath : processedOldPaths)
         {
             if (RepositoryPath.isAncestor(oldPath, processedPath))
             {
                 RenameChange processedRename = processedRenames.get(processedPath);
 
                 if (RepositoryPath.isAncestor(newPath, processedRename.getNewPath()))
                 {
                     return processedRename.getNewPath() + oldPath.substring(processedPath.length());
                 }
             }

            if (oldPath.equals(processedPath))
            {
                RenameChange processedRename = processedRenames.get(processedPath);

                if (newPath.equals(processedRename.getNewPath()))
                {
                    return processedRename.getNewPath();
                }
            }
         }
 
         return oldPath;
     }
 
     private boolean folderExistsInTree(String filePath, RevObject commitRevTree)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         ObjectReader objectReader = repository.newObjectReader();
         TreeWalk folder = null;
         try
         {
             folder = TreeWalk.forPath(objectReader, filePath, commitRevTree);
             return folder != null;
         }
         finally
         {
             if (folder != null)
             {
                 folder.release();
             }
 
             if (objectReader != null)
             {
                 objectReader.release();
             }
         }
     }
 }
