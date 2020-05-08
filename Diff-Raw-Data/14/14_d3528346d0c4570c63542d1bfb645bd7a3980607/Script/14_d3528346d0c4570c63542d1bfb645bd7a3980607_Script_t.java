 package filescript;
 import actions.Action;
 import filters.filter;
 import orders.order;
 import fileManager.*;
 
 import java.util.*;
 import java.io.File;
 public class Script {
 	private Action _actionCommand;
 	private filter _filterCommand;
 	private order _orderCommand;
 	
 	public Script(Action act, filter filt, order ord)
 	{
 		_actionCommand = act;
 		_filterCommand = filt;
 		_orderCommand = ord;
 	}
 	
 	public void runScript(FileManager fm)
 	{
 		fm.ChangeComparator(_orderCommand);
 		for (Iterator<FileElement> i1 = fm.getFilesIterator(); i1.hasNext(); )
 		{
 			File f = new File (i1.next().getFileName());
 			if (_filterCommand.isFileFilterd(f))
 			{
 				//_actionCommand
 			}
 		}
 	}
 	
 	
 }
