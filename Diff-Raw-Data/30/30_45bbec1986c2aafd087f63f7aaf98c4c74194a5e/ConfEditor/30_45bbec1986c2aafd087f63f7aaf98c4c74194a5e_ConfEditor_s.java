 package com.rytong.ui.editor.conf;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.erlide.backend.BackendCore;
 import org.erlide.backend.IBackend;
 import org.erlide.jinterface.ErlLogger;
 
 import com.ericsson.otp.erlang.OtpErlangAtom;
 import com.ericsson.otp.erlang.OtpErlangList;
 import com.ericsson.otp.erlang.OtpErlangLong;
 import com.ericsson.otp.erlang.OtpErlangObject;
 import com.ericsson.otp.erlang.OtpErlangString;
 import com.ericsson.otp.erlang.OtpErlangTuple;
 import com.rytong.ui.editor.ColorManager;
 import com.rytong.ui.internal.RytongUIPlugin;
 import com.rytong.ui.internal.preference.PreferenceConstants;
 
 /**
 * ConfEditor is the TextEditor instance used by the plugin.
  * 
  * @author lu.jingbo@rytong.com
  * 
  */
 public class ConfEditor extends TextEditor {
 
 	private ColorManager colorManager;
 	private ConfScanner confScanner;
 
 	public ConfEditor() {
 		super();
 		// setDocumentProvider(provider);
 	}
 
 	@Override
 	protected void initializeEditor() {
 		super.initializeEditor();
 		colorManager = new ConfColorManager();
 		SourceViewerConfiguration cfg = new ConfSourceViewerConfiguration(this);
 		setSourceViewerConfiguration(cfg);
 	}
 
 	@Override
 	public void doSaveAs() {
 		super.doSaveAs();
 	}
 
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		super.doSave(progressMonitor);
 		make_errors();
 	}
 
 	@Override
 	public void dispose() {
 		colorManager.dispose();
 		super.dispose();
 	}
 
 	public ConfScanner getConfScanner() {
 		if (confScanner == null)
 			confScanner = new ConfScanner(colorManager);
 		return confScanner;
 	}
 
 	private void make_errors() {
 		IEditorInput editorInput = this.getEditorInput();
 
 		if (!(editorInput instanceof IFileEditorInput)) {
 			return;
 		}
 		IFile file = ((IFileEditorInput) editorInput).getFile();
 		String fn = file.getLocation().toFile().getAbsolutePath();
 
 		// start by clearing all the old markers.
 		int depth = IResource.DEPTH_INFINITE;
 		try {
 			file.deleteMarkers(IMarker.PROBLEM, true, depth);
 		} catch (CoreException e) {
 			ErlLogger.info("failed to delete old markers.");
 			e.printStackTrace();
 		}
 		Object[] rst = consult_conf(fn);
 		if (rst != null) {
 			int lineno = (int) rst[0];
 			String errmsg = (String) rst[1];
 			IPreferenceStore prefs = RytongUIPlugin.getDefault() .getPreferenceStore();
 			String severity = prefs.getString(PreferenceConstants.VALIDATION);
 			if (PreferenceConstants.SYNTAX_VALIDATION_IGNORE.equals(severity)) {
 				ErlLogger.debug("Possible syntax errors ignored due to preference settings");
 				return;
 			}
 
 			int markerSeverity = IMarker.SEVERITY_ERROR;
 
 			if (PreferenceConstants.SYNTAX_VALIDATION_WARNING.equals(severity))
 				markerSeverity = IMarker.SEVERITY_WARNING;
 			IMarker marker;
 			try {
 				marker = file.createMarker(IMarker.PROBLEM);
 
 				marker.setAttribute(IMarker.SEVERITY, markerSeverity);
 				marker.setAttribute(IMarker.MESSAGE, errmsg);
 				marker.setAttribute(IMarker.LINE_NUMBER, lineno);
 			} catch (CoreException e) {
 				ErlLogger.warn("failed to create marker.");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Object[] consult_conf(String fn) {
     
 		IBackend ideBackend = BackendCore.getBackendManager().getIdeBackend();
 		Object[] rst = null;
 		if (ideBackend != null) {
 			ErlLogger.debug("call erlang backend to parse conf file");
 			OtpErlangObject res = null;
 			try {
 				res = ideBackend.call("file", "consult", "s", fn);
 				if (res instanceof OtpErlangTuple) {
 					final OtpErlangTuple tuple = (OtpErlangTuple) res;
 					ErlLogger.debug("the tuple : " + tuple);
 					OtpErlangAtom tag = (OtpErlangAtom) tuple.elementAt(0);
 
 					if (tag.atomValue().equals("error")) {
 						OtpErlangTuple errdata = (OtpErlangTuple) tuple
 								.elementAt(1);
 						int lineno = ((OtpErlangLong) errdata.elementAt(0))
 								.intValue();
						OtpErlangList errlist = (OtpErlangList) errdata
								.elementAt(2);
 						String errmsg = "";
						for (final OtpErlangObject err : errlist) {
							final OtpErlangString msg = (OtpErlangString) err;
							errmsg += msg.stringValue();
						}
 						rst = new Object[] { lineno, errmsg };
 					}
 				}
 			} catch (Exception e) {
 				ErlLogger.warn("failed to consult conf file.");
 				e.printStackTrace();
 			}
 		}
 		return rst;
 	}
 }
