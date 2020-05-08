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
 
 package com.microsoft.gittf.core.tasks;
 
 import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.lib.FileMode;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectInserter;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevTree;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
 import org.eclipse.jgit.treewalk.TreeWalk;
 
 import com.microsoft.gittf.core.GitTFConstants;
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.config.ChangesetCommitMap;
 import com.microsoft.gittf.core.interfaces.VersionControlService;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressDisplay;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.gittf.core.util.StashUtil;
 import com.microsoft.gittf.core.util.tree.CommitTreeEntry;
 import com.microsoft.gittf.core.util.tree.CommitTreePath;
 import com.microsoft.gittf.core.util.tree.CommitTreePathComparator;
 import com.microsoft.tfs.core.clients.versioncontrol.GetItemsOptions;
 import com.microsoft.tfs.core.clients.versioncontrol.PropertyConstants;
 import com.microsoft.tfs.core.clients.versioncontrol.PropertyUtils;
 import com.microsoft.tfs.core.clients.versioncontrol.path.ServerPath;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.DeletedState;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingSet;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
 import com.microsoft.tfs.util.FileHelpers;
 
 public abstract class CreateCommitForPendingSetsTask
     extends CreateCommitTask
 {
     private static final Log log = LogFactory.getLog(CreateCommitForPendingSetsTask.class);
 
     private boolean createStashCommit = false;
 
     public CreateCommitForPendingSetsTask(
         final Repository repository,
         final VersionControlService versionControlClient,
         ObjectId parentCommitID)
     {
         super(repository, versionControlClient, parentCommitID);
     }
 
     public void setCreateStashCommit(boolean createStashCommit)
     {
         this.createStashCommit = createStashCommit;
     }
 
     public abstract String getProgressMonitorMessage();
 
     public abstract PendingSet[] getPendingSets();
 
     public abstract String getOwnerDisplayName();
 
     public abstract String getOwner();
 
     public abstract String getCommitterDisplayName();
 
     public abstract String getCommitter();
 
     public abstract Calendar getCommitDate();
 
     public abstract String getComment();
 
     public abstract String getName();
 
     @Override
     public TaskStatus run(final TaskProgressMonitor progressMonitor)
     {
         progressMonitor.beginTask(getProgressMonitorMessage(), 1, TaskProgressDisplay.DISPLAY_SUBTASK_DETAIL);
 
         ObjectInserter repositoryInserter = null;
         TreeWalk treeWalker = null;
         RevWalk walk = null;
 
         try
         {
             validateTempDirectory();
 
             Item rootServerItem =
                 versionControlService.getItem(
                     serverPath,
                     LatestVersionSpec.INSTANCE,
                     DeletedState.NON_DELETED,
                     GetItemsOptions.NONE);
 
             String serverPathToUse = rootServerItem.getServerItem();
 
             Set<String> pendingSetItemPath = new TreeSet<String>();
             Map<String, PendingChange> pendingSetMap = new HashMap<String, PendingChange>();
 
             PendingSet[] pendingSets = getPendingSets();
 
             /*
              * keep track of the items added, renamed and folders renamed for
              * special handling later
              */
             Set<String> itemsAddedInPendingSet = new TreeSet<String>();
             Set<String> itemsRenamedInPendingSet = new TreeSet<String>();
             Set<String> itemsDeletedInPendingSet = new TreeSet<String>();
 
             Set<String> foldersRenamedInPendingSet = new TreeSet<String>(Collections.reverseOrder());
             Set<String> foldersDeletedInPendingSet = new TreeSet<String>();
 
             progressMonitor.displayVerbose(Messages.getString("CreateCommitForPendingSetsTask.VerboseItemsProcessedFromPendingSets")); //$NON-NLS-1$
 
             for (PendingSet set : pendingSets)
             {
                 for (PendingChange change : set.getPendingChanges())
                 {
                     String serverItem = change.getServerItem();
                     String sourceServerItem =
                         change.getSourceServerItem() != null ? change.getSourceServerItem() : null;
 
                     String pathToUse = serverItem;
 
                     ChangeType changeType = change.getChangeType();
 
                     if (change.getItemType() == ItemType.FILE)
                     {
                         if (changeType.contains(ChangeType.ADD)
                             || changeType.contains(ChangeType.BRANCH)
                             || changeType.contains(ChangeType.UNDELETE))
                         {
                             itemsAddedInPendingSet.add(serverItem);
                         }
                         else if (changeType.contains(ChangeType.RENAME))
                         {
                             itemsRenamedInPendingSet.add(sourceServerItem);
 
                             pathToUse = sourceServerItem;
                         }
                         else if (changeType.contains(ChangeType.DELETE))
                         {
                             itemsDeletedInPendingSet.add(serverItem);
                         }
                         else
                         {
                             /*
                              * in case there is a source server item use that.
                              * This will be true in the case of a file edit and
                              * its parent has been renamed
                              */
                             if (change.getSourceServerItem() != null)
                             {
                                 pathToUse = sourceServerItem;
                             }
                         }
                     }
                     else if (change.getItemType() == ItemType.FOLDER)
                     {
                         if (changeType.contains(ChangeType.RENAME))
                         {
                             foldersRenamedInPendingSet.add(sourceServerItem);
 
                             pathToUse = sourceServerItem;
                         }
                         else if (changeType.contains(ChangeType.DELETE))
                         {
                             foldersDeletedInPendingSet.add(serverItem);
                         }
                     }
 
                     progressMonitor.displayVerbose(pathToUse);
 
                     pendingSetItemPath.add(pathToUse);
                     pendingSetMap.put(pathToUse, change);
                 }
             }
 
             progressMonitor.displayVerbose(""); //$NON-NLS-1$
 
             progressMonitor.setWork(pendingSetItemPath.size());
 
             repositoryInserter = repository.newObjectInserter();
             treeWalker = new NameConflictTreeWalk(repository);
             walk = new RevWalk(repository);
 
             ObjectId baseCommitId = parentCommitID;
             if (baseCommitId == null)
             {
                 ChangesetCommitMap commitMap = new ChangesetCommitMap(repository);
                 baseCommitId = commitMap.getCommitID(commitMap.getLastBridgedChangesetID(true), false);
             }
 
             RevCommit parentCommit = walk.parseCommit(baseCommitId);
             if (parentCommit == null)
             {
                 throw new Exception(
                     Messages.getString("CreateCommitForPendingSetsTask.LatestDownloadedChangesetNotFound")); //$NON-NLS-1$
             }
 
             RevTree baseCommitTree = parentCommit.getTree();
 
             /*
              * We want trees sorted by children first so we can simply walk them
              * (child-first) to build the hierarchy once we've finished
              * inserting blobs.
              */
 
             final Map<CommitTreePath, Map<CommitTreePath, CommitTreeEntry>> baseTreeHeirarchy =
                 new TreeMap<CommitTreePath, Map<CommitTreePath, CommitTreeEntry>>(new CommitTreePathComparator());
 
             final Map<CommitTreePath, Map<CommitTreePath, CommitTreeEntry>> pendingSetTreeHeirarchy =
                 new TreeMap<CommitTreePath, Map<CommitTreePath, CommitTreeEntry>>(new CommitTreePathComparator());
 
             treeWalker.setRecursive(true);
             treeWalker.addTree(baseCommitTree);
 
             /*
              * Phase one: build the pending set commit tree by copying the
              * parent tree
              */
 
             progressMonitor.displayVerbose(Messages.getString("CreateCommitForPendingSetsTask.VerboseItemsDownloadedFromPendingSets")); //$NON-NLS-1$
 
             while (treeWalker.next())
             {
                 String itemServerPath = ServerPath.combine(serverPathToUse, treeWalker.getPathString());
 
                 /* if the item has a pending change apply the pending change */
                 if (pendingSetItemPath.contains(itemServerPath))
                 {
                     progressMonitor.displayVerbose(itemServerPath);
 
                     if (createStashCommit)
                     {
                         createBlob(
                             repositoryInserter,
                             baseTreeHeirarchy,
                             pendingSetMap.get(itemServerPath),
                             true,
                             progressMonitor);
                     }
 
                     if (!itemsDeletedInPendingSet.contains(itemServerPath)
                         && !itemsRenamedInPendingSet.contains(itemServerPath))
                     {
                         createBlob(
                             repositoryInserter,
                             pendingSetTreeHeirarchy,
                             pendingSetMap.get(itemServerPath),
                             false,
                             progressMonitor);
                     }
 
                     progressMonitor.worked(1);
                 }
                 /* if the item parent is renamed handle this case */
                 else if (isParentInCollection(foldersRenamedInPendingSet, itemServerPath))
                 {
                     if (createStashCommit)
                     {
                         createBlob(
                             repositoryInserter,
                             baseTreeHeirarchy,
                             itemServerPath,
                             treeWalker.getObjectId(0),
                             treeWalker.getFileMode(0),
                             progressMonitor);
                     }
 
                    String destinationServerItem =
                        updateServerItemWithParentRename(foldersRenamedInPendingSet, itemServerPath, pendingSetMap);
                    if (ServerPath.isChild(serverPathToUse, destinationServerItem))
                    {
                        createBlob(
                            repositoryInserter,
                            pendingSetTreeHeirarchy,
                            destinationServerItem,
                            treeWalker.getObjectId(0),
                            treeWalker.getFileMode(0),
                            progressMonitor);
                    }
                 }
                 /*
                  * add all other items to the tree unless their parent was
                  * deleted
                  */
                 else
                 {
                     if (createStashCommit)
                     {
                         createBlob(
                             repositoryInserter,
                             baseTreeHeirarchy,
                             itemServerPath,
                             treeWalker.getObjectId(0),
                             treeWalker.getFileMode(0),
                             progressMonitor);
                     }
 
                     if (!isParentInCollection(foldersDeletedInPendingSet, itemServerPath))
                     {
                         createBlob(
                             repositoryInserter,
                             pendingSetTreeHeirarchy,
                             itemServerPath,
                             treeWalker.getObjectId(0),
                             treeWalker.getFileMode(0),
                             progressMonitor);
                     }
                 }
             }
 
             progressMonitor.displayVerbose(""); //$NON-NLS-1$
 
             /* for items that were added in the shelveset add those here */
 
             progressMonitor.displayVerbose(Messages.getString("CreateCommitForPendingSetsTask.VerboseItemsDownloadedFromPendingSetsAdds")); //$NON-NLS-1$
 
             for (String newItem : itemsAddedInPendingSet)
             {
                 if (!ServerPath.isChild(serverPathToUse, newItem))
                 {
                     // Ignore files that are added that are not mapped in the
                     // repository
                     continue;
                 }
 
                 progressMonitor.displayVerbose(newItem);
 
                 createBlob(
                     repositoryInserter,
                     pendingSetTreeHeirarchy,
                     pendingSetMap.get(newItem),
                     false,
                     progressMonitor);
 
                 progressMonitor.worked(1);
             }
 
             for (String renamedItem : itemsRenamedInPendingSet)
             {
                 PendingChange change = pendingSetMap.get(renamedItem);
 
                 if (!ServerPath.isChild(serverPathToUse, change.getServerItem()))
                 {
                     // Ignore files that are renamed to server items that are
                     // outside the repository
                     continue;
                 }
 
                 progressMonitor.displayVerbose(renamedItem);
 
                 createBlob(repositoryInserter, pendingSetTreeHeirarchy, change, false, progressMonitor);
 
                 progressMonitor.worked(1);
             }
 
             progressMonitor.displayVerbose(""); //$NON-NLS-1$
 
             /* Phase two: add child trees to their parents. */
             progressMonitor.setDetail(Messages.getString("CreateCommitTask.CreatingTrees")); //$NON-NLS-1$
 
             ObjectId rootBaseTree = createStashCommit ? createTrees(repositoryInserter, baseTreeHeirarchy) : null;
             ObjectId rootPendingSetTree = createTrees(repositoryInserter, pendingSetTreeHeirarchy);
 
             /* Phase three: create the commit. */
             progressMonitor.setDetail(Messages.getString("CreateCommitTask.CreatingCommit")); //$NON-NLS-1$
 
             if (createStashCommit)
             {
                 this.commitId =
                     StashUtil.create(
                         repository,
                         repositoryInserter,
                         rootBaseTree,
                         rootPendingSetTree,
                         rootBaseTree,
                         parentCommitID,
                         getOwnerDisplayName(),
                         getOwner(),
                         getComment(),
                         getName());
             }
             else
             {
                 this.commitId = createCommit(repositoryInserter, rootPendingSetTree, parentCommitID);
             }
 
             progressMonitor.endTask();
 
             return TaskStatus.OK_STATUS;
         }
         catch (Exception e)
         {
             log.error(e);
             return new TaskStatus(TaskStatus.ERROR, e);
         }
         finally
         {
             FileHelpers.deleteDirectory(tempDir);
 
             if (repositoryInserter != null)
             {
                 repositoryInserter.release();
             }
 
             if (treeWalker != null)
             {
                 treeWalker.release();
             }
 
             if (walk != null)
             {
                 walk.release();
             }
         }
     }
 
     private String updateServerItemWithParentRename(
         Set<String> folderCollection,
         String serverPath,
         Map<String, PendingChange> pendingSetMap)
     {
         String parentToUpdate = getParentInCollection(folderCollection, serverPath);
 
         Check.notNull(parentToUpdate, "parentToUpdate"); //$NON-NLS-1$
 
         PendingChange pendingChange = pendingSetMap.get(parentToUpdate);
 
         Check.notNull(pendingChange, "pendingChange"); //$NON-NLS-1$
 
         String newParentName = pendingChange.getServerItem();
 
         return newParentName + serverPath.substring(parentToUpdate.length());
     }
 
     private boolean isParentInCollection(Set<String> folderCollection, String serverPath)
     {
         return getParentInCollection(folderCollection, serverPath) != null;
     }
 
     private String getParentInCollection(Set<String> folderCollection, String serverPath)
     {
         String currentPath = ServerPath.getParent(serverPath);
         while (currentPath != null && currentPath.length() > 0 && !currentPath.equals(ServerPath.ROOT))
         {
             if (folderCollection.contains(currentPath))
             {
                 return currentPath;
             }
 
             currentPath = ServerPath.getParent(currentPath);
         }
 
         return null;
     }
 
     private void createBlob(
         final ObjectInserter repositoryInserter,
         final Map<CommitTreePath, Map<CommitTreePath, CommitTreeEntry>> treeHierarchy,
         final PendingChange pendingChange,
         final boolean addBaseContent,
         final TaskProgressMonitor progressMonitor)
         throws Exception
     {
         if (pendingChange.getItemType() == ItemType.FOLDER)
         {
             return;
         }
 
         File tempFile = null;
         InputStream tempInputStream = null;
         ObjectId blobID = null;
 
         try
         {
             tempFile = File.createTempFile(GitTFConstants.GIT_TF_NAME, null, tempDir);
 
             if (addBaseContent)
             {
                 versionControlService.downloadBaseFile(pendingChange, tempFile.getAbsolutePath());
             }
             else
             {
                 versionControlService.downloadShelvedFile(pendingChange, tempFile.getAbsolutePath());
             }
 
             if (tempFile.exists())
             {
                 tempInputStream = new FileInputStream(tempFile);
                 blobID = repositoryInserter.insert(OBJ_BLOB, tempFile.length(), tempInputStream);
             }
             else
             {
                 blobID = ObjectId.zeroId();
             }
 
             FileMode fileMode;
 
             /* handle executable files */
             if (pendingChange.getPropertyValues() != null
                 && PropertyConstants.EXECUTABLE_ENABLED_VALUE.equals(PropertyUtils.selectMatching(
                     pendingChange.getPropertyValues(),
                     PropertyConstants.EXECUTABLE_KEY)))
             {
                 fileMode = FileMode.EXECUTABLE_FILE;
             }
             else
             {
                 fileMode = FileMode.REGULAR_FILE;
             }
 
             String serverItem =
                 pendingChange.getSourceServerItem() != null && addBaseContent ? pendingChange.getSourceServerItem()
                     : pendingChange.getServerItem();
 
             createBlob(repositoryInserter, treeHierarchy, serverItem, blobID, fileMode, progressMonitor);
         }
         finally
         {
             if (tempInputStream != null)
             {
                 tempInputStream.close();
             }
 
             if (tempFile != null)
             {
                 tempFile.delete();
             }
         }
     }
 
     private ObjectId createCommit(ObjectInserter repositoryInserter, ObjectId rootPendingSetTree, ObjectId parentId)
         throws IOException
     {
         Check.notNull(repositoryInserter, "repositoryInserter"); //$NON-NLS-1$
         Check.notNull(rootPendingSetTree, "rootTree"); //$NON-NLS-1$
 
         return createCommit(
             repositoryInserter,
             rootPendingSetTree,
             parentId,
             getOwnerDisplayName(),
             getOwner(),
             getCommitterDisplayName(),
             getCommitter(),
             getCommitDate(),
             getComment());
     }
 }
