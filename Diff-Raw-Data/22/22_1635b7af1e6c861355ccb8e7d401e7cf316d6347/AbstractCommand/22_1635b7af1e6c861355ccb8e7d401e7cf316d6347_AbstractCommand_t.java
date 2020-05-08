 package common.command;
 
 import common.Result;
 
 /**
  * This class is an abstract base for useful commands such as 
  * {@link common.command.AddItemCommand AddItemCommand}, 
  * {@link common.command.RemoveItemCommand RemoveItemCommand}, and
  * {@link common.command.TransferItemCommand TransferItemCommand}
  * 
  * These commands are designed to be undoable.
  * @author murphyra
  *
  */
 public abstract class AbstractCommand {
 	
 	boolean _executed = false;
 	
 	/**
 	 *  Calling this method executes the actions that this command describes. 
 	 *  @return A Result object indicating the success of the action.
 	 *  
 	 *  Calling this method twice in a row will return a false result. undo() must be called before execute() can again be called.
 	 */
 	public Result execute(){
 		if (_executed) return new Result(false, "Must undo before executing again.");
		Result result = executeGuts();
		if (result.getStatus())
			_executed = true;
		return result;
 	}
 
 	 protected abstract Result executeGuts();
 	
 	/**
 	 *  Calling this method undoes the actions done by execute().
 	 *  @return A Result object indicating the success of the undo.
 	 *  
 	 * 	Calling this undo() before execute() is called will return a false result. 
 	 *  Calling this method twice in a row will return a false result. execute() must be called before undo() can again be called.
 	 */
 	public Result undo(){
 		if (!_executed) return new Result(false, "Must execute before undoing.");
		Result result = undoGuts();
		if (result.getStatus())
			_executed = false;
		return result;
 	}
 
 	protected abstract Result undoGuts();
 	
 	/**
 	 * Returns true if this command can be undone.
 	 * @return boolean
 	 */
 	public boolean canUndo() {
 		return _executed;
 	}
 	
 	/**
 	 * Returns true if this command can be executed.
 	 * @return boolean telling whether the command can be executed.
 	 */
 	public boolean canExecute() {
 		return !_executed;
 	}
 	
 }
