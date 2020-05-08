 package org.dawnsci.macro.console;
 
 import org.eclipse.dawnsci.macro.api.IMacroEventListener;
 import org.eclipse.dawnsci.macro.api.IMacroService;
 import org.eclipse.dawnsci.macro.api.MacroEventObject;
 import org.eclipse.jface.text.source.ISourceViewer;
 
 /**
  * Class to link any IDocument to recording macros.
  * 
  * @author fcp94556
  *
  */
 public class DocumentInserter implements IMacroEventListener {
 
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
 		this.job      = new DocumentInsertionJob(viewer);
 	}
 
 	@Override
	public void macroChangePerformed(MacroEventObject mevt) {
 		
 		String cmd = type==InsertionType.PYTHON ? mevt.getPythonCommand() : mevt.getJythonCommand();
 		String contents = viewer.getDocument().get().trim();
 		if (contents.endsWith(cmd.trim()) || contents.endsWith(cmd.trim()+"\n>>>") || contents.endsWith(cmd.trim()+"\r\n>>>")) {
 			System.out.println("Command already made : "+cmd);
 			return; // Avoid recursion, do nothing
 		}
 		job.add(cmd);
 	}
 
 	@Override
 	public boolean isDisposed() {
 		if (viewer!=null && viewer.getTextWidget()!=null) {
 			return viewer.getTextWidget().isDisposed();
 		}
 		return false;
 	}
 
 	private boolean isConnected = false;
 	public void connect() {
 		mservice.addMacroListener(this);
 		isConnected = true;
 	}
 	public void disconnect() {
 		mservice.removeMacroListener(this);
 		isConnected = false;
 		if (job!=null) job.cancel();
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
 }
