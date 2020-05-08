 package eplic.core.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import eplic.core.breakpoint.BreakpointManager;
 import eplic.core.eventHandler.EventCenter;
 import eplic.core.mode.NormalMode;
 import eplic.core.mode.RecordMode;
 import eplic.core.mode.TraceMode;
 import eplic.core.ui.pcplDialog;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class SampleHandler extends AbstractHandler {
 	/**
 	 * The constructor.
 	 */
 	private pcplDialog _dialog;
 	public SampleHandler() {
 		initDialog();
 	}
 
 	private void initDialog() {
 		_dialog = new pcplDialog(Display.getDefault().getActiveShell());
 	}
 
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		/*IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 		MessageDialog.openInformation(
 				window.getShell(),
 				"Core",
 				"Hello, Eclipse world");*/
 		//_dialog.setInput(EventCenter.getInstance().getModeList());
 		//_dialog.run();
 		if(EventCenter.getInstance().getModeType() == 0){		//normal mode
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 			MessageDialog.openInformation(
 					window.getShell(),
 					"Core",
 					"EPLIC Plugin is started");
 			BreakpointManager.getInstance().removeAllBreakpoint();
 			BreakpointManager.getInstance().setAllBreakpoint();
 			NormalMode nm = new NormalMode();
 			EventCenter.getInstance().setNorMode(nm);
 			System.out.print("BreakPoint Setup \n");
 			System.out.print("change Mode Type 1\n");
 		}
 		else if(EventCenter.getInstance().getModeType() == 1){		//normal mode
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 			MessageDialog.openInformation(
 					window.getShell(),
 					"Core",
 					"Now, Do something you want to trace");
 			RecordMode rm = new RecordMode();
 			EventCenter.getInstance().setRecMode(rm);
 			System.out.print("change Mode Type 2\n");
 		}
 		else if(EventCenter.getInstance().getModeType() == 2){//record mode
 			//EventCenter.getInstance().setMode(EventCenter.getInstance().getNorMode());
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 			MessageDialog.openInformation(
 					window.getShell(),
 					"Core",
 					"Change to TraceMode");
 			TraceMode tm = new TraceMode();
			EventCenter.getInstance().setTraMode(tm);
 			System.out.print("change Mode Type 3\n");
 		}
 		else if(EventCenter.getInstance().getModeType() == 3){		//normal mode
 			EventCenter.getInstance().reset();
 			BreakpointManager.getInstance().reset();
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 			MessageDialog.openInformation(
 					window.getShell(),
 					"Core",
 					"EPLIC Reset");
 		}
 		else{	
 			System.err.print("mode error");
 		}
 
 		return null;
 	}
 	
 
 }
