 package operation;
 
 import java.util.Scanner;
 
 import org.apache.log4j.Logger;
 
 import data.Task;
 
 
 public abstract class Operation {
 
 	
 	
 	
 	protected boolean isUndoAble=false;
 	private static Logger logger=Logger.getLogger(Operation.class);
 	public static Operation getOperationObj(String userCommand)
 	{
 		Operation object;
 		
 		String intendedOperation;
 		intendedOperation=userCommand.trim().split("\\s+")[0];
 		intendedOperation = intendedOperation.toLowerCase();
 		logger.debug(intendedOperation);
 		
 		
 		if (intendedOperation.equals("add") || intendedOperation.equals("insert")){
 			object = new Add(intendedOperation);
 			}
 		else if (intendedOperation.equals("delete") || intendedOperation.equals("remove")){
 			object = new Delete(intendedOperation);
 		}
 		else if (intendedOperation.equals("modify") || intendedOperation.equals("update") || 
				intendedOperation.equals("edit")) {
 			object =new Modify(intendedOperation);
 		}
 		else if (intendedOperation.equals("search") || intendedOperation.equals("find")){
 			object = new Search(intendedOperation);
 		}
 		else if (intendedOperation.equals("completed") || intendedOperation.equals("done")){
 			object = new ToggleCompleted(intendedOperation);
 		}
 		else if(intendedOperation.equals("star") || intendedOperation.equals("important")){
 			object = new ToggleImportant(intendedOperation);
 		}
 		else if (intendedOperation.equals("archive")){
 			object = new Archive(intendedOperation);
 		}
 		else if (intendedOperation.equals("overdue")){
 			object= new Overdue(intendedOperation);
 		}
 		else if (intendedOperation.equals("login")){
 			object= new GoogleCalendarOp();
 		}
 		else
 		{
 			object=new Default();
 		}
 
 		
 		
 		return object; 
 	}
 	
 	
 	public abstract Task[] execute(String userCommand);
 	
 	public abstract Task[] undo();
 	
 	public abstract Task[] redo();
 	
 	protected Task[] execute(Task taskToBeExecuted)
 	{
 		return null;
 		
 	}		
 	public abstract boolean isUndoAble();
 	
 	public abstract boolean isInputCorrect(String command);
 	public abstract String getErrorMessage();
 	
 	public abstract String getOperationName();
 	
 	/**
 	 * @param args
 	 */
 
 
 }
