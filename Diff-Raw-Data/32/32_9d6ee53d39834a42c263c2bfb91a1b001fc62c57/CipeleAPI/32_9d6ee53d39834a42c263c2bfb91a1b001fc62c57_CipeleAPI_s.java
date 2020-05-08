 package eu.fiveminutes.cipele46.api;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Base64;
 import android.util.Log;
 
 import com.android.volley.Request.Method;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonArrayRequest;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.Volley;
 
 import eu.fiveminutes.cipele46.model.Ad;
 import eu.fiveminutes.cipele46.model.AdStatus;
 import eu.fiveminutes.cipele46.model.AdType;
 import eu.fiveminutes.cipele46.model.Category;
 import eu.fiveminutes.cipele46.model.City;
 import eu.fiveminutes.cipele46.model.District;
 import eu.fiveminutes.cipele46.model.User;
 
 public class CipeleAPI {
 
 	private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
 
 	private static final String HTTP_HEADER_ACCEPT = "Accept";
 	private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
 	private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
 
 	// Keys - alphabetical ordering
 	private static final String AD_TYPE = "ad_type";
 	private static final String CATEGORY_ID = "category_id";
 	private static final String CITY_ID = "city_id";
 	private static final String CITIES = "cities";
 	private static final String DESCRIPTION = "description";
 	private static final String DISTRICT_ID = "district_id";
 	private static final String EMAIL = "email";
 	private static final String FAVORITES = "favorites";
 	private static final String FIRST_NAME = "first_name";
 	private static final String ID = "id";
 	private static final String IMAGE = "image";
 	private static final String LAST_NAME = "last_name";
 	private static final String NAME = "name";
 	private static final String PAGE = "page";
 	private static final String PASSWORD = "password";
 	private static final String PASSWORD_CONFIRMATION = "password_confirmation";
 	private static final String PER_PAGE = "per_page";
 	private static final String PHONE = "phone";
 	private static final String QUERY = "query";
 	private static final String REGION_ID = "region_id";
 	private static final String STATUS = "status";
 	private static final String TITLE = "title";
 	private static final String USER = "user";
 	private static final String AD_ID = "ad_id";
 	private static final String AUTHOR_EMAIL = "author_email";
 	private static final String CONTENT = "content";
 
 	// Paths
 
 	// private static final String CATEGORIES = "categories.json";
 	// private static final String REGIONS = "regions.json";
 	// private static final String ADS = "ads.json";
 	// private static final String USERS = "users.json";
 	// private static final String CURRENT_USER = "user/current.json";
 	// private static final String EDIT_AD = "ads/%s";
 	// private static final String TOGGLE_FAVORITES = "favorites/toggle/%s";
 
 	private static final String CATEGORIES = "categories";
 	private static final String REGIONS = "regions";
 	private static final String ADS = "ads";
 	private static final String USERS = "users";
 	private static final String CURRENT_USER = "users/current";
 	private static final String EDIT_AD = "ads/%s";
 	private static final String TOGGLE_FAVORITES = "ads/%s/toggle_favorite ";
 	private static final String AD_ENQUIRY = "ads/%s/reply ";
 
 	public enum UserAdSection {
 		ACTIVE_ADS, FAVORITE_ADS, CLOSED_ADS
 	}
 
 	private static CipeleAPI cipele;
 
 	private RequestQueue reqQueue;
 	private String baseURLString;
 
 	// Cached categories. Live during the app session.
 	private List<Category> categories;
 
 	private String TAG = this.getClass().getSimpleName();
 
 	private List<District> cachedListOfDistricts;
 
 	public void init(Context c) {
 		reqQueue = Volley.newRequestQueue(c);
 	}
 
 	public static CipeleAPI get() {
 		if (cipele == null) {
 			cipele = new CipeleAPI();
 		}
 		return cipele;
 	}
 
 	public CipeleAPI() {
 		baseURLString = "http://staging.cipele46.org/api/";
 	}
 
 	public void registerUser(String firstName, String lastName, String email, String phone, String password, final UserRegistrationListener url) {
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				String s = new String(error.networkResponse.data);
 				Log.e(TAG, "Registration failed - " + s);
				url.onFailure(error);
 			}
 		};
 
 		Listener<JSONObject> userListener = new Listener<JSONObject>() {
 
 			@Override
 			public void onResponse(JSONObject response) {
 				Log.d(TAG, "Registration succeded" + response);
 
 				url.onSuccess();
 
 			}
 		};
 
 		JSONObject userObj = new JSONObject();
 		JSONObject requestObj = new JSONObject();
 
 		try {
 			userObj.put(FIRST_NAME, firstName);
 			userObj.put(LAST_NAME, lastName);
 			userObj.put(EMAIL, email);
 			userObj.put(PHONE, phone);
 			userObj.put(PASSWORD, password);
 			userObj.put(PASSWORD_CONFIRMATION, password);
 
 			requestObj.put(USER, userObj);
 
 		} catch (JSONException e) {
 			e.printStackTrace();
			url.onFailure(e);
 			return;
 		}
 
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
 
 		RegistrationRequest jsonReq = new RegistrationRequest(Method.POST, baseURLString + USERS, requestObj, headers, userListener, errorListener);
 
 		reqQueue.add(jsonReq);
 
 	}
 
 	public static String basicAuthHeaderValue(String email, String password) {
 		String x = email + ":" + password;
 		try {
 			return "Basic " + Base64.encodeToString(x.getBytes("UTF-8"), Base64.DEFAULT);
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 
 	public void loginUser(String email, String password, final UserLoginListener url) {
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				if (error.networkResponse != null && error.networkResponse.data != null) {
 					String s = new String(error.networkResponse.data);
 					Log.e(TAG, "Login failed - " + s);
 				}
 				url.onFailure(error);
 			}
 		};
 
 		Listener<User> userListener = new Listener<User>() {
 
 			@Override
 			public void onResponse(User user) {
 				user.setName(user.getFirstName() + " " + user.getLastName());
 				url.onSuccess(user);
 			}
 		};
 
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put(HTTP_HEADER_AUTHORIZATION, basicAuthHeaderValue(email, password));
 
 		reqQueue.add(new GsonRequest<User>(baseURLString + CURRENT_USER, User.class, headers, userListener, errorListener));
 	}
 
 	private Map<String, String> getUserRequestHeaders(User u) {
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put(HTTP_HEADER_AUTHORIZATION, u.getBasicAuth());
 		headers.put(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
 		headers.put(HTTP_HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON);
 		return headers;
 	}
 
 	/**
 	 * 
 	 * @param type
 	 *            Ad type. Required.
 	 * @param categoryID
 	 *            If null, all categories implied.
 	 * @param districtID
 	 *            If null, all districts implied.
 	 * @param adsListener
 	 */
 	public void getAds(final AdType type, final Long categoryID, final Long districtID, final AdsListener adsListener) {
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e(TAG, "", error);
 				adsListener.onFailure(error);
 			}
 		};
 
 		Listener<JSONArray> arrayListener = new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 
 				try {
 					Log.v(TAG, response.toString());
 					List<Ad> adList = parseAdList(response);
 					adsListener.onSuccess(adList);
 				} catch (JSONException e) {
 					Log.e(TAG, "", e);
 					adsListener.onFailure(e);
 				}
 			}
 		};
 
 		StringBuilder sb = new StringBuilder(baseURLString);
 		sb.append("ads?");
 		
 		if (categoryID != -1) {
 			sb.append(CATEGORY_ID);
 			sb.append("=");
 			sb.append(categoryID);
 			sb.append("&");
 		}
 		
 		if (districtID != -1) {
 			sb.append(DISTRICT_ID);
 			sb.append("=");
 			sb.append(districtID);
 			sb.append("&");
 		}
 
 		sb.append(AD_TYPE);
 		sb.append("=");
 		sb.append(type == AdType.SUPPLY ? 1 : 2);
 		
 		Log.v(TAG, sb.toString());
 		
 		reqQueue.add(new JsonArrayRequest(sb.toString(), arrayListener, errorListener));
 	}
 
 	/**
 	 * 
 	 * @param user
 	 *            ad section, required.
 	 * @param adsListener
 	 */
 	public void getUserAds(UserAdSection userAdSection, int pageNumber, int adsPerPage, User activeUser, final AdsListener adsListener) {
 
 		ErrorListener errorListener = new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e(TAG, "", error);
 				adsListener.onFailure(error);
 			}
 		};
 
 		Listener<JSONArray> arrayListener = new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 
 				try {
 					List<Ad> adList = parseAdList(response);
 					adsListener.onSuccess(adList);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					adsListener.onFailure(e);
 				}
 			}
 		};
 
 		StringBuilder sb = new StringBuilder(baseURLString);
 		sb.append("ads?");
 		sb.append(PAGE);
 		sb.append("=");
 		sb.append(pageNumber);
 		sb.append("&");
 		sb.append(PER_PAGE);
 		sb.append("=");
 		sb.append(adsPerPage);
 //		sb.append("&");
 //		sb.append(USER);
 //		sb.append("=1");
 
 		if (userAdSection == UserAdSection.FAVORITE_ADS) {
 			sb.append("&");
 			sb.append(FAVORITES);
 			sb.append("=1");
 		} else if (userAdSection == UserAdSection.ACTIVE_ADS) {
 			sb.append("&");
 			sb.append(STATUS);
 			sb.append("=2");
 		} else if (userAdSection == UserAdSection.CLOSED_ADS) {
 			sb.append("&");
 			sb.append(STATUS);
 			sb.append("=3");
 		}
 
 		UserAdsRequest req = new UserAdsRequest(sb.toString(), getUserRequestHeaders(activeUser), arrayListener, errorListener);
 		//
 		// Log.d(TAG, activeUser.getBasicAuth());
 		// Log.d(TAG, sb.toString());
 		//
 		reqQueue.add(req);
 	}
 
 	private static List<Ad> parseAdList(JSONArray array) throws JSONException {
 		List<Ad> adList = new ArrayList<Ad>();
 
 		JSONObject obj;
 		for (int i = 0; i < array.length(); i++) {
 			obj = array.getJSONObject(i);
 			adList.add(parseAd(obj));
 		}
 
 		return adList;
 	}
 
 	private static Ad parseAd(JSONObject obj) throws JSONException {
 
 		Ad newAd = new Ad();
 		newAd.setId(obj.getLong(ID));
 		newAd.setTitle(obj.getString(TITLE));
 		newAd.setDescription(obj.getString(DESCRIPTION));
 		newAd.setEmail(obj.getString(EMAIL));
 		newAd.setPhone(obj.getString(PHONE));
 		// newAd.setImageURLString(obj.optString("imageUrl"));
 		newAd.setCityID(obj.optLong(CITY_ID));
 		newAd.setCategoryID(obj.optLong(CATEGORY_ID));
 		newAd.setDistrictID(obj.optLong(DISTRICT_ID, -1));
 
 		int statusNumber = obj.getInt(STATUS);
 		AdStatus status = AdStatus.ACTIVE;
 		switch (statusNumber) {
 		case 1:
 			status = AdStatus.PENDING;
 			break;
 		case 2:
 			status = AdStatus.ACTIVE;
 			break;
 		case 3:
 			status = AdStatus.CLOSED;
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid ad status for ad " + newAd.getTitle());
 		}
 		newAd.setStatus(status);
 
 		int typeNumber = obj.getInt(AD_TYPE);
 		AdType type = AdType.SUPPLY;
 		switch (typeNumber) {
 		case 1:
 			type = AdType.SUPPLY;
 			break;
 		case 2:
 			type = AdType.DEMAND;
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid ad type for ad " + newAd.getTitle());
 		}
 		newAd.setType(type);
 
 		return newAd;
 	}
 
 	public String getDistrictNameForID(Long districtID) {
 
 		if (districtID == -1) {
 			return "Sve županije";
 		}
 
 		if (cachedListOfDistricts == null) {
 			return "err0";
 		}
 
 		for (District cat : cachedListOfDistricts) {
 			if (cat.getId().longValue() == districtID.longValue()) {
 				return cat.getName();
 			}
 		}
 
 		return "err1";
 	}
 
 	public String getCategoryNameForID(Long categoryID) {
 
 		if (categoryID == -1) {
 			return "Sve kategorije";
 		}
 
 		if (categories == null) {
 			return "Kategorije nisu učitane";
 		}
 
 		for (Category cat : categories) {
 			if (cat.getId().longValue() == categoryID.longValue()) {
 				return cat.getName();
 			}
 		}
 
 		return "err2";
 	}
 
 	public void getCategories(final CategoriesListener categoriesListener) {
 
 		if (categories != null) {
 			categoriesListener.onSuccess(categories);
 			return;
 		}
 
 		String url = baseURLString + CATEGORIES;
 		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				Log.i(TAG, response.toString());
 
 				List<Category> listofCategory = Collections.<Category> emptyList();
 
 				for (int i = 0; i < response.length(); i++) {
 
 					if (listofCategory.isEmpty()) {
 						listofCategory = new ArrayList<Category>();
 					}
 
 					JSONObject jsonObject;
 					try {
 
 						jsonObject = response.getJSONObject(i);
 						Category category = new Category();
 						category.setId(jsonObject.getLong(ID));
 						category.setName(jsonObject.getString(NAME));
 						listofCategory.add(category);
 
 					} catch (JSONException e) {
 						e.printStackTrace();
 						categoriesListener.onFailure(e);
 					}
 				}
 
 				listofCategory.add(0, new Category(-1L, "Sve kategorije"));
 
 				categories = listofCategory;
 
 				categoriesListener.onSuccess(listofCategory);
 
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.i(TAG, "" + error.getMessage());
 				categoriesListener.onFailure(error);
 
 			}
 		});
 
 		reqQueue.add(jsonArrayRequest);
 
 	}
 
 	public void getDistrictWithCities(final DistrictWithCitiesListener districtWithCitiesListener) {
 
 		if (cachedListOfDistricts != null) {
 			districtWithCitiesListener.onSuccess(cachedListOfDistricts);
 			Log.d(TAG, "Using cachedListOfDistricts");
 			return;
 		}
 
 		String url = baseURLString + REGIONS;
 		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Listener<JSONArray>() {
 
 			@Override
 			public void onResponse(JSONArray response) {
 				Log.i(TAG, response.toString());
 
 				List<District> listOfDistricts = Collections.<District> emptyList();
 
 				try {
 
 					for (int i = 0; i < response.length(); i++) {
 
 						if (listOfDistricts.isEmpty()) {
 							listOfDistricts = new ArrayList<District>();
 						}
 
 						JSONObject jsonObject;
 
 						jsonObject = response.getJSONObject(i);
 						District district = new District();
 						district.setId(jsonObject.getLong(ID));
 						district.setName(jsonObject.getString(NAME));
 
 						JSONArray cities = jsonObject.getJSONArray(CITIES);
 						List<City> listOfCities = Collections.<City> emptyList();
 
 						for (int j = 0; j < cities.length(); j++) {
 
 							if (listOfCities.isEmpty()) {
 								listOfCities = new ArrayList<City>();
 							}
 
 							JSONObject jsonCity = cities.getJSONObject(j);
 							City city = new City();
 							city.setId(jsonCity.getString(ID));
 							city.setName(jsonCity.getString(NAME));
 							// city.setDistrictId(jsonCity.getString(REGION_ID));
 							listOfCities.add(city);
 						}
 
 						district.setCities(listOfCities);
 						listOfDistricts.add(district);
 
 					}
 
 					listOfDistricts.add(0, new District(-1L, "Sve županije"));
 					cachedListOfDistricts = listOfDistricts;
 
 					districtWithCitiesListener.onSuccess(listOfDistricts);
 
 				} catch (JSONException e) {
 					e.printStackTrace();
 					districtWithCitiesListener.onFailure(e);
 				}
 
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.i(TAG, "" + error.getMessage());
 				districtWithCitiesListener.onFailure(error);
 
 			}
 		});
 
 		reqQueue.add(jsonArrayRequest);
 
 	}
 
 	public void sendAdEnquiry(long adId, String emailAddress, String message, final RequestCompletedListener adEnquiryListener) {
 		String url = baseURLString + String.format(AD_ENQUIRY, adId);
 		JSONObject data = new JSONObject();
 		try {
 			// data.put(AD_ID, adId);
 			data.put(EMAIL, emailAddress);
 			data.put(CONTENT, message);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		AdEnquiryRequest req = new AdEnquiryRequest(Method.POST, url, data, new Listener<JSONObject>() {
 
 			@Override
 			public void onResponse(JSONObject response) {
 				adEnquiryListener.onCompleted(200);
 
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				if (error.networkResponse != null && error.networkResponse.data != null) {
 					String s = new String(error.networkResponse.data);
 					Log.e("failed", s);
 				}
 				adEnquiryListener.onCompleted(400);
 			}
 		});
 		reqQueue.add(req);
 	}
 
 	public void createNewAd(Ad ad, String imageEncoded, User user) {
 		String url = baseURLString + ADS;
 		JSONObject data = new JSONObject();
 		
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put(HTTP_HEADER_AUTHORIZATION, user.getBasicAuth());
 		
 		try {
 			JSONObject adObj = new JSONObject();
 			adObj.put(CATEGORY_ID, ad.getCategoryID());
 			adObj.put(CITY_ID, ad.getCityID());
 			adObj.put(TITLE, ad.getTitle());
 			adObj.put(DESCRIPTION, ad.getDescription());
 			adObj.put(EMAIL, ad.getEmail());
 			adObj.put(PHONE, ad.getPhone());
 			adObj.put(IMAGE, imageEncoded);
 			adObj.put(STATUS, 2);
 			adObj.put(AD_TYPE, ad.getType() == AdType.SUPPLY ? 1 : 2);
 			data.put("ad", adObj);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		NewAdRequest req = new NewAdRequest(Method.POST, url, data, new Listener<JSONObject>() {
 
 			@Override
 			public void onResponse(JSONObject response) {
 				Log.e("aa", response.toString());
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				Log.e("aa", "" + error.getMessage());
 			}
 		}, headers);
 		 reqQueue.add(req);
 	}
 
 	public void editUser(User user) {
 
 	}
 	
 	public void toggleFavoriteAd(long adId, User user){
 		String url = baseURLString + String.format(TOGGLE_FAVORITES, adId);
 		
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put(HTTP_HEADER_AUTHORIZATION, user.getBasicAuth());
 
 		
 		ToggleFavoriteAdRequest req = new ToggleFavoriteAdRequest(Method.PUT, url, null, new Listener<JSONObject>() {
 
 			@Override
 			public void onResponse(JSONObject response) {
 				Log.e("aaa", response.toString());
 				
 			}
 		}, new ErrorListener() {
 
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				if (error.networkResponse != null && error.networkResponse.data != null){
 					String s = new String(error.networkResponse.data);
 					Log.e("aaa",s);
 				}
 			}
 		}, headers);
 		reqQueue.add(req);
 	}
 
 }
