 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - ompleted initial version (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.debug.ui.console;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.console.IHyperlink;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.ui.ide.IDE;
 
 /**
  * A hyper link from a stack trace line of the form "*(*.rb:*)"
  */
 public class RubyFileHyperlink implements IHyperlink {
 
 	private static final String ERROR_UNKNOWN_HYPERLINK = "Unknown hyperlink"; //$NON-NLS-1$
 
 	private static final String ERROR_NO_COLON_IN_LINK = "No ':' in link"; //$NON-NLS-1$
 
 	static final boolean DEBUG = false;
 
 	private final TextConsole fConsole;
 
 	public RubyFileHyperlink(TextConsole console) {
 		fConsole = console;
 	}
 
 	public void linkEntered() {
 	}
 
 	public void linkExited() {
 	}
 
 	public void linkActivated() {
 		final String fileName;
 		int lineNumber;
 		try {
 			final String linkText = getLinkText();
 			fileName = extractFileName(linkText);
 			lineNumber = extractLineNumber(linkText);
 		} catch (IllegalArgumentException e) {
 			DLTKDebugPlugin.log(e);
 			return;
 		}
 		// documents start at 0
 		if (lineNumber > 0) {
 			lineNumber--;
 		}
 		try {
 			final Object element = findSourceModule(fileName);
 			if (element == null) {
 				// did not find source
 				MessageDialog
 						.openInformation(
 								DLTKDebugUIPlugin.getActiveWorkbenchShell(),
 								ConsoleMessages.RubyFileHyperlink_Information_1,
 								MessageFormat
 										.format(
 												ConsoleMessages.RubyFileHyperlink_Source_not_found_for__0__2,
 												new Object[] { fileName }));
 				return;
 			}
 			final IEditorInput input = EditorUtility.getEditorInput(element);
 			if (input == null) {
 				return;
 			}
 			final IEditorDescriptor descriptor = IDE.getEditorDescriptor(input
 					.getName());
 			final IWorkbenchPage page = DLTKDebugUIPlugin.getActivePage();
 			final IEditorPart editor = page.openEditor(input, descriptor
 					.getId());
 			EditorUtility.revealInEditor(editor, lineNumber);
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin
 					.errorDialog(
 							ConsoleMessages.RubyFileHyperlink_An_exception_occurred_while_following_link__3,
 							e);
 		}
 	}
 
 	/**
 	 * Finds {@link IFile} or {@link ISourceModule} matching the specified file
 	 * name
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static Object findSourceModule(String fileName) {
 		final IPath path = Path.fromOSString(fileName);
 		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		final IFile f = root.getFileForLocation(path);
 		if (f != null) {
 			return f;
 		}
 		if (DEBUG) {
 			System.out.println("File for " + path + " is not found"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final IDLTKLanguageToolkit toolkit = RubyLanguageToolkit.getDefault();
 		final RubyConsoleSourceModuleLookup lookup = new RubyConsoleSourceModuleLookup(
 				toolkit);
 		return lookup.findSourceModuleByLocalPath(path);
 	}
 
 	/**
 	 * Returns the fully qualified name of the type to open
 	 * 
 	 * @return fully qualified type name
 	 * @exception IllegalArgumentException
 	 *                if unable to parse the type name
 	 */
 	static String extractFileName(String linkText)
 			throws IllegalArgumentException {
 		int pos = linkText.lastIndexOf(':');
 		if (pos > 0 && pos < linkText.length() - 1) {
 			return normalizePath(linkText.substring(0, pos));
 		}
 		throw new IllegalArgumentException(ERROR_NO_COLON_IN_LINK);
 	}
 
 	/**
 	 * Calls {@link File#getCanonicalPath()} for the specified file name
 	 * 
 	 * @param filePath
 	 * @return
 	 */
 	private static String normalizePath(String filePath) {
 		try {
 			final File file = new File(filePath);
 			return file.getCanonicalPath();
 		} catch (IOException e) {
 			return filePath;
 		}
 	}
 
 	/**
 	 * Returns the line number associated with the stack trace or -1 if none.
 	 * 
 	 * @exception IllegalArgumentException
 	 *                if unable to parse the number
 	 */
 	static int extractLineNumber(String linkText)
 			throws IllegalArgumentException {
 		int pos = linkText.lastIndexOf(':');
 		if (pos > 0 && pos < linkText.length() - 1) {
 			return Integer.parseInt(linkText.substring(pos + 1));
 		}
 		throw new IllegalArgumentException(ERROR_NO_COLON_IN_LINK);
 	}
 
 	/**
 	 * Returns this link's text
 	 * 
 	 * @exception IllegalArgumentException
 	 *                if unable to retrieve the text
 	 */
 	protected String getLinkText() throws IllegalArgumentException {
 		IRegion region = fConsole.getRegion(this);
 		if (region == null) {
 			throw new IllegalArgumentException(ERROR_UNKNOWN_HYPERLINK);
 		}
 		return getText(region.getOffset(), region.getLength());
 	}
 
 	protected String getText(int offset, int length)
 			throws IllegalArgumentException {
 		try {
 			return fConsole.getDocument().get(offset, length);
 		} catch (BadLocationException e) {
			throw new IllegalArgumentException(e.getMessage());
 		}
 	}
 
 }
