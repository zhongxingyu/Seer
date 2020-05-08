 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.gui.scriptcontroller.logging;
 
 import gda.factory.Findable;
 import gda.factory.Finder;
 import gda.jython.scriptcontroller.logging.ILoggingScriptController;
 import gda.jython.scriptcontroller.logging.ScriptControllerLogResultDetails;
 import gda.jython.scriptcontroller.logging.ScriptControllerLogResults;
 import gda.observable.IObserver;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Maps between the ScriptControllerLoggingMessage objects and the tree in ScriptControllerLogView.
  */
 public class ScriptControllerLogContentProvider implements ITreeContentProvider, IObserver {
 
 	private static final Logger logger = LoggerFactory.getLogger(ScriptControllerLogContentProvider.class);
 
 	private ILoggingScriptController[] controllers;
 	private String[] knownScripts = new String[] {};
 	private ScriptControllerLogResults[] results = null;
 	private HashMap<String, ILoggingScriptController> mapID2Controller = new HashMap<String, ILoggingScriptController>();
 	private final ScriptControllerLogView view;
 	private final Lock updateViewLock = new ReentrantLock();
 
 	public ScriptControllerLogContentProvider(ScriptControllerLogView view, String scriptControllerNames) {
 		super();
 		this.view = view;
 
 		String[] controllerNames = scriptControllerNames.split(",");
 
 		for (String name : controllerNames) {
 			Findable objRef = Finder.getInstance().find(name.trim());
 			if (objRef instanceof ILoggingScriptController) {
 				ILoggingScriptController newcontroller = (ILoggingScriptController) objRef;
 				controllers = (ILoggingScriptController[]) ArrayUtils.add(controllers, newcontroller);
 				try {
 					newcontroller.addIObserver(new ScriptControllerLogHelper(this));
 				} catch (Exception e) {
					logger.error("Error adding observer to LoggingScriptController", e);
 				}
 			} else {
 				logger.warn("ScriptControllerLogContentProvider could not find a LoggingScriptController called "
 						+ name + ". The ScriptControllerLogView view will not work");
 			}
 
 		}
 
 	}
 
 	@Override
 	public void dispose() {
 		// no resources to dispose of.
 	}
 
 	@Override
 	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		System.out.println("inputChanged, dont know why - This message needs improving so please understand this problem");
 	}
 
 	@Override
 	public Object[] getChildren(Object arg0) {
 		// only 1 layer in this tree
 		if (arg0 instanceof ScriptControllerLogResults) {
 			String id = ((ScriptControllerLogResults) arg0).getUniqueID();
 			ScriptControllerLogResultDetails details;
 			try {
 				ILoggingScriptController controller = mapID2Controller.get(id);
 				details = controller.getDetails(id);
 				if (details != null) {
 					return new Object[] { details };
 				}
 			} catch (Exception e) {
				logger.error("Error getting ILoggingScriptController or details from it", e);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Object[] getElements(Object arg0) {
 		// returns array of ScriptControllerLogResults
 		if (results == null) {
 			readAllResultsFromDatabases();
 		}
 		return results;
 	}
 
 	public void refresh() {
 		results = null;
 		readAllResultsFromDatabases();
 	}
 
 	private void readAllResultsFromDatabases() {
 		if (controllers == null) {
 			logger.error("No ILoggingScriptController configured in the product plugin_customization.ini file.\n"
 					+ "Talk to your Data Acq contact to add a uk.ac.gda.client/gda.loggingscriptcontrollers.to_observe entry");
 		}
 
 		for (ILoggingScriptController controller : controllers) {
 			ScriptControllerLogResults[] thisTable = controller.getTable();
 			for (ScriptControllerLogResults row : thisTable) {
 				mapID2Controller.put(row.getUniqueID(), controller);
 			}
 			results = (ScriptControllerLogResults[]) ArrayUtils.addAll(thisTable, results);
 		}
 
 		// recreate the filters
 		knownScripts = new String[] {};
 		for (ScriptControllerLogResults result : results) {
 			String scriptName = result.getScriptName();
 			if (!ArrayUtils.contains(knownScripts, scriptName)) {
 				knownScripts = (String[]) ArrayUtils.add(knownScripts, scriptName);
 				view.updateFilter(knownScripts);
 			}
 		}
 		view.updateFilter(knownScripts);
 
 		orderResultsByTime();
 	}
 
 	private void orderResultsByTime() {
 		try {
 			Arrays.sort(results);
 		} catch (Exception e) {
			logger.error("Error sorting results", e);
 		}
 	}
 
 	@Override
 	public Object getParent(Object arg0) {
 		if (arg0 instanceof ScriptControllerLogResultDetails) {
 			String id = ((ScriptControllerLogResultDetails) arg0).getUniqueID();
 			return getResultFor(id);
 		}
 		return null;
 	}
 
 	private ScriptControllerLogResults getResultFor(String uniqueID) {
 		if (results == null) {
 			readAllResultsFromDatabases();
 			if (results == null) {
 				return null;
 			}
 		}
 		for (ScriptControllerLogResults result : results) {
 			if (result.equals(uniqueID)) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean hasChildren(Object arg0) {
 		return (arg0 instanceof ScriptControllerLogResults);
 	}
 
 	@Override
 	public void update(final Object source, final Object arg) {
 
 		try {
 			if (arg instanceof ScriptControllerLogResults && updateViewLock.tryLock(500, TimeUnit.MILLISECONDS)) {
 				try {
 					addToKnowScripts(((ScriptControllerLogResults) arg).getScriptName());
 					if (haveSeenBefore((ScriptControllerLogResults) arg)) {
 						ScriptControllerLogResults temp = (ScriptControllerLogResults) arg;
 						if (!results[0].getUniqueID().equals(temp.getUniqueID())) {
 							return;
 						}
 						ScriptControllerLogResults newResults = new ScriptControllerLogResults(temp.getUniqueID(),
 								temp.getScriptName(), results[0].getStarted(), temp.getUpdated());
 						results[0] = newResults;
 						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								view.getTreeViewer().refresh();
 							}
 						});
 					} else {
 						ScriptControllerLogResults temp = (ScriptControllerLogResults) arg;
 						mapID2Controller.put(temp.getUniqueID(), (ILoggingScriptController) source);
 						results = (ScriptControllerLogResults[]) ArrayUtils.addAll(
 								new ScriptControllerLogResults[] { temp }, results);
 
 						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 
 								view.getTreeViewer()
 										.setInput(ScriptControllerLogContentProvider.this.getElements(null));
 								view.getTreeViewer().refresh();
 								view.getTreeViewer().collapseAll();
 								view.getTreeViewer().expandToLevel(arg, 1);
 								view.getTreeViewer().reveal(arg);
 								// view.getTreeViewer().setSelection(new StructuredSelection(arg), true);
 
 							}
 						});
 					}
 				} finally {
 					updateViewLock.unlock();
 				}
 
 			}
 		} catch (InterruptedException e) {
 			// ignore as this will only ultimately return to the server
 		}
 	}
 
 	private void addToKnowScripts(String scriptName) {
 		if (!ArrayUtils.contains(knownScripts, scriptName)) {
 			knownScripts = (String[]) ArrayUtils.add(knownScripts, scriptName);
 			view.updateFilter(knownScripts);
 		}
 	}
 
 	private boolean haveSeenBefore(ScriptControllerLogResults arg) {
 		for (ScriptControllerLogResults res : results) {
 			if (res.getUniqueID().equals(arg.getUniqueID())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
