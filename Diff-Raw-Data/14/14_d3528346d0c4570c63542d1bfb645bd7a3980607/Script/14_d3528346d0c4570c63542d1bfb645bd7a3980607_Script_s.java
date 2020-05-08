 package filescript;
 import actions.Action;
import actions.SectionAction;
 import filters.filter;
 import orders.order;
 import fileManager.*;
<<<<<<< HEAD
import myFileScriptExceptions.*;
 
=======
 import java.util.*;
 import java.io.File;
>>>>>>> c85660ecf6062171651265e94e73a9669a82d929
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
<<<<<<< HEAD

=======
 		fm.ChangeComparator(_orderCommand);
 		for (Iterator<FileElement> i1 = fm.getFilesIterator(); i1.hasNext(); )
 		{
 			File f = new File (i1.next().getFileName());
 			if (_filterCommand.isFileFilterd(f))
 			{
 				//_actionCommand
 			}
 		}
>>>>>>> c85660ecf6062171651265e94e73a9669a82d929
 	}
 	
 	
 }
