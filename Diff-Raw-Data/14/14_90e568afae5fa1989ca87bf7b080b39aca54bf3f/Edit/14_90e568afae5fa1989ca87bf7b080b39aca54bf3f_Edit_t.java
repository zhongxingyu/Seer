 //@author A0092101Y
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 public class Edit implements UndoableCommand {
     private List<AbstractTask> editSpace, wholeTaskList;
     private AbstractTask originalTask, editedTask;
     private String editParameter;
     int index;
     private Logging editLog = new Logging("Edit");
 
     // Initializes the edit parameters
     public Edit(List<AbstractTask> editSpace, int index, String editParameter)
 	    throws IndexOutOfBoundsException, IllegalArgumentException {
 
 	validateConstructorArguments(editSpace, index, editParameter);
 
 	this.editSpace = editSpace;
 	this.index = index;
 	this.editParameter = editParameter;
 
 	editLog.addLog(Logging.LoggingLevel.INFO,
 		"Edit(): edit object initialized with editSpace of size: "
 			+ editSpace.size() + " index: " + index
 			+ " and editParameter: " + editParameter);
     }
 
     private void validateConstructorArguments(List<AbstractTask> editSpace,
 	    int index, String editParameter) {
 
 	if (editSpace == null || editSpace.size() <= 0) {
 	    editLog.addLog(Logging.LoggingLevel.WARNING,
 		    "Edit(): edit object initialization failed. Illegal editSpace (editSpace = "
 			    + editSpace + ")");
 	    throw new IllegalArgumentException(
 		    "editSpace cannot be empty or null");
 	}
 
 	if (editParameter == null) {
 	    editLog.addLog(
 		    Logging.LoggingLevel.WARNING,
 		    "Edit(): edit object initialization failed. Illegal editParameter (editParameter = "
 			    + editParameter + ")");
 	    throw new IllegalArgumentException(
 		    "the edit parameter cannot be null");
 	}
 
 	if (index <= 0 || index > editSpace.size()) {
 	    editLog.addLog(Logging.LoggingLevel.WARNING,
 		    "Edit(): edit object initialization failed. Index is out of bounds (index = "
 			    + index + ")");
 	    throw new IndexOutOfBoundsException(
 		    "index pointer is outside the edit space");
 	}
     }
 
     // Executes the edit operation, saving both the original and edited tasks
     // and returning them in a list original->edited
     public List<AbstractTask> execute(List<AbstractTask> wholeTaskList)
 	    throws IllegalArgumentException {
 	validateExecuteArguments(wholeTaskList);
 
 	this.wholeTaskList = wholeTaskList;
 	originalTask = editSpace.get(index - 1);
 	editedTask = editTask(originalTask);
 
 	wholeTaskList.remove(originalTask);
 	wholeTaskList.add(editedTask);
 
 	editLog.addLog(Logging.LoggingLevel.INFO, "Edit.execute(): The task \""
 		+ originalTask + "\" has been changed to \"" + editedTask
 		+ "\"");
 
 	return generateReturnList(originalTask, editedTask);
     }
 
     private void validateExecuteArguments(List<AbstractTask> wholeTaskList) {
 	if (wholeTaskList == null || wholeTaskList.size() <= 0) {
 	    editLog.addLog(Logging.LoggingLevel.WARNING,
 		    "Edit.execute(): delete execution failed. Illegal task list (wholeTaskList = "
 			    + wholeTaskList + ")");
 	    throw new IllegalArgumentException(
 		    "taskList cannot be empty or null");
 	}
     }
 
     // edits a task according to the parameter given and returns it
     private AbstractTask editTask(AbstractTask originalTask) {
 	AbstractTask editedTask = (AbstractTask) originalTask.clone();
 
 	StringTokenizer st = new StringTokenizer(editParameter);
 	String paramToken = st.nextToken();
 
 	if (!paramToken.startsWith(".")) {
 	    editDescription(editedTask, paramToken);
 
 	    while (st.hasMoreTokens()) {
 		paramToken = st.nextToken();
 		if (!paramToken.startsWith("."))
 		    addToField(editedTask, paramToken, "desc");
 		else
 		    break;
 	    }
 	}
 
 	while (true) {
 	    switch (paramToken) {
 	    case ".at":
 	    case ".venue":
 		paramToken = st.nextToken();
 		editVenue(editedTask, paramToken);
 
 		while (st.hasMoreTokens()) {
 		    paramToken = st.nextToken();
 		    if (!paramToken.startsWith("."))
 			addToField(editedTask, paramToken, "venue");
 		    else
 			break;
 		}
 		break;
 	    case ".status":
 		paramToken = st.nextToken();
 		editStatus(editedTask, paramToken);
 		break;
 	    case ".by":
 	    case ".deadline":
 		String deadline = st.nextToken();
 		if (st.hasMoreTokens())
 		    paramToken = st.nextToken();
 		else
 		    paramToken = "";
 
 		deadline = checkAndInsertTime(deadline, paramToken, "end");
 		if (!(new DateTime(deadline)).validateDateTime())
 		    throw new IllegalArgumentException(
 			    "invalid deadline format");
 
 		editDeadline(editedTask, deadline);
 		break;
 	    case ".from":
 		String from = st.nextToken();
 		if (st.hasMoreTokens())
 		    paramToken = st.nextToken();
 		else
 		    paramToken = "";
 
 		from = checkAndInsertTime(from, paramToken, "start");
 		if (!(new DateTime(from)).validateDateTime())
 		    throw new IllegalArgumentException(
 			    "invalid start time format");
 
 		editStartTime(editedTask, from);
 		break;
 	    case ".to":
 		String to = st.nextToken();
 		if (st.hasMoreTokens())
 		    paramToken = st.nextToken();
 		else
 		    paramToken = "";
 
 		to = checkAndInsertTime(to, paramToken, "end");
 		if (!(new DateTime(to)).validateDateTime())
 		    throw new IllegalArgumentException(
 			    "invalid end time format");
 
 		editEndTime(editedTask, to);
 		break;
	    default:
		if (st.hasMoreTokens())
		    paramToken = st.nextToken();
		else
		    paramToken = "";
 	    }
 
 	    if (paramToken.startsWith("."))
 		continue;
 	    else if (st.hasMoreTokens())
 		paramToken = st.nextToken();
 	    else
 		break;
 	}
 
 	return editedTask;
     }
 
     private String checkAndInsertTime(String date, String time, String type) {
 	if (!time.startsWith(".") && !time.equals(""))
 	    date = date + " " + time;
 	else if (type.equals("start"))
 	    date = date + " 00:00";
 	else
 	    date = date + " 23:59";
 	return date;
     }
 
     private void addToField(AbstractTask task, String value, String last) {
 	switch (last) {
 	case "desc":
 	    editDescription(task, task.getDescription() + " " + value);
 	    break;
 	case "venue":
 	    editVenue(task, task.getVenue() + " " + value);
 	    break;
 	default:
 	    assert false;
 	}
     }
 
     private void editEndTime(AbstractTask editedTask, String fieldValue) {
 	if (!(editedTask instanceof TimedTask))
 	    return;

 	TimedTask task = (TimedTask) editedTask;
 	task.setEndDate(fieldValue);
     }
 
     private void editStartTime(AbstractTask editedTask, String fieldValue) {
 	if (!(editedTask instanceof TimedTask))
 	    return;

 	TimedTask task = (TimedTask) editedTask;
 	task.setStartDate(fieldValue);
     }
 
     private void editDeadline(AbstractTask editedTask, String fieldValue) {
 	if (!(editedTask instanceof DeadlineTask))
 	    return;

 	DeadlineTask task = (DeadlineTask) editedTask;
 	task.setEndDate(fieldValue);
     }
 
     private void editStatus(AbstractTask editedTask, String fieldValue) {
 	switch (fieldValue.toLowerCase()) {
 	case "done":
 	    editedTask.setStatus(AbstractTask.Status.DONE);
 	    break;
 	case "undone":
 	    editedTask.setStatus(AbstractTask.Status.UNDONE);
 	    break;
 	case "impossible":
 	    editedTask.setStatus(AbstractTask.Status.IMPOSSIBLE);
 	    break;
 	}
     }
 
     private void editVenue(AbstractTask editedTask, String fieldValue) {
 	editedTask.setVenue(fieldValue);
     }
 
     private void editDescription(AbstractTask editedTask, String fieldValue) {
 	editedTask.setDescription(fieldValue);
     }
 
     // undoes this edit operation
     // returns the list: edited->original
     public List<AbstractTask> undo() {
 	// the task list from where the edited task came from must still exist
 	assert wholeTaskList != null;
 	// the edited task must exist in task list
 	assert wholeTaskList.contains(editedTask);
 
 	wholeTaskList.remove(editedTask);
 	wholeTaskList.add(originalTask);
 
 	editLog.addLog(Logging.LoggingLevel.INFO, "Edit.undo(): The task \""
 		+ editedTask + "\" has been changed back to \"" + originalTask
 		+ "\"");
 
 	return generateReturnList(editedTask, originalTask);
     }
 
     // generates the returned task list
     private List<AbstractTask> generateReturnList(AbstractTask oldTask,
 	    AbstractTask newTask) {
 	List<AbstractTask> returnList = new Vector<AbstractTask>();
 	returnList.add(oldTask);
 	returnList.add(newTask);
 
 	return returnList;
     }
 }
