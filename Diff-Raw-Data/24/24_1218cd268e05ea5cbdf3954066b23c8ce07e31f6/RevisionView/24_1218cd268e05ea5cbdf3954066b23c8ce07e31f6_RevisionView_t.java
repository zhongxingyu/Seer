 package cahootseclipse.views;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import com.cahoots.eclipse.Activator;
 import com.cahoots.eclipse.indigo.misc.MessageDialog;
 import com.cahoots.eclipse.optransformation.OpMemento;
 import com.cahoots.eclipse.optransformation.OpSession;
 import com.cahoots.eclipse.optransformation.OpSessionRegister;
 import com.google.inject.Injector;
 
 public class RevisionView extends ViewPart {
 	class ViewContentProvider implements IStructuredContentProvider {
 		private OpMemento memento;
 
 		public void inputChanged(final Viewer v, final Object oldInput,
 				final Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 		public Object[] getElements(final Object parent) {
 			if (memento != null) {
 				return memento.getTransformations().toArray();
 			} else {
 				return new String[] {};
 			}
 		}
 		
 		public void setMemento(final OpMemento memento) {
 			this.memento = memento;
 		}
 	}
 
 	class ViewLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 		public String getColumnText(final Object obj, final int index) {
 			return getText(obj);
 		}
 
 		public Image getColumnImage(final Object obj, final int index) {
 			return getImage(obj);
 		}
 
 		public Image getImage(final Object obj) {
 			return PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 
 	class NameSorter extends ViewerSorter {
 	}
 
 	public class LinkWithEditorPartListener implements IPartListener2 {
 		private final RevisionView view;
 
 		public LinkWithEditorPartListener(final RevisionView view) {
 			this.view = view;
 		}
 
 		public void partActivated(final IWorkbenchPartReference ref) {
 			if (ref.getPart(true) instanceof IEditorPart) {
 				view.editorActivated(view.getViewSite().getPage()
 						.getActiveEditor());
 			}
 		}
 
 		public void partBroughtToTop(final IWorkbenchPartReference ref) {
 			if (ref.getPart(true) == view) {
 				view.editorActivated(view.getViewSite().getPage()
 						.getActiveEditor());
 			}
 		}
 
 		public void partOpened(final IWorkbenchPartReference ref) {
 			if (ref.getPart(true) == view) {
 				view.editorActivated(view.getViewSite().getPage()
 						.getActiveEditor());
 			}
 		}
 
 		public void partVisible(final IWorkbenchPartReference ref) {
 			if (ref.getPart(true) == view) {
 				final IEditorPart editor = view.getViewSite().getPage()
 						.getActiveEditor();
 				if (editor != null) {
 					view.editorActivated(editor);
 				}
 			}
 		}
 
 		public void partClosed(final IWorkbenchPartReference ref) {
 		}
 
 		public void partDeactivated(final IWorkbenchPartReference ref) {
 		}
 
 		public void partHidden(final IWorkbenchPartReference ref) {
 		}
 
 		public void partInputChanged(final IWorkbenchPartReference ref) {
 		}
 	}
 
 	// ============================================================================
 	public static final String ID = "cahootseclipse.views.RevisionView";
 
 	private TableViewer viewer;
 	private Action doubleClickAction;
 
 	private final OpSessionRegister opSessionRegister;
 	private final MessageDialog messageDialog;
 
 	public RevisionView() {
 		final Injector injector = Activator.getInjector();
 		opSessionRegister = injector.getInstance(OpSessionRegister.class);
 		messageDialog = injector.getInstance(MessageDialog.class);
 	}
 
 	public void createPartControl(final Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		source = new ViewContentProvider();
 		viewer.setContentProvider(source);
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 		viewer.setInput(getViewSite());
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem()
 				.setHelp(viewer.getControl(), "cahoots-eclipse.viewer");
 
 		doubleClickAction = new Action() {
 			public void run() {
 				final ISelection selection = viewer.getSelection();
 				final Object obj = ((IStructuredSelection) selection)
 						.getFirstElement();
				// TODO: Implement the revision history on per element
				if (obj instanceof Object) {
					
				}
 			}
 		};
 
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(final DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 
 		linkWithEditorAction = new Action("Link with Editor",
 				IAction.AS_CHECK_BOX) {
 			public void run() {
 				toggleLinking(isChecked());
 			}
 		};
 		//linkWithEditorAction.setImageDescriptor( ISharedImages.IMG_OBJ_ELEMENT);
 		getViewSite().getActionBars().getToolBarManager()
 				.add(linkWithEditorAction);
 		getSite().getPage().addPartListener(linkWithEditorPartListener);
 	}
 
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(
 			this);
 	private Action linkWithEditorAction;
 	private boolean linkingActive = false;
 	private String currentOpId = "0";
 
 	private ViewContentProvider source;
 
 	public void editorActivated(final IEditorPart activeEditor) {
 		if (!linkingActive || !getViewSite().getPage().isPartVisible(this)) {
 			return;
 		}
 		
 		// do something with content of the editor
 		final OpSession session = opSessionRegister.getSession(currentOpId);
 		if (session == null) {
 			messageDialog.info(null, "Error", "No Session open to link with");
 		} else {
 			final OpMemento memento = session.getMemento();
 			source.setMemento(memento);
 		}
 	}
 
 	protected void toggleLinking(final boolean checked) {
 		this.linkingActive = checked;
 		if (checked) {
 			editorActivated(getSite().getPage().getActiveEditor());
 		}
 	}
 }
