 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.launching;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ScriptUtils;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.LaunchingMessages;
 import org.eclipse.dltk.launching.LibraryLocation;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 
 /**
  * Abstract implementation of a interpreter install type. Subclasses should
  * implement
  * <ul>
  * <li><code>IInterpreterInstall doCreateInterpreterInstall(String id)</code></li>
  * <li><code>String getName()</code></li>
  * <li><code>IStatus validateInstallLocation(File installLocation)</code></li>
  * <li><code>String getLanguageId()</code></li>
  * </ul>
  * <p>
  * Clients implementing Interpreter install types should subclass this class.
  * </p>
  */
 public abstract class AbstractInterpreterInstallType implements
 		IInterpreterInstallType, IExecutableExtension {
 	public static final String DLTK_PATH_PREFIX = "DLTK:";
 
 	private List fInterpreters;
 
 	private String fId;
 
 	private static HashMap fCachedLocations = new HashMap();
 
 	protected AbstractInterpreterInstallType() {
 		fInterpreters = new ArrayList();
 	}
 
 	public IInterpreterInstall[] getInterpreterInstalls() {
 		return (IInterpreterInstall[]) fInterpreters
 				.toArray(new IInterpreterInstall[fInterpreters.size()]);
 	}
 
 	public void disposeInterpreterInstall(String id) {
 		Iterator it = fInterpreters.iterator();
 		while (it.hasNext()) {
 			IInterpreterInstall install = (IInterpreterInstall) it.next();
 			if (install.getId().equals(id)) {
 				it.remove();
 				ScriptRuntime.fireInterpreterRemoved(install);
 				return;
 			}
 		}
 	}
 
 	public IInterpreterInstall findInterpreterInstall(String id) {
 		Iterator it = fInterpreters.iterator();
 		while (it.hasNext()) {
 			IInterpreterInstall install = (IInterpreterInstall) it.next();
 			if (install.getId().equals(id)) {
 				return install;
 			}
 		}
 
 		return null;
 	}
 
 	public IInterpreterInstall createInterpreterInstall(String id)
 			throws IllegalArgumentException {
 		if (findInterpreterInstall(id) != null) {
 			String format = LaunchingMessages.InterpreterInstallType_duplicateInterpreter;
 			throw new IllegalArgumentException(MessageFormat.format(format,
 					new String[] { id }));
 		}
 
 		IInterpreterInstall install = doCreateInterpreterInstall(id);
 		fInterpreters.add(install);
 		return install;
 	}
 
 	/**
 	 * Subclasses should return a new instance of the appropriate
 	 * <code>IInterpreterInstall</code> subclass from this method.
 	 * 
 	 * @param id
 	 *            The Interpreter's id. The <code>IInterpreterInstall</code>
 	 *            instance that is created must return <code>id</code> from
 	 *            its <code>getId()</code> method. Must not be
 	 *            <code>null</code>.
 	 * @return the newly created IInterpreterInstall instance. Must not return
 	 *         <code>null</code>.
 	 */
 	protected abstract IInterpreterInstall doCreateInterpreterInstall(String id);
 
 	/**
 	 * Initializes the id parameter from the "id" attribute in the configuration
 	 * markup. Subclasses should not override this method.
 	 * 
 	 * @param config
 	 *            the configuration element used to trigger this execution. It
 	 *            can be queried by the executable extension for specific
 	 *            configuration properties
 	 * @param propertyName
 	 *            the name of an attribute of the configuration element used on
 	 *            the <code>createExecutableExtension(String)</code> call.
 	 *            This argument can be used in the cases where a single
 	 *            configuration element is used to define multiple executable
 	 *            extensions.
 	 * @param data
 	 *            adapter data in the form of a <code>String</code>, a
 	 *            <code>Hashtable</code>, or <code>null</code>.
 	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
 	 *      java.lang.String, java.lang.Object)
 	 */
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) {
 		fId = config.getAttribute("id"); //$NON-NLS-1$
 	}
 
 	public String getId() {
 		return fId;
 	}
 
 	public IInterpreterInstall findInterpreterInstallByName(String name) {
 		Iterator it = fInterpreters.iterator();
 		while (it.hasNext()) {
 			IInterpreterInstall install = (IInterpreterInstall) it.next();
 			if (install.getName().equals(name)) {
 				return install;
 			}
 		}
 
 		return null;
 	}
 
 	protected static void storeFile(File dest, URL url) throws IOException {
 		InputStream input = null;
 		OutputStream output = null;
 		try {
 			input = new BufferedInputStream(url.openStream());
 			output = new BufferedOutputStream(new FileOutputStream(dest));
 
 			// Simple copy
 			int ch = -1;
 			while ((ch = input.read()) != -1) {
 				output.write(ch);
 			}
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 
 			if (output != null) {
 				output.close();
 			}
 		}
 	}
 
 	protected String[] extractEnvironment() {
 		Map systemEnv = DebugPlugin.getDefault().getLaunchManager()
 				.getNativeEnvironmentCasePreserved();
 
 		filterEnvironment(systemEnv);
 
 		List list = new ArrayList();
 		Iterator it = systemEnv.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 			list.add(entry.getKey() + "=" + entry.getValue());
 		}
 
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * filter out any undesirable entries from the system environment
 	 * 
 	 * <p>
 	 * default implementation does nothing. subclasses are free to override.
 	 * </p>
 	 * 
 	 * @param environment
 	 *            system environment
 	 */
 	protected void filterEnvironment(Map environment) {
 		// Nothing to do
 	}
 
 	protected File storeToMetadata(Plugin plugin, String name, String path)
 			throws IOException {
 		return ScriptUtils.storeToMetadata(plugin, name, path);
 	}
 
 	/**
 	 * Process should write one line into console with format 'path1 path2
 	 * path3'
 	 * 
 	 * @param monitor
 	 * @param p
 	 * @return
 	 */
 	protected String[] readPathsFromProcess(IProgressMonitor monitor, Process p) {
 		final BufferedReader dataIn = new BufferedReader(new InputStreamReader(
 				p.getInputStream()));
 
 		final List result = new ArrayList();
 
 		final Object lock = new Object();
 
 		Thread tReading = new Thread(new Runnable() {
 			public void run() {
 				try {
 					while (true) {
 						String line = dataIn.readLine();
 						if (line != null) {
 							result.add(line);
 						} else {
 							break;
 						}
 					}
 
 					synchronized (lock) {
 						lock.notifyAll();
 					}
 
 				} catch (IOException e) {
 				}
 			}
 		});
 
 		tReading.start();
 
 		synchronized (lock) {
 			try {
				lock.wait(10000);
 			} catch (InterruptedException e) {
 
 			}
 			p.destroy();
 		}
 
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	public static LibraryLocation[] correctLocations(final List locs) {
 		List resolvedLocs = new ArrayList();
 		for (Iterator iter = locs.iterator(); iter.hasNext();) {
 			LibraryLocation l = (LibraryLocation) iter.next();
 			String res;
 			try {
 				File f = l.getLibraryPath().toFile();
 				if (f != null)
 					res = f.getCanonicalPath();
 				else
 					continue;
 			} catch (IOException e) {
 				continue;
 			}
 			LibraryLocation n = new LibraryLocation(new Path(res));
 			if (!resolvedLocs.contains(n))
 				resolvedLocs.add(n);
 		}
 
 		LibraryLocation[] libs = (LibraryLocation[]) resolvedLocs
 				.toArray(new LibraryLocation[resolvedLocs.size()]);
 		return libs;
 	}
 
 	protected void fillLocationsExceptOne(final List locs, String[] paths,
 			IPath path) {
 		String sPath = path.toOSString();
 		for (int i = 0; i < paths.length; i++) {
 			if (!paths[i].equals(sPath)) {
 				File f = new File(paths[i]);
 				if (f.exists()) {
 					LibraryLocation l = new LibraryLocation(new Path(paths[i]));
 					if (!locs.contains(l)) {
 						locs.add(l);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * run the interpreter library lookup in a
 	 * <code>ProgressMonitorDialog</code>
 	 */
 	protected void runLibraryLookup(final IRunnableWithProgress runnable)
 			throws InvocationTargetException, InterruptedException {
 
 		/*
 		 * ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
 		 * Display current = Display.getCurrent(); if (current != null) { try {
 		 * progress.run(true, false, runnable); } catch (SWTException ex) {
 		 * runnable.run(new NullProgressMonitor()); } } else { runnable.run(new
 		 * NullProgressMonitor()); }
 		 */
 		runnable.run(new NullProgressMonitor());
 	}
 
 	protected abstract String getPluginId();
 
 	protected abstract String[] getPossibleInterpreterNames();
 
 	protected abstract ILog getLog();
 
 	protected abstract File createPathFile() throws IOException;
 
 	protected String[] buildCommandLine(File installLocation, File pathFile) {
 		String path = installLocation.getAbsolutePath();
 		return new String[] { path, pathFile.getAbsolutePath() };
 	}
 
 	protected String getBuildPathDelimeter() {
 		return " ";
 	}
 
 	protected String[] parsePaths(String result) {
 		String res = result;
 		if (res.startsWith(DLTK_PATH_PREFIX)) {
 			res = res.substring(DLTK_PATH_PREFIX.length());
 		}
 		String[] paths = res.split(getBuildPathDelimeter());
 		List filtered = new ArrayList();
 		for (int i = 0; i < paths.length; ++i) {
 			if (!paths[i].equals(".")) {
 				filtered.add(paths[i].trim());
 			}
 		}
 
 		return (String[]) filtered.toArray(new String[filtered.size()]);
 
 	}
 
 	/**
 	 * Then multiple lines of output are provided, we parse only paths started
 	 * from "DLTK:" sequence.
 	 * 
 	 * @param result
 	 * @return
 	 */
 	protected String[] parsePaths(String[] result) {
 		List filtered = new ArrayList();
 		for (int k = 0; k < result.length; ++k) {
 			String res = result[k];
 			if (res.startsWith(DLTK_PATH_PREFIX)) {
 				res = res.substring(DLTK_PATH_PREFIX.length());
 
 				String[] paths = parsePaths(res);
 				for (int i = 0; i < paths.length; ++i) {
 					if (!paths[i].equals(".")) {
 						filtered.add(paths[i].trim());
 					}
 				}
 			}
 		}
 		return (String[]) filtered.toArray(new String[filtered.size()]);
 	}
 
 	public IStatus validateInstallLocation(File installLocation) {
 		if (!installLocation.exists() || !installLocation.isFile()
 				|| installLocation.isHidden()) {
 			return createStatus(IStatus.ERROR,
 					InterpreterMessages.errNonExistentOrInvalidInstallLocation,
 					null);
 		}
 
 		String possibleNames[] = getPossibleInterpreterNames();
 
 		boolean matchFound = false;
 		final String name = installLocation.getName();
 
 		// name.matches(possibleName + ".*\\.exe")
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			for (int i = 0; i < possibleNames.length; ++i) {
 				final String possibleName = possibleNames[i];
 				if (name.equals(possibleName + ".exe")
 						|| name.equals(possibleName + ".bat")) {
 					matchFound = true;
 					break;
 				}
 			}
 		} else {
 			for (int i = 0; i < possibleNames.length; i++) {
 				final String possibleName = possibleNames[i];
 				if (name.indexOf(possibleName) == 0) {
 					matchFound = true;
 					break;
 				}
 			}
 		}
 
 		if (matchFound) {
 			return createStatus(IStatus.OK, "", null);
 		} else {
 			return createStatus(IStatus.ERROR,
 					InterpreterMessages.errNoInterpreterExecutablesFound, null);
 		}
 	}
 
 	protected void retrivePaths(final File installLocation,
 			final List locations, IProgressMonitor monitor, File pathFile) {
 		Process process = null;
 		try {
 			monitor.beginTask(InterpreterMessages.statusFetchingLibs, 1);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			String[] cmdLine;
 			String[] env = extractEnvironment();
 
 			cmdLine = buildCommandLine(installLocation, pathFile);
 			try {
 				process = DebugPlugin.exec(cmdLine, null, env);
 				if (process != null) {
 					String result[] = readPathsFromProcess(monitor, process);
 					if (result == null) {
 						throw new IOException("null result from process");
 					}
 					String[] paths = null;
 					if (result.length == 1) {
 						paths = parsePaths(result[0]);
 					} else {
 						paths = parsePaths(result);
 					}
 
 					IPath path = new Path(pathFile.getCanonicalPath())
 							.removeLastSegments(1);
 
 					fillLocationsExceptOne(locations, paths, path);
 				}
 			} catch (CoreException e) {
 				// TODO: handle
 			}
 
 		} catch (IOException e) {
 			if (DLTKCore.VERBOSE) {
 				getLog().log(
 						createStatus(IStatus.ERROR,
 								"Unable to lookup library paths", e));
 			}
 		} finally {
 			if (process != null) {
 				process.destroy();
 			}
 			monitor.done();
 		}
 	}
 
 	protected IRunnableWithProgress createLookupRunnable(
 			final File installLocation, final List locations) {
 		return new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) {
 				try {
 					retrivePaths(installLocation, locations, monitor,
 							createPathFile());
 				} catch (IOException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 	}
 
 	public synchronized LibraryLocation[] getDefaultLibraryLocations(
 			final File installLocation) {
 		if (fCachedLocations.containsKey(installLocation)) {
 			return (LibraryLocation[]) fCachedLocations.get(installLocation);
 		}
 
 		final ArrayList locations = new ArrayList();
 
 		final IRunnableWithProgress runnable = createLookupRunnable(
 				installLocation, locations);
 
 		try {
 			runLibraryLookup(runnable);
 		} catch (InvocationTargetException e) {
 			getLog().log(
 					createStatus(IStatus.ERROR,
 							"Error to get default libraries:", e));
 		} catch (InterruptedException e) {
 			getLog().log(
 					createStatus(IStatus.ERROR,
 							"Error to get default libraries:", e));
 		}
 
 		LibraryLocation[] libs = correctLocations(locations);
 		if (libs.length != 0) {
 			fCachedLocations.put(installLocation, libs);
 		}
 
 		return libs;
 	}
 
 	protected IStatus createStatus(int severity, String message,
 			Throwable throwable) {
 		return new Status(severity, getPluginId(), 0, message, throwable);
 	}
 }
