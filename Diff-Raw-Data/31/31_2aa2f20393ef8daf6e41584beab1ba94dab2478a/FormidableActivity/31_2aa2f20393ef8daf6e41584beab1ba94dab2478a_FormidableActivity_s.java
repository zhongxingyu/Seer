 package com.example.formidable;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.webkit.ConsoleMessage;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebStorage.QuotaUpdater;
 import android.webkit.WebView;
 
 import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
 
 @SuppressLint("SetJavaScriptEnabled")
 public class FormidableActivity extends Activity {
 	
 	static {
 	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
 	}
 
 	//private static final String TAG = "MainActivity";
 	private EventSource eventSource;
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         initialize();       
         RecordRepository repository = eventSource.getRepository();
         
         String recordId = newId();     
         repository.put(createSimpleEvent(3, recordId, "surname:Bhuwalka"));     
         repository.put(createSimpleEvent(2, recordId, "name:Angshu"));
         repository.put(createSimpleEvent(1, recordId, "name:Chris"));
         
         //check search functionality
         repository.put(createSimpleEvent(1, newId(), "name:Vivek", "surname:Singh"));
         		
 		Map<String, Object> record = repository.get(recordId);
 		System.out.println(String.format("Name: %s %s", record.get("name"), record.get("surname")));
         
         WebView view = initView();
         //view.loadUrl(formateUrl("opendatakit.collect2/form-files/default/index.html"));
         //view.loadUrl(formateUrl("webapp/default/index.html#formPath=../IMNCI/&pageRef=1"));
         view.loadUrl(formateUrl("opendatakit.collect2/form-files/default/index.html#formPath=../example/&pageRef=1"));
         search(eventSource, "name:vivek AND surname:singh");
     }
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	private WebView initView() {
 		setContentView(R.layout.activity_main);
         WebView browser = (WebView) findViewById(R.id.webview);
         browser.setWebChromeClient(new WebChromeClient() {
       	  @Override
       	  public void onExceededDatabaseQuota(String url,
 					String databaseIdentifier, long quota,
 					long estimatedDatabaseSize, long totalQuota,
 					QuotaUpdater quotaUpdater) {
       		  quotaUpdater.updateQuota(estimatedDatabaseSize * 2);
       	  }
       	   
 
       	  public boolean onConsoleMessage(ConsoleMessage cm) {
       	    Log.d("MyApplication", cm.message() + " -- From line "
       	                         + cm.lineNumber() + " of "
       	                         + cm.sourceId() );
       	    return true;
       	  }
       	});
         
         WebSettings settings = browser.getSettings();
         settings.setJavaScriptEnabled(true);
         settings.setDatabaseEnabled(true);
         
 //        String databasePath = this.getApplicationContext().getDir("database", MODE_PRIVATE).getPath();
 //        System.out.println("****** Database path = " + databasePath);
         settings.setDatabasePath("/data/data/com.example.formidable/databases");
         
         settings.setDomStorageEnabled(true);
         settings.setGeolocationDatabasePath(Configuration.getGeolocDbPath());
         settings.setGeolocationEnabled(true);
         settings.setAllowFileAccess(true);
         
         browser.addJavascriptInterface(new OdkCollectJSInterface(eventSource.getRepository()), "extRepo");
         //browser.loadUrl("http://"+getLocalIpAddress()+":"+PORT);
         //browser.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
         return browser;
 	}
 
 	private void initialize() {
 		this.eventSource = new EventSource(this);
 	}
 
 	private Event createSimpleEvent(int epoch, String recordId, String... arguments) {
         Map<String, Object> map = new HashMap<String, Object>();
         for (String arg : arguments) {
         	String[] parts = arg.split(":");
         	map.put(parts[0], parts[1]);
         }
 		return new Event(epoch, recordId, map);		
 	}
 
 		
 
 	private String newId() {
 		return UUID.randomUUID().toString();
 	}    
     
     private String formateUrl(String url) {
     	return "file:///android_asset/" + url;
 	}
     
     private boolean validStr(String value) {
 		return (value != null) && (!"".equals(value));
 	}
     
     private void search(EventSource src, String criteria) {
 		src.getSearchAgent().triggerSearch(criteria, new SearchHandler() {
 			@Override
 			void realize(JSONObject[] results) {
 				for (JSONObject result : results) {
 					//we should probably create something Event.parse(JSOBObject) to get a Event object out of json
 					//and return an array of Event Objects. parse method can extract attributes like
 					//epoch, docid, revid, and all the elements in the data element recursively
 					//System.out.println("search result record => %s".format(result.toString()));
 				}
 			}
 			
 			@Override
 			void fault(Object resp) {
 				System.out.println("SearchAgent.triggerSearch => Error : " + resp.toString());
 			}
 		});
 	}
     
 	private String getLocalIpAddress() {
 		try {
 			for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en
 					.hasMoreElements();) {
 				NetworkInterface intf = (NetworkInterface) en.nextElement();
 				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr
 						.hasMoreElements();) {
 					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						return inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 		} catch (SocketException ex) {
 			Log.e("Exception", ex.toString());
 		}
 		return null;
 	}
     
     
     public class OdkCollectJSInterface {
     		private RecordRepository repository;
 
 		public OdkCollectJSInterface(RecordRepository repository) {
 			this.repository = repository;
     		}
 		
 		public void addEvent(String event) {
 			try {
 				JSONObject eventJson = new JSONObject(event);
 				
				String formId = eventJson.optString("formId");
 				String currentInstanceId = eventJson.optString("currentInstanceId");
 				boolean isComplete = eventJson.optBoolean("asComplete");
 				
 				System.out.println(String.format("Collect form submission of formId: %s, instanceId: %s, final:%b", formId, currentInstanceId, isComplete));
 				
 				JSONObject modelJson = eventJson.optJSONObject("model");
 				JSONObject dataJson = eventJson.optJSONObject("data");
 				JSONObject metadataJson = eventJson.optJSONObject("metadata");
				JSONArray modelNames = modelJson.names();
 				
 				//merge the model and data 
				for (int i=0; i<modelNames.length(); i++) {
					String fieldName = (String) modelNames.get(i);
					JSONObject modelField = modelJson.getJSONObject(fieldName);
 					
					JSONObject dataField = dataJson.optJSONObject(fieldName);
					if (dataField != null) {
 						//we can get actual typed values if required
						String fieldValue = dataField.optString("value"); 
						modelField.put("value", fieldValue);
 					}
 				}
 				
 				
				for (int i=0; i<modelNames.length(); i++) {
					String fieldName = (String) modelNames.get(i);
					JSONObject modelField = modelJson.getJSONObject(fieldName);
					System.out.println(String.format("****** field %s = %s", fieldName, modelField));
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 //			try {
 //				JSONObject json = new JSONObject(eventProps);
 //				String epoch = json.optString("epoch");
 //				String recordId = json.optString("recordId");
 //				if (!validStr(epoch)) {
 //					epoch = "1";
 //				}
 //				if (!validStr(recordId)) {
 //					recordId = newId();
 //				}
 //				
 //				Event event = new Event(Integer.valueOf(epoch), recordId);
 //				
 //				JSONObject data = json.getJSONObject("data");
 //				JSONArray names = data.names();
 //				for (int i=0; i<names.length(); i++) {
 //					String key = (String) names.get(i);
 //					String value = (String) data.get(key);
 //					event.addAttribute(key, value);
 //					//System.out.println(String.format("%s = %s", key, value));
 //				}
 //				repository.put(event);
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //			}
 		}		
 		
     }
     
     	
 }
