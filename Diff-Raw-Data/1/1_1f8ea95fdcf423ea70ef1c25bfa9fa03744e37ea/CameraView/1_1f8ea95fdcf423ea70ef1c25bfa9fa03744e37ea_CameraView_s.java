 package ch.cern.atlas.apvs.client.ui;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 
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
 
 	public static final String HELMET = "Helmet";
 	public static final String HAND = "Hand";
 	
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
 	private String videoWidth;
 	private String videoHeight;
 	private String videoPoster = "Default-640x480.jpg";
 	private Image image;
 
 	private String type;
 
 	private String ptuId;
 
 	private PtuSettings settings;
 
 	private String currentCameraUrl;
 
 	private final static String quickTime = "<script type=\"text/javascript\" language=\"javascript\" src=\"quicktime/AC_QuickTime.js\"></script>";
 
 	public CameraView(ClientFactory factory, Arguments args) {
 		this(factory, args.getArg(0), "100%", "100%");
 	}
 
 	public CameraView(ClientFactory factory, final String type, String width,
 			String height) {
 		this.type = type;
 		this.videoWidth = width;
 		this.videoHeight = height;
 
 		image = new Image();
 		image.setWidth(videoWidth);
 		image.setHeight(videoHeight);
 		image.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				update(true);
 			}
 		});
 		image.addDoubleClickHandler(new DoubleClickHandler() {
 
 			@Override
 			public void onDoubleClick(DoubleClickEvent event) {
 				System.err.println("Double Click " + event + " enlarge");
 			}
 		});
 
 		PtuSettingsChangedEvent.subscribe(factory.getRemoteEventBus(),
 				new PtuSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedEvent event) {
 
 						settings = event.getPtuSettings();
 
 						update(false);
 					}
 				});
 
 		SelectPtuEvent.subscribe(factory.getEventBus("local"), new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 
 				update(false);
 			}
 		});
 	}
 
 	private String getCameraUrl(String type, String ptuId) {
 		if ((settings == null) || (ptuId == null)) {
 			return null;
 		}
 
 		return settings.getCameraUrl(ptuId, type);
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
 					video.setWidth(videoWidth);
 					video.setHeight(videoHeight);
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
