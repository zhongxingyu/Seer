 /*
  * Created on May 29, 2006
  */
 package org.sakaiproject.tool.tasklist.rsf;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.sakaiproject.tool.tasklist.api.Task;
 
 import uk.org.ponder.rsf.components.ELReference;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIDeletionBinding;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UISelectChoice;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.DefaultView;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.stringutil.StringList;
 
 public class TaskListProducer implements ViewComponentProducer,
     NavigationCaseReporter, DefaultView {
   public static final String VIEW_ID = "TaskList";
   private Locale locale;
   private List taskList;
   private String siteId;
   private String userId;
   private String username;
 
   public String getViewID() {
     return VIEW_ID;
   }
 
   public void setTaskList(List taskList) {
     this.taskList = taskList;
   }
 
   public void setLocale(Locale locale) {
     this.locale = locale;
   }
 
   public void setSiteId(String siteId) {
     this.siteId = siteId;
   }
   
   public void setUserId(String userId) {
     this.userId = userId;
   }
   
   public void setUserName(String username) {
     this.username = username;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
       ComponentChecker checker) {
     UIOutput.make(tofill, "current-username", username);
     // Illustrates fetching messages from locator - remaining messages are
     // written out in template - localising HTML template directly is 
     // probably easier than localising properties files.
     UIOutput.make(tofill, "task-list-title", null, 
         "#{messageLocator.task_list_title}");
 
     UIForm newtask = UIForm.make(tofill, "new-task-form");
     UIInput.make(newtask, "new-task-name", "#{Task.new 1.task}");
     // no binding for this command since "commit" is a null-action using OTP
     UICommand.make(newtask, "submit-new-task");
     // pre-bind the task's owner to avoid annoying the handler having to fetch
     // it
     newtask.parameters.add(new UIELBinding("#{Task.new 1.owner}", userId));
     newtask.parameters.add(new UIELBinding("#{Task.new 1.siteId}", siteId));
 
     UIForm deleteform = UIForm.make(tofill, "delete-task-form");
 
     // Create a multiple selection control for the tasks to be deleted.
     // We will fill in the options at the loop end once we have collected them.
     UISelect deleteselect = UISelect.makeMultiple(deleteform, "delete-select",
         null, "#{deleteIds}", new String[] {});
 
     StringList deletable = new StringList();
     // JSF DateTimeConverter is a fun piece of kit - now here's a good way to
     // shed 600 lines:
     // http://fisheye5.cenqua.com/viewrep/javaserverfaces-sources/jsf-api/src/javax/faces/convert/DateTimeConverter.java
     DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
         DateFormat.SHORT, locale);
     for (int i = 0; i < taskList.size(); ++i) {
       Task task = (Task) taskList.get(i);
       boolean candelete = task.getOwner().equals(userId);
       UIBranchContainer taskrow = UIBranchContainer.make(deleteform,
           candelete ? "task-row:deletable"
              : "task-row:nondeletable");
       if (candelete) {
         UISelectChoice.make(taskrow, "task-select", deleteselect.getFullID(),
             deletable.size());
         deletable.add(task.getId().toString());
       }
       UIOutput.make(taskrow, "task-name", task.getTask());
       UIOutput.make(taskrow, "task-owner", task.getOwner());
       UIOutput.make(taskrow, "task-date", df.format(task.getCreationDate()));
     }
     deleteselect.optionlist.setValue(deletable.toStringArray());
     deleteform.parameters.add(new UIDeletionBinding("#{Task}", 
         new ELReference("#{deleteIds}")));
     // similarly no action binding here since deletion binding does all
     UICommand.make(deleteform, "delete-tasks");
 
   }
 
 
   public List reportNavigationCases() {
     List togo = new ArrayList();
     // Always navigate back to this view (actually the RSF default)
     togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
     return togo;
   }
 
 }
