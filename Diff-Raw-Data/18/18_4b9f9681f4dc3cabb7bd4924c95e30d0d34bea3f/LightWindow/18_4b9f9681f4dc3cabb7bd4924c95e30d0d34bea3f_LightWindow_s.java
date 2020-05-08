 package com.partyrock.gui;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 
 import com.partyrock.LightMaster;
 import com.partyrock.element.ElementController;
 
 /**
  * The main GUI window
  * @author Matthew
  * 
  */
 public class LightWindow {
 	private LightMaster master;
 	private LightWindowManager manager;
 	private Shell shell;
 	private ToolBar toolbar;
 	private ScrolledComposite tableScroll;
 	private Table table;
 
 	public LightWindow(LightMaster master, LightWindowManager manager) {
 		this.master = master;
 		this.manager = manager;
 
 		// Construct the GUI shell
 		this.shell = new Shell(manager.getDisplay());
 		shell.setText("Light Master");
 
 		// Layout with 1 column, and no equal width columns
 		shell.setLayout(new GridLayout(1, false));
 
 		// Create Toolbar
 		toolbar = new ToolBar(shell, SWT.HORIZONTAL);
 
 		// Generate Toolbar items
 		for (int i = 0; i < 8; i++) {
 			ToolItem item = new ToolItem(toolbar, SWT.PUSH);
 			item.setText("Item " + (i + 1));
 		}
 
 		// Finish Toolbar init
 		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		toolbar.pack();
 
 		// Main table scroll container
 		tableScroll = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		tableScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		tableScroll.setExpandHorizontal(true);
 		tableScroll.setExpandVertical(true);
 
 		// Actually make the table
 		table = new Table(tableScroll, SWT.MULTI | SWT.FULL_SELECTION);
 		table.setHeaderVisible(false);
 		table.setLinesVisible(false);
 		table.setToolTipText("");
 //		table.addKeyListener(this);
 
 		// Set the scroll contianer's content
 		tableScroll.setContent(table);
 
 		// Construct the table's columns (label + content)
 		int columnCount = 2;
 		for (int a = 0; a < columnCount; a++) {
 			TableColumn column = new TableColumn(table, SWT.NONE);
 
 			// This is hidden anyways, only here to help debugging if necessary
 			column.setText("Column " + a);
 		}
		
		
 
 		// Load in TableItems
 		updateElements();
 
 		shell.open();
 
 		while (!shell.isDisposed()) {
 			if (!manager.getDisplay().readAndDispatch()) {
 				manager.getDisplay().sleep();
 			}
 		}
 
 		manager.getDisplay().dispose();
 
 	}
 
 	/**
 	 * Add all elements from LightMaster into the Table. This will keep the
 	 * current order, so it can be called whenever, and the user won't lose the
 	 * order they've specified. Elements added are added to the bottom.
 	 */
 	public void updateElements() {
 		ArrayList<ElementController> elements = master.getElements();
 
 		for (TableItem item : table.getItems()) {
 			ElementController controller = (ElementController) item.getData();
 			if (elements.contains(controller)) {
 				elements.remove(controller);
 				addElementAsRow(controller);
 			}
 			item.dispose();
 		}
 	}
 
 	/**
 	 * Adds a specific element to the end of the table, and sets the element as
 	 * the item's data.
 	 * @param element the element to add
 	 */
 	public void addElementAsRow(ElementController element) {
 		TableItem item = new TableItem(table, SWT.NONE);
 		item.setText(element.getName());
 		item.setData(element);
 	}
 }
