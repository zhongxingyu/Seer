 package com.framework.leopardus.utils;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Base64;
 import android.util.Log;
 
 import com.framework.leopardus.enums.RESTHeaders;
 import com.framework.leopardus.interfaces.MethodInterface;
 import com.framework.leopardus.interfaces.ModelCallback;
 import com.framework.leopardus.interfaces.RESTCallback;
 import com.framework.leopardus.models.Model;
 import com.framework.leopardus.utils.storage.RESTInternalStorage;
 
 public class RESTSimpleHelper implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1084686258993182978L;
 	private String user;
 	private String host;
 	private String passwd;
 	private HttpClient httpClient;
 	private boolean logged = false;
 	private boolean requirelogin = false;
 	// private HttpContext localContext = new BasicHttpContext();
 	private HttpParams httpParameters;
 	private static Map<String, RESTSimpleHelper> instances = new HashMap<String, RESTSimpleHelper>();
 	private static RESTInternalStorage rest_is;
 
 	public static boolean INFO = false;
 
 	private RESTSimpleHelper(String host) {
 		this(host, null, null);
 	}
 
 	public void enableInternalStorage(Context context) {
 		if (rest_is == null) {
 			rest_is = new RESTInternalStorage(context);
 		}
 	}
 
 	public void store(String url, int status, String content) {
 		if (rest_is != null) {
 			try {
 				if (status >= 200 && status < 300) {
 					int r = rest_is.updateContent(url, content);
 					if (r == -1) {
 						Log.e("Leopardus",
 								"RESTSimpleTool(store) error: When save or update "
 										+ url);
 					}
 				}
 				// TODO: implement delete when status require it
 			} catch (Exception e) {
 				Log.e("Leopardus",
 						"RESTSimpleTool(store) error: " + e.getMessage());
 			}
 		}
 	}
 
 	private RESTSimpleHelper(String host, String user, String passwd) {
 		httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
 		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
 		httpClient = new DefaultHttpClient(httpParameters);
 		this.host = host;
 		requirelogin = user != null && passwd != null;
 		this.user = user;
 		this.passwd = passwd;
 	}
 
 	public static RESTSimpleHelper getInstance(String host) {
 		return getInstance(host, null, null);
 	}
 
 	public static RESTSimpleHelper getInstance(String host, String user,
 			String passwd) {
 		if (!host.endsWith("/")) {
 			host += "/";
 		}
 		if (!host.startsWith("http://") && !host.startsWith("https://")) {
 			host = "http://" + host;
 		}
 		if (!instances.containsKey(host)) {
 			RESTSimpleHelper rst = null;
 			if (user != null && passwd != null) {
 				rst = new RESTSimpleHelper(host, user, passwd);
 			} else {
 				rst = new RESTSimpleHelper(host);
 			}
 			instances.put(host, rst);
 		}
 		RESTSimpleHelper instance = instances.get(host);
 		instance.request(RESTHeaders.GET, "", new RESTCallback(instance) {
 
 			@Override
 			public void onFinish(int status, String section, HttpResponse resp) {
 				// this.getRESTSimpleHelperInstance().requirelogin = false;
 			}
 		});
 		return instance;
 	}
 
 	public String requestFromCache(String url) {
 		if (rest_is != null) {
 			return rest_is.getContent(url);
 		}
 		return null;
 	}
 
 	public void request(RESTHeaders protocol, String url, RESTCallback callback) {
 		request(protocol, url, null, callback);
 	}
 
 	public void request(final RESTHeaders protocol, String apiSection,
 			final Map<String, Object> args, final RESTCallback callback) {
 		try {
 			callback.onStarted();
 		} catch (Exception e) {
 			Log.e("Leopardus", "Error on onStart execution");
 		}
 		if (apiSection.startsWith("/")) {
 			apiSection = apiSection.substring(1);
 		}
 		if (apiSection == null) {
 			apiSection = "";
 		}
 		if (!apiSection.endsWith("/") && !apiSection.equals("")) {
 			apiSection += "/";
 		}
 		if (callback != null) {
 			callback.setRequestUrl(host + apiSection);
 		}
 		final String APISection = apiSection;
 		Runnable r = new Runnable() {
 			public void run() {
 				if (protocol.equals(RESTHeaders.GET)) {
 					onGet(APISection, null, callback);
 				} else if (protocol.equals(RESTHeaders.POST)) {
 					onPost(APISection, args, callback);
 				} else if (protocol.equals(RESTHeaders.PUT)) {
 					onPut(APISection, args, callback);
 				} else if (protocol.equals(RESTHeaders.DELETE)) {
 					onDelete(APISection, null, callback);
 				}
 				try {
 					callback.onFinished();
 				} catch (Exception e) {
 					Log.e("Leopardus", "Error on onStart execution");
 				}
 			}
 		};
 		Thread hilo = new Thread(r);
 		// hilo.run();
 		hilo.start();
 	}
 
 	private void onGet(String apiSection, Map<String, Object> args,
 			RESTCallback callback) {
 		HttpGet get = new HttpGet(host + apiSection);
		get.setHeader("Content-Type", "application/json;charset=ISO-8859-1");
 		if (requirelogin) {
 			get.addHeader("Authorization", getLoginHeader());
 		}
 		try {
 			HttpResponse resp;
 			// resp = httpClient.execute(get, localContext);
 			resp = httpClient.execute(get);
 			if (callback != null) {
 				// TODO:
 				callback.onFinish(resp.getStatusLine().getStatusCode(),
 						apiSection, resp);
 			}
 		} catch (ClientProtocolException e) {
 			if (callback != null) {
 				callback.onClientProtocolException(e);
 			}
 		} catch (IOException e) {
 			if (callback != null) {
 				callback.onIOException(e);
 			}
 		} catch (Exception e) {
 			if (callback != null) {
 				callback.onException(e);
 			}
 		}
 	}
 
 	private void onPost(String apiSection, Map<String, Object> args,
 			RESTCallback callback) {
 		HttpPost post = new HttpPost(host + apiSection);
		post.setHeader("content-type", "application/json;charset=ISO-8859-1");
 		if (requirelogin) {
 			post.addHeader("Authorization", getLoginHeader());
 		}
 		JSONObject dato = new JSONObject();
 		if (args != null) {
 			for (String llave : args.keySet()) {
 				try {
 					dato.put(llave, args.get(llave));
 				} catch (JSONException e) {
 					throw new RuntimeException(
 							"Leopardus(RestSimpleTool): Error adding argument "
 									+ llave + ":"
 									+ String.valueOf(args.get(llave))
 									+ " to request: " + e.getMessage());
 				}
 			}
 		}
 		try {
			StringEntity entity = new StringEntity(dato.toString());
 			post.setEntity(entity);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(
 					"Leopardus(RestSimpleTool): Error with argument coding: "
 							+ e.getMessage());
 		}
 		try {
 			HttpResponse resp;
 			// resp = httpClient.execute(post, localContext);
 			resp = httpClient.execute(post);
 			if (callback != null) {
 				// TODO: Evaluate action of internal storage
 				// store(host+apiSection, resp.getStatusLine().getStatusCode(),
 				// resp);
 				callback.onFinish(resp.getStatusLine().getStatusCode(),
 						apiSection, resp);
 			}
 		} catch (ClientProtocolException e) {
 			if (callback != null) {
 				callback.onClientProtocolException(e);
 			}
 		} catch (IOException e) {
 			if (callback != null) {
 				callback.onIOException(e);
 			}
 		} catch (Exception e) {
 			if (callback != null) {
 				callback.onException(e);
 			}
 		}
 	}
 
 	private void onPut(String apiSection, Map<String, Object> args,
 			RESTCallback callback) {
 		// HttpClient httpClient= new DefaultHttpClient();
 		HttpPut put = new HttpPut(host + apiSection);
		put.setHeader("content-type", "application/json;charset=ISO-8859-1");
 		if (requirelogin) {
 			put.addHeader("Authorization", getLoginHeader());
 		}
 		JSONObject dato = new JSONObject();
 		if (args != null) {
 			for (String llave : args.keySet()) {
 				try {
 					dato.put(llave, args.get(llave));
 				} catch (JSONException e) {
 					throw new RuntimeException(
 							"Leopardus(RestSimpleTool): Error adding argument "
 									+ llave + ":"
 									+ String.valueOf(args.get(llave))
 									+ " to request: " + e.getMessage());
 				}
 			}
 		}
 		try {
 			StringEntity entity = new StringEntity(dato.toString());
 			put.setEntity(entity);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(
 					"Leopardus(RestSimpleTool): Error with argument coding: "
 							+ e.getMessage());
 		}
 		try {
 			HttpResponse resp;
 			// resp = httpClient.execute(put, localContext);
 			resp = httpClient.execute(put);
 			if (callback != null) {
 				// TODO: Evaluate action of internal storage
 				// store(host+apiSection, resp.getStatusLine().getStatusCode(),
 				// resp);
 				callback.onFinish(resp.getStatusLine().getStatusCode(),
 						apiSection, resp);
 			}
 		} catch (ClientProtocolException e) {
 			if (callback != null) {
 				callback.onClientProtocolException(e);
 			}
 		} catch (IOException e) {
 			if (callback != null) {
 				callback.onIOException(e);
 			}
 		} catch (Exception e) {
 			if (callback != null) {
 				callback.onException(e);
 			}
 		}
 	}
 
 	private void onDelete(String apiSection, Map<String, Object> args,
 			RESTCallback callback) {
 		HttpDelete del = new HttpDelete(host + apiSection);
		del.setHeader("Content-Type", "application/json;charset=ISO-8859-1");
 		if (requirelogin) {
 			del.addHeader("Authorization", getLoginHeader());
 		}
 		try {
 			HttpResponse resp;
 			// resp = httpClient.execute(del, localContext);
 			resp = httpClient.execute(del);
 			if (callback != null) {
 				// TODO: Evaluate action of internal storage
 				// store(host+apiSection, resp.getStatusLine().getStatusCode(),
 				// resp);
 				callback.onFinish(resp.getStatusLine().getStatusCode(),
 						apiSection, resp);
 			}
 		} catch (ClientProtocolException e) {
 			if (callback != null) {
 				callback.onClientProtocolException(e);
 			}
 		} catch (IOException e) {
 			if (callback != null) {
 				callback.onIOException(e);
 			}
 		} catch (Exception e) {
 			if (callback != null) {
 				callback.onException(e);
 			}
 		}
 	}
 
 	public boolean isLogged() {
 		return logged;
 	}
 
 	private String getLoginHeader() {
 		return "Basic "
 				+ Base64.encodeToString((user + ":" + passwd).getBytes(),
 						Base64.NO_WRAP);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void getModelFomRequest(String url, final ModelCallback callback) {
 		this.request(RESTHeaders.GET, url, new RESTCallback(this) {
 			@Override
 			public void onStarted() {
 				callback.onStarted();
 			}
 
 			@Override
 			public void onFinished() {
 				callback.onFinished();
 			}
 
 			@Override
 			public void onFinish(int status, String section, HttpResponse resp) {
 				if (status >= 200 && status < 300) {
 					try {
 						RESTSimpleHelper i = getRESTSimpleHelperInstance();
 						String respStr = EntityUtils.toString(resp.getEntity());
 						i.store(getRequestUrl(), status, respStr);
 						responseToModel(respStr);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else if (status >= 400 && status < 500) {
 					try {
 						String respStr = EntityUtils.toString(resp.getEntity());
 						callback.onObjectNotFound(status, respStr);
 					} catch (Exception e) {
 						callback.onObjectNotFound(status,
 								"Error loading object");
 					}
 				}
 			}
 
 			private void responseToModel(String respStr) {
 				ObjectMapper mapper = new ObjectMapper();
 				try {
 					try {
 						HashMap<String, Object> json = (HashMap<String, Object>) mapper
 								.readValue(respStr, HashMap.class);
 						Model m = new Model();
 						// updateInternalReferences(json);
 						m.updateData(json);
 						callback.onModelLoaded(m);
 						callback.onFinish();
 					} catch (org.codehaus.jackson.map.JsonMappingException e) {
 						ArrayList<HashMap<String, Object>> jsonArray = (ArrayList<HashMap<String, Object>>) mapper
 								.readValue(respStr, ArrayList.class);
 						for (HashMap<String, Object> json : jsonArray) {
 							Model m = new Model();
 							// updateInternalReferences(json);
 							m.updateData(json);
 							callback.onModelLoaded(m);
 						}
 						callback.onFinish();
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			@SuppressWarnings("unused")
 			private void updateInternalReferences(final Map<String, Object> json) {
 				for (String _key : json.keySet()) {
 					final String Key = _key;
 					// *
 					if (json.get(Key) instanceof String) {
 						String url = String.valueOf(json.get(Key));
 						if (url.startsWith("http") && url.contains("api/")) {
 							url = url.split("api/")[1];
 							getModelFomRequest(url, new ModelCallback() {
 
 								@Override
 								public void onModelLoaded(Model m) {
 									json.put(Key, m);
 								}
 							});
 						}
 					} else
 					// */
 					if (json.get(Key) instanceof ArrayList) {
 						List<String> slist = (List<String>) json.get(Key);
 						final List<Object> nlist = new ArrayList<Object>();
 						for (String url : slist) {
 							if (url.startsWith("http") && url.contains("api/")) {
 								url = url.split("api/")[1];
 								final ArrayList<Object> arr = new ArrayList<Object>();
 								json.put(Key, arr);
 								getModelFomRequest(url, new ModelCallback() {
 
 									@Override
 									public void onModelLoaded(Model m) {
 										nlist.add(m);
 									}
 								});
 							}
 						}
 						json.put(Key, nlist);
 					}
 
 				}
 			}
 
 			@Override
 			public void onClientProtocolException(ClientProtocolException cpe) {
 				super.onClientProtocolException(cpe);
 				if (rest_is != null) {
 					String respStr = getRESTSimpleHelperInstance()
 							.requestFromCache(getRequestUrl());
 					responseToModel(respStr);
 				}
 			}
 
 			@Override
 			public void onIOException(IOException ioe) {
 				super.onIOException(ioe);
 				if (rest_is != null) {
 					String respStr = getRESTSimpleHelperInstance()
 							.requestFromCache(getRequestUrl());
 					responseToModel(respStr);
 				}
 			}
 
 		});
 	}
 
 	@Override
 	public String toString() {
 		return host;
 	}
 
 }
