 package org.nodeclipse.debug.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.chromium.debug.core.model.ChromiumLineBreakpoint;
 import org.chromium.debug.core.model.VmResourceId;
 import org.chromium.sdk.Script;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.nodeclipse.debug.util.NodeResourceUtil;
 import org.nodeclipse.ui.Activator;
 import org.osgi.framework.Bundle;
 
 public class ResourceManager extends
 		org.chromium.debug.core.model.ResourceManager {
 	private IProject debugProject = null;
 
 	public ResourceManager(IProject debugProject) {
 		super(debugProject);
 		this.debugProject = debugProject;
 	}
 
 	public synchronized void addScript(Script newScript) {
 		VmResourceId id = VmResourceId.forScript(newScript);
 		try {
 			VmResourceInfo info = resourceIdToInfo.get(id);
 			ScriptSet scriptSet;
 			if (info == null) {
 				scriptSet = new ScriptSet();
 				info = createAndRegisterResourceFile(id, scriptSet);
 			} else {
 				// TODO(peter.rybin): support adding scripts to one resource at
 				// once not to rewrite file
 				// every time.
 				scriptSet = (ScriptSet) info.getMetadata();
 				;
 			}
 			scriptSet.add(newScript);
 			writeScriptSource(scriptSet.asCollection(), info.getFile());
 			addCromiumBreakpointFromJs(id.getName(), info.getFile());
 		} catch (RuntimeException e) {
 			throw new RuntimeException("Failed to add script " + id, e);
 		}
 	}
 
 	private void addCromiumBreakpointFromJs(String jsFilename,
 			IResource cromiumFile) {
 		NodeResourceUtil.register(jsFilename, cromiumFile);
 		IBreakpoint[] breakpoins = DebugPlugin.getDefault()
 				.getBreakpointManager()
 				.getBreakpoints(VProjectWorkspaceBridge.DEBUG_MODEL_ID);
 		for (IBreakpoint b : breakpoins) {
 			try {
 				if (machFile(jsFilename, b.getMarker().getResource())) {
 					addChromiumBreakpoint(cromiumFile.getName(), ((ILineBreakpoint)b).getLineNumber());
 				}
 			} catch(Exception ex) {
 				new RuntimeException("Failed to add script " + jsFilename, ex);
 			}
 		}
 	}
 
 	private boolean machFile(String jsFilename, IResource breakpointResource) throws CoreException {
 		if (jsFilename == null || breakpointResource == null) {
 			return false;
 		}
 		if (jsFilename.equals(breakpointResource.getName())) {
 			return true;
 		}
 		String path = breakpointResource.getFullPath().toOSString();
 		if (jsFilename.equals(path)) {
 			return true;
 		}
 		path = breakpointResource.getLocationURI().getPath();
 		if(jsFilename.equals(path)) {
 			return true;
 		}
 		return false;
 	}
 	
 	private void addChromiumBreakpoint(String filename, int lineNumber) throws CoreException {
 		IResource resource = debugProject.getFile(filename);
        ChromiumLineBreakpoint lineBreakpoint = 
     		new ChromiumLineBreakpoint(resource, lineNumber, VProjectWorkspaceBridge.DEBUG_MODEL_ID);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
 	}
 }
