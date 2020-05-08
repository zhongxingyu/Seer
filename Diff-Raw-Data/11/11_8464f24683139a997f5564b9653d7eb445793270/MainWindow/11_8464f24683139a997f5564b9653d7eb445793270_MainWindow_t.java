 package net.mms_projects.tostream.ui.swt;
 
 import net.mms_projects.tostream.Encoder;
 import net.mms_projects.tostream.EncoderOutputListener;
 import net.mms_projects.tostream.Messages;
 import net.mms_projects.tostream.OSValidator;
 import net.mms_projects.tostream.Settings;
 import net.mms_projects.tostream.SettingsListener;
 import net.mms_projects.tostream.ToStream;
 import net.mms_projects.tostream.managers.DeviceManager;
 import net.mms_projects.tostream.managers.EncoderManager;
 import net.mms_projects.tostream.managers.VideoDeviceManager;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 import swing2swt.layout.BorderLayout;
 
 public class MainWindow extends Shell {
 
 	Encoder ffmpegWrapper;
 
 	private EncoderManager encoderManager;
 	private Settings settings;
 	private DebugConsole debugWindow;
 
 	// Menu items
 	private MenuItem mntmShowDebugconsole;
 
 	private Text settingBitrate;
 	private Combo settingFramerate;
 	private Text settingStreamUrl;
 
 	/**
 	 * Create the shell.
 	 * 
 	 * @param display
 	 * @param debugWindow
 	 * @param videoManager
 	 */
 	public MainWindow(Display display, final EncoderManager encoderManager,
 			final Settings settings, final DebugConsole debugWindow,
 			final VideoDeviceManager videoManager) {
 		super(display, SWT.SHELL_TRIM);
 		this.encoderManager = encoderManager;
 		this.settings = settings;
 		this.debugWindow = debugWindow;
 
 		createMenu();
 
 		addShellListener(new ShellAdapter() {
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				if (encoderManager.getCurrentItem().isRunning()) {
 					try {
 						encoderManager.getCurrentItem().stopEncoder();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		setLayout(new BorderLayout(0, 0));
 
 		debugWindow.addShellListener(new ShellAdapter() {
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				arg0.doit = false;
 				debugWindow.setVisible(false);
 				mntmShowDebugconsole.setSelection(false);
 			}
 		});
 
 		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
 
 		TabItem tabStandard = new TabItem(tabFolder, SWT.NONE);
 		tabStandard.setText(Messages.getString("MainWindow.tab.standard")); //$NON-NLS-1$
 
 		Composite compositeStandard = new Composite(tabFolder, SWT.NONE);
 		tabStandard.setControl(compositeStandard);
 		compositeStandard.setLayout(new GridLayout(3, false));
 
 		Label labelVideoDevice = new Label(compositeStandard, SWT.NONE);
 		labelVideoDevice.setText(Messages.getString("setting.video-device"));
 
 		final Combo settingVideoDevice = new Combo(compositeStandard,
 				SWT.READ_ONLY);
 		GridData gd_settingVideoDevice = new GridData();
 		gd_settingVideoDevice.widthHint = 200;
 		settingVideoDevice.setLayoutData(gd_settingVideoDevice);
 		settingVideoDevice.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				try {
 					videoManager.setCurrentItem(settingVideoDevice.getText(),
 							settings);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		settingVideoDevice.setItems(videoManager.getItemNames());
 		settingVideoDevice.select(videoManager.getItemIndex(settings));
 
 		Button buttonVideoSetting = new Button(compositeStandard, SWT.NONE);
 		buttonVideoSetting.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				VideoSettings dialog = new VideoSettings(getShell(),
 						videoManager.getCurrentItem());
 				dialog.open();
 			}
 		});
 		buttonVideoSetting.setText("Settings");
 
 		Label labelAudioDevice = new Label(compositeStandard, SWT.NONE);
 		labelAudioDevice.setText(Messages.getString("setting.audio-device"));
 
 		final Combo settingAudioDevice = new Combo(compositeStandard,
 				SWT.READ_ONLY);
 		GridData gd_settingAudioDevice = new GridData();
 		gd_settingAudioDevice.widthHint = 200;
 		settingAudioDevice.setLayoutData(gd_settingAudioDevice);
 		settingAudioDevice.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				DeviceManager.setAudioDevice(settingAudioDevice.getText(),
 						settings);
 			}
 		});
 		settingAudioDevice.setItems(DeviceManager.getAudioDevices());
 		settingAudioDevice.select(DeviceManager.getAudioDeviceIndex(settings));
 		new Label(compositeStandard, SWT.NONE);
 
 		Label labelVideoEncodePreset = new Label(compositeStandard, SWT.NONE);
 		labelVideoEncodePreset.setText("Video encode preset");
 
 		Combo settingVideoEncodePreset = new Combo(compositeStandard,
 				SWT.READ_ONLY);
 		GridData gd_settingVideoEncodePreset = new GridData(SWT.LEFT,
 				SWT.CENTER, false, false, 1, 1);
 		gd_settingVideoEncodePreset.widthHint = 200;
 		settingVideoEncodePreset.setLayoutData(gd_settingVideoEncodePreset);
 		settingVideoEncodePreset.setItems(new String[] { "a", "a", "a" });
 		new Label(compositeStandard, SWT.NONE);
 
 		Label labelVideoFrameRate = new Label(compositeStandard, SWT.NONE);
 		labelVideoFrameRate.setText(Messages
 				.getString("setting.video-frame-rate"));
 
 		settingFramerate = new Combo(compositeStandard, SWT.BORDER);
 		settingFramerate.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				System.out.println(settingFramerate.getText());
 				try {
 					settings.set(Settings.FRAME_RATE,
 							settingFramerate.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingFramerate.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				System.out.println(settingFramerate.getText());
 				try {
 					settings.set(Settings.FRAME_RATE,
 							settingFramerate.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingFramerate.setItems(new String[] { "5", "10", "15", "20", "25",
 				"30", "35", "40", "45", "50", "55", "60" });
 		GridData gd_settingFramerate = new GridData(SWT.LEFT, SWT.CENTER,
 				false, false, 1, 1);
 		gd_settingFramerate.widthHint = 200;
 		settingFramerate.setLayoutData(gd_settingFramerate);
 		settingFramerate.setText(settings.get(Settings.FRAME_RATE));
 		new Label(compositeStandard, SWT.NONE);
 
 		Label labelAudioBitrate = new Label(compositeStandard, SWT.NONE);
 		labelAudioBitrate.setText(Messages.getString("setting.audio-bitrate"));
 
 		new Label(compositeStandard, SWT.NONE);
 		new Label(compositeStandard, SWT.NONE);
 
 		Label labelAudioChannels = new Label(compositeStandard, SWT.NONE);
 		labelAudioChannels
 				.setText(Messages.getString("setting.audio-channels"));
 
 		new Label(compositeStandard, SWT.NONE);
 		new Label(compositeStandard, SWT.NONE);
 
 		Label labelStreamUrl = new Label(compositeStandard, SWT.NONE);
 		labelStreamUrl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
 				false, 1, 1));
 		labelStreamUrl.setText(Messages.getString("setting.stream-url"));
 
 		settingStreamUrl = new Text(compositeStandard, SWT.BORDER | SWT.WRAP);
 		GridData gd_settingStreamUrl = new GridData(SWT.LEFT, SWT.CENTER,
 				false, false, 1, 1);
 		gd_settingStreamUrl.widthHint = 200;
 		gd_settingStreamUrl.heightHint = 52;
 		settingStreamUrl.setLayoutData(gd_settingStreamUrl);
 		new Label(compositeStandard, SWT.NONE);
 		settingStreamUrl.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.set(Settings.STREAM_URL,
 							settingStreamUrl.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 
 		TabItem tabAdvanced = new TabItem(tabFolder, SWT.NONE);
 		tabAdvanced.setText(Messages.getString("MainWindow.tab.advanced"));
 
 		Composite compositeAdvanced = new Composite(tabFolder, SWT.NONE);
 		tabAdvanced.setControl(compositeAdvanced);
 		compositeAdvanced.setLayout(new GridLayout(2, false));
 
 		Label labelVideoBitrate = new Label(compositeAdvanced, SWT.NONE);
 		labelVideoBitrate.setText(Messages.getString("setting.video-bitrate"));
 
 		settingBitrate = new Text(compositeAdvanced, SWT.BORDER);
 		settingBitrate.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.set(Settings.BITRATE, settingBitrate.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingBitrate.setText(settings.get(Settings.BITRATE));
 
 		TabItem tabStreamingSettings = new TabItem(tabFolder, SWT.NONE);
 		tabStreamingSettings.setText(Messages
 				.getString("MainWindow.tab.streaming-settings")); //$NON-NLS-1$
 
 		Composite composite_2 = new StreamingSettings(tabFolder, settings);
 		tabStreamingSettings.setControl(composite_2);
 
 		Composite composite_1 = new Composite(this, SWT.NONE);
 		composite_1.setLayoutData(BorderLayout.SOUTH);
 		composite_1.setLayout(new GridLayout(1, false));
 
 		Composite composite = new Composite(composite_1, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 
 		final Button buttonStart = new Button(composite, SWT.NONE);
 		buttonStart.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					encoderManager.getCurrentItem().startEncoder();
 				} catch (Exception error) {
 					MessageBox msg = new MessageBox(new Shell());
 					msg.setText("An erorr occured");
 					msg.setMessage("Error while starting FFmpeg: "
 							+ error.getMessage());
 					msg.open();
 				}
 			}
 		});
 		buttonStart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false, 1, 1));
 		buttonStart.setText("Start");
 
 		final Button buttonStop = new Button(composite, SWT.NONE);
 		buttonStop.setEnabled(false);
 		buttonStop.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					encoderManager.getCurrentItem().stopEncoder();
 				} catch (Exception error) {
 					MessageBox msg = new MessageBox(new Shell());
 					msg.setText("An erorr occured");
 					msg.setMessage("Error while stopping FFmpeg: "
 							+ error.getMessage());
 					msg.open();
 				}
 			}
 		});
 		buttonStop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
 				1, 1));
 		buttonStop.setText("Stop");
 
 		final Label labelStatus = new Label(composite_1, SWT.NONE);
 		GridData gd_labelStatus = new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1);
 		gd_labelStatus.widthHint = 367;
 		labelStatus.setLayoutData(gd_labelStatus);
 		labelStatus.setText(Messages.getString("MainWindow.beginning-status"));
 		settings.addListener(Settings.BITRATE, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
 				if (!OSValidator.isWindows()) {
 					settingBitrate.setText(value);
 				}
 			}
 		});
 		settings.addListener(Settings.FRAME_RATE, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
 				if (!OSValidator.isWindows()) {
 					settingFramerate.setText(value);
 				}
 			}
 		});
 		if (settings.get(Settings.STREAM_URL) != null) {
 			settingStreamUrl.setText(settings.get(Settings.STREAM_URL));
 		}
		/*settings.addListener(Settings.STREAM_URL, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
 				if (!OSValidator.isWindows()) {
 					settingStreamUrl.setText(value);
 				}
 			}
		})*/;
 
 		encoderManager.addListener(new EncoderOutputListener() {
 			@Override
 			public void onStart() {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						buttonStop.setEnabled(true);
 						buttonStart.setEnabled(false);
 					}
 				});
 			}
 
 			@Override
 			public void onStatusUpdate(final int frame, final int framerate,
 					final double bitrate) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						labelStatus.setText("FPS: " + framerate + " - Frame: " //$NON-NLS-2$
 								+ frame + " - Bitrate: " + bitrate + " kbit/s"); //$NON-NLS-2$
 					}
 				});
 			}
 
 			@Override
 			public void onStop() {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						buttonStop.setEnabled(false);
 						buttonStart.setEnabled(true);
 					}
 				});
 			}
 		});
 
 		createContents();
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	/**
 	 * Create contents of the shell.
 	 */
 	protected void createContents() {
 		setText(ToStream.getApplicationName());
 		setSize(600, 600);
 
 	}
 
 	protected void createMenu() {
 		Menu menu = new Menu(this, SWT.BAR);
 		setMenuBar(menu);
 
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText(Messages.getString("MainWindow.menu.file"));
 
 		Menu menu_1 = new Menu(mntmFile);
 		mntmFile.setMenu(menu_1);
 
 		MenuItem mntmQuit = new MenuItem(menu_1, SWT.NONE);
 		mntmQuit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				close();
 			}
 		});
 		mntmQuit.setText(Messages.getString("application.exit"));
 
 		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
 		mntmHelp.setText(Messages.getString("MainWindow.menu.help"));
 
 		Menu menu_2 = new Menu(mntmHelp);
 		mntmHelp.setMenu(menu_2);
 
 		mntmShowDebugconsole = new MenuItem(menu_2, SWT.CHECK);
 		mntmShowDebugconsole.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (mntmShowDebugconsole.getSelection()) {
 					debugWindow.open();
 				} else {
 					debugWindow.close();
 				}
 			}
 		});
 		mntmShowDebugconsole.setText(Messages
 				.getString("MainWindow.showDebugConsole"));
 		mntmShowDebugconsole.setSelection(debugWindow.getVisible());
 
 		MenuItem mntmAdvancedSettings = new MenuItem(menu_2, SWT.NONE);
 		mntmAdvancedSettings.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				AdvancedSettings advancedSettings = new AdvancedSettings(
 						arg0.display.getActiveShell(), settings);
 				advancedSettings.open();
 			}
 		});
 		mntmAdvancedSettings.setText(Messages
 				.getString("MainWindow.menuItemAdvancedSettings"));
 
 		MenuItem mntmSelectEncoder = new MenuItem(menu_2, SWT.NONE);
 		mntmSelectEncoder.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				SwitchEncoder switcher = new SwitchEncoder(getShell(),
 						encoderManager);
 				switcher.open();
 			}
 		});
 		mntmSelectEncoder.setText(Messages
 				.getString("MainWindow.mntmSelectEncoder.text")); //$NON-NLS-1$
 
 		new MenuItem(menu_2, SWT.SEPARATOR);
 
 		MenuItem mntmAbout = new MenuItem(menu_2, SWT.NONE);
 		mntmAbout.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				AboutDialog about = new AboutDialog(arg0.display
 						.getActiveShell());
 				about.open();
 			}
 		});
 		mntmAbout.setText(Messages.getString("application.about"));
 	}
 }
