 package it.giacomos.android.osmer.pro.widgets.map.report;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.content.Context;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Toast;
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.pro.OsmerActivity;
 import it.giacomos.android.osmer.pro.locationUtils.GeocodeAddressTask;
 import it.giacomos.android.osmer.pro.locationUtils.GeocodeAddressUpdateListener;
 import it.giacomos.android.osmer.pro.locationUtils.LocationInfo;
 import it.giacomos.android.osmer.pro.locationUtils.NearLocationFinder;
 import it.giacomos.android.osmer.pro.preferences.Settings;
 import it.giacomos.android.osmer.pro.service.sharedData.ReportRequestNotification;
 import it.giacomos.android.osmer.pro.service.sharedData.ServiceSharedData;
 import it.giacomos.android.osmer.pro.widgets.map.MapBaloonInfoWindowAdapter;
 import it.giacomos.android.osmer.pro.widgets.map.OMapFragment;
 import it.giacomos.android.osmer.pro.widgets.map.OOverlayInterface;
 import it.giacomos.android.osmer.pro.widgets.map.OverlayType;
 import it.giacomos.android.osmer.pro.widgets.map.ReportRequestListener;
 import it.giacomos.android.osmer.pro.widgets.map.report.network.PostType;
 import it.giacomos.android.osmer.pro.widgets.map.report.network.ReportUpdater;
 import it.giacomos.android.osmer.pro.widgets.map.report.network.ReportUpdaterListener;
 
 public class ReportOverlay implements OOverlayInterface, 
 ReportOverlayTaskListener, OnMarkerClickListener,
 OnMapClickListener, OnMapLongClickListener, OnInfoWindowClickListener, 
 OnMarkerDragListener, GeocodeAddressUpdateListener, ReportUpdaterListener,
 OnTiltChangeListener,
 OnClickListener
 {
 	/* 
 	 * 0 <= tilt < TILT_MARKERS_SHOW_ALL_THRESH => only request/reports are shown
 	 * TILT_MARKERS_SHOW_ALL_THRESH  <= tilt < TILT_MARKERS_SHOW_ONLY_USERS_THRESH =>
 	 *   all markers are shown together
 	 * tilt >= TILT_MARKERS_SHOW_ONLY_USERS_THRESH => only active users markers are shown.
 	 */
 	private final int TILT_MARKERS_SHOW_ALL_THRESH = 10;
 	private final int TILT_MARKERS_SHOW_ONLY_USERS_THRESH = 15;
 
 	private OMapFragment mMapFrag;
 	private ReportOverlayTask mReportOverlayTask;
 	private MapBaloonInfoWindowAdapter mMapBaloonInfoWindowAdapter;
 	private ReportRequestListener mMyReportRequestListener; /* OsmerActivity */
 	private GeocodeAddressTask mGeocodeAddressTask;
 	private ReportUpdater mReportUpdater;
 	private float mMapTilt;
 
 	/* maps the marker id (obtained with marker.getId()) with the associated marker data */
 	private HashMap<String , DataInterface> mDataInterfaceMarkerIdHash;
 	private ArrayList<DataInterface> mDataInterfaceList;
 
 	public ReportOverlay(OMapFragment oMapFragment) 
 	{
 		mMapFrag = oMapFragment;
 		mMyReportRequestListener = null; /* OsmerActivity */
 		mReportOverlayTask = null;
 		mMapBaloonInfoWindowAdapter = new MapBaloonInfoWindowAdapter(mMapFrag.getActivity());
 		mGeocodeAddressTask = null;
 		mReportUpdater = new ReportUpdater(oMapFragment.getActivity().getApplicationContext(),  this);
 		mMapFrag.getMap().setInfoWindowAdapter(mMapBaloonInfoWindowAdapter);
 		mDataInterfaceMarkerIdHash = new HashMap<String, DataInterface>();
 		mDataInterfaceList = new ArrayList<DataInterface>();
 		mMapTilt = 0;
 
 		Context ctx = mMapFrag.getActivity();
 		if(!new Settings(ctx).tiltTutorialShown())
 		{
 			LayoutInflater li = mMapFrag.getActivity().getLayoutInflater();
 			View transparentTutorial = li.inflate(R.layout.map_tilt_transparent_help, 
 					(ViewGroup) mMapFrag.getView());
 			((Button) transparentTutorial.findViewById(R.id.reportMapTiltHelpOkButton))
 				.setOnClickListener(this);
 		}
 	}
 
 	@Override
 	public void clear() 
 	{
 		mReportUpdater.clear();
 		mCancelTasks();
 		mRemoveMarkers();
 	}
 
 	/** ReportOverlay has a ReportUpdater which registers with the LocationClient and 
 	 *  with the  NetworkStatusMonitor to obtain location updates and to be notified 
 	 *  when the network goes up/down. When the activity is paused, it is necessary to
 	 *  release such resources.
 	 * 
 	 */
 	public void onPause()
 	{
 		mReportUpdater.onPause();
 		/* we can cancel report overlay tasks and geocode address tasks if paused, because
 		 * the activity, when resumed, updates data.
 		 */
 		mCancelTasks();
 	}
 
 	public void onResume()
 	{
 		mReportUpdater.onResume();
 	}
 
 	private void mRemoveMarkers()
 	{
 		for(String markerId : mDataInterfaceMarkerIdHash.keySet())
 			mDataInterfaceMarkerIdHash.get(markerId).getMarker().remove();
 		mDataInterfaceMarkerIdHash.clear();
 	}
 
 	private void mCancelTasks()
 	{
 		if(mReportOverlayTask != null)
 			mReportOverlayTask.cancel(false);
 		if(mGeocodeAddressTask != null)
 			mGeocodeAddressTask.cancel(true);
 	}
 
 	public void setMapTilt(float ti)
 	{
 		mMapTilt = ti;
 	}
 
 	@Override
 	public int type() 
 	{
 		return OverlayType.REPORT;
 	}
 
 	@Override
 	public void hideInfoWindow() 
 	{
 		for(String id : mDataInterfaceMarkerIdHash.keySet())
 			mDataInterfaceMarkerIdHash.get(id).getMarker().hideInfoWindow();
 	}
 
 	@Override
 	public boolean isInfoWindowVisible()
 	{
 		for(String markerId : mDataInterfaceMarkerIdHash.keySet())
 		{
 			Marker m = mDataInterfaceMarkerIdHash.get(markerId).getMarker();
 			if(m.isInfoWindowShown())
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	/** redraws all markers, the user report markers, my request marker and buddy request notification
 	 *  marker, if present.
 	 */
 	public void onReportOverlayTaskFinished(DataInterface [] dataInterfaceList) 
 	{
 		if(dataInterfaceList != null) /* the task may return null */
 		{
 			/* store new data into mDataInterfaceList field */
 			mDataInterfaceList = new ArrayList<DataInterface>(Arrays.asList(dataInterfaceList));
 			/* save my request markers that haven't been published yet in order not to lose them
 			 * when the update takes place. Actually, mRemoveMarkers below clears all markers.
 			 */
 			ArrayList<DataInterface> myRequestsYetUnpublishedBackup = mSaveYetUnpublishedMyRequestData();
 			mRemoveMarkers();
 			/* creates markers on the map according to the tilt value. Populates mDataInterfaceMarkerIdHash */
 			mUpdateMarkers();
 
 			/* do not need data interface list anymore, since it's been saved into hash */
 			dataInterfaceList = new DataInterface[0];
 			/* restore yet unpublished markers that the user was just placing into the map */
 			mRestoreYetUnpublishedMyRequestData(myRequestsYetUnpublishedBackup);
 		}
 
 		mCheckForFreshNotifications();
 	}
 
 	private void mUpdateMarkers() 
 	{
 		boolean showAll = (mMapTilt >= TILT_MARKERS_SHOW_ALL_THRESH &&
 				mMapTilt < TILT_MARKERS_SHOW_ONLY_USERS_THRESH);
 		boolean showUsers = (mMapTilt >= TILT_MARKERS_SHOW_ONLY_USERS_THRESH);
 
 	//	mDataInterfaceMarkerIdHash.clear();
 
 		/* invoked when map tilt changes. We need to remove or add markers
 		 * appropriately. mDataInterfaceMarkerIdHash is updated with the new id
 		 * of the marker, if added. Each 
 		 * DataInterface stored in its values will have its marker set
 		 * or unset (i.e. set to null) accordingly to the shown/hidden status 
 		 * of the marker.
 		 * 
 		 */
 		for(DataInterface dataI : mDataInterfaceList)
 		{
 //			Log.e("reportOvarlay.mUpdateMarkers", " ty " + dataI.getType() + "showAll " + showAll
 //					+ " showUser " + showUsers + " tilt " + mMapTilt);
 			
 			int typ = dataI.getType();
 			Marker marker = dataI.getMarker();
 			
 			if(showAll || (typ == DataInterface.TYPE_ACTIVE_USER && showUsers) 
 					|| (typ != DataInterface.TYPE_ACTIVE_USER && !showUsers) )
 			{
 				if(marker == null)
 				{
 					/* must generate marker for that data interface */
 					marker = mMapFrag.getMap().addMarker(dataI.getMarkerOptions());
 					/* store it into our data reference */
 					dataI.setMarker(marker);
 //					Log.e("reportOvarlay.mUpdateMarkers", "setting marker " + marker.getTitle() + 
 //							" id " + marker.getId());
 					mDataInterfaceMarkerIdHash.put(marker.getId(), dataI);
 
 				}
 				else
 				{
 //					Log.e("reportOvarlay.mUpdateMarkers", "marker " + marker.getTitle() + 
 //							" should be visible already");
 
 					/* if there was a not null marker in the data interface, then it
 					 * should be visible.
 					 */
 				}
 				/* end: if marker to be shown */
 			} 
 			else
 			{
 				/* marker must be hidden */
 				if(marker != null) /* is visible */
 				{
 					/* remove couple id, DataInterface from hash */
 					mDataInterfaceMarkerIdHash.remove(marker.getId());
 //					Log.e("reportOvarlay.mUpdateMarkers", "removigng marker " + marker.getTitle() + 
 //							" id " + marker.getId());
 					marker.remove();
 					dataI.setMarker(null);
 				}
 			}
 		} /* end for DataInterface dataI : mDataInterfaceMarkerIdHash.values() */
 
 //		Log.e("ReportOverlay.mUpdateMarkers", "from tilt change: hash size " + mDataInterfaceMarkerIdHash.size()
 //				+ " dataSize " + mDataInterfaceList.size());
 
 	}
 
 	private void mCheckForFreshNotifications() 
 	{
 		for(DataInterface di : mDataInterfaceMarkerIdHash.values())
 		{
 			if(di.getType() == DataInterface.TYPE_REQUEST && !di.isWritable())
 			{
 				// ReportRequestNotification(String datet, String user, double lat, double lon, String loc)
 				RequestData rd = (RequestData) di;
				ReportRequestNotification repReqN = new ReportRequestNotification(rd.datetime,
 						rd.username, rd.getLatitude(), rd.getLongitude(), rd.locality);
 				/* put the report request notification into the data shared with the service,
 				 * so that the service does not trigger a notification.
 				 */
 				Context ctx = mMapFrag.getActivity().getApplicationContext();
 				ServiceSharedData ssd = ServiceSharedData.Instance(ctx);
 				if(ssd.canBeConsideredNew(repReqN, ctx))
 				{
 					/* true, sets the Notification request notified */
 					ssd.updateCurrentRequest(repReqN, true);
 					// NOTE
 					// The following call has been removed and its functionality has been
 					// put into the updateCurrentRequest through the boolean parameter.
 					// ssd.setWasNotified(repReqN);
 					/* animate camera to new request */
 					mMapFrag.moveTo(rd.getLatitude(), rd.getLongitude());
 					rd.getMarker().showInfoWindow();
 				}	
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker m) 
 	{
 		Log.e("ReportOverlay.OnmarkerClick", m.getTitle());
 		mMapBaloonInfoWindowAdapter.setTitle(m.getTitle());
 		mMapBaloonInfoWindowAdapter.setText(m.getSnippet());
 		return false;
 	}
 
 	public void update(Context ctx, boolean force) 
 	{
 		mReportUpdater.update(force);
 	}
 
 	@Override
 	public void onReportUpdateError(String error)
 	{
 		((OsmerActivity) mMapFrag.getActivity()).makePendingAlertErrorDialog(error);
 	}
 
 	/** This is invoked when the report data in textual form has completed downloading.
 	 * @param txt the downloaded data in simple text format.
 	 * 
 	 * This method parses the textual data and creates two lists, using DataParser class:
 	 * 1) the published reports (of type ReportData)
 	 * 2) the pending request list (of type RequestData).
 	 * The two lists are merged together and sent to an async task that creates the data
 	 * structures used to afterwards build markers and place them on the map.
 	 * When the async task finishes, onReportOverlayTaskFinished() method is invoked.
 	 */
 	@Override
 	public void onReportUpdateDone(String txt) 
 	{		
 		/* In this first implementation, let the markers be updated even if the text has not changed.
 		 * When the task has been completed, the buddy request notification marker is drawn if pertinent,
 		 * inside onReportOverlayTaskFinished().
 		 */
 
 		/* ok start processing data */
 		DataParser reportDataFactory = new DataParser();
 		DataInterface dataList[] = reportDataFactory.parse(txt);
 
 		mReportOverlayTask = new ReportOverlayTask(mMapFrag.getActivity().getApplicationContext(), this);
 		mReportOverlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataList);
 
 		if(dataList == null || dataList.length == 0)
 			Toast.makeText(mMapFrag.getActivity().getApplicationContext(), R.string.reportNoneAvailable, Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onMapLongClick(LatLng point) 
 	{
 		mRemoveUnpublishedMyRequestMarkers();
 		Marker myRequestMarker = mCreateMyRequestMarker(point);
 		mStartGeocodeAddressTask(myRequestMarker);
 		myRequestMarker.showInfoWindow();
 
 	}
 
 	/* Create my request marker. Embed the marker in a RequestData which is finally
 	 * put into the hash table containing all the markers on the map.
 	 */
 	private Marker mCreateMyRequestMarker(LatLng point)
 	{
 		Context ctx = mMapFrag.getActivity().getApplicationContext();
 		Settings s = new Settings(ctx);
 		String userName = s.getReporterUserName();
 		Date date = Calendar.getInstance().getTime();
 		DateFormat df = DateFormat.getDateInstance();
 		/* RequestData(String d, String user, String local, double la, double lo, String wri, boolean isPublished) */
 		RequestData myRequestData = new RequestData(df.format(date), userName, "-", point.latitude, point.longitude, "w", false);
 		myRequestData.buildMarkerOptions(ctx);
 		Marker myRequestMarker = mMapFrag.getMap().addMarker(myRequestData.getMarkerOptions());
 		myRequestData.setMarker(myRequestMarker);
 		/* prepend "MyRequestMarker" to the id of my request marker */
 		mDataInterfaceMarkerIdHash.put(myRequestMarker.getId(), myRequestData);
 
 		return myRequestMarker;
 	}
 
 	public void setOnReportRequestListener(ReportRequestListener rrl)
 	{
 		mMyReportRequestListener = rrl;  /* OsmerActivity */
 	}
 
 	private void mStartGeocodeAddressTask(Marker marker)
 	{
 	//	Log.e("ReportOverlay.mStartGeocodeAddressTask", "starting geocode address task");
 		mGeocodeAddressTask = 
 				new GeocodeAddressTask(mMapFrag.getActivity().getApplicationContext(),
 						this, marker.getId());
 		LatLng ll = marker.getPosition();
 		Location location = new Location("");
 		location.setLatitude(ll.latitude);
 		location.setLongitude(ll.longitude);
 		mGeocodeAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location);
 	}
 
 	@Override
 	public void onGeocodeAddressUpdate(LocationInfo locationInfo, String id) 
 	{	
 		//Log.e("ReportOverlay.onGeocodeAddressUpdate", "-> " + locationInfo.locality);
 		DataInterface dataI = mDataInterfaceMarkerIdHash.get(id);
 		if(dataI != null) /* may have been removed */
 		{
 			RequestData rd = (RequestData) dataI;
 			rd.setLocality(locationInfo.locality);
 			rd.updateWithLocality(locationInfo.locality, mMapFrag.getActivity().getApplicationContext());
 			rd.getMarker().hideInfoWindow();
 			rd.getMarker().showInfoWindow();
 			mMyReportRequestListener.onMyReportLocalityChanged(locationInfo.locality);
 		}
 	}
 
 	/** OsmerActivity implements ReportRequestListener.
 	 *  onInfoWindowClick invokes OsmerActivity callbacks in order to perform
 	 *  specific actions such as show dialog fragments which are appropriate for
 	 *  the action represented by the baloon.
 	 */
 	@Override
 	public void onInfoWindowClick(Marker marker) 
 	{
 		DataInterface dataI = mDataInterfaceMarkerIdHash.get(marker.getId());
 		/* retrieve my request marker, if present. If it's mine and not published, trigger the request dialog
 		 * execution.
 		 */
 		if(dataI != null && dataI.getType() == DataInterface.TYPE_REQUEST &&
 				dataI.isWritable() && !dataI.isPublished())
 		{
 			mMyReportRequestListener.onMyReportRequestTriggered(marker.getPosition(), dataI.getLocality());
 			marker.hideInfoWindow();
 		}
 		/* if the marker is mine, be it my request or my report, it is possible to remove it */
 		else if(dataI != null && dataI.isWritable() && dataI.isPublished())
 		{
 			PostType postType = null;
 			if(dataI.getType() == DataInterface.TYPE_REQUEST)
 				postType = PostType.REQUEST_REMOVE;
 			else  if(dataI.getType() == DataInterface.TYPE_REPORT)
 				postType = PostType.REPORT_REMOVE;
 			if(postType != null)
 				mMyReportRequestListener.onMyPostRemove(marker.getPosition(), postType);
 		}
 		/* a request for me? (read only) */
 		else if(dataI != null && !dataI.isWritable() && dataI.isPublished() 
 				&& dataI.getType() == DataInterface.TYPE_REQUEST)
 		{
 			mMyReportRequestListener.onMyReportPublish();
 		}
 		else if(dataI == null) /* must build a new request */
 		{
 
 		}
 	}
 
 	@Override
 	public void onMarkerDrag(Marker arg0) {
 
 	}
 
 	@Override
 	public void onMarkerDragEnd(Marker marker) 
 	{
 		/* must update data into data interface */
 		DataInterface di = mDataInterfaceMarkerIdHash.get(marker.getId());
 		if(di != null && di.getType() == DataInterface.TYPE_REQUEST) /* must be */
 		{
 			RequestData reqD = (RequestData) di;
 			reqD.setLatitude(marker.getPosition().latitude);
 			reqD.setLongitude(marker.getPosition().longitude);
 			mStartGeocodeAddressTask(marker);
 		}
 	}
 
 	@Override
 	public void onMarkerDragStart(Marker arg0) 
 	{
 		if(mGeocodeAddressTask != null)
 		{
 			mGeocodeAddressTask.cancel(false);
 		}
 	}
 
 	public void onPostActionResult(boolean error, String message, PostType postType) 
 	{
 
 	}
 
 	public void removeMyPendingReportRequestMarker(LatLng position) 
 	{
 		for(Iterator<Map.Entry<String, DataInterface>> it = mDataInterfaceMarkerIdHash.entrySet().iterator(); it.hasNext(); ) 
 		{
 			Map.Entry<String, DataInterface> entry = it.next();
 			DataInterface dataI = entry.getValue();
 			/* get, among all DataInterface entries, the requests and our request */
 			if(dataI.getType() == DataInterface.TYPE_REQUEST && dataI.isWritable() 
 					&& position.latitude == dataI.getLatitude() 
 					&& position.longitude == dataI.getLongitude())
 			{
 				//				Log.e("removeMyPendingReportRequestMarker", "Cancel hit on dialog? removing maker "
 				//						+ dataI.getLocality() + dataI.getMarker().getTitle());
 				Marker toRemoveMarker = dataI.getMarker();
 				toRemoveMarker.remove();
 				it.remove(); /* remove from hash */
 			}
 		}
 
 	}
 
 	private void mRestoreYetUnpublishedMyRequestData(ArrayList<DataInterface> backupData)
 	{
 		//		Log.e("mRestoreYetUnpublishedMyRequestData", "bk data sixe " + backupData.size() + " data if hash " 
 		//				+ mDataInterfaceMarkerIdHash.size());
 		for(DataInterface di : backupData)
 		{
 			/* must rebuild marker options because locality may have not been set in the memorized marker options.
 			 * onGeocodeAddressTask actually updates the locality by changing the snippet on the marker rather 
 			 * than regenerating a MarkerOptions.
 			 */
 			MarkerOptions mo = ((RequestData) di).buildMarkerOptions(mMapFrag.getActivity().getApplicationContext());
 			Marker myRestoredRequestMarker = mMapFrag.getMap().addMarker(mo);
 			di.setMarker(myRestoredRequestMarker); /* replace old marker with new one */
 			myRestoredRequestMarker.showInfoWindow();
 			/* restore data in the hash now! */
 			mDataInterfaceMarkerIdHash.put(myRestoredRequestMarker.getId(), di);
 
 
 		}
 	}
 
 	private ArrayList<DataInterface> mSaveYetUnpublishedMyRequestData() 
 	{
 		ArrayList<DataInterface> myRequestsYetUnpublished = new ArrayList<DataInterface>();
 		for(DataInterface di : mDataInterfaceMarkerIdHash.values())
 		{
 			if(di.getType() == DataInterface.TYPE_REQUEST && di.isWritable() && !di.isPublished())
 			{
 				myRequestsYetUnpublished.add(di);
 			}
 			//			else
 			//				Log.e("mSaveYetUnpublishedMyRequestData", "not saving " + di.getLocality() +  " id " + di.getMarker().getId() +
 			//						"w " + di.isWritable()  + ", pub"  + di.isPublished());
 		}
 		return myRequestsYetUnpublished;
 	}
 
 	@Override
 	public void onMapClick(LatLng arg0) 
 	{
 		if(this.isInfoWindowVisible())
 			this.hideInfoWindow();
 		else
 			mRemoveUnpublishedMyRequestMarkers();
 
 	}
 
 	private void mRemoveUnpublishedMyRequestMarkers() 
 	{
 		for(Iterator<Map.Entry<String, DataInterface>> it = mDataInterfaceMarkerIdHash.entrySet().iterator(); it.hasNext(); ) 
 		{
 			Map.Entry<String, DataInterface> entry = it.next();
 
 			DataInterface di = entry.getValue();
 			if(di.getType() == DataInterface.TYPE_REQUEST && !di.isPublished() && di.isWritable())
 			{
 				di.getMarker().remove();
 				it.remove();
 			}
 		}
 	}
 
 	private DataInterface mFindNearestReportOrRequest(double lat, 
 			double lon, float thresholdDistance)
 	{
 		float distMt = 1000000;
 		float minDist = distMt;
 		LatLng activeUserLatLng = new LatLng(lat, lon);
 		LatLng repReqLatLng = null;
 		NearLocationFinder nearLocFinder = new NearLocationFinder();
 		DataInterface di = null;
 		ArrayList<DataInterface> repreqList = mGetReportsAndRequestsList();
 		for(DataInterface din : repreqList)
 		{
 			repReqLatLng = new LatLng(din.getLatitude(), din.getLongitude());
 			distMt = nearLocFinder.distanceBetween(activeUserLatLng, repReqLatLng);
 			if(distMt < thresholdDistance)
 			{
 				if(distMt < minDist)
 				{
 					di = din;
 					minDist = distMt;
 				}
 			}
 		}
 		return di;
 	}
 
 	private ArrayList<DataInterface> mGetReportsAndRequestsList()
 	{
 		ArrayList<DataInterface> ret = new ArrayList<DataInterface>();
 		for(DataInterface di : mDataInterfaceMarkerIdHash.values())
 		{
 			if(di.getType() == DataInterface.TYPE_REQUEST || di.getType() == 
 					DataInterface.TYPE_REPORT)
 			{
 				ret.add(di);
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public void onTiltChanged(float tilt) 
 	{
 		/* check if changed, first of all! */
 		boolean update = mMarkersNeedUpdate(mMapTilt, tilt);
 		mMapTilt = tilt;
 		if(update)
 		{
 			/* by passing null to mUpdateMarkers, the mDataInterfaceMarkerIdHash is not
 			 * touched but the active user/report/request markers are hidden or
 			 * shown according to the tilt value.
 			 */
 			mUpdateMarkers();
 		}
 	}
 
 	private boolean mMarkersNeedUpdate(float oldTilt, float newTilt)
 	{
 		int oldStatus = 0, newStatus = 0;
 
 		if(oldTilt < TILT_MARKERS_SHOW_ALL_THRESH)
 			oldStatus = 0x01;
 		if(oldTilt >= TILT_MARKERS_SHOW_ALL_THRESH && oldTilt < TILT_MARKERS_SHOW_ONLY_USERS_THRESH)
 			oldStatus |= 0x02;
 		if(oldTilt >= TILT_MARKERS_SHOW_ONLY_USERS_THRESH)
 			oldStatus |= 0x04;
 
 		if(newTilt < TILT_MARKERS_SHOW_ALL_THRESH)
 			newStatus = 0x01;
 		if(newTilt >= TILT_MARKERS_SHOW_ALL_THRESH && oldTilt < TILT_MARKERS_SHOW_ONLY_USERS_THRESH)
 			newStatus |= 0x02;
 		if(newTilt >= TILT_MARKERS_SHOW_ONLY_USERS_THRESH)
 			newStatus |= 0x04;
 
 		return oldStatus != newStatus;
 	}
 	
 	@Override
 	public void onClick(View b) 
 	{
 		if(b.getId() == R.id.reportMapTiltHelpOkButton)
 		{
 			Log.e("ReportOverlay", "removing view");
 			ViewGroup mainV = (ViewGroup) mMapFrag.getView();
 			View transparentTutorial = mainV.findViewById(R.id.tiltTutorialLayout);
 			mainV.removeView(transparentTutorial);
 			new Settings(mMapFrag.getActivity()).setTiltTutorialShown(true);
 		}
 		
 	}
 
 }
