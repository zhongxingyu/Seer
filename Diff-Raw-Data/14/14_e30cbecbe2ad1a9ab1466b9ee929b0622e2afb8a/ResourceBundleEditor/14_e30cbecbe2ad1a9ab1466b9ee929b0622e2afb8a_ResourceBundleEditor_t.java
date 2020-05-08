 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.i18n.resourcebundle.editors;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.internal.resources.Folder;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.MultiPageEditorPart;
 
 import com.essiembre.eclipse.i18n.resourcebundle.ResourceBundlePlugin;
 
 /**
  * Multi-page editor for editing resource bundles.
  */
 public class ResourceBundleEditor extends MultiPageEditorPart {
     
     /** All bundles. */
     private Bundles bundles;
     
     /** List box containing all keys. */
     private org.eclipse.swt.widgets.List bundleKeys;
 
     /** Internationalization data-capture page. */
     private I18NPage i18nPage;
    /** New locale page. */
    private NewLocalePage newLocalePage;
     
     /** File path, up to file name, excluding language, country and extension */
     private String bundlePath;
     /** File name, excluding language, country and extension */
     private String bundleName;
 
     /** Bundle image. */
     private static Image bundleImage = 
             BundleUtils.loadImage("icons/resourcebundle.gif");
     /** Property image. */
     private static Image propertyImage = 
             BundleUtils.loadImage("icons/propertiesfile.gif");
 
     /** Class name of Properties file editor (Eclipse 3.1). */
     private static final String PROPERTIES_EDITOR_CLASS_NAME = 
             "org.eclipse.jdt.internal.ui.propertiesfileeditor."
           + "PropertiesFileEditor";
     
     
     /**
      * Creates a multi-page editor example.
      */
     public ResourceBundleEditor() {
         super();
     }
     
     /**
      * Creates the pages of the multi-page editor.
      */
     protected void createPages() {
         // Create I18N page
         i18nPage = new I18NPage(
                 getContainer(), SWT.H_SCROLL | SWT.V_SCROLL, bundles);
         int index = addPage(i18nPage);
         setPageText(index, ResourceBundlePlugin.getResourceString(
                 "editor.properties"));
         setPageImage(index, bundleImage);
         
         // Create text editor pages for each locales
         try {
             for (int i = 0; i <  bundles.count(); i++) {
                 Bundle bundle = bundles.getBundle(i);
                 TextEditor editor = bundle.getEditor();
                 index = addPage(editor, editor.getEditorInput());
                 setPageText(index, bundle.getTitle());
                 setPageImage(index, propertyImage);
             }
            
            // Add "new locale" page
            newLocalePage = new NewLocalePage(
                    getContainer(), bundlePath, bundleName);
            index = addPage(newLocalePage);
            setPageText(index, "New...");
            setPageImage(index, BundleUtils.loadImage(
                    "icons/newpropertiesfile.gif"));
         } catch (PartInitException e) {
             ErrorDialog.openError(
                     getSite().getShell(), "Error creating nested text editor",
                     null, e.getStatus());
         }
     }
 
     /**
      * The <code>MultiPageEditorPart</code> implementation of this 
      * <code>IWorkbenchPart</code> method disposes all nested editors.
      * Subclasses may extend.
      */
     public void dispose() {
         super.dispose();
     }
     /**
      * Saves the multi-page editor's document.
      */
     public void doSave(IProgressMonitor monitor) {
         i18nPage.refreshEditorOnChanges();
         for (int i = 0; i <  bundles.count(); i++) {
             Bundle bundle = bundles.getBundle(i);
             TextEditor editor = bundle.getEditor();
             editor.doSave(monitor);
         }
     }
     
     /**
      * @see org.eclipse.ui.ISaveablePart#doSaveAs()
      */
     public void doSaveAs() {
         // Save As not allowed.
     }
     
     /**
      * The <code>MultiPageEditorExample</code> implementation of this method
      * checks that the input is an instance of <code>IFileEditorInput</code>.
      */
     public void init(IEditorSite site, IEditorInput editorInput)
         throws PartInitException {
         if (editorInput instanceof IFileEditorInput) {
             IFile file = ((IFileEditorInput) editorInput).getFile();
             initBundlePathAndName(file);
             setPartName(bundleName + "[...]." + file.getFileExtension());
             setContentDescription(ResourceBundlePlugin.getResourceString(
                     "editor.content.desc") + bundleName + "."); 
             setTitleImage(bundleImage);
             closeIfAreadyOpen(site);
             // Create editors
             bundles = new Bundles();
             try {
                 loadNLBundles(site, file);
             } catch (CoreException e) {
                 e.printStackTrace();
                 throw new PartInitException("Can't initialize editor.", e);
             }
             if (bundles.count() == 0) {
                 loadStandardBundles(site, file);
             }
             bundles.refreshData();
             super.init(site, editorInput);
         } else {
             throw new PartInitException(
                     "Invalid Input: Must be IFileEditorInput");
         }
     }
 
     /**
      * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     /**
      * Calculates the contents of page GUI page when it is activated.
      */
     protected void pageChange(int newPageIndex) {
         super.pageChange(newPageIndex);
         if (newPageIndex == 0) {
             i18nPage.refresh();
         }
     }
 
     /**
      * Load resource bundles the standard way, i.e., a set of *.properties 
      * files in the same directory, with a locale representation in the 
      * file names.
      * @param site IEditorSite instance
      * @param file IFile instance
      * @throws PartInitException problem loading bundles
      */
     private void loadStandardBundles(IEditorSite site, IFile file)
             throws PartInitException {
         IResource[] resources = null;
         try {
             resources = file.getParent().members();
         } catch (CoreException e) {
             throw new PartInitException(
                     "Can't initialize resource bundle editor.", e);
         }
         for (int i = 0; i < resources.length; i++) {
             IResource resource = resources[i];
             String regex = "^(" + bundleName + ")"
                     + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})"
                     + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\."
                     + file.getFileExtension() + ")$";
             String resourceName = resource.getName();
             if (resource instanceof IFile && resourceName.matches(regex)) {
                 
                 // Build local title
                 String localeText = resourceName.replaceFirst(regex, "$2");
                 StringTokenizer tokens = 
                     new StringTokenizer(localeText, "_");
                 List localeSections = new ArrayList();
                 while (tokens.hasMoreTokens()) {
                     localeSections.add(tokens.nextToken());
                 }
                 switch (localeSections.size()) {
                 case 0:
                     loadBundle(site, resource, null, null, null);
                     break;
                 case 1:
                     loadBundle(site, resource, 
                             (String) localeSections.get(0), null, null);
                     break;
                 case 2:
                     loadBundle(site, resource, 
                             (String) localeSections.get(0),
                             (String) localeSections.get(1), null);
                     break;
                 case 3:
                     loadBundle(site, resource, 
                             (String) localeSections.get(0),
                             (String) localeSections.get(1),
                             (String) localeSections.get(2));
                     break;
                 default:
                     break;
                 }
             }
         }
     }
     
     /**
      * Loads resource bundles laid out the recommended way when 
      * internationalizing Eclipse plugins.
      * @param site IEditorSite instance
      * @param file IFile instance
      * @throws PartInitException problem loading bundles
      * @throws CoreException problem loading bundles
      */
     private void loadNLBundles(IEditorSite site, IFile file)
             throws PartInitException, CoreException {
         String filename = file.getName();
         
         // Locate "nl" directory (if any)
         IContainer container = file.getParent();
         IResource nlDir = null;
         while (container != null 
                 && nlDir == null || !(nlDir instanceof Folder)) {
             nlDir = container.findMember("nl");
             container = container.getParent();
         }
         
         // Load properties files in "nl" structure.
         if (nlDir != null && nlDir instanceof Folder) {
             // Load root file, if exists.
             IResource resource = nlDir.getParent().findMember(filename);
             loadBundle(site, resource, null, null, null);
 
             // Load "language" matching files in "nl" tree.
             IResource[] langResources = ((Folder) nlDir).members();
             for (int i = 0; i < langResources.length; i++) {
                 String language = null;
                 IResource langResource = langResources[i];
                 if (langResource instanceof IFolder) {
                     IFolder langFolder = (IFolder) langResource;
                     language = langFolder.getName();
                     loadBundle(site, langFolder.findMember(filename),
                             language, null, null);
 
                     // Load "country" matching files in "nl" tree.
                     String country = null;
                     IResource[] cntryResources = langFolder.members();
                     for (int j = 0; j < cntryResources.length; j++) {
                         IResource cntryResource = cntryResources[j];
                         if (cntryResource instanceof IFolder) {
                             IFolder cntryFolder = (IFolder) cntryResource;
                             country = cntryFolder.getName();
                             loadBundle(site, cntryFolder.findMember(filename),
                                     language, country, null);
                             
                             // Load "variant" matching files in "nl" tree.
                             IResource[] vrntResources = cntryFolder.members();
                             for (int k = 0; k < vrntResources.length; k++) {
                                 IResource vrntResource = vrntResources[k];
                                 if (vrntResource instanceof IFolder) {
                                     IFolder vrntFolder = (IFolder) vrntResource;
                                     loadBundle(site, 
                                             vrntFolder.findMember(filename),
                                             language, country, 
                                             vrntFolder.getName());
                                 }
                             }
                         }
                     }                        
                 }
             }
         }
     }
 
     /**
      * Loads a resource bundle.
      * @param site IEditorSite instance
      * @param resource the IResource to load, provided it is a file.
      * @param lang ISO language code
      * @param country ISO country code
      * @param variant locale variant
      * @throws PartInitException problem loading bundles
      */
     private void loadBundle(IEditorSite site, IResource resource, 
             String lang, String country, String variant)
             throws PartInitException {
         if (resource != null && resource instanceof IFile) {
             Bundle bundle = new Bundle();
             Locale locale = null;
             if (lang != null && country != null && variant != null) {
                 locale = new Locale(lang, country, variant);
             } else if (lang != null && country != null) {
                 locale = new Locale(lang, country);
             } else if (lang != null) {
                 locale = new Locale(lang);
             }
             bundle.setLocale(locale);
             if (locale != null) {
                 bundle.setTitle(locale.getDisplayName());
             } else {
                 bundle.setTitle(ResourceBundlePlugin.getResourceString(
                         "editor.default"));
             }
             
             IEditorInput newEditorInput = 
                     new FileEditorInput((IFile) resource);
             TextEditor textEditor = null;
             try {
                 // Use PropertiesFileEditor if available
                 textEditor = (TextEditor) Class.forName(
                         PROPERTIES_EDITOR_CLASS_NAME).newInstance();
             } catch (Exception e) {
                 // Use default editor otherwise
                 textEditor = new TextEditor();
             }
             textEditor.init(site, newEditorInput);
             bundle.setEditor(textEditor);
             bundles.addBundle(bundle);
         }
     }
     
     /**
      * Sets the bundle path and file name for the selected file.  
      * @param file <code>IFile</code>
      */
     private void initBundlePathAndName(IFile file) {
         // Bundle name
         String name = file.getName();
        String regex = "^(.*?)"
                 + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})"
                 + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\."
                 + file.getFileExtension() + ")$";
         this.bundleName = name.replaceFirst(regex, "$1");
 
         // Bundle path
         this.bundlePath = file.getFullPath().removeLastSegments(1).append(
                 this.bundleName).toString();
     }
     
     private void closeIfAreadyOpen(IEditorSite site) {
         IWorkbenchPage[] pages = site.getWorkbenchWindow().getPages();
         for (int i = 0; i < pages.length; i++) {
             IWorkbenchPage page = pages[i];
             IEditorReference[] editors = page.getEditorReferences();
             for (int j = 0; j < editors.length; j++) {
                 IEditorPart editor = editors[j].getEditor(false);
                 if (editor instanceof ResourceBundleEditor
                         && ((ResourceBundleEditor) editor)
                                 .getBundlePath().equals(bundlePath)) {
                     page.closeEditor(editor, true);
                     //page.activate(editor);
                 }
             }
         }
     }
     
     /**
      * Gets the "bundleName" attribute.
      * @return Returns the bundleName.
      */
     public String getBundleName() {
         return bundleName;
     }
     /**
      * Gets the "bundlePath" attribute.
      * @return Returns the bundlePath.
      */
     public String getBundlePath() {
         return bundlePath;
     }
 }
