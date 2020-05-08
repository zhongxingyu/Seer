 package th.in.llun.thorfun.api;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import th.in.llun.thorfun.api.model.CategoryStory;
 import th.in.llun.thorfun.api.model.Comment;
 import th.in.llun.thorfun.api.model.Neighbour;
 import th.in.llun.thorfun.api.model.Post;
 import th.in.llun.thorfun.api.model.RemoteCollection;
 import th.in.llun.thorfun.api.model.Story;
 import th.in.llun.thorfun.api.model.ThorfunResult;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.util.Base64;
 import android.util.Log;
 
 public class Thorfun {
 
 	public static final String LOG_TAG = "Thorfun";
 
 	public static final int DEFAULT_PAGE_LIMIT = 15;
 
 	public static final String CONFIG_NAME = "thorfun.network";
 	public static final String CONFIG_KEY_COOKIES = "cookies";
 
 	static final String METHOD_DELETE = "delete";
 	static final String METHOD_POST = "post";
 	static final String METHOD_PUT = "put";
 	static final String METHOD_GET = "get";
 
 	private static Thorfun sInstance;
 
 	private ExecutorService mExecutor;
 	private DefaultHttpClient mClient;
 	private HttpContext mClientContext;
 	private BasicCookieStore mCookieStore;
 
 	private boolean mIsLoggedIn = false;
 
 	private Context mContext;
 
 	public static Thorfun getInstance(Context context) {
 		if (sInstance == null) {
 			sInstance = new Thorfun(context);
 		}
 		return sInstance;
 	}
 
 	public Thorfun(Context context) {
 		HttpParams params = new BasicHttpParams();
 		ConnManagerParams.setMaxTotalConnections(params, 100);
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 
 		// Create and initialize scheme registry
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory
 		    .getSocketFactory(), 80));
 		schemeRegistry.register(new Scheme("https", SSLSocketFactory
 		    .getSocketFactory(), 443));
 
 		// Create an HttpClient with the ThreadSafeClientConnManager.
 		// This connection manager must be used if more than one thread will
 		// be using the HttpClient.
 		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
 		    schemeRegistry);
 		mClient = new DefaultHttpClient(cm, params);
 
 		mContext = context;
 		mExecutor = Executors.newSingleThreadExecutor();
 
 		mClientContext = new BasicHttpContext();
 
 		SharedPreferences preference = mContext.getSharedPreferences(CONFIG_NAME,
 		    Context.MODE_PRIVATE);
 		String cookies = preference.getString(CONFIG_KEY_COOKIES, null);
 		if (cookies != null) {
 			try {
 				mCookieStore = restoreCookieStore(cookies);
 				mIsLoggedIn = true;
 			} catch (Exception e) {
 				mCookieStore = new BasicCookieStore();
 			}
 		} else {
 			mCookieStore = new BasicCookieStore();
 		}
 		mClient.setCookieStore(mCookieStore);
 	}
 
 	public boolean isLoggedIn() {
 		return mIsLoggedIn;
 	}
 
 	public void login(String username, String password,
 	    final ApiResponse<String> result) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("username", username);
 		map.put("password", password);
 
 		invoke("http://thorfun.com/ajax/login", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(final String token) {
			    	if (!token.equals("false")) {
			    		String cookies = saveCookieStore(mCookieStore);
					    SharedPreferences preference = mContext.getSharedPreferences(
					        CONFIG_NAME, Context.MODE_PRIVATE);
					    Editor editor = preference.edit();
					    editor.putString(CONFIG_KEY_COOKIES, cookies);
					    editor.commit();

					    mIsLoggedIn = true;
			    	}
 				    result.onResponse(token);
 			    }
 
 		    });
 	}
 
 	public void logout(final ApiResponse<String> result) {
 
 		invoke("http://thorfun.com/ajax/login", METHOD_DELETE,
 		    new HashMap<String, String>(0), new BaseRemoteResult() {
 
 			    public void onResponse(String response) {
 				    mIsLoggedIn = false;
 				    SharedPreferences preference = mContext.getSharedPreferences(
 				        CONFIG_NAME, Context.MODE_PRIVATE);
 				    Editor editor = preference.edit();
 				    editor.remove(CONFIG_KEY_COOKIES);
 				    editor.commit();
 
 				    result.onResponse(response);
 			    }
 
 		    });
 
 	}
 
 	public void getSelfNeighbour(final ThorfunResult<Neighbour> result) {
 		jsonInvoke("http://thorfun.com/ajax/neighbour", METHOD_GET,
 		    new HashMap<String, String>(0), new BaseRemoteResult() {
 
 			    public void onResponse(JSONObject response) {
 				    result.onResponse(new Neighbour(response));
 			    };
 
 		    });
 	}
 
 	public void getStory(String id, final ThorfunResult<Story> result) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("id", id);
 
 		jsonInvoke("http://thorfun.com/ajax/story", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(JSONObject response) {
 				    result.onResponse(new Story(response));
 			    }
 
 		    });
 	}
 
 	public void loadStory(CategoryStory lastStory, String sort,
 	    final ThorfunResult<RemoteCollection<CategoryStory>> result) {
 
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("category_id", CategoryStory.CATEGORY_ALL);
 		map.put("following", "false");
 		map.put("popular", "false");
 		map.put("editor_pick", "false");
 		map.put("sort", sort);
 		map.put("limit", Integer.toString(DEFAULT_PAGE_LIMIT));
 		if (lastStory != null) {
 			map.put("skipId", lastStory.getID());
 			map.put("skipView", String.format("%d", lastStory.getViewNumber()));
 		}
 		jsonInvoke("http://thorfun.com/ajax/home/story", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(JSONArray responses) {
 
 				    ArrayList<CategoryStory> stories = new ArrayList<CategoryStory>(
 				        responses.length());
 				    for (int index = 0; index < responses.length(); index++) {
 					    JSONObject object = responses.optJSONObject(index);
 					    stories.add(new CategoryStory(object));
 				    }
 
 				    result.onResponse(new RemoteCollection<CategoryStory>(stories));
 			    }
 
 		    });
 
 	}
 
 	public void loadMyStory(CategoryStory lastStory, String username,
 	    final ThorfunResult<RemoteCollection<CategoryStory>> result) {
 
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("limit", Integer.toString(DEFAULT_PAGE_LIMIT));
 		map.put("username", username);
 		if (lastStory != null) {
 			map.put("skipId", lastStory.getID());
 			map.put("skip", String.format("%d", lastStory.getViewNumber()));
 		}
 		jsonInvoke("http://thorfun.com/ajax/neighbour/story", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(JSONArray responses) {
 
 				    ArrayList<CategoryStory> stories = new ArrayList<CategoryStory>(
 				        responses.length());
 				    for (int index = 0; index < responses.length(); index++) {
 					    JSONObject object = responses.optJSONObject(index);
 					    stories.add(new CategoryStory(object));
 				    }
 
 				    result.onResponse(new RemoteCollection<CategoryStory>(stories));
 			    }
 
 		    });
 
 	}
 
 	public void loadBoard(final Post lastPost,
 	    final ThorfunResult<RemoteCollection<Post>> result) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("category_id", "all");
 		map.put("following", "false");
 		map.put("sort", "time");
 		map.put("limit", Integer.toString(DEFAULT_PAGE_LIMIT));
 		if (lastPost != null) {
 			map.put("skipId", lastPost.getID());
 			map.put("skipComment", String.format("%d", lastPost.getTotalComment()));
 		}
 
 		jsonInvoke("http://thorfun.com/ajax/home/board", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(JSONArray responses) {
 
 				    ArrayList<Post> posts = new ArrayList<Post>(responses.length());
 				    for (int index = 0; index < responses.length(); index++) {
 					    JSONObject object = responses.optJSONObject(index);
 					    posts.add(new Post(object));
 				    }
 
 				    result.onResponse(new RemoteCollection<Post>(posts));
 
 			    }
 
 		    });
 	}
 
 	public void like(String storyID, final ApiResponse<String> result) {
 		HashMap<String, String> map = new HashMap<String, String>(1);
 		map.put("id", storyID);
 
 		invoke("http://thorfun.com/ajax/story/like", METHOD_POST, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(String response) {
 				    result.onResponse(response);
 			    }
 
 		    });
 
 	}
 
 	public void unlike(String storyID, final ApiResponse<String> result) {
 		HashMap<String, String> map = new HashMap<String, String>(1);
 		map.put("id", storyID);
 
 		invoke("http://thorfun.com/ajax/story/unlike", METHOD_POST, map,
 		    new BaseRemoteResult() {
 
 			    public void onResponse(String response) {
 				    result.onResponse(response);
 			    }
 
 		    });
 	}
 
 	public void loadComments(CategoryStory story, Comment lastComment,
 	    final ThorfunResult<RemoteCollection<Comment>> result) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("limit", Integer.toString(DEFAULT_PAGE_LIMIT));
 		map.put("id", story.getID());
 		if (lastComment != null) {
 			map.put("skipId", Integer.toString(lastComment.getID()));
 		}
 
 		jsonInvoke("http://thorfun.com/ajax/story/comment_before", METHOD_GET, map,
 		    new BaseRemoteResult() {
 
 			    @Override
 			    public void onResponse(JSONArray responses) throws Exception {
 				    ArrayList<Comment> comments = new ArrayList<Comment>(responses
 				        .length());
 				    for (int index = 0; index < responses.length(); index++) {
 					    JSONObject object = responses.optJSONObject(index);
 					    comments.add(new Comment(object));
 				    }
 
 				    result.onResponse(new RemoteCollection<Comment>(comments));
 			    }
 
 		    });
 	}
 
 	public void comment(CategoryStory story, String text,
 	    final ThorfunResult<Comment> result) {
 		HashMap<String, String> map = new HashMap<String, String>(3);
 		map.put("id", story.getID());
 		map.put("text", text);
 		map.put("t", Long.toString(new Date().getTime() * 1000));
 
 		jsonInvoke("http://thorfun.com/ajax/story/comment", METHOD_POST, map,
 		    new BaseRemoteResult() {
 
 			    @Override
 			    public void onResponse(JSONObject response) throws Exception {
 				    result.onResponse(new Comment(response));
 			    }
 		    });
 
 	}
 
 	private void jsonInvoke(final String url, String method,
 	    Map<String, String> parameters, final RemoteResult result) {
 
 		invoke(url, method, parameters, new BaseRemoteResult() {
 
 			@Override
 			public void onResponse(String output) throws Exception {
 				try {
 					JSONObject json = new JSONObject(output);
 					result.onResponse(json);
 				} catch (JSONException je) {
 					JSONArray json = new JSONArray(output);
 					result.onResponse(json);
 				}
 			}
 
 		});
 
 	}
 
 	private void invoke(final String url, String method,
 	    Map<String, String> parameters, final RemoteResult result) {
 
 		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>(
 		    parameters.size());
 		for (String key : parameters.keySet()) {
 			pairs.add(new BasicNameValuePair(key, parameters.get(key)));
 		}
 
 		HttpUriRequest request = null;
 		if (method.equals(METHOD_DELETE) || method.equals(METHOD_GET)
 		    || method == null) {
 			final StringBuilder builder = new StringBuilder(url);
 
 			if (parameters.size() > 0) {
 				if (!url.endsWith("?"))
 					builder.append("?");
 				builder.append(URLEncodedUtils.format(pairs, "UTF-8"));
 			}
 
 			if (method.equals(METHOD_DELETE)) {
 				request = new HttpDelete(builder.toString());
 			} else {
 				request = new HttpGet(builder.toString());
 			}
 
 		} else {
 			HttpEntityEnclosingRequestBase entityEnclosing = null;
 			if (method.equals(METHOD_PUT)) {
 				entityEnclosing = new HttpPut(url);
 			} else {
 				entityEnclosing = new HttpPost(url);
 			}
 
 			try {
 				entityEnclosing.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				Log.e(Thorfun.LOG_TAG, "Can't set entity to request", e);
 			}
 
 			request = entityEnclosing;
 		}
 
 		final HttpUriRequest finalRequest = request;
 
 		mExecutor.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					HttpResponse response = mClient.execute(finalRequest, mClientContext);
 					HttpEntity entity = response.getEntity();
 					if (entity != null) {
 						ByteArrayOutputStream baos = new ByteArrayOutputStream();
 						entity.writeTo(baos);
 						final String output = baos.toString();
 						Log.v(LOG_TAG, output);
 
 						Handler handler = new Handler(mContext.getMainLooper());
 						handler.post(new Runnable() {
 
 							@Override
 							public void run() {
 								try {
 									result.onResponse(output);
 								} catch (Exception e) {
 									Log.e(LOG_TAG, e.getMessage(), e);
 								}
 							}
 						});
 					}
 
 				} catch (Exception e) {
 					Log.e(LOG_TAG, e.getMessage(), e);
 				}
 			}
 		});
 
 	}
 
 	private static BasicCookieStore restoreCookieStore(String string) {
 		BasicCookieStore cookieStore = new BasicCookieStore();
 
 		String[] cookies = string.split("$");
 		for (String cookie : cookies) {
 			String[] fragments = cookie.split("%");
 			cookieStore.addCookie(new BasicClientCookie(fragments[0], fragments[1]));
 		}
 
 		return cookieStore;
 	}
 
 	private static String saveCookieStore(BasicCookieStore cookieStore) {
 		StringBuilder output = new StringBuilder();
 		List<Cookie> cookies = cookieStore.getCookies();
 		for (Cookie cookie : cookies) {
 			String name = Base64.encodeToString(cookie.getName().getBytes(),
 			    Base64.DEFAULT);
 			String value = Base64.encodeToString(cookie.getValue().getBytes(),
 			    Base64.DEFAULT);
 			String cookieString = name + "%" + value;
 			output.append(cookieString + "$");
 		}
 		output.deleteCharAt(output.length() - 1);
 
 		return output.toString();
 	}
 
 }
