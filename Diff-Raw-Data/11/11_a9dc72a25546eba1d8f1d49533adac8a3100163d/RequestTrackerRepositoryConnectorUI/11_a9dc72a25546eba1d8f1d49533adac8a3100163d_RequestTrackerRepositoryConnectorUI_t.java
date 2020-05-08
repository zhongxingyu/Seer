 /*
  * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package se.fnord.rt.ui;
 
 import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
 import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.mylyn.tasks.core.ITaskComment;
 import org.eclipse.mylyn.tasks.core.ITaskMapping;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
 import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
 import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
 
 import se.fnord.rt.core.RequestTrackerRepositoryConnector;
 
 public class RequestTrackerRepositoryConnectorUI extends AbstractRepositoryConnectorUi {
 
     public RequestTrackerRepositoryConnectorUI() {
         // TODO Auto-generated constructor stub
     }
 
     @Override
     public String getConnectorKind() {
         return RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND;
     }
 
     @Override
     public boolean hasSearchPage() {
         return true;
     }
 
     @Override
     public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
         return new RequestTrackerTaskRepositoryPage(taskRepository);
     }
 
     @Override
     public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery queryToEdit) {
         RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
         wizard.addPage(new RTQueryPage(repository, queryToEdit));
         return wizard;
     }
 
     @Override
     public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String getTaskKindLabel(ITask task) {
         return "Issue";
     }
     
     @Override
     public String getReplyText(TaskRepository taskRepository, ITask task, ITaskComment taskComment, boolean includeTask) {
        final IRepositoryPerson author = taskComment.getAuthor();
        if (author != null) {
            if (author.getName() != null)
                return author.getName() + " wrote";
            if (author.getPersonId() != null)
                return author.getPersonId() + " wrote";
        }
        return "Commenter wrote";
     }
     
 }
