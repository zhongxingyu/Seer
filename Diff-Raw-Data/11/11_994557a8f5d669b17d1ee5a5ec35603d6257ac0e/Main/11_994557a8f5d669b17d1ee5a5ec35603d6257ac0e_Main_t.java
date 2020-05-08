 package dimitri.suls.allshare.activities;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import com.sec.android.allshare.Device;
 import com.sec.android.allshare.Device.DeviceType;
 import com.sec.android.allshare.control.TVController;
 import com.sec.android.allshare.control.TVController.RemoteKey;
 import com.sec.android.allshare.media.AVPlayer;
 import com.sec.android.allshare.media.ImageViewer;
 
 import dimitri.suls.allshare.R;
 import dimitri.suls.allshare.activities.helpers.InitializeManager;
 import dimitri.suls.allshare.activities.helpers.TabManager;
 import dimitri.suls.allshare.device.frontend.manager.DeviceFrontendManager;
 import dimitri.suls.allshare.device.model.manager.DeviceCommand;
 import dimitri.suls.allshare.device.model.manager.DeviceManager;
 import dimitri.suls.allshare.media.frontend.managers.MediaFrontendManager;
 import dimitri.suls.allshare.media.frontend.managers.MediaFrontendManager.MediaListItemClickListener;
 import dimitri.suls.allshare.media.model.managers.MediaManager;
 import dimitri.suls.allshare.media.model.managers.MediaManager.MediaType;
 import dimitri.suls.allshare.serviceprovider.model.managers.ServiceProviderManager;
 import dimitri.suls.allshare.serviceprovider.model.managers.ServiceProviderObserver;
 import dimitri.suls.allshare.tv.listeners.TVTouchListener;
 
 public class Main extends Activity {
 	private TabManager tabManager = null;
 	private MediaManager mediaManager = null;
 	private ServiceProviderManager serviceProviderManager = null;
 	private DeviceManager tvControllerDeviceManager = null;
 	private DeviceManager avPlayerDeviceManager = null;
 	private DeviceManager imageViewerDeviceManager = null;
 	private DeviceFrontendManager tvControllerDeviceFrontendManager = null;
 	private DeviceFrontendManager avPlayerDeviceFrontendManager = null;
 	private DeviceFrontendManager imageViewerDeviceFrontendManager = null;
 	private EditText editTextBrowseTerm = null;
 	// TODO: Reference needed for MediaFrontendManagers?
 	private MediaFrontendManager songsFrontendManager = null;
 	private MediaFrontendManager videosFrontendManager = null;
 	private MediaFrontendManager imagesFrontendManager = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		tabManager = new TabManager(this);
 		mediaManager = new MediaManager(this);
 
 		InitializeManager initializeManager = new InitializeManager(this);
 
 		initializeManager.start();
 	}
 
 	@Override
 	protected void onDestroy() {
		if (serviceProviderManager != null) {
			serviceProviderManager.close();
		}
 
 		super.onDestroy();
 	}
 
 	public void initialize() throws Exception {
 		serviceProviderManager = new ServiceProviderManager(this);
 
 		serviceProviderManager.addObserver(new ServiceProviderObserver() {
 			@Override
 			public void createdServiceProvider() {
 				initializeEachDeviceManager();
 				initializeEachMediaList();
 				initializeEditTextBrowseTerm();
 				initializeTVTouchListener();
 				initializeSeekBarVolumeAVPlayer();
 				initializeSwitchMuteAVPlayer();
 			}
 		});
 	}
 
 	private void initializeEachDeviceManager() {
 		tvControllerDeviceManager = new DeviceManager(DeviceType.DEVICE_TV_CONTROLLER, serviceProviderManager);
 		avPlayerDeviceManager = new DeviceManager(DeviceType.DEVICE_AVPLAYER, serviceProviderManager);
 		imageViewerDeviceManager = new DeviceManager(DeviceType.DEVICE_IMAGEVIEWER, serviceProviderManager);
 
 		ListView listViewTVControllers = (ListView) findViewById(R.id.listViewTVControllers);
 		ListView listViewAVPlayers = (ListView) findViewById(R.id.listViewAVPlayers);
 		ListView listViewImageViewers = (ListView) findViewById(R.id.listViewImageViewers);
 
 		TextView textViewSelectedTVController = (TextView) findViewById(R.id.textViewSelectedTVController);
 		TextView textViewSelectedAVPlayer = (TextView) findViewById(R.id.textViewSelectedAVPlayer);
 		TextView textViewSelectedImageViewer = (TextView) findViewById(R.id.textViewSelectedImageViewer);
 
 		tvControllerDeviceFrontendManager = new DeviceFrontendManager(this, tvControllerDeviceManager, listViewTVControllers,
 				textViewSelectedTVController, "TV-controller", tabManager, 1);
 		avPlayerDeviceFrontendManager = new DeviceFrontendManager(this, avPlayerDeviceManager, listViewAVPlayers, textViewSelectedAVPlayer,
 				"AV-player", tabManager, 2);
 		imageViewerDeviceFrontendManager = new DeviceFrontendManager(this, imageViewerDeviceManager, listViewImageViewers,
 				textViewSelectedImageViewer, "image-viewer", tabManager, 3);
 	}
 
 	private void initializeEachMediaList() {
 		ListView listViewSongs = (ListView) findViewById(R.id.listViewSongs);
 		ListView listViewVideos = (ListView) findViewById(R.id.listViewVideos);
 		ListView listViewImages = (ListView) findViewById(R.id.listViewImages);
 
 		MediaListItemClickListener avListItemClickListener = new MediaListItemClickListener() {
 			@Override
 			public DeviceManager getDeviceManager() {
 				return avPlayerDeviceManager;
 			}
 
 			@Override
 			public DeviceCommand getDeviceCommand() {
 				DeviceCommand deviceCommand = new DeviceCommand() {
 					@Override
 					public void execute(Device selectedDevice) {
 						AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 						avPlayer.play(getSelectedItem(), getContentInfo());
 					}
 				};
 
 				return deviceCommand;
 			}
 		};
 
 		MediaListItemClickListener imageListItemClickListener = new MediaListItemClickListener() {
 			@Override
 			public DeviceManager getDeviceManager() {
 				return imageViewerDeviceManager;
 			}
 
 			@Override
 			public DeviceCommand getDeviceCommand() {
 				DeviceCommand deviceCommand = new DeviceCommand() {
 					@Override
 					public void execute(Device selectedDevice) {
 						ImageViewer imageViewer = (ImageViewer) selectedDevice;
 
 						imageViewer.show(getSelectedItem(), getContentInfo());
 					}
 				};
 
 				return deviceCommand;
 			}
 		};
 
 		songsFrontendManager = new MediaFrontendManager(this, listViewSongs, avListItemClickListener, mediaManager, MediaType.AUDIO);
 		videosFrontendManager = new MediaFrontendManager(this, listViewVideos, avListItemClickListener, mediaManager, MediaType.VIDEO);
 		imagesFrontendManager = new MediaFrontendManager(this, listViewImages, imageListItemClickListener, mediaManager, MediaType.IMAGES);
 	}
 
 	private void initializeEditTextBrowseTerm() {
 		editTextBrowseTerm = (EditText) findViewById(R.id.editTextBrowseTerm);
 	}
 
 	// TODO: Add slider to adjust speed of motion-control.
 	private void initializeTVTouchListener() {
 		View tabTouch = findViewById(R.id.tabTVTouch);
 
 		tabTouch.setOnTouchListener(new TVTouchListener(tvControllerDeviceManager));
 	}
 
 	private void initializeSeekBarVolumeAVPlayer() {
 		SeekBar seekBarVolumeAVPlayer = (SeekBar) findViewById(R.id.seekBarVolumeAVPlayer);
 
 		seekBarVolumeAVPlayer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			private int volumeLevel = 0;
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
 				volumeLevel = progress;
 
 				avPlayerDeviceManager.execute(new DeviceCommand() {
 					@Override
 					public void execute(Device selectedDevice) {
 						AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 						avPlayer.setVolume(volumeLevel);
 					}
 				});
 			}
 		});
 	}
 
 	private void initializeSwitchMuteAVPlayer() {
 		Switch switchMuteAVPlayer = (Switch) findViewById(R.id.switchMuteAVPlayer);
 
 		switchMuteAVPlayer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			private boolean isMuted = false;
 
 			@Override
 			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
 				isMuted = isChecked;
 
 				avPlayerDeviceManager.execute(new DeviceCommand() {
 					@Override
 					public void execute(Device selectedDevice) {
 						AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 						avPlayer.setMute(isMuted);
 					}
 				});
 			}
 		});
 	}
 
 	// TODO: Add button to disconnect from the selected device
 
 	public void refreshDeviceListEvent(View view) {
 		tvControllerDeviceFrontendManager.refreshDeviceList();
 		avPlayerDeviceFrontendManager.refreshDeviceList();
 		imageViewerDeviceFrontendManager.refreshDeviceList();
 	}
 
 	private void sendRemoteKey(final RemoteKey remoteKey) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.sendRemoteKey(remoteKey);
 			}
 		});
 	}
 
 	public void volumeUpKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_VOLUP);
 	}
 
 	public void volumeDownKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_VOLDOWN);
 	}
 
 	public void muteKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_MUTE);
 	}
 
 	public void channelUpKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_CHUP);
 	}
 
 	public void channelDownKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_CHDOWN);
 	}
 
 	public void preChKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_PRECH);
 	}
 
 	public void chListKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_CH_LIST);
 	}
 
 	public void arrowUpKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_UP);
 	}
 
 	public void arrowDownKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_DOWN);
 	}
 
 	public void arrowLeftKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_LEFT);
 	}
 
 	public void arrowRightKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_RIGHT);
 	}
 
 	public void enterKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_ENTER);
 	}
 
 	public void toolsKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_TOOLS);
 	}
 
 	public void infoKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_INFO);
 	}
 
 	public void returnKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_RETURN);
 	}
 
 	public void exitKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_EXIT);
 	}
 
 	public void smartHubKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_CONTENTS);
 	}
 
 	public void menuKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_MENU);
 	}
 
 	public void sourceKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_SOURCE);
 	}
 
 	public void powerOffKeyEvent(View view) {
 		sendRemoteKey(RemoteKey.KEY_POWEROFF);
 	}
 
 	// TODO: Add more buttons for numeric keys/dash, play-keys, color-keys, ..
 
 	public void openWebPageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 				String URL = editTextBrowseTerm.getText().toString();
 
 				tvController.openWebPage(URL);
 			}
 		});
 	}
 
 	public void searchInternetEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 				String searchTerm = editTextBrowseTerm.getText().toString();
 
 				tvController.openWebPage("http://www.google.com/search?q=" + searchTerm);
 			}
 		});
 	}
 
 	public void sendKeyboardStringEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 				String text = editTextBrowseTerm.getText().toString();
 
 				tvController.sendKeyboardString(text);
 				tvController.sendKeyboardEnd();
 			}
 		});
 	}
 
 	public void goHomePageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.goHomePage();
 			}
 		});
 	}
 
 	public void closeWebPageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.closeWebPage();
 			}
 		});
 	}
 
 	public void browserScrollUpEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.browserScrollUp();
 			}
 		});
 	}
 
 	public void browserScrollDownEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.browserScrollDown();
 			}
 		});
 	}
 
 	public void goPreviousWebPageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.goPreviousWebPage();
 			}
 		});
 	}
 
 	public void goNextWebPageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.goNextWebPage();
 			}
 		});
 	}
 
 	public void browserZoomInEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.browserZoomIn();
 			}
 		});
 	}
 
 	public void browserZoomOutEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.browserZoomOut();
 			}
 		});
 	}
 
 	public void browserZoomDefaultEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.browserZoomDefault();
 			}
 		});
 	}
 
 	public void refreshWebPageEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.refreshWebPage();
 			}
 		});
 	}
 
 	public void stopWebPageLoadingEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.stopWebPageLoading();
 			}
 		});
 	}
 
 	public void sendTouchClickEvent(View view) {
 		tvControllerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				TVController tvController = (TVController) selectedDevice;
 
 				tvController.sendTouchClick();
 			}
 		});
 	}
 
 	public void pauseAVPlayerEvent(View view) {
 		avPlayerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 				avPlayer.pause();
 			}
 		});
 	}
 
 	public void resumeAVPlayerEvent(View view) {
 		avPlayerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 				avPlayer.resume();
 			}
 		});
 	}
 
 	public void stopAVPlayerEvent(View view) {
 		avPlayerDeviceManager.execute(new DeviceCommand() {
 			@Override
 			public void execute(Device selectedDevice) {
 				AVPlayer avPlayer = (AVPlayer) selectedDevice;
 
 				avPlayer.stop();
 			}
 		});
 	}
 }
