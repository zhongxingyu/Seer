 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.internal.core.model.ScriptModelConstants;
 import org.eclipse.dltk.debug.ui.breakpoints.BreakpointUtils;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class ScriptToggleBreakpointAdapter implements IToggleBreakpointsTarget {
 
 	protected ITextEditor getPartEditor(IWorkbenchPart part) {
 		if (part instanceof ITextEditor) {
 			return (ITextEditor) part;
 		}
 
 		return null;
 	}
 
 	protected IResource getPartResource(IWorkbenchPart part) {
 		ITextEditor textEditor = getPartEditor(part);
 		if (textEditor != null) {
 			IResource resource = (IResource) textEditor.getEditorInput()
 					.getAdapter(IResource.class);
 			return resource;
 		}
 
 		return null;
 	}
 
 	public ScriptToggleBreakpointAdapter() {
 
 	}
 
 	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
 			throws CoreException {
 		if (selection instanceof ITextSelection) {
 
 			ITextSelection textSelection = (ITextSelection) selection;
 			int lineNumber = textSelection.getStartLine() + 1; // one based
 
 			IResource resource = getPartResource(part);
 
 			if (resource != null) {
 				IBreakpoint[] breakpoints = DebugPlugin.getDefault()
 						.getBreakpointManager().getBreakpoints(
 								ScriptModelConstants.MODEL_ID);
 
 				for (int i = 0; i < breakpoints.length; i++) {
 					IBreakpoint breakpoint = breakpoints[i];
 					if (resource.equals(breakpoint.getMarker().getResource())) {
 						if (((ILineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
 							// delete existing breakpoint
 							breakpoint.delete();
 							return;
 						}
 					}
 				}
 				ITextEditor partEditor = getPartEditor(part);
 				if (partEditor instanceof ScriptEditor) {
 					ScriptEditor ed = (ScriptEditor) partEditor;
 					IRegion lineInformation;
 					try {
 						lineInformation = ed.getScriptSourceViewer()
 								.getDocument().getLineInformation(
 										lineNumber - 1);
 						String string = ed.getScriptSourceViewer()
 								.getDocument().get(lineInformation.getOffset(),
 										lineInformation.getLength());
 						int contains = string.indexOf("function");
 						if (contains != -1) {
 							string = string.substring(
 									contains + "function".length()).trim();
 							int apos = string.indexOf('(');
 							if (apos >= 0)
 								string = string.substring(0, apos).trim();
 							BreakpointUtils.addMethodEntryBreakpoint(
 									partEditor, lineNumber, string, string);
 							return;
 						} else
 							BreakpointUtils.addLineBreakpoint(partEditor,
 									lineNumber);
 					} catch (BadLocationException e) {
 						DLTKDebugPlugin.log(e);
 						return;
 					}
 				} else
 					BreakpointUtils.addLineBreakpoint(partEditor, lineNumber);
 			}
 		}
 	}
 
 	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
 			ISelection selection) {
 		return getPartResource(part) != null;
 	}
 
 	public void toggleMethodBreakpoints(IWorkbenchPart part,
 			ISelection selection) throws CoreException {
 	}
 
 	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
 			ISelection selection) {
 		return false;
 	}
 
 	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
 			throws CoreException {
 		if (selection instanceof ITextSelection) {
 
 			ITextSelection textSelection = (ITextSelection) selection;
 			int lineNumber = textSelection.getStartLine() + 1; // one based
 
 			IResource resource = getPartResource(part);
 
 			if (resource != null) {
 				IBreakpoint[] breakpoints = DebugPlugin.getDefault()
 						.getBreakpointManager().getBreakpoints(
 								ScriptModelConstants.MODEL_ID);
 
 				for (int i = 0; i < breakpoints.length; i++) {
 					IBreakpoint breakpoint = breakpoints[i];
 					if (resource.equals(breakpoint.getMarker().getResource())) {
 						if (((ILineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
 							// delete existing breakpoint
 							breakpoint.delete();
 							return;
 						}
 					}
 				}
 				ITextEditor partEditor = getPartEditor(part);
 				if (partEditor instanceof ScriptEditor) {
 					ScriptEditor ed = (ScriptEditor) partEditor;
 					IRegion lineInformation;
 					try {
 						lineInformation = ed.getScriptSourceViewer()
 								.getDocument().getLineInformation(
 										lineNumber - 1);
 						String string = ed.getScriptSourceViewer()
 								.getDocument().get(lineInformation.getOffset(),
 										lineInformation.getLength());
 						int indexOf = string.indexOf('=');
 						string = string.substring(0, indexOf);
 						indexOf = string.lastIndexOf('.') + 1;
 						if (indexOf != -1)
 							string = string.substring(indexOf);
 						indexOf = string.lastIndexOf(' ' + 1);
 						if (indexOf != -1)
 							string = string.substring(indexOf).trim();
 						BreakpointUtils.addWatchPoint(partEditor, lineNumber,
 								string);
 					} catch (BadLocationException e) {
 						DLTKDebugPlugin.log(e);
 						return;
 					}
 				} else
 					BreakpointUtils.addWatchPoint(partEditor, lineNumber,
 							"Hello");
 			}
 		}
 	}
 
 	public boolean canToggleWatchpoints(IWorkbenchPart part,
 			ISelection selection) {
 		if (selection instanceof ITextSelection) {
 			ITextSelection ts = (ITextSelection) selection;
 			int startLine = ts.getStartLine();
 			String ta = ts.getText();
 			if (part instanceof ScriptEditor) {
 				ScriptEditor ed = (ScriptEditor) part;
 				try {
 					IRegion lineInformation = ed.getScriptSourceViewer()
 							.getDocument()
 							.getLineInformation(ts.getStartLine());
 					String string = ed.getScriptSourceViewer().getDocument()
 							.get(lineInformation.getOffset(),
 									lineInformation.getLength());
 					return string.indexOf('=') > -1;
 				} catch (BadLocationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
			return ta.contains("=");
 		}
 		return true;
 	}
 }
