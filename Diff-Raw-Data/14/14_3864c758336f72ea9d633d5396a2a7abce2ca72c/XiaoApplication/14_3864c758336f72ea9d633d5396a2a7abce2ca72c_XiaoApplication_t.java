 package com.xiaojiujiu;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.beanu.arad.AradApplication;
import com.beanu.arad.AradApplicationConfig;
 import com.beanu.arad.utils.Log;
 
 public class XiaoApplication extends AradApplication {
 
 	public LocationClient mLocationClient = null;
 	public MyLocationListenner myListener = new MyLocationListenner();
 
 	@Override
 	public void onCreate() {
 
 		mLocationClient = new LocationClient(this);
 		mLocationClient.registerLocationListener(myListener);
 		super.onCreate();
 	}
 
 	/**
 	 * 监听函数，又新位置的时候，格式化成字符串，输出到屏幕中
 	 */
 	public class MyLocationListenner implements BDLocationListener {
 		@Override
 		public void onReceiveLocation(BDLocation location) {
 			if (location == null)
 				return;
			AppHolder.getInstance().location = location;
 			mLocationClient.stop();

 			StringBuffer sb = new StringBuffer(256);
 			sb.append("time : ");
 			sb.append(location.getTime());
 			sb.append("\nerror code : ");
 			sb.append(location.getLocType());
 			sb.append("\nlatitude : ");
 			sb.append(location.getLatitude());
 			sb.append("\nlontitude : ");
 			sb.append(location.getLongitude());
 			sb.append("\nradius : ");
 			sb.append(location.getRadius());
 			if (location.getLocType() == BDLocation.TypeGpsLocation) {
 				sb.append("\nspeed : ");
 				sb.append(location.getSpeed());
 				sb.append("\nsatellite : ");
 				sb.append(location.getSatelliteNumber());
 			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
 				// sb.append("\n省：");
 				// sb.append(location.getProvince());
 				// sb.append("\n市：");
 				// sb.append(location.getCity());
 				// sb.append("\n区/县：");
 				// sb.append(location.getDistrict());
 				sb.append("\naddr : ");
 				sb.append(location.getAddrStr());
 			}
 			sb.append("\nsdk version : ");
 			sb.append(mLocationClient.getVersion());
 			sb.append("\nisCellChangeFlag : ");
 			sb.append(location.isCellChangeFlag());
 			Log.i(sb.toString());
 		}
 
 		public void onReceivePoi(BDLocation poiLocation) {
 			if (poiLocation == null) {
 				return;
 			}
 			mLocationClient.stop();
 			StringBuffer sb = new StringBuffer(256);
 			sb.append("Poi time : ");
 			sb.append(poiLocation.getTime());
 			sb.append("\nerror code : ");
 			sb.append(poiLocation.getLocType());
 			sb.append("\nlatitude : ");
 			sb.append(poiLocation.getLatitude());
 			sb.append("\nlontitude : ");
 			sb.append(poiLocation.getLongitude());
 			sb.append("\nradius : ");
 			sb.append(poiLocation.getRadius());
 			if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
 				sb.append("\naddr : ");
 				sb.append(poiLocation.getAddrStr());
 			}
 			if (poiLocation.hasPoi()) {
 				sb.append("\nPoi:");
 				sb.append(poiLocation.getPoi());
 			} else {
 				sb.append("noPoi information");
 			}
 			Log.i(sb.toString());
 		}
 	}

	@Override
	protected AradApplicationConfig appConfig() {
		AradApplicationConfig config = new AradApplicationConfig();
		return config;
	}
 }
