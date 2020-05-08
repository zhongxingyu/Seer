 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.editors;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.pde.core.IBaseModel;
 import org.eclipse.pde.core.IWritable;
 import org.eclipse.pde.core.plugin.IPluginModelBase;
 import org.eclipse.pde.internal.core.ICoreConstants;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.core.ibundle.IBundleModel;
 import org.eclipse.pde.internal.core.plugin.IWritableDelimiter;
 import org.eclipse.pde.internal.ui.IPreferenceConstants;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.editor.ISortableContentOutlinePage;
 import org.eclipse.pde.internal.ui.editor.JarEntryEditorInput;
 import org.eclipse.pde.internal.ui.editor.JarEntryFile;
 import org.eclipse.pde.internal.ui.editor.ModelDataTransfer;
 import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
 import org.eclipse.pde.internal.ui.editor.PDESourcePage;
 import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
 import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
 import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
 import org.eclipse.pde.internal.ui.editor.plugin.PluginInputContext;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.virgo.ide.eclipse.editors.AbstractPdeFormPage;
 import org.eclipse.virgo.ide.facet.core.FacetUtils;
 import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
 import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
 import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
 import org.eclipse.virgo.ide.ui.StatusHandler;
 import org.eclipse.virgo.ide.ui.editors.model.BundleModelUtility;
 import org.eclipse.virgo.kernel.repository.BundleDefinition;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * @author Christian Dupuis
  * @author Leo Dos Santos
  * @author Steffen Pingel
  */
 public class BundleManifestEditor extends ManifestEditor {
 
 	public static String ID_EDITOR = "org.eclipse.virgo.ide.ui.bundlemanifest";
 
 	protected boolean fEquinox = true;
 
 	protected boolean fShowExtensions = true;
 
 	protected IEclipsePreferences fPrefs;
 
 	protected List<IBundleManifestSaveListener> saveListeners = new ArrayList<IBundleManifestSaveListener>(3);
 
 	/**
 	 * Returns the list of pages for this editor. Public for testing.
 	 */
 	public IEditorPart[] getParts() {
 		ArrayList<IEditorPart> parts = new ArrayList<IEditorPart>(getPageCount());
 		if (pages != null) {
 			for (int i = 0; i < pages.size(); i++) {
 				Object page = pages.get(i);
 				if (page instanceof IEditorPart) {
 					parts.add((IEditorPart) page);
 				}
 			}
 		}
 		return parts.toArray(new IEditorPart[parts.size()]);
 	}
 
 	@Override
 	protected void addEditorPages() {
 		try {
 			BundleOverviewPage overviewPage = new BundleOverviewPage(this);
 			addPage(overviewPage);
 			saveListeners.add(overviewPage);
 
 			BundleDependenciesPage dependenciesPage = new BundleDependenciesPage(this);
 			addPage(dependenciesPage);
 			saveListeners.add(dependenciesPage);
 
 			BundleRuntimePage runtimePage = new BundleRuntimePage(this);
 			addPage(runtimePage);
 			saveListeners.add(runtimePage);
 		} catch (PartInitException e) {
 			StatusHandler.log(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, "Failed to create editor pages", e));
 		}
 		addSourcePage(BundleInputContext.CONTEXT_ID);
 	}
 
 	@Override
 	protected PDESourcePage createSourcePage(PDEFormEditor editor, String title, String name, String contextId) {
 		if (contextId.equals(BundleInputContext.CONTEXT_ID)) {
 			return new SpringBundleSourcePage(editor, title, name);
 		}
 		return super.createSourcePage(editor, title, name, contextId);
 	}
 
 	@Override
 	protected void createResourceContexts(InputContextManager manager, IFileEditorInput input) {
 		IFile file = input.getFile();
 		IContainer container = file.getParent();
 
 		IFile manifestFile = null;
 		String name = file.getName().toLowerCase(Locale.ENGLISH);
 		if (name.endsWith("mf")) { //$NON-NLS-1$
 			if (container instanceof IFolder) {
 				container = container.getParent();
 			}
 			manifestFile = file;
 		}
 		if (manifestFile != null && manifestFile.exists()) {
 			IEditorInput in = new FileEditorInput(manifestFile);
 			manager.putContext(in, new SpringBundleInputContext(this, in, file == manifestFile));
 		}
 		manager.monitorFile(manifestFile);
 
 		fPrefs = new ProjectScope(container.getProject()).getNode(PDECore.PLUGIN_ID);
 		if (fPrefs != null) {
 			fShowExtensions = fPrefs.getBoolean(ICoreConstants.EXTENSIONS_PROPERTY, true);
 			fEquinox = fPrefs.getBoolean(ICoreConstants.EQUINOX_PROPERTY, true);
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		super.doSave(monitor);
 		for (IBundleManifestSaveListener currListener : this.saveListeners) {
 			currListener.manifestSaved();
 		}
 	}
 
 	@Override
 	public boolean isEquinox() {
 		return fEquinox;
 	}
 
 	@Override
 	public void monitoredFileAdded(IFile file) {
 		if (fInputContextManager == null) {
 			return;
 		}
 		String name = file.getName();
 		if (name.equalsIgnoreCase(BundleModelUtility.F_MANIFEST)) {
 			if (!fInputContextManager.hasContext(BundleInputContext.CONTEXT_ID)) {
 				IEditorInput in = new FileEditorInput(file);
 				fInputContextManager.putContext(in, new SpringBundleInputContext(this, in, false));
 			}
 		}
 	}
 
 	/**
 	 * Copy of super method with work-around to maintain compatibility with Eclipse 3.4 and 3.5.
 	 * 
 	 * @see #createSystemFileContexts(InputContextManager, IEditorInput)
 	 */
	@Override
 	protected void createInputContexts(InputContextManager contextManager) {
 		IEditorInput input = getEditorInput();
 		if (input instanceof IFileEditorInput) {
 			// resource - find the project
 			createResourceContexts(contextManager, (IFileEditorInput) input);
 		} else if (PdeCompatibilityUtil.isSystemFileEditorInput(input)) {
 			// system file - find the file system folder
 			createSystemFileContexts(contextManager, input);
 		} else if (input instanceof IStorageEditorInput) {
 			createStorageContexts(contextManager, (IStorageEditorInput) input);
 		} else if (input instanceof IURIEditorInput) {
 			IURIEditorInput uriEditorInput = (IURIEditorInput) input;
 			try {
 				IFileStore store = EFS.getStore(uriEditorInput.getURI());
 				if (!EFS.SCHEME_FILE.equals(store.getFileSystem().getScheme())) {
 					return;
 				}
 				IEditorInput sinput = PdeCompatibilityUtil.createSystemFileEditorInput(uriEditorInput);
 				if (sinput == null) {
 					// Eclipse 3.5 or later
 					sinput = new FileStoreEditorInput(store);
 				}
 				createSystemFileContexts(contextManager, sinput);
 			}
 
 			catch (CoreException e) {
 				return;
 			}
 		}
 	}
 
 	/**
 	 * The signature of createSystemFileContexts() has changed between Eclipse 3.4 and 3.5. This method serves as a
 	 * work-around with a more generic signature.
 	 * 
 	 * @param input
 	 *            either a SystemFileEditorInput or FileStoreEditorInput
 	 */
 	protected void createSystemFileContexts(InputContextManager manager, IEditorInput input) {
 		File file = (File) input.getAdapter(File.class);
 		if (file == null && input instanceof FileStoreEditorInput) {
 			file = new File(((IURIEditorInput) input).getURI());
 		}
 		if (file == null) {
 			return;
 		}
 		File manifestFile = null;
 		String name = file.getName().toLowerCase(Locale.ENGLISH);
		if (name.startsWith("manifest.mf")) { //$NON-NLS-1$
 			manifestFile = file;
 		}
 		try {
 			if (manifestFile != null && manifestFile.exists()) {
 				IEditorInput in = PdeCompatibilityUtil.createSystemFileEditorInput(manifestFile);
 				if (in == null) {
 					// Eclipse 3.5 or later
 					IFileStore store = EFS.getStore(manifestFile.toURI());
 					in = new FileStoreEditorInput(store);
 				}
 				manager.putContext(in, new SpringBundleInputContext(this, in, file == manifestFile));
 			}
 		} catch (CoreException e) {
 			PDEPlugin.logException(e);
 		}
 	}
 
 	@Override
 	protected void createStorageContexts(InputContextManager manager, IStorageEditorInput input) {
 		if (input instanceof JarEntryEditorInput) {
 			createJarEntryContexts(manager, (JarEntryEditorInput) input);
 			return;
 		}
 
 		String name = input.getName().toLowerCase(Locale.ENGLISH);
		if (name.startsWith("manifest.mf")) { //$NON-NLS-1$
 			manager.putContext(input, new SpringBundleInputContext(this, input, true));
 		}
 	}
 
 	@Override
 	protected void createJarEntryContexts(InputContextManager manager, JarEntryEditorInput input) {
 		IStorage storage = input.getStorage();
 		ZipFile zip = (ZipFile) storage.getAdapter(ZipFile.class);
 		try {
 			if (zip == null) {
 				return;
 			}
 
 			if (zip.getEntry("META-INF/MANIFEST.MF") != null) { //$NON-NLS-1$
 				input = new JarEntryEditorInput(new JarEntryFile(zip, "META-INF/MANIFEST.MF")); //$NON-NLS-1$
 				manager.putContext(
 						input,
 						new SpringBundleInputContext(this, input, storage.getName().equals(
 								BundleModelUtility.F_MANIFEST)));
 			}
 		} finally {
 			try {
 				if (zip != null) {
 					zip.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	@Override
 	protected void setShowExtensions(boolean show) throws BackingStoreException {
 		if (fPrefs != null) {
 			fPrefs.putBoolean(ICoreConstants.EXTENSIONS_PROPERTY, show);
 			fPrefs.flush();
 		}
 		fShowExtensions = show;
 	}
 
 	@Override
 	public boolean showExtensionTabs() {
 		if (fInputContextManager.hasContext(PluginInputContext.CONTEXT_ID)) {
 			return true;
 		}
 		IBaseModel model = getAggregateModel();
 		return fShowExtensions && model != null && model.isEditable();
 	}
 
 	@Override
 	public String getTitle() {
 		IPluginModelBase model = (IPluginModelBase) getAggregateModel();
 		if (model == null || !model.isValid()) {
 			return super.getTitle();
 		}
 		String text = getTitleText(model);
 		if (text == null) {
 			return super.getTitle();
 		}
 		return model.getResourceString(text);
 	}
 
 	private String getTitleText(IPluginModelBase model) {
 		IPreferenceStore store = PDEPlugin.getDefault().getPreferenceStore();
 		String pref = store.getString(IPreferenceConstants.PROP_SHOW_OBJECTS);
 
 		StringBuilder builder = new StringBuilder();
 
 		if (model.getUnderlyingResource() != null) {
 			if (model.getUnderlyingResource().getName().equals("TEST.MF")) {
 				builder.append("test: ");
 			} else if (model.getUnderlyingResource().getName().equals("template.mf")) {
 				builder.append("template: ");
 			}
 		}
 
 		if (FacetUtils.isParProject(getCommonProject())) {
 			BundleInputContext context = (BundleInputContext) getContextManager().findContext(
 					BundleInputContext.CONTEXT_ID);
 			if (context != null) {
 				IBundleModel bundleModel = (IBundleModel) context.getModel();
 				if (pref != null && pref.equals(IPreferenceConstants.VALUE_USE_NAMES)) {
 					builder.append(bundleModel.getBundle().getHeader(IHeaderConstants.PAR_NAME));
 				}
 				builder.append(bundleModel.getBundle().getHeader(IHeaderConstants.PAR_SYMBOLICNAME));
 			}
 		}
 
 		if (pref != null && pref.equals(IPreferenceConstants.VALUE_USE_NAMES)
 				&& model.getPluginBase().getName() != null) {
 			builder.append(model.getPluginBase().getName());
 		}
 		if (model.getPluginBase().getId() != null) {
 			builder.append(model.getPluginBase().getId());
 		}
 		return builder.toString();
 	}
 
 	@Override
 	protected ISortableContentOutlinePage createContentOutline() {
 		return new BundleManifestOutlinePage(this);
 	}
 
 	public static IEditorPart openBundleEditor(String bundle, String version, IProject project) {
 		return openPluginEditor(RepositoryUtils.getBundleDefinition(bundle, version, project));
 	}
 
 	public static IEditorPart openPluginEditor(BundleDefinition bundleDefinition) {
 		if (bundleDefinition == null) {
 			Display.getDefault().beep();
 			return null;
 		}
 		return openPluginEditor(bundleDefinition, false);
 	}
 
 	public static IEditorPart openPluginEditor(Object object, boolean source) {
 		if (object instanceof BundleDefinition) {
 			URI file = ((BundleDefinition) object).getLocation();
 			return openExternalPlugin(new File(file), "META-INF/MANIFEST.MF");
 		}
 		return null;
 	}
 
 	// private static IEditorPart openWorkspacePlugin(IFile pluginFile) {
 	// return openEditor(new FileEditorInput(pluginFile));
 	// }
 
 	public static IEditorPart openExternalPlugin(File location, String filename) {
 		IEditorInput input = null;
 		if (location.isFile()) {
 			try {
 				ZipFile zipFile = new ZipFile(location);
 				if (zipFile.getEntry(filename) != null) {
 					input = new JarEntryEditorInput(new JarEntryFile(zipFile, filename));
 				}
 			} catch (IOException e) {
 			}
 		} else {
 			File file = new File(location, filename);
 			if (file.exists()) {
 				IFileStore store;
 				try {
 					store = EFS.getStore(file.toURI());
 					input = new FileStoreEditorInput(store);
 				} catch (CoreException e) {
 				}
 			}
 		}
 		return openEditor(input);
 	}
 
 	public static IEditorPart openEditor(IEditorInput input) {
 		if (input != null) {
 			try {
 				return PDEPlugin.getActivePage().openEditor(input, BundleManifestEditor.ID_EDITOR);
 			} catch (PartInitException e) {
 				PDEPlugin.logException(e);
 			}
 		}
 		return null;
 	}
 
	@Override
 	public void contributeToToolbar(IToolBarManager manager) {
 		// ignore
 	}
 
 	@Override
 	protected void performGlobalAction(String id) {
 		ISelection selection = getSelection();
 		IFormPage page = getActivePageInstance();
 		if (page instanceof AbstractPdeFormPage) {
 			boolean handled = ((AbstractPdeFormPage) page).performGlobalAction(id);
 			if (!handled) {
 				if (id.equals(ActionFactory.UNDO.getId())) {
 					fInputContextManager.undo();
 					return;
 				}
 				if (id.equals(ActionFactory.REDO.getId())) {
 					fInputContextManager.redo();
 					return;
 				}
 				if (id.equals(ActionFactory.CUT.getId()) || id.equals(ActionFactory.COPY.getId())) {
 					copyToClipboard(selection);
 					return;
 				}
 			}
 		} else {
 			super.performGlobalAction(id);
 		}
 	}
 
 	private void copyToClipboard(ISelection selection) {
 		Object[] objects = null;
 		String textVersion = null;
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection ssel = (IStructuredSelection) selection;
 			if (ssel == null || ssel.size() == 0) {
 				return;
 			}
 			objects = ssel.toArray();
 			StringWriter writer = new StringWriter();
 			PrintWriter pwriter = new PrintWriter(writer);
 			Class<?> objClass = null;
 			for (int i = 0; i < objects.length; i++) {
 				Object obj = objects[i];
 				if (objClass == null) {
 					objClass = obj.getClass();
 				} else if (objClass.equals(obj.getClass()) == false) {
 					return;
 				}
 				if (obj instanceof IWritable) {
 					// Add a customized delimiter in between all serialized
 					// objects to format the text representation
 					if ((i != 0) && (obj instanceof IWritableDelimiter)) {
 						((IWritableDelimiter) obj).writeDelimeter(pwriter);
 					}
 					((IWritable) obj).write("", pwriter); //$NON-NLS-1$
 				} else if (obj instanceof String) {
 					// Delimiter is always a newline
 					pwriter.println((String) obj);
 				}
 			}
 			pwriter.flush();
 			textVersion = writer.toString();
 			try {
 				pwriter.close();
 				writer.close();
 			} catch (IOException e) {
 			}
 		} else if (selection instanceof ITextSelection) {
 			textVersion = ((ITextSelection) selection).getText();
 		}
 		if ((textVersion == null || textVersion.length() == 0) && objects == null) {
 			return;
 		}
 		// set the clipboard contents
 		Object[] o = null;
 		Transfer[] t = null;
 		if (objects == null) {
 			o = new Object[] { textVersion };
 			t = new Transfer[] { TextTransfer.getInstance() };
 		} else if (textVersion == null || textVersion.length() == 0) {
 			o = new Object[] { objects };
 			t = new Transfer[] { ModelDataTransfer.getInstance() };
 		} else {
 			o = new Object[] { objects, textVersion };
 			t = new Transfer[] { ModelDataTransfer.getInstance(), TextTransfer.getInstance() };
 		}
 		getClipboard().setContents(o, t);
 	}
 
 }
