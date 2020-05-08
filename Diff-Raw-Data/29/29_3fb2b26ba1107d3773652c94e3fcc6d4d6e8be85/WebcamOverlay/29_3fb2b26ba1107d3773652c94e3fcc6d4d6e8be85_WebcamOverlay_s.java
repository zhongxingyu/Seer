 package it.giacomos.android.osmer.pro.widgets.map;
 
 import it.giacomos.android.osmer.pro.OsmerActivity;
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.pro.network.Data.DataPool;
 import it.giacomos.android.osmer.pro.network.Data.DataPoolTextListener;
 import it.giacomos.android.osmer.pro.network.state.BitmapTask;
 import it.giacomos.android.osmer.pro.network.state.BitmapTaskListener;
 import it.giacomos.android.osmer.pro.network.state.BitmapType;
 import it.giacomos.android.osmer.pro.network.state.ViewType;
 import it.giacomos.android.osmer.pro.preferences.Settings;
 import it.giacomos.android.osmer.pro.webcams.OsmerWebcamListDecoder;
 import it.giacomos.android.osmer.pro.webcams.OtherWebcamListDecoder;
 import it.giacomos.android.osmer.pro.webcams.WebcamData;
 import it.giacomos.android.osmer.pro.webcams.WebcamDataHelper;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class WebcamOverlay implements 
 BitmapTaskListener, 
 OOverlayInterface,
 OnMarkerClickListener,
 OnInfoWindowClickListener,
 OnMapClickListener,
 DataPoolTextListener
 {	
 	public WebcamOverlay(int markerResId, 
 			OMapFragment mapFrag, 
 			ArrayList<WebcamData> additionalWebcamData) 
 	{
 		mMarkerResId = markerResId;
 		mMap = mapFrag.getMap();
 		mContext = mapFrag.getActivity().getApplicationContext();
 		mMarkerWebcamHash = new HashMap<Marker, WebcamData>();
 
 		mSettings = new Settings(mapFrag.getActivity().getApplicationContext());
 		mCurrentBitmapTask = null;
 		mCustomMarkerBitmapFactory = new CustomMarkerBitmapFactory(mapFrag.getResources());
 		mCustomMarkerBitmapFactory.setTextWidthScaleFactor(2.5f);
 		mCustomMarkerBitmapFactory.setAlphaTextContainer(100);
 		mCustomMarkerBitmapFactory.setInitialFontSize(mSettings.mapWebcamMarkerFontSize());
 		//		Log.e("WebcamOverlay", "initial font size of marker " + mSettings.mapWebcamMarkerFontSize());
 		mWebcamOverlayChangeListener = mapFrag;
 		mInfoWindowAdapter = new WebcamBaloonInfoWindowAdapter(mapFrag.getActivity());
 		/* adapter and listeners */
 		mMap.setInfoWindowAdapter(mInfoWindowAdapter);
 		mMap.setOnMarkerClickListener(this);
 		mMap.setOnMapClickListener(this);
 		mMap.setOnInfoWindowClickListener(this);
 		mCurrentlySelectedMarker = null;
		mWaitBitmap = BitmapFactory.decodeResource(mapFrag.getResources(), R.drawable.webcam_download);
 		mWaitString = mapFrag.getResources().getString(R.string.webcam_downloading);
 		mWebcamIcon = BitmapFactory.decodeResource(mapFrag.getResources(), mMarkerResId);
 		mAdditionalWebcamData = additionalWebcamData;
 		mIsActive = true;
 	}
 
 	public void disconnectFromDataPool(OsmerActivity oa)
 	{
 		DataPool dataPool = oa.getDataPool();
 		/* unregister from data pool updates */
 		dataPool.unregisterTextListener(ViewType.WEBCAMLIST_OSMER);
 		dataPool.unregisterTextListener(ViewType.WEBCAMLIST_OTHER);
 	}
 
 	/** Registers the webcam overlay as a text listener of DataPool.
 	 * Then, if the data pool does not contain up to date values, the webcam list 
 	 * is obtained by means of the DataPoolCacheUtils. In this case, a Runnable is
 	 * scheduled in order to initialize the overlay with a certain delay, so that
 	 * the interface preserves smoothness while switching to the webcam overlay.
 	 * 
 	 * @param oa reference to the OsmerActivity
 	 */
 	public void connectToDataPool(OsmerActivity oa)
 	{
 		DataPool dataPool = oa.getDataPool();
 		/* initialization */
 		DelayedWebcamOverlayInitializer wodpi = 
 				new DelayedWebcamOverlayInitializer(this, dataPool, mContext);
 		wodpi.start();
 	}
 
 	/** Implements DataPoolTextListener onTextChanged  (for webcam list) */
 	@Override
 	public void onTextChanged(String txt, ViewType t, boolean fromCache) 
 	{		
 		if(mIsActive)
 		{
 			if(!fromCache)
 			{
 				WebcamDataHelper helper = new WebcamDataHelper();
 				helper.setDataUpdatedNow(mContext);
 				helper = null;
 			}
 
 			ArrayList<WebcamData> wcData = null;
 			if (t == ViewType.WEBCAMLIST_OSMER) 
 			{
 				OsmerWebcamListDecoder osmerDec = new OsmerWebcamListDecoder();
 				wcData = osmerDec.decode(txt);
 			} 
 			else if (t == ViewType.WEBCAMLIST_OTHER) 
 			{
 				OtherWebcamListDecoder otherDec = new OtherWebcamListDecoder();
 				wcData = otherDec.decode(txt);
 			}
 
 			/* add to the list the fixed additional webcam data initialized in the 
 			 * class constructor and passed by OMapFragment.
 			 */
 			wcData.addAll(mAdditionalWebcamData);
 
 			scheduleUpdate(wcData); /* place markers on map */
 		}
 	}
 
 	@Override
 	public void onTextError(String error, ViewType t) 
 	{
 
 	}	
 
 	/** executes update after a bunch of milliseconds not to block the 
 	 * user interface while parsing xml file and generating all the markers.
 	 * In this way the user immediately switches to the webcam map mode and 
 	 * has the impression that the markers appear immediately after the mode 
 	 * switch
 	 */
 	public void scheduleUpdate(ArrayList<WebcamData> wcData)
 	{
 		if(mIsActive)
 			new Handler().postDelayed(new WebcamOverlayUpdateRunnable(this, wcData), 450);
 	}
 
 	/**
 	 * Uses the webcam data list previously saved by onTextChanged to create a marker for each
 	 * WebcamData stored in the list. Then adds each marker to the map.
 	 * @param res a reference to the Resources to use in order to create the webcam icons.
 	 */
 	public void update(ArrayList<WebcamData> wcData)
 	{
 		/* update is called by a runnable. The user may have switched to another map mode in the meantime.
 		 * Look for mIsActive to prevent unwanted webcam marker updates inside a map mode .
 		 */
 		if(mIsActive)
 		{
 			for(WebcamData wd : wcData)
 			{
 				if(!webcamInList(wd))
 				{
 					LatLng ll = wd.latLng;
 					if(ll != null)
 					{ 
 						MarkerOptions mo = new MarkerOptions();
 						mo.title(wd.location).snippet(wd.text).position(wd.latLng);
 						BitmapDescriptor bmpDescriptor = mCustomMarkerBitmapFactory.getIcon(mWebcamIcon, wd.location);
 						mo.icon(bmpDescriptor);
 						Marker m = mMap.addMarker(mo);
 						mMarkerWebcamHash.put(m, wd);
 					}
 				}
 			}
 			/* save in settings the optimal font size in order for CustomMarkerBitmapFactory to quickly 
 			 * obtain it without calculating it again.
 			 */
 			mSettings.setMapWebcamMarkerFontSize(mCustomMarkerBitmapFactory.getCachedFontSize());
 		}
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) 
 	{
 		mCurrentlySelectedMarker = marker;
 		WebcamData wd = mMarkerWebcamHash.get(marker);
 		cancelCurrentWebcamTask();
 		mCurrentBitmapTask = new BitmapTask(this, BitmapType.WEBCAM);
 		//		Log.e("onMarkerClick", "getting image for" + wd.location);
 		try 
 		{
 			URL webcamUrl = new URL(wd.url);
 			mCurrentBitmapTask.parallelExecute(webcamUrl);
			mInfoWindowAdapter.setData(wd.location + " - " + mWaitString, mWaitBitmap, false);
 		}
 		catch (MalformedURLException e) 
 		{
 			mWebcamOverlayChangeListener.onWebcamErrorMessageChanged(e.getLocalizedMessage());
 			mCurrentlySelectedMarker = null;
 		}
 		/* do not show info window until the image has been retrieved */
 		return false;
 	}
 
 	@Override
 	public void onBitmapBytesUpdate(byte[] bytes, BitmapType bt) 
 	{
 
 	}
 
 	@Override
 	public void onBitmapUpdate(Bitmap bmp, BitmapType bt, String errorMessage, AsyncTask<URL, Integer, Bitmap> unusedTaskParameter) 
 	{
 		/* no need to check the mIsActive flag because the bitmap task is 
 		 * cancelled by clear().
 		 */
 		if(bmp == null && !errorMessage.isEmpty())
 		{
 			mWebcamOverlayChangeListener.onWebcamErrorMessageChanged(errorMessage);
 		}
 		else if(bt == BitmapType.WEBCAM && bmp != null)
 		{
 			if(!errorMessage.isEmpty())
 				mWebcamOverlayChangeListener.onWebcamErrorMessageChanged(errorMessage);
 			if(mCurrentlySelectedMarker != null)
 			{
 				if(!errorMessage.isEmpty())
 					mInfoWindowAdapter.setData(errorMessage, null, false);
 				else
 				{
 					mInfoWindowAdapter.setData(mMarkerWebcamHash.get(mCurrentlySelectedMarker).text, bmp, true);
 					/* notify map fragment that the image has changed */
 					mWebcamOverlayChangeListener.onWebcamBitmapChanged(bmp);						
 				}
 				mCurrentlySelectedMarker.showInfoWindow();
 			}
 		}
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) 
 	{
 		if(mInfoWindowAdapter.isImageValid())
 			mWebcamOverlayChangeListener.onWebcamInfoWindowImageClicked();
 		else
 			mWebcamOverlayChangeListener.onWebcamMessageChanged(R.string.webcam_wait_for_image);
 	}
 
 	@Override
 	public void onMapClick(LatLng arg0) 
 	{
 		//		Log.e("onMapClick", " cancelling task ");
 		cancelCurrentWebcamTask();
 	}
 
 	/* Attempts to cancel execution of this task. This attempt will fail if the task 
 	 * has already completed, already been cancelled, or could not be cancelled for 
 	 * some other reason. If successful, and this task has not started when cancel 
 	 * is called, this task should never run. If the task has already started, then 
 	 * the mayInterruptIfRunning parameter determines 
 	 * whether the thread executing this task should be interrupted in an attempt to
 	 *  stop the task.
 	 *  Returns
 	 * false if the task could not be cancelled, typically because it has already completed normally; 
 	 * true otherwise
 	 */
 	public void cancelCurrentWebcamTask()
 	{
 		if(mCurrentBitmapTask != null  && mCurrentBitmapTask.getStatus() == AsyncTask.Status.RUNNING)
 		{
 			//			Log.e("cancelCurrentWebcamTask", "cancelling task");
 			mWebcamOverlayChangeListener.onWebcamBitmapTaskCanceled(mCurrentBitmapTask.getUrl());
 			mCurrentBitmapTask.cancel(false);
 		}
 		//		else
 		//			Log.e("cancelCurrentWebcamTask", "NOT cancelling task (not runnig)");
 	}
 
 	@Override
 	public void clear() 
 	{
 		/* important! cancel bitmap task if running */
 		cancelCurrentWebcamTask();
 		for(Map.Entry<Marker, WebcamData> entrySet : mMarkerWebcamHash.entrySet())
 			entrySet.getKey().remove();
 		mMarkerWebcamHash.clear();
		mWaitBitmap.recycle();
 		/* recycle bitmap and unbind drawable */
 		mInfoWindowAdapter.finalize();
 		/* Marks the overlay as finalized, disabling all updates from async tasks */
 		mIsActive = false;
 	}
 
 	protected WebcamData getDataByLatLng(LatLng ll)
 	{
 		for(WebcamData wd : mMarkerWebcamHash.values())
 		{
 			if(wd.latLng == ll)
 				return wd;
 		}
 		return null;
 	}
 
 	boolean webcamInList(WebcamData otherWebcamData)
 	{
 		for(WebcamData wd : mMarkerWebcamHash.values())
 			if(wd.equals(otherWebcamData))
 				return true;
 		return false;
 	}
 
 	@Override
 	public int type() 
 	{
 
 		return 0;
 	}
 
 	@Override
 	public void hideInfoWindow() 
 	{
 		for(Map.Entry<Marker, WebcamData> entrySet : mMarkerWebcamHash.entrySet())
 			if(entrySet.getKey().isInfoWindowShown())
 				entrySet.getKey().hideInfoWindow();
 	}
 
 	@Override
 	public boolean isInfoWindowVisible() 
 	{
 		for(Map.Entry<Marker, WebcamData> entrySet : mMarkerWebcamHash.entrySet())
 			if(entrySet.getKey().isInfoWindowShown())
 				return true;
 		return false;
 	}
 
 
 	private int mMarkerResId;
 	private GoogleMap mMap;
 	private HashMap<Marker, WebcamData> mMarkerWebcamHash;
 	private BitmapTask mCurrentBitmapTask;
 	private CustomMarkerBitmapFactory mCustomMarkerBitmapFactory;
 	private WebcamOverlayChangeListener mWebcamOverlayChangeListener;
 	private WebcamBaloonInfoWindowAdapter mInfoWindowAdapter;
 	private Marker mCurrentlySelectedMarker;
	private Bitmap mWaitBitmap;
 	private String mWaitString;
 	private Settings mSettings;
 	private ArrayList<WebcamData> mAdditionalWebcamData;
 	private Context mContext;
 	private Bitmap mWebcamIcon;
 	private boolean mIsActive;
 }
