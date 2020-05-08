 package wyclipse.ui.dialogs;
 
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 import wyclipse.ui.util.*;
 
 /**
  * <p>
  * Responsible for allowing the user to select an existing folder relative from
  * a project root, or to add a new one. This is used in the
  * <code>NewWhileyProjectWizard</code> and the
  * <code>WhileyPathPropertyPage</code> for configuring source and output
  * folders. This dialog will display existing folders relative to the given
  * project.
  * </p>
  * 
  * <b>Note:</b> that this dialog does not actually create any new folders,
  * although it gives the appearance of doing so. This is because the folders are
  * only created when the given "transaction" is completed. That is, when the
  * used selects finish or apply on the <code>NewWhileyProjectWizard</code> or
  * <code>WhileyPathPropertyPage</code>. Thus, in the case that the user selects
  * "cancel", there is actually nothing to undo.
  * 
  * @author David J. Pearce
  * 
  */
 public class VirtualContainerSelectionDialog extends Dialog {	
 	
 	/**
 	 * Provides a "virtual" project within which we can see existing folders
 	 * within the project, and add new folders. However, new folders are not
 	 * actually created on the filesystem and remain "virtual".
 	 */
 	private VirtualContainer project;
 	
 	/**
 	 * The tree-based folder view which makes up the majority of this dialog.
 	 */
 	private TreeViewer view;
 	
 	/**
 	 * A shared variable which is used to store the result so it can be accessed
 	 * by the owner of this dialog. This is necessary because the TreeViewer
 	 * will be disposed after "OK" is pressed, and we'll no longer be able to
 	 * determine its selection.
 	 */
 	private IPath selection;
 	
 	public VirtualContainerSelectionDialog(Shell parentShell, VirtualContainer project) {
 		super(parentShell);
 		this.project = project;
 	}
 
 	public IPath getResult() {
 		return selection;
 	}
 	
 	@Override
 	public Control createDialogArea(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		
 		// =====================================================================
 		// Configure Grid
 		// =====================================================================
 
 		GridLayout layout = new GridLayout();		
 		layout.numColumns = 2;
 		layout.verticalSpacing = 9;	
 		layout.marginWidth = 20;
 		container.setLayout(layout);
 
 		// =====================================================================
 		// Configure TreeView
 		// =====================================================================
 		this.view = new TreeViewer(container, SWT.VIRTUAL | SWT.BORDER);
 		this.view.setContentProvider(new ContentProvider());
 		this.view.setLabelProvider(new LabelProvider());
 		this.view.setInput(new Object[]{project});
 		// Force project root to be selected
 		view.setSelection(new StructuredSelection(project),true);		
 		// Force project root to be expanded
 		view.setExpandedState(project, true);
 		
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
 		gd.horizontalSpan = 2;
 		gd.verticalSpan = 10;
 		gd.heightHint = 300;
 		gd.widthHint = 300;
 		this.view.getTree().setLayout(new GridLayout());
 		this.view.getTree().setLayoutData(gd);
 		this.view.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				TreeItem[] selections = view.getTree().getSelection();
 				if(selections.length > 0) {
 					VirtualContainer node = (VirtualContainer) selections[0].getData();
 					selection = node.getRoot();
 					System.out.println("SELECTION: " + selection);
					selection = selection.makeRelativeTo(project.getRoot());
 					System.out.println("SELECTION: " + selection + " RELATIVE TO: " + project.getRoot());
 				}
 			}
 			
 		});
 
 		// =====================================================================
 		// Make New Folder Button
 		// =====================================================================
 		Button makeNewFolderButton = WyclipseUI.createButton(container, "Create New Folder", 150);
 		
 		makeNewFolderButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleMakeNewFolder();
 			}
 		});
 		
 		// =====================================================================
 		// Done
 		// =====================================================================
 
 		container.pack();
 		return container;
 	}
 	
 	private void handleMakeNewFolder() {
 		NewFolderDialog dialog = new NewFolderDialog(getShell());
 		if (dialog.open() == Window.OK) {
 			TreeItem[] items = view.getTree().getSelection();
 			VirtualContainer node = project;
 			if (items.length > 0) {
 				node = (VirtualContainer) items[0].getData();
 			}
 			String name = dialog.getResult();
 			VirtualContainer newFolder = new VirtualContainer(node.getRoot().append(name)); 
 			node.getChildren().add(newFolder);
 			view.refresh();
 			// Force the newly created folder to be automatically selected
 			view.setSelection(new StructuredSelection(newFolder),true);
 			
 		}
 	}
 			
 	/**
 	 * The content provider is responsible for deconstructing the object being
 	 * viewed in the viewer, so that the <code>TreeViewer</code> can navigate
 	 * them. In this case, this means it deconstructs those TreeNodes.
 	 * 
 	 * @author David J. Pearce
 	 * 
 	 */
 	private final static class ContentProvider implements ITreeContentProvider {
 
 		@Override
 		public void dispose() {			
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 			
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			 return (Object[]) inputElement;
 		}
 
 		@Override
 		public Object[] getChildren(Object parentElement) {			
 			if (parentElement instanceof VirtualContainer) {
 				VirtualContainer node = (VirtualContainer) parentElement;
 				return node.getChildren().toArray();				
 			} else {
 				return new Object[] {};
 			}
 		}
 
 		@Override
 		public Object getParent(Object element) {
 			return null;
 		}
 
 		@Override
 		public boolean hasChildren(Object element) {
 			if(element instanceof VirtualContainer) {
 				VirtualContainer node = (VirtualContainer) element;
 				return node.getChildren().size() > 0;
 			}
 			return false;
 		}		
 	}
 	
 	/**
 	 * The label provider is responsible for associating labels with the objects
 	 * being viewed in the viewer; in this case, that means it associates labels
 	 * with folders.
 	 * 
 	 * @author David J. Pearce
 	 * 
 	 */
 	protected static class LabelProvider implements ILabelProvider {
 
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public Image getImage(Object element) {
 			IWorkbench workbench = PlatformUI.getWorkbench();
 			ISharedImages images = workbench.getSharedImages();
 			return images.getImage(ISharedImages.IMG_OBJ_FOLDER);
 		}
 
 		@Override
 		public String getText(Object element) {
 			return element.toString();
 		}		
 	}
 	
 }
