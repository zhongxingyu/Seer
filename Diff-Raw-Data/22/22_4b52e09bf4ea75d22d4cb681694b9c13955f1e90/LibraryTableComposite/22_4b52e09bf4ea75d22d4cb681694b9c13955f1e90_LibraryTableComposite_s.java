 package net.kbserve.pyjdt.properties.view;
 
 import net.kbserve.pyjdt.properties.models.ICPEType;
 import net.kbserve.pyjdt.properties.models.RootContainer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 public class LibraryTableComposite extends Composite {
 
 	private final class TableSelectionListener implements SelectionListener {
 		@Override
 		public void widgetSelected(SelectionEvent arg0) {
 			System.out.println("Selection: " + arg0.item);
 			try {
 				TreeItem tree = (TreeItem) arg0.item;
 				ICPEType cpc = (ICPEType) tree.getData();
 				cpc.setEnabled(tree.getChecked());
 				if (!tree.getChecked()) {
 					TreeItem parentItem = tree.getParentItem();
 					if (parentItem.getChecked()) {
 						parentItem.setGrayed(true);
 					}
 				}
 
 				for (TreeItem child : tree.getItems()) {
 					ICPEType childClasspathContainer = (ICPEType) child
 							.getData();
 					child.setChecked(childClasspathContainer
 							.isEnabled());
 				}
 			} catch (ClassCastException e) {
 			}
 
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent arg0) {
 		}
 	}
 
 	private IProject project;
 	private Tree table;
 	private TableSelectionListener tableSelectionListener;
 
 	public LibraryTableComposite(Composite parent, int style, IProject project) {
 		super(parent, style);
 		setLayout(new GridLayout(1, true));
 		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		table = new Tree(this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
 				| SWT.H_SCROLL);
 
 		table.setLayout(new GridLayout(1, true));
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		this.project = project;
 	}
 
 	public TreeItem[] getItems() {
 		return table.getItems();
 	}
 
 	@Override
 	public void pack(boolean changed) {
 		if (changed) {
 			RootContainer root = RootContainer.getRoot(project);
 			root.update();
 			table.clearAll(true);
 			table.setItemCount(0);
			setupClasspathInfo(table, root);
 			if(tableSelectionListener==null) {
 				tableSelectionListener = new TableSelectionListener();
 				table.addSelectionListener(tableSelectionListener);
 			}
 		}
 		table.pack(changed);
 		super.pack(changed);
 	}
 
 	private void setupClasspathInfo(Tree tree, ICPEType cp) {
 		if (cp.isAvailable()) {
 			TreeItem ti = new TreeItem(tree, SWT.NONE);
 			setUpTreeItem(cp, ti);
 			for (ICPEType child : cp.getChildren()) {
 				setupClasspathInfo(ti, child);
 			}
 		}
 	}
 
 	private void setupClasspathInfo(TreeItem treeItem, ICPEType cp) {
 		if (cp.isAvailable()) {
 			TreeItem ti = new TreeItem(treeItem, SWT.NONE);
 			setUpTreeItem(cp, ti);
 			for (ICPEType child : cp.getChildren()) {
 				setupClasspathInfo(ti, child);
 			}
 		}
 	}
 
 	private void setUpTreeItem(ICPEType cp, TreeItem checked) {
 		checked.setText(cp.toString());
 		checked.setChecked(cp.isEnabled());
 		checked.setData(cp);
 	}
 
 }
