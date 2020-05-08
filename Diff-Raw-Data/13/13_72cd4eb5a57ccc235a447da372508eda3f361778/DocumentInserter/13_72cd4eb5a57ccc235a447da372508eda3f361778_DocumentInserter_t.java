 package org.dawnsci.macro.console;
 
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.dawnsci.macro.api.IMacroEventListener;
 import org.eclipse.dawnsci.macro.api.IMacroService;
 import org.eclipse.dawnsci.macro.api.MacroEventObject;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * Class to link any IDocument to recording macros.
  * 
  * @author fcp94556
  *
  */
 public class DocumentInserter implements IMacroEventListener, IPartListener {
 
 	private static IMacroService mservice;
 	public static void setMacroService(IMacroService s) {
 		mservice = s;
 	}
 	public static IMacroService getMacroService() {
 		return mservice;
 	}
 	
 	private ISourceViewer  viewer;
 	private InsertionType  type;
 	private DocumentInsertionJob job;
 		
 	public void init(ISourceViewer viewer, InsertionType type) {
 		this.viewer   = viewer;
 		this.type     = type;
 		this.job      = new DocumentInsertionJob(type, viewer);
 		
 		viewer.getTextWidget().addDisposeListener(new DisposeListener() {
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				if (job!=null) job.stop();
 			}
 		});
 	}
 
 	@Override
 	public void macroChangePerformed(MacroEventObject mevt) {
 		
 		if (viewer==null || viewer.getDocument()==null) return;
 
 		String cmd = type==InsertionType.PYTHON ? mevt.getPythonCommand() : mevt.getJythonCommand();
 		String contents = viewer.getDocument().get().trim();
 		if (contents.endsWith(cmd.trim()) || contents.endsWith(cmd.trim()+"\n>>>") || contents.endsWith(cmd.trim()+"\r\n>>>")) {
 			System.out.println("Command already made : "+cmd);
 			return; // Avoid recursion, do nothing
 		}
 		if (mevt.isImmediate()) {
 			try {
 				IDocument document = viewer.getDocument();
 				document.replace(document.getLength(), 0, cmd);
 				document.replace(document.getLength(), 0, "\n");
 
 			} catch (BadLocationException e) {
 				e.printStackTrace(); 
 			}
 
 		} else {
 		    job.add(cmd);
 		}
 	}
 
 	@Override
 	public boolean isDisposed() {
 		if (viewer==null) return true;
 		if (viewer!=null && viewer.getTextWidget()!=null) {
 			return viewer.getTextWidget().isDisposed();
 		}
 		return false;
 	}
 
 	private boolean isConnected = false;
 	public void connect() {
 		mservice.addMacroListener(this);
 		isConnected = true;
 		EclipseUtils.getPage().addPartListener(this);
 	}
 	public void disconnect() {
 		mservice.removeMacroListener(this);
 		isConnected = false;
 		if (EclipseUtils.getPage()!=null) EclipseUtils.getPage().removePartListener(this);
 	}
 
 	public boolean isConnected() {
 		return isConnected;
 	}
 	public void toggleConnected() {
 		if (isConnected()) {
 			disconnect();
 		} else {
 			connect();
 		}
 	}
 	@Override
 	public void partActivated(IWorkbenchPart part) {
         try {
         	IPlottingSystem sys = (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
         	if (sys!=null && mservice!=null) {
     			mservice.publish(new MacroEventObject(sys));
     		}
 
         } catch (Exception ne) {
         	// Ignored, it's just for setting the macros when parts are selected
         }
 	}
 	@Override
 	public void partBroughtToTop(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void partClosed(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void partDeactivated(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void partOpened(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 		
 	}
	public void dispose() {
		disconnect();
		if (job!=null) job.stop();
	}
 }
