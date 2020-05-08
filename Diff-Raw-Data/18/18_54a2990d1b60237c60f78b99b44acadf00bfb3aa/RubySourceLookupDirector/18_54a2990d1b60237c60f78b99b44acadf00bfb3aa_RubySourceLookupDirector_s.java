 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.File;
 import java.net.URI;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IPersistableSourceLocator;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.internal.core.model.ScriptStackFrame;
import org.eclipse.dltk.ruby.debug.model.RubyStackFrame;
 
 public class RubySourceLookupDirector implements IPersistableSourceLocator {
 
 	public RubySourceLookupDirector() {
 	}
 
 	public Object getSourceElement(IStackFrame stackFrame) {
 		// Transition code, will be removed later
 		URI uri = null;
		if (stackFrame instanceof RubyStackFrame) {
			RubyStackFrame sf = (RubyStackFrame) stackFrame;
			uri = sf.getFile();
		} else if (stackFrame instanceof IScriptStackFrame) {
 			ScriptStackFrame sf = (ScriptStackFrame) stackFrame;
 			uri = sf.getFileName();
 		}
 
 		if (uri == null) {
 			return null;
 		}
 
 		String pathname = uri.getPath();
 
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			pathname = pathname.substring(1);
 		}
 
		//System.out.println("====> " + pathname);
 
 		File file = new File(pathname);
 
		IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(new Path(file.getParent()));
 
 		if (container != null) {
 			IResource resource = container.findMember(file.getName());
 
 			if (resource instanceof IFile) {
 				return (IFile) resource;
 			}
 		} else {
 			return file;
 		}
 
 		return null;
 	}
 
 	public String getMemento() throws CoreException {
 		return null;
 	}
 
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
 
 	}
 
 	public void initializeFromMemento(String memento) throws CoreException {
 
 	}
 }
