 /**
  * 
  */
 package org.teagle.vcttool.view;
 
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabFolder2Adapter;
 import org.eclipse.swt.custom.CTabFolderEvent;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.teagle.vcttool.control.VctController;
 
 import teagle.vct.model.Vct;
 
 /**
  * @author sim
  * 
  */
 public class VctToolView implements IVctToolView {
 
 	abstract class VctToolViewSelectionAdapter extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			// tabFolderVcts.
 			final CTabItem tab = VctToolView.this.tabFolderVcts.getSelection();
 			final Vct vct = ((VctController) tab.getData()).getVct();
 
 			this.fireEvent(vct);
 		}
 
 		protected abstract void fireEvent(Vct vct);
 	}
 
 	class NewVctAdapter extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			VctToolView.this.fireNewEvent(event.widget.getData());
 		}
 	}
 
 	class RefreshAdapter extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			VctToolView.this.fireRefreshEvent(event.widget.getData());
 		}
 	}
 
 	class SaveAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireSaveEvent(vct);
 		}
 	}
 
 	class SaveAsAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireSaveAsEvent(vct);
 		}
 	}
 
 	class BookAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireBookingBookEvent(vct);
 		}
 
 	}
 
 	class ExportXmlAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireBookingExportXmlEvent(vct);
 		}
 	}
 
 	class BookingRefreshStateAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireBookingRefreshStateEvent(vct);
 		}
 	}
 
 	class BookingShowStateAdapter extends VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireBookingShowStateEvent(vct);
 		}
 	}
 
 	class BookingCloneAsUnprovisionedAdapter extends
 			VctToolViewSelectionAdapter {
 		@Override
 		protected void fireEvent(final Vct vct) {
 			VctToolView.this.fireBookingCloneUnprovisionedEvent(vct);
 		}
 	}
 
 	class DeleteAdapter extends SelectionAdapter {
 
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			final CTabItem tab = VctToolView.this.tabFolderVcts.getSelection();
 			final VctView view = (VctView) tab.getControl();
 			final Vct vct = ((VctController) tab.getData()).getVct();
 
 			VctToolView.this.fireDeleteVctEvent(vct, view, tab);
 		}
 
 	}
 
 	class StartAdapter extends SelectionAdapter {
 
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			final CTabItem tab = VctToolView.this.tabFolderVcts.getSelection();
 			final VctView view = (VctView) tab.getControl();
 			final Vct vct = ((VctController) tab.getData()).getVct();
 
 			VctToolView.this.fireStartVctEvent(vct, view, tab);
 		}
 
 	}
 
 	class StopAdapter extends SelectionAdapter {
 
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			final CTabItem tab = VctToolView.this.tabFolderVcts.getSelection();
 			final VctView view = (VctView) tab.getControl();
 			final Vct vct = ((VctController) tab.getData()).getVct();
 
 			VctToolView.this.fireStopVctEvent(vct, view, tab);
 		}
 
 	}
 
 	private Shell shell;
 
 	private SashForm sashForm;
 
 	private CTabFolder tabFolderVcts;
 
 	private CTabFolder tabFolderSelection;
 
 	private final Set<CommandListener> commandListeners = new CopyOnWriteArraySet<CommandListener>();
 	private final Set<BookingListener> bookingListeners = new CopyOnWriteArraySet<BookingListener>();
 
 	private final CoolBar coolBar;
 	private final ToolBar toolbarFile;
 	private ToolItem buttonStart;
 	private ToolItem buttonStop;
 
 	private ToolItem buttonDelete;
 
 	public VctToolView() {
 		this.initShell();
 		this.initMainMenu();
 
 		this.coolBar = new CoolBar(this.shell, SWT.FLAT);
 		this.coolBar
 				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		this.toolbarFile = new ToolBar(this.coolBar, SWT.FLAT);
 
 		this.createButtons();
 		this.initToolbar();
 		this.initTabs();
 	}
 
 	private void initTabs() {
 		this.sashForm = new SashForm(this.shell, SWT.NONE);
 		this.sashForm
 				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		this.tabFolderSelection = new CTabFolder(this.sashForm, SWT.BORDER);
 
 		this.tabFolderVcts = new CTabFolder(this.sashForm, SWT.BOTTOM
 				| SWT.BORDER);
 
 		this.tabFolderVcts.addCTabFolder2Listener(new CTabFolder2Adapter() {
 			@Override
 			public void close(final CTabFolderEvent event) {
 				/*
 				 * if (tabFolderVcts.getItemCount() == 1) event.doit = false;
 				 */
 			}
 		});
 
 		this.sashForm.setWeights(new int[] { 32, 68 });
 	}
 
 	private void initToolbar() {
 		Point size = this.toolbarFile.getSize();
 
 		final CoolItem coolItemFile = new CoolItem(this.coolBar, SWT.NONE);
 		coolItemFile.setControl(this.toolbarFile);
 
 		Point preferred = coolItemFile.computeSize(size.x, size.y);
 		coolItemFile.setPreferredSize(preferred);
 
 		final ToolBar toolbarRest = new ToolBar(this.coolBar, SWT.FLAT);
 
 		// String[] icons = { "export", "showtsk_tsk", null, "refresh",
 		// "showchild_mode" };
 
 		// for (String icon : icons) {
 		// if (icon != null) {
 		// ToolItem item = new ToolItem(toolbarRest, SWT.PUSH);
 		// InputStream in = getClass().getResourceAsStream("/icons/" + icon +
 		// ".gif");
 		// item.setImage(new Image(shell.getDisplay(), in));
 		// item.setData(icon);
 		//
 		// item.setToolTipText(icon);
 		// // item.addSelectionListener(this);
 		// } else {
 		// new ToolItem(toolbarRest, SWT.SEPARATOR);
 		// }
 		// }
 
 		toolbarRest.pack();
 		size = toolbarRest.getSize();
 
 		final CoolItem coolItemRest = new CoolItem(this.coolBar, SWT.NONE);
 		coolItemRest.setControl(toolbarRest);
 
 		preferred = coolItemRest.computeSize(size.x, size.y);
 		coolItemRest.setPreferredSize(preferred);
 
 		this.coolBar.pack();
 	}
 
 	private void initMainMenu() {
 		final Menu mainMenu = new Menu(this.shell, SWT.BAR);
 		this.shell.setMenuBar(mainMenu);
 
 		final MenuItem menuItemFile = new MenuItem(mainMenu, SWT.CASCADE);
 		menuItemFile.setText("&File");
 
 		final Menu menuFile = this.createFileMenu();
 		menuItemFile.setMenu(menuFile);
 
 		final MenuItem menuItemTools = new MenuItem(mainMenu, SWT.CASCADE);
 		menuItemTools.setText("&Tools");
 
 		final Menu menuTools = this.createToolsMenu();
 		menuItemTools.setMenu(menuTools);
 
 		final MenuItem menuItemBooking = new MenuItem(mainMenu, SWT.CASCADE);
 		menuItemBooking.setText("&Booking");
 
 		final Menu menuBooking = this.createBookingMenu();
 		menuItemBooking.setMenu(menuBooking);
 
 		// No content yet
 		/*
 		 * MenuItem menuItemHelp = new MenuItem(mainMenu, SWT.CASCADE);
 		 * menuItemHelp.setText("&Help");
 		 * 
 		 * Menu menuHelp = createHelpMenu(); menuItemHelp.setMenu(menuHelp);
 		 */
 
 	}
 
 	private void initShell() {
 		final Display display = new Display();
 		this.shell = new Shell(display);
		this.shell.setText("FITeagle VCTTool | Future Internet Technology Experimentation and Management Framework");
 		this.shell.setSize(900, 500);
 
 		final GridLayout layout = new GridLayout(1, false);
 		this.shell.setLayout(layout);
 
 		this.shell.setImage(new Image(this.shell.getDisplay(), this.getClass()
 				.getResourceAsStream("/icons/openteagle.png")));
 
 		this.shell.addShellListener(new ShellAdapter() {
 			@Override
 			public void shellClosed(final ShellEvent event) {
 				VctToolView.this.fireExitEvent(event.data);
 			}
 		});
 	}
 
 	private void createButtons() {
 		this.createSaveBookButtons();
 		this.createLifecycleButtons();
 		this.toolbarFile.pack();
 	}
 
 	private void createSaveBookButtons() {
 		this.createButton("/icons/new_wiz.gif", "Create new VCT",
 				new NewVctAdapter());
 		this.createButton("/icons/refresh.gif", "Refresh", new RefreshAdapter());
 
 		new ToolItem(this.toolbarFile, SWT.SEPARATOR);
 		this.createButton("/icons/save_edit.gif", "Save this VCT",
 				new SaveAdapter());
 		this.createButton("/icons/saveas_edit.gif", "Save this VCT as...",
 				new SaveAsAdapter());
 		this.createButton("/icons/book.png", "Book this VCT", new BookAdapter());
 	}
 
 	private void createLifecycleButtons() {
 		new ToolItem(this.toolbarFile, SWT.SEPARATOR);
 		this.buttonDelete = this.createButton("/icons/close.png",
 				"Delete this VCT", new DeleteAdapter());
 		this.buttonStart = this.createButton("/icons/start.png",
 				"Start this VCT", new StartAdapter());
 		this.buttonStop = this.createButton("/icons/stop.png", "Stop this VCT",
 				new StopAdapter());
 		this.setLifecycleButtonsEnabled(false);
 	}
 
 	private ToolItem createButton(final String pathToIcon,
 			final String tooltip, final SelectionAdapter selectionAdapter) {
 		final ToolItem button = new ToolItem(this.toolbarFile, SWT.PUSH);
 		button.setImage(new Image(this.shell.getDisplay(), this.getClass()
 				.getResourceAsStream(pathToIcon)));
 		button.setToolTipText(tooltip);
 		button.addSelectionListener(selectionAdapter);
 		return button;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showDialog(org.teagle.vcttool.view.VctToolDialog)
 	 */
 	public void showDialog(final VctToolDialog d) {
 		final Shell dlg = d.getShell();
 		dlg.setParent(this.shell);
 		dlg.open();
 		while (!dlg.isDisposed())
 			if (!dlg.getDisplay().readAndDispatch())
 				dlg.getDisplay().sleep();
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showMessage(java.lang.String, java.lang.String, int)
 	 */
 	public void showMessage(String msg, String title, int style) {
 		if (style < 0)
 			style = SWT.ICON_INFORMATION;
 		if (title == null)
 			title = "";
 		if (msg == null)
 			msg = "";
 		final MessageBox box = new MessageBox(this.getShell(), style
 				| SWT.PRIMARY_MODAL);
 		box.setText(title);
 		box.setMessage(msg);
 		box.open();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showError(java.lang.String, java.lang.String)
 	 */
 	public void showError(final String msg, String title) {
 		if (title == null)
 			title = "Error";
 
 		this.showMessage(msg, title, SWT.ICON_ERROR);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showError(java.lang.String)
 	 */
 	public void showError(final String msg) {
 		this.showError(msg, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showException(java.lang.Throwable, java.lang.String)
 	 */
 	public void showException(final Throwable t, final String title) {
 		t.printStackTrace();
 		this.showMessage(t.getClass().getSimpleName() + ": " + t.getMessage()
 				+ " (java console has more details)", title, SWT.ICON_ERROR
 				| SWT.OK);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#showException(java.lang.Throwable)
 	 */
 	public void showException(final Throwable t) {
 		this.showException(t, "An error has occured.");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#run()
 	 */
 	public void run() {
 		while (!this.shell.isDisposed())
 			if (!this.shell.getDisplay().readAndDispatch())
 				this.shell.getDisplay().sleep();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#close()
 	 */
 	public void close() {
 		this.shell.close();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#getShell()
 	 */
 	public Shell getShell() {
 		return this.shell;
 	}
 
 	private Menu createFileMenu() {
 		final Menu menuFile = new Menu(this.shell, SWT.DROP_DOWN);
 
 		final MenuItem menuItemNew = new MenuItem(menuFile, SWT.PUSH);
 		menuItemNew.setText("&New Vct");
 		menuItemNew.setImage(new Image(this.shell.getDisplay(), this.getClass()
 				.getResourceAsStream("/icons/new_wiz.gif")));
 		menuItemNew.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent event) {
 				VctToolView.this.fireNewEvent(event.widget.getData());
 			}
 		});
 
 		new MenuItem(menuFile, SWT.SEPARATOR);
 
 		//
 		// MenuItem menuItemOpen = new MenuItem(menuFile, SWT.PUSH);
 		// menuItemOpen.setText("&Open Vct");
 		// // menuItemNew.setImage(new Image(shell.getDisplay(),
 		// getClass().getResourceAsStream("/icons/new_wiz.gif")));
 		// menuItemOpen.addSelectionListener(new SelectionAdapter() {
 		// @Override
 		// public void widgetSelected(SelectionEvent event) {
 		// // fireOpenEvent(event.widget.getData());
 		// fireOpenEvent();
 		// }
 		// });
 		//
 		// new MenuItem(menuFile, SWT.SEPARATOR);
 
 		final MenuItem menuItemSave = new MenuItem(menuFile, SWT.PUSH);
 		menuItemSave.setText("&Save");
 		menuItemSave.addSelectionListener(new SaveAdapter());
 
 		final MenuItem menuItemSaveAs = new MenuItem(menuFile, SWT.PUSH);
 		menuItemSaveAs.setText("Save &As...");
 		menuItemSaveAs.addSelectionListener(new SaveAsAdapter());
 
 		new MenuItem(menuFile, SWT.SEPARATOR);
 
 		final MenuItem menuItemExit = new MenuItem(menuFile, SWT.PUSH);
 		menuItemExit.setText("E&xit");
 		menuItemExit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent event) {
 				VctToolView.this.fireExitEvent(event.widget.getData());
 				VctToolView.this.shell.close();
 			}
 		});
 
 		return menuFile;
 	}
 
 	private Menu createToolsMenu() {
 		final Menu menuTools = new Menu(this.shell, SWT.DROP_DOWN);
 
 		final MenuItem menuItemConsole = new MenuItem(menuTools, SWT.PUSH);
 		menuItemConsole.setText("&Console");
 		menuItemConsole.setEnabled(false);
 
 		new MenuItem(menuTools, SWT.SEPARATOR);
 
 		final MenuItem menuItemPreferences = new MenuItem(menuTools, SWT.PUSH);
 		menuItemPreferences.setText("&Preferences...");
 		menuItemPreferences.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent event) {
 				VctToolView.this.firePreferencesEvent(event.data);
 			}
 		});
 
 		return menuTools;
 	}
 
 	private Menu createBookingMenu() {
 		final Menu menuBooking = new Menu(this.shell, SWT.DROP_DOWN);
 
 		final MenuItem menuItemBook = new MenuItem(menuBooking, SWT.PUSH);
 		menuItemBook.setText("&Book");
 		menuItemBook.addSelectionListener(new BookAdapter());
 
 		final MenuItem menuItemExport = new MenuItem(menuBooking, SWT.PUSH);
 		menuItemExport.setText("&Export to XML");
 		menuItemExport.addSelectionListener(new ExportXmlAdapter());
 
 		new MenuItem(menuBooking, SWT.SEPARATOR);
 
 		final MenuItem menuItemRefresh = new MenuItem(menuBooking, SWT.PUSH);
 		menuItemRefresh.setText("&Refresh booking state");
 		menuItemRefresh.addSelectionListener(new BookingRefreshStateAdapter());
 
 		final MenuItem menuItemShowState = new MenuItem(menuBooking, SWT.PUSH);
 		menuItemShowState.setText("&Show booking state");
 		menuItemShowState.addSelectionListener(new BookingShowStateAdapter());
 
 		new MenuItem(menuBooking, SWT.SEPARATOR);
 
 		final MenuItem menuItemClone = new MenuItem(menuBooking, SWT.PUSH);
 		menuItemClone.setText("&Clone all resources as unprovisioned");
 		menuItemClone
 				.addSelectionListener(new BookingCloneAsUnprovisionedAdapter());
 
 		return menuBooking;
 	}
 
 	/*
 	 * private Menu createHelpMenu() { Menu menuHelp = new Menu(shell,
 	 * SWT.DROP_DOWN);
 	 * 
 	 * MenuItem menuItemContents = new MenuItem(menuHelp, SWT.PUSH);
 	 * menuItemContents.setText("&Contents");
 	 * //menuItemContents.addSelectionListener(this);
 	 * menuItemContents.setEnabled(false);
 	 * 
 	 * MenuItem menuItemAbout = new MenuItem(menuHelp, SWT.PUSH);
 	 * menuItemAbout.setText("&About...");
 	 * //menuItemAbout.addSelectionListener(this);
 	 * menuItemAbout.setEnabled(false);
 	 * 
 	 * return menuHelp; }
 	 */
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#addCommandListener(org.teagle.vcttool.view.CommandListener)
 	 */
 	public void addCommandListener(final CommandListener listener) {
 		this.commandListeners.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#removeCommandListener(org.teagle.vcttool.view.CommandListener)
 	 */
 	public void removeCommandListener(final CommandListener listener) {
 		this.commandListeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#addBookingListener(org.teagle.vcttool.view.BookingListener)
 	 */
 	public void addBookingListener(final BookingListener listener) {
 		this.bookingListeners.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#removeBookingListener(org.teagle.vcttool.view.BookingListener)
 	 */
 	public void removeBookingListener(final BookingListener listener) {
 		this.bookingListeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#getVctPane()
 	 */
 	public CTabFolder getVctPane() {
 		return this.tabFolderVcts;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#addVctView(org.teagle.vcttool.view.VctView, org.teagle.vcttool.control.VctController, java.lang.String)
 	 */
 	public void addVctView(final VctView vctView, final VctController data,
 			final String name) {
 		final CTabItem tabItem = new CTabItem(this.tabFolderVcts, SWT.CLOSE);
 		tabItem.setText(name);
 		tabItem.setData(data);
 
 		tabItem.setControl(vctView);
 
 		this.tabFolderVcts.setSelection(tabItem);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#getSelectionPane()
 	 */
 	public CTabFolder getSelectionPane() {
 		return this.tabFolderSelection;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#addSelectionControl(java.lang.String, org.eclipse.swt.widgets.Control, int)
 	 */
 	public void addSelectionControl(final String name, final Control control,
 			final int i) {
 
 		final CTabItem tabItem = i != -1 ? new CTabItem(
 				this.tabFolderSelection, SWT.NONE, i) : new CTabItem(
 				this.tabFolderSelection, SWT.NONE);
 		tabItem.setText(name);
 
 		tabItem.setControl(control);
 
 		this.tabFolderSelection.setSelection(i != -1 ? i
 				: this.tabFolderSelection.indexOf(tabItem));
 	}
 
 	private void fireExitEvent(final Object data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onExit();
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void firePreferencesEvent(final Object data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onPreferences();
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireNewEvent(final Object data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onNew();
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireRefreshEvent(final Object data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onRefresh();
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/*
 	 * private void fireOpenEvent() { for (Iterator<CommandListener> it =
 	 * commandListeners.iterator(); it.hasNext(); ) { CommandListener listener =
 	 * it.next(); try { listener.onOpen(); } catch (RuntimeException e) {
 	 * e.printStackTrace(); } } }
 	 */
 
 	private void fireSaveEvent(final Vct data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onSave(this, data);
 			} catch (final RuntimeException e) {
 				// log output
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireSaveAsEvent(final Vct data) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onSaveAs(this, data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireBookingExportXmlEvent(final Vct data) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onExportXml(data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireBookingRefreshStateEvent(final Vct data) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onRefreshState(data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireBookingShowStateEvent(final Vct data) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onShowState(data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireBookingCloneUnprovisionedEvent(final Vct data) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onCloneUnprovisioned(data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireDeleteVctEvent(final Vct data, final VctView view,
 			final CTabItem tab) {
 		for (CommandListener listener : this.commandListeners) {
 			try {
 				listener.onDelete(this, data, view, tab);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireStartVctEvent(final Vct data, final VctView view,
 			final CTabItem tab) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onStart(this, data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireStopVctEvent(final Vct data, final VctView view,
 			final CTabItem tab) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onStop(this, data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void fireBookingBookEvent(final Vct data) {
 		for (BookingListener listener : this.bookingListeners) {
 			try {
 				listener.onBook(this, data);
 			} catch (final RuntimeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.teagle.vcttool.view.IVctToolView#setLifecycleButtonsEnabled(boolean)
 	 */
 	public void setLifecycleButtonsEnabled(final boolean bool) {
 		this.buttonStart.setEnabled(bool);
 		this.buttonStop.setEnabled(bool);
 		this.buttonDelete.setEnabled(bool);
 	}
 
 }
