 package com.partyrock.gui;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 
 import com.partyrock.LightMaster;
 import com.partyrock.element.ElementController;
 import com.partyrock.gui.elements.ElementDisplay;
 import com.partyrock.gui.elements.ElementTableRenderer;
 import com.partyrock.gui.elements.ElementUpdater;
 import com.partyrock.gui.elements.ElementsEditor;
 import com.partyrock.gui.uc.UCEditor;
 import com.partyrock.tools.PartyToolkit;
 
 /**
  * The main GUI window
  * @author Matthew
  * 
  */
 public class LightWindow implements ElementTableRenderer, ElementDisplay {
 	private LightMaster master;
 	private LightWindowManager windowManager;
 	private Shell shell;
 	private ToolBar toolbar;
 	private ScrolledComposite tableScroll;
 	private Table table;
 	private ElementsEditor editor;
 
 	public LightWindow(LightMaster master, LightWindowManager manager) {
 		this.master = master;
 		this.windowManager = manager;
 
 		this.windowManager.addElementDisplay(this);
 
 		// Construct the GUI shell
 		this.shell = new Shell(manager.getDisplay());
 		shell.setText("Light Master");
 
 		// Layout with 1 column, and no equal width columns
 		shell.setLayout(new GridLayout(1, false));
 
 		// Create Toolbar
 		toolbar = new ToolBar(shell, SWT.HORIZONTAL);
 
 		// Generate Toolbar items
 		ToolItem elementsEditorButton = new ToolItem(toolbar, SWT.PUSH);
 		elementsEditorButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				showElementsEditor();
 			}
 		});
 		elementsEditorButton.setText("Elements Editor");
 
 		ToolItem ucEditorButton = new ToolItem(toolbar, SWT.PUSH);
 		ucEditorButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				showUCEditor();
 			}
 		});
 		ucEditorButton.setText("C Editor");
 
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
 		ElementUpdater.updateElements(this, table);
 
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 
 		Menu menu_1 = new Menu(mntmFile);
 		mntmFile.setMenu(menu_1);
 
 		MenuItem mntmSaveLocation = new MenuItem(menu_1, SWT.NONE);
 		mntmSaveLocation.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				saveLocationFile();
 			}
 		});
 		mntmSaveLocation.setText("Save Location");
 
 		MenuItem mntmSaveLocationAs = new MenuItem(menu_1, SWT.NONE);
 		mntmSaveLocationAs.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				saveLocationFileAs();
 			}
 		});
 		mntmSaveLocationAs.setText("Save Location As");
 
 		MenuItem mntmLoadLocation = new MenuItem(menu_1, SWT.NONE);
 		mntmLoadLocation.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				loadLocation();
 			}
 		});
 		mntmLoadLocation.setText("Load Location");
 
 		shell.open();
 
 	}
 
 	/**
 	 * Performs the GUI loop. This method will not return until the GUI closes.
 	 */
 	public void loop() {
 		while (!shell.isDisposed()) {
 			if (!windowManager.getDisplay().readAndDispatch()) {
 				windowManager.getDisplay().sleep();
 			}
 		}
 
 		windowManager.getDisplay().dispose();
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
 
 		// This is awkwardly necessary here
 		ElementUpdater.packTable(table);
 	}
 
 	/**
 	 * Saves a location file (to the existing location if there already is one)
 	 */
 	public void saveLocationFile() {
 		if (master.getLocation() == null) {
 			// No location exists yet
 			this.saveLocationFileAs();
 		}
 
 		master.saveLocationFile();
 	}
 
 	/**
 	 * Saves a new location file (pops up the file prompt)
 	 */
 	public void saveLocationFileAs() {
 		File f = getLocationFileFromDialog(SWT.SAVE);
 
 		if (f.exists()) {
 			boolean override = PartyToolkit.openConfirm(shell, "Are you sure you wish to override the existing location file "
 					+ f.getName(), "Overwrite?");
 			if (override) {
 				// If we don't delete, PersistentSettings automatically merges
 				// the files which would be very bad (some of the old elements
 				// might hang around, but only some, and it would depend on the
 				// number of new elements. Basically, it would be very bad.)
 
 				f.delete();
 			} else {
 				return;
 			}
 		}
 		if (f != null) {
 			master.saveLocationToFile(f);
 		}
 	}
 
 	public void loadLocation() {
 		File f = getLocationFileFromDialog(SWT.OPEN);
 		if (f != null) {
 			master.loadLocation(f);
 		}
 	}
 
 	/**
 	 * Gets a new location file from a new FileDialog
 	 * @param style
 	 * @return
 	 */
 	public File getLocationFileFromDialog(int style) {
 		FileDialog dialog = new FileDialog(shell, style);
 		dialog.setFilterNames(new String[] { "Location Files", "All Files (*.*)" });
 		dialog.setFilterExtensions(new String[] { "*.loc", "*.*" });
 		dialog.setFilterPath(".");
 		dialog.setFileName("location.loc");
 		String fileName = dialog.open();
 		if (fileName != null && !fileName.trim().equals("")) {
 			try {
 				File file = new File(fileName);
 				return file;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		System.err.println("Error saving location file");
 		return null;
 	}
 
 	public LightMaster getMaster() {
 		return master;
 	}
 
 	public Shell getShell() {
 		return shell;
 	}
 
 	/**
 	 * Shows an ElementsEditor
 	 */
 	public void showElementsEditor() {
 		if (editor != null && !editor.isDisposed()) {
 			editor.getShell().forceActive();
 		} else {
 			editor = new ElementsEditor(this);
 			windowManager.addElementDisplay(editor);
 			editor.open();
 		}
 	}
 
 	/**
 	 * Updates the elements in the table
 	 */
 	public void updateElements() {
 		ElementUpdater.updateElements(this, table);
 	}
 
 	public void showUCEditor() {
 		UCEditor editor = new UCEditor(this);
 		editor.open();
 
 	}
 
 	@Override
 	public boolean isDisposed() {
 		return shell.isDisposed();
 	}

	public LightWindowManager getWindowManager() {
		return windowManager;
	}
 }
