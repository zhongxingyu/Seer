 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.gda.client.experimentdefinition;
 
 import gda.configuration.properties.LocalProperties;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.beans.validation.AbstractValidator;
 import uk.ac.gda.client.experimentdefinition.components.ExperimentObjectListener;
 
 /**
  * Provides an interface to the implementation specific classes to be used in the experimentdefinition plugin as defined
  * in the uk.ac.gda.client.experimentdefinition extension point.
  */
 public class ExperimentFactory {
 
 	// the name of the class used to manager the
 	private static final String UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION = "uk.ac.gda.client.experimentdefinition";
 	// the names of the elements in the uk.ac.gda.client.experimentdefinition extension point
 	public static final String EXPERIMENT_OBJECT_ELEMENT_NAME = "experimentobjectmanager";
 	// public static final String EXPERIMENT_ELEMENT_NAME = "experimentcontroller";
 	public static final String VALIDATOR_ELEMENT_NAME = "validator";
 	static String templatesFolder = null;
 
 	private final static Logger logger = LoggerFactory.getLogger(ExperimentFactory.class);
 
 	private static List<IExperimentObjectManager> managers;
 	private static IExperimentEditorManager theExptEditorManager = null;
 	private static AbstractValidator theValidator = null;
 
 	/**
 	 * Add an ExperimentObjectManager to a cache of available managers. Each one represents a group of scans.
 	 * 
 	 * @param runObjectManager
 	 */
 	public static void addManager(IExperimentObjectManager runObjectManager) {
 		if (managers == null)
 			managers = new ArrayList<IExperimentObjectManager>(3);
 		managers.add(runObjectManager);
 	}
 
 	public static void emptyManagers() {
 		managers.removeAll(managers);
 		managers = null;
 	}
 
 	/**
 	 * Adds an observer for a specific experiment (run).
 	 * 
 	 * @param targetFolder
 	 * @param l
 	 * @throws Exception
 	 */
 	public static void addRunObjectListener(final IFolder targetFolder, ExperimentObjectListener l) throws Exception {
 		final List<IExperimentObjectManager> mans = getRunManagers(targetFolder);
 		if (mans != null) {
 			for (IExperimentObjectManager man : mans) {
 				man.addExperimentObjectListener(l);
 			}
 		}
 	}
 
 	// /**
 	// * Validates all the xml in the given folder
 	// *
 	// * @param targetFolder
 	// * @throws Exception
 	// */
 	// public static void checkFolder(IContainer targetFolder) throws Exception {
 	// final List<IExperimentObjectManager> mans = getRunManagers(targetFolder);
 	// if (mans != null) {
 	// for (IExperimentObjectManager man : mans) {
 	// man.checkError();
 	// }
 	// }
 	// }
 
 	/**
 	 * Create a new multiscan in the given location.
 	 * 
 	 * @param file
 	 * @return the manager object of the new multiscan
 	 * @throws Exception
 	 */
 	public static IExperimentObjectManager createExperimentObjectMananger(IFile file) throws Exception {
 
 		IExperimentObjectManager man = (IExperimentObjectManager) Class.forName(getExperimentObjectManagerClass())
 				.getConstructors()[0].newInstance((Object[]) null);
 
 		man.load(file);
 
 		return man;
 	}
 
 	/**
 	 * Delete the manager file and any .xml files in the same multiscan that are no longer referenced by other
 	 * multiscans.
 	 * 
 	 * @param file
 	 * @throws Exception
 	 */
 	public static void deleteManager(final IFile file) throws Exception {
 		final IExperimentObjectManager man = getManager(file);
 		if (man == null) {
 			file.delete(true, null);
 			return;
 		}
 
 		final List<IExperimentObjectManager> mans = getRunManagers(file.getParent());
 		if (mans == null || mans.size() < 2) { // If there are none or ours is
 												// the only one.
 			file.delete(true, null);
 			return;
 		}
 
 		mans.remove(man);
 
 		try {
 			final List<IExperimentObject> runs = man.getExperimentList();
 			for (IExperimentObject ro : runs) {
 
 				for (IFile ifile : ro.getFiles()) {
 					if (!isFileReferenced(ifile, mans))
 						ifile.delete(true, null);
 				}
 			}
 		} finally {
 			file.delete(true, null);
 		}
 	}
 
 	public static void fireRunObjectListeners(IContainer runFolder) throws Exception {
 		final List<IExperimentObjectManager> mans = getRunManagers(runFolder);
 		if (mans != null) {
 			for (IExperimentObjectManager man : mans) {
 				man.fireExperimentObjectListeners();
 			}
 		}
 	}
 
 	/**
 	 * @return the singleton instance of the class which organises the visible editors in the experiment perspective
 	 */
 	public static IExperimentEditorManager getExperimentEditorManager() {
 
 		if (theExptEditorManager == null) {
 			try {
 				IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
 						UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION);
 				for (IConfigurationElement element : config) {
 					if (element.getName().equals("editormanager")) {
 						theExptEditorManager = (IExperimentEditorManager) element.createExecutableExtension("class");
 						break;
 					}
 				}
 
 				// use default if still not defined
 				if (theExptEditorManager == null) {
 					theExptEditorManager = new ExperimentEditorManager();
 				}
 
 			} catch (CoreException e) {
 				logger.error("Exception getting ExperimentEditorManager: " + e.getMessage(), e);
 			}
 		}
 
 		return theExptEditorManager;
 	}
 
 	public static String getExperimentObjectManagerClass() throws Exception {
 
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
 				UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION);
 		for (IConfigurationElement element : config) {
 			if (element.getName().equals(EXPERIMENT_OBJECT_ELEMENT_NAME)) {
 				return element.getAttribute("class");
 			}
 		}
 		throw new Exception("No extension point found for ExperimentObjectManagerClass ("
 				+ UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION + ")");
 	}
 
 	/**
 	 * The IExperimentObjectManager exists once for any .scan file in the workspace. If files that the manager
 	 * references become stale then the old manager is deleted and a new one created. The Factory has all the RunObjects
 	 * in the the file as a collection and can rewrite the file by calling the write() method.
 	 * 
 	 * @param file
 	 * @return IExperimentObjectManager
 	 * @throws Exception
 	 */
 	public static IExperimentObjectManager getManager(final IFile file) throws Exception {
 
 		if (!file.getName().endsWith(".scan"))
 			return null;
 
 		if (managers == null)
 			managers = new ArrayList<IExperimentObjectManager>(3);
 
 		if (!file.exists()) {
 			file.getLocation().toFile().createNewFile();
 			file.create(null, true, null);
 			return createExperimentObjectMananger(file);
 		}
 		for (Iterator<IExperimentObjectManager> it = managers.iterator(); it.hasNext();) {
 			final IExperimentObjectManager man = it.next();
 			if (man.getFile().equals(file))
 				return man;
 			if (!man.getFile().exists())
 				it.remove();
 		}
 		return createExperimentObjectMananger(file);
 	}
 
 	protected static IExperimentObjectManager getManager(final IFolder folder, String multiScanName) {
 		IFile scanFile = folder.getFile(multiScanName + ".scan");
 		try {
 			return getManager(scanFile);
 		} catch (Exception e) {
 			logger.error("Exception trying to find the multiscan (ExperimentObjectManager) for " + multiScanName
 					+ " in folder " + folder.getName(), e);
 			return null;
 		}
 	}
 
 	public static IExperimentObjectManager getManager(IExperimentObject ob) {
 		return getManager(ob.getFolder(), ob.getMultiScanName());
 	}
 
 	/**
 	 * @param dir
 	 * @param fileName
 	 * @return all the multiscan managers in the given folder which reference the given file
 	 * @throws Exception
 	 */
 	public static List<IExperimentObjectManager> getReferencedManagers(final IContainer dir, final String fileName)
 			throws Exception {
 
 		final List<IExperimentObjectManager> all = getRunManagers(dir);
 		if (all == null || all.isEmpty())
 			return null;
 
 		final List<IExperimentObjectManager> ret = new ArrayList<IExperimentObjectManager>(all.size());
 		for (IExperimentObjectManager man : all) {
 			if (man.isFileNameUsed(fileName))
 				ret.add(man);
 		}
 		return ret;
 	}
 
 	/**
 	 * @param containingFolder
 	 * @return all the multiscan managers in the given folder
 	 * @throws Exception
 	 */
 	public static List<IExperimentObjectManager> getRunManagers(final IContainer containingFolder) throws Exception {
 
 		if (!containingFolder.exists())
 			return null;
 		final IResource[] fa = containingFolder.members();
 		if (fa == null)
 			return null;
 
 		final List<IExperimentObjectManager> ret = new ArrayList<IExperimentObjectManager>(7);
 		for (int i = 0; i < fa.length; i++) {
 			if (!(fa[i] instanceof IFile))
 				continue;
 			if (fa[i].getName().endsWith(".scan")) {
 				ret.add(ExperimentFactory.getManager((IFile) fa[i]));
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * @return the ID of the class which provides the validation of the XML files.
 	 */
 	public static AbstractValidator getValidator() {
 
 		if (theValidator == null) {
 
 			try {
 				IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
 						UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION);
 				for (IConfigurationElement element : config) {
 					if (element.getName().equals(VALIDATOR_ELEMENT_NAME)) {
 						theValidator = (AbstractValidator) element.createExecutableExtension("class");
 					}
 				}
 			} catch (Exception e) {
 				logger.error(
 						"Could not create a Validator object - XML files will not be validated before running scans!",
 						e);
 			}
 		}
 		return theValidator;
 	}
 
 	private static boolean isFileReferenced(IFile xmlFile, List<IExperimentObjectManager> mans) {
 		for (IExperimentObjectManager man : mans) {
 			final List<IExperimentObject> runs = man.getExperimentList();
 			for (IExperimentObject ro : runs)
 				if (ro.isFileUsed(xmlFile))
 					return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Rename the file and inform its object manager and the editor manager
 	 * 
 	 * @param orig
 	 * @param name
 	 * @throws Exception
 	 */
 	public static void refactorFile(IFile orig, String name) throws Exception {
 
 		final String origName = orig.getName();
 
 		final IFile nameFile = ((IFolder) orig.getParent()).getFile(name);
 		if (nameFile.exists())
 			throw new Exception("'" + name + "' already exists in '" + nameFile.getParent() + "'.");
 
 		final String origExt = orig.getName().substring(orig.getName().indexOf('.'));
 		final String nameExt = name.substring(name.indexOf('.'));
 		if (!origExt.equals(nameExt))
 			throw new Exception("'" + name + "' has a different file extension to '" + orig.getName()
 					+ "'.\n\nThe extension must be '" + origExt + "'.");
 
 		final IExperimentObjectManager man = getManager(orig);
 		if (man != null) {
 			orig.move(nameFile.getFullPath(), true, null);
 			man.setFile(nameFile);
 			ExperimentFactory.getExperimentEditorManager().notifyFileNameChange(origName, nameFile);
 			return;
 		}
 
 		try {
 
 			final IFolder folder = (IFolder) orig.getParent();
 			orig.move(nameFile.getFullPath(), true, null);
 
 			final List<IExperimentObjectManager> mans = getRunManagers(folder);
 			if (mans != null)
 				for (IExperimentObjectManager m : mans) {
 					final List<IExperimentObject> runs = m.getExperimentList();
 					for (IExperimentObject ob : runs) {
 						ob.renameFile(orig.getName(), name);
 					}
 					// m.checkError(); // Might have fixed an error.
 					m.write();
 				}
 		} finally {
 			ExperimentFactory.getExperimentEditorManager().notifyFileNameChange(origName, nameFile);
 		}
 	}
 
 	public static void removeRunObjectListener(final IFolder targetFolder, ExperimentObjectListener l) throws Exception {
 		final List<IExperimentObjectManager> mans = getRunManagers(targetFolder);
 		if (mans != null)
 			for (IExperimentObjectManager man : mans)
 				man.removeExperimentObjectListener(l);
 	}
 
 	public static void removeRunObjectListener(ExperimentObjectListener l) {
 		if (managers == null)
 			return;
 		for (IExperimentObjectManager man : managers)
 			man.removeExperimentObjectListener(l);
 	}
 
 	/**
 	 * @return the name of the project in the RCP client workspace in which all the xml files are contained
 	 */
 	public static String getExperimentProjectName() {
 		String exafsName = LocalProperties.get("gda.client.experimentdefinition.project.name");
 		if (exafsName == null) {
 			exafsName = "experiment";
 		}
 		return exafsName;
 	}
 
 	public static String getTemplatesFolderPath() {
 
 		if (templatesFolder == null) {
 			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
 					ExperimentFactory.UK_AC_GDA_CLIENT_EXPERIMENTDEFINITION);
 			for (IConfigurationElement element : config) {
 				if (element.getName().equals("templatesLocation")) {
 					String valueInRegistry = element.getAttribute("folderName");
 					// use the LocalProperties variable parsing to pick up variables such as ${gda.var} etc.
					String passedValue = LocalProperties.get("gda.client.experimentdefinition.project.name", valueInRegistry);
 					templatesFolder = passedValue;
 					if (!templatesFolder.endsWith(File.separator)) {
 						templatesFolder += File.separator;
 					}
 					return templatesFolder;
 				}
 			}
 			templatesFolder = LocalProperties.getConfigDir() + File.separator + "templates" + File.separator;
 		}
 		return templatesFolder;
 	}
 }
