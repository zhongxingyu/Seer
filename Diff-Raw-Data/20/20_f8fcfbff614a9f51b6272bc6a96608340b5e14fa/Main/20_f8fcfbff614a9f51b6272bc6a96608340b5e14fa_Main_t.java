 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
 
 public class Main {
 	protected static final String DISCONNECTED_TEXT = "---";
 
 	protected Shell shlChatnk;
 	private TabFolder tabFolder;
 	private TabItem activeTabItem = null;
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Main window = new Main();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shlChatnk.open();
 		shlChatnk.layout();
 		while (!shlChatnk.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shlChatnk = new Shell();
 		shlChatnk.setSize(592, 500);
 		shlChatnk.setText("Chatnik");
 		shlChatnk.setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		Menu menu = new Menu(shlChatnk, SWT.BAR);
 		shlChatnk.setMenuBar(menu);
 
 		MenuItem mntmConnection = new MenuItem(menu, SWT.CASCADE);
 		mntmConnection.setText("Connection");
 
 		Menu menu_1 = new Menu(mntmConnection);
 		mntmConnection.setMenu(menu_1);
 
 		MenuItem mntmConnect = new MenuItem(menu_1, SWT.NONE);
 		mntmConnect.setText("Connect to");
 
 		MenuItem mntmDisconnect = new MenuItem(menu_1, SWT.NONE);
 		mntmDisconnect.setText("Disconnect");
 
 		tabFolder = new TabFolder(shlChatnk, SWT.NONE);
 
 		// LISTENERS
		shlChatnk.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				for (TabItem ti : tabFolder.getItems()) {
					if (ti == null)
						continue;

					Tab tab = (Tab) ti.getControl();

					if (tab != null && tab.connection != null) {
						tab.connection.send("LOGOUT");
						tab.connection.disconnect();
						tab.dispose();
					}
				}
			}
		});

 		tabFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				activeTabItem = tabFolder.getSelection()[0];
 			}
 		});
 
 		// Connect listeners
 		mntmConnect.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				final Login loginDialog = new Login(shlChatnk, SWT.NONE);
 
 				boolean ok = loginDialog.open();
 				if (ok) {
 					TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
 					tabItem.setText(DISCONNECTED_TEXT);
 
 					Tab tab = new Tab(tabFolder, SWT.NONE);
 					tabItem.setControl(tab);
 					activeTabItem = tabItem;
 					tabFolder.setSelection(activeTabItem);
 
 					// TODO check them
 					tab.connection = new Connection(loginDialog.getHost(),
 							Integer.parseInt(loginDialog.getPort()),
 							loginDialog.getLoginName());
 
 					if (!tab.connection.connected()) {
 						activeTabItem.setText(tab.connection.connectionInfo());
 						tab.dispose();
 						return;
 					}
 
 					tab.updateTab();
 					activeTabItem.setText(tab.connection.connectionInfo());
 				}
 			}
 		});
 
 		mntmDisconnect.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if (activeTabItem == null)
 					return;
 
 				Tab tab = (Tab) activeTabItem.getControl();
 
 				if (tab != null && tab.connection != null) {
 					tab.connection.send("LOGOUT");
 					tab.connection.disconnect();
 					tab.dispose();
 				}
 
 				activeTabItem.dispose();
 			}
 		});
 	}
 }
