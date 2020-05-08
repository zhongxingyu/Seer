 package net.sourceforge.eclipseccase.views;
 
 import java.util.Vector;
 import net.sourceforge.clearcase.ElementHistory;
 import net.sourceforge.eclipseccase.ui.actions.CompareWithVersionAction;
 import net.sourceforge.eclipseccase.ui.actions.VersionTreeAction;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class HistoryView extends ViewPart {
 	private Table historyTable;
 
 	private Label elementLabel;
 
 	private IResource element = null;
 
 	private Vector<ElementHistory> elemHistory = null;
 
 	private Action versionTreeAction;
 
 	private Action compareAction;
 
	private Action openAction;

 	private Menu historyMenu;
 
 	MenuItem versionTreeItem;
 
 	MenuItem compareMenuItem;
 
 	@Override
 	public void createPartControl(Composite parent) {
 		Group historyGroup = new Group(parent, SWT.BORDER_SOLID);
 		historyGroup.setLayout(new GridLayout(1, false));
 		elementLabel = new Label(historyGroup, SWT.LEFT);
 		elementLabel.setText("Element: no");
 
 		GridData data = new GridData();
 		data.grabExcessHorizontalSpace = true;
 		data.horizontalAlignment = GridData.FILL;
 		elementLabel.setLayoutData(data);
 
 		historyTable = new Table(historyGroup, SWT.BORDER_SOLID | SWT.MULTI | SWT.FULL_SELECTION);
 
 		data = new GridData();
 		data.grabExcessHorizontalSpace = true;
 		data.grabExcessVerticalSpace = true;
 		data.horizontalAlignment = GridData.FILL;
 		data.verticalAlignment = GridData.FILL;
 		historyGroup.setLayoutData(data);
 
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		gd.horizontalSpan = 4;
 		historyTable.setLayoutData(gd);
 		historyTable.setHeaderVisible(true);
 		TableColumn date = new TableColumn(historyTable, SWT.LEFT);
 		TableColumn user = new TableColumn(historyTable, SWT.LEFT);
 		TableColumn version = new TableColumn(historyTable, SWT.LEFT);
 		TableColumn label = new TableColumn(historyTable, SWT.LEFT);
 		TableColumn comment = new TableColumn(historyTable, SWT.LEFT);
 		date.setText("Date");
 		user.setText("User");
 		version.setText("Version");
 		label.setText("Label");
 		comment.setText("Comment");
 		date.setWidth(150);
 		user.setWidth(100);
 		version.setWidth(200);
 		label.setWidth(200);
 		comment.setWidth(400);
 
 		historyMenu = new Menu(parent.getShell(), SWT.POP_UP);
 		compareMenuItem = new MenuItem(historyMenu, SWT.PUSH);
 		compareMenuItem.setText("Compare with...");
 		compareMenuItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				compare();
 			}
 		});
 		compareMenuItem.setEnabled(false);
 
 		versionTreeItem = new MenuItem(historyMenu, SWT.PUSH);
 		versionTreeItem.setText("Show Version Tree");
 		versionTreeItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				versionTree();
 			}
 		});
 		versionTreeItem.setEnabled(false);
 
 		historyTable.setMenu(historyMenu);
 
 		historyTable.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (historyTable.getSelectionCount() == 1) {
 					compareMenuItem.setText("Compare Active with selected");
 					compareMenuItem.setEnabled(true);
 					compareAction.setEnabled(true);
 				} else if (historyTable.getSelectionCount() == 2) {
 					compareMenuItem.setText("Compare selected versions");
 					compareMenuItem.setEnabled(true);
 					compareAction.setEnabled(true);
 				} else {
 					compareMenuItem.setText("Compare with...");
 					compareMenuItem.setEnabled(false);
 					compareAction.setEnabled(false);
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// System.out.println("select default");
 			}
 		});
 
 		compareAction = new Action() {
 			@Override
 			public void run() {
 				compare();
 			}
 		};
 
 		versionTreeAction = new Action() {
 			@Override
 			public void run() {
 				versionTree();
 			}
 		};
		openAction = new Action() {
			@Override
			public void run() {
				open();
			}
		};

 		compareAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/diff.png"));
 		compareAction.setToolTipText("Compare with history");
 		compareAction.setEnabled(false);
 		versionTreeAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/tree.png"));
 		versionTreeAction.setToolTipText("Open Version Tree");
 		versionTreeAction.setEnabled(false);
 
 		getViewSite().getActionBars().getToolBarManager().add(compareAction);
 		getViewSite().getActionBars().getToolBarManager().add(versionTreeAction);
 		getViewSite().getActionBars().getToolBarManager().update(true);
 
 	}
 
 	@Override
 	public void setFocus() {
 
 	}
 
 	public void setHistoryInformation(IResource element, Vector<ElementHistory> history) {
 		this.element = element;
 		this.elemHistory = history;
 
 		historyTable.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				historyTable.removeAll();
 				if (HistoryView.this.element != null) {
 					elementLabel.setText(HistoryView.this.element.getLocation().toOSString());
 				}
 				for (ElementHistory elem : elemHistory) {
 					String[] row = new String[5];
 
 					row[0] = elem.getDate();
 					row[1] = elem.getuser();
 					row[2] = elem.getVersion();
 					row[3] = elem.getLabel();
 					row[4] = elem.getComment();
 
 					TableItem rowItem = new TableItem(historyTable, SWT.NONE);
 					rowItem.setText(row);
 				}
 
 				historyTable.update();
 				versionTreeAction.setEnabled(true);
 				versionTreeItem.setEnabled(true);
 				getViewSite().getActionBars().getToolBarManager().update(true);
 			}
 		});
 	}
 
 	private void open() {
 
 	}
 
 	private void versionTree() {
 		VersionTreeAction action = new VersionTreeAction();
 		action.setResource(element);
 		action.execute((IAction) null);
 	}
 
 	private void compare() {
 		if (historyTable.getSelectionCount() == 1 || historyTable.getSelectionCount() == 2) {
 			CompareWithVersionAction action = new CompareWithVersionAction();
 			action.setElement(element);
 			if (historyTable.getSelectionCount() == 1) {
 				action.setVersionA(historyTable.getSelection()[0].getText(2));
 			} else if (historyTable.getSelectionCount() == 2) {
 				action.setVersionA(historyTable.getSelection()[0].getText(2));
 				action.setVersionB(historyTable.getSelection()[1].getText(2));
 			}
 			action.execute((IAction) null);
 		}
 	}
 
 }
