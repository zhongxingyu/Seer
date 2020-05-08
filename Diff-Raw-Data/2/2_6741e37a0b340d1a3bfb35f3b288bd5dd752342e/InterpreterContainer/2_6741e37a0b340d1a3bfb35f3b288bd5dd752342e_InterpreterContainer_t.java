 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IBuiltinModuleProvider;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallChangedListener;
 import org.eclipse.dltk.launching.LaunchingMessages;
 import org.eclipse.dltk.launching.LibraryLocation;
 import org.eclipse.dltk.launching.PropertyChangeEvent;
 import org.eclipse.dltk.launching.ScriptRuntime;
 
 import com.ibm.icu.text.MessageFormat;
 
 /**
  * Interpreter Container - resolves a buildpath container tp interpreter
  */
 public class InterpreterContainer implements IBuildpathContainer {
 	/**
 	 * Corresponding interpreter
 	 */
 	private IInterpreterInstall fInterpreterInstall = null;
 	/**
 	 * Container path used to resolve to this interpreter
 	 */
 	private IPath fPath = null;
 	/**
 	 * Cache of buildpath entries per Interpreter install. Cleared when a
 	 * Interpreter changes.
 	 */
 	private static Map fgBuildpathEntries = null;
 	private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];
 
 	/**
 	 * Returns the buildpath entries associated with the given interpreter.
 	 * 
 	 * @param interpreter
 	 * @return buildpath entries
 	 */
 	private static IBuildpathEntry[] getBuildpathEntries(IInterpreterInstall interpreter) {
 		if (fgBuildpathEntries == null) {
 			fgBuildpathEntries = new HashMap(10);
 			// add a listener to clear cached value when a Interpreter changes or is
 			// removed
 			IInterpreterInstallChangedListener listener = new IInterpreterInstallChangedListener() {
 				public void defaultInterpreterInstallChanged(IInterpreterInstall previous, IInterpreterInstall current) {}
 
 				public void interpreterChanged(PropertyChangeEvent event) {
 					if (event.getSource() != null) {
 						fgBuildpathEntries.remove(event.getSource());
 					}
 				}
 
 				public void interpreterAdded(IInterpreterInstall newInterpreter) {
 					
 				}
 
 				public void interpreterRemoved(IInterpreterInstall removedInterpreter) {
 					fgBuildpathEntries.remove(removedInterpreter);
 				}
 			};
 			ScriptRuntime.addInterpreterInstallChangedListener(listener);
 		}
 		IBuildpathEntry[] entries = (IBuildpathEntry[]) fgBuildpathEntries.get(interpreter);
 		if (entries == null) {
 			entries = computeBuildpathEntries(interpreter);
 			fgBuildpathEntries.put(interpreter, entries);
 		}
 		return entries;
 	}
 
 	/**
 	 * Computes the buildpath entries associated with a interpreter - one entry
 	 * per library.
 	 * 
 	 * @param interpreter
 	 * @return buildpath entries
 	 */
 	private static IBuildpathEntry[] computeBuildpathEntries(IInterpreterInstall interpreter) {
 		LibraryLocation[] libs = interpreter.getLibraryLocations();
 		if (libs == null) {
 			libs = ScriptRuntime.getLibraryLocations(interpreter);
 		}
 		List entries = new ArrayList(libs.length);
 		List rawEntries = new ArrayList (libs.length);
 		for (int i = 0; i < libs.length; i++) {
 			IPath entryPath = libs[i].getSystemLibraryPath();
 		
 			if (!entryPath.isEmpty()) {
 				
 				//	resolve symlink
 				try {
 					File f = entryPath.toFile();
 					if (f == null)
 						continue;
 					entryPath = new Path(f.getCanonicalPath());
 				} catch (IOException e) {
 					continue;
 				}
 				
 				if (rawEntries.contains(entryPath))
 					continue;
 				
 				/*if (!entryPath.isAbsolute())
 					Assert.isTrue(false, "Path for IBuildpathEntry must be absolute"); //$NON-NLS-1$*/
 				IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
 				ArrayList excluded = new ArrayList(); // paths to exclude
 				for (int j = 0; j < libs.length; j++) {
 					IPath otherPath = libs[j].getSystemLibraryPath();
 					if (otherPath.isEmpty())
 						continue;
 					//resolve symlink
 					try {
 						File f = entryPath.toFile();
 						if (f == null)
 							continue;
 						entryPath = new Path(f.getCanonicalPath());
 					} catch (IOException e) {
 						continue;
 					}
 										
 					// compare, if it contains some another					
 					if (entryPath.isPrefixOf(otherPath) && !otherPath.equals(entryPath) ) {						
 						IPath pattern = otherPath.removeFirstSegments(entryPath.segmentCount()).append("*");
 						if( !excluded.contains(pattern ) ) {
 							excluded.add(pattern);
 						}
 					}
 				}
 
 				entries.add(DLTKCore.newLibraryEntry(entryPath, EMPTY_RULES, attributes,
 						BuildpathEntry.INCLUDE_ALL, (IPath[]) excluded.toArray(new IPath[excluded.size()]), false, true));
 				rawEntries.add (entryPath);
 			}
 		}
 		// Add builtin entry.
 		{
 			IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
			entries.add(DLTKCore.newBuiltinEntry(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY.append(interpreter.getInstallLocation().getAbsolutePath()), EMPTY_RULES, attributes, BuildpathEntry.INCLUDE_ALL, new IPath[0], false, true ));
 		}
 		return (IBuildpathEntry[]) entries.toArray(new IBuildpathEntry[entries.size()]);
 	}
 
 	/**
 	 * Constructs a interpreter buildpath container on the given interpreter
 	 * install
 	 * 
 	 * @param interpreter
 	 *            Interpreter install - cannot be <code>null</code>
 	 * @param path
 	 *            container path used to resolve this interpreter
 	 */
 	public InterpreterContainer(IInterpreterInstall interpreter, IPath path) {
 		fInterpreterInstall = interpreter;
 		fPath = path;
 	}
 
 	/**
 	 * @see IBuildpathContainer#getBuildpathEntries()
 	 */
 	public IBuildpathEntry[] getBuildpathEntries() {
 		return getBuildpathEntries(fInterpreterInstall);
 	}
 
 	/**
 	 * @see IBuildpathContainer#getDescription()
 	 */
 	public String getDescription() {
 		String tag = fInterpreterInstall.getName();
 		return MessageFormat.format(LaunchingMessages.InterpreterEnvironmentContainer_InterpreterEnvironment_System_Library_1, new String[] {
 			tag
 		});
 	}
 
 	/**
 	 * @see IBuildpathContainer#getKind()
 	 */
 	public int getKind() {
 		return IBuildpathContainer.K_DEFAULT_SYSTEM;
 	}
 
 	/**
 	 * @see IBuildpathContainer#getPath()
 	 */
 	public IPath getPath() {
 		return fPath;
 	}
 
 	public IBuiltinModuleProvider getBuiltinProvider() {
 		return fInterpreterInstall;
 	}
 }
