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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.jgit.errors.CorruptObjectException;
 import org.eclipse.jgit.errors.IncorrectObjectTypeException;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.lib.FileMode;
 import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevObject;
 import org.eclipse.jgit.treewalk.TreeWalk;
 
 import com.microsoft.gittf.core.util.RepositoryPath;
 import com.microsoft.tfs.util.Check;
 
 public class CheckinAnalysisChangeCollection
 {
     private final Repository repository;
     private final RevObject targetTree;
     private final RevObject sourceTree;
 
     private final List<AddChange> adds = new ArrayList<AddChange>();
     private final List<EditChange> edits = new ArrayList<EditChange>();
     private final List<DeleteChange> deletes = new ArrayList<DeleteChange>();
     private final List<RenameChange> renames = new ArrayList<RenameChange>();
 
     private boolean processDeletedFolders = true;
 
     private Set<String> processedDeletedFolders = new HashSet<String>();
 
     public CheckinAnalysisChangeCollection()
     {
         this.repository = null;
         this.sourceTree = null;
         this.targetTree = null;
     }
 
     public CheckinAnalysisChangeCollection(
         final Repository repository,
         final RevObject sourceTree,
         final RevObject targetTree)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
         Check.notNull(sourceTree, "sourceTree"); //$NON-NLS-1$
         Check.notNull(targetTree, "targetTree"); //$NON-NLS-1$
 
         this.repository = repository;
         this.sourceTree = sourceTree;
         this.targetTree = targetTree;
     }
 
     public void setProcessDeletedFolders(boolean processDeletedFolders)
     {
         this.processDeletedFolders = processDeletedFolders;
     }
 
     public boolean isEmpty()
     {
         return adds.isEmpty() && edits.isEmpty() && deletes.isEmpty() && renames.isEmpty();
     }
 
     public final List<AddChange> getAdds()
     {
         return Collections.unmodifiableList(adds);
     }
 
     public final List<EditChange> getEdits()
     {
         return Collections.unmodifiableList(edits);
     }
 
     public final List<DeleteChange> getDeletes()
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         return Collections.unmodifiableList(deletes);
     }
 
     public final List<RenameChange> getRenames()
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         return Collections.unmodifiableList(renames);
     }
 
     public final void pendAdd(AddChange change)
         throws Exception
     {
         adds.add(change);
     }
 
     public final void pendEdit(EditChange change)
         throws Exception
     {
         edits.add(change);
     }
 
     public final void pendDelete(DeleteChange change)
         throws Exception
     {
         if (processDeletedFolders)
         {
             pendFolderDeleteIfNeeded(change.getType() == FileMode.TREE ? change.getPath()
                 : RepositoryPath.getParent(change.getPath()));
         }
 
         deletes.add(change);
     }
 
     public final void pendRename(RenameChange change)
         throws Exception
     {
         renames.add(change);
     }
 
     public int size()
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         return adds.size() + deletes.size() + edits.size() + renames.size();
     }
 
     public TfsFolderRenameDetector createFolderRenameDetector()
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         return new TfsFolderRenameDetector(repository, sourceTree, targetTree, getRenames());
     }
 
     private void pendFolderDeleteIfNeeded(String folderPath)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         if (repository == null || targetTree == null)
         {
             return;
         }
 
         ObjectReader objectReader = repository.newObjectReader();
         try
         {
             /* if this is a folder delete */
             String folderToDelete = getUpperMostFolderToDelete(folderPath, null, objectReader, targetTree);
 
             if (processedDeletedFolders.contains(folderToDelete))
             {
                 return;
             }
             processedDeletedFolders.add(folderToDelete);
 
             if (folderToDelete != null && folderToDelete.length() != 0)
             {
                 deletes.add(new DeleteChange(folderToDelete, FileMode.TREE));
             }
         }
         finally
         {
             if (objectReader != null)
             {
                 objectReader.release();
             }
         }
     }
 
     private String getUpperMostFolderToDelete(
         String filePath,
         String previousFilePath,
         ObjectReader objectReader,
         RevObject commitRevTree)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
         if (filePath == null || filePath.length() == 0)
         {
             return previousFilePath;
         }
 
         if (!folderExistsInTree(filePath, objectReader, commitRevTree))
         {
             return getUpperMostFolderToDelete(RepositoryPath.getParent(filePath), filePath, objectReader, commitRevTree);
         }
 
         return previousFilePath;
     }
 
     private boolean folderExistsInTree(String filePath, ObjectReader objectReader, RevObject commitRevTree)
         throws MissingObjectException,
             IncorrectObjectTypeException,
             CorruptObjectException,
             IOException
     {
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
         }
     }
 }
