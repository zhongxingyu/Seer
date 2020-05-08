 package plugin.views;
 
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.part.*;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IMarkSelection;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.Menu;
 
 
 /**
  * This sample class demonstrates how to plug-in a new
  * workbench view. The view shows data obtained from the
  * model. The sample creates a dummy model on the fly,
  * but a real implementation would connect to the model
  * available either in this or another plug-in (e.g. the workspace).
  * The view is connected to the model using a content provider.
  * <p>
  * The view uses a label provider to define how model
  * objects should be presented in the view. Each
  * view can present the same model objects using
  * different labels and icons, if needed. Alternatively,
  * a single label provider can be shared between views
  * in order to ensure that objects of the same type are
  * presented in the same way everywhere.
  * <p>
  */
 
 public class EduRideJunitView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "plugin.views.EduRideJunitView";
 
 	private TableViewer viewer;
 	private Action action1;
 	private Action action2;
 	private Action doubleClickAction;
 
	private TableColumn nameColumn;
	private TableColumn correctColumn;
	private TableColumn messageColumn;
 	/*
 	 * The content provider class is responsible for
 	 * providing objects to the view. It can wrap
 	 * existing objects in adapters or simply return
 	 * objects as-is. These objects may be sensitive
 	 * to the current input of the view, or ignore
 	 * it and always show the same content 
 	 * (like Task List, for example).
 	 */
 	 
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
 			return new String[] { "Assignment 1", "Two", "Three" };
 		}
 	}
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().
 					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 	class NameSorter extends ViewerSorter {
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public EduRideJunitView() {
 	}
 
 
 	private PageBook pagebook;
 	private TableViewer tableviewer;
 	private TextViewer textviewer;
 	
 	// the listener we register with the selection service 
 	private ISelectionListener listener = new ISelectionListener() {
 		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
 			// we ignore our own selections
 			if (sourcepart != EduRideJunitView.this) {
 			    showSelection(sourcepart, selection);
 			}
 		}
 	};
 	
 	/**
 	 * Shows the given selection in this view.
 	 */
 	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
 		setContentDescription(sourcepart.getTitle() + " (" + selection.getClass().getName() + ")");
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection ss = (IStructuredSelection) selection;
 			showItems(ss.toArray());
 		}
 		if (selection instanceof ITextSelection) {
 			ITextSelection ts  = (ITextSelection) selection;
 			showText(ts.getText());
 		}
 		if (selection instanceof IMarkSelection) {
 			IMarkSelection ms = (IMarkSelection) selection;
 			try {
 			    showText(ms.getDocument().get(ms.getOffset(), ms.getLength()));
 			} catch (BadLocationException ble) { }
 		}
 	}
 	
 	private void showItems(Object[] items) {
 		tableviewer.setInput(items);
 		pagebook.showPage(tableviewer.getControl());
 	}
 	
 	private void showText(String text) {
 		textviewer.setDocument(new Document(text));
 		pagebook.showPage(textviewer.getControl());
 	}
 
 	public void dispose() {
 		// important: We need do unregister our listener when the view is disposed
 		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
 		super.dispose();
 	}
 		
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		//apple
 		//viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
 		final Table table = viewer.getTable();
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Assignment");
		nameColumn.setWidth(200);
		correctColumn = new TableColumn(table, SWT.LEFT);
		correctColumn.setText("Step");
		correctColumn.setWidth(100);
		messageColumn = new TableColumn(table, SWT.LEFT);
		messageColumn.setText("Inputs");
		messageColumn.setWidth(200);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(false);
 		
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 		viewer.setInput(getViewSite());
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "plugin.viewer");
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 		contributeToActionBars();		
 		
 		// the PageBook allows simple switching between two viewers
 		pagebook = new PageBook(parent, SWT.NONE);
 		
 		tableviewer = new TableViewer(pagebook, SWT.NONE);
 		tableviewer.setLabelProvider(new WorkbenchLabelProvider());
 		tableviewer.setContentProvider(new ArrayContentProvider());
 		
 		// we're cooperative and also provide our selection
 		// at least for the tableviewer
 		getSite().setSelectionProvider(tableviewer);
 		
 		textviewer = new TextViewer(pagebook, SWT.H_SCROLL | SWT.V_SCROLL);
 		textviewer.setEditable(false);
 		
 		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
 		
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				EduRideJunitView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(action1);
 		manager.add(new Separator());
 		manager.add(action2);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(action1);
 		manager.add(action2);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 	
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(action1);
 		manager.add(action2);
 	}
 
 	private void makeActions() {
 		action1 = new Action() {
 			public void run() {
 				showMessage("Action 1 executed");
 			}
 		};
 		action1.setText("Action 1");
 		action1.setToolTipText("Action 1 tooltip");
 		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 		
 		action2 = new Action() {
 			public void run() {
 				showMessage("Action 2 executed");
 			}
 		};
 		action2.setText("Action 2");
 		action2.setToolTipText("Action 2 tooltip");
 		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 		doubleClickAction = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 				showMessage("Double-click detected on "+obj.toString());
 			}
 		};
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 	}
 	private void showMessage(String message) {
 		MessageDialog.openInformation(
 			viewer.getControl().getShell(),
 			"Test Plugin",
 			message);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 		pagebook.setFocus();
 	}
 }
