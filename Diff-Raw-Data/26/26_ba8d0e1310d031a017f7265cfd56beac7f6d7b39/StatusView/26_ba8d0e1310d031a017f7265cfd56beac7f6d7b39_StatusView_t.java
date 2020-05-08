 package ggit.views;
 
 
 import ggit.Activator;
 import ggit.Config;
 import ggit.status.CompositeAction;
 import ggit.status.FileAction;
 import ggit.status.FileItem;
 import ggit.status.Status;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.part.ViewPart;
 
 
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
 
 public class StatusView extends ViewPart {
 	private TreeViewer viewer;
 	private Action refreshAction;
 	private Action commitAction;
 	private Action doubleClickAction;
 	private Status repositoryStatus=new Status();
 
 	private static final String COMMITABLE = "Committable";
 	private static final String NOT_COMMITABLE = "Not Committable";
 
 
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
 			Collection<FileItem> items = repositoryStatus.getItems();
 			if( !items.isEmpty() )
 			{
 				return items.toArray();
 			}
 			return new Object[0];
 		}
 	}
 
 	class TreeContentProvider implements ITreeContentProvider
 	{
 
 
 		private Collection<FileItem> getItems()
 		{
 			return repositoryStatus.getItems();
 		}
 
 		@Override
 		public Object[] getChildren(Object parentElement) {
 			ArrayList<FileItem> ret = new ArrayList<FileItem>();
 			for (FileItem fi : getItems()) {
 				if( parentElement.equals(COMMITABLE) == fi.isCommitable() )
 				{
 					ret.add(fi);
 				}
 			}
 			return ret.toArray();
 		}
 
 		@Override
 		public Object getParent(Object element) {
 			if (element instanceof FileItem) {
 				FileItem fi = (FileItem) element;
 				return fi.isCommitable()?COMMITABLE:NOT_COMMITABLE;
 			}
 			return null;
 		}
 
 		@Override
 		public boolean hasChildren(Object element) {
 			if(COMMITABLE.equals(element)||NOT_COMMITABLE.equals(element))
 			{
 				return getChildren(element).length>0;
 			}
 			return false;
 
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			//return getItems().toArray();
 			if( hasChildren(COMMITABLE))
 			{
 				if(hasChildren(NOT_COMMITABLE))
 				{
 					return new Object[]{COMMITABLE,NOT_COMMITABLE};
 				}else
 				{
 					return new Object[]{COMMITABLE};
 				}
 			}else
 			{
 				if(hasChildren(NOT_COMMITABLE))
 				{
 					return new Object[]{NOT_COMMITABLE};
 				}else
 				{
 					return new Object[0];
 				}
 			}
 		}
 
 		@Override
 		public void dispose() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			if (obj instanceof FileItem) {
 				FileItem fi = (FileItem) obj;
 				if( index==0)
 				{
 					return fi.getShortStatus();
 				}
 				if (index==1) {
 					return fi.getFileName();
 				}
 				return fi.getDescription();
 			}
 			if( index==0)
 			{
 				return getText(obj);
 			}
 			return "";
 		}
 		public Image getColumnImage(Object obj, int index) {
 			if( index==0)
 			{
 				return getImage(obj);
 			}
 			return null;
 		}
 		public Image getImage(Object obj) {
 			 if (obj instanceof FileItem)
 			 {
 
 				    FileItem fileItem = (FileItem) obj;
 				    return fileItem.getImage();
 
 //				return PlatformUI.getWorkbench().
 //						getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 			 }
 			 if( COMMITABLE.equals(obj))
 			 {
 			        return Activator.getImage("staged.png");
 			 }
 			return null;
 		}
 	}
 	class NameSorter extends ViewerSorter {
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public StatusView() {
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		createTable(parent);
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 		contributeToActionBars();
 		this.updateStatus();
 	}
 
 
 	private void createTable(Composite parent) {
 		//viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL|SWT.FULL_SELECTION);
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL|SWT.FULL_SELECTION);
 
 		// setup columns
 
 		TreeColumn statusColumn = new TreeColumn(viewer.getTree(),SWT.NONE);
 		statusColumn.setText("Status");
 		statusColumn.setResizable(true);
 		statusColumn.setWidth(100);
 
 		TreeColumn fileColumn = new TreeColumn(viewer.getTree(),SWT.NONE);
 		fileColumn.setText("File");
 		fileColumn.setResizable(true);
 		fileColumn.setWidth(300);
 
 		TreeColumn descriptionColumn = new TreeColumn(viewer.getTree(),SWT.NONE);
 		descriptionColumn.setText("Description");
 		descriptionColumn.setResizable(true);
 		descriptionColumn.setWidth(300);
 
 		//viewer.setContentProvider(new ViewContentProvider());
 		viewer.setContentProvider(new TreeContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 		viewer.setInput(getViewSite());
 		viewer.getTree().setLinesVisible(true);
 		viewer.getTree().setHeaderVisible(true);
 
 	}
 
 	 void updateStatus()
 	{
 		this.refreshAction.run();
 	}
 
 	void refreshViewAssync()
 	{
 		getSite().getShell().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				viewer.getTree().setEnabled(true);
 				viewer.refresh();
 				viewer.expandAll();
 			}
 		});
 	}
 
 
 	 void markAsOutdated()
 	{
 		getSite().getShell().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				viewer.getTree().setEnabled(false);
 			}
 		});
 	}
 
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				StatusView.this.fillContextMenu(manager);
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
 		manager.add(refreshAction);
 		manager.add(new Separator());
 		manager.add(commitAction);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		IStructuredSelection selection3 = (IStructuredSelection) viewer.getSelection();
 		ArrayList<Action> actions = getActionsForSelection(selection3);
 		for (Action action : actions) {
 				manager.add( action);
 		}
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 
 	private ArrayList<Action> getActionsForSelection(
 			IStructuredSelection selection3) {
 		ArrayList<Action> actions = new ArrayList<Action>();
 		if( !selection3.isEmpty())
 		{
 			// category selected, provide actions for all items
 			Object[] array = selection3.toArray();
 			if( array.length==1 && array[0] instanceof String)
 			{
				String string = (String) array[0];
 				array = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(array[0]);
				if( COMMITABLE.equals(string))
				{
					actions.add(commitAction);
				}
 			}
 			ArrayList<Action> all = new ArrayList<Action>();
 			for (Object o : array) {
 				FileItem  fi = (FileItem) o;
 				Collection<FileAction> availableActions = fi.availableActions();
 				if(availableActions!=null)
 				{
 					all.addAll(availableActions);
 				}
 			}
 			actions.addAll( CompositeAction.joinActions(all));
 		}
 		else
 		{
 			actions.add(refreshAction);
 			actions.add(commitAction);
 		}
 		ArrayList<Action> ret = new ArrayList<Action>();
 		for (Action action : actions) {
 				ret.add( new RefreshViewActionWrapper(action,this));
 		}
 
 		return ret;
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(refreshAction);
 		manager.add(commitAction);
 	}
 
 	private void makeActions() {
 		refreshAction = new RefreshStatusAction(this,repositoryStatus);
 
 		commitAction = new Action() {
 			public void run() {
 				  InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
 				            "Enter commit message", "", "", null);
 				        if (dlg.open() == Window.OK) {
 				          // User clicked OK; update the label with the input
 				        	String value2 = dlg.getValue();
 				        	Config.execGit("commit","-m",value2);
 				        	updateStatus();
 				        }
 			}
 		};
 		commitAction.setText("Commit");
 		commitAction.setToolTipText("Commit all to git");
 		commitAction.setImageDescriptor(Activator.getImageDescriptor("icons/staged.png"));
 		doubleClickAction = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				ArrayList<Action> actions = getActionsForSelection((IStructuredSelection) selection);
 				for (Action action : actions) {
 					if( action.getText().startsWith("Diff"))
 					{
 						new RefreshViewActionWrapper(action, StatusView.this).run();
 						return;
 					}
 				}
 
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
 	 void showMessage(final String message) {
 		 Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				MessageDialog.openInformation(
 						viewer.getControl().getShell(),
 						"GGit Status View",
 						message);
 			}
 		};
 		getSite().getShell().getDisplay().asyncExec(runnable);
 	}
 
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 }
