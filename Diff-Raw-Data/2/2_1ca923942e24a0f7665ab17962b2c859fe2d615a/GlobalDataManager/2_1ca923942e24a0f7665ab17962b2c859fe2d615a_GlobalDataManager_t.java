 //liuchong@baixing.com
 package com.baixing.data;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.util.Pair;
 import android.widget.Toast;
 
 import com.baidu.mapapi.MKEvent;
 import com.baidu.mapapi.MKGeneralListener;
 import com.baixing.android.api.ApiClient;
 import com.baixing.android.api.ApiParams;
 import com.baixing.entity.Ad;
 import com.baixing.entity.Category;
 import com.baixing.entity.CityDetail;
 import com.baixing.entity.CityList;
 import com.baixing.entity.Filterss;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.message.BxMessageCenter;
 import com.baixing.message.BxMessageCenter.IBxNotification;
 import com.baixing.message.IBxNotificationNames;
 import com.baixing.util.Communication;
 import com.baixing.util.Util;
 
 public class GlobalDataManager implements Observer{
 	public static final String kWBBaixingAppKey = "3747392969";
 	public static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
 	public static WeakReference<Context> context;	
 	
 	public static boolean update = false;
 	private static boolean textMode = false;
 	private static boolean needNotifiySwitchMode = true;
 	private static SharedPreferences preferences = null;
 	private static GlobalDataManager instance = null;
 	private static int lastDestoryInstanceHash = 0;
 	
 	protected static final String PREFS_FILE = "device_id.xml";
     protected static final String PREFS_DEVICE_ID = "device_id";
 
     //
     private String version="";
     private String channelId;
     private AccountManager accountManager;
     private NetworkCacheManager networkCache;
     private LocationManager locationManager;
     
     private Class lastActiveCls;
     
     public final ImageCacheManager getImageManager(){
     	return ImageCacheManager.getInstance();
     }
     
 	public static void setTextMode(boolean tMode){
 		GlobalDataManager.textMode = tMode;
 		GlobalDataManager.needNotifiySwitchMode = false;
 		
 		if(null == preferences){
 			preferences = context.get() != null ? 
 					context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE)
 					: null;
 		} 
 		
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putBoolean("isTextMode", tMode);
 		editor.putBoolean("needNotifyUser", false);
 		editor.commit();
 	}
 	
 	public static boolean isTextMode(){
 		return GlobalDataManager.textMode;
 	}
 	
 	public static boolean needNotifySwitchMode()
 	{
 		return GlobalDataManager.needNotifiySwitchMode;
 	}
 	
 	public void setLastActiveActivity(Class cls) {
 		lastActiveCls = cls;
 	}
 	
 	public Class getLastActiveClass() {
 		return lastActiveCls;
 	}
 	
 	public List<Ad> getListMyStore() {
 		return listMyStore;
 	}
 	
 	public void clearMyStore()
 	{
 		if (this.listMyStore != null)
 		{
 			this.listMyStore.clear();
 		}
 	}
 	
 	public List<Ad> addFav(Ad detail)
 	{
 		if (this.listMyStore == null)
 		{
 			this.listMyStore = new ArrayList<Ad>();
 		}
 		
 		this.listMyStore.add(0, detail);
 		BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_FAV_ADDED, detail);
 		return this.listMyStore;
 	}
 	
 	public boolean isFav(Ad detail) {
 		if(detail == null) return false;
 		List<Ad> myStore = GlobalDataManager.getInstance().getListMyStore();
 		if(myStore == null) return false;
 		for(int i = 0; i < myStore.size(); ++ i){
 			if(myStore.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 					.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public List<Ad> removeFav(Ad detail)
 	{
 		if (this.listMyStore == null || detail == null)
 		{
 			return this.listMyStore;
 		}
 		
 		for (int i = 0; i < listMyStore.size(); i++) {
 			if (detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 					.equals(listMyStore.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))) {
 				listMyStore.remove(i);
 				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_FAV_REMOVE, detail);
 				break;
 			}
 		}
 		
 		return this.listMyStore;
 	}
 	
 	public void updateFav(List<Ad> favs)
 	{
 		this.listMyStore = new ArrayList<Ad>();
 		
 		if (favs != null)
 		{
 			for (int i=0; i<favs.size() && i<=50; i++)
 			{
 				this.listMyStore.add(favs.get(i));
 			}
 		}
 	}
 
 	private void updateFav(Ad[] list) {
 		this.listMyStore = new ArrayList<Ad>();
 
 		if (list != null)
 		{
 			int i=0;
 			for (Ad item : list)
 			{
 				this.listMyStore.add(item);
 				i++;
 				if (i == 50)
 				{
 					break;
 				}
 			}
 		}
 					
 	}
 
 	//我的发布信息
 	public List<Ad> listMyPost = null;
 	
 	public List<Ad> getListMyPost() {
 		return listMyPost;
 	}
 	
 	public boolean isMyAd(String adId) {
 		if(null != listMyPost && null != adId){
 			for(int i = 0; i < listMyPost.size(); ++ i){
 				if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 						.equals(adId)){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void setListMyPost(List<Ad> listMyPost) {
 		this.listMyPost = listMyPost;
 	}
 
 	//我的收藏数据集合
 	private List<Ad> listMyStore = new ArrayList<Ad>();
 	
 
 	//搜索记录
 	private List<String> listRemark = new ArrayList<String>();
 	
 	public List<String> getListRemark() {
 		return listRemark;
 	}
 
 	public void updateRemark(String[] list) {
 		
 		this.listRemark = new ArrayList<String>();
 		
 		if (list != null)
 		{
 			for (String s : list)
 			{
 				this.listRemark.add(s);
 			}
 		}
 	}
 	
 	public void updateRemark(List<String> list) {
 		
 		this.listRemark = new ArrayList<String>();
 		
 		if (list != null)
 		{
 			this.listRemark.addAll(list);
 		}
 	}
 
 	//登录以后的手机号码保存
 	public String mobile = "";
 
 	public String getMobile() {
 		return mobile;
 	}
 
 	public void setMobile(String mobile) {
 		this.mobile = mobile;
 	}
 
 //	public String personMark = "";
 //	
 //	public String getPersonMark() {
 //		return personMark;
 //	}
 //
 //	public void setPersonMark(String personMark) {
 //		this.personMark = personMark;
 //	}
 	
 	public void updateCityList(CityList cityList)
 	{
 		if (cityList == null || cityList.getListDetails() == null
 				|| cityList.getListDetails().size() == 0) {
 		} else {
 			GlobalDataManager.getInstance().setListCityDetails(cityList.getListDetails());
 			
 			//update current city name
 			byte[] cityData = Util.loadData(getApplicationContext(), "cityName");
 			String cityName = cityData == null ? null : new String(cityData); //(String) Util.loadDataFromLocate(getApplicationContext(), "cityName", String.class);
 			if (cityName == null || cityName.equals("")) {
 			} else {
 				List<CityDetail> cityDetails = GlobalDataManager.getInstance().getListCityDetails();
 				boolean exist = false;
 				for(int i = 0;i< cityDetails.size();i++)
 				{
 					if(cityName.equals(cityDetails.get(i).getName()))
 					{
 						String englishCityName = cityDetails.get(i).getEnglishName();
 						GlobalDataManager.getInstance().setCityEnglishName(englishCityName);
 						GlobalDataManager.getInstance().setCityName(cityName);
 						exist = true;
 						break;
 					}
 				}
 				if (!exist) { // FIXME: @zhongjiawu
 					GlobalDataManager.getInstance().setCityEnglishName("shanghai");
 					GlobalDataManager.getInstance().setCityName("上海");
 				}
 			}
 		}
 	}
 	
 	private String phoneNumber = "";
 	
 	public void setPhoneNumber(String number){
 		phoneNumber = number;
 	}
 	
 	public String getPhoneNumber(){
 		return phoneNumber;
 	}
 	
 	private String address = "";
 	
 	public void setAddress(String ad){
 		address = ad;
 	}
 	
 	public String getAddress(){
 		return address;
 	}	
 	
 	//热门城市列表
 	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
 	
 	public List<CityDetail> getListHotCity() {
 		return listHotCity;
 	}
 
 	public void setListHotCity(List<CityDetail> listHotCity) {
 		this.listHotCity = listHotCity;
 	}
 
 	// 定义省份和对应的城市集合
 	public HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();
 
 	public HashMap<String, List<CityDetail>> getShengMap() {
 		return shengMap;
 	}
 
 	public void setShengMap(HashMap<String, List<CityDetail>> shengMap) {
 		this.shengMap = shengMap;
 	}
 
 	// 定义城市列表集合
 	public List<CityDetail> listCityDetails = new ArrayList<CityDetail>();
 
 	public List<CityDetail> getListCityDetails() {
 		return listCityDetails;
 	}
 
 	public void setListCityDetails(List<CityDetail> listCityDetails) {
 		this.listCityDetails = listCityDetails;
 	}
 
 //	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
 	
 	private Category allCategory = new Category();
 	
 	// 筛选木板中的类型集合
 	public List<Filterss> listFilterss = new ArrayList<Filterss>();
 
 	// 城市英文名
 	private String cityEnglishName = "";
 
 	public String getCityEnglishName() {
 		return cityEnglishName;
 	}
 	public void setCityEnglishName(String cityEnglishName) {
 		this.cityEnglishName = cityEnglishName;
 		
 		ApiClient.getInstance().addCommonParam(ApiParams.KEY_CITY, this.cityEnglishName);
 	}
 	
 	public String queryCategoryDisplayName(String englishName){
 		Category cat = allCategory.findCategoryByEnglishName(englishName);
 		
 		return cat == null ? englishName : cat.getName();
 	}
 
 	public List<Category> getFirstLevelCategory() {
 		return allCategory.getChildren();
 	}
 	
 	public String cityName = "";
 	public String getCityName() {
 		return cityName;
 	}
 
 	public void setCityName(String cityName) {
 		this.cityName = cityName;
 	}
 	
 	static public void resetApplication()
 	{
 		if (instance != null)
 		{
 			lastDestoryInstanceHash = instance.hashCode();
 		}
 		GlobalDataManager.instance = null;
 	}
 	
 	static void initStaticFields()
 	{
 		lastDestoryInstanceHash = 0;
 	}
 	
 	static public boolean isAppDestroy(int appHash)
 	{
 		return appHash !=0 && appHash == lastDestoryInstanceHash;
 	}
 
 	static public GlobalDataManager getInstance(){
 		try {
 			if(null == preferences){
 				preferences = context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE);
 				textMode = preferences.getBoolean("isTextMode", false);
 				needNotifiySwitchMode = preferences.getBoolean("needNotifyUser", true);
 			}
 			
 			if(instance == null){
 				instance = new GlobalDataManager();
 			}
 			
 		} catch (Throwable t) {
 			//Igonor.
 		} finally {
 		}
 		
 		
 		return instance;
 	}
 	
 	public LocationManager getLocationManager() {
 		return this.locationManager;
 	}
 	
 	public AccountManager getAccountManager() {
 		return this.accountManager;
 	}
 	
 	public NetworkCacheManager getNetworkCacheManager() {
 		return this.networkCache;
 	}
 	
 	public String getVersion() {
 		return version;
 	}
 	
 	public String getChannelId() {
 		return channelId;
 	}
 	
 	private GlobalDataManager(){
 		this.accountManager = new AccountManager();
 		this.networkCache = NetworkCacheManager.createInstance(context.get());
 		this.locationManager = new LocationManager(context);
 		
 		Context androidContext = context == null ? null : context.get();
 		if(androidContext != null){
 			try{
 				version = Util.getVersion(androidContext);
 				PackageManager packageManager = androidContext.getPackageManager();
 				ApplicationInfo ai = packageManager.getApplicationInfo(androidContext.getPackageName(), PackageManager.GET_META_DATA);
				channelId = String.valueOf(ai.metaData.get("UMENG_CHANNEL"));
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		
 		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGIN);
 		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
 	}
 	
 	public Context getApplicationContext(){
 		return (GlobalDataManager.context == null || GlobalDataManager.context.get() == null) ? 
 				null : GlobalDataManager.context.get();
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		if (data instanceof IBxNotification)
 		{
 			IBxNotification note = (IBxNotification) data;
 			if (IBxNotificationNames.NOTIFICATION_USER_CREATE.equals(note.getName())
 					|| IBxNotificationNames.NOTIFICATION_LOGIN.equals(note.getName())
 					|| IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())) {
 				
 				Context cxt = context.get();
 				if (cxt != null)
 				{
 					accountManager.refreshAndGetMyId(cxt);
 				}
 			}
 		}
 	}
 	
 	public void loadCategorySync(){
 		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getApplicationContext(), 
 				"saveFirstStepCate");
 		
 		if (pair.second == null || pair.second.length() == 0){
 			pair = Util.loadDataAndTimestampFromAssets(this.getApplicationContext(), "cateJson.txt");
 		}
 		
 		String json = pair.second;
 		if (json != null && json.length() > 0) {
 			this.allCategory = JsonUtil.loadCategoryTree(Communication.decodeUnicode(json));
 		}
 	
 	}
 	
 	public void loadCitySync(){
 		CityList cityList = new CityList();
 		// 1. load from locate.
 		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(getApplicationContext(), "cityjson");
 		
 		// 2. load from asset
 		if (pair.second == null || pair.second.length() == 0)
 		{	
 			pair = Util.loadDataAndTimestampFromAssets(getApplicationContext(), "cityjson.txt");
 		}
 		
 		if (pair.second == null || pair.second.length() == 0) {
 			cityList = null;
 		} else {
 			cityList = JsonUtil.parseCityListFromJson((pair.second));
 			GlobalDataManager.getInstance().updateCityList(cityList);
 		}
 	}
 	
 	public void loadPersonalSync(){
 		// 获取搜索记录
 		String[] objRemark = (String[]) Util.loadDataFromLocate(getApplicationContext(), "listRemark", String[].class);
 		GlobalDataManager.getInstance().updateRemark(objRemark);
 
 		Ad[] objStore = (Ad[]) Util.loadDataFromLocate(getApplicationContext(), "listMyStore", Ad[].class);
 		GlobalDataManager.getInstance().updateFav(objStore);
 		
 //		byte[] personalMark = Util.loadData(getApplicationContext(), "personMark");//.loadDataFromLocate(parentActivity, "personMark");
 //		if(personalMark != null){
 //			GlobalDataManager.getInstance().setPersonMark(new String(personalMark));
 //		}
 
 	}
 }
