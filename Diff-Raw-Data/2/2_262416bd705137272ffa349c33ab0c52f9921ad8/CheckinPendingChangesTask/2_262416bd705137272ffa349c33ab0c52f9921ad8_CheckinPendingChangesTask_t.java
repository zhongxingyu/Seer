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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.OutputConstants;
 import com.microsoft.gittf.core.config.ChangesetCommitMap;
 import com.microsoft.gittf.core.config.GitTFConfiguration;
 import com.microsoft.gittf.core.interfaces.WorkspaceService;
 import com.microsoft.gittf.core.tasks.framework.Task;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.gittf.core.util.StringUtil;
 import com.microsoft.gittf.core.util.TfsBranchUtil;
 import com.microsoft.tfs.core.clients.build.IBuildDefinition;
 import com.microsoft.tfs.core.clients.build.IBuildRequest;
 import com.microsoft.tfs.core.clients.build.IBuildServer;
 import com.microsoft.tfs.core.clients.build.flags.BuildReason;
 import com.microsoft.tfs.core.clients.versioncontrol.CheckinFlags;
 import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
 import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ActionDeniedBySubscriberException;
 import com.microsoft.tfs.core.clients.versioncontrol.exceptions.CheckinException;
 import com.microsoft.tfs.core.clients.versioncontrol.exceptions.TeamFoundationServerExceptionProperties;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkItemCheckinInfo;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
 import com.microsoft.tfs.core.pendingcheckin.CheckinConflict;
 
 public class CheckinPendingChangesTask
     extends Task
 {
     public static final int CHANGESET_NUMBER_NOT_AS_EXPECTED = 32;
 
     private final Repository repository;
     private final RevCommit commit;
     private final WorkspaceService workspace;
     private final VersionControlClient versionControlClient;
     private final PendingChange[] changes;
     private final String comment;
 
     private WorkItemCheckinInfo[] workItems;
     private boolean overrideGatedCheckin;
     private String buildDefinition = null;
     private int expectedChangesetNumber = -1;
 
     private int changesetID = -1;
 
     public CheckinPendingChangesTask(
         final Repository repository,
         final RevCommit commit,
         final String comment,
         final VersionControlClient versionControlClient,
         final WorkspaceService workspace,
         final PendingChange[] changes)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
         Check.notNull(commit, "commit"); //$NON-NLS-1$
         Check.notNullOrEmpty(comment, "comment"); //$NON-NLS-1$
         Check.notNull(workspace, "workspace"); //$NON-NLS-1$
         Check.notNull(versionControlClient, "versionControlClient"); //$NON-NLS-1$
         Check.isTrue(changes.length > 0, "changes.length > 0"); //$NON-NLS-1$
 
         this.repository = repository;
         this.commit = commit;
         this.comment = comment;
         this.workspace = workspace;
         this.versionControlClient = versionControlClient;
         this.changes = changes;
     }
 
     public int getChangesetID()
     {
         return changesetID;
     }
 
     public void setWorkItemCheckinInfo(WorkItemCheckinInfo[] workItems)
     {
         this.workItems = workItems;
     }
 
     public void setOverrideGatedCheckin(boolean overrideGatedCheckin)
     {
         this.overrideGatedCheckin = overrideGatedCheckin;
     }
 
     public void setBuildDefinition(String buildDefinition)
     {
         this.buildDefinition = buildDefinition;
     }
 
     public void setExpectedChangesetNumber(int expectedChangesetNumber)
     {
         this.expectedChangesetNumber = expectedChangesetNumber;
     }
 
     @Override
     public TaskStatus run(final TaskProgressMonitor progressMonitor)
     {
         progressMonitor.beginTask(Messages.getString("CheckinPendingChangesTask.CheckingIn"), 100); //$NON-NLS-1$
 
         try
         {
             ChangesetCommitMap commitMap = new ChangesetCommitMap(repository);
             GitTFConfiguration configuration = GitTFConfiguration.loadFrom(repository);
 
             if (buildDefinition == null || buildDefinition.length() == 0)
             {
                 buildDefinition = configuration.getBuildDefinition();
             }
 
             CheckinFlags checkinFlags = CheckinFlags.NONE;
 
             if (overrideGatedCheckin)
             {
                 checkinFlags = checkinFlags.combine(CheckinFlags.OVERRIDE_GATED_CHECK_IN);
             }
 
             if (workspace.canCheckIn())
             {
                 changesetID =
                     workspace.checkIn(
                         changes,
                         null,
                         null,
                         comment == null ? commit.getFullMessage() : comment,
                         null,
                         workItems,
                         null,
                         checkinFlags);
 
                 commitMap.setChangesetCommit(changesetID, commit.getId());
 
                 /* Update tfs branch */
                 try
                 {
                     TfsBranchUtil.update(repository, commit);
                 }
                 catch (Exception e)
                 {
                     return new TaskStatus(TaskStatus.ERROR, e);
                 }
             }
 
             if (shouldVerifyChangesetNumber())
             {
                 return verifyChangesetNumber();
             }
         }
         catch (ActionDeniedBySubscriberException e)
         {
             // we can use any affected gated config
             // from MSDN:
             // http://msdn.microsoft.com/en-us/library/microsoft.teamfoundation.versioncontrol.client.checkinparameters.queuebuildforgatedcheckin.aspx
 
             /*
              * If one or more of the items being checked in affects a gated
              * build definition, the check-in will be rejected because it must
              * go through the gated check-in system. The server will create a
              * shelveset of the changes submitted for check-in and throw a
              * GatedCheckinException to the client containing the names of the
              * affected build definitions, the name of the created shelveset,
              * and a check-in ticket string (a cookie).
              * 
              * The client must call IBuildServer.QueueBuild with an
              * IBuildRequest containing the shelveset name, the checkin ticket
              * string, and a reason of BuildReason.CheckInShelveset. The build
              * can be queued against any of the affected definitions
              */
             IBuildServer buildServer = workspace.getBuildServer();
             if (buildServer == null)
             {
                 // no active build server, display message and exit
                 // let user build shelveset manually
                 return new TaskStatus(TaskStatus.ERROR, e);
             }
             TeamFoundationServerExceptionProperties properties = e.getProperties();
             Object[] buildDefUris = properties.getObjectArrayProperty("AffectedBuildDefinitionUris"); //$NON-NLS-1$
             String checkInTicket = properties.getStringProperty("CheckInTicket"); //$NON-NLS-1$
             String shelvesetName = properties.getStringProperty("ShelvesetName"); //$NON-NLS-1$
 
             // delegate error if any of these missing
             if (buildDefUris == null || buildDefUris.length == 0 || checkInTicket == null || shelvesetName == null)
             {
                 return new TaskStatus(TaskStatus.ERROR, e);
             }
 
             IBuildDefinition[] buildDefs =
                 buildServer.queryBuildDefinitionsByURI(StringUtil.convertToStringArray(buildDefUris));
             if (buildDefs == null || buildDefs.length == 0)
             {
                 return new TaskStatus(TaskStatus.ERROR, e);
             }
 
             /*
              * if there are more than one affected builds, then do not queue a
              * build and delegate error the reason being (a) this is how TF.exe
              * /no prompt behave and we would like to keep git-tf and tf.exe as
              * consistent as possible. (b) the first gated definition might not
              * be the correct one, it might pass and code might get checked in
              * that actually breaks the build
              */
             String buildDefinitionToUse = null;
             if (buildDefinition != null && buildDefinition.length() > 0)
             {
                 buildDefinitionToUse = getBuildDefinitionFromList(buildDefinition, buildDefs);
 
                 if (buildDefinitionToUse == null || buildDefinitionToUse.length() == 0)
                 {
                     return new TaskStatus(TaskStatus.ERROR, Messages.formatString(
                         "CheckinPendingChangesTask.InvalidGatedDefinitionSpecifiedFormat", //$NON-NLS-1$
                         buildDefinition) + OutputConstants.NEW_LINE + e.getLocalizedMessage());
                 }
             }
             else if (buildDefUris.length == 1)
             {
                 buildDefinitionToUse = buildDefUris[0].toString();
             }
 
             if (buildDefinitionToUse == null || buildDefinitionToUse.length() == 0)
             {
                 return new TaskStatus(TaskStatus.ERROR, e);
             }
 
             IBuildRequest buildRequest = buildServer.createBuildRequest(buildDefinitionToUse);
             buildRequest.setGatedCheckInTicket(checkInTicket);
             buildRequest.setShelvesetName(shelvesetName);
             buildRequest.setReason(BuildReason.CHECK_IN_SHELVESET);
             try
             {
                 buildServer.queueBuild(buildRequest);
                 return new TaskStatus(TaskStatus.ERROR, Messages.formatString(
                     "CheckinPendingChangesTask.GatedBuildQueuedFormat", //$NON-NLS-1$
                     shelvesetName));
             }
             catch (Exception ex)
             {
                 return new TaskStatus(TaskStatus.ERROR, ex);
             }
         }
         catch (CheckinException e)
         {
             if (e.allConflictsResolved() || e.isAnyResolvable())
             {
                 return new TaskStatus(
                     TaskStatus.ERROR,
                     Messages.getString("CheckinPendingChangesTask.OtherUserCheckinDetected")); //$NON-NLS-1$
             }
            else if (e.getCheckinConflicts() != null && e.getCheckinConflicts().length > 0)
             {
                 return new TaskStatus(TaskStatus.ERROR, buildErrorMessage(e.getCheckinConflicts()));
             }
 
             return new TaskStatus(TaskStatus.ERROR, e);
         }
         catch (Exception e)
         {
             return new TaskStatus(TaskStatus.ERROR, e);
         }
         finally
         {
             progressMonitor.endTask();
             progressMonitor.dispose();
         }
 
         return TaskStatus.OK_STATUS;
     }
 
     private String buildErrorMessage(CheckinConflict[] checkinConflicts)
     {
         StringBuilder sb = new StringBuilder();
 
         for (int count = 0; count < checkinConflicts.length; count++)
         {
             sb.append(checkinConflicts[count].getMessage());
 
             if (count != (checkinConflicts.length - 1))
             {
                 sb.append(OutputConstants.NEW_LINE);
             }
         }
 
         return sb.toString();
     }
 
     private String getBuildDefinitionFromList(String buildDefinitionToUse, IBuildDefinition[] buildDefs)
     {
         Check.notNull(buildDefinitionToUse, "buildDefinitionToUse"); //$NON-NLS-1$
         Check.notNullOrEmpty(buildDefs, "buildDefUris"); //$NON-NLS-1$
 
         List<String> possibleBuildDefinitions = new ArrayList<String>();
 
         for (IBuildDefinition buildDef : buildDefs)
         {
             if (buildDef.getName().equalsIgnoreCase(buildDefinitionToUse))
             {
                 return buildDef.getURI();
             }
 
             if (buildDef.getName().toLowerCase().contains(buildDefinitionToUse.toLowerCase()))
             {
                 possibleBuildDefinitions.add(buildDef.getURI());
             }
         }
 
         if (possibleBuildDefinitions.size() == 1)
         {
             return possibleBuildDefinitions.get(0);
         }
 
         return null;
     }
 
     private boolean shouldVerifyChangesetNumber()
     {
         return expectedChangesetNumber > 0;
     }
 
     private TaskStatus verifyChangesetNumber()
     {
         if (changesetID == expectedChangesetNumber)
         {
             return TaskStatus.OK_STATUS;
         }
 
         if (anyChangesetModifiesMappedPath(expectedChangesetNumber, changesetID))
         {
             return new TaskStatus(TaskStatus.OK, CHANGESET_NUMBER_NOT_AS_EXPECTED);
         }
 
         return TaskStatus.OK_STATUS;
     }
 
     private boolean anyChangesetModifiesMappedPath(int start, int end)
     {
         GitTFConfiguration configuration = GitTFConfiguration.loadFrom(repository);
 
         Changeset[] sneakedInChangesets =
             versionControlClient.queryHistory(
                 configuration.getServerPath(),
                 LatestVersionSpec.INSTANCE,
                 0,
                 RecursionType.FULL,
                 null,
                 new ChangesetVersionSpec(start),
                 new ChangesetVersionSpec(end),
                 Integer.MAX_VALUE,
                 false,
                 false,
                 false,
                 false);
 
         for (Changeset changeset : sneakedInChangesets)
         {
             // if this is before the range we are interested in - ignore
             if (changeset.getChangesetID() < start)
             {
                 break;
             }
 
             // if this is between start and end - then yes the changes do affect
             // the mapping
             if (changeset.getChangesetID() < end)
             {
                 return true;
             }
         }
 
         // otherwise we are good
         return false;
     }
 }
