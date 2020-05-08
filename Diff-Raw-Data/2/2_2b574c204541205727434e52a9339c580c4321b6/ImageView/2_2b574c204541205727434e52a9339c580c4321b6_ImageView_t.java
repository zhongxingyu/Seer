 package ch.cern.atlas.apvs.client.ui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.google.gwt.media.client.Video;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ImageView extends SimplePanel {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private String currentCameraUrl;
 	private String videoWidth;
 	private String videoHeight;
 	private String videoPoster = "Default-640x480.jpg";
 	protected Image image;
 	private boolean isMovingJPEG;
 	
 	private ClientFactory factory;
 
 	// FIXME #644 can use 
 //	private final static String quickTime = "/quicktime/AC_QuickTime.js";
 	private final static String quickTime = "http://localhost:8095/quicktime/AC_QuickTime.js";
 
 	protected ImageView() {
 	}
 
 	public ImageView(ClientFactory factory, String cameraUrl) {
 		init(factory, "100%", "");
 		setUrl(cameraUrl);
 	}
 
 	protected void init(ClientFactory factory, String width, String height) {
 		this.factory = factory;
 		this.videoWidth = width;
 		this.videoHeight = height;
 
 		image = new Image();
 		image.setWidth(videoWidth);
 		image.setHeight(videoHeight);
 
 		// keep setting URL for video not to get stuck, FIXME #425 find a better
 		// way...
 		// FIX seems to keep adding resources in Safari (WebKit), so avoid this
 		boolean keepSettingUrl = !Window.Navigator.getUserAgent().contains("WebKit");
 		if (keepSettingUrl) {
 			Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
 
 				@Override
 				public boolean execute() {
 					if (!isAttached()) {
 						return false;
 					}
 
 					if ((currentCameraUrl != null) && isMovingJPEG) {
 						image.setUrl(currentCameraUrl);
 						// Window.alert("Set URL to "+currentCameraUrl);
 					}
 
 					return true;
 				}
 			}, 30000);
 		}
 	}
 
 	public boolean setUrl(String cameraUrl) {
 
 		if ((cameraUrl == null) || cameraUrl.trim().equals("")) {
 			currentCameraUrl = null;
 
 			if (image != null) {
 				image.setUrl("");
 			}
 			image = new Image();
 			image.setWidth(videoWidth);
 			image.setHeight(videoHeight);
 
 			image.setUrl(videoPoster);
 			image.setTitle("");
 			setWidget(image);
 			isMovingJPEG = false;
 			return false;
 		}
 
 		if (cameraUrl.equals(currentCameraUrl)) {
 			return false;
 		}
 
 		currentCameraUrl = cameraUrl;
 
		if (cameraUrl.startsWith("http://") || cameraUrl.startsWith("https://")) {
 			if (cameraUrl.endsWith(".mjpg")) {
 				log.info(cameraUrl);
 
 				if (image != null) {
 					image.setUrl("");
 				}
 				image = new Image();
 				image.setWidth(videoWidth);
 				image.setHeight(videoHeight);
 
 				image.setUrl(cameraUrl);
 				image.setTitle(cameraUrl);
 				setWidget(image);
 				isMovingJPEG = true;
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
 				log.info(video.toString());
 				video.setTitle(cameraUrl);
 				setWidget(video);
 				isMovingJPEG = false;
 			}
 		} else if (cameraUrl.startsWith("rtsp://")) {
 			Widget video = new HTML(
 					"<embed width=\""
 							+ getOffsetWidth()
 							+ "\" height=\""
 							+ getOffsetHeight()
 							+ "\" src=\""
 							+ cameraUrl
 							+ "\" autoplay=\"true\" type=\"video/quicktime\" controller=\"true\" quitwhendone=\"false\" loop=\"false\"/></embed>");
 			log.info(video.toString());
 			video.setTitle(cameraUrl);
 			setWidget(video);
 			isMovingJPEG = false;
 		} else {
 			Widget video = new HTML(
 					getQuickTime()
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
 			log.info(video.toString());
 			video.setTitle(cameraUrl);
 			setWidget(video);
 			isMovingJPEG = false;
 		}
 		return true;
 	}
 	
 	private String getQuickTime() {
 		return "<script type=\"text/javascript\" language=\"javascript\" src=\""+factory.getProxy().getReverseUrl(quickTime)+"\"></script>";
 	}
 }
