 package com.smaxll.apps.android.flickrautobackup;
 
 import android.database.ContentObserver;
 import android.os.Handler;
import android.util.Log;
 
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ImageTableObserver extends ContentObserver {
 
 	static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ImageTableObserver.class);
 
 	public ImageTableObserver() {
 		super(new Handler());
 	}
 
 	@Override
 	public void onChange(boolean change) {
 		try {
 			if (!Utils.getBooleanProperty(Preferences.AUTOUPLOAD, true) && !Utils.getBooleanProperty(Preferences.AUTOUPLOAD_VIDEOS, true)) {
 				LOG.debug("autoupload disabled");
                Log.d("Log", "autoupload disabled");
                return;
 			}
 
 			if (!FlickrApi.isAuthentified()) {
 				LOG.debug("Flickr not authentified yet");
                Log.d("Log", "Flickr not authentified yet");
 				return;
 			}
 
            Log.d("stargin loading media", "loading media");
 			List<Media> media = Utils.loadImages(null, 10);
 			if (media == null || media.isEmpty()) {
 				LOG.debug("no media found");
                Log.d("no media", "no media found");
 				return;
 			}
            Log.d(".", "media loaded");
 
 			List<Media> not_uploaded = new ArrayList<Media>();
 			for (Media image : media.subList(0, Math.min(10, media.size()))) {
 				if (image.mediaType == Utils.MediaType.photo && !Utils.getBooleanProperty(Preferences.AUTOUPLOAD, true)) {
 					LOG.debug("not uploading " + media + " because photo upload disabled");
 					continue;
 				} else if (image.mediaType == Utils.MediaType.video && !Utils.getBooleanProperty(Preferences.AUTOUPLOAD_VIDEOS, true)) {
 					LOG.debug("not uploading " + media + " because video upload disabled");
 					continue;
 				} else {
 					boolean uploaded = FlickrApi.isUploaded(image);
 					LOG.debug("uploaded : " + uploaded + ", " + image);
 					if (!uploaded) {
 						File file = new File(image.path);
 						if (!Utils.isAutoUpload(new Folder(file.getParent()))) {
 							LOG.debug("Ignored : " + file);
 						} else {
 							int sleep = 0;
 							while (file.length() < 100 && sleep < 5) {
 								LOG.debug("sleeping a bit");
 								sleep++;
 								Thread.sleep(1000);
 							}
 							not_uploaded.add(image);
 						}
 					}
 				}
 			}
 			if (!not_uploaded.isEmpty()) {
 				LOG.debug("enqueuing " + not_uploaded.size() + " media: " + not_uploaded);
 				UploadService.enqueue(true, not_uploaded, null, Utils.getInstantAlbumId(), STR.instantUpload);
 //				FlickrUploaderActivity.staticRefresh(true);
 			}
 		} catch (Throwable e) {
 			LOG.error(e.getMessage(), e);
 		}
 	}
 }
