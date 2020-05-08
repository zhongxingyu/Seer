 package be.kuleuven.cs.distrinet.chameleon.eclipse.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ColumnViewerEditor;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ICheckStateProvider;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.IOpenListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.HelpListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 
import be.kuleuven.cs.distrinet.chameleon.ui.widget.TreeListener;
import be.kuleuven.cs.distrinet.chameleon.ui.widget.TreeNode;
 
 /**
  * A tree viewer with tristate checkboxes.
  * 
  * @author Marko van Dooren
  */
 public class TristateTreeViewer extends Composite {
 
 	public TristateTreeViewer(Composite parent, int style) {
 		super(parent, style);
 		init();
 	}
 	
 	private List<TreeListener> _listeners;
 
 	public void addTreeListener(TreeListener listener) {
 		if(listener != null) {
 			if(_listeners == null) {
 				_listeners = new ArrayList<>();
 			}
 			_listeners.add(listener);
 		}
 	}
 	
 	public void removeTreeListener(TreeListener listener) {
 		if(listener != null) {
 			if(_listeners != null) {
 				_listeners.remove(listener);
 			}
 		}
 	}
 	
 	private void notifyItemChanged(TreeItem item) {
 		if(_listeners != null) {
 			boolean checked = item.getChecked();
 			boolean grayed = item.getGrayed();
 			for(TreeListener listener: _listeners) {
 				listener.itemChanged((TreeNode)item.getData(), checked, grayed);
 			}
 		}
 	}
 	
 	private CheckboxTreeViewer _inner;
 	
 	public void setContentProvider(ITreeContentProvider provider) {
 		_inner.setContentProvider(provider);
 	}
 	
 	public void setLabelProvider(ILabelProvider provider) {
 		_inner.setLabelProvider(provider);
 	}
 	
 	public void setInput(Object object) {
 		_inner.setInput(object);
 	}
 	
   @Override
   public void setLayoutData(Object layoutData) {
   	super.setLayoutData(layoutData);
   	_inner.getTree().setLayoutData(layoutData);
   }
 	
 	private void init() {
 		_inner = new CheckboxTreeViewer(this,SWT.H_SCROLL | SWT.V_SCROLL);
		setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		setLayout(layout);
//		ScrollBar horizontalBar = _inner.getTree().getHorizontalBar();
//		horizontalBar.setVisible(true);
 		_inner.getTree().addListener(SWT.Expand, new Listener() {
       public void handleEvent(Event e) {
         TreeItem item = (TreeItem) e.item;
         if(item.getChecked() && (! item.getGrayed())) {
         	setChecked(item);
         }
       }
     });
 		_inner.getTree().addListener(SWT.Selection, new Listener(){
 
 			void updateParents(TreeItem changed) {
 				TreeItem item = changed.getParentItem();
 				if (item == null) return;
 				boolean checked = item.getChecked();
 				boolean grayed = item.getGrayed();
 				boolean allUnchecked = true;
 				boolean allChecked = true;
 				boolean someGray = false;
 				for(TreeItem child:  item.getItems()) {
 					if(child.getGrayed() || (! (allUnchecked | allChecked))) {
 						someGray = true;
 						break;
 					} else if(child.getChecked() && allUnchecked) {
 						allUnchecked = false;
 					} else if((! child.getChecked()) && allChecked) {
 						allChecked = false;
 					}
 				}
 				if(someGray) {
 					grayed = true;
 				} else if(allChecked) {
 					grayed = false;
 					checked = true;
 				} else {
 					grayed = true;
 				}
 				if(grayed) {
 					checked = true;
 				} 
 				item.setChecked(checked);
 				item.setGrayed(grayed);
 				notifyItemChanged(item);
 				updateParents(item);
 			}
 
 			@Override
 			public void handleEvent(Event event) {
 				if(event.detail == SWT.CHECK) {
 					TreeItem item = (TreeItem) event.item;
 					itemClicked(item);
 					updateParents(item);
 				}
 			}
 
 			private void itemClicked(TreeItem item) {
 				if(! item.getChecked()) {
 					setUnchecked(item);
 				} else {
 					setChecked(item);
 				}
 			}
 
 		});
 	}
 	
 	private void setChecked(TreeItem item) {
 		item.setChecked(true);
 		item.setGrayed(false);
 		notifyItemChanged(item);
 		for(TreeItem child: item.getItems()) {
 			setChecked(child);
 		}
 	}
 
 //	private void setMaybe(TreeItem item) {
 //		item.setGrayed(true);
 //		item.setChecked(true);
 //	}
 
 	private void setUnchecked(TreeItem item) {
 		item.setChecked(false);
 		item.setGrayed(false);
 		notifyItemChanged(item);
 		for(TreeItem child: item.getItems()) {
 			setUnchecked(child);
 		}
 	}
 
 
 	public int hashCode() {
 		return _inner.hashCode();
 	}
 
 	public void addHelpListener(HelpListener listener) {
 		_inner.addHelpListener(listener);
 	}
 
 	public ViewerCell getCell(Point point) {
 		return _inner.getCell(point);
 	}
 
 	public void addCheckStateListener(ICheckStateListener listener) {
 		_inner.addCheckStateListener(listener);
 	}
 
 	public void setCheckStateProvider(ICheckStateProvider checkStateProvider) {
 		_inner.setCheckStateProvider(checkStateProvider);
 	}
 
 	public void add(Object parentElementOrTreePath, Object[] childElements) {
 		_inner.add(parentElementOrTreePath, childElements);
 	}
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		_inner.addSelectionChangedListener(listener);
 	}
 
 	public IContentProvider getContentProvider() {
 		return _inner.getContentProvider();
 	}
 
 	public boolean equals(Object obj) {
 		return _inner.equals(obj);
 	}
 
 	public Object getInput() {
 		return _inner.getInput();
 	}
 
 	public Control getControl() {
 		return _inner.getControl();
 	}
 
 	public Object getData(String key) {
 		return _inner.getData(key);
 	}
 
 	public IBaseLabelProvider getLabelProvider() {
 		return _inner.getLabelProvider();
 	}
 
 	public boolean getChecked(Object element) {
 		return _inner.getChecked(element);
 	}
 
 	public Object[] getCheckedElements() {
 		return _inner.getCheckedElements();
 	}
 
 	public Tree getTree() {
 		return _inner.getTree();
 	}
 
 	public void removeHelpListener(HelpListener listener) {
 		_inner.removeHelpListener(listener);
 	}
 
 	public boolean getGrayed(Object element) {
 		return _inner.getGrayed(element);
 	}
 
 	public Object[] getGrayedElements() {
 		return _inner.getGrayedElements();
 	}
 
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		_inner.removeSelectionChangedListener(listener);
 	}
 
 	public void setLabelProvider(IBaseLabelProvider labelProvider) {
 		_inner.setLabelProvider(labelProvider);
 	}
 
 	public void setData(String key, Object value) {
 		_inner.setData(key, value);
 	}
 
 	public String toString() {
 		return _inner.toString();
 	}
 
 	public void cancelEditing() {
 		_inner.cancelEditing();
 	}
 
 	public void setChildCount(Object elementOrTreePath, int count) {
 		_inner.setChildCount(elementOrTreePath, count);
 	}
 
 	public void addDoubleClickListener(IDoubleClickListener listener) {
 		_inner.addDoubleClickListener(listener);
 	}
 
 	public void replace(Object parentElementOrTreePath, int index, Object element) {
 		_inner.replace(parentElementOrTreePath, index, element);
 	}
 
 	public void addOpenListener(IOpenListener listener) {
 		_inner.addOpenListener(listener);
 	}
 
 	public CellEditor[] getCellEditors() {
 		return _inner.getCellEditors();
 	}
 
 	public void setSelection(ISelection selection) {
 		_inner.setSelection(selection);
 	}
 
 	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
 		_inner.addPostSelectionChangedListener(listener);
 	}
 
 	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {
 		_inner.addDragSupport(operations, transferTypes, listener);
 	}
 
 	public ICellModifier getCellModifier() {
 		return _inner.getCellModifier();
 	}
 
 	public Object[] getColumnProperties() {
 		return _inner.getColumnProperties();
 	}
 
 	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener) {
 		_inner.addDropSupport(operations, transferTypes, listener);
 	}
 
 	public boolean isCellEditorActive() {
 		return _inner.isCellEditorActive();
 	}
 
 	public void removeCheckStateListener(ICheckStateListener listener) {
 		_inner.removeCheckStateListener(listener);
 	}
 
 	public boolean setChecked(Object element, boolean state) {
 		return _inner.setChecked(element, state);
 	}
 
 	public void addFilter(ViewerFilter filter) {
 		_inner.addFilter(filter);
 	}
 
 	public void refresh(Object element) {
 		_inner.refresh(element);
 	}
 
 	public void refresh(Object element, boolean updateLabels) {
 		_inner.refresh(element, updateLabels);
 	}
 
 	public void update(Object element, String[] properties) {
 		_inner.update(element, properties);
 	}
 
 	public void setCellEditors(CellEditor[] editors) {
 		_inner.setCellEditors(editors);
 	}
 
 	public void setCheckedElements(Object[] elements) {
 		_inner.setCheckedElements(elements);
 	}
 
 	public void setCellModifier(ICellModifier modifier) {
 		_inner.setCellModifier(modifier);
 	}
 
 	public boolean setGrayed(Object element, boolean state) {
 		return _inner.setGrayed(element, state);
 	}
 
 	public void setColumnProperties(String[] columnProperties) {
 		_inner.setColumnProperties(columnProperties);
 	}
 
 	public boolean isExpandable(Object element) {
 		return _inner.isExpandable(element);
 	}
 
 	public boolean setGrayChecked(Object element, boolean state) {
 		return _inner.setGrayChecked(element, state);
 	}
 
 	public void setGrayedElements(Object[] elements) {
 		_inner.setGrayedElements(elements);
 	}
 
 	public CellLabelProvider getLabelProvider(int columnIndex) {
 		return _inner.getLabelProvider(columnIndex);
 	}
 
 	public boolean setParentsGrayed(Object element, boolean state) {
 		return _inner.setParentsGrayed(element, state);
 	}
 
 	public void add(Object parentElementOrTreePath, Object childElement) {
 		_inner.add(parentElementOrTreePath, childElement);
 	}
 
 	public void setColumnViewerEditor(ColumnViewerEditor columnViewerEditor) {
 		_inner.setColumnViewerEditor(columnViewerEditor);
 	}
 
 	public ColumnViewerEditor getColumnViewerEditor() {
 		return _inner.getColumnViewerEditor();
 	}
 
 	public boolean setSubtreeChecked(Object element, boolean state) {
 		return _inner.setSubtreeChecked(element, state);
 	}
 
 	public void addTreeListener(ITreeViewerListener listener) {
 		_inner.addTreeListener(listener);
 	}
 
 	public void setAllChecked(boolean state) {
 		_inner.setAllChecked(state);
 	}
 
 	public void collapseAll() {
 		_inner.collapseAll();
 	}
 
 	public void collapseToLevel(Object elementOrTreePath, int level) {
 		_inner.collapseToLevel(elementOrTreePath, level);
 	}
 
 	public boolean isBusy() {
 		return _inner.isBusy();
 	}
 
 	public IElementComparer getComparer() {
 		return _inner.getComparer();
 	}
 
 	public void remove(Object parentOrTreePath, int index) {
 		_inner.remove(parentOrTreePath, index);
 	}
 
 	public ViewerFilter[] getFilters() {
 		return _inner.getFilters();
 	}
 
 	public void setContentProvider(IContentProvider provider) {
 		_inner.setContentProvider(provider);
 	}
 
 	public void setHasChildren(Object elementOrTreePath, boolean hasChildren) {
 		_inner.setHasChildren(elementOrTreePath, hasChildren);
 	}
 
 	public void expandAll() {
 		_inner.expandAll();
 	}
 
 	public void expandToLevel(int level) {
 		_inner.expandToLevel(level);
 	}
 
 	public ViewerSorter getSorter() {
 		return _inner.getSorter();
 	}
 
 	public void expandToLevel(Object elementOrTreePath, int level) {
 		_inner.expandToLevel(elementOrTreePath, level);
 	}
 
 	public ViewerComparator getComparator() {
 		return _inner.getComparator();
 	}
 
 	public int getAutoExpandLevel() {
 		return _inner.getAutoExpandLevel();
 	}
 
 	public void setSelection(ISelection selection, boolean reveal) {
 		_inner.setSelection(selection, reveal);
 	}
 
 	public Object[] getExpandedElements() {
 		return _inner.getExpandedElements();
 	}
 
 	public void editElement(Object element, int column) {
 		_inner.editElement(element, column);
 	}
 
 	public boolean getExpandedState(Object elementOrTreePath) {
 		return _inner.getExpandedState(elementOrTreePath);
 	}
 
 	public void refresh() {
 		_inner.refresh();
 	}
 
 	public void refresh(boolean updateLabels) {
 		_inner.refresh(updateLabels);
 	}
 
 	public void removeOpenListener(IOpenListener listener) {
 		_inner.removeOpenListener(listener);
 	}
 
 	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
 		_inner.removePostSelectionChangedListener(listener);
 	}
 
 	public void removeDoubleClickListener(IDoubleClickListener listener) {
 		_inner.removeDoubleClickListener(listener);
 	}
 
 	public void removeFilter(ViewerFilter filter) {
 		_inner.removeFilter(filter);
 	}
 
 	public void setFilters(ViewerFilter[] filters) {
 		_inner.setFilters(filters);
 	}
 
 	public void resetFilters() {
 		_inner.resetFilters();
 	}
 
 	public void setSorter(ViewerSorter sorter) {
 		_inner.setSorter(sorter);
 	}
 
 	public void setComparator(ViewerComparator comparator) {
 		_inner.setComparator(comparator);
 	}
 
 	public void setUseHashlookup(boolean enable) {
 		_inner.setUseHashlookup(enable);
 	}
 
 	public void setComparer(IElementComparer comparer) {
 		_inner.setComparer(comparer);
 	}
 
 	public Widget testFindItem(Object element) {
 		return _inner.testFindItem(element);
 	}
 
 	public Widget[] testFindItems(Object element) {
 		return _inner.testFindItems(element);
 	}
 
 	public void update(Object[] elements, String[] properties) {
 		_inner.update(elements, properties);
 	}
 
 	public void remove(Object[] elementsOrTreePaths) {
 		_inner.remove(elementsOrTreePaths);
 	}
 
 	public void remove(Object parent, Object[] elements) {
 		_inner.remove(parent, elements);
 	}
 
 	public void remove(Object elementsOrTreePaths) {
 		_inner.remove(elementsOrTreePaths);
 	}
 
 	public void removeTreeListener(ITreeViewerListener listener) {
 		_inner.removeTreeListener(listener);
 	}
 
 	public void reveal(Object elementOrTreePath) {
 		_inner.reveal(elementOrTreePath);
 	}
 
 	public Item scrollDown(int x, int y) {
 		return _inner.scrollDown(x, y);
 	}
 
 	public Item scrollUp(int x, int y) {
 		return _inner.scrollUp(x, y);
 	}
 
 	public void setAutoExpandLevel(int level) {
 		_inner.setAutoExpandLevel(level);
 	}
 
 	public void setExpandedElements(Object[] elements) {
 		_inner.setExpandedElements(elements);
 	}
 
 	public void setExpandedTreePaths(TreePath[] treePaths) {
 		_inner.setExpandedTreePaths(treePaths);
 	}
 
 	public void setExpandedState(Object elementOrTreePath, boolean expanded) {
 		_inner.setExpandedState(elementOrTreePath, expanded);
 	}
 
 	public Object[] getVisibleExpandedElements() {
 		return _inner.getVisibleExpandedElements();
 	}
 
 	public ISelection getSelection() {
 		return _inner.getSelection();
 	}
 
 	public TreePath[] getExpandedTreePaths() {
 		return _inner.getExpandedTreePaths();
 	}
 
 	public void insert(Object parentElementOrTreePath, Object element, int position) {
 		_inner.insert(parentElementOrTreePath, element, position);
 	}
 
 	public void setExpandPreCheckFilters(boolean checkFilters) {
 		_inner.setExpandPreCheckFilters(checkFilters);
 	}
 	
 	
 }
