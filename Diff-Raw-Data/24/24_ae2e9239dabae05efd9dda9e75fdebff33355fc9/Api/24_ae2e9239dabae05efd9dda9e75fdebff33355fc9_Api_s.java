 package net.beshkenadze.android.socialwallpapers.api;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import net.beshkenadze.android.socialwallpapers.data.Album;
 import net.beshkenadze.android.socialwallpapers.data.FQLResponse;
 import net.beshkenadze.android.socialwallpapers.data.Image;
 import net.beshkenadze.android.socialwallpapers.data.Like;
 import net.beshkenadze.android.socialwallpapers.data.Photo;
 import net.beshkenadze.android.utils.Debug;
 import net.beshkenadze.android.utils.Download;
 
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Display;
 import android.view.WindowManager;
 
 import com.facebook.android.Facebook;
 import com.facebook.android.Util;
 
 public class Api {
 	protected static final int QUERY_RESULT = 0;
 	private String mOwnerId;
 	private Facebook mFacebook;
 	private Context mContext;
 	private Point mSize;
 	private WallpaperManager mWallpaperManager;
 
 	public interface DefaultApiRequest {
 		void onError();
 	}
 
 	public interface OnApiRequest extends DefaultApiRequest {
 	}
 
 	public interface OnAlbumsRequest extends DefaultApiRequest {
 		void onRecive(List<Album> albums);
 	}
 
 	public interface OnAlbumRequest extends DefaultApiRequest {
 		void onRecive(Album album);
 	}
 
 	public interface OnPhotosRequest extends DefaultApiRequest {
 		void onRecive(List<Photo> photos);
 	}
 
 	public interface OnPhotoRequest extends DefaultApiRequest {
 		void onRecive(String photo, String preview);
 	}
 
 	public interface OnWallaperSet extends DefaultApiRequest {
 		void onSet();
 	}
 
 	public interface OnloadImageToWallpaper extends DefaultApiRequest {
 		void onSet();
 	}
 
 	public Api(Context c, Facebook facebook, String ownerId) {
 		mFacebook = facebook;
 		mContext = c;
 		mOwnerId = ownerId;
 
 		WindowManager wm = (WindowManager) mContext
 				.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point size = new Point();
		display.getSize(size);
		setDisplaySize(size);
 
 		mWallpaperManager = WallpaperManager.getInstance(c);
 	}
 
 	public int getDisplayWidth() {
 		return getDisplaySize().x;
 	}
 
 	public int getDisplayHeight() {
 		return getDisplaySize().y;
 
 	}
 
 	private Point getDisplaySize() {
 		return mSize;
 	}
 
 	private void setDisplaySize(Point size) {
 		mSize = size;
 	}
 
 	public boolean isReady() {
 		return mFacebook.isSessionValid();
 
 	}
 
 	public void loadImageToWallpaper(final String src,
 			final OnloadImageToWallpaper onloadImageToWallpaper) {
 		new Thread() {
 			@Override
 			public void run() {
 
 				File image = new Download(mContext).get(src);
 				FileInputStream is;
 				try {
 					is = new FileInputStream(image);
 					BufferedInputStream bis = new BufferedInputStream(is);
 					Bitmap bitmap = BitmapFactory.decodeStream(bis);
 					setCustomWallpaper(bitmap);
 					onloadImageToWallpaper.onSet();
 				} catch (FileNotFoundException e) {
 					onloadImageToWallpaper.onError();
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public Drawable getCurrentWallpaper() {
 		Drawable wallpaperDrawable = mWallpaperManager.getDrawable();
 		return wallpaperDrawable;
 	}
 
 	public void setCustomWallpaper(final Bitmap bitmap) {
 		try {
 			mWallpaperManager.setBitmap(bitmap);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void getAlbums(final OnAlbumsRequest callback) {
 		String fql = String
 				.format("SELECT aid, owner, name, object_id FROM album WHERE owner='%1$s'",
 						mOwnerId);
 		Handler albumsHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				List<Album> albums = new ArrayList<Album>();
 				switch (msg.arg1) {
 				case QUERY_RESULT:
 					String data = (String) msg.obj;
 					FQLResponse response = parseAlbumsList(data);
 					if (response != null) {
 						albums = response.getAlbums();
 
 					}
 					callback.onRecive(albums);
 					break;
 
 				default:
 					break;
 				}
 			}
 
 		};
 		query(fql, albumsHandler);
 	}
 
 	public void getPhotos(String aid, final OnPhotosRequest callback) {
 		String fql = String.format(
 				"SELECT pid, src ,images, caption FROM photo WHERE aid='%1$s'",
 				aid);
 		Handler photosHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				List<Photo> photos = new ArrayList<Photo>();
 				switch (msg.arg1) {
 				case QUERY_RESULT:
 					String data = (String) msg.obj;
 					Debug.i("data:" + data);
 
 					FQLResponse response = parsePhotosList(data);
 					if (response != null) {
 						photos = response.getPhotos();
 					}
 					callback.onRecive(photos);
 					break;
 
 				default:
 					break;
 				}
 			}
 
 		};
 		query(fql, photosHandler);
 	}
 
 	public void getCurrentWeekAlbum(final OnAlbumRequest onAlbumRequest) {
 		getAlbums(new OnAlbumsRequest() {
 			@Override
 			public void onRecive(List<Album> albums) {
 				if (albums.size() > 0) {
 					onAlbumRequest.onRecive(albums.get(0));
 				}
 			}
 
 			@Override
 			public void onError() {
 				onAlbumRequest.onError();
 			}
 		});
 
 	}
 
 	protected void prepareTopLiked(String aid,
 			final OnPhotosRequest onPhotosRequest, boolean user_only) {
 		String fql = String
 				.format("SELECT object_id FROM like WHERE object_id IN (SELECT object_id FROM photo WHERE aid = '%1$s')",
 						aid);
 		if (user_only) {
 			fql += " AND user_id = me()";
 		}
 		Debug.i("fql:" + fql);
 		Handler photosHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				List<Like> likes = new ArrayList<Like>();
 				Map<String, Integer> count = new TreeMap<String, Integer>();
 				switch (msg.arg1) {
 				case QUERY_RESULT:
 					String data = (String) msg.obj;
 					Debug.i("data:" + data);
 					//
 					FQLResponse response = parse(data);
 					if (response != null) {
 						likes = response.getLikes();
 						for (Like like : likes) {
 							if (count.containsKey(like.getObjectId())) {
 								count.put(like.getObjectId(),
 										count.get(like.getObjectId()) + 1);
 							} else {
 								count.put(like.getObjectId(), 1);
 							}
 						}
 						String fql = "SELECT pid, src ,images, caption FROM photo WHERE ";
 						SortedSet<Entry<String, Integer>> sorted = entriesSortedByValues(count);
 						int index = 0;
 						for (Entry<String, Integer> entry : sorted) {
 
 							if (index > 0) {
 								fql += " OR ";
 							}
 							fql += " object_id=" + entry.getKey();
 							index++;
 						}
						getTopLiked(fql, onPhotosRequest);

 					}
 					break;
 
 				default:
 					break;
 				}
 			}
 
 		};
 		query(fql, photosHandler);
 	}
 
 	private void getTopLiked(String fql, final OnPhotosRequest onPhotosRequest) {
 		Handler photosHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				List<Photo> photos = new ArrayList<Photo>();
 				switch (msg.arg1) {
 				case QUERY_RESULT:
 					String data = (String) msg.obj;
 					Debug.i("data:" + data);
 
 					FQLResponse response = parsePhotosList(data);
 					if (response != null) {
 						photos = response.getPhotos();
 					}
 					onPhotosRequest.onRecive(photos);
 					break;
 
 				default:
 					break;
 				}
 			}
 
 		};
 		query(fql, photosHandler);
 	}
 
 	private FQLResponse parseAlbumsList(String xml) {
 		Serializer serializer = new Persister();
 		FQLResponse mFQLResponse = null;
 		try {
 			mFQLResponse = serializer.read(FQLResponse.class, xml);
 			return mFQLResponse;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return mFQLResponse;
 	}
 
 	private FQLResponse parse(String xml) {
 		Serializer serializer = new Persister();
 		FQLResponse mFQLResponse = null;
 		try {
 			mFQLResponse = serializer.read(FQLResponse.class, xml);
 			return mFQLResponse;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return mFQLResponse;
 	}
 
 	private FQLResponse parsePhotosList(String xml) {
 		Serializer serializer = new Persister();
 		FQLResponse mFQLResponse = null;
 		try {
 			mFQLResponse = serializer.read(FQLResponse.class, xml);
 			return mFQLResponse;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return mFQLResponse;
 	}
 
 	public void query(final String fql, final Handler handler) {
 		new Thread() {
 			@Override
 			public void run() {
 				Bundle b = new Bundle();
 				b.putString("access_token", mFacebook.getAccessToken());
 				b.putString("query", fql);
 
 				try {
 					String myResult = Util.openUrl(
 							"https://api.facebook.com/method/fql.query", "GET",
 							b);
 					Message msg = new Message();
 					msg.arg1 = QUERY_RESULT;
 					msg.obj = myResult;
 					handler.sendMessage(msg);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public String getOwnerId() {
 		return mOwnerId;
 	}
 
 	public void setOwnerId(String mOwnerId) {
 		this.mOwnerId = mOwnerId;
 	}
 
 	public void getRandomPhoto(Album album, final OnPhotoRequest onPhotoRequest) {
 		getPhotos(album.getAid(), new OnPhotosRequest() {
 			@Override
 			public void onRecive(List<Photo> photos) {
 				if (photos != null && photos.size() > 0) {
 					int randInt = getRandomInt(photos.size() - 1);
 					Photo photo = photos.get((randInt > 0 ? randInt
 							: 0));
 					String src = photo.getImages().get(0)
 							.getSource();
 					String preview = src;
 					for (Image image : photo.getImages()) {
 						if (getDisplayWidth() <= Integer
 								.parseInt(image.getWidth())
 								&& getDisplayHeight() <= Integer
 										.parseInt(image.getHeight())) {
 							preview = image.getSource();
 						}
 					}
 					onPhotoRequest.onRecive(src, preview);
 				}
 			}
 
 			@Override
 			public void onError() {
 				onPhotoRequest.onError();
 			}
 		});
 	}
 
 	public void getTopPhotos(Album album,
 			final OnPhotosRequest onPhotosRequest, final boolean user_only) {
 		prepareTopLiked(album.getAid(), new OnPhotosRequest() {
 			@Override
 			public void onRecive(List<Photo> photos) {
 				if (photos != null && photos.size() > 0) {
 					onPhotosRequest.onRecive(photos);
 				}
 			}
 
 			@Override
 			public void onError() {
 				onPhotosRequest.onError();
 			}
 		}, user_only);
 	}
 
 	private int getRandomInt(int max) {
 		if(max > 0){
 			return new Random().nextInt(max);
 		}
 		return 0;
 	}
 
 	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
 			Map<K, V> map) {
 		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
 				new Comparator<Map.Entry<K, V>>() {
 					@Override
 					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
 						int res = e2.getValue().compareTo(e1.getValue());
 						return res != 0 ? res : 1;
 					}
 				});
 		sortedEntries.addAll(map.entrySet());
 		return sortedEntries;
 	}
 }
