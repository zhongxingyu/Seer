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
 package com.essiembre.eclipse.rbe.ui.editor;
 
 
 import java.util.Locale;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.MultiPageEditorPart;
 
 import com.essiembre.eclipse.rbe.RBEPlugin;
 import com.essiembre.eclipse.rbe.ui.UIUtils;
 import com.essiembre.eclipse.rbe.ui.editor.i18n.I18nPage;
 import com.essiembre.eclipse.rbe.ui.editor.locale.NewLocalePage;
 import com.essiembre.eclipse.rbe.ui.editor.resources.ResourceManager;
 import com.essiembre.eclipse.rbe.ui.editor.resources.SourceEditor;
 
 /**
  * Multi-page editor for editing resource bundles.
  */
 public class ResourceBundleEditor extends MultiPageEditorPart
         implements IGotoMarker {
 
     /** Editor ID, as defined in plugin.xml. */
     public static final String EDITOR_ID = 
        "com.essiembre.eclipse.rbe.ui.editor.ResourceBundleEditor"; //$NON-NLS-1$
     
     private ResourceManager resourceMediator;
     private I18nPage i18nPage;
     /** New locale page. */
     private NewLocalePage newLocalePage;
     
     /**
      * Creates a multi-page editor example.
      */
     public ResourceBundleEditor() {
         super();
     }
 
     /**
      * The <code>MultiPageEditorExample</code> implementation of this method
      * checks that the input is an instance of <code>IFileEditorInput</code>.
      */
     public void init(IEditorSite site, IEditorInput editorInput)
         throws PartInitException {
         if (editorInput instanceof IFileEditorInput) {
             IFile file = ((IFileEditorInput) editorInput).getFile();
             try {
                 resourceMediator = new ResourceManager(site, file);
             } catch (CoreException e) {
                 UIUtils.showErrorDialog(
                         getSite().getShell(), e, "error.init.ui"); //$NON-NLS-1$
             }
                         
             setPartName(resourceMediator.getEditorDisplayName());
             setContentDescription(
                     RBEPlugin.getString("editor.content.desc") //$NON-NLS-1$
                   + resourceMediator.getEditorDisplayName() + ".");//$NON-NLS-1$
             setTitleImage(UIUtils.getImage(UIUtils.IMAGE_RESOURCE_BUNDLE));
             closeIfAreadyOpen(site, file);
             super.init(site, editorInput);
         } else {
             throw new PartInitException(
                     "Invalid Input: Must be IFileEditorInput"); //$NON-NLS-1$
         }
     }
     
     /**
      * Creates the pages of the multi-page editor.
      */
     protected void createPages() {
         // Create I18N page
         i18nPage = new I18nPage(
                getContainer(), SWT.H_SCROLL | SWT.V_SCROLL, resourceMediator);
         int index = addPage(i18nPage);
         setPageText(index, RBEPlugin.getString(
                 "editor.properties")); //$NON-NLS-1$
         setPageImage(index, UIUtils.getImage(UIUtils.IMAGE_RESOURCE_BUNDLE));
         
         // Create text editor pages for each locales
         try {
             SourceEditor[] sourceEditors = resourceMediator.getSourceEditors();
             for (int i = 0; i < sourceEditors.length; i++) {
                 SourceEditor sourceEditor = sourceEditors[i];
                 index = addPage(
                         sourceEditor.getEditor(), 
                         sourceEditor.getEditor().getEditorInput());
                 setPageText(index, UIUtils.getDisplayName(
                         sourceEditor.getLocale()));
                 setPageImage(index, 
                         UIUtils.getImage(UIUtils.IMAGE_PROPERTIES_FILE));
             }
         } catch (PartInitException e) {
             ErrorDialog.openError(getSite().getShell(), 
                 "Error creating text editor page.", //$NON-NLS-1$
                 null, e.getStatus());
         }
         
         // Add "new locale" page
         newLocalePage = new NewLocalePage(getContainer(), resourceMediator);
         index = addPage(newLocalePage);
         setPageText(index, RBEPlugin.getString("editor.new.tab")); //$NON-NLS-1$
         setPageImage(
                 index, UIUtils.getImage(UIUtils.IMAGE_NEW_PROPERTIES_FILE));
     }
 
     /**
      * Saves the multi-page editor's document.
      */
     public void doSave(IProgressMonitor monitor) {
         i18nPage.refreshEditorOnChanges();
         resourceMediator.save(monitor);
     }
     
     /**
      * @see org.eclipse.ui.ISaveablePart#doSaveAs()
      */
     public void doSaveAs() {
         // Save As not allowed.
     }
     
     /**
      * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     /**
      * Change current page based on locale.  If there is no editors associated
      * with current locale, do nothing.
      * @param locale locale used to identify the page to change to
      */
     public void setActivePage(Locale locale) {
         SourceEditor[] editors = resourceMediator.getSourceEditors();
         int index = -1;
         for (int i = 0; i < editors.length; i++) {
             SourceEditor editor = editors[i];
             Locale editorLocale = editor.getLocale();
             if (editorLocale != null && editorLocale.equals(locale)
                     || editorLocale == null && locale == null) {
                 index = i;
                 break;
             }
         }
         if (index > -1) {
             setActivePage(index + 1);
         }
     }
 
     /**
      * @see org.eclipse.ui.ide.IGotoMarker#gotoMarker(
      *         org.eclipse.core.resources.IMarker)
      */
     public void gotoMarker(IMarker marker) {
         IPath markerPath = marker.getResource().getProjectRelativePath();
         SourceEditor[] sourceEditors = resourceMediator.getSourceEditors();
         for (int i = 0; i < sourceEditors.length; i++) {
             SourceEditor editor = sourceEditors[i];
             IPath editorPath = editor.getFile().getProjectRelativePath();
             if (markerPath.equals(editorPath)) {
                 setActivePage(editor.getLocale());
                 IDE.gotoMarker(editor.getEditor(), marker);
                 break;
             }
         }
     }
     
     /**
      * Calculates the contents of page GUI page when it is activated.
      */
     protected void pageChange(int newPageIndex) {
         super.pageChange(newPageIndex);
         if (newPageIndex == 0) {
             resourceMediator.reloadProperties();
             i18nPage.refreshTextBoxes();
         }
     }
 
     
     /**
      * Is the given file a member of this resource bundle.
      * @param file file to test
      * @return <code>true</code> if file is part of bundle
      */
     public boolean isBundleMember(IFile file) {
         return resourceMediator.isResource(file);
     }
 
     private void closeIfAreadyOpen(IEditorSite site, IFile file) {
         IWorkbenchPage[] pages = site.getWorkbenchWindow().getPages();
         for (int i = 0; i < pages.length; i++) {
             IWorkbenchPage page = pages[i];
             IEditorReference[] editors = page.getEditorReferences();
             for (int j = 0; j < editors.length; j++) {
                 IEditorPart editor = editors[j].getEditor(false);
                 if (editor instanceof ResourceBundleEditor) {
                     ResourceBundleEditor rbe = (ResourceBundleEditor) editor;
                     if (rbe.isBundleMember(file)) {
                         page.closeEditor(editor, true);
                     }
                 }
             }
         }
     }
 
     
     
     /**
      * @see org.eclipse.ui.IWorkbenchPart#dispose()
      */
     public void dispose() {
         i18nPage.dispose();
         newLocalePage.dispose();
         super.dispose();
     }
 }
