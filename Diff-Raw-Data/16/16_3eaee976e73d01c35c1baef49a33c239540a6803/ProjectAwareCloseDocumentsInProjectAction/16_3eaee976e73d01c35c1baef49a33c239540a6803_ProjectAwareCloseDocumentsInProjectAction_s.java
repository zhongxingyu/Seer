 /**
  * Copyright 2013 markiewb
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package de.markiew.netbeans.plugin.actions.closedocuments;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.openide.loaders.DataObject;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.awt.ActionReferences;
 import org.openide.awt.ActionRegistration;
 import org.openide.util.NbBundle.Messages;
 import org.openide.windows.TopComponent;
 
 @ActionID(
         category = "Project",
         id = "closedocuments.ProjectAwareCloseDocumentsInProjectAction"
         )
 @ActionRegistration(
         displayName = "#CTL_ProjectAwareCloseDocumentsInProjectAction"
         )
 @ActionReferences({
    @ActionReference(path = "Projects/Actions", position = 10, separatorAfter = 50)})
 
 @Messages("CTL_ProjectAwareCloseDocumentsInProjectAction=Close All Documents From This Project")
 public final class ProjectAwareCloseDocumentsInProjectAction implements ActionListener {
 
     private final Project context;
     private final boolean documentsFromSameProject = true;
 
     public ProjectAwareCloseDocumentsInProjectAction(Project context) {
         this.context = context;
     }
 
     @Override
     public void actionPerformed(ActionEvent ev) {
         Project currentProject = context;
         for (TopComponent tc : DocumentManager.getInstance().getDocumentsForProject(currentProject, documentsFromSameProject)) {
             tc.close();
         }
     }
 
 }
