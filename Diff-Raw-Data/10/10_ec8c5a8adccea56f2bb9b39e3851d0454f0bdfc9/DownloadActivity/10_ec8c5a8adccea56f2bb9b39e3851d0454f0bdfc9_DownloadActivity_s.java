 package edu.vanderbilt.cs282.feisele.assignment6.ui;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.ProgressDialog;
 import android.content.AsyncQueryHandler;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager;
 import android.text.Editable;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import edu.vanderbilt.cs282.feisele.assignment6.DownloadCallback;
 import edu.vanderbilt.cs282.feisele.assignment6.DownloadRequest;
 import edu.vanderbilt.cs282.feisele.assignment6.R;
 import edu.vanderbilt.cs282.feisele.assignment6.lifecycle.LLActivity;
 import edu.vanderbilt.cs282.feisele.assignment6.provider.DownloadContentProviderSchema.ImageTable;
 import edu.vanderbilt.cs282.feisele.assignment6.provider.DownloadContentProviderSchema.Order;
 import edu.vanderbilt.cs282.feisele.assignment6.provider.DownloadContentProviderSchema.Selection;
 import edu.vanderbilt.cs282.feisele.assignment6.service.DownloadService;
 
 /**
  * 
  * An activity which prompts the user for an image to download. <h2>Program
  * Description</h2>
  * <p>
  * This assignment builds upon the various Android concurrency models from the
  * previous assignmentsThis assignment gives you experience with several
  * variants of an Android ContentProvider to download bitmap images from a web
  * server and display them via an Activity that communicates to the
  * ContentProvider via a ContentResolver. This Activity has a similar user
  * interface as previous assignments and works as follows:
  * 
  * <ol>
  * <li>The Activity provides a menu of buttons and displays a default image
  * (configured via the XML assets files)</li>
  * <li>The user is prompted to enter the URL for a new bitmap image</li>
  * <li>After entering the desired URL, the user can select one of several
  * buttons that provide different ways to download the image.</li>
  * <li>After the URL download has completed it will be displayed in an ImageView
  * </li>
  * <li>The user can reset the image to its default contents by clicking the
  * "Reset Image" button.</li>
  * 
  * </ol>
  * <p>
  * note: the default image is configured via an XML resource file and the
  * default image itself is part of the project's assets.
  * 
  * 
  * 
  * 
  * <h2>Fault Handling</h2>
  * If there is a problem in the entered URL a toast is displayed indicating the
  * problem.
  * 
  * <h2>Details</h2>
  * <ul>
  * <li>
  * It contains a DownloadActivity class that inherits from Activity and uses the
  * XML layout containing a TextView object that prompts for the URL of the
  * bitmap file and stores the entered URL in an EditText object.
  * <li>
  * It uses five Button objects with the labels "Download File",
  * "Query via query()", "Query via CursorLoader", "Query via AsyncQueryHandler",
  * and "Reset Image" to run the corresponding hook methods that use the URL
  * provided by the user to download and display the designated bitmap file using
  * the appropriate methods.
  * 
  * <li>
  * The service components must run in a separate process than the
  * DownloadActivity component.
  * <li>
  * The Button objects that initiate the downloading of the bitmap file must be
  * connected to the corresponding DownloadActivity.run*() methods via the
  * appropriate android:onClick="..." attributes.
  * </ul>
  * 
  * 
  * @author "Fred Eisele" <phreed@gmail.com>
  * 
  */
 public class DownloadActivity extends LLActivity {
 	static private final Logger logger = LoggerFactory
 			.getLogger("class.activity.download");
 
 	static final int NUM_ITEMS = 10;
 
 	private static final int IMAGE_LOADER_ID = 0x01;
 
 	private CursorPagerAdapter<DownloadFragment> adapter;
 	private ViewPager pager;
 
 	private EditText urlEditText = null;
 	private ProgressDialog progress;
 
 	private Uri activeUri = null;
 
 	public String getActiveUri() {
 		if (this.activeUri == null)
 			return "";
 		return this.activeUri.toString();
 	}
 
 	private LoaderManager.LoaderCallbacks<Cursor> imageCursorLoader = null;
 
 	/**
 	 * An extension to the basic connection which holds information about the
 	 * state of the connection and the service binding.
 	 * <p>
 	 * This cannot be implemented as a generic as there is no interface defining
 	 * the asInterface() method on the stub. (or at least I don't know how)
 	 */
 	static abstract public class DownloadServiceConnection<T> implements
 			ServiceConnection {
 		public T service;
 		public boolean isBound;
 
 		public void onServiceConnected(ComponentName className, IBinder iservice) {
 			logger.debug("call service connected");
 			this.isBound = true;
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 			this.isBound = false;
 		}
 	};
 
 	/**
 	 * provide implementation for the DownloadCall class.
 	 */
 	static private DownloadServiceConnection<DownloadRequest> asyncConnection = new DownloadServiceConnection<DownloadRequest>() {
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder iservice) {
 			super.onServiceConnected(className, iservice);
 			this.service = DownloadRequest.Stub.asInterface(iservice);
 		}
 	};
 
 	/**
 	 * The fragment is used to preserve state across various changes.
 	 * <p>
 	 * In this implementation fragment is not intimately tied to a single
 	 * activity instance. When the activity is restarted a new activity instance
 	 * is (may be) created. The fragment persists across this restart and it
 	 * must be attached to the new activity instance. For this reason the
 	 * convenient "<fragment>" xml element cannot be used. A ViewGroup (in this
 	 * case a FrameLayout is used.
 	 * <p>
 	 * 
 	 * @see http 
 	 *      ://developer.android.com/training/basics/fragments/fragment-ui.html
 	 *      #AddAtRuntime
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_download);
 
 		this.pager = (ViewPager) findViewById(R.id.image_pager);
 		this.adapter = new CursorPagerAdapter<DownloadFragment>(
 				getSupportFragmentManager(), DownloadFragment.class, null);
 		this.pager.setAdapter(this.adapter);
 
 		this.urlEditText = (EditText) findViewById(R.id.edit_image_url);
 
 		this.explicitlyBindService(DownloadActivity.asyncConnection,
 				DownloadService.class);
 
 		this.imageCursorLoader = new MyCursorLoader(this);
 
 		if (savedInstanceState != null) {
 			this.activeUri = Uri.parse(savedInstanceState
 					.getString(ACTIVE_URL_KEY));
 		}
 		final Bundle bundle = new Bundle();
 		bundle.putString(ACTIVE_URL_KEY, this.getActiveUri());
 
 		final LoaderManager lm = this.getSupportLoaderManager();
 		lm.initLoader(IMAGE_LOADER_ID, bundle, this.imageCursorLoader);
 	}
 
 	final static String PROGRESS_RUNNING_STATE_KEY = "progress_running_state_key";
 	final static String ACTIVE_URL_KEY = "active_url_key";
 
 	/**
 	 * If the download is ongoing then the progress indicator will need to be
 	 * started. Set a flag in the fragment indicating that there is a pending
 	 * download as well.
 	 */
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		final boolean wasProgressRunning = savedInstanceState
 				.getBoolean(PROGRESS_RUNNING_STATE_KEY);
 		if (wasProgressRunning)
 			logger.trace("progress was running ");
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putBoolean(PROGRESS_RUNNING_STATE_KEY,
 				this.isProgressRunning());
 		savedInstanceState.putString(ACTIVE_URL_KEY, this.getActiveUri());
 
 		super.onSaveInstanceState(savedInstanceState);
 		logger.debug("onSaveInstanceState");
 	}
 
 	/**
 	 * Shut things down that aren't needed.
 	 */
 	@Override
 	public void onStop() {
 		super.onStop();
 		this.stopProgress();
 
 		if (DownloadActivity.asyncConnection.isBound)
 			this.unbindService(DownloadActivity.asyncConnection);
 	}
 
 	/**
 	 * Generic helper method for binding to a service.
 	 * 
 	 * @param <T>
 	 */
 	private void explicitlyBindService(ServiceConnection conn,
 			Class<? extends DownloadService> clazz) {
 		logger.debug("bining to service explicitly {} {}", conn, clazz);
 		final Intent intent = new Intent(this, clazz);
 		this.bindService(intent, conn, Context.BIND_AUTO_CREATE);
 	}
 
 	/**
 	 * User cursor loader to get the latest image from the content provider.
 	 */
 	private static class MyCursorLoader implements
 			LoaderManager.LoaderCallbacks<Cursor> {
 		private final DownloadActivity master;
 
 		public MyCursorLoader(DownloadActivity master) {
 			this.master = master;
 		}
 
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 			logger.debug("loader on create : {} {}", id, bundle);
 			final String activeUrl = bundle.getString(ACTIVE_URL_KEY);
 			final CursorLoader cursorLoader = new CursorLoader(this.master,
 					ImageTable.CONTENT_URI, null, Selection.BY_URI.code,
 					new String[] { activeUrl }, Order.BY_ID.ascending());
 			return cursorLoader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 			logger.debug("loader finished: {}", loader.getId());
 			switch (loader.getId()) {
 			case IMAGE_LOADER_ID:
 				logger.debug("loaded image");
 				this.master.adapter.swapCursor(cursor);
 			}
 		}
 
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 			logger.debug("loader reset");
 			switch (loader.getId()) {
 			case IMAGE_LOADER_ID:
 				this.master.adapter.swapCursor(null);
 			}
 		}
 	}
 
 	/**
 	 * (@see
 	 * http://tumble.mlcastle.net/post/25875136857/bridging-cursorloaders-and
 	 * -viewpagers-on-android)
 	 * 
 	 * @param <F>
 	 */
 	public class CursorPagerAdapter<F extends Fragment> extends
 			FragmentStatePagerAdapter {
 		private final Class<F> fragmentClass;
 		private Cursor cursor;
 
 		/**
 		 * Provide the FragmentManager and initial Cursor (which is usually
 		 * null), the constructor also takes the Class object for the type of
 		 * Fragment you wish to create, and the projection you passed to your
 		 * CursorLoader. The projection will be used to automatically fill in
 		 * your Fragment’s arguments with the data from the Cursor.
 		 * 
 		 * @param fm
 		 * @param fragmentClass
 		 * @param cursor
 		 */
 		public CursorPagerAdapter(final FragmentManager fm,
 				final Class<F> fragmentClass, Cursor cursor) {
 			super(fm);
 			this.fragmentClass = fragmentClass;
 			this.cursor = cursor;
 		}
 
 		/**
 		 * The cursor can return values of various types. These should be cast
 		 * into a similar form for the arguments.
 		 */
 		@Override
 		public F getItem(int position) {
 			if (cursor == null) {
 				return null;
 			}
 			cursor.moveToPosition(position);
 			final F frag;
 			try {
 				frag = this.fragmentClass.newInstance();
 			} catch (Exception ex) {
 				throw new RuntimeException(ex);
 			}
 			frag.onAttach(DownloadActivity.this);
 			final Bundle args = new Bundle();
 			final String[] projection = cursor.getColumnNames();
 			for (int ix = 0; ix < projection.length; ++ix) {
 				switch (cursor.getType(ix)) {
 				case Cursor.FIELD_TYPE_NULL:
 					break;
 				case Cursor.FIELD_TYPE_FLOAT:
 					args.putFloat(projection[ix], cursor.getFloat(ix));
 					break;
 				case Cursor.FIELD_TYPE_BLOB:
 					break;
 				case Cursor.FIELD_TYPE_INTEGER:
 					args.putInt(projection[ix], cursor.getInt(ix));
 					break;
 				case Cursor.FIELD_TYPE_STRING:
 					args.putString(projection[ix], cursor.getString(ix));
 					break;
 				}
 			}
 			frag.setArguments(args);
 			return frag;
 		}
 
 		/**
 		 * Inform the pager how many items there are.
 		 */
 		@Override
 		public int getCount() {
 			if (cursor == null) {
 				return 0;
 			}
 			return cursor.getCount();
 		}
 
 		/**
 		 * This is pure voodoo to get the reload effect.
 		 * <p>
 		 * getItemPosition() is called when the host view is attempting to
 		 * determine if an item's position has changed such as following
 		 * notifyDataSetChanged(). Returning POSITION_NONE indicates the object
 		 * is no longer present in the adapter. When notifyDataSetChanged() is
 		 * called, getItemPosition() is called for each object. Returning
 		 * POSITION_NONE indicates that the position of the object nowhere,
 		 * hence the view pager will remove each view and reload.
 		 */
 		@Override
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 
 		/**
 		 * Used to indicate that the underlying data has changed. The
 		 * notifyDataSetChanged() causes the views displayed by the fragments to
 		 * refresh.
 		 * 
 		 * @param cursor
 		 */
 		public void swapCursor(Cursor cursor) {
 			if (this.cursor == cursor) {
 				return;
 			}
 			logger.debug("cursor swapped");
 			this.cursor = cursor;
 			this.notifyDataSetChanged();
 		}
 
 	}
 
 	/**
 	 * Initialize and configure the progress dialog. Record the fact that a
 	 * download is expected in the fragment.
 	 */
 	private void startProgress(CharSequence msg) {
 		logger.debug("startProgress");
 		this.progress = new ProgressDialog(this);
 		this.progress.setTitle(R.string.dialog_progress_title);
 		this.progress.setMessage(msg);
 		this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		this.progress.setProgress(0);
 		this.progress.show();
 	}
 
 	/**
 	 * The progress can be null when the activity is stopped. The progress is
 	 * not dismissed unless it is showing. Record the fact that progress in
 	 * complete in the fragment.
 	 */
 	private void stopProgress() {
 		logger.debug("stopProgress");
 		if (this.isProgressRunning())
 			this.progress.dismiss();
 	}
 
 	/**
 	 * Check to see if the progress indicator is active.
 	 * 
 	 * @return
 	 */
 	private boolean isProgressRunning() {
 		if (this.progress == null)
 			return false;
 		if (!this.progress.isShowing())
 			return false;
 		return true;
 	}
 
 	/**
 	 * Progress dialog can be shut down the
 	 */
 	public void onComplete(final String msg) {
 		logger.debug("onComplete");
 		this.runOnUiThread(new Runnable() {
 			final DownloadActivity master = DownloadActivity.this;
 
 			public void run() {
 				master.activeUri = Uri.parse(msg);
 				master.stopProgress();
 			}
 		});
 	}
 
 	/**
 	 * Should any of the downloads fail produce a warning indicator on the url
 	 * field. As this is most likely called from the background thread
 	 * performing the download the field update is forced to the ui thread.
 	 */
 	public void onFault(final CharSequence msg) {
 		this.runOnUiThread(new Runnable() {
 			final CharSequence msg_ = msg;
 			final DownloadActivity master = DownloadActivity.this;
 
 			public void run() {
 				logger.debug("onFault");
 				Toast.makeText(master, msg, Toast.LENGTH_LONG).show();
 
 				final Drawable dr = master.getResources().getDrawable(
 						R.drawable.indicator_input_warn);
 				dr.setBounds(0, 0, dr.getIntrinsicWidth(),
 						dr.getIntrinsicHeight());
 				master.urlEditText.setError(msg_, dr);
 
 				master.stopProgress();
 			}
 		});
 	}
 
 	/**
 	 * Extract the url from the edit text widget. Check that the string in the
 	 * widget is a proper url. If the field is empty then use the value provided
 	 * as the hint. If the field is invalid
 	 * <ul>
 	 * <li>return a null indicating that the action should not be performed.</li>
 	 * <li>generate a toast informing the operator of his error</li>
 	 * <li>mark the field as having an error</li>
 	 * </ul>
 	 * 
 	 * @return
 	 */
 	private Uri getValidUrlFromWidget() {
 		final Editable urlEditable = this.urlEditText.getText();
 		if (urlEditable.length() < 1) {
 			return getUrlFromHint(this.urlEditText.getHint());
 		}
 		final String uriStr = urlEditable.toString();
 		if (uriStr == null) {
 			return getUrlFromHint(this.urlEditText.getHint());
 		}
 		try {
 			/** a cheap parse, not exactly correct, I'll get this next time */
 			new URL(uriStr);
 			return Uri.parse(uriStr);
 		} catch (MalformedURLException e) {
 			logger.warn("bad uri string");
 		}
 		final CharSequence errorMsg = this.getResources().getText(
 				R.string.error_malformed_url);
 		this.urlEditText.setError(errorMsg);
 		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
 		return null;
 	}
 
 	/**
 	 * If the operator has not actually entered a uri then get the one provided
 	 * as a hint.
 	 * 
 	 * @param seq
 	 * @return
 	 */
 	private Uri getUrlFromHint(final CharSequence seq) {
 		final String uriStr = seq.toString();
 		if (uriStr == null) {
 			return Uri.parse(this.getResources()
 					.getText(R.string.prompt_image_url).toString());
 		}
 		return Uri.parse(uriStr);
 	}
 
 	/**
 	 * Load the default image from the assets.
 	 * 
 	 * @param view
 	 */
 	public void resetImage(View view) {
 		logger.debug("resetDatabase");
 		final AsyncQueryHandler handler = new AsyncQueryHandler(
 				this.getContentResolver()) {
 			@Override
 			public void onDeleteComplete(int token, Object cookie, int result) {
 				logger.debug("reset complete : {} items deleted", result);
 				final DownloadActivity master = (DownloadActivity) cookie;
 				master.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						logger.debug("default cursor swap");
 						master.adapter.swapCursor(ImageTable.DEFAULT_CURSOR);
 					}
 				});
 			}
 		};
 		handler.startDelete(1, this, ImageTable.CONTENT_URI,
 				Selection.ALL.code, null);
 	}
 
 	/**
 	 * Download Images
 	 * <p>
 	 * Activity uses the Async AIDL model from assignment 5 to request a Bound
 	 * Service download the bitmaps in the designated URL and store them in the
 	 * application's internal storage. The Service should then create a URI for
 	 * the file that indicates file metadata (e.g., the timestamp for when the
 	 * file was downloaded represented as a long) and insert the corresponding
 	 * URI for the file into the ContentProvider (defined in your
 	 * AndroidManifest.xml file) along with metadata about the file . The
 	 * callback AIDL method returns the URI to the Activity, which displays the
 	 * URI as a Toast.</dd>
 	 * 
 	 * @param view
 	 */
 	public void runDownload(View view) {
 		logger.debug("runDownloadAsyncAidl");
 		final Uri uri = this.getValidUrlFromWidget();
 		if (uri == null)
 			return;
 
 		if (!DownloadActivity.asyncConnection.isBound) {
 			logger.warn("async service not bound");
 			return;
 		}
 		logger.debug("download async aidl");
 		try {
 			DownloadActivity.asyncConnection.service.downloadImage(uri,
 					callback);
 		} catch (RemoteException ex) {
 			logger.error("download async aidl", ex);
 		}
 		this.startProgress(this.getResources().getText(
 				R.string.message_progress_async));
 	}
 
 	private final DownloadCallback.Stub callback = new DownloadCallback.Stub() {
 		private DownloadActivity master = DownloadActivity.this;
 
 		public void sendPath(String url) throws RemoteException {
 			master.onComplete(url);
 		}
 
 		public void sendFault(String msg) throws RemoteException {
 			master.onFault(msg);
 		}
 
 	};
 
 	/**
 	 * Query via query()
 	 * <p>
 	 * The DownloadActivity spawns a Thread (or an AsyncTask) that calls query()
 	 * on the ContentResolver to request that the associated ContentProvider to
 	 * provide a Cursor containing all the file(s) that match the URI back to
 	 * thread, which opens the file(s) and causes the bitmap(s) to be displayed
 	 * on the screen.
 	 * 
 	 * @param view
 	 */
 	public void runQueryViaQuery(View view) {
 		logger.debug("run query via query()");
 		final Runnable makeQuery = new Runnable() {
 			private final DownloadActivity master = DownloadActivity.this;
 
 			public void run() {
 				final Cursor cursor = master.getContentResolver().query(
 						ImageTable.CONTENT_URI, null, Selection.BY_URI.code,
 						new String[] { master.getActiveUri() },
 						Order.BY_ID.ascending());
 				master.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						logger.debug("query cursor size=<{}>",
 								cursor.getCount());
 						master.adapter.swapCursor(cursor);
 						master.adapter.notifyDataSetChanged();
 					}
 				});
 			}
 		};
 		new Thread(makeQuery).start();
 	}
 
 	/**
 	 * Query via CursorLoader
 	 * <p>
 	 * The DownloadActivity uses a CursorLoader to return a Cursor containing
 	 * all the file(s) that match the URI back to thread, which opens the
 	 * file(s) and causes the bitmap(s) to be displayed on the screen back to
 	 * DownloadActivity as an onLoadFinished() callback, which opens the file(s)
 	 * and causes the bitmap(s) to be displayed on the screen.
 	 * 
 	 * @param view
 	 */
 	public void runQueryViaLoader(View view) {
 		logger.debug("run query via content loader");
 
 		final Bundle bundle = new Bundle();
 		bundle.putString(ACTIVE_URL_KEY, this.getActiveUri());
 
 		final LoaderManager lm = this.getSupportLoaderManager();
 		lm.restartLoader(IMAGE_LOADER_ID, bundle, this.imageCursorLoader);
 	}
 
 	/**
 	 * Query via AsyncQueryHandler
 	 * <p>
 	 * The DownloadActivity uses an AsyncQueryHandler to return a Cursor
 	 * containing all the file(s) that match the URI back to thread, back to
 	 * DownloadActivity as an onQueryComplete() callback, which opens the
 	 * file(s) and causes the bitmap(s) to be displayed on the screen.
 	 * 
 	 * @param view
 	 */
 	public void runQueryViaHandler(View view) {
 		logger.debug("run query via async query handler");
 		final AsyncQueryHandler handler = new AsyncQueryHandler(
 				this.getContentResolver()) {
 			@Override
 			public void onQueryComplete(int token, Object cookie,
 					final Cursor cursor) {
 				final DownloadActivity master = (DownloadActivity) cookie;
 				master.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						master.adapter.swapCursor(cursor);
 					}
 				});
 			}
 		};
 		handler.startQuery(1, this, ImageTable.CONTENT_URI, null,
 				Selection.BY_URI.code, new String[] { this.getActiveUri() },
 				Order.BY_ID.ascending());
 	}
 
 }
