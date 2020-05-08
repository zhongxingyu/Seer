 /*
  * Created on May 29, 2006
  */
 package org.sakaiproject.tool.tasklist.rsf;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.tool.api.ToolManager;
 import org.sakaiproject.tool.tasklist.api.Task;
 import org.sakaiproject.tool.tasklist.api.TaskListManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.localeutil.LocaleGetter;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
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
   private UserDirectoryService userDirectoryService;
   private TaskListManager taskListManager;
   private ToolManager toolManager;
   private MessageLocator messageLocator;
   private LocaleGetter localegetter;
 
   public String getViewID() {
     return VIEW_ID;
   }
 
   public void setMessageLocator(MessageLocator messageLocator) {
     this.messageLocator = messageLocator;
   }
 
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
     this.userDirectoryService = userDirectoryService;
   }
 
   public void setTaskListManager(TaskListManager taskListManager) {
     this.taskListManager = taskListManager;
   }
 
   public void setToolManager(ToolManager toolManager) {
     this.toolManager = toolManager;
   }
 
   public void setLocaleGetter(LocaleGetter localegetter) {
     this.localegetter = localegetter;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
       ComponentChecker checker) {
     String username = userDirectoryService.getCurrentUser().getDisplayName();
     UIOutput.make(tofill, "current-username", username);
     // Illustrates fetching messages from locator - remaining messages are
     // written out
     // in template - localising HTML template directly is probably easier than
     // localising properties files.
     UIOutput.make(tofill, "task-list-title", messageLocator
         .getMessage("task_list_title"));
 
     User currentuser = userDirectoryService.getCurrentUser();
     String currentuserid = currentuser.getEid();
 
     UIForm newtask = UIForm.make(tofill, "new-task-form");
     UIInput.make(newtask, "new-task-name", "#{taskListBean.newtask.task}");
     UICommand.make(newtask, "submit-new-task",
         "#{taskListBean.processActionAdd}");
     // pre-bind the task's owner to avoid annoying the handler having to fetch
     // it
     newtask.parameters.add(new UIELBinding("#{taskListBean.newtask.owner}",
         currentuserid));
     String siteId = toolManager.getCurrentPlacement().getContext();
     newtask.parameters.add(new UIELBinding("#{taskListBean.siteID}", siteId));
 
     UIForm deleteform = UIForm.make(tofill, "delete-task-form");
 
     // Create a multiple selection control for the tasks to be deleted.
     // We will fill in the options at the loop end once we have collected them.
     UISelect deleteselect = UISelect.makeMultiple(deleteform, "delete-select",
         null, "#{taskListBean.deleteids}", new String[] {});
     Map tasks = taskListManager.findAllTasks(siteId);
 
     StringList deletable = new StringList();
     // JSF DateTimeConverter is a fun piece of kit - now here's a good way to shed
     // 600 lines:
     // http://fisheye5.cenqua.com/viewrep/javaserverfaces-sources/jsf-api/src/javax/faces/convert/DateTimeConverter.java
     DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
         DateFormat.SHORT, localegetter.get());
     for (Iterator iter = tasks.values().iterator(); iter.hasNext();) {
       Task task = (Task) iter.next();
       boolean candelete = task.getOwner().equals(currentuserid);
       UIBranchContainer taskrow = UIBranchContainer.make(deleteform,
           candelete ? "task-row:deletable"
              : "task-row:nondeletable", task.getId().toString());
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
     deleteform.parameters.add(new UIELBinding("#{taskListBean.siteID}", siteId));
 
     UICommand.make(deleteform, "delete-tasks",
         "#{taskListBean.processActionDelete}");
 
   }
 
   public List reportNavigationCases() {
     List togo = new ArrayList(); // Always navigate back to this view.
     togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
     return togo;
   }
 
 }
