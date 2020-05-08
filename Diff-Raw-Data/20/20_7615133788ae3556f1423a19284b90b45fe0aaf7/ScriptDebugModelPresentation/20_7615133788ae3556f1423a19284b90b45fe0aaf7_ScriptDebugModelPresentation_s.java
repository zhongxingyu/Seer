 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.core.model.IWatchExpression;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.IValueDetailListener;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPathEditorInput;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.editors.text.ILocationProvider;
 import org.eclipse.ui.part.FileEditorInput;
 
 public abstract class ScriptDebugModelPresentation extends LabelProvider
 		implements IDebugModelPresentation {
 
 	public static class ExternalFileEditorInput implements IPathEditorInput,
 			ILocationProvider {
 		private File file;
 
 		public ExternalFileEditorInput(File file) {
 			super();
 			this.file = file;
 		}
 
 		public boolean exists() {
 			return file.exists();
 		}
 
 		public ImageDescriptor getImageDescriptor() {
 			return null;
 		}
 
 		public String getName() {
 			return file.getName();
 		}
 
 		public IPersistableElement getPersistable() {
 			return null;
 		}
 
 		public String getToolTipText() {
 			return file.getAbsolutePath();
 		}
 
 		public Object getAdapter(Class adapter) {
 			if (ILocationProvider.class.equals(adapter))
 				return this;
 
 			return Platform.getAdapterManager().getAdapter(this, adapter);
 		}
 
 		public IPath getPath(Object element) {
 			if (element instanceof ExternalFileEditorInput) {
 				ExternalFileEditorInput input = (ExternalFileEditorInput) element;
 				return Path.fromOSString(input.file.getAbsolutePath());
 			}
 			return null;
 		}
 
 		public IPath getPath() {
 			return Path.fromOSString(file.getAbsolutePath());
 		}
 
 		public boolean equals(Object o) {
 			if (o == this)
 				return true;
 
 			if (o instanceof ExternalFileEditorInput) {
 				ExternalFileEditorInput input = (ExternalFileEditorInput) o;
 				return file.equals(input.file);
 			}
 
 			if (o instanceof IPathEditorInput) {
 				IPathEditorInput input = (IPathEditorInput) o;
 				return getPath().equals(input.getPath());
 			}
 
 			return false;
 		}
 
 		public int hashCode() {
 			return file.hashCode();
 		}
 	}
 
 	protected String getThreadText(IScriptThread thread) {
 		return thread.toString();
 	}
 
 	protected String getStackFrameText(IScriptStackFrame stackFrame) {
 		try {
 			return stackFrame.getName();
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 			return stackFrame.toString();
 		}
 	}
 
 	public String getVariableText(IScriptVariable variable) {
		return variable.toString();
 	}
 
 	protected String getValueText(IScriptValue value) {
 		return value.toString();
 	}
 
 	protected String getBreakpointText(IScriptBreakpoint breakpoint) {
 		try {
 			if (breakpoint instanceof IScriptLineBreakpoint) {
 				IScriptLineBreakpoint b = (IScriptLineBreakpoint) breakpoint;
 				return b.getResourceName() + " [line: " + b.getLineNumber()
 						+ "]";
 			}
 		} catch (CoreException e) {
 			// Nothing to do
 		}
 
 		return breakpoint.toString();
 	}
 
	protected String getWatchExpressionText(IWatchExpression exp) {
		String text = exp.getExpressionText() + " Error !";
 		try {
			if (!exp.hasErrors()) {
				text = exp.getExpressionText();
				IValue value = exp.getValue();
				if (value != null) {
 					text += " = " + value.getValueString();
 				}
 			}
 		} catch (DebugException e) {
 			// TODO: log exception
 		}
 
 		return text;
 	}
 
 	public final String getText(Object element) {
 		if (element instanceof IScriptBreakpoint) {
 			return getBreakpointText((IScriptBreakpoint) element);
 		} else if (element instanceof IScriptThread) {
 			return getThreadText((IScriptThread) element);
 		} else if (element instanceof IScriptStackFrame) {
 			return getStackFrameText((IScriptStackFrame) element);
 		} else if (element instanceof IScriptVariable) {
 			return getVariableText((IScriptVariable) element);
 		} else if (element instanceof IScriptValue) {
 			return getValueText((IScriptValue) element);
 		} else if (element instanceof IWatchExpression) {
 			return getWatchExpressionText((IWatchExpression) element);
 		}
 
 		return element.toString();
 	}
 
 	public void computeDetail(IValue value, IValueDetailListener listener) {
 		String detail = "";
 		try {
 			detail = value.getValueString();
 		} catch (DebugException e) {
 			// Nothing to do
 		}
 		listener.detailComputed(value, detail);
 	}
 
 	public void setAttribute(String attribute, Object value) {
 	}
 
 	public Image getImage(Object element) {
 		return null;
 	}
 
 	public IEditorInput getEditorInput(Object element) {
 		if (element instanceof File) {
 			return new ExternalFileEditorInput((File) element);
 		} else if (element instanceof IFile) {
 			return new FileEditorInput((IFile) element);
 		} else if (element instanceof ILineBreakpoint) {
 			return new FileEditorInput((IFile) ((ILineBreakpoint) element)
 					.getMarker().getResource());
 		}
 		return null;
 	}
 
 	public abstract String getEditorId(IEditorInput input, Object element);
 }
