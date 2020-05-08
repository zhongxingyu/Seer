 package wyclipse.ui.dialogs;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.URIUtil;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 import wyclipse.ui.util.WyclipseUI;
 
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
 public class FolderSelectionDialog extends Dialog {	
 	private TreeNode root;
 	private TreeViewer view;
 	private IPath selection;
 	
 	public FolderSelectionDialog(Shell parentShell, String rootName, IPath rootLocation) {
 		super(parentShell);
 		this.root = new TreeNode(rootName, rootLocation);
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
 		this.view.setInput(root);
 		
 		//this.getButton(SWT.OK).setEnabled(false);
 		
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
 					TreeNode node = (TreeNode) selections[0].getData();
 					selection = node.root;
 					//getButton(SWT.OK).setEnabled(true);
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
 			TreeNode node = root;
 			if (items.length > 0) {
				System.out.println("*** GOT SELECTION: " + dialog.getResult());
 				node = (TreeNode) items[0].getData();
 			}
			node.getChildren().add(
					new TreeNode(dialog.getResult(), (IPath) null));
 			
 			view.refresh();
 		}
 	}
 	
 	private static class TreeNode {
 		private String name;
 		private IPath root;
 		private ArrayList<TreeNode> children;
 
 		public TreeNode(String name, IPath root) {
 			this.root = root;
 			this.name = name;
 			// initially children is null; only when children is requested do we
 			// actually look what's there (i.e. lazily).
 		}
 
 		public TreeNode(String name, File root) {
 			this.root = new Path(root.toString());
 			this.name = name;
 			// initially children is null; only when children is requested do we
 			// actually look what's there (i.e. lazily).
 		}
 		
 		List<TreeNode> getChildren() {
 			if (children == null) {
 				children = new ArrayList<TreeNode>();
 				if (root != null) {
 					// non-virtual node
 					File dir = root.toFile();
					File[] contents = dir.listFiles();
					for (File f : contents) {
						if (f.isDirectory()) {
							children.add(new TreeNode(f.getName(), f));
 						}
 					}
 				}
 			}
 			return children;
 		}
 		
 		public String toString() {
 			return name;
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
 
 		private TreeNode cachedInputElement;
 		private TreeNode cachedResult;
 		
 		@Override
 		public void dispose() {			
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 			
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if(inputElement == cachedInputElement) {
 				return new Object[] { cachedResult };
 			} else if (inputElement instanceof TreeNode) {
 				TreeNode node = (TreeNode) inputElement;
 				// NOTE: cannot just reuse node here.
 				cachedInputElement = node;
 				cachedResult = new TreeNode(node.name, node.root);
 				return new Object[] { cachedResult };
 			} else {
 				return new Object[]{};
 			}
 		}
 
 		@Override
 		public Object[] getChildren(Object parentElement) {
 			System.out.println("GET CHILDREN: " + parentElement);
 			if (parentElement instanceof TreeNode) {
 				TreeNode node = (TreeNode) parentElement;
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
 			if(element instanceof TreeNode) {
 				TreeNode node = (TreeNode) element;
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
