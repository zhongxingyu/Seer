 package jag.kumamoto.apps.StampRally;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import jag.kumamoto.apps.StampRally.Data.StampPin;
 import jag.kumamoto.apps.gotochi.R;
 import aharisu.util.Pair;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.util.FloatMath;
 
 
 /**
  * 
  * スタンプラリーのピンに到着したかを監視するサービス
  * 
  * @author aharisu
  *
  */
 public class ArriveWatcherService extends Service{
 	
 	private static final class LocationDistanceCalculator {
 		
 		private static final class DataStore {
 			public float latSeconds;
 			public float latitude;
 			public float longitude;
 		}
 		
 		private float mLatSeconds;
 		private float mLatOrigin;
 		private float mLongiOrigin;
 		
 		private final DataStore mDataStore = new DataStore();
 		
 		/*
 		public LocationDistanceCalculator(int latitudeE6, int longitudeE6) {
 			this(latitudeE6 * 1e-6f, longitudeE6 * 1e-6f);
 		}
 		*/
 		
 		public LocationDistanceCalculator(float latitude, float longitude) {
 			calclation(latitude, longitude, mDataStore);
 			
 			mLatSeconds = mDataStore.latSeconds;
 			mLatOrigin = mDataStore.latitude;
 			mLongiOrigin = mDataStore.longitude;
 		}
 		
 		private void calclation(float latitude, float longitude, DataStore ret) {
 			int latDegree = (int)latitude;
 			latitude -= latDegree;
 			latitude *= 60;
 			
 			int latMinute = (int)latitude;
 			latitude -= latMinute;
 			latitude *= 60;
 			
 			float latSecond = latitude;
 			
 			int longiDegree = (int)longitude;
 			longitude -= longiDegree;
 			longitude *= 60;
 			
 			int longiMinute = (int)longitude;
 			longitude -= longiMinute;
 			longitude *= 60;
 			
 			float longiSecond = longitude;
 			
 			
 			ret.latSeconds = latDegree * 60 * 60 + latMinute * 60 + latSecond;
 			ret.latitude = ret.latSeconds / 3600 * (float)Math.PI / 180;
 			ret.longitude = (longiDegree * 60 * 60 + longiMinute * 60 + longiSecond) / 3600 * (float)Math.PI / 180;
 		}
 		
 		
 		/**
 		 * 
 		 * @param latitudeE6
 		 * @param longitudeE6
 		 * @return 基準点との距離.単位はm(メーター).
 		 */
 		public float calcDistance(int latitudeE6, int longitudeE6) {
 			calclation(latitudeE6 * 1e-6f, longitudeE6 * 1e-6f, mDataStore);
 			
 			float latO = (mLatSeconds + mDataStore.latSeconds) / 2 / 3600 * (float)Math.PI / 180;
 			
 			float tmp1 = (mLongiOrigin - mDataStore.longitude) * FloatMath.cos(latO);
 			tmp1 = tmp1 * tmp1; //tmp1 ^ 2;
 			float tmp2 = mLatOrigin - mDataStore.latitude;
 			tmp2 = tmp2 * tmp2; //tmp2 ^ 2;
 			
 			return FloatMath.sqrt(tmp1 + tmp2) * 6370 * 1000;
 		}
 		
 	}
 	
 	private static final int LocationUpdateMinTimeShort = 1 * 60 * 1000;//1分より短い間隔では通知されない
 	private static final int LocationUpdateMinDistanceShort = 5; //5m動いていなければ通知しない
 	private static final int LocationUpdateMinTimeNormal = 5 * 60 * 1000;//5分より短い間隔では通知されない
 	private static final int LocationUpdateMinDistanceNormal = 10;//10m動いていなければ通知しない
 	private static final int LocationUpdateMinTimeLong = 10 * 60 * 1000;//10分より短い間隔では通知されない
 	private static final int LocationUpdateMinDistanceLong= 30;//30m動いていなければ通知しない
 	
 	private static final int AllowErrroRange = 200; //200mの誤差ならOK
 	
 	private String mLocationProvier;
 	
 	private ArrayList<Long> mCurArriveLocation = new ArrayList<Long>();
 	
 	
 	private static final int NotificationIdOrigin = 5108;
 	private int mCurId = 1;
 	private HashMap<Long, Integer> mPinIdToNotificationIdMap = new HashMap<Long, Integer>();
 	
 	
 	private final RemoteCallbackList<IApproachPinCallback> mApproachCallbackList
 		= new RemoteCallbackList<IApproachPinCallback>();
 	
 	
 	private final IArriveWatcherService.Stub mStub = new IArriveWatcherService.Stub() {
 		
 		@Override public void showArriveNotification(StampPin pin) throws RemoteException {
 			ArriveWatcherService.this.showArriveNotification(pin);
 		}
 		
 		@Override public void removeArriveNotification(long pinId) throws RemoteException {
 			ArriveWatcherService.this.removeArriveNotification(pinId);
 		}
 
 		@Override public long[] getArrivedStampPins() throws RemoteException {
 			int count = mCurArriveLocation.size();
 			long[] ids = new long[count];
 			
 			for(int i = 0;i < count;++i) {
 				ids[i] = mCurArriveLocation.get(i);
 			}
 			
 			return ids;
 		}
 		
 		@Override public void changeArriveCheckInterval(int type) throws RemoteException {
 			requestLocationUpdates(type);
 		}
 		
 		@Override public void checkArrive() throws RemoteException {
 			if(mLocationProvier != null) {
 				Location location =  ((LocationManager)getSystemService(Context.LOCATION_SERVICE))
 					.getLastKnownLocation(mLocationProvier);
 				if(location != null) {
 					mLocationListener.onLocationChanged(location);
 				}
 			}
 		}
 
 		@Override public void onLocationChanged(Location location) throws RemoteException {
 			if(location != null) {
 				mLocationListener.onLocationChanged(location);
 			}
 		}
 
 		@Override public void registerApproachCallback(IApproachPinCallback callback) throws RemoteException {
 			mApproachCallbackList.register(callback);
 		}
 		
 		@Override public void unregisterApproachCallback(IApproachPinCallback callback) throws RemoteException {
 			mApproachCallbackList.unregister(callback);
 		}
 	};
 	
 	
 	@Override public IBinder onBind(Intent intent) {
 		return mStub;
 	}
 	
 	@Override public void onCreate() {
 		super.onCreate();
 		
 		setupLocationListener();
 		
 		showNotification();
 	}
 	
 	@Override public void onDestroy() {
 		super.onDestroy();
 		
 		stopLocationListener();
 		
 		removeNotification();
 		
 		removeAllArriveNotification();
 	}
 	
 	
 	private final LocationListener mLocationListener = new LocationListener() {
 		
 		final class ApproachData {
 			public final long id;
 			public boolean isTouch;
 			
 			public ApproachData(long id) {
 				this.id = id;
 				isTouch = true;
 			}
 		}
 		private final ArrayList<ApproachData> mInRangeOneKilometerList = new ArrayList<ApproachData>();
 		private final ArrayList<ApproachData> mInRangeHalfKilometerList = new ArrayList<ApproachData>();
 		
 		
 		@Override public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 		
 		@Override public void onProviderEnabled(String provider) {
 		}
 		
 		@Override public void onProviderDisabled(String provider) {
 			setupLocationListener();
 		}
 		
 		@Override public void onLocationChanged(Location location) {
 			
 			LocationDistanceCalculator calc = new LocationDistanceCalculator(
 					(float)location.getLatitude(), (float)location.getLongitude());
 			
 			//許容誤差計算
 			float accuracy = location.getAccuracy();
 			float allowErrorRange = AllowErrroRange + accuracy;
 			if(allowErrorRange > 500) {
 				allowErrorRange = accuracy;
 			}
 			
			StampPin[] pins = StampRallyDB.getStampPins();
 			
 			
 			ArrayList<Long> arriveList = new ArrayList<Long>();
 			for(int i = 0;i < pins.length;++i) {
 				StampPin pin = pins[i];
 				float distance = calc.calcDistance(pin.latitude, pin.longitude);
 				if(distance < allowErrorRange) {
 					arriveList.add((Long)pin.id);
 				}
 				
 				if(distance < 500) {
 					//500m以下
 					if(!checkApproachData(mInRangeHalfKilometerList, pin)) {
 						mInRangeHalfKilometerList.add(new ApproachData(pin.id));
 						doCallback(pin, (int)distance);
 					}
 				} else if(distance < 1000) {
 					//1000m以下
 					
 					//以前が500m以下でない、かつ、1000m以上だった時
 					if(!hasApproachData(mInRangeHalfKilometerList, pin) &&
 							checkApproachData(mInRangeOneKilometerList, pin)) {
 						mInRangeOneKilometerList.add(new ApproachData(pin.id));
 						doCallback(pin, (int)distance);
 					}
 				}
 			}
 			
 			//接近リストをアップデートする（不要なデータを削除する）
 			updateApproachData(mInRangeHalfKilometerList);
 			updateApproachData(mInRangeOneKilometerList);
 			
 			//新しく到着したピンと、離れたピンを抽出する
 			Pair<List<Long>, List<Long>> pair = extractNewAndDelete(mCurArriveLocation, arriveList);
 			
 			//離れたピンのノーティフィケーションを消す
 			//既に消されていても問題はない
 			int deleteSize = pair.v2.size();
 			for(int i = 0;i < deleteSize;++i) {
 				removeArriveNotification(pair.v2.get(i));
 			}
 			
 			int newSizse = pair.v1.size();
 			int index = 0;
 			for(int i = 0;i < newSizse;++i) {
 				//TODO 既にマップが表示されている場合は何か特別なことをする？
 				
 				long id = pair.v1.get(i);
 				for(int count = 0;count < pins.length;index = (index + 1) % pins.length) {
 					if(pins[index].id == id) {
 						//新しく到着したピンのノーティフィケーションを出す
 						showArriveNotification(pins[index]);
 						break;
 					}
 				}
 			}
 			
 			mCurArriveLocation = arriveList;
 		}
 		
 		/**
 		 * 
 		 * @param cur
 		 * @param newer
 		 * @return V1:new. V2:delete
 		 */
 		private Pair<List<Long>, List<Long>> extractNewAndDelete(List<Long> cur, List<Long> newer) {
 			int curSize = cur.size();
 			int newerSize = newer.size();
 			
 			boolean[] curFlag = new boolean[curSize];
 			boolean[] newerFlag = new boolean[newerSize];
 			
 			for(int i = 0;i < curSize;++i) {
 				for(int j = 0;j < newerSize;++j) {
 					if(cur.get(i).equals(newer.get(j))) {
 						curFlag[i] = true;
 						newerFlag[j] = true;
 						break;
 					}
 				}
 			}
 			
 			ArrayList<Long> newAdded = new ArrayList<Long>();
 			for(int i = 0;i < newerSize;++i) {
 				if(!newerFlag[i]) {
 					newAdded.add(newer.get(i));
 				}
 			}
 			
 			ArrayList<Long> delete = new ArrayList<Long>();
 			for(int i = 0;i < curSize;++i) {
 				if(!curFlag[i]) {
 					delete.add(cur.get(i));
 				}
 			}
 			
 			return new Pair<List<Long>, List<Long>>(newAdded, delete);
 		}
 		
 		private boolean hasApproachData(ArrayList<ApproachData> list, StampPin pin) {
 			for(ApproachData data : list) {
 				if(data.id == pin.id) {
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		private boolean checkApproachData(ArrayList<ApproachData> list, StampPin pin) {
 			for(ApproachData data : list) {
 				if(data.id == pin.id) {
 					data.isTouch = true;
 					
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 		private void updateApproachData(ArrayList<ApproachData> list) {
 			for(int i = 0;i < list.size();++i) {
 				ApproachData data = list.get(i);
 				if(!data.isTouch) {
 					list.remove(i);
 					return;
 				} else {
 					data.isTouch = false;
 				}
 			}
 		}
 		
 		private void doCallback(StampPin pin, int distance) {
 			int count = mApproachCallbackList.beginBroadcast();
 			
 			for(int i = 0;i < count;++i) {
 				try {
 					mApproachCallbackList.getBroadcastItem(i).onApproachPin(pin, distance);
 				} catch(RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			mApproachCallbackList.finishBroadcast();
 		}
 		
 	};
 	
 	private void setupLocationListener() {
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setSpeedRequired(false);		//速度情報は不要
 		criteria.setAltitudeRequired(false);	//高度情報は不要
 		criteria.setBearingRequired(false);	//方位情報は不要
 		
 		mLocationProvier = lm.getBestProvider(criteria, true);
 		
 		requestLocationUpdates(StampRallyPreferences.getArrivePollingIntervalType(this));
 	}
 	
 	private void requestLocationUpdates(int type) {
 		if(mLocationProvier == null) {
 			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			criteria.setSpeedRequired(false);		//速度情報は不要
 			criteria.setAltitudeRequired(false);	//高度情報は不要
 			criteria.setBearingRequired(false);	//方位情報は不要
 			
 			mLocationProvier = lm.getBestProvider(criteria, true);
 		} 
 		
 		int minTime;
 		float minDistance;
 		switch(type) {
 		case 0:
 			minTime = LocationUpdateMinTimeShort;
 			minDistance = LocationUpdateMinDistanceShort;
 			break;
 		case 1:
 			minTime = LocationUpdateMinTimeNormal;
 			minDistance = LocationUpdateMinDistanceNormal;
 			break;
 		case 2:
 			minTime = LocationUpdateMinTimeLong;
 			minDistance = LocationUpdateMinDistanceLong;
 			break;
 		default:
 			return;
 		}
 		
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.requestLocationUpdates(
 				mLocationProvier,
 				minTime,
 				minDistance,
 				mLocationListener);
 	}
 	
 	private void stopLocationListener() {
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.removeUpdates(mLocationListener);
 	}
 	
 	private void showNotification() {
 		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = new Notification(
 				R.drawable.icon_status_bar,
 				"スタンプラリースタートだよ",
 				System.currentTimeMillis());
 		
 		notification.setLatestEventInfo(this, "スタンプラリー", null, 
 				PendingIntent.getActivity(this, 0, new Intent(this, MapActivity.class), 
 						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | 
 						Intent.FLAG_ACTIVITY_NEW_TASK));
 		
 		notification.flags = notification.flags |
 			Notification.FLAG_NO_CLEAR |
 			Notification.FLAG_ONGOING_EVENT;
 		
 		nm.notify(NotificationIdOrigin, notification);
 	}
 	
 	private void removeNotification() {
 		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.cancel(NotificationIdOrigin);
 	}
 	
 	private void showArriveNotification(StampPin pin) {
 		int notificationId = NotificationIdOrigin + mCurId;
 		++mCurId;
 		
 		//ピンId->ノーティフィケーションIdマップに登録しておく
 		mPinIdToNotificationIdMap.put(pin.id, notificationId);
 		
 		
 		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = new Notification(
 				R.drawable.icon_status_bar_arrived,
 				pin.name + "に着いたよ",
 				System.currentTimeMillis());
 		
 		//ロケーションインフォのアクティビティを起動するインテント
 		Intent intent = new Intent(this, LocationInfoActivity.class);
 		intent.putExtra(ConstantValue.ExtrasStampPin, pin);
 		intent.putExtra(ConstantValue.ExtrasIsArrive, true);
 		if(pin.type == StampPin.STAMP_TYPE_QUIZ) {
 			intent.putExtra(ConstantValue.ExtrasShowGoQuiz, true);
 		}
 		
 		notification.setLatestEventInfo(this, "スタンプ発見!!", pin.name + "に到着しました",
 				PendingIntent.getActivity(this, 0, intent, 
 						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | 
 						Intent.FLAG_ACTIVITY_NEW_TASK));
 		
 		notification.flags = notification.flags |
 			Notification.FLAG_NO_CLEAR;
 		
 		nm.notify(notificationId, notification);
 	}
 	
 	private void removeArriveNotification(long id) {
 		Long objId = (Long)id;
 		
 		Integer notificationId = mPinIdToNotificationIdMap.get(objId);
 		if(notificationId != null) {
 			mPinIdToNotificationIdMap.remove(objId);
 			
 			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 			
 			nm.cancel(notificationId);
 		}
 	}
 	
 	private void removeAllArriveNotification() {
 		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.cancelAll();
 		mPinIdToNotificationIdMap.clear();
 	}
 
 }
