 package net.mms_projects.copyit.ui.swt;
 
 import net.mms_projects.copyit.ui.swt.forms.PreferencesDialog;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolTip;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;
 
 public class TrayEntry {
 
 	protected Display display = Display.getDefault();
 	protected Menu menu;
 	protected TrayItem trayItem;
 
 	protected Tray tray;
 	protected ActionProvider actionProvider;
 
 	private MenuItem menuItemCopyIt;
 	private MenuItem menuItemPasteIt;
 
 	private Shell activityShell;
 
 	public TrayEntry(Tray tray, final ActionProvider actionProvider,
 			Shell activityShell) {
 		this.tray = tray;
 		this.actionProvider = actionProvider;
 
 		this.trayItem = new TrayItem(tray, 0);
 
		this.trayItem.setImage(SWTResourceManager.getImage("res/drawable-xxhdpi/app_icon_small.png"));
 		this.trayItem.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				System.out.println("selection");
 			}
 		});
 
 		this.activityShell = activityShell;
 
 		this.createMenu();
 	}
 
 	public void enableFeatures() {
 		this.menuItemPasteIt.setEnabled(true);
 		this.menuItemCopyIt.setEnabled(true);
 	}
 
 	public void disableFeatures() {
 		this.menuItemPasteIt.setEnabled(false);
 		this.menuItemCopyIt.setEnabled(false);
 	}
 
 	protected void createMenu() {
 		this.menu = new Menu(this.activityShell, SWT.POP_UP);
 
 		this.menuItemCopyIt = new MenuItem(menu, SWT.PUSH);
 		this.menuItemCopyIt.setText("Copy it");
 		this.menuItemCopyIt.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				TrayEntry.this.actionProvider.doDataSet();
 
 				final ToolTip tip = new ToolTip(TrayEntry.this.activityShell,
 						SWT.BALLOON | SWT.ICON_INFORMATION);
 				tip.setText("Notification");
 				tip.setMessage("Your clipboard has been pushed to the server.");
 				trayItem.setToolTip(tip);
 				tip.setVisible(true);
 			}
 		});
 
 		this.menuItemPasteIt = new MenuItem(menu, SWT.PUSH);
 		this.menuItemPasteIt.setText("Paste it");
 		this.menuItemPasteIt.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				TrayEntry.this.actionProvider.doDataGet();
 				
 				final ToolTip tip = new ToolTip(TrayEntry.this.activityShell,
 						SWT.BALLOON | SWT.ICON_INFORMATION);
 				tip.setText("Notification");
 				tip.setMessage("Your clipboard has been pulled from the server.");
 				trayItem.setToolTip(tip);
 				tip.setVisible(true);
 			}
 		});
 
 		MenuItem menuItemPreferences = new MenuItem(menu, SWT.PUSH);
 		menuItemPreferences.setText("Preferences");
 		menuItemPreferences.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				new PreferencesDialog(TrayEntry.this.activityShell,
 						TrayEntry.this.actionProvider.settings).open();
 			}
 		});
 
 		MenuItem menuItemExit = new MenuItem(menu, SWT.PUSH);
 		menuItemExit.setText("Exit");
 		menuItemExit.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				TrayEntry.this.activityShell.close();
 			}
 		});
 
 		this.trayItem.addListener(SWT.MenuDetect, new Listener() {
 			public void handleEvent(Event event) {
 				TrayEntry.this.menu.setVisible(true);
 			}
 		});
 	}
 
 }
