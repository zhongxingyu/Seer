 /**
  * Copyright Universite Joseph Fourier (www.ujf-grenoble.fr)
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.workbench.common.ui.view.repositoryview;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.ViewPart;
 
 import fr.liglab.adele.cilia.workbench.common.parser.AbstractFile;
 import fr.liglab.adele.cilia.workbench.common.selectionservice.SelectionService;
 import fr.liglab.adele.cilia.workbench.common.service.AbstractRepoService;
 import fr.liglab.adele.cilia.workbench.common.service.Changeset;
 import fr.liglab.adele.cilia.workbench.common.service.Changeset.Operation;
 import fr.liglab.adele.cilia.workbench.common.service.IRepoServiceListener;
 
 /**
  * 
  * @author Etienne Gandrille
  */
 public abstract class RepositoryView<ModelType extends AbstractFile<AbstractType>, AbstractType> extends ViewPart implements IRepoServiceListener {
 
 	/** Main viewer. */
 	protected TreeViewer viewer;
 
 	/** The view internal model. */
 	private List<ModelType> model = new ArrayList<ModelType>();
 
 	/** Message area used to display last model reload date. */
 	protected Label messageArea;
 
 	/** The message area prefix. */
 	protected final String messageAreaPrefix = "Repository directory: ";
 
 	protected final AbstractRepoService<ModelType, AbstractType> repoService;
 
 	private final String partId;
 
 	public RepositoryView(String partId, AbstractRepoService<ModelType, AbstractType> repoService) {
 		this.repoService = repoService;
 		this.partId = partId;
 	}
 
 	public void createPartControl(Composite parent) {
 		// Global layout
 		GridLayout layout = new GridLayout(1, false);
 		parent.setLayout(layout);
 
 		// Viewer
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		viewer.setAutoExpandLevel(2);
 
 		// Label
 		messageArea = new Label(parent, SWT.WRAP);
 		messageArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
 		// Register repository listener
 		repoService.registerListener(this);
 
 		// Populates view
 		refresh();
 
 		// IMPORTANT we have 2 selection providers, because
 		// - the eclipse built-in selection provider has a bug : it notifies
 		// twice !
 		// - the eclipse built-in selection provider is mandatory for properties
 		// view...
 
 		// Selection provider for sending events on focus
 		SelectionService.getInstance().addSelectionProvider(partId, viewer);
 
 		// Selection provider for properties view
 		getSite().setSelectionProvider(viewer);
 	}
 
 	public void refresh() {
 		refreshMessageArea();
 		model = repoService.getRepoContent();
 		viewer.setContentProvider(repoService.getContentProvider());
 		viewer.setInput(model);
 		viewer.refresh();
 	}
 
 	public void refreshMessageArea() {
 		messageArea.setText(computeMessageAreaText());
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * Gets the repository directory.
 	 * 
 	 * @return null, if not available
 	 */
 	protected File getRepositoryDirectory() {
 		return repoService.getRepositoryLocation();
 	}
 
 	protected String computeMessageAreaText() {
 		File dir = getRepositoryDirectory();
 		if (dir == null)
 			return messageAreaPrefix + "not available";
 		else
 			return messageAreaPrefix + dir.getAbsolutePath();
 	}
 
 	/**
 	 * Gets the first element selected in the viewer.
 	 * 
 	 * @return the element, or null if not found.
 	 */
 	public Object getFirstSelectedElement() {
 		// ISelection sel =
 		// getSite().getWorkbenchWindow().getSelectionService().getSelection();
 		ISelection sel = viewer.getSelection();
 		if (sel != null && sel instanceof TreeSelection) {
 			TreeSelection ts = (TreeSelection) sel;
 			return ts.getFirstElement();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Called when repository changes. Refresh view if needed.
 	 * 
 	 * @param changes
 	 *            the changes
 	 */
 	@Override
 	public void repositoryContentUpdated(AbstractRepoService<?, ?> abstractRepoService, List<Changeset> changes) {
 		// Force refresh
 		if (changes == null) {
 			refresh();
 			return;
 		}
 		// Check refresh
 		for (Changeset change : changes) {
 			if (change.getOperation() != Operation.UPDATE) {
 				refresh();
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		repoService.unregisterListener(this);
 	}
 
 	/**
 	 * Adds a listener to an editor to be notify as soon as he saves.
 	 * 
 	 * @param editor
 	 *            the editor
 	 */
 	protected void addEditorSavedListener(IWorkbenchPart editor) {
 		if (editor != null) {
 			editor.addPropertyListener(new IPropertyListener() {
 				@Override
 				public void propertyChanged(Object source, int propId) {
 					if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
 						EditorPart editor = (EditorPart) source;
 						if (editor.isDirty() == false)
 							repoService.updateModel();
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Finds open editors, which are editing a file located in the repository.
 	 * Filters results using the editor title : title must end with prefix.
 	 * 
 	 * @param prefix
 	 * @return
 	 */
 	protected List<IEditorReference> getRelevantFileStoreEditors(String prefix) {
 
 		List<IEditorReference> retval = new ArrayList<IEditorReference>();
 
 		IEditorReference[] ref = getViewSite().getPage().getEditorReferences();
 		for (IEditorReference editor : ref) {
 			try {
 				IEditorInput input = editor.getEditorInput();
 				if (input instanceof IURIEditorInput) {
 					IURIEditorInput fileStore = (IURIEditorInput) input;
 					URI uri = fileStore.getURI();
 					String scheme = uri.getScheme();
 					if (scheme.equals("file")) {
 						String path = uri.getPath();
 						if (path.startsWith(getRepositoryDirectory().getAbsolutePath()))
 							if (editor.getTitle().toLowerCase().endsWith(prefix))
 								retval.add(editor);
 					}
 				}
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return retval;
 	}
 
 	protected void openFileEditor(DoubleClickEvent event) {
 		Object element = getFirstSelectedElement();
 
 		if (element != null) {
 			try {
 				@SuppressWarnings("unchecked")
 				AbstractFile<ModelType> repoElement = (AbstractFile<ModelType>) element;
 				// Even files with an invalid model can be open.
 				openFileEditor(repoElement.getFile());
 
 			} catch (Exception e) {
 				// do nothing
 				// we are using try... catch instead because instanceof is not
 				// possible (type erasure)
 			}
 		}
 	}
 
 	/**
 	 * Opens an editor for editing a file, which name is given into parameter.
 	 * Registers a listener to update the repo model as soon as the editor
 	 * saves.
 	 * 
 	 * @param file
 	 *            the file path
 	 */
 	protected void openFileEditor(File file) {
 		IFileStore fileStore;
 		try {
 			fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getAbsolutePath()));
 			IWorkbenchPage page = getViewSite().getPage();
 			IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
 			addEditorSavedListener(editor);
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public AbstractRepoService<ModelType, AbstractType> getRepoService() {
 		return repoService;
 	}
 }
