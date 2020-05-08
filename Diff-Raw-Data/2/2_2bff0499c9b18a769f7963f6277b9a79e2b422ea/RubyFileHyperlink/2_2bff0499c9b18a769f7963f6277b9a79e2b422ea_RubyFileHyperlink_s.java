 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.debug.ui.console;
 
 
 import java.text.MessageFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.console.IHyperlink;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
 * A hyperlink from a stack trace line of the form "*(*.java:*)"
  */
 public class RubyFileHyperlink implements IHyperlink {
 	
 	private TextConsole fConsole;
 
 	/**
 	 * Constructor for RubyFileHyperlink.
 	 */
 	public RubyFileHyperlink(TextConsole console) {
 		fConsole = console;
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkEntered()
 	 */
 	public void linkEntered() {
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkExited()
 	 */
 	public void linkExited() {
 	}
 
 	/**
 	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
 	 */
 	public void linkActivated() {
 		try {
 			String fileName;
             int lineNumber;
             try {
                 String linkText = getLinkText();
                 fileName = getFileName(linkText);
                 lineNumber = getLineNumber(linkText);
             } catch (CoreException e1) {
                 ErrorDialog.openError(DLTKDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.RubyFileHyperlink_Error, ConsoleMessages.RubyFileHyperlink_Error, e1.getStatus()); 
                 return;
             }
 			
 			// documents start at 0
 			if (lineNumber > 0) {
 				lineNumber--;
 			}
 			Object sourceElement = getSourceModule(fileName);
 			if (sourceElement != null) {				
 				IEditorInput editorInput = getEditorInput(sourceElement);
 				if (editorInput != null) {
 					String editorId = getEditorId(editorInput, sourceElement);
 					if (editorId != null) {
 						IEditorPart editorPart = DLTKDebugUIPlugin.getActivePage().openEditor(editorInput, editorId);
 						if (editorPart instanceof ITextEditor && lineNumber >= 0) {
 							ITextEditor textEditor = (ITextEditor)editorPart;
 							IDocumentProvider provider = textEditor.getDocumentProvider();
 							provider.connect(editorInput);
 							IDocument document = provider.getDocument(editorInput);
 							try {
 								IRegion line = document.getLineInformation(lineNumber);
 								textEditor.selectAndReveal(line.getOffset(), line.getLength());
 							} catch (BadLocationException e) {
                                 MessageDialog.openInformation(DLTKDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.RubyFileHyperlink_0, MessageFormat.format("{0}{1}{2}", new String[] {(lineNumber+1)+"", ConsoleMessages.RubyFileHyperlink_1, fileName}));  //$NON-NLS-2$ //$NON-NLS-1$
 							}
 							provider.disconnect(editorInput);
 						}
 						return;
 					}
 				}
 			}
 			// did not find source
 			MessageDialog.openInformation(DLTKDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.RubyFileHyperlink_Information_1, MessageFormat.format(ConsoleMessages.RubyFileHyperlink_Source_not_found_for__0__2, new String[] {fileName}));  
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.errorDialog(ConsoleMessages.RubyFileHyperlink_An_exception_occurred_while_following_link__3, e); 
 			return;
 		}
 	}
 	
 	/**
 	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
 	 */
 	public String getEditorId(IEditorInput input, Object inputObject) {
 		try {
 			IEditorDescriptor descriptor= IDE.getEditorDescriptor(input.getName());
 			return descriptor.getId();
 		} catch (PartInitException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * @see IDebugModelPresentation#getEditorInput(Object)
 	 */
 	public IEditorInput getEditorInput(Object item) {
 		try {			
 			return EditorUtility.getEditorInput(item);
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 			return null;
 		}
 	}
 	
 
 	protected Object getSourceModule(String fileName) throws CoreException {				
 		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
 		return f;
 	}
 
 	/**
 	 * Returns the fully qualified name of the type to open
 	 *  
 	 * @return fully qualified type name
 	 * @exception CoreException if unable to parse the type name
 	 */
 	protected String getFileName(String linkText) throws CoreException {
         Pattern p = Pattern.compile("^(.+rb):(\\d+)$");
         Matcher m = p.matcher(linkText);
         if (m.find()) {
         	String name = m.group(1);
         	return name;
         }		
         IStatus status = new Status(IStatus.ERROR, DLTKDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.RubyFileHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null); 
         throw new CoreException(status);
     }	
 	
 	/**
 	 * Returns the line number associated with the stack trace or -1 if none.
 	 * 
 	 * @exception CoreException if unable to parse the number
 	 */
 	protected int getLineNumber(String linkText) throws CoreException {
 		Pattern p = Pattern.compile("^(.+rb):(\\d+)$");
         Matcher m = p.matcher(linkText);
         if (m.find()) {
         	String lineText = m.group(2);
         	try {
 				return Integer.parseInt(lineText);
 			} catch (NumberFormatException e) {
 				IStatus status = new Status(IStatus.ERROR, DLTKDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.RubyFileHyperlink_Unable_to_parse_line_number_from_hyperlink__6, e); 
 				throw new CoreException(status);
 			}        	
         }			
 		IStatus status = new Status(IStatus.ERROR, DLTKDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.RubyFileHyperlink_Unable_to_parse_line_number_from_hyperlink__7, null); 
 		throw new CoreException(status);			
 	}
 	
 	/**
 	 * Returns the console this link is contained in.
 	 *  
 	 * @return console
 	 */
 	protected TextConsole getConsole() {
 		return fConsole;
 	}
 	
 	/**
 	 * Returns this link's text
 	 * 
 	 * @exception CoreException if unable to retrieve the text
 	 */
 	protected String getLinkText() throws CoreException {
 	    try {
             IDocument document = getConsole().getDocument();
 	        IRegion region = getConsole().getRegion(this);
            // int regionOffset = region.getOffset();
             
             return document.get(region.getOffset(), region.getLength());
             
 //	        int lineNumber = document.getLineOfOffset(regionOffset);
 //	        IRegion lineInformation = document.getLineInformation(lineNumber);
 //            int lineOffset = lineInformation.getOffset();
 //	        String line = document.get(lineOffset, lineInformation.getLength());
             
             //int regionOffsetInLine = regionOffset - lineOffset;
 
             //int linkEnd = line.indexOf(')', regionOffsetInLine);
             //int linkStart = line.lastIndexOf(' ', regionOffsetInLine);
             
             //return line.substring(linkStart==-1?0:linkStart+1,linkEnd+1);
             //return line.trim();
 		} catch (BadLocationException e) {
 			IStatus status = new Status(IStatus.ERROR, DLTKDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.RubyFileHyperlink_Unable_to_retrieve_hyperlink_text__8, e); 
 			throw new CoreException(status);
 		}		
 	}
 
 }
