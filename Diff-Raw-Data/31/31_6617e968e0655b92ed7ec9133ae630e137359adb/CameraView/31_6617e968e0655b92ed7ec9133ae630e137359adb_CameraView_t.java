 package ch.cern.atlas.apvs.client.ui;
 
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.event.SupervisorSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.settings.Settings;
 import ch.cern.atlas.apvs.client.settings.SupervisorSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.media.client.Video;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class CameraView extends SimplePanel {
 
 	public static final int HELMET = 0;
 	public static final int HAND = 1;
 
 	private static final int DEFAULT_WIDTH = 400;
 	private static final int DEFAULT_HEIGHT = 300;
 
 	// FIXME
 	// private final String cameraURL = "rtsp://pcatlaswpss02:8554/worker1";
 	// private final String cameraURL =
 	// "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
 	// private final String cameraURL =
 	// "http://devimages.apple.com/iphone/samples/bipbop/gear4/prog_index.m3u8";
 	// private final String cameraURL =
 	// "http://quicktime.tc.columbia.edu/users/lrf10/movies/sixties.mov";
 	// private final String cameraURL =
 	// "rtsp://quicktime.tc.columbia.edu:554/users/lrf10/movies/sixties.mov";
 	private int videoWidth;
 	private int videoHeight;
 	private String videoPoster = "Default-640x480.jpg";
 	private Image image;
 
 	private int type;
 
 	private Integer ptuId;
 
 	private SupervisorSettings settings;
 
 	private String currentCameraUrl;
 
 	private final static String quickTime = "<script type=\"text/javascript\" language=\"javascript\" src=\"quicktime/AC_QuickTime.js\"></script>";
 
 	public CameraView(RemoteEventBus remoteEventBus,
 			RemoteEventBus localEventBus, int type) {
 		this(remoteEventBus, localEventBus, type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
 	}
 
 	public CameraView(RemoteEventBus remoteEventBus,
 			RemoteEventBus localEventBus, final int type, int width, int height) {
 		this.type = type;
 		this.videoWidth = width;
 		this.videoHeight = height;
 		
 	    image = new Image();
 		image.setWidth(videoWidth + Unit.PX.toString());
 		image.setHeight(videoHeight + Unit.PX.toString());
		image.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				update(true);
			}
		});
		image.addDoubleClickHandler(new DoubleClickHandler() {
			
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				System.err.println("Double Click "+event+" enlarge");
			}
		});
 
 		SupervisorSettingsChangedEvent.subscribe(remoteEventBus,
 				new SupervisorSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onSupervisorSettingsChanged(
 							SupervisorSettingsChangedEvent event) {
 
 						settings = event.getSupervisorSettings();
 
						update(false);
 					}
 				});
 
 		SelectPtuEvent.subscribe(localEventBus, new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 
				update(false);
 			}
 		});
 	}
 
 	private String getCameraUrl(int type, Integer ptuId) {
 		if ((settings == null) || (ptuId == null)) {
 			return null;
 		}
 
 		return type == HELMET ? settings.getHelmetCameraUrl(
 				Settings.DEFAULT_SUPERVISOR, ptuId) : settings
 				.getHandCameraUrl(Settings.DEFAULT_SUPERVISOR, ptuId);
 	}
 
	private void update(boolean force) {
 		String cameraUrl = getCameraUrl(type, ptuId);
 		if ((cameraUrl == null) || cameraUrl.trim().equals("")) {
 			image.setUrl(videoPoster);
 			setWidget(image);
 			return;
 		}
 
		if (!force && (cameraUrl.equals(currentCameraUrl))) {
 			return;
 		}
 
 		currentCameraUrl = cameraUrl;
 
 		if (cameraUrl.startsWith("http://")) {
 			if (cameraUrl.endsWith(".mjpg")) {
 				System.err.println(cameraUrl);
 				image.setUrl(cameraUrl);
 				setWidget(image);
 			} else {
 				Video video = Video.createIfSupported();
 				if (video != null) {
 					video.setWidth(videoWidth + Unit.PX.toString());
 					video.setHeight(videoHeight + Unit.PX.toString());
 					video.setControls(true);
 					video.setAutoplay(true);
 					video.setPoster(videoPoster);
 					video.setMuted(true);
 					video.setLoop(true);
 					video.addSource(cameraUrl);
 				}
 				System.err.println(video.toString());
 				setWidget(video);
 			}
 		} else if (cameraUrl.startsWith("rtsp://")) {
 			Widget video = new HTML(
 					"<embed width=\""
 							+ videoWidth
 							+ "\" height=\""
 							+ videoHeight
 							+ "\" src=\""
 							+ cameraUrl
 							+ "\" autoplay=\"true\" type=\"video/quicktime\" controller=\"true\" quitwhendone=\"false\" loop=\"false\"/></embed>");
 			System.err.println(video.toString());
 			setWidget(video);
 		} else {
 			Widget video = new HTML(
 					quickTime
 							+ "<script language=\"javascript\" type=\"text/javascript\">"
 							+ "QT_WriteOBJECT('"
 							+ videoPoster
 							+ "', '"
 							+ videoWidth
 							+ "', '"
 							+ videoHeight
 							+ "', '', 'href', '"
 							+ cameraUrl
 							+ "', 'autohref', 'true', 'target', 'myself', 'controller', 'true', 'autoplay', 'true');</script>");
 			System.err.println(video.toString());
 			setWidget(video);
 		}
 	}
 }
