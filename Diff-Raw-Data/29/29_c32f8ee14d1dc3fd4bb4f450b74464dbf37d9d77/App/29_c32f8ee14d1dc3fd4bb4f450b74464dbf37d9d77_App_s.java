 package com.yoharnu.newontv;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 import org.apache.commons.io.FileUtils;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
 import com.dropbox.client2.DropboxAPI.Entry;
 import com.dropbox.client2.ProgressListener;
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.exception.DropboxException;
 import com.dropbox.client2.exception.DropboxUnlinkedException;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
 import com.yoharnu.newontv.events.LoadingEvent;
 import com.yoharnu.newontv.shows.Series;
 
 import android.app.Activity;
 import android.app.Application;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class App extends Application {
 
 	public static final String API_KEY = "ogANe1zEs1OHNs7OUO5I";
 	protected static Context context;
 	public static SharedPreferences preferences;
 	public static LinkedList<Series> shows;
 	public static GregorianCalendar today;
 	private static boolean mExternalStorageAvailable = false;
 	private static boolean mExternalStorageWriteable = false;
 	final static private String APP_KEY = "dik24sgjbrpvnqm";
 	final static private String APP_SECRET = "ijw8q9xd90lo7h2";
 	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
 	static DropboxAPI<AndroidAuthSession> mDBApi;
 	static private boolean changed = false;
 
 	public void onCreate() {
 		super.onCreate();
 
 		context = getApplicationContext();
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		shows = new LinkedList<Series>();
 		today = new GregorianCalendar();
 		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
 		AndroidAuthSession session = new AndroidAuthSession(appKeys,
 				ACCESS_TYPE);
 		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
 		AccessTokenPair access = getStoredKeys();
 		if (access != null)
 			mDBApi.getSession().setAccessTokenPair(access);
 		checkPermissions();
 	}
 
 	private AccessTokenPair getStoredKeys() {
 		String key = preferences.getString("db-key", null);
 		String secret = preferences.getString("db-secret", null);
 		if (key == null || secret == null)
 			return null;
 		return new AccessTokenPair(key, secret);
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 
 	public static void add(String seriesId) {
 		Series temp = new Series(seriesId, Series.ID);
 		boolean present = false;
 		for (int i = 0; i < shows.size(); i++) {
 			if (shows.get(i).getSeriesId().equals(seriesId)) {
 				present = true;
 			}
 		}
 		if (!present) {
 			shows.add(temp);
 		}
 	}
 
 	public static void add(Series series) {
 		boolean present = false;
 		for (int i = 0; i < shows.size(); i++) {
 			if (shows.get(i).getSeriesId().equals(series.getSeriesId())) {
 				present = true;
 				break;
 			}
 		}
 		if (!present) {
 			shows.add(series);
 		}
 	}
 
 	public static void save() {
 		saveToFile();
 	}
 
 	private static void saveToFile() {
 		try {
 			File temp = new File(context.getFilesDir(), "shows-temp");
 			PrintStream out = new PrintStream(new File(context.getFilesDir(),
 					"shows-temp"));
 			for (int i = 0; i < shows.size(); i++) {
 				out.println(shows.get(i).getSeriesId());
 			}
 			File save = new File(context.getFilesDir(), "shows");
 			FileUtils.copyFile(temp, save);
 			temp.delete();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static void saveToDropbox(final Activity activity) {
 		final ProgressDialog pd = new ProgressDialog(activity);
 		pd.setCancelable(false);
 		pd.setTitle("Loading...");
 		pd.setMessage("Uploading to Dropbox");
 		pd.setIndeterminate(true);
 		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				pd.show();
 			}
 		});
 		new Thread(new Runnable() {
 			public void run() {
 				FileInputStream inputStream = null;
 				try {
 					File local = new File(context.getFilesDir(), "shows");
 					if (!local.exists())
 						return;
 					Entry existingEntry = mDBApi.metadata("/shows", 1, null,
 							false, null);
 					Log.i("Dropbox", "The file's rev is now: "
 							+ existingEntry.rev);
 					inputStream = new FileInputStream(local);
 					if (existingEntry != null && !existingEntry.isDeleted) {
 						mDBApi.delete("/shows");
 					}
 					Entry newEntry = mDBApi.putFile("/shows", inputStream,
 							local.length(), null, null);
 					Log.i("DbExampleLog", "The uploaded file's rev is: "
 							+ newEntry.rev);
 					SharedPreferences.Editor e = preferences.edit();
 					e.putString("db-shows-rev", newEntry.rev);
 					e.commit();
 				} catch (DropboxUnlinkedException e) {
 					// User has unlinked, ask them to link again here.
 					Log.e("Dropbox", "User has unlinked.");
 				} catch (DropboxException e) {
 					Log.e("Dropbox", "Something went wrong while uploading.");
 					e.printStackTrace();
 				} catch (FileNotFoundException e) {
 					Log.e("Dropbox", "File not found.");
 				} finally {
 					if (inputStream != null) {
 						try {
 							inputStream.close();
 						} catch (IOException e) {
 						}
 					}
 					pd.dismiss();
 				}
 			}
 		}).start();
 	}
 
 	static void load(final Activity activity) throws InterruptedException {
 		loadFromFile(activity);
 	}
 
 	static void loadFromDropbox(final Activity activity) {
 		final ProgressDialog pd = new ProgressDialog(activity);
 		pd.setCancelable(false);
 		pd.setTitle("Loading...");
 		pd.setMessage("Downloading from Dropbox");
 		pd.setIndeterminate(false);
 		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				pd.show();
 			}
 		});
 		new Thread(new Runnable() {
 			public void run() {
 				FileOutputStream outputStream = null;
 				try {
 					Entry existingEntry = mDBApi.metadata("/shows", 1, null,
 							false, null);
 					Log.i("Dropbox", "The file's rev is now: "
 							+ existingEntry.rev);
 					if (existingEntry.rev.equals(preferences.getString(
 							"db-shows-rev", null))
 							|| existingEntry == null
 							|| existingEntry.isDeleted) {
 						activity.runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								pd.dismiss();
 							}
 						});
 						return;
 					}
 					File local = new File(App.context.getFilesDir(), "shows");
 					outputStream = new FileOutputStream(local);
 					DropboxFileInfo info = mDBApi.getFile("/shows", null,
 							outputStream, new ProgressListener() {
 								@Override
 								public void onProgress(final long bytes,
 										final long total) {
 									activity.runOnUiThread(new Runnable() {
 										public void run() {
 											pd.setMax((int) total);
 											pd.setProgress((int) bytes);
 											if (bytes == total) {
 												pd.dismiss();
 											}
 										}
 									});
 								}
 							});
 					Log.i("Dropbox", "The file's rev is: "
 							+ info.getMetadata().rev);
 					SharedPreferences.Editor e = preferences.edit();
 					e.putString("db-shows-rev", info.getMetadata().rev);
 					e.commit();
 				} catch (DropboxException e) {
 					Log.e("Dropbox", "Something went wrong while downloading.");
 				} catch (FileNotFoundException e) {
 					Log.e("Dropbox", "File not found.");
 				} finally {
 					if (outputStream != null) {
 						try {
 							outputStream.close();
 						} catch (IOException e) {
 						}
 					}
 					if (pd.isShowing())
 						pd.dismiss();
 				}
 				try {
 					loadFromFile(activity);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 
 	}
 
 	protected static void loadFromFile(final Activity activity)
 			throws InterruptedException {
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 				final ProgressDialog pd = new ProgressDialog(activity);
 				pd.setTitle("Loading...");
 				pd.setMessage("Setting up shows");
 				pd.setIndeterminate(false);
 				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 				pd.setCancelable(false);
 				pd.show();
 				new Thread(new Runnable() {
 					public void run() {
 						try {
 							File shows = new File(context.getFilesDir(),
 									"shows");
 							Scanner s = new Scanner(shows);
 							App.shows.clear();
 
 							LinkedList<String> temp = new LinkedList<String>();
 							while (s.hasNextLine()) {
 								temp.add(s.nextLine());
 							}
 							s.close();
 
 							{
 								final int workaround = temp.size();
 								activity.runOnUiThread(new Runnable() {
 									public void run() {
 										pd.setMax(workaround);
 										pd.setProgress(0);
 									}
 								});
 							}
 							for (int i = 0; i < temp.size(); i++) {
 								add(temp.get(i));
 								final int workaround = i + 1;
 								activity.runOnUiThread(new Runnable() {
 									public void run() {
 										pd.setProgress(workaround);

 									}
 								});
 							}
 						} catch (FileNotFoundException e) {
 							e.printStackTrace();
 						}
 						activity.runOnUiThread(new Runnable() {
 							public void run() {
								pd.dismiss();
 							}
 						});
 						LoadingEvent.done();
 					}
 				}).start();
 			}
 		});
 	}
 
 	public static void sortByName() {
 		for (int i = 0; i < App.shows.size(); i++) {
 			Series temp = App.shows.get(i);
 			int iHole = i;
 			while (iHole > 0
 					&& App.shows.get(iHole - 1).getSeriesName()
 							.compareTo(temp.getSeriesName()) > 0) {
 				App.shows.set(iHole, App.shows.get(iHole - 1));
 				iHole--;
 			}
 			App.shows.set(iHole, temp);
 		}
 	}
 
 	protected void checkPermissions() {
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need
 			// to know is we can neither read nor write
 			mExternalStorageAvailable = false;
 			mExternalStorageWriteable = false;
 		}
 	}
 
 	public static boolean isExternalStorageWriteable() {
 		return mExternalStorageWriteable;
 	}
 
 	public static boolean isExternalStorageAvailable() {
 		return mExternalStorageAvailable;
 	}
 
 	public static boolean hasChanged() {
 		return changed;
 	}
 
 	public static void setChanged(boolean changed) {
 		App.changed = changed;
 	}
 
 }
