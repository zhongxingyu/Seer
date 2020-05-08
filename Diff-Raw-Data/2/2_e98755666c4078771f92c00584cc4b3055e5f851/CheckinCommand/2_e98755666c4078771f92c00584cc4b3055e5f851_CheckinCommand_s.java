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
 
 package com.microsoft.gittf.client.clc.commands;
 
 import java.util.Collection;
 
 import org.eclipse.jgit.lib.AbbreviatedObjectId;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 
 import com.microsoft.gittf.client.clc.Console.Verbosity;
 import com.microsoft.gittf.client.clc.ExitCode;
 import com.microsoft.gittf.client.clc.Main;
 import com.microsoft.gittf.client.clc.Messages;
 import com.microsoft.gittf.client.clc.arguments.Argument;
 import com.microsoft.gittf.client.clc.arguments.ArgumentOptions;
 import com.microsoft.gittf.client.clc.arguments.ChoiceArgument;
 import com.microsoft.gittf.client.clc.arguments.SwitchArgument;
 import com.microsoft.gittf.client.clc.arguments.ValueArgument;
 import com.microsoft.gittf.client.clc.commands.framework.CommandTaskExecutor;
 import com.microsoft.gittf.client.clc.commands.framework.ConsoleOutputTaskHandler;
 import com.microsoft.gittf.core.config.GitTFConfiguration;
 import com.microsoft.gittf.core.tasks.CheckinHeadCommitTask;
 import com.microsoft.gittf.core.tasks.framework.Task;
 import com.microsoft.gittf.core.tasks.framework.TaskCompletedHandler;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ActionDeniedBySubscriberException;
 
 public class CheckinCommand
     extends PendingChangesCommand
 {
     public static final String COMMAND_NAME = "checkin"; //$NON-NLS-1$
 
     private static final CheckinTaskCompletedHandler checkinTaskCompletedHandler = new CheckinTaskCompletedHandler();
 
     private static Argument[] ARGUMENTS = new Argument[]
     {
         new SwitchArgument("help", Messages.getString("Command.Argument.Help.HelpText")), //$NON-NLS-1$ //$NON-NLS-2$
 
         new ChoiceArgument(Messages.getString("Command.Argument.Display.HelpText"), //$NON-NLS-1$
             new SwitchArgument("quiet", //$NON-NLS-1$
                 'q',
                 Messages.getString("Command.Argument.Quiet.HelpText")), //$NON-NLS-1$
 
             new SwitchArgument("verbose", //$NON-NLS-1$
                 Messages.getString("Command.Argument.Verbose.HelpText")) //$NON-NLS-1$
         ),
 
         new ValueArgument("message", //$NON-NLS-1$
             'm',
             Messages.getString("CheckinCommand.Argument.Message.ValueDescription"), //$NON-NLS-1$
             Messages.getString("CheckinCommand.Argument.Message.HelpText"), //$NON-NLS-1$
             ArgumentOptions.VALUE_REQUIRED),
 
         new ChoiceArgument(Messages.getString("CheckinCommand.Argument.DepthChoice.HelpText"), //$NON-NLS-1$
             /* Users can specify one of --deep, --depth or --shallow. */
             new SwitchArgument("deep", //$NON-NLS-1$
                 Messages.getString("CheckinCommand.Argument.Deep.HelpText")), //$NON-NLS-1$
 
             new SwitchArgument("shallow", //$NON-NLS-1$
                 Messages.getString("CheckinCommand.Argument.Shallow.HelpText")) //$NON-NLS-1$
         ),
 
         new ChoiceArgument(Messages.getString("CheckinCommand.Argument.SquashAutoSquash.HelpText"), //$NON-NLS-1$
             /*
              * User can specify one of --squash:[commit id],[commit id] or
              * --autosquash
              */
             new ValueArgument("squash", //$NON-NLS-1$
                 Messages.getString("CheckinCommand.Argument.Squash.ValueDescription"), //$NON-NLS-1$
                 Messages.getString("CheckinCommand.Argument.Squash.HelpText"), //$NON-NLS-1$
                 ArgumentOptions.VALUE_REQUIRED.combine(ArgumentOptions.MULTIPLE)),
 
             new SwitchArgument("autosquash", //$NON-NLS-1$
                 Messages.getString("CheckinCommand.Argument.AutoSquash.HelpText")) //$NON-NLS-1$            
         ),
 
         new ValueArgument("resolve", //$NON-NLS-1$
             Messages.getString("PendingChangesCommand.Argument.Resolve.ValueDescription"), //$NON-NLS-1$
             Messages.getString("PendingChangesCommand.Argument.Resolve.HelpText"), //$NON-NLS-1$
             ArgumentOptions.VALUE_REQUIRED.combine(ArgumentOptions.MULTIPLE)),
 
         new ValueArgument("associate", //$NON-NLS-1$
             Messages.getString("PendingChangesCommand.Argument.Associate.ValueDescription"), //$NON-NLS-1$
             Messages.getString("PendingChangesCommand.Argument.Associate.HelpText"), //$NON-NLS-1$
             ArgumentOptions.VALUE_REQUIRED.combine(ArgumentOptions.MULTIPLE)),
 
         new SwitchArgument("no-lock", Messages.getString("CheckinCommand.Argument.NoLock.HelpText")), //$NON-NLS-1$ //$NON-NLS-2$
 
         new SwitchArgument("preview", 'p', Messages.getString("CheckinCommand.Argument.Preview.HelpText")), //$NON-NLS-1$ //$NON-NLS-2$
 
         new SwitchArgument("bypass", //$NON-NLS-1$
             Messages.getString("CheckinCommand.Argument.Bypass.HelpText")), //$NON-NLS-1$
 
     };
 
     @Override
     protected String getCommandName()
     {
         return COMMAND_NAME;
     }
 
     @Override
     public Argument[] getPossibleArguments()
     {
         return ARGUMENTS;
     }
 
     @Override
     public String getHelpDescription()
     {
         return Messages.getString("CheckinCommand.HelpDescription"); //$NON-NLS-1$
     }
 
     @Override
     public int run()
         throws Exception
     {
         verifyGitTfConfigured();
         verifyRepoSafeState();
 
         boolean deep = GitTFConfiguration.loadFrom(getRepository()).getDeep();
         deep = isDepthSpecified() ? getDeepFromArguments() : deep;
 
         if (getArguments().contains("squash") && !getArguments().contains("deep")) //$NON-NLS-1$ //$NON-NLS-2$
         {
             throw new Exception(Messages.getString("CheckinCommand.SquashOnlyAvailableWithDeep")); //$NON-NLS-1$
         }
 
         final boolean noLock = getArguments().contains("no-lock"); //$NON-NLS-1$
         final boolean preview = getArguments().contains("preview"); //$NON-NLS-1$
         final boolean overrideGatedCheckin = getArguments().contains("bypass"); //$NON-NLS-1$
         final boolean autoSquashMultipleParents = getArguments().contains("autosquash"); //$NON-NLS-1$
 
         String message = getArguments().contains("message") ? //$NON-NLS-1$
             ((ValueArgument) getArguments().getArgument("message")).getValue() : null; //$NON-NLS-1$
 
         if (deep && message != null)
         {
             Main.printWarning(Messages.getString("CheckinCommand.MessageWillBeIgnoreBecauseDeepSpecified")); //$NON-NLS-1$
         }
 
         final CheckinHeadCommitTask checkinTask =
             new CheckinHeadCommitTask(
                 getRepository(),
                 getVersionControlClient(),
                 getServerConfiguration().getServerPath());
 
         checkinTask.setWorkItemCheckinInfo(getWorkItemCheckinInfo());
         checkinTask.setDeep(deep);
         checkinTask.setLock(!noLock);
         checkinTask.setPreview(preview);
         checkinTask.setOverrideGatedCheckin(overrideGatedCheckin);
         checkinTask.setSquashCommitIDs(getSquashCommitIDs());
         checkinTask.setAutoSquash(autoSquashMultipleParents);
         checkinTask.setComment(message);
 
         /*
          * Hook up a custom task executor that does not print gated errors to
          * standard error (we handle those specially.)
          */
         final CommandTaskExecutor taskExecutor = new CommandTaskExecutor(getProgressMonitor());
         taskExecutor.removeTaskCompletedHandler(CommandTaskExecutor.CONSOLE_OUTPUT_TASK_HANDLER);
         taskExecutor.addTaskCompletedHandler(checkinTaskCompletedHandler);
 
         final TaskStatus checkinStatus = taskExecutor.execute(checkinTask);
 
         if (checkinStatus.isOK() && checkinStatus.getCode() == CheckinHeadCommitTask.ALREADY_UP_TO_DATE)
         {
             getConsole().getOutputStream(Verbosity.NORMAL).println(Messages.getString("CheckinCommand.AlreadyUpToDate")); //$NON-NLS-1$
         }
 
         return checkinStatus.isOK() ? ExitCode.SUCCESS : ExitCode.FAILURE;
     }
 
     private AbbreviatedObjectId[] getSquashCommitIDs()
         throws Exception
     {
         Repository repository = getRepository();
 
         ObjectReader objReader = null;
         RevWalk revWalk = null;
         try
         {
             objReader = repository.newObjectReader();
             revWalk = new RevWalk(repository);
 
             Argument[] squashPrefixArgs = getArguments().getArguments("squash"); //$NON-NLS-1$
 
             if (squashPrefixArgs == null || squashPrefixArgs.length == 0)
             {
                 return null;
             }
 
             AbbreviatedObjectId[] squashCommitIDs = new AbbreviatedObjectId[squashPrefixArgs.length];
 
             for (int i = 0; i < squashPrefixArgs.length; i++)
             {
                 squashCommitIDs[i] = AbbreviatedObjectId.fromString(((ValueArgument) squashPrefixArgs[i]).getValue());
 
                 Collection<ObjectId> candidateObjects = null;
 
                 try
                 {
                     candidateObjects = objReader.resolve(squashCommitIDs[i]);
                 }
                 catch (Exception e)
                 {
                     /*
                      * commit id could not be resolved by git
                      */
                 }
 
                 if (candidateObjects == null || candidateObjects.size() == 0)
                 {
                     throw new Exception(Messages.formatString(
                         "CheckinCommand.CommitIdAmbiguousFormat", squashCommitIDs[i].name())); //$NON-NLS-1$
                 }
                 else if (candidateObjects.size() > 1)
                 {
                     throw new Exception(Messages.formatString(
                         "CheckinCommand.CommitIdAmbiguousFormat", squashCommitIDs[i].name())); //$NON-NLS-1$
                 }
                 else
                 {
                     RevCommit revCommit = revWalk.parseCommit(candidateObjects.toArray(new ObjectId[1])[0]);
 
                     if (revCommit == null)
                     {
                         throw new Exception(Messages.formatString(
                             "CheckinCommand.CommitIdDoesNotExistFormat", squashCommitIDs[i].name())); //$NON-NLS-1$
                     }
                 }
             }
 
             return squashCommitIDs;
         }
         finally
         {
             if (objReader != null)
             {
                 objReader.release();
             }
 
             if (revWalk != null)
             {
                 revWalk.release();
             }
         }
     }
 
     private static class CheckinTaskCompletedHandler
         implements TaskCompletedHandler
     {
         private static final ConsoleOutputTaskHandler consoleOutputTaskHandler = new ConsoleOutputTaskHandler();
 
         public void onTaskCompleted(Task task, TaskStatus status)
         {
             if (status.getSeverity() == TaskStatus.ERROR
                 && status.getException() instanceof ActionDeniedBySubscriberException)
             {
                 Main.printError(Messages.getString("CheckinCommand.GatedCheckinAborted")); //$NON-NLS-1$
                 Main.printError(status.getException().getLocalizedMessage(), false);
             }
             else
             {
                 /* Delegate to console output handler */
                 consoleOutputTaskHandler.onTaskCompleted(task, status);
             }
         }
     }
 }
