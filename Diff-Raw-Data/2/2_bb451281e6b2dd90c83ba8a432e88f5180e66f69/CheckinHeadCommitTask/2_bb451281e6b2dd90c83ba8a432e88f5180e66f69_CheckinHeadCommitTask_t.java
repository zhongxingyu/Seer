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
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.LogCommand;
 import org.eclipse.jgit.lib.AbbreviatedObjectId;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 
 import com.microsoft.gittf.core.GitTFConstants;
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.OutputConstants;
 import com.microsoft.gittf.core.config.ChangesetCommitMap;
 import com.microsoft.gittf.core.config.GitTFConfiguration;
 import com.microsoft.gittf.core.interfaces.WorkspaceService;
 import com.microsoft.gittf.core.tasks.framework.NullTaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskExecutor;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressDisplay;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.tasks.pendDiff.PendDifferenceTask;
 import com.microsoft.gittf.core.tasks.pendDiff.RenameMode;
 import com.microsoft.gittf.core.util.ChangesetCommitUtil;
 import com.microsoft.gittf.core.util.ChangesetCommitUtil.ChangesetCommitDetails;
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.gittf.core.util.CommitUtil;
 import com.microsoft.gittf.core.util.CommitWalker;
 import com.microsoft.gittf.core.util.CommitWalker.CommitDelta;
 import com.microsoft.gittf.core.util.DateUtil;
 import com.microsoft.gittf.core.util.RepositoryUtil;
 import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
 import com.microsoft.tfs.core.clients.versioncontrol.path.ServerPath;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.DeletedState;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkItemCheckinInfo;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
 import com.microsoft.tfs.util.FileHelpers;
 
 public class CheckinHeadCommitTask
     extends WorkspaceTask
 {
     public static final int ALREADY_UP_TO_DATE = 1;
 
     private static final Log log = LogFactory.getLog(CheckinHeadCommitTask.class);
 
     private final Repository repository;
     private final String serverPath;
 
     private boolean deep = false;
 
     private AbbreviatedObjectId[] squashCommitIDs = new AbbreviatedObjectId[0];
     private WorkItemCheckinInfo[] workItems;
     private boolean lock = true;
     private boolean overrideGatedCheckin;
     private boolean autoSquashMultipleParents;
     private boolean preview = false;
     private String comment = null;
     private String buildDefinition = null;
     private boolean includeMetaData = true;
     private RenameMode renameMode = RenameMode.ALL;
 
     public CheckinHeadCommitTask(
         final Repository repository,
         final VersionControlClient versionControlClient,
         final String serverPath)
     {
         super(repository, versionControlClient, serverPath);
 
         Check.notNull(repository, "repository"); //$NON-NLS-1$
         Check.notNullOrEmpty(serverPath, "serverPath"); //$NON-NLS-1$
 
         this.repository = repository;
         this.serverPath = serverPath;
     }
 
     public boolean getDeep()
     {
         return deep;
     }
 
     public void setDeep(final boolean deep)
     {
         this.deep = deep;
     }
 
     public AbbreviatedObjectId[] getSquashCommitIDs()
     {
         return squashCommitIDs;
     }
 
     public void setSquashCommitIDs(AbbreviatedObjectId[] squashCommitIDs)
     {
         this.squashCommitIDs = (squashCommitIDs == null) ? new AbbreviatedObjectId[0] : squashCommitIDs;
     }
 
     public WorkItemCheckinInfo[] getWorkItemCheckinInfo()
     {
         return workItems;
     }
 
     public void setWorkItemCheckinInfo(WorkItemCheckinInfo[] workItems)
     {
         this.workItems = workItems;
     }
 
     public boolean getOverrideGatedCheckin()
     {
         return this.overrideGatedCheckin;
     }
 
     public void setOverrideGatedCheckin(boolean overrideGatedCheckin)
     {
         this.overrideGatedCheckin = overrideGatedCheckin;
     }
 
     public boolean getLock()
     {
         return this.lock;
     }
 
     public void setLock(boolean lock)
     {
         this.lock = lock;
     }
 
     public boolean getAutoSquash()
     {
         return this.autoSquashMultipleParents;
     }
 
     public void setAutoSquash(boolean autoSquashMultipleParents)
     {
         this.autoSquashMultipleParents = autoSquashMultipleParents;
     }
 
     public boolean getPreview()
     {
         return preview;
     }
 
     public void setPreview(boolean preview)
     {
         this.preview = preview;
     }
 
     public String getComment()
     {
         return comment;
     }
 
     public void setComment(String comment)
     {
         this.comment = comment;
     }
 
     public String getBuildDefinition()
     {
         return buildDefinition;
     }
 
     public void setBuildDefinition(String buildDefinition)
     {
         this.buildDefinition = buildDefinition;
     }
 
     public boolean getIncludeMetaData()
     {
         return includeMetaData;
     }
 
     public void setIncludeMetaData(boolean includeMetaData)
     {
         this.includeMetaData = includeMetaData;
     }
 
     public void setRenameMode(RenameMode renameMode)
     {
         this.renameMode = renameMode;
     }
 
     @Override
     public TaskStatus run(final TaskProgressMonitor progressMonitor)
     {
         GitTFWorkspaceData workspaceData = null;
 
         progressMonitor.beginTask(
             Messages.formatString("CheckinHeadCommitTask.CheckingInToPathFormat", //$NON-NLS-1$
                 preview ? Messages.getString("CheckinHeadCommitTask.Preview") : "", //$NON-NLS-1$ //$NON-NLS-2$
                 GitTFConfiguration.loadFrom(repository).getServerPath()),
             1,
             TaskProgressDisplay.DISPLAY_PROGRESS.combine(TaskProgressDisplay.DISPLAY_SUBTASK_DETAIL));
 
         try
         {
             /*
              * Create a temporary workspace
              */
 
             WorkspaceService workspace = null;
             File workingFolder = null;
 
             workspaceData = createWorkspace(progressMonitor.newSubTask(1), preview);
 
             workspace = workspaceData.getWorkspace();
             workingFolder = workspaceData.getWorkingFolder();
 
             int expectedChangesetNumber = -1;
             boolean otherUserCheckinDetected = false;
 
             /* in deep mode we should always lock the workspace */
             if (lock && deep)
             {
                 final TaskStatus lockStatus =
                     new TaskExecutor(progressMonitor.newSubTask(1)).execute(new LockTask(workspace, serverPath));
 
                 if (!lockStatus.isOK())
                 {
                     return lockStatus;
                 }
             }
             /*
              * if we are not locking we should attempt to detect other users
              * checkin in shallow mode
              */
             else if (!deep)
             {
                 Changeset[] latestChangesets =
                     versionControlClient.queryHistory(
                        ServerPath.ROOT,
                         LatestVersionSpec.INSTANCE,
                         0,
                         RecursionType.FULL,
                         null,
                         null,
                         null,
                         1,
                         false,
                         false,
                         false,
                         false);
 
                 Check.notNull(latestChangesets, "latestChangesets"); //$NON-NLS-1$
 
                 expectedChangesetNumber = latestChangesets[0].getChangesetID() + 1;
             }
 
             /*
              * Walk the repo
              */
             ObjectId headCommitID = RepositoryUtil.getMasterHeadCommitID(repository);
 
             final ChangesetCommitMap commitMap = new ChangesetCommitMap(repository);
 
             final ChangesetCommitDetails lastBridgedChangeset = ChangesetCommitUtil.getLastBridgedChangeset(commitMap);
             final ChangesetCommitDetails latestChangeset =
                 ChangesetCommitUtil.getLatestChangeset(commitMap, versionControlClient, serverPath);
 
             /*
              * This is a repository that has been configured and never checked
              * in to tfs before. We need to validate that the path in tfs either
              * does not exist or is empty
              */
             if (lastBridgedChangeset == null || lastBridgedChangeset.getChangesetID() < 0)
             {
                 Item[] items =
                     versionControlClient.getItems(
                         serverPath,
                         LatestVersionSpec.INSTANCE,
                         RecursionType.ONE_LEVEL,
                         DeletedState.NON_DELETED,
                         ItemType.ANY).getItems();
 
                 if (items != null && items.length > 0)
                 {
                     /* The folder can exist but has to be empty */
                     if (!(items.length == 1 && ServerPath.equals(items[0].getServerItem(), serverPath)))
                     {
                         return new TaskStatus(TaskStatus.ERROR, Messages.formatString(
                             "CheckinHeadCommitTask.CannotCheckinToANonEmptyFolderFormat", serverPath)); //$NON-NLS-1$
                     }
                 }
             }
 
             /*
              * There is a changeset for this path on the server, but it does not
              * have a corresponding commit in the map. The user must merge with
              * the latest changeset.
              */
             else if (latestChangeset != null && latestChangeset.getCommitID() == null)
             {
                 return new TaskStatus(TaskStatus.ERROR, Messages.formatString(
                     "CheckinHeadCommitTask.NotFastForwardFormat", //$NON-NLS-1$
                     Integer.toString(latestChangeset.getChangesetID())));
             }
 
             /*
              * The server path does not exist, but we have previously downloaded
              * some items from it, thus it has been deleted. We cannot proceed.
              */
             else if (latestChangeset == null && lastBridgedChangeset != null)
             {
                 return new TaskStatus(TaskStatus.ERROR, Messages.formatString(
                     "CheckinHeadCommitTask.ServerPathDoesNotExistFormat", //$NON-NLS-1$
                     serverPath));
             }
 
             /*
              * The current HEAD is the latest changeset on the TFS server.
              * Nothing to do.
              */
             else if (latestChangeset != null && latestChangeset.getCommitID().equals(headCommitID))
             {
                 return new TaskStatus(TaskStatus.OK, CheckinHeadCommitTask.ALREADY_UP_TO_DATE);
             }
 
             progressMonitor.setDetail(Messages.getString("CheckinHeadCommitTask.ExaminingRepository")); //$NON-NLS-1$
             List<CommitDelta> commitsToCheckin =
                 getCommitsToCheckin(latestChangeset != null ? latestChangeset.getCommitID() : null, headCommitID);
             progressMonitor.setDetail(null);
 
             int lastChangesetID = -1;
             ObjectId lastCommitID = null;
 
             boolean anyThingCheckedIn = false;
 
             progressMonitor.setWork(commitsToCheckin.size() * 2);
 
             for (int i = 0; i < commitsToCheckin.size(); i++)
             {
                 CommitDelta commitDelta = commitsToCheckin.get(i);
                 boolean isLastCommit = (i == (commitsToCheckin.size() - 1));
 
                 progressMonitor.setDetail(Messages.formatString("CheckinHeadCommitTask.CommitFormat", //$NON-NLS-1$
                     CommitUtil.abbreviate(repository, commitDelta.getToCommit())));
 
                 /* Save space: clean working folder after each checkin */
                 if (i > 0)
                 {
                     cleanWorkingFolder(workingFolder);
                 }
 
                 final PendDifferenceTask pendTask =
                     new PendDifferenceTask(
                         repository,
                         commitDelta.getFromCommit(),
                         commitDelta.getToCommit(),
                         workspace,
                         serverPath,
                         workingFolder);
 
                 pendTask.setRenameMode(renameMode);
 
                 pendTask.validate();
 
                 if (preview)
                 {
                     if (i == 0)
                     {
                         progressMonitor.displayMessage(Messages.getString("CheckinHeadCommitTask.CheckedInPreview")); //$NON-NLS-1$
                         progressMonitor.displayMessage(""); //$NON-NLS-1$
                     }
 
                     ObjectId fromCommit = commitDelta.getFromCommit();
                     ObjectId toCommit = commitDelta.getToCommit();
 
                     if (fromCommit == null || fromCommit == ObjectId.zeroId() || commitsToCheckin.size() != 1)
                     {
                         progressMonitor.displayMessage(Messages.formatString(
                             "CheckinHeadCommitTask.CheckedInPreviewSingleCommitFormat", //$NON-NLS-1$
                             i + 1,
                             CommitUtil.abbreviate(repository, toCommit)));
                     }
                     else
                     {
                         progressMonitor.displayMessage(Messages.formatString(
                             "CheckinHeadCommitTask.CheckedInPreviewDifferenceCommitsFormat", //$NON-NLS-1$
                             i + 1,
                             CommitUtil.abbreviate(repository, toCommit),
                             CommitUtil.abbreviate(repository, fromCommit)));
                     }
 
                     String checkinComment = comment == null ? buildCommitComment(commitDelta) : comment;
 
                     progressMonitor.displayMessage(""); //$NON-NLS-1$
                     progressMonitor.displayMessage(getCommentDisplayString(checkinComment));
 
                     progressMonitor.displayMessage(Messages.getString("CheckinHeadCommitTask.CheckedInPreviewTableHeader")); //$NON-NLS-1$
                     progressMonitor.displayMessage("---------------------------------------------------------------------"); //$NON-NLS-1$
                 }
 
                 final TaskStatus pendStatus = new TaskExecutor(progressMonitor.newSubTask(1)).execute(pendTask);
 
                 if (!pendStatus.isOK())
                 {
                     return pendStatus;
                 }
 
                 if (pendStatus.getCode() == PendDifferenceTask.NOTHING_TO_PEND)
                 {
                     continue;
                 }
 
                 anyThingCheckedIn = true;
 
                 if (!preview)
                 {
                     final CheckinPendingChangesTask checkinTask =
                         new CheckinPendingChangesTask(
                             repository,
                             commitDelta.getToCommit(),
                             comment == null ? buildCommitComment(commitDelta) : comment,
                             versionControlClient,
                             workspace,
                             pendTask.getPendingChanges());
 
                     if (isLastCommit)
                     {
                         checkinTask.setWorkItemCheckinInfo(workItems);
                     }
 
                     checkinTask.setOverrideGatedCheckin(overrideGatedCheckin);
                     checkinTask.setBuildDefinition(buildDefinition);
                     checkinTask.setExpectedChangesetNumber(expectedChangesetNumber);
 
                     progressMonitor.setDetail(Messages.getString("CheckinHeadCommitTask.CheckingIn")); //$NON-NLS-1$
 
                     final TaskStatus checkinStatus =
                         new TaskExecutor(progressMonitor.newSubTask(1)).execute(checkinTask);
 
                     if (!checkinStatus.isOK())
                     {
                         return checkinStatus;
                     }
 
                     lastChangesetID = checkinTask.getChangesetID();
                     lastCommitID = commitDelta.getToCommit();
                     otherUserCheckinDetected =
                         checkinStatus.getCode() == CheckinPendingChangesTask.CHANGESET_NUMBER_NOT_AS_EXPECTED;
 
                     expectedChangesetNumber = -1;
 
                     progressMonitor.displayVerbose(Messages.formatString(
                         "CheckinHeadCommitTask.CheckedInChangesetFormat", //$NON-NLS-1$
                         CommitUtil.abbreviate(repository, lastCommitID),
                         Integer.toString(checkinTask.getChangesetID())));
                 }
                 else
                 {
                     progressMonitor.displayMessage("---------------------------------------------------------------------"); //$NON-NLS-1$
                     progressMonitor.displayMessage(""); //$NON-NLS-1$
                 }
             }
 
             final TaskProgressMonitor cleanupMonitor = progressMonitor.newSubTask(1);
             cleanupWorkspace(cleanupMonitor, workspaceData);
             workspaceData = null;
 
             progressMonitor.endTask();
 
             if (!preview)
             {
                 // There was nothing detected to checkin.
                 if (!anyThingCheckedIn)
                 {
                     return new TaskStatus(TaskStatus.OK, CheckinHeadCommitTask.ALREADY_UP_TO_DATE);
                 }
 
                 if (commitsToCheckin.size() == 1)
                 {
                     progressMonitor.displayMessage(Messages.formatString(
                         "CheckinHeadCommitTask.CheckedInFormat", CommitUtil.abbreviate(repository, lastCommitID), Integer.toString(lastChangesetID))); //$NON-NLS-1$
                 }
                 else
                 {
                     progressMonitor.displayMessage(Messages.formatString(
                         "CheckinHeadCommitTask.CheckedInMultipleFormat", Integer.toString(commitsToCheckin.size()), Integer.toString(lastChangesetID))); //$NON-NLS-1$                
                 }
 
                 if (otherUserCheckinDetected)
                 {
                     progressMonitor.displayWarning(Messages.getString("CheckinHeadCommitTask.OtherUserCheckinDetected")); //$NON-NLS-1$
                 }
             }
 
             return TaskStatus.OK_STATUS;
         }
         catch (Exception e)
         {
             return new TaskStatus(TaskStatus.ERROR, e);
         }
         finally
         {
             if (workspaceData != null)
             {
                 cleanupWorkspace(new NullTaskProgressMonitor(), workspaceData);
             }
         }
     }
 
     private String buildCommitComment(CommitDelta commitDelta)
     {
         if (deep)
         {
             if (includeMetaData)
             {
                 return buildCommitComment(commitDelta.getToCommit());
             }
             else
             {
                 return commitDelta.getToCommit().getFullMessage();
             }
         }
 
         try
         {
             LogCommand logCommand = new Git(repository).log();
             logCommand.addRange(commitDelta.getFromCommit().getId(), commitDelta.getToCommit().getId());
             logCommand.setMaxCount(OutputConstants.DEFAULT_MAXCOMMENTROLLUP + 1);
             Iterable<RevCommit> commits = logCommand.call();
 
             int commitCounter = 0;
 
             StringBuilder comment = new StringBuilder();
 
             comment.append(Messages.formatString("CheckinHeadCommitTask.ShallowCheckinRollupFormat", //$NON-NLS-1$
                 CommitUtil.abbreviate(repository, commitDelta.getToCommit().getId()),
                 CommitUtil.abbreviate(repository, commitDelta.getFromCommit().getId())) + OutputConstants.NEW_LINE);
             comment.append(OutputConstants.NEW_LINE);
 
             for (RevCommit commit : commits)
             {
                 commitCounter++;
 
                 if (commitCounter > OutputConstants.DEFAULT_MAXCOMMENTROLLUP)
                 {
                     comment.append(Messages.formatString(
                         "CheckinHeadCommitTask.ShallowCheckinCommentDisplayTruncatedFormat", //$NON-NLS-1$
                         OutputConstants.DEFAULT_MAXCOMMENTROLLUP,
                         CommitUtil.abbreviate(repository, commit.getId()),
                         CommitUtil.abbreviate(repository, commitDelta.getFromCommit().getId())));
 
                     break;
                 }
 
                 comment.append(buildCommitComment(commit));
             }
 
             if (commitCounter == 1)
             {
                 return buildCommitComment(commitDelta.getToCommit());
             }
 
             return comment.toString();
         }
         catch (Exception e)
         {
             // if we fail execute the log command we default to the destination
             // commit full message
 
             return buildCommitComment(commitDelta.getToCommit());
         }
     }
 
     private String buildCommitComment(RevCommit commit)
     {
         StringBuilder comment = new StringBuilder();
 
         comment.append(Messages.formatString("CheckinHeadCommitTask.ShallowCheckinCommentFormat", //$NON-NLS-1$
             CommitUtil.abbreviate(repository, commit.getId()),
             DateUtil.formatDate(new Date(((long) commit.getCommitTime()) * 1000))) + OutputConstants.NEW_LINE);
         comment.append(Messages.formatString("CheckinHeadCommitTask.ShallowCheckinCommentAuthorFormat", //$NON-NLS-1$
             commit.getAuthorIdent().getName(),
             commit.getAuthorIdent().getEmailAddress()) + OutputConstants.NEW_LINE);
         comment.append(Messages.formatString("CheckinHeadCommitTask.ShallowCheckinCommentCommitterFormat", //$NON-NLS-1$
             commit.getCommitterIdent().getName(),
             commit.getCommitterIdent().getEmailAddress()) + OutputConstants.NEW_LINE);
         comment.append("-----------------------------------------------------------------" + OutputConstants.NEW_LINE); //$NON-NLS-1$
         comment.append(getCommentDisplayString(commit.getFullMessage()));
         comment.append(OutputConstants.NEW_LINE);
 
         return comment.toString();
     }
 
     private String getCommentDisplayString(String checkinComment)
     {
         String[] lines = checkinComment.split(OutputConstants.NEW_LINE);
 
         StringBuilder sb = new StringBuilder();
         for (String line : lines)
         {
             sb.append(MessageFormat.format("    {0}{1}", line, OutputConstants.NEW_LINE)); //$NON-NLS-1$
         }
 
         return sb.toString();
     }
 
     private final void cleanWorkingFolder(final File workingFolder)
     {
         try
         {
             FileHelpers.deleteDirectory(workingFolder);
             workingFolder.mkdirs();
         }
         catch (Exception e)
         {
             /* Not fatal */
             log.warn(MessageFormat.format("Could not clean up temporary directory {0}", //$NON-NLS-1$
                 workingFolder.getAbsolutePath()), e);
         }
     }
 
     private final List<CommitDelta> getCommitsToCheckin(
         final ObjectId latestChangesetCommitID,
         final ObjectId headCommitID)
         throws Exception
     {
         Check.notNull(headCommitID, "headCommitID"); //$NON-NLS-1$
 
         List<CommitDelta> commitsToCheckin;
 
         /*
          * In the case of shallow commit, we do not care if the user provided
          * ids to squash or not since we are not preserving history anyways we
          * select any path we find and that would be ok
          */
         if (autoSquashMultipleParents || !deep)
         {
             commitsToCheckin =
                 CommitWalker.getAutoSquashedCommitList(repository, latestChangesetCommitID, headCommitID);
         }
         else
         {
             commitsToCheckin =
                 CommitWalker.getCommitList(repository, latestChangesetCommitID, headCommitID, squashCommitIDs);
         }
 
         int depth = deep ? Integer.MAX_VALUE : GitTFConstants.GIT_TF_SHALLOW_DEPTH;
 
         /* Prune the list of commits down to their depth. */
         if (commitsToCheckin.size() > depth)
         {
             List<CommitDelta> prunedCommits = new ArrayList<CommitDelta>();
 
             RevCommit lastToCommit = null;
             RevCommit lastFromCommit = null;
 
             for (int i = 0; i < depth - 1; i++)
             {
                 CommitDelta delta = commitsToCheckin.get(commitsToCheckin.size() - 1 - i);
 
                 prunedCommits.add(delta);
 
                 lastToCommit = delta.getFromCommit();
             }
 
             lastFromCommit = commitsToCheckin.get(0).getFromCommit();
 
             if (lastToCommit == null)
             {
                 lastToCommit = commitsToCheckin.get(commitsToCheckin.size() - 1).getToCommit();
             }
 
             Check.notNull(lastToCommit, "lastToCommit"); //$NON-NLS-1$
 
             prunedCommits.add(new CommitDelta(lastFromCommit, lastToCommit));
 
             commitsToCheckin = prunedCommits;
         }
 
         return commitsToCheckin;
     }
 
     private void cleanupWorkspace(final TaskProgressMonitor progressMonitor, final GitTFWorkspaceData workspaceData)
     {
         if (workspaceData == null)
         {
             return;
         }
 
         final WorkspaceService workspace = workspaceData.getWorkspace();
 
         progressMonitor.beginTask(Messages.getString("CheckinHeadCommitTask.DeletingWorkspace"), //$NON-NLS-1$
             TaskProgressMonitor.INDETERMINATE,
             TaskProgressDisplay.DISPLAY_PROGRESS);
 
         if (workspaceData.getWorkspace() != null && lock && deep)
         {
             final TaskStatus unlockStatus =
                 new TaskExecutor(progressMonitor.newSubTask(1)).execute(new UnlockTask(workspace, serverPath));
 
             if (!unlockStatus.isOK())
             {
                 log.warn(MessageFormat.format("Could not unlock {0}: {1}", serverPath, unlockStatus.getMessage())); //$NON-NLS-1$                
             }
         }
 
         disposeWorkspace(progressMonitor.newSubTask(1));
 
         progressMonitor.endTask();
     }
 }
