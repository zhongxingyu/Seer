 package net.mms_projects.copyit.ui;
 
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.ui.swt.ActionProvider;
 import net.mms_projects.copyit.ui.swt.TrayEntry;
 import net.mms_projects.copyit.ui.swt.forms.PreferencesDialog;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 
 public class SwtGui extends AbstractUi {
 
	protected Display display = Display.getDefault();
	protected Tray tray = display.getSystemTray();
 
 	// The system tray icon
	protected TrayEntry trayEntry = null;
 
	protected Shell activityShell = new Shell(this.display);
 
 	public SwtGui(Settings settings) {
 		super(settings);
 
 		ActionProvider actionProvider = new ActionProvider(this.display,
 				settings);
 		this.trayEntry = new TrayEntry(this.tray, actionProvider,
 				this.activityShell);
 	}
 
 	@Override
 	public void open() {
 		if (this.settings.get("device.id") == null) {
 			MessageBox firstTimer = new MessageBox(this.activityShell);
 			firstTimer
 					.setMessage("It appairs this is the first time you started the app. "
 							+ "The preferences will open to setup your login.");
 			firstTimer.open();
 
 			new PreferencesDialog(this.activityShell, this.settings).open();
 		}
 
 		while (!this.activityShell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 }
