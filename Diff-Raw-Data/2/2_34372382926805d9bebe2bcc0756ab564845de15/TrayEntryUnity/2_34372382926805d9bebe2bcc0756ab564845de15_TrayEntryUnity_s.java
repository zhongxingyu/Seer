 package net.mms_projects.copyit.ui.swt;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import net.mms_projects.copyit.ClipboardManager;
 import net.mms_projects.copyit.DesktopIntegration;
 import net.mms_projects.copyit.Messages;
 import net.mms_projects.copyit.PathBuilder;
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.SettingsListener;
 import net.mms_projects.copyit.SyncManager;
 import net.mms_projects.copyit.app.CopyItDesktop;
 import net.mms_projects.copyit.ui.swt.forms.AboutDialog;
 import net.mms_projects.copyit.ui.swt.forms.PreferencesDialog;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.freedesktop.Notifications;
 import org.freedesktop.dbus.DBusSigHandler;
 import org.freedesktop.dbus.DBusSignal;
 import org.freedesktop.dbus.UInt32;
 import org.freedesktop.dbus.Variant;
 import org.freedesktop.dbus.exceptions.DBusException;
 
 public class TrayEntryUnity extends TrayEntry implements DBusSigHandler,
 		SettingsListener {
 
 	private DesktopIntegration integration;
 
 	public TrayEntryUnity(Settings settings, Shell activityShell,
 			SyncManager syncManager, ClipboardManager clipboardManager) {
 		super(settings, activityShell, syncManager, clipboardManager);
 
 		this.settings.addListener("sync.polling.enabled", this);
 
 		try {
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.ready.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_pull.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_push.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_open_preferences.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_open_about.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_quit.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_enable_sync.class, this);
 			CopyItDesktop.dbusConnection.addSigHandler(
 					DesktopIntegration.action_disable_sync.class, this);
 		} catch (DBusException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		URL inputUrl = getClass().getResource(
 				"/drawable-xxhdpi/app_icon_small_mono.png");
 		File dest = new File(PathBuilder.getCacheDirectory(), "tray_icon.png");
 		try {
 			FileUtils.copyURLToFile(inputUrl, dest);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		File script = CopyItDesktop.exportResource("desktop-integration.py");
 		if (script == null) {
 			script = new File("scripts/desktop-integration.py");
 		}
 
 		String[] command = new String[2];
 		command[0] = "python";
 		command[1] = script.getAbsolutePath();
 		try {
 			final Process process = Runtime.getRuntime().exec(command);
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					System.out.println("Stopping desktop integration script");
 					process.destroy();
 				}
 			});
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}

		this.syncManager.addListener(this);
 	}
 
 	@Override
 	public void handle(DBusSignal signal) {
 		if (signal instanceof DesktopIntegration.ready) {
 			String icon = new File(PathBuilder.getCacheDirectory(),
 					"tray_icon.png").getAbsolutePath();
 			try {
 				integration = CopyItDesktop.dbusConnection.getRemoteObject(
 						"net.mms_projects.copyit.DesktopIntegration", "/",
 						DesktopIntegration.class);
 				integration.setup(icon, icon);
 				integration.set_enabled(this.settings
 						.getBoolean("sync.polling.enabled"));
 			} catch (DBusException e) {
 				System.out
 						.println("Could not connect to desktop integration script although it reported as ready. Exiting...");
 				System.exit(1);
 			}
 
 		} else if (signal instanceof DesktopIntegration.action_push) {
 			clipboardManager.getContent();
 		} else if (signal instanceof DesktopIntegration.action_pull) {
 			syncManager.doPull();
 		} else if (signal instanceof DesktopIntegration.action_open_preferences) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					new PreferencesDialog(TrayEntryUnity.this.activityShell,
 							TrayEntryUnity.this.settings).open();
 				}
 			});
 		} else if (signal instanceof DesktopIntegration.action_open_about) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					new AboutDialog(TrayEntryUnity.this.activityShell, SWT.NONE)
 							.open();
 				}
 			});
 		} else if (signal instanceof DesktopIntegration.action_quit) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					TrayEntryUnity.this.activityShell.close();
 					System.exit(0);
 				}
 			});
 		} else if (signal instanceof DesktopIntegration.action_enable_sync) {
 			this.settings.set("sync.polling.enabled", true);
 			this.integration.set_enabled(true);
 		} else if (signal instanceof DesktopIntegration.action_disable_sync) {
 			this.settings.set("sync.polling.enabled", false);
 			this.integration.set_enabled(false);
 		}
 	}
 
 	@Override
 	public void onChange(String key, String value) {
 		if ("sync.polling.enabled".equals(key)) {
 			this.integration.set_enabled(Boolean.parseBoolean(value));
 		}
 	}
 
 	@Override
 	public void onPushed(String content, Date date) {
 		try {
 			Notifications notify = CopyItDesktop.dbusConnection
 					.getRemoteObject("org.freedesktop.Notifications",
 							"/org/freedesktop/Notifications",
 							Notifications.class);
 			Map<String, Variant<Byte>> hints = new HashMap<String, Variant<Byte>>();
 			hints.put("urgency", new Variant<Byte>((byte) 2));
 			notify.Notify("CopyIt", new UInt32(0), "", "CopyIt",
 					Messages.getString("text_content_pushed", content),
 					new LinkedList<String>(), hints, -1);
 		} catch (DBusException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onPulled(final String content, Date date) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				clipboardManager.setContent(content);
 			}
 		});
 		try {
 			Notifications notify = CopyItDesktop.dbusConnection
 					.getRemoteObject("org.freedesktop.Notifications",
 							"/org/freedesktop/Notifications",
 							Notifications.class);
 			Map<String, Variant<Byte>> hints = new HashMap<String, Variant<Byte>>();
 			hints.put("urgency", new Variant<Byte>((byte) 2));
 			notify.Notify("CopyIt", new UInt32(0), "", "CopyIt",
 					Messages.getString("text_content_pulled", content),
 					new LinkedList<String>(), hints, -1);
 		} catch (DBusException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
