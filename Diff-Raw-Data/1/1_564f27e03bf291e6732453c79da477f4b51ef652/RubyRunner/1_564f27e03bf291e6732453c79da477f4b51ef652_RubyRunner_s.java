 /*******************************************************************************
  * Copyright (c) 2005, 2006 Eclipse Foundation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Bjorn Freeman-Benson - initial implementation
  *     Ward Cunningham - initial implementation
  *     José Fonseca - adapted for python
  *******************************************************************************/
 
 package org.eshell.rubymonkey;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.eclipsemonkey.EclipseMonkeyPlugin;
 import org.eclipse.eclipsemonkey.IMonkeyScriptRunner;
 import org.eclipse.eclipsemonkey.RunMonkeyException;
 import org.eclipse.eclipsemonkey.ScriptMetadata;
 import org.eclipse.eclipsemonkey.StoredScript;
 import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;
 import org.eclipse.eclipsemonkey.dom.Utilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.embed.LocalContextScope;
 import org.jruby.embed.ScriptingContainer;
 import org.osgi.framework.Bundle;
 
 /**
  *
  */
 public class RubyRunner implements IMonkeyScriptRunner {
 
 	IPath path;
 	IWorkbenchWindow window;
 	StoredScript storedScript;
 
 	static MessageConsole console;
 	static MessageConsoleStream consoleOutStream;
 	static MessageConsoleStream consoleErrStream;
 	
 
 	/**
 	 * 
 	 * @param path
 	 * @param window
 	 */
 	public RubyRunner(IPath path, IWorkbenchWindow window) {
 		this.path = path;
 		if(window == null) {
 			this.window =  PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		} else {
 			this.window = window;
 		}
 	}
 	
 	/**
 	 * @see org.eclipse.eclipsemonkey.IMonkeyScriptRunner#getStoredScript()
 	 */
 	public StoredScript getStoredScript() {
 		return storedScript;
 	}
 
 	/**
 	 * @see org.eclipse.eclipsemonkey.IMonkeyScriptRunner#run(java.lang.String, java.lang.Object[])
 	 */
 	public Object run(String entryName, Object[] functionArgs) 
 			throws RunMonkeyException {
 		Object result = null;
 		
 		try {
 			String fileName = this.path.toPortableString();
 			Map<String,StoredScript> scriptStore = EclipseMonkeyPlugin.getDefault().getScriptStore();
 
 			storedScript = (StoredScript) (scriptStore.get(fileName));
 			
 			if (!storedScript.metadata.ensure_doms_are_loaded(window)) {
 				return null;
 			}
 
 			defineDynamicVariables(path);
 
 			ScriptingContainer interp = null;
 			try {
 				interp = new ScriptingContainer(LocalContextScope.THREADSAFE);
 
 				ClassLoader loader = getClassLoader(); 
 				RubyInstanceConfig config = interp.getProvider().getRubyInstanceConfig();
 				config.setOutput(new PrintStream(getConsoleOutStream()));
 				config.setError(new PrintStream(getConsoleErrStream()));
 				config.setLoader(loader);
 				
 				List<String> loadPaths = new ArrayList<String>();
 				for (String path : scriptStore.keySet())
 				{
 				    if (!path.endsWith(".rb")) continue;
 				    path = new File(path).getParent().replace("\\", "\\\\");
 				    if (!loadPaths.contains(path)){ loadPaths.add(path); }
 				}
 				loadPaths.addAll(config.loadPaths());
 				config.setLoadPaths(loadPaths);
 				
 				interp.put("$loader", loader);
 		        Bundle[] bundles = RubyPlugin.getDefault().getContext().getBundles();
 		        Map<String, Bundle> allBundles = new HashMap<String, Bundle>();
 		        for(Bundle bundle : bundles) {
 		            allBundles.put(bundle.getSymbolicName(), bundle);
 		        }
 		        interp.put("$bundles", allBundles);
 				defineStandardGlobalVariables(interp);
 				defineExtensionGlobalVariables(interp, storedScript.metadata);
 				interp.runScriptlet(new FileInputStream(path.toFile()), path.toPortableString());
 			}
 			catch(Exception e)
 			{
 			    e.printStackTrace();
 			    error(e, this.path.toString(), e.toString());
 			}
 			finally {
 				undefineDynamicVariables(path);
 			}
 		}
 		catch (Exception x) {
 			error(x, this.path.toString(), x.toString());
 		}
 //		catch (IOException x)
 //		{
 //			error(x, this.path.toString(), x.toString());
 //		}
 		
 		return result;
 	}
 
     private ClassLoader getClassLoader() {
 //        postProperties.put("python.home",getPluginRootDir());
         
         RubyClassLoader classLoader = new RubyClassLoader(ScriptingContainer.class.getClassLoader());
         
 //        PySystemState state = new PySystemState();
 //        state.setClassLoader(classLoader);
 //        PySystemState.initialize(preProperties, postProperties, new String[0], classLoader);
         
 //        Py.setSystemState(state);
         return classLoader;
     }
 
 	private void defineStandardGlobalVariables(ScriptingContainer interp) {
 		interp.put("$window", window);
 		//interp.set("loadBundle", this.loadBundle)
 	}
 
 	private void defineExtensionGlobalVariables(ScriptingContainer interp,
 			ScriptMetadata metadata) throws IOException 
 	{
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint point = registry
 				.getExtensionPoint("org.eclipse.eclipsemonkey.dom");
 		if (point == null) return;
 		IExtension[] extensions = point.getExtensions();
 		for (IExtension extension : extensions) {
 			IConfigurationElement[] configurations = extension
 					.getConfigurationElements();
 			for (IConfigurationElement element : configurations) {
 				try {
 					IExtension declaring = element.getDeclaringExtension();
 //					String declaring_plugin_id = declaring
 //							.getDeclaringPluginDescriptor()
 //							.getUniqueIdentifier();
 					String declaring_plugin_id = declaring.getNamespaceIdentifier();
 					
 					if (metadata.containsDOM_by_plugin(declaring_plugin_id)) {
 						String variableName = element
 								.getAttribute("variableName");
 						Object object = element
 								.createExecutableExtension("class");
 						IMonkeyDOMFactory factory = (IMonkeyDOMFactory) object;
 						
 						Object rootObject = factory.getDOMroot();
 						
 						interp.put(variableName, rootObject);
 					}
 				} catch (InvalidRegistryObjectException x) {
 					// ignore bad extensions
 				} catch (CoreException x) {
 					// ignore bad extensions
 				}
 			}
 		}
 	}
 	
 	private void defineDynamicVariables(IPath path) {
 		Utilities.state().begin(path);
 		Utilities.state().set(Utilities.SCRIPT_NAME, path.toPortableString());
 	}
 
 	private void undefineDynamicVariables(IPath path) {
 		Utilities.state().end(path);
 	}
 
 	private void error(Exception x, String fileName, String string)
 			throws RunMonkeyException {
 
 		RunMonkeyException e = new RunMonkeyException(x.getClass().getName(), fileName, null,
 				string);
 
 		MessageConsoleStream cs = getConsoleErrStream();		
 		cs.println(e.toString());
 
 		throw e;
 	}
 	
 	/**
 	 * Returns a reference to the current console, initializing it if it's not created
 	 * 
 	 * @return A console stream
 	 */
 	public static MessageConsole getConsole() {
 		if (console == null) {
 			console = new MessageConsole("Eclipse Monkey Ruby Console", null);
 			consoleOutStream = console.newMessageStream();
 			consoleErrStream = console.newMessageStream();
 
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 				public void run() {
 					consoleOutStream.setColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK));
 					consoleErrStream.setColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
 				}
 			});
 
 			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
 		}
 
 		return console;
 	}
 
 	/**
 	 * Returns a reference to the current console, initializing it if it's not created
 	 * 
 	 * @return A console stream
 	 */
 	public static MessageConsoleStream getConsoleOutStream() {
 		getConsole();
 		return consoleOutStream;
 	}
 
 	/**
 	 * Returns a reference to the current console, initializing it if it's not created
 	 * 
 	 * @return A console stream
 	 */
 	public static MessageConsoleStream getConsoleErrStream() {
 		getConsole();
 		return consoleErrStream;
 	}
 }
