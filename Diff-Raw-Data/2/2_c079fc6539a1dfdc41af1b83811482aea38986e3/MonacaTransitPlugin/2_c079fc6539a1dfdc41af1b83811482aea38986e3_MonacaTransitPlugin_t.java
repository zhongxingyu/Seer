 package mobi.monaca.framework.plugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.MonacaPageActivity;
 import mobi.monaca.framework.MonacaURI;
 import mobi.monaca.framework.transition.TransitionParams;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Handler;
 import android.util.Log;
 
 
 public class MonacaTransitPlugin extends Plugin {
 	public static final String TAG = MonacaTransitPlugin.class.getSimpleName();
 
     protected Handler handler = new Handler();
 
     protected MonacaPageActivity getMonacaPageActivity() {
         return (MonacaPageActivity) cordova.getActivity();
     }
 
     @Override
     public PluginResult execute(String action, final JSONArray args,
             String callbackId) {
 
 //    	MyLog.v(TAG, "action: " + action);
         // push
         if (action.equals("push") || action.equals("slide") || action.equals("slideLeft")) {
             getMonacaPageActivity().pushPageAsync(buildTransitUrl(args),
                     TransitionParams.from(args.optJSONObject(1), "slideLeft"));
             return new PluginResult(PluginResult.Status.OK);
         }
         
         if (action.equals("slideRight")) {
             getMonacaPageActivity().pushPageAsync(buildTransitUrl(args),
                     TransitionParams.from(args.optJSONObject(1), "slideRight"));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         // modal
         if (action.equals("modal")) {
            getMonacaPageActivity().pushPageAsync(buildTransitUrl(args),
                     TransitionParams.from(args.optJSONObject(1), "modal"));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         // link
         if (action.equals("link")) {
             JSONObject obj = args.optJSONObject(1);
             obj = obj != null ? obj : new JSONObject();
             loadRelativePathAsync(buildTransitUrl(args));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         if (action.equals("pop")) {
             getMonacaPageActivity().popPageAsync(
                     TransitionParams.from(new JSONObject(), "pop"));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         if (action.equals("dismiss")) {
             getMonacaPageActivity().popPageAsync(
                     TransitionParams.from(new JSONObject(), "dismiss"));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         // execute browser
         if (action.equals("browse")) {
             handler.post(new Runnable() {
                 @Override
                 public void run() {
                     Uri uri = Uri.parse(args.optString(0));
                     Intent i = new Intent(Intent.ACTION_VIEW, uri);
                     cordova.getActivity().startActivity(i);
                 }
             });
             return new PluginResult(PluginResult.Status.OK);
         }
 
         // go to home
         if (action.equals("home")) {
             getMonacaPageActivity().goHomeAsync(args.optJSONObject(0));
             return new PluginResult(PluginResult.Status.OK);
         }
 
         if (action.equals("clearPageStack")) {
         	boolean clearAll = args.optBoolean(0, false);
             clearPageStack(clearAll);
             return new PluginResult(PluginResult.Status.OK);
         }
 
         return new PluginResult(Status.INVALID_ACTION);
     }
 	public void loadRelativePathAsync(String relativePath) {
 //		MyLog.v(TAG, "loadRelativePathAsync. relativePath:" + relativePath);
 		final String newUri = getMonacaPageActivity().getCurrentUriWithoutOptions() + "/../" + relativePath;
 //		MyLog.v(TAG, "uri unresolved=" + newUri);
 
 		getMonacaPageActivity().runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				if (newUri.startsWith("file://")) {
 					try {
 						getMonacaPageActivity().loadUri("file://" + new File(newUri.substring(7)).getCanonicalPath(), false);
 //						MyLog.v(TAG, "uri resolved=" + getMonacaPageActivity().getCurrentUriWithoutOptions());
 					} catch (Exception e) {
 						e.printStackTrace();
 						getMonacaPageActivity().loadUri(getMonacaPageActivity().getCurrentUriWithoutOptions(), false);
 					}
 				} else {
 					getMonacaPageActivity().loadUri(getMonacaPageActivity().getCurrentUriWithoutOptions(), false);
 				}
 			}
 		});
 	}
 
     /** build url containing query **/
     protected String buildTransitUrl(JSONArray args) {
        	String transitUrl = new String(args.optString(0));
 
     	// has query parameter
     	if (args.optString(2) != null) {
     		try {
     			Log.d(TAG, "optString(2) : " + args.optString(2));
 				JSONObject queryParamsJson = new JSONObject(args.optString(2));
 				transitUrl = MonacaURI.buildUrlWithQuery(transitUrl, queryParamsJson);
 				Log.d(TAG, "build new url :" + transitUrl);
 			} catch (JSONException e) {
 				// TODO 自動生成された catch ブロック
 				e.printStackTrace();
 			}
     	}
 
     	return transitUrl;
     }
 
     protected void pushPage(String url, TransitionParams params) {
         getMonacaPageActivity().pushPageAsync(url, params);
     }
 
     protected void clearPageStack(boolean clearAll) {
         List<MonacaPageActivity> pages = new ArrayList<MonacaPageActivity>(MonacaApplication.getPages());
     	if (clearAll) {
     		pages = pages.subList(0, pages.size() - 1);
     		Collections.reverse(pages);
 
     		for (MonacaPageActivity page : pages) {
     			page.finish();
     		}
     	} else {
     		if (pages.size() > 1) {
     			MonacaPageActivity previousPage = pages.get(pages.size() - 2);
     			previousPage.finish();
     		}
     	}
     }
 
 }
