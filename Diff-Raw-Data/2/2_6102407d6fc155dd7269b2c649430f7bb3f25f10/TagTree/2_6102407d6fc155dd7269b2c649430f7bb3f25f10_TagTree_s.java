 package itemfiler.ui;
 
 import itemfiler.model.Filter;
 import itemfiler.model.Tag;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 public class TagTree extends Refreshable {
 
 	private Tree tree;
 	private MainWindow mainWindow;
 
 	public TagTree(Composite parent, int style, MainWindow main) {
 		super(parent, style);
 
 		mainWindow = main;
 
 		this.setLayout(new FillLayout());
 		tree = new Tree(this, SWT.BORDER | style);
 		tree.setData(this);
 
 		tree.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (SWT.CHECK == e.detail) {
 					// maintain childrens checked state
 					setChildrensCheckedState(((TreeItem) e.item).getChecked(),
 							((TreeItem) e.item));
 
 					// maintain parents checked state
 					maintainParentsCheckedState(
 							((TreeItem) e.item).getChecked(),
 							((TreeItem) e.item));
 
 					List<String> result = new ArrayList<>();
 					gatherCheckedItems(null, result);
 					mainWindow.setFilter(new Filter(result, result
 							.remove("untagged"), result.remove("trash")));
 					mainWindow.refresh((Refreshable) tree.getData());
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		refresh();
 	}
 
 	private void gatherCheckedItems(TreeItem node, List<String> result) {
 		try {
 			if (node.getChecked())
 				result.add(node.getText());
 			else
 				for (TreeItem current : node.getItems())
 					gatherCheckedItems(current, result);
 		} catch (NullPointerException e) {
 			// process root elements
 			for (TreeItem current : tree.getItems())
 				gatherCheckedItems(current, result);
 		}
 	}
 
 	private void setChildrensCheckedState(boolean checked, TreeItem item) {
 		for (TreeItem current : item.getItems())
 			setChildrensCheckedState(checked, current);
 
 		item.setChecked(checked);
 	}
 
 	private void maintainParentsCheckedState(boolean checked, TreeItem item) {
 		try {
 			if (checked)
 				for (TreeItem current : item.getParentItem().getItems())
 					if (!current.getChecked())
 						return;
 
 			item.getParentItem().setChecked(checked);
 			maintainParentsCheckedState(checked, item.getParentItem());
 		} catch (NullPointerException e) {
 			// reached the tree's root
 		}
 	}
 
 	public void refresh() {
 		// clean
 		for (TreeItem current : tree.getItems())
 			current.dispose();
 
 		List<String> sorted = new ArrayList<>(Tag.getFilteredStrings(""));
 		Collections.sort(sorted);
 
 		build(null, "", sorted);
 
		if ((getShell().getStyle() & SWT.CHECK) > 0) {
 			TreeItem tmp = new TreeItem(tree, SWT.NONE);
 			tmp.setText("untagged");
 
 			tmp = new TreeItem(tree, SWT.NONE);
 			tmp.setText("trash");
 		}
 
 	}
 
 	/**
 	 * @param parent
 	 * @param currentPrefix
 	 * @param tags
 	 *            alphabetically ASC sorted list of tags.
 	 */
 	private void build(TreeItem parent, String currentPrefix, List<String> tags) {
 		try {
 			for (;;)
 				if (tags.get(0).startsWith(currentPrefix)) {
 					String[] tmp = tags.get(0)
 							.substring(currentPrefix.length()).split("-");
 
 					TreeItem item;
 					if (null == parent)
 						item = new TreeItem(tree, SWT.NONE);
 					else
 						item = new TreeItem(parent, SWT.NONE);
 					item.setText(tmp[0]);
 
 					if (1 == tmp.length)
 						tags.remove(0);
 					build(item, currentPrefix + tmp[0] + "-", tags);
 				} else
 					return;
 		} catch (IndexOutOfBoundsException e) {
 		}
 
 	}
 
 	public List<String> getSelection() {
 		List<String> result = new ArrayList<>();
 		for (TreeItem current : tree.getSelection())
 			result.add(rebuildTagName(current));
 		return result;
 	}
 
 	private String rebuildTagName(TreeItem item) {
 		if (null == item.getParentItem())
 			return item.getText();
 		else
 			return rebuildTagName(item.getParentItem()) + "-" + item.getText();
 	}
 }
