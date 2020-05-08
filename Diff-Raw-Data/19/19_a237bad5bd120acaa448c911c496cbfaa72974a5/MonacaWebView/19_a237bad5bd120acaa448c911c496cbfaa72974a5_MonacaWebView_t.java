 package mobi.monaca.framework.view;
 
 import java.io.IOException;
 
 import mobi.monaca.framework.MonacaPageActivity;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.api.LOG;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.AssetManager;
 import android.content.res.XmlResourceParser;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 
 public class MonacaWebView extends CordovaWebView {
 	public static final String TAG = MonacaWebView.class.getSimpleName();
 
 	public static final String INITIALIZATION_REQUEST_URL = "INITIALIZATION";
 	public static final String INITIALIZATION_MADIATOR = "javascript:";
 	public static final String INITIALIZATION_DESCRIPTION = "The connection to the server was unsuccessful.";
 	public static final int INITIALIZATION_ERROR_CODE = -6;
 
 	protected MonacaPageActivity page;
	protected Context context;
 	private boolean notBackButton = true;
 
 	@Deprecated
 	public MonacaWebView(Context context, AttributeSet attrs, int defStyle,
 			boolean privateBrowsing) {
 		super(context, attrs, defStyle, privateBrowsing);
		this.context = context;
 		init();
 	}
 
 	@Deprecated
 	public MonacaWebView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
		this.context = context;
 		init();
 	}
 
 	@Deprecated
 	public MonacaWebView(Context context, AttributeSet attrs) {
 		super(context, attrs);
		this.context = context;
 		init();
 	}
 
 	public MonacaWebView(MonacaPageActivity page) {
 		super(page);
 		this.page = page;
		this.context = page.getContext();
 		init();
 	}
 
 	protected void init() {
 		resumeTimers();
 		loadCordovaConfigurationFromManifest();
 		notBackButton = true;
 	}
 
 	@Override
 	public void goBack() {
 //		MyLog.d(TAG, "called goBack");
 		super.goBack();
 	}
 
 	@Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (page != null &&
				(keyCode == KeyEvent.KEYCODE_BACK && (page.hasBackButtonEventer() || page.hasOnTapBackButtonAction()))) {
 			// if monaca has backbutton handler, do not call cordova backbutton event
 			return true;
 		} else {
 			//to prevent from calling backHistory() by backbutton
 			// TODO find smarter way
 			notBackButton = !(this.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK);
 			boolean supersReturn = super.onKeyUp(keyCode, event);
 			notBackButton = true;
 			return supersReturn;
 		}
 	}
 
 	@Override
 	public void loadUrlIntoView(final String url) {
 		if (url.equals(INITIALIZATION_REQUEST_URL)) {
 			super.loadUrlIntoView(INITIALIZATION_MADIATOR);
 		} else {
 			super.loadUrlIntoView(url);
 		}
 	}
 
 	@Override
 	public boolean backHistory() {
 //		MyLog.d(TAG, "backHistory()");
 		//to prevent from calling backHistory() by backbutton
 		// TODO find smarter way
 		if (notBackButton) {
 			return super.backHistory();
 		} else {
 			notBackButton = true;
 			return false;
 		}
 	}
 
 
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 		MyLog.d(TAG, "onKeyDown");
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 		//	 MonacaPageActivity uses BACK_BUTTON
 			return false;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	public void loadCordovaConfigurationFromManifest() {
         AssetManager am;
         XmlResourceParser parser = null;
 
 		try {
			am = context.createPackageContext(context.getPackageName(), 0).getAssets();
 			parser = am.openXmlResourceParser("AndroidManifest.xml");
 		} catch (NameNotFoundException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 
 		parseCordovaTagFromXml(parser);
 	}
 
 	public void parseCordovaTagFromXml(XmlPullParser parser) {
 
 		if (parser == null) {
 			return;
 		}
 
 		 int eventType;
 		 String tag;
          try {
 			while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
 				tag = parser.getName();
 			     if (eventType == XmlPullParser.START_TAG && "cordova".equals(tag)) {
 			    	 MyLog.d(TAG,"found cordova tag");
 						boolean continues = true;
 
 						while (continues) {
 							switch (eventType) {
 							case XmlPullParser.START_TAG:
 								tag = parser.getName();
 
 								if ("access".equals(tag)) {
 									String origin = parser.getAttributeValue(null, "origin");
 									String subdomains = parser.getAttributeValue(null, "subdomains");
 
 									 if (origin != null) {
 										 addWhiteListEntry(origin, subdomains != null && subdomains.compareToIgnoreCase("true") == 0);
 									 }
 
 									MyLog.d(TAG, "addWhiteList : " +  origin);
 								}
 
 								if ("log".equals(tag)) {
 					                   String level = parser.getAttributeValue(null, "level");
 					                   if (level != null) {
 					                        LOG.setLogLevel(level);
 					                   }
 								}
 								if ("preference".equals(tag)) {
 				                    String name = parser.getAttributeValue(null, "name");
 				                    String value = parser.getAttributeValue(null, "value");
 
 				                    MyLog.d("CordovaLog", "Found preference for " + name + "=" + value);
 
 				                    // Save preferences in Intent
 				                    try {
 				                    	page.getIntent().putExtra(name, value);
 				                    } catch (Exception e) {
 
 				                    }
 				                }
 								break;
 							case XmlPullParser.END_TAG:
 								tag = parser.getName();
 								continues = !"cordova".equals(tag);
 								break;
 							}
 							eventType = parser.next();
 						}
 					}
 			 }
 		} catch (XmlPullParserException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 }
