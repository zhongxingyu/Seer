 package com.androidhuman.ctsprepare.shell;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import com.androidhuman.ctsprepare.data.BasicDeviceInfo;
 import com.androidhuman.ctsprepare.data.Task;
 import com.androidhuman.ctsprepare.dialog.EditGoogleAccountDialog;
 import com.androidhuman.ctsprepare.dialog.EditSdkPathDialog;
 import com.androidhuman.ctsprepare.dialog.EditWifiApDialog;
 import com.androidhuman.ctsprepare.util.AdbCommand;
 import com.androidhuman.ctsprepare.util.AdbCommand.AdbCommandException;
 import com.androidhuman.ctsprepare.util.AdbCommand.AdbCommandResultListener;
 import com.androidhuman.ctsprepare.util.AdbWrapper;
 import com.androidhuman.ctsprepare.util.AdbWrapper.KeyCode;
 import com.androidhuman.ctsprepare.util.AdbWrapper.ResultListener;
 import com.androidhuman.ctsprepare.util.Utils;
 
 public class Main {
 	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 	protected Shell shlPrects;
 	private Table table;
 	private ArrayList<BasicDeviceInfo> deviceList = new ArrayList<BasicDeviceInfo>();
 	
 	Button btnCopyMediaFiles;
 	Button btnInstallCtsdeviceadminapk;
 	
 	Button btnActivateCtsdeviceadminOn;
 	Button btnConfigureWifi;
 	Button btnConfigureScreenTimeout;
 	
 	Button btnRefresh;
 	Button btnStart;
 	
 	ProgressBar progressBar_1;
 	
 	ProgressBar progressBar;
 	Label lblReady;
 	
 	LinkedList<Task> taskList;
 	int currentDevIdx = -1;
 	boolean isInstallBlockDlgDismissed = false;
 	private Text txtLog;
 	private Button btnSetGoogleAccount;
 	
 	private boolean exitApp = false;
 	
 	private AdbCommand cmd = new AdbCommand();
 	private Button btnSetInternetAsDefault;
 	private Button btnDisableSyncSettings;
 	
 	/**
 	 * Launch the application.
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
 		if(exitApp){
 			shlPrects.close();
 			return;
 		}
 		shlPrects.open();
 		shlPrects.layout();
 		while (!shlPrects.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shlPrects = new Shell();
 		shlPrects.setSize(702, 643);
		shlPrects.setText("Pre-CTS 1.3 (20140121)");
 		
 		Group grpOptions = new Group(shlPrects, SWT.NONE);
 		grpOptions.setText("File / Installation");
 		grpOptions.setBounds(10, 10, 312, 154);
 		
 		btnCopyMediaFiles = new Button(grpOptions, SWT.CHECK);
 		btnCopyMediaFiles.setSelection(true);
 		btnCopyMediaFiles.setBounds(10, 46, 195, 16);
 		btnCopyMediaFiles.setText("Copy media files");
 		
 		btnInstallCtsdeviceadminapk = new Button(grpOptions, SWT.CHECK);
 		btnInstallCtsdeviceadminapk.setSelection(true);
 		btnInstallCtsdeviceadminapk.setBounds(10, 24, 292, 16);
 		btnInstallCtsdeviceadminapk.setText("Install CtsDeviceAdmin.apk");
 		
 		table = new Table(shlPrects, SWT.BORDER | SWT.FULL_SELECTION);
 		table.setBounds(11, 170, 666, 210);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		TableColumn tblclmnSerial = new TableColumn(table, SWT.NONE);
 		tblclmnSerial.setWidth(114);
 		tblclmnSerial.setText("Serial");
 		
 		TableColumn tblclmnModel = new TableColumn(table, SWT.NONE);
 		tblclmnModel.setWidth(144);
 		tblclmnModel.setText("Model");
 		
 		TableColumn tblclmnVersion = new TableColumn(table, SWT.NONE);
 		tblclmnVersion.setWidth(68);
 		tblclmnVersion.setText("Version");
 		
 		TableColumn tblclmnProgress = new TableColumn(table, SWT.NONE);
 		tblclmnProgress.setWidth(62);
 		tblclmnProgress.setText("Progress");
 		
 		TableColumn tblclmnDetails = new TableColumn(table, SWT.NONE);
 		tblclmnDetails.setWidth(259);
 		tblclmnDetails.setText("Details");
 		
 		Menu menu_2 = new Menu(table);
 		table.setMenu(menu_2);
 		
 		btnStart = new Button(shlPrects, SWT.NONE);
 		btnStart.setEnabled(false);
 		btnStart.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 
 				((Button)e.widget).setEnabled(false);	
 				btnRefresh.setVisible(false);
 				progressBar_1.setVisible(true);
 				
 				progressBar.setSelection(0);
 				
 				final boolean copyMediaFiles = btnCopyMediaFiles.getSelection();
 				final boolean installCtsDeviceAdmin = btnInstallCtsdeviceadminapk.getSelection();
 				
 				final boolean activateCtsDeviceAdmin = btnActivateCtsdeviceadminOn.getSelection();
 				final boolean configureWifi = btnConfigureWifi.getSelection();
 				final boolean configureScreenTimeout = btnConfigureScreenTimeout.getSelection();
 				final boolean setGoogleAccount = btnSetGoogleAccount.getSelection(); 
 				final boolean setInternetAsDefault = btnSetInternetAsDefault.getSelection();
 				final boolean installSettingsApp = btnDisableSyncSettings.getSelection();
 				
 				// Process task list for device
 				new Thread(new Runnable(){
 					public void run(){
 						
 						//AdbCommand cmd = new AdbCommand();
 						
 						try{
 							// for each devices
 							int devCount = deviceList.size();
 							for(int i=0; i<devCount; i++){
 								final BasicDeviceInfo info = deviceList.get(i);
 								currentDevIdx = i;
 								// Add tasks into task list
 								taskList = new LinkedList<Task>();
 								
 								if(installCtsDeviceAdmin){
 									taskList.add(new Task(Task.INSTALL_CTS_DEV_ADMIN));
 								}
 								if(copyMediaFiles){
 									taskList.add(new Task(Task.COPY_MEDIA));
 								}
 								if(activateCtsDeviceAdmin){
 									taskList.add(new Task(Task.ACTIVATE_DEV_ADMIN));
 								}
 								if(configureWifi){
 									taskList.add(new Task(Task.CONFIGURE_WIFI));
 								}
 								if(configureScreenTimeout){
 									taskList.add(new Task(Task.CONFIGURE_SCR_TIMEOUT));
 								}
 								if(setGoogleAccount){
 									taskList.add(new Task(Task.SET_GOOGLE_ACCOUNT));
 								}
 								if(setInternetAsDefault){
 									taskList.add(new Task(Task.SET_INTERNET_AS_DEFAULT));
 								}
 								if(installSettingsApp){
 									taskList.add(new Task(Task.DISABLE_MASTER_SYNC));
 								}
 										
 								final int initialTaskCnt = taskList.size();
 								
 								boolean automationJarInstalled = false;
 								while(true){
 									try{
 										final Task task = taskList.remove();
 										
 										updateProgress(currentDevIdx, 
 												initialTaskCnt-taskList.size(), initialTaskCnt, task.toString());
 																				
 										boolean result = false;
 										
 										switch(task.type){
 										case Task.INSTALL_CTS_DEV_ADMIN:
 											String apkPath = null;
 											String versionPre = info.version.substring(0, 3);
 											apkPath = "cts_device_admin/"+versionPre+"/CtsDeviceAdmin.apk";
 											
 											File daFile = new File(apkPath);
 											if(!daFile.exists()){
 												log(info.serial, "[FAIL] CTS Device admin not found on "+daFile.getAbsolutePath());
 												break;
 											}
 											
 											isInstallBlockDlgDismissed = false;
 											new Thread(new Runnable(){
 												public void run(){
 													try {
 														System.out.print("Waiting for dialog popup...");
 														log(info.serial, "Waiting for app verification service popup...");
 														Thread.sleep(7000);
 														if(!isInstallBlockDlgDismissed){
 															// Install block dialog still not dismissed.
 															System.out.print("Attempting dismiss install block dialog...");
 															log(info.serial, "Attempting dialog dismissal...");
 															AdbWrapper wrapper = new AdbWrapper();
 															wrapper.sendKeyCode(info.serial, AdbWrapper.KeyCode.DOWN);
 															Thread.sleep(500);
 															wrapper.sendKeyCode(info.serial, KeyCode.RIGHT);
 															Thread.sleep(500);
 															wrapper.sendKeyCode(info.serial, KeyCode.OK);
 															System.out.println("Done");
 															log(info.serial, "Popup successfully dismissed.");
 														}else{
 															System.out.println("Dialog dismissed or already accepted app verification service.");
 															log(info.serial, "Seems to no popup showed up.");
 														}
 													} catch (InterruptedException e) {
 														e.printStackTrace();
 													}
 												}
 											}).start();
 											result = new AdbCommand().executeSimple(String.format("-s %s install %s", info.serial, apkPath));
 											if(!result){
 												throw new IllegalStateException("Error installing CtsDeviceAdmin");
 											}
 											isInstallBlockDlgDismissed = true;
 											break;
 											
 										case Task.COPY_MEDIA:
 											// Check have a valid media file (cts_media directory existence)
 											File file = new File("cts_media");
 											if(!file.exists() || !file.isDirectory() || file.listFiles().length==0){
 												log(info.serial, "[FAIL] Failed to find cts media files in cts_media diretory.");
 												log("Please download CTS Media files from http://s.android.com/compatibility/downloads.html");
 												log("then extract its contents into cts_media directory.");
 												log("For more details, please read README.txt");
 												break;
 											}
 											// 1920*1080
 											updateStatusMessage(currentDevIdx, "Copying media (1920x1280)");
 											log(info.serial, "Copying media (1920x1280)");
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_short/1920x1080 /mnt/sdcard/test/bbb_short/1920x1080", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 1920*1080 short");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_full/1920x1080 /mnt/sdcard/test/bbb_full/1920x1080", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 1920*1080 full");
 											}
 											
 											// 1280*720
 											updateStatusMessage(currentDevIdx, "Copying media (1280x720)");
 											log(info.serial, "Copying media (1280x720)");
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_short/1280x720 /mnt/sdcard/test/bbb_short/1280x720", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 1280x720 short");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_full/1280x720 /mnt/sdcard/test/bbb_full/1280x720", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 1280x720 full");
 											}
 																						
 											// 720*480
 											updateStatusMessage(currentDevIdx, "Copying media (720x480)");
 											log(info.serial, "Copying media (720x480)");
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_short/720x480 /mnt/sdcard/test/bbb_short/720x480", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 720x480 short");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_full/720x480 /mnt/sdcard/test/bbb_full/720x480", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 720x480 full");
 											}
 											
 											// Others
 											updateStatusMessage(currentDevIdx, "Copying media (Others)");
 											log(info.serial, "Copying media (Others)");
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_short/176x144 /mnt/sdcard/test/bbb_short/176x144", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 176x144 short");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_full/176x144 /mnt/sdcard/test/bbb_full/176x144", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 176x144 full");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_short/480x360 /mnt/sdcard/test/bbb_short/480x360", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 480x360 short");
 											}
 											result = new AdbCommand().executeSimple(
 													String.format("-s %s push cts_media/bbb_full/480x360 /mnt/sdcard/test/bbb_full/480x360", info.serial));
 											if(!result){
 												throw new IllegalStateException("Error copying 480x360 full");
 											}
 											break;
 											
 										case Task.DISABLE_MASTER_SYNC:
 											// 
 											isInstallBlockDlgDismissed = false;
 											new Thread(new Runnable(){
 												public void run(){
 													try {
 														System.out.print("Waiting for dialog popup...");
 														log(info.serial, "Waiting for app verification service popup...");
 														Thread.sleep(5000);
 														if(!isInstallBlockDlgDismissed){
 															// Install block dialog still not dismissed.
 															System.out.print("Attempting dismiss install block dialog...");
 															log(info.serial, "Attempting dialog dismissal...");
 															AdbWrapper wrapper = new AdbWrapper();
 															wrapper.sendKeyCode(info.serial, AdbWrapper.KeyCode.DOWN);
 															Thread.sleep(500);
 															wrapper.sendKeyCode(info.serial, KeyCode.RIGHT);
 															Thread.sleep(500);
 															wrapper.sendKeyCode(info.serial, KeyCode.OK);
 															System.out.println("Done");
 															log(info.serial, "Popup successfully dismissed.");
 														}else{
 															System.out.println("Dialog dismissed or already accepted app verification service.");
 															log(info.serial, "Seems to no popup showed up.");
 														}
 													} catch (InterruptedException e) {
 														e.printStackTrace();
 													}
 												}
 											}).start();
 											
 											log(info.serial, "Installing Sync settings apk...");
 											result = new AdbCommand().executeSimple(String.format("-s %s install %s", info.serial, "automation/PreCtsAndroidSettingsApp.apk"));
 											if(!result){
 												throw new IllegalStateException("Error installing PreCtsAndroidSettings");
 											}
 											// App installed
 											isInstallBlockDlgDismissed = true;			
 											
 											// Launch application
 											log(info.serial, "Launching sync settings...");
 											new AdbCommand().execute(String.format("-s %s shell am start -a com.androidhuman.action.LAUNCH_ANDROID_SETTINGS", info.serial));
 											
 											// Pause for 5 seconds (wait for app to close)
 											try{ Thread.sleep(5000); }catch(Exception e){};
 											
 											// Done! (Nothing to do)
 											log(info.serial, "Disabled sync settings.");
 											break;
 											
 										case Task.ACTIVATE_DEV_ADMIN:
 											if(!automationJarInstalled){
 												log(info.serial, "Installing automation jar...");
 												automationJarInstalled = new AdbCommand().executeSimple(
 														String.format("-s %s push automation/PreCtsAutomator.jar /data/local/tmp", info.serial));
 												if(!automationJarInstalled){
 													throw new IllegalStateException("[FAIL] Error installing automation jar.");
 												}
 											}
 											new AdbCommand().execute(String.format("-s %s shell am start -S \"com.android.settings/.Settings\\$DeviceAdminSettingsActivity\"", info.serial));
 											new AdbCommand().execute(
 													String.format(
 															"-s %s shell uiautomator runtest PreCtsAutomator.jar -c com.androidhuman.ctsprepare.automator.PreCTSAutomate#testActivateDeviceAdmin", 
 															info.serial), 
 													new AdbCommandResultListener(){
 
 												@Override
 												public void onPreExecute() {
 													log(info.serial, task.toString()+" started");
 												}
 
 												@Override
 												public void onError(
 														String errmsg) {
 													log(info.serial, errmsg);
 												}
 
 												@Override
 												public void onOutput(String line) {
 													log(info.serial, line);
 												}
 
 												@Override
 												public void onSuccess() {
 													log(info.serial, task.toString()+" finished");
 												}
 												
 											});
 											break;
 											
 										case Task.CONFIGURE_WIFI:
 											if(!automationJarInstalled){
 												log(info.serial, "Installing automation jar...");
 												automationJarInstalled = new AdbCommand().executeSimple(
 														String.format("-s %s push automation/PreCtsAutomator.jar /data/local/tmp", info.serial));
 												if(!automationJarInstalled){
 													throw new IllegalStateException("Error installing automation jar.");
 												}
 											}
 											
 											// Push information file
 											boolean wifiPushed = new AdbCommand().executeSimple(String.format("-s %s push automation/wifi.info /data/local/tmp", info.serial));
 											if(!wifiPushed){
 												log(info.serial, "[FAIL] Failed to push WiFi information file on device.");
 												return;
 											}
 											
 											new AdbCommand().execute(String.format("-s %s shell am start -a android.settings.WIFI_SETTINGS", info.serial));
 											new AdbCommand().execute(
 													String.format(
 															"-s %s shell uiautomator runtest PreCtsAutomator.jar -c com.androidhuman.ctsprepare.automator.PreCTSAutomate#testActivateWifi", 
 															info.serial), 
 													new AdbCommandResultListener(){
 
 												@Override
 												public void onPreExecute() {
 													log(info.serial, task.toString()+" started");
 												}
 
 												@Override
 												public void onError(
 														String errmsg) {
 													log(info.serial, errmsg);
 												}
 
 												@Override
 												public void onOutput(String line) {
 													log(info.serial, line);
 												}
 
 												@Override
 												public void onSuccess() {
 													log(info.serial, task.toString()+" finished");
 												}
 												
 											});
 											break;
 											
 										case Task.CONFIGURE_SCR_TIMEOUT:
 											if(!automationJarInstalled){
 												log(info.serial, "Installing automation jar...");
 												automationJarInstalled = new AdbCommand().executeSimple(
 														String.format("-s %s push automation/PreCtsAutomator.jar /data/local/tmp", info.serial));
 												if(!automationJarInstalled){
 													throw new IllegalStateException("[FAIL] Error installing automation jar.");
 												}
 											}
 											new AdbCommand().execute(String.format("-s %s shell am start -a android.settings.DISPLAY_SETTINGS", info.serial));
 											new AdbCommand().execute(
 													String.format(
 															"-s %s shell uiautomator runtest PreCtsAutomator.jar -c com.androidhuman.ctsprepare.automator.PreCTSAutomate#testSetTimeout", 
 															info.serial), 
 													new AdbCommandResultListener(){
 
 												@Override
 												public void onPreExecute() {
 													log(info.serial, task.toString()+" started");
 												}
 
 												@Override
 												public void onError(
 														String errmsg) {
 													log(info.serial, errmsg);
 												}
 
 												@Override
 												public void onOutput(String line) {
 													log(info.serial, line);
 												}
 
 												@Override
 												public void onSuccess() {
 													log(info.serial, task.toString()+" finished");
 												}
 												
 											});
 											break;
 											
 										case Task.SET_GOOGLE_ACCOUNT:
 											File f = new File("automation/account.info");
 											if(!f.exists() || f.length()==0){
 												log(info.serial, "[FAIL] Google account info missing");
 												break;
 											}
 											if(!automationJarInstalled){
 												log(info.serial, "Installing automation jar...");
 												automationJarInstalled = cmd.executeSimple(
 														String.format("-s %s push automation/PreCtsAutomator.jar /data/local/tmp", info.serial));
 												if(!automationJarInstalled){
 													throw new IllegalStateException("Error installing automation jar.");
 												}
 											}
 											// Push information file
 											boolean accountPushed = new AdbCommand().executeSimple(String.format("-s %s push automation/account.info /data/local/tmp", info.serial));
 											if(!accountPushed){
 												log(info.serial, "[FAIL] Failed to push account information file on device.");
 												return;
 											}
 											
 											// Show add account activity
 											cmd.execute(String.format("-s %s shell am start -a android.settings.ADD_ACCOUNT_SETTINGS", info.serial));
 											// Start automation task
 											cmd.execute(
 													String.format(
 															"-s %s shell uiautomator runtest PreCtsAutomator.jar -c com.androidhuman.ctsprepare.automator.PreCTSAutomate#testAddGoogleAccount", 
 															info.serial), 
 													new AdbCommandResultListener(){
 
 												@Override
 												public void onPreExecute() {
 													log(info.serial, task.toString()+" started");
 												}
 
 												@Override
 												public void onError(
 														String errmsg) {
 													log(info.serial, errmsg);
 												}
 
 												@Override
 												public void onOutput(String line) {
 													log(info.serial, line);
 												}
 
 												@Override
 												public void onSuccess() {
 													log(info.serial, task.toString()+" finished");
 												}
 												
 											});
 											break;
 											
 										case Task.SET_INTERNET_AS_DEFAULT:
 											if(!automationJarInstalled){
 												log(info.serial, "Installing automation jar...");
 												automationJarInstalled = cmd.executeSimple(
 														String.format("-s %s push automation/PreCtsAutomator.jar /data/local/tmp", info.serial));
 												if(!automationJarInstalled){
 													throw new IllegalStateException("Error installing automation jar.");
 												}
 											}
 											
 											// Open random site
 											cmd.execute(String.format("-s %s shell am start -a android.intent.action.VIEW -d http://android.com",info.serial));
 											
 											// Start automation task
 											cmd.execute(
 													String.format(
 															"-s %s shell uiautomator runtest PreCtsAutomator.jar -c com.androidhuman.ctsprepare.automator.PreCTSAutomate#testSetInternetAsDefault", 
 															info.serial), 
 													new AdbCommandResultListener(){
 
 												@Override
 												public void onPreExecute() {
 													log(info.serial, task.toString()+" started");
 												}
 
 												@Override
 												public void onError(
 														String errmsg) {
 													log(info.serial, errmsg);
 												}
 
 												@Override
 												public void onOutput(String line) {
 													log(info.serial, line);
 												}
 
 												@Override
 												public void onSuccess() {
 													log(info.serial, task.toString()+" finished");
 												}
 												
 											});
 											break;
 										}
 										
 									}catch(NoSuchElementException e){
 										// Exit loop
 										break;
 									}
 								}
 								Display.getDefault().syncExec(new Runnable(){
 									public void run(){
 										updateProgress(currentDevIdx, 
 												initialTaskCnt-taskList.size(), initialTaskCnt,
 												"Completed");
 									}
 								});
 								log(info.serial, "All process done.");
 							}
 							Display.getDefault().syncExec(new Runnable(){
 								public void run(){
 									lblReady.setText("Done");
 									// Update progress on progress bar
 									progressBar.setSelection(100);
 								}
 							});
 							
 						}catch(IllegalStateException e){
 							log(e.getMessage());
 						}finally{
 							Display.getDefault().syncExec(new Runnable(){
 								public void run(){
 									((Button)e.widget).setEnabled(true);
 									btnRefresh.setVisible(true);
 									progressBar_1.setVisible(false);
 								}
 							});
 						}
 						
 					}
 				}).start();
 				
 			}
 		});
 		btnStart.setBounds(547, 394, 130, 38);
 		btnStart.setText("Start");
 		
 		progressBar = new ProgressBar(shlPrects, SWT.NONE);
 		progressBar.setBounds(10, 394, 398, 17);
 		
 		lblReady = new Label(shlPrects, SWT.NONE);
 		lblReady.setBounds(11, 417, 242, 27);
 		lblReady.setText("Ready");
 		
 		Group grpAutomation = new Group(shlPrects, SWT.NONE);
 		grpAutomation.setText("Automation");
 		grpAutomation.setBounds(328, 10, 348, 154);
 		
 		btnActivateCtsdeviceadminOn = new Button(grpAutomation, SWT.CHECK);
 		btnActivateCtsdeviceadminOn.setBounds(10, 24, 328, 16);
 		btnActivateCtsdeviceadminOn.setSelection(true);
 		btnActivateCtsdeviceadminOn.setText("Activate CtsDeviceAdmin on device");
 		
 		btnConfigureWifi = new Button(grpAutomation, SWT.CHECK);
 		btnConfigureWifi.setBounds(10, 46, 127, 16);
 		btnConfigureWifi.setSelection(true);
 		btnConfigureWifi.setText("Configure Wi-Fi");
 		
 		btnConfigureScreenTimeout = new Button(grpAutomation, SWT.CHECK);
 		btnConfigureScreenTimeout.setBounds(10, 68, 328, 16);
 		btnConfigureScreenTimeout.setSelection(true);
 		btnConfigureScreenTimeout.setText("Configure screen timeout");
 		
 		btnSetGoogleAccount = new Button(grpAutomation, SWT.CHECK);
 		btnSetGoogleAccount.setSelection(true);
 		btnSetGoogleAccount.setBounds(10, 84, 262, 24);
 		btnSetGoogleAccount.setText("Set Google account");
 		
 		btnSetInternetAsDefault = new Button(grpAutomation, SWT.CHECK);
 		btnSetInternetAsDefault.setSelection(true);
 		btnSetInternetAsDefault.setBounds(10, 107, 262, 24);
 		btnSetInternetAsDefault.setText("Set Internet as default");
 		
 		btnDisableSyncSettings = new Button(grpAutomation, SWT.CHECK);
 		btnDisableSyncSettings.setSelection(true);
 		btnDisableSyncSettings.setBounds(10, 128, 262, 24);
 		btnDisableSyncSettings.setText("Disable sync settings");
 		
 		btnRefresh = new Button(shlPrects, SWT.NONE);
 		btnRefresh.setBounds(420, 394, 121, 38);
 		btnRefresh.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				refreshDeviceList();
 			}
 		});
 		btnRefresh.setText("Refresh");
 		
 		progressBar_1 = new ProgressBar(shlPrects, SWT.INDETERMINATE);
 		progressBar_1.setVisible(false);
 		progressBar_1.setBounds(10, 344, 666, 8);
 		
 		txtLog = new Text(shlPrects, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
 		txtLog.setBounds(10, 450, 666, 126);
 		
 		Menu menu = new Menu(shlPrects, SWT.BAR);
 		shlPrects.setMenuBar(menu);
 		
 		MenuItem mntmSettings = new MenuItem(menu, SWT.CASCADE);
 		mntmSettings.setText("Settings");
 		
 		Menu menu_1 = new Menu(mntmSettings);
 		mntmSettings.setMenu(menu_1);
 		
 		MenuItem mntmSetGoogleAccount = new MenuItem(menu_1, SWT.NONE);
 		mntmSetGoogleAccount.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				new EditGoogleAccountDialog(shlPrects, SWT.NONE).open();
 			}
 		});
 		mntmSetGoogleAccount.setText("Set Google account...");
 		
 		MenuItem mntmSetWifiAp = new MenuItem(menu_1, SWT.NONE);
 		mntmSetWifiAp.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				new EditWifiApDialog(shlPrects, SWT.NONE).open();
 			}
 		});
 		mntmSetWifiAp.setText("Set WiFi AP...");
 		
 		new MenuItem(menu_1, SWT.SEPARATOR);
 		
 		MenuItem mntmSetAndroidSdk = new MenuItem(menu_1, SWT.NONE);
 		mntmSetAndroidSdk.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				new EditSdkPathDialog(shlPrects, SWT.NONE).open();
 			}
 		});
 		mntmSetAndroidSdk.setText("Set Android SDK Path...");
 		
 		// Check SDK location
 		
 		while(Utils.getAdbPath()==null){
 			if(Utils.showYesNoMessageBox(
 					shlPrects, 
 					"Android SDK location is not set. Set now?\n(Pressing no will quit the application)")){
 				new EditSdkPathDialog(shlPrects, SWT.NONE).open();
 			}else{
 				exitApp = true;
 				break;
 			}
 		}
 	}
 	
 	private void log(final String msg){
 		Display.getDefault().asyncExec(new Runnable(){
 			public void run(){
 				txtLog.append(msg);txtLog.append("\n");
 				txtLog.setTopIndex(txtLog.getLineCount()-1);
 			}
 		});
 	}
 	
 	private void log(String serial, String msg){
 		log(String.format("[%s | %s] %s", format.format(new Date()), serial, msg));
 	}
 	
 	private void updateProgress(final int index, final int currentProgress, final int totalProgress, final String message){
 		Display.getDefault().asyncExec(new Runnable(){
 			public void run(){
 
 			// Update progress on table
 			TableItem item = table.getItem(index);
 			item.setText(3, String.format("%d/%d", currentProgress, totalProgress));
 			item.setText(4, message);
 			
 			// Update progress on progress bar
 			int progress = currentDevIdx*100/deviceList.size();
 			progressBar.setSelection(progress);
 			
 			// Update label
 			lblReady.setText("Running task on device "+deviceList.get(currentDevIdx).serial);
 			}
 		}
 		);
 	}
 	
 	private void refreshDeviceList(){
 		// Refreshes device list
 		log("Refreshing device list...");
 		// Clear current device list
 		deviceList.clear();
 		table.removeAll();
 		table.setItemCount(0);
 		
 		// Get attached device info
 		try{
 			new AdbWrapper(Display.getDefault()).getAttachedDevices(new ResultListener<ArrayList<BasicDeviceInfo>>(){
 
 						@Override
 						public void onResult(ArrayList<BasicDeviceInfo> result) {
 							// Received device info
 							deviceList = result;
 							log(String.format("Found %d device(s).", result.size()));
 							for(BasicDeviceInfo info : deviceList){
 								// Create table entry
 								TableItem item = new TableItem(table, SWT.NULL);
 								item.setText(0, info.serial==null ? "Unknown" : info.serial);
 								item.setText(1, info.model==null ? "Unknown" : info.model);
 								item.setText(2, info.version==null ? "Unknown" : info.version);
 								item.setText(3, "N/A");
 								item.setText(4, info.serial==null ? "Not available" : "Ready");
 							}
 							
 						}
 
 						@Override
 						public void onFailed(String msg) {
 							Utils.showErrorMessage(shlPrects, msg);
 						}
 
 						@Override
 						public void onPreExecute() {
 							btnRefresh.setEnabled(false);
 							btnRefresh.setText("Wait...");
 						}
 
 						@Override
 						public void onPostExecute() {
 							btnRefresh.setEnabled(true);
 							btnRefresh.setText("Refresh");
 							btnStart.setEnabled(deviceList.size()!=0);
 							
 						}
 						
 					});
 			
 		}catch(AdbCommandException ex){
 			Utils.showErrorMessage(null, ex.getMessage());
 		}
 		
 	}
 	
 	private void updateStatusMessage(final int index, final String message){
 		Display.getDefault().asyncExec(new Runnable(){
 			public void run(){
 				TableItem item = table.getItem(index);
 				item.setText(4, message);
 			}
 		});
 	}
 }
