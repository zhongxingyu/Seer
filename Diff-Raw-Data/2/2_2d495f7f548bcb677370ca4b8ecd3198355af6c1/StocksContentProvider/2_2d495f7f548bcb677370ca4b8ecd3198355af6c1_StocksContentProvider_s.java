 package net.sf.jmoney.stocks.navigator;
 
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CommodityInfo;
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.views.StocksNode;
 import net.sf.jmoney.views.IDynamicTreeNode;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Display;
 
 public class StocksContentProvider implements ITreeContentProvider {
 
 	private DatastoreManager sessionManager;
 	
 	private Object stocksTreeNode;
 
 	private SessionChangeListener listener = null;
 	
 	public Object[] getChildren(Object parentElement) {
 		if (parentElement instanceof DatastoreManager) {
 			return new Object[] {
 					stocksTreeNode
 			};
 		}
 		if (parentElement instanceof IDynamicTreeNode) {
 			return ((IDynamicTreeNode)parentElement).getChildren().toArray();
 		}
 		return new Object[0];
 	}
 
 	public Object getParent(Object element) {
 		if (element instanceof IDynamicTreeNode) {
 			return sessionManager;
 		} else if (element instanceof Stock) {
 			return stocksTreeNode;
 		}
 		return null;  // Should never happen
 	}
 
 	public boolean hasChildren(Object element) {
 		if (element instanceof DatastoreManager) {
 			return true;
 		}
 		if (element instanceof IDynamicTreeNode) {
 			return ((IDynamicTreeNode)element).hasChildren();
 		}
 		return false;
 	}
 
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	public void dispose() {
 		if (listener != null) {
 			sessionManager.removeChangeListener(listener);
 		}
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		/*
 		 * The input never changes because the input to this viewer is the same as the input
 		 * to the workbench page and the input to the workbench page can't change (we replace
 		 * the page if the session changes).  Therefore no cleanup of the old input is needed.
 		 */
 		sessionManager = (DatastoreManager)newInput;
 		if (sessionManager != null) {
 			// Create the node that contain the stocks in this session.
 			stocksTreeNode = new StocksNode(sessionManager); 
 			
 			listener = new MyCurrentSessionChangeListener((TreeViewer)viewer);
 			sessionManager.addChangeListener(listener);
 		}
 	}
 
 	private class MyCurrentSessionChangeListener extends SessionChangeAdapter {
 		private TreeViewer viewer;
 		
 		MyCurrentSessionChangeListener(TreeViewer viewer) {
 			this.viewer = viewer;
 		}
 		
 		@Override	
 		public void objectInserted(ExtendableObject newObject) {
 			if (newObject instanceof Stock) {
 				viewer.insert(stocksTreeNode, newObject, 0);
 			}
 		}
 
 		@Override	
 		public void objectRemoved(final ExtendableObject deletedObject) {
			if (deletedObject instanceof Account) {
 				/*
 				 * This listener method is called before the object is deleted.
 				 * This allows listener methods to access the deleted object and
 				 * its position in the model.  However, this is too early to
 				 * refresh views.  Therefore we must delay the refresh of the view
 				 * until after the object is deleted.
 				 */
 				Display.getCurrent().asyncExec(new Runnable() {
 					public void run() {
 						viewer.remove(deletedObject);
 					}
 				});
 			}
 		}
 
 		@Override	
 		public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor propertyAccessor, Object oldValue, Object newValue) {
 			if (changedObject instanceof Stock
 					&& propertyAccessor == CommodityInfo.getNameAccessor()) {
 				viewer.update(changedObject, null);
 			}
 		}
 	}
 
 }
