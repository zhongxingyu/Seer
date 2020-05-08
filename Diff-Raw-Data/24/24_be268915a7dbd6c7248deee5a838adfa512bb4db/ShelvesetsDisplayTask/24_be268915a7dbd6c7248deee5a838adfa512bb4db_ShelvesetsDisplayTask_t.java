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
import java.util.Collections;
import java.util.List;
 
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.interfaces.VersionControlService;
 import com.microsoft.gittf.core.tasks.framework.Task;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressDisplay;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.gittf.core.util.shelveset.ShelvesetCompartor;
 import com.microsoft.gittf.core.util.shelveset.ShelvesetSortOption;
 import com.microsoft.gittf.core.util.shelveset.ShelvesetView;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingSet;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Shelveset;
 
 /**
  * Looks up and displays the shelveset(s) queried using the shelveset name and
  * shelveset owner
  * 
  */
 public class ShelvesetsDisplayTask
     extends Task
 {
     private final VersionControlService versionControlService;
     private final ShelvesetView view;
     private final String shelvesetName;
     private final String shelvesetOwnerName;
 
     private boolean displayDetails = false;
     private ShelvesetSortOption sortOption = ShelvesetSortOption.DATE;
 
     /**
      * Constructor
      * 
      * @param versionControlService
      *        the version control service
      * @param view
      *        the shelveset view implementation to display the shelvesets
      * @param shelvesetName
      *        the shelveset name
      * @param shelvesetOwnerName
      *        the shelveset owner name
      */
     public ShelvesetsDisplayTask(
         final VersionControlService versionControlService,
         final ShelvesetView view,
         final String shelvesetName,
         final String shelvesetOwnerName)
     {
         Check.notNull(versionControlService, "versionControlService"); //$NON-NLS-1$
         Check.notNull(view, "view"); //$NON-NLS-1$
 
         this.versionControlService = versionControlService;
         this.view = view;
         this.shelvesetName = shelvesetName;
         this.shelvesetOwnerName = shelvesetOwnerName;
     }
 
     /**
      * Sets whether to display shelveset details or not
      * 
      * @param displayDetails
      */
     public void setDisplayDetails(final boolean displayDetails)
     {
         this.displayDetails = displayDetails;
     }
 
     /**
      * Sets the shelveset sorting option
      * 
      * @param sortOption
      */
     public void setSortOption(final ShelvesetSortOption sortOption)
     {
         this.sortOption = sortOption;
     }
 
     @Override
     public TaskStatus run(TaskProgressMonitor progressMonitor)
         throws Exception
     {
         progressMonitor.beginTask(Messages.getString("ShelvesetsDisplayTask.DownloadingShelvesets"), //$NON-NLS-1$
             1,
             TaskProgressDisplay.DISPLAY_PROGRESS.combine(TaskProgressDisplay.DISPLAY_SUBTASK_DETAIL));
 
         /* Queries the server for the matching shelvesets */
         Shelveset[] results = versionControlService.queryShelvesets(shelvesetName, shelvesetOwnerName);
 
         /* If there are no shelvesets that match the criteria show an error */
         if (results.length == 0)
         {
             progressMonitor.endTask();
             return new TaskStatus(TaskStatus.ERROR, Messages.getString("ShelvesetsDisplayTask.NoShelvesetsFound")); //$NON-NLS-1$
         }
 
         /*
          * If there is one shelveset matching and details is specified show the
          * super detailed view which displays the pending changes as well
          */
         if (displayDetails && results.length == 1)
         {
             // display shelveset details
             PendingSet[] shelvesetDetails = versionControlService.queryShelvesetChanges(results[0], false);
             progressMonitor.endTask();
 
             view.displayShelvesetDetails(results[0], shelvesetDetails);
         }
         else
         {
             progressMonitor.endTask();
 
             // Sort shelvesets
            List<Shelveset> shelvesets = new ArrayList<Shelveset>(results.length);
             for (Shelveset shelveset : results)
             {
                 shelvesets.add(shelveset);
             }
 
            Collections.sort(shelvesets, new ShelvesetCompartor(sortOption));

             // display all shelvesets
             view.displayShelvesets(shelvesets.toArray(new Shelveset[shelvesets.size()]), displayDetails);
         }
         return TaskStatus.OK_STATUS;
     }
 }
