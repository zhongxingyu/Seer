 /*******************************************************************************
  * Copyright (c) 2012 Martin Reiterer.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Martin Reiterer - initial API and implementation
  ******************************************************************************/
 package org.eclipse.babel.tapiji.tools.core.ui.utils;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.babel.tapiji.tools.core.Logger;
 import org.eclipse.babel.tapiji.tools.core.ui.Activator;
 import org.eclipse.babel.tapiji.tools.core.ui.ResourceBundleManager;
 import org.eclipse.babel.tapiji.tools.core.ui.preferences.CheckItem;
 import org.eclipse.babel.tapiji.tools.core.ui.preferences.TapiJIPreferences;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 /**
  * 
  * @author mgasser
  * 
  */
 public class RBFileUtils extends Action {
     public static final String PROPERTIES_EXT = "properties";
 
     /**
      * Returns true if a file is a ResourceBundle-file
      */
     public static boolean isResourceBundleFile(IResource file) {
         boolean isValied = false;
 
         if (file != null && file instanceof IFile && !file.isDerived()
                 && file.getFileExtension() != null
                 && file.getFileExtension().equalsIgnoreCase("properties")) {
             isValied = true;
 
             // Check if file is not in the blacklist
             IPreferenceStore pref = null;
             if (Activator.getDefault() != null) {
                 pref = Activator.getDefault().getPreferenceStore();
             }
 
             if (pref != null) {
                 List<CheckItem> list = TapiJIPreferences
                         .getNonRbPatternAsList();
                 for (CheckItem item : list) {
                     if (item.getChecked()
                             && file.getFullPath().toString()
                                     .matches(item.getName())) {
                         isValied = false;
 
                         // if properties-file is not RB-file and has
                         // ResouceBundleMarker, deletes all ResouceBundleMarker
                         // of the file
                         if (org.eclipse.babel.tapiji.tools.core.util.RBFileUtils
                                 .hasResourceBundleMarker(file)) {
                             try {
                                 file.deleteMarkers(EditorUtils.RB_MARKER_ID,
                                         true, IResource.DEPTH_INFINITE);
                             } catch (CoreException e) {
                             }
                         }
                     }
                 }
             }
         }
 
         return isValied;
     }
 
     /**
      * @param container
      * @return Set with all ResourceBundles in this container
      */
     public static Set<String> getResourceBundleIds(IContainer container) {
         Set<String> resourcebundles = new HashSet<String>();
 
         try {
             for (IResource r : container.members()) {
                 if (r instanceof IFile) {
                     String resourcebundle = getCorrespondingResourceBundleId((IFile) r);
                     if (resourcebundle != null) {
                         resourcebundles.add(resourcebundle);
                     }
                 }
             }
         } catch (CoreException e) {/* resourcebundle.size()==0 */
         }
 
         return resourcebundles;
     }
 
     /**
      * 
      * @param file
      * @return ResourceBundle-name or null if no ResourceBundle contains the
      *         file
      */
     // TODO integrate in ResourceBundleManager
     public static String getCorrespondingResourceBundleId(IFile file) {
         ResourceBundleManager rbmanager = ResourceBundleManager.getManager(file
                 .getProject());
         String possibleRBId = null;
 
         if (isResourceBundleFile(file)) {
             possibleRBId = ResourceBundleManager.getResourceBundleId(file);
 
             for (String rbId : rbmanager.getResourceBundleIdentifiers()) {
                 if (possibleRBId.equals(rbId)) {
                     return possibleRBId;
                 }
             }
         }
         return null;
     }
 
     /**
      * Removes the properties-file of a given locale from a ResourceBundle, if
      * the ResourceBundle provides the locale.
      * 
      * @param project
      * @param rbId
      * @param locale
      */
     public static void removeFileFromResourceBundle(IProject project,
             String rbId, Locale locale) {
         ResourceBundleManager rbManager = ResourceBundleManager
                 .getManager(project);
 
         if (!rbManager.getProvidedLocales(rbId).contains(locale)) {
             return;
         }
 
         final IFile file = rbManager.getResourceBundleFile(rbId, locale);
        final String filename = file.getName();

         new Job("remove properties-file") {
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 try {
                     EditorUtils.deleteAuditMarkersForResource(file);
                     file.delete(true, monitor);
                 } catch (CoreException e) {
                     // MessageDialog.openError(Display.getCurrent().getActiveShell(),
                     // "Confirm", "File could not be deleted");
                     Logger.logError("File could not be deleted", e);
                 }
                 return Status.OK_STATUS;
             }
         }.schedule();
     }
 
     /**
      * Removes all properties-files of a given locale from all ResourceBundles
      * of a project.
      * 
      * @param rbManager
      * @param locale
      * @return
      */
    public static void removeLanguageFromProject(IProject project, Locale locale) {
        ResourceBundleManager rbManager = ResourceBundleManager
                 .getManager(project);
 
        for (String rbId : rbManager.getResourceBundleIdentifiers()) {
             removeFileFromResourceBundle(project, rbId, locale);
         }
 
     }
 
     /**
      * @return the locale of a given properties-file
      */
     public static Locale getLocale(IFile file) {
         String localeID = file.getName();
         localeID = localeID.substring(0,
                 localeID.length() - "properties".length() - 1);
         String baseBundleName = ResourceBundleManager
                 .getResourceBundleName(file);
 
         Locale locale;
         if (localeID.length() == baseBundleName.length()) {
             locale = null; // Default locale
         } else {
             localeID = localeID.substring(baseBundleName.length() + 1);
             String[] localeTokens = localeID.split("_");
             switch (localeTokens.length) {
             case 1:
                 locale = new Locale(localeTokens[0]);
                 break;
             case 2:
                 locale = new Locale(localeTokens[0], localeTokens[1]);
                 break;
             case 3:
                 locale = new Locale(localeTokens[0], localeTokens[1],
                         localeTokens[2]);
                 break;
             default:
                 locale = new Locale("");
                 break;
             }
         }
         return locale;
     }
 
     /**
      * @return number of ResourceBundles in the subtree
      */
     public static int countRecursiveResourceBundle(IContainer container) {
         return getSubResourceBundle(container).size();
     }
 
     private static List<String> getSubResourceBundle(IContainer container) {
         ResourceBundleManager rbmanager = ResourceBundleManager
                 .getManager(container.getProject());
 
         String conatinerId = container.getFullPath().toString();
         List<String> subResourceBundles = new ArrayList<String>();
 
         for (String rbId : rbmanager.getResourceBundleIdentifiers()) {
             for (IResource r : rbmanager.getResourceBundles(rbId)) {
                 if (r.getFullPath().toString().contains(conatinerId)
                         && (!subResourceBundles.contains(rbId))) {
                     subResourceBundles.add(rbId);
                 }
             }
         }
         return subResourceBundles;
     }
 
 }
