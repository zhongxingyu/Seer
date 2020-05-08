 package net.udrunk.model;
 
 import java.io.IOException;
 import java.net.ContentHandler;
 import java.net.URLStreamHandlerFactory;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import net.udrunk.domain.Checkin;
 import net.udrunk.domain.Login;
 import net.udrunk.domain.Place;
 import net.udrunk.domain.Point;
 import net.udrunk.domain.User;
 import net.udrunk.domain.dto.AllPlacesDto;
 import net.udrunk.infra.DataBaseHelper;
 import net.udrunk.infra.JamendoCache;
 import net.udrunk.infra.MyLocation;
 import net.udrunk.infra.MyLocation.LocationResult;
 import net.udrunk.infra.PointSerializer;
 import net.udrunk.infra.resttemplate.UdrunkJsonHttpMessageConverter;
 import net.udrunk.services.CheckinService;
 import net.udrunk.services.UdrunkClient;
 
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpRequest;
 import org.springframework.http.client.ClientHttpRequestExecution;
 import org.springframework.http.client.ClientHttpRequestInterceptor;
 import org.springframework.http.client.ClientHttpResponse;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.HttpServerErrorException;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import android.annotation.SuppressLint;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.imageloader.BitmapContentHandler;
 import com.google.android.imageloader.ImageLoader;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.googlecode.androidannotations.annotations.AfterInject;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EBean;
 import com.googlecode.androidannotations.annotations.RootContext;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 import com.googlecode.androidannotations.api.Scope;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 
 @EBean(scope = Scope.Singleton)
 public class Model extends Observable {
 
 	public static final int LOGIN_SUCCESS = 0;
 	public static final int LOGIN_FAILED = 1;
 	
 	public static final int CHECKINS_UPDATING = 10;
 	public static final int CHECKINS_UPDATED = 11;
 	
 	public static final int PLACES_UPDATING = 20;
 	public static final int PLACES_UPDATED = 21;
 
 	@RootContext
 	protected Context context;
 
 	@RestService
 	protected UdrunkClient restClient;
 
 	protected DataBaseHelper databaseHelper;
 
 	protected List<Place> places;
 
 	protected boolean connected;
 	
 	protected MyLocation myLocation = new MyLocation();
 	
 	public Location currentLocation;
 
 
 	@AfterInject
 	protected void afterInjection() {
 		imageLoader = createImageLoader(context);
 		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
 		
 		GsonBuilder gsonb = new GsonBuilder();
 		gsonb.registerTypeHierarchyAdapter(Point.class, new PointSerializer());
 		Gson gson = gsonb.create();
 		messageConverters.add(new UdrunkJsonHttpMessageConverter(gson));
 		restClient.getRestTemplate().setMessageConverters(messageConverters );
 	}
 
 	public Login getCurrentLogin() {
 		SharedPreferences settings = context.getSharedPreferences("udrunk", 0);
 		String loginStr = settings.getString("login", null);
 		Gson gson = new Gson();
 		Login result = gson.fromJson(loginStr, Login.class);
 		return result;
 	}
 	
 	public User getCurrentUser() {
 		User result = new User();
 		result.setId(getCurrentLogin().getId());
 		return result;
 	}
 
 	public List<Place> getPlaces() {
 		return places;
 	}
 
 	public void setPlaces(List<Place> places) {
 		this.places = places;
 	}
 
 	public List<Checkin> getCheckins() {
 		try {
 			return getDBHelper().getCheckinDao().queryBuilder().orderBy("added", false).query();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	protected DataBaseHelper getDBHelper() {
 		if (databaseHelper == null) {
 			databaseHelper = OpenHelperManager.getHelper(context,
 					DataBaseHelper.class);
 		}
 		return databaseHelper;
 	}
 
 	public boolean checkinsLoading;
 	public boolean placesLoading;
 
 	@AfterInject
 	public void doSomethingAfterInjection() {
 		doBindService();
 	}
 
 	/**
 	 * 
 	 * INIT AUTHENTIFICATION
 	 * 
 	 */
 
 	protected void initAuth() {
 
 		// This should of course be extracted into a separate class
 		ClientHttpRequestInterceptor authInterceptor = new ClientHttpRequestInterceptor() {
 
 			@Override
 			public ClientHttpResponse intercept(HttpRequest request,
 					byte[] body, ClientHttpRequestExecution execution)
 					throws IOException {
 				HttpHeaders headers = request.getHeaders();
 
 				String username = "valentin";
 				String key = "valentin";
 
 				headers.set("Authorization", "ApiKey " + username + ":" + key);
 
 				return execution.execute(request, body);
 			}
 		};
 
 		RestTemplate rt = restClient.getRestTemplate();
 
 		ClientHttpRequestInterceptor[] interceptors = { authInterceptor };
 		rt.setInterceptors(interceptors);
 
 	}
 
 	/**
 	 * 
 	 * LOGIN SERVICE
 	 * 
 	 */
 	
 	@Background
 	public void login(String login, String pass) {
 		try {
 			Log.d("Login", login + " " + pass);
 			Login currentLogin = restClient.login(login, pass).getLogin();
			SharedPreferences settings = context.getSharedPreferences("udrunk", 0);
			Gson gson = new Gson();
		    settings.edit().putString("login", gson.toJson(currentLogin)).commit();
		    onLoginSucess();
 		} catch (RestClientException e) {
 			e.printStackTrace();
 			showErrorToast("Login : " + e.getMessage());
 			onLoginFailed();
 		}
 	}
 	
 	@UiThread
 	protected void onLoginSucess()
 	{
 		initAuth();
 		notifyObservers(LOGIN_SUCCESS);
 	}
 	
 	@UiThread
 	protected void onLoginFailed()
 	{
 		notifyObservers(LOGIN_FAILED);
 	}
 	
 	
 	/**
 	 * 
 	 * PLACE SERVICE
 	 * 
 	 */
 
 	public void retrievePlaces() {
 		if (!placesLoading) {
 			placesLoading = true;
 			notifyObservers(PLACES_UPDATING);
 			
 			myLocation.getLocation(context, locationResult);
 		}
 	}
 	
 	LocationResult locationResult = new LocationResult() {
 		
 		@Override
 		public void gotLocation(Location location) {
 			currentLocation = location;
 			retrievePlacesBackground(location.getLatitude(), location.getLongitude());
 		}
 	};
 
 	@Background
 	protected void retrievePlacesBackground(double lat, double lg) {
 		try {
 			AllPlacesDto placesDto = restClient.getPlaces(lat, lg, getCurrentLogin().getUsername(), getCurrentLogin().getApi_key());
 			setPlaces(placesDto.objects);
 		} catch (RestClientException e) {
 			showErrorToast("Places : " + e.getMessage());
 		} finally {
 			onPlacesRetieved();
 		}
 	}
 
 	@UiThread
 	protected void onPlacesRetieved() {
 		placesLoading = false;
 		notifyObservers(PLACES_UPDATED);
 	}
 
 	/**
 	 * 
 	 * CHECKIN SERVICE
 	 * 
 	 */
 	private Messenger checkinServiceMessenger = null;
 
 	private Messenger inMessenger = new Messenger(new IncomingHandler());
 
 	private boolean checkinUpdateRequested;
 
 	protected boolean mIsBound = false;
 
 	protected void doBindService() {
 		mIsBound = true;
 		context.bindService(new Intent(context, CheckinService.class),
 				mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	protected void doUnbindService() {
 		if (mIsBound) {
 			// If we have received the service, and hence registered with
 			// it, then now is the time to unregister.
 			if (checkinServiceMessenger != null) {
 				try {
 					Message msg = Message.obtain(null,
 							CheckinService.MSG_UNREGISTER_CLIENT);
 					msg.replyTo = inMessenger;
 					checkinServiceMessenger.send(msg);
 				} catch (RemoteException e) {
 					// There is nothing special we need to do if the service
 					// has crashed.
 				}
 			}
 
 			// Detach our existing connection.
 			context.unbindService(mConnection);
 			mIsBound = false;
 			Toast.makeText(context, "Unbinding", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@SuppressLint("HandlerLeak")
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case CheckinService.MSG_GET_CHECKINS:
 				checkinsLoading = false;
 				Toast.makeText(Model.this.context, "Checkin retieved",
 						Toast.LENGTH_SHORT).show();
 				onCheckinsRetieved();
 				break;
 			case CheckinService.MSG_GET_CHECKINS_FAILED:
 				checkinsLoading = false;
 				Toast.makeText(Model.this.context, "Checkin failed",
 						Toast.LENGTH_SHORT).show();
 				onCheckinsRetieved();
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder binder) {
 			checkinServiceMessenger = new Messenger(binder);
 			connected = true;
 
 			try {
 				Message msg = Message.obtain(null,
 						CheckinService.MSG_REGISTER_CLIENT);
 				msg.replyTo = inMessenger;
 				checkinServiceMessenger.send(msg);
 				onCheckinsServiceConnected();
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			checkinServiceMessenger = null;
 			connected = false;
 		}
 	};
 
 	@UiThread
 	public void retrieveCheckins() {
 		if (connected) {
 			if (!checkinsLoading) {
 				checkinsLoading = true;
 				notifyObservers(CHECKINS_UPDATING);
 				
 				Toast.makeText(context, "Retieving Checkins",
 						Toast.LENGTH_SHORT).show();
 				Message msg = Message.obtain(null,
 						CheckinService.MSG_GET_CHECKINS);
 				try {
 					checkinServiceMessenger.send(msg);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		} else {
 			checkinUpdateRequested = true;
 		}
 	}
 
 	protected void onCheckinsRetieved() {
 		notifyObservers(CHECKINS_UPDATED);
 	}
 
 	protected void onCheckinsServiceConnected() {
 		if (checkinUpdateRequested) {
 			checkinUpdateRequested = false;
 			retrieveCheckins();
 		}
 	}
 
 	@Background
 	public void insertCheckin(Checkin checkin) {
 		try {
 			restClient.insertCheckin(checkin, getCurrentLogin().getUsername(), getCurrentLogin().getApi_key());
 		} catch (HttpServerErrorException e) {
 			e.printStackTrace();
 			Log.w("Model", e.getResponseBodyAsString());
 			showErrorToast(e.getMessage());
 		} catch (HttpClientErrorException e) {
 			e.printStackTrace();
 			Log.w("Model", e.getResponseBodyAsString());
 			showErrorToast(e.getMessage());
 		} catch (RestClientException e) {
 			e.printStackTrace();
 			showErrorToast(e.getMessage());
 		} finally {
 			retrieveCheckins();
 		}
 	}
 
 	/*
 	 * IMAGE LOADER
 	 */
 
 	private static final int IMAGE_TASK_LIMIT = 3;
 
 	public ImageLoader imageLoader;
 
 	public static final String API_DOMAIN = "http://udrunk.valentinbourgoin.net";
 
 	private static ImageLoader createImageLoader(Context context) {
 		// Install the file cache (if it is not already installed)
 		JamendoCache.install(context);
 
 		// Just use the default URLStreamHandlerFactory because
 		// it supports all of the required URI schemes (http).
 		URLStreamHandlerFactory streamFactory = null;
 
 		// Load images using a BitmapContentHandler
 		// and cache the image data in the file cache.
 		ContentHandler bitmapHandler = JamendoCache.capture(
 				new BitmapContentHandler(), null);
 
 		// For pre-fetching, use a "sink" content handler so that the
 		// the binary image data is captured by the cache without actually
 		// parsing and loading the image data into memory. After pre-fetching,
 		// the image data can be loaded quickly on-demand from the local cache.
 		ContentHandler prefetchHandler = JamendoCache.capture(
 				JamendoCache.sink(), null);
 
 		// Perform callbacks on the main thread
 		Handler handler = null;
 
 		return new ImageLoader(IMAGE_TASK_LIMIT, streamFactory, bitmapHandler,
 				prefetchHandler, ImageLoader.DEFAULT_CACHE_SIZE, handler);
 	}
 
 	@UiThread
 	public void showErrorToast(String message) {
 		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 	}
 	
 	@Override
 	public void notifyObservers(Object data) {
 		setChanged();
 		super.notifyObservers(data);
 	}
 
 }
