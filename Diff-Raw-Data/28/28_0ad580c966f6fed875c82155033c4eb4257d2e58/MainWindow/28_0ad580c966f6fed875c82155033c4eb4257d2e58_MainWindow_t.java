 package net.mms_projects.tostream.ui.swt;
 
 import java.io.IOException;
 
 import net.mms_projects.tostream.DeviceManager;
 import net.mms_projects.tostream.EncoderOutputListener;
 import net.mms_projects.tostream.FfmpegWrapper;
 import net.mms_projects.tostream.OSValidator;
 import net.mms_projects.tostream.Settings;
 import net.mms_projects.tostream.SettingsListener;
 import net.mms_projects.tostream.ToStream;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
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
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 public class MainWindow extends Shell {
 
 	FfmpegWrapper ffmpegWrapper;
 	private Text settingBitrate;
 	private Text settingsResolutionX;
 	private Text settingsResolutionY;
 	private Text settingFramerate;
 	private Text settingStreamUrl;
 
 	private RecordingSelectionWindow regionSelectionWindow = new RecordingSelectionWindow(
 			getDisplay());
 	private Text settingsLocationX;
 	private Text settingsLocationY;
 
 	/**
 	 * Create the shell.
 	 * 
 	 * @param display
 	 * @param debugWindow 
 	 */
 	public MainWindow(Display display, final FfmpegWrapper ffmpegWrapper,
 			final Settings settings, final DebugConsole debugWindow) {
 		super(display, SWT.SHELL_TRIM);
 		addShellListener(new ShellAdapter() {
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				if (ffmpegWrapper.running) {
 					try {
 						ffmpegWrapper.stopEncoder();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		this.ffmpegWrapper = ffmpegWrapper;
 		setLayout(new GridLayout(2, false));
 
 		Menu menu = new Menu(this, SWT.BAR);
 		setMenuBar(menu);
 
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 
 		Menu menu_1 = new Menu(mntmFile);
 		mntmFile.setMenu(menu_1);
 
 		MenuItem mntmQuit = new MenuItem(menu_1, SWT.NONE);
 		mntmQuit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				close();
 			}
 		});
 		mntmQuit.setText("Quit");
 		
 		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
 		mntmHelp.setText("Help");
 		
 		Menu menu_2 = new Menu(mntmHelp);
 		mntmHelp.setMenu(menu_2);
 		
 		final MenuItem mntmShowDebugconsole = new MenuItem(menu_2, SWT.CHECK);
 		mntmShowDebugconsole.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (mntmShowDebugconsole.getSelection()) {
 					debugWindow.open();
 				}
 				else {
 					debugWindow.close();
 				}
 			}
 		});
 		mntmShowDebugconsole.setText("Show debugconsole");
 		mntmShowDebugconsole.setSelection(debugWindow.getVisible());
 		
 		MenuItem mntmAdvancedSettings = new MenuItem(menu_2, SWT.NONE);
 		mntmAdvancedSettings.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				AdvancedSettings advancedSettings = new AdvancedSettings(arg0.display.getActiveShell(), settings);
 				advancedSettings.open();
 			}
 		});
 		mntmAdvancedSettings.setText("Advanced settings");
 		
 		new MenuItem(menu_2, SWT.SEPARATOR);
 		
 		MenuItem mntmAbout = new MenuItem(menu_2, SWT.NONE);
 		mntmAbout.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				AboutDialog about = new AboutDialog(arg0.display.getActiveShell());
 				about.open();
 			}
 		});
 		mntmAbout.setText("About " + ToStream.getApplicationName());
 		debugWindow.addShellListener(new ShellAdapter() {
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				arg0.doit = false;
 				debugWindow.setVisible(false);
 				mntmShowDebugconsole.setSelection(false);
 			}
 		});
 		
 		Label lblVideoDevice = new Label(this, SWT.NONE);
 		lblVideoDevice.setText("Video device");
 		
 		final Combo settingVideoDevice = new Combo(this, SWT.READ_ONLY);
 		settingVideoDevice.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				DeviceManager.setVideoDevice(settingVideoDevice.getText(), settings);
 			}
 		});
 		settingVideoDevice.setItems(DeviceManager.getVideoDevices());
 		settingVideoDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		settingVideoDevice.select(DeviceManager.getVideoDeviceIndex(settings));
 
 		Label lblVideoBitrate = new Label(this, SWT.NONE);
 		lblVideoBitrate.setText("Video bitrate");
 
 		settingBitrate = new Text(this, SWT.BORDER);
 		settingBitrate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		settingBitrate.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.set(Settings.BITRATE, settingBitrate.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingBitrate.setText(settings.get(Settings.BITRATE));
 		settings.addListener(Settings.BITRATE, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
				if (!OSValidator.isWindows()) {
					settingBitrate.setText(value);
				}
 			}
 		});
 
 		Label lblVideoEncodePreset = new Label(this, SWT.NONE);
 		lblVideoEncodePreset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false, 1, 1));
 		lblVideoEncodePreset.setText("Video encode preset");
 
 		Combo settingVideoEncodePreset = new Combo(this, SWT.READ_ONLY);
 		settingVideoEncodePreset.setLayoutData(new GridData(SWT.FILL,
 				SWT.CENTER, true, false, 1, 1));
 		settingVideoEncodePreset.setItems(new String[] { "a", "a", "a" });
 
 		Label lblVideoResolution = new Label(this, SWT.NONE);
 		lblVideoResolution.setText("Video resolution");
 
 		Composite compositeVideoResolution = new Composite(this, SWT.NONE);
 		compositeVideoResolution.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, false, 1, 1));
 		compositeVideoResolution.setLayout(new GridLayout(4, false));
 
 		settingsResolutionX = new Text(compositeVideoResolution, SWT.BORDER);
 		settingsResolutionX.setText(Integer.toString(settings
 				.getAsIntegerArray(Settings.RESOLUTION)[0]));
 		settingsResolutionX.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.videoResolution[0] = Integer
 							.parseInt(settingsResolutionX.getText());
 				} catch (java.lang.NumberFormatException e) {
 					settingsResolutionX.setText("0");
 				}
 				try {
 					settings.set(Settings.RESOLUTION, settings.videoResolution);
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingsResolutionX.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(final VerifyEvent event) {
 				switch (event.keyCode) {
 				case SWT.BS: // Backspace
 				case SWT.DEL: // Delete
 				case SWT.HOME: // Home
 				case SWT.END: // End
 				case SWT.ARROW_LEFT: // Left arrow
 				case SWT.ARROW_RIGHT: // Right arrow
 					return;
 				}
 
 				if ((!Character.isDigit(event.character)) && (Character.getNumericValue(event.character) != -1)) {
 					event.doit = false; // disallow the action
 					System.out.println("'" + Character.getNumericValue(event.character) + "'");
 				}
 			}
 		});
 		settingsResolutionX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
 				true, false, 1, 1));
 
 		Label lblX = new Label(compositeVideoResolution, SWT.NONE);
 		lblX.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1,
 				1));
 		lblX.setText("X");
 
 		settingsResolutionY = new Text(compositeVideoResolution, SWT.BORDER);
 		settingsResolutionY.setText(Integer.toString(settings
 				.getAsIntegerArray(Settings.RESOLUTION)[1]));
 		settingsResolutionY.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.videoResolution[1] = Integer
 							.parseInt(settingsResolutionY.getText());
 				} catch (java.lang.NumberFormatException e) {
 					settingsResolutionY.setText("0");
 				}
 				try {
 					settings.set(Settings.RESOLUTION, settings.videoResolution);
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingsResolutionY.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(final VerifyEvent event) {
 				switch (event.keyCode) {
 				case SWT.BS: // Backspace
 				case SWT.DEL: // Delete
 				case SWT.HOME: // Home
 				case SWT.END: // End
 				case SWT.ARROW_LEFT: // Left arrow
 				case SWT.ARROW_RIGHT: // Right arrow
 					return;
 				}
 
 				if ((!Character.isDigit(event.character)) && (Character.getNumericValue(event.character) != -1)) {
 					event.doit = false; // disallow the action
 					System.out.println(event.character);
 				}
 			}
 		});
 		settingsResolutionY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
 				true, false, 1, 1));
 		
 		settings.addListener(Settings.RESOLUTION, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
 				String[] resolution = value.split(",");
				if (!OSValidator.isWindows()) {
					settingsResolutionX.setText(resolution[0]);
					settingsResolutionY.setText(resolution[1]);
				}
 			}
 		});
 
 		Button btnSelectRegion = new Button(compositeVideoResolution, SWT.NONE);
 		btnSelectRegion.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				regionSelectionWindow.setSize(settings.getAsIntegerArray(Settings.RESOLUTION)[0], settings.getAsIntegerArray(Settings.RESOLUTION)[1]);
 				regionSelectionWindow.setLocation(settings.getAsIntegerArray(Settings.LOCATION)[0], settings.getAsIntegerArray(Settings.LOCATION)[1]);
 				regionSelectionWindow.open();
 			}
 		});
 		btnSelectRegion.setText("Select region");
 		
 		settingsLocationX = new Text(compositeVideoResolution, SWT.BORDER);
 		settingsLocationX.setText(Integer.toString(settings.getAsIntegerArray(Settings.LOCATION)[0]));
 		settingsLocationX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		settingsLocationX.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				Integer[] location = settings.getAsIntegerArray(Settings.LOCATION);
 				try {
 					location[0] = Integer.parseInt(settingsLocationX.getText());
 				} catch (java.lang.NumberFormatException e) {
 					settingsLocationX.setText("0");
 				}
 				try {
 					settings.set(Settings.LOCATION, location);
 				} catch (Exception e) {
 				}
 			}
 		});
 		Label lblLocation = new Label(compositeVideoResolution, SWT.NONE);
 		lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1,
 				1));
 		lblLocation.setText(",");
 		
 		settingsLocationY = new Text(compositeVideoResolution, SWT.BORDER);
 		settingsLocationY.setText(Integer.toString(settings.getAsIntegerArray(Settings.LOCATION)[1]));
 		settingsLocationY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		settingsLocationY.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				Integer[] location = settings.getAsIntegerArray(Settings.LOCATION);
 				try {
 					location[1] = Integer.parseInt(settingsLocationY.getText());
 				} catch (java.lang.NumberFormatException e) {
 					settingsLocationY.setText("0");
 				}
 				try {
 					settings.set(Settings.LOCATION, location);
 				} catch (Exception e) {
 				}
 			}
 		});
 		
 		settings.addListener(Settings.LOCATION, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
 				String[] resolution = value.split(",");
				if (!OSValidator.isWindows()) {
					settingsLocationX.setText(resolution[0]);
					settingsLocationY.setText(resolution[1]);
				}
 			}
 		});
 		
 		new Label(compositeVideoResolution, SWT.NONE);
 
 		Label lblVideoFrameRate = new Label(this, SWT.NONE);
 		lblVideoFrameRate.setText("Video frame rate");
 
 		settingFramerate = new Text(this, SWT.BORDER);
 		settingFramerate.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.set(Settings.FRAME_RATE,
 							settingFramerate.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingFramerate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		settingFramerate.setText(settings.get(Settings.FRAME_RATE));
 		settings.addListener(Settings.FRAME_RATE, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
				if (!OSValidator.isWindows()) {
					settingFramerate.setText(value);
				}
 			}
 		});
 
 		Label lblAudioBitrate = new Label(this, SWT.NONE);
 		lblAudioBitrate.setText("Audio bitrate");
 		new Label(this, SWT.NONE);
 
 		Label lblAudioChannels = new Label(this, SWT.NONE);
 		lblAudioChannels.setText("Audio channels");
 		new Label(this, SWT.NONE);
 
 		Label lblStreamUrl = new Label(this, SWT.NONE);
 		lblStreamUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblStreamUrl.setText("Stream URL");
 
 		settingStreamUrl = new Text(this, SWT.BORDER);
 		settingStreamUrl.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				try {
 					settings.set(Settings.STREAM_URL,
 							settingStreamUrl.getText());
 				} catch (Exception e) {
 				}
 			}
 		});
 		settingStreamUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		if (settings.get(settings.STREAM_URL) != null) {
 			settingStreamUrl.setText(settings.get(settings.STREAM_URL));
 		}
 		settings.addListener(Settings.STREAM_URL, new SettingsListener() {
 			@Override
 			public void settingSet(String value) {
				if (!OSValidator.isWindows()) {
					settingStreamUrl.setText(value);
				}
 			}
 		});
 		
 		new Label(this, SWT.NONE);
 
 		Composite composite = new Composite(this, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
 				1, 1));
 		composite.setLayout(new GridLayout(2, false));
 
 		final Button buttonStart = new Button(composite, SWT.NONE);
 		buttonStart.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					ffmpegWrapper.startEncoder();
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
 					ffmpegWrapper.stopEncoder();
 				} catch (Exception error) {
 					MessageBox msg = new MessageBox(new Shell());
 					msg.setText("An erorr occured");
 					msg.setMessage("Error while stopping FFmpeg: "
 							+ error.getMessage());
 					msg.open();
 				}
 			}
 		});
 		buttonStop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false, 1, 1));
 		buttonStop.setText("Stop");
 		new Label(this, SWT.NONE);
 
 		final Label lblStatus = new Label(this, SWT.NONE);
 		lblStatus.setText("Please start to get the status");
 
 		this.ffmpegWrapper.addListener(new EncoderOutputListener() {
 			public void onStatusUpdate(final int frame, final int framerate) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						lblStatus.setText("FPS: " + framerate + " - Frame: "
 								+ frame);
 					}
 				});
 			}
 			@Override
 			public void onStart() {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						buttonStop.setEnabled(true);
 						buttonStart.setEnabled(false);
 						
 						regionSelectionWindow.close();
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
 		
 		regionSelectionWindow.addListener(new RecordingSelectionListener() {
 			@Override
 			public void selectionChanged(final Point location, final Point size) {
 				Display.getDefault().asyncExec(new Runnable() {
 					
 					@Override
 					public void run() {
 						settingsResolutionX.setText(Integer.toString(size.x));
 						settingsResolutionY.setText(Integer.toString(size.y));
 						
 						settingsLocationX.setText(Integer.toString(location.x));
 						settingsLocationY.setText(Integer.toString(location.y));
 						
 						Integer[] resolution = {size.x, size.y};
 						Integer[] locationArray = {location.x, location.y};
 						try {
 							settings.set(Settings.RESOLUTION, resolution);
 							settings.set(Settings.LOCATION, locationArray);
 						} catch (Exception e) {
 						}
 					}
 				});
 			}
 		});
 		
 		createContents();
 	}
 
 	/**
 	 * Create contents of the shell.
 	 */
 	protected void createContents() {
 		setText("2STREAM");
 		setSize(600, 400);
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
