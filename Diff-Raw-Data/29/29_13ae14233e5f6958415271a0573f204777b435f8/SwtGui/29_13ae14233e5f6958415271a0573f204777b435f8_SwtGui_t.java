 package net.mms_projects.copyit.ui;
 
import javax.swing.JFrame;
import javax.swing.JOptionPane;

 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.ui.swt.ActionProvider;
 import net.mms_projects.copyit.ui.swt.TrayEntry;
 import net.mms_projects.copyit.ui.swt.forms.PreferencesDialog;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 
 public class SwtGui extends AbstractUi {
 
	protected Display display;
	protected Tray tray;
 
 	// The system tray icon
	protected TrayEntry trayEntry;
 
	protected Shell activityShell;
 
 	public SwtGui(Settings settings) {
 		super(settings);
 
		try {
			this.display = Display.getDefault();
		} catch (UnsatisfiedLinkError error) {
			error.printStackTrace();
			
			String message = "It looks like there was an error while loading the SWT libraries.\n"
					+ "The following error was thrown:\n\n" + error.getMessage();
			JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		this.tray = display.getSystemTray();
		this.activityShell = new Shell(this.display);

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
