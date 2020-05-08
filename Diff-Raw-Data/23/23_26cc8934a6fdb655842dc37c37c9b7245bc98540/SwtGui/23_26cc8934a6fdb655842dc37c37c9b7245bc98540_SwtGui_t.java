 package net.mms_projects.copy_it.ui;
 
 import java.util.Date;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
import net.mms_projects.copy_it.*;
 import net.mms_projects.copy_it.api.ServerApi;
 import net.mms_projects.copy_it.api.endpoints.GetBuildInfo;
 import net.mms_projects.copy_it.api.responses.JenkinsBuildResponse;
 import net.mms_projects.copy_it.app.CopyItDesktop;
 import net.mms_projects.copy_it.clipboard_backends.SwtBackend;
 import net.mms_projects.copy_it.integration.GnomeIntegration;
 import net.mms_projects.copy_it.integration.BasicSwtIntegration;
 import net.mms_projects.copy_it.integration.UnityIntegration;
 import net.mms_projects.copy_it.ui.swt.forms.DataQueue;
 import net.mms_projects.copy_it.ui.swt.forms.PreferencesDialog;
 import net.mms_projects.utils.OSValidator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SwtGui extends AbstractUi {
 
 	protected DataQueue queueWindow;
 	protected Display display;
 	protected Shell activityShell;
 
 	protected SyncManager syncManager;
 	protected ClipboardManager clipboardManager;
 
 	private final Logger log = LoggerFactory.getLogger(this.getClass());
 
 	public SwtGui(Settings settings, SyncManager syncManager,
 			ClipboardManager clipboardManager) {
 		super(settings);
 
 		this.syncManager = syncManager;
 		this.clipboardManager = clipboardManager;
 
 		try {
 			this.display = Display.getDefault();
 		} catch (UnsatisfiedLinkError error) {
 			error.printStackTrace();
 
			String message = Messages.getString("text.error.swt_loading", error.getMessage());
 			JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		SwtBackend swtClipboard = new SwtBackend(this.clipboardManager);
 
 		this.clipboardManager.addCopyService(swtClipboard);
 		this.clipboardManager.addPasteService(swtClipboard);
 		this.clipboardManager.addPollingService(swtClipboard);
 
 		this.activityShell = new Shell(this.display);
 
 		EnvironmentIntegration environmentIntegration = null;
 
 		if (OSValidator.isUnix()) {
 			String desktop = System.getenv("XDG_CURRENT_DESKTOP");
 			if (desktop.equalsIgnoreCase("Unity")) {
 				UnityIntegration environmentIntegrationUnity = new UnityIntegration(
 						CopyItDesktop.dbusConnection, this.settings,
 						this.activityShell, syncManager, clipboardManager);
 				syncManager.addListener(environmentIntegrationUnity);
 				clipboardManager.addListener(environmentIntegrationUnity);
 
 				environmentIntegration = environmentIntegrationUnity;
 			} else if (desktop.equalsIgnoreCase("GNOME")) {
 				BasicSwtIntegration environmentIntegrationSwt = new GnomeIntegration(
 						CopyItDesktop.dbusConnection, this.settings,
 						this.activityShell, syncManager,
 						clipboardManager);
 				syncManager.addListener(environmentIntegrationSwt);
 				clipboardManager.addListener(environmentIntegrationSwt);
 
 				environmentIntegration = environmentIntegrationSwt;
 			}
 		}
 		if (environmentIntegration == null) {
 			BasicSwtIntegration environmentIntegrationSwt = new BasicSwtIntegration(this.settings,
 					this.activityShell, syncManager,
 					clipboardManager);
 			syncManager.addListener(environmentIntegrationSwt);
 			clipboardManager.addListener(environmentIntegrationSwt);
 
 			environmentIntegration = environmentIntegrationSwt;
 		}
 
 		this.queueWindow = new DataQueue(this.activityShell, SWT.DIALOG_TRIM,
 				this.clipboardManager);
 	}
 
 	@Override
 	public void open() {
 		if (this.settings.get("run.firsttime") == null) {
 			MessageBox firstTimer = new MessageBox(this.activityShell);
 			firstTimer
					.setMessage(Messages.getString("text.firstrun"));
 			firstTimer.open();
 
 			new PreferencesDialog(this.activityShell, this.settings).open();
 			this.settings.set("run.firsttime", "nope");
 		}
 
 		// this.checkVersion();
 
 		syncManager.addListener(new SyncListener() {
 			@Override
 			public void onPushed(String content, Date date) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onPulled(final String content, Date date) {
 				if (!SwtGui.this.settings.getBoolean("sync.queue.enabled")) {
 					clipboardManager.requestSet(content);
 				}
 			}
 		});
 		syncManager.addListener(queueWindow);
 
 		this.queueWindow.setup();
 		this.queueWindow.setEnabled(this.settings
 				.getBoolean("sync.queue.enabled"));
 
 		this.settings.addListener("sync.queue.enabled", this.queueWindow);
 
 		while (!this.activityShell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	public void checkVersion() {
 		if (CopyItDesktop.getBuildNumber() != 0) {
 			this.display.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					int currentBuildNumber = CopyItDesktop.getBuildNumber();
 					int latestBuildNumber = 0;
 
 					ServerApi api = new ServerApi();
 					api.apiUrl = "http://jenkins.marlinc.nl";
 
 					JenkinsBuildResponse response = null;
 					try {
 						response = new GetBuildInfo(api).getLatestStableBuild();
 						latestBuildNumber = response.number;
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					if (latestBuildNumber > currentBuildNumber) {
 						log.info("A new version is available: {} ",
 								latestBuildNumber);
 						MessageBox box = new MessageBox(activityShell,
 								SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
 						box.setMessage("There is a new version available! Click yes to download the latest version.");
 						int boxResponse = box.open();
 						if (boxResponse == SWT.YES) {
 							OpenBrowser.openUrl(response.url);
 						}
 					}
 				}
 			});
 		}
 	}
 }
