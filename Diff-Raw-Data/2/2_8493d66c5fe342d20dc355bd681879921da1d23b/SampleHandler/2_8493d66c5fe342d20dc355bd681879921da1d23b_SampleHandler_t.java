 package pcpl.core.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.ui.IWorkbenchWindow;
 
 import pcpl.core.eventHandler.BreakPointListener;
 import pcpl.core.eventHandler.EventCenter;
 import pcpl.core.launch.pcplLauncher;
 import pcpl.core.mode.AbstractMode;
 import pcpl.core.mode.NormalMode;
 import pcpl.core.mode.RecordMode;
 import pcpl.core.mode.TraceMode;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class SampleHandler extends AbstractHandler {
 	/**
 	 * The constructor.
 	 */
 	public SampleHandler() {
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
 		if(EventCenter.getInstance().getModeType() == 1){		//normal mode
 			EventCenter.getInstance().setModeType(2);
 			System.out.print("change Mode Type 2\n");
 		}
 		else if(EventCenter.getInstance().getModeType() == 2){//record mode
 			EventCenter.getInstance().removeBreakPointListener(EventCenter.getInstance().getRecMode());
 			//EventCenter.getInstance().setMode(EventCenter.getInstance().getNorMode());
 			TraceMode t = new TraceMode();
 			EventCenter.getInstance().setTraMode(t);
 			System.out.print("change Mode Type 3\n");
 		}
		else if(EventCenter.getInstance().getModeType() == 3){		//normal mode
 			EventCenter.getInstance().setModeType(1);
 			System.out.print("change Mode Type 1\n");
 		}
 		else{	//something not good happend
 			System.err.print("mode error");
 		}
 
 		return null;
 	}
 	
 
 }
