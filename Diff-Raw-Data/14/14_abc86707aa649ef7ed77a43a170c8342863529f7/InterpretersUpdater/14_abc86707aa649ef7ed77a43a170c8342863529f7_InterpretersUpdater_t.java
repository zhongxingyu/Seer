 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.launching.InterpreterDefinitionsContainer;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 
 
 /**
  * Processes add/removed/changed Interpreters.
  */
 public class InterpretersUpdater {
 	
 	// the Interpreters defined when this updated is instantiated
 	private InterpreterDefinitionsContainer fOriginalInterpreters;	
 	
 	/**
 	 * Contstructs a new Interpreter updater to update Interpreter install settings.
 	 */
 	public InterpretersUpdater() {
 		saveCurrentAsOriginal ();
 	}
 	
 	private void saveCurrentAsOriginal () {
 		fOriginalInterpreters = new InterpreterDefinitionsContainer();
 		IInterpreterInstall def = null;
 		
		String[] natures = ScriptRuntime.getInterpreterNatures();
 		for (int i = 0; i < natures.length; i++) {
 			def = ScriptRuntime.getDefaultInterpreterInstall(natures[i]);
 			
 			if (def != null) {
 				fOriginalInterpreters.setDefaultInterpreterInstallCompositeID(natures[i], ScriptRuntime.getCompositeIdFromInterpreter(def));
 			}
 		}
 		
 	
 		IInterpreterInstallType[] types = ScriptRuntime.getInterpreterInstallTypes();
 		for (int i = 0; i < types.length; i++) {
 			IInterpreterInstall[] Interpreters = types[i].getInterpreterInstalls();
 			if (Interpreters != null)
 				for (int j = 0; j < Interpreters.length; j++) {
 					fOriginalInterpreters.addInterpreter(Interpreters[j]);
 				}
 		}
 	}
 	
 	/**
 	 * Updates Interpreter settings and returns whether the update was successful.
 	 * 
 	 * @param InterpreterEnvironments new installed InterpreterEnvironments
 	 * @param defaultInterp new default Interpreter
 	 * @return whether the update was successful
 	 */
 	public boolean updateInterpreterSettings(String nature, IInterpreterInstall[] InterpreterEnvironments, IInterpreterInstall defaultInterp) {
 		
 		// Create a Interpreter definition container
 		InterpreterDefinitionsContainer InterpreterContainer = new InterpreterDefinitionsContainer();
 		
 		// Set the default Interpreter Id on the container
 		if (defaultInterp != null) {
 			String defaultInterpId = ScriptRuntime.getCompositeIdFromInterpreter(defaultInterp);
 			InterpreterContainer.setDefaultInterpreterInstallCompositeID(nature, defaultInterpId);
 		} else {
 			InterpreterContainer.setDefaultInterpreterInstallCompositeID(nature, null);
 		}
 		
 		
 		// Set the Interpreters on the container
 		for (int i = 0; i < InterpreterEnvironments.length; i++) {
 			InterpreterContainer.addInterpreter(InterpreterEnvironments[i]);
 		}
 		
 		//get old other lang interpreters
 		List oldList = fOriginalInterpreters.getInterpreterList();
 		String[] natures = fOriginalInterpreters.getInterpreterNatures();
 		for (Iterator iter = oldList.iterator(); iter.hasNext();) {
 			IInterpreterInstall ii = (IInterpreterInstall) iter.next();
 			if (!ii.getInterpreterInstallType().getNatureId().equals(nature)) {
 				InterpreterContainer.addInterpreter(ii);
 			}
 		}
 		for (int i = 0; i < natures.length; i++) {
 			if (!nature.equals(natures[i])) {
 				String sid = fOriginalInterpreters.getDefaultInterpreterInstallCompositeID(natures[i]);
 				InterpreterContainer.setDefaultInterpreterInstallCompositeID(natures[i], sid);				
 			}
 		}
 		
 		
 		// Generate XML for the Interpreter defs and save it as the new value of the Interpreter preference
 		saveInterpreterDefinitions(InterpreterContainer);
 		
 		saveCurrentAsOriginal ();
 		
 		return true;
 	}
 	
 	private void saveInterpreterDefinitions(final InterpreterDefinitionsContainer container) {
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				try {
 					monitor.beginTask(InterpretersMessages.InterpretersUpdater_0, 100); 
 					String InterpreterDefXML = container.getAsXML();
 					monitor.worked(40);
 					ScriptRuntime.getPreferences().setValue(ScriptRuntime.PREF_INTERPRETER_XML, InterpreterDefXML);
 					monitor.worked(30);
 					ScriptRuntime.savePreferences();
 					monitor.worked(30);
 				} catch (IOException ioe) {
 					DLTKDebugUIPlugin.log(ioe);
 				} catch (ParserConfigurationException e) {
 					DLTKDebugUIPlugin.log(e);
 				} catch (TransformerException e) {
 					DLTKDebugUIPlugin.log(e);
 				} finally {
 					monitor.done();
 				}
 				
 			}
 		};
 		try {
 			DLTKDebugUIPlugin.getDefault().getWorkbench().getProgressService().busyCursorWhile(runnable);			
 		} catch (InvocationTargetException e) {
 			DLTKDebugUIPlugin.log(e);
 		} catch (InterruptedException e) {
 			DLTKDebugUIPlugin.log(e);
 		} 
 	}
 }
