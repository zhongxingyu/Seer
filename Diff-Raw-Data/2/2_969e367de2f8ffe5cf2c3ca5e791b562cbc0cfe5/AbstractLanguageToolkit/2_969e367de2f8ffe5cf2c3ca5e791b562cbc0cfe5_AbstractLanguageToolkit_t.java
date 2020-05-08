 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.problem.DLTKProblemReporter;
 import org.eclipse.dltk.compiler.problem.DefaultProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.search.DLTKSearchParticipant;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.core.util.Messages;
 
 public abstract class AbstractLanguageToolkit implements IDLTKLanguageToolkit {
 	public AbstractLanguageToolkit() {
 	}
 
 	public IProblemFactory createProblemFactory() {
 		return new DefaultProblemFactory();
 	}
 
 	public IProblemReporter createProblemReporter(IResource resource,
 			IProblemFactory factory) {
 
 		try {
 			resource.deleteMarkers(IMarker.PROBLEM, true,
 					IResource.DEPTH_INFINITE);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return new DLTKProblemReporter(resource, factory);
 	}
 
 	protected abstract String getCorePluginID();
 
 	public IStatus validateSourceModule(String name) {
 		if (name == null) {
 			return new Status(IStatus.ERROR, this.getCorePluginID(), -1,
 					Messages.convention_unit_nullName, null);
 		}
 		if (!isScriptLikeFileName(name)) {
 			return new Status(IStatus.ERROR, this.getCorePluginID(), -1,
 					MessageFormat.format(
 							Messages.convention_unit_notScriptName,
 							new String[] { "", "" }), null);
 		}
 		return IModelStatus.VERIFIED_OK;
 	}
 
 	public boolean isScriptLikeFileName(String name) {
 		// TODO: Add more correct checking here.
 		String[] extensions = this.getLanguageFileExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			if (name.endsWith("." + extensions[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return false;
 	}
 
 	public boolean validateSourcePackage(IPath path) {
 		return true;
 	}
 
 	public ICompletionEngine createCompletionEngine(
 			ISearchableEnvironment environment, CompletionRequestor requestor,
 			Map options, IScriptProject project) {
 		return null;
 	}
 
 	public IMatchLocatorParser createMatchParser(MatchLocator locator) {
 		return null;
 	}
 
 	public IStatus validateSourceModule(IResource resource) {
 		return validateSourceModule(resource.getName());
 	}
 
 	public IStatus validateSourceModule(IPath resource) {
 		if (resource.toString().startsWith(
 				IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)) {
 			return IModelStatus.VERIFIED_OK;
 		}
 		return validateSourceModule(resource.lastSegment());
 	}
 
 	public IStatus validateSourceModuleName(String str) {
 		return validateSourceModule(str);
 	}
 
 	public String getEditorID(Object inputElement) {
 		return null;
 	}
 
 	public ISelectionEngine createSelectionEngine(
 			ISearchableEnvironment environment, Map options) {
 		return null;
 	}
 
 	public SourceIndexerRequestor createSourceRequestor() {
 		return new SourceIndexerRequestor();
 	}
 
 	public DLTKSearchParticipant createSearchParticipant() {
 		return null;
 	}
 
 	public MatchLocator createMatchLocator(SearchPattern pattern,
 			SearchRequestor requestor, IDLTKSearchScope scope,
 			SubProgressMonitor monitor) {
 		return null;
 	}
 
 	public ICalleeProcessor createCalleeProcessor(IMethod method,
 			IProgressMonitor monitor, IDLTKSearchScope scope) {
 		return null;
 	}
 
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public ICallProcessor createCallProcessor() {
 		return null;
 	}
 
 	public abstract String[] getLanguageFileExtensions();
 
 	public IType[] getParentTypes(IType type) {
 		return null;
 	}
 
 	public IStatus validateSourceModule(IModelElement parent, String str) {
 		return validateSourceModule(str);
 	}
 
 	protected Status createNotScriptFileStatus() {
 		return new Status(IStatus.ERROR, getCorePluginID(), -1, MessageFormat
 				.format(Messages.convention_unit_notScriptName, new String[] {
 						getLanguageFileExtensions().toString(), "Tcl" }), null);
 	}
 
 	private final int bufferLength = 2 * 1024;
 	private byte buf[] = new byte[bufferLength + 1];
 
 	private boolean checkHeader(File file, Pattern[] headerPaterns,
 			Pattern[] footerPatterns) throws FileNotFoundException, IOException {
 		FileInputStream reader = null;
 		try {
 			reader = new FileInputStream(file);
 			reader.read(buf);
 
 			String header = new String(buf);
 
 			if (header != null) {
 				if (checkBufferForPatterns(header, headerPaterns)) {
 					return true;
 				}
 				if (file.length() < bufferLength && footerPatterns != null) {
 					if (checkBufferForPatterns(header, footerPatterns)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private boolean checkFooter(File file, Pattern[] footerPatterns)
 			throws FileNotFoundException, IOException {
 		RandomAccessFile raFile = new RandomAccessFile(file, "r");
 		try {
 			long len = bufferLength;
 			long fileSize = raFile.length();
 			long offset = fileSize - len;
 			if (offset < 0) {
 				offset = 0;
 			}
 			raFile.seek(offset);
 			int code = raFile.read(buf);
 			if (code != -1) {
 				String content = new String(buf, 0, code);
 				if (checkBufferForPatterns(content, footerPatterns)) {
 					return true;
 				}
 			}
 			return false;
 		} finally {
 			raFile.close();
 		}
 
 	}
 
 	private boolean checkBufferForPatterns(CharSequence header,
 			Pattern[] patterns) {
 		for (int i = 0; i < patterns.length; i++) {
 			Matcher m = patterns[i].matcher(header);
 			if (m.find()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public IStatus checkPatterns(File file, Pattern[] headerPatterns,
 			Pattern[] footerPatterns) {
 		synchronized (buf) {
 			try {
 				if (checkHeader(file, headerPatterns, footerPatterns)) {
 					return IModelStatus.VERIFIED_OK;
 				}
				if (footerPatterns != null && file.length() > bufferLength
 						&& checkFooter(file, footerPatterns)) {
 					return IModelStatus.VERIFIED_OK;
 				}
 			} catch (FileNotFoundException e) {
 				return new Status(IStatus.ERROR, DLTKCore.PLUGIN_ID, -1,
 						"Can't open file", null);
 			} catch (IOException e) {
 				return new Status(IStatus.ERROR, DLTKCore.PLUGIN_ID, -1,
 						"Can't read file", null);
 			}
 		}
 		return new Status(IStatus.ERROR, DLTKCore.PLUGIN_ID, -1,
 				"Header not found", null);
 	}
 }
